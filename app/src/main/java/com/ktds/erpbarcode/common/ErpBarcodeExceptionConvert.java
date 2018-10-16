package com.ktds.erpbarcode.common;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class ErpBarcodeExceptionConvert {
	private static final String TAG = "ErpBarcodeExceptionConvert";
	
	/**
	 * JSON String 데이터를 ErpBarcodeException Class로 변환.
	 * 
	 * @param jsonString
	 */
	public static ErpBarcodeException jsonStringToErpBarcodeException(String jsonString) {
		
		ErpBarcodeException erpBarcodeException = null;
		JSONObject parameter = new JSONObject();
		
		JSONTokener jsonTokener = new JSONTokener(jsonString);
		try {
			parameter = (JSONObject) jsonTokener.nextValue();
			
			int errCode = parameter.getInt("errCode");
			String errMessage = parameter.getString("errMessage");
			int sound = parameter.getInt("sound");
			
			erpBarcodeException = new ErpBarcodeException(errCode, errMessage, sound);
			
		} catch (JSONException e) {
			Log.i(TAG, "오류 데이터 변환중 오류가 발생했습니다. " + e.getMessage());
			//erpBarcodeException = new ErpBarcodeException(-1, "오류 데이터 변환중 오류가 발생했습니다.", BarcodeSoundPlay.SOUND_NOT_PLAY);
		}
		
		return erpBarcodeException;
	}

	/**
	 * ErpBarcodeException Class 데이터를 JSON String로 변환.
	 * 
	 * @param erpBarcodeException
	 */
	public static String erpBarcodeExceptionToJsonString(ErpBarcodeException erpBarcodeException) {
		
		JSONObject parameter = new JSONObject();
		try {
			parameter.put("errCode", erpBarcodeException.getErrCode());
			parameter.put("errMessage", erpBarcodeException.getErrMessage());
			parameter.put("sound", erpBarcodeException.getSound());
		} catch (JSONException e) {
			Log.i(TAG, "오류 데이터 변환중 오류가 발생했습니다. " + e.getMessage());
		}

		return parameter.toString();
	}
}
