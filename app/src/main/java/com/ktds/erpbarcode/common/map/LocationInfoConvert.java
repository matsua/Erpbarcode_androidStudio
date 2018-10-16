package com.ktds.erpbarcode.common.map;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LocationInfoConvert {
	//private static final String TAG = "LocationInfoConvert";
	
	public static LocationInfo jsonStringToLocationInfo(String jsonString) {

		Gson gson = new Gson();
		Type deviceType = new TypeToken<LocationInfo>(){}.getType();
		LocationInfo locationInfo = gson.fromJson(jsonString, deviceType);
		
		return locationInfo;
	}
	
	public static String locationInfoToJsonString(LocationInfo locationInfo) {
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(locationInfo);
		
		return jsonString;
	}
}
