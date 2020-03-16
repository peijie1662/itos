package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2020年3月15日 上午11:28:18
*/
public enum NotifyEnum {

	SMS("SMS", "手机短信"), //
	ITOSMES("ITOSMES","ITOS消息"),//
	BIGHORN("BIGHORN","大喇叭");

	private String value;

	private String desc;

	private NotifyEnum(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public boolean eq(String s) {
		return s.equals(this.getValue());
	}

	public static Optional<NotifyEnum> from(String s) {
		return Arrays.asList(NotifyEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}
	
}
