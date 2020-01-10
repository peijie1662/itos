package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2019年12月25日 下午12:15:05
*/
public enum TaskStatusEnum {
	
	CHECKIN("CHECKIN", "登记任务"), //
	PROGRESSING("PROGRESSING","处理任务"), //
	MODIFY("MODIFY","修改任务内容"), //
	SWAP("SWAP","任务处理人更换"),//
	CANCEL("CANCEL","取消任务"),//
	SUCCESS("SUCCESS","任务成功"),//
	FAIL("FAIL","任务失败"); //

	private String value;

	private String desc;

	private TaskStatusEnum(String value, String desc) {
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

	public static Optional<TaskStatusEnum> from(String s) {
		return Arrays.asList(TaskStatusEnum.values()).stream().filter(item -> {
			return item.eq(s);
		}).findAny();
	}

}
