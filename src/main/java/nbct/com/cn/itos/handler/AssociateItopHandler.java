package nbct.com.cn.itos.handler;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import nbct.com.cn.itos.config.Configer;

/**
 * @author PJ
 * @version 创建时间：2020年1月20日 上午10:52:19
 */
public class AssociateItopHandler {

	/**
	 * 电话关联数据
	 */
	public void phoneAssociate(RoutingContext ctx) {

	}

	/**
	 * 用户名
	 */
	private String getUsers(JSONArray users) {
		if (Objects.nonNull(users)) {
			String re = users.stream().map(item -> {
				return JsonObject.mapFrom(item).getString("contact_id_friendlyname").replace(" ", "");
			}).collect(Collectors.joining(","));
			return re;
		} else {
			return "";
		}
	}

	/**
	 * IP
	 */
	private String getIp(JSONArray ips) {
		if (Objects.nonNull(ips)) {
			return JsonObject.mapFrom(ips.get(0)).getString("name");
		} else {
			return "";
		}
	}

	/**
	 * 设备号关联数据
	 */
	public void machineNameAssociate(RoutingContext ctx) {
		Vertx vertx = ctx.vertx();
		JsonObject rp = ctx.getBodyAsJson();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			// 1.寻找ITOP服务
			String machineName = rp.getString("machineName").toUpperCase();
			WebClient webClient = WebClient.create(vertx);
			String regUrl = Configer.getRegisterUrl() + "/provider/" + Configer.itopServer;
			webClient.getAbs(regUrl).send(handle -> {
				if (handle.succeeded()) {
					JSONObject r = handle.result().bodyAsJson(JSONObject.class);
					if (r.getBoolean("flag")) {
						// 2.找到ITOP服务
						JSONObject provider = r.getJSONArray("data").getJSONObject(0);
						String loginUrl = "http://" + provider.getString("ip") + ":" + provider.getInteger("port")
								+ "/api/ITop/CI/name/" + machineName;
						webClient.postAbs(loginUrl).sendJsonObject(null, h -> {
							if (h.succeeded()) {
								if (h.result().bodyAsString().startsWith("{}")) {
									res.end(Err());
								} else {
									JSONObject lr = h.result().bodyAsJson(JSONObject.class);
									JsonObject j = new JsonObject();
									if (machineName.startsWith("H") || machineName.startsWith("F-H")
											|| machineName.startsWith("D") || machineName.startsWith("F-D")
											|| machineName.startsWith("P") || machineName.startsWith("F-P")) {
										j.put("machineName", machineName);
										j.put("userName", getUsers(lr.getJSONArray("contacts_list")));
										j.put("location", lr.getString("detailaddr"));
										j.put("modelName", lr.getString("model_name"));
										j.put("ip", getIp(lr.getJSONArray("physicalinterface_list")));
									} else {
										j.put("machineName", lr.getString("name"));
										j.put("driverName", lr.getString("drivername"));
										j.put("driverPhone", lr.getString("driverphone"));
										j.put("machineIp", lr.getString("machineryip"));
										j.put("machineNo", lr.getString("machinerypc"));
									}
									res.end(OK(j));
								}
							} else {
								res.end(Err("无法访问ITOP服务:" + h.cause().getMessage()));
							}
						});
					} else {
						res.end(Err("访问注册出错:" + r.getString("errMsg")));
					}
				}
			});
		} catch (Exception e) {
			res.end(Err("ITOP出错:" + e.getMessage()));
		}
	}

}
