package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.CommonTask;
import nbct.com.cn.itos.model.DispatchClient;

/**
 * @author PJ
 * @version 创建时间：2020年1月14日 下午2:20:05
 */
public class DispatchClientHandler {

	private static List<DispatchClient> clients = new ArrayList<DispatchClient>();

	/**
	 * 终端数据载入(页面来刷新)
	 */
	public void loadData(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		loadData();
		res.end(OK());// 不考虑失败了
	}

	/**
	 * 终端数据载入
	 */
	public void loadData() {
		SQLClient client = Configer.client;
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection conn = cr.result();
				conn.query("select * from itos_service", r -> {
					if (r.succeeded()) {
						clients.clear();
						clients.addAll(r.result().getRows().stream().map(row -> {
							return new DispatchClient().from(row);
						}).collect(Collectors.toList()));
					} else {
						r.cause().printStackTrace();
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 登记终端
	 */
	public void addClient(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String func = "{call itos.p_addclient(?,?,?,?)}";
		JsonArray params = new JsonArray().add(rp.getString("serviceName") + "^" + rp.getString("modelKey") + "^"
				+ rp.getString("description") + "^" + rp.getString("remark1") + "^" + rp.getString("remark2"));
		JsonArray outputs = new JsonArray().addNull().add("VARCHAR").add("VARCHAR").add("VARCHAR");
		JdbcHelper.call(ctx, func, params, outputs);
	}

	/**
	 * 修改终端
	 */
	public void updateClient(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "update itos_service set description = ?,modelKey = ?,remark1 = ?, remark2 = ? "//
				+ "where serviceName = ?";
		JsonArray params = new JsonArray()//
				.add(rp.getString("description"))//
				.add(rp.getString("modelKey"))//
				.add(rp.getString("remark1"))//
				.add(rp.getString("remark2"))//
				.add(rp.getString("serviceName"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 删除终端
	 */
	public void deleteClient(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "delete from itos_service where serviceName = ?";
		JsonArray params = new JsonArray()//
				.add(rp.getString("serviceName"));
		JdbcHelper.update(ctx, sql, params);
	}

	/**
	 * 查找终端信息（终端访问）<br>
	 * 终端访问参数 {serviceName:"..."}
	 */
	public void getClient(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String sql = "select * from itos_service where serviceName = ?";
		JsonArray params = new JsonArray().add(rp.getString("serviceName"));
		JdbcHelper.oneRow(ctx, sql, params, new DispatchClient());
	}

	/**
	 * 所有可下发终端
	 */
	public void getClientList(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		LocalDateTime now = LocalDateTime.now();
		try {
			clients.forEach(client -> {
				client.setOnLine(now.minusSeconds(Configer.heartbeatThreshold).isBefore(client.getActiveTime()));
			});
			JsonArray cs = new JsonArray(clients);
			res.end(OK(cs));
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
		}
	}

	/**
	 * 所有终端任务列表(页面访问)
	 */
	public void getDispatchAllTask(RoutingContext ctx) {
		String sql = "select * from itos_task where category in (?,?,?) and invalid = 'N' and composeId is null";
		JsonArray params = new JsonArray();
		params.add(CategoryEnum.CMD.getValue());
		params.add(CategoryEnum.PROCEDURE.getValue());
		params.add(CategoryEnum.CUSTOM.getValue());
		JdbcHelper.rows(ctx, sql, params, new CommonTask());
	}

	/**
	 * 终端任务列表(终端访问)<br>
	 * 终端参数 {serviceName:"...",ip:"..."}
	 */
	public void getDispatchTaskList(RoutingContext ctx) {
		JsonObject rp = ctx.getBodyAsJson();
		String serviceName = rp.getString("serviceName");
		// 1.更新在线状态
		Optional<DispatchClient> o = clients.stream().filter(client -> {
			return client.getServiceName().equals(serviceName);
		}).findAny();
		if (o.isPresent()) {
			DispatchClient c = o.get();
			c.setIp(rp.getString("ip"));
			c.setActiveTime(LocalDateTime.now());
		}
		// 2.终端对应任务
		String sql = "select * from itos_task where status = 'CHECKIN' and invalid = 'N'" + //
				" and instr((select modelKey from itos_service where servicename= ? ),modelId ) > 0";
		JsonArray params = new JsonArray();
		params.add(serviceName);
		JdbcHelper.rows(ctx, sql, params, new CommonTask());
	}

}
