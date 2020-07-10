package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年7月9日 下午9:13:33
 */
public class TopologyConnector implements RowMapper<TopologyConnector> {

	private String scene;

	private String sourceId;
	
	private String sourceName;

	private String targetId;

	private String targetName;
	
	private String direction;

	private String preText;

	private String sufText;

	private String lineType;

	@Override
	public TopologyConnector from(JsonObject row) {
		TopologyConnector con = new TopologyConnector();
		con.setScene(row.getString("SCENE"));
		con.setSourceId(row.getString("SOURCEID"));
		con.setTargetId(row.getString("TARGETID"));
		con.setDirection(row.getString("DIRECTION"));
		con.setPreText(row.getString("PRETEXT"));
		con.setSufText(row.getString("SUFTEXT"));
		con.setLineType(row.getString("LINETYPE"));
		con.setSourceName(row.getString("SOURCENAME"));
		con.setTargetName(row.getString("TARGETNAME"));
		return con;
	}

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getPreText() {
		return preText;
	}

	public void setPreText(String preText) {
		this.preText = preText;
	}

	public String getSufText() {
		return sufText;
	}

	public void setSufText(String sufText) {
		this.sufText = sufText;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

}
