package com.ktds.erpbarcode.common.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserInfoTable {
	// Database table
	public static final String TABLE_USER_INFO = "user_info";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_USER_NAME = "user_name";
	public static final String COLUMN_TEL_NO = "tel_no";
	public static final String COLUMN_PHONE_NO = "phone_no";
	public static final String COLUMN_ORG_ID = "org_id";
	public static final String COLUMN_ORG_NAME = "org_name";
	public static final String COLUMN_ORG_CODE = "org_code";
	public static final String COLUMN_ORG_TYPE_CODE = "org_type_code";
	public static final String COLUMN_COMPANY_CODE = "company_code";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_USER_INFO
			+ "(" + COLUMN_USER_ID + " varchar(20) primary key  not null, "
			+ COLUMN_PASSWORD + " varchar2(20) not null, " 
			+ COLUMN_USER_NAME + " varchar2(40) not null," 
			+ COLUMN_TEL_NO + " varchar2(20) null,"
			+ COLUMN_PHONE_NO + " varchar2(20) null," 
			+ COLUMN_ORG_ID + " varchar2(10) null," 
			+ COLUMN_ORG_NAME + " varchar2(100) null," 
			+ COLUMN_ORG_CODE + " varchar2(10) null," 
			+ COLUMN_ORG_TYPE_CODE + " varchar2(10) null," 
			+ COLUMN_COMPANY_CODE + " varchar2(10) null" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(BpIItemTable.class.getName(), "Upgrading database from version "+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_INFO);
		onCreate(database);
	}
}
