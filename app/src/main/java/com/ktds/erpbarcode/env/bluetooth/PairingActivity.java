package com.ktds.erpbarcode.env.bluetooth;

import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.env.SettingPreferences;
import com.ktds.erpbarcode.print.zebra.SettingsHelper;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class PairingActivity extends Activity {

    private static final String TAG = "PairingActivity";
    
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter;
    
	private PairedDevicesAdapter mPairedDevicesAdapter;
    private ListView mPairedDevicesListView;
    private NewDevicesAdapter mNewDevicesAdapter;
    private ListView mNewDevicesListView;
    
    private DeviceInfo mDeviceInfo;
    
    private Handler mTimerHandler = new Handler();
    
    private SettingPreferences mSharedSetting;
    
    public static final String INPUT_PAIRING_KIND = "pairing_kind"; //scanner, printer
    private String iPairingKind = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
        iPairingKind = getIntent().getStringExtra(INPUT_PAIRING_KIND);
        
        setMenuLayout();
        setContentView(R.layout.env_bluetooth_devicelist);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setJobGubun("블루투스 페어링");
		GlobalData.getInstance().setNowOpenActivity(this);
        
        setLayout();
        
        mSharedSetting = new SettingPreferences(getApplicationContext());

        
        //새로운 기기 조회 설정.
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //등록된 기기 조회
        getPairedDevices();
    }
    
    private void setMenuLayout() {
    	ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(iPairingKind.equals("scanner")){
        	actionBar.setTitle(R.string.bluetooth_scanner_title);
        }else{
        	actionBar.setTitle(R.string.bluetooth_printer_title);
        }
        
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
		} else {
        	return super.onOptionsItemSelected(item);
        }
	    return false;
    }
	
	private void setLayout() {
        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.bluetooth_scan_button);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                //v.setVisibility(View.GONE);
            }
        });

        mPairedDevicesAdapter = new PairedDevicesAdapter(getApplicationContext());
        mPairedDevicesAdapter.setKind(iPairingKind);
        mPairedDevicesListView = (ListView) findViewById(R.id.bluetooth_paired_devices);
        mPairedDevicesListView.setAdapter(mPairedDevicesAdapter);
        mPairedDevicesListView.setOnItemClickListener(mPairedDeviceClickListener);
        
        mNewDevicesAdapter = new NewDevicesAdapter(getApplicationContext());
        mNewDevicesListView = (ListView) findViewById(R.id.bluetooth_new_devices);
        mNewDevicesListView.setAdapter(mNewDevicesAdapter);
        mNewDevicesListView.setOnItemClickListener(mNewDeviceClickListener);
        
        //mDeviceHelper = ScannerConnectHelper.getInstance();
	}

    @Override
    public void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        //if (mBluetoothService != null) {
        //    // Only if the state is STATE_NONE, do we know that we haven't started already
        //    if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
        //      // Start the Bluetooth chat services
        //    	mBluetoothService.start();
        //    }
        //}

        //mDeviceHelper.resumeBluetoothStart();
    }

	
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //if (mBluetoothService != null) mBluetoothService.stop();
        
        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        
        mTimerHandler.removeCallbacks(mUpdateTimeTask);
    }
    
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			// 장치 모두 재 검색한다.
			doDiscovery();
		}
	};
    
    /**
     * 등록된 블루투스 기기 조회
     */
    private void getPairedDevices() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        mPairedDevicesAdapter.itemClear();
        
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.bluetooth_paired_devices_title).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	//Add only KDC Bluetooth devices.
            	if(iPairingKind.equals("scanner")){
            		if(device.getName().toLowerCase().contains("kdc"))	
            		{
            			DeviceInfo deviceinfo = new DeviceInfo();
            			deviceinfo.setDeviceName(device.getName());
            			deviceinfo.setDeviceAddress(device.getAddress());
            			mPairedDevicesAdapter.addItem(deviceinfo);
            		}
            	}else{
            		if(device.getName().toLowerCase().contains("zd500"))	
            		{
            			DeviceInfo deviceinfo = new DeviceInfo();
            			deviceinfo.setDeviceName(device.getName());
            			deviceinfo.setDeviceAddress(device.getAddress());
            			mPairedDevicesAdapter.addItem(deviceinfo);
            		}
            	}
            }
        } else {
            //String noDevices = getResources().getText(R.string.none_paired).toString();
            //mPairedDevicesArrayAdapter.add(noDevices);
        }
    }
    
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        // Indicate scanning in the title
    	setProgressBarIndeterminateVisibility(true);
    	// setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.bluetooth_new_devices_title).setVisibility(View.VISIBLE);

        getPairedDevices();
        
        if(mNewDevicesAdapter !=null)
        {
        	mNewDevicesAdapter.itemClear();
        	mNewDevicesAdapter.notifyDataSetChanged();
        }
        
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
    
    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
                //Add only KDC Bluetooth devices.
                if(iPairingKind.equals("scanner")){
                	if(device.getName().contains("kdc")){
                	}else{
                		return;
                	}
                    	
            	}else{
            		if(device.getName().contains("ZD500")){
            		}else{
            			return;
            		}
            	}
                
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	DeviceInfo deviceinfo = new DeviceInfo();
                	deviceinfo.setDeviceName(device.getName());
                	deviceinfo.setDeviceAddress(device.getAddress());
                	mNewDevicesAdapter.addItem(deviceinfo);
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                // 새로운 디바이스 정보가 0건일때 처리..
                if (mNewDevicesAdapter.getCount() == 0) {

                }
            }
        }
    };

    private OnItemClickListener mPairedDeviceClickListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            mDeviceInfo = (DeviceInfo) mPairedDevicesAdapter.getItem(position);

            Log.d(TAG, "address==>"+mDeviceInfo.getDeviceAddress());
            //connectDevice(mDeviceInfo.getDeviceAddress());
            connectDevice(mDeviceInfo);
            if(iPairingKind.equals("scanner")){
            	KTSyncData.mKScan.displayDialog(PairingActivity.this, "연결중", mDeviceInfo.getDeviceName()+"에 연결중입니다.(최장 10초 소요)", 10000, true);
        	}
        }
    };
    
    private OnItemClickListener mNewDeviceClickListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            mDeviceInfo = (DeviceInfo) mNewDevicesAdapter.getItem(position);

            Log.d(TAG, "address==>"+mDeviceInfo.getDeviceAddress());
            //connectDevice(mDeviceInfo.getDeviceAddress());
            connectDevice(mDeviceInfo);
            if(iPairingKind.equals("scanner")){
            	KTSyncData.mKScan.displayDialog(PairingActivity.this, "연결중", mDeviceInfo.getDeviceName()+"에 페어링 및 연결중 입니다.(최장 10초 소요)", 10000, true);
        	}
            
            //-------------------------------------------------------
            // 10초후에 페이링 정보 다시 조회한다.
            //-------------------------------------------------------
            mTimerHandler.removeCallbacks(mUpdateTimeTask);
            mTimerHandler.postDelayed(mUpdateTimeTask, 12000);
        }
    };

/*
    private void connectDevice(String address) {
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device);
    }
*/
    private void connectDevice(DeviceInfo deviceInfo) {
    	if(iPairingKind.equals("scanner")){
        	ScannerDeviceData scannerDevice = ScannerDeviceData.getInstance();
        	scannerDevice.setConnected(true);
        	scannerDevice.setDeviceName(deviceInfo.getDeviceName());
        	scannerDevice.setDeviceAddress(deviceInfo.getDeviceAddress());
        	
        	ScannerConnectHelper deviceHelper = ScannerConnectHelper.getInstance();
        	if (!deviceHelper.initBluetooth(getApplicationContext())) {
        		ScannerDeviceData.getInstance().clear();
        	} else {
        		if (!deviceHelper.deviceConnect()) {
        			ScannerDeviceData.getInstance().clear();
        		} else {
        			// 연결 성공하면 SettingPreferences()에 저장한다.
        			mSharedSetting.setBarcodeScanner(true, deviceInfo.getDeviceName(),
        					deviceInfo.getDeviceAddress());
        		}
        	}
    	}
    	else{
    		SettingsHelper.saveBluetoothAddress(PairingActivity.this, deviceInfo.getDeviceAddress());
    		Toast.makeText(getApplicationContext(), "프린터가 저장되었습니다.",Toast.LENGTH_SHORT).show();
    	}
    }

}
