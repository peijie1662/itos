package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.jdbc.ComposeDetailRowMapper;
import nbct.com.cn.itos.jdbc.JdbcHelper;

/**
 * @author PJ
 * @version 创建时间：2020年1月20日 上午8:35:15
 */
public class ComposeHandler {

	/**
	 * 保存组合模版详细信息
	 * 
	 * @param ctx
	 */
	public void saveComposeModelDetail(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String composeId = rp.getString("composeId");
		JsonArray details = rp.getJsonArray("details");
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		// 1.删除原有内容
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.删除原数据
				Supplier<Future<Void>> delf = () -> {
					Future<Void> f = Future.future(promise -> {
						JsonArray params = new JsonArray().add(composeId);
						String sql = "delete from itos_compose where composeId = ?";
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete();
							} else {
								promise.fail(r.cause().getMessage());
							}
						});
					});
					return f;
				};
				// 2.保存新数据
				Function<Void, Future<Void>> savef = (task) -> {
					Future<Void> f = Future.future(promise -> {
						String sql = "insert into itos_compose(composeId,composeLevel,modelId) values(?,?,?)";
						List<JsonArray> batch = new ArrayList<>();
						details.stream().forEach(item -> {
							JsonObject j = JsonObject.mapFrom(item);
							batch.add(new JsonArray()//
									.add(j.getString("composeId"))//
									.add(j.getInteger("composeLevel"))//
									.add(j.getString("modelId")));
						});
						conn.batchWithParams(sql, batch, r -> {
							if (r.succeeded()) {
								promise.complete();
							} else {
								promise.fail(r.cause().getMessage());
							}
						});
					});
					return f;
				};
				// 3.执行
				delf.get().compose(r -> {
					return savef.apply(r);
				}).setHandler(r -> {
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

	/**
	 * 组合详细信息
	 * 
	 * @param ctx
	 */
	public void getComposeDetail(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_compose where composeId = ? order by composeLevel";
		JsonArray params = new JsonArray().add(rp.getString("composeId"));
		JdbcHelper.rows(ctx, sql, params, new ComposeDetailRowMapper());
	}
	
	/**
	 * 读取该组合模版
	 * @param ctx
	 */
	public void getComposeTasksByModels(RoutingContext ctx) {
		
	}
}