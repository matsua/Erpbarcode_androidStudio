package com.ktds.erpbarcode.barcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

public class LocBarcodeService {

	private static final String TAG = "LocBarcodeService";
	
	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..

    private Geocoder mGeocoder = null;
    private final Handler mHandler;
    private LocBarcodeThread mLocBarcodeThread = null;
    private int mState;
    private String mJobGubun;
    
    public LocBarcodeService(Handler handler) {
    	mJobGubun = GlobalData.getInstance().getJobGubun();
        mHandler = handler;
        mState = STATE_NONE;
    }
    
    public LocBarcodeService(Context context, Handler handler) {
    	mJobGubun = GlobalData.getInstance().getJobGubun();
    	mGeocoder = new Geocoder(context, Locale.KOREA);
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
    
    public synchronized void search(String locCd) {
    	locCd = locCd.toUpperCase();
    	
    	boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", locCd);
    	if(!flag){
    		handlerSendMessage(STATE_ERROR, "처리할 수 없는 위치바코드입니다.");
        	return;
    	}
    	
    	// 베이 위치 또는 P 위치로는 송부취소(팀간) 불가 처리
    	if (mJobGubun.equals("송부취소(팀간)")) {
            if (locCd.startsWith("P") || locCd.length() >= 17 && !locCd.substring(17).equals("0000"))
            {
            	handlerSendMessage(STATE_ERROR, "'베이' 또는 'P' 위치로는\n\r'" + mJobGubun + "'\n\r작업을 하실 수 없습니다.");
            	return;
            }
    	}
    	if (mJobGubun.equals("납품입고") || mJobGubun.equals("입고(팀내)") 
    			|| mJobGubun.equals("접수(팀간)") || mJobGubun.equals("부외실물등록요청") 
    			|| mJobGubun.equals("수리완료") || mJobGubun.equals("개조개량완료") 
    			|| mJobGubun.equals("고장등록취소") || mJobGubun.equals("수리의뢰취소") 
    			|| mJobGubun.equals("개조개량의뢰취소") || mJobGubun.equals("개조개량완료") || mJobGubun.equals("형상구성(창고내)")) {
        	if (!locCd.startsWith("VS") && locCd.length() > 18 && !locCd.substring(17).equals("0000")) {
        		handlerSendMessage(STATE_ERROR, "'베이' 위치로는 '" + mJobGubun + "'\n\r작업을 하실 수 없습니다.");
                return;
        	}
        }
    	
    	// Start the thread to listen on a BluetoothServerSocket
        if (mLocBarcodeThread == null) {
        	mLocBarcodeThread = new LocBarcodeThread(locCd);
        	mLocBarcodeThread.start();
        }
    }

    private class LocBarcodeThread extends Thread {
    	private String _LocCd;
    	
    	public LocBarcodeThread(String locCode) {
    		_LocCd = locCode;
    	}
        public void run() {
            Log.i(TAG, "LocBarcodeThread   Start...");
            LocBarcodeInfo locInfo = new LocBarcodeInfo();
            try {
            	LocationHttpController locationhttp = new LocationHttpController();
            	locInfo = locationhttp.getLocBarcodeData(_LocCd);
				//---------------------------------------------------
				// 단지 위치바코드 있는지 없는지 체크하고 SAP메시지 출력을 위해서 호출함.
				//  이건 뭐하는 소스인지..  참나~~
				//---------------------------------------------------
            	if(!mJobGubun.equals("형상구성(창고내)")){
            		locationhttp.getLocBarcodeCheckData(_LocCd);
            	}
			} catch (ErpBarcodeException e) {
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
            if (locInfo == null) {
            	handlerSendMessage(STATE_NOT_FOUND, "위치바코드 조회 정보가 없습니다. ");
				return;
			}
            
    		// 1. 인계/인수 : MDM에서 I/F 받은 전체 위치 코드에 대해서만 허용(ERPBarcode에서 생성한 가상창고 불허)
            // 2012.11.12 - 철거시 가상창고 스캔 못하게.. request by 박장수
            if (mJobGubun.equals("인계") || mJobGubun.equals("인수") 
            		|| mJobGubun.equals("시설등록") || mJobGubun.equals("철거"))
            {
                if (locInfo.getLocCd().startsWith("VS")) {
                	handlerSendMessage(STATE_ERROR, "가상창고 위치바코드는\n\r스캔하실 수 없습니다.");
                    return;
                }
            }
    		// 2. 시설등록 : MDM에서 I/F 받은 전체 위치 코드에 대해서만 허용(ERPBarcode에서 생성한 가상창고 불허)
    		// 3. 개조개량 의뢰 : ERPBarcode에 서 생성한 가상위치 중 창고유형 ‘06’에 대해서만 허용(다른 Type 불허)
    		// 4. 개조개량 완료 : 초기화면은 멀티입고(입고화면), 사용자사 Scan한 위치가 MDM에서 I/F 받은 위치이면 실장 화면
    		// 그렇지 않고 ERPBarcode에서 생성한 가상창고이면 멀티입고(입고화면)(단, 창고유형 ‘04’, ‘05’ 만 허용)
    		if  (mJobGubun.equals("개조개량의뢰")) {
    			if (!locInfo.getRoomTypeCode().equals("06")) {
    				handlerSendMessage(STATE_ERROR, "'업체창고' 위치바코드를\n\r스캔하세요. ");
    				return;
    			}

    		} else if (mJobGubun.equals("입고(팀내)")) {
                // 팀내입고 스캔 : 현재) 가상창고 위치코드만 스캔 가능 	운용위치코드도 스캔 가능하게 테스트 환경 요청 - request by 박장수 2013.0.28
                // ##msg  QA에서는 운용위치코드도 허용
    			/* 
    			if (!mThisLocCodeInfo.getRoomTypeCode().equals("04")
    					&& !mThisLocCodeInfo.getRoomTypeCode().equals("05")
    					&& !mThisLocCodeInfo.getRoomTypeCode().equals("06")) {
    				Log.d(TAG, "'KT창고' 또는 '재활용창고'\n\r또는 '업체창고' 위치바코드를\n\r스캔하세요.");
    				showMessageDialog("'KT창고' 또는 '재활용창고'\n\r또는 '업체창고' 위치바코드를\n\r스캔하세요.");
    				return;
    			} */

    		} else if (mJobGubun.equals("개조개량완료") || mJobGubun.equals("수리완료")
    				|| mJobGubun.equals("접수(팀간)")) {
                //4. 개조개량 완료 : 초기화면은 멀티입고(입고화면), 사용자사 Scan한 위치가 MDM에서 I/F 받은 위치이면 실장 화면
                //그렇지 않고 ERPBarcode에서 생성한 가상창고이면 멀티입고(입고화면)(단, 창고유형 ‘04’, ‘05’ 만 허용
                // '06' 업체창고 추가 - request by 박장수 2012.07.10
    			if (locInfo.getLocCd().startsWith("VS")) {
    				if ((!locInfo.getRoomTypeCode().equals("04")) && 
    					(!locInfo.getRoomTypeCode().equals("05")) && 
    					(!locInfo.getRoomTypeCode().equals("06")) ) {
    					handlerSendMessage(STATE_ERROR, "KT창고 & 재활용창고 \n위치바코드를 스캔하세요. ");
    					return;
    				}
    			}
    		}
    		
    		//-------------------------------------------------------
    		// 위치바코드 주소로 GPS(위도,경도)정보 조회.
    		//-------------------------------------------------------
    		if (mGeocoder != null) {
    			String addressName = locInfo.getLocationFullName();
    			addressName = addressName.replace("[토지]", "");
    			addressName = addressName.replace("[일반]", "");
    			addressName = addressName.replace("[건물]", "");
    			addressName = addressName.replace("[비건물]", "");
    			addressName = addressName.replace("[나대지]", "");
    			addressName = addressName.replace("상가주택", "");
    			
    			String[] arr_addressNamme = addressName.split(" ");
    			
    			if (arr_addressNamme.length>3) {
    				addressName = arr_addressNamme[0]+" "+arr_addressNamme[1]+" "+arr_addressNamme[2]+" "+arr_addressNamme[3];
    			}
    			else if (arr_addressNamme.length>2) {
    				addressName = arr_addressNamme[0]+" "+arr_addressNamme[1]+" "+arr_addressNamme[2];
    			}
    			
    			List<Address> geoAddress = new ArrayList<Address>();
    			try {
    				geoAddress = mGeocoder.getFromLocationName(addressName, 1);
    			} catch (IOException e) {
    				//handlerSendMessage(STATE_ERROR, "주소로 GPS(위도,경도)정보 조회중 오류가 발생했습니다. ");
    				e.printStackTrace();
    			}

    			
    			double currentLatitude = SessionUserData.getInstance().getLatitude();
    			double currentLongitude = SessionUserData.getInstance().getLongitude();
    			if (geoAddress != null && geoAddress.size() > 0 
    					&& currentLatitude != 0 && currentLongitude != 0) {
    				locInfo.setLatitude(geoAddress.get(0).getLatitude());
    				locInfo.setLongitude(geoAddress.get(0).getLongitude());
    				
    				//---------------------------------------------------------
    				// 현재위치와 위치바코드의 위치의 거리차를 구한다.
    				//---------------------------------------------------------
    				double diffTitude = getDistance(currentLatitude, currentLongitude, locInfo.getLatitude(), locInfo.getLongitude());
    				locInfo.setDiffTitude(diffTitude);
    			}
    		}
    		//-------------------------------------------------------
    		
    		
    		//-------------------------------------------------------
    		// 성공 메시지를 요청한데로 전송한다.
    		//-------------------------------------------------------
    		String jsonMessage = LocBarcodeConvert.locBarcodeInfoToJsonString(locInfo);


            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            setState(STATE_SUCCESS);
        }
    }
    
    /**
     * 두개의 GPS POINT의 거리차를 산출한다.
     * 
     * @param fromLatitude
     * @param fromLongitude
     * @param toLatitude
     * @param toLongitude
     * @return
     */
    public double getDistance(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
		double from_lat = fromLatitude;
		double from_lng = fromLongitude;
		double to_lat = toLatitude;
		double to_lng = toLongitude;
		
	    double R = 6371.01; // earth’s radius (mean radius = 6,371km)
	    double dLat =  Math.toRadians(to_lat-from_lat);

	    double dLon =  Math.toRadians(to_lng-from_lng); 
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(from_lat)) * Math.cos(Math.toRadians(to_lat)) * 
	               Math.sin(dLon/2) * Math.sin(dLon/2); 
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	    double dr1 = R * c; //in radians      

	    return dr1;
	}
}
