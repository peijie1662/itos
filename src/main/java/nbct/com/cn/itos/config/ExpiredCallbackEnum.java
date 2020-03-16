package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2020年3月15日 上午10:55:23
*/
public enum ExpiredCallbackEnum {
	
	DONE("DONE", "视作完成"), //
	CANCEL("CANCEL","视作取消"), //
	NONE("NONE","不处理");

	private String value;

	private String desc;

	private ExpiredCallbackEnum(String value, String desc) {
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

	public static Optional<ExpiredCallbackEnum> from(String s) {
		return Arrays.asList(ExpiredCallbackEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}

}
