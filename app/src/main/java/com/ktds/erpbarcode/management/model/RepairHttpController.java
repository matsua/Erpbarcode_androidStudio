package com.ktds.erpbarcode.management.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class RepairHttpController {
	private static final String TAG = "RepairHttpController";
	
	private HttpAddressConfig mHttpAddress;
	private String mProject;
	
	public RepairHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}
	
	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}
	

	/**
	 * 고장정보 조회
	 * 
	 * @param barcode    설비바코드
	 */
	public JSONArray getOutOfServiceListData(String barcode) throws ErpBarcodeException {
		// 고장정보조회
		choicePath(HttpAddressConfig.PATH_GET_GETOUTOFSERVICELIST);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("I_TYPE", "D");
			if (GlobalData.getInstance().getJobGubun().equals("고장등록취소")) {
				jsonParam.put("I_STAT", "1");
			}
			else if (GlobalData.getInstance().getJobGubun().equals("수리의뢰취소")) {
				jsonParam.put("I_STAT", "2");
			}

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
		JSONArray jsonSubParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("BARCODE", barcode);

			jsonSubParamList = new JSONArray();
			jsonSubParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "서브 파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		
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
			throw new ErpBarcodeException(-1, "유효하지 않은 고장정보정보입니다. ");
		}
		Log.i(TAG, "고장정보 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<BarcodeListInfo> getOutOfServiceListToBarcodeListInfos(String barcode) throws ErpBarcodeException {
		JSONArray jsonResults = getOutOfServiceListData(barcode);
		
		List<BarcodeListInfo> barcodeListInfos = new ArrayList<BarcodeListInfo>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	BarcodeListInfo barcodeInfo = BarcodeInfoConvert.jsonToRapairBarcodeListInfo(jsonobj);
                // 설비 상태 별 Validation
               	SuportLogic.chkZPSTATU(barcodeInfo.getFacStatus(), barcodeInfo.getFacStatusName(), barcodeInfo.getProductCode());
            	
            	barcodeListInfos.add(barcodeInfo);
    		} catch (ErpBarcodeException e) {
    			throw e;
    		} catch (JSONException e) {
    			Log.d(TAG, "getOutOfServiceListToRapairBarcodeInfos  JSON 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "고장등록취소 조회 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return barcodeListInfos;
	}
	
	/**
	 * 수리완료 정보조회
	 * 
	 * @param locCd             위치바코드
	 * @param injuryBarcode     수리바코드
	 * @param newBarcode        새로운바코드
	 * @param RREASON           수리사유
	 */
	public JSONArray getRepairReceiptListData(String locCd, String injuryBarcode, String newBarcode, String RREASON) throws ErpBarcodeException {
		// 수리완료 정보조회
		choicePath(HttpAddressConfig.PATH_GET_GETREPAIRRECEIPTLIST);

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
            jsonParam.put("ZEQUIPLP", locCd);
            jsonParam.put("EXBARCODE", injuryBarcode);
            jsonParam.put("BARCODE", newBarcode);
            jsonParam.put("RREASON", RREASON);			

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
			throw new ErpBarcodeException(-1, "유효하지 않은 고장정보정보입니다. ");
		}
		Log.i(TAG, "고장정보 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}
	
	public List<BarcodeListInfo> getRepairReceiptListToBarcodeListInfos(String locCd, String injuryBarcode, String newBarcode, String RREASON) throws ErpBarcodeException {
		JSONArray jsonResults = getRepairReceiptListData(locCd, injuryBarcode, newBarcode, RREASON);
		
		List<BarcodeListInfo> barcodeListInfos = new ArrayList<BarcodeListInfo>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	BarcodeListInfo barcodeInfo = BarcodeInfoConvert.jsonToRapairReceiptBarcodeListInfo(jsonobj);
            	barcodeListInfos.add(barcodeInfo);
    		} catch (JSONException e) {
    			Log.d(TAG, "getRepairDoneListToRapairBarcodeInfos  JSON 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "수리완료 정보 조회 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return barcodeListInfos;
	}
	
	public List<RemodelBarcodeInfo> getRemodelBarcodeList(String locCd) throws ErpBarcodeException {
		JSONArray jsonResults = getRemodelBarcodeListData(locCd);
		
		List<RemodelBarcodeInfo> remodelBarcodeInfos = new ArrayList<RemodelBarcodeInfo>();
        for (int i=0;i<jsonResults.length();i++) {
    		try {
            	JSONObject jsonobj = jsonResults.getJSONObject(i);
            	
            	RemodelBarcodeInfo remodelBarcodeInfo = RemodelBarcodeConvert.jsonToRemodelBarcodeInfo(jsonobj);
            	remodelBarcodeInfos.add(remodelBarcodeInfo);
    		} catch (JSONException e) {
    			Log.d(TAG, "getRemodelBarcodeList  JSON 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    			throw new ErpBarcodeException(-1, "개조개량 대상 자재 조회 자료 변환중 오류가 발생했습니다." );
    		}
        }
        
        return remodelBarcodeInfos;
	}

	/**
	 * 개조개량의뢰/개조개량완료 대상 자재코드 리스트 가져오기
	 * 
	 * @param locCd      위치바코드
	 */
	private JSONArray getRemodelBarcodeListData(String locCd) throws ErpBarcodeException {
		if (GlobalData.getInstance().getJobGubun().equals("개조개량의뢰")) {
			choicePath(HttpAddressConfig.PATH_GET_RMDLIMPR_RQFORM_NEW);
		}
		else if (GlobalData.getInstance().getJobGubun().equals("개조개량완료")) {
			choicePath(HttpAddressConfig.PATH_GET_RMDLIMPR_CPFORM_NEW);
		}

		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			if (GlobalData.getInstance().getJobGubun().equals("개조개량의뢰")) {
				String reqDate = SystemInfo.getNowDate();
				jsonParam.put("REQ_DATE", reqDate);
				jsonParam.put("I_LOCCODE", locCd);
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
			throw new ErpBarcodeException(-1, "유효하지 않은 개조개량 정보입니다. ");
		}
		Log.i(TAG, "개조개량 결과건수 ==>"+jsonresults.length());
		
		return jsonresults;
	}

}
