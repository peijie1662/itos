package nbct.com.cn.itos.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.ExpiredCallbackEnum;
import nbct.com.cn.itos.config.NotifyEnum;
import nbct.com.cn.itos.config.TaskStatusEnum;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.ConvertUtil;
import util.DateUtil;

/**
 * 通用任务
 * 
 * @author PJ
 * @version 创建时间：2019年12月25日 上午10:44:30
 */
public class CommonTask implements RowMapper<CommonTask> {

	private String taskId;

	private CategoryEnum category;

	private TaskStatusEnum status;

	private String abs;

	private String content;

	private List<String> keys;

	private List<String> handler;

	private String phone;

	private String location;

	private String customer;

	private String modelId;

	private LocalDateTime planDt;

	private String taskIcon;

	private String composeId;

	private LocalDateTime expiredTime;

	private ExpiredCallbackEnum callback;

	private List<NotifyEnum> notify;

	private boolean executedCallback;

	private boolean executedNotify;

	public CommonTask from(JsonObject j) {
		try {
			CommonTask task = new CommonTask();
			task.setTaskId(j.getString("TASKID"));
			task.setAbs(j.getString("ABSTRACT"));
			task.setCategory(CategoryEnum.from(j.getString("CATEGORY")).get());
			task.setStatus(TaskStatusEnum.from(j.getString("STATUS")).get());
			task.setContent(j.getString("CONTENT"));
			task.setHandler(ConvertUtil.strToList(j.getString("HANDLER")));
			task.setKeys(ConvertUtil.strToList(j.getString("KEYS")));
			task.setPhone(j.getString("PHONE"));
			task.setLocation(j.getString("LOCATION"));
			task.setCustomer(j.getString("CUSTOMER"));
			task.setTaskIcon(j.getString("TASKICON"));
			task.setModelId(j.getString("MODELID"));
			task.setComposeId(j.getString("COMPOSEID"));
			task.setPlanDt(DateUtil.utcToLocalDT(j.getString("PLANDT")));
			task.setExpiredTime(DateUtil.utcToLocalDT(j.getString("EXPIREDTIME")));
			// 超期回调
			ExpiredCallbackEnum callback = Objects.nonNull(j.getString("EXPIREDCALLBACK"))
					? ExpiredCallbackEnum.from(j.getString("EXPIREDCALLBACK")).get()
					: ExpiredCallbackEnum.NONE;
			task.setCallback(callback);
			// 超期通知
			String notify = j.getString("EXPIREDNOTIFY");
			if (Objects.nonNull(notify)) {
				List<NotifyEnum> n = Arrays.asList(notify.split(",")).stream().map(item -> {
					return NotifyEnum.from(item).get();
				}).collect(Collectors.toList());
				task.setNotify(n);
			} else {
				task.setNotify(Collections.emptyList());
			}
			// 标记
			task.setExecutedCallback(ConvertUtil.strToBool(j.getString("EXECUTEDCALLBACK")));
			task.setExecutedNotify(ConvertUtil.strToBool(j.getString("EXECUTEDNOTIFY")));
			return task;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("后台数据转换成任务时发生错误。");
		}
	}

	/**
	 * 临时生成新任务
	 */
	public static CommonTask from(TimerTaskModel model, LocalDateTime planDt) {
		if (model.isInvalid())
			throw new RuntimeException("模版处于无效状态，不能生成任务。");
		CommonTask task = new CommonTask();
		task.setTaskId(UUID.randomUUID().toString());
		task.setCategory(model.getCategory());
		task.setStatus(TaskStatusEnum.CHECKIN);
		task.setAbs(model.getAbs());
		task.setContent(model.getComments());
		task.setCustomer("SYS");
		task.setModelId(model.getModelId());
		task.setTaskIcon("AUTO");// 机器人，代表系统生成任务
		task.setPlanDt(planDt);
		task.setExpiredTime(task.getPlanDt().minusSeconds(-model.getExpired()));// 这里肯定有planDt
		task.setCallback(model.getCallback());
		task.setNotify(model.getNotify());
		task.setExecutedCallback(false);
		task.setExecutedNotify(false);
		return task;
	}

	/**
	 * 生成新任务
	 * 
	 * @param content
	 * @return
	 */
	public static List<CommonTask> from(TimerTaskModel model) {
		if (model.isInvalid())
			throw new RuntimeException("模版处于无效状态，不能生成任务。");
		// 1.初始化
		Supplier<CommonTask> createTask = () -> {
			CommonTask task = new CommonTask();
			task.setTaskId(UUID.randomUUID().toString());
			task.setCategory(model.getCategory());
			task.setStatus(TaskStatusEnum.CHECKIN);
			task.setAbs(model.getAbs());
			task.setContent(model.getComments());
			task.setCustomer("SYS");
			task.setModelId(model.getModelId());
			task.setTaskIcon("AUTO");// 机器人，代表系统生成任务
			task.setCallback(model.getCallback());
			task.setNotify(model.getNotify());
			task.setExecutedCallback(false);
			task.setExecutedNotify(false);
			return task;
		};
		// 2.创建
		List<CommonTask> tasks = new ArrayList<CommonTask>();
		if (DateUtil.isEmpty(model.getPlanDates())) {
			throw new RuntimeException("未发现计划时间，无法生成计划任务。");
		}
		String[] dts = model.getPlanDates().split(",");
		switch (model.getCycle()) {
		case PERDAY:
			Arrays.asList(model.getPlanDates().split(",")).forEach(dt -> {
				CommonTask task = createTask.get();
				// 自定义每日格式 HHMM
				int hour = Integer.parseInt(dt.substring(0, 2));
				int min = Integer.parseInt(dt.substring(2));
				task.setPlanDt(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, min)));
				task.setExpiredTime(task.getPlanDt().minusSeconds(-model.getExpired()));
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
				task.setExpiredTime(task.getPlanDt().minusSeconds(-model.getExpired()));
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
				int day = Integer.parseInt(dts[i * 3 + 1]);// 周几
				LocalDate curDt = LocalDate.now();
				LocalDate pd = curDt.with(TemporalAdjusters.dayOfWeekInMonth(week, DayOfWeek.of(day)));
				// 时间
				int hour = Integer.parseInt(dts[i * 3 + 2].substring(0, 2));
				int min = Integer.parseInt(dts[i * 3 + 2].substring(2));
				LocalTime pt = LocalTime.of(hour, min);
				// 组合
				CommonTask task = createTask.get();
				task.setPlanDt(LocalDateTime.of(pd, pt));
				task.setExpiredTime(task.getPlanDt().minusSeconds(-model.getExpired()));
				tasks.add(task);
			});
			break;
		case CIRCULAR:
			if (dts.length > 1) {
				throw new RuntimeException("循环计划的计划时间格式出错，无法生成计划任务。");
			}
			CommonTask task = createTask.get();
			task.setPlanDt(LocalDateTime.now());
			task.setExpiredTime(task.getPlanDt().minusSeconds(-model.getExpired()));
			tasks.add(task);
			break;	
		default:
			System.out.println("find some strange cycle.");
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

	public List<String> getHandler() {
		return handler;
	}

	public void setHandler(List<String> handler) {
		this.handler = handler;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public CategoryEnum getCategory() {
		return category;
	}

	public void setCategory(CategoryEnum category) {
		this.category = category;
	}

	public String getTaskIcon() {
		return taskIcon;
	}

	public void setTaskIcon(String taskIcon) {
		this.taskIcon = taskIcon;
	}

	public String getComposeId() {
		return composeId;
	}

	public void setComposeId(String composeId) {
		this.composeId = composeId;
	}

	public LocalDateTime getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(LocalDateTime expiredTime) {
		this.expiredTime = expiredTime;
	}

	public List<NotifyEnum> getNotify() {
		return notify;
	}

	public void setNotify(List<NotifyEnum> notify) {
		this.notify = notify;
	}

	public ExpiredCallbackEnum getCallback() {
		return callback;
	}

	public void setCallback(ExpiredCallbackEnum callback) {
		this.callback = callback;
	}

	public boolean isExecutedCallback() {
		return executedCallback;
	}

	public void setExecutedCallback(boolean executedCallback) {
		this.executedCallback = executedCallback;
	}

	public boolean isExecutedNotify() {
		return executedNotify;
	}

	public void setExecutedNotify(boolean executedNotify) {
		this.executedNotify = executedNotify;
	}

}
