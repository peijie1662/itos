package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;

import java.time.LocalDateTime;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.AddressEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.ModelRowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月26日 下午10:56:58
 */
public class ModelHandler {

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
		if (rp.getString("category") == null) {
			res.end(Err("任务类别不能为空。"));
			return;
		}
		if (rp.getString("cycle") == null) {
			res.end(Err("执行周期不能为空。"));
			return;
		}
		if (rp.getString("abs") == null) {
			res.end(Err("任务简介不能为空。"));
			return;
		}
		if (rp.getString("comments") == null) {
			res.end(Err("任务内容不能为空。"));
			return;
		}
		if (rp.getString("planDates") == null) {
			res.end(Err("任务时间不能为空。"));
			return;
		}
		String sql = "insert into itos_taskmodel(modelId,category,cycle,comments,planDates,oper,opdate,invalid,abstract) "
				+ " values(?,?,?,?,?,?,?,?,?)";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		params.add(rp.getString("category"));
		params.add(rp.getString("cycle"));
		params.add(rp.getString("comments"));
		params.add(rp.getString("planDates"));
		params.add("SYS");
		params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
		params.add("N");
		params.add(rp.getString("abs"));
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
