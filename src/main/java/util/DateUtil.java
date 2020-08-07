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
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author PJ
 * @version 创建时间：2019年12月24日 下午4:05:31 类说明
 */
public class DateUtil {
	
	/**
	 *返回秒数 
	 */
	public static long getSecond(LocalDateTime dt) {
		return dt.toEpochSecond(ZoneOffset.of("+8"));
	}

	/**
	 * 字符串转LocalDateTime
	 */
	public static LocalDateTime strToLocal(String str) {
		String dateTimeStr = "";
		// 1.如果只有时间值，没有明确指定日期时间，补上当前日期，
		if (str.length() == 8) {
			String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			dateTimeStr = dateStr + " " + str;
		} else {
			dateTimeStr = str;
		}
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return LocalDateTime.parse(dateTimeStr, df);
	}

	/**
	 * UTC时间字符串转成LocalDateTime (无异常)
	 */
	public static LocalDateTime utcToLocalExNoExp(String utcTime) {
		if (Objects.isNull(utcTime))
			return LocalDateTime.now();
		try {
			return utcToLocalEx(utcTime);
		} catch (Exception e) {
			return LocalDateTime.now();
		}
	}

	/**
	 * UTC时间字符串转成LocalDateTime (DB -> OBJ)
	 */
	public static LocalDateTime utcToLocalEx(String utcTime) throws ParseException {
		if (Objects.nonNull(utcTime)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dt = sdf.parse(utcTime);
			return dt.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
		} else {
			return null;
		}
	}

	/**
	 * UTC时间字符串转成LocalDateTime (DB -> OBJ)
	 */
	public static LocalDateTime utcToLocalDT(String utcTime) throws ParseException {
		if (Objects.nonNull(utcTime)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dt = sdf.parse(utcTime);
			return dt.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
		} else {
			return null;
		}
	}

	/**
	 * UTC时间字符串转成LocalDate (DB -> OBJ)
	 */
	public static LocalDate utcToLocal(String utcTime) throws ParseException {
		if (Objects.nonNull(utcTime)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dt = sdf.parse(utcTime);
			return dt.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDate();
		} else {
			return null;
		}
	}

	/**
	 * LocalDate转成UTC时间字符串 (OBJ -> DB)
	 */
	public static String localToUtcStr(LocalDateTime dt) {
		if (Objects.nonNull(dt)) {
			ZoneId zone = ZoneId.systemDefault();
			Instant instant = dt.atZone(zone).toInstant();
			Date d = Date.from(instant);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf.format(d);
		} else {
			return null;
		}
	}

	/**
	 * 给定UTC时间开始时间
	 */
	public static String getDBDateBeginStr(String dateUtcStr) {
		LocalDateTime dt = utcToLocalExNoExp(dateUtcStr);
		String dtStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
		return "to_date('" + dtStr + "','yyyy-mm-dd hh24:mi:ss')";
	}

	/**
	 * 给定UTC时间结束时间
	 */
	public static String getDBDateEndStr(String dateUtcStr) {
		LocalDateTime dt = utcToLocalExNoExp(dateUtcStr);
		String dtStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
		return "to_date('" + dtStr + "','yyyy-mm-dd hh24:mi:ss')";
	}

	/**
	 * 当前时间字符串
	 */
	public static String curDtStr() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
	
	/**
	 *LocalDateTime转常用日期字符串 
	 */
	public static String toDateTimeStr(LocalDateTime dt) {
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
	
	/**
	 * 将UTC字符串转常用日期字符串
	 */
	public static String toDateTimeStr(String utc) {
		LocalDateTime dt = utcToLocalExNoExp(utc);
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 * 根据周几返回日期
	 */
	public static LocalDate getDateByYearAndWeekNumAndDayOfWeek(Integer year, Integer num, int dayOfWeek) {
		// 1.周数小于10在前面补个0
		String numStr = num < 10 ? "0" + String.valueOf(num) : String.valueOf(num);
		// 2.2019-W01-01获取第一周的周一日期，2019-W02-07获取第二周的周日日期
		String weekDate = String.format("%s-W%s-%s", year, numStr, dayOfWeek);
		return LocalDate.parse(weekDate, DateTimeFormatter.ISO_WEEK_DATE);
	}

	/**
	 * 数据库当前日期时间
	 */
	public static String getDBTimeStr() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String ds = "to_date('" + df.format(new Date()) + "','yyyy-mm-dd hh24:mi:ss')";
		return ds;
	}

}
