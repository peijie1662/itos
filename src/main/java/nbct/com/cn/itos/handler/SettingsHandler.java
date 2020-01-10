package nbct.com.cn.itos.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.SmartTipsRowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年1月9日 上午9:34:00
 */
public class SettingsHandler {

	/**
	 * 智能提示列表
	 */
	public void getSmartTipsList(RoutingContext ctx) {
		String sql = "select * from itos_smart_tips";
		JdbcHelper.rows(ctx, sql, new SmartTipsRowMapper());
	}

	/**
	 * 新增智能提示
	 */
	public void addSmartTips(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(UUID.randomUUID().toString())//
				.add(rp.getString("preReg"))//
				.add(rp.getString("nextWord"))//
				.add(rp.getString("oper"))//
				.add(DateUtil.localToUtcStr(LocalDateTime.now()));
		String sql = "insert into itos_smart_tips(tipId,preReg,nextWord,oper,opDate) "//
				+ " values(?,?,?,?,?)";
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 修改智能提示
	 */
	public void updateSmartTips(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(rp.getString("preReg"))//
				.add(rp.getString("nextWord"))//
				.add(rp.getString("oper"))//
				.add(DateUtil.localToUtcStr(LocalDateTime.now()))//
				.add(rp.getString("tipId"));
		String sql = "update itos_smart_tips set preReg = ?,nextWord = ?,oper = ?,opDate = ? "//
				+ " where tipId = ? ";
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 删除智能提示
	 */
	public void deleteSmartTips(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonArray params = new JsonArray()//
				.add(rp.getString("tipId"));
		String sql = "delete from itos_smart_tips where tipId = ? ";
		JdbcHelper.update(ctx, sql, params);
	}

}
