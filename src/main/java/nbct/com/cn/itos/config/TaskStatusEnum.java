package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
* @author PJ 
* @version 创建时间：2019年12月25日 下午12:15:05
*/
public enum TaskStatusEnum {
	
	CHECKIN("CHECKIN", "登记任务"), //
	PROCESSING("PROCESSING","处理任务"), //
	MODIFY("MODIFY","修改任务内容"), //
	SWAP("SWAP","任务处理人更换"),//
	CANCEL("CANCEL","取消任务"),//
	DONE("DONE","任务完成");//

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
