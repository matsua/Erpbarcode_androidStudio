package com.ktds.erpbarcode.management;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.DeviceBarcodeService;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

public class AddrInfoActivity extends Activity {
	private JSONObject addrInfo;
	private AddressLocInTask mAddressLocInTask;
	public static final String INPUT_ADDR_BARCD = "addr_barCd";
	public static final String INPUT_ADDR_BARNM = "addr_barNm";
	public static final String INPUT_GUBUN = "addr_gubun";
	private String p_addrCd = "";
	private String p_addrNm = "";
	private String p_gubun = "";
	
	private EditText text2, text3, text4;
	private TextView text1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        
        p_addrCd = getIntent().getStringExtra(INPUT_ADDR_BARCD);
        p_addrNm = getIntent().getStringExtra(INPUT_ADDR_BARNM);
        p_gubun = getIntent().getStringExtra(INPUT_GUBUN);
        
        setContentView(R.layout.addinfo_activity);
        
        text1 = (TextView)findViewById(R.id.textView1);
        text2 = (EditText)findViewById(R.id.editText2);
        text3 = (EditText)findViewById(R.id.editText3);
        text4 = (EditText)findViewById(R.id.editText4);
        
        Button close = (Button)findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				finish();
		     }
	     });
        
        if(p_addrCd.length() < 10){
        	getDeviceBarcodeInfo(p_addrCd);
        	return;
        }

        getLocAddInfo(p_addrCd, p_gubun);
	}
	
	public void getLocAddInfo(String locCd, String gubun) {
		if (mAddressLocInTask == null) {
			String locInqCd = locCd.substring(0,11);
			mAddressLocInTask = new AddressLocInTask(locInqCd);
			mAddressLocInTask.execute((Void) null);
		}
	}
	
	public class AddressLocInTask extends AsyncTask<Void, Void, Boolean> {
		private String _locCd = "";
		private ErpBarcodeException _ErpBarException;
		
		public AddressLocInTask(String locCd) {
			_locCd = locCd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			addrInfo = new JSONObject();
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				addrInfo = locationhttp.getLocBarcodeAdd(_locCd);
				
				if (addrInfo == null) {
					throw new ErpBarcodeException(-1, "장치바코드-위치코드 정보가 없습니다. ");
				}
			} 
			catch (ErpBarcodeException e) {
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mAddressLocInTask = null;
			super.onPostExecute(result);
			if (result) {
				try {
					text1.setText(p_addrCd);
					text2.setText(p_addrNm);
					text3.setText(addrInfo.getString("legalAddr").toString());
					text4.setText(addrInfo.getString("loadAddr").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} 
			else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mAddressLocInTask = null;
			super.onCancelled();
		}
	}
	

	/**
	 * 장치바코드 정보 조회.
	 */
	private void getDeviceBarcodeInfo(String deviceId) {
		DeviceBarcodeService devicebarcodeService = new DeviceBarcodeService(new DeviceBarcodeHandler());
		devicebarcodeService.search(deviceId);
	}
	
    private class DeviceBarcodeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DeviceBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	DeviceBarcodeInfo deviceInfo = DeviceBarcodeConvert.jsonStringToDeviceBarcodeInfo(findedMessage);
                
                successDeviceBarcodeProcess(deviceInfo);
        		
                break;
            case DeviceBarcodeService.STATE_NOT_FOUND:
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다."));
            	break;
            case DeviceBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void successDeviceBarcodeProcess(DeviceBarcodeInfo deviceBarcodeInfo) {
    	p_addrCd = deviceBarcodeInfo.getLocationCode();
    	p_addrNm = deviceBarcodeInfo.getLocationShortName();
    	
    	getLocAddInfo(p_addrCd, p_addrNm);
        
    }
}
