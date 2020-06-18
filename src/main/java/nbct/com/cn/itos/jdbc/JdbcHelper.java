package nbct.com.cn.itos.jdbc;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.model.CallResult;

/**
 * @author PJ
 * @version 创建时间：2019年4月10日 下午12:37:19 类说明
 */
public class JdbcHelper {

	/**
	 * 查询数据集
	 * 
	 * @param ctx
	 * @param sql
	 */
	public static void rows(RoutingContext ctx, String sql) {
		JdbcHelper.rows(ctx, sql, null, null);
	}

	/**
	 * 查询数据集
	 * 
	 * @param     <T>
	 * 
	 * @param ctx
	 * @param sql
	 */
	public static <T> void rows(RoutingContext ctx, String sql, RowMapper<T> mapper) {
		JdbcHelper.rows(ctx, sql, null, mapper);
	}

	/**
	 * 查询数据集
	 * 
	 * @param     <T>
	 * 
	 * @param ctx
	 * @param sql
	 */
	public static <T> void rows(RoutingContext ctx, String sql, JsonArray params) {
		JdbcHelper.rows(ctx, sql, params, null);
	}

	/**
	 * 查询数据集
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 */
	public static <T> void rows(RoutingContext ctx, String sql, JsonArray params, RowMapper<T> mapper) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.queryWithParams(sql, params, qr -> {
						try {
							if (qr.succeeded()) {
								if (mapper != null) {
									res.end(OK(mapper.from(qr.result().getRows())));
								} else {
									res.end(OK(qr.result().getRows()));
								}
							} else {
								res.end(Err(qr.cause().getMessage()));
							}
						} catch (Exception e) {
							res.end(Err(e.getMessage()));
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 查询数据集(MAP)
	 * 
	 * @param     <T>
	 * 
	 * @param ctx
	 * @param sql
	 */
	public static <T> void entrys(RoutingContext ctx, String sql, RowMapper<T> mapper, String key) {
		JdbcHelper.entrys(ctx, sql, null, mapper, key);
	}

	/**
	 * 查询数据集(MAP)
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 */
	public static <T> void entrys(RoutingContext ctx, String sql, JsonArray params, RowMapper<T> mapper, String key) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.queryWithParams(sql, params, qr -> {
						try {
							if (qr.succeeded()) {
								if (mapper != null) {
									res.end(OK(mapper.mfrom(qr.result().getRows(), key)));
								} else {
									res.end(OK(qr.result().getRows()));
								}
							} else {
								res.end(Err(qr.cause().getMessage()));
							}
						} catch (Exception e) {
							res.end(Err(e.getMessage()));
							e.printStackTrace();
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 查询数据集(1)
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 */
	public static <T> void oneRow(RoutingContext ctx, String sql, JsonArray params, RowMapper<T> mapper) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.queryWithParams(sql, params, qr -> {
						if (qr.succeeded()) {
							T t = mapper.from(qr.result().getRows().get(0));
							res.end(OK(JsonObject.mapFrom(t)));// 需要转为JsonObject,普通Object不认
						} else {
							res.end(Err(qr.cause().getMessage()));
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 修改记录
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static Future<CallResult<String>> update(String sql, JsonArray params) {
		Future<CallResult<String>> f = Future.future(promise -> {
			SQLClient client = Configer.client;
			client.getConnection(cr -> {
				if (cr.succeeded()) {
					SQLConnection connection = cr.result();
					if (connection != null) {
						connection.updateWithParams(sql, params, qr -> {
							if (qr.succeeded()) {
								promise.complete(new CallResult<String>().ok());
							} else {
								promise.fail(qr.cause().getMessage());
							}
							connection.close();
						});
					} else {
						promise.fail("使用数据库连接出错");
					}
				} else {
					promise.fail("无法获得数据库连接");
				}
			});
		});
		return f;
	}

	/**
	 * 修改记录
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 * @return
	 */
	public static void update(RoutingContext ctx, String sql, JsonArray params) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.updateWithParams(sql, params, qr -> {
						if (qr.succeeded()) {
							res.end(OK());
						} else {
							res.end(Err(qr.cause().getMessage()));
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 批量修改记录
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 * @return
	 */
	public static void batchUpdate(RoutingContext ctx, List<String> sqls) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.batch(sqls, qr -> {
						if (qr.succeeded()) {
							res.end(OK());
						} else {
							res.end(Err(qr.cause().getMessage()));
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 调用存储过程
	 * 
	 * @param ctx
	 * @param func
	 * @param params in参数
	 * @param output out参数
	 */
	public static void call(RoutingContext ctx, String func, JsonArray params, JsonArray outputs) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.callWithParams(func, params, outputs, qr -> {
						if (qr.succeeded()) {
							JsonArray j = qr.result().getOutput();
							Boolean flag = "0".equals(j.getString(1));
							String errMsg = j.getString(2);
							String outMsg = j.getString(3);
							if (flag) {
								res.end(OK(outMsg));
							} else {
								res.end(Err(errMsg));
							}
						} else {
							res.end(Err(qr.cause().getMessage()));
						}
						connection.close();
					});
				} else {
					res.end(Err("the DB connect is null."));
				}
			} else {
				res.end(Err("get DB connect err."));
			}
		});
	}

	/**
	 * 转换日期字符串
	 */
	public static String toDbDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = "to_date('" + sdf.format(dt) + "','yyyy-mm-dd hh24:mi:ss')";
		return dtStr;
	}
}
