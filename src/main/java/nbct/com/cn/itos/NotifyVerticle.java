package nbct.com.cn.itos;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import nbct.com.cn.itos.config.Configer;
import nbct.com.cn.itos.config.SceneEnum;
import nbct.com.cn.itos.model.TSmsQueue;
import util.ConvertUtil;

/**
 * @author PJ
 * @version 创建时间：2020年5月2日 下午8:30:40
 */
public class NotifyVerticle extends AbstractVerticle {

	public static Logger logger = Logger.getLogger(NotifyVerticle.class);

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
		JsonObject smsServer = Configer.smsServer;
		WebClient wc = WebClient.create(vertx,
				new WebClientOptions().setIdleTimeout(2).setConnectTimeout(2000).setMaxWaitQueueSize(5));
		try {
			wc.get(smsServer.getInteger("port"), //
					smsServer.getString("ip"), //
					smsServer.getString("sendUrl")) //
					.addQueryParam("jsonStr", JSON.toJSONString(sqs))//
					.send(r -> {
						if (r.failed()) {
							r.cause().printStackTrace();
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发短信
	 * 
	 * @param msg
	 */
	private void sendSms(Message<JsonObject> msg) {
		System.out.println("SMS: " + msg.body());

		SQLClient client = Configer.client;
		String sql = "select * from itos_user where userid in ('PJ','LSH','XZL','WMH')";
		String content = String.format("计划%s执行的任务%s已超时,%s", //
				msg.body().getString("planDt"), //
				msg.body().getString("abs"), //
				msg.body().getString("expiredDesc"));
		client.getConnection(cr -> {
			if (cr.succeeded()) {
				SQLConnection connection = cr.result();
				if (connection != null) {
					connection.queryWithParams(sql, null, qr -> {
						if (qr.succeeded()) {
							List<TSmsQueue> sqs = qr.result().getRows().stream().filter(row -> {
								return !ConvertUtil.emptyOrNull(row.getString("PHONE"));
							}).map(row -> {
								TSmsQueue sq = new TSmsQueue();
								sq.setPhone(row.getString("PHONE"));
								sq.setContent(content);
								sq.setEnvironment("OFFICE");
								return sq;
							}).collect(Collectors.toList());
							callSmsSend(sqs);
						}
						connection.close();
					});
				}
			}
		});

	}

	/**
	 * 大喇叭
	 * 
	 * @param msg
	 */
	private void sendBigHorn(Message<JsonObject> msg) {
		System.out.println(msg.body());
	}

}
