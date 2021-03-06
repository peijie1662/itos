package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author PJ
 * @version 创建时间：2019年12月24日 下午4:40:57
 */
public enum CategoryEnum {

	COMMON("COMMON", "人工来执行的普通任务"), //
	CMD("CMD", "下发终端的命令行任务"), //
	PROCEDURE("PROCEDURE", "下发终端的调用存储过程任务"), //
	APPSERVER("APPSERVER", "调用其它服务执行任务"), //
	CUSTOM("CUSTOM", "下发终端的自定义任务"), //
	COMPOSE("COMPOSE", "组合任务"), //
	BROADCAST("BROADCAST", "广播任务"), //
	SYSTEM("SYSTEM", "系统执行任务");//

	private String value;

	private String desc;

	private CategoryEnum(String value, String desc) {
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

	public static Optional<CategoryEnum> from(String s) {
		return Arrays.asList(CategoryEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}
	
	public String toString() {
		return value;
	}

}
