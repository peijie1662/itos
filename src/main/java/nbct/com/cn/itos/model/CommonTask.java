package nbct.com.cn.itos.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import nbct.com.cn.itos.config.TaskStatusEnum;
import util.DateUtil;

/**
 * 通用任务
 * 
 * @author PJ
 * @version 创建时间：2019年12月25日 上午10:44:30
 */
public class CommonTask {

	private String taskId;

	private TaskStatusEnum status;
	
	private String abs;

	private String content;

	private List<ItopUser> handler;

	private String customer;

	private String modelId;

	private LocalDateTime planDt;

	/**
	 * 生成新任务
	 * 
	 * @param content
	 * @return
	 */
	public static List<CommonTask> from(TimerTaskModel model) {
		// 1.初始化
		Supplier<CommonTask> createTask = () -> {
			CommonTask task = new CommonTask();
			task.setTaskId(UUID.randomUUID().toString());
			task.setStatus(TaskStatusEnum.CHECKIN);
			task.setAbs(model.getAbs());
			task.setContent(model.getComments());
			task.setCustomer("SYS");
			task.setModelId(model.getModelId());
			return task;
		};
		// 2.创建
		List<CommonTask> tasks = new ArrayList<CommonTask>();
		if (DateUtil.isEmpty(model.getPlanDates())) {
			throw new RuntimeException("未发现计划时间，无法生成计划任务。");
		}
		String[] dts = model.getPlanDates().split(",");
		switch (model.getCategory()) {
		case PERDAY:
			Arrays.asList(model.getPlanDates().split(",")).forEach(dt -> {
				CommonTask task = createTask.get();
				// 自定义每日格式 HHMM
				int hour = Integer.parseInt(dt.substring(0, 2));
				int min = Integer.parseInt(dt.substring(2));
				task.setPlanDt(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, min)));
				tasks.add(task);
			});
			break;
		case PERWEEK:
			if (dts.length % 2 != 0) {
				throw new RuntimeException("周计划的计划时间格式出错，无法生成计划任务。");
			}
			IntStream.range(0, dts.length / 2).forEach(i -> {
				// 日期
				int weekDay = Integer.parseInt(dts[i * 2]);// 每周第几天
				LocalDate curDt = LocalDate.now();
				LocalDate pd = DateUtil.getDateByYearAndWeekNumAndDayOfWeek(curDt.getYear(),
						curDt.get(ChronoField.ALIGNED_WEEK_OF_YEAR), weekDay);
				// 时间
				int hour = Integer.parseInt(dts[i * 2 + 1].substring(0, 2));
				int min = Integer.parseInt(dts[i * 2 + 1].substring(2));
				LocalTime pt = LocalTime.of(hour, min);
				// 组合
				CommonTask task = createTask.get();
				task.setPlanDt(LocalDateTime.of(pd, pt));
				tasks.add(task);
			});
			break;
		case PERMONTH:
			if (dts.length % 3 != 0) {
				throw new RuntimeException("月计划的计划时间格式出错，无法生成计划任务。");
			}
			IntStream.range(0, dts.length / 3).forEach(i -> {
				// 日期
				int week = Integer.parseInt(dts[i * 3]);// 每月第几周
				int day = Integer.parseInt(dts[i*3 + 1]);//周几
				LocalDate curDt = LocalDate.now();
				LocalDate pd = curDt.with(TemporalAdjusters.dayOfWeekInMonth(week, DayOfWeek.of(day)));
				// 时间
				int hour = Integer.parseInt(dts[i * 3 + 2].substring(0, 2));
				int min = Integer.parseInt(dts[i * 3 + 2].substring(2));
				LocalTime pt = LocalTime.of(hour, min);
				// 组合
				CommonTask task = createTask.get();
				task.setPlanDt(LocalDateTime.of(pd, pt));
				tasks.add(task);
			});
			break;
		default:
			System.out.println("default");
			break;
		}
		return tasks;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public TaskStatusEnum getStatus() {
		return status;
	}

	public void setStatus(TaskStatusEnum status) {
		this.status = status;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<ItopUser> getHandler() {
		return handler;
	}

	public void setHandler(List<ItopUser> handler) {
		this.handler = handler;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public LocalDateTime getPlanDt() {
		return planDt;
	}

	public void setPlanDt(LocalDateTime planDt) {
		this.planDt = planDt;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

}
