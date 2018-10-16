package com.ktds.erpbarcode.job;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ktds.erpbarcode.common.database.WorkItem;

public class JobActionStepManager {
	
	//private static final String TAG = "JobActionStepManager";
	
	//---------------------------------------------------------------
	// 작업아이템 단계별 작업관리.
	//---------------------------------------------------------------
	public static final String JOBTYPE_INITIAL_TASK = "initial_task";   // Activity 생성시 최초 작업. 
	public static final String JOBTYPE_ORG = "org";
	public static final String JOBTYPE_RECEIPT_ORG = "receipt_org";
	public static final String JOBTYPE_LOC = "loc";
	public static final String JOBTYPE_SEARCH_TASK = "search_task";     // 조회 작업. 
	public static final String JOBTYPE_WBS = "wbs";
	public static final String JOBTYPE_DEVICE = "device";
	public static final String JOBTYPE_FAC = "fac";
	public static final String JOBTYPE_UU_CHECK = "uu_check";
	public static final String JOBTYPE_FAC_STATUS = "fac_status";
	public static final String JOBTYPE_UFAC = "ufac";
	public static final String JOBTYPE_SEARCH = "search";
	public static final String JOBTYPE_CANCEL_SCAN = "cancel_scan";
	public static final String JOBTYPE_DELETE = "delete";
	public static final String JOBTYPE_CHK_SCAN = "chk_scan";
	public static final String JOBTYPE_MOVE = "move";
	public static final String JOBTYPE_EDIT = "edit";
	public static final String JOBTYPE_HIERACHY_CHECK = "hierachy_check";
	public static final String JOBTYPE_CBO_STATUS = "cbo_status";            // 고장코드
	public static final String JOBTYPE_CBO_REASON_TEXT = "cbo_reason_text";  // 고장내역
	
	public static final String JOBTYPE_SN_CHANGE = "sn_change";				 // 설비S/N변경
	
	
	//---------------------------------------------------------------
	// 작업아이템 단계별 상태관리.
	//---------------------------------------------------------------
	public static final int JOB_STEP_NONE = 1;
	public static final int JOB_STEP_FINISHED = 2;
	public static final int JOB_STEP_ERROR = 3;
	
	
	private WorkItem mStepWorkItem;
	private final Handler mJobHandler;
	
	public JobActionStepManager(WorkItem workItem, final Handler handler) {
		mStepWorkItem = workItem;
		mJobHandler = handler;
	}
	
	public WorkItem getStepWorkItem() {
		return mStepWorkItem;
	}

	public void finishedHandler() {
		
		String jsonString = WorkItemConvert.WorkItemToJsonString(mStepWorkItem);
		
		Message msg = mJobHandler.obtainMessage(JOB_STEP_FINISHED);
		Bundle bundle = new Bundle();
		bundle.putString("message", jsonString);
        msg.setData(bundle);
        mJobHandler.sendMessage(msg);
	}
	
	public void errorHandler() {
		String jsonString = WorkItemConvert.WorkItemToJsonString(mStepWorkItem);
		
		Message msg = mJobHandler.obtainMessage(JOB_STEP_ERROR);
		Bundle bundle = new Bundle();
		bundle.putString("message", jsonString);
        msg.setData(bundle);
        mJobHandler.sendMessage(msg);
	}
}
