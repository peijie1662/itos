package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;

import java.time.LocalDateTime;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.AddressEnum;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.ModelRowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月26日 下午10:56:58
 */
public class ModelHandler {

	/**
	 * 组合任务模版列表
	 * @param ctx
	 */
	public void getComposeModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel where invalid = 'N' "//
				+ " and category = 'COMPOSE' order by opdate";
		JdbcHelper.rows(ctx, sql, new ModelRowMapper());
	}
	
	/**
	 * 保存组合模版详细信息
	 * @param ctx
	 */
	public void saveComposeModelDetail(RoutingContext ctx){
		JsonObject rp = ctx.getBodyAsJson();
		
		JsonArray details = rp.getJsonArray("details");
		details.stream().forEach(item -> {
			
			JsonObject j = JsonObject.mapFrom(item);
			
			System.out.println(j.encodePrettily());
		});
		
		HttpServerResponse res = ctx.response();
		res.end("{flag:true}");
	}
	
	/**
	 * 组合详细信息
	 * @param ctx
	 */
	public void getComposeDetail(RoutingContext ctx){
		
	}

	public void getTimerTaskModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel order by category,opdate";
		JdbcHelper.rows(ctx, sql, new ModelRowMapper());
	}

	public void updateTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_taskmodel set comments = ?,planDates= ? where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("comments"));
		params.add(rp.getString("planDates"));
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
		// 日志
		String log = DateUtil.curDtStr() + " " + "模版'" + rp.getString("abs") + "'已被修改。";
		ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
	}

	public void deleteTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete itos_taskmodel where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
		// 日志
		String log = DateUtil.curDtStr() + " " + "模版'" + rp.getString("abs") + "'已被刪除。";
		ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
	}

	public void addTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		String category = rp.getString("category");
		String abs = rp.getString("abs");
		String comments = rp.getString("comments");
		String planDates = rp.getString("planDates");
		if (category == null) {
			res.end(Err("任务类别不能为空。"));
			return;
		}
		if (abs == null) {
			res.end(Err("任务简介不能为空。"));
			return;
		}
		if (comments == null) {
			res.end(Err("任务内容不能为空。"));
			return;
		}
		// COMPOSE任务无需执行周期与时间，因为都是临时手动触发的。
		if (!CategoryEnum.COMPOSE.getValue().equals(category)) {
			if (rp.getString("cycle") == null) {
				res.end(Err("执行周期不能为空。"));
				return;
			}
			if (planDates == null) {
				res.end(Err("任务时间不能为空。"));
				return;
			}
		}
		String sql = "insert into itos_taskmodel(modelId,category,cycle,comments,planDates,oper,opdate,invalid,abstract) "
				+ " values(?,?,?,?,?,?,?,?,?)";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		params.add(category);
		params.add(rp.getString("cycle"));
		params.add(comments);
		params.add(planDates);
		params.add("SYS");
		params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
		params.add("N");
		params.add(abs);
		JdbcHelper.update(ctx, sql, params);
		// 日志
		String log = DateUtil.curDtStr() + " " + "新增模版'" + rp.getString("abs") + "'";
		ctx.vertx().eventBus().send(AddressEnum.SYSLOG.getValue(), log);
	}

	/**
	 * 改变模版有效状态
	 */
	public void chgModelStatus(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_taskmodel set invalid = ? where modelId = ?";
		JsonArray params = new JsonArray().add(rp.getString("invalid")).add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
	}

}
