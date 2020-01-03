package nbct.com.cn.itos.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.jdbc.TaskRowMapper;

/**
 * @author PJ
 * @version 创建时间：2019年12月28日 下午9:02:04
 */
public class TaskHandler {

	public void getExecuteTaskList(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_task where status = ? invalid = 'N' and apikey like '%?%' ";
		JsonArray params = new JsonArray();
		params.add(TaskStatusEnum.CHECKIN.getValue());
		params.add(rp.getString("apikey"));
		JdbcHelper.rows(ctx, sql, params);
	}

	public void getTaskList(RoutingContext ctx) {
		String sql = "select * from itos_task where invalid = 'N' order by opdate desc";
		JdbcHelper.rows(ctx, sql, new TaskRowMapper());
	}

	public void phoneAssociate(RoutingContext ctx) {

	}

}
