package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.TimerTaskModel;
import nbct.com.cn.itos.model.TimerTaskModelGroup;
import util.ConvertUtil;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2019年12月26日 下午10:56:58
 */
public class ModelHandler {

	/**
	 * 组合任务模版列表
	 */
	public void getComposeModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel where invalid = 'N' "//
				+ " and category = 'COMPOSE' order by opdate";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	/**
	 * 非组合任务模版列表
	 */
	public void getNotComposeModelList(RoutingContext ctx) {
		String sql = "select aa.*,bb.serviceName,bb.description as serviceDescription,bb.domain as serviceDomain from "
				+ //
				" (select * from itos_taskmodel where invalid = 'N' " + //
				" and category not in ('COMPOSE', 'BROADCAST'))  aa, " + //
				" (select a.modelId, b.serviceName, b.description, b.domain from (select * " + //
				" from itos_taskmodel where invalid = 'N' and category not in ('COMPOSE', 'BROADCAST')) a, " + //
				" itos_service b where instr(b.modelkey, a.modelId) > 0) bb " + //
				" where aa.modelId = bb.modelId(+) order by aa.opdate";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	/**
	 * 单个模版
	 */
	public void getTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_taskmodel where modelId = ?";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		JdbcHelper.rows(ctx, sql, params, new TimerTaskModel());
	}

	/**
	 * 所有模版列表
	 */
	public void getTimerTaskModelList(RoutingContext ctx) {
		String sql = "select * from itos_taskmodel order by groupId,orderInGroup";
		JdbcHelper.rows(ctx, sql, new TimerTaskModel());
	}

	/**
	 * 更新模版
	 */
	public void updateTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		JsonObject param = new JsonObject();
		param.put("modelId", rp.getString("modelId"));		
		param.put("comments", rp.getString("comments"));
		param.put("planDates", rp.getString("planDates"));
		param.put("expired", ConvertUtil.getInteger(rp.getInteger("expired"), 24 * 60 * 60));
		param.put("expiredCallback", rp.getString("callback"));
		param.put("expiredNotify", ConvertUtil.arrToString(rp.getJsonArray("notify")));
		param.put("startDate", DateUtil.toDateTimeStr(rp.getString("startDate")));
		param.put("compensate", ConvertUtil.boolToStr(rp.getBoolean("compensate")));
		String func = "{call itos.p_model_upd(?,?,?,?)}";
		JsonArray params = new JsonArray().add(param.encodePrettily());
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
		// 1.消息
		//String msg = String.format("%s::模版%s已修改", DateUtil.curDtStr(), rp.getString("abs"));
		//MsgUtil.sysLog(ctx, msg);
	}

	/**
	 * 刪除模版
	 */
	public void deleteTimerTaskModel(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete itos_taskmodel where modelId = ? ";
		JsonArray params = new JsonArray();
		params.add(rp.getString("modelId"));
		JdbcHelper.update(ctx, sql, params);
		//String msg = DateUtil.curDtStr() + " " + "模版'" + rp.getString("abs") + "'已被刪除。";
		//MsgUtil.sysLog(ctx, msg);
	}

	/**
	 * 添加模版
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
		String sql = "insert into itos_taskmodel(modelId,category,cycle,comments,planDates,oper,opdate," + //
				" invalid,abstract,expired,expiredCallback,expiredNotify,startDate,compensate) " + //
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
		params.add(ConvertUtil.getInteger(rp.getInteger("expired"), 24 * 60 * 60));
		params.add(rp.getString("callback"));
		params.add(ConvertUtil.arrToString(rp.getJsonArray("notify")));
		params.add(rp.getString("startDate"));
		params.add(ConvertUtil.boolToStr(rp.getBoolean("compensate")));
		JdbcHelper.update(ctx, sql, params);
		//String msg = DateUtil.curDtStr() + " " + "新增模版'" + rp.getString("abs") + "'";
		//MsgUtil.sysLog(ctx, msg);
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

	/**
	 * 修改模版分组信息 <br>
	 * 传入数组 [{modelId:'xx',groupId:'xx',index:xx},...]
	 */
	public void chgModelGroup(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		List<String> sqls = rp.getJsonArray("models").stream().map(item -> {
			JsonObject jo = JsonObject.mapFrom(item);
			String modelId = jo.getString("modelId");
			String groupId = jo.getString("groupId");
			Integer index = jo.getInteger("index");
			String sql = "update itos_taskmodel set groupId = '" + groupId + "',orderInGroup = " + String.valueOf(index)
					+ " where modelId = '" + modelId + "'";
			return sql;
		}).collect(Collectors.toList());
		JdbcHelper.batchUpdate(ctx, sqls);
	}

	/**
	 * 添加分组
	 */
	public void addGroup(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "insert into itos_taskmodelgroup(groupId,groupName,groupOrder,groupDesc) values(?,?,?,?)";
		JsonArray params = new JsonArray()//
				.add(UUID.randomUUID().toString())//
				.add(rp.getString("groupName"))//
				.add(rp.getInteger("groupOrder"))//
				.add(rp.getString("groupDesc"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 删除分组
	 */
	public void delGroup(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String groupId = rp.getString("groupId");
		List<String> sqls = new ArrayList<String>();
		sqls.add("delete from itos_taskmodelgroup where groupId = '" + groupId + "'");
		sqls.add("update itos_taskmodel set groupId = '' where groupId = '" + groupId + "'");
		JdbcHelper.batchUpdate(ctx, sqls);
	}

	/**
	 * 分组列表
	 */
	public void getGroups(RoutingContext ctx) {
		String sql = "select * from itos_taskmodelgroup order by groupOrder";
		JdbcHelper.rows(ctx, sql, new TimerTaskModelGroup());
	}

	/**
	 * 分组排序['id1','id2',...]
	 */
	public void sortingGroup(RoutingContext ctx) {
		JsonArray gps = ctx.getBodyAsJsonArray();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		List<String> sqls = new ArrayList<String>();
		for (int i = 0; i <= gps.size() - 1; i++) {
			sqls.add("update itos_taskmodelgroup set grouporder = " + i + //
					" where groupId = '" + gps.getString(i) + "'");
		}
		JdbcHelper.batchUpdate(ctx, sqls);
	}

}
