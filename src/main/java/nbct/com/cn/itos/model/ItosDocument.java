package nbct.com.cn.itos.model;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年6月17日 上午9:58:18
 */
public class ItosDocument implements RowMapper<ItosDocument> {

	public static Logger log = LogManager.getLogger(ItosDocument.class);

	private String filePath;

	private String fileName;

	private String category;

	private String abs;

	private String groupId;

	private Integer orderInGroup;

	private String oper;

	private LocalDateTime opDate;

	@Override
	public ItosDocument from(JsonObject row) {
		ItosDocument doc = new ItosDocument();
		doc.setFilePath(row.getString("FILEPATH"));
		doc.setFileName(row.getString("FILENAME"));
		doc.setCategory(row.getString("CATEGORY"));
		doc.setAbs(row.getString("ABSTRACT"));
		doc.setGroupId(row.getString("GROUPID"));
		doc.setOrderInGroup(row.getInteger("ORDERINGROUP"));
		doc.setOper(row.getString("OPER"));
		try {
			doc.setOpDate(DateUtil.utcToLocalDT(row.getString("OPDATE")));
		} catch (ParseException e) {
			log.error("FROM-01::", e);
		}
		return doc;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public LocalDateTime getOpDate() {
		return opDate;
	}

	public void setOpDate(LocalDateTime opDate) {
		this.opDate = opDate;
	}

	public Integer getOrderInGroup() {
		return orderInGroup;
	}

	public void setOrderInGroup(Integer orderInGroup) {
		this.orderInGroup = orderInGroup;
	}

}
