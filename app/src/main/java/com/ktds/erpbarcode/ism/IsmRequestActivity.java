package com.ktds.erpbarcode.ism;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.DeviceBarcodeService;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.ProductInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.SelectProductActivity;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;
import com.ktds.erpbarcode.management.AddrInfoActivity;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class IsmRequestActivity extends Activity {

	private static final String TAG = "IsmRequestActivity";
	private static final int ACTION_SELECTPRODUCTACTIVITY = 1;
	private static final int ACTION_SELECTPRODUCTACTIVITY_IN_GRID = 2;
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mOrgCodeText;
	//private Button mLocSearchButton;
    private EditText mLocCdText;
    private EditText mLocNameText;
    private EditText mDeviceIdText;
	private EditText mProductCodeText;
    private EditText mProductNameText;
    private Button mProductSearchButton;
    private EditText mUFacCdText;
    private TextView mUPartTypeText;
    private Spinner mRequestStatusSpinner;
    private BasicSpinnerAdapter mRequestStatusAdapter;
    private EditText mReplaceFacCdText;
    private TextView mReplacePartTypeText;
    private EditText mManufacturerSNText;
    
    private Button mSearchButton, mInitButton, mDeleteButton, mRequestButton;
    private TextView mListFooterTotalCount;
    
    // 위치바코드정보
    private LocBarcodeInfo mThisLocCodeInfo;
    
	private ListView mBarcodeListView;
	private IsmRequestListAdapter mIsmRequestListAdapter;
	private FindInstoreMarkingInTask mFindInstoreMarkingInTask;

	private RelativeLayout mBarcodeProgress;
	
	private String mJobGubun;
	
	private Button mAddInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		mJobGubun = GlobalData.getInstance().getJobGubun();
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		setMenuLayout();
		setContentView(R.layout.ism_ismrequest_activity);
		setLayout();
		initScreen();
		
		Log.d(TAG, "Create  Start...");
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
        actionBar.setTitle(mJobGubun + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

    private void setLayout() {
    	// 운용조직
    	//mOrgInputbar = (LinearLayout) findViewById(R.id.ismrequest_organization_inputbar);
    	mOrgCodeText = (EditText) findViewById(R.id.ismrequest_organization_orgCode);
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);

    	// 위치코드 
        mLocCdText = (EditText) findViewById(R.id.ismrequest_loc_locCd);
        mLocCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mLocCdText);
			                return true;
			            }
			            return true;
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
                	changeLocCd(barcode);
                    return true;
                }
                return false;
            }
        });
        
        // 위치코드명        
        mLocNameText = (EditText) findViewById(R.id.ismrequest_locInfo_locName);
        
//      matsua : windowPopup 생성  
        mAddInfo = (Button) findViewById(R.id.addInfoBtn);
		mAddInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mLocCdText.getText().toString().length() < 1){
					return;
				}
				
				Intent intent = new Intent(getApplicationContext(), AddrInfoActivity.class);
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARCD, mLocCdText.getText().toString());
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARNM, mLocNameText.getText().toString());
				intent.putExtra(AddrInfoActivity.INPUT_GUBUN, "위치바코드");
		        startActivity(intent);
			}
		 });
		
        // 장치바코드
        mDeviceIdText = (EditText) findViewById(R.id.ismrequest_device_deviceId);
        mDeviceIdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mDeviceIdText);
			                return true;
			            }
			            return true;
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
                	changeDeviceId(barcode);
                    return true;
                }
                return false;
            }
        });
        
        // 자재코드
        mProductCodeText = (EditText) findViewById(R.id.ismrequest_product_productCode);
        mProductCodeText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mProductCodeText);
			                return false;
			            }
			            return false;
			        }
			    });
        mProductCodeText.addTextChangedListener(
				new TextWatcher() {
					public void afterTextChanged(Editable s) { }
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String barcode = s.toString(); 
						
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.toUpperCase().trim();
							Log.d(TAG, "자재코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							String productCode = mProductCodeText.getText().toString().toUpperCase().trim();
							mProductCodeText.setText(productCode);
							 
							changeProductCode(barcode.trim());
						}
					}
				});
        mProductCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().toUpperCase().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	
                	String productCode = mProductCodeText.getText().toString().toUpperCase().trim();
					mProductCodeText.setText(productCode);
					 
                	changeProductCode(barcode.trim());
                    return true;
                }
                return false;
            }
        });
        mProductSearchButton = ((Button) findViewById(R.id.ismrequest_product_search_button));
        mProductSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showSelectProductActivity(ACTION_SELECTPRODUCTACTIVITY);
					}
				});
        mProductNameText = (EditText) findViewById(R.id.ismrequest_productinfo_productName);
        
        // 상위바코드
        mUFacCdText = (EditText) findViewById(R.id.ismrequest_high_UFacCd);
        mUFacCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mUFacCdText);
			                return true;
			            }
			            return true;
			        }
			    });
        mUFacCdText.addTextChangedListener(
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
							Log.d(TAG, "상위바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeUFacCd(barcode.trim());
						}
					}
				});
        mUFacCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeUFacCd(barcode);
                    return true;
                }
                return false;
            }
        });
        mUPartTypeText = (TextView) findViewById(R.id.ismrequest_high_UPartType);
        // 요청사유
        mRequestStatusSpinner = (Spinner) findViewById(R.id.ismrequest_reqstat_requestStatus);
        mRequestStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

        List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
        spinneritems.add(new SpinnerInfo("", "선택하세요."));
        spinneritems.add(new SpinnerInfo("1", "훼손"));
		spinneritems.add(new SpinnerInfo("3", "오부착(교환)"));
		spinneritems.add(new SpinnerInfo("4", "교품"));
		//spinneritems.add(new SpinnerInfo("5", "개조개량"));
		mRequestStatusAdapter = new BasicSpinnerAdapter(getApplicationContext(), spinneritems);
		mRequestStatusSpinner.setAdapter(mRequestStatusAdapter);
		mRequestStatusSpinner.setSelection(0);
		//SpinnerInfo spinnerInfo = (SpinnerInfo) mRequestStatusSpinner.getItem(mRequestStatusAdapter.getSelectedItemPosition());

		// 훼손바코드
		mReplaceFacCdText = (EditText) findViewById(R.id.ismrequest_damage_replaceFacCd);
		mReplaceFacCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mReplaceFacCdText);
			                return false;
			            }
			            return false;
			        }
			    });
		mReplaceFacCdText.addTextChangedListener(
				new TextWatcher() {
					public void afterTextChanged(Editable s) { }
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String barcode = s.toString().toUpperCase();
						
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							Log.d(TAG, "훼손바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeReplaceBarcode(barcode.trim());
						}
					}
				});
		mReplaceFacCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeReplaceBarcode(barcode);
                    return true;
                }
                return false;
            }
        });
		mReplacePartTypeText = (TextView) findViewById(R.id.ismrequest_damage_replacePartType);

		// 제조사S/N
		mManufacturerSNText = (EditText) findViewById(R.id.ismrequest_manu_manufacturerSN);
        
		// 조회버튼
		mSearchButton = ((Button) findViewById(R.id.ismrequest_crud_search));
        mSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String productCode = mProductCodeText.getText().toString().toUpperCase().trim();
						mProductCodeText.setText(productCode);
						getInstoreMarkings();
					}
				});
        
        // 초기화버튼
        mInitButton = ((Button) findViewById(R.id.ismrequest_crud_init));
        mInitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						initScreen();
					}
				});
        
        // 삭제버튼
        mDeleteButton = ((Button) findViewById(R.id.ismrequest_crud_delete));
        mDeleteButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						deleteTreeData();
					}
				});

        // 요청버튼
        mRequestButton = ((Button) findViewById(R.id.ismrequest_crud_request));
        mRequestButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						SendCheck sendCheck = new SendCheck();
						sendCheck.orgCode = SessionUserData.getInstance().getOrgId();
						if (mThisLocCodeInfo == null) {
				    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
				    	} else {
				    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
				    	}
						
						if (mRequestStatusAdapter.getCount() > 0) {
							SpinnerInfo spinnerInfo = (SpinnerInfo) mRequestStatusAdapter.getItem(mRequestStatusSpinner.getSelectedItemPosition());
							sendCheck.requestStatusCode = spinnerInfo.getCode();
						}
						
						executeSendCheck(sendCheck);
					}
				});
		
        // ListView
        mIsmRequestListAdapter = new IsmRequestListAdapter(this);
		
		mBarcodeListView = (ListView) findViewById(R.id.ismrequest_listView);
		mBarcodeListView.setAdapter(mIsmRequestListAdapter);
		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				mIsmRequestListAdapter.changeSelectedPosition(position);
				
				IsmBarcodeInfo model = mIsmRequestListAdapter.getItem(position);
				
				showChangeMatnr(model);
			}
		});
		
		mListFooterTotalCount = (TextView) findViewById(R.id.ismrequest_listfooter_totalCount);
		
		mBarcodeProgress = (RelativeLayout) findViewById(R.id.ismrequest_barcodeProgress);
	}
    
    private void showChangeMatnr(final IsmBarcodeInfo model) {
		SpinnerInfo spinnerInfo = (SpinnerInfo) mRequestStatusAdapter.getItem(mRequestStatusSpinner.getSelectedItemPosition());
		if (spinnerInfo.getCode().equals("3")) {
			// ---------------------------------------------------------------------
			// Yes/No Dialog
			// ---------------------------------------------------------------------
			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
			GlobalData.getInstance().setGlobalAlertDialog(true);
			
			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
			String alertMessage = "요청사유가 '오부착(교환)' 입니다.\n\r자재코드를 변경하시겠습니까?";
			final Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle("알림");
			TextView msgText = new TextView(this);
			msgText.setPadding(10, 30, 10, 30);
			msgText.setText(alertMessage);
			msgText.setGravity(Gravity.CENTER);
			msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			msgText.setTextColor(Color.BLACK);
			builder.setView(msgText);
			builder.setCancelable(false);
			builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//-----------------------------------------------------------
				    // Dialog화면 호출시 Open/Close 처리하는 Flag
				    //-----------------------------------------------------------
					GlobalData.getInstance().setGlobalAlertDialog(false);
					
					//model.setProductCode(productCode);
					showSelectProductActivity(ACTION_SELECTPRODUCTACTIVITY_IN_GRID);
					//-----------------------------------------------------------
				    // 화면 초기 Setting후 변경 여부 Flag를 초기화 하고..
					// Ativity를 종료한다.
				    //-----------------------------------------------------------
					GlobalData.getInstance().setChangeFlag(false);
				}
			});
			builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//-----------------------------------------------------------
				    // Dialog화면 호출시 Open/Close 처리하는 Flag
				    //-----------------------------------------------------------
					GlobalData.getInstance().setGlobalAlertDialog(false);
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
    }
    private void deleteTreeData() {
    	if (mIsmRequestListAdapter.getCheckedItems().size() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
    		return;
    	}
    	
    	mIsmRequestListAdapter.removeCheckedItems();
    	showSummaryCount();
    }
    
	
	private void initScreen() {
    	
		GlobalData.getInstance().setGlobalProgress(false);
		GlobalData.getInstance().setGlobalAlertDialog(false);
		
    	mLocCdText.setText("");
    	mLocNameText.setText("");
    	mDeviceIdText.setText("");
    	mProductCodeText.setText("");
    	mProductNameText.setText("");
        mUFacCdText.setText("");
        mUPartTypeText.setText("");
        // 요청사유
		mRequestStatusSpinner.setSelection(0);
		//SpinnerInfo spinnerInfo = (SpinnerInfo) mRequestStatusSpinner.getItem(mRequestStatusAdapter.getSelectedItemPosition());

		mReplaceFacCdText.setText("");
		mReplacePartTypeText.setText("");
		// 제조사S/N
		mManufacturerSNText.setText("");    	
    	
    	mIsmRequestListAdapter.itemClear();
    	showSummaryCount();
    	
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
		// TODO Auto-generated method stub
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
    	mBarcodeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
    // 위치바코드
    private void changeLocCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String locCd = mLocCdText.getText().toString();
    	if (locCd.isEmpty()) {
    		mLocCdText.setText(barcode);
    	}
    	
    	getLocBarcodeData(barcode);
    }
    
    // 장치바코드
    public void changeDeviceId(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String deviceId = mDeviceIdText.getText().toString();
    	if (deviceId.isEmpty()) {
    		mDeviceIdText.setText(barcode);
    	}

    	getDeviceBarcodeData(barcode);
    }
    
    /**
     * 자재코드 change event 발생시 자재정보 조회.
     * @param barcode
     */
    private void changeProductCode(String barcode) {
        barcode = barcode.toUpperCase();

        String productCode = mProductCodeText.getText().toString().toUpperCase().trim();
    	if (productCode.isEmpty()) {
    		mProductCodeText.setText(barcode);
    	}
    	

    	getProductInfoData(barcode);
    }
    
    private void showSelectProductActivity(int resultCode) {
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SelectProductActivity.class);
		intent.putExtra(SelectProductActivity.INPUT_MODE, SelectProductActivity.INPUT_MODE_SEARCH_RESULT);
		startActivityForResult(intent, resultCode);
    }
    
    private void changeUFacCd(String barcode) {
        barcode = barcode.toUpperCase();

        String UFacCd = mUFacCdText.getText().toString().trim();
    	if (UFacCd.isEmpty()) {
    		mUFacCdText.setText(barcode);
    	}
    	
    }
    
    private void changeReplaceBarcode(String barcode) {
        barcode = barcode.toUpperCase();

        String replaceBarcode = mReplaceFacCdText.getText().toString().trim();
    	if (replaceBarcode.isEmpty()) {
    		mReplaceFacCdText.setText(barcode);
    	}
    	
    }
    
    /**
	 * 위치바코드 정보 조회.
	 */
	private void getLocBarcodeData(String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		mThisLocCodeInfo = null;
		LocBarcodeService locbarcodeService = new LocBarcodeService(getApplicationContext(), new LocBarcodeHandler());
		locbarcodeService.search(barcode);
	}
	/**
	 * LocBarcodeInfo정보 조회 결과 Handler
	 */
	private class LocBarcodeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case LocBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	mThisLocCodeInfo = LocBarcodeConvert.jsonStringToLocBarcodeInfo(findedMessage);
            	
        		successLocBarcodeProcess();
        		
                break;
            case LocBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 위치코드입니다."));
            	break;
            case LocBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void successLocBarcodeProcess() {
		Log.d(TAG, "위치바코드 getLocCd==>"+mThisLocCodeInfo.getLocCd());
		Log.d(TAG, "위치바코드 getLocName==>"+mThisLocCodeInfo.getLocName());
		Log.d(TAG, "위치바코드 getDeviceId==>"+mThisLocCodeInfo.getDeviceId());
		Log.d(TAG, "위치바코드 getRoomTypeCode==>"+mThisLocCodeInfo.getRoomTypeCode());
		Log.d(TAG, "위치바코드 getRoomTypeName==>"+mThisLocCodeInfo.getRoomTypeName());
		Log.d(TAG, "위치바코드 getOperationSystemCode==>"+mThisLocCodeInfo.getOperationSystemCode());
		
		mLocCdText.setText(mThisLocCodeInfo.getLocCd());
		mLocNameText.setText(mThisLocCodeInfo.getLocName());
    }
    
	/**
	 * 장치바코드 정보 조회.
	 */
	private void getDeviceBarcodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);

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
            	DeviceBarcodeInfo deviceInfo = DeviceBarcodeConvert.jsonStringToDeviceBarcodeInfo(findedMessage);
            	
        		successDeviceBarcodeProcess(deviceInfo);
        		
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
    };
    
    private void successDeviceBarcodeProcess(DeviceBarcodeInfo deviceBarcodeInfo) {
		Log.d(TAG, "장치바코드 getDeviceId==>"+deviceBarcodeInfo.getDeviceId());
		Log.d(TAG, "장치바코드 getItemCode==>"+deviceBarcodeInfo.getItemCode());
		Log.d(TAG, "장치바코드 getItemName==>"+deviceBarcodeInfo.getItemName());
		Log.d(TAG, "장치바코드 getOperationSystemCode==>"+deviceBarcodeInfo.getOperationSystemCode());
		
		mDeviceIdText.setText(deviceBarcodeInfo.getDeviceId());
    }
    
    private void getProductInfoData(String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
    	setBarcodeProgressVisibility(true);
		PDABarcodeService pdaBarcodeService = new PDABarcodeService(getApplicationContext(), new ProductInfosInfoHandler());
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_EXPANSION_PRODUCT, barcode);
		} catch (ErpBarcodeException e) {
			setBarcodeProgressVisibility(false);
			GlobalData.getInstance().showMessageDialog(e);
			return;
		}
	}
    
    private class ProductInfosInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<ProductInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToProductInfos(findedMessage);


            	agoProductInfo(pdaItems.get(0));

                break;
            case PDABarcodeService.STATE_NOT_FOUND:
            	String notfoundMessage = msg.getData().getString("message");
            	ErpBarcodeException notfoundException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(notfoundMessage);
            	GlobalData.getInstance().showMessageDialog(notfoundException);
            	break;
            case PDABarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    }
    
    private void agoProductInfo(ProductInfo productInfo) {
        mProductNameText.setText(productInfo.getProductName());
    }
    
    
	/**
	 * 인스토어마킹 대상 조회.
	 */
	private void getInstoreMarkings() {
		if (isBarcodeProgressVisibility()) return;
		
		String orgCode = "";
		String locCd = mLocCdText.getText().toString().trim();
		String deviceId = mDeviceIdText.getText().toString().trim();
		String facCd  = mReplaceFacCdText.getText().toString().trim();
		String UFacCd = mUFacCdText.getText().toString().trim();
		String serge = mManufacturerSNText.getText().toString().trim();
		String productCode = mProductCodeText.getText().toString().toUpperCase().trim();
		
		if (locCd.isEmpty() && deviceId.isEmpty() && facCd.isEmpty() &&
				UFacCd.isEmpty() && serge.isEmpty() && productCode.isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1,"검색 조건을 입력하세요."));
			return;
		}

		mIsmRequestListAdapter.itemClear();
		showSummaryCount();
		
		if (mFindInstoreMarkingInTask == null) {
			setBarcodeProgressVisibility(true);
			
			mFindInstoreMarkingInTask = new FindInstoreMarkingInTask(orgCode, locCd, deviceId, facCd, UFacCd, serge, productCode);
			mFindInstoreMarkingInTask.execute((Void) null);
		}
	}
	
	public class FindInstoreMarkingInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		String _OrgCode, _LocCd, _DeviceId, _FacCd, _UFacCd, _Serge, _ProductCode;
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		
		public FindInstoreMarkingInTask(String orgCode, String locCd, String deviceId, String facCd, 
				String UFacCd, String serge, String productCode) {
			_OrgCode = orgCode;
			_LocCd = locCd;
			_DeviceId = deviceId;
			_FacCd = facCd;
			_UFacCd = UFacCd;
			_Serge = serge;
			_ProductCode = productCode;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				_IsmBarcodeInfos = ismhttp.getInstoreMarkingToIsmBarcodeInfos(_OrgCode, _LocCd, _DeviceId, _FacCd, _UFacCd, _Serge, _ProductCode);
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			
			Log.d(TAG, "FindInstoreMarkingInTask   for 건수:"+_IsmBarcodeInfos.size());
			
			String oldProductCode = "";
			
			for (int i=_IsmBarcodeInfos.size()-1; i>=0; i--) {
				IsmBarcodeInfo ismBarcodeInfo = _IsmBarcodeInfos.get(i);
				
				String ZPSTATU = ismBarcodeInfo.getFacStatus();
				// 불용확정, 사용중지, 납품취소 무조건 Skip 처리
                if (ZPSTATU.equals("0240") || ZPSTATU.equals("0260") || ZPSTATU.equals("0021"))
                {     
                	_IsmBarcodeInfos.remove(i);
                    continue;
                }

				String status = "";
				String extGW = "";
				
				List<ProductInfo> productInfos = null;
				if (!ismBarcodeInfo.getProductCode().equals(oldProductCode)) {
					try {
						BarcodeHttpController barcodehttp = new BarcodeHttpController();
						productInfos = barcodehttp.getProductInfosServer(ismBarcodeInfo.getProductCode(), "", "");
					} catch (ErpBarcodeException e) {
						_ErpBarException = e;
						return false;
					}
					
					if (productInfos != null && productInfos.size() > 0) {
						status = productInfos.get(0).getStatus();  // 상태
						if (status.equals("STOP"))
                            status = "사용중지";
                        else if (status.equals("USE"))
                            status = "사용중";
                        else
                            status = "알수없음";
						extGW = productInfos.get(0).getZzmatn();   // 대체자재코드
					}
					
					Log.d(TAG, "FindInstoreMarkingInTask   status==>"+status);
					Log.d(TAG, "FindInstoreMarkingInTask   extGW==>"+extGW);
					
					oldProductCode = ismBarcodeInfo.getProductCode();
				}
				
				ismBarcodeInfo.setUseStopYn(status);
				ismBarcodeInfo.setReplaceProductCode(extGW);
				
				_IsmBarcodeInfos.set(i, ismBarcodeInfo);
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingInTask = null;
			
			if (result) {
				if (_IsmBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 설비바코드가 없습니다."));	
				} else {
					mIsmRequestListAdapter.addItems(_IsmBarcodeInfos);

					showSummaryCount();
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
			mFindInstoreMarkingInTask = null;
		}
	}
	/**
	 * List FooterBar Summary Tobal Count 정보를 조회한다.
	 */
	private void showSummaryCount() {
		// 조회건수를 보여준다.
		int totalCount = mIsmRequestListAdapter.getCount();
		mListFooterTotalCount.setText(String.valueOf(totalCount) + "건");
	}
	
	/**
     * 전송시 유효성 체크 Class
     */
	public class SendCheck {
		public String orgCode = "";
		public LocBarcodeInfo locBarcodeInfo = null;
		public String requestStatusCode = "";
	}
	
	/**
	 * 바코드 작업내용 전송하기..
	 */
	private void executeSendCheck(final SendCheck sendCheck) {
		Log.d(TAG, "executeSendCheck  Start...");


		if (sendCheck.requestStatusCode.isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "요청사유를 선택하세요."));
			return;
		}
		
		if (mIsmRequestListAdapter.getCheckedItems().size() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}
		
		
		// ---------------------------------------------------------------------
		// 전송여부 최종 확인..
		// ---------------------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
		String message = "전송하시겠습니까?";
		final Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(this);
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);
				
				new SendDataInTask(sendCheck).execute();
			}
		});
		builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}

	// 전송 비동기 테스크
	private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private final SendCheck _SendCheck;
		private int _SendCount = 0;
		private OutputParameter _OutputParameter;

		public SendDataInTask(final SendCheck sendCheck) {
			_SendCheck = sendCheck;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setBarcodeProgressVisibility(true);
			mRequestButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "SendDataInTask  Start...");

			
			JSONArray jsonParamList = new JSONArray();
			final List<IsmBarcodeInfo> sendIsmBarcodeInfos = mIsmRequestListAdapter.getAllItems();
			
			Log.d(TAG, "SendDataInTask Body Record Count==>"+ sendIsmBarcodeInfos.size());

			_SendCount = 0;
			for (IsmBarcodeInfo sendIsmBarcodeInfo : sendIsmBarcodeInfos) {

				if (!sendIsmBarcodeInfo.isChecked()) continue;

				JSONObject jsonParam = new JSONObject();
				try {
					jsonParam.put("PBLS_CNT", "1");
					jsonParam.put("GNRT_REQ_TP_CD", "IMD");
					jsonParam.put("GNRT_TARG_CD", "INS");

					if (_SendCheck.requestStatusCode.equals("1")) {
						jsonParam.put("PBLS_WHY_CD", "1"); // 요청사유 - 훼손
					} else if (_SendCheck.requestStatusCode.equals("3")) {
						jsonParam.put("PBLS_WHY_CD", "3"); // 요청사유 - 오부착(교환)
					} else if (_SendCheck.requestStatusCode.equals("4")) {
						jsonParam.put("PBLS_WHY_CD", "4"); // 요청사유 - 교품
					} else if (_SendCheck.requestStatusCode.equals("5")) {
						jsonParam.put("PBLS_WHY_CD", "5"); // 요청사유-개조개량
					}
					
					
					String[] splZeqart = sendIsmBarcodeInfo.getAssetClass().split("/");
					jsonParam.put("ZEQART1", splZeqart[0]);
					jsonParam.put("ZEQART2", splZeqart[1]);
					jsonParam.put("ZEQART3", splZeqart[2]);
					jsonParam.put("ZEQART4", splZeqart[3]);
					jsonParam.put("EQUNR", sendIsmBarcodeInfo.getFacCd());
					jsonParam.put("ZEQUIPLP", sendIsmBarcodeInfo.getLocCd());
					jsonParam.put("ZEQUIPGC", sendIsmBarcodeInfo.getDeviceId());
					jsonParam.put("SUBMT", sendIsmBarcodeInfo.getProductCode());
					jsonParam.put("SERGE", sendIsmBarcodeInfo.getManufacturerSN());
					jsonParam.put("ZKOSTL", sendIsmBarcodeInfo.getCostCenter());

				} catch (JSONException e1) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
					return false;
				} catch (Exception e) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
					return false;
				}

				jsonParamList.put(jsonParam);

				_SendCount++; // 전송건수.
			} // for end.

 
			try {
				SendHttpController sendhttp = new SendHttpController();
				_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_INSTOREMARKING, 
						jsonParamList, null);
				
				if (_OutputParameter == null) {
					throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송중 오류가 발생했습니다.");
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
			setProgressBarIndeterminateVisibility(false);

			if (result) {
				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + " - 정상 전송되었습니다.";
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				initScreen();
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			setBarcodeProgressVisibility(false);
			mRequestButton.setEnabled(true);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
    	//------------------------------------------------------------
    	// PopUp Activity 호출후에는 무조건 스케너 Focus를 지정한다.
    	//------------------------------------------------------------
    	mScannerHelper.focusEditText(mLocCdText);
    	
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SELECTPRODUCTACTIVITY) {
				String productCode = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_CODE);
				String productName = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_NAME);
				Log.d(TAG, "ACTION_SELECTPRODUCTACTIVITY   productCode==>"+productCode);
				Log.d(TAG, "ACTION_SELECTPRODUCTACTIVITY   productName==>"+productName);
				
				if (!productCode.isEmpty()) {
					mProductCodeText.setText(productCode);
					mProductNameText.setText(productName);
				}
			}
			else if (requestCode == ACTION_SELECTPRODUCTACTIVITY_IN_GRID) {
				String productCode = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_CODE);
				String productName = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_NAME);
				
				if (!productCode.isEmpty()) {
					IsmBarcodeInfo model = mIsmRequestListAdapter.getItem(mIsmRequestListAdapter.getSelectedPosition());
					model.setProductCode(productCode);
					model.setProductName(productName);
					mIsmRequestListAdapter.notifyDataSetChanged();
    				GlobalData.getInstance().showMessageDialog(
    						new ErpBarcodeException(-1, "'오부착(교환)' 자재코드를\n\r'" + productCode + "'로\n\r설정하였습니다."));
				}
			}
		} else {
			if (requestCode == ACTION_SELECTPRODUCTACTIVITY) {
				mScannerHelper.focusEditText(mProductCodeText);
			}
		}
    }
}
