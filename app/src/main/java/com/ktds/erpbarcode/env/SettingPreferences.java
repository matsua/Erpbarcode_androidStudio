package com.ktds.erpbarcode.env;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;

public class SettingPreferences {

	private static final String TAG = "SettingPreferences";
	public static final String PREFS_SETTING = "prdf_setting";
	
	private SharedPreferences mSharedSetting;
	
	public SettingPreferences(Context context) {
		Log.d(TAG, "환경설정 XML   Start...");
		mSharedSetting = context.getSharedPreferences(PREFS_SETTING, Context.MODE_PRIVATE);
	}
	
	/**
	 * 블루투스 바코드스케너 설정.
	 */
	public void setBarcodeScanner(boolean yn, String deviceName, String deviceAddress) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("bluetooth_barcodescanner_connect_yn", yn);
		editor.putString("bluetooth_barcodescanner_devicename", deviceName);
		editor.putString("bluetooth_barcodescanner_deviceaddress", deviceAddress);
		editor.commit();
		
		// 바코드스케너 싱글톤Class 내용 변경.
		if (isBarcodeScannerConnect()) {
			ScannerDeviceData.getInstance().setConnected(true);
			ScannerDeviceData.getInstance().setDeviceName(deviceName);
			ScannerDeviceData.getInstance().setDeviceAddress(deviceAddress);
		} else {
			ScannerDeviceData.getInstance().setConnected(false);
			ScannerDeviceData.getInstance().setDeviceName("");
			ScannerDeviceData.getInstance().setDeviceAddress("");
		}
	}
	public boolean isBarcodeScannerConnect() {
		return mSharedSetting.getBoolean("bluetooth_barcodescanner_connect_yn", false);
	}
	public String getBarcodeScannerDeviceName() {
		return mSharedSetting.getString("bluetooth_barcodescanner_devicename", "");
	}
	public String getBarcodeScannerDeviceAddress() {
		return mSharedSetting.getString("bluetooth_barcodescanner_deviceaddress", "");
	}
	public void removeBarcodeScanner() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("bluetooth_barcodescanner_connect_yn");
		editor.remove("bluetooth_barcodescanner_devicename");
		editor.remove("bluetooth_barcodescanner_deviceaddress");
		editor.commit();
	}
	
	/**
	 * 소프트키보드 활성 여부
	 * **사용안함**
	 */
	public void setScannerSoftKeyboard(boolean yn) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("bluetooth_softkeyboard_connect_yn", yn);
		editor.commit();
	}
	public boolean isScannerSoftKeyboard() {
		return mSharedSetting.getBoolean("bluetooth_softkeyboard_connect_yn", false);
	}
	public void removeScannerSoftKeyboard() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("bluetooth_softkeyboard_connect_yn");
		editor.commit();
	}

	/**
	 * 자동로그인 설정.
	 * **사용안함**
	 */
	public void setAutoLogin(boolean yn, String userId, String password) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("account_autologin_yn", yn);
		editor.putString("account_autologin_userId", userId);
		editor.putString("account_autologin_password", password);
		editor.commit();
	}
	public boolean isAutoLogin() {
		return mSharedSetting.getBoolean("account_autologin_yn", false);
	}
	public String getUserId() {
		return mSharedSetting.getString("account_autologin_userId", "");
	}
	public String getPassword() {
		return mSharedSetting.getString("account_autologin_password", "");
	}
	public void removeAutoLogin() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("account_autologin_yn");
		editor.remove("account_autologin_userId");
		editor.remove("account_autologin_password");
		editor.commit();
	}
	
	/**
	 * 자재마스터 업데이트
	 */
	public void setSurveyMasterUpdate(String workDttm) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putString("surveymaster_update_workdttm", workDttm);
		editor.commit();
	}
	public String getSurveyMasterUpdate_WorkDttm() {
		return mSharedSetting.getString("surveymaster_update_workdttm", "");
	}
	public void removeSurveyMasterUpdate() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("surveymaster_update_workdttm");
		editor.commit();
	}
	
	/**
	 * 사운드 및 효과음 활성 여부
	 */
	public void setSoundEffectsLock(boolean yn) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("soundeffects_lock_yn", yn);
		editor.commit();
	}
	public boolean isSoundEffectsLock() {
		return mSharedSetting.getBoolean("soundeffects_lock_yn", false);
	}
	public void removeSoundEffectsLock() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("soundeffects_lock_yn");
		editor.commit();
	}
	
	/**
	 * 공지사항 오늘하루 열지 않기
	 */
	public void setNoticeNowNotOpen(String nowDate, boolean isNotOpen) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putString("notice_nownotopen_nowdate", nowDate);
		editor.putBoolean("notice_nownotopen_isnotopen", isNotOpen);
		editor.commit();
	}
	public String getNotice_NowDate() {
		return mSharedSetting.getString("notice_nownotopen_nowdate", "");
	}
	public boolean isNotice_isNotOpen() {
		return mSharedSetting.getBoolean("notice_nownotopen_isnotopen", false);
	}
	public void removeNoticeNowNotOpen() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("notice_nownotopen_nowdate");
		editor.remove("notice_nownotopen_isnotopen");
		editor.commit();
	}
	
	/**
	 * 약괸동의여부 
	 */
	public void setInfoAgree1(boolean agree) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("PERSONAL_INFO_AGREE1", agree);
		editor.commit();
	}
	
	public void setInfoAgree2(boolean agree) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("PERSONAL_INFO_AGREE2", agree);
		editor.commit();
	}
	
	public void setInfoAgree3(boolean agree) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("PERSONAL_INFO_AGREE3", agree);
		editor.commit();
	}
	
	public void setInfoAgree4(boolean agree) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("PERSONAL_INFO_AGREE4", agree);
		editor.commit();
	}
	
	public boolean getInfoAgree1() {
		return mSharedSetting.getBoolean("PERSONAL_INFO_AGREE1", false);
	}
	
	public boolean getInfoAgree2() {
		return mSharedSetting.getBoolean("PERSONAL_INFO_AGREE2", false);
	}
	
	public boolean getInfoAgree3() {
		return mSharedSetting.getBoolean("PERSONAL_INFO_AGREE3", false);
	}
	
	public boolean getInfoAgree4() {
		return mSharedSetting.getBoolean("PERSONAL_INFO_AGREE4", false);
	}
	
	public void removeInfoAgree() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("PERSONAL_INFO_AGREE1");
		editor.remove("PERSONAL_INFO_AGREE2");
		editor.remove("PERSONAL_INFO_AGREE3");
		editor.remove("PERSONAL_INFO_AGREE4");
		editor.commit();
	}
	
	/**
	 * 로그인 로그아웃 여부 
	 */
	public void setLoginYn(boolean login,String devId) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putBoolean("LOGINYN", login);
		editor.putString("LOGINID", devId);
		editor.commit();
	}
	public boolean getLogin() {
		return mSharedSetting.getBoolean("LOGINYN", false);
	}
	public String getLoginId() {
		return mSharedSetting.getString("LOGINID", "000");
	}
	public void removeLoginYn() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.remove("LOGINYN");
		editor.remove("LOGINID");
		editor.commit();
	}
	
	/**
	 * XML 전체내용삭제.
	 */
	public void allRemove() {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.clear();
		editor.commit();
	}
	
	/**
	 * 프린터 설정내역 저장 
	 * @throws JSONException 
	 */
	
	public void setDefaultPrinterUserInfo() throws JSONException {
	    SharedPreferences.Editor mEdit= mSharedSetting.edit();
	    JSONArray printerInfoArray = new JSONArray();
        JSONObject printer_0 = new JSONObject();
        printer_0.put("key", "0");
        printer_0.put("x", "570");
        printer_0.put("y", "0");
        printer_0.put("xu", "0");
        printer_0.put("yu", "0");
        printer_0.put("sd", "25");
        printerInfoArray.put(printer_0);
        
        JSONObject printer_1 = new JSONObject();
        printer_1.put("key", "1");
        printer_1.put("x", "520");
        printer_1.put("y", "0");
        printer_1.put("xu", "0");
        printer_1.put("yu", "0");
        printer_1.put("sd", "25");
        printerInfoArray.put(printer_1);
        
        JSONObject printer_2 = new JSONObject();
        printer_2.put("key", "2");
        printer_2.put("x", "540");
        printer_2.put("y", "70");
        printer_2.put("xu", "0");
        printer_2.put("yu", "0");
        printer_2.put("sd", "25");
        printerInfoArray.put(printer_2);
        
        JSONObject printer_3 = new JSONObject();
        printer_3.put("key", "3");
        printer_3.put("x", "420");
        printer_3.put("y", "110");
        printer_3.put("xu", "0");
        printer_3.put("yu", "0");
        printer_3.put("sd", "25");
        printerInfoArray.put(printer_3);
        
        JSONObject printer_5 = new JSONObject();
        printer_5.put("key", "5");
        printer_5.put("x", "330");
        printer_5.put("y", "0");
        printer_5.put("xu", "0");
        printer_5.put("yu", "0");
        printer_5.put("sd", "25");
        printerInfoArray.put(printer_5);
        
        JSONObject printer_6 = new JSONObject();
        printer_6.put("key", "6");
        printer_6.put("x", "500");
        printer_6.put("y", "0");
        printer_6.put("xu", "0");
        printer_6.put("yu", "0");
        printer_6.put("sd", "25");
        printerInfoArray.put(printer_6);
        
        JSONObject printer_7 = new JSONObject();
        printer_7.put("key", "7");
        printer_7.put("x", "540");
        printer_7.put("y", "230");
        printer_7.put("xu", "0");
        printer_7.put("yu", "0");
        printer_7.put("sd", "25");
        printerInfoArray.put(printer_7);
        
        JSONObject printer_8 = new JSONObject();
        printer_8.put("key", "8");
        printer_8.put("x", "0");
        printer_8.put("y", "0");
        printer_8.put("xu", "0");
        printer_8.put("yu", "0");
        printer_8.put("sd", "25");
        printerInfoArray.put(printer_8);
        
        JSONObject printer_9 = new JSONObject();
        printer_9.put("key", "9");
        printer_9.put("x", "0");
        printer_9.put("y", "0");
        printer_9.put("xu", "0");
        printer_9.put("yu", "0");
        printer_9.put("sd", "25");
        printerInfoArray.put(printer_9);
        
        JSONObject printer_10 = new JSONObject();
        printer_10.put("key", "10");
        printer_10.put("x", "0");
        printer_10.put("y", "0");
        printer_10.put("xu", "0");
        printer_10.put("yu", "0");
        printer_10.put("sd", "25");
        printerInfoArray.put(printer_10);
        
	    mEdit.putInt("printer_info",printerInfoArray.length() * 5);
	    for(int i = 0; i < printerInfoArray.length(); i++){
	    	JSONObject printer_temp = printerInfoArray.getJSONObject(i);
	    	mEdit.putString("printer_info_" + printer_temp.getString("key") + "_x", printer_temp.getString("x"));
	    	mEdit.putString("printer_info_" + printer_temp.getString("key") + "_y", printer_temp.getString("y"));
	    	mEdit.putString("printer_info_" + printer_temp.getString("key") + "_xu", printer_temp.getString("xu"));
	    	mEdit.putString("printer_info_" + printer_temp.getString("key") + "_yu", printer_temp.getString("yu"));
	    	mEdit.putString("printer_info_" + printer_temp.getString("key") + "_sd", printer_temp.getString("sd"));
        }
	    mEdit.commit();
	}
	
	public void setPrinterUserInfo(String idx, String key, String value) {
		SharedPreferences.Editor editor = mSharedSetting.edit();
		editor.putString("printer_info_" + idx +"_" + key, value);
		editor.commit();
	}
	
	public String getPrinterUserInfo(String idx, String value) {
		String returnValue = mSharedSetting.getString("printer_info_" + idx + "_" + value, "");
		return returnValue; 
	}
}
