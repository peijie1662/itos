package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
import java.util.List;
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
import nbct.com.cn.itos.config.AddressEnum;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.TaskLogRowMapper;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.TimerTaskModel;
import util.ConvertUtil;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月28日 下午9:02:04
 */
public class CommonTaskHandler {

	/**
	 * 任务日志
	 */
	public void getTaskLog(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_tasklog where taskId = ? order by opdate";
		JsonArray params = new JsonArray().add(rp.getString("taskId"));
		JdbcHelper.rows(ctx, sql, params, new TaskLogRowMapper());
	}

	/**
	 * 任务操作(MODIFY)
	 */
	public void modifyTask(RoutingContext ctx) {
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
									CommonTask task = CommonTask.from(rs.get(0));
									if (task.getContent().equals(rp.getString("content"))) {
										promise.fail("任务内容没有变化，不需要保存。");
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
				// 2.更新内容
				Function<CommonTask, Future<CommonTask>> savef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(rp.getString("content"))//
								.add(rp.getString("taskId"));
						String sql = "update itos_task set content = ? where taskId = ?";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								// 这里不更新内容，保留原内容作为日志。
								promise.complete(task);
							} else {
								promise.fail("修改任务内容失败");
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
								.add("用户" + rp.getString("oper") + "修改了任务内容。")//
								.add(ConvertUtil.listToStr(task.getHandler()))//
								.add(task.getContent())//
								.add(rp.getString("content"))//
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
				}).setHandler(r -> {
					if (r.succeeded()) {
						String log = DateUtil.curDtStr() + " " + "修改了任务'" + r.result().getAbs() + "'的内容";
						ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
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
	 * 任务操作(PROCESSING,DONE,CANCEL)
	 */
	public void updateTaskStatus(RoutingContext ctx) {
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
									promise.complete(CommonTask.from(rs.get(0)));
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
				// 2.更新状态
				Function<CommonTask, Future<CommonTask>> savef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(rp.getString("status"))//
								.add(rp.getString("taskId"));
						String sql = "update itos_task set status = ? where taskId = ?";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								task.setStatus(TaskStatusEnum.from(rp.getString("status")).get());
								promise.complete(task);
							} else {
								promise.fail("更新任务状态失败");
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
								.add("用户" + rp.getString("oper") + "将任务状态置为" + rp.getString("status"))//
								.add(ConvertUtil.listToStr(task.getHandler()))//
								.add(task.getAbs())//
								.add(rp.getString("remark"))//
								.add(rp.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "handler,abstract,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?)";
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
				// 4.组合任务
				Function<CommonTask, Future<CommonTask>> composef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(task.getTaskId());// 传入参数
						JsonArray outputs = new JsonArray()//
								.addNull()// 传入
								.add("VARCHAR")// flag
								.add("VARCHAR")// errMsg
								.add("VARCHAR");// outMsg
						conn.callWithParams("{call itos.p_compose_task(?,?,?,?)}", params, outputs, r -> {
							if (r.succeeded()) {
								JsonArray j = r.result().getOutput();
								Boolean flag = "0".equals(j.getString(1));// flag
								//int newTask = j.getInteger(3);// 新建下阶段任务数量
								if (flag) {
									promise.complete(task);
								} else {
									promise.fail("组合任务过程内部出错。");
								}
							} else {
								r.cause().printStackTrace();
								promise.fail("调用组合任务的出错。");
							}
						});
					});
					return f;
				};
				// 5.EXECUTE
				getf.get().compose(r -> {
					return savef.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).compose(r -> {
					return composef.apply(r);
				}).setHandler(r -> {
					if (r.succeeded()) {
						String log = DateUtil.curDtStr() + " " + "更新任务'" + r.result().getAbs() + "'的状态为"
								+ rp.getString("status");
						ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
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
	 * 临时从模版触发任务(ONCE)<br>
	 * 传入参数:modelId,planDt,userId
	 */
	public void saveOnceTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读取模版信息
				Supplier<Future<CommonTask>> crtf = () -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(rp.getString("modelId"));
						String sql = "select * from itos_taskmodel where modelId = ?";
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								List<JsonObject> rs = r.result().getRows();
								if (rs.size() > 0) {
									try {
										TimerTaskModel model = TimerTaskModel.from(rs.get(0));
										CommonTask task = CommonTask.from(model,
												DateUtil.utcToLocalEx(rp.getString("planDt")));
										promise.complete(task);
									} catch (Exception e) {
										promise.fail(e.getMessage());
									}
								} else {
									promise.fail("模版中没有这个modelId");
								}
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.保存任务信息
				Function<CommonTask, Future<CommonTask>> savef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						String sql = "insert into itos_task(taskId,category,abstract,modelId," + //
								" status,content,planDt,invalid,taskicon,oper,opdate) " + //
								" values(?,?,?,?,?,?,?,?,?,?,?,?)";
						JsonArray params = new JsonArray()//
								.add(task.getTaskId())//
								.add(task.getCategory().getValue())//
								.add(task.getAbs())//
								.add(task.getModelId())// 通过modelId关联到dispatchClient
								.add("CHECKIN")//
								.add(task.getContent())//
								.add(DateUtil.localToUtcStr(task.getPlanDt()))//
								.add("N")//
								.add(task.getTaskIcon())//
								.add(rp.getString("userId"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(task);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存任务出错。");
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
								.add("用户" + rp.getString("userId") + "临时从模版生成新任务'" + task.getAbs() + "'")//
								.add(ConvertUtil.listToStr(task.getHandler()))//
								.add(task.getAbs())//
								.add(rp.getString("userId"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "handler,abstract,oper,opDate) values(?,?,?,?,?,?,?,?)";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(task);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存临时从模版生成新任务日志出错。");
							}
						});
					});
					return f;
				};
				// 4.执行
				crtf.get().compose(r -> {
					return savef.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).setHandler(r -> {
					if (r.succeeded()) {
						String log = DateUtil.curDtStr() + "用户" + rp.getString("oper") + "临时从模版生成新任务'"
								+ r.result().getAbs() + "'";
						ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
						res.end(OK());
					} else {
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});

			}
		});
	}

}
