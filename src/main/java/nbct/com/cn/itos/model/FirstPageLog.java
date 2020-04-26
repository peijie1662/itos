package nbct.com.cn.itos.model;

import java.time.LocalDateTime;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年4月26日 上午8:46:43
 */
public class FirstPageLog implements RowMapper<FirstPageLog>{

	/**
	 * 操作时间
	 */
	private LocalDateTime opDate;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 描述
	 */
	private String statusDesc;

	public FirstPageLog from(JsonObject j) {
		try {
			FirstPageLog log = new FirstPageLog();
			log.setOpDate(DateUtil.utcToLocalDT(j.getString("OPDATE")));
			log.setStatus(j.getString("STATUS"));
			log.setStatusDesc(j.getString("STATUSDESC"));
			return log;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("后台数据转换成日志时发生错误。");
		}
	}

	public LocalDateTime getOpDate() {
		return opDate;
	}

	public void setOpDate(LocalDateTime opDate) {
		this.opDate = opDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

}
