package nbct.com.cn.itos.model;

import java.util.List;

import io.vertx.core.json.JsonObject;

/**
* @author PJ 
* @version 创建时间：2020年1月20日 上午8:19:35
*/
public class ComposeTaskModel {
	
	private List<JsonObject> detail;

	public List<JsonObject> getDetail() {
		return detail;
	}

	public void setDetail(List<JsonObject> detail) {
		this.detail = detail;
	} 

}
