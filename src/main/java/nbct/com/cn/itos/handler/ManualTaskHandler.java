package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.DeliverRepair;
import util.ConvertUtil;
import util.DateUtil;
import util.MsgUtil;

/**
 * @author PJ
 * @version 创建时间：2020年1月17日 上午8:54:23
 */
public class ManualTaskHandler {

	/**
	 * 人工执行任务列表
	 */
	public void getManualTaskList(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray range = rp.getJsonArray("dateRange");
		boolean paramValid = Objects.nonNull(range) && range.size() == 2;
		String startDt = paramValid ? DateUtil.getDBDateBeginStr(range.getString(0)) : DateUtil.getDBDateBeginStr(null);
		String endDt = paramValid ? DateUtil.getDBDateEndStr(range.getString(1)) : DateUtil.getDBDateEndStr(null);
		String sql = "select * from itos_task where category in (?) " + // 1.普通类型
				" and invalid = 'N' " + // 2.有效
				" and planDt >=" + startDt + " and planDt <=" + endDt + // 3.时间范围
				" and composeId is null " + // 4.不是组合任务中的子任务
				" order by opdate desc";
		JsonArray params = new JsonArray();
		params.add(CategoryEnum.COMMON.getValue());
		JdbcHelper.rows(ctx, sql, params, new CommonTask());
	}

	/**
	 * 保存人工任务
	 */
	public void saveManualTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.保存任务
				Supplier<Future<JsonObject>> savef = () -> {
					Future<JsonObject> f = Future.future(promise -> {
						String sql = "insert into itos_task(taskId,category,abstract,phone,location,customer," + //
						"status,content,handler,invalid,taskicon,plandt,oper,opdate) " + //
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						JsonArray params = new JsonArray();
						String taskId = rp.getString("taskId");
						String oper = rp.getString("oper");
						String handler = rp.getString("handler");
						String abs = rp.getString("abstract");
						params.add(taskId);
						params.add(CategoryEnum.COMMON.getValue());
						params.add(abs);
						params.add(rp.getString("phone"));
						params.add(rp.getString("location"));
						params.add(rp.getString("customer"));
						params.add("CHECKIN");
						params.add(rp.getString("content"));
						params.add(handler);
						params.add("N");
						params.add(rp.getString("taskIcon"));
						params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						params.add(oper);
						params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(new JsonObject()//
										.put("taskId", taskId)//
										.put("oper", oper).put("handler", handler).put("abstract", abs));
							} else {
								promise.fail("保存任务出错。");
								r.cause().printStackTrace();
							}
						});
					});
					return f;
				};
				// 2.保存日志
				Function<JsonObject, Future<JsonObject>> logf = (JsonObject j) -> {
					Future<JsonObject> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(UUID.randomUUID().toString())//
								.add(j.getString("taskId"))//
								.add(TaskStatusEnum.CHECKIN.getValue())//
								.add("用户" + j.getString("oper") + "登记了该任务。")//
								.add(j.getString("handler"))//
								.add(j.getString("abstract"))//
								.add(j.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "handler,abstract,oper,opDate) values(?,?,?,?,?,?,?,?)";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(j);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存新建任务的日志出错。");
							}
						});
					});
					return f;
				};
				// 3.执行
				savef.get().compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.succeeded()) {
						String msg = DateUtil.curDtStr() + " " + "登记任务'" + rp.getString("abstract") + "'";
						MsgUtil.sysLog(ctx, msg);
						res.end(OK(r.result()));
					} else {
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 任务操作(SWAP)
	 */
	public void swapTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.找到对应Task
				Supplier<Future<CommonTask>> getf = () -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(rp.getString("taskId"));
						String sql = "select * from itos_task where taskId = ?";
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								List<JsonObject> rs = r.result().getRows();
								if (rs.size() > 0) {
									CommonTask task = new CommonTask().from(rs.get(0));
									if (ConvertUtil.listToStr(task.getHandler()).equals(rp.getString("handler"))) {
										promise.fail("处理人员没有变化，不需要保存。");
									} else {
										promise.complete(task);
									}
								} else {
									promise.fail("任务表中找不到这个TaskId");
								}
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.更新handler
				Function<CommonTask, Future<CommonTask>> savef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(rp.getString("handler"))//
								.add(rp.getString("taskId"));
						String sql = "update itos_task set handler = ? where taskId = ?";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								// 2.1这里不更新handler，保留原handler作为日志。
								promise.complete(task);
							} else {
								promise.fail("修改任务处理人失败");
							}
						});
					});
					return f;
				};
				// 3.保存日志
				Function<CommonTask, Future<CommonTask>> logf = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(UUID.randomUUID().toString())//
								.add(task.getTaskId())//
								.add(task.getStatus().getValue())//
								.add("用户" + rp.getString("oper") + "修改了任务处理人员。")//
								.add(rp.getString("handler"))//
								.add(ConvertUtil.listToStr(task.getHandler()))// 修改前处理人员
								.add(rp.getString("handler"))// 修改后处理人员
								.add(task.getAbs())//
								.add(rp.getString("remark"))//
								.add(rp.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "handler,oldcontent,newcontent,abstract,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?,?,?)";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(task);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存更新任务的日志出错。");
							}
						});
					});
					return f;
				};
				// 4.EXECUTE
				getf.get().compose(r -> {
					return savef.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.succeeded()) {
						String msg = DateUtil.curDtStr() + " " + "修改了任务'" + r.result().getAbs() + "'的处理人员";
						MsgUtil.sysLog(ctx, msg);
						res.end(OK());
					} else {
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 新委外记录
	 */
	public void saveDeliverRepair(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(UUID.randomUUID().toString())//
				.add(rp.getString("taskId"))//
				.add(rp.getString("deliverDate"))//
				.add(rp.getString("receiptDate"))//
				.add(rp.getString("invoiceNumber"))//
				.add(rp.getString("amount"))//
				.add(rp.getString("remark"))//
				.add(rp.getString("oper"))//
				.add(DateUtil.localToUtcStr(LocalDateTime.now()));
		String sql = "insert into itos_deliver_repair(drId,taskId,deliverDate,receiptDate," + //
				" invoiceNumber,amount,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?)";
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 修改委外记录
	 */
	public void updateDeliverRepair(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(rp.getString("deliverDate"))//
				.add(rp.getString("receiptDate"))//
				.add(rp.getString("invoiceNumber"))//
				.add(rp.getString("amount"))//
				.add(rp.getString("remark"))//
				.add(rp.getString("drId"));
		String sql = "update itos_deliver_repair set deliverDate = ?,receiptDate = ?," + //
				"invoiceNumber = ?,amount = ?,remark = ? where drId = ?";
		JdbcHelper.update(ctx, sql, params);
	}
	
	/**
	 * 删除委外记录
	 */
	public void deleteDeliverRepair(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(rp.getString("drId"));
		String sql = "delete itos_deliver_repair where drId = ?";
		JdbcHelper.update(ctx, sql, params);		
	}
	
	/**
	 * 任务委外列表
	 */
	public void getDeliverRepair(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_deliver_repair where taskId = ? order by opDate";
		JsonArray params = new JsonArray().add(rp.getString("taskId"));
		JdbcHelper.rows(ctx, sql, params, new DeliverRepair());
	}

}
