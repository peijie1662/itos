package nbct.com.cn.itos.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.SysCode;

/**
 * @author PJ
 * @version 创建时间：2020年7月4日 下午6:15:04
 */
public class SysCodeHandler {

	/**
	 * 添加<br>
	 * 传入完整对象
	 */
	public void addSysCode(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		if (rp.getString("desc") == null) rp.put("desc", "");
		if (rp.getString("v1") == null) rp.put("v1", "");
		if (rp.getString("v2") == null) rp.put("v2", "");
		if (rp.getString("v3") == null) rp.put("v3", "");
		rp.put("opt", "ADD");
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}	

	/**
	 * 修改<br>
	 * 传入完整对象
	 */
	public void updSysCode(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		if (rp.getString("desc") == null) rp.put("desc", "");
		if (rp.getString("v1") == null) rp.put("v1", "");
		if (rp.getString("v2") == null) rp.put("v2", "");
		if (rp.getString("v3") == null) rp.put("v3", "");
		rp.put("opt", "UPD");
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}	

	/**
	 * 删除<br>
	 * 传入分组ID&子项ID
	 */
	public void delSysCode(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		if (rp.getString("desc") == null) rp.put("desc", "");
		if (rp.getString("v1") == null) rp.put("v1", "");
		if (rp.getString("v2") == null) rp.put("v2", "");
		if (rp.getString("v3") == null) rp.put("v3", "");
		rp.put("opt", "DEL");
		String func = "{call itos.p_syscode(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}	

	/**
	 * 分组列表<br>
	 * 传入分组ID
	 */
	public void listSysCode(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_syscode where sycategory = ? order by syid";
		JsonArray params = new JsonArray().add(rp.getString("category"));
		JdbcHelper.rows(ctx, sql, params, new SysCode());
	}

}
