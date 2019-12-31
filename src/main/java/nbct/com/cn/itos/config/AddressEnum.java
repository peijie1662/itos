package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2019年12月31日 上午11:05:05
*/
public enum AddressEnum {
	
	SYSLOG("itos.sys.log", "系统日志");

	private String value;

	private String desc;

	private AddressEnum(String value, String desc) {
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

	public static Optional<AddressEnum> from(String s) {
		return Arrays.asList(AddressEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}

}
