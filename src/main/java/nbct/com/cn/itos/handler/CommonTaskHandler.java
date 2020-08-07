package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.ConfigVerticle.SC;
import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.CommonTaskLog;
import nbct.com.cn.itos.model.TimerTaskModel;
import util.ConvertUtil;
import util.DateUtil;
import util.ModelUtil;
import util.MsgUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月28日 下午9:02:04
 */
public class CommonTaskHandler {

	/**
	 * 单个任务
	 */
	public void getTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_task where taskId = ?";
		JsonArray params = new JsonArray().add(rp.getString("taskId"));
		JdbcHelper.rows(ctx, sql, params, new CommonTask());
	}

	/**
	 * 任务日志
	 */
	public void getTaskLog(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from (select a.*,decode(status,'CHECKIN',1,'PROCESSING',2,3) as seq " + //
				"from itos_tasklog a where taskId = ? ) order by seq,opdate";
		JsonArray params = new JsonArray().add(rp.getString("taskId"));
		JdbcHelper.rows(ctx, sql, params, new CommonTaskLog());
	}

	/**
	 * 任务操作(MODIFY)
	 */
	public void modifyTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SC.getConnection(cr -> {
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
								// c这里不更新内容，保留原内容作为日志。
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
								.add(task.getCategory().getValue())//
								.add(task.getStatus().getValue())//
								.add("用户" + rp.getString("oper") + "修改了任务内容。")//
								.add(ConvertUtil.listToStr(task.getHandler()))//
								.add(task.getContent())//
								.add(rp.getString("content"))//
								.add(task.getAbs())//
								.add(rp.getString("remark"))//
								.add(rp.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,category,status,statusdesc," + //
						"handler,oldcontent,newcontent,abstract,remark,oper,opDate) " + //
						"values(?,?,?,?,?,?,?,?,?,?,?,?)";
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
						String msg = DateUtil.curDtStr() + " " + "修改了任务'" + r.result().getAbs() + "'的内容";
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
	 *
	 * 任务操作(PROCESSING,DONE,CANCEL,FAIL)<br>
	 * 参数 {taskId:"...",status:"...",oper:"...",remark:"..."}
	 */
	public void updateTaskStatus(RoutingContext ctx) {
		// TODO 改成存储过程。
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		JsonObject rp;
		try {
			rp = ctx.getBodyAsJson();
		} catch (Exception e) {
			res.end(Err("传入参数Json格式错误"));
			return;
		}
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.找到对应Task
				Supplier<Future<CommonTask>> getf = () -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(rp.getString("taskId"));
						String sql = "select * from itos_task where taskId = ?";
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								List<JsonObject> rs = r.result().getRows();
								if (rs.size() > 0) {
									CommonTask task = new CommonTask().from(rs.get(0));
									if (CategoryEnum.BROADCAST == task.getCategory()) {
										promise.fail("广播任务不能改变状态");
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
				// 2.更新状态
				Function<CommonTask, Future<CommonTask>> savef = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						String status = rp.getString("status");
						JsonArray params = new JsonArray()//
								.add(status)//
								.add(rp.getString("taskId"));
						if (!task.getCategory().eq("COMMON") && task.getStatus().isAfterOrParalleling(status)) {
							promise.fail("非普通任务的状态更新必须遵循顺序。");
						} else {
							String sql = "update itos_task set status = ? where taskId = ?";
							conn.updateWithParams(sql, params, r -> {
								if (r.succeeded()) {
									task.setStatus(TaskStatusEnum.from(rp.getString("status")).get());
									promise.complete(task);
								} else {
									promise.fail("更新任务状态失败");
								}
							});
						}
					});
					return f;
				};
				// 3.保存日志
				Function<CommonTask, Future<CommonTask>> logf = (task) -> {
					Future<CommonTask> f = Future.future(promise -> {
						JsonArray params = new JsonArray()//
								.add(UUID.randomUUID().toString())//
								.add(task.getTaskId())//
								.add(task.getCategory().getValue())//
								.add(task.getModelId())//
								.add(task.getStatus().getValue())//
								.add("用户" + rp.getString("oper") + "将任务状态置为" + rp.getString("status"))//
								.add(ConvertUtil.listToStr(task.getHandler()))//
								.add(task.getAbs())//
								.add(rp.getString("remark"))//
								.add(rp.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,category,modelId,status,statusdesc,"//
								+ "handler,abstract,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?,?,?)";
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
						if (task.getComposeId() == null) {
							promise.complete(task);// 非组合任务路过
						} else {
							JsonArray params = new JsonArray().add(task.getTaskId() + "^" + "SYS");// 传入参数
							JsonArray outputs = new JsonArray()//
									.addNull()// 传入
									.add("VARCHAR")// flag
									.add("VARCHAR")// errMsg
									.add("VARCHAR");// outMsg
							conn.callWithParams("{call itos.p_compose_task_next(?,?,?,?)}", params, outputs, r -> {
								if (r.succeeded()) {
									JsonArray j = r.result().getOutput();
									Boolean flag = "0".equals(j.getString(1));// flag
									String newTask = j.getString(3);// c新建下阶段任务数量
									MsgUtil.mixLC(ctx, Integer.parseInt(newTask), task.getComposeId());
									if (flag) {
										promise.complete(task);
									} else {
										promise.fail("组合任务过程内部出错:" + j.getString(2));
									}
								} else {
									r.cause().printStackTrace();
									promise.fail("调用组合任务的出错。");
								}
							});
						}
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
				}).onComplete(r -> {
					if (r.succeeded()) {
						String msg = DateUtil.curDtStr() + " " + "更新任务'" + r.result().getAbs() + "'的状态为"
								+ rp.getString("status");
						MsgUtil.mixLC(ctx, msg, r.result().getComposeId());
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
		
		
		System.out.println("11111111111");
		
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读取模版信息
				Supplier<Future<List<CommonTask>>> crtf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(rp.getString("modelId"));
						String sql = "select * from itos_taskmodel where modelId = ?";
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								List<JsonObject> rs = r.result().getRows();
								if (rs.size() > 0) {
									try {
										TimerTaskModel model = new TimerTaskModel().from(rs.get(0));
										List<CommonTask> tasks = CommonTask.from(model,
												DateUtil.utcToLocalEx(rp.getString("planDt")));
										promise.complete(tasks);
									} catch (Exception e) {
										e.printStackTrace();
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
				Function<List<CommonTask>, Future<List<CommonTask>>> savef = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
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
									.add(DateUtil.localToUtcStr(LocalDateTime.now()))//
									.add(DateUtil.localToUtcStr(task.getExpiredTime()))//
									.add(Objects.nonNull(task.getCallback()) ? task.getCallback().getValue() : "")//
									.add(Objects.nonNull(task.getNotify()) ? task.getNotify().stream().map(item -> {
										return item.getValue();
									}).collect(Collectors.joining(",")) : "")//
									.add("N")//
									.add("N"));
						});
						String sql = "insert into itos_task(taskId,category,abstract,modelId," + //
						" status,content,planDt,invalid,taskicon,oper,opdate," + //
						" expiredTime,expiredcallback,expirednotify,executedcallback,executednotify) " + //
						" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(tasks);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存任务出错。");
							}
						});
					});
					return f;
				};
				// 3.保存日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
									.add(task.getCategory().getValue())//
									.add(task.getModelId())//
									.add(task.getStatus().getValue())//
									.add("用户" + rp.getString("userId") + "临时从模版生成新任务'" + task.getAbs() + "'")//
									.add(ConvertUtil.listToStr(task.getHandler()))//
									.add(task.getAbs())//
									.add(rp.getString("userId"))//
									.add(DateUtil.localToUtcStr(LocalDateTime.now())));
						});
						String sql = "insert into itos_tasklog(logId,taskId,category,modelId,status,statusdesc,"//
								+ "handler,abstract,oper,opDate) values(?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(tasks);
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
				}).onComplete(r -> {
					if (r.succeeded()) {
						r.result().forEach(task -> {
							String msg = DateUtil.curDtStr() + "用户" + rp.getString("userId") + "临时从模版生成新任务'"
									+ task.getAbs() + "'";
							MsgUtil.mixLC(ctx, msg, task.getComposeId());
						});
						res.end(OK());
					} else {
						r.cause().printStackTrace();
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 在时间周期内生成该模版任务
	 */
	public void savePeriodTask(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		final String modelId = rp.getString("modelId");
		final LocalDateTime bgDt = DateUtil.utcToLocalExNoExp(rp.getString("bgDt"));
		final LocalDateTime edDt = DateUtil.utcToLocalExNoExp(rp.getString("edDt"));
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读取模版信息
				Supplier<Future<TimerTaskModel>> loadf = () -> {
					Future<TimerTaskModel> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(modelId);
						String sql = "select * from itos_taskmodel where modelId = ?";
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								List<JsonObject> rs = r.result().getRows();
								if (rs.size() > 0) {
									try {
										TimerTaskModel model = new TimerTaskModel().from(rs.get(0));
										promise.complete(model);
									} catch (Exception e) {
										e.printStackTrace();
										promise.fail("模版数据实例化错误");
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
				// 2.时间循环
				Function<TimerTaskModel, Future<List<CommonTask>>> loopf = (model) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						List<CommonTask> tasks = new ArrayList<CommonTask>();
						// 1.清除模版扫描时间
						model.setScanDate(null);
						// 2.模拟时间流逝
						LocalDateTime at = LocalDateTime.from(bgDt);
						while (at.isBefore(edDt)) {
							if (ModelUtil.couldCreateTask(model, at)) {
								model.setScanDate(at);
								tasks.addAll(CommonTask.fromAt(model, at));
							}
							at = at.plusSeconds(1);
						}
						promise.complete(tasks);
					});
					return f;
				};
				// 3.保存任务
				Function<List<CommonTask>, Future<List<CommonTask>>> savef = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
									.add(task.getTaskId())//
									.add(task.getCategory()).add(task.getStatus())//
									.add(task.getAbs())//
									.add(task.getContent())//
									.add(DateUtil.localToUtcStr(task.getPlanDt()))//
									.add(task.getModelId())//
									.add("N")//
									.add(task.getTaskIcon())//
									.add("SYS")//
									.add(DateUtil.localToUtcStr(LocalDateTime.now()))//
									.add(DateUtil.localToUtcStr(task.getExpiredTime()))//
									.add(Objects.nonNull(task.getCallback()) ? task.getCallback().getValue() : "")//
									.add(Objects.nonNull(task.getNotify()) ? task.getNotify().stream().map(item -> {
										return item.getValue();
									}).collect(Collectors.joining(",")) : "")//
									.add("N")//
									.add("N"));
						});
						String sql = "insert into itos_task(taskId,category,status,abstract,content,plandt,modelId,"//
								+ "invalid,taskicon,oper,opDate,expiredTime,expiredCallback,expiredNotify,"//
								+ "executedCallback,executedNotify) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(tasks);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存系统任务出错。");
							}
						});
					});
					return f;
				};
				// 4.保存日志
				Function<List<CommonTask>, Future<String>> logf = (tasks) -> {
					Future<String> f = Future.future(promise -> {
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
									.add(task.getCategory().getValue())//
									.add(TaskStatusEnum.CHECKIN.getValue())//
									.add(String.format("系统按照任务模版%s生成任务。", task.getAbs()))//
									.add("")// 待认领
									.add("")// 原内容
									.add("")// 新内容
									.add(task.getModelId())//
									.add(task.getAbs())//
									.add("SYS")//
									.add(DateUtil.localToUtcStr(LocalDateTime.now())));//
						});
						String sql = "insert into itos_tasklog(logId,taskId,category,status,statusdesc,"//
								+ " handler,oldcontent,newcontent,modelId,abstract,oper,opdate) "//
								+ " values(?,?,?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete("扫描任务模版完毕。");
							} else {
								r.cause().printStackTrace();
								promise.fail("保存系统任务日志出错。");
							}
						});
					});
					return f;
				};
				// 5.执行
				loadf.get().compose(r -> {
					return loopf.apply(r);
				}).compose(r -> {
					return savef.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.succeeded()) {
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
