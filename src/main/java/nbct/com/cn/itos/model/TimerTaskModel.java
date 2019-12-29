package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
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
	
	private String abs;

	private String comments;

	private String planDates;

	private boolean invalid;

	private LocalDate scanDate;
	
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

	public CategoryEnum getCategory() {
		return category;
	}

	public void setCategory(CategoryEnum category) {
		this.category = category;
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

	public static TimerTaskModel from(JsonObject j) throws ParseException {
		TimerTaskModel t = new TimerTaskModel();
		t.setModelId(j.getString("MODELID"));
		Optional<CategoryEnum> warpCategory = CategoryEnum.from(j.getString("CATEGORY"));
		if (warpCategory.isPresent()) {
			t.setCategory(warpCategory.get());
		} else {
			throw new RuntimeException("读取任务模版失败，未定义的模板类型。");
		}
		t.setAbs(j.getString("ABSTRACT"));
		t.setComments(j.getString("COMMENTS"));
		t.setInvalid("Y".equals(j.getString("INVAILD")));
		t.setPlanDates(j.getString("PLANDATES"));
		t.setScanDate(DateUtil.utcToLocal(j.getString("SCANDATE")));
		return t;
	}

	@Override
	public String toString() {
		return "TimerTaskModel [modelId=" + modelId + ", category=" + category + ", comments=" + comments
				+ ", planDates=" + planDates + ", invalid=" + invalid + ", scanDate=" + scanDate + "]";
	}


}
