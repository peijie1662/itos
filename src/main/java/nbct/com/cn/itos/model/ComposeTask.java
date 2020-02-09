package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDateTime;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.config.CategoryEnum;
import nbct.com.cn.itos.config.TaskStatusEnum;
import util.ConvertUtil;
import util.DateUtil;

/**
* @author PJ 
* @version 创建时间：2020年1月28日 上午10:43:29
*/
public class ComposeTask extends CommonTask {
	
	private LocalDateTime bgDt;
	
	private LocalDateTime edDt;
	
	public ComposeTask from(JsonObject j) {
		ComposeTask task = new ComposeTask();
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
		try {
			task.setBgDt(DateUtil.utcToLocalDT(j.getString("BGDT")));
			task.setEdDt(DateUtil.utcToLocalDT(j.getString("EDDT")));
		} catch (ParseException e) {
			throw new RuntimeException(e.getCause());
		}
		return task;
	}

	public LocalDateTime getBgDt() {
		return bgDt;
	}

	public void setBgDt(LocalDateTime bgDt) {
		this.bgDt = bgDt;
	}

	public LocalDateTime getEdDt() {
		return edDt;
	}

	public void setEdDt(LocalDateTime edDt) {
		this.edDt = edDt;
	}

}
