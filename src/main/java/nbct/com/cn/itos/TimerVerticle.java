package nbct.com.cn.itos;

import static nbct.com.cn.itos.ConfigVerticle.CONFIG;
import static nbct.com.cn.itos.ConfigVerticle.SC;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.handler.SystemTaskHandler;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.TimerTaskModel;
import util.ConvertUtil;
import util.DateUtil;
import util.ModelUtil;

/**
 * 自动任务
 * 
 * @author PJ
 * @version 创建时间：2019年12月23日 下午1:47:07
 */
public class TimerVerticle extends AbstractVerticle {

	public static Logger log = LogManager.getLogger(TimerVerticle.class);

	/**
	 * 扫描中间值
	 */
	class ScanTempResult {
		/**
		 * 需要生成任务的模版
		 */
		public List<TimerTaskModel> models;

		/**
		 * 生成的任务
		 */
		public List<CommonTask> tasks;

	}

	/**
	 * 扫描模版，生成任务
	 */
	private void createTask() {
		LocalDateTime curDt = LocalDateTime.now();
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.创建新任务
				Supplier<Future<ScanTempResult>> loadf = () -> {
					Future<ScanTempResult> f = Future.future(promise -> {
						String sql = "select * from itos_taskmodel where invalid = 'N' and category <> 'COMPOSE'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								ScanTempResult tr = new ScanTempResult();
								tr.models = r.result().getRows().stream().map(row -> {
									try {
										return new TimerTaskModel().from(row);
									} catch (Exception e) {
										log.error("TASK-01::" + row.getString("ABSTRACT"), e);
										return null;
									}
								}).filter(model -> {
									return (model != null) && ModelUtil.couldCreateTask(model, curDt);
								}).collect(Collectors.toList());
								promise.complete(tr);
							} else {
								promise.fail("读取模版列表出错。");
							}
						});
					});
					return f;
				};
				// 2.保存新任务
				Function<ScanTempResult, Future<ScanTempResult>> taskf = (tr) -> {
					Future<ScanTempResult> f = Future.future(promise -> {
						tr.tasks = new ArrayList<CommonTask>();
						if (tr.models.size() == 0) {
							promise.complete(tr);
							return;
						}
						tr.models.forEach(model -> {
							if (model.isCompensate()) {
								tr.tasks.addAll(CommonTask.fromCompensate(model, curDt));
							} else {
								tr.tasks.addAll(CommonTask.fromNoCompensate(model, curDt));
							}
						});
						if (tr.tasks.size() == 0) {
							promise.complete(tr);
							return;
						}
						List<JsonArray> params = tr.tasks.stream().map(task -> {
							TimerTaskModel model = tr.models.stream()
									.filter(item -> item.getModelId().equals(task.getModelId())).findAny().get();
							JsonObject o = new JsonObject();
							o.put("taskId", task.getTaskId());
							o.put("category", task.getCategory());
							o.put("modelId", task.getModelId());
							o.put("abs", task.getAbs());
							o.put("status", task.getStatus());
							o.put("content", task.getContent());
							o.put("planDt", DateUtil.toDateTimeStr(task.getPlanDt()));
							o.put("opDate", DateUtil.toDateTimeStr(curDt));
							o.put("taskIcon", task.getTaskIcon());
							o.put("expiredTime", DateUtil.toDateTimeStr(task.getExpiredTime()));
							o.put("epiredCallback", task.getCallback() != null ? task.getCallback().getValue() : "");
							o.put("epiredNotify", task.getNotify() != null ? task.getNotify().stream().map(item -> {
								return item.getValue();
							}).collect(Collectors.joining(",")) : "");
							o.put("logId", UUID.randomUUID().toString());
							o.put("scanDate", DateUtil.toDateTimeStr(model.getScanDate()));
							JsonArray a = new JsonArray();
							a.add(o.encodePrettily());
							return a;
						}).collect(Collectors.toList());
						String func = "{call itos.p_task_add(?)}";
						List<JsonArray> outputs = params.stream().map(item -> {
							JsonArray output = new JsonArray().addNull();
							return output;
						}).collect(Collectors.toList());
						conn.batchCallableWithParams(func, params, outputs, r -> {
							if (r.succeeded()) {
								promise.complete(tr);
							} else {
								r.cause().printStackTrace();
								promise.fail(r.cause().getMessage());
							}
						});
					});
					return f;
				};
				// 3.执行
				loadf.get().compose(r -> {
					return taskf.apply(r);
				}).onComplete(r -> {
					if (r.failed()) {
						r.cause().printStackTrace();
						log.error("TASK-02::", r.cause());
					} else {
						r.result().tasks.forEach(task -> {
							log.info("TASK-03::" + task.getAbs() + "任务自动生成。");
						});
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 定时注册
	 */
	private void registe() {
		JsonObject provider = CONFIG.getJsonObject("provider");
		JsonObject registerUrl = CONFIG.getJsonObject("registerUrl");
		WebClient wc = WebClient.create(vertx,
				new WebClientOptions().setIdleTimeout(2).setConnectTimeout(2000).setMaxWaitQueueSize(5));
		try {
			provider.put("ip", InetAddress.getLocalHost().getHostAddress());
			wc.post(registerUrl.getInteger("port"), registerUrl.getString("ip"), registerUrl.getString("url"))
					.timeout(1000).sendJsonObject(provider, ar -> {
						if (!ar.succeeded()) {
							log.warn("REGISTE-01::", ar.cause());
						}
					});
		} catch (Exception e) {
			log.warn("REGISTE-02::", e);
		}
	}

	/**
	 * 扫描所有未完成任务，并执行超期回调<br>
	 */
	private void expired() {
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读数据
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status not in ('DONE','CANCEL','FAIL') and " + //
						" expiredTime is not null and expiredtime < sysdate and expiredcallback <> 'NONE' " + //
						" and nvl(executedcallback,' ') <> 'Y'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								CommonTask ct = new CommonTask();
								List<CommonTask> list = r.result().getRows().stream().map(item -> {
									return ct.from(item);
								}).collect(Collectors.toList());
								promise.complete(list);
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.更新状态
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = list -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "update itos_task set status = ?,executedcallback = 'Y' where taskId = ? ";
						List<JsonArray> params = list.stream().map(item -> {
							return new JsonArray()//
									.add(item.getCallback().getValue())// 这里回调代码与状态代码相同，所以可以直接赋值。
									.add(item.getTaskId());
						}).collect(Collectors.toList());
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(list);
							} else {
								r.cause().printStackTrace();
								promise.fail("执行系统任务超期回调出错。");
							}
						});
					});
					return f;
				};
				// 3.超期通知
				Function<List<CommonTask>, Future<List<CommonTask>>> nf = list -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						list.stream().forEach(task -> {
							task.getNotify().forEach(item -> {
								switch (item) {
								case SMS:
									DeliveryOptions options = new DeliveryOptions();
									options.addHeader("CATEGORY", "EXPIRED");
									String planDt = task.getPlanDt()
											.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
									vertx.eventBus().send(SceneEnum.SMS.addr(), new JsonObject()//
											.put("abs", task.getAbs())//
											.put("planDt", planDt)//
											.put("expiredStatus", task.getCallback().getValue())//
											.put("expiredDesc", task.getCallback().getDesc()), options);
									break;
								case BIGHORN:
									break;
								case ITOSMES:
									break;
								default:
									break;
								}
							});
						});
						promise.complete(list);
					});
					return f;
				};
				// 4.记录日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = tasks -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						tasks.forEach(task -> {
							// String msg = DateUtil.curDtStr() + " 系统检测到任务'" + task.getAbs() + "超期," + //
							// "任务状态自动转为'" + task.getCallback().getValue();
							// MsgUtil.mixLC(vertx, msg, task.getComposeId());
						});
						List<JsonArray> params = tasks.stream().map(task -> {
							return new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
									.add(task.getCategory().getValue())//
									.add(task.getModelId())//
									.add(task.getStatus().getValue())//
									.add("任务超期，系统将任务状态置为" + task.getCallback().getValue())//
									.add(ConvertUtil.listToStr(task.getHandler()))//
									.add(task.getAbs())//
									.add("")// remark
									.add("SYS")//
									.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						}).collect(Collectors.toList());
						String sql = "insert into itos_tasklog(logId,taskId,category,modelId,status,statusdesc,"//
								+ "handler,abstract,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(tasks);
							} else {
								promise.fail("保存系统任务日志出错。");
							}
						});
					});
					return f;
				};
				// 5.组合任务
				Function<List<CommonTask>, Future<List<CommonTask>>> composef = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String param = tasks.stream().filter(item -> {
							return Objects.nonNull(item.getComposeId());
						}).map(item -> {
							return item.getTaskId() + "^" + "SYS";
						}).collect(Collectors.joining(","));
						if (ConvertUtil.emptyOrNull(param)) {
							promise.complete(tasks);
						} else {
							JsonArray params = new JsonArray().add(param);// c传入参数
							JsonArray outputs = new JsonArray()//
									.addNull()// c传入
									.add("VARCHAR")// flag
									.add("VARCHAR")// errMsg
									.add("VARCHAR");// outMsg
							conn.callWithParams("{call itos.p_compose_task_next(?,?,?,?)}", params, outputs, r -> {
								if (r.succeeded()) {
									JsonArray j = r.result().getOutput();
									Boolean flag = "0".equals(j.getString(1));// flag
									// String newTask = j.getString(3);// c新建下阶段任务数量
									if (flag) {
										// MsgUtil.mixLC(vertx, newTask, "SOMEID");// c这时的值是批量值，没什么意义。
										promise.complete(tasks);
									} else {
										promise.fail("组合任务过程内部出错:" + j.getString(2));
									}
								} else {
									promise.fail("调用组合任务的出错。");
								}
							});
						}
					});
					return f;
				};
				// 6.执行
				loadf.get().compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return nf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).compose(r -> {
					return composef.apply(r);
				}).onComplete(r -> {
					if (r.failed()) {
						log.error("EXPIRED-01::", r.cause());
					} else {
						r.result().forEach(task -> {
							log.info("EXPIRED-02::" + task.getAbs() + "任务自动过期处理。");
						});
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 1秒循环任务
	 */
	private void loopOneSecond(SystemTaskHandler handler) {
		// 1.按模版生成任务
		createTask();
	}

	/**
	 * 5秒循环任务
	 */
	private void loopFiveSecond(SystemTaskHandler handler) {
		// 1.检查任务过期
		expired();
		// 2.注册本服务
		registe();
		// 3.执行定时任务
		handler.timerTask();
		// 4.执行公告任务
		handler.announceTask();
		// 5.执行比对任务
		handler.compareTask();
		// 6.执行清理任务
		handler.housekeep();
		// 7.EDI进度检查任务
		handler.ediProgress();
	}

	@Override
	public void start() throws Exception {
		SystemTaskHandler systemTaskHandler = new SystemTaskHandler(vertx);
		vertx.setPeriodic(5000, timerId -> {
			vertx.executeBlocking(future -> {
				loopFiveSecond(systemTaskHandler);
				future.complete();
			}, false, null);
		});
		vertx.setPeriodic(1000, timerId -> {
			vertx.executeBlocking(future -> {
				loopOneSecond(systemTaskHandler);
				future.complete();
			}, false, null);
		});
	}

}
