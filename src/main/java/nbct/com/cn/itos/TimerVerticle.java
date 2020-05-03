package nbct.com.cn.itos;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.CycleEnum;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.model.AppInfo;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.TimerTaskModel;
import util.ConvertUtil;
import util.DateUtil;
import util.MsgUtil;

/**
 * 自动任务
 * 
 * @author PJ
 * @version 创建时间：2019年12月23日 下午1:47:07
 */
public class TimerVerticle extends AbstractVerticle {

	public static Logger logger = Logger.getLogger(TimerVerticle.class);

	/**
	 * 扫描模版，生成任务
	 */
	private void task() {
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读数据
				Supplier<Future<List<TimerTaskModel>>> loadf = () -> {
					Future<List<TimerTaskModel>> f = Future.future(promise -> {
						String sql = "select * from itos_taskmodel where invalid = 'N' and category <> 'COMPOSE' "//
								+ "order by category,opdate";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								List<TimerTaskModel> models = r.result().getRows().stream().map(row -> {
									try {
										return new TimerTaskModel().from(row);
									} catch (Exception e) {
										e.printStackTrace();
										throw new RuntimeException(e.getMessage());
									}
								}).filter(model -> {
									boolean valid = (!model.getCycle().eq("NONE")) && model.getScanDate() == null;
									if (model.getScanDate() != null) {
										LocalDateTime ct = LocalDateTime.now();
										LocalDate cd = ct.toLocalDate();
										LocalDateTime mt = model.getScanDate();
										LocalDate md = mt.toLocalDate();
										CycleEnum mc = model.getCycle();
										// 1.扫描每日任务,当前日期>标记时间的日期，就需要生成新任务。
										valid = valid || (mc == CycleEnum.PERDAY && md.isBefore(cd));
										// 2.扫描每周任务，当前日期的年+第几周>标记时间的年+第几周，就需要生成新任务。
										int cw = cd.getYear() + cd.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
										int rw = md.getYear() + md.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
										valid = valid || (mc == CycleEnum.PERWEEK && cw > rw);
										// 3.扫描每月任务，当前日期的年+第几月>标记时间的年+第几月，就需要生成新任务。
										int cm = cd.getYear() + cd.getMonthValue();
										int rm = md.getYear() + md.getMonthValue();
										valid = valid || (mc == CycleEnum.PERMONTH && cm > rm);
										// 4.扫描循环任务，当前时间-间隔时间(秒)>标记时间，就需要生成新任务。
										valid = valid || (mc == CycleEnum.CIRCULAR && mt
												.isBefore(ct.minusSeconds(Integer.parseInt(model.getPlanDates()))));
									}
									// 5.循环任务不能超过开始时间
									if (valid && model.getCycle() == CycleEnum.CIRCULAR) {
										valid = Objects.isNull(model.getStartDate())
												|| model.getStartDate().isBefore(LocalDateTime.now());
									}
									return valid;
								}).collect(Collectors.toList());
								promise.complete(models);
							} else {
								promise.fail("读取模版列表出错。");
							}
						});
					});
					return f;
				};
				// 2.更新标记
				Function<List<TimerTaskModel>, Future<List<TimerTaskModel>>> updatef = (
						List<TimerTaskModel> models) -> {
					Future<List<TimerTaskModel>> f = Future.future(promise -> {
						String sql = "update itos_taskmodel set scandate = ? where modelId = ?";
						List<JsonArray> params = models.stream().map(model -> {
							return new JsonArray()//
									.add(DateUtil.localToUtcStr(LocalDateTime.now()))//
									.add(model.getModelId());//
						}).collect(Collectors.toList());
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(models);
							} else {
								r.cause().printStackTrace();
								promise.fail("更新系统任务扫描时间出错。");
							}
						});
					});
					return f;
				};
				// 3.保存任务
				Function<List<TimerTaskModel>, Future<List<CommonTask>>> taskf = (List<TimerTaskModel> models) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						List<JsonArray> params = new ArrayList<JsonArray>();
						List<CommonTask> tasks = new ArrayList<CommonTask>();
						models.forEach(model -> {
							tasks.addAll(CommonTask.from(model));
						});
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
				Function<List<CommonTask>, Future<String>> logf = (List<CommonTask> tasks) -> {
					Future<String> f = Future.future(promise -> {
						tasks.forEach(task -> {
							String msg = String.format("%s 系统按照任务模版%s生成任务，执行时间是%s", //
									DateUtil.curDtStr(), //
									task.getAbs(), //
									task.getPlanDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
							MsgUtil.sysLog(vertx, msg);
						});
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
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
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ " handler,oldcontent,newcontent,modelId,abstract,oper,opdate) "//
								+ " values(?,?,?,?,?,?,?,?,?,?,?)";
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
					return updatef.apply(r);
				}).compose(r -> {
					return taskf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).setHandler(r -> {
					if (r.succeeded()) {
						// logger.info(r.result());
					} else {
						logger.error(r.cause().getMessage());
						r.cause().printStackTrace();
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
		JsonObject provider = Configer.provider;
		JsonObject registerUrl = Configer.registerUrl;
		WebClient wc = WebClient.create(vertx,
				new WebClientOptions().setIdleTimeout(2).setConnectTimeout(2000).setMaxWaitQueueSize(5));
		try {
			provider.put("ip", InetAddress.getLocalHost().getHostAddress());
			wc.post(registerUrl.getInteger("port"), registerUrl.getString("ip"), registerUrl.getString("url"))
					.timeout(1000).sendJsonObject(provider, ar -> {
						if (!ar.succeeded()) {
							// ar.cause().printStackTrace();
						}
					});
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * 从注册获取服务信息-NEW
	 */
	public void newAppInfo() {
		JsonObject provider = Configer.provider;
		JsonObject registerUrl = Configer.registerUrl;
		WebClient wc = WebClient.create(vertx,
				new WebClientOptions().setIdleTimeout(2).setConnectTimeout(2000).setMaxWaitQueueSize(5));
		try {
			provider.put("ip", InetAddress.getLocalHost().getHostAddress());
			wc.post(registerUrl.getInteger("port"), registerUrl.getString("ip"), registerUrl.getString("active"))
					.timeout(1000).sendJsonObject(provider, ar -> {
						if (ar.succeeded()) {
							ar.result().bodyAsJsonArray().stream().map(item -> {
								JsonObject jo = JsonObject.mapFrom(item);
								AppInfo appInfo = new AppInfo();
								appInfo.setServerName(jo.getString("serverName"));
								return appInfo;
							});
							vertx.eventBus().send(SceneEnum.NEWAPPINFO.addr(), null);
						}
					});
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * 扫描所有未完成任务，并执行超期回调<br>
	 */
	private void expired() {
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
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
									String planDt = task.getPlanDt()
											.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
									vertx.eventBus().send(SceneEnum.SMS.addr(), new JsonObject()//
											.put("abs", task.getAbs())//
											.put("planDt", planDt)//
											.put("expiredStatus", task.getCallback().getValue())//
											.put("expiredDesc", task.getCallback().getDesc()));
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
							String msg = DateUtil.curDtStr() + " " + "系统检测到任务'" + task.getAbs() + "'超期," + //
							"任务状态自动转为'" + task.getCallback().getValue() + "'";
							MsgUtil.mixLC(vertx, msg, task.getComposeId());
						});
						List<JsonArray> params = tasks.stream().map(task -> {
							return new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
									.add(task.getModelId())//
									.add(task.getStatus().getValue())//
									.add("任务超期，系统将任务状态置为" + task.getCallback().getValue())//
									.add(ConvertUtil.listToStr(task.getHandler()))//
									.add(task.getAbs())//
									.add("")// remark
									.add("SYS")//
									.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						}).collect(Collectors.toList());
						String sql = "insert into itos_tasklog(logId,taskId,modelId,status,statusdesc,"//
								+ "handler,abstract,remark,oper,opDate) values(?,?,?,?,?,?,?,?,?,?)";
						conn.batchWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(tasks);
							} else {
								r.cause().printStackTrace();
								promise.fail("保存系统任务日志出错。");
							}
						});
					});
					return f;
				};
				// 5.组合任务
				Function<List<CommonTask>, Future<String>> composef = (tasks) -> {
					Future<String> f = Future.future(promise -> {
						String param = tasks.stream().filter(item -> {
							return Objects.nonNull(item.getComposeId());
						}).map(item -> {
							return item.getTaskId() + "^" + "SYS";
						}).collect(Collectors.joining(","));
						if (ConvertUtil.emptyOrNull(param)) {
							promise.complete();
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
									String newTask = j.getString(3);// c新建下阶段任务数量
									if (flag) {
										MsgUtil.mixLC(vertx, newTask, "SOMEID");// c这时的值是批量值，没什么意义。
										promise.complete();
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
				// 6.执行
				loadf.get().compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).compose(r -> {
					return nf.apply(r);
				}).compose(r -> {
					return composef.apply(r);
				}).setHandler(r -> {
					if (r.failed())
						r.cause().printStackTrace();
					conn.close();
				});
			}
		});
	}

	@Override
	public void start() throws Exception {
		vertx.setPeriodic(5000, timerId -> {
			expired();
			task();
			registe();
			newAppInfo();
		});
	}

}
