package com.ktds.erpbarcode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ktds.erpbarcode.barcode.model.AssetClassInfo;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.base.BaseBarcodeInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.env.model.NoticeInfo;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;

public class BaseHttpController {
	private static final String TAG = "BaseHttpController";

	private HttpAddressConfig mHttpAddress;
	private String mProject;

	public BaseHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}

	public void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			Log.i(TAG, "URL주소가 유효하지 않습니다. ");
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}

	/**
	 * GbicDataList 조회.
	 * 
	 * @param itemCd
	 *            ""
	 */
	public List<String> getGbicList(String code) throws ErpBarcodeException {
		JSONArray jsonResults = getGbicDataList(code);

		Log.i(TAG, "GbicDataList   jsonResults==>" + jsonResults.toString());

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < jsonResults.length(); i++) {
			try {
				list.add(jsonResults.getJSONObject(i).getString("itemCode"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * GbicDataList 조회.
	 * 
	 * @param itemCd
	 *            ""
	 */
	public JSONArray getGbicDataList(String itemCd) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_GET_GBIC_LIST);

		InputParameter input = new InputParameter();
		input.setNomalParam("itemCd", "");

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		System.out.println("jsonresults >> " + jsonresults);

		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "GbicDataList 가 없습니다. ");
		}
		Log.i(TAG, "GbicDataList 결과건수 ==>" + jsonresults.length());

		return jsonresults;
	}

	/**
	 * 최상위 운용조직 목록 조회.
	 * 
	 * @param orgCode
	 *            운용조직코드
	 */
	public JSONArray getOrgTreeSearch(String orgCode)
			throws ErpBarcodeException {
		// 최상위 운용조직 조회.
		choicePath(HttpAddressConfig.PATH_GET_ORGTREE_SEARCH);

		InputParameter input = new InputParameter();
		input.setNomalParam("parentOrgCode", orgCode);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "최상위 운용조직 조회 결과 정보가 없습니다. ");
		}
		Log.i(TAG, "최상위 운용조직내역 결과건수 ==>" + jsonresults.length());

		return jsonresults;

	}

	public List<OrgCodeInfo> getOrgTreeSearchToOrgCodeInfos(String code)
			throws ErpBarcodeException {

		JSONArray jsonResults = getOrgTreeSearch(code);

		Log.i(TAG, "최상위 운용조직내역   jsonResults==>" + jsonResults.toString());

		List<OrgCodeInfo> orgInfos = new ArrayList<OrgCodeInfo>();
		for (int i = 0; i < jsonResults.length(); i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				OrgCodeInfo orgInfo = jsonStringToOrgCodeInfo(jsonobj);
				orgInfos.add(orgInfo);
			} catch (JSONException e) {
				Log.d(TAG,
						"하위부서 조회중 오류가 발생했습니다.   JSONException==>"
								+ e.getMessage());
				throw new ErpBarcodeException(-1, "하위부서 조회중 오류가 발생했습니다.");
			}
		}

		return orgInfos;
	}

	/**
	 * 부서명으로 운용조직 목록 조회.
	 * 
	 * @param orgName
	 *            운용조직명
	 */
	public JSONArray getOrgNameSearch(String orgName)
			throws ErpBarcodeException {
		// 운용조직 조회.
		choicePath(HttpAddressConfig.PATH_GET_ORGNAME_SEARCH);

		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put("key", orgName);
		} catch (JSONException e) {
			Log.d(TAG, "파라미터대입중 오류가 발생했습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "파라미터 대입중 오류가 발생했습니다. ");
		}

		InputParameter input = new InputParameter();
		input.setNomalParam("orgName", jsonParam);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "운용조직이름으로 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "운용조직내역 결과건수 ==>" + jsonresults.length());

		return jsonresults;
	}

	public List<OrgCodeInfo> getOrgNameSearchToOrgCodeInfos(String name)
			throws ErpBarcodeException {

		JSONArray jsonResults = getOrgNameSearch(name);

		List<OrgCodeInfo> orgInfos = new ArrayList<OrgCodeInfo>();
		for (int i = 0; i < jsonResults.length(); i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				OrgCodeInfo orgInfo = jsonStringToOrgNameInfo(jsonobj);
				if (orgInfo != null) {
					orgInfos.add(orgInfo);
				}
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "하위부서 조회중 오류가 발생했습니다.");
			}
		}

		return orgInfos;
	}

	/**
	 * 페이지별 자재마스터 조회. (환경설정에서 자재마스터 정보 로컬DB로 내려받을때 사용)
	 * 
	 * @param orgCode
	 *            운용조직코드
	 * @param workDate
	 *            작업일자
	 * @param pageNo
	 *            페이지번호
	 * @param pageCount
	 *            페이지건수
	 */
	public JSONArray getSurveyMaster(String orgCode, String workDate,
			int pageNo, int pageCount) throws ErpBarcodeException {
		// 자재마스터 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_SURVEYMASTER);

		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("ORGCODE", orgCode);
			jsonParam.put("EAI_CDATE", workDate);
			jsonParam.put("currentPageNo", String.valueOf(pageNo));
			jsonParam.put("currentPageCount", String.valueOf(pageCount));
			jsonParam.put("pageUseYN", "Y");

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
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "자재마스터 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "자재마스터정보 결과건수 ==>" + jsonresults.length());

		return jsonresults;
	}

	/**
	 * 자산분류정보 조회.
	 * 
	 * @param productCode
	 *            자재코드
	 */
	public JSONArray getAssetData(String productCode)
			throws ErpBarcodeException {
		// 자산분류 조회.
		choicePath(HttpAddressConfig.PATH_GET_ASSET);

		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("itemCode", productCode);

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
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "자산분류 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "자산분류 결과건수 ==>" + jsonresults.length());

		return jsonresults;

	}

	public AssetClassInfo getAssetClassInfo(String productCode)
			throws ErpBarcodeException {

		JSONArray jsonresults = getAssetData(productCode);

		AssetClassInfo assetClassInfo = null;
		try {
			JSONObject jsonobj = jsonresults.getJSONObject(0);

			assetClassInfo = BarcodeInfoConvert.jsonToAssetClassInfo(
					productCode, jsonobj);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "자재마스터의 자산분류정보 변환중 오류가 발생했습니다.");
		}
		return assetClassInfo;
	}

	/**
	 * 운용조직정보를 JSON 데이터를 OrgCodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	public OrgCodeInfo jsonStringToOrgCodeInfo(JSONObject jsonobj)
			throws JSONException {

		// "orgCode": "000030@C000030@\/주식회사 케이티\/인재경영실\/000030",

		String orgCodeInfo = jsonobj.getString("orgCode").trim();
		String[] arr_values = orgCodeInfo.split("@");
		String orgCode = arr_values[0];

		String orgName = jsonobj.getString("orgName").trim();
		String costCenter = jsonobj.getString("costCenter").trim();
		String parentOrgCode = jsonobj.getString("parentOrgCode").trim();
		int level = jsonobj.getInt("orgLevel");

		OrgCodeInfo orgInfo = new OrgCodeInfo();
		orgInfo.setOrgCode(orgCode);
		orgInfo.setOrgName(orgName);
		orgInfo.setParentOrgCode(parentOrgCode);
		orgInfo.setLevel(level);
		orgInfo.setCostCenter(costCenter);

		return orgInfo;
	}

	/**
	 * 운용조직 부서명 정보 JSON 데이터를 OrgNameInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	public OrgCodeInfo jsonStringToOrgNameInfo(JSONObject jsonobj)
			throws JSONException {

		// "orgCode": "261993@C261993",
		// "orgName": "\/주식회사 케이티\/인재경영실\/HR기획담당\/261993",

		String orgCodeInfo = jsonobj.getString("orgCode").trim();
		if (orgCodeInfo.isEmpty())
			return null;

		String[] arr_values = orgCodeInfo.split("@");
		String orgCode = arr_values[0];
		String costCenter = arr_values[1];

		String[] splOrgName = jsonobj.getString("orgName").split("/");
		String orgName = "";
		if (splOrgName.length >= 5)
			orgName = splOrgName[splOrgName.length - 4] + "/"
					+ splOrgName[splOrgName.length - 3] + "/"
					+ splOrgName[splOrgName.length - 2];

		else if (splOrgName.length == 4)
			orgName = splOrgName[splOrgName.length - 3] + "/"
					+ splOrgName[splOrgName.length - 2];

		else if (splOrgName.length == 3)
			orgName = splOrgName[splOrgName.length - 2];

		OrgCodeInfo orgInfo = new OrgCodeInfo();
		orgInfo.setSearched(true); // 하단 조회하지 못하게 한다.
		orgInfo.setOrgCode(orgCode);
		orgInfo.setOrgName(orgName);
		orgInfo.setParentOrgCode("");
		orgInfo.setLevel(0);
		orgInfo.setCostCenter(costCenter);

		return orgInfo;
	}

	/**
	 * 공지사항 목록 조회.
	 */
	public JSONArray getNoticesData() throws ErpBarcodeException {
		// 공지사항 조회.
		choicePath(HttpAddressConfig.PATH_GET_NOTICE);

		InputParameter input = new InputParameter();
		JSONArray jsonParamList = null;
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", SessionUserData.getInstance().getUserId());

			jsonParamList = new JSONArray();
			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		input.setParamList(jsonParamList);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "공지사항 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "공지사항 결과건수 ==>" + jsonresults.length());

		return jsonresults;
	}

	public List<NoticeInfo> getNoticeInfos() throws ErpBarcodeException {

		JSONArray jsonResults = getNoticesData();

		List<NoticeInfo> noticeInfos = new ArrayList<NoticeInfo>();
		for (int i = 0; i < jsonResults.length(); i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);

				NoticeInfo noticeInfo = jsonStringToNoticeInfo(jsonobj);
				noticeInfos.add(noticeInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "공지사항 자료 변환중 오류가 발생했습니다.");
			}
		}

		return noticeInfos;
	}

	public NoticeInfo jsonStringToNoticeInfo(JSONObject jsonobj)
			throws JSONException {

		int seq = jsonobj.getInt("boardSequence");
		String title = jsonobj.getString("title").trim();
		String message = jsonobj.getString("description").trim();

		message = message.replace("&lt;p&gt;", "\n");
		message = message.replace("&lt;/p&gt;", "\n");
		message = message.replace("&lt;div&gt;", "\n");
		message = message.replace("&lt;/div&gt;", "");
		message = message.replace("&#xA;", "");

		message = message.replace("&#39;", "'");
		message = message.replace("&#34;", "\"");
		message = message.replace("&#44;", ",");
		message = message.replace("&lt;br /&gt;", "\n");
		message = message.replace("\t", "");
		message = message.replace("&amp;quot;", "\"");
		message = message.replace("&amp;nbsp;", " ");
		message = message.replace("&amp;gt;", "");

		NoticeInfo noticeInfo = new NoticeInfo();
		noticeInfo.setSeq(seq);
		noticeInfo.setTitle(title);
		noticeInfo.setMessage(message);

		return noticeInfo;
	}

	/**
	 * 사용자조회 검색
	 * 
	 * @param type
	 * @throws ErpBarcodeException
	 * @throws JSONException
	 */

	public List<OrgCodeInfo> getUserInfo(String userId, String userNm)
			throws ErpBarcodeException {
		JSONArray userInfoResults = getUserInfoRequest(userId, userNm);
		List<OrgCodeInfo> userInfos = new ArrayList<OrgCodeInfo>();
		for (int i = 0; i < userInfoResults.length(); i++) {
			try {
				JSONObject jsonObj = userInfoResults.getJSONObject(i);
				OrgCodeInfo userInfo = jsonUserInfo(jsonObj);
				userInfos.add(userInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		return userInfos;
	}

	public JSONArray getUserInfoRequest(String userId, String userNm)
			throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_BARCODE_USER_INFO);
		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();

		try {
			jsonParam.put("userId", userId);
			jsonParam.put("userName", userNm);
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
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 사용자 정보입니다. ");
		}
		Log.i(TAG, "사용자조회 결과건수 ==>" + jsonresults.length());

		return jsonresults;

	}

	/**
	 * 사용자조회 에서 JSONObject 형태의 데이터를 OrgCodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private OrgCodeInfo jsonUserInfo(JSONObject jsonobj) throws JSONException {
		OrgCodeInfo userInfo = new OrgCodeInfo();
		userInfo.setUserId(jsonobj.getString("userId").trim()); // 사용자ID
		userInfo.setUserName(jsonobj.getString("userName").trim()); // 사용자명
		userInfo.setOrgName(jsonobj.getString("orgName").trim()); // 소속부서
		userInfo.setChecked(false);
		return userInfo;
	}

    //Add by sesang 20190819 정합성 작업대상 수량 체크
	public int getConsistency(String orgId) throws ErpBarcodeException {

		choicePath(HttpAddressConfig.PATH_GET_CONSISTENCY_COUNT);

		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put("I_JOB", "3");
			jsonParam.put("I_ZKOSTL", orgId);

		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}
		input.setParam(jsonParam);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();

		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "정합성 조회 결과가 없습니다. ");
		}
		Log.i(TAG, "정합성 작업대상 결과건수 ==>" + jsonresults.toString());

		if (jsonresults.length() > 0) {
			try {
				JSONObject json = jsonresults.getJSONObject(0);
				String str = json.getString("E_DEVICEID_COUNT");
				return Integer.parseInt(str);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, "정합성 자료 변환중 오류가 발생했습니다.");
			}

		}
		return 0;
	}
	// end sesang


	/**
	 * 베이스 검색
	 * 
	 * @param type
	 * @throws ErpBarcodeException
	 * @throws JSONException
	 */

	public List<BaseBarcodeInfo> getBaseManagement(String locCd, String facCd, String bsnNo, int type, String facType, String snCd) throws ErpBarcodeException {
		JSONArray baseBarcodeInfoResult = getBaseManagementRequest(locCd, facCd, bsnNo, type, facType, snCd);
		List<BaseBarcodeInfo> baseBarcodeInfos = new ArrayList<BaseBarcodeInfo>();
		for (int i = 0; i < baseBarcodeInfoResult.length(); i++) {
			try {
				JSONObject jsonObj = baseBarcodeInfoResult.getJSONObject(i);
				BaseBarcodeInfo baseInfo = jsonBaseManagement(jsonObj);
				baseBarcodeInfos.add(baseInfo);
			} catch (JSONException e) {
				throw new ErpBarcodeException(-1, e.getMessage());
			}
		}
		return baseBarcodeInfos;
	}

	public JSONArray getBaseManagementRequest(String locCd, String facCd, String bsnNo, int type, String facType, String snCd) throws ErpBarcodeException {
		if (type == 1 || type == 5) {
			choicePath(HttpAddressConfig.PATH_POST_BASE_MANAGEMENT);
		} else {
			choicePath(HttpAddressConfig.PATH_POST_BASE_SEARCH);
		}

		InputParameter input = new InputParameter();
		JSONObject jsonParam = new JSONObject();

		try {
			jsonParam.put("com", bsnNo);
			if (type == 1 && locCd.length() > 0) {
				jsonParam.put("sdId", locCd);
			}
			
			if(type == 5){
				jsonParam.put("sdId", locCd);
				jsonParam.put("serge", snCd);
			}

			jsonParam.put("bcId", facCd);
			jsonParam.put("facType", facType);
			jsonParam.put("userId", SessionUserData.getInstance().getUserId());

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
			throw new ErpBarcodeException(outputParameter.getStatus(),
					outputParameter.getOutMessage());
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "유효하지 않은 정보입니다. ");
		}
		Log.i(TAG, "조회된 결과건수 ==>" + jsonresults.length());

		return jsonresults;

	}

	/**
	 * 베이스검색 에서 JSONObject 형태의 데이터를 BaseBarcodeInfo Class로 변환한다.
	 * 
	 * @param jsonobj
	 */
	private BaseBarcodeInfo jsonBaseManagement(JSONObject jsonobj)
			throws JSONException {
		BaseBarcodeInfo baseInfo = new BaseBarcodeInfo();
//		baseInfo.setLocCode(jsonobj.getString("locCd").trim());
//		baseInfo.setFacCode(jsonobj.getString("facCd").trim());
		return baseInfo;
	}

}
