package com.ktds.erpbarcode.survey.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class SurveyHttpController {
	private static final String TAG = "SpotCheckHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public SurveyHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	
	/**
	 * 현장점검 serial 번호 가져오기.
	 */
	public JSONObject getSpotCheckGetSerial() throws ErpBarcodeException {
		
		// 현장점검 serial 번호 가져오기.
		choicePath(HttpAddressConfig.PATH_GET_SPOTCHECKGETSERIAL);
		

		InputParameter input = new InputParameter();
		JSONArray jsonParamList = new JSONArray();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		JSONObject jsonresult = null;
		try {
			jsonresult = jsonresults.getJSONObject(0);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "현장점검 serial 번호 결과정보가 없습니다. ");
		}
		
		if (jsonresult==null) {
			Log.d(TAG, "현장점검 serial 번호 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "현장점검 serial 번호 결과정보가 없습니다. ");
		}
		
		return jsonresult;
	}
	
	/**
	 * 플랜트 얻기
	 */
	public JSONArray getPlant() throws ErpBarcodeException {
		// 플랜트 얻기
		choicePath(HttpAddressConfig.PATH_GET_GETPLANT);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("KOSTL", SessionUserData.getInstance().getOrgId());

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
		if (jsonresults==null) {
			Log.d(TAG, "플랜트 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "플랜트 결과정보가 없습니다. ");
		}
		Log.d(TAG, "플랜트 결과 건수 ==>" + jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 저장위치 조회
	 * 
	 * @param plant    플랜트
	 */
	public JSONArray getSL(String plant) throws ErpBarcodeException {
		// 저장위치 얻기
		choicePath(HttpAddressConfig.PATH_GET_GETSL);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("PLANT", plant);

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
		if (jsonresults==null) {
			Log.d(TAG, "저장위치 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "저장위치 결과정보가 없습니다. ");
		}
		Log.d(TAG, "저장위치 결과 건수 ==>" + jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 실사문서 조회
	 * 
	 * @param year       회계년도
	 * @param month      회계월
	 * @param plant      플랜트
	 * @param slCode     저장위치
	 */
	public JSONArray getIBLNR(String year, String month, String plant, String slCode) throws ErpBarcodeException {
		// 실사문서 얻기
		choicePath(HttpAddressConfig.PATH_GET_GETIBLNR);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("GJAHR", year);
			jsonParam.put("ZZPI_IND", month);
			jsonParam.put("WERKS", plant);
			jsonParam.put("LGORT", slCode);

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
		if (jsonresults==null) {
			Log.d(TAG, "실사문서 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "실사문서 결과정보가 없습니다. ");
		}
		Log.d(TAG, "실사문서 결과 건수 ==>" + jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 실사자재 조회
	 * 
	 * @param year      회계년도
	 * @param iblnr     실사문서
	 */
	public JSONArray getProductSurveyList(String year, String iblnr) throws ErpBarcodeException {
		// 실사자재 얻기
		choicePath(HttpAddressConfig.PATH_GET_GETPRODUCTSURVEYLIST);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("GJAHR", year);
			jsonParam.put("IBLNR", iblnr);

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
		if (jsonresults==null) {
			Log.d(TAG, "실사자재 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "실사자재 결과정보가 없습니다. ");
		}
		Log.d(TAG, "실사자재 결과 건수 ==>" + jsonresults.length());
		
		return jsonresults;
	}
	
	/**
	 * 실사 스캔 리스트 얻기
	 * 
	 * @param year           회계년도
	 * @param iblnr          실사문서
	 * @param itemNumber     아이템건수
	 */
	public JSONArray getProductSurveyScanList(String year, String iblnr, String itemNumber) throws ErpBarcodeException {
		// 실사 스캔 리스트 얻기
		choicePath(HttpAddressConfig.PATH_GET_GETPRODUCTSURVEYSCANLIST);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("GJAHR", year);   // 회계연도
			jsonParam.put("IBLNR", iblnr);  // 실사문서번호
			jsonParam.put("ZEILI", itemNumber);  // 항번

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
		if (jsonresults==null) {
			Log.d(TAG, "실사스캔리스트 결과정보가 없습니다. ");
			throw new ErpBarcodeException(-1, "실사스캔리스트 결과정보가 없습니다. ");
		}
		Log.d(TAG, "실사스캔리스트 결과 건수 ==>" + jsonresults.length());
		
		return jsonresults;
	}
}
