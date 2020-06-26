package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.AppInfo;
import nbct.com.cn.itos.model.TopologyCoordinate;

/**
 * @author PJ
 * @version 创建时间：2020年4月21日 下午12:14:44
 */
public class AppInfoHandler {

	public static Logger log = LogManager.getLogger(AppInfoHandler.class);

	/**
	 * 遗产服务
	 */
	private static List<AppInfo> LEGACY_APPS = new ArrayList<AppInfo>();

	/**
	 * 新服务
	 */
	private static List<AppInfo> NEW_APPS = new ArrayList<AppInfo>();

	private Vertx vertx;

	public AppInfoHandler(Vertx vertx) {
		this.vertx = vertx;
	}

	/**
	 * 设置Legacy服务信息，终端调用
	 */
	public void setLegacyAppInfo(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			LEGACY_APPS = ctx.getBodyAsJsonArray().stream().map(item -> {
				AppInfo app = AppInfo.from(JsonObject.mapFrom(item));
				app.setType("LEGACY");
				return app;
			}).collect(Collectors.toList());
			res.end(OK());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
		}
	}

	/**
	 * 设置New服务信息，定时服务调用
	 */
	public void setNewAppInfo(Message<String> msg) {
		WebClient webClient = WebClient.create(vertx);
		String activeUrl = Configer.getActiveUrl();
		webClient.getAbs(activeUrl).send(handle -> {
			if (handle.succeeded()) {
				JsonArray apps = handle.result().bodyAsJsonArray();
				NEW_APPS = apps.stream().map(item -> {
					AppInfo app = AppInfo.from(JsonObject.mapFrom(item));
					app.setType("NEW");
					app.setValid(true);// c新服务读过来即默认有效
					return app;
				}).collect(Collectors.toList());
			}
		});
	}

	/**
	 * 服务列表
	 */
	public void getAppInfoList(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
		apps.addAll(LEGACY_APPS);
		apps.addAll(NEW_APPS);
		String sql = "select * from itos_topology";
		JdbcHelper.rows(sql, new TopologyCoordinate()).onSuccess(coordinates -> {
			apps.forEach(app -> {
				Optional<TopologyCoordinate> c = coordinates.stream()
						.filter(coordinate -> coordinate.getServerName().equals(app.getServerName())
								&& coordinate.getIp().equals(app.getIp()))
						.findAny();
				if (c.isPresent()) {
					app.setX(c.get().getX());
					app.setY(c.get().getY());
				}
			});
			res.end(OK(apps));
		}).onFailure(e -> {
			res.end(Err(e.getCause().getMessage()));
		});
	}

	/**
	 * 服务拓扑位置
	 */
	public void getTopologyCoordinate(RoutingContext ctx) {
		String sql = "select * from itos_topology";
		JdbcHelper.rows(ctx, sql, new TopologyCoordinate());
	}

	/**
	 * 更新拓扑位置
	 */
	public void updTopologyCoordinate(RoutingContext ctx) {
		JsonArray details = ctx.getBodyAsJsonArray();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				// 1.删除原坐标
				Supplier<Future<Void>> delf = () -> {
					Future<Void> f = Future.future(promise -> {
						String sql = "delete from itos_topology";
						conn.update(sql, r -> {
							if (r.succeeded()) {
								promise.complete();
							} else {
								promise.fail(r.cause().getMessage());
							}
						});
					});
					return f;
				};
				// 2.保存新位置
				Function<Void, Future<Void>> savef = (task) -> {
					Future<Void> f = Future.future(promise -> {
						String sql = "insert into itos_topology(serverName,ip,x,y) values(?,?,?,?)";
						List<JsonArray> batch = new ArrayList<>();
						details.stream().forEach(item -> {
							JsonObject j = JsonObject.mapFrom(item);
							batch.add(new JsonArray().add(j.getString("serverName")).add(j.getInteger("ip"))
									.add(j.getString("x")).add(j.getInteger("y")));
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
				}).onComplete(r -> {
					if (r.succeeded()) {
						log.info(String.format("UPDTOPOLOGYCOORDINATE-01::更新拓扑图信息"));
						res.end(OK());
					} else {
						log.error("UPDTOPOLOGYCOORDINATE-02::", r.cause());
						res.end(Err(r.cause().getMessage()));
					}
					conn.close();
				});
			}
		});
	}

}
