package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
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
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.CommonTask;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年1月17日 上午8:54:47
 */
public class DispatchTaskHandler {

	/**
	 * 所有下发终端任务列表(页面访问)
	 */
	public void getDispatchAllTask(RoutingContext ctx) {
		String sql = "select * from itos_task where category in (?,?) and invalid = 'N' and composeId is null";
		JsonArray params = new JsonArray();
		params.add(CategoryEnum.CMD.getValue());
		params.add(CategoryEnum.PROCEDURE.getValue());
		JdbcHelper.rows(ctx, sql, params, new CommonTask());
	}

	/**
	 * 下发终端任务列表(终端访问)
	 */
	public void getDispatchTaskList(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_task where category in (?,?) and status = ? and invalid = 'N'" + //
				" and instr((select modelID from itos_service where servicename= ? ),modelId ) > 0";
		JsonArray params = new JsonArray();
		params.add(CategoryEnum.CMD.getValue());
		params.add(CategoryEnum.PROCEDURE.getValue());
		params.add(TaskStatusEnum.CHECKIN.getValue());
		params.add(rp.getString("serviceName"));
		JdbcHelper.rows(ctx, sql, params);
	}

	/**
	 * 保存下发任务
	 */
	public void saveDispatchTask(RoutingContext ctx) {
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
						String sql = "insert into itos_task(taskId,category,abstract,modelId," + //
								"status,content,invalid,taskicon,oper,opdate) values(?,?,?,?,?,?,?,?,?,?,?)";
						JsonArray params = new JsonArray();
						String taskId = rp.getString("taskId");
						String oper = rp.getString("oper");
						String abs = rp.getString("abstract");
						String category = rp.getString("category");
						params.add(taskId);
						params.add(CategoryEnum.from(category));
						params.add(abs);
						params.add(rp.getString("modelId"));// 通过modelId关联到dispatchClient
						params.add("CHECKIN");
						params.add(rp.getString("content"));
						params.add("N");
						params.add(rp.getString("taskIcon"));
						params.add(oper);
						params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete(new JsonObject()//
										.put("taskId", taskId)//
										.put("oper", oper)//
										.put("abstract", abs));
							} else {
								promise.fail("保存任务出错。");
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
								.add(j.getString("abstract"))//
								.add(j.getString("oper"))//
								.add(DateUtil.localToUtcStr(LocalDateTime.now()));
						String sql = "insert into itos_tasklog(logId,taskId,status,statusdesc,"//
								+ "abstract,oper,opDate) values(?,?,?,?,?,?,?,?)";
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
				}).setHandler(r -> {
					if (r.succeeded()) {
						String log = DateUtil.curDtStr() + " " + "登记任务'" + rp.getString("abs") + "'";
						ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
						res.end(OK(r.result()));
					} else {
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});
			}
		});
	}

}
