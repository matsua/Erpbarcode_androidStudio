package com.ktds.erpbarcode.infosearch;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;

public class SearchLocCheckActivity extends Activity {

	private static final String TAG = "SearchLocActivity";
	public static final String RESULT_LOC_CODE = "locCode";
	public static final String RESULT_LOC_NAME = "locName";
	
	private String p_deviceId;
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mLocCdText;
	//private TextView mLocNameText;
	private FindSpotCheckGetLoccodeInTask mFindSpotCheckGetLoccodeInTask;
	private List<LocBarcodeInfo> mLocBarcodeInfos;
	private ListView mLocListView;
	private LocListAdapter mLocListAdapter;
	
	private TextView mListFooterTotalCount;
	
	private String mJobGubun;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mJobGubun = GlobalData.getInstance().getJobGubun();
		//-----------------------------------------------------------
    	// input parameter
    	//-----------------------------------------------------------
    	p_deviceId = getIntent().getStringExtra("p_deviceId");
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		setMenuLayout();
		setContentView(R.layout.infosearch_searchloccheck_activity);
		setLayout();
		initScreen();
	}
	
	/**
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
		String title = "";
		if (mJobGubun.equals("장치바코드정보")) {
			title = "위치바코드를 스캔하세요.";
		} else {
			title = "위치바코드정보";
		}
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
    	// 위치코드        
        mLocCdText = (EditText) findViewById(R.id.searchloccheck_loc_locCd);
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
							Log.d(TAG, "위치바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
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

        // 위치코드명        
        //mLocNameText = (TextView) findViewById(R.id.spotcheck_locInfo_locName);
        
        mLocListAdapter = new LocListAdapter(getApplicationContext());
        mLocListView = (ListView) findViewById(R.id.searchloccheck_locListView);
        mLocListView.setAdapter(mLocListAdapter);
        mLocListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);

				//LocInfo locInfo = mLocListAdapter.getItem(position);
			}
		});
        
        mListFooterTotalCount = (TextView) findViewById(R.id.searchloccheck_listFooter_totalCount);
	}
	
	private void initScreen() {
		// 위치바코드로 스케너 Focus 지정한다.
		mScannerHelper.focusEditText(mLocCdText);

		getSpotCheckGetLoccodeData(p_deviceId);
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
	
	
    /**
     *  Progress를 관리한다.
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
    /**
     * 위치바코드 정보 변경시 처리.
     */
    // 위치바코드
    public void changeLocCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String locCd = mLocCdText.getText().toString();
    	if (locCd.isEmpty()) {
    		mLocCdText.setText(barcode);
    	}
    	
    	LocBarcodeInfo locInfo = mLocListAdapter.getLocBarcodeInfo(barcode);
    	if (locInfo != null) {
    		if (mJobGubun.startsWith("현장점검")) {
	    		if (locInfo.getLocCd().startsWith("VS") || locInfo.getLocCd().endsWith("0000")) {
	    			GlobalData.getInstance().showMessageDialog(
	    					new ErpBarcodeException(-1, "가상창고/실 위치 점검은\n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
	    			return;
	    		}
    		}
    		
    		Intent intent = new Intent();
            intent.putExtra(RESULT_LOC_CODE, locInfo.getLocCd());
            intent.putExtra(RESULT_LOC_NAME, locInfo.getLocName());
    		setResult(Activity.RESULT_OK, intent);   
    		finish();
    	} else {
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "해당 장치에 존재하지 않는\n\r위치바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
    	}
    }
    
	/**
	 * 현장점검 장치아이디 하위 설비의 위치코드 리스트 가져오기
	 */
	private void getSpotCheckGetLoccodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;

		if (mFindSpotCheckGetLoccodeInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindSpotCheckGetLoccodeInTask = new FindSpotCheckGetLoccodeInTask(deviceId);
			mFindSpotCheckGetLoccodeInTask.execute((Void) null);
		}
	}
	public class FindSpotCheckGetLoccodeInTask extends AsyncTask<Void, Void, Boolean> {
		private String _DeviceId = "";
		private ErpBarcodeException _ErpBarException;
		
		public FindSpotCheckGetLoccodeInTask(String deviceId) {
			_DeviceId = deviceId;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				mLocBarcodeInfos = locationhttp.getSpotCheckGetLocDataByDeviceId(_DeviceId);
				
				if (mLocBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "장치바코드-위치코드 정보가 없습니다. ");
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
			mFindSpotCheckGetLoccodeInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				mLocListAdapter.addItems(mLocBarcodeInfos);
				
				showSummaryCount();

			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mFindSpotCheckGetLoccodeInTask = null;
		}
	}
	
	/**
	 * List FooterBar Summary Tobal Count 정보를 조회한다.
	 */
	private void showSummaryCount() {
		int totalCount = mLocListAdapter.getCount();
		mListFooterTotalCount.setText(String.valueOf(totalCount) + "건");
	}
}
