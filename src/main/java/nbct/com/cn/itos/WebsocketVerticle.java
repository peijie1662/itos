package nbct.com.cn.itos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.model.ItosUser;

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
		es.consumer(SceneEnum.SYSLOG.value(), this::pushSysLog);
		es.consumer(SceneEnum.CONTROLCENTER.value(), this::pushControlCenter);
	}

	/**
	 * 自动任务生成日志推送给管理员  //TODO 做消息匹配
	 * @param msg
	 */
	private void pushSysLog(Message<String> msg) {
		onlineUsers.forEach((id, user) -> {
			if ("ADMIN".equals(user.getRole())) {
				user.getWs().writeFinalTextFrame(msg.body());
			}
		});
	}

	/**
	 * 控制中心消息推送给所有用户
	 * @param msg
	 */
	private void pushControlCenter(Message<String> msg) {
		onlineUsers.forEach((id, user) -> {
			user.getWs().writeFinalTextFrame(msg.body());
		});
	}

	public void websocketMethod(HttpServer server) {
		server.websocketHandler(webSocket -> {
			String id = webSocket.binaryHandlerID();
			if (!onlineUsers.containsKey(id)) {
				onlineUsers.put(id, new ItosUser().setWs(webSocket));
			}
			webSocket.frameHandler(handler -> {
				String[] clentMsg = handler.textData().split("\\^");
				String header = clentMsg[0];
				switch (header) {
				case "USERLOGIN":
					// 按照ID补充用户信息
					JsonObject j = new JsonObject(clentMsg[1]);
					ItosUser user = onlineUsers.get(id);
					user.setUserId(j.getString("userId"));
					user.setUserName(j.getString("userName"));
					user.setRole(j.getString("role"));
					break;
				default:
					System.out.println("OTHERS WEBSOCKET HEADER.");
				}
			});
			webSocket.closeHandler(handler -> onlineUsers.remove(id));
		});
	}

}
