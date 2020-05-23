package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.CycleEnum;
import nbct.com.cn.itos.config.ExpiredCallbackEnum;
import nbct.com.cn.itos.config.NotifyEnum;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.DateUtil;

/**
 * 定时任务模版
 * 
 * @author PJ
 * @version 创建时间：2019年12月24日 上午10:30:22
 */
public class TimerTaskModel implements RowMapper<TimerTaskModel> {

	public static final int DEFAULT_EXPIRED = 24 * 60 * 60;//default 24 hours

	private String modelId;

	private CategoryEnum category;

	private CycleEnum cycle;

	private String abs;

	private String comments;

	private String planDates;

	private boolean invalid;
	
	private LocalDateTime startDate;

	private LocalDateTime scanDate;

	private Integer expired;

	private ExpiredCallbackEnum callback;

	private List<NotifyEnum> notify;
	
	private String groupId;
	
	private Integer orderInGroup;
	
	private String serviceName;
	
	private String serviceDescription;
	
	private String serviceDomain;

	public TimerTaskModel from(JsonObject j) {
		try {
			TimerTaskModel t = new TimerTaskModel();
			t.setModelId(j.getString("MODELID"));
			t.setCategory(CategoryEnum.from(j.getString("CATEGORY")).get());
			t.setCycle(CycleEnum.from(j.getString("CYCLE")).get());
			t.setAbs(j.getString("ABSTRACT"));
			t.setComments(j.getString("COMMENTS"));
			t.setInvalid("Y".equals(j.getString("INVALID")));
			t.setPlanDates(j.getString("PLANDATES"));
			t.setStartDate(DateUtil.utcToLocalDT(j.getString("STARTDATE")));
			t.setScanDate(DateUtil.utcToLocalDT(j.getString("SCANDATE")));
			// expired time
			Integer expired = Objects.nonNull(j.getInteger("EXPIRED")) ? j.getInteger("EXPIRED") : DEFAULT_EXPIRED;
			t.setExpired(expired);
			// expired callback
			ExpiredCallbackEnum callback = Objects.nonNull(j.getString("EXPIREDCALLBACK"))
					? ExpiredCallbackEnum.from(j.getString("EXPIREDCALLBACK")).get()
					: ExpiredCallbackEnum.NONE;
			t.setCallback(callback);
			// expired notify
			String notify = j.getString("EXPIREDNOTIFY");
			if (Objects.nonNull(notify)) {
				List<NotifyEnum> n = Arrays.asList(notify.split(",")).stream().map(item -> {
					return NotifyEnum.from(item).get();
				}).collect(Collectors.toList());
				t.setNotify(n);
			} else {
				t.setNotify(Collections.emptyList());
			}
			t.setGroupId(j.getString("GROUPID"));
			t.setOrderInGroup(j.getInteger("ORDERINGROUP"));
			t.setServiceName(j.getString("SERVICENAME"));
			t.setServiceDescription(j.getString("SERVICEDESCRIPTION"));
			t.setServiceDomain(j.getString("SERVICEDOMAIN"));
			return t;
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("后台数据转换成模版时发生错误。");
		}
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getPlanDates() {
		return planDates;
	}

	public void setPlanDates(String planDates) {
		this.planDates = planDates;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public CycleEnum getCycle() {
		return cycle;
	}

	public void setCycle(CycleEnum cycle) {
		this.cycle = cycle;
	}

	public CategoryEnum getCategory() {
		return category;
	}

	public void setCategory(CategoryEnum category) {
		this.category = category;
	}

	public LocalDateTime getScanDate() {
		return scanDate;
	}

	public void setScanDate(LocalDateTime scanDate) {
		this.scanDate = scanDate;
	}

	public ExpiredCallbackEnum getCallback() {
		return callback;
	}

	public void setCallback(ExpiredCallbackEnum callback) {
		this.callback = callback;
	}

	public List<NotifyEnum> getNotify() {
		return notify;
	}

	public void setNotify(List<NotifyEnum> notify) {
		this.notify = notify;
	}

	public Integer getExpired() {
		return expired;
	}

	public void setExpired(Integer expired) {
		this.expired = expired;
	}

	public Integer getOrderInGroup() {
		return orderInGroup;
	}

	public void setOrderInGroup(Integer orderInGroup) {
		this.orderInGroup = orderInGroup;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public static int getDefaultExpired() {
		return DEFAULT_EXPIRED;
	}

	public String getServiceDomain() {
		return serviceDomain;
	}

	public void setServiceDomain(String serviceDomain) {
		this.serviceDomain = serviceDomain;
	}

}
