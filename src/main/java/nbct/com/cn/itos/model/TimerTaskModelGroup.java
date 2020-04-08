package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年3月22日 下午4:21:30
*/
public class TimerTaskModelGroup implements RowMapper<TimerTaskModelGroup> {
	
	private String groupId;
	
	private String groupName;
	
	private String groupDesc;
	
	private Integer groupOrder;
	
	public TimerTaskModelGroup from(JsonObject j){
		TimerTaskModelGroup gp = new TimerTaskModelGroup();
		gp.setGroupId(j.getString("GROUPID"));
		gp.setGroupName(j.getString("GROUPNAME"));
		gp.setGroupDesc(j.getString("GROUPDESC"));
		gp.setGroupOrder(j.getInteger("GROUPORDER"));
		return gp;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public Integer getGroupOrder() {
		return groupOrder;
	}

	public void setGroupOrder(Integer groupOrder) {
		this.groupOrder = groupOrder;
	}

}
