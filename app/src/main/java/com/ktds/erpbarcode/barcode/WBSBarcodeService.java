package com.ktds.erpbarcode.barcode;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.barcode.model.WBSConvert;
import com.ktds.erpbarcode.barcode.model.WBSInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

public class WBSBarcodeService {

	private static final String TAG = "WBSBarcodeService";
	
	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..

    private final Handler mHandler;
    private WBSThread mWBSThread;
    private int mState;
    
    public WBSBarcodeService(Handler handler) {
        mHandler = handler;
        mState = STATE_NONE;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    public synchronized int getState() {
        return mState;
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
        setState(STATE_ERROR);
    }
    public synchronized void handlerSendMessage(int state, String errorMessage) {
    	handlerSendMessage(state, new ErpBarcodeException(-1, errorMessage));
    }

    
    public synchronized void search(String locCd, String workCat) {
        if (mWBSThread == null) {
        	mWBSThread = new WBSThread(locCd, workCat);
        	mWBSThread.start();
        }
    }

    private class WBSThread extends Thread {
    	private String _LocCd;
    	private String _WorkCat;
    	private List<WBSInfo> _WBSInfos = null;
    	
    	public WBSThread(String locCd, String workCat) {
    		_LocCd = locCd;
    		_WorkCat = workCat;
    	}
    	
        public void run() {
            try {
            	LocationHttpController lochttp = new LocationHttpController();
            	_WBSInfos = lochttp.getWbsInfos(_LocCd, _WorkCat);
				
				if (_WBSInfos == null) {
					throw new ErpBarcodeException(-1, "WBS 조회 결과가 없습니다. ");
				}
			} catch (ErpBarcodeException e) {
				Log.e(TAG, "WBSThread  서버로 WBS정보 조회 요청중 오류가 발생했습니다.==>" + e.getErrMessage());
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
            if (_WBSInfos.size() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "조회 건수가 0건입니다.");
		        return;
			}

            String jsonMessage = WBSConvert.wbsInfosToJsonArrayString(_WBSInfos);
    		
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setState(STATE_SUCCESS);
        }
    }
}
