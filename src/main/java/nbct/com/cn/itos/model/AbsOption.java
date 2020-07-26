package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年7月22日 下午1:48:32
*/
public class AbsOption implements RowMapper<AbsOption>{
	
	private String modelId;
	
	private String abs;
	
	private String groupName;
	
	private String groupDesc;
	
	@Override
	public AbsOption from(JsonObject row) {
		AbsOption abs = new AbsOption();
		abs.setModelId(row.getString("MODELID"));
		abs.setGroupName(row.getString("GROUPNAME"));
		abs.setGroupDesc(row.getString("GROUPDESC"));
		abs.setAbs(row.getString("ABS"));
		return abs;
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
	
}
