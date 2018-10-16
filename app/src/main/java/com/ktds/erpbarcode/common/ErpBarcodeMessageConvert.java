package com.ktds.erpbarcode.common;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class ErpBarcodeMessageConvert {
	private static final String TAG = "ErpBarcodeMessageConvert";
	
	/**
	 * JSON String 데이터를 ErpBarcodeMessage Class로 변환.
	 * 
	 * @param jsonString
	 */
	public static ErpBarcodeMessage jsonStringToErpBarcodeMessage(String jsonString) {
		
		ErpBarcodeMessage erpBarcodeMessage = null;
		JSONObject parameter = new JSONObject();
		
		JSONTokener jsonTokener = new JSONTokener(jsonString);
		try {
			parameter = (JSONObject) jsonTokener.nextValue();
			
			int messageCode = parameter.getInt("messageCode");
			String message = parameter.getString("message");
			int sound = parameter.getInt("sound");
			
			erpBarcodeMessage = new ErpBarcodeMessage(messageCode, message, sound);
			
		} catch (JSONException e) {
			Log.i(TAG, "오류 데이터 변환중 오류가 발생했습니다. " + e.getMessage());
		}
		
		return erpBarcodeMessage;
	}

	/**
	 * ErpBarcodeMessage Class 데이터를 JSON String로 변환.
	 * 
	 * @param ErpBarcodeMessage
	 */
	public static String ErpBarcodeMessageToJsonString(ErpBarcodeMessage erpBarcodeMessage) {
		
		JSONObject parameter = new JSONObject();
		try {
			parameter.put("messageCode", erpBarcodeMessage.getMessageCode());
			parameter.put("message", erpBarcodeMessage.getMessage());
			parameter.put("sound", erpBarcodeMessage.getSound());
		} catch (JSONException e) {
			Log.i(TAG, "오류 데이터 변환중 오류가 발생했습니다. " + e.getMessage());
		}
		
		return parameter.toString();
	}
}
