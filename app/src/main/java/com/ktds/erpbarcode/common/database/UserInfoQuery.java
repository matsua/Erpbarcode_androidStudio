package com.ktds.erpbarcode.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.encryption.StringEncrypter;

public class UserInfoQuery {
	private static final String TAG = "UserInfoQuery";
	

	private SQLiteDatabase database;
	private ErpBarcodeDatabaseHelper dbHelper;
	private String[] allColumns = { 
			UserInfoTable.COLUMN_USER_ID,
			UserInfoTable.COLUMN_PASSWORD, 
			UserInfoTable.COLUMN_USER_NAME,
			UserInfoTable.COLUMN_TEL_NO,
			UserInfoTable.COLUMN_PHONE_NO,
			UserInfoTable.COLUMN_ORG_ID,
			UserInfoTable.COLUMN_ORG_NAME,
			UserInfoTable.COLUMN_ORG_CODE,
			UserInfoTable.COLUMN_ORG_TYPE_CODE,
			UserInfoTable.COLUMN_COMPANY_CODE };

	public UserInfoQuery(Context context) {
		dbHelper = new ErpBarcodeDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
	    dbHelper.close();
	}
	
	public UserInfo getUserInfoById(String userId) throws SQLiteException {
		String selection = UserInfoTable.COLUMN_USER_ID + " = " + "'" + encryptString(userId) + "'";

		Cursor cursor = database.query(UserInfoTable.TABLE_USER_INFO, allColumns, selection, null, null, null, null);

		Log.i(TAG, "getUserInfoById  Count==>"+cursor.getCount());
		
		UserInfo userInfo = new UserInfo();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			userInfo = cursorToUserInfo(cursor);
		}
		cursor.close();
		
		return userInfo;
	}
	
	public void createUserInfo(UserInfo userInfo) {
		
		ContentValues values = new ContentValues();
		
	    values.put(UserInfoTable.COLUMN_USER_ID, encryptString(userInfo.getUserId()));
	    values.put(UserInfoTable.COLUMN_PASSWORD, encryptString(userInfo.getPassword()));
	    values.put(UserInfoTable.COLUMN_USER_NAME, encryptString(userInfo.getUserName()));
	    values.put(UserInfoTable.COLUMN_TEL_NO, encryptString(userInfo.getTelNo()));
	    values.put(UserInfoTable.COLUMN_PHONE_NO, encryptString(userInfo.getPhoneNo()));
	    values.put(UserInfoTable.COLUMN_ORG_ID, encryptString(userInfo.getOrgId()));
	    values.put(UserInfoTable.COLUMN_ORG_NAME, encryptString(userInfo.getOrgName()));
	    values.put(UserInfoTable.COLUMN_ORG_CODE, encryptString(userInfo.getOrgCode()));
	    values.put(UserInfoTable.COLUMN_ORG_TYPE_CODE, encryptString(userInfo.getOrgTypeCode()));
	    values.put(UserInfoTable.COLUMN_COMPANY_CODE, encryptString(userInfo.getCompanyCode()));
	    
	    database.insert(UserInfoTable.TABLE_USER_INFO, null, values);

	}
	
	public void deleteUserInfo(String userId) {
		Log.i(TAG, "deleteUserInfo     id==>"+userId);
	    database.delete(UserInfoTable.TABLE_USER_INFO, UserInfoTable.COLUMN_USER_ID + " = " + "'" + encryptString(userId) + "'", null);
	}
	
	public void deleteAllUserInfo() {
		Log.i(TAG, "deleteAllUserInfo     Start...");
	    database.delete(UserInfoTable.TABLE_USER_INFO, null, null);
	}
	
	private UserInfo cursorToUserInfo(Cursor cursor) {
		UserInfo userInfo = new UserInfo();
		
		userInfo.setUserId(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_USER_ID))));
		userInfo.setPassword(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_PASSWORD))));
		userInfo.setUserName(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_USER_NAME))));
		userInfo.setTelNo(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_TEL_NO))));
		userInfo.setPhoneNo(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_PHONE_NO))));
		userInfo.setOrgId(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_ORG_ID))));
		userInfo.setOrgName(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_ORG_NAME))));
		userInfo.setOrgCode(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_ORG_CODE))));
		userInfo.setOrgTypeCode(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_ORG_TYPE_CODE))));
		userInfo.setCompanyCode(decryptString(cursor.getString(cursor.getColumnIndexOrThrow(UserInfoTable.COLUMN_COMPANY_CODE))));

		return userInfo;
	}
	
	public String encryptString(String string){
		StringEncrypter encrypt;
		String encryptString = "";
		
		if(string.equals("")){
			return "";
		}
		
		try {
			encrypt = new StringEncrypter("kt_erp_common_key", "kt_erp_common_iv");
			encryptString = encrypt.encrypt(string);
		} catch (ErpBarcodeException e) {
			e.printStackTrace();
		}
		
		System.out.println("encryptString :: " + encryptString);
		
		return encryptString;
	}
	
	public String decryptString(String string){
		StringEncrypter decrypt;
		String decryptString = "";
		
		if(string.equals("")){
			return "";
		}
		
		try {
			decrypt = new StringEncrypter("kt_erp_common_key", "kt_erp_common_iv");
			decryptString = decrypt.decrypt(string);
		} catch (ErpBarcodeException e) {
			e.printStackTrace();
		}
		
		System.out.println("decryptString :: " + decryptString);
		
		return decryptString;
		
	}
}
