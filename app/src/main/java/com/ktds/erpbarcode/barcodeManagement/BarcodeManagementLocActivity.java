package com.ktds.erpbarcode.barcodeManagement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.SettingPreferences;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.ism.IsmManagementActivity;
import com.ktds.erpbarcode.ism.IsmRequestListAdapter;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;
import com.ktds.erpbarcode.management.model.SendHttpController;
import com.ktds.erpbarcode.print.zebra.BarcodePrintController;
import com.ktds.erpbarcode.print.zebra.PrinterSettingActivity;
import com.ktds.erpbarcode.print.zebra.SettingsHelper;

public class BarcodeManagementLocActivity extends Activity {
	private static final String TAG = "BarcodeManagementLocActivity";  
	private static final int ACTION_INFO_PRINTER = 3;
	private static final int ACTION_SELECT_USER = 0;
	
	private ScannerConnectHelper mScannerHelper;
	private SettingPreferences mSharedSetting;
	
	private RelativeLayout mBarcodeProgress;
	
	private BarcodeSpinnerInTask mBarcodeSpinnerInTask;
	
	private ExpandableListView expandableList;  
	private SuperTreeViewAdapter superAdapter;
	private FindLocRequestInTask mFindLocRequestInTask;
	
	private JSONArray sidoArray = new JSONArray();
	private JSONArray sigoonArray = new JSONArray();
	private JSONArray dongArray = new JSONArray();
	
	private Spinner mSpinnerSidoCode;
	private String mTouchSidoCode = "";
	private BasicSpinnerAdapter mSidoCodeAdapter;
	
	private Spinner mSpinnerSigoonCode;
	private String mTouchSigoonCode = "";
	private BasicSpinnerAdapter mSigoonCodeAdapter;
	
	private EditText mSpinnerLbtCode;
	private String mTouchLbtCode = "";
//	private BasicSpinnerAdapter mLbtCodeAdapter;
	
	private Spinner mSpinnerDongCode;
	private String mTouchDongCode = "";
	private String mTouchDongName = "";
	private String mTouchDongValue = "";
	private BasicSpinnerAdapter mDongCodeAdapter;
	
	private Spinner mSpinnerArgumentDecisionCode;
	private String mTouchArgumentDecisionCode = "";
	private BasicSpinnerAdapter mArgumentDecisionAdapter;
	
	private Spinner mSpinnerLbCode;
	private BasicSpinnerAdapter mLbCodeAdapter;
	
	private SpinnerInfo sidoCodeSpinnerInfo;
	private SpinnerInfo sigoonCodeSpinnerInfo;
	private SpinnerInfo dongCodeSpinnerInfo;
	private SpinnerInfo argumentDecisionInfo;
	
	private EditText mDong;
	private int dongPosition;
	private Boolean request = false;
	
	private CheckBox mCheckBox;
	
	private EditText mLocCdText;
	private EditText mBunji;
	private EditText mHo;
	
	private EditText mDeviceIdText;
	private EditText mDeviceIdCode;
	private EditText mRegisterUserId;
	private Button mRegisterUserIdSearch;
	private EditText mRegisterDateFrom;
	private EditText mRegisterDateTo;
	private Calendar mCalendar = Calendar.getInstance();
	private int mRegisterDateType;
	
	private ListView mBarcodeListView;
	private IsmRequestListAdapter mIsmRequestListAdapter;
	private FindDeviceRequestInTask mFindDeviceRequestInTask;
	private FindSmRequestInTask mFindSmRequestInTask;
	
	private EditText mSmPoNum;
	private EditText mSmGubunNum;
	private EditText mBarcodeFrom;
	private EditText mBarcodeTo;
	
	private String mJobGubun = "";
	
	private FindSourceMarkingPrintCompleteInTask mFindSourceMarkingPrintCompleteInTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		
		setContentView(R.layout.barcode_management_loc_activity);
		
		mBarcodeProgress = (RelativeLayout) findViewById(R.id.barcode_barcodeProgress);
		
		setMenuLayout();
		
		if(mJobGubun.equals("위치바코드")){
			getBarcodeSpinnerData(0,"");
			mScannerHelper.focusEditText(mLocCdText);
			mTouchLbtCode = "7";
		}
	}
	
	/**
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();
 		
 		if (ScannerDeviceData.getInstance().isConnected()) {
 			if ((mScannerHelper.getState() == BluetoothService.STATE_CONNECTING) ||
 				(mScannerHelper.getState() == BluetoothService.STATE_CONNECTED)) {
 			} else {
 				boolean isInitBluetooth = mScannerHelper.initBluetooth(getApplicationContext());
 				if (isInitBluetooth) mScannerHelper.deviceConnect();
 			}
 		}
    }
	
	private void setMenuLayout() {
		mJobGubun = GlobalData.getInstance().getJobGubun();
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mJobGubun + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
        
        LinearLayout locView = (LinearLayout) findViewById(R.id.loc_view);
        LinearLayout smView = (LinearLayout) findViewById(R.id.sm_view);
        LinearLayout deviceView = (LinearLayout) findViewById(R.id.device_view);
        
        HorizontalScrollView locHeader = (HorizontalScrollView) findViewById(R.id.loc_header);
        HorizontalScrollView deviceHeader = (HorizontalScrollView) findViewById(R.id.device_header);
        HorizontalScrollView smHeader = (HorizontalScrollView) findViewById(R.id.sm_header);
        
        if(mJobGubun.equals("위치바코드")){
        	locView.setVisibility(View.VISIBLE);
        	locHeader.setVisibility(View.VISIBLE);
        	deviceView.setVisibility(View.GONE);
        	smView.setVisibility(View.GONE);
        	deviceHeader.setVisibility(View.GONE);
        	smHeader.setVisibility(View.GONE);
        	
        	// 위치코드        
        	mLocCdText = (EditText) findViewById(R.id.barcode_management_locCd);
        	mLocCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
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
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
        	mLocCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				return true;
        			}
        			return false;
        		}
        	});
        	
        	mDong = (EditText)findViewById(R.id.barcode_dong);
        	mDong.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_DONE) {
        				String value = v.getText().toString().trim();
        				if(value.length() < 1){
        					return false;
        				}
        				request = true;
        				getBarcodeSpinnerData(2,value);
        				return true;
        			}
        			return false;
        		}
        	});
        	
        	mBunji = (EditText)findViewById(R.id.barcode_bunji);
        	mHo = (EditText)findViewById(R.id.barcode_ho);
        	
        	superAdapter = new SuperTreeViewAdapter(this,stvClickEvent);  
        	expandableList = (ExpandableListView) BarcodeManagementLocActivity.this.findViewById(R.id.ExpandableListView01);
        	
        	mCheckBox = (CheckBox)findViewById(R.id.repair_checkBox);
        	mCheckBox.setTextColor(Color.RED);
        	mCheckBox.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						selectAll();
        						return true;
        					}
        					return true;
        				}
        			});
        	
        	mSpinnerLbtCode = (EditText) findViewById(R.id.ism_label_setting);
            mSpinnerLbtCode.setText("30x80mm");
            
            //프린트 보정 
    		Button mPrintSensor = (Button) findViewById(R.id.ism_print_sensor);
            mPrintSensor.setOnClickListener(
    		new View.OnClickListener() {
    			@Override
    			public void onClick(View view) {
    				int rscode = barcodePrintSensor();
    				switch (rscode) {
    				case 1:
    					break;
    				case -1:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "블루투스 연결에\n\r실패 했습니다.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				default:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 상태를\n\r확인 하세요.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				}
    			}
    		});
        	
		}else if(mJobGubun.equals("장치바코드")){
			deviceView.setVisibility(View.VISIBLE);
			deviceHeader.setVisibility(View.VISIBLE);
			locView.setVisibility(View.GONE);
			smView.setVisibility(View.GONE);
			locHeader.setVisibility(View.GONE);
        	smHeader.setVisibility(View.GONE);
			
			// 장치ID         
			mDeviceIdText = (EditText) findViewById(R.id.barcode_management_deviceId);
			mDeviceIdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
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
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mDeviceIdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				return true;
        			}
        			return false;
        		}
        	});

			// 장비ID        
			mDeviceIdCode = (EditText) findViewById(R.id.barcode_management_deviceIdCode);
			mDeviceIdCode.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mDeviceIdCode.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						mScannerHelper.focusEditText(mDeviceIdCode);
        						return false;
        					}
        					return false;
        				}
        			});
			mDeviceIdCode.addTextChangedListener(
        			new TextWatcher() {
        				public void afterTextChanged(Editable s) { }
        				@Override
        				public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
        				@Override
        				public void onTextChanged(CharSequence s, int start, int before, int count) {
        					String barcode = s.toString();
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mDeviceIdCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				String deviceId = v.getText().toString().trim().toUpperCase();
        				mDeviceIdCode.setText(deviceId);
        				return true;
        			}
        			return false;
        		}
        	});
			
//			mSpinnerArgumentDecisionCode = (Spinner) findViewById(R.id.barcode_management_spinner5);
//			List<SpinnerInfo> lbtspinneritems = new ArrayList<SpinnerInfo>();
//			lbtspinneritems.add(new SpinnerInfo("","선택하세요."));
//			lbtspinneritems.add(new SpinnerInfo("1", "이전"));
//			lbtspinneritems.add(new SpinnerInfo("2", "이후"));
//			
//			mArgumentDecisionAdapter = new BasicSpinnerAdapter(getApplicationContext(), lbtspinneritems);
//			mSpinnerArgumentDecisionCode.setAdapter(mArgumentDecisionAdapter);
//			mSpinnerArgumentDecisionCode.setOnItemSelectedListener(new OnItemSelectedListener() {
//		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
//		        	SpinnerInfo argumentDecisionCodeSpinnerInfo = (SpinnerInfo) mArgumentDecisionAdapter.getItem(position);
//		        	mSpinnerArgumentDecisionCode.setSelection(mArgumentDecisionAdapter.getPosition(argumentDecisionCodeSpinnerInfo.getCode()));
//		        	mTouchArgumentDecisionCode = argumentDecisionCodeSpinnerInfo.getCode();
//		        }
//		        public void onNothingSelected(AdapterView<?> arg0) {
//		        	
//		        }
//		    });
			
			// 생성자         
			mRegisterUserId = (EditText) findViewById(R.id.barcode_management_registerUserId);
//			mRegisterUserId.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mRegisterUserId.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
//        						mScannerHelper.focusEditText(mRegisterUserId);
        						return false;
        					}
        					return false;
        				}
        			});
			
			mRegisterUserIdSearch = (Button)findViewById(R.id.barcode_management_registerUserId_sel);
			mRegisterUserIdSearch.setOnClickListener(
	    			new View.OnClickListener() {
	    				@Override
	    				public void onClick(View view) {
	    					Intent intent = new Intent(getApplicationContext(),FindUserActivity.class);
    			    		startActivityForResult(intent, ACTION_SELECT_USER);
	    				}
	    			});
			
			//생성일
			mRegisterDateFrom = (EditText) findViewById(R.id.register_date_start);
			mRegisterDateFrom.setOnTouchListener(
					new OnTouchListener() {
				        @Override
				        public boolean onTouch(View v, MotionEvent event) {
				        	mRegisterDateType = 0;
				            switch(event.getAction()) {
				            case MotionEvent.ACTION_DOWN:
				                return false;
				            case MotionEvent.ACTION_UP:
				            	new DatePickerDialog(BarcodeManagementLocActivity.this,
				            			mDateSetListener,
	                                    mCalendar.get(Calendar.YEAR),
	                                    mCalendar.get(Calendar.MONTH),
	                                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
				                return true;
				            }
				            return false;
				        }
				    });
	        
//			mRegisterDateFrom.setText(SystemInfo.getNowDate("-"));
	        
			mRegisterDateTo = (EditText) findViewById(R.id.register_date_end);
			mRegisterDateTo.setOnTouchListener(
					new OnTouchListener() {
				        @Override
				        public boolean onTouch(View v, MotionEvent event) {
				        	mRegisterDateType = 1;
				            switch(event.getAction()) {
				            case MotionEvent.ACTION_DOWN:
				                return false;
				            case MotionEvent.ACTION_UP:
				            	new DatePickerDialog(BarcodeManagementLocActivity.this,
				            			mDateSetListener,
	                                    mCalendar.get(Calendar.YEAR),
	                                    mCalendar.get(Calendar.MONTH),
	                                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
				                return true;
				            }
				            return false;
				        }
				    });
	        
//			mRegisterDateTo.setText(SystemInfo.getNowDate("-"));
			
			//라벨용지
			mSpinnerLbCode = (Spinner) findViewById(R.id.ism_label_setting2);
			List<SpinnerInfo> lbtspinneritems2 = new ArrayList<SpinnerInfo>();
			lbtspinneritems2.add(new SpinnerInfo("","선택하세요."));
			lbtspinneritems2.add(new SpinnerInfo("1", "6x35mm"));
			lbtspinneritems2.add(new SpinnerInfo("2", "20x45mm"));
			lbtspinneritems2.add(new SpinnerInfo("3", "30x80mm"));
			
			mLbCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), lbtspinneritems2);
			mSpinnerLbCode.setAdapter(mLbCodeAdapter);
			mSpinnerLbCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	SpinnerInfo LbCodeSpinnerInfo = (SpinnerInfo) mLbCodeAdapter.getItem(position);
		        	mSpinnerLbCode.setSelection(mLbCodeAdapter.getPosition(LbCodeSpinnerInfo.getCode()));
		        	mTouchLbtCode = LbCodeSpinnerInfo.getCode();
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        	
		        }
		    });
			
			//프린트 보정 
    		Button mPrintSensor = (Button) findViewById(R.id.ism_print_sensor2);
            mPrintSensor.setOnClickListener(
    		new View.OnClickListener() {
    			@Override
    			public void onClick(View view) {
    				int rscode = barcodePrintSensor();
    				switch (rscode) {
    				case 1:
    					break;
    				case -1:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "블루투스 연결에\n\r실패 했습니다.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				default:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 상태를\n\r확인 하세요.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				}
    			}
    		});
            
            mIsmRequestListAdapter = new IsmRequestListAdapter(this);
    		
    		mBarcodeListView = (ListView) findViewById(R.id.listView01);
    		mBarcodeListView.setAdapter(mIsmRequestListAdapter);
    		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Log.d(TAG, "onItemClick   position==>" + position);
    				mIsmRequestListAdapter.changeSelectedPosition(position);
    			}
    		});
    		
    		mCheckBox = (CheckBox)findViewById(R.id.repair_checkBox2);
        	mCheckBox.setTextColor(Color.RED);
        	mCheckBox.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						selectAllSingle();
        						return true;
        					}
        					return true;
        				}
        			});
        	
		}else if(mJobGubun.equals("소스마킹")){
			smView.setVisibility(View.VISIBLE);
			locView.setVisibility(View.GONE);
			deviceView.setVisibility(View.GONE);
			deviceHeader.setVisibility(View.GONE);
			locHeader.setVisibility(View.GONE);
        	smHeader.setVisibility(View.VISIBLE);
			
			// PO번호         
			mSmPoNum = (EditText) findViewById(R.id.barcode_management_smPoNum);
			mSmPoNum.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mSmPoNum.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						mScannerHelper.focusEditText(mSmPoNum);
        						return false;
        					}
        					return false;
        				}
        			});
			mSmPoNum.addTextChangedListener(
        			new TextWatcher() {
        				public void afterTextChanged(Editable s) { }
        				@Override
        				public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
        				@Override
        				public void onTextChanged(CharSequence s, int start, int before, int count) {
        					String barcode = s.toString();
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mSmPoNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				return true;
        			}
        			return false;
        		}
        	});

			// 항목번호        
			mSmGubunNum = (EditText) findViewById(R.id.barcode_management_smTypeNum);
			mSmGubunNum.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mSmGubunNum.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						mScannerHelper.focusEditText(mSmGubunNum);
        						return false;
        					}
        					return false;
        				}
        			});
			mSmGubunNum.addTextChangedListener(
        			new TextWatcher() {
        				public void afterTextChanged(Editable s) { }
        				@Override
        				public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
        				@Override
        				public void onTextChanged(CharSequence s, int start, int before, int count) {
        					String barcode = s.toString();
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mSmGubunNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				return true;
        			}
        			return false;
        		}
        	});
			
			//출력여부 
			mSpinnerArgumentDecisionCode = (Spinner) findViewById(R.id.barcode_management_spinner6);
			List<SpinnerInfo> lbtspinneritems = new ArrayList<SpinnerInfo>();
			lbtspinneritems.add(new SpinnerInfo("","선택하세요."));
			lbtspinneritems.add(new SpinnerInfo("Y","출력"));
			lbtspinneritems.add(new SpinnerInfo("N", "미출력"));
			
			mArgumentDecisionAdapter = new BasicSpinnerAdapter(getApplicationContext(), lbtspinneritems);
			mSpinnerArgumentDecisionCode.setAdapter(mArgumentDecisionAdapter);
			mSpinnerArgumentDecisionCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	SpinnerInfo argumentDecisionCodeSpinnerInfo = (SpinnerInfo) mArgumentDecisionAdapter.getItem(position);
		        	mSpinnerArgumentDecisionCode.setSelection(mArgumentDecisionAdapter.getPosition(argumentDecisionCodeSpinnerInfo.getCode()));
		        	mTouchArgumentDecisionCode = argumentDecisionCodeSpinnerInfo.getCode();
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        	
		        }
		    });
			
			// 바코드From     
			mBarcodeFrom = (EditText) findViewById(R.id.barcode_management_smBarcodeFrom);
			mBarcodeFrom.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mBarcodeFrom.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						mScannerHelper.focusEditText(mBarcodeFrom);
        						return false;
        					}
        					return false;
        				}
        			});
			mBarcodeFrom.addTextChangedListener(
        			new TextWatcher() {
        				public void afterTextChanged(Editable s) { }
        				@Override
        				public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
        				@Override
        				public void onTextChanged(CharSequence s, int start, int before, int count) {
        					String barcode = s.toString();
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mBarcodeFrom.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				String barcodeFrom = v.getText().toString().trim().toUpperCase();
        				mBarcodeFrom.setText(barcodeFrom);
        				return true;
        			}
        			return false;
        		}
        	});
			
			// 바코드To     
			mBarcodeTo = (EditText) findViewById(R.id.barcode_management_smBarcodeTo);
			mBarcodeTo.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
			mBarcodeTo.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						mScannerHelper.focusEditText(mBarcodeTo);
        						return false;
        					}
        					return false;
        				}
        			});
			mBarcodeTo.addTextChangedListener(
        			new TextWatcher() {
        				public void afterTextChanged(Editable s) { }
        				@Override
        				public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
        				@Override
        				public void onTextChanged(CharSequence s, int start, int before, int count) {
        					String barcode = s.toString();
        					if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
        						barcode = barcode.trim();
        						if (barcode.isEmpty()) return;
        					}
        				}
        			});
			mBarcodeTo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        		@Override
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        			if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
        				String barcodeTo = v.getText().toString().trim().toUpperCase();
        				mBarcodeTo.setText(barcodeTo);
        				return true;
        			}
        			return false;
        		}
        	});
			
			//라벨용지
			mSpinnerLbCode = (Spinner) findViewById(R.id.ism_label_setting3);
			List<SpinnerInfo> lbtspinneritems2 = new ArrayList<SpinnerInfo>();
			lbtspinneritems2.add(new SpinnerInfo("","선택하세요."));
			lbtspinneritems2.add(new SpinnerInfo("0", "6x20mm"));
			lbtspinneritems2.add(new SpinnerInfo("1", "6x35mm"));
			lbtspinneritems2.add(new SpinnerInfo("2", "20x45mm"));
			lbtspinneritems2.add(new SpinnerInfo("3", "30x80mm"));
			lbtspinneritems2.add(new SpinnerInfo("5", "6x58mm"));
			lbtspinneritems2.add(new SpinnerInfo("6", "7x50mm"));
			
			mLbCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), lbtspinneritems2);
			mSpinnerLbCode.setAdapter(mLbCodeAdapter);
			mSpinnerLbCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	SpinnerInfo LbCodeSpinnerInfo = (SpinnerInfo) mLbCodeAdapter.getItem(position);
		        	mSpinnerLbCode.setSelection(mLbCodeAdapter.getPosition(LbCodeSpinnerInfo.getCode()));
		        	mTouchLbtCode = LbCodeSpinnerInfo.getCode();
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        	
		        }
		    });
			
			//프린트 보정 
    		Button mPrintSensor = (Button) findViewById(R.id.ism_print_sensor3);
            mPrintSensor.setOnClickListener(
    		new View.OnClickListener() {
    			@Override
    			public void onClick(View view) {
    				int rscode = barcodePrintSensor();
    				switch (rscode) {
    				case 1:
    					break;
    				case -1:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "블루투스 연결에\n\r실패 했습니다.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				default:
    					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 상태를\n\r확인 하세요.", BarcodeSoundPlay.SOUND_ERROR));
    					break;
    				}
    			}
    		});
            
            mIsmRequestListAdapter = new IsmRequestListAdapter(this);
    		
    		mBarcodeListView = (ListView) findViewById(R.id.listView02);
    		mBarcodeListView.setAdapter(mIsmRequestListAdapter);
    		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				Log.d(TAG, "onItemClick   position==>" + position);
    				mIsmRequestListAdapter.changeSelectedPosition(position);
    			}
    		});
    		
    		mCheckBox = (CheckBox)findViewById(R.id.repair_checkBox3);
        	mCheckBox.setTextColor(Color.RED);
        	mCheckBox.setOnTouchListener(
        			new OnTouchListener() {
        				@Override
        				public boolean onTouch(View v, MotionEvent event) {
        					switch(event.getAction()) {
        					case MotionEvent.ACTION_DOWN:
        						selectAllSingle();
        						return true;
        					}
        					return true;
        				}
        			});
            
		}
        
        mBarcodeProgress = (RelativeLayout) findViewById(R.id.barcode_barcodeProgress);
        
        //검색
        Button request = (Button)findViewById(R.id.ism_request);
    	request.setOnClickListener(
    			new View.OnClickListener() {
    				@Override
    				public void onClick(View view) {
    					if(mJobGubun.equals("위치바코드")){
    						getLocRequest();
    					}else if(mJobGubun.equals("장치바코드")){
    						getDeviceRequest();
    					}else{
    						getSmRequest();
    					}
    				}
    			});
        
		//출력테스트 
		Button mPrintTest = (Button) findViewById(R.id.ism_print_test);
        mPrintTest.setOnClickListener(
		new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mTouchLbtCode.length() < 1 || mTouchLbtCode == null){
	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 라벨 사이즈가 없습니다. "));
	    			return;
	    		}
				
				Intent intent = new Intent(getApplicationContext(), PrinterSettingActivity.class);
				intent.putExtra(PrinterSettingActivity.INPUT_PRINT_TYPE, mTouchLbtCode);
	    		startActivityForResult(intent, ACTION_INFO_PRINTER);
			}
		});
        
        //출력
        Button mPrint = (Button) findViewById(R.id.ism_print);
        mPrint.setOnClickListener(
		new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				request(1);
			}
		});
	}
	
	DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                	mCalendar.set(year, monthOfYear, dayOfMonth);
                	String dateString = DateFormat.format("yyyy-MM-dd", mCalendar).toString();
                	if(mRegisterDateType == 0){
                		mRegisterDateFrom.setText(dateString);
                	}else{
                		mRegisterDateTo.setText(dateString);
                	}
                }
            };
	
	private void setLayout(int type){
		if(type == 0){
			mSpinnerSidoCode = (Spinner) findViewById(R.id.barcode_management_spinner1);
	        
			List<SpinnerInfo> sidospinneritems = new ArrayList<SpinnerInfo>();
			sidospinneritems.add(new SpinnerInfo("00", "선택하세요"));
			for(int s = 0; s < sidoArray.length(); s++){
				try {
					JSONObject mpObj = sidoArray.getJSONObject(s);
					sidospinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mSidoCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), sidospinneritems);
			mSpinnerSidoCode.setAdapter(mSidoCodeAdapter);
			mSpinnerSidoCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	sidoCodeSpinnerInfo = (SpinnerInfo) mSidoCodeAdapter.getItem(position);
		        	mSpinnerSidoCode.setSelection(mSidoCodeAdapter.getPosition(sidoCodeSpinnerInfo.getCode()));
		        	mTouchSidoCode = sidoCodeSpinnerInfo.getCode();
		        	getBarcodeSpinnerData(1, mTouchSidoCode);
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        }
		    });
		}else if(type == 1){
			mSpinnerSigoonCode = (Spinner) findViewById(R.id.barcode_management_spinner3);
	        
			List<SpinnerInfo> sigoonspinneritems = new ArrayList<SpinnerInfo>();
			sigoonspinneritems.add(new SpinnerInfo("00", "선택하세요"));
			for(int s = 0; s < sigoonArray.length(); s++){
				try {
					JSONObject mpObj = sigoonArray.getJSONObject(s);
					sigoonspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mSigoonCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), sigoonspinneritems);
			mSpinnerSigoonCode.setAdapter(mSigoonCodeAdapter);
			mSpinnerSigoonCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	sigoonCodeSpinnerInfo = (SpinnerInfo) mSigoonCodeAdapter.getItem(position);
		        	mSpinnerSigoonCode.setSelection(mSigoonCodeAdapter.getPosition(sigoonCodeSpinnerInfo.getCode()));
		        	mTouchSigoonCode = sigoonCodeSpinnerInfo.getCode();
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        }
		    });
			
			if(request){
				String middleId = "";
	        	try {
	        		middleId = dongArray.getJSONObject(dongPosition).optString("middleId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        	mSpinnerSigoonCode.setSelection(mSigoonCodeAdapter.getRangePosition(middleId));
	        	request = false;
			}
		}else if(type == 2){
			mSpinnerDongCode = (Spinner) findViewById(R.id.barcode_management_spinner2);
			List<SpinnerInfo> dongspinneritems = new ArrayList<SpinnerInfo>();
//			dongspinneritems.add(new SpinnerInfo("", "선택하세요."));
			for(int s = 0; s < dongArray.length(); s++){
				try {
					JSONObject mpObj = dongArray.getJSONObject(s);
					dongspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mDongCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), dongspinneritems);
			mSpinnerDongCode.setAdapter(mDongCodeAdapter);
			mSpinnerDongCode.performClick();
			mSpinnerDongCode.setOnItemSelectedListener(new OnItemSelectedListener() {
		        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
		        	dongPosition = position;
		        	dongCodeSpinnerInfo = (SpinnerInfo) mDongCodeAdapter.getItem(position);
		        	mSpinnerDongCode.setSelection(mDongCodeAdapter.getPosition(dongCodeSpinnerInfo.getCode()));
		        	mTouchDongCode = dongCodeSpinnerInfo.getCode();
		        	mTouchDongName = dongCodeSpinnerInfo.getTitle();
		        	
		        	if(mTouchDongCode.length() > 0){
		        		mTouchDongValue = "";
		        		try {
		        			mTouchDongValue = dongArray.getJSONObject(dongPosition).optString("dong");
						} catch (JSONException e) {
							e.printStackTrace();
						}
		        		mDong.setText(mTouchDongValue);
		        	}
		        	
		        	String broadId = "";
					try {
						broadId = dongArray.getJSONObject(dongPosition).optString("broadId");
					} catch (JSONException e) {
						e.printStackTrace();
					}
		        	mSpinnerSidoCode.setSelection(mSidoCodeAdapter.getPosition(broadId));
		        }
		        public void onNothingSelected(AdapterView<?> arg0) {
		        }
		    });
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		GlobalData.getInstance().setNowOpenActivity(this);
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SELECT_USER) {
				String userId = data.getExtras().getString(FindUserActivity.OUTPUT_SEL_USER_ID);
				mRegisterUserId.setText(userId.trim());
			} 
    	}
    }
	
	/**
	 * 위치바코드 검색 조회.
	 */
	private void getLocRequest() {
		if(mLocCdText.getText().toString().equals("")){
			if(mTouchSidoCode == null || mTouchSidoCode.length() == 0 || mTouchSidoCode.equals("00")){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "시/도 코드를 선택하세요. "));
				return;
			}
			
			if(mTouchSigoonCode == null || mTouchSigoonCode.length() == 0 || mTouchSigoonCode.equals("00")){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "시/군/구 코드를 선택하세요. "));
				return;
			}
			
			if(mTouchDongValue == null || mTouchDongValue.length() == 0){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "동을 입력후 선택하세요. "));
				return;
			}
		}
		
		String boardId = mTouchSidoCode;
		String middleBmassId = mTouchSigoonCode;
		String geoName = mTouchDongValue;
		String locationCode = mLocCdText.getText().toString();
		String bunji = mBunji.getText().toString();
		String ho = mHo.getText().toString();
		
		superAdapter.RemoveAll();
		
		if (mFindLocRequestInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindLocRequestInTask = new FindLocRequestInTask(locationCode, boardId, middleBmassId, geoName, bunji, ho);
			mFindLocRequestInTask.execute((Void) null);
		}
	}
	
	public class FindLocRequestInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		String _locationCode, _boardId, _middleBmassId, _geoName, _bunji, _ho;
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		
		public FindLocRequestInTask(String locationCode, String boardId, String middleBmassId, String geoName, String bunji, String ho) {
			_boardId = boardId;
			_middleBmassId = middleBmassId;
			_geoName = geoName;
			_locationCode = locationCode;
			_bunji = bunji;
			_ho = ho;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				try {
					_IsmBarcodeInfos = ismhttp.getLocBarcodeToIsmBarcodeInfos(_locationCode, _boardId, _middleBmassId, _geoName, _bunji, _ho);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 위치 정보입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			Log.d(TAG, "FindLocRequestInTask   for 건수:"+_IsmBarcodeInfos.size());
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindLocRequestInTask = null;
			mCheckBox.setChecked(false);
			if (result) {
				if (_IsmBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 위치바코드 정보가 없습니다."));	
				} else {
					superAdapter.addItems(_IsmBarcodeInfos);
					getNodeData(_IsmBarcodeInfos);
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
			mFindLocRequestInTask = null;
		}
	}
	
	
	/**
	 * spinner data 조회 
	 */
	public void getBarcodeSpinnerData(int spinnerType, String spinnerKey) {
		if (mBarcodeSpinnerInTask == null) {
			mBarcodeSpinnerInTask = new BarcodeSpinnerInTask(spinnerType, spinnerKey);
			mBarcodeSpinnerInTask.execute((Void) null);
		}
	}
	
	public class BarcodeSpinnerInTask extends AsyncTask<Void, Void, Boolean> {
		private int _spinnerType = 0;
		private String _spinnerKey = "";
		private ErpBarcodeException _ErpBarException;
		private JSONArray _jsonResults = new JSONArray();
		
		public BarcodeSpinnerInTask(int spinnerType, String spinnerKey) {
			_spinnerType = spinnerType;
			_spinnerKey = spinnerKey;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				if(_spinnerType == 3){
					_jsonResults = ismhttp.getIsmPrintSpinner(2);
				}else{
					try {
						_jsonResults = ismhttp.getBarcodeSpinner(_spinnerType, _spinnerKey);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "화면구성값 체크 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				_jsonResults = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mBarcodeSpinnerInTask = null;
			JSONArray jsonArray_temp = new JSONArray();
			
			super.onPostExecute(result);
			if (result) {
				try {
					if(_spinnerType == 2){
						for(int i = 0; i < _jsonResults.length(); i++){
							JSONObject jsonobj = _jsonResults.getJSONObject(i);
							JSONObject jsonObject_temp = new JSONObject();
							jsonObject_temp.put("code", jsonobj.getString("mgahaLegalDongCode"));
							jsonObject_temp.put("name", jsonobj.getString("address"));
							jsonObject_temp.put("dong", jsonobj.getString("dongName"));
							jsonObject_temp.put("broadId", jsonobj.getString("broadId"));
							jsonObject_temp.put("middleId", jsonobj.getString("middleId"));
							jsonArray_temp.put(jsonObject_temp);
						}
					}else{
						for(int i = 0; i < _jsonResults.length(); i++){
							JSONObject jsonobj = _jsonResults.getJSONObject(i);
							JSONObject jsonObject_temp = new JSONObject();
							jsonObject_temp.put("code", jsonobj.getString("commonCode"));
							jsonObject_temp.put("name", jsonobj.getString("commonCodeName"));
							jsonArray_temp.put(jsonObject_temp);
						}
					}
					
					//시도 0 , 시군구 1 , 읍면동 2
					if(_spinnerType == 0) sidoArray = jsonArray_temp;
					else if(_spinnerType == 1) sigoonArray = jsonArray_temp;
					else if(_spinnerType == 2) dongArray = jsonArray_temp;
					
					setLayout(_spinnerType);
					
				} catch (JSONException e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "화면구성값 대입중 오류가 발생했습니다." + e));
					return;
				} catch (Exception e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "화면구성값 대입중 오류가 발생했습니다." + e));
					return;
				}

			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mBarcodeSpinnerInTask = null;
			super.onCancelled();
		}
	}
	
	/**
	 * 장치바코드 검색 조회.
	 */
	private void getDeviceRequest() {
		String deviceId = mDeviceIdText.getText().toString().trim();
		String deviceCd = mDeviceIdCode.getText().toString().trim().toUpperCase();
		String userId = mRegisterUserId.getText().toString().trim();
		String bgiDate = mRegisterDateFrom.getText().toString().replace("-", "");
		String endDate = mRegisterDateTo.getText().toString().replace("-", "");
		
		if(bgiDate.length() > 0 && endDate.length() > 0 && Integer.parseInt(bgiDate) > Integer.parseInt(endDate)){
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "검색 시작일이 종료일보다 클수 없습니다."));	
			return;
		}
		
		if(userId.length() > 3){
			if(bgiDate.length() < 6 || endDate.length() < 6){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "생성일을 입력하세요. "));
				return;
			}
		}
		
		if (mFindDeviceRequestInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindDeviceRequestInTask = new FindDeviceRequestInTask(deviceId, deviceCd, userId, bgiDate, endDate);
			mFindDeviceRequestInTask.execute((Void) null);
		}
	}
	
	public class FindDeviceRequestInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		String _deviceId, _deviceCd, _userId, _dateFrom, _DateTo;
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		
		public FindDeviceRequestInTask(String deviceId, String deviceCd, String userId, String dateFrom, String dateTo) {
			_deviceId = deviceId;
			_deviceCd = deviceCd;
			_userId = userId;
			_dateFrom = dateFrom;
			_DateTo = dateTo;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				try {
					_IsmBarcodeInfos = ismhttp.getDeviceBarcodeToIsmBarcodeInfos(_deviceId, _deviceCd, _userId, _dateFrom, _DateTo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 장치 정보입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			Log.d(TAG, "FindDeviceRequestInTask   for 건수:"+_IsmBarcodeInfos.size());
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindDeviceRequestInTask = null;
			mCheckBox.setChecked(false);
			if (result) {
				if (_IsmBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 장치바코드 정보가 없습니다."));	
				} else {
					mIsmRequestListAdapter.addItems(_IsmBarcodeInfos);
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
			mFindDeviceRequestInTask = null;
		}
	}
	
	/**
	 * 소스마킹 검색 조회.
	 */
	private void getSmRequest() {
		String poNum = mSmPoNum.getText().toString().trim();
		String typeNum = mSmGubunNum.getText().toString().trim();
		String printYn = mTouchArgumentDecisionCode;
		String barcodeFrom = mBarcodeFrom.getText().toString().trim().toUpperCase();
		String barcodeTo = mBarcodeTo.getText().toString().trim().toUpperCase();
		
		if(poNum.length() > 0){
            if(typeNum.length() < 1){
            	if(barcodeFrom.length() < 1 || barcodeTo.length() < 1){
            		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "검색조건을 입력해 주세요."));
            		return;
            	}
            }
        }else{
//        	if(barcodeFrom.length() < 1 || barcodeTo.length() < 1){
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "검색조건을 입력해 주세요."));
                return;
//        	}
        }
		
		if(barcodeFrom.length() > 0 && barcodeTo.length() > 0){
			if(barcodeFrom.length() > 18 || barcodeTo.length() > 18 || barcodeFrom.length() < 14 || barcodeTo.length() < 14){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 바코드입니다."));
				return;
			}
			
			if(barcodeFrom.length() != barcodeTo.length()){
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "바코드 입력 자릿수가 틀립니다."));
            	return;
            }
		}
		
		if (mFindSmRequestInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindSmRequestInTask = new FindSmRequestInTask(poNum, typeNum, printYn, barcodeFrom, barcodeTo);
			mFindSmRequestInTask.execute((Void) null);
		}
	}
	
	public class FindSmRequestInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		String _poNum, _typeNum, _printYn, _barcodeFrom, _barcodeTo;
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		
		public FindSmRequestInTask(String poNum, String typeNum, String printYn, String barcodeFrom, String barcodeTo) {
			_poNum = poNum;
			_typeNum = typeNum;
			_printYn = printYn;
			_barcodeFrom = barcodeFrom;
			_barcodeTo = barcodeTo;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				try {
					_IsmBarcodeInfos = ismhttp.getSmBarcodeToIsmBarcodeInfos(_poNum, _typeNum, _printYn, _barcodeFrom, _barcodeTo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 위치 정보입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			Log.d(TAG, "FindSmRequestInTask   for 건수:"+_IsmBarcodeInfos.size());
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindSmRequestInTask = null;
			mCheckBox.setChecked(false);
			if (result) {
				if (_IsmBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 장치바코드 정보가 없습니다."));	
				} else {
					mIsmRequestListAdapter.addItems(_IsmBarcodeInfos);
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
			mFindSmRequestInTask = null;
		}
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return false;
		if (item.getItemId()==android.R.id.home) {
			finish();
		} else {
        	return true;
        }
	    return false;
    }
	
	@Override
	public void onBackPressed() {
		if (GlobalData.getInstance().isGlobalProgress()) return;
		super.onBackPressed();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		GlobalData.getInstance().setNowOpenActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private int barcodePrintSensor(){
		int result, rscode = -1;
		
		BarcodePrintController bpc = new BarcodePrintController();
		String address = SettingsHelper.getBluetoothAddress(BarcodeManagementLocActivity.this);
		
		result = bpc.printOpen(address);
		if(result == 1){
			rscode = bpc.printSensor();
		}else{
			rscode = result;
		}
		return rscode;
	}
	
	private void request(final int type) {
		//0:출력테스트, 1:출력 
		if(mTouchLbtCode.length() < 1){
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 라벨 사이즈가 없습니다. "));
			return;
		}
		
		if(mJobGubun.equals("위치바코드")){
			if (superAdapter.getCheckedItems().size() == 0) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
				return;
			}
			
			if (superAdapter.getCheckedItems().size() > 1000) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "바코드 전송은 1000건 까지만 가능합니다."));
				return;
			}
		}else{
			if (mIsmRequestListAdapter.getCheckedItems().size() == 0) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
				return;
			}
			
			if (mIsmRequestListAdapter.getCheckedItems().size() > 1000) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "바코드 전송은 1000건 까지만 가능합니다."));
				return;
			}
		}
    	barcodePrint();
    }
	
	private int barcodePrint(){
		mSharedSetting = new SettingPreferences(getApplicationContext());
		
		if(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "x") == null || (mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "x")).equals("")){
        	try {
				mSharedSetting.setDefaultPrinterUserInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
		
		int x_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "x"));
		int y_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "y"));
        
		int xu_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "xu"));
		int yu_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "yu"));
		int darkness = Integer.parseInt(mSharedSetting.getPrinterUserInfo(mTouchLbtCode, "sd"));
        
        if(xu_coordinate == 0) xu_coordinate = x_coordinate;
        if(yu_coordinate == 0) yu_coordinate = y_coordinate;
        
		int result, rscode = -1;
		
		final List<IsmBarcodeInfo> sendBarcodeInfos;
		
		if(mJobGubun.equals("위치바코드")){
			sendBarcodeInfos = superAdapter.getCheckedItems();
		}else{
			sendBarcodeInfos = mIsmRequestListAdapter.getCheckedItems();
		}
		
		BarcodePrintController bpc = new BarcodePrintController();
		String address = SettingsHelper.getBluetoothAddress(BarcodeManagementLocActivity.this);
		
		result = bpc.printOpen(address);
		if(result == 1){
			System.out.println("barcodePrint >> " + sendBarcodeInfos.size());
			
//			for (IsmBarcodeInfo sendIsmBarcodeInfo : sendBarcodeInfos) {
//				System.out.println("sendIsmBarcodeInfo >> " + sendIsmBarcodeInfo.getNewBarcode());
				rscode = bpc.printBarcodeDirectCommand(sendBarcodeInfos,Integer.parseInt(mTouchLbtCode), xu_coordinate, yu_coordinate, darkness);
//			}
			
		}else{
			rscode = result;
		}
		return rscode;
	}
	
	public void statusChange(List<IsmBarcodeInfo> sendIsmBarcodeInfos){
		mFindSourceMarkingPrintCompleteInTask = new FindSourceMarkingPrintCompleteInTask(sendIsmBarcodeInfos);
		mFindSourceMarkingPrintCompleteInTask.execute((Void) null);
	}
	
	//소스마킹 인쇄 완료 상태값 변경 
	// 전송 비동기 테스크
	public class FindSourceMarkingPrintCompleteInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private OutputParameter _OutputParameter;
		private List<IsmBarcodeInfo> mSendIsmBarcodeInfos;
		
		public FindSourceMarkingPrintCompleteInTask(List<IsmBarcodeInfo> sendIsmBarcodeInfos) {
			mSendIsmBarcodeInfos = sendIsmBarcodeInfos;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
    		jsonParamList.put(jsonParam);

			JSONArray jsonSubParamList = new JSONArray();
			
			for(IsmBarcodeInfo sendIsmBarcodeInfo : mSendIsmBarcodeInfos){
				JSONObject jsonSubParam = new JSONObject();
				try {
					jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getFacCd());													//신규바코드
					jsonSubParam.put("userId", SessionUserData.getInstance().getUserId());											//처리자ID
					jsonSubParam.put("process", "S");																				//process
					jsonSubParam.put("oldBarcodeYN", sendIsmBarcodeInfo.getOldBarcodeYN());										//구바코드여부
				} catch (JSONException e1) {
					return false;
				} catch (Exception e) {
					return false;
				}
				jsonSubParamList.put(jsonSubParam);
			}
			
			try {
				SendHttpController sendhttp = new SendHttpController();
				_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ISM_PRN_STATUS, jsonSubParamList);

				if (_OutputParameter == null) {
					throw new ErpBarcodeException(-1, "'"+ GlobalData.getInstance().getJobGubun()	+ "' 정보 전송중 오류가 발생했습니다.");
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
			mFindSourceMarkingPrintCompleteInTask = null;
			if (result) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "정상 전송되었습니다."));
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindSourceMarkingPrintCompleteInTask = null;
		}
	}
	
	protected void selectAll() {
    	if (superAdapter.getAllItems().size() == 0) {
    		return;
    	}
    	
    	for(int i=0; i<superAdapter.getAllItems().size();i++){
    		superAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    	}
    	
    	mCheckBox.setChecked(!mCheckBox.isChecked());
    	getNodeData(superAdapter.getAllItems());
	}
	
	protected void selectAllSingle() {
    	if (mIsmRequestListAdapter.getAllItems().size() == 0) {
    		return;
    	}
    	
    	for(int i=0; i<mIsmRequestListAdapter.getAllItems().size();i++){
    		if(mJobGubun.equals("소스마킹")){
    			if(!mIsmRequestListAdapter.getItem(i).getSilStatus().equals("BC_CPT")){
    				mIsmRequestListAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    			}
    		}else if(mJobGubun.equals("장치바코드")){
    			if(!mIsmRequestListAdapter.getItem(i).getConditions().equals("20") && !mIsmRequestListAdapter.getItem(i).getConditions().equals("30")){
    				mIsmRequestListAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    			}
    		}else{
    			mIsmRequestListAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    		}
    	}
    	
    	mCheckBox.setChecked(!mCheckBox.isChecked());
    	mIsmRequestListAdapter.notifyDataSetChanged();
	}
	
	//프로그레스바 유무 여부 
	public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	mBarcodeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
	
	public void getNodeData(List<IsmBarcodeInfo> _IsmBarcodeInfos){
		superAdapter.RemoveAll();  
        superAdapter.notifyDataSetChanged();
        
        int depth1Count = 0;
        for(int idx = 0; idx < _IsmBarcodeInfos.size(); idx++){
        	if(_IsmBarcodeInfos.get(idx).geLocLevel().equals("1")) depth1Count++;
        }
        
        int totalCount = 0;
        List<SuperTreeViewAdapter.SuperTreeNode> superTreeNode = superAdapter.GetTreeNode();
        for(int i = 0; i < depth1Count; i++){  
            SuperTreeViewAdapter.SuperTreeNode superNode = new SuperTreeViewAdapter.SuperTreeNode();
            _IsmBarcodeInfos.get(totalCount).setPosition(totalCount);
            superNode.parentObj = _IsmBarcodeInfos.get(totalCount);
            totalCount++;
            
            if(totalCount == _IsmBarcodeInfos.size() || _IsmBarcodeInfos.get(totalCount).geLocLevel().equals("1")){
            	superTreeNode.add(superNode);
            	continue;
            }
            
            if(_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("2")){
            	while (_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("2")) {
            		TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
            		_IsmBarcodeInfos.get(totalCount).setPosition(totalCount);
            		node.parentObj = _IsmBarcodeInfos.get(totalCount);
            		totalCount++;
            		
            		if(totalCount == _IsmBarcodeInfos.size()) {
            			superNode.childs.add(node);
            			break;
            		}
            		
            		if(_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("3")){
            			while (_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("3")) {
            				_IsmBarcodeInfos.get(totalCount).setPosition(totalCount);
            				node.childs.add(_IsmBarcodeInfos.get(totalCount));
            				totalCount++;
            				if(totalCount == _IsmBarcodeInfos.size() || !_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("3")) break;
            			}
            		}
        			
            		superNode.childs.add(node);
            		if(totalCount == _IsmBarcodeInfos.size() || !_IsmBarcodeInfos.get(totalCount).geLocLevel().equals("2")) break;
            	}
            }
            superTreeNode.add(superNode);
        }
        
        superAdapter.UpdateTreeNode(superTreeNode);  
        expandableList.setAdapter(superAdapter);  
    } 
	
	OnChildClickListener stvClickEvent=new OnChildClickListener(){  
        @Override  
        public boolean onChildClick(ExpandableListView parent,View v, int groupPosition, int childPosition, long id) {  
            String str="parent id:"+String.valueOf(groupPosition)+",children id:"+String.valueOf(childPosition);  
            Toast.makeText(BarcodeManagementLocActivity.this, str, 300).show();  
            return false;  
        }  
    };
}
