package nbct.com.cn.itos;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import static nbct.com.cn.itos.ConfigVerticle.CONFIG;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.jdbc.JdbcHelper;
import nbct.com.cn.itos.model.ItosUser;
import nbct.com.cn.itos.model.TSmsQueue;

/**
 * @author PJ
 * @version 创建时间：2020年5月2日 下午8:30:40
 */
public class NotifyVerticle extends AbstractVerticle {

	public static Logger log = LogManager.getLogger(NotifyVerticle.class);

	@Override
	public void start() throws Exception {
		EventBus es = vertx.eventBus();
		es.consumer(SceneEnum.SMS.addr(), this::sendSms);
		es.consumer(SceneEnum.BIGHORN.addr(), this::sendBigHorn);
	}

	/*
	 * 调用短信服务
	 */
	private void callSmsSend(List<TSmsQueue> sqs) {
		JsonObject smsServer = CONFIG.getJsonObject("smsServer");
		WebClient wc = WebClient.create(vertx,
				new WebClientOptions().setIdleTimeout(2).setConnectTimeout(2000).setMaxWaitQueueSize(5));
		try {
			wc.get(smsServer.getInteger("port"), //
					smsServer.getString("ip"), //
					smsServer.getString("sendUrl")) //
					.addQueryParam("jsonStr", JSON.toJSONString(sqs))//
					.send(r -> {
						if (r.failed()) {
							log.error("CALLSMSSEND-01::", r.cause());
						} else {
							log.info("CALLSMSSEND-02::" + JSON.toJSONString(sqs));
						}
					});
		} catch (Exception e) {
			log.error("CALLSMSSEND-03::", e);
		}
	}

	/**
	 * 发短信
	 * 
	 * @param msg
	 */
	private void sendSms(Message<JsonObject> msg) {
		String category = msg.headers().get("CATEGORY");
		// 1.读取短信用户
		String sql = "select * from itos_user where phone is not null and instr(subscription,?)>0";
		JsonArray params = new JsonArray().add(category);
		Future<List<ItosUser>> f = JdbcHelper.rows(sql, params, new ItosUser());
		f.onSuccess(users -> {
			String content = null;
			// 2.超期信息
			if ("EXPIRED".equals(category)) {
				content = String.format("计划%s执行的任务%s已超时,%s", //
						msg.body().getString("planDt"), //
						msg.body().getString("abs"), //
						msg.body().getString("expiredDesc"));
			}
			// 3.比对信息
			if ("COMPARE".equals(category)) {
				content = msg.body().getString("msg");
			}
			// 4.发短信
			final String c = content;
			if (content != null) {
				List<TSmsQueue> sqs = users.stream().map(user -> {
					TSmsQueue sq = new TSmsQueue();
					sq.setPhone(user.getPhone());
					sq.setContent(c);
					sq.setEnvironment("OFFICE");
					return sq;
				}).collect(Collectors.toList());
				callSmsSend(sqs);
			}
		}).onFailure(e -> {
			log.error("SENDSMS-01::", e);
		});
	}

	/**
	 * 大喇叭
	 * 
	 * @param msg
	 */
	private void sendBigHorn(Message<JsonObject> msg) {
		log.info("需喇叭广播内容:" + msg.body());
	}

}
