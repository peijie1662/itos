package nbct.com.cn.itos.model;

import java.time.LocalDateTime;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年6月20日 上午11:03:49
 */
public class CompareFile implements RowMapper<CompareFile> {

	private String compareId;

	private String compareGroup;

	private String filePath;

	private String fileName;

	private String compareOption;

	private String serviceName;

	private String curFileSize;// 文件大小

	private String curFileModifyTime;// 文件修改时间

	private LocalDateTime curRefreshDate;// 刷新时间

	@Override
	public CompareFile from(JsonObject row) {
		CompareFile cf = new CompareFile();
		cf.setCompareId(row.getString("COMPAREID"));
		cf.setCompareGroup(row.getString("COMPAREGROUP"));
		cf.setFilePath(row.getString("FILEPATH"));
		cf.setFileName(row.getString("FILENAME"));
		cf.setServiceName(row.getString("SERVICENAME"));
		cf.setCompareOption(row.getString("COMPAREOPTION"));
		return cf;
	}

	public String getCompareId() {
		return compareId;
	}

	public void setCompareId(String compareId) {
		this.compareId = compareId;
	}

	public String getCompareGroup() {
		return compareGroup;
	}

	public void setCompareGroup(String compareGroup) {
		this.compareGroup = compareGroup;
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

	public String getCompareOption() {
		return compareOption;
	}

	public void setCompareOption(String compareOption) {
		this.compareOption = compareOption;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getCurFileSize() {
		return curFileSize;
	}

	public void setCurFileSize(String curFileSize) {
		this.curFileSize = curFileSize;
	}

	public String getCurFileModifyTime() {
		return curFileModifyTime;
	}

	public void setCurFileModifyTime(String curFileModifyTime) {
		this.curFileModifyTime = curFileModifyTime;
	}

	public LocalDateTime getCurRefreshDate() {
		return curRefreshDate;
	}

	public void setCurRefreshDate(LocalDateTime curRefreshDate) {
		this.curRefreshDate = curRefreshDate;
	}

}
