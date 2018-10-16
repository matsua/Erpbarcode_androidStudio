package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.TabMenuItem;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.DeviceBarcodeService;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;

public class SelectDeviceActivity extends Activity {

	private static final String TAG = "SelectDeviceActivity";
	private static final int ACTION_SEARCHLOCACTIVITY = 1;
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mDeviceIdText;
	private EditText mLocCdText;
	private EditText mLocNameText;
	
	private LinearLayout mLocLayout;
	private LinearLayout mLocInfoLayout;

	private Button mFooterTabDeviceButton;
	private Button mFooterTabSubFacButton;
	private FrameLayout mDeviceTab;
	private FrameLayout mLocTab;
	private Fragment mDeviceFragment;
	private Fragment mLocFragment;
	
	private LinearLayout mCrudButtonBar;
	private Button mSearchButton;
	
	private DeviceBarcodeInfo mThisDeviceBarcodeInfo;
	private FindSpotCheckGetLocInTask mFindSpotCheckGetLocInTask;
	
	//private String mJobGubun = "";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//mJobGubun = GlobalData.getInstance().getJobGubun();

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
				
		initBarcodeScanner();
		setMenuLayout();
		setFrameLayout(savedInstanceState);
		setLayout();

		Log.d(TAG, "Create  Start...");
		initScreen();
	}
	
	/****************************************************************
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();
 		
 		if (ScannerDeviceData.getInstance().isConnected()) {
 			if ((mScannerHelper.getState() == BluetoothService.STATE_CONNECTING) ||
 				(mScannerHelper.getState() == BluetoothService.STATE_CONNECTED)) {
 				// 바코드 스케너가 연결된 상태이면...
 			} else {
 				boolean isInitBluetooth = mScannerHelper.initBluetooth(getApplicationContext());
 				if (isInitBluetooth) mScannerHelper.deviceConnect();
 			}
 		}
    }
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setFrameLayout(Bundle savedInstanceState) {
		if (findViewById(R.id.selectdevice_frame) == null) {
			setContentView(R.layout.infosearch_selectdevice_activity);
		}

		if (savedInstanceState != null)
			mDeviceFragment = getFragmentManager().getFragment(savedInstanceState, "mDeviceFragment");
		if (mDeviceFragment == null) {
			mDeviceFragment = SelectDeviceSubFragment.newInstance();
		}
		
		getFragmentManager().beginTransaction()
			.replace(R.id.selectdevice_device_frame, mDeviceFragment)
			.commit();
		
		if (savedInstanceState != null)
			mLocFragment = getFragmentManager().getFragment(savedInstanceState, "mLocFragment");
		if (mLocFragment == null) {
			mLocFragment = SelectDeviceSubLocFragment.newInstance();
		}
		
		getFragmentManager().beginTransaction()
			.replace(R.id.selectdevice_loc_frame, mLocFragment)
			.commit();
	}
	
	private void setLayout() {
		mLocLayout = (LinearLayout) findViewById(R.id.selectdevice_loc_layout);
		mLocInfoLayout = (LinearLayout) findViewById(R.id.selectdevice_locinfo_layout);
		
		mDeviceIdText = (EditText) findViewById(R.id.selectdevice_deviceId);
		mDeviceIdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
		mDeviceIdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mDeviceIdText);
			                return false;
			            }
			            return false;
			        }
			    });
		mDeviceIdText.addTextChangedListener(
				new TextWatcher() {
					public void afterTextChanged(Editable s) { }
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String barcode = s.toString();
						
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							Log.d(TAG, "장치바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeDeviceId(barcode.trim());
						}
					}
				});
		mDeviceIdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeDeviceId(barcode.trim());
                    return true;
                }
                return false;
            }
        });
		
		mLocCdText = (EditText) findViewById(R.id.selectdevice_locCd);
		mLocCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
		mLocCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mLocCdText);
			                return false;
			            }
			            return false;
			        }
			    });
        mLocCdText.addTextChangedListener(
				new TextWatcher() {
					public void afterTextChanged(Editable s) { }
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String barcode = s.toString();
						
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							Log.d(TAG, "장치바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeLocCd(barcode.trim());
						}
					}
				});
        mLocCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeLocCd(barcode.trim());
                    return true;
                }
                return false;
            }
        });
        
		mLocNameText = (EditText) findViewById(R.id.selectdevice_locName);

		
		//-----------------------------------------------------------
		// 하단 Tab관련
		//-----------------------------------------------------------
		mFooterTabDeviceButton = (Button) findViewById(R.id.selectdevice_footertab_device_button);
		mFooterTabDeviceButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchFooterTab("device");
					}
				});
		mFooterTabSubFacButton = (Button) findViewById(R.id.selectdevice_footertab_subFac_button);
		mFooterTabSubFacButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchFooterTab("subfac");
					}
				});
		
		mDeviceTab = (FrameLayout) findViewById(R.id.selectdevice_device_frame);
		mLocTab = (FrameLayout) findViewById(R.id.selectdevice_loc_frame);
		
		
		mCrudButtonBar = (LinearLayout) findViewById(R.id.selectdevice_crud_buttonbar);
		mSearchButton = (Button) findViewById(R.id.selectdevice_crud_search);
		mSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String locCd = mLocCdText.getText().toString().trim();
						String deviceId = mDeviceIdText.getText().toString().trim();
						
						if (deviceId.isEmpty()) {
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 장치ID/위치ID 입니다.", BarcodeSoundPlay.SOUND_ERROR));
							return;
						}
						
						getSAPBarcodeInfos(locCd, deviceId, "");
					}
				});
	}
	
	private void initScreen() {
		mDeviceIdText.setText("");
		mLocCdText.setText("");
		mLocNameText.setText("");
		
		mLocLayout.setVisibility(View.GONE);
		mLocInfoLayout.setVisibility(View.GONE);
		mCrudButtonBar.setVisibility(View.GONE);
		
		// 하위 Tab 메뉴를 생성한다.
		List<TabMenuItem> tabItems = new ArrayList<TabMenuItem>();
		TabMenuItem tabItem = new TabMenuItem();
		tabItem.setItemCode("device");
		tabItem.setItemName("장치바코드 상세정보");
		tabItems.add(tabItem);
		
		tabItem = new TabMenuItem();
		tabItem.setItemCode("loc");
		tabItem.setItemName("하위설비");
		tabItems.add(tabItem);
		//---------------------------------------------------------------------
		
		switchFooterTab("device");
		
		mScannerHelper.focusEditText(mDeviceIdText);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
		} else {
        	return true;
        }
	    return false;
    }

	@Override
	protected void onRestart() {
		super.onRestart();
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
	}
	
	private void switchFooterTab(String tab) {
		
		if (tab.equals("device")) {
			mFooterTabDeviceButton.setSelected(true);
			mFooterTabSubFacButton.setSelected(false);
			
			mDeviceTab.setVisibility(View.VISIBLE);
			mLocTab.setVisibility(View.GONE);
			mLocLayout.setVisibility(View.GONE);
			mLocInfoLayout.setVisibility(View.GONE);
			mCrudButtonBar.setVisibility(View.GONE);
		} else {
			mFooterTabDeviceButton.setSelected(false);
			mFooterTabSubFacButton.setSelected(true);
			
			mDeviceTab.setVisibility(View.GONE);
			mLocTab.setVisibility(View.VISIBLE);
			mLocLayout.setVisibility(View.VISIBLE);
			mLocInfoLayout.setVisibility(View.VISIBLE);
			mCrudButtonBar.setVisibility(View.VISIBLE);
			
			String deviceId =  mDeviceIdText.getText().toString().trim();
			String locCd =  mLocCdText.getText().toString().trim();
			// 장치바코드가 있고 위치바코드가 없으면 새로 읽어 온다.
			if (deviceId.length() == 9 && locCd.isEmpty()) {
				getDeviceBarcodeData(deviceId);
			}
		}
	}

    /**
     *  Progress를 관리한다.
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	
    	SelectDeviceSubLocFragment fragment = (SelectDeviceSubLocFragment) getFragmentManager().findFragmentById(R.id.selectdevice_loc_frame);
		if (fragment != null) {
			fragment.setFragmentProgressVisibility(show);
    	}
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
    public BarcodeTreeAdapter getBarcodeListAdapter() {
    	SelectDeviceSubLocFragment fragment = (SelectDeviceSubLocFragment) getFragmentManager().findFragmentById(R.id.selectdevice_loc_frame);
		if (fragment != null) {
			return fragment.getBarcodeTreeAdapter();
    	}
		return null;
    }
    public void getSAPBarcodeInfos(String locCd, String deviceId, String barcode) {
    	SelectDeviceSubLocFragment fragment = (SelectDeviceSubLocFragment) getFragmentManager().findFragmentById(R.id.selectdevice_loc_frame);
		if (fragment != null) {
			fragment.getSAPBarcodeInfos(locCd, deviceId, barcode);
    	}
    }
    
    private void changeDeviceId(String barcode) {
    	Log.d(TAG, "changeDeviceId   Start...");
    	if (barcode.length() != 9) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "유효하지 않은 장치ID/위치ID 입니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}

    	getDeviceBarcodeData(barcode);
	}
	
    private void changeLocCd(String loccd) {
    	if (loccd.isEmpty()) return;
    	getSAPBarcodeInfos(loccd, mDeviceIdText.getText().toString(), "");
	}
    
    private void showDeviceInfoDisplay(DeviceBarcodeInfo deviceInfo) {
    	SelectDeviceSubFragment fragment = (SelectDeviceSubFragment) getFragmentManager()
				.findFragmentById(R.id.selectdevice_device_frame);
		if (fragment != null) {
			fragment.showDeviceInfoDisplay(deviceInfo);
    	}
    }
	
	/**
	 * 장치바코드 정보 조회.
	 */
	private void getDeviceBarcodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
		
		setBarcodeProgressVisibility(true);
		Log.d(TAG, "getDeviceBarcodeData   Progress Skip");
		mThisDeviceBarcodeInfo = null;  // 꼭 전역장치바코드인포 변수를 초기화한다.
		
    	mLocCdText.setText("");
    	mLocNameText.setText("");
    	getBarcodeListAdapter().itemClear();
		
		DeviceBarcodeService devicebarcodeService = new DeviceBarcodeService(new DeviceBarcodeHandler());
		devicebarcodeService.search(deviceId);
	}
	
	/**
	 * DeviceBarcodeInfo정보 조회 결과 Handler
	 */
	private class DeviceBarcodeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case LocBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	mThisDeviceBarcodeInfo = DeviceBarcodeConvert.jsonStringToDeviceBarcodeInfo(findedMessage);
            	
        		successDeviceBarcodeProcess();
        		
                break;
            case LocBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다."));
            	break;
            case LocBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
        
        private void successDeviceBarcodeProcess() {
			Log.d(TAG, "장치바코드 getDeviceId==>"+mThisDeviceBarcodeInfo.getDeviceId());
			Log.d(TAG, "장치바코드 getItemCode==>"+mThisDeviceBarcodeInfo.getItemCode());
			Log.d(TAG, "장치바코드 getItemName==>"+mThisDeviceBarcodeInfo.getItemName());
			
			// 장치TAB이면 상세정보 조회하여 보여준다.
			if (mDeviceTab.getVisibility() == View.VISIBLE) {
				showDeviceInfoDisplay(mThisDeviceBarcodeInfo);
			} else {
				String deviceId = mDeviceIdText.getText().toString().trim();
				if (mThisDeviceBarcodeInfo.getOperationSystemToken().equals("04")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("08")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("09")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("10")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("99")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("69")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("79")
						|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("89")) {
					getSpotCheckGetLoccodeData(deviceId);
				} else {
					showSearchLocCheckActivity(deviceId);
				}
			}
        }
    };
    
    /**
     * 위치바코드 조회 Activity Open
     * 
     * @param deviceId
     */
    private void showSearchLocCheckActivity(String deviceId) {
    	//-----------------------------------------------------------
    	// 바코드 스캔 필드 없으면 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SearchLocCheckActivity.class);
		intent.putExtra("p_deviceId", deviceId);
		startActivityForResult(intent, ACTION_SEARCHLOCACTIVITY);
    }
	
	/**
	 * 장치아이디 하위 설비의 위치코드 리스트 가져오기
	 * 무선장비바코드인 경우 처리.
	 */
	public void getSpotCheckGetLoccodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		if (mFindSpotCheckGetLocInTask == null) {
			mFindSpotCheckGetLocInTask = new FindSpotCheckGetLocInTask(deviceId);
			mFindSpotCheckGetLocInTask.execute((Void) null);
		}
	}
	public class FindSpotCheckGetLocInTask extends AsyncTask<Void, Void, Boolean> {
		private String _DeviceId = "";
		private ErpBarcodeException _ErpBarException;
		
		private List<LocBarcodeInfo> _LocBarcodeInfos;
		
		public FindSpotCheckGetLocInTask(String deviceId) {
			_DeviceId = deviceId;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				_LocBarcodeInfos = locationhttp.getSpotCheckGetLocDataByDeviceId(_DeviceId);
				
				if (_LocBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "장치바코드에 해당하는 위치바코드 조회 결과가 없습니다. ");
				}
			} catch (ErpBarcodeException e) {
				Log.d(TAG, e.getErrMessage());
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindSpotCheckGetLocInTask = null;
			
			if (result) {
				if (_LocBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "조회된 위치코드가 없습니다."));
				}
				
				String locCd = "";
				String locName = "";
			
				for (LocBarcodeInfo locBarcodeInfo : _LocBarcodeInfos) {
					locCd = locBarcodeInfo.getLocCd();
					locName = locBarcodeInfo.getLocName();
					if (!locCd.isEmpty()) {
						break;
					}
				}
				Log.d(TAG, "wirelessFlag locCd==>"+locCd);
				Log.d(TAG, "wirelessFlag locName==>"+locName);
				mLocCdText.setText(locCd);
				mLocNameText.setText(locName);

			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			setBarcodeProgressVisibility(false);
			mFindSpotCheckGetLocInTask = null;
			super.onCancelled();
		}
	}
	
    @Override
    // 위치코드 선택창 성공 시
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//------------------------------------------------------------
    	// PopUp Activity 호출후에는 무조건 스케너 Focus를 지정한다.
    	//------------------------------------------------------------
    	mScannerHelper.focusEditText(mDeviceIdText);
    	
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SEARCHLOCACTIVITY) {
				String locCd = data.getExtras().getString(SearchLocCheckActivity.RESULT_LOC_CODE);
				String locName = data.getExtras().getString(SearchLocCheckActivity.RESULT_LOC_NAME);
				
				Log.d(TAG, "ACTION_SEARCHLOCACTIVITY   locCd==>"+locCd);
				Log.d(TAG, "ACTION_SEARCHLOCACTIVITY   locName==>"+locName);
				mLocCdText.setText(locCd);
				mLocNameText.setText(locName);
				changeLocCd(locCd);				
				mScannerHelper.focusEditText(mLocCdText);
			}
		} else {
			if (requestCode == ACTION_SEARCHLOCACTIVITY) {
				mScannerHelper.focusEditText(mDeviceIdText);
			}
		}
    }
}
