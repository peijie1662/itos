package nbct.com.cn.itos.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.AppInfo;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PJ
 * @version 创建时间：2020年4月21日 下午12:14:44
 */
public class AppInfoHandler {

	public static Logger log = LogManager.getLogger(AppInfoHandler.class);

	/**
	 * 客户端实时数据<br>
	 * key serviceName@IP
	 */
	private final static ConcurrentHashMap<String, JsonObject> APPS = new ConcurrentHashMap<String, JsonObject>();

	/**
	 * 添加服务信息 <br>
	 * 页端传入应为完整的AppInfo对象
	 */
	public void addAppInfo(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		rp.put("serviceId", UUID.randomUUID().toString());
		String func = "{call itos.p_appinfo_add(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 修改服务信息<br>
	 * 页端传入应为完整的AppInfo对象(根据serviceId进行修改)
	 */
	public void updAppInfo(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_appinfo_upd(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 删除服务信息<br>
	 * 页端传入应为serviceId
	 */
	public void delAppInfo(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray().add(rp.getString("serviceId"));
		String sql = "delete from itos_appinfo where serviceId = ?";
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 服务列表<br>
	 * 从表中读取定义数据，不包含其它数据。
	 */
	public void listAppInfo(RoutingContext ctx) {
		String sql = "select * from itos_appinfo";
		JdbcHelper.rows(ctx, sql, new AppInfo());
	}

	/**
	 * 客户端上传服务信息
	 */
	public void actualAppInfo(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		try {
			ctx.getBodyAsJsonArray().forEach(item -> {
				JsonObject jo = JsonObject.mapFrom(item);
				String key = jo.getString("serviceName") + "@" + jo.getString("ip");
				APPS.put(key, jo);
			});
			res.end(OK());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
		}
	}
	
	

	/**
	 * 场景服务列表<br>
	 * 1.读取定义表与定位表关联数据<br>
	 * 2.注入实时信息
	 */
//	public void sceneAppInfoList(RoutingContext ctx) {
//		HttpServerResponse res = ctx.response();
//		res.putHeader("content-type", "application/json");
//		ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
//		apps.addAll(LEGACY_APPS);
//		apps.addAll(NEW_APPS);
//		String sql = "select * from itos_topology";
//		JdbcHelper.rows(sql, new TopologyCoordinate()).onSuccess(coordinates -> {
//			apps.forEach(app -> {
//				Optional<TopologyCoordinate> c = coordinates.stream()
//						.filter(coordinate -> coordinate.getServerName().equals(app.getServerName())
//								&& coordinate.getIp().equals(app.getIp()))
//						.findAny();
//				if (c.isPresent()) {
//					app.setX(c.get().getX());
//					app.setY(c.get().getY());
//				}
//			});
//			res.end(OK(apps));
//		}).onFailure(e -> {
//			res.end(Err(e.getCause().getMessage()));
//		});
//	}

	/**
	 * 服务拓扑位置
	 * 
	 * public void getTopologyCoordinate(RoutingContext ctx) { String sql = "select
	 * * from itos_topology"; JdbcHelper.rows(ctx, sql, new TopologyCoordinate()); }
	 */

	/**
	 * 按照场景更新拓扑位置
	 * 
	 * public void updTopologyCoordinate(RoutingContext ctx) { JsonArray details =
	 * ctx.getBodyAsJsonArray(); HttpServerResponse res = ctx.response();
	 * res.putHeader("content-type", "application/json"); SQLClient client =
	 * Configer.client; client.getConnection(cr -> { if (cr.succeeded()) {
	 * SQLConnection conn = cr.result(); // 1.删除原坐标 Supplier<Future<Void>> delf = ()
	 * -> { Future<Void> f = Future.future(promise -> { String sql = "delete from
	 * itos_topology"; conn.update(sql, r -> { if (r.succeeded()) {
	 * promise.complete(); } else { promise.fail(r.cause().getMessage()); } }); });
	 * return f; }; // 2.保存新位置 Function<Void, Future<Void>> savef = (task) -> {
	 * Future<Void> f = Future.future(promise -> { String sql = "insert into
	 * itos_topology(serverName,ip,x,y) values(?,?,?,?)"; List<JsonArray> batch =
	 * new ArrayList<>(); details.stream().forEach(item -> { JsonObject j =
	 * JsonObject.mapFrom(item); batch.add(new
	 * JsonArray().add(j.getString("serverName")).add(j.getString("ip"))
	 * .add(j.getInteger("x")).add(j.getInteger("y"))); });
	 * conn.batchWithParams(sql, batch, r -> { if (r.succeeded()) {
	 * promise.complete(); } else { promise.fail(r.cause().getMessage()); } }); });
	 * return f; }; // 3.执行 delf.get().compose(r -> { return savef.apply(r);
	 * }).onComplete(r -> { if (r.succeeded()) {
	 * log.info(String.format("UPDTOPOLOGYCOORDINATE-01::更新拓扑图信息")); res.end(OK());
	 * } else { log.error("UPDTOPOLOGYCOORDINATE-02::", r.cause());
	 * res.end(Err(r.cause().getMessage())); } conn.close(); }); } }); }
	 */

}
