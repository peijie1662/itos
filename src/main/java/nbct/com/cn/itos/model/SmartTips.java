package nbct.com.cn.itos.model;

import java.util.List;

import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.ConvertUtil;

/**
* @author PJ 
* @version 创建时间：2020年1月9日 上午9:49:05
*/
public class SmartTips implements RowMapper<SmartTips>{
	
	private String preReg;
	
	private List<String> nextWord;
	
	public SmartTips from(JsonObject j){
		SmartTips st = new SmartTips();
		st.setPreReg(j.getString("PREREG"));
		st.setNextWord(ConvertUtil.strToList(j.getString("NEXTWORD")));
		return st;
	}

	public String getPreReg() {
		return preReg;
	}

	public void setPreReg(String preReg) {
		this.preReg = preReg;
	}

	public List<String> getNextWord() {
		return nextWord;
	}

	public void setNextWord(List<String> nextWord) {
		this.nextWord = nextWord;
	}

}
