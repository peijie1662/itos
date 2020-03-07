package nbct.com.cn.itos.model;

import java.time.LocalDateTime;
import java.util.Arrays;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年1月14日 下午2:21:00
*/
public class DispatchClient implements RowMapper<DispatchClient>{
	
	private String serviceName;
	
	private JsonArray modelKey; 
	
	private String description;
	
	private String remark1;
	
	private String remark2;
	
	private String ip;
	
	private LocalDateTime activeTime;
	
	private boolean onLine;
	
	/**
	 * 从DB中读记录
	 */
	public DispatchClient from(JsonObject j){
		DispatchClient client = new DispatchClient();
		client.setServiceName(j.getString("SERVICENAME"));
		client.setModelKey(new JsonArray());
		Arrays.asList(j.getString("MODELKEY").split(",")).forEach(item -> {
			client.getModelKey().add(item);
		});
		client.setDescription(j.getString("DESCRIPTION"));
		client.setRemark1(j.getString("REMARK1"));
		client.setRemark2(j.getString("REMARK2"));
		client.setActiveTime(LocalDateTime.now());
		return client;
	}

	public String getServiceName() {
		return serviceName;
	}

	public DispatchClient setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}

	public LocalDateTime getActiveTime() {
		return activeTime;
	}

	public DispatchClient setActiveTime(LocalDateTime activeTime) {
		this.activeTime = activeTime;
		return this;
	}

	public JsonArray getModelKey() {
		return modelKey;
	}

	public DispatchClient setModelKey(JsonArray modelKey) {
		this.modelKey = modelKey;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isOnLine() {
		return onLine;
	}

	public void setOnLine(boolean onLine) {
		this.onLine = onLine;
	}

}
