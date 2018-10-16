package com.ktds.erpbarcode.infosearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class InfoHttpController {
	private static final String TAG = "InfoHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public InfoHttpController() throws ErpBarcodeException {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	
	/**
	 * 설비바코드 상세 정보 조회.
	 * 
	 * @param facCd        설비바코드
	 */
	public JSONObject getFacBarcodeDetailData(String facCd) throws ErpBarcodeException {

		// 설비정보 상세 조회.
		choicePath(HttpAddressConfig.PATH_GET_FACINFOINQUERY_DETAIL);
				
		
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("EQUNR", facCd);

			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), "설비정보상세 ~바디결과정보가 없습니다. ");
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		Log.i(TAG, "설비정보상세 결과건수 ==>"+jsonresults.length());
		
		JSONObject jsonresult = null;
		try {
			jsonresult = jsonresults.getJSONObject(0);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "설비정보상세 결과정보가 없습니다. ");
		}
		
		if (jsonresult==null) {
			throw new ErpBarcodeException(-1, "설비정보상세 결과정보가 없습니다. ");
		}
		
		return jsonresult;
	}
	

}
