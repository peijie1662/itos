package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author PJ
 * @version 创建时间：2019年12月24日 下午4:05:31 类说明
 */
public class DateUtil {
	
	/**
	 * UTC时间字符串转成LocalDateTime (DB -> OBJ)
	 */
	public static LocalDateTime utcToLocalDT(String utcTime) throws ParseException {
		if (utcTime == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dt = sdf.parse(utcTime);
		return dt.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
	}

	/**
	 * UTC时间字符串转成LocalDate (DB -> OBJ)
	 */
	public static LocalDate utcToLocal(String utcTime) throws ParseException {
		if (utcTime == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dt = sdf.parse(utcTime);
		return dt.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDate();
	}

	/**
	 * LocalDate转成UTC时间字符串 (OBJ -> DB)
	 */
	public static String localToUtcStr(LocalDateTime dt) {
		if (dt == null) {
			return null;
		}
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = dt.atZone(zone).toInstant();
		Date d = Date.from(instant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(d);
	}
	
	/**
	 * 当前时间字符串
	 */
	public static String curDtStr(){
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
	
	/**
	 * 根据周几返回日期 
	 */
	public static LocalDate getDateByYearAndWeekNumAndDayOfWeek(Integer year, Integer num, int dayOfWeek) {
		//周数小于10在前面补个0
        String numStr = num < 10 ? "0" + String.valueOf(num) : String.valueOf(num);
        //2019-W01-01获取第一周的周一日期，2019-W02-07获取第二周的周日日期
        String weekDate = String.format("%s-W%s-%s", year, numStr, dayOfWeek);
        return LocalDate.parse(weekDate, DateTimeFormatter.ISO_WEEK_DATE);
    }
	
	public static String getDBTimeStr() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String ds = "to_date('" + df.format(new Date()) + "','yyyy-mm-dd hh24:mi:ss')";
		return ds;
	}

	public static boolean strEquals(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		} else if (str1 != null) {
			return str1.equals(str2);
		}
		return false;
	}

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
