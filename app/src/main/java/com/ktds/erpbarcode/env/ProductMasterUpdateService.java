package com.ktds.erpbarcode.env;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.database.BpIItemQuery;

public class ProductMasterUpdateService {
	private static final String TAG = "ProductMasterUpdateService";
	
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_MAX_PROGRESS = 3;
    public static final int STATE_INCREMENT_PROGRESS = 4;
    
    private BpIItemQuery bpIItemQuery;
    
    private final Handler mHandler;
    private TotalCountThread mTotalCountThread = null;
    
    private int mPageNumber;
    private String mWorkDttm;
    
    public ProductMasterUpdateService(Context context, Handler handler) {
    	bpIItemQuery = new BpIItemQuery(context);
		bpIItemQuery.open();
        mHandler = handler;
    }
    
    /**
     * ErpBarcodeException Class를 JSON String으로 에러 메지지 전송한다.
     * 나중에 사용할 예정.
     * 
     * @param errorMessage
     */
    public synchronized void handlerSendMessage(int state, ErpBarcodeException erpbarException) {
    	String jsonString = ErpBarcodeExceptionConvert.erpBarcodeExceptionToJsonString(erpbarException);
    	
    	Message msg = mHandler.obtainMessage(state);
		Bundle bundle = new Bundle();
		bundle.putString("message", jsonString);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    public synchronized void handlerSendMessage(int state, String errorMessage) {
    	handlerSendMessage(state, new ErpBarcodeException(-1, errorMessage));
    }


    /**
     * 자재마스터 업데이트 시작.
     * 
     * @param orgCode
     * @param workdttm
     */
    public synchronized void startUpdate(String orgCode, String workdttm) {

    	if (TextUtils.isEmpty(workdttm))
    		workdttm = "00000000000000000";
    	
        if (mTotalCountThread == null) {
        	mTotalCountThread = new TotalCountThread(orgCode, workdttm);
        	mTotalCountThread.start();
        }
    }
    
    private class TotalCountThread extends Thread {
    	private String _orgCode;
    	
    	public TotalCountThread(String orgCode, String workdttm) {
    		_orgCode = orgCode;
    		mWorkDttm = workdttm;
    	}
        public void run() {
        	JSONArray jsonResults = null;
        	try {
				BaseHttpController basehttp = new BaseHttpController();
				jsonResults = basehttp.getSurveyMaster(_orgCode, mWorkDttm, 1, 1);
				if (jsonResults == null) {
					throw new ErpBarcodeException(-1, "자재마스터 정보가 없습니다. ");
				}
			} catch (ErpBarcodeException e) {
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
        	
        	if (jsonResults.length() == 0) {
        		handlerSendMessage(STATE_ERROR, new ErpBarcodeException(-1, "추가된 업데이트 정보가 없습니다."));
				return;
			}
			// 무조건 1건만 전체건수만 가져온다.
			int totalCount = 0;
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(0);
				totalCount = Integer.valueOf(jsonobj.getString("totalCount"));
			} catch (JSONException e) {
				handlerSendMessage(STATE_ERROR, new ErpBarcodeException(-1, "자재마스터 변수에 대입중 오류가 발생했습니다."));
				return;
			}
            
			startProductMasterUpdate(totalCount);
        }
    }
    
	private void startProductMasterUpdate(int totalCount) {
		
		//-----------------------------------------------
		// 5000건으로 나누어서 나머지를 loop update 처리한다.
		//-----------------------------------------------
		mPageNumber = (totalCount / 5000) + 1;
		
		Log.i(TAG, "threadPoolExecutors  mPageNumber==>"+mPageNumber);
		
		//-------------------------------------------------------
        // progress max 정보를 보낸다.
        //-------------------------------------------------------
        Message msg = mHandler.obtainMessage(STATE_MAX_PROGRESS);
		Bundle bundle = new Bundle();
		bundle.putInt("count", mPageNumber);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

		// 처음일때는 Table 내용 모두 삭재한다.
		if (mWorkDttm.equals("00000000000000000")) {
			bpIItemQuery.deleteAllProductInfo();
			int totCount = bpIItemQuery.totalCount();
			Log.i(TAG, "threadPoolExecutors  bpIItemQuery.totCount==>"+totCount);
		}

		threadPoolExecutors(1);
	}
	
	private void threadPoolExecutors(int page) {
		Log.i(TAG, "threadPoolExecutors  page==>"+page);
		
		//-----------------------------------------------------------
		// 자재마스터 update page가 실행되었으면 여기서 마무리한다.
		//-----------------------------------------------------------
		if (mPageNumber < page) {
			Log.i(TAG, "threadPoolExecutors  Success");
			
            //-------------------------------------------------------
            // 조회 결과를 요청한 Handler로 전송한다.
            //-------------------------------------------------------
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
            mHandler.sendMessage(msg);
	    	
			return;
		}
		
		//-------------------------------------------------------
        // progress 진행 정보를 보낸다.
        //-------------------------------------------------------
        Message msg = mHandler.obtainMessage(STATE_INCREMENT_PROGRESS);
		Bundle bundle = new Bundle();
		bundle.putInt("count", 1);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        //-------------------------------------------------------
        // 다음 page 자재마스터를 update한다.
        //-------------------------------------------------------
        ProductMasterUpdateInTask updateTask= new ProductMasterUpdateInTask(page);
		updateTask.start();
	}
	
	public class ProductMasterUpdateInTask extends Thread {
		private int _Page;
		private JSONArray _JsonResults = null;
		
		public ProductMasterUpdateInTask(int page) {
			_Page = page;
		}
			
		@Override
		public void run() {
			Log.i(TAG, "ProductMasterUpdateInTask  Start...  _Page==>"+_Page);
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_JsonResults = basehttp.getSurveyMaster("", mWorkDttm, _Page, 5000);
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1, "자재마스터 정보가 없습니다.  _Page==>"+_Page);
				}
			} catch (ErpBarcodeException e) {
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
			
			if (_JsonResults.length() > 0) {
				try {
					bpIItemQuery.createBulkProductInfos(_JsonResults);
				} catch (ErpBarcodeException e) {
					handlerSendMessage(STATE_ERROR, e);
					return;
				}
			}
			
			Log.i(TAG, "ProductMasterUpdateInTask  End...  _Page==>"+_Page);
			
			int totCount = bpIItemQuery.totalCount();
			Log.i(TAG, "ProductMasterUpdateInTask  End...  bpIItemQuery.totCount==>"+totCount);
			
			//-----------------------------------------------------------------
			// 작업성공메시지 전송.
			//-----------------------------------------------------------------
			threadPoolExecutors(_Page+1);
		}
	}
}
