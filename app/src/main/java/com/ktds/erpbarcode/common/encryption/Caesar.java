package com.ktds.erpbarcode.common.encryption;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Caesar {

	private static HashMap<String, String> dic;

	public static String decrypt(String value) {
		if (dic == null)
			decriptInit();

		if (value.startsWith(" "))
			value = value.replaceAll(" ", "");

		//if (!value.startsWith("+") && !value.toUpperCase().startsWith("K9")) return value;	// 기능 일시 중지 - request by 박장수, 김희선 2014.12.17
		if (!value.startsWith("+") && !value.startsWith("K9")) return value;	

		String res = "";

		if (value.startsWith("+"))
			value = value.substring(1, value.length() - 1);

		for (int i = 0; i < value.length(); i++) {

			Set<String> key = dic.keySet();
			for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
				String keyName = (String) iterator.next();
				String valueName = (String) dic.get(keyName);
				if (String.valueOf(value.charAt(i)).equals(valueName)) {
					res += keyName;
					break;
				}
			}

			if (res.length() != (i + 1))
				res += value.charAt(i);
		}

		return res + "\n";
	}
	
	public static String encrypt(String value) {
		if (dic == null)
			encriptInit();

		if (value.startsWith(" "))
			value = value.replaceAll(" ", "");

		//if (!value.startsWith("+") && !value.toUpperCase().startsWith("K9")) return value;	// 기능 일시 중지 - request by 박장수, 김희선 2014.12.17
		if (!value.startsWith("+") && !value.startsWith("K9")) return value;	

		String res = "";

		if (value.startsWith("+"))
			value = value.substring(1, value.length() - 1);

		for (int i = 0; i < value.length(); i++) {

			Set<String> key = dic.keySet();
			for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
				String keyName = (String) iterator.next();
				String valueName = (String) dic.get(keyName);
				if (String.valueOf(value.charAt(i)).equals(valueName)) {
					res += keyName;
					break;
				}
			}

			if (res.length() != (i + 1))
				res += value.charAt(i);
		}

		return res + "\n";
	}

	private static void decriptInit() {
		dic = new HashMap<String, String>();
		dic.put("A", "Q");
		dic.put("B", "I");
		dic.put("C", "Z");
		dic.put("D", "R");
		dic.put("E", "D");
		dic.put("F", "G");
		dic.put("G", "X");
		dic.put("H", "S");
		dic.put("I", "N");
		dic.put("J", "C");
		dic.put("K", "F");
		dic.put("L", "J");
		dic.put("M", "T");
		dic.put("N", "L");
		dic.put("O", "H");
		dic.put("P", "P");
		dic.put("Q", "Y");
		dic.put("R", "O");
		dic.put("S", "U");
		dic.put("T", "A");
		dic.put("U", "E");
		dic.put("V", "M");
		dic.put("W", "K");
		dic.put("X", "B");
		dic.put("Y", "W");
		dic.put("Z", "V");
		dic.put("1", "4");
		dic.put("2", "1");
		dic.put("3", "5");
		dic.put("4", "2");
		dic.put("5", "3");
		dic.put("6", "0");
		dic.put("7", "6");
		dic.put("8", "9");
		dic.put("9", "8");
		dic.put("0", "7");
	}
	
	private static void encriptInit() {
		dic = new HashMap<String, String>();
		dic.put("Q", "A");
		dic.put("I", "B");
		dic.put("Z", "C");
		dic.put("R", "D");
		dic.put("D", "E");
		dic.put("G", "F");
		dic.put("X", "G");
		dic.put("S", "H");
		dic.put("N", "I");
		dic.put("C", "J");
		dic.put("F", "K");
		dic.put("J", "L");
		dic.put("T", "M");
		dic.put("L", "N");
		dic.put("H", "O");
		dic.put("P", "P");
		dic.put("Y", "Q");
		dic.put("O", "R");
		dic.put("U", "S");
		dic.put("A", "T");
		dic.put("E", "U");
		dic.put("M", "V");
		dic.put("K", "W");
		dic.put("B", "X");
		dic.put("W", "Y");
		dic.put("V", "Z");
		dic.put("4", "1");
		dic.put("1", "2");
		dic.put("5", "3");
		dic.put("2", "4");
		dic.put("3", "5");
		dic.put("0", "6");
		dic.put("6", "7");
		dic.put("9", "8");
		dic.put("8", "9");
		dic.put("7", "0");
	}
}
