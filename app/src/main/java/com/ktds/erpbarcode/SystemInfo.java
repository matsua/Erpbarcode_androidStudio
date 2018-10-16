package com.ktds.erpbarcode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SystemInfo {
	/**
	 * 현재 년도를 리턴한다.
	 */
	public static String getNowYear() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/**
	 * 현재 월를 리턴한다.
	 */
	public static String getNowMonth() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/**
	 * 현재 년월일을 리턴한다.
	 */
	public static String getNowDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/**
	 * 현재 시분초를 리턴한다.
	 */
	public static String getNowTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sd = new SimpleDateFormat("HHmmss");  
		String time = sd.format(c.getTime());  //시간가져오기
		return time;
	}

	/**
	 * 현재 년월일을 형식(-,/)에 따라 리턴한다.
	 */
	public static String getNowDate(String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (format.equals("-")) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} else if (format.equals("/")) {
			dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		}
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/**
	 * 현재 년월일 시분초를 리턴한다.
	 */
	public static String getNowDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

}
