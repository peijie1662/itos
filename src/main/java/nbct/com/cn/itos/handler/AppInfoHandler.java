package nbct.com.cn.itos.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.AppInfo;
import nbct.com.cn.itos.model.TopologyConnector;
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
	 * 定义服务列表<br>
	 * 从表中读取定义数据，不包含其它数据。
	 */
	public void listAppInfo(RoutingContext ctx) {
		String sql = "select * from itos_appinfo order by domain,serviceType,ip";
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
				String key = jo.getString("serviceObj") + "@" + jo.getString("ip");
				APPS.put(key, jo);
			});
			res.end(OK());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
		}
	}

	/**
	 * 添加服务到场景中
	 */
	public void addSceneApp(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_scene_app_add(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 场景服务列表<br>
	 * 1.读取定义表与定位表关联数据<br>
	 * 2.注入实时信息
	 */
	public void listSceneApp(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		JsonArray params = new JsonArray().add(rp.getString("scene"));
		String sql = "select b.*, a.x, a.y from itos_topology_loc a, itos_appinfo b " + //
				" where a.scene = ? and a.serviceId = b.serviceId ";
		JdbcHelper.rows(sql, params, new AppInfo()).onSuccess(list -> {
			// TODO
			res.end(OK(list));
		}).onFailure(e -> {
			log.error("LISTSCENEAPP-01::", e);
			res.end(Err(e.getMessage()));
		});
	}

	/**
	 * 删除场景服务
	 */
	public void delSceneApp(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_scene_app_del(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 场景连接列表
	 */
	public void listSceneCon(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select a.*, b.servicename as sourcename, c.servicename as targetname " + //
				"  from itos_topology_con a, itos_appinfo b, itos_appinfo c " + //
				" where scene = ? and a.sourceid = b.serviceid and a.targetid = c.serviceid";
		JsonArray params = new JsonArray().add(rp.getString("scene"));
		JdbcHelper.rows(ctx, sql, params, new TopologyConnector());
	}

	/**
	 * 添加场景连接
	 */
	public void addSceneCon(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_scene_con_add(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 删除场景连接
	 */
	public void delSceneCon(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete from itos_topology_con where scene = ? and sourceid = ? and targetid = ?";
		JsonArray params = new JsonArray().add(rp.getString("scene")).add(rp.getString("sourceId"))
				.add(rp.getString("targetId"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 按照场景更新拓扑位置
	 */
	public void updTopologyCoordinate(RoutingContext ctx) {
		JsonArray cons = ctx.getBodyAsJsonArray();
		String func = "{call itos.p_scene_loc_save(?,?,?,?)}";
		JsonArray params = new JsonArray().add(cons.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

}
