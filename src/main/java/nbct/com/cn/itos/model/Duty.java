package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年2月9日 下午3:45:17
 */
public class Duty implements RowMapper<Duty> {

	private String userId;

	private String dutyDate;

	public Duty from(JsonObject j) {
		Duty duty = new Duty();
		duty.setUserId(j.getString("USERID"));
		duty.setDutyDate(j.getString("DUTYDATE"));
		return duty;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDutyDate() {
		return dutyDate;
	}

	public void setDutyDate(String dutyDate) {
		this.dutyDate = dutyDate;
	}

}
