package nbct.com.cn.itos.jdbc;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;

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
	 * @param <T>
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
	 * @param <T>
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
						if (qr.succeeded()) {
							if (mapper != null) {
								res.end(OK(mapper.from(qr.result().getRows())));
							} else {
								res.end(OK(qr.result().getRows()));
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
	 * 查询数据集(1)
	 * 
	 * @param ctx
	 * @param sql
	 * @param params
	 */
	public static void oneRow(RoutingContext ctx, String sql, JsonArray params) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.queryWithParams(sql, params, qr -> {
						if (qr.succeeded()) {
							res.end(OK(qr.result().getRows().get(0)));
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
	 * 转换日期字符串
	 */
	public static String toDbDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = "to_date('" + sdf.format(dt) + "','yyyy-mm-dd hh24:mi:ss')";
		return dtStr;
	}
}
