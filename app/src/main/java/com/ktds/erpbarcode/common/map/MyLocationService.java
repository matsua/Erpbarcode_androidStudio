package com.ktds.erpbarcode.common.map;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

public class MyLocationService {
	
	public static final int STATE_NONE = 0;        // we're doing nothing
    public static final int STATE_SUCCESS = 2;     // success
    public static final int STATE_ERROR = -1;      // error

	private LocationManager mLocationManager;
	private final Handler mHandler;
	
	private LocationListener mLocationListener = new LocationListener() {
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) { }
	    @Override
	    public void onProviderEnabled(String provider) { }
	    @Override
	    public void onProviderDisabled(String provider) { }
	    @Override
	    public void onLocationChanged(Location location) {
	        Log.i("Location", location.toString());
	        
	        LocationInfo locationInfo = new LocationInfo();
	        locationInfo.setLatitude(location.getLatitude());   // 위도.
	        locationInfo.setLongitude(location.getLongitude()); // 경도.
	        
	        handlerSendMessage(locationInfo);
	        
	        //-------------------------------------------------------
	        // 현재위치 결과 가져오면 requestLocationUpdates는 종료한다.
	        //-------------------------------------------------------
	        stopRequestLocation();
	    }
	};
	
	public MyLocationService(LocationManager locationManager, Handler handler) {
		mHandler = handler;
		// Get the location manager
		mLocationManager = locationManager;
	    //startRequestLocation();
	}
	
	public synchronized void startRequestLocation() {
		
		//Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
		//criteria.setAltitudeRequired(true);
		//criteria.setBearingRequired(true);
		//criteria.setSpeedRequired(true);
		//criteria.setCostAllowed(true);
		////criteria.setPowerRequirement(Criteria.POWER_HIGH);
		//criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		
		//String provider = mLocationManager.getBestProvider(criteria, true);
		//if (provider != null) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.001f, mLocationListener);
		//}
	}
	
	public synchronized void stopRequestLocation() {
		mLocationManager.removeUpdates(mLocationListener);
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
	
	public synchronized void handlerSendMessage(LocationInfo myLocationInfo) {
		
		String jsonString = LocationInfoConvert.locationInfoToJsonString(myLocationInfo);

		Message msg = mHandler.obtainMessage(STATE_SUCCESS);
		Bundle bundle = new Bundle();
		bundle.putString("message", jsonString);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
	}
}
