package nbct.com.cn.itos.model;

import java.time.LocalDateTime;
import java.util.List;

import io.vertx.core.json.JsonObject;

/**
* @author PJ 
* @version 创建时间：2020年1月14日 下午2:21:00
*/
public class DispatchClient {
	
	private String serviceName;
	
	private String ip;
	
	private String remark;
	
	private List<String> apiKey;
	
	private LocalDateTime activeTime;
	
	private boolean online;
	
	public static DispatchClient from(JsonObject j){
		DispatchClient client = new DispatchClient();
		client.setServiceName(j.getString("SERVICENAME"));
		client.setIp(j.getString("ip"));
		return client;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public List<String> getApiKey() {
		return apiKey;
	}

	public void setApiKey(List<String> apiKey) {
		this.apiKey = apiKey;
	}

	public LocalDateTime getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(LocalDateTime activeTime) {
		this.activeTime = activeTime;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

}
