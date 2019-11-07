package com.ktds.erpbarcode.ism.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class IsmHttpController {
	private static final String TAG = "IsmHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public IsmHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	
	/**
	 * 인스토어마킹 정보 조회.
	 * 
	 * @param orgCode        운영조직코드
	 * @param locCd          위치바코드
	 * @param deviceId       장치ID
	 * @param facCd          설비바코드
	 * @param UFacCd         상위바코드
	 * @param serge          
	 * @param productCode    자재코드
	 */
	public JSONArray getInstoreMarking(String orgCode, String locCd, String deviceId, String facCd, 
			String UFacCd, String serge, String productCode) throws ErpBarcodeException {

		// 인스토어마킹 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_INSTOREMARKING);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_ZKOSTL", orgCode);         // 운용조직
			jsonParam.put("I_ZEQUIPLP", locCd);         // 위치바코드
			jsonParam.put("I_ZEQUIPGC", deviceId);      // 장치바코드
			jsonParam.put("I_FEQUNR", facCd);           // 설비바코드
			jsonParam.put("I_HEQUNR", UFacCd);          // 상위바코드
			jsonParam.put("I_SERGE", serge);            // 훼손바코드
			jsonParam.put("I_SUBMT", productCode);      // 자재코드

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
			throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다. ");
		}
		Log.i(TAG, "인스토어마킹 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<IsmBarcodeInfo> getInstoreMarkingToIsmBarcodeInfos(String orgCode, String locCd, String deviceId, String facCd, 
			String UFacCd, String serge, String productCode) throws ErpBarcodeException {
		
		JSONArray jsonResults = getInstoreMarking(orgCode, locCd, deviceId, facCd, UFacCd, serge, productCode);
		
		List<IsmBarcodeInfo> ismBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<jsonResults.length();i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				IsmBarcodeInfo ismBarcodeInfo = jsonInstoreMarkingToIsmBarcodeInfo(jsonobj);
				ismBarcodeInfos.add(ismBarcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return ismBarcodeInfos;
	}
	
	/**
	 * 인스토어마킹관리 정보 조회.
	 * 
	 */
	public JSONArray getInstoreMarkingManagement(String orgCode, String sttCode, String rqrCode,
			String newCode, String ijrcode, String bgiDate, String endDate) throws ErpBarcodeException {

		// 인스토어마킹 정보 조회.
		choicePath(HttpAddressConfig.PATH_POST_ISM_PRT_INS_SEARCH);
		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put("operationDeptCode", orgCode);         		// 운용조직
			jsonParam.put("transactionStatusCode", sttCode);         	// 진행상태
			jsonParam.put("newBarcode", newCode);      					// 신규바코드
			jsonParam.put("injuryBarcode", ijrcode);           			// 훼손바코드
			jsonParam.put("itemCode", "");          					// 자재코드
			jsonParam.put("publicationWhyCode", rqrCode);            	// 요청사유
			jsonParam.put("beginDate", bgiDate);      					// 처리일
			jsonParam.put("endDate", endDate);      					// 처리일
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		InputParameter input = new InputParameter();
		try {
			input.setNobodyParam(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다. ");
		}
		Log.i(TAG, "인스토어마킹 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<IsmBarcodeInfo> getInstoreMarkingToIsmManagementBarcodeInfos(String orgCode, String sttCode, String rqrCode, 
			String newCode, String ijrcode, String bgiDate, String endDate) throws ErpBarcodeException {
		
		JSONArray jsonResults = getInstoreMarkingManagement(orgCode, sttCode, rqrCode, newCode, ijrcode, bgiDate, endDate);
		
		List<IsmBarcodeInfo> ismBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<jsonResults.length();i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				IsmBarcodeInfo ismBarcodeInfo = jsonInstoreMarkingManagementToIsmBarcodeInfo(jsonobj);
				ismBarcodeInfos.add(ismBarcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return ismBarcodeInfos;
	}
	

	/**
	 * 인스토어마킹에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonInstoreMarkingToIsmBarcodeInfo(JSONObject jsonobj) throws JSONException {
		
		IsmBarcodeInfo ismBarcodeInfo = new IsmBarcodeInfo();
		
		String isExist = jsonobj.getString("isExist").trim();

		ismBarcodeInfo.setFacCd(jsonobj.getString("FEQUNR").trim());
		ismBarcodeInfo.setFacStatus(jsonobj.getString("ZPSTATU").trim());
		ismBarcodeInfo.setFacStatusName(SuportLogic.getFACStatusName(jsonobj.getString("ZPSTATU").trim()));
		ismBarcodeInfo.setProductCode(jsonobj.getString("SUBMT").trim());
		ismBarcodeInfo.setProductName(jsonobj.getString("MAKTX").trim());
		
		// 품목구분
		ismBarcodeInfo.setDevType(jsonobj.getString("ZPGUBUN").trim());
		String devTypeName = SuportLogic.getDevTypeName(jsonobj.getString("ZPGUBUN").trim()); 
		ismBarcodeInfo.setDevTypeName(devTypeName);
		
		// 부품종류
		ismBarcodeInfo.setPartTypeCode(jsonobj.getString("ZPPART").trim());
		String partType = SuportLogic.getPartType(jsonobj.getString("ZPPART").trim(), jsonobj.getString("ZPGUBUN").trim());
		ismBarcodeInfo.setPartType(partType);
		String partTypeName = SuportLogic.getNodeStringType(partType);
		ismBarcodeInfo.setPartTypeName(partTypeName);

		ismBarcodeInfo.setLocCd(jsonobj.getString("ZEQUIPLP").trim());
		ismBarcodeInfo.setLocName(jsonobj.getString("PLOCNAME").trim());
		String assetClass = jsonobj.getString("ZEQART1").trim() + "/" +
				jsonobj.getString("ZEQART2").trim() + "/" +
				jsonobj.getString("ZEQART3").trim() + "/" +
				jsonobj.getString("ZEQART4").trim();
		ismBarcodeInfo.setAssetClass(assetClass);
		String assetClassName = jsonobj.getString("ZATEXT01").trim() + "/" +
				jsonobj.getString("ZATEXT02").trim() + "/" +
				jsonobj.getString("ZATEXT03").trim() + "/" +
				jsonobj.getString("ZATEXT04").trim();
		ismBarcodeInfo.setAssetClassName(assetClassName);
		String manufacturerName = jsonobj.getString("NAME1").trim() + "/" +	jsonobj.getString("HERST").trim();
		ismBarcodeInfo.setManufacturerName(manufacturerName);
		ismBarcodeInfo.setManufacturerSN(jsonobj.getString("SERGE").trim());
		ismBarcodeInfo.setDeviceId(jsonobj.getString("ZEQUIPGC").trim());
		ismBarcodeInfo.setUseStopYn("");
		ismBarcodeInfo.setReplaceProductCode("");
		ismBarcodeInfo.setExistingProductCode(isExist);
		ismBarcodeInfo.setCostCenter(jsonobj.getString("ZKOSTL").trim());
		
		return ismBarcodeInfo;
	}
	
	/**
	 * 인스토어마킹관리에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonInstoreMarkingManagementToIsmBarcodeInfo(JSONObject jsonobj) throws JSONException {
		
		IsmBarcodeInfo ismBarcodeInfo = new IsmBarcodeInfo();
		
		ismBarcodeInfo.setNewBarcode(jsonobj.getString("newBarcode").trim());							//신규바코드
		ismBarcodeInfo.setInjuryBarcode(jsonobj.getString("injuryBarcode").trim());						//훼손바코드
		ismBarcodeInfo.setProductCode(jsonobj.getString("itemCode").trim());							//자재코드 
		ismBarcodeInfo.setProductName(jsonobj.getString("itemName").trim());							//자재명 
		ismBarcodeInfo.setDevType(jsonobj.getString("itemCategoryName").trim());						//품목구분
		ismBarcodeInfo.setPartType(jsonobj.getString("partKindName").trim());							//부품종류 
		ismBarcodeInfo.setTransactionDate(jsonobj.getString("transactionDate").trim());					//처리일 
		ismBarcodeInfo.setTransactionTime(jsonobj.getString("transactionTime").trim());					//처리시간 
		ismBarcodeInfo.setTransactionStatusName(jsonobj.getString("transactionStatusName").trim());		//진행상태
		ismBarcodeInfo.setTransactionStatusCode(jsonobj.getString("transactionStatusCode").trim());		//진행상태코드 
		ismBarcodeInfo.setDeviceId(jsonobj.getString("deviceId").trim());								//장치바코드
		ismBarcodeInfo.setLocCd(jsonobj.getString("locationCode").trim());								//위치코드
		ismBarcodeInfo.setLocName(jsonobj.getString("locationName").trim());							//위치명 
		ismBarcodeInfo.setItemClassificationName(jsonobj.getString("itemClassificationName").trim());	//자재분류(대/중/소/세)
		ismBarcodeInfo.setAssetClassName(jsonobj.getString("assetClassificationName").trim());			//자산분류(대/중/소/세)
		ismBarcodeInfo.setManufacturerName(jsonobj.getString("makerName").trim());						//제조사명 
		ismBarcodeInfo.setManufacturerSN(jsonobj.getString("makerSerial").trim());						//제조사 S/N
		ismBarcodeInfo.setTransactionUserName(jsonobj.getString("transactionUserName").trim());			//처리자
		ismBarcodeInfo.setTransactionUserId(jsonobj.getString("transactionUserId").trim());				//처리자id
		ismBarcodeInfo.setPublicationWhyName(jsonobj.getString("publicationWhyName").trim());			//요청사유
		ismBarcodeInfo.setOrgName(jsonobj.getString("orgName").trim());									//운용조직 
		ismBarcodeInfo.setGenerationRequestSeq(jsonobj.getString("generationRequestSeq").toString());	//채번요청일련번호
		ismBarcodeInfo.setGenerationRequestDetailSeq(jsonobj.getString("generationRequestDetailSeq").toString());	//채번요청상세일련번호
		ismBarcodeInfo.setOldBarcodeYN(jsonobj.getString("oldBarcodeYN").toString());					//구바코드여부
		ismBarcodeInfo.setPublicationWhyCode(jsonobj.getString("publicationWhyCode").toString());		//발행사유코드
		return ismBarcodeInfo;
	}
	
	/**
	 * 수리완료 인스토어마킹 정보 조회.
	 * 
	 * @param locCd        위치바코드
	 * @param barcode      설비바코드
	 */
	public JSONArray getRepairReceipt_IM(String locCd, String barcode) throws ErpBarcodeException {

		// 수리완료 인스토어마킹 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_GETREPAIRRECEIPT_IM);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("BARCODE", barcode);  								// 스캔한 설비바코드
			jsonParam.put("ZEQUIPLP", locCd);             	// 스캔한 위치코드

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
			throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다. ");
		}
		Log.i(TAG, "인스토어마킹 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}

	/**
	 * 인스토어마킹 완료 정보 조회.
	 * 
	 * @param facCd         설비바코드
	 */
	public JSONArray getInstoreMarkingComplete(String facCd) throws ErpBarcodeException {

		// 인스토어마킹 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_IM_COMPLETESCAN);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("NEW_BARCODE", facCd);   // 새로운 설비바코드
			jsonParam.put("LOC_CD", "");           // 위치코드
			jsonParam.put("DEV_ID", "");           // 장치바코드

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
			throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다. ");
		}
		Log.i(TAG, "인스토어마킹 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<IsmBarcodeInfo> getInstoreMarkingCompleteToIsmBarcodeInfos(String facCd) throws ErpBarcodeException {
		
		JSONArray ismResults = getInstoreMarkingComplete(facCd);
		
		List<IsmBarcodeInfo> ismBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<ismResults.length();i++) {
			try {
				JSONObject jsonObj = ismResults.getJSONObject(i);
				
				IsmBarcodeInfo ismBarcodeInfo = jsonInstoreMarkingCompleteToIsmBarcodeInfo(jsonObj);
				
				ismBarcodeInfos.add(ismBarcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return ismBarcodeInfos;
	}
	
	/**
	 * 인스토어마킹완료에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	public IsmBarcodeInfo jsonInstoreMarkingCompleteToIsmBarcodeInfo(JSONObject jsonObj) throws JSONException {
		IsmBarcodeInfo ismBarcodeInfo = new IsmBarcodeInfo();

		// 품목구분
		ismBarcodeInfo.setChecked(true);
		ismBarcodeInfo.setDevType(jsonObj.getString("itemCategoryCode").trim());
		String devTypeName = SuportLogic.getDevTypeName(jsonObj.getString("itemCategoryCode").trim()); 
		ismBarcodeInfo.setDevTypeName(devTypeName);
		
		// 부품종류
		ismBarcodeInfo.setPartTypeCode(jsonObj.getString("partKindCode").trim());
		String partType = SuportLogic.getPartType(jsonObj.getString("partKindCode").trim(), jsonObj.getString("itemCategoryCode").trim());
		ismBarcodeInfo.setPartType(partType);
		String partTypeName = SuportLogic.getNodeStringType(partType);
		ismBarcodeInfo.setPartTypeName(partTypeName);
		
		ismBarcodeInfo.setNewBarcode(jsonObj.getString("newBarcode"));
		ismBarcodeInfo.setLocCd(jsonObj.getString("locationCode"));
		ismBarcodeInfo.setDeviceId(jsonObj.getString("deviceId"));
		ismBarcodeInfo.setItemCode(jsonObj.getString("itemCode"));
		ismBarcodeInfo.setItemName(jsonObj.getString("itemName"));
		ismBarcodeInfo.setItemCategoryCode(jsonObj.getString("itemCategoryCode"));	// zpgubun - 품목구분
		ismBarcodeInfo.setItemCategoryName(jsonObj.getString("itemCategoryName"));
		ismBarcodeInfo.setPartKindCode(jsonObj.getString("partKindCode"));			// partType
		ismBarcodeInfo.setPartKindName(jsonObj.getString("partKindName"));
		ismBarcodeInfo.setItemLargeClassificationCode(jsonObj.getString("itemLargeClassificationCode"));
		ismBarcodeInfo.setItemMiddleClassificationCode(jsonObj.getString("itemMiddleClassificationCode"));
		ismBarcodeInfo.setItemSmallClassificationCode(jsonObj.getString("itemSmallClassificationCode"));
		ismBarcodeInfo.setItemDetailClassificationCode(jsonObj.getString("itemDetailClassificationCode"));
		ismBarcodeInfo.setInjuryBarcode(jsonObj.getString("injuryBarcode"));
		ismBarcodeInfo.setPublicationWhyCode(jsonObj.getString("publicationWhyCode"));	// 대체발행사유코드
		ismBarcodeInfo.setPublicationWhyName(jsonObj.getString("publicationWhyName"));	// 대체발행사유명
		ismBarcodeInfo.setSupplierCode(jsonObj.getString("supplierCode"));
		ismBarcodeInfo.setDeptCode(jsonObj.getString("deptCode"));
		ismBarcodeInfo.setOperationDeptCode(jsonObj.getString("operationDeptCode"));
		ismBarcodeInfo.setMakerCode(jsonObj.getString("makerCode"));
		ismBarcodeInfo.setMakerSerial(jsonObj.getString("makerSerial"));
		//ismBarcodeInfo.setMakerNational(jsonObj.getString("makerNational"));	// SAP에서 넘어오지 않음
		ismBarcodeInfo.setMakerNational("");
		ismBarcodeInfo.setObtainDay(jsonObj.getString("obtainDay"));
		ismBarcodeInfo.setGenerationRequestSeq(jsonObj.getString("generationRequestSeq"));
		ismBarcodeInfo.setMakerItemYn(jsonObj.getString("makerItemYn"));
		//ismBarcodeInfo.setFacilityCategory(jsonObj.getString("facilityCategory"));		// 없는뎅.....
		ismBarcodeInfo.setFacilityCategory("");		// 없는뎅.....

		
		return ismBarcodeInfo;
	}
	
	
	/**
	 * 인스토어마킹관리 스피너 밸류 조회 - 진행상태 0 , 요청사유 1 , 라벨용지 2 , 요청취소사유 3
	 * 
	 * @param type       
	 */
	public JSONArray getIsmPrintSpinner(int type) throws ErpBarcodeException {
		if(type == 0){
			choicePath(HttpAddressConfig.PATH_POST_ISM_PRINT_STATUS);
		}else if(type == 1){
			choicePath(HttpAddressConfig.PATH_POST_ISM_PRINT_PBLS_WHY);
		}else if(type == 2){
			choicePath(HttpAddressConfig.PATH_POST_ISM_PRINT_LABEL_TP);
		}else{
			choicePath(HttpAddressConfig.PATH_POST_ISM_PRINT_INS_DEL);
		}
		
		JSONArray jsonParamList = new JSONArray();
		JSONObject jsonParam = new JSONObject();
		jsonParamList.put(jsonParam);
		JSONArray jsonSubParamList = new JSONArray();
		
		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);
		input.setSubParamList(jsonSubParamList);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "화면구성값 조회중 오류.");
		}

		return jsonresults;
	}
	
	/**
	 * 위치 바코드관리 스피너 밸류 조회 - 시도 0 , 시군구 1 , 읍면동 2
	 * 
	 * @param type       
	 * @throws JSONException 
	 */
	public JSONArray getBarcodeSpinner(int type, String key) throws ErpBarcodeException, JSONException {
		if(type == 0){
			choicePath(HttpAddressConfig.PATH_POST_BARCODE_SIDO);
		}else if(type == 1){
			choicePath(HttpAddressConfig.PATH_POST_BARCODE_SIGOON);
		}else if(type == 2){
			choicePath(HttpAddressConfig.PATH_POST_BARCODE_DONG);
		}
		
		InputParameter input = new InputParameter();
		
		if(type == 0){
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
			jsonParamList.put(jsonParam);
			JSONArray jsonSubParamList = new JSONArray();
			
			input.setParamList(jsonParamList);
			input.setSubParamList(jsonSubParamList);
		}else if(type == 1){
			JSONObject jsonParam = new JSONObject();
			try {
				jsonParam.put("broadId", key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			input.setParam(jsonParam);
		}else if(type == 2){
			JSONObject jsonParam1 = new JSONObject();
			JSONObject jsonParam2 = new JSONObject();
			JSONObject jsonParam3 = new JSONObject();
			JSONObject jsonParams = new JSONObject();
			try {
				jsonParam1.put("key", key);
				jsonParams.put("locName", jsonParam1);
				
				jsonParam2.put("key", "");
				jsonParams.put("broadId", jsonParam2);
				
				jsonParam3.put("key", "");
				jsonParams.put("middleId", jsonParam3);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			input.setNobodyParam(jsonParams);
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "화면구성값 조회중 오류.");
		}

		return jsonresults;
	}
	
	/**
	 * 위치 바코드관리 검색 
	 * 
	 * @param type       
	 * @throws ErpBarcodeException 
	 * @throws JSONException 
	 */
	
	public List<IsmBarcodeInfo> getLocBarcodeToIsmBarcodeInfos(String locationCode, String boardId, String middleBmassId, String geoName, String bunji, String ho) throws ErpBarcodeException, JSONException{
		
		JSONArray ismResults = getBarcodeLocRequest(locationCode, boardId, middleBmassId, geoName, bunji, ho);
		
		List<IsmBarcodeInfo> barcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<ismResults.length();i++) {
			try {
				JSONObject jsonObj = ismResults.getJSONObject(i);
				
				IsmBarcodeInfo barcodeInfo = jsonLocBarcodeManagementToIsmBarcodeInfo(jsonObj);
				
				barcodeInfos.add(barcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return barcodeInfos;
	}

	public JSONArray getBarcodeLocRequest(String locationCode, String boardId, String middleBmassId, String geoName, String bunji, String ho) throws ErpBarcodeException, JSONException {
		choicePath(HttpAddressConfig.PATH_POST_BARCODE_LOC_REQ);
		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();
		
		try {
			if(locationCode.length() > 0)jsonParam.put("locationCode", locationCode.substring(0, 11));
			if(boardId.length() > 0)jsonParam.put("boardId", boardId);
			if(middleBmassId.length() > 0)jsonParam.put("middleBmassId", middleBmassId);
			if(geoName.length() > 0)jsonParam.put("geoName", geoName);
			if(bunji.length() > 0)jsonParam.put("BUNJI", bunji);
			if(ho.length() > 0)jsonParam.put("HO", ho);
			
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		try {
			input.setNobodyParam(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다. ");
		}
		Log.i(TAG, "위치바코드 관리 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
		
	}
	
	/**
	 * 위치바코드 관리에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonLocBarcodeManagementToIsmBarcodeInfo(JSONObject jsonobj) throws JSONException {
		
		IsmBarcodeInfo barcodeInfo = new IsmBarcodeInfo();
		
		barcodeInfo.setSilYuhung(jsonobj.getString("locationTypeName").trim());			//유형 
		barcodeInfo.setLocCd(jsonobj.getString("locationCode").trim());					//위치바코드
		barcodeInfo.setLocLevel(jsonobj.getString("locationLevel").trim());				//레벨 
		barcodeInfo.setLocName(jsonobj.getString("locationName").trim());				//위치명 
		//barcodeInfo.setSilType(jsonobj.getString("roomTypeCode").trim());				//실유형
		//barcodeInfo.setSilName(jsonobj.getString("roomTypeName").trim());				//실명칭
		barcodeInfo.setSilType(jsonobj.getString("roomTypeName").trim());				//실유형
		barcodeInfo.setSilName(jsonobj.getString("subLocationName").trim());				//실명칭
		barcodeInfo.setSilStatus(jsonobj.getString("sttus").trim());					//상태 
		barcodeInfo.setSilGubun(jsonobj.getString("subLocationTypeName").trim());		//실층구분
		barcodeInfo.setSilAddress(jsonobj.getString("geoName").trim());					//주소
		barcodeInfo.setSilBulGb(jsonobj.getString("buildingDistionctYN").trim());		//건물군여부
		barcodeInfo.setSilBuType(jsonobj.getString("buildingTypeName").trim());			//건물유형
		barcodeInfo.setSilKTBulType(jsonobj.getString("ktBuildingTypeName").trim());	//KT건물유형
		barcodeInfo.setSilKuksa(jsonobj.getString("mnofiId").trim());					//관리국사
		barcodeInfo.setSilKuksaName(jsonobj.getString("mnofiName").trim());				//관리국사명 
		barcodeInfo.setComment(jsonobj.getString("description").trim());				//설명

		// sesang 20190910 대표위치 정보 추가
		if (jsonobj.has("repLocCd")) barcodeInfo.setRepLocCd(jsonobj.getString("repLocCd").trim());					//대표주소코드
		if (jsonobj.has("repLocNm")) barcodeInfo.setRepLocNm(jsonobj.getString("repLocNm").trim());					//대표주소명
		// end sesang
		return barcodeInfo;
	}
	
	/**
	 * 장치 바코드관리 검색 
	 * 
	 * @param type       
	 * @throws ErpBarcodeException 
	 * @throws JSONException 
	 */
	
	public List<IsmBarcodeInfo> getDeviceBarcodeToIsmBarcodeInfos(String deviceId, String deviceCd, String userId, String dateFrom, String dateTo) throws ErpBarcodeException, JSONException{
		
		JSONArray ismResults = getBarcodeDeviceRequest(deviceId, deviceCd, userId, dateFrom, dateTo);
		
		List<IsmBarcodeInfo> barcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<ismResults.length();i++) {
			try {
				JSONObject jsonObj = ismResults.getJSONObject(i);
				
				IsmBarcodeInfo barcodeInfo = jsonDeviceBarcodeManagementToIsmBarcodeInfo(jsonObj);
				
				barcodeInfos.add(barcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return barcodeInfos;
	}

	public JSONArray getBarcodeDeviceRequest(String deviceId, String deviceCd, String userId, String dateFrom, String dateTo) throws ErpBarcodeException, JSONException {
		choicePath(HttpAddressConfig.PATH_POST_BARCODE_DEVICE_REQ);
		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();
		
		try {
			if(deviceId.length() > 0)jsonParam.put("deviceId", deviceId);
			if(deviceCd.length() > 0)jsonParam.put("operationSystemCode", deviceCd);
			if(userId.length() > 0)jsonParam.put("registerUserId", userId);
			if(dateFrom.length() > 0)jsonParam.put("registerFromDate", dateFrom);
			if(dateTo.length() > 0)jsonParam.put("registerToDate", dateTo);
			
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		try {
			input.setNobodyParam(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 장치바코드 정보입니다. ");
		}
		Log.i(TAG, "장치바코드 관리 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
		
	}
	
	/**
	 * 위치바코드 관리에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonDeviceBarcodeManagementToIsmBarcodeInfo(JSONObject jsonobj) throws JSONException {
		IsmBarcodeInfo barcodeInfo = new IsmBarcodeInfo();
		barcodeInfo.setDeviceId(jsonobj.getString("deviceId").trim());							//장치ID
		barcodeInfo.setItemName(jsonobj.getString("deviceName").trim());						//장치명 
		barcodeInfo.setProductCode(jsonobj.getString("projectNo").trim());						//프로젝트번호 
		barcodeInfo.setWbsCode(jsonobj.getString("wbsCode").trim());							//WBS
		barcodeInfo.setOperationDeptCode(jsonobj.getString("operationSystemTokenName").trim());	//운용시스템구분자
		barcodeInfo.setDevType(jsonobj.getString("operationSystemCode").trim());				//장비ID코드
		barcodeInfo.setDevTypeName(jsonobj.getString("operationSystemName").trim());			//장비ID명 
		barcodeInfo.setAssetClassName(jsonobj.getString("locationIdTokenName").trim());			//장비ID명칭 
		barcodeInfo.setFacStatus(jsonobj.getString("deviceStatusName").trim());					//장치상태명 
		barcodeInfo.setItemCode(jsonobj.getString("itemCode").trim());							//자재코드 
		barcodeInfo.setAssetClass(jsonobj.getString("argumentDecisionName").trim());			//인수확정여부명 
		barcodeInfo.setItemDetailClassificationCode(jsonobj.getString("active").trim());		//활성여부 
		barcodeInfo.setTransactionDate(jsonobj.getString("registerDate").trim());				//생성일시 
		barcodeInfo.setComment("");																//비고 
		barcodeInfo.setChecked(false);															
		barcodeInfo.setConditions(jsonobj.getString("conditions").trim());						//표준서비스코드상태
		return barcodeInfo;
	}
	
	/**
	 * 소스마킹 검색 
	 * 
	 * @param type       
	 * @throws ErpBarcodeException 
	 * @throws JSONException 
	 */
	
	public List<IsmBarcodeInfo> getSmBarcodeToIsmBarcodeInfos(String poNum, String typeNum, String printYn, String barcodeFrom, String barcodeTo) throws ErpBarcodeException, JSONException{
		
		JSONArray ismResults = getBarcodeSmRequest(poNum, typeNum, printYn, barcodeFrom, barcodeTo);
		
		List<IsmBarcodeInfo> barcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<ismResults.length();i++) {
			try {
				JSONObject jsonObj = ismResults.getJSONObject(i);
				
				IsmBarcodeInfo barcodeInfo = jsonSmBarcodeManagementToIsmBarcodeInfo(jsonObj);
				
				barcodeInfos.add(barcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		
		return barcodeInfos;
	}

	public JSONArray getBarcodeSmRequest(String poNum, String typeNum, String printYn, String barcodeFrom, String barcodeTo) throws ErpBarcodeException, JSONException {
		choicePath(HttpAddressConfig.PATH_POST_BARCODE_SM_REQ);
		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();
		
		try {
			if(poNum.length() > 0)jsonParam.put("purchaseNumber", poNum);
			if(typeNum.length() > 0)jsonParam.put("purchaseLineNumber", typeNum);
			if(printYn.length() > 0)jsonParam.put("selectPrtYn", printYn);
			if(barcodeFrom.length() > 0)jsonParam.put("newBarcodeFrom", barcodeFrom);
			if(barcodeTo.length() > 0)jsonParam.put("newBarcodeTo", barcodeTo);
			
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		input.setParam(jsonParam);
		
		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();
		
		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}
		
		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 소스마킹 정보입니다. ");
		}
		Log.i(TAG, "소스마킹 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
		
	}
	
	/**
	 * 위치바코드 관리에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonSmBarcodeManagementToIsmBarcodeInfo(JSONObject jsonobj) throws JSONException {
		IsmBarcodeInfo barcodeInfo = new IsmBarcodeInfo();
		barcodeInfo.setFacCd(jsonobj.getString("barcode").trim());								//설비바코드
		barcodeInfo.setOldBarcodeYN(jsonobj.getString("oldBarcodeYN").trim());					//구바코드여부
		barcodeInfo.setItemCode(jsonobj.getString("itemCode").trim());							//자재코드 
		barcodeInfo.setFacilityCategory(jsonobj.getString("purchaseLineNumber").trim());		//항목번호 
		barcodeInfo.setItemName(jsonobj.getString("itemName").trim());							//자재명 
		barcodeInfo.setPartKindName(jsonobj.getString("partKindName").trim());					//부품종류 
		barcodeInfo.setProductCode(jsonobj.getString("productTypeName").trim());				//품목구분
		barcodeInfo.setSilStatus(jsonobj.getString("etc").trim());								//출력상태 
		barcodeInfo.setComment("");																//비고 
		barcodeInfo.setChecked(false);
		return barcodeInfo;
	}


	/**
	 * 기지국/중계기 위치 바코드관리 검색
	 *
	 * @param type
	 * @throws ErpBarcodeException
	 * @throws JSONException
	 * sesang 20190919
	 */

	public List<IsmBarcodeInfo> getNmsToLocBarcodeInfos(String deviceId, String nmsDeviceId) throws ErpBarcodeException, JSONException{

		JSONArray ismResults = getNmsLocBarcodeRequest(deviceId, nmsDeviceId);

		List<IsmBarcodeInfo> barcodeInfos = new ArrayList<IsmBarcodeInfo>();
		for (int i=0;i<ismResults.length();i++) {
			try {
				JSONObject jsonObj = ismResults.getJSONObject(i);

				IsmBarcodeInfo barcodeInfo = jsonNmsToLocBarcodeInfo(jsonObj);

				barcodeInfos.add(barcodeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}

		return barcodeInfos;
	}

	public JSONArray getNmsLocBarcodeRequest(String deviceId, String nmsDeviceId) throws ErpBarcodeException, JSONException {
		choicePath(HttpAddressConfig.PATH_POST_NMS_LOC_REQ);
		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();

		try {
			if(deviceId.length() > 0) jsonParam.put("deviceId", deviceId);
			if(nmsDeviceId.length() > 0) jsonParam.put("nmsDeviceId", nmsDeviceId);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}

		try {
			input.setNobodyParam(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 위치바코드 정보입니다. ");
		}
		Log.i(TAG, "위치바코드 관리 결과건수 ==>"+jsonresults.length());

		return jsonresults;

	}

	/**
	 * 위치바코드 관리에서 JSONObject 형태의 데이터를 IsmBarcodeInfo Class로 변환한다.
	 *
	 * @param jsonobj
	 */
	private IsmBarcodeInfo jsonNmsToLocBarcodeInfo(JSONObject jsonobj) throws JSONException {

		IsmBarcodeInfo barcodeInfo = new IsmBarcodeInfo();

		barcodeInfo.setLocCd(jsonobj.getString("locationCode").trim());					//위치바코드
		barcodeInfo.setLocName(jsonobj.getString("locationName").trim());				//위치명
		barcodeInfo.setRepLocCd(jsonobj.getString("repLocCd").trim());					//대표주소코드
		barcodeInfo.setRepLocNm(jsonobj.getString("repLocNm").trim());					//대표주소명 

		barcodeInfo.setDeviceId(jsonobj.getString("deviceId").trim());					//장치아이디 
		barcodeInfo.setNmsDeviceId(jsonobj.getString("nmsDeviceId").trim());			//장비
		barcodeInfo.setDeviceName(jsonobj.getString("deviceName").trim());				//장치(장비)
		
		barcodeInfo.setSilName(jsonobj.getString("subLocationName").trim());			//실명칭 
		
//		String geoName = jsonobj.has("geoName") ? jsonobj.getString("geoName").trim() : "";
//		if (geoName.length() == 0) geoName = jsonobj.getString("repLocNm").trim();
//		barcodeInfo.setSilAddress(geoName);	
		
		return barcodeInfo;
	}

}
