package com.ktds.erpbarcode.management.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class TransferHttpController {
	private static final String TAG = "TransferHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public TransferHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	

	/**
	 * 인계대상정보 조회.
	 * 
	 * @param locCd     위치바코드
	 * @param wbsNo     WBS번호
	 */
	public JSONArray getTransferScanData(String locCd, String wbsNo) throws ErpBarcodeException {

		// 인계대상 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_TRANSFERSCAN);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_LOCCODE", locCd);
			jsonParam.put("I_POSID", wbsNo);

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 인계대상정보입니다. ");
		}
		Log.i(TAG, "인계대상 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 인계대상정보 서브결과 조회.
	 * 
	 * @param locCd     위치바코드
	 * @param wbsNo     WBS번호
	 */
	public JSONArray getTransferScanSubResultsData(String locCd, String wbsNo) throws ErpBarcodeException {

		// 인계대상 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_TRANSFERSCAN);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_LOCCODE", locCd);
			jsonParam.put("I_POSID", wbsNo);

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodySubResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 인계대상정보입니다. ");
		}
		Log.i(TAG, "인계대상 서브결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}

	
	/**
	 * 인수대상정보 서브결과 조회.
	 * 
	 * @param locCd     위치바코드
	 * @param wbsNo     WBS번호
	 */
	public JSONArray getArgumentScanData(String locCd, String wbsNo) throws ErpBarcodeException {

		// 인계대상 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_ARGUMENTSCAN);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_DATA_C", "1");
			jsonParam.put("I_LOCCODE", locCd);
			jsonParam.put("I_POSID", wbsNo);

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 인수대상정보입니다. ");
		}
		Log.i(TAG, "인수대상 서브결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 시설등록대상정보 서브결과 조회.
	 * 
	 * @param locCd     위치바코드
	 * @param wbsNo     WBS번호
	 */
	public JSONArray getInstConfScanData(String locCd, String wbsNo) throws ErpBarcodeException {

		// 시설등록대상 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_INSTCONFSCAN);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_POSID", wbsNo);
			jsonParam.put("I_LOCCODE", locCd);
			jsonParam.put("I_DATA_C", "2");


			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 시설등록대상 정보입니다. ");
		}
		Log.i(TAG, "시설등록대상 서브결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 인수확정 정보 조회
	 * 
	 * @param wbsNo           WBS번호
	 * @param devideId        장치ID
	 */
	public JSONArray getArgumentScanConfirmData(String wbsNo, String devideId) throws ErpBarcodeException {

		// 인수확정 조회.
		choicePath(HttpAddressConfig.PATH_GET_ARGUMENTSCAN_CONFIRM);

		JSONObject jsonParam = new JSONObject();
		try {
			if (GlobalData.getInstance().getJobGubun().equals("인수")) {
				jsonParam.put("POSID", wbsNo);
			}
			jsonParam.put("DEVICEID", devideId);
			jsonParam.put("WORKSTEP", "02");
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParam(jsonParam);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 시설등록대상 정보입니다. ");
		}
		Log.i(TAG, "시설등록대상 서브결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
}
