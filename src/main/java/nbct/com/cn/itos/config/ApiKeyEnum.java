package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author PJ
 * @version 创建时间：2019年12月24日 下午4:40:57
 */
public enum ApiKeyEnum {

	EHRTASK("EHRTASK", "Ehr任务客户端"); //

	private String value;

	private String desc;

	private ApiKeyEnum(String value, String desc) {
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

	public static Optional<ApiKeyEnum> from(String s) {
		return Arrays.asList(ApiKeyEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}

}
