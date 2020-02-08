package nbct.com.cn.itos.model;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

/**
* @author PJ 
* @version 创建时间：2019年12月25日 下午12:29:47
*/
public class ItosUser {
	
	private String userName;
	
	private String userId;
	
	private String password;
	
	private String workId;
	
	private String role;
	
	private String department;
	
	private String phone;
	
	private String shortPhone;
	
	private String authority;
	
	private String firstPage;
	
	private ServerWebSocket ws;
	
	public static ItosUser from(JsonObject j) {
		ItosUser user = new ItosUser();
		user.setUserId(j.getString("USERID"));
		user.setUserName(j.getString("USERNAME"));
		user.setWorkId(j.getString("WORKID"));
		user.setDepartment(j.getString("DEPARTMENT"));
		user.setAuthority(j.getString("AUTHORITY"));
		user.setPhone(j.getString("PHONE"));
		user.setShortPhone(j.getString("SHORTPHONE"));
		user.setRole(j.getString("ROLE"));
		user.setFirstPage(j.getString("FIRSTPAGE"));
		return user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getShortPhone() {
		return shortPhone;
	}

	public void setShortPhone(String shortPhone) {
		this.shortPhone = shortPhone;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public ServerWebSocket getWs() {
		return ws;
	}

	public ItosUser setWs(ServerWebSocket ws) {
		this.ws = ws;
		return this;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

}
