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
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.model.DispatchClient;

/**
 * @author PJ
 * @version 创建时间：2020年1月14日 下午2:20:05
 */
public class DispatchClientHandler {

	private static List<DispatchClient> clients = new ArrayList<DispatchClient>();

	/**
	 * 终端数据载入
	 */
	public void loadData(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		loadData();
		res.end(OK());//不考虑失败了
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
						throw new RuntimeException("load dispatch client data error!");
					}
					conn.close();
				});
			}
		});
	}

	/**
	 * 所有可下发终端
	 */
	public void getClientList(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		LocalDateTime ct = LocalDateTime.now();
		clients.forEach(client -> {
			client.setOnline(ct.minusSeconds(Configer.heartbeatThreshold).isBefore(client.getActiveTime()));
		});
		JsonArray cs = new JsonArray(clients);
		res.end(OK(cs));
	}

	/**
	 * 终端注册
	 */
	public void registe(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		JsonObject rp = ctx.getBodyAsJson();
		String serviceName = rp.getString("serviceName");
		Optional<DispatchClient> c = clients.stream().filter(client -> {
			return client.getServiceName().equals(serviceName);
		}).findAny();
		if (c.isPresent()) {
			c.get().setIp(rp.getString("ip"))//
			.setApiKey(rp.getJsonArray("apiKey"))//
			.setActiveTime(LocalDateTime.now());
			res.end(OK());
		}else{
			res.end(Err(serviceName + "没有登记，不能登录。"));
		}
	}

}
