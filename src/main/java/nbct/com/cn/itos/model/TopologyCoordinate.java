package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
* @author PJ 
* @version 创建时间：2020年6月26日 下午7:42:43
*/
public class TopologyCoordinate implements RowMapper<TopologyCoordinate>{
	
	private String serverName;
	
	private String ip;
	
	private Integer  x;
	
	private Integer  y;

	@Override
	public TopologyCoordinate from(JsonObject row) {
		TopologyCoordinate tc = new TopologyCoordinate();
		tc.setServerName(row.getString("SERVERNAME"));
		tc.setIp(row.getString("IP"));
		tc.setX(row.getInteger("X"));
		tc.setY(row.getInteger("Y"));
		return tc;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

}
