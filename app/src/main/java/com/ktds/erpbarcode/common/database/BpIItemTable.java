package com.ktds.erpbarcode.common.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BpIItemTable {
	// Database table
	public static final String TABLE_BP_I_ITEM = "bp_i_item";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PRODUCT_CODE = "matnr";
	public static final String COLUMN_PRODUCT_NAME = "maktx";
	public static final String COLUMN_DEVTYPE = "zmatgb";
	public static final String COLUMN_BISMT = "bismt";
	public static final String COLUMN_EQSHAPE = "eqshape";
	public static final String COLUMN_PARTTYPECODE = "comptype";
	public static final String COLUMN_ZZOLDBARCDIND = "zzoldbarcdind";
	public static final String COLUMN_ZZOLDBARMATL = "zzoldbarmatl";
	public static final String COLUMN_ZZNEWBARCDIND = "zznewbarcdind";
	public static final String COLUMN_ZZNEWBARMATL = "zznewbarmatl";
	public static final String COLUMN_ZEMAFT = "zemaft";
	public static final String COLUMN_ZEMAFT_NAME = "zemaft_name";
	public static final String COLUMN_ZEFAMATNR = "zefamatnr";
	public static final String COLUMN_EXTWG = "extwg";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_EAI_CDATE = "eai_cdate";
	public static final String COLUMN_ZZMATN = "zzmatn";
	public static final String COLUMN_MTART = "mtart";
	public static final String COLUMN_BARCD = "barcd";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_BP_I_ITEM
			+ "(" + COLUMN_ID + " integer primary key, "
			+ COLUMN_PRODUCT_CODE + " varchar2(20) not null, " 
			+ COLUMN_PRODUCT_NAME + " varchar2(150) null," 
			+ COLUMN_DEVTYPE + " varchar2(2) null,"
			+ COLUMN_BISMT + " varchar2(18) null," 
			+ COLUMN_EQSHAPE + " varchar2(2) null," 
			+ COLUMN_PARTTYPECODE + " varchar2(2) null," 
			+ COLUMN_ZZOLDBARCDIND + " varchar2(3) null," 
			+ COLUMN_ZZOLDBARMATL + " varchar2(20) null," 
			+ COLUMN_ZZNEWBARCDIND + " varchar2(3) null,"
			+ COLUMN_ZZNEWBARMATL + " varchar2(20) null," 
			+ COLUMN_ZEMAFT + " varchar2(3) null," 
			+ COLUMN_ZEMAFT_NAME + " varchar2(120) null," 
			+ COLUMN_ZEFAMATNR + " varchar2(18) null," 
			+ COLUMN_EXTWG + " varchar2(18) null," 
			+ COLUMN_STATUS + " varchar2(4) null,"
			+ COLUMN_EAI_CDATE + " varchar2(17) null," 
			+ COLUMN_ZZMATN + " varchar2(400) null," 
			+ COLUMN_MTART + " varchar2(5) null," 
			+ COLUMN_BARCD + " varchar2(1) null" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(BpIItemTable.class.getName(), "Upgrading database from version "+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_BP_I_ITEM);
		onCreate(database);
	}
}
