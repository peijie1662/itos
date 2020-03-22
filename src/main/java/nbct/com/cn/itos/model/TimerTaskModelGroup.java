package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年3月22日 下午4:21:30
* 类说明
*/
public class TimerTaskModelGroup implements RowMapper<TimerTaskModelGroup> {
	
	private String modelGroup;
	
	private Integer groupOrder;
	
	public TimerTaskModelGroup from(JsonObject j){
		TimerTaskModelGroup gp = new TimerTaskModelGroup();
		gp.setModelGroup(j.getString("MODELGROUP"));
		gp.setGroupOrder(j.getInteger("GROUPORDER"));
		return gp;
	}
	
	public String getModelGroup() {
		return modelGroup;
	}

	public void setModelGroup(String modelGroup) {
		this.modelGroup = modelGroup;
	}

	public Integer getGroupOrder() {
		return groupOrder;
	}

	public void setGroupOrder(Integer groupOrder) {
		this.groupOrder = groupOrder;
	}

}
