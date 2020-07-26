package nbct.com.cn.itos.handler;

import com.alibaba.fastjson.JSONObject;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import nbct.com.cn.itos.ConfigVerticle;

import static nbct.com.cn.itos.model.CallResult.OK;
import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.ConfigVerticle.CONFIG;

/**
 * 登录
 * 
 * @author PJ
 * @version 创建时间：2019年12月23日 上午8:52:01<br>
 * 
 * 暂时不用，用户登录先在ITOS数据库校验，以后再统一集成。
 */
public class LoginHandler {
	
	public void handleLogin(RoutingContext ctx) {
		Vertx vertx = ctx.vertx();
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		JsonObject params = ctx.getBodyAsJson();
		// 1.寻找登录服务
		try {
			WebClient webClient = WebClient.create(vertx);
			String regUrl = ConfigVerticle.getRegisterUrl() + "/provider/" + CONFIG.getString("loginServer");
			webClient.getAbs(regUrl).send(handle -> {
				if (handle.succeeded()) {
					JSONObject r = handle.result().bodyAsJson(JSONObject.class);
					if (r.getBoolean("flag")) {
						// 2.找到登录服务，尝试登录
						JSONObject provider = r.getJSONArray("data").getJSONObject(0);
						String loginUrl = "http://" + provider.getString("ip") + ":" + provider.getString("port")
								+ "/auth";
						webClient.postAbs(loginUrl).sendJsonObject(params, h -> {
							if (h.succeeded()) {
								JSONObject lr = h.result().bodyAsJson(JSONObject.class);
								if (lr.getBoolean("flag")) {
									//TODO 模拟返回用户信息
									JsonObject j = new JsonObject();
									j.put("flag", true);
									j.put("functionId", "100,200");
									j.put("userName", "裴捷");
									j.put("userId", "PJ");
									j.put("workId", "1038");
									j.put("deptName", "ITD");
									j.put("role", "admin");
									res.end(OK(j));
									//res.end(OK(JSONObject.parseObject(lr.getString("outMsg"))));
								} else {
									res.end(Err("访问登录服务出错:" + lr.getString("errMsg")));
								}
							} else {
								res.end(Err("无法访问登录服务:" + h.cause().getMessage()));
							}
						});
					} else {
						res.end(Err("访问注册出错:" + r.getString("errMsg")));
					}
				}
			});
		} catch (Exception e) {
			res.end(Err("登录出错:" + e.getMessage()));
		}
	}

}
