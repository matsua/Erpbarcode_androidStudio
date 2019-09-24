package com.ktds.erpbarcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.HttpCommend;
import com.ktds.erpbarcode.common.http.InputParameter;
import com.ktds.erpbarcode.common.http.OutputParameter;

public class SignHttpController {
	private static final String TAG = "SignHttpController";

	private HttpAddressConfig mHttpAddress;
	private String mProject;

	public SignHttpController() {
		mProject = HttpAddressConfig.PROJECT_ERPBARCODE;
	}

	private void choicePath(String path) throws ErpBarcodeException {
		mHttpAddress = new HttpAddressConfig(mProject, path);
		if (!mHttpAddress.isUrlAddress()) {
			Log.i(TAG, "URL주소가 유효하지 않습니다. ");
			throw new ErpBarcodeException(-1, "서버요청 주소가 유효하지 않습니다. ");
		}
	}

	/**
	 * 사용자 로그인
	 *
	 * @param userId        사용자ID
	 * @param password      비밀번호
	 * @param devId			로그인시도단말고유번호
	 * @param phoneNum		로그인시도단말전화번호
	 */
	public Boolean logIn(String userId, String password, String devId, String phoneNum) throws ErpBarcodeException {
		boolean duplication = false;
		// 로그인
		choicePath(HttpAddressConfig.PATH_GET_LOGIN);

		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", userId);
			jsonParam.put("userPassword", password);

			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}

		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			if (outputParameter.getOutMessage().indexOf("소속 정보가 없습니다")>=0) {
				throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
			}

			if(outputParameter.getOutMessage().contains("현재 접속중")){
				if(outputParameter.getOutMessage().contains("/")){
					String[] messages = outputParameter.getOutMessage().split("/");
					if(messages[1].equals(devId) || messages[1].equals("") || messages[1].equals("null")){
						return true;
					}else if(messages[2].equals(phoneNum) || messages[2].equals("") || messages[2].equals("null")){
						return true;
					}
				}
			}

			if(outputParameter.getOutMessage().contains("현재 접속중") && outputParameter.getOutMessage().contains("/")){
				String[] messages = outputParameter.getOutMessage().split("/");
				throw new ErpBarcodeException(outputParameter.getStatus(), messages[0]);
			}else{
				throw new ErpBarcodeException(outputParameter.getStatus(), outputParameter.getOutMessage());
			}
		}

		JSONObject jsonHeader = outputParameter.getHeader();
		if (jsonHeader == null) {
			throw new ErpBarcodeException(-1, "로그인 헤더정보 ~~바디결과정보가 없습니다. ");
		}

		//---------------------------------------------------------------------
		// 로그인 세션정보 싱글톤 변수에 대입.
		//---------------------------------------------------------------------

		SessionUserData sessionData = SessionUserData.getInstance();

		try {
			sessionData.setAuthenticated(true);
			sessionData.setUserId(jsonHeader.getString("userId"));
			sessionData.setUserPasswd("");
			sessionData.setUserName(jsonHeader.getString("userName"));
			sessionData.setTelNo(jsonHeader.getString("userCellPhoneNo"));
			sessionData.setUserCellPhoneNo(jsonHeader.getString("userCellPhoneNo"));
			sessionData.setOrgId(jsonHeader.getString("orgId"));
			sessionData.setOrgName(jsonHeader.getString("orgName"));
			sessionData.setOrgCode(jsonHeader.getString("orgCode"));
			sessionData.setOrgTypeCode(jsonHeader.getString("orgTypeCode"));
			sessionData.setSessionId(jsonHeader.getString("sessionId"));
			sessionData.setConfirmationYn(jsonHeader.getString("confirmationYn"));
			// Modify by sesang 20190729 비밀번호 변경 로직 삭제
			//sessionData.setPasswdUpdateYn(jsonHeader.getString("passwdUpdateYn"));
			sessionData.setPasswdUpdateYn("N");
			// end sesang

			// 추가 반
			try {
				sessionData.setCenterName(jsonHeader.getString("centerName"));
				sessionData.setCenterId(jsonHeader.getString("centerId"));
				sessionData.setSummaryOrgName(jsonHeader.getString("summaryOrg"));	// 전체 조직명
			} catch (Exception e) {

			}

			//---------------------------------------------------------------------
			// 조직이 케이티이면 로긴 불가 처리
			//---------------------------------------------------------------------
			if(sessionData.getOrgCode().equals("C000001")) {
				throw new ErpBarcodeException(outputParameter.getStatus(),
						"KT 조직이 '케이티'인 경우\n\r로그인 하실 수 없습니다.\n\rIDMS에서 조직 정보를 변경하신 후 \n\r사용하시기 바랍니\n\r문의처 : ISC(1588-3391)");
			}
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "로그인 세션데이터 대입중 오류가 발생했습니다. ");
		}

		return duplication;
	}

	/**
	 * 사용자 로그인 기기 저장
	 *
	 * @param userId        사용자ID
	 * @param phoneNum		사용자 기기 전화번호
	 * @param loginDevId    사용자기기 고유번호
	 */
	public void setDeviceId(String userId, String devId, String phoneNum) throws ErpBarcodeException {
		choicePath(HttpAddressConfig.PATH_POST_LOGIN_DEV_INFO);
		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("userId", userId);
			jsonParam.put("userPassword", "");
			jsonParam.put("loginDev", getAndroidOsVersion());
			jsonParam.put("loginDevId", devId);
			jsonParam.put("loginDevNumber", phoneNum);

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

		JSONObject jsonHeader = outputParameter.getHeader();
		if (jsonHeader == null) {
			throw new ErpBarcodeException(-1, "헤더정보 ~~바디결과정보가 없습니다. ");
		}
	}

	/**
	 * 사용자 로그아웃
	 *
	 * @param userId        사용자ID
	 */
	public void logOut(String userId) throws ErpBarcodeException {
		// 로그아웃
		choicePath(HttpAddressConfig.PATH_GET_LOGOUT);
		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put("userId", userId);
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

		JSONObject jsonHeader = outputParameter.getHeader();
		if (jsonHeader == null) {
			throw new ErpBarcodeException(-1, "로그아웃 헤더정보 ~~바디결과정보가 없습니다. ");
		}
	}

	/**
	 * 본인인증번호 요청.
	 */
	public JSONArray getLoginMakeCertification() throws ErpBarcodeException {
		// SMS 본인인증
		choicePath(HttpAddressConfig.PATH_GET_LOGIN_MAKE_CERTIFICATION);


		InputParameter input = new InputParameter();

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), "인증번호생성 중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요.");
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "인증번호생성 중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요.");
		}
		Log.i(TAG, "본인인증번호 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}

	/**
	 * 본인인증번호 전송
	 */
	public void sendLoginSendCertification() throws ErpBarcodeException {
		// SMS 본인인증
		choicePath(HttpAddressConfig.PATH_POST_LOGIN_SEND_CERTIFICATION);


		InputParameter input = new InputParameter();

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), "인증 정보 전송 중 오류가 발생하였습니다.\n\r잠시 후 다시 실행하세요.");
		}
	}

	/**
	 * 프로그램 버젼 정보 조회.
	 */
	public JSONArray getProgramVersion(String appVersion) throws ErpBarcodeException {
		// 프로그램 버젼 정보 조회.
		choicePath(HttpAddressConfig.PATH_GET_PROGRAM_VERSION);

		JSONArray jsonParamList = new JSONArray();
		try {
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("PGM_ID", "A");
			jsonParam.put("MODEL_ID", "KDC350");
			jsonParam.put("VERSION", appVersion);

			jsonParamList.put(jsonParam);
		} catch (JSONException e) {
			throw new ErpBarcodeException(-1, "파라미터리스트 대입중 오류가 발생했습니다. ");
		}

		InputParameter input = new InputParameter();
		input.setParamList(jsonParamList);

		HttpCommend httpCommend = new HttpCommend(mHttpAddress, input);
		OutputParameter outputParameter = httpCommend.httpSend();

		if (!outputParameter.isSuccess()) {
			throw new ErpBarcodeException(outputParameter.getStatus(), "프로그램 버젼정보 조회중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요.");
		}

		JSONArray jsonresults = outputParameter.getBodyResults();
		if (jsonresults == null) {
			throw new ErpBarcodeException(-1, "프로그램 버젼정보 조회중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요.");
		}
		Log.i(TAG, "프로그램 버젼정보 결과건수 ==>"+jsonresults.length());

		return jsonresults;
	}

	public String getAndroidOsVersion(){
		String osVer = String.valueOf(android.os.Build.VERSION.SDK_INT);

		switch (Integer.parseInt(osVer)) {
			case 23:
				osVer = "Android 6.0";
				break;
			case 22:
				osVer = "Android 5.1";
				break;
			case 21:
				osVer = "Android 5.0";
				break;
			case 20:
				osVer = "Android 4.4W";
				break;
			case 19:
				osVer = "Android 4.4";
				break;
			case 18:
				osVer = "Android 4.3";
				break;
			case 17:
				osVer = "Android 4.2";
				break;
			case 16:
				osVer = "Android 4.1";
				break;
			case 15:
				osVer = "Android 4.0.3";
				break;
			case 14:
				osVer = "Android 4.0";
				break;
			case 13:
				osVer = "Android 3.2";
				break;
			case 12:
				osVer = "Android 3.1";
				break;
			case 11:
				osVer = "Android 3.0";
				break;
			case 10:
				osVer = "Android 2.3.3";
				break;
			default:
				osVer = "ANDROID";
				break;
		}
		return osVer;
	}
}
