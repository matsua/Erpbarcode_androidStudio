package com.ktds.erpbarcode.barcode.model;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DeviceBarcodeConvert {
	//private static final String TAG = "DeviceBarcodeConvert";
	
	public static DeviceBarcodeInfo jsonStringToDeviceBarcodeInfo(String jsonString) {
		
		Gson gson = new Gson();
		Type deviceType = new TypeToken<DeviceBarcodeInfo>(){}.getType();
		DeviceBarcodeInfo deviceBarcodeInfo = gson.fromJson(jsonString, deviceType);
	
		return deviceBarcodeInfo;
	}
	
	public static String deviceBarcodeInfoToJsonString(DeviceBarcodeInfo deviceBarcodeInfo) {

		Gson gson = new Gson();
		String jsonString = gson.toJson(deviceBarcodeInfo);

		return jsonString;
	}
}
