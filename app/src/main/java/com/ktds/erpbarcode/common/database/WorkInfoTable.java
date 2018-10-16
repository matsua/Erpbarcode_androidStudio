package com.ktds.erpbarcode.common.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WorkInfoTable {
	// Database table
	public static final String TABLE_WORK_INFO = "work_info";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WORK_NAME = "work_name";
	public static final String COLUMN_LOC_CD = "loc_cd";
	public static final String COLUMN_LOC_NAME = "loc_name";
	public static final String COLUMN_WBS_NO = "wbs_no";
	public static final String COLUMN_DEVICE_ID = "device_id";
	public static final String COLUMN_INPUT_TIME = "input_time";
	public static final String COLUMN_TRAN_YN = "tran_yn";
	public static final String COLUMN_TRAN_TIME = "tran_time";
	public static final String COLUMN_SRCH_YN = "srch_yn";
	public static final String COLUMN_DLVRY_ORD = "dlvry_ord";
	public static final String COLUMN_CHECK_SCAN = "check_scan";
	public static final String COLUMN_OFFLINE_YN = "offline_yn";
	public static final String COLUMN_TRANS_NO = "trans_no";
	public static final String COLUMN_UFAC_CD = "ufac_cd";
	public static final String COLUMN_TREE_YN = "tree_yn";
	public static final String COLUMN_TRY_TIME = "try_time";
	public static final String COLUMN_TRAN_RSLT = "tran_rslt";
	public static final String COLUMN_ITEM_COUNT = "item_count";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_WORK_INFO
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_WORK_NAME + " varchar2(100) not null, " 
			+ COLUMN_LOC_CD + " varchar2(21) null," 
			+ COLUMN_LOC_NAME + " varchar2(100) null,"
			+ COLUMN_WBS_NO + " varchar2(20) null," 
			+ COLUMN_DEVICE_ID + " varchar2(9) null," 
			+ COLUMN_INPUT_TIME + " date default (datetime('now', 'localtime'))," 
			+ COLUMN_TRAN_YN + " varchar2(1) null," 
			+ COLUMN_TRAN_TIME + " varchar2(8) null," 
			+ COLUMN_SRCH_YN + " varchar2(1) null,"
			+ COLUMN_DLVRY_ORD + " varchar2(100) null," 
			+ COLUMN_CHECK_SCAN + " varchar2(1) null," 
			+ COLUMN_OFFLINE_YN + " varchar2(1) null," 
			+ COLUMN_TRANS_NO + " varchar2(12) null," 
			+ COLUMN_UFAC_CD + " varchar2(20) null," 
			+ COLUMN_TREE_YN + " varchar2(17) null,"
			+ COLUMN_TRY_TIME + " date default (datetime('now', 'localtime'))," 
			+ COLUMN_TRAN_RSLT + " varchar2(255) null," 
			+ COLUMN_ITEM_COUNT + " integer default 0" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(WorkInfoTable.class.getName(), "Upgrading database from version "+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_WORK_INFO);
		onCreate(database);
	}
}
