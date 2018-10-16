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

import com.ktds.erpbarcode.job.JobActionStepManager;

public class WorkItemQuery {
	private static final String TAG = "WorkItemQuery";

	private SQLiteDatabase database;
	private ErpBarcodeDatabaseHelper dbHelper;
	private String[] allColumns = { 
			WorkItemTable.COLUMN_ID,
			WorkItemTable.COLUMN_WORK_ID, 
			WorkItemTable.COLUMN_JOB_TYPE,
			WorkItemTable.COLUMN_JOB_DATA,
			WorkItemTable.COLUMN_INPUT_TIME };

	public WorkItemQuery(Context context) {
		dbHelper = new ErpBarcodeDatabaseHelper(context);
	}
	
	public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
	    dbHelper.close();
	}
	
	public int totalCount() {
		Cursor tottalcursor = database.query(WorkItemTable.TABLE_WORK_ITEM,
				allColumns, null, null, null, null, null);
		Log.i(TAG, "totalCount==>"+tottalcursor.getCount());
		int count = tottalcursor.getCount();
		tottalcursor.close();
		return count;
	}

	public WorkItem getWorkItemById(String id) throws SQLiteException {
		String selection = WorkItemTable.COLUMN_ID + " = " + id;

		Cursor cursor = database.query(WorkItemTable.TABLE_WORK_ITEM,
				allColumns, selection, null, null, null, null);

		Log.i(TAG, "getWorkItemById  Count==>"+cursor.getCount());
		
		cursor.moveToFirst();
		WorkItem workItem = cursorToWorkItem(cursor);
		cursor.close();
		
		return workItem;
	}
	
	public List<WorkItem> getAllWorkItems() throws SQLiteException {
		Cursor cursor = database.query(WorkItemTable.TABLE_WORK_ITEM,
				allColumns, null, null, null, null, null);

		Log.i(TAG, "getAllWorkItems  getCount==>"+cursor.getCount());
		
		List<WorkItem> workItems = new ArrayList<WorkItem>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			WorkItem workItem = cursorToWorkItem(cursor);
			workItems.add(workItem);
			cursor.moveToNext();
		}
		cursor.close();
		
		return workItems;
	}
	
	public List<WorkItem> getWorkItemsByWorkId(int workId) throws SQLiteException {
		String selection = WorkItemTable.COLUMN_WORK_ID + " = " + String.valueOf(workId);
		String orderby = WorkItemTable.COLUMN_ID;

		Cursor cursor = database.query(WorkItemTable.TABLE_WORK_ITEM,
				allColumns, selection, null, null, null, orderby);

		Log.i(TAG, "getWorkItemsByWorkId  Count==>"+cursor.getCount());
		
		List<WorkItem> workItems = new ArrayList<WorkItem>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			WorkItem workItem = cursorToWorkItem(cursor);
			workItems.add(workItem);
			cursor.moveToNext();
		}
		cursor.close();
		
		return workItems;
	}

	
	public WorkItem createWorkItem(WorkItem workItem) throws SQLiteException {
		
		ContentValues values = new ContentValues();
	    //values.put(WorkItemTable.COLUMN_ID, workItem.getId());
	    values.put(WorkItemTable.COLUMN_WORK_ID, workItem.getWorkId());
	    values.put(WorkItemTable.COLUMN_JOB_TYPE, workItem.getJobType());
	    values.put(WorkItemTable.COLUMN_JOB_DATA, workItem.getJobData());
	    values.put(WorkItemTable.COLUMN_INPUT_TIME, workItem.getInputTime());
	    
	    long insertId = database.insert(WorkItemTable.TABLE_WORK_ITEM, null, values);
	    
	    String where = WorkItemTable.COLUMN_ID + " = " + insertId;
	    Cursor cursor = database.query(WorkItemTable.TABLE_WORK_ITEM, allColumns, where, null, null, null, null);
	    cursor.moveToFirst();
	    WorkItem newWorkItem = cursorToWorkItem(cursor);
	    cursor.close();
	    return newWorkItem;
	}

	public void deleteWorkItem(int _id) throws SQLiteException {
		Log.i(TAG, "deleteWorkItem     id==>"+_id);
	    database.delete(WorkItemTable.TABLE_WORK_ITEM, WorkItemTable.COLUMN_ID + " = " + _id, null);
	}
	
	public void deleteWorkItemsByWorkId(int workId) throws SQLiteException {
		Log.i(TAG, "deleteWorkItemByWorkId     workId==>"+workId);
	    database.delete(WorkItemTable.TABLE_WORK_ITEM, WorkItemTable.COLUMN_WORK_ID + " = " + workId, null);
	}
	
	public void deleteAllWorkItem() throws SQLiteException {
		Log.i(TAG, "deleteAllWorkItem     Start...");
	    database.delete(WorkItemTable.TABLE_WORK_ITEM, null, null);
	}
	
	private WorkItem cursorToWorkItem(Cursor cursor) {
		WorkItem workItem = new WorkItem();
		
		workItem.setId(cursor.getInt(cursor.getColumnIndexOrThrow(WorkItemTable.COLUMN_ID)) );
		workItem.setWorkId(cursor.getInt(cursor.getColumnIndexOrThrow(WorkItemTable.COLUMN_WORK_ID)) );
		workItem.setJobType(cursor.getString(cursor.getColumnIndexOrThrow(WorkItemTable.COLUMN_JOB_TYPE)) );
		workItem.setJobData(cursor.getString(cursor.getColumnIndexOrThrow(WorkItemTable.COLUMN_JOB_DATA)) );
		workItem.setInputTime(cursor.getString(cursor.getColumnIndexOrThrow(WorkItemTable.COLUMN_INPUT_TIME)) );
		workItem.setStepStatus(JobActionStepManager.JOB_STEP_NONE);
		
		return workItem;
	}
}
