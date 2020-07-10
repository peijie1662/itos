package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年6月26日 下午7:42:43
*/
public class TopologyCoordinate implements RowMapper<TopologyCoordinate>{
	
	private String scene;
	
	private String serviceId;
	
	private Integer  x;
	
	private Integer  y;

	@Override
	public TopologyCoordinate from(JsonObject row) {
		TopologyCoordinate tc = new TopologyCoordinate();
		tc.setScene(row.getString("SCENE"));
		tc.setServiceId(row.getString("SERVICEID"));
		tc.setX(row.getInteger("X"));
		tc.setY(row.getInteger("Y"));
		return tc;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}
