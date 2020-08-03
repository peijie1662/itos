package nbct.com.cn.itos.model;

import java.time.LocalDateTime;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年4月16日 上午8:40:25
 */
public class AppInfo implements RowMapper<AppInfo> {

	private String serviceId;

	private String serviceName;

	private String serviceDesc;

	private String serviceAbs;

	private String serviceObj;

	private String ip;

	private Integer port;

	private String visible;

	private String serviceType;

	private String version;

	private String domain;

	private String remark;

	private Integer x;

	private Integer y;

	private LocalDateTime active;//实时信息
	
	private String actualStatus;//实时状态 "VALID","INVALID","UNKNOW"

	@Override
	public AppInfo from(JsonObject row) {
		AppInfo app = new AppInfo();
		app.setServiceId(row.getString("SERVICEID"));
		app.setServiceName(row.getString("SERVICENAME"));
		app.setServiceDesc(row.getString("SERVICEDESC"));
		app.setServiceAbs(row.getString("SERVICEABS"));
		app.setServiceType(row.getString("SERVICETYPE"));
		app.setServiceObj(row.getString("SERVICEOBJ"));
		app.setIp(row.getString("IP"));
		app.setPort(row.getInteger("PORT"));
		app.setVersion(row.getString("VERSION"));
		app.setRemark(row.getString("REMARK"));
		app.setVisible(row.getString("VISIBLE"));
		app.setDomain(row.getString("DOMAIN"));
		app.setX(row.getInteger("X", 0));
		app.setY(row.getInteger("Y", 0));
		return app;
	}

	/**
	 * 从客户端信息生成
	 */
	public static AppInfo fromClient(JsonObject jo) {
		AppInfo app = new AppInfo();
		app.setServiceObj(jo.getString("serviceObj"));
		app.setIp(jo.getString("ip"));
		return app;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceDesc() {
		return serviceDesc;
	}

	public void setServiceDesc(String serviceDesc) {
		this.serviceDesc = serviceDesc;
	}

	public String getServiceAbs() {
		return serviceAbs;
	}

	public void setServiceAbs(String serviceAbs) {
		this.serviceAbs = serviceAbs;
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

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getServiceObj() {
		return serviceObj;
	}

	public void setServiceObj(String serviceObj) {
		this.serviceObj = serviceObj;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public LocalDateTime getActive() {
		return active;
	}

	public void setActive(LocalDateTime active) {
		this.active = active;
	}

	public String getActualStatus() {
		return actualStatus;
	}

	public void setActualStatus(String actualStatus) {
		this.actualStatus = actualStatus;
	}

}
