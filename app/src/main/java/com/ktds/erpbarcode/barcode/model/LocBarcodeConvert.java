package com.ktds.erpbarcode.barcode.model;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LocBarcodeConvert {
	//private static final String TAG = "LocBarcodeConvert";
	
	public static LocBarcodeInfo jsonStringToLocBarcodeInfo(String jsonString) {
		
		Gson gson = new Gson();
		Type deviceType = new TypeToken<LocBarcodeInfo>(){}.getType();
		LocBarcodeInfo locBarcodeInfo = gson.fromJson(jsonString, deviceType);

		return locBarcodeInfo;
	}
	
	public static String locBarcodeInfoToJsonString(LocBarcodeInfo locBarcodeInfo) {
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(locBarcodeInfo);
		
		return jsonString;
	}
}
