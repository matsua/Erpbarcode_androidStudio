package com.ktds.erpbarcode.barcode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.management.model.TransferHttpController;

public class TransferBarcodeService {

	private static final String TAG = "TransferBarcodeService";

	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..
    public static final int STATE_ERROR_2 = -2;    // 오류발생..
    
    private final Handler mHandler;
    private TransferBarcodeThread mTransferBarcodeThread;
    private TransferSubBarcodeThread mTransferSubBarcodeThread;
    private int mState;
    
    public TransferBarcodeService(Handler handler) {
        mState = STATE_NONE;
        mHandler = handler;
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
    
    public synchronized void search(String locCd, String wbsNo, boolean isReScan) {
    	Log.i(TAG, "search Start..   locCd==>"+locCd);
    	Log.i(TAG, "search Start..   wbsNo==>"+wbsNo);
 
    	if (isReScan) {
    		if (mTransferSubBarcodeThread == null) {
    			mTransferSubBarcodeThread = new TransferSubBarcodeThread(locCd, wbsNo);
    			mTransferSubBarcodeThread.start();
            }
    	} else {
    		if (mTransferBarcodeThread == null) {
    			mTransferBarcodeThread = new TransferBarcodeThread(locCd, wbsNo);
    			mTransferBarcodeThread.start();
            }
    	}
    }
    
    /**
     * 인계대상 조회.
     */
    private class TransferBarcodeThread extends Thread {
    	String _locCd;
    	String _wbsNo;
    	
    	public TransferBarcodeThread(String locCd, String wbsNo) {
    		_locCd = locCd;
    		_wbsNo = wbsNo;    		
    	}
        public void run() {
            Log.i(TAG, "TransferBarcodeThread  Start...");
            JSONArray jsonResults = null;
            try {
            	TransferHttpController transferhttp = new TransferHttpController();
            	if (GlobalData.getInstance().getJobGubun().equals("인계")) {
            		jsonResults = transferhttp.getTransferScanData(_locCd, _wbsNo);
            		if (jsonResults == null) {
    					throw new ErpBarcodeException(-1, "인계대상 조회 결과가 없습니다. ");
    				}
            	
            	} else if (GlobalData.getInstance().getJobGubun().equals("인수")) {
            		jsonResults = transferhttp.getArgumentScanData(_locCd, _wbsNo);
            		if (jsonResults == null) {
    					throw new ErpBarcodeException(-1, "인수대상 조회 결과가 없습니다. ");
    				}
            	
            	} else if (GlobalData.getInstance().getJobGubun().equals("시설등록")) {
            		jsonResults = transferhttp.getInstConfScanData(_locCd, _wbsNo);
            		if (jsonResults == null) {
    					throw new ErpBarcodeException(-1, "시설등록대상 조회 결과가 없습니다. ");
    				}
            	}

			} catch (ErpBarcodeException e) {
				Log.d(TAG, "TransferBarcodeThread  서버에 " +GlobalData.getInstance().getJobGubun()+ "대상 조회 요청중 오류가 발생했습니다.  >>>>>"+e.getMessage());
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
			if (jsonResults.length() == 0) {
				handlerSendMessage(STATE_NOT_FOUND, "조회 건수가 0건입니다.");
		        return;
			}

            List<BarcodeListInfo> barcodeInfos = new ArrayList<BarcodeListInfo>();
            String oldDeviceId = "";
            for (int i=0;i<jsonResults.length();i++) {
        		try {
	            	JSONObject jsonobj = jsonResults.getJSONObject(i);
	            	
	            	BarcodeListInfo barcodeInfo = BarcodeInfoConvert.jsonToTransferBarcodeListInfo(jsonobj);
	            	if (!oldDeviceId.equals(barcodeInfo.getDeviceId())) {
	            		DeviceBarcodeInfo deviceInfo = null;
	                    try {
	                    	BarcodeHttpController barcodehttp = new BarcodeHttpController();
	                    	deviceInfo = barcodehttp.getDeviceBarcodeData(barcodeInfo.getDeviceId());
	        			} catch (ErpBarcodeException e) {
	        				Log.e(TAG, "DeviceUpdateInTask  서버로 장치바코드 조회 요청중 오류가 발생했습니다.==>" + e.getErrMessage());
	        				//Modify by sesang 20190425 리스 자산 처리 : 위치 바코드 비 활성화 시 오류 메시지 수정 및 break -> continue
	        				if (GlobalData.getInstance().getJobGubun() != null && GlobalData.getInstance().getJobGubun().equals("시설등록")) {
	        					String message = e.getErrMessage();
	        					if (message != null && message.contains("활성화 되어 있지 않아 스캔이 불가합니다.")) {	        						
	        						handlerSendMessage(STATE_ERROR_2, message);
	        					} else {
	        						handlerSendMessage(STATE_ERROR, "'" + GlobalData.getInstance().getJobGubun() + "' 장치바코드 조회 요청중 오류가 발생했습니다." );
	        					}
	        					continue;
	        				} else {	        					
	    	        			handlerSendMessage(STATE_ERROR, "'" + GlobalData.getInstance().getJobGubun() + "' 장치바코드 조회 요청중 오류가 발생했습니다." );
	    	        			break;
	        				}
	        			}
	                    // Modify by sesang 20190425 리스 자산 처리 : 위치 바코드 비 활성화 시 오류 메시지 수정 및 break -> continue
	                    if (deviceInfo == null) {
	                    	if (GlobalData.getInstance().getJobGubun().equals("시설등록")) {
	                    		handlerSendMessage(STATE_ERROR, "'" + GlobalData.getInstance().getJobGubun() + "' 장치바코드 조회 결과가 없습니다." );
	        					continue;
	        				} else {
	        					handlerSendMessage(STATE_ERROR, "'" + GlobalData.getInstance().getJobGubun() + "' 장치바코드 조회 결과가 없습니다." );
	        					continue;
	        				}        					
        				}
	                    
	                    String UFacBarcode = _locCd;
	                    BarcodeListInfo deviceBarcode = new BarcodeListInfo(GlobalData.getInstance().getJobGubun(), "D", deviceInfo.getDeviceId(), deviceInfo.getDeviceName(), UFacBarcode, 
	                			deviceInfo.getItemCode(), deviceInfo.getItemName(), deviceInfo.getDeviceStatusCode(), deviceInfo.getDeviceStatusName(), 
	                			deviceInfo.getLocationCode(), barcodeInfo.getWbsNo(), deviceInfo.getOperationSystemCode());
	            	
	                    barcodeInfos.add(deviceBarcode);
	            	}
	            	
	            	// 만약 상위바코드가 없으면.
                    if (barcodeInfo.getuFacCd().isEmpty()) {
                    	barcodeInfo.setuFacCd(barcodeInfo.getDeviceId());
	        		}

                    //---------------------------------------------------------
                    // 화면에 보여주기 위해서 운용조직 여부 = "Y"
                    //---------------------------------------------------------
                    barcodeInfo.setCheckOrgYn("Y");
                    
	            	barcodeInfos.add(barcodeInfo);
	            	
	            	// 이전 장치바코드ID
	            	oldDeviceId = barcodeInfo.getDeviceId();
        		} catch (JSONException e) {
        			Log.d(TAG, "TransferBarcodeThread  인계대상 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
					handlerSendMessage(STATE_ERROR, "'" + GlobalData.getInstance().getJobGubun() + "' 대상 자료 변환중 오류가 발생했습니다." );
	    			return;
        		}
            }
            
            if (barcodeInfos.size() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "조회 건수가 0건입니다.");
		        return;
			}
            
            String jsonarrayMessage = BarcodeInfoConvert.barcodeInfosToJsonArrayString(barcodeInfos);

            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonarrayMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setState(STATE_SUCCESS);
        }
    }

    /**
     * 인계대상 재스캔요청정보(서브결과) 조회.
     */
    private class TransferSubBarcodeThread extends Thread {
    	String _locCd;
    	String _wbsNo;
    	
    	public TransferSubBarcodeThread(String locCd, String wbsNo) {
    		_locCd = locCd;
    		_wbsNo = wbsNo;
    	}
        public void run() {
            Log.i(TAG, "TransferSubBarcodeThread  Start...");
            JSONArray jsonResults = null;
            try {
            	TransferHttpController transferhttp = new TransferHttpController();
            	jsonResults = transferhttp.getTransferScanSubResultsData(_locCd, _wbsNo);
				
				if (jsonResults == null) {
					throw new ErpBarcodeException(-1, "인계대상 조회 결과가 없습니다. ");
				}
				
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "TransferBarcodeThread  서버에 인계대상 조회 요청중 오류가 발생했습니다.  >>>>>"+e.getMessage());
				handlerSendMessage(STATE_ERROR, e.getErrMessage());
				return;
			}
            
            if (jsonResults.length() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "조회 건수가 0건입니다.");
		        return;
			}

            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonResults.toString());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            setState(STATE_SUCCESS);
        }
    }

}
