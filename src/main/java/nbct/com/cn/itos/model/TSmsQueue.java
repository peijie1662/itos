package nbct.com.cn.itos.model;

import java.sql.Timestamp;

/**
* @author PJ 
* @version 创建时间：2020年5月3日 下午1:14:54
*/
public class TSmsQueue {
	
	private String smsId;
	
	private String phone;
	
	private String content;
	
	private String sentFlag;
	
	private Timestamp recTime;
	
	private Timestamp sentTime;
	
	private String deliveredFlag;
	
	private String environment;

	public String getSmsId() {
		return smsId;
	}

	public void setSmsId(String smsId) {
		this.smsId = smsId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSentFlag() {
		return sentFlag;
	}

	public void setSentFlag(String sentFlag) {
		this.sentFlag = sentFlag;
	}

	public Timestamp getRecTime() {
		return recTime;
	}

	public void setRecTime(Timestamp recTime) {
		this.recTime = recTime;
	}

	public Timestamp getSentTime() {
		return sentTime;
	}

	public void setSentTime(Timestamp sentTime) {
		this.sentTime = sentTime;
	}

	public String getDeliveredFlag() {
		return deliveredFlag;
	}

	public void setDeliveredFlag(String deliveredFlag) {
		this.deliveredFlag = deliveredFlag;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

}
