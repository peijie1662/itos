package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2019年12月31日 上午11:05:05
*/
public enum SceneEnum {
	
	SYSLOG("itos.sys.log", "系统日志"),
	ONLINEUSER("itos.sys.users","用户在线"),
	CONTROLCENTER("itos.controlcenter.msg","控制中心");

	private String value;

	private String desc;

	private SceneEnum(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public String value() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public boolean eq(String s) {
		return s.equals(this.value());
	}

	public static Optional<SceneEnum> from(String s) {
		return Arrays.asList(SceneEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}

}
