package com.ktds.erpbarcode.infosearch;

import java.util.List;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;

public class SelectLocActivity extends Activity {

	private static final String TAG = "SelectLocActivity";
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mLocCdText;
	private Button mSearchButton;
	private LocBarcodesInTask mLocBarcodesInTask;
	private ListView mLocBarcodeListView;
	private LocListAdapter mLocListAdapter;
	
	private TextView mTotalCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		setMenuLayout();
		setContentView(R.layout.infosearch_selectloc_activity);
		setLayout();
		initScreen();
		
		Log.d(TAG, "Create  Start...");
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
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
    	// 위치코드        
        mLocCdText = (EditText) findViewById(R.id.selectloc_locCd);
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
							getLocBarcodes(barcode.trim());
						}
					}
				});
		mLocCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	getLocBarcodes(barcode);
                    return true;
                }
                return false;
            }
        });

        mSearchButton = ((Button) findViewById(R.id.selectloc_search_button));
        mSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String locCd = mLocCdText.getText().toString().trim();
						getLocBarcodes(locCd);
					}
				});

        mLocListAdapter = new LocListAdapter(getApplicationContext(), false);
		
		mLocBarcodeListView = (ListView) findViewById(R.id.selectloc_listView);
		mLocBarcodeListView.setAdapter(mLocListAdapter);
		mLocBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);

			}
		});
		
		mTotalCount = (TextView) findViewById(R.id.selectloc_bottomBar_totalCount);
	}
	
	private void initScreen() {
    	Log.d(TAG, "initScreen Start...");
    	mLocCdText.setText("");
    	mTotalCount.setText("");
    	
    	mScannerHelper.focusEditText(mLocCdText);
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
	 * 논리위치바코드(창고위치) 정보 조회.
	 */
	private void getLocBarcodes(String locCd) {
		if (locCd.isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 입력하세요."));
			return;
		}
		
		boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", locCd);
    	if(!flag){
        	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 위치바코드입니다."));
			return;
    	}
		
		if (isBarcodeProgressVisibility()) return;
		
		mLocListAdapter.itemClear();
		mTotalCount.setText("");
		
		if (mLocBarcodesInTask == null) {
			setBarcodeProgressVisibility(true);
			mLocBarcodesInTask = new LocBarcodesInTask(locCd);
			mLocBarcodesInTask.execute((Void) null);
		}
	}
	
	public class LocBarcodesInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _LocCd = "";
		private List<LocBarcodeInfo> _LocBarcodeInfos;
		
		public LocBarcodesInTask(String locCd) {
			_LocCd = locCd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				_LocBarcodeInfos = locationhttp.getLocBarcodesData(_LocCd);
				if (_LocBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "위치바코드정보 조회정보가 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "위치바코드정보 서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mLocBarcodesInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				if (_LocBarcodeInfos.size() > 0) {
					mLocListAdapter.addItems(_LocBarcodeInfos);
					
					// 조회건수를 보여준다.
					String totalCount = String.valueOf(mLocListAdapter.getCount());
					mTotalCount.setText(totalCount + "건");
				}
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mLocBarcodesInTask = null;
		}
	}
}
