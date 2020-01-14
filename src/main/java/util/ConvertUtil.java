package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* @author PJ 
* @version 创建时间：2019年12月31日 下午6:21:52
*/
public class ConvertUtil {
	
	/**
	 * 字符串','分割成List
	 * @param arr
	 */
	public static List<String> strToList(String s){
		List<String> list = new ArrayList<>();
		if (s != null){
			list.addAll(Arrays.asList(s.split(",")));
		}
		return list;
	}
	
	public static String listToStr(List<String> list){
		if(list == null){
			return "";
		}else{
			return String.join(",", list);
		}
	}

}
