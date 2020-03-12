package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;

import java.time.LocalDateTime;
import java.util.Objects;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.TimerTaskModel;
import util.DateUtil;
import util.MsgUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月26日 下午10:56:58
 */
public class ModelHandler {

	/**
	 * 组合任务模版列表
	 * 
	 * @param ctx
	 */
	public void getComposeModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel where invalid = 'N' "//
				+ " and category = 'COMPOSE' order by opdate";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	/**
	 * 非组合任务模版列表
	 * 
	 * @param ctx
	 */
	public void getNotComposeModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel where invalid = 'N' "//
				+ " and category <> 'COMPOSE' order by opdate";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	public void getTimerTaskModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel order by category,opdate";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	/**
	 * 更新模版
	 * @param ctx
	 */
	public void updateTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_taskmodel set comments = ?,planDates = ?,expired = ? where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("comments"));
		params.add(rp.getString("planDates"));
		params.add(rp.getString("expired"));
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
		String msg = DateUtil.curDtStr() + " " + "模版'" + rp.getString("abs") + "'已被修改。";
		MsgUtil.sysLog(ctx, msg);
	}

	/**
	 * 刪除模版
	 * @param ctx
	 */
	public void deleteTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete itos_taskmodel where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
		String msg = DateUtil.curDtStr() + " " + "模版'" + rp.getString("abs") + "'已被刪除。";
		MsgUtil.sysLog(ctx, msg);
	}

	/**
	 * 添加模版
	 * @param ctx
	 */
	public void addTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		String category = rp.getString("category");
		String abs = rp.getString("abs");
		String comments = rp.getString("comments");
		String cycle = rp.getString("cycle");
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
			if (cycle == null) {
				res.end(Err("执行周期不能为空。"));
				return;
			}
			if ((!"NONE".equals(cycle)) && Objects.isNull(planDates)) {
				res.end(Err("任务时间不能为空。"));
				return;
			}
		}
		String sql = "insert into itos_taskmodel(modelId,category,cycle,comments,planDates,oper,opdate,invalid,abstract,expired) "
				+ " values(?,?,?,?,?,?,?,?,?,?)";
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
		int expired = Objects.isNull(rp.getString("expired"))?24*60*60:Integer.parseInt(rp.getString("expired"));
		params.add(expired);
		JdbcHelper.update(ctx, sql, params);
		String msg = DateUtil.curDtStr() + " " + "新增模版'" + rp.getString("abs") + "'";
		MsgUtil.sysLog(ctx, msg);
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
