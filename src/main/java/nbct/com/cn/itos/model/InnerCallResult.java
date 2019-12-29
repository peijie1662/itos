package nbct.com.cn.itos.model;

import java.util.List;

import io.vertx.core.json.JsonObject;

/**
* @author PJ 
* @version 创建时间：2019年12月24日 下午12:59:16
*/
public class InnerCallResult {
	
	private List<JsonObject> list;

	public List<JsonObject> getList() {
		return list;
	}

	public void setList(List<JsonObject> list) {
		this.list = list;
	}

}
