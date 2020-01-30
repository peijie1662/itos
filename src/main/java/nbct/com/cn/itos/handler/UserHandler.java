package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.List;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.UserRowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年1月30日 下午6:24:24
 */
public class UserHandler {

	/**
	 * 用户信息列表
	 */
	public void getUserList(RoutingContext ctx) {
		String sql = "select * from itos_user order by role,workId";
		JdbcHelper.rows(ctx, sql, new UserRowMapper());
	}

	/**
	 * 用户登录
	 */
	public void handleLogin(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String userId = rp.getString("userId").toUpperCase();
		String password = rp.getString("password").toUpperCase();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				String sql = "select * from itos_user where upper(userId) = ?";
				JsonArray params = new JsonArray().add(userId);
				SQLConnection connection = cr.result();
				connection.queryWithParams(sql, params, qr -> {
					if (qr.succeeded()) {
						List<JsonObject> r = qr.result().getRows();
						if (r.size() > 0) {
							if (password.equals(r.get(0).getString("PASSWORD").toUpperCase())) {
								res.end(OK(new UserRowMapper().from(r)));
							} else {
								res.end(Err("密码错误。"));
							}
						} else {
							res.end(Err("没有这个用户。"));
						}
					} else {
						res.end(Err(qr.cause().getMessage()));
					}
					connection.close();
				});
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

}
