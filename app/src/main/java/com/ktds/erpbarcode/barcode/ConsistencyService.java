package com.ktds.erpbarcode.barcode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

import java.util.regex.Pattern;

public class ConsistencyService {

	private static final String TAG = "ConsistencyService";

	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..
    public static final int STATE_NOT_CONSISTENCY = -2;      // 오류발생..

    private final Handler mHandler;
    private ConsistencyThread mConsistencyThread;
    private int mState;

    public ConsistencyService(Handler handler) {
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
    
    public synchronized void search(String locId, String deviceId) {
    	boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", deviceId);
    	if(!flag){
    		handlerSendMessage(STATE_ERROR, "처리할 수 없는 장치바코드입니다.");
        	return;
    	}
        if (mConsistencyThread == null) {
        	deviceId = deviceId.toUpperCase();
            mConsistencyThread = new ConsistencyThread(locId, deviceId);
            mConsistencyThread.start();
        }
    }

    private class ConsistencyThread extends Thread {
        private String _LocId;
    	private String _DeviceId;
    	
    	public ConsistencyThread(String locId, String deviceId) {
    	    _LocId = locId;
    		_DeviceId = deviceId;
    	}
        public void run() {
            DeviceBarcodeInfo deviceInfo = null;
            try {
            	BarcodeHttpController barcodehttp = new BarcodeHttpController();
            	boolean check = barcodehttp.checkDeviceBarcodeData(_LocId, _DeviceId);
			} catch (ErpBarcodeException e) {
				handlerSendMessage(STATE_ERROR, e);
				return;
			}

            String jsonMessage = DeviceBarcodeConvert.deviceBarcodeInfoToJsonString(deviceInfo);

            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setState(STATE_SUCCESS);
        }
    }
}
