package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import io.vertx.core.json.JsonObject;
import util.ConvertUtil;
import util.DateUtil;

/**
* @author PJ 
* @version 创建时间：2020年1月7日 上午9:52:51
*/
public class CommonTaskLog {
	
	private String logId;
	
	private String taskId;
	
	private String status;
	
	private String statusdesc;
	
	private List<String> handler;
	
	private String oldContent;
	
	private String newContent;
	
	private String modelId;
	
	private String abs;
	
	private String oper;
	
	private LocalDateTime opDate;
	
	public static CommonTaskLog from(JsonObject j) throws ParseException{
		CommonTaskLog log = new CommonTaskLog();
		log.setLogId(j.getString("LOGID"));
		log.setTaskId(j.getString("TASKID"));
		log.setAbs(j.getString("ABSTRACT"));
		log.setStatus(j.getString("STATUS"));
		log.setStatusdesc(j.getString("STATUSDESC"));
		log.setNewContent(j.getString("NEWCONTENT"));
		log.setOldContent(j.getString("OLDCONTENT"));
		log.setHandler(ConvertUtil.strToList(j.getString("HANDLER")));
		log.setOper(j.getString("OPER"));
		log.setOpDate(DateUtil.utcToLocalDT(j.getString("OPDATE")));
		return log;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusdesc() {
		return statusdesc;
	}

	public void setStatusdesc(String statusdesc) {
		this.statusdesc = statusdesc;
	}

	public List<String> getHandler() {
		return handler;
	}

	public void setHandler(List<String> handler) {
		this.handler = handler;
	}

	public String getOldContent() {
		return oldContent;
	}

	public void setOldContent(String oldContent) {
		this.oldContent = oldContent;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public LocalDateTime getOpDate() {
		return opDate;
	}

	public void setOpDate(LocalDateTime opDate) {
		this.opDate = opDate;
	}
	
}
