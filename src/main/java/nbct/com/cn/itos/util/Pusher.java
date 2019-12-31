package nbct.com.cn.itos.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import nbct.com.cn.itos.config.AddressEnum;

/**
 * @author PJ
 * @version 创建时间：2019年12月31日 下午12:20:02
 */
public class Pusher {

	public static void sendLog(Vertx vertx, String log) {
		EventBus eb = vertx.eventBus();
		log = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + log;
		eb.send(AddressEnum.SYSLOG.getValue(), log);
	}

}
