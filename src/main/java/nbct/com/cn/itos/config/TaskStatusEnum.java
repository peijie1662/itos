package nbct.com.cn.itos.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author PJ
 * @version 创建时间：2019年12月25日 下午12:15:05
 */
public enum TaskStatusEnum {

	CHECKIN("CHECKIN", "登记任务", 1), //
	PROCESSING("PROCESSING", "处理任务", 2), //
	MODIFY("MODIFY", "修改任务内容", -1), //
	SWAP("SWAP", "任务处理人更换", -1), //
	CANCEL("CANCEL", "取消任务", 3), //
	DONE("DONE", "任务完成", 3),//
	FAIL("FAIL","任务失败",3);

	private String value;

	private String desc;

	private int order;

	private TaskStatusEnum(String value, String desc, int order) {
		this.value = value;
		this.desc = desc;
		this.order = order;
	}

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * 顺序在给定状态之前或并列
	 * 
	 * @param status
	 * @return
	 */
	public boolean isPreviousOrParalleling(String status) {
		return this.getOrder() <= TaskStatusEnum.from(status).get().getOrder();
	}
	
	/**
	 * 顺序在给定状态之后或并列
	 * 
	 * @param status
	 * @return
	 */
	public boolean isAfterOrParalleling(String status) {
		return this.getOrder() >= TaskStatusEnum.from(status).get().getOrder();
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
