package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;

/**
* @author PJ 
* @version 创建时间：2020年1月21日 下午2:52:22
*/
public class ComposeDetail {
	
	private String composeId;
	
	private int composeLevel;
	
	private String modelId;
	
	public static ComposeDetail from(JsonObject j){
		ComposeDetail cd = new ComposeDetail();
		cd.setComposeId(j.getString("COMPOSEID"));
		cd.setComposeLevel(j.getInteger("COMPOSELEVEL"));
		cd.setModelId(j.getString("MODELID"));
		return cd;
	}

	public String getComposeId() {
		return composeId;
	}

	public void setComposeId(String composeId) {
		this.composeId = composeId;
	}

	public int getComposeLevel() {
		return composeLevel;
	}

	public void setComposeLevel(int composeLevel) {
		this.composeLevel = composeLevel;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
}
