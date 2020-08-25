package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.ConfigVerticle.CONFIG;
import static nbct.com.cn.itos.ConfigVerticle.SC;
import static nbct.com.cn.itos.handler.CompareHandler.CUR_COMPARES;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.client.WebClient;
import nbct.com.cn.itos.ConfigVerticle;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.CompareFile;
import util.ConvertUtil;
import util.DateUtil;
import util.MsgUtil;

/**
 * @author PJ
 * @version 创建时间：2020年5月25日 下午12:20:13
 */
public class SystemTaskHandler {

	public static Logger log = LogManager.getLogger(SystemTaskHandler.class);

	private Vertx vertx;

	public SystemTaskHandler(Vertx vertx) {
		this.vertx = vertx;
	}

	class BusinessResult {
		public boolean flag;
		public String errMsg;
		public String sucMsg;

		public BusinessResult() {

		}

		public BusinessResult(boolean flag, String msg) {
			this.flag = flag;
			if (flag) {
				this.sucMsg = msg;
			} else {
				this.errMsg = msg;
			}
		}
	}

	/**
	 * EDI短信初始状态
	 */
	private static String EDIPROGRESS_SMS = "NONE";

	/**
	 * 更新task的任务状态为DONE
	 * 
	 * @param conn
	 * @param errMsg
	 */
	private Function<List<CommonTask>, Future<List<CommonTask>>> taskDoneFunction(SQLConnection conn, String errMsg) {
		Function<List<CommonTask>, Future<List<CommonTask>>> uf = list -> {
			Future<List<CommonTask>> f = Future.future(promise -> {
				String sql = "update itos_task set status = 'DONE' where taskId = ? ";
				List<JsonArray> params = list.stream().map(item -> {
					return new JsonArray().add(item.getTaskId());
				}).collect(Collectors.toList());
				conn.batchWithParams(sql, params, r -> {
					if (r.succeeded()) {
						promise.complete(list);
					} else {
						r.cause().printStackTrace();
						promise.fail(errMsg);
					}
				});
			});
			return f;
		};
		return uf;

	}

	/**
	 * 保存task的DONE日志
	 * 
	 * @param conn
	 * @param msg
	 */
	private Function<List<CommonTask>, Future<List<CommonTask>>> taskLogFunction(SQLConnection conn,
			BusinessResult br) {
		Function<List<CommonTask>, Future<List<CommonTask>>> logf = tasks -> {
			Future<List<CommonTask>> f = Future.future(promise -> {
				List<JsonArray> params = tasks.stream().map(task -> {
					return new JsonArray()//
							.add(UUID.randomUUID().toString())//
							.add(task.getTaskId())//
							.add(task.getCategory().getValue())//
							.add(task.getModelId())//
							.add("DONE")//
							.add(br.flag ? br.sucMsg : br.errMsg)//
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
						r.cause().printStackTrace();
						promise.fail("保存系统任务日志出错。");
					}
				});
			});
			return f;
		};
		return logf;

	}

	/**
	 * 属于组合任务的task处理
	 * 
	 * @param conn
	 */
	private Function<List<CommonTask>, Future<String>> taskComposeFunction(SQLConnection conn) {
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
		return composef;

	}

	/**
	 * 执行系统比对任务
	 */
	public void compareTask() {
		final BusinessResult br = new BusinessResult();
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读系统比对任务
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status = 'CHECKIN' " + //
						"and category = 'SYSTEM' and invalid = 'N'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								try {
									CommonTask ct = new CommonTask();
									List<CommonTask> list = r.result().getRows().stream().map(item -> {
										return ct.from(item);
									}).filter(item -> {
										JsonObject jo = new JsonObject(item.getContent());
										if ("COMPARE".equals(jo.getString("header"))) {
											return item.getPlanDt().isBefore(LocalDateTime.now());
										} else {
											return false;
										}
									}).collect(Collectors.toList());
									promise.complete(list);
								} catch (Exception e) {
									e.printStackTrace();
									promise.fail(e.getMessage());
								}
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.读取比对表，删除无效内存数据
				Function<List<CommonTask>, Future<List<CommonTask>>> readf = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_filecompare";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								try {
									// 2.1数据库记录
									List<String> cfs = r.result().getRows().stream().map(item -> {
										return new CompareFile().from(item).getCompareId();
									}).collect(Collectors.toList());
									// 2.2内存差集
									List<String> difs = CUR_COMPARES.keySet().stream().filter(k -> {
										return !cfs.contains(k);
									}).collect(Collectors.toList());
									// 2.3删除差集
									difs.forEach(k -> {
										CUR_COMPARES.remove(k);
									});
									promise.complete(tasks);
								} catch (Exception e) {
									e.printStackTrace();
									promise.fail(e.getMessage());
								}
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 3.执行比对
				Function<List<CommonTask>, Future<List<CommonTask>>> comparef = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						br.flag = true;
						br.sucMsg = "比对完成，未发现问题";
						// 3.1没有比对任务
						if (tasks.size() == 0) {
							promise.complete(tasks);
							return;
						}
						Map<String, List<CompareFile>> cm = CUR_COMPARES.values().stream()
								.collect(Collectors.groupingBy(CompareFile::getCompareGroup));
						cm.forEach((k, v) -> {
							String compareResult = null;
							// 3.2.比对文件长度
							long fileSizeCount = v.stream().map(cf -> cf.getCurFileSize()).distinct().count();
							if (fileSizeCount > 1) {
								compareResult = String.format("ITOS:分组%s文件长度不一致", k);
							}
							// 3.3比对文件修改时间
							long fileModifyTimeCount = v.stream().map(cf -> cf.getCurFileModifyTime()).distinct()
									.count();
							if (fileModifyTimeCount > 1) {
								if (compareResult == null) {
									compareResult = String.format("ITOS:分组%s文件修改时间不一致", k);
								} else {
									compareResult += "文件修改时间也不一致";
								}
							}
							// 3.4.短信
							if (compareResult != null) {
								DeliveryOptions options = new DeliveryOptions();
								options.addHeader("CATEGORY", "COMPARE");
								vertx.eventBus().send(SceneEnum.SMS.addr(), new JsonObject()//
										.put("msg", compareResult), options);
								br.flag = false;
								br.errMsg = compareResult;
							}
						});
						// 3.5比对完成
						promise.complete(tasks);
					});
					return f;
				};
				// 4.更新状态为DONE
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = taskDoneFunction(conn, "比对任务更新状态出错。");
				// 5.日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = taskLogFunction(conn, br);
				// 6.执行
				loadf.get().compose(r -> {
					return readf.apply(r);
				}).compose(r -> {
					return comparef.apply(r);
				}).compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.failed())
						log.error("COMPARETASK-01::", r.cause());
					conn.close();
				});
			}
		});

	}

	/**
	 * EDI任务进度监控
	 */
	public void ediProgress() {
		final BusinessResult br = new BusinessResult();
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读系统EDI监控任务
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status = 'CHECKIN' " + //
						"and category = 'SYSTEM' and invalid = 'N'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								try {
									CommonTask ct = new CommonTask();
									List<CommonTask> list = r.result().getRows().stream().map(item -> {
										return ct.from(item);
									}).filter(item -> {
										JsonObject jo = new JsonObject(item.getContent());
										if ("EDIPROGRESS".equals(jo.getString("header"))) {
											return item.getPlanDt().isBefore(LocalDateTime.now());
										} else {
											return false;
										}
									}).collect(Collectors.toList());
									promise.complete(list);
								} catch (Exception e) {
									e.printStackTrace();
									promise.fail(e.getMessage());
								}
							} else {
								promise.fail("读EDI进度任务，访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.读最早未执行EDI任务时间
				Function<List<CommonTask>, Future<List<CommonTask>>> cf = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						// 2.1没有任务
						if (tasks.size() == 0) {
							promise.complete(tasks);
							return;
						}
						// 2.2读最早时间
						String sql = " select count(1) as tasknum from itos_task where " + //
						" abstract in ('RECEIVE_EDI','WRITELINKIN') and status = 'CHECKIN' " + //
						" and plandt < sysdate - 1/24/60*5";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								JsonObject row = r.result().getRows().get(0);
								if (row.getInteger("TASKNUM") > 0) {
									if ("NONE".equals(EDIPROGRESS_SMS)) {
										DeliveryOptions options = new DeliveryOptions();
										options.addHeader("CATEGORY", "EDIPROGRESS");
										vertx.eventBus().send(SceneEnum.SMS.addr(), new JsonObject()//
												.put("msg", "EDI任务进度滞后请检查" + DateUtil.curDtStr()), options);
										br.flag = false;
										br.errMsg = "EDI任务超时请检查";
										EDIPROGRESS_SMS = "SEND";
									}
								} else {
									if ("SEND".equals(EDIPROGRESS_SMS)) {
										DeliveryOptions options = new DeliveryOptions();
										options.addHeader("CATEGORY", "EDIPROGRESS");
										vertx.eventBus().send(SceneEnum.SMS.addr(), new JsonObject()//
												.put("msg", "EDI任务进度恢复正常" + DateUtil.curDtStr()), options);
										EDIPROGRESS_SMS = "NONE";
									}
								}
								promise.complete(tasks);
							} else {
								promise.fail("读EDI进度任务，访问数据库出错");
							}
						});
					});
					return f;
				};
				// 3.更新状态为DONE
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = taskDoneFunction(conn, "EDI检查任务更新状态出错。");
				// 5.日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = taskLogFunction(conn, br);
				// 6.执行
				loadf.get().compose(r -> {
					return cf.apply(r);
				}).compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.failed())
						log.error("EDIPROGRESS-01::", r.cause());
					conn.close();
				});
			}
		});
	}

	/**
	 * 执行系统延时任务
	 */
	public void timerTask() {
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读系统延时任务
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status = 'CHECKIN' " + //
						"and category = 'SYSTEM' and invalid = 'N'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								try {
									CommonTask ct = new CommonTask();
									List<CommonTask> list = r.result().getRows().stream().map(item -> {
										return ct.from(item);
									}).filter(item -> {
										JsonObject jo = new JsonObject(item.getContent());
										if ("DELAY".equals(jo.getString("header"))) {
											LocalDateTime doneTime = item.getPlanDt()
													.plusSeconds(jo.getInteger("length"));
											return doneTime.isBefore(LocalDateTime.now());
										} else if ("APPOINTED".equals(jo.getString("header"))) {
											LocalDateTime appointedTime = DateUtil.strToLocal(jo.getString("time"));
											return appointedTime.isBefore(LocalDateTime.now());
										} else {
											return false;
										}
									}).collect(Collectors.toList());
									promise.complete(list);
								} catch (Exception e) {
									e.printStackTrace();
									promise.fail(e.getMessage());
								}
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.更新状态
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = taskDoneFunction(conn, "延时任务更新状态出错。");
				// 3.记录日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = taskLogFunction(conn,
						new BusinessResult(true, "时间到期，系统将任务状态置为DONE"));
				// 4.组合任务
				Function<List<CommonTask>, Future<String>> composef = taskComposeFunction(conn);
				// 5.执行
				loadf.get().compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).compose(r -> {
					return composef.apply(r);
				}).onComplete(r -> {
					if (r.failed())
						log.error("TIMERTASK-01::", r.cause());
					conn.close();
				});
			}
		});

	}

	/**
	 * 执行系统公告任务
	 */
	public void announceTask() {
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读系统公告任务
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status = 'CHECKIN' " + //
						"and category = 'SYSTEM' and invalid = 'N'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								CommonTask ct = new CommonTask();
								List<CommonTask> list = r.result().getRows().stream().map(item -> {
									return ct.from(item);
								}).filter(item -> {
									JsonObject jo = new JsonObject(item.getContent());
									if ("ANNOUNCE".equals(jo.getString("header"))) {
										return item.getPlanDt().isBefore(LocalDateTime.now());
									} else {
										return false;
									}
								}).collect(Collectors.toList());
								promise.complete(list);
							} else {
								promise.fail("访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.发起公告
				Function<List<CommonTask>, Future<List<CommonTask>>> announcef = list -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						WebClient webClient = WebClient.create(vertx);
						String regUrl = ConfigVerticle.getRegisterUrl() + "/provider/" + CONFIG.getString("itafServer");
						webClient.getAbs(regUrl).send(handle -> {
							if (handle.succeeded()) {
								JSONObject r = handle.result().bodyAsJson(JSONObject.class);
								if (r.getBoolean("flag")) {
									JSONObject provider = r.getJSONArray("data").getJSONObject(0);
									list.stream().forEach(task -> {
										String announceUrl = "http://" + provider.getString("ip") + ":"
												+ provider.getInteger("port") + "/broadcast/save";
										JsonObject content = new JsonObject(task.getContent());
										JsonObject param = content.getJsonObject("announcement");
										param.put("contactId", 0);
										param.put("publishUser", "SYS");
										param.put("expirationTime",
												DateUtil.localToUtcStr(LocalDateTime.now().plusMinutes(5)));
										param.put("valid", "Y");
										webClient.postAbs(announceUrl).sendJsonObject(param, h -> {
											if (h.succeeded()) {
												log.info("ANNOUNCETASK-01::" + task.getContent());
											}
										});
									});
								}
							}
						});
						promise.complete(list);
					});
					return f;
				};
				// 3.更新状态
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = taskDoneFunction(conn, "公告任务更新状态出错。");
				// 4.记录日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = taskLogFunction(conn,
						new BusinessResult(true, "公告完成，系统将任务状态置为DONE"));
				// 5.组合任务
				Function<List<CommonTask>, Future<String>> composef = taskComposeFunction(conn);
				// 6.执行
				loadf.get().compose(r -> {
					return announcef.apply(r);
				}).compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).compose(r -> {
					return composef.apply(r);
				}).onComplete(r -> {
					if (r.failed())
						log.error("ANNOUNCETASK-02::", r.cause());
					conn.close();
				});
			}
		});

	}

	/**
	 * ITOS系统日志清理任务
	 */
	public void housekeep() {
		final BusinessResult br = new BusinessResult();
		br.flag = true;
		br.sucMsg = "清理完成";
		SC.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读系统清理任务
				Supplier<Future<List<CommonTask>>> loadf = () -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						String sql = "select * from itos_task where status = 'CHECKIN' " + //
						"and category = 'SYSTEM' and invalid = 'N'";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								try {
									CommonTask ct = new CommonTask();
									List<CommonTask> list = r.result().getRows().stream().map(item -> {
										return ct.from(item);
									}).filter(item -> {
										JsonObject jo = new JsonObject(item.getContent());
										if ("HOUSEKEEP".equals(jo.getString("header"))) {
											return item.getPlanDt().isBefore(LocalDateTime.now());
										} else {
											return false;
										}
									}).collect(Collectors.toList());
									promise.complete(list);
								} catch (Exception e) {
									e.printStackTrace();
									promise.fail(e.getMessage());
								}
							} else {
								promise.fail("读系统清理任务，访问数据库出错");
							}
						});
					});
					return f;
				};
				// 2.执行清理
				Function<List<CommonTask>, Future<List<CommonTask>>> cf = (tasks) -> {
					Future<List<CommonTask>> f = Future.future(promise -> {
						// 2.1没有清理任务
						if (tasks.size() == 0) {
							promise.complete(tasks);
							return;
						}
						// 2.2清理
						String func = "{call itos.p_housekeep(?,?,?,?)}";
						JsonArray params = new JsonArray();
						JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
						conn.callWithParams(func, params, outputs, qr -> {
							if (qr.succeeded()) {
								JsonArray j = qr.result().getOutput();
								Boolean flag = "0".equals(j.getString(1));
								String errMsg = j.getString(2);
								if (flag) {
									promise.complete(tasks);
								} else {
									promise.fail(errMsg);
								}
							} else {
								br.flag = false;
								br.errMsg = "调用存储过程出错";
								promise.fail("执行清理，访问数据库出错");
							}
						});
					});
					return f;
				};
				// 3.更新状态为DONE
				Function<List<CommonTask>, Future<List<CommonTask>>> uf = taskDoneFunction(conn, "比对任务更新状态出错。");
				// 4.日志
				Function<List<CommonTask>, Future<List<CommonTask>>> logf = taskLogFunction(conn, br);
				// 5.执行
				loadf.get().compose(r -> {
					return cf.apply(r);
				}).compose(r -> {
					return uf.apply(r);
				}).compose(r -> {
					return logf.apply(r);
				}).onComplete(r -> {
					if (r.failed())
						log.error("HOUSEKEEP-01::", r.cause());
					conn.close();
				});
			}
		});

	}

}
