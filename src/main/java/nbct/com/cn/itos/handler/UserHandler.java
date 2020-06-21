package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.ItosUser;
import nbct.com.cn.itos.model.SysCode;

/**
 * @author PJ
 * @version 创建时间：2020年1月30日 下午6:24:24
 */
public class UserHandler {

	public static Logger log = LogManager.getLogger(UserHandler.class);

	/**
	 * 在线用户
	 */
	public void onlineUsers(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		ctx.vertx().eventBus().request(SceneEnum.ONLINEUSER.addr(), null, reply -> {
			if (reply.succeeded()) {
				res.end(OK(reply.result().body()));
			} else {
				res.end(Err(reply.cause().getMessage()));
			}
		});
	}

	/**
	 * 保存用户
	 */
	public void saveUser(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "insert into itos_user(userId,userName,workId,password,authority,department," + //
				"phone,shortPhone,role) values(?,?,?,?,?,?,?,?,?)";
		JsonArray params = new JsonArray()//
				.add(rp.getString("userId"))//
				.add(rp.getString("userName"))//
				.add(rp.getString("workId"))//
				.add(rp.getString("userId"))// password默认为userId
				.add(rp.getString("authority"))//
				.add(rp.getString("department"))//
				.add(rp.getString("phone"))//
				.add(rp.getString("shortPhone"))//
				.add(rp.getString("role"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 删除用户
	 */
	public void delUser(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete from itos_user where userId = ?";
		JsonArray params = new JsonArray().add(rp.getString("userId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 用户信息列表
	 */
	public void getUserList(RoutingContext ctx) {
		String sql = "select * from itos_user order by role,workId";
		JdbcHelper.rows(ctx, sql, new ItosUser());
	}

	/**
	 * 修改权限
	 */
	public void updateAuthority(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_user set authority = ? where userId = ?";
		JsonArray params = new JsonArray().add(rp.getString("authority")).add(rp.getString("userId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 修改短信订阅 
	 */
	public void updateSubscription(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_user set subscription = ? where userId = ?";
		JsonArray params = new JsonArray().add(rp.getString("subscription")).add(rp.getString("userId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 短信订阅主题
	 */
	public void smsTopicList(RoutingContext ctx) {
		String sql = "select * from itos_syscode where sycategory = 'SMSSUBSCRIPTION' order by syid";
		JdbcHelper.rows(ctx, sql, new SysCode());
	}

	/**
	 * 修改首页
	 */
	public void updateFirstPage(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_user set firstPage = ? where userId = ?";
		JsonArray params = new JsonArray().add(rp.getString("firstPage")).add(rp.getString("userId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 修改内容
	 */
	public void updateContent(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_user set userName = ?,workId = ?," + //
				"department = ?,phone = ?,shortPhone = ?,role = ? where userId = ?";
		JsonArray params = new JsonArray()//
				.add(rp.getString("userName"))//
				.add(rp.getString("workId"))//
				.add(rp.getString("department"))//
				.add(rp.getString("phone"))//
				.add(rp.getString("shortPhone"))//
				.add(rp.getString("role"))//
				.add(rp.getString("userId"));
		JdbcHelper.update(ctx, sql, params);
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
								log.info(String.format("HANDLELOGIN-01::%s登录成功。", userId));
								res.end(OK(new ItosUser().from(r)));
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

	/**
	 * 修改密码
	 */
	public void updatePassword(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String userId = rp.getString("userId");
		String oldPass = rp.getString("oldPass").toUpperCase();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.检查旧密码
				Supplier<Future<Void>> getf = () -> {
					Future<Void> f = Future.future(promise -> {
						String sql = "select * from itos_user where upper(userId) = ?";
						JsonArray params = new JsonArray().add(userId);
						conn.queryWithParams(sql, params, r -> {
							if (r.succeeded()) {
								JsonObject j = r.result().getRows().get(0);
								if (oldPass.equals(j.getString("PASSWORD").toUpperCase())) {
									promise.complete();
								} else {
									promise.fail("旧密码错误。");
								}
							} else {
								promise.fail("读用户信息出错。");
							}
						});
					});
					return f;
				};
				// 2.保存新密码
				Function<Void, Future<Void>> uf = (Void) -> {
					Future<Void> f = Future.future(promise -> {
						String sql = "update itos_user set password = ? where userId = ?";
						JsonArray params = new JsonArray().add(rp.getString("newPass")).add(rp.getString("userId"));
						conn.updateWithParams(sql, params, r -> {
							if (r.succeeded()) {
								promise.complete();
							} else {
								promise.fail("修改密码错误。");
							}
						});
					});
					return f;
				};
				// 3.执行
				getf.get().compose(r -> {
					return uf.apply(r);
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
