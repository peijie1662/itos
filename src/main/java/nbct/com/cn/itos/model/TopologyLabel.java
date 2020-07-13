package nbct.com.cn.itos.model;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;

/**
 * @author PJ
 * @version 创建时间：2020年7月13日 下午1:48:51
 */
public class TopologyLabel implements RowMapper<TopologyLabel> {
	
	private String labId;

	private String scene;

	private Integer x;

	private Integer y;

	private String text;

	private Integer width;

	@Override
	public TopologyLabel from(JsonObject row) {
		TopologyLabel lab = new TopologyLabel();
		lab.setLabId(row.getString("LABID"));
		lab.setScene(row.getString("SCENE"));
		lab.setText(row.getString("TEXT"));
		lab.setX(row.getInteger("X", 0));
		lab.setY(row.getInteger("Y", 0));
		lab.setWidth(row.getInteger("WIDTH", 999));
		return lab;
	}

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getLabId() {
		return labId;
	}

	public void setLabId(String labId) {
		this.labId = labId;
	}


}
