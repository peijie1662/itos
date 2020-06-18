package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年6月18日 下午12:31:09
*/
public class SysCode implements RowMapper<SysCode>{
	
	private String category;
	
	private String id;
	
	private String desc;
	
	private String v1;
	
	private String v2;
	
	private String v3;

	@Override
	public SysCode from(JsonObject row) {
		SysCode sc = new SysCode();
		sc.setCategory(row.getString("SYCATEGORY"));
		sc.setId(row.getString("SYID"));
		sc.setDesc(row.getString("SYDESC"));
		sc.setV1(row.getString("SYV1"));
		sc.setV2(row.getString("SYV2"));
		sc.setV3(row.getString("SYV3"));
		return sc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getV1() {
		return v1;
	}

	public void setV1(String v1) {
		this.v1 = v1;
	}

	public String getV2() {
		return v2;
	}

	public void setV2(String v2) {
		this.v2 = v2;
	}

	public String getV3() {
		return v3;
	}

	public void setV3(String v3) {
		this.v3 = v3;
	}

}
