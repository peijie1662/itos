package nbct.com.cn.itos;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.model.ItosUser;

import static nbct.com.cn.itos.model.CallResult.OK;

/**
 * @author PJ
 * @version 创建时间：2019年12月29日 下午7:21:58
 */
public class WebsocketVerticle extends AbstractVerticle {

	private final static Map<String, ItosUser> onlineUsers = new ConcurrentHashMap<>();

	@Override
	public void start() throws Exception {
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route("/").handler(routingContext -> {
			routingContext.response().sendFile("html/ws.html");
		});
		websocketMethod(server);
		server.requestHandler(router).listen(Configer.getWebsocketPort());
		EventBus es = vertx.eventBus();
		// 系统日志场景
		es.consumer(SceneEnum.SYSLOG.addr(), this::pushSysLog);
		// 控制中心场景
		es.consumer(SceneEnum.CONTROLCENTER.addr(), this::pushControlCenter);
		// 在线用户请求
		es.consumer(SceneEnum.ONLINEUSER.addr(), this::onlineUsers);
	}

	/**
	 * 推送系统日志给'系统日志场景'用户
	 */
	private void pushSysLog(Message<String> msg) {
		onlineUsers.forEach((id, user) -> {
			if (user.getScene().contains(SceneEnum.SYSLOG)) {
				user.getWs().writeFinalTextFrame(msg.body());
			}
		});
	}

	/**
	 * 推送控制中心消息给控制'中心场景'用户
	 */
	private void pushControlCenter(Message<String> msg) {
		onlineUsers.forEach((id, user) -> {
			if (user.getScene().contains(SceneEnum.CONTROLCENTER)) {
				user.getWs().writeFinalTextFrame(msg.body());
			}
		});
	}

	/**
	 * 请求在线用户
	 */
	public void onlineUsers(Message<String> msg) {
		List<JsonObject> list = onlineUsers.values().stream().map(item -> {
			return JsonObject.mapFrom(item);
		}).collect(Collectors.toList());
		msg.reply(new JsonArray(list));
	}

	/**
	 * 处理页面消息
	 */
	public void websocketMethod(HttpServer server) {
		server.websocketHandler(webSocket -> {
			String id = webSocket.binaryHandlerID();
			if (!onlineUsers.containsKey(id)) {
				onlineUsers.put(id, new ItosUser().setWs(webSocket));
			}
			webSocket.frameHandler(handler -> {

				System.out.println("passing in: " + handler.textData());
				
				try {
					String[] clientMsg = handler.textData().split("\\^");
					if (clientMsg.length >= 2) {
						String header = clientMsg[0];
						JsonObject j = new JsonObject(clientMsg[1]);
						ItosUser user = onlineUsers.get(id);
						List<SceneEnum> scenes = j.getJsonArray("scenes").stream().map(item -> {
							return SceneEnum.absFrom(item.toString()).get();
						}).collect(Collectors.toList());
						switch (header) {
						// 用户登录时记录在线信息
						case "USERLOGIN":
							user.setUserId(j.getString("userId"));
							user.setUserName(j.getString("userName"));
							user.setDepartment(j.getString("department"));
							user.setPhone(j.getString("phone"));
							user.setShortPhone(j.getString("shortPhone"));
							user.setRole(j.getString("role"));
							user.setScene(scenes);
							break;
						// 用户的场景发生转换	
						case "USERSCENE":
							user.setScene(scenes);
							break;
						default:
							System.out.println("OTHERS WEBSOCKET HEADER:" + header);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			webSocket.closeHandler(handler -> onlineUsers.remove(id));
		});
	}

}
