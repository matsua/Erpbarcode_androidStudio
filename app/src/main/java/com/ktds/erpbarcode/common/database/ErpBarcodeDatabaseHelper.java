package com.ktds.erpbarcode.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ErpBarcodeDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "erpbarcode.db";
	private static final int DATABASE_VERSION = 5;

	public ErpBarcodeDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		BpIItemTable.onCreate(database);
		WorkInfoTable.onCreate(database);
		WorkItemTable.onCreate(database);
		UserInfoTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		BpIItemTable.onUpgrade(database, oldVersion, newVersion);
		WorkInfoTable.onUpgrade(database, oldVersion, newVersion);
		WorkItemTable.onUpgrade(database, oldVersion, newVersion);
		UserInfoTable.onUpgrade(database, oldVersion, newVersion);
	}

}
