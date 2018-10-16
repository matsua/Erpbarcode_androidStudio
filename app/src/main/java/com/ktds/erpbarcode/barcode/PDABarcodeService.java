package com.ktds.erpbarcode.barcode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.ProductInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.database.BpIItemQuery;

public class PDABarcodeService {

	private static final String TAG = "PDABarcodeService";

	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..
    
    //---------------------------------------------------------------
    // 자재 검색 분류
    //---------------------------------------------------------------
    public static final int SEARCH_LOCAL_PRODUCT = 100;
    // 자재정보
    public static final int SEARCH_EXPANSION_PRODUCT = 200;
    // 자재정보 + 자산분류
    public static final int SEARCH_EXPANSION_PRODUCT_AND_ASSETCLASS = 210;
    // 자재정보 ==> 바코드리스트인포(BarcodeListInfo)
    public static final int SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO = 220;   
    
    
    private BpIItemQuery bpIItemQuery;
    private final Handler mHandler;
    private LocalProductThread mLocalProductThread;
    private ServerProductThread mServerProductThread;
    private ServerProductInfoThread mServerProductInfoThread;		// 서버에서 자재정보 검색 Thread
    private int mState;
    
    public PDABarcodeService(Context context, Handler handler) {
        mState = STATE_NONE;
        mHandler = handler;
        bpIItemQuery = new BpIItemQuery(context);
		bpIItemQuery.open();
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
    
    public synchronized void search(int searchExpansion, String barcode) throws ErpBarcodeException {

    	String matnr = "";
    	String bismt = "";
 
    	if (searchExpansion == SEARCH_LOCAL_PRODUCT || searchExpansion == SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO) {
    		try {
        		int nine_idx = barcode.indexOf("9");

        		// 무선단말 바코드 : 18자리 - 앞의 10자리 잘라서 bismt 로 쿼리( 예: 094160010106022000)
        		// 유선단말 바코드 : 17자리 - 앞의 4자리 빼고 K 붙인 뒤 7자리 더해서 MATNR 쿼리 ( 예 :
        		// ASCS9074649000007 )
        		if (barcode.length() == 16 && !barcode.startsWith("K")) {
        			// 신바코드인데 16자리면서 K9으로 시작하지 않으면 무조건 앞에서 8자리가 자재코드
        			// FS16023000000002:처리할 수 없는 설비바코드입니다.
        			matnr = barcode.substring(0, 8);
        		} else if (barcode.length() == 18) {
        			 // 무선단말/기계장치(길이 18자리) 이면 BISMT 10자리로 조회 한다. 서버의 BP_ITEM의 LGCY_ITEM_CD = 단말의 BISMT
        			bismt = barcode.substring(0, 10);
        		} else if (barcode.length() == 17) {
        			// 유선단말(길이 17자리) 이면 MATNR 10자리로 조회한다. 서버의 BP_ITEM의 LGCY_ITEM_CD = 단말의 BISMT
        			if (barcode.substring(4, 5).equals("9")) {
        				matnr = "K" + barcode.substring(4, 4 + 7);
        			} else {
        				// 다섯번째 숫자가 9가 아니면 무조건 앞에서 8자리가 자재코드
        				matnr = barcode.substring(0, 8);
        			}
        		} else if (barcode.length() >= 16 && barcode.length() <= 18 && nine_idx > 0) {
        			// 장치 바코드 때문에 길이 10자리 이상으로 국한 시킴
        			matnr = "K" + barcode.substring(nine_idx, nine_idx + 7);
        		}
    		} catch (Exception e) {
    			throw new ErpBarcodeException(-1, "바코드를 자재코드로 변환중 오류가 발생했습니다. " + e.getMessage());
    		}
        	
        	if (matnr.isEmpty() && bismt.isEmpty()) {
        		throw new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다.");
        	}
    		
    	} else {
    		matnr = barcode;         	
    	}
    	
    	Log.i(TAG, "search    matnr==>"+matnr);
    	// Local SqlLite DB에서 자재정보를 조회한다.
    	if (searchExpansion == SEARCH_LOCAL_PRODUCT) {
    		if (mLocalProductThread == null) {
            	mLocalProductThread = new LocalProductThread(barcode, matnr, bismt);
            	mLocalProductThread.start();
            }
		// 서버에서 DB에서 자재정보를 조회한다.
    	} else if (searchExpansion == SEARCH_EXPANSION_PRODUCT 
    			|| searchExpansion == SEARCH_EXPANSION_PRODUCT_AND_ASSETCLASS) {
    		if (mServerProductThread == null) {
    			mServerProductThread = new ServerProductThread(searchExpansion, matnr, bismt);
    			mServerProductThread.start();
            }
    	// 서버에서 DB에서 자재정보를 조회한다.
    	} else if (searchExpansion == SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO) {
    		if (mServerProductInfoThread == null) {
    			mServerProductInfoThread = new ServerProductInfoThread(barcode, matnr, bismt);
    			mServerProductInfoThread.start();
            }
    	}
    }

    private class LocalProductThread extends Thread {
    	private String _Barcode;
    	private String _Matnr;
    	private String _Bismt;
    	
    	public LocalProductThread(String barcode, String matnr, String bismt) {
    		_Barcode = barcode;
    		_Matnr = matnr;
    		_Bismt = bismt;
    	}
        public void run() {
            Log.i(TAG, "LocalProductThread  Start...");
            List<ProductInfo> productInfos = null;
            try {
            	productInfos = bpIItemQuery.getProductInfosByMatnrAndBismt(_Matnr, _Bismt);

			} catch (Exception e) {
				handlerSendMessage(STATE_ERROR, "스마트폰에서 자재마스터 정보 조회중 오류가 발생했습니다.");
				return;
			}
            
            if (productInfos.size() <= 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "자재정보가 존재하지 않습니다.");
		        return;
			}
            
            ArrayList<BarcodeListInfo> barcodeListInfos = new ArrayList<BarcodeListInfo>();
            for (ProductInfo productInfo : productInfos ) {
            	BarcodeListInfo barcodeInfo = new BarcodeListInfo();
            	try {
					barcodeInfo = BarcodeInfoConvert.productInfoToBarcodeListInfo(productInfo);
					barcodeInfo.setBarcode(_Barcode);
				} catch (JSONException e) {
					Log.d(TAG, "LocalProductThread  자재마스터 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
					handlerSendMessage(STATE_ERROR, "자재마스터 자료 변환중 오류가 발생했습니다.");
	    			return;
				}
            	barcodeListInfos.add(barcodeInfo);
            }
            
            String jsonarrayMessage = BarcodeInfoConvert.barcodeInfosToJsonArrayString(barcodeListInfos);
            
            //-------------------------------------------------------
            // 조회 결과를 요청한 Handler로 전송한다.
            //-------------------------------------------------------
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonarrayMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            
            setState(STATE_SUCCESS);
        }
    }
    
    /**
     * 서버에서 자재정보를 조회하여 자재인포(List<ProductInfo>) 정보로 변환한다.
     * 
     * @param searchExpansion 조회 분류 조건 위 상수 참조바람.
     */
    private class ServerProductThread extends Thread {
		private int _SearchExpansion;
    	private String _Matnr;
    	private String _Bismt;

    	public ServerProductThread(int searchExpansion, String matnr, String bismt) {
    		_SearchExpansion = searchExpansion;
    		_Matnr = matnr;
    		_Bismt = bismt;
    	}
        public void run() {
        	
            List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
            try {
            	if (_SearchExpansion == SEARCH_EXPANSION_PRODUCT) {
            		BarcodeHttpController barcodehttp = new BarcodeHttpController();
    				productInfos = barcodehttp.getProductInfosServer(_Matnr, "", _Bismt);
            	} else if (_SearchExpansion == SEARCH_EXPANSION_PRODUCT_AND_ASSETCLASS) {
            		BarcodeHttpController barcodehttp = new BarcodeHttpController();
    				productInfos = barcodehttp.getProductInfosAndAssetClassInfoServer(_Matnr, "", _Bismt);
				}
            	if (productInfos == null) {
					throw new ErpBarcodeException(-1, "자재정보가 존재하지 않습니다.");
				}
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "ServerProductThread  서버에 자재마스터 조회 요청중 오류가 발생했습니다.  >>>>>"+e.getMessage());
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
            if (productInfos.size() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "자재정보가 존재하지 않습니다.");
		        return;
			}

            
            String jsonarrayMessage = BarcodeInfoConvert.productInfosToJsonArrayString(productInfos);
    		
            //-------------------------------------------------------
            // 조회 결과를 요청한 Handler로 전송한다.
            //-------------------------------------------------------
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonarrayMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            
            setState(STATE_SUCCESS);
        }
    }
    
    /**
     * 서버에서 자재정보를 조회하여 바코드리스트인포 정보로 변환한다.
     */
    private class ServerProductInfoThread extends Thread {
    	private String _Barcode;
    	private String _Matnr;
    	private String _Bismt;
    	
    	public ServerProductInfoThread(String barcode, String matnr, String bismt) {
    		_Barcode = barcode;
    		_Matnr = matnr;
    		_Bismt = bismt;
    	}
        public void run() {
            Log.i(TAG, "ServerBarcodeInfoThread  Start...");
            List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
            try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				productInfos = barcodehttp.getProductInfosServer(_Matnr, "", _Bismt);
				
				if (productInfos == null) {
					throw new ErpBarcodeException(-1, "자재정보가 존재하지 않습니다.");
				}
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "ServerBarcodeInfoThread  서버에 자재마스터 조회 요청중 오류가 발생했습니다.  >>>>>"+e.getMessage());
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
            if (productInfos.size() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "자재정보가 존재하지 않습니다.");
		        return;
			}
            
            ArrayList<BarcodeListInfo> barcodeListInfos = new ArrayList<BarcodeListInfo>();
            for (ProductInfo productInfo : productInfos ) {
            	BarcodeListInfo barcodeInfo = new BarcodeListInfo();
            	try {
					barcodeInfo = BarcodeInfoConvert.productInfoToBarcodeListInfo(productInfo);
					barcodeInfo.setBarcode(_Barcode);
				} catch (JSONException e) {
					Log.d(TAG, "ServerBarcodeInfoThread  자재마스터 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
					handlerSendMessage(STATE_ERROR, "자재마스터 자료 변환중 오류가 발생했습니다." );
	    			return;
				}
            	barcodeListInfos.add(barcodeInfo);
            }
            
            String jsonarrayMessage = BarcodeInfoConvert.barcodeInfosToJsonArrayString(barcodeListInfos);
    		
            //-------------------------------------------------------
            // 조회 결과를 요청한 Handler로 전송한다.
            //-------------------------------------------------------
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonarrayMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            
            setState(STATE_SUCCESS);
        }
    }
}
