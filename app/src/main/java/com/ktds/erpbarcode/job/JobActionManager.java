package com.ktds.erpbarcode.job;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkInfoQuery;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.database.WorkItemQuery;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

public class JobActionManager {
	
	private static final String TAG = "JobActionManager";
	//---------------------------------------------------------------
	// 작업 상태.
	//---------------------------------------------------------------
	public static final int JOB_GENERAL = 1;
	public static final int JOB_WORKING = 2;
	public static final int JOB_APPEND = 3;
	

	private WorkInfoQuery mWorkInfoQuery;
	private WorkItemQuery mWorkItemQuery;
	
	private int mWorkStatus;
	private int mWorkId;
	private String mJobGubun;
	private final List<WorkItem> mWorkDataList = new ArrayList<WorkItem>();
	private JSONObject mItemParameter;

	private List<WorkItem> mStepItems;
	private JobActionStepManager mJobActionStepManager;

	public JobActionManager(Context context) {
		mWorkInfoQuery = new WorkInfoQuery(context);
		mWorkInfoQuery.open();
		mWorkItemQuery = new WorkItemQuery(context);
		mWorkItemQuery.open();
		
		mWorkId = GlobalData.getInstance().getWorkId();
		if (mWorkId > 0) {
			mWorkStatus = JOB_WORKING;
		} else {
			mWorkStatus = JOB_GENERAL;
		}
		
		initWorkData();
		initStepItem();
	}
	
	public int getWorkStatus() {
		return mWorkStatus;
	}

	public void setJobWorkMode(int status) {
		mWorkStatus = status;
		
		if (status == JOB_GENERAL) {
			mWorkId = 0;
			GlobalData.getInstance().setWorkId(0);
			initWorkData();
			mStepItems = null;
		} else if (status == JOB_APPEND) {
			initWorkData();
			initStepItem();
		}
	}

	private List<WorkItem> getWorkItemsByWorkId() throws ErpBarcodeException {
		List<WorkItem> workItems = null;
		try {
			workItems = mWorkItemQuery.getWorkItemsByWorkId(mWorkId);
		} catch(SQLiteException e) {
			throw new ErpBarcodeException(-1, e.getMessage());
        }
		return workItems;
	}
	
	public void initItemParameter() {
		mItemParameter = new JSONObject();
	}
	public void addItemParameter(String key, String value) throws ErpBarcodeException {
		try {
			if (!mItemParameter.isNull(key))
				throw new ErpBarcodeException(-1, "이미 등록된 키정보입니다. ");
			
			mItemParameter.put(key, value);
		} catch(JSONException e) {
        	Log.i(TAG, "JSONObject 정보 저장중 오류가 발생했습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "JSONObject 정보 저장중 오류가 발생했습니다. ");
        }
	}

	public void setStepItem(String jobType, String key, String value) throws ErpBarcodeException {
		initItemParameter();
		addItemParameter(key, value);
		setStepItem(jobType);
	}
	
	public void setStepItem(String jobType) throws ErpBarcodeException {
		if (mItemParameter == null)
			throw new ErpBarcodeException(-1, "저장할 정보가 없습니다. ");
		
		WorkItem workItem = new WorkItem();
		try {
			workItem.setId(0);
			workItem.setWorkId(0);
			workItem.setJobType(jobType);
			workItem.setJobData(mItemParameter.toString());
			workItem.setInputTime(null);
		} catch (Exception e) {
			throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
		}
		
		mWorkDataList.add(workItem);
	}
	
	private void initWorkData() {
		initItemParameter();
		mWorkDataList.clear();
	}
	
	public WorkInfo getThisWorkInfo() {
		return mWorkInfoQuery.getWorkInfoById(mWorkId);
	}

	public void saveWorkData(String locCd, String locName, String wbsNo, String deviceId, int itemCount, String offlineYn) throws ErpBarcodeException {
		//-----------------------------------------------------------
		// Device의 현재일시
		//-----------------------------------------------------------
		String sysDateTime = SystemInfo.getNowDateTime();
		
		mJobGubun = GlobalData.getInstance().getJobGubun();
		
		WorkInfo workInfo = new WorkInfo();
		try {
			workInfo.setId(mWorkId);
			workInfo.setWorkName(mJobGubun);
			workInfo.setLocCd(locCd);
			workInfo.setLocName(locName);
			workInfo.setWbsNo(wbsNo);
			workInfo.setDeviceId(deviceId);
			workInfo.setInputTime(sysDateTime);
			workInfo.setTranYn("N");
			workInfo.setTranTime(null);
			workInfo.setSrchYn("N");
			workInfo.setItemCount(itemCount);
			workInfo.setOfflineYn(offlineYn);
		} catch (Exception e) {
			throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
		}
		
		
		try {
			if (workInfo.getId() == 0) {
				workInfo = mWorkInfoQuery.createWorkInfo(workInfo);
			} else {
				WorkInfo tempWorkInfo = new WorkInfo();
				tempWorkInfo = mWorkInfoQuery.getWorkInfoById(workInfo.getId());
				if (tempWorkInfo == null)
					throw new ErpBarcodeException(-1, "존재하지 않는 작업ID입니다. ", BarcodeSoundPlay.SOUND_NOTEXISTS);
				
				workInfo = mWorkInfoQuery.updateWorkInfo(workInfo);
			}
		} catch (ErpBarcodeException e) {
			throw e;
		} catch (SQLiteException e) {
			throw new ErpBarcodeException(-1, "작업인포 생성/수정중 오류가 발생했습니다. " + e.getMessage());
		}
		
		//-----------------------------------------------------------
		// 등록한 순서로 처리하기 위해서..
		//-----------------------------------------------------------
		for (WorkItem workitem : mWorkDataList) {
			try {
				workitem.setId(0);
				workitem.setWorkId(workInfo.getId());
				//workitem.setJobType(jobKey);
				//workitem.setJobData(jobJsonObject.toString());
				workitem.setInputTime(sysDateTime);
			} catch (Exception e) {
				throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
			}
			
			try {
				workitem = mWorkItemQuery.createWorkItem(workitem);
			} catch (SQLiteException e) {
				throw new ErpBarcodeException(-1, "작업아이템 생성중 오류가 발생했습니다. " + e.getMessage());
			}
		}
		
		//-----------------------------------------------------------
		// 등록후 작업변수 데이터는 삭제한다.
		//-----------------------------------------------------------
		mWorkId = workInfo.getId();
		GlobalData.getInstance().setWorkId(workInfo.getId());
	}
	
	// Add by sesang 20190620 전송 후 작업관리 저장 데이터 추가 
	public void saveSendData(String traYn, String serverMsg, String locCd, String locName, String wbsNo, String deviceId, int itemCount, String offlineYn) throws ErpBarcodeException {
		
		if (mWorkId == 0) return;

		//-----------------------------------------------------------
		// Device의 현재일시
		//-----------------------------------------------------------
		String sysDateTime = SystemInfo.getNowDateTime();

		mJobGubun = GlobalData.getInstance().getJobGubun();

		Log.d(TAG, "saveWorkData : " + locCd + ", " + locName + ", " + deviceId);

		WorkInfo workInfo = new WorkInfo();
		try {
			workInfo.setId(mWorkId);
			workInfo.setWorkName(mJobGubun);
			workInfo.setLocCd(locCd);
			workInfo.setLocName(locName);
			workInfo.setWbsNo(wbsNo);
			workInfo.setDeviceId(deviceId);
			workInfo.setInputTime(sysDateTime);
			workInfo.setSrchYn("N");
			workInfo.setItemCount(itemCount);
			workInfo.setOfflineYn(offlineYn);
			workInfo.setTranYn(traYn);
			workInfo.setTranRslt(serverMsg);
			workInfo.setTranTime(sysDateTime);

		} catch (Exception e) {
			throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
		}


		try {
			if (workInfo.getId() == 0) {
				workInfo = mWorkInfoQuery.createWorkInfo(workInfo);
			} else {
				WorkInfo tempWorkInfo = new WorkInfo();
				tempWorkInfo = mWorkInfoQuery.getWorkInfoById(workInfo.getId());
				if (tempWorkInfo == null)
					throw new ErpBarcodeException(-1, "존재하지 않는 작업ID입니다. ", BarcodeSoundPlay.SOUND_NOTEXISTS);

				workInfo = mWorkInfoQuery.updateWorkInfo(workInfo);
			}
		} catch (ErpBarcodeException e) {
			throw e;
		} catch (SQLiteException e) {
			throw new ErpBarcodeException(-1, "작업인포 생성/수정중 오류가 발생했습니다. " + e.getMessage());
		}

		//-----------------------------------------------------------
		// 등록한 순서로 처리하기 위해서..
		//-----------------------------------------------------------
		for (WorkItem workitem : mWorkDataList) {
			try {
				workitem.setId(0);
				workitem.setWorkId(workInfo.getId());
				//workitem.setJobType(jobKey);
				//workitem.setJobData(jobJsonObject.toString());
				workitem.setInputTime(sysDateTime);
			} catch (Exception e) {
				throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
			}

			try {
				workitem = mWorkItemQuery.createWorkItem(workitem);
			} catch (SQLiteException e) {
				throw new ErpBarcodeException(-1, "작업아이템 생성중 오류가 발생했습니다. " + e.getMessage());
			}
		}

		//-----------------------------------------------------------
		// 등록후 작업변수 데이터는 삭제한다.
		//-----------------------------------------------------------
		mWorkId = workInfo.getId();
		GlobalData.getInstance().setWorkId(workInfo.getId());

		//-----------------------------------------------------------
		// 등록후 작업변수 데이터는 삭제한다.
		//-----------------------------------------------------------
		mWorkId = 0;
		GlobalData.getInstance().setWorkId(0);

	}
	// End sesang
	
	/**
	 * 작업아이템들 모두 삭제한다.
	 *
	 * @param workId
	 */
	public void deleteWorkItemsByWorkId(int workId) {
		mWorkItemQuery.deleteWorkItemsByWorkId(workId);
	}

	
	public void saveSendData(String traYn, String serverMsg) throws ErpBarcodeException {

		if (mWorkId == 0) return;

		//-----------------------------------------------------------
		// Device의 현재일시
		//-----------------------------------------------------------
		String sysDateTime = SystemInfo.getNowDateTime();


		WorkInfo workInfo = new WorkInfo();
		workInfo = mWorkInfoQuery.getWorkInfoById(mWorkId);
		if (workInfo == null) {
			throw new ErpBarcodeException(-1, "작업인포 조회 결과가 없습니다. ");
		}

		try {
			workInfo.setTranYn(traYn);
			workInfo.setTranRslt(serverMsg);
			workInfo.setTranTime(sysDateTime);
		} catch (Exception e) {
			throw new ErpBarcodeException(-1, "변수 대입중 오류가 발생했습니다. " + e.getMessage());
		}

		try {
			workInfo = mWorkInfoQuery.updateWorkInfo(workInfo);
		} catch (SQLiteException e) {
			throw new ErpBarcodeException(-1, "작업인포 수정중 오류가 발생했습니다. " + e.getMessage());
		}

		//-----------------------------------------------------------
		// 등록후 작업변수 데이터는 삭제한다.
		//-----------------------------------------------------------
		mWorkId = 0;
		GlobalData.getInstance().setWorkId(0);
	}

	/****************************************************************
	 * 여기서부터 작업아이템 단계별 작업관리.
	 */
	private void initStepItem() {
		try {
			mStepItems = getWorkItemsByWorkId();
		} catch (ErpBarcodeException e) {
			e.printStackTrace();
		}
	}
	public WorkItem startStepItem(int position, Handler jobHandler) {
		WorkItem workItem = mStepItems.get(position);
		if (workItem == null) return null; 
		
		mJobActionStepManager = new JobActionStepManager(workItem, jobHandler);
		
		return mStepItems.get(position);
	}
	public JobActionStepManager getJobStepManager() {
		return mJobActionStepManager;
	}
	
	public int getStepWorkCount() {
		return mStepItems.size();
	}
	public String getJsonStepValue(String stepJsonString, String stepKey) {
    	String value = "";
    	try {
			JSONObject jsonObj = new JSONObject(stepJsonString);
			value = jsonObj.getString(stepKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return value;
    }

	/**
	 * 여기까지 작업아이템 단계별 작업관리.
	 ****************************************************************/

}
