package com.ktds.erpbarcode.barcode.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

public class BarcodeHttpController {
	private static final String TAG = "BarcodeHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public BarcodeHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	
	/**
	 * 장치바코드 조회
	 * 
	 * @param deviceId
	 */
	public DeviceBarcodeInfo getDeviceBarcodeData(String deviceBarcode) throws ErpBarcodeException {
		// 장치바코드 조회 선택.
		choicePath(HttpAddressConfig.PATH_GET_MULTIINFO);
		

		InputParameter input = new InputParameter();
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("SOURCE_CODE", deviceBarcode);
			
			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다. ");
		}
		
		JSONObject jsonresult = null;
		try {
			jsonresult = jsonresults.getJSONObject(0);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다. ");
		}

		if (jsonresult==null) {
			Log.d(TAG, "유효하지 않은 장치바코드입니다. ");
			throw new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다. ");
		}
		
		DeviceBarcodeInfo deviceInfo = new DeviceBarcodeInfo();
		try {
			deviceInfo.setDeviceId(jsonresult.getString("deviceId").trim());
			deviceInfo.setDeviceName(jsonresult.getString("deviceName").trim());
			deviceInfo.setProjectNo(jsonresult.getString("projectNo").trim());
			deviceInfo.setWbsNo(jsonresult.getString("wbsNo").trim());
			deviceInfo.setOperationSystemToken(jsonresult.getString("operationSystemToken").trim());
			deviceInfo.setOperationSystemTokenName(jsonresult.getString("operationSystemTokenName").trim());
			deviceInfo.setOperationSystemCode(jsonresult.getString("operationSystemCode").trim());
			deviceInfo.setLocationIdToken(jsonresult.getString("locationIdTokenName").trim());
			deviceInfo.setDeviceStatusCode(jsonresult.getString("deviceStatusCode").trim());
			deviceInfo.setDeviceStatusName(jsonresult.getString("deviceStatusName").trim());
			deviceInfo.setItemCode(jsonresult.getString("itemCode").trim());
			deviceInfo.setItemName(jsonresult.getString("itemName").trim());
			deviceInfo.setAssetClassificationCode(jsonresult.getString("assetClassificationCode").trim());
			deviceInfo.setAssetClassificationName(jsonresult.getString("assetClassificationName").trim());
			deviceInfo.setMigrationTypeCode(jsonresult.getString("migrationTypeCode").trim());
			deviceInfo.setMigrationTypeName(jsonresult.getString("migrationTypeName").trim());
			deviceInfo.setArgumentDecisionName(jsonresult.getString("argumentDecisionName").trim());
			deviceInfo.setStandardServiceCode(jsonresult.getString("standardServiceCode").trim());
			deviceInfo.setStandardServiceName(jsonresult.getString("standardServiceName").trim());
			deviceInfo.setLocationCode(jsonresult.getString("locationCode").trim());
			deviceInfo.setLocationName(jsonresult.getString("locationName").trim());
			deviceInfo.setLocationShortName(jsonresult.getString("locationShortName").trim());
			deviceInfo.setDetailAddress(jsonresult.getString("detailAddress").trim());
			deviceInfo.setOperationOrgCode(jsonresult.getString("operationOrgCode").trim());
			deviceInfo.setOperatioinOrgName(jsonresult.getString("operatioinOrgName").trim());
			deviceInfo.setOwnOrgCode(jsonresult.getString("ownOrgCode").trim());
			deviceInfo.setOwnOrgName(jsonresult.getString("ownOrgName").trim());
			deviceInfo.setLevel1Code(jsonresult.getString("level1").trim());
			deviceInfo.setLevel1Name(jsonresult.getString("level1Name").trim());
			deviceInfo.setLevel2Code(jsonresult.getString("level2").trim());
			deviceInfo.setLevel2Name(jsonresult.getString("level2Name").trim());
			deviceInfo.setLevel3Code(jsonresult.getString("level3").trim());
			deviceInfo.setLevel3Name(jsonresult.getString("level3Name").trim());
			deviceInfo.setLevel4Code(jsonresult.getString("level4").trim());
			deviceInfo.setLevel4Name(jsonresult.getString("level4Name").trim());
			
			if (deviceInfo.getOperationSystemToken().equals("02") && deviceInfo.getStandardServiceCode().equals("")) {
				throw new ErpBarcodeException(-1, "장치아이디 '" + deviceBarcode + "' 는 \n\r운영시스템 구분자가 'ITAM' 이며\n\rIT표준서비스코드가 '없음' 이므로\n\r스캔이 불가합니다.\n\r전사기준정보관리시스템(MDM)에\n\r문의하세요.");
			}
			if ((deviceInfo.getOperationSystemToken().equals("04") || deviceInfo.getOperationSystemToken().equals("08") || deviceInfo.getOperationSystemToken().equals("09") || deviceInfo.getOperationSystemToken().equals("10")) && deviceInfo.getOperationSystemCode().equals("")) {
				throw new ErpBarcodeException(-1, "통합NMS 대상 장치ID의 장비ID가\n\r삭제되어 스캔이 불가합니다.\n\r전사기준정보관리시스템(MDM)에\n\r문의하세요.");
			}
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "장치바코드 결과정보 변수대입중 오류가 발생했습니다. " + e.getMessage());
		}
		
		return deviceInfo;
	}
	
	/**
	 * SAP서버에서 바코드정보를 조회.
	 * 
	 * @param orgCode   운용조직
	 * @param locCode   위치바코드
	 * @param deviceId  장치바코드
	 * @param barcode   스캔바코드
	 * @param iFlag ("".단건조회, H.하위장비들 조회.)
	 */
	public JSONArray getSAPBarcodeData(String orgCode, String locCd, String deviceId, String barcode, String iFlag) throws ErpBarcodeException {
		String jobGubun = GlobalData.getInstance().getJobGubun();  
		
		// SAP바코드 조회.
		if (jobGubun.equals("납품취소")) {
			choicePath(HttpAddressConfig.PATH_GET_OUTINTOSTATUSCHANGELIST);
		}
		else if (jobGubun.equals("실장") || jobGubun.equals("수리완료") || jobGubun.equals("형상구성(창고내)")) {
			choicePath(HttpAddressConfig.PATH_GET_FACINFOINQUERY_MM);
		} 
		else if (jobGubun.startsWith("현장점검")) {
			choicePath(HttpAddressConfig.PATH_GET_FACINFO_INQUERY_SPOTCHECK);
		} else {
			choicePath(HttpAddressConfig.PATH_GET_FACINFO_INQUERY);
		}
				
		if (jobGubun.isEmpty()) {
			throw new ErpBarcodeException(-1, "업무구분을 입력하세요. ");
		}
		
		if (!locCd.isEmpty() || !deviceId.isEmpty() || (!barcode.isEmpty()))  {
			//
		} else {
			throw new ErpBarcodeException(-1, "위치/장치바코드/설비바코드를 입력하세요. ");
		}
		

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			
			if  (jobGubun.equals("설비정보") || jobGubun.equals("장치바코드하위설비조회")) {
				if (barcode.length() == 21) {
					jsonParam.put("I_ZEQUIPLP", barcode);
                }
				
				// 장치바코드
				if (barcode.length() == 9) {
					jsonParam.put("I_ZEQUIPGC", barcode);
                    if (!locCd.isEmpty())
                    	jsonParam.put("I_ZEQUIPLP", locCd);

                } else {
                	jsonParam.put("I_EQUNR", barcode);
                }
				
				// 장치바코드하위설비조회는 우선 최상위 바코드만 불러 온다.
                if (jobGubun.equals("장치바코드하위설비조회")) {
                	jsonParam.put("I_FLAG", "H");
                } else {
                	jsonParam.put("I_FLAG", "H");
                }
			} else {
	            if (!locCd.isEmpty()) 
	            	jsonParam.put("I_ZEQUIPLP", locCd);
	            if (!deviceId.isEmpty()) 
	            	jsonParam.put("I_ZEQUIPGC", deviceId);
	            if (!barcode.isEmpty()) 
	            	jsonParam.put("I_EQUNR", barcode);
	            if (!orgCode.isEmpty()) 
	            	jsonParam.put("I_ZKOSTL", orgCode);
	
	            // 하위포함 여부.... 장비실사, 인계, 시설등록, 개조개량은 무조건 full 스캔, 현장점검 추가는 하위설비 가져 오지 않음
                if (jobGubun.startsWith("개조개량")
                		|| jobGubun.equals("장비실사") 
	            		|| jobGubun.equals("인계") 
	            		|| jobGubun.equals("시설등록")
	            		|| jobGubun.equals("설비S/N변경")
	            		|| jobGubun.startsWith("현장점검") && locCd.isEmpty())
                {
                	jsonParam.put("I_FLAG", "");     
                }
                else  if (jobGubun.equals("납품취소")) {
	            	//
	            } else {
            		jsonParam.put("I_FLAG", "H");
	            }
	        }

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
			throw new ErpBarcodeException(-1, "존재하지 않는 설비바코드입니다. ", BarcodeSoundPlay.SOUND_NOTEXISTS);
		}
		Log.i(TAG, "SAP바코드 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<BarcodeListInfo> getSAPBarcodeDataToBarcodeListInfos(String orgCode, String locCd, 
			String deviceId, String barcode, String iFlag) throws ErpBarcodeException {

		JSONArray jsonResults = getSAPBarcodeData(orgCode, locCd, deviceId, barcode, iFlag);
		
		List<BarcodeListInfo> barcodeListInfos = new ArrayList<BarcodeListInfo>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	BarcodeListInfo barcodeInfo = BarcodeInfoConvert.jsonToBarcodeListInfo(jsonobj);
            	barcodeListInfos.add(barcodeInfo);
    		} catch (JSONException e) {
    			Log.d(TAG, "getSAPBarcodeDataToBarcodeListInfos  SAP설비바코드 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "SAP설비바코드 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return barcodeListInfos;
	}
	
	/**
	 * 자재정보 서버에서 조회
	 *    
	 * @param matnr        자재코드
	 * @param maktx        자재명
	 * @param bismt        무선자재코드
	 */
	public JSONArray getBarcodeInfosServer(String matnr, String maktx, String bismt) throws ErpBarcodeException {
		// 서버바코드 조회.
		choicePath(HttpAddressConfig.PATH_GET_ITEMINFO);
		
		if (matnr.isEmpty() && maktx.isEmpty() && bismt.isEmpty()) {
			throw new ErpBarcodeException(-1, "자재코드 또는 자재명을 입력하세요.");
		}
		
		if (!matnr.isEmpty() && matnr.length() < 6)
            throw new ErpBarcodeException(-1, "자재코드를 6자리 이상 입력하세요.");
		if (!maktx.isEmpty() && maktx.length() < 3)
			throw new ErpBarcodeException(-1, "자재명을 3자리 이상 입력하세요.");
		
		boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", matnr);
    	if(!flag)
			throw new ErpBarcodeException(-1, "처리할 수 없는 자재코드입니다.");
    	
		
		InputParameter input = new InputParameter();
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("matnr", matnr);
			jsonParam.put("maktx", maktx);
			jsonParam.put("bismt", bismt);

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		input.setParamList(jsonParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "자재 바디결과정보가 없습니다. ");
		}
		Log.i(TAG, "자재 결과건수 ==>"+jsonresults.length());
		return jsonresults;
	}
	
	public List<ProductInfo> getProductInfosServer(String matnr, String maktx, String bismt) throws ErpBarcodeException {
			// 자재정보를 서버에서 조회한다.
			JSONArray jsonresults = getBarcodeInfosServer(matnr, "", bismt);

			// JSONArray 형태를 List<ProductInfo>로 변환한다.
			List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
	        for (int i=0;i<jsonresults.length();i++) {
	    		try {
	            	JSONObject jsonobj = jsonresults.getJSONObject(i);
	            	
	            	ProductInfo productInfo = BarcodeInfoConvert.jsonToProductInfo(1, jsonobj);
	            	productInfos.add(productInfo);
	    		} catch (JSONException e) {
	    			Log.d(TAG, "getProductInfosServer  자재정보 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
	    			throw new ErpBarcodeException(-1, "자재정보 자료 변환중 오류가 발생했습니다." );
	    		}
	        }

		return productInfos;
	}
	
	/**
	 * 자재정보 + 자산분류 정보를 조회한다.
	 * 
	 * @param matnr  자재코드
	 * @param maktx  자재명
	 * @param bismt  무선자재
	 */
	public List<ProductInfo> getProductInfosAndAssetClassInfoServer(String matnr, String maktx, String bismt) throws ErpBarcodeException {
		// 자재정보를 서버에서 조회한다.
		JSONArray jsonresults = getBarcodeInfosServer(matnr, "", "");

		// JSONArray 형태를 List<ProductInfo>로 변환한다.
		List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
        for (int i=0;i<jsonresults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonresults.getJSONObject(i);
            	
            	ProductInfo productInfo = BarcodeInfoConvert.jsonToProductInfo(1, jsonobj);
            	
            	//-------------------------------------------------------------
            	// 자산분류 정보를 조회한다.
            	//-------------------------------------------------------------
            	BaseHttpController basehttp = new BaseHttpController();
            	AssetClassInfo assetClassInfo = basehttp.getAssetClassInfo(productInfo.getProductCode());
    			if (assetClassInfo == null) {
    				throw new ErpBarcodeException(-1, "유효하지 않은 자산분류 정보입니다.");
    			}
    			
    			productInfo.setAssetClassInfo(assetClassInfo);
            	
            	productInfos.add(productInfo);
    		} catch (JSONException e) {
    			Log.d(TAG, "getProductInfosAndAssetClassInfoServer  자재정보 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "자재정보 자료 변환중 오류가 발생했습니다." );
    		}
        }

		return productInfos;
	}
	
	
	/**
	 * 부서간 이동 조회.
	 * 
	 * @param barcode       설비바코드
	 * @param KOSTL         운용조직코드
	 */
	public JSONArray getMoveRequestData(String barcode, String KOSTL) throws ErpBarcodeException {

		// 부서간 이동 조회.
		choicePath(HttpAddressConfig.PATH_GET_DEPTMOVEREQUEST);
		
		if (GlobalData.getInstance().getJobGubun().isEmpty()) {
			throw new ErpBarcodeException(-1, "업무구분을 입력하세요. ");
		}


		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			
			if  (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)")) {
				jsonParam.put("STEP", "S");
				jsonParam.put("SZKOSTL", SessionUserData.getInstance().getOrgId());  // 코스트센터
                if (!KOSTL.isEmpty()) jsonParam.put("RKOSTL", KOSTL);
                
                // 송부취소(팀간) -자기 조직 아닌 바코드 추가 - request by 박장수 : 2013.08.20
                if (!barcode.isEmpty()) jsonParam.put("EQUNR", barcode);
				jsonParam.put("SRCSYS", "A");

			} else if  (GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
				jsonParam.put("STEP", "S");
				jsonParam.put("RKOSTL", SessionUserData.getInstance().getOrgId());
                if (!KOSTL.isEmpty()) jsonParam.put("SZKOSTL", KOSTL);
                
                // DR-2014-37505 송부취소, 접수 시 1000건만 불러오기, 리스트에 없는 접수 대상 건 스캔시 추 - request by 박장수 2014.08.18 -> 2014.08.19 - 류성호
                if (!barcode.isEmpty()) jsonParam.put("EQUNR", barcode);
				jsonParam.put("SRCSYS", "A");
			}
			
			jsonParam.put("INCL", "X");
            // 이동중만 보이게.. X 전송 시 다 보임
            jsonParam.put("INCLCL", "");

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
			throw new ErpBarcodeException(-1, "부서간이동정보 ~~바디결과정보가 없습니다. ");
		}
		Log.i(TAG, "부서간이동정보 결과건수 ==>"+jsonresults.length());


		return jsonresults;
	}
	
	/**
	 * 고장등록 기준수 초과 체크 
	 * 
	 * @param barcode       설비바코드
	 */
	public JSONArray getBreakDownCheck(String barcode) throws ErpBarcodeException {

		// 고장등록 기준수 초과 체크.
		choicePath(HttpAddressConfig.PATH_GET_BREAKDOWN_CHECK);
		
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			
			jsonParam.put("I_EQUNR", barcode);

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
			throw new ErpBarcodeException(-1, "고장등록 기준수 초과 체크 결과정보가 없습니다. ");
		}
		Log.i(TAG, "고장등록 기준수 초과 체크 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}
	
	/**
	 * 장치바코드 하위 설비 중 운용이면서 유닛 제외 리스트 가져오기
	 * 
	 * @param deviceId       장치ID
	 */
	public JSONArray getDeviceIdBelowFacListData(String deviceId) throws ErpBarcodeException {

		// 장치바코드 하위 설비 중 운용이면서 유닛 제외 리스트 가져오기
		choicePath(HttpAddressConfig.PATH_GET_DEVICEIDBELOWFACLIST);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
            jsonParam.put("I_ZEQUIPGC", deviceId);

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
			throw new ErpBarcodeException(-1, "장치바코드 운영중인 하위설비 목록 ~~바디결과정보가 없습니다. ");
		}
		Log.i(TAG, "장치바코드 운영중인 하위설비 목록 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}
	
	public List<String> getDeviceIdBelowFacListToBarcodeListInfos(String deviceId) throws ErpBarcodeException {
		JSONArray jsonResults = getDeviceIdBelowFacListData(deviceId);
		Log.i(TAG, "장치바코드 운영중인 하위설비 목록 결과 jsonResults.toString()==>"+jsonResults.toString());
		
		List<String> facCds = new ArrayList<String>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	String facCd = jsonobj.getString("EQUNR");
            	facCds.add(facCd);
    		} catch (JSONException e) {
    			Log.d(TAG, "getDeviceIdBelowFacListToBarcodeListInfos  장치바코드의 운용중인 하위설비 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "장치바코드의 운용중인 하위설비 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return facCds;
	}
}
