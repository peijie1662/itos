package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDate;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.CycleEnum;
import util.DateUtil;

/**
 * 定时任务模版
 * 
 * @author PJ
 * @version 创建时间：2019年12月24日 上午10:30:22
 */
public class TimerTaskModel {

	private String modelId;
	
	private CategoryEnum category;

	private CycleEnum cycle;
	
	private String abs;

	private String comments;

	private String planDates;

	private boolean invalid;

	private LocalDate scanDate;
	
	private String apiKey;
	
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

	public LocalDate getScanDate() {
		return scanDate;
	}

	public void setScanDate(LocalDate scanDate) {
		this.scanDate = scanDate;
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
	
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public CategoryEnum getCategory() {
		return category;
	}

	public void setCategory(CategoryEnum category) {
		this.category = category;
	}

	public static TimerTaskModel from(JsonObject j) throws ParseException {
		TimerTaskModel t = new TimerTaskModel();
		t.setModelId(j.getString("MODELID"));
		t.setCategory(CategoryEnum.from(j.getString("CATEGORY")).get());
		t.setCycle(CycleEnum.from(j.getString("CYCLE")).get());
		t.setAbs(j.getString("ABSTRACT"));
		t.setComments(j.getString("COMMENTS"));
		t.setInvalid("Y".equals(j.getString("INVAILD")));
		t.setPlanDates(j.getString("PLANDATES"));
		t.setScanDate(DateUtil.utcToLocal(j.getString("SCANDATE")));
		t.setApiKey(j.getString("APIKEY"));
		return t;
	}

}
