package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import util.ConvertUtil;

/**
* @author PJ 
* @version 创建时间：2020年4月16日 上午8:40:25
*/
public class AppInfo {
	
	private String serverName;
	
	private String serverDesc;
	
	private String ip;
	
	private Integer port;
	
	private boolean valid;
	
	private String type;
	
	private Integer x;
	
	private Integer y;
	
	public static AppInfo from(JsonObject jo) {
		AppInfo app = new AppInfo();
		app.setServerName(jo.getString("serverName"));
		app.setServerDesc(jo.getString("desc"));
		app.setIp(jo.getString("ip"));
		app.setPort(jo.getInteger("port"));
		app.setValid(ConvertUtil.strToBool(jo.getString("valid")));
		return app;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerDesc() {
		return serverDesc;
	}

	public void setServerDesc(String serverDesc) {
		this.serverDesc = serverDesc;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
