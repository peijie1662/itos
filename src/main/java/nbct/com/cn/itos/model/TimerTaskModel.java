package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Objects;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.CycleEnum;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.DateUtil;

/**
 * 定时任务模版
 * 
 * @author PJ
 * @version 创建时间：2019年12月24日 上午10:30:22
 */
public class TimerTaskModel implements RowMapper<TimerTaskModel> {

	private String modelId;

	private CategoryEnum category;

	private CycleEnum cycle;

	private String abs;

	private String comments;

	private String planDates;

	private boolean invalid;

	private LocalDateTime scanDate;
	
	private int expired;

	public TimerTaskModel from(JsonObject j) {
		TimerTaskModel t = new TimerTaskModel();
		t.setModelId(j.getString("MODELID"));
		t.setCategory(CategoryEnum.from(j.getString("CATEGORY")).get());
		t.setCycle(CycleEnum.from(j.getString("CYCLE")).get());
		t.setAbs(j.getString("ABSTRACT"));
		t.setComments(j.getString("COMMENTS"));
		t.setInvalid("Y".equals(j.getString("INVALID")));
		t.setPlanDates(j.getString("PLANDATES"));
		try {
			t.setScanDate(DateUtil.utcToLocalDT(j.getString("SCANDATE")));
		} catch (ParseException e) {
			throw new RuntimeException("TimerTaskModel日期格式转换错误。");
		}
		if (Objects.nonNull(j.getInteger("EXPIRED"))) {
			t.setExpired(j.getInteger("EXPIRED"));
		}else {
			t.setExpired(24*60*60);//default 24 hours
		}
		return t;
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

	public int getExpired() {
		return expired;
	}

	public void setExpired(int expired) {
		this.expired = expired;
	}

}
