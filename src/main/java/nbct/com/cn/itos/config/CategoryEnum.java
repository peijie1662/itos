package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author PJ
 * @version 创建时间：2019年12月24日 下午4:40:57
 */
public enum CategoryEnum {

	PERDAY("PERDAY", "每日任务"), //
	PERWEEK("PERWEEK","每周任务"), //
	PERMONTH("PERMONTH","每月任务"); //

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

}
