package nbct.com.cn.itos.model;

import java.time.LocalDateTime;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年1月14日 下午2:21:00
*/
public class DispatchClient implements RowMapper<DispatchClient>{
	
	private String serviceName;
	
	private String ip;
	
	private String remark;
	
	private JsonArray apiKey;
	
	private LocalDateTime activeTime;
	
	private boolean online;
	
	public DispatchClient from(JsonObject j){
		DispatchClient client = new DispatchClient();
		client.setServiceName(j.getString("SERVICENAME"));
		client.setIp(j.getString("ip"));
		return client;
	}

	public String getServiceName() {
		return serviceName;
	}

	public DispatchClient setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public DispatchClient setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public String getRemark() {
		return remark;
	}

	public DispatchClient setRemark(String remark) {
		this.remark = remark;
		return this;
	}

	public LocalDateTime getActiveTime() {
		return activeTime;
	}

	public DispatchClient setActiveTime(LocalDateTime activeTime) {
		this.activeTime = activeTime;
		return this;
	}

	public boolean isOnline() {
		return online;
	}

	public DispatchClient setOnline(boolean online) {
		this.online = online;
		return this;
	}

	public JsonArray getApiKey() {
		return apiKey;
	}

	public DispatchClient setApiKey(JsonArray apiKey) {
		this.apiKey = apiKey;
		return this;
	}

}
