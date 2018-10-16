package com.ktds.erpbarcode.common.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.encryption.StringEncrypter;

public class OutputParameter {
	private static final String TAG = "OutputParameter";
	
	private JSONObject mRootParameter;
	
	public OutputParameter(String gzipString) throws ErpBarcodeException  {
		String originalString = decodeDecryptString(gzipString);
		
		Log.i(TAG, "<<<<< output parameter >>>>>    originalString==>"+originalString);
		
		JSONTokener jsonTokener = new JSONTokener(originalString);
		try {
			mRootParameter = (JSONObject) jsonTokener.nextValue();
		} catch (JSONException e) {
			mRootParameter = null;
		}
	}
	
    private String decodeDecryptString(String encrypted) throws ErpBarcodeException {
    	String date = SystemInfo.getNowDate();
		StringEncrypter decrypt = new StringEncrypter("kt_erp_common_key" + "_" + date, "kt_erp_common_iv");
		String zipString = decrypt.decrypt(encrypted);

    	GZipManager gzip = new GZipManager(zipString);
        String originalString = gzip.deCompress();

        return originalString;
    }
    
    public boolean isSuccess() {
    	if (mRootParameter==null)
    		return false;
    	
    	boolean isMessage = mRootParameter.has("message");
    	
    	if (!isMessage) {
    		return false;
    	} else {
    		try {
				JSONObject header = getHeader();
				int status = header.getInt("status");
				if (status <= 0) {
					return false;
				}
				
			} catch (ErpBarcodeException e) {
				return false;
			} catch (JSONException e) {
				return false;
			}
    	}
    	
    	return true;
    }
    
    public int getStatus() {
    	int status = 0;
    	boolean isMessage = mRootParameter.has("message");
    	if (isMessage) {
    		try {
				JSONObject header = getHeader();
				status = header.getInt("status");
				
				//---------------------------------------------------
				// Session Time Out (-1) ErrorCode 는 (-99999)로 변경한다.
				//---------------------------------------------------
				if (status == -1) status = -99999;
				
			} catch (ErpBarcodeException e) {
				return -1;
			} catch (JSONException e) {
				return -1;
			}
    	}
    	return status;
    }
    
    public String getOutMessage() {
    	String message = "";
    	boolean isMessage = mRootParameter.has("message");
    	if (isMessage) {
    		try {
				JSONObject header = getHeader();
				message = header.getString("detail");
			} catch (ErpBarcodeException e) {
				return "";
			} catch (JSONException e) {
				return "";
			}
    	}
    	return message;
    }
    
    public JSONObject getMessgae() throws ErpBarcodeException {
    	JSONObject jsonMessage = null;
    	try {
			jsonMessage = mRootParameter.getJSONObject("message");
		} catch (JSONException e1) {
			Log.i(TAG, "존재하지 않는 메시지정보입니다. ");
			throw new ErpBarcodeException(-1, "존재하지 않는 메시지정보입니다. ");
		}
    	return jsonMessage;
    }
    
    public JSONObject getHeader() throws ErpBarcodeException {

    	boolean isHeader = getMessgae().has("header");
    	if (!isHeader) {
    		Log.i(TAG, "존재하지 않는 헤더정보입니다. ");
			throw new ErpBarcodeException(-1, "존재하지 않는 헤더정보입니다. ");
    	}
    	
    	JSONObject header = null;
		try {
			header = getMessgae().getJSONObject("header");
		} catch (JSONException e) {
			Log.i(TAG, "헤더정보 추출중 오류가 발행했습니다. " +e.getMessage());
			throw new ErpBarcodeException(-1, "헤더정보 추출중 오류가 발행했습니다. ");
		}
    	
    	return header;
    }
    
    public JSONObject getBody() throws ErpBarcodeException {
    	
    	boolean isBody = getMessgae().has("body");
    	if (!isBody) {
    		Log.i(TAG, "존재하지 않는 바디정보입니다. ");
			throw new ErpBarcodeException(-1, "존재하지 않는 바디정보입니다. ");
    	}
    	
    	JSONObject body = null;
		try {
			body = getMessgae().getJSONObject("body");
		} catch (JSONException e) {
			Log.i(TAG, "바디정보 추출중 오류가 발행했습니다. " +e.getMessage());
			throw new ErpBarcodeException(-1, "바디정보 추출중 오류가 발행했습니다. ");
		}
    	
    	return body;
    }
    
    public JSONArray getBodyResults() throws ErpBarcodeException {
    	boolean isResults = getBody().has("result");
    	if (!isResults) {
    		Log.i(TAG, "존재하지 않는 바디result정보입니다. ");
			throw new ErpBarcodeException(-1, "존재하지 않는 바디 result정보입니다. ");
    	}
    	
    	JSONArray results = null;
		try {
			results = getBody().getJSONArray("result");
		} catch (JSONException e) {
			Log.i(TAG, "바디result정보 추출중 오류가 발행했습니다. " +e.getMessage());
			throw new ErpBarcodeException(-1, "바디result정보 추출중 오류가 발행했습니다. ");
		}
		
		return results;
    }
    
    public JSONArray getBodySubResults() throws ErpBarcodeException {
    	boolean isResults = getBody().has("subResult");
    	if (!isResults) {
    		Log.i(TAG, "존재하지 않는 바디_서브결과정보입니다. ");
			throw new ErpBarcodeException(-1, "존재하지 않는 바디_서브결과정보입니다. ");
    	}
    	
    	JSONArray subResults = null;
		try {
			subResults = getBody().getJSONArray("subResult");
		} catch (JSONException e) {
			Log.i(TAG, "바디_서브결과정보 추출중 오류가 발행했습니다. " +e.getMessage());
			throw new ErpBarcodeException(-1, "바디_서브결과정보 추출중 오류가 발행했습니다. ");
		}
		
		return subResults;
    }

}
