package com.ktds.erpbarcode.env.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.encryption.Caesar;
import com.ktds.erpbarcode.env.SettingPreferences;

public class ScannerConnectHelper extends Activity {
	
	private static final String TAG = "DeviceConectHelper";
	
    // Message types sent from the BluetoothService Handler 
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DISPLAY = 6;
    public static final int MESSAGE_SEND = 7;
    public static final int MESSAGE_CONNECTED = 13;
    
    public static final int MESSAGE_EXIT = 0;
    public static final int MESSAGE_SETTING = 255;
    
    public static final String SOFT_KEY_BOARD_OPEN = "softkeyboard_open";

    
    public static final String TOAST = "toast";
    
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    
    private static ScannerConnectHelper instance;

    private BluetoothDevice mDevice;
    public BluetoothAdapter mBluetoothAdapter;
    public static BluetoothService mBluetoothService;
	
	private EditText mFocusEditText;
	
	private SettingPreferences mSharedSetting;
	
    private byte[] displayBuf = new byte[256];
    
	public static synchronized ScannerConnectHelper getInstance() {
		if (instance==null) {
			instance=new ScannerConnectHelper();
		}
		return instance;
	}

	public boolean initBluetooth(Context context) {
		Log.d(TAG, "initBluetooth   Start...");
		
		mSharedSetting = new SettingPreferences(context);

		//KoamTac
		KTSyncData.mKScan = new KScan(context, mHandler);
		
		if (!ScannerDeviceData.getInstance().isConnected()) {
			Log.d(TAG, "ScannerDeviceData  Not Connected");
			SettingPreferences sharedSetting = new SettingPreferences(context);
			Log.d(TAG, "sharedSetting.isBarcodeScanner()==>"+sharedSetting.isBarcodeScannerConnect());
			if (sharedSetting.isBarcodeScannerConnect()) {
				ScannerDeviceData scannerDevice = ScannerDeviceData.getInstance();
				scannerDevice.setConnected(true);
				scannerDevice.setDeviceName(sharedSetting.getBarcodeScannerDeviceName());
				scannerDevice.setDeviceAddress(sharedSetting.getBarcodeScannerDeviceAddress());
			} else {
				Log.d(TAG, "바코드스케너 장치가 선택되지 않았습니다.");
				return false;
			}
		}
		
		Log.d(TAG, "Bluetooth Device Address ==>"+ScannerDeviceData.getInstance().getDeviceAddress());
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			// 나중에 블루투스 환경설정 화면을 OPEN 한다.
			return false;
		}
		
		// Get the local Bluetooth adapter
		try {
			mDevice = mBluetoothAdapter.getRemoteDevice(ScannerDeviceData.getInstance().getDeviceAddress());
		} catch (Exception e) {
			Log.d(TAG, "블루투스 장치 선택하는중 오류가 발생했습니다.  ==>"+e.getMessage());
			return false;
		}
		
		if (mDevice==null) {
			Log.d(TAG, "선택된 블루투스 장치가 없습니다. ");
			return false;
		}
        
		return true;
	}
	
	public boolean deviceConnect() {
		if (mBluetoothService == null) {
        	// Initialize the BluetoothService to perform bluetooth connections
            mBluetoothService = new BluetoothService(mHandler);
        }
    
    	// Only if the state is STATE_NONE, do we know that we haven't started already
        if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
          // Start the Bluetooth chat services
        	mBluetoothService.start();
        } else {
        	if (mDevice==null) {
        		Log.d(TAG, "연결된 블루투스 장치가 없습니다. ");
        		return false;
        	}
        }
        
        mBluetoothService.connect(mDevice);
        
        return true;
	}
	
	public void resumeBluetoothStart() {
		if (mBluetoothService == null) {
        	// Initialize the BluetoothService to perform bluetooth connections
            mBluetoothService = new BluetoothService(mHandler);
        }
		
		if (mBluetoothService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
	        if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
	          // Start the Bluetooth chat services
	        	mBluetoothService.start();
	        }
		}
	}
	
	public int getState() {
		if (mBluetoothService != null) {
			return mBluetoothService.getState();
		}
        return BluetoothService.STATE_NONE;
    }
	
	public void focusEditText(EditText editText) {

		String jobGubun = GlobalData.getInstance().getJobGubun();
		mFocusEditText = editText;
		if (editText == null) return;
		
		mFocusEditText.requestFocus();
		InputMethodManager imm = ( InputMethodManager ) mFocusEditText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
		
		String tagValue = "";
		if (mFocusEditText.getTag() != null && mFocusEditText.getTag() instanceof String) {
			tagValue = mFocusEditText.getTag().toString();
		}
		
		if (tagValue.equals(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN)
				|| jobGubun.equals("설비정보")
				|| mSharedSetting.isScannerSoftKeyboard() 
				|| GlobalData.getInstance().getAdminFlag()
		) 
		{
			imm.showSoftInput(mFocusEditText, InputMethodManager.SHOW_IMPLICIT);
			mFocusEditText.selectAll();
		} else {
			imm.hideSoftInputFromWindow(mFocusEditText.getApplicationWindowToken() , 0);
		}
	}
	
	/**
	 * 스캐너 Focus는 못하게 한다. 
	 * @param editText
	 */
	public void notFocusEditText(EditText editText) {
		editText.requestFocus();
		mFocusEditText = null;
	}
	
	private void writeEditText(String message) {
		if (mFocusEditText==null) {
			Log.d(TAG, "포커스된 바코드스캔 항목이 없습니다. ");
			return;
		}
		
		// 작업 중이면 무시하기
		if (GlobalData.getInstance().isGlobalProgress() || GlobalData.getInstance().isGlobalAlertDialog()) return; 
		
		mFocusEditText.setText(message);
	}
	
	public void stop() {
		if (mBluetoothService != null) mBluetoothService.stop();
	}

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	KTSyncData.mKScan.DeviceConnected(true);
                	if(!GlobalData.getInstance().getJobGubun().equals("블루투스 페어링"))
                		Toast.makeText(GlobalData.getInstance().getNowOpenActivity().getApplicationContext(),"스캐너에 연결되었습니다.",Toast.LENGTH_SHORT).show();
                	Log.d(TAG, "handleMessage MESSAGE_STATE_CHANGE.STATE_CONNECTED ->");
                    break;
                case BluetoothService.STATE_CONNECTING:
                	Toast.makeText(GlobalData.getInstance().getNowOpenActivity().getApplicationContext(),"스캐너와 연결중 입니다.",Toast.LENGTH_SHORT).show();
                	Log.d(TAG, "handleMessage MESSAGE_STATE_CHANGE.STATE_CONNECTING ->");
                    break;
                case BluetoothService.STATE_LISTEN:
                  	if(KTSyncData.bIsConnected == true)
                	{
                  		if(!GlobalData.getInstance().getJobGubun().equals("블루투스 페어링"))
                  			Toast.makeText(GlobalData.getInstance().getNowOpenActivity().getApplicationContext(),"스캐너와 연결이 끊어졌습니다.",Toast.LENGTH_SHORT).show();
                	}   	
                	KTSyncData.bIsConnected = false;  
                	Log.d(TAG, "handleMessage MESSAGE_STATE_CHANGE.STATE_LISTEN ->");
                    break;
                case BluetoothService.STATE_NONE:
                	KTSyncData.bIsConnected = false;
                	Log.d(TAG, "handleMessage MESSAGE_STATE_CHANGE.STATE_NONE ->");
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                Log.d(TAG, "handleMessage MESSAGE_WRITE ->"+writeMessage);
                break;
            case MESSAGE_DISPLAY:
            	displayBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                // 바코드 결과는 암호화가 되었있다. 그러므로 여기서 복호화 한다.
               
            	String readMessage = new String(displayBuf, 0, msg.arg1);
            	String[] tmpArray = readMessage.split("_");
            	String strFCCData = tmpArray[0];
            	String barcodeData = tmpArray[1];
            	
            	/*
            	 * 기능 일시 중지 - request by 박장수, 김희선 - 2014.12.17 - 다시 오픈 request by 이종용 2014.04.02
            	 */
            	if(barcodeData.startsWith("k9")){
            		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "잘못 된 형식의 바코드입니다."));
            		return;
            	}
            	
            	//barcodeData = Caesar.decrypt(barcodeData);

            	//---------------------------------------------------
            	// 바코드 스케너 연결시 등록하는걸로 이동함.
            	// 단말설비바코드 셋팅
        		//SessionUserData sessionData = SessionUserData.getInstance();
    			//sessionData.setJobEqunr(strFCCData);
            	
                Log.d(TAG, "handleMessage MESSAGE_READ ->"+strFCCData+barcodeData);

                writeEditText(barcodeData);
                
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                // 바코드 결과는 암호화가 되었있다. 그러므로 여기서 복호화 한다.
                
                //String readMessage = new String(readBuf, 0, msg.arg1);
                //readMessage = readMessage.replace("\n", "");
                //readMessage = Caesar.decrypt(readMessage);
                //Log.d(TAG, "handleMessage MESSAGE_READ ->"+readMessage);
                
                //writeEditText(readMessage);
                
                for (int i = 0; i < msg.arg1; i++) {
                	KTSyncData.mKScan.HandleInputData(readBuf[i]);
                }
                break;
     	   case MESSAGE_SEND:
	        	//mConversationArrayAdapter.add(new String("1"));
               byte[] sendBuf = (byte[]) msg.obj;
               
               mBluetoothService.write(sendBuf);
	        	break;                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                String connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Log.d(TAG, "handleMessage MESSAGE_DEVICE_NAME ->"+connectedDeviceName);
                break;
            case MESSAGE_TOAST:
            	Log.d(TAG, "handleMessage MESSAGE_TOAST ->");
            	break;
            }
        }
    };
    
    public void ToastMessage() {
    	if(KTSyncData.bIsConnected){
    		Toast.makeText(GlobalData.getInstance().getNowOpenActivity().getApplicationContext(),"스캐너에 연결되었습니다.",Toast.LENGTH_SHORT).show();
    	}
    	else{
    		Toast.makeText(GlobalData.getInstance().getNowOpenActivity().getApplicationContext(),"스캐너와 연결이 끊어졌습니다.",Toast.LENGTH_SHORT).show();
    	}
    }
}
