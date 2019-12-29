package nbct.com.cn.itos.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.ModelRowMapper;
import util.DateUtil;

import static nbct.com.cn.itos.model.CallResult.Err;

/**
 * @author PJ
 * @version 创建时间：2019年12月26日 下午10:56:58
 */
public class ModelHandler {

	public void getTimerTaskModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel where invalid = 'N' order by category,opdate";
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
	}

	public void deleteTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_taskmodel set invalid='Y' where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
	}

	public void addTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		// 检查
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		if (rp.getString("category") == null) {
			res.end(Err("任务类别不能为空。"));
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
		// 保存
		String sql = "insert into itos_taskmodel(modelId,category,comments,planDates,oper,opdate,invalid,abstract) "
				+ " values(?,?,?,?,?,?,?,?)";
		JsonArray params = new JsonArray();
		params.add(UUID.randomUUID().toString());
		params.add(rp.getString("category"));
		params.add(rp.getString("comments"));
		params.add(rp.getString("planDates"));
		params.add("SYS");
		params.add(DateUtil.localToUtcStr(LocalDateTime.now()));
		params.add("N");
		params.add(rp.getString("abs"));
		JdbcHelper.update(ctx, sql, params);
	}

}
