package com.ktds.erpbarcode.common.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WorkItemTable {
	// Database table
	public static final String TABLE_WORK_ITEM = "work_item";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_WORK_ID = "work_id";
	public static final String COLUMN_JOB_TYPE = "job_type";
	public static final String COLUMN_JOB_DATA = "job_data";
	public static final String COLUMN_INPUT_TIME = "input_time";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_WORK_ITEM
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_WORK_ID + " integer not NULL, "
			+ COLUMN_JOB_TYPE + " varchar2(20) NULL,"
			+ COLUMN_JOB_DATA + " varchar2(1024) NULL,"
			+ COLUMN_INPUT_TIME + " date default (datetime('now', 'localtime'))" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(WorkItemTable.class.getName(), "Upgrading database from version "+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_WORK_ITEM);
		onCreate(database);
	}
}
