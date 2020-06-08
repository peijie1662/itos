package util;

/**
 * @author PJ
 * @version 创建时间：2020年6月5日 下午2:05:44
 */
public class CommonUtil {
	/**
	 * 字符串相等
	 */
	public static boolean strEquals(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		} else if (str1 != null) {
			return str1.equals(str2);
		}
		return false;
	}

	/**
	 * 字符串为空
	 */
	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		} else {
			if ("".equals(s.trim())) {
				return true;
			} else {
				return false;
			}
		}
	}
}
