package nbct.com.cn.itos.model;

import io.vertx.core.http.ServerWebSocket;

/**
* @author PJ 
* @version 创建时间：2019年12月25日 下午12:29:47
*/
public class ItosUser {
	
	private String userName;
	
	private String userId;
	
	private String workId;
	
	private String role;
	
	private String deptName;
	
	private String functionId;
	
	private ServerWebSocket ws;

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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public ServerWebSocket getWs() {
		return ws;
	}

	public ItosUser setWs(ServerWebSocket ws) {
		this.ws = ws;
		return this;
	}

	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	@Override
	public String toString() {
		return "ItosUser [userName=" + userName + ", userId=" + userId + ", workId=" + workId + ", role=" + role
				+ ", deptName=" + deptName + ", functionId=" + functionId + ", ws=" + ws + "]";
	}

}
