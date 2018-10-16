package com.ktds.erpbarcode.barcode.model;

import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.GlobalData;

public class WBSConvert {
	//private static final String TAG = "WBSConvert";
	
	/**
	 * JSON ARRAY 형태의 데이터를 List<WBSInfo> Class로 변환. 
	 * 
	 * @param jsonArrayString
	 */
	public static List<WBSInfo> jsonArrayStringToWBSInfos(String jsonArrayString) {

		Gson gson = new Gson();
		Type listType = new TypeToken<List<WBSInfo>>(){}.getType();
		List<WBSInfo> wbsInfos = gson.fromJson(jsonArrayString, listType);
		
		return wbsInfos;
	}
	
	/**
	 * List<WBSInfo> Class 데이터를 JSON ARRAY 형태로 변환. 
	 * 
	 * @param wbsInfos
	 */
	public static String wbsInfosToJsonArrayString(List<WBSInfo> wbsInfos) {
		
		Gson gson = new Gson();
		String jsonArrayString = gson.toJson(wbsInfos);
		
		return jsonArrayString;
	}
	
	/**
	 * JSON형태의 데이터를 WBS 정보로 변환.
	 * 
	 * @param jsonobj
	 * @return
	 * @throws JSONException
	 */
    public static WBSInfo jsonStringToWBSInfo(JSONObject jsonobj) throws JSONException {
    	WBSInfo wbsInfo = new WBSInfo();
    	String contractorName = jsonobj.getString("NAME1").replace("null", "");	// 시공사정보
    	String wbsNo = jsonobj.getString("POSID").replace("null", "");			// wbs 번호
    	String wbsHistory = jsonobj.getString("POST1").replace("null", "");		// wbs 내역
    	wbsInfo.setContractorName(contractorName);
    	wbsInfo.setWbsNo(wbsNo);
    	wbsInfo.setWbsHistory(wbsHistory);
    	if (GlobalData.getInstance().getJobGubun().equals("인계") || GlobalData.getInstance().getJobGubun().equals("인수") || GlobalData.getInstance().getJobGubun().equals("시설등록"))
        {
	    	String jpjtType = jsonobj.getString("ZPJT_CODET").replace("null", "");
	    	String wbsStatus = jsonobj.getString("STATUS").replace("null", "");
	    	String wbsStatusName = "";
	    	if (wbsStatus.equals("01")) {
	    		wbsStatusName = "01-생성(CRTD)";
	    	} else if (wbsStatus.equals("02")) {
	    		wbsStatusName = "02-릴리즈(REL)";
	    	} else if (wbsStatus.equals("03")) {
	    		wbsStatusName = "03-종료(CLSD)";
	    	} else {
	    		wbsStatusName = "";
	    	}
	    	
	    	String kostlStatus = jsonobj.getString("KOSTL_STATUS").replace("null", "");
    	
	    	wbsInfo.setJpjtType(jpjtType);
	    	wbsInfo.setWbsStatus(wbsStatus);
	    	wbsInfo.setWbsStatusName(wbsStatusName);
	    	wbsInfo.setKostlStatus(kostlStatus);
        } else {
	    	wbsInfo.setJpjtType("");
	    	wbsInfo.setWbsStatus("");
	    	wbsInfo.setWbsStatusName("");
	    	wbsInfo.setKostlStatus("");
        }
    	return wbsInfo;
    }
}
