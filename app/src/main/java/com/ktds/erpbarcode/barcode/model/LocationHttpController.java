package com.ktds.erpbarcode.barcode.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class LocationHttpController {
	private static final String TAG = "LocationHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public LocationHttpController() throws ErpBarcodeException {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	
	/**
	 * 위치바코드 정보 조회.
	 * 
	 * @param locCd
	 */
	public LocBarcodeInfo getLocBarcodeData(String locCd) throws ErpBarcodeException {
		// 위치바코드 조회.
		choicePath(HttpAddressConfig.PATH_GET_LOC_CHECK);
				
		
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("LOC_CODE", locCd);

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
		Log.d(TAG, "위치바코드정보 결과건수 ==>"+jsonresults.length());
		
		JSONObject jsonresult = null;
		try {
			jsonresult = jsonresults.getJSONObject(0);
		} catch (JSONException e) {
			Log.d(TAG, "위치바코드 결과정보가 없습니다. JSONException==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "위치바코드 결과정보가 없습니다. ");
		}
		
		if (jsonresult==null) {
			Log.d(TAG, "위치바코드 결과정보가 없습니다.");
			throw new ErpBarcodeException(-1, "위치바코드 결과정보가 없습니다. ");
		}
		
		LocBarcodeInfo locinfo = new LocBarcodeInfo();
		try {
			locinfo.setLocCd(jsonresult.getString("completeLocationCode").trim());
			locinfo.setLocName(jsonresult.getString("locationShortName").trim());
			locinfo.setDeviceId(jsonresult.getString("deviceId").trim());
			locinfo.setRoomTypeCode(jsonresult.getString("roomTypeCode").trim());
			locinfo.setRoomTypeName(jsonresult.getString("roomTypeName").trim());
			locinfo.setOperationSystemCode(jsonresult.getString("operationSystemCode").trim());
			locinfo.setOrgId(jsonresult.getString("zkostl").trim());
			locinfo.setOrgName(jsonresult.getString("zktext").trim());
			locinfo.setLocationCode(jsonresult.getString("locationCode").trim());
			locinfo.setLocationFullName(jsonresult.getString("locationFullName").trim());
			locinfo.setLocationName(jsonresult.getString("locationName").trim());
			locinfo.setLocationName2(jsonresult.getString("locationName2").trim());
			locinfo.setLocationShortName(jsonresult.getString("locationShortName").trim());
			locinfo.setLocationTypeCode(jsonresult.getString("locationTypeCode").trim());
			locinfo.setLocationTypeName(jsonresult.getString("locationTypeName").trim());
		} catch (JSONException e) {
			Log.d(TAG, "위치바코드 결과정보 대입중 오류가 발생했습니다. JSONException==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "위치바코드 결과정보 대입중 오류가 발생했습니다. ");
		}
		
		return locinfo;
	}
	
	/**
	 * 위치바코드 목록 조회.
	 * 
	 * @param locCd
	 */
	public List<LocBarcodeInfo> getLocBarcodesData(String locCd) throws ErpBarcodeException {
		// 위치바코드 조회.
		choicePath(HttpAddressConfig.PATH_GET_LOC_CHECK);
				
		
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("LOC_CODE", locCd);

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
		Log.d(TAG, "위치바코드정보 결과건수 ==>"+jsonresults.length());
		
		List<LocBarcodeInfo> locBarcodeInfos = new ArrayList<LocBarcodeInfo>();
		
		for (int i=0;i<jsonresults.length();i++) {
			LocBarcodeInfo locinfo = new LocBarcodeInfo();
			try {
				JSONObject jsonobj = jsonresults.getJSONObject(i);

				locinfo.setLocCd(jsonobj.getString("completeLocationCode"));
				locinfo.setLocName(jsonobj.getString("locationShortName"));
				locinfo.setDeviceId(jsonobj.getString("deviceId"));
				locinfo.setRoomTypeCode(jsonobj.getString("roomTypeCode"));
				locinfo.setRoomTypeName(jsonobj.getString("roomTypeName"));
				locinfo.setOperationSystemCode(jsonobj.getString("operationSystemCode"));
				locinfo.setOrgId(jsonobj.getString("zkostl"));
				locinfo.setOrgName(jsonobj.getString("zktext"));
				
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "자재정보 대입(JSON)중 오류가 발생했습니다." + e.getMessage());
			}
			locBarcodeInfos.add(locinfo);
		}

		return locBarcodeInfos;
	}
	
	/**
	 * 위치바코드 유효한지 체크만 한다.
	 * 
	 * @param locCd
	 */
	public void getLocBarcodeCheckData(String locCd) throws ErpBarcodeException {
		// 위치바코드 조회.
		choicePath(HttpAddressConfig.PATH_GET_LOC_WM_CHECK);
				
		
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_ZLOCCODE", locCd);

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
		Log.d(TAG, "위치바코드정보 OTD 결과건수 ==>"+jsonresults.length());

	}
	
	/**
	 * 논리위치바코드(창고위치) 조회.
	 * 
	 * @return
	 */
	public JSONArray getLogicalLocationCode() throws ErpBarcodeException {
		// 논리위치바코드(창고위치) 조회.
		choicePath(HttpAddressConfig.PATH_GET_LOGICAL_LOCATION_CODE);

		
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("companyCode", SessionUserData.getInstance().getCompanyCode());
			jsonParam.put("organizationCode", SessionUserData.getInstance().getOrgId());

			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			Log.d(TAG, "파라미터리스트 대입중 오류가 발생했습니다. " + e.getMessage());
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
			throw new ErpBarcodeException(-1, "논리위치바코드(창고위치) 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "논리위치바코드(창고위치) 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;

	}
	
	/**
	 * 현장점검 장치아이디 하위 설비의 위치코드 리스트 가져오기.
	 * 
	 * @param deviceBarcode
	 */
	public List<LocBarcodeInfo> getSpotCheckGetLocDataByDeviceId(String deviceBarcode) throws ErpBarcodeException {
		// 현장점검 장치아이디 하위 설비의 위치코드 리스트 가져오기.
		choicePath(HttpAddressConfig.PATH_GET_SPOTCHECK_GETLOCCODEBYDEVICEID);
		

		InputParameter input = new InputParameter();
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_DEVICE_IDGC", deviceBarcode);
			
			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "장치아이디 하위설비정보 서버에 요청중 오류가 발생했습니다. ");
		}
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		
		List<LocBarcodeInfo> locBarcodeInfos = new ArrayList<LocBarcodeInfo>();
		for (int i=0;i<jsonresults.length();i++) {
			LocBarcodeInfo locinfo = new LocBarcodeInfo();
			try {
				JSONObject jsonobj = jsonresults.getJSONObject(i);

				locinfo.setLocCd(jsonobj.getString("ZEQUIPLP").trim());
				locinfo.setLocName(jsonobj.getString("ZEQUIPLP_TXT").trim());
				
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "장치바코드에 해당하는 위치바코드 목록 대입(JSON)중 오류가 발생했습니다." + e.getMessage());
			}
			if (!locinfo.getLocCd().isEmpty()) {
				locBarcodeInfos.add(locinfo);
			}
		}
		
		return locBarcodeInfos;
	}
	
	/**
	 * 철거 WBS 조회.
	 * 
	 * @param barcode         설비바코드
	 * @param workCat         
	 */
	public JSONArray getRemovalWbsData(String barcode, String workCat) throws ErpBarcodeException {

		// 철거 WBS 조회.
		choicePath(HttpAddressConfig.PATH_GET_REMOVAL_WBS);
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();

            jsonParam.put("I_LOCCODE", barcode);
            if (!workCat.isEmpty()) {
            	jsonParam.put("I_WORKCAT", workCat);
            }

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "조회 요청조건 대입중 오류가 발생했습니다. ");
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
			throw new ErpBarcodeException(-1, "철거WBS 정보 조회중 오류가 발생했습니다. ");
		}
		Log.i(TAG, "철거WBS 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}
	
	/**
	 * 서버에서 WBS정보를 JSON형태로 조회한다.
	 * 
	 * @param barcode
	 * @param workCat
	 */
	public JSONArray getWbsData(String barcode, String workCat) throws ErpBarcodeException {

		String jobGubun = GlobalData.getInstance().getJobGubun();
		
		// WBS 조회.
    	if (jobGubun.equals("철거") || jobGubun.equals("다중철거")) {
    		choicePath(HttpAddressConfig.PATH_GET_REMOVAL_WBS);
        } else {
        	choicePath(HttpAddressConfig.PATH_GET_WBS);
        }
		
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();

            jsonParam.put("I_LOCCODE", barcode);
            if (!jobGubun.equals("철거") && !jobGubun.equals("다중철거")) {
            	jsonParam.put("I_WORKCAT", workCat);
            }

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "조회 요청조건 대입중 오류가 발생했습니다. ");
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
			throw new ErpBarcodeException(-1, "WBS 정보 조회중 오류가 발생했습니다. ");
		}
		Log.i(TAG, "WBS 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}
	
	/**
	 * 서버에서 WBS정보를 조회한다.
	 * 
	 * @param barcode
	 * @param workCat
	 */
	public List<WBSInfo> getWbsInfos(String barcode, String workCat) throws ErpBarcodeException {
		JSONArray jsonResults = getWbsData(barcode, workCat);
		
		List<WBSInfo> wbsInfos = new ArrayList<WBSInfo>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	WBSInfo wbsInfo = WBSConvert.jsonStringToWBSInfo(jsonobj);
            	wbsInfos.add(wbsInfo);
    		} catch (JSONException e) {
    			Log.d(TAG, "getWbsInfos  WBS 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "WBS 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return wbsInfos;
	}
	
	/**
	 * 위치바코드 주소 조회.
	 * 
	 * @param locCd
	 */
	public JSONObject getLocBarcodeAdd(String locCd) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_GET_LOC_ADDNAME);
		
		JSONArray jsonParamList = new JSONArray();
		
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("locationCode", locCd);

			jsonParamList.put(jsonParam);
		} 
		catch (JSONException e) {
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
		JSONObject jsonresult = null;
		JSONObject jsonresultData = new JSONObject();
		
		try{
			jsonresult = jsonresults.getJSONObject(0);
		}catch (JSONException e) {
			throw new ErpBarcodeException(-1, "위치바코드 결과정보가 없습니다. ");
		}
		
		if (jsonresult == null) {
			throw new ErpBarcodeException(-1, "위치바코드 결과정보가 없습니다. ");
		}
		
		try {
			//법정동주소 
			String legalDongName = jsonresult.getString("legalDongName");
			String addressTypeName = jsonresult.getString("addressTypeName");
			String bunji = jsonresult.getString("bunji");
			String ho = jsonresult.getString("ho");
			String detailAddress = jsonresult.getString("detailAddress");
			
			String legalAddress = legalDongName;
			
            if (addressTypeName.equals("산")){
            	legalAddress += " 산";
            }

            legalAddress += " " + bunji;
            if (!ho.equals("")) legalAddress += "-" + ho;
            if (!detailAddress.equals("")) legalAddress += " " + detailAddress;
            
            //도로명주소 
            String roadName = jsonresult.getString("roadName");
            String buildingMainNo = jsonresult.getString("buildingMainNo");
            String buildingSubNo = jsonresult.getString("buildingSubNo");
            
            String loadAddr = roadName + " " + buildingMainNo;
            if (!buildingSubNo.equals("")){
            	loadAddr += "-" + buildingSubNo;
            }
            
            jsonresultData.put("legalAddr", legalAddress);
            jsonresultData.put("loadAddr", loadAddr);
			
		} catch (JSONException e) {
			Log.d(TAG, "주소조회 : 위치바코드주소조회 결과정보 대입중 오류가 발생했습니다. JSONException==>"+e.getMessage());
			throw new ErpBarcodeException(-1, "위치바코드 결과정보 대입중 오류가 발생했습니다. ");
		}

		return jsonresultData;
	}
	
	/**
	 * 고장이력리스트를 조회한다.
	 * 
	 * @param fac_barcode
	 */
	public List<FailureListInfo> getFailureList(String barcode, String deviceid) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_GET_FAILURE_LIST);

		InputParameter input = new InputParameter();
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			if(barcode.length() > 0) jsonParam.put("EQUNR", barcode);
			if(deviceid.length() > 0) jsonParam.put("ZEQUIPGC", deviceid);
			
			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "고장이력 서버에 요청중 오류가 발생했습니다. ");
		}
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		
		List<FailureListInfo> failureListInfos = new ArrayList<FailureListInfo>();
		for (int i=0;i<jsonresults.length();i++) {
			FailureListInfo failureinfo = new FailureListInfo();
			try {
				JSONObject jsonobj = jsonresults.getJSONObject(i);
				failureinfo.setEqunr(jsonobj.getString("EQUNR").trim());
				failureinfo.setZequipgc(jsonobj.getString("ZEQUIPGC").trim());
				failureinfo.setMatnr(jsonobj.getString("MATNR").trim());
				failureinfo.setMaktx(jsonobj.getString("MAKTX").trim());
				failureinfo.setAuldt(jsonobj.getString("AULDT").trim());
				failureinfo.setErdat(jsonobj.getString("ERDAT").trim());
				failureinfo.setAusbs(jsonobj.getString("AUSBS").trim());
				failureinfo.setFecodn(jsonobj.getString("FECODN").trim());
				failureinfo.setUrstx2(jsonobj.getString("URSTX2").trim());
				failureinfo.setLifnrn(jsonobj.getString("LIFNRN").trim());
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "고장이력 대입(JSON)중 오류가 발생했습니다." + e.getMessage());
			}
			if (!failureinfo.getEqunr().isEmpty()) {
				failureListInfos.add(failureinfo);
			}
		}
		return failureListInfos;
	}
	
	
	public void changePassword(String password) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_PASSWORD_CHANGE);
		
		JSONArray jsonParamList = new JSONArray();
		
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userPassword", password);

			jsonParamList.put(jsonParam);
		} 
		catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
	}
	
	public JSONArray requestAuthNum(String userId, String phoneNum, String email) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_REQUEST_USERAUTH);
		
		InputParameter input = new InputParameter();
		
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", userId);
//			jsonParam.put("userName", userName);
			jsonParam.put("phoneNo", phoneNum);
			jsonParam.put("email", email);
			input.setNobodyParam(jsonParam);
		} 
		catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		return jsonresults;
	}
	
	public void confirmAuthNum(String userId, String userAuthNum, String serverAuthNum) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_CONFIRM_USERAUTH);
		
		InputParameter input = new InputParameter();
		
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", userId);
			jsonParam.put("userInputCertificationNumber", userAuthNum);
			jsonParam.put("createFromServerCertificationNumber", serverAuthNum);
			input.setNobodyParam(jsonParam);
		} 
		catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
	}
	
	public void savePassword(String userId, String userPassword, String type) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_SAVE_PASSWORD);
		
		InputParameter input = new InputParameter();
		
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", userId);
			jsonParam.put("userPassword", userPassword);
			if(type.equals("update")){
				jsonParam.put("passwdUpdateYn", "Y");
			}
			input.setNobodyParam(jsonParam);
		} 
		catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
	}
}
