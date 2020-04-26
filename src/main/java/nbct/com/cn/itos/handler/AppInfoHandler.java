package nbct.com.cn.itos.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.model.AppInfo;

import static nbct.com.cn.itos.model.CallResult.Err;
import static nbct.com.cn.itos.model.CallResult.OK;

/**
 * @author PJ
 * @version 创建时间：2020年4月21日 下午12:14:44
 */
public class AppInfoHandler {

	/**
	 * 遗产服务
	 */
	private static List<AppInfo> LEGACY_APPS = new ArrayList<AppInfo>();

	/**
	 * 新服务
	 */
	private static List<AppInfo> NEW_APPS = new ArrayList<AppInfo>();

	/**
	 * 设置Legacy服务信息，终端调用
	 */
	public void setLegacyAppInfo(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		try {
			LEGACY_APPS = ctx.getBodyAsJsonArray().stream().map(item -> {
				AppInfo app = AppInfo.from(JsonObject.mapFrom(item));
				app.setType("DELPHI");
				return app;
			}).collect(Collectors.toList());
			res.end(OK());
		} catch (Exception e) {
			res.end(Err(e.getMessage()));
		}
	}
	
	/**
	 * 设置New服务信息，定时服务调用
	 */
	public void setNewAppInfo(Message<String> msg) {
		
		
		
		//System.out.println("hahaha");
	}

	/**
	 * 服务列表
	 */
	public void getAppInfoList(RoutingContext ctx) {
		HttpServerResponse res = ctx.response();
		res.putHeader("content-type", "application/json");
		ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
		apps.addAll(LEGACY_APPS);
		apps.addAll(NEW_APPS);
		res.end(OK(apps));
	}

}
