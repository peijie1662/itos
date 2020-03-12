package nbct.com.cn.itos.model;

import java.util.List;

import com.alibaba.fastjson.JSON;

/**
* @author PJ 
* @version 创建时间：2020年3月11日 下午1:05:37
*/
public class ItosMsg {
	
	/**
	 * 消息头
	 */
	private String header;
	
	/**
	 * 数量值
	 */
	private Integer count;
	
	/**
	 * 字符串内容
	 */
	private String content;
	
	/**
	 * 列表
	 */
	private List<?> list;
	
	private ItosMsg() {
		
	}
	
	public static ItosMsg from(String header,Integer count) {
		ItosMsg msg = new ItosMsg();
		msg.setHeader(header);
		msg.setCount(count);
		return msg;
	}
	
	public static ItosMsg from(String header,String content) {
		ItosMsg msg = new ItosMsg();
		msg.setHeader(header);
		msg.setContent(content);
		return msg;
	}
	
	public static<T> ItosMsg from(String header,List<T> list) {
		ItosMsg msg = new ItosMsg();
		msg.setHeader(header);
		msg.setList(list);
		return msg;
	}
	
	public String json() {
		return JSON.toJSONString(this);
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

}
