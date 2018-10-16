package com.ktds.erpbarcode.common.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.ktds.erpbarcode.common.widget.SpinnerInfo;

public class WorkInfoQuery {
	private static final String TAG = "WorkInfoQuery";

	private SQLiteDatabase database;
	private ErpBarcodeDatabaseHelper dbHelper;
	private String[] allColumns = {
			WorkInfoTable.COLUMN_ID,
			WorkInfoTable.COLUMN_WORK_NAME, 
			WorkInfoTable.COLUMN_LOC_CD,
			WorkInfoTable.COLUMN_LOC_NAME,
			WorkInfoTable.COLUMN_WBS_NO,
			WorkInfoTable.COLUMN_DEVICE_ID,
			WorkInfoTable.COLUMN_INPUT_TIME,
			WorkInfoTable.COLUMN_TRAN_YN,
			WorkInfoTable.COLUMN_TRAN_TIME,
			WorkInfoTable.COLUMN_SRCH_YN,
			WorkInfoTable.COLUMN_DLVRY_ORD,
			WorkInfoTable.COLUMN_CHECK_SCAN,
			WorkInfoTable.COLUMN_OFFLINE_YN,
			WorkInfoTable.COLUMN_TRANS_NO,
			WorkInfoTable.COLUMN_UFAC_CD,
			WorkInfoTable.COLUMN_TREE_YN,
			WorkInfoTable.COLUMN_TRY_TIME,
			WorkInfoTable.COLUMN_TRAN_RSLT,
			WorkInfoTable.COLUMN_ITEM_COUNT };

	public WorkInfoQuery(Context context) {
		dbHelper = new ErpBarcodeDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
	    dbHelper.close();
	}
	
	public int totalCount() {
		Cursor tottalcursor = database.query(WorkInfoTable.TABLE_WORK_INFO,
				allColumns, null, null, null, null, null);
		Log.i(TAG, "getWorkInfosByMatnrAndBismt     tottalcursor.getCount()==>"+tottalcursor.getCount());
		int count = tottalcursor.getCount();
		tottalcursor.close();
		return count;
	}

	public WorkInfo getWorkInfoById(int id) throws SQLiteException {
		String selection = WorkInfoTable.COLUMN_ID + " = " + id;

		Cursor cursor = database.query(WorkInfoTable.TABLE_WORK_INFO,
				allColumns, selection, null, null, null, null);

		Log.i(TAG, "getWorkInfosById     Count==>"+cursor.getCount());
		
		cursor.moveToFirst();
		WorkInfo workInfo = cursorToWorkInfo(cursor);
		cursor.close();
		
		return workInfo;
	}
	
	public List<WorkInfo> getAllWorkInfos() throws SQLiteException {
		Cursor cursor = database.query(WorkInfoTable.TABLE_WORK_INFO,
				allColumns, null, null, null, null, null);

		Log.i(TAG, "getAllWorkInfos    getCount==>"+cursor.getCount());
		
		List<WorkInfo> workInfos = new ArrayList<WorkInfo>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			WorkInfo workInfo = cursorToWorkInfo(cursor);
			workInfos.add(workInfo);
			cursor.moveToNext();
		}
		cursor.close();
		
		return workInfos;
	}
	
	/**
	 * 작업구분별 작업내역 목록 조회.
	 * 
	 * @param jobGubun  작업명 (L, D, F..)
	 * @param sendGubun 전송구분 (N.미전송, Y.전송완료, E.전송실패, W.경고)
	 * @param jobDate   작업일자
	 * @return
	 */
	public List<WorkInfo> getWorkInfos(String jobGubun, String sendGubun, String jobDate) throws SQLiteException {
		String selection = "";
		if (!jobGubun.isEmpty()) {
			selection = WorkInfoTable.COLUMN_WORK_NAME + " = '" + jobGubun + "'";
		}
		
		if (!sendGubun.isEmpty()) {
			if (!selection.isEmpty()) selection += " and ";
			selection += WorkInfoTable.COLUMN_TRAN_YN + " = '" + sendGubun + "'";
		}
		
		if (!jobDate.isEmpty()) {
			if (!selection.isEmpty()) selection += " and ";
			selection += "STRFTIME('%Y-%m-%d'," + WorkInfoTable.COLUMN_INPUT_TIME + ") = '" + jobDate + "'";
		}

		String orderby = WorkInfoTable.COLUMN_INPUT_TIME + " DESC";

		Cursor cursor = database.query(WorkInfoTable.TABLE_WORK_INFO,
				allColumns, selection, null, null, null, orderby);

		Log.i(TAG, "getWorkInfos    getCount==>"+cursor.getCount());
		
		List<WorkInfo> workInfos = new ArrayList<WorkInfo>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			WorkInfo workInfo = cursorToWorkInfo(cursor);
			workInfos.add(workInfo);
			cursor.moveToNext();
		}
		cursor.close();
		
		return workInfos;
	}
	
	public List<SpinnerInfo> getWorkJobGubuns() throws SQLiteException {
		String sql = "SELECT DISTINCT " + WorkInfoTable.COLUMN_WORK_NAME + 
				" FROM " + WorkInfoTable.TABLE_WORK_INFO + 
				" ORDER BY " + WorkInfoTable.COLUMN_INPUT_TIME + " DESC";

		Cursor cursor = database.rawQuery(sql, null);

		Log.i(TAG, "getWorkJobGubuns  Count==>"+cursor.getCount());
		
		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
		spinneritems.add(new SpinnerInfo("","작업 전체"));
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String spinnerCode = cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_WORK_NAME));
			String spinnerName = cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_WORK_NAME));
			SpinnerInfo spinnerInfo = new SpinnerInfo(spinnerCode, spinnerName);
			spinneritems.add(spinnerInfo);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return spinneritems;
	}
	
	public List<SpinnerInfo> getWorkSendGubuns() throws SQLiteException {
		String sql = "SELECT DISTINCT " + "CASE  WHEN " + WorkInfoTable.COLUMN_TRAN_YN + " = null THEN 'N' ELSE " + WorkInfoTable.COLUMN_TRAN_YN + " END AS " + WorkInfoTable.COLUMN_TRAN_YN +
				" FROM " + WorkInfoTable.TABLE_WORK_INFO + 
				" ORDER BY " + WorkInfoTable.COLUMN_INPUT_TIME + " DESC";

		Cursor cursor = database.rawQuery(sql, null);

		Log.i(TAG, "getWorkSendGubuns  Count==>"+cursor.getCount());
		
		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
		spinneritems.add(new SpinnerInfo("","전송 전체"));
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String spinnerName = "";
			String tranYn = cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRAN_YN));
			if (tranYn.equals("Y")) {
				spinnerName = "전송성공";
			} else if (tranYn.equals("E")) {
				spinnerName = "전송실패";
			} else if (tranYn.equals("W")) {
				spinnerName = "경고";
			} else {
				spinnerName = "미전송";
			}

			SpinnerInfo spinnerInfo = new SpinnerInfo(tranYn, spinnerName);
			spinneritems.add(spinnerInfo);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return spinneritems;
	}
	
	public List<SpinnerInfo> getWorkJobDates() throws SQLiteException {
		
		String sql = "SELECT DISTINCT " + "STRFTIME('%Y-%m-%d'," + WorkInfoTable.COLUMN_INPUT_TIME + ") AS JOB_DATE" + 
				" FROM " + WorkInfoTable.TABLE_WORK_INFO + 
				" ORDER BY " + "STRFTIME('%Y-%m-%d'," + WorkInfoTable.COLUMN_INPUT_TIME + ")  DESC";

		Cursor cursor = database.rawQuery(sql, null);

		Log.i(TAG, "getWorkJobDates  Count==>"+cursor.getCount());
		
		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
		spinneritems.add(new SpinnerInfo("","일자 전체"));
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String spinnerCode = cursor.getString(cursor.getColumnIndexOrThrow("JOB_DATE"));
			String spinnerName = cursor.getString(cursor.getColumnIndexOrThrow("JOB_DATE"));
			SpinnerInfo spinnerInfo = new SpinnerInfo(spinnerCode, spinnerName);
			spinneritems.add(spinnerInfo);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return spinneritems;
	}
	
	public WorkInfo createWorkInfo(WorkInfo workInfo) throws SQLiteException {

		ContentValues values = new ContentValues();
	    //values.put(WorkInfoTable.COLUMN_ID, workInfo.getId());
	    values.put(WorkInfoTable.COLUMN_WORK_NAME, workInfo.getWorkName());
	    values.put(WorkInfoTable.COLUMN_LOC_CD, workInfo.getLocCd());
	    values.put(WorkInfoTable.COLUMN_LOC_NAME, workInfo.getLocName());
	    values.put(WorkInfoTable.COLUMN_WBS_NO, workInfo.getWbsNo());
	    values.put(WorkInfoTable.COLUMN_DEVICE_ID, workInfo.getDeviceId());
	    values.put(WorkInfoTable.COLUMN_INPUT_TIME, workInfo.getInputTime());
	    values.put(WorkInfoTable.COLUMN_TRAN_YN, workInfo.getTranYn());
	    values.put(WorkInfoTable.COLUMN_TRAN_TIME, workInfo.getTranTime());
	    values.put(WorkInfoTable.COLUMN_SRCH_YN, workInfo.getSrchYn());
	    values.put(WorkInfoTable.COLUMN_DLVRY_ORD, workInfo.getDlvryOrd());
	    values.put(WorkInfoTable.COLUMN_CHECK_SCAN, workInfo.getCheckScan());
	    values.put(WorkInfoTable.COLUMN_OFFLINE_YN, workInfo.getOfflineYn());
	    values.put(WorkInfoTable.COLUMN_TRANS_NO, workInfo.getTransNo());
	    values.put(WorkInfoTable.COLUMN_UFAC_CD, workInfo.getuFacCd());
	    values.put(WorkInfoTable.COLUMN_TREE_YN, workInfo.getTreeYn());
	    values.put(WorkInfoTable.COLUMN_TRY_TIME, workInfo.getTryTime());
	    values.put(WorkInfoTable.COLUMN_TRAN_RSLT, workInfo.getTranRslt());
	    values.put(WorkInfoTable.COLUMN_ITEM_COUNT, workInfo.getItemCount());
	    
	    long insertId = database.insert(WorkInfoTable.TABLE_WORK_INFO, null, values);
	    
	    WorkInfo newWorkInfo = getWorkInfoById( (int) insertId);
	    return newWorkInfo;
	}
	
	public WorkInfo updateWorkInfo(WorkInfo workInfo) throws SQLiteException {
		
		ContentValues values = new ContentValues();
	    values.put(WorkInfoTable.COLUMN_ID, workInfo.getId());
	    values.put(WorkInfoTable.COLUMN_WORK_NAME, workInfo.getWorkName());
	    values.put(WorkInfoTable.COLUMN_LOC_CD, workInfo.getLocCd());
	    values.put(WorkInfoTable.COLUMN_LOC_NAME, workInfo.getLocName());
	    values.put(WorkInfoTable.COLUMN_WBS_NO, workInfo.getWbsNo());
	    values.put(WorkInfoTable.COLUMN_DEVICE_ID, workInfo.getDeviceId());
	    values.put(WorkInfoTable.COLUMN_INPUT_TIME, workInfo.getInputTime());
	    values.put(WorkInfoTable.COLUMN_TRAN_YN, workInfo.getTranYn());
	    values.put(WorkInfoTable.COLUMN_TRAN_TIME, workInfo.getTranTime());
	    values.put(WorkInfoTable.COLUMN_SRCH_YN, workInfo.getSrchYn());
	    values.put(WorkInfoTable.COLUMN_DLVRY_ORD, workInfo.getDlvryOrd());
	    values.put(WorkInfoTable.COLUMN_CHECK_SCAN, workInfo.getCheckScan());
	    values.put(WorkInfoTable.COLUMN_OFFLINE_YN, workInfo.getOfflineYn());
	    values.put(WorkInfoTable.COLUMN_TRANS_NO, workInfo.getTransNo());
	    values.put(WorkInfoTable.COLUMN_UFAC_CD, workInfo.getuFacCd());
	    values.put(WorkInfoTable.COLUMN_TREE_YN, workInfo.getTreeYn());
	    values.put(WorkInfoTable.COLUMN_TRY_TIME, workInfo.getTryTime());
	    values.put(WorkInfoTable.COLUMN_TRAN_RSLT, workInfo.getTranRslt());
	    values.put(WorkInfoTable.COLUMN_ITEM_COUNT, workInfo.getItemCount());
	    
	    String where = WorkInfoTable.COLUMN_ID + " = " + workInfo.getId();
	    database.update(WorkInfoTable.TABLE_WORK_INFO, values, where, null);
	    
	    WorkInfo newWorkInfo = getWorkInfoById( workInfo.getId());
	    return newWorkInfo;
	}

	public void deleteWorkInfo(int _id) throws SQLiteException {
		Log.i(TAG, "deleteWorkInfo     id==>"+_id);
	    database.delete(WorkInfoTable.TABLE_WORK_INFO, WorkInfoTable.COLUMN_ID + " = " + _id, null);
	}
	
	public void deleteAllWorkInfo() throws SQLiteException {
		Log.i(TAG, "deleteAllWorkInfo     Start...");
	    database.delete(WorkInfoTable.TABLE_WORK_INFO, null, null);
	}
	
	private WorkInfo cursorToWorkInfo(Cursor cursor) {
		WorkInfo workInfo = new WorkInfo();
		
		workInfo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_ID)) );
		workInfo.setWorkName(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_WORK_NAME)) );
		workInfo.setLocCd(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_LOC_CD)) );
		workInfo.setLocName(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_LOC_NAME)) );
		workInfo.setWbsNo(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_WBS_NO)) );
		workInfo.setDeviceId(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_DEVICE_ID)) );
		workInfo.setInputTime(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_INPUT_TIME)) );
		workInfo.setTranYn(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRAN_YN)) );
		workInfo.setTranTime(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRAN_TIME)) );
		workInfo.setSrchYn(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_SRCH_YN)) );
		workInfo.setDlvryOrd(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_DLVRY_ORD)) );
		workInfo.setCheckScan(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_CHECK_SCAN)) );
		workInfo.setOfflineYn(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_OFFLINE_YN)) );
		workInfo.setTransNo(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRANS_NO)) );
		workInfo.setuFacCd(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_UFAC_CD)) );
		workInfo.setTreeYn(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TREE_YN)) );
		workInfo.setTryTime(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRY_TIME)) );
		workInfo.setTranRslt(cursor.getString(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_TRAN_RSLT)) );
		workInfo.setItemCount(cursor.getInt(cursor.getColumnIndexOrThrow(WorkInfoTable.COLUMN_ITEM_COUNT)) );

		return workInfo;
	}
}
