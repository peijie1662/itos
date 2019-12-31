package nbct.com.cn.itos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.TimerTaskModel;
import nbct.com.cn.itos.util.Pusher;
import util.DateUtil;

/**
 * 自动任务
 * 
 * @author PJ
 * @version 创建时间：2019年12月23日 下午1:47:07
 */
public class TimerVerticle extends AbstractVerticle {

	public static Logger logger = Logger.getLogger(TimerVerticle.class);

	private void task() {
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.读数据
				Supplier<Future<List<TimerTaskModel>>> loadf = () -> {
					Future<List<TimerTaskModel>> f = Future.future(promise -> {
						String sql = "select * from itos_taskmodel where invalid = 'N' order by category,opdate";
						conn.query(sql, r -> {
							if (r.succeeded()) {
								List<TimerTaskModel> models = r.result().getRows().stream().map(row -> {
									try {
										return TimerTaskModel.from(row);
									} catch (Exception e) {
										throw new RuntimeException(e.getMessage());
									}
								}).filter(model -> {
									LocalDate cur = LocalDate.now();
									boolean valid = model.getScanDate() == null;
									if (model.getScanDate() != null) {
										// 扫描每日任务，当前日期>标记时间的日期，就需要生成新任务。
										valid = valid || (model.getCategory() == CategoryEnum.PERDAY
												&& model.getScanDate().isBefore(cur));
										// 扫描每周任务，当前日期的年+第几周>标记时间的年+第几周，就需要生成新任务。
										int cw = cur.getYear() + cur.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
										int rw = model.getScanDate().getYear()
												+ model.getScanDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
										valid = valid || (model.getCategory() == CategoryEnum.PERWEEK && cw > rw);
										// 扫描每月任务，当前日期的年+第几月>标记时间的年+第几月，就需要生成新任务。
										int cm = cur.getYear() + cur.getMonthValue();
										int rm = model.getScanDate().getYear() + model.getScanDate().getMonthValue();
										valid = valid || (model.getCategory() == CategoryEnum.PERMONTH && cm > rm);
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
									.add(task.getStatus())//
									.add(task.getAbs())//
									.add(task.getContent())//
									.add(DateUtil.localToUtcStr(task.getPlanDt()))//
									.add(task.getModelId())//
									.add("N")//
									.add("SYS")//
									.add(DateUtil.localToUtcStr(LocalDateTime.now())));//
						});
						String sql = "insert into itos_task(taskId,status,abstract,content,plandt,modelId,"//
								+ "invalid,oper,opDate) values(?,?,?,?,?,?,?,?,?)";
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
							Pusher.sendLog(vertx, "系统按照任务模版'" + task.getAbs() + "'生成任务,执行时间是'"
									+ task.getPlanDt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'");
						});
						List<JsonArray> params = new ArrayList<JsonArray>();
						tasks.forEach(task -> {
							params.add(new JsonArray()//
									.add(UUID.randomUUID().toString())//
									.add(task.getTaskId())//
									.add(TaskStatusEnum.CHECKIN.getValue())//
									.add("系统按照任务模版生成任务。")//
									.add("")// 待认领
									.add("")// 原内容
									.add("")// 新内容
									.add(task.getModelId())//
									.add(task.getAbs()));//
						});
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "handler,oldcontent,newcontent,modelId,abstract) values(?,?,?,?,?,?,?,?,?)";
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
						logger.info(r.result());
					} else {
						logger.error(r.cause().getMessage());
					}
					conn.close();
				});
			}
		});
	}

	@Override
	public void start() throws Exception {
		vertx.setPeriodic(15000, timerId -> {
			task();
		});
	}

}
