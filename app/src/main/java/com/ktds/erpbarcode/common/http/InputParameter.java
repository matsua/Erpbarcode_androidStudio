package com.ktds.erpbarcode.common.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.encryption.StringEncrypter;

public class InputParameter {
	private static final String TAG = "InputParameter";
	
	private JSONObject mRootParameter;
	private boolean mPageUse = false;
	private String mPage = "1";

	public InputParameter() throws ErpBarcodeException {
		InitializationBaseParameter();
	}
	
	public void setPageValue(boolean pageUse, String page){
		mPageUse = pageUse;
		mPage = page;
		
		System.out.println("setPageValue >>  mPageUse = " + mPageUse);
		System.out.println("setPageValue >>  mPage = " + mPage);
	}
	
	private void InitializationBaseParameter() throws ErpBarcodeException {
		if (mRootParameter == null) {
			mRootParameter = new JSONObject();
		}
		
		SessionUserData sessionUserData = SessionUserData.getInstance();
		
		try {
			JSONObject jsonHeader = new JSONObject();
			jsonHeader.put("userId", sessionUserData.getUserId());
			jsonHeader.put("userPasswd", "");
			jsonHeader.put("userName", sessionUserData.getUserName());
			jsonHeader.put("telNo", sessionUserData.getUserCellPhoneNo());
			jsonHeader.put("userCellPhoneNo", sessionUserData.getUserCellPhoneNo());
			jsonHeader.put("orgId", sessionUserData.getOrgId());
			jsonHeader.put("orgName", sessionUserData.getOrgName());
			jsonHeader.put("orgTypeCode", sessionUserData.getOrgTypeCode());
			if(sessionUserData.getJobEqunr().trim().length() < 1){
				jsonHeader.put("job_equnr", "");
			}else{
				jsonHeader.put("job_equnr", sessionUserData.getJobEqunr().trim());				//	단말설비바코드
			}
			
			// DR-2014-37505 송부취소, 접수 시 1000건만 불러오기 - request by 박장수 2014.08.18 -> 2014.08.19 - 류성호
			if (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)") || GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
				jsonHeader.put("currentPageNo", "1");
				if(GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)")){
					jsonHeader.put("currentPageCount", "1000");
				}else{
					jsonHeader.put("currentPageCount", "500");
				}
				
				jsonHeader.put("pageUseYN", "Y");
			}else if(GlobalData.getInstance().getJobGubun().startsWith("현장점검") && mPageUse){
				jsonHeader.put("currentPageNo", mPage);
				jsonHeader.put("currentPageCount", "1000");
				jsonHeader.put("pageUseYN", "Y");
			}else {
				jsonHeader.put("currentPageNo", "");
				jsonHeader.put("currentPageCount", "");
				jsonHeader.put("pageUseYN", "");
			}
			jsonHeader.put("sessionId", sessionUserData.getSessionId());
			jsonHeader.put("orgCode", sessionUserData.getOrgCode());
			
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("param", new JSONObject());
			jsonBody.put("paramList", new JSONArray());
			jsonBody.put("subParamList", new JSONArray());

			JSONObject jsonMessage = new JSONObject();
			jsonMessage.put("header", jsonHeader);
			jsonMessage.put("body", jsonBody);
			jsonMessage.put("call", "ANDROID");
			jsonMessage.put("jobSeq", Long.valueOf("1"));
			jsonMessage.put("operatorId", "");
			jsonMessage.put("runProgramId", "");
			jsonMessage.put("sysRegisterDate", new JSONObject());
			jsonMessage.put("sysUpdateDate", new JSONObject());
			
			mRootParameter.put("message", jsonMessage);
    
        } catch(Exception e) {
        	Log.i(TAG, "인스턴스 생성중 오류가 발생했습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "인스턴스 생성중 오류가 발생했습니다. ");
        }
	}
	
	public void bodyClear() {
		try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonBody.put("param", new JSONObject());
			jsonBody.put("paramList", new JSONObject());
			jsonBody.put("subParamList", new JSONObject());
		} catch (JSONException e) {
			Log.i(TAG, "입력파라메터 바디정보 초기화중 오류가 발생했습니다. " + e.getMessage());
		}
    }
	
	public JSONObject getBody() throws ErpBarcodeException {
		JSONObject jsonBody;
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			jsonBody = jsonMessage.getJSONObject("body");
		} catch (JSONException e) {
			Log.i(TAG, "바디파라미터 조회중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "바디파라미터 조회중 오류가 발생했습니다. ");
		}
    	return jsonBody;
	}
	
	public JSONObject getParam() throws ErpBarcodeException {
    	JSONObject jsonParam;
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonParam = jsonBody.getJSONObject("param");
		} catch (JSONException e) {
			Log.i(TAG, "입력파라메터 조회중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "입력파라메터 조회중 오류가 발생했습니다. ");
		}
    	return jsonParam;
    }
	
	public void setNomalParam(String key, String value) throws ErpBarcodeException {
		mRootParameter = null;
		mRootParameter = new JSONObject();
    	try {
    		mRootParameter.put(key, value);
		} catch (JSONException e) {
			Log.i(TAG, "파라메터 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터 등록중 오류가 발생했습니다. ");
		}
    }
	
	public void setNomalParam(String key, JSONObject value) throws ErpBarcodeException {
		mRootParameter = null;
		mRootParameter = new JSONObject();
    	try {
    		mRootParameter.put(key, value);
		} catch (JSONException e) {
			Log.i(TAG, "파라메터 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터 등록중 오류가 발생했습니다. ");
		}
    }
	
	public void setNomalParam(JSONObject value) throws ErpBarcodeException {
    	try {
    		mRootParameter = value;
		} catch (Exception e) {
			Log.i(TAG, "파라메터 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터 등록중 오류가 발생했습니다. ");
		}
    }
	
    public void setParam(JSONObject value) throws ErpBarcodeException {
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonBody.put("param", value);
		} catch (JSONException e) {
			Log.i(TAG, "파라메터 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터 등록중 오류가 발생했습니다. ");
		}
    }
    
    public JSONArray getParamList() throws ErpBarcodeException {
    	JSONArray jsonParamList;
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonParamList = jsonBody.getJSONArray("paramList");
		} catch (JSONException e) {
			Log.i(TAG, "파라메터리스트 조회중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터리스트 조회중 오류가 발생했습니다. ");
		}
    	return jsonParamList;
    }

    public void setParamList(JSONArray value) throws ErpBarcodeException {
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonBody.put("paramList", value);
		} catch (JSONException e) {
			Log.i(TAG, "파라메터리스트 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터리스트 등록중 오류가 발생했습니다. ");
		}
    }
    
    public JSONArray getSubParamList() throws ErpBarcodeException {
    	JSONArray jsonSubParamList;
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonSubParamList = jsonBody.getJSONArray("subParamList");
		} catch (JSONException e) {
			Log.i(TAG, "서브파라메터리스트 조회중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "서브파라메터리스트 조회중 오류가 발생했습니다. ");
		}
    	return jsonSubParamList;
    }
    
    public void setSubParamList(JSONArray value) throws ErpBarcodeException {
    	try {
			JSONObject jsonMessage = mRootParameter.getJSONObject("message");
			JSONObject jsonBody = jsonMessage.getJSONObject("body");
			jsonBody.put("subParamList", value);
		} catch (JSONException e) {
			Log.i(TAG, "서브파라메터리스트 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "서브파라메터리스트 등록중 오류가 발생했습니다. ");
		}
    }
    
    public void setNobodyParam(JSONObject value) throws ErpBarcodeException, JSONException {
    	mRootParameter = new JSONObject();
    	mRootParameter = value;
    }
    
    public void setNobodyParamList(JSONArray value) throws ErpBarcodeException {
    	mRootParameter = new JSONObject();
    	try {
			mRootParameter.put("params", value);
		} catch (JSONException e) {
			Log.i(TAG, "파라메터리스트 등록중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라메터리스트 등록중 오류가 발생했습니다. ");
		}
    }
    
    public String encodeEncryptString() throws ErpBarcodeException {
    	
        String message = mRootParameter.toString();
        
        Log.i(TAG, "<<<<< input parameter >>>>>   message ==>"+message);
        
        GZipManager gzip = new GZipManager(message);
        String zipString = gzip.compress();
	    
        // 암호화 작업
		StringEncrypter encrypt = new StringEncrypter("kt_erp_common_key" + "_" + SystemInfo.getNowDate(), "kt_erp_common_iv");
		String encryptString = encrypt.encrypt(zipString);

        return encryptString;
    }

}
