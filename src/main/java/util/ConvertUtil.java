package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author PJ
 * @version 创建时间：2019年12月31日 下午6:21:52
 */
public class ConvertUtil {
	
	/**
	 * String => JsonObject
	 * 
	 * @param str
	 */
	public static JsonObject strToJsonObject(String str) {
		try {
			return new JsonObject(str);
		}catch(Exception e) {
			return null;
		}
	}

	/**
	 * String => List
	 * 
	 * @param arr
	 */
	public static List<String> strToList(String s) {
		List<String> list = new ArrayList<>();
		if (s != null) {
			list.addAll(Arrays.asList(s.split(",")));
		}
		return list;
	}

	/**
	 * List => String
	 * 
	 * @param list
	 * @return
	 */
	public static String listToStr(List<String> list) {
		if (list == null) {
			return "";
		} else {
			return String.join(",", list);
		}
	}

	/**
	 * String => boolean
	 * 
	 * @param s
	 * @return
	 */
	public static boolean strToBool(String s) {
		if (Objects.nonNull(s)) {
			return "Y".equals(s) ? true : false;
		} else {
			return false;
		}
	}

	/**
	 * boolean => String
	 * 
	 * @param b
	 * @return
	 */
	public static String boolToStr(boolean b) {
		return b ? "Y" : "N";
	}

	/**
	 * 空字符串
	 * 
	 * @param s
	 * @return
	 */
	public static boolean emptyOrNull(String s) {
		return Objects.isNull(s) || "".equals(s);
	}
	
	/**
	 * 整数值
	 * @param val
	 * @param defaultInt
	 * @return
	 */
	public static Integer getInteger(Integer val,Integer defaultInt) {
	  return Objects.isNull(val) ? 24 * 60 * 60 : val;
	}

	/**
	 * JsonArray => String
	 * 
	 * @param ja
	 * @return
	 */
	public static String arrToString(JsonArray ja) {
		return Objects.nonNull(ja) ? ja.stream().map(item -> {
			return item.toString();
		}).collect(Collectors.joining(",")) : "";
	}

}
