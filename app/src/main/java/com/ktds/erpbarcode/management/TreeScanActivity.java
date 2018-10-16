package com.ktds.erpbarcode.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.DeviceBarcodeService;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.SAPBarcodeService;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.WBSBarcodeService;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.FailureListInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.FailureListAdapter;
import com.ktds.erpbarcode.infosearch.SearchWbsCheckActivity;
import com.ktds.erpbarcode.infosearch.SelectFacDetailActivity;
import com.ktds.erpbarcode.infosearch.SelectOrgCodeActivity;
import com.ktds.erpbarcode.infosearch.model.InfoHttpController;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class TreeScanActivity extends Activity {

	private static final String TAG = "TreeScanActivity";
	
	private static final int ACTION_SELECTORGCODEACTIVITY = 1;
	private static final int ACTION_SEARCHWBSCHECKACTIVITY = 2;
	private static final int ACTION_SEARCHLOCINFOACTIVITY = 3;
	private static final int ACTION_TAKE_GALLERY = 4;
	private static final int ACTION_TAKE_PHOTO = 5;
	private static final int ACTION_REQUEST_GWLEN_O = 6;

	
	public static final int EDIT_MODE_NONE = 100;
	public static final int EDIT_MODE_MODIFY = 110;
	public static final int EDIT_MODE_MOVE = 120;
	
    private String mJobGubun = "";
	
	private ScannerConnectHelper mScannerHelper;
	
	private Fragment mBarcodeTreeFragment;
    
	//private LinearLayout mOrgInputbar;
    private EditText mOrgCodeText;
    private LinearLayout mReceiptOrgInputbar;
    private EditText mReceiptOrgCodeText;
    private Button mReceiptOrgSearchButton;
    
    // 위치바코드
    private LinearLayout mLocInputbar;
    private TextView mLocCdLevel;
    private EditText mLocCdText;
    private EditText mLocNameText;
    private EditText mGPSLocDataText;

    // 장치바코드
    private LinearLayout mDeviceInputbar;
    private EditText mDeviceIdText;
    private TextView mOprSysCdText;
    private EditText mDeviceIdInfoText; 
    
    // 설비바코드
    //private LinearLayout mFacInputbar;
    private EditText mFacCdText;
    private TextView mPartTypeText;
    private CheckBox mUUCheck;
    private Button mDeltailButton;
    
    // 형상구성
    private LinearLayout mHierachyInputbar;
	private TextView mHierachyLevel;

	// 설비상태
	private LinearLayout mFacStatusInputbar;
    private Spinner mSpinnerFacStatus;
    private String mTouchFacStatusCode = null;
    private BasicSpinnerAdapter mFacStatusAdapter;
    
    // 상위바코드
    private LinearLayout mUFacInputbar;
    private EditText mUFacCdText;
    private TextView mUPartTypeText;

    //---------------------------------------------------------------
    // CRUD BAR
    //---------------------------------------------------------------
    private LinearLayout mCrudNoneInputbar;
    private CheckBox mChkScanCheck;
	private CheckBox mHierachyCheck;   // 배송출고 - 배송오더 checkBox로 사용함.
    private List<BarcodeListInfo> mFirstBarcodeListInfos;
    private Button mInitButton, mCancelScanButton, mDeleteButton, mSaveButton, mSendButton;    
    
	private LocBarcodeInfo mThisLocCodeInfo;  // 현재 스캔한 위치바코드 정보.
	private LogicalLocationInTask mLogicalLocationInTask;
	
	// wbs
    private LinearLayout mWbsInputbar;
    private EditText mWbsNoText;

	// 고장코드
	private LinearLayout mCboCodeInputbar;
	private Spinner mSpinnerCboCode;
	private String mTouchCboCode = null;
	private BasicSpinnerAdapter mCboCodeAdapter;
	// 고장내역, 고장설비 사진
	private LinearLayout mCboReasonInputbar;
	private EditText mCboReasonText;
//    private ImageView mCboPhotoImageView;
//    private StorageTools mStorageTools;
//    private Uri mCboPhotoUri;
//    private ScreenTools mScreenTools;

    //---------------------------------------------------------------
    // 현재 스캔한 상위바코드 정보.
    //---------------------------------------------------------------
    private BarcodeListInfo mThisUFacInfo;
	
	//---------------------------------------------------------------
	// 수정, 이동 모드 관련 변수들..
	//---------------------------------------------------------------
    private LinearLayout mCrudEditModeInputbar;
    private Button mMoveButton, mCancelEditModeButton;
	private TextView mEditModeComment;
	private int mEditMode = EDIT_MODE_NONE;
	private Long mEditTreeKey = null;
	private BarcodeListInfo mEditBarcodeInfo = null;

	private Button mAddInfo;

	// 위치정보 
	private String pLocCd_deviceId, pLocNm_deviceId;
	
	//고장정보 
	private FindFacBarcodeDetailInTask mFindFacBarcodeDetailInTask;
	private JSONObject mFacJsonResult;
	private Button mFailureButton;
	
	private FailureListInTask mFailureListInTask;
	private List<FailureListInfo> mFailureListInfo;
	private ListView mFailureListView;
	private FailureListAdapter mFailureListAdapter;
	
	private Button mFailureInit, mDivInfo;
	private Boolean isFacQuMode = false;
	private String roomTypeCode;
	
	private SendCheck mSendCheck;

	@Override
    public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
//		mStorageTools = new StorageTools(this);
//		mScreenTools = new ScreenTools(this);
		
        initBarcodeScanner();
        setMenuLayout();
        setTreeLayout(savedInstanceState);
        setLayout();
        setFieldVisibility();
        initScreen("all");
        
        //-----------------------------------------------------------
        // 작업관리에서 호출이면..
        //-----------------------------------------------------------
        GlobalData.getInstance().initJobActionManager();
        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
        	new Handler().postDelayed(
        			new Runnable() {
		        		public void run() {
		        			jobNextExecutors();  // 작업아이템 1번째부터 진행한다.
		        		}
		        	}, 1000);
        } else {
        	if (SessionUserData.getInstance().isOffline()) {
        		if (mJobGubun.equals("송부취소(팀간)") || mJobGubun.equals("접수(팀간)")) {
        			try {
						GlobalData.getInstance().getJobActionManager().setStepItem(
								JobActionStepManager.JOBTYPE_INITIAL_TASK, "taskName", "getMoveRequestData");
					} catch (ErpBarcodeException e) {
						e.printStackTrace();
					}
            	}
        	} else {
        		// 납품입고일 경우 조직 논리 창고 자동 셋팅
        		if (mJobGubun.equals("납품입고")) {
        			getLogicalLocationData();
        		} else if (mJobGubun.equals("송부취소(팀간)") || mJobGubun.equals("접수(팀간)")) {
            		getMoveRequestData("", "");
            	}
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
	}

    
    /**
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();

 		Log.i(TAG, "initBarcodeScanner  getState()==>" + mScannerHelper.getState());
 		if (ScannerDeviceData.getInstance().isConnected()) {
 			if ((mScannerHelper.getState() == BluetoothService.STATE_CONNECTING) ||
 				(mScannerHelper.getState() == BluetoothService.STATE_CONNECTED)) {
 				// 바코드 스캐너가 연결된 상태이면...
 			} else {
 				boolean isInitBluetooth = mScannerHelper.initBluetooth(getApplicationContext());
 				if (isInitBluetooth) mScannerHelper.deviceConnect();
 			}
 		}
    }
    
    private void setTreeLayout(Bundle savedInstanceState) {
		if (findViewById(R.id.treescan_frame) == null) {
			setContentView(R.layout.management_treescan_activity);
		}

		if (savedInstanceState != null){
			mBarcodeTreeFragment = getFragmentManager().getFragment(savedInstanceState, "mBarcodeTreeFragment");
		}
		if (mBarcodeTreeFragment == null) {
			mBarcodeTreeFragment = new TreeScanTreeFragment();
		}
		
		getFragmentManager().beginTransaction().replace(R.id.treescan_barcodetree_frame, mBarcodeTreeFragment).commit();
	}
    
    private void setLayout() {
    	// 운용조직
    	//mOrgInputbar = (LinearLayout) findViewById(R.id.treescan_organization_inputbar);
    	 
    	mOrgCodeText = (EditText) findViewById(R.id.treescan_organization_orgCode);
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);
    	 

        // 접수조직
    	mReceiptOrgInputbar = (LinearLayout) findViewById(R.id.treescan_receiptOrganization_inputbar);
    	mReceiptOrgCodeText = (EditText) findViewById(R.id.treescan_receiptOrganization_orgCode);
    	mReceiptOrgSearchButton = ((Button) findViewById(R.id.treescan_receiptOrganization_search_button));
        mReceiptOrgSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showSelectOrgCodeActivity();
					}
				});
    	
    	// 위치코드 
        mLocInputbar = (LinearLayout) findViewById(R.id.treescan_loc_inputbar);
        mLocCdLevel = (TextView) findViewById(R.id.treescan_location_locCd_lavel);
        mLocCdText = (EditText) findViewById(R.id.treescan_location_locCd);
        //test : matsua
        //mLocCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
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
							Log.i(TAG, "위치바코드 Chang Event  barcode==>" + barcode);
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
                	Log.i(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeLocCd(barcode);
                    return true;
                }
                return false;
            }
        });
        
        // 위치바코드 주소 정보
        mAddInfo = ((Button) findViewById(R.id.addInfoBtn));
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
        
        // 장치아이디 주소 정보
        Button mAddInfo_deviceId = ((Button) findViewById(R.id.addInfoBtn_deviceId));
        mAddInfo_deviceId.setOnClickListener(new View.OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if(mDeviceIdText.getText().toString().length() < 1){
					return;
				}
        		
        		Intent intent = new Intent(getApplicationContext(), AddrInfoActivity.class);
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARCD, pLocCd_deviceId);
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARNM, pLocNm_deviceId);
				intent.putExtra(AddrInfoActivity.INPUT_GUBUN, "장치바코드");
				startActivity(intent);
        	}
        });
        
        // 위치코드명        
        mLocNameText = (EditText) findViewById(R.id.treescan_locInfo_locName);


        // GPS 위치데이타        
        mGPSLocDataText = (EditText) findViewById(R.id.treescan_gps_currentGPS);
        mGPSLocDataText.setSelected(true);

        // WBS번호
        mWbsInputbar = (LinearLayout) findViewById(R.id.treescan_wbs_inputbar);
        mWbsNoText = (EditText) findViewById(R.id.treescan_wbs_wbsNo);
        
        // 장치바코드
        mDeviceInputbar = (LinearLayout) findViewById(R.id.treescan_device_inputbar);
        
        if(mJobGubun.equals("고장정보")||mJobGubun.equals("고장수리이력")){
			mFacCdText = (EditText) findViewById(R.id.treescan_fac_facCd_failure);
			mDeviceIdText = (EditText) findViewById(R.id.treescan_device_failure);
			((LinearLayout)findViewById(R.id.dev_header)).setVisibility(View.GONE);
		}
		else{
			mFacCdText = (EditText) findViewById(R.id.treescan_fac_facCd);
			mDeviceIdText = (EditText) findViewById(R.id.treescan_device_deviceId);
		}
        
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
							Log.i(TAG, "장치바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeDeviceId(barcode);
						}
					}
				});
        mDeviceIdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.i(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeDeviceId(barcode);
                    return true;
                }
                return false;
            }
        });

        mOprSysCdText = (TextView) findViewById(R.id.treescan_device_oprSysCd);
        mDeviceIdInfoText = (EditText) findViewById(R.id.treescan_deviceInfo_deviceInfo);

		// 고장코드
		mCboCodeInputbar = (LinearLayout) findViewById(R.id.treescan_cboCode_inputbar);
		mSpinnerCboCode = (Spinner) findViewById(R.id.treescan_cboCode_spinner);
		List<SpinnerInfo> cbospinneritems = new ArrayList<SpinnerInfo>();
		cbospinneritems.add(new SpinnerInfo("", "선택하세요."));
		cbospinneritems.add(new SpinnerInfo("Z001", "Z001:이상로그발생"));
		cbospinneritems.add(new SpinnerInfo("Z002", "Z002:H/W FAULT(동작불가)"));
		cbospinneritems.add(new SpinnerInfo("Z003", "Z003:절체 불량"));
		cbospinneritems.add(new SpinnerInfo("Z004", "Z004:행업"));
		cbospinneritems.add(new SpinnerInfo("Z005", "Z005:서비스 불가"));
		cbospinneritems.add(new SpinnerInfo("Z006", "Z006:POWER 불량"));
		cbospinneritems.add(new SpinnerInfo("Z007", "Z007:FAN 불량"));
		cbospinneritems.add(new SpinnerInfo("Z008", "Z008:낙뢰피해"));
		cbospinneritems.add(new SpinnerInfo("Z999", "Z999:기타"));
		mCboCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), cbospinneritems);
		mSpinnerCboCode.setAdapter(mCboCodeAdapter);
		mSpinnerCboCode.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	//-----------------------------------------------------
					// 단지 아래에서 변경 정보 체크하기 위해서..
					//-----------------------------------------------------
	            	SpinnerInfo cboCodeSpinnerInfo = (SpinnerInfo) mSpinnerCboCode.getSelectedItem();
	            	mTouchCboCode = cboCodeSpinnerInfo.getCode();
	            	
		            return false;
	            }
	            return false;
			}
		});
		mSpinnerCboCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo cboCodeSpinnerInfo = (SpinnerInfo) mCboCodeAdapter.getItem(position);

	        	setJobWorkCboCode(cboCodeSpinnerInfo.getCode());
	        }
	        public void onNothingSelected(AdapterView<?> arg0) { }
	    });
		
		mCboReasonInputbar = (LinearLayout) findViewById(R.id.treescan_cboReason_inputbar);
		mCboReasonText = (EditText) findViewById(R.id.treescan_cboReason);
		mCboReasonText.addTextChangedListener(
				new TextWatcher(){
			        public void afterTextChanged(Editable s) {
			        	if (!s.toString().isEmpty()) {
			        		//-----------------------------------------------------
							// 고장내역 변경이면 정보 수정으로 본다.
							//-----------------------------------------------------
							GlobalData.getInstance().setChangeFlag(true);
							GlobalData.getInstance().setSendAvailFlag(true);
			        	}
			        }
			        public void beforeTextChanged(CharSequence s, int start, int count, int after){
			        }
			        public void onTextChanged(CharSequence s, int start, int before, int count){
			        	if(s.length() == 80){
			        		Toast.makeText(TreeScanActivity.this, "고장내역은 80자 이내로 입력하세요.", 500).show();
			        	}
			        }
			    });
//		mCboPhotoImageView  = (ImageView) findViewById(R.id.treescan_cboPhoto);
//		mCboPhotoImageView.setOnClickListener(
//    			new View.OnClickListener() {
//		    		@Override
//		    		public void onClick(View v) {
//		    			showImageSelectionMenu(v);
//		    		}
//		    	});

		// 형상 구성
		mHierachyInputbar = (LinearLayout) findViewById(R.id.treescan_Hierachy_inputbar);
		mHierachyLevel = (TextView) findViewById(R.id.treescan_fac_Hierachy_lavel);
		mHierachyCheck = (CheckBox) findViewById(R.id.treescan_crud_Hierachy);
		mHierachyCheck.setOnClickListener(
    			new View.OnClickListener() {
		    		@Override
		    		public void onClick(View v) {
		    			Log.i(TAG, "mHierachyCheck   click==>" + mHierachyCheck.isChecked());
		    			setJobWorkHierachyCheck(mHierachyCheck.isChecked());
		    		}
		    	});
		//mHierachyCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    //    @Override
	    //    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
	    //    	setJobWorkHierachyCheck(isChecked);
	    //    }
	    //});

		// 설비바코드
        //mFacInputbar = (LinearLayout) findViewById(R.id.treescan_fac_inputbar);
		//test : matsua
		//mFacCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
        mFacCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	Toast toast;
			            switch(event.getAction()) {
				            case MotionEvent.ACTION_DOWN:
								mScannerHelper.focusEditText(mFacCdText);
				                return true;
				            }
			            return true;
			        }
			    });
        mFacCdText.addTextChangedListener(
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
							Log.i(TAG, "설비바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeFacCd(barcode.trim());
						}
					}
				});
        mFacCdText.setOnEditorActionListener(
        		new TextView.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	String barcode = v.getText().toString().trim();
		                	Log.i(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
		                	if (barcode.isEmpty()) return true;
		                	changeFacCd(barcode.trim());
		                    return true;
		                }
		                return false;
		            }
		        });
        
        mPartTypeText = (TextView) findViewById(R.id.treescan_fac_partType);
        mUUCheck = (CheckBox) findViewById(R.id.treescan_fac_UU);
        mUUCheck.setOnClickListener(
    			new View.OnClickListener() {
		    		@Override
		    		public void onClick(View v) {
		    			Log.i(TAG, "mUUCheck   click==>" + mUUCheck.isChecked());
		    			setJobWorkUUCheck(mUUCheck.isChecked());
		    		}
		    	});
        
        mDeltailButton = (Button) findViewById(R.id.treescan_fac_search_button);
        mDeltailButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String facCd = mFacCdText.getText().toString().trim();
						if (facCd.isEmpty()) {
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드를 스캔하세요."));
							return;
						}
						Intent intent = new Intent(getApplicationContext(), SelectFacDetailActivity.class);
						intent.putExtra(SelectFacDetailActivity.INPUT_FAC_CD, facCd);
				        startActivity(intent);
					}
				});
        
        mFailureButton = (Button) findViewById(R.id.treescan_fac_failure_button);
        mFailureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String facCd = mFacCdText.getText().toString().trim();
						if (facCd.isEmpty()) {
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드를 스캔하세요."));
							return;
						}
						Intent intent = new Intent(getApplicationContext(), SelectFacDetailActivity.class);
						intent.putExtra(SelectFacDetailActivity.INPUT_FAC_CD, facCd);
				        startActivity(intent);
					}
				});
        
        // 상위바코드
        mUFacInputbar = (LinearLayout) findViewById(R.id.treescan_high_inputbar);
        mUFacCdText = (EditText) findViewById(R.id.treescan_high_UFacCd);
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
							Log.i(TAG, "상위바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeUFacCd(barcode.trim());
						}
					}
				});
        mUFacCdText.setOnEditorActionListener(
        		new TextView.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	String barcode = v.getText().toString().trim();
		                	Log.i(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
		                	if (barcode.isEmpty()) return true;
		                	changeUFacCd(barcode.trim());
		                    return true;
		                }
		                return false;
		            }
		        });
        
        mUPartTypeText = (TextView) findViewById(R.id.treescan_fac_UPartType);
        
		// 설비상태 Spinner 셋팅
        mFacStatusInputbar = (LinearLayout) findViewById(R.id.treescan_zp_inputbar);
		mSpinnerFacStatus = (Spinner) findViewById(R.id.treescan_zp_zpStatus);
		List<SpinnerInfo> facspinneritems = new ArrayList<SpinnerInfo>();
		if (mJobGubun.equals("입고(팀내)") || mJobGubun.equals("접수(팀간)") || mJobGubun.equals("설비상태변경")||mJobGubun.equals("고장등록")) {
			if (mJobGubun.equals("입고(팀내)")) {
				facspinneritems.add(new SpinnerInfo("0100", "유휴"));
				facspinneritems.add(new SpinnerInfo("0110", "예비"));
				facspinneritems.add(new SpinnerInfo("0200", "불용대기"));
			} else if (mJobGubun.equals("설비상태변경")||mJobGubun.equals("고장등록")) {
				facspinneritems.add(new SpinnerInfo("0100", "유휴"));
				facspinneritems.add(new SpinnerInfo("0110", "예비"));
				facspinneritems.add(new SpinnerInfo("0070", "미운용"));
				facspinneritems.add(new SpinnerInfo("0200", "불용대기"));
				
			} else if (mJobGubun.equals("접수(팀간)")) {
				facspinneritems.add(new SpinnerInfo("0100", "유휴"));
				facspinneritems.add(new SpinnerInfo("0110", "예비"));
			}
		}
		mFacStatusAdapter = new BasicSpinnerAdapter(getApplicationContext(), facspinneritems);
		mSpinnerFacStatus.setAdapter(mFacStatusAdapter);
		mSpinnerFacStatus.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	
	            	//-----------------------------------------------------
					// 단지 아래에서 변경 정보 체크하기 위해서..
					//-----------------------------------------------------
	            	SpinnerInfo facStatusSpinnerInfo = (SpinnerInfo) mSpinnerFacStatus.getSelectedItem();
	            	mTouchFacStatusCode = facStatusSpinnerInfo.getCode();
	            	
		            return false;
	            }
	            return false;
			}
		});
		mSpinnerFacStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo facStatusSpinnerInfo = (SpinnerInfo) mFacStatusAdapter.getItem(position);

	        	setJobWorkFacStatus(facStatusSpinnerInfo.getCode());
	        }
	        public void onNothingSelected(AdapterView<?> arg0) { }
	    });
        
        // 스캔필수
        mChkScanCheck = (CheckBox) findViewById(R.id.treescan_crud_chkScan);
    	mChkScanCheck.setOnClickListener(
    			new View.OnClickListener() {
		    		@Override
		    		public void onClick(View v) {
		    			changeChkScan();
		    		}
		    	});
        
        // 버튼 ----------------------------------------------------------------------------------
    	mCrudNoneInputbar = (LinearLayout) findViewById(R.id.treescan_crud_none_buttonbar);
    	mCrudEditModeInputbar = (LinearLayout) findViewById(R.id.treescan_crud_editmode_buttonbar);
    	mEditModeComment = (TextView) findViewById(R.id.treescan_crud_editmodeComment);
    	
        // 초기화        
        mInitButton = (Button) findViewById(R.id.treescan_crud_init);        
        mInitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//-----------------------------------------------------------
				        // 작업관리에서 호출이면 여기서 시작한다.
				        //-----------------------------------------------------------
				        GlobalData.getInstance().initJobActionManager();
				        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_GENERAL) {
				        	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
				        }
						
						initScreen();
					}
				});

        // 스캔취소     
        mCancelScanButton = (Button) findViewById(R.id.treescan_crud_cancelScan);        
        mCancelScanButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						cancelScanData();
					}
				});
        
        // 이동     
        mMoveButton = (Button) findViewById(R.id.treescan_crud_move);        
        mMoveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						moveData();
					}
				});
        
        // 수정모드 취소
        mCancelEditModeButton = (Button) findViewById(R.id.treescan_crud_cancelEditMode);        
        mCancelEditModeButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						cancelEditMode();
					}
				});
        
        // 삭제        
        mDeleteButton = (Button) findViewById(R.id.treescan_crud_delete);        
        mDeleteButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						deleteData();
					}
				});

        // 저장        
        mSaveButton = (Button) findViewById(R.id.treescan_crud_save);        
        mSaveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveWorkData();
					}
				});

        // 전송        
        mSendButton = (Button) findViewById(R.id.treescan_crud_send);        
        mSendButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {

				    	SendCheck sendCheck = new SendCheck();
				    	sendCheck.orgCode = SessionUserData.getInstance().getOrgId();
				    	
				    	String receiptOrgInfo = mReceiptOrgCodeText.getText().toString().trim();
				    	if (receiptOrgInfo.isEmpty()) {
				    		sendCheck.receiptOrgCode = "";
				    	} else {
				    		sendCheck.receiptOrgCode = receiptOrgInfo.split("/")[0];
				    	}

				    	if (mThisLocCodeInfo == null) {
				    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
				    	} else {
				    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
				    	}
				    	
				    	
				    	if (mDeviceInputbar.getVisibility() == View.VISIBLE) {
				    		sendCheck.isDeviceId = true;
				    	} else {
				    		sendCheck.isDeviceId = false;
				    	}
				    	sendCheck.deviceId = mDeviceIdText.getText().toString().trim();
				    	sendCheck.facCd = mFacCdText.getText().toString().trim();
				    	sendCheck.partType = mPartTypeText.getText().toString().trim();
				    	
				    	if (mUFacInputbar.getVisibility() == View.VISIBLE) {
				    		sendCheck.isUFacCd = true;
				    	} else {
				    		sendCheck.isUFacCd = false;
				    	}
				    	sendCheck.UFacCd = mUFacCdText.getText().toString().trim();
				    	sendCheck.uPartType = mUPartTypeText.getText().toString().trim();
				    	sendCheck.UFacCd_DeviceId_Yn = null;
				    	sendCheck.instConfPartType = getBarcodeTreeAdapter().getInstConfPartType();
				    	sendCheck.notScanYn = null;
				    	sendCheck.instConfInheritanceYn = null;
				    	
				    	// 설비상태코드
				    	if (mJobGubun.equals("입고(팀내)") || mJobGubun.equals("접수(팀간)")|| mJobGubun.equals("설비상태변경")) {
				    		if (mFacStatusAdapter.getCount() > 0) {
				    			SpinnerInfo facStatusSpinnerInfo = (SpinnerInfo) mFacStatusAdapter.getItem(mSpinnerFacStatus.getSelectedItemPosition());
				    			sendCheck.facStatusCode = facStatusSpinnerInfo.getCode();
				    		}
				    	}

				    	// 고장코드
				    	if (mCboCodeAdapter.getCount() > 0) {
				    		SpinnerInfo cboSpinnerInfo = (SpinnerInfo) mCboCodeAdapter.getItem(mSpinnerCboCode.getSelectedItemPosition());
				    		sendCheck.cboCode = cboSpinnerInfo.getCode();
				    	}

				    	//-----------------------------------------------------
				    	// 전송데이터 유효성 체크...
				    	//-----------------------------------------------------
						executeSendCheck(sendCheck);
					}
				});
        
        mFailureInit = (Button) findViewById(R.id.treescan_info_init);        
        mFailureInit.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mFacCdText.setText("");
						mDeviceIdText.setText("");
						
						((EditText) findViewById(R.id.treescan_fac_facNm_failure)).setText("");
						((EditText) findViewById(R.id.treescan_fac_facCode_failure)).setText("");
						((EditText) findViewById(R.id.treescan_device_nm_failure)).setText("");
						
						mFailureListAdapter.itemClear();
						
						mScannerHelper.focusEditText(mFacCdText);
					}
				});
        
        mDivInfo = (Button) findViewById(R.id.treescan_device_info_button);        
        mDivInfo.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(mDeviceIdText.getText().toString().length() < 1){
							return;
						}
		        		
		        		Intent intent = new Intent(getApplicationContext(), AddrInfoActivity.class);
						intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARCD, pLocCd_deviceId);
						intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARNM, pLocNm_deviceId);
						intent.putExtra(AddrInfoActivity.INPUT_GUBUN, "장치바코드");
						startActivity(intent);
					}
				});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return false;
		if (item.getItemId()==android.R.id.home) {
			if (!mJobGubun.equals("설비정보")) {
	    		if (GlobalData.getInstance().isChangeFlag()) {
					changeFlagYesNoDialog();
					return true;
		        }
	        }
			finish();
		} else {
        	return true;
        }
	    return false;
    }
	
	@Override
	public void onBackPressed() {
		if (GlobalData.getInstance().isGlobalProgress()) return;
		
		if (!mJobGubun.equals("설비정보")) {
    		if (GlobalData.getInstance().isChangeFlag()) {
				changeFlagYesNoDialog();
				return;
	        }
        }
		
		super.onBackPressed();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		//-----------------------------------------------------------
		// Activity종료시 작업ID는 초기화한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
		
		super.onDestroy();
	}
	
	private void changeFlagYesNoDialog() {
		// ---------------------------------------------------------------------
		// 전송여부 최종 확인..
		// ---------------------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		String message = "수정한 자료가 존재합니다.\n\r저장하지 않고 종료하시겠습니까?";

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
				//-----------------------------------------------------------
			    // Dialog화면 호출시 Open/Close 처리하는 Flag
			    //-----------------------------------------------------------
				GlobalData.getInstance().setGlobalAlertDialog(false);
				
				//-----------------------------------------------------------
			    // 화면 초기 Setting후 변경 여부 Flag를 초기화 하고..
				// Ativity를 종료한다.
			    //-----------------------------------------------------------
				GlobalData.getInstance().setChangeFlag(false);
				finish();
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
		return;
	}
	
	protected void breakDownYesNoDialog(String barcode, int max) {
		
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		String message = "설비 " +  barcode  +" 는\n\r고장 기준수( " + max + "회)를\n\r초과한 설비입니다.\n\r처분(불용대기) 하시려면 '예'\n\r고장등록 하시려면 '아니오'를\n\r선택하세요.";

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
				GlobalData.getInstance().setJobGubun("설비상태변경");
				mJobGubun = GlobalData.getInstance().getJobGubun();

				setMenuLayout();
		        setFieldVisibility();
		        String fcd = mFacCdText.getText().toString();
		        initScreen("fac");
		        mFacCdText.setText(fcd);
		        mSpinnerFacStatus.setSelection(3);
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
	
    private void setFieldVisibility() {
    	//-----------------------------------------------------------
		// view change
		//-----------------------------------------------------------
    	if(mJobGubun.equals("설비상태변경")){
    		mCboCodeInputbar.setVisibility(View.GONE);
			mCboReasonInputbar.setVisibility(View.GONE);
			mFacStatusInputbar.setVisibility(View.VISIBLE);
			mChkScanCheck.setEnabled(true);
    	}
		//-----------------------------------------------------------
		// 접수조직BAR 
		//-----------------------------------------------------------
    	if(mJobGubun.equals("고장정보")||mJobGubun.equals("고장수리이력")){
    		((LinearLayout) findViewById(R.id.treescan_organization_inputbar)).setVisibility(View.GONE);
    		((LinearLayout) findViewById(R.id.treescan_fac_inputbar)).setVisibility(View.GONE);
    	}
    	
		if (!mJobGubun.equals("송부(팀간)") && !mJobGubun.equals("접수(팀간)") && !mJobGubun.equals("철거")) {
    		mReceiptOrgInputbar.setVisibility(View.GONE);
    	}
		
		if (mJobGubun.equals("접수(팀간)")) {
			((TextView) findViewById(R.id.treescan_receiptOrganization_orgCode_lavel)).setText("송부조직");
		}
		
		//-----------------------------------------------------------
		// 위치바코드BAR 
		//-----------------------------------------------------------
		if (mJobGubun.equals("출고(팀내)") || mJobGubun.equals("탈장") || mJobGubun.equals("송부(팀간)") 
				|| mJobGubun.equals("설비상태변경")
				|| mJobGubun.equals("철거") && SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")
				|| mJobGubun.equals("설비정보")
				|| mJobGubun.equals("고장정보")
				|| mJobGubun.equals("고장수리이력")
				|| mJobGubun.equals("형상해제(창고내)")
		) {
			// 위치바코드
			mLocInputbar.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.treescan_locInfo_inputbar)).setVisibility(View.GONE);

			// GPS현재주소
			((LinearLayout) findViewById(R.id.treescan_gps_inputbar)).setVisibility(View.GONE);
    	}
		
		if (mJobGubun.equals("납품취소") || mJobGubun.equals("고장등록") || mJobGubun.equals("고장수리이력")) {
			// 위치바코드
			mLocInputbar.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.treescan_locInfo_inputbar)).setVisibility(View.GONE);

			// GPS현재주소
			((LinearLayout) findViewById(R.id.treescan_gps_inputbar)).setVisibility(View.GONE);
			
			// 스캔필수 disable
			mChkScanCheck.setEnabled(false);
		}
		
		//DR-2013-57935 : 송부취소(팀간)에서 위치코드 스캔 추가 - request by 김희선 2014.06.03 - modify by 양혜진,정수연,최미소  2014.06.03
		if(mJobGubun.equals("송부취소(팀간)")){
			// 위치바코드
			/*
			mLocInputbar.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.treescan_locInfo_inputbar)).setVisibility(View.GONE);

			// GPS현재주소
			((LinearLayout) findViewById(R.id.treescan_gps_inputbar)).setVisibility(View.GONE);
			*/
			// 스캔필수 disable
			mChkScanCheck.setEnabled(false);
		}
		
		if (mJobGubun.equals("접수(팀간)")) {
			// 스캔필수 disable
			mChkScanCheck.setEnabled(false);
		}

		// 납품입고는 스캔필수 체크박스 없음
		if (mJobGubun.equals("납품입고")) {
			mChkScanCheck.setVisibility(View.GONE);
		}

		if (mJobGubun.equals("배송출고")) {
			mLocCdLevel.setText("수신위치");
		}

		// 형상구성 - 납품입고와 배송출고만 형상을구성함(배송오더) 체크박스가 있음
		if (!mJobGubun.equals("납품입고") && !mJobGubun.equals("배송출고")) {
			mHierachyInputbar.setVisibility(View.GONE);
		}
		if (mJobGubun.equals("배송출고")) {
			mHierachyLevel.setText("배송오더");
			mHierachyCheck.setText("생성");
		}
		// 고장코드
		if (!mJobGubun.equals("고장등록")) {
			mCboCodeInputbar.setVisibility(View.GONE);
			mCboReasonInputbar.setVisibility(View.GONE);
		}

		//-----------------------------------------------------------
		// WBS BAR 
		//-----------------------------------------------------------
		if (!mJobGubun.equals("철거") || SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) {
			mWbsInputbar.setVisibility(View.GONE);
		}

		//-----------------------------------------------------------
		// 장치바코드BAR 
		//-----------------------------------------------------------
		if (!mJobGubun.equals("실장")) {
    		// 장치바코드, 장치바코드INFO
    		mDeviceInputbar.setVisibility(View.GONE);
        	((LinearLayout) findViewById(R.id.treescan_deviceInfo_inputbar)).setVisibility(View.GONE);
    	}
		
		if (!mJobGubun.equals("납품입고") && !mJobGubun.equals("실장") && !mJobGubun.equals("형상구성(창고내)")) {
			//설비바코드 U-U
			mUUCheck.setVisibility(View.GONE);
		}
		if (!mJobGubun.equals("설비정보")) {
			//설비상세조회
			mDeltailButton.setVisibility(View.GONE);
		}		
		
		//-----------------------------------------------------------
		// 설비상태BAR 
		//-----------------------------------------------------------
		if (!mJobGubun.equals("입고(팀내)") && !mJobGubun.equals("설비상태변경") && !mJobGubun.equals("접수(팀간)")) {
			// 설비상태BAR
			mFacStatusInputbar.setVisibility(View.GONE);
    	}
    	
		//-----------------------------------------------------------
		// 상위바코드BAR 
		//-----------------------------------------------------------
    	if (!mJobGubun.equals("실장") && !mJobGubun.equals("형상구성(창고내)")) {
			// 상위바코드BAR
			mUFacInputbar.setVisibility(View.GONE);
		}
    	
    	
    	if(mJobGubun.equals("형상구성(창고내)")){
    		mChkScanCheck.setChecked(true);
    		mChkScanCheck.setEnabled(false);
    	}
    	
    	if(mJobGubun.equals("형상해제(창고내)")){
    		mChkScanCheck.setChecked(true);
    		mChkScanCheck.setEnabled(true);
    	}
    	
    	//-----------------------------------------------------------
		// CRUD버튼BAR 
    	//-----------------------------------------------------------
    	if (mJobGubun.equals("설비정보") || mJobGubun.equals("고장정보") || mJobGubun.equals("고장수리이력")) {
			// CRUD버튼BAR
			((LinearLayout) findViewById(R.id.treescan_crud_buttonbar)).setVisibility(View.GONE);
		}
    	if (mJobGubun.equals("송부취소(팀간)") || mJobGubun.equals("접수(팀간)")) {
    		mCancelScanButton.setVisibility(View.VISIBLE);
    		mMoveButton.setVisibility(View.GONE);
    		mDeleteButton.setVisibility(View.GONE);
    	} else if (mJobGubun.equals("납품입고")) {
    		mCancelScanButton.setVisibility(View.GONE);
    		mMoveButton.setVisibility(View.VISIBLE);
    		mDeleteButton.setVisibility(View.VISIBLE);
    	} else if (mJobGubun.equals("납품취소")) {
    		mDeleteButton.setVisibility(View.GONE);
    	} else {
    		mCancelScanButton.setVisibility(View.GONE);
    		mMoveButton.setVisibility(View.GONE);
    		mDeleteButton.setVisibility(View.VISIBLE);
    	}
    	
    	if(mJobGubun.equals("고장정보") || mJobGubun.equals("고장수리이력")){
    		((LinearLayout) findViewById(R.id.failure_fac_box)).setVisibility(View.VISIBLE);
    	}
    	
    	if(mJobGubun.equals("고장수리이력")){
    		((LinearLayout) findViewById(R.id.listLinear)).setVisibility(View.VISIBLE);
    		((Button) findViewById(R.id.treescan_fac_failure_button)).setVisibility(View.GONE);
    		
    		mFailureListAdapter = new FailureListAdapter(getApplicationContext());
            mFailureListView = (ListView) findViewById(R.id.listView);
            mFailureListView.setAdapter(mFailureListAdapter);
		}
    }
    
    private void initScreen() {
    	initScreen("all");
        
    	if (!SessionUserData.getInstance().isOffline()) {
    		// 납품입고일 경우 조직 논리 창고 자동 셋팅
    		if (mJobGubun.equals("납품입고")) {
    			getLogicalLocationData();
    		} else if (mJobGubun.equals("송부취소(팀간)") || mJobGubun.equals("접수(팀간)")) {
        		getMoveRequestData("", "");
        	}
    	}
    }
    
    private void initScreen(String step) {
    	if (step.equals("all") || step.equals("base")) {
			GlobalData.getInstance().setGlobalProgress(false);
			GlobalData.getInstance().setGlobalAlertDialog(false);
			GlobalData.getInstance().setChangeFlag(false);
			GlobalData.getInstance().setSendAvailFlag(false);
		}
    	if (step.equals("all") || step.equals("loc")) {
    		// 송부(팀간), 철거는 접수조직 클리어 하지 않음 
    		if (mJobGubun.equals("접수(팀간)")) {
    			mReceiptOrgCodeText.setText("");
    		}
    		mLocCdText.setText("");
    		mLocNameText.setText("");
    		mThisLocCodeInfo = null;  // 현재 스캔한 위치바코드 정보 초기화.
    	}
    	if (step.equals("all") || step.equals("wbs")) {
    		mWbsNoText.setText("");
    	}
    	if (step.equals("all") || step.equals("device")) {
    		mDeviceIdText.setText("");
    		mDeviceIdText.setEnabled(true);
    		mOprSysCdText.setText("");
    		mDeviceIdInfoText.setText("");
	    	pLocCd_deviceId=pLocNm_deviceId="";	// 위치정보 초기화 
    	}
    	if (step.equals("all") || step.equals("cbo")) {
    		// 고장코드
        	if (mCboCodeInputbar.getVisibility()==View.VISIBLE) {
        		mSpinnerCboCode.setSelection(0);
        	}
        	mCboReasonText.setText("");
//        	mCboPhotoImageView.setImageResource(R.drawable.common_button_bg_red);
    	}
    	if (step.equals("all") || step.equals("fac")) {
    		mFacCdText.setText("");
        	mPartTypeText.setText("");
        	mUUCheck.setChecked(false);
    	}
    	if (step.equals("all") || step.equals("ufac")) {
    		mUFacCdText.setText("");
    		mUFacCdText.setEnabled(true);
//    		if(mJobGubun.equals("형상구성(창고내)")){
//    			mUFacCdText.setEnabled(false);
//    		}
    		mUPartTypeText.setText("");
    		mThisUFacInfo = null;    // 현재 스캔한 상위바코드 정보 초기화.
    	}
    	if (step.equals("all") || step.equals("facstatus")) {
    		if (mFacStatusInputbar.getVisibility()==View.VISIBLE) {
    			if(mJobGubun.equals("입고(팀내)") || mJobGubun.equals("접수(팀간)") || mJobGubun.equals("설비상태변경")){
    				mSpinnerFacStatus.setSelection(0);
    			}else{
    				mSpinnerFacStatus.setSelection(1);		// 설비상태
    			}
    		}
    	}
    	if (step.equals("all") || step.equals("hierarchy")) {
    		mHierachyCheck.setChecked(!mJobGubun.equals("납품입고"));   // 형상구성 - 납품입고는 default 해제
    		mChkScanCheck.setChecked(true);    // 스캔필수
    		mFirstBarcodeListInfos = null;     // 최초스캔한 설비바코드 목록.
    	}
    	if (step.equals("all") || step.equals("crudbar")) {
    		mDeleteButton.setEnabled(true);
    		mSendButton.setEnabled(true);
    	}
    	if (step.equals("all") || step.equals("editmode")) {
    		setEditMode(EDIT_MODE_NONE);
    		mCrudNoneInputbar.setVisibility(View.VISIBLE);
    		mCrudEditModeInputbar.setVisibility(View.GONE);
    		mEditModeComment.setText("");
    		mEditTreeKey = null;
    		mEditBarcodeInfo = null;
    	}
    	if (step.equals("all") || step.equals("tree")) {
    		initScreenTreeFragment();
    		setTextBoxEnable();
       	}
    	if (step.equals("all") || step.equals("scanfocus")) {
    		// 초기에는 위치바코드를 스케너Focus 지정한다.
        	if (getLocInputbarVisibility()==View.VISIBLE) {
        		mScannerHelper.focusEditText(mLocCdText);
        	} else  {
        		mScannerHelper.focusEditText(mFacCdText);
        	}
       	}
    }
    
    /**
     * 여기서부터 BarcodeTreeFragment 연계 메서드들..
     */
    public void getMoveRequestData(String barcode, String KOSTL) {
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			
			initScreen("tree");
			
			// 송부취소(팀간), 접수(팀간) 초기에 자료를 조회한다.
			fragment.getMoveRequestData(barcode, KOSTL);
    	}
    }
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
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
    
    private void doChkScan(String locCd, String deviceId, String barcode, String iFlag, boolean onlyFacFlag) {
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			fragment.doChkScan(locCd, deviceId, barcode, iFlag, onlyFacFlag);
    	}
    }
    private void addScanData(String locCd, String deviceId, String barcode, String iFlag, boolean onlyFacFlag) {
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			fragment.addScanData(locCd, deviceId, barcode, iFlag, onlyFacFlag);
    	}
    }
    private BarcodeTreeAdapter getBarcodeTreeAdapter() {
    	BarcodeTreeAdapter barcodeTreeAdapter = null;
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			barcodeTreeAdapter = fragment.getBarcodeTreeAdapter();
    	}
		return barcodeTreeAdapter;
    }
    private TreeViewList getBarcodeTreeView() {
    	TreeViewList treeViewList = null;
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			treeViewList = fragment.getBarcodeTreeView();
    	}
		return treeViewList;
    }
    private void initScreenTreeFragment() {
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			fragment.initScreenTreeFragment();
    	}
    }
    private void showSummaryCount() {
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			fragment.showSummaryCount();
    	}
    }
    private void addBarcodeInfosTreeView(String goto_barcode, final List<BarcodeListInfo> barcodeInfos) {

    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			fragment.addBarcodeInfosTreeView(goto_barcode, barcodeInfos);
    	}
    }
    public String getCheckParentBarcodeInfo(BarcodeListInfo addBarcodeInfo) {
    	String parentBarcode = "";
    	
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			parentBarcode = fragment.getCheckParentBarcodeInfo(addBarcodeInfo);
    	}
		return parentBarcode;
    }
    public String compareTargetHierarchy(BarcodeListInfo selectBarcodeInfo, BarcodeListInfo targetBarcodeInfo) {
    	String parentBarcode = "";
    	
    	TreeScanTreeFragment fragment = (TreeScanTreeFragment) getFragmentManager().findFragmentById(R.id.treescan_barcodetree_frame);
		if (fragment != null) {
			parentBarcode = fragment.compareTargetHierarchy(selectBarcodeInfo, targetBarcodeInfo);
    	}
		return parentBarcode;
    }
    public void setFirstBarcodeListInfos(List<BarcodeListInfo> barcodeListInfos) {
    	mFirstBarcodeListInfos = barcodeListInfos;
    }
    /**
     * 여기까지 BarcodeTreeFragment 연계 메서드들..
     */

    private void showSelectOrgCodeActivity() {
    	
    	if (mJobGubun.equals("접수(팀간)")) {
        	if (getBarcodeTreeAdapter().getCount() == 0) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "송부조직이 존재하지 않습니다."));
        		return;
        	}
        	
        	List<OrgCodeInfo> orgCodeInfos = new ArrayList<OrgCodeInfo>();
        	
        	List<Long> keyList = getBarcodeTreeAdapter().getAllNextNodeList();
    		for (Long key : keyList) {
    			BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
    			
        		boolean isOrg = false;
        		for (OrgCodeInfo checkOrgInfo : orgCodeInfos) {
        			if (checkOrgInfo.getCostCenter().equals(barcodeInfo.getSkostl())) {
        				isOrg = true;
        				break;
        			}
        		}
        		
        		// 중복되지 않은 정보만 추가한다.
        		if (!isOrg) {
        			OrgCodeInfo orgInfo = new OrgCodeInfo();
        			orgInfo.setOrgCode(barcodeInfo.getSkostl().substring(1));
        			orgInfo.setOrgName(barcodeInfo.getSkostlTxt());
        			orgInfo.setParentOrgCode("");
        			orgInfo.setCostCenter(barcodeInfo.getSkostl());
        			orgInfo.setLevel(0);
        			orgInfo.setSearched(true);
        			
        			orgCodeInfos.add(orgInfo);
        		}
			}
        	
        	// 조회된 부서간이동정보중 송부한 운용조직 정보만 중복제외하고 SelectOrgActivity에 보여주기 위해서.. 
 
        	
        	if (orgCodeInfos.size() == 0) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "송부조직이 존재하지 않습니다."));
        		return;
        	}
        	
        	
        	//-----------------------------------------------------------
        	// Json Object 변환한다.
        	//-----------------------------------------------------------
        	Gson gson = new Gson();
    		String jsonArrayString = gson.toJson(orgCodeInfos);
    		
    		if (jsonArrayString.isEmpty()) return;
    		
        	//-----------------------------------------------------------
        	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
        	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
        	//-----------------------------------------------------------
        	mScannerHelper.focusEditText(null);
        	
        	Intent intent = new Intent(getApplicationContext(), SelectOrgCodeActivity.class);
        	intent.putExtra(SelectOrgCodeActivity.INPUT_JSON_STRING, jsonArrayString);
    		startActivityForResult(intent, ACTION_SELECTORGCODEACTIVITY);
    	} else {
        	//-----------------------------------------------------------
        	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
        	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
        	//-----------------------------------------------------------
        	mScannerHelper.focusEditText(null);
    		
    		Intent intent = new Intent(getApplicationContext(), SelectOrgCodeActivity.class);
    		intent.putExtra(SelectOrgCodeActivity.INPUT_JSON_STRING, "");
    		startActivityForResult(intent, ACTION_SELECTORGCODEACTIVITY);
    	}
    }
    
    /**
     *  EDIT_MODE 상태
     */
    public int getEditMode() {
		return mEditMode;
	}
    /**
     *  EDIT_MODE 상태
     */
	public void setEditMode(int editMode) {
		this.mEditMode = editMode;
	}
	
	/**
     *  EDIT_MODE TreeKey
     */
	public Long getEditTreeKey() {
		return mEditTreeKey;
	}
	
	/**
     *  EDIT_MODE TreeKey
     */
	public void setEditTreeKey(Long editTreeKey) {
		this.mEditTreeKey = editTreeKey;
	}

	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public BarcodeListInfo getEditBarcodeInfo() {
		return mEditBarcodeInfo;
	}

	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public void setEditBarcodeInfo(BarcodeListInfo editBarcodeInfo) {
		this.mEditBarcodeInfo = editBarcodeInfo;
	}
	
	public void setJobWorkReceiptOrgCode(String receiptOrgInfo) {
		mReceiptOrgCodeText.setText(receiptOrgInfo);
		
		//-----------------------------------------------------------
		// 작업관리에 작업Task 등록...
		//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_RECEIPT_ORG, "orgCode", receiptOrgInfo);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}

		// 접수팀간이면 부서간이동조회 호출
		if (mJobGubun.equals("접수(팀간)")) {
			String costCenter = "";
			if (!receiptOrgInfo.isEmpty()) {
				costCenter = receiptOrgInfo.split("/")[0];
			}
			
			getMoveRequestData("", costCenter.trim());
		}
	}
    
    public LocBarcodeInfo getThisLocCodeInfo() {
		return mThisLocCodeInfo;
	}
    
    public int getLocInputbarVisibility() {
    	return mLocInputbar.getVisibility();
    }
    
    /**
	 * 위치바코드 정보 변경시 처리.
	 */
    private void changeLocCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineLocBarcodeData(barcode);
    	} else {
    		getLocBarcodeData(barcode);
    	}
    }
    
    public void setJobWorkWbsNo(String wbsNo) {
    	mWbsNoText.setText(wbsNo);

    	//-----------------------------------------------------------
    	// 인수,인계 조회 정보가 없어도 변경을 본다.
    	//-----------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    	
    	//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			return;
		} else {
    		try {
    			GlobalData.getInstance().getJobActionManager().setStepItem(
    					JobActionStepManager.JOBTYPE_WBS, "wbsNo", wbsNo);
    		} catch (ErpBarcodeException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    /**
     * 장치ID 변경시 장치바코드 조회.
     */
    public void changeDeviceId(String barcode) {
    	barcode = barcode.toUpperCase();
    	
		initScreen("device");
		if (getLocInputbarVisibility()==View.VISIBLE 
    			&& (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty())) {
    		initScreen("loc");
    		initScreen("wbs");
			
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mLocCdText);
			return;
		}
    	
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineDeviceBarcodeData(barcode);
    		
    	} else {
    		getDeviceBarcodeData(barcode);
    	}
    }
    
    public int getUFacInputbarVisibility() {
    	return mUFacInputbar.getVisibility();
    }
    
    public EditText getUFacCdText() {
    	return mUFacCdText;
    }
    
    /**
     * 설비바코드 정보 변경시 처리.
     */
    private void changeFacCd(String barcode) {
        barcode=barcode.toUpperCase();
    	
        if (mJobGubun.equals("납품취소") || mJobGubun.equals("설비정보")) { 
        	getBarcodeTreeAdapter().itemClear();
        	if (barcode.equals("1359")) {
        		GlobalData.getInstance().setAdminFlag(true);
        		return;
        	}
        }
        
        if(mJobGubun.equals("고장수리이력")){
        	if(barcode.length() < 16 || barcode.length() > 18){
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할수 없는 설비바코드 입니다."));
        		mFacCdText.setText("");
    			mScannerHelper.focusEditText(mFacCdText);
    			return;
        	}
        }
        
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineProductBarcodeData("fac", barcode);
       	} else {
    		if (getLocInputbarVisibility()==View.VISIBLE 
    				&& (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty())) {
    			initScreen("fac");
    			
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mLocCdText);
    			return;
    		}
    		
    		if (mJobGubun.equals("납품입고") || mChkScanCheck.isChecked()) {
            	if(!mJobGubun.equals("고장정보") && !mJobGubun.equals("고장수리이력")){
            		boolean onlyFacFlag = (!mJobGubun.equals("납품입고"));
            		doChkScan("", "", barcode, "H", onlyFacFlag);
            	}
            	else{
            		if(mJobGubun.equals("고장수리이력")){
            			((EditText) findViewById(R.id.treescan_fac_facNm_failure)).setText("");
            			((EditText) findViewById(R.id.treescan_fac_facCode_failure)).setText("");
            			((TextView) findViewById(R.id.listCount)).setText("");
    					mFailureListAdapter.itemClear();
    				}
            		getFacBarcodeDetailData(barcode);
            	}
        	} else {
        		addScanData("", "", barcode, "H", true);
        	}
    	}
    }
    
    /****************************************************************
	 * SAP장치바코드 정보 조회.
	 */
    private void getFacBarcodeDetailData(String facBarCode) {
		if (isBarcodeProgressVisibility()) return;
		
		if (mFindFacBarcodeDetailInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindFacBarcodeDetailInTask = new FindFacBarcodeDetailInTask(facBarCode);
			mFindFacBarcodeDetailInTask.execute((Void) null);
		}
	}
    
	private class FindFacBarcodeDetailInTask extends AsyncTask<Void, Void, Boolean> {
		private String _FacCd = "";
		private ErpBarcodeException _ErpBarException;
		
		public FindFacBarcodeDetailInTask(String facCd) {
			_FacCd = facCd;
			mFacJsonResult = null;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				InfoHttpController infohttp = new InfoHttpController();
				mFacJsonResult = infohttp.getFacBarcodeDetailData(_FacCd);
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "설비바코드상세정보 서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				mFacJsonResult = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindFacBarcodeDetailInTask = null;
			if (result) {
				showBarcodeInfoDisplay();
			} else {
				mFacJsonResult = null;
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindFacBarcodeDetailInTask = null;
			mFacJsonResult = null;
			setBarcodeProgressVisibility(false);
		}
	}
	
	/**
	 * 고장수리이력 정보조회.
	 */
	public void getFailureListData() {
		setBarcodeProgressVisibility(true);
		initScreen("tree");
		mFailureListAdapter.itemClear();
		mFailureListInTask = new FailureListInTask(mFacCdText.getText().toString(), mDeviceIdText.getText().toString());
		mFailureListInTask.execute((Void) null);
	}
	
	/****************************************************************
	 * 고장이력조회.
	 */
	private class FailureListInTask extends AsyncTask<Void, Void, Boolean> {
		private String _FacCd = "";
		private String _DevCd = "";
		private ErpBarcodeException _ErpBarException;
		
		public FailureListInTask(String facCd, String devCd) {
			_FacCd = facCd.trim();
			_DevCd = devCd.trim();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController listhttp = new LocationHttpController();
				mFailureListInfo = listhttp.getFailureList(_FacCd,_DevCd);
				if (mFailureListInfo == null) {
					throw new ErpBarcodeException(-1, "고장수리이력 정보가 없습니다. ");
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
			mFailureListInTask = null;
			if (result) {
				if(mFacCdText.getText().toString().length() == 0 && mDeviceIdText.getText().toString().length() > 0){
					mFailureListAdapter.setMode("dev");
					
					((LinearLayout) findViewById(R.id.fcc_header)).setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.dev_header)).setVisibility(View.VISIBLE);
				}else{
					mFailureListAdapter.setMode("fcc");
					
					((LinearLayout) findViewById(R.id.fcc_header)).setVisibility(View.VISIBLE);
					((LinearLayout) findViewById(R.id.dev_header)).setVisibility(View.GONE);
				}
				
				mFailureListAdapter.addItems(mFailureListInfo);
				((TextView) findViewById(R.id.listCount)).setText(mFailureListAdapter.getCount() + "건");
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFailureListInTask = null;
			setBarcodeProgressVisibility(false);
		}
	}
	
	public void showBarcodeInfoDisplay() {
		Log.d(TAG, "설비바코드상세정보 nextFacBarcodeDetail  Start...");
		if (mFacJsonResult == null) {
			return;
		}
		try {
			((EditText) findViewById(R.id.treescan_fac_facNm_failure)).setText(mFacJsonResult.getString("SUBMT"));
			((EditText) findViewById(R.id.treescan_fac_facCode_failure)).setText(mFacJsonResult.getString("MAKTX"));
			((EditText) findViewById(R.id.treescan_device_nm_failure)).setText("");
			mDeviceIdText.setText("");
			if(mFacJsonResult.getString("ZEQUIPGC").length() > 0){
				mDeviceIdText.setText(mFacJsonResult.getString("ZEQUIPGC"));
				if (mJobGubun.equals("고장수리이력")){
					isFacQuMode = true;
					getDeviceBarcodeData(mFacJsonResult.getString("ZEQUIPGC"));
				}
			}else{
				getFailureListData();
			}
		} catch (JSONException e) {
			Log.d(TAG, "설비바코드 상세 정보 필드에 대입중 오류가 발생했습니다. " + e.getMessage());
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드 상세 정보 필드에 대입중 오류가 발생했습니다. "));
			return;
		}
	}
    
    public boolean isUUCheck() {
    	return mUUCheck.isChecked();
    }
    
    public void setUUCheck(boolean checked) {
    	mUUCheck.setChecked(checked);
    }
    
    public void setJobWorkUUCheck(boolean checked) {
    	mUUCheck.setChecked(checked);
    	
    	//-----------------------------------------------------
		// UUCheck 이벤트 발생하면 정보 수정으로 본다.
		//-----------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
    	
    	//-----------------------------------------------------------
		// 단계별 작업을 여기서 처리한다.
		//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_UU_CHECK, "check_yn", (checked ? "Y" : "N"));
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    public void setJobWorkFacStatus(String facStatusCode) {
    	if (mTouchFacStatusCode == null)
    		return;
    	if (TextUtils.isEmpty(mTouchFacStatusCode) && TextUtils.isEmpty(mTouchFacStatusCode))
    		return;
    	if (mTouchFacStatusCode.equals(facStatusCode)) 
    		return;
    	
    	int position = mFacStatusAdapter.getPosition(facStatusCode);
    	mSpinnerFacStatus.setSelection(position);
    	
    	//-----------------------------------------------------
		// 고장코드 변경이면 정보 수정으로 본다.
		//-----------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    	
    	//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_FAC_STATUS, "facStatusCode", facStatusCode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		mTouchFacStatusCode = null;
    }
    
    public void setJobWorkCboCode(String cboCode) {
    	if (mTouchCboCode == null) 
    		return;
    	if (TextUtils.isEmpty(mTouchCboCode) && TextUtils.isEmpty(cboCode))
    		return;
    	if (mTouchCboCode.equals(cboCode)) 
    		return;
    	
    	int position = mCboCodeAdapter.getPosition(cboCode);
    	mSpinnerCboCode.setSelection(position);
    	
    	//-----------------------------------------------------
		// 고장코드 변경이면 정보 수정으로 본다.
		//-----------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    	
    	//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_CBO_STATUS, "cboCode", cboCode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		mTouchCboCode = null;
    }
    
    public String isLocCd() {
    	return mLocCdText.getText().toString();
    }
    
    public boolean isChkScan() {
    	return mChkScanCheck.isChecked();
    }
    
    public boolean isHierachyCheck() {
    	return mHierachyCheck.isChecked();
    }
    
    public void setJobWorkHierachyCheck(boolean checked) {
    	mHierachyCheck.setChecked(checked);
    	
    	if (checked) {
    		mUUCheck.setEnabled(true);
    		mUUCheck.setChecked(false);
    	} else {
    		mUUCheck.setEnabled(false);
    		mUUCheck.setChecked(false);
    	}
    	
		//-----------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_HIERACHY_CHECK, "check_yn", (checked ? "Y" : "N"));
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 상위바코드 정보 변경시 처리.
     */
    private void changeUFacCd(String barcode) {
        mUFacCdText.setText("");		// 먼저 초기화

        barcode = barcode.toUpperCase();

    	Long thisSelectedKey = getBarcodeTreeAdapter().getBarcodeKey(barcode);
    	// 중복스캔입니다...............................
    	if (thisSelectedKey != null) {
        	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
        	return;
        }
    	
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineProductBarcodeData("ufac", barcode);
    	} else {
    		getUFacCdInfos(barcode);
    	}
    }
    
    /**
     * 포토 이미지 선택 메뉴를 show한다.
     */
//    private void showImageSelectionMenu(View v) {
//    	
//    	final Long selectedKey = getBarcodeTreeAdapter().getSelected();
//    	if (selectedKey == null || selectedKey < 0) {
//    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비를 선택하세요.", BarcodeSoundPlay.SOUND_NOTIFY));
//    		return;
//    	}
//    	
//    	PopupMenu popup = new PopupMenu(this, v);
//        popup.getMenuInflater().inflate(R.menu.image_selection_popup, popup.getMenu());
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//            	if (item.getTitle().equals("미리보기")) {
//            		showZoomImageActivity();
//            	} else if (item.getTitle().equals("앨범")) {
//                	dispatchGalleryIntent();
//                } else if (item.getTitle().equals("촬영")) {
//                	dispatchTakePictureIntent();
//                }
//
//                return true;
//            }
//        });
//
//        popup.show();
//    }
//    
//    private void showZoomImageActivity() {
//    	//-----------------------------------------------------------
//    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
//    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
//    	//-----------------------------------------------------------
//    	mScannerHelper.focusEditText(null);
//    	
//    	final Long selectedKey = getBarcodeTreeAdapter().getSelected();
//    	if (selectedKey == null || selectedKey < 0)
//    		return;
//    	
//    	BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
//    	
//    	Intent intent = new Intent(getApplicationContext(), ZoomImageActivity.class);
//		intent.putExtra(ZoomImageActivity.INPUT_IMAGE_PATH, model.getCboPhotoUri());
//		startActivity(intent);
//    }
//    
//    private void dispatchGalleryIntent() {
//		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		startActivityForResult(intent, ACTION_TAKE_GALLERY);
//	}
//    
//    private void dispatchTakePictureIntent() {
//    	mCboPhotoUri = mStorageTools.getImageFile();
//        Log.d(TAG, "<<<<< create Image File >>>>>  mCboPhotoUri==>"+mCboPhotoUri.getPath());
//        
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCboPhotoUri);
//		startActivityForResult(intent, ACTION_TAKE_PHOTO);
//	}
//    
//    private void choicePhotoDisplay(String imagePath) {
//		Log.d(TAG, "<<<<< choicePhotoDisplay >>>>>  imagePath==>"+imagePath);
//		if (TextUtils.isEmpty(imagePath)) {
//			mCboPhotoImageView.setImageResource(R.drawable.common_button_bg_red);
//			return;
//		}
//		
//		if ((imagePath.matches(".*http://.*")) || (imagePath.matches(".*https://.*"))) {
//			GlobalData.getInstance().showMessageDialog(
//					new ErpBarcodeException(-1, "스마트폰에서 저장한 이미지만 선택가능합니다.", BarcodeSoundPlay.SOUND_NOTIFY));
//			return;
//		}
//		
//		// 이미 선택한 이미지인지 비교.
//		//final List<Long> sendKeys = getBarcodeTreeAdapter().getManager().getAllNextNodeList();
//		//for (Long sendKey : sendKeys) {
//		//	BarcodeListInfo sendBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(sendKey);
//		//	if (sendBarcodeInfo != null && sendBarcodeInfo.getCboPhotoUri().equals(imagePath)) {
//		//		GlobalData.getInstance().showMessageDialog(
//		//				new ErpBarcodeException(-1, "다른 설비에 선택된 이미지입니다.", BarcodeSoundPlay.SOUND_NOTIFY));
//		//    	return;
//		//	}
//		//}
//
//		int max_pixel = mScreenTools.toPixels(80);
//		int image_degrees = StorageTools.getCameraOrientation(imagePath);
//		Bitmap bitmap = StorageTools.createImaegBitmap(imagePath, max_pixel);
//		bitmap = StorageTools.getRotatedBitmap(bitmap, image_degrees);
//		
//		mCboPhotoImageView.setImageBitmap(bitmap);
//		
//		Long selectedKey = getBarcodeTreeAdapter().getSelected();
//		if (selectedKey != null) {
//			BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
//			if (barcodeInfo != null) {
//				barcodeInfo.setCboPhotoUri(imagePath);
//			}
//		}
//	}

    /**
     * 필수스캔 여부 변경시 초기화 한다.
     */
    private void changeChkScan() {
    	//-----------------------------------------------------------
    	// 작업관리에서 사용함.
    	//-----------------------------------------------------------
    	final String chk_scan_yn = (mChkScanCheck.isChecked() ? "Y" : "N");


    	if (mChkScanCheck.isChecked()) {
    		mDeleteButton.setEnabled(true);
    		//-------------------------------------------------------------
        	// 단계별 작업을 여기서 처리한다.
        	//-------------------------------------------------------------
    		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
    			try {
    				GlobalData.getInstance().getJobActionManager().addItemParameter("chk_scan_yn", chk_scan_yn);
    				GlobalData.getInstance().getJobActionManager().addItemParameter("dialog_yn", "*");
    				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_CHK_SCAN);
    			} catch (ErpBarcodeException e) {
    				e.printStackTrace();
    			}
    		}
    		
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "'" + mJobGubun + "' 하실 설비를\n\r모두 스캔하세요.\n\r스캔하지 않은 설비는 '분실위험'\n\r처리됩니다."));
    		return;
    		
    	} else {
    		
    		//-----------------------------------------------------------
    		// Yes/No Dialog
    		//-----------------------------------------------------------
    		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    		GlobalData.getInstance().setGlobalAlertDialog(true);
    		
    		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SCAN);
    		String message = "스캔이 원칙입니다.\n\r스캔 없이 하위 포함 일괄 처리에\n\r대한 모든 책임은\n\r담당자에게 있습니다.\n\r진행 하시겠습니까?";
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

                	nextChangeChkScan();
                	
                	//-------------------------------------------------------------
                	// 단계별 작업을 여기서 처리한다.
                	//-------------------------------------------------------------
            		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
            			try {
            				GlobalData.getInstance().getJobActionManager().addItemParameter("chk_scan_yn", chk_scan_yn);
            				GlobalData.getInstance().getJobActionManager().addItemParameter("dialog_yn", "Y");
            				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_CHK_SCAN);
            			} catch (ErpBarcodeException e) {
            				e.printStackTrace();
            			}
            		}
                	return;
                }
            });
            builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	GlobalData.getInstance().setGlobalAlertDialog(false);
                	
                	mChkScanCheck.setChecked(true);
	            	nextChangeChkScan();
	            	
	            	//-------------------------------------------------------------
                	// 단계별 작업을 여기서 처리한다.
                	//-------------------------------------------------------------
            		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
            			try {
            				GlobalData.getInstance().getJobActionManager().addItemParameter("chk_scan_yn", chk_scan_yn);
            				GlobalData.getInstance().getJobActionManager().addItemParameter("dialog_yn", "N");
            				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_CHK_SCAN);
            			} catch (ErpBarcodeException e) {
            				e.printStackTrace();
            			}
            		}
	            	return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
    	}
    }
    
    /**
     * 최초 조회된 Tree의 바코드 조회정보로 다시 초기화 한다.
     */
    private void nextChangeChkScan() {
    	Log.i(TAG, "nextChangeChkScan  Start...");
    	if (mFirstBarcodeListInfos != null && mFirstBarcodeListInfos.size() > 0) {
    		// 최초 스캔정보로 되돌린다.
    		Log.i(TAG, "nextChangeChkScan  최초스캔 데이터 있음~~");
    		getBarcodeTreeAdapter().itemClear();
    		getBarcodeTreeAdapter().addItems(mFirstBarcodeListInfos);
    		
    		BarcodeListInfo barcodeInfo = mFirstBarcodeListInfos.get(0);
    		mFacCdText.setText(barcodeInfo.getBarcode());
    	}
    	
        // 스캔 필수 해제를 하였을 경우 2번 항목에서 추가한 바코드 “11501005211030035” 는 삭제 후 I/F 되어야 함. 스캔필수 해제하였을 경우 추가 스캔된 바코드는 삭제되어야 함.
    	// 입고(팀내), 철거 제외 - request by 정진우 2013.12.16 - 운영반영 - 2013.12.26
    	// 실장 제외 모두 병렬 처리 허용 - request by 김희선 2014.01.04 - 운영 반영 - 2014.01.13
    	// DR-2014-13014 U-U 스캔 후 해제 시 하위 설비 삭제 기능 개선 
    	/*if (mJobGubun.equals("실장") && !mChkScanCheck.isChecked()) {
    		List<Long> listKeys = getBarcodeTreeAdapter().getAllNextNodeList();
    		
    		int count=0;
			for (Long key : listKeys) {
				BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
				if (barcodeInfo!=null) {
					String scanValue = barcodeInfo.getScanValue();
					if (scanValue.equals("2") || scanValue.equals("3") && count!=0) {	
						getBarcodeTreeAdapter().removeNode(key);;	// 추가거나 3이고 최초 스캔이 아니면 삭제
					}
				}
				count++;
			}
			if (getBarcodeTreeAdapter().getCount() > 0) {
				getBarcodeTreeAdapter().changeSelected(0);
				choiceBarcodeDataDisplay(getBarcodeTreeAdapter().getBarcodeListInfo(getBarcodeTreeAdapter().getSelected()));
				getBarcodeTreeView().setSelection(0);
			}
			
			showSummaryCount();
    	}*/
    	mDeleteButton.setEnabled(mChkScanCheck.isChecked());
    	mScannerHelper.focusEditText(mFacCdText);
    }

    /**
     * 환경에 따라 바코드 입력컴포넌트들을 활성화/비활성화 한다.
     */
    public void setTextBoxEnable() {
        if (mDeviceInputbar.getVisibility() != View.VISIBLE && !mJobGubun.equals("형상구성(창고내)")) return;

		String instConfPartType = "";
		if (getBarcodeTreeAdapter()!=null) instConfPartType = getBarcodeTreeAdapter().getInstConfPartType();

		if  (instConfPartType.equals("")) {
			mUFacCdText.setEnabled(false);
			mUFacCdText.setText("");
			mDeviceIdText.setEnabled(false);
			mDeviceIdText.setText("");
		} else if  (instConfPartType.equals("R")) {
			mUFacCdText.setEnabled(false);
			mUFacCdText.setText("");
			mDeviceIdText.setEnabled(true);
		} else if (instConfPartType.equals("S")) {
			mUFacCdText.setEnabled(true);
			mDeviceIdText.setEnabled(true);
		} else if (instConfPartType.equals("U")) {
			mUFacCdText.setEnabled(true);
			mDeviceIdText.setEnabled(false);
			mDeviceIdText.setText("");
			mDeviceIdInfoText.setText("");
		} else if (instConfPartType.equals("E")) {
			/***
             * request by 강준석 2012.07.11
             * 단품 실장 시 상위바코드 필드 비활성화 처리
             */
			mUFacCdText.setEnabled(false);
			mUFacCdText.setText("");
			
			mDeviceIdText.setEnabled(true);
		}

		// 강준석창고에서는 장치ID 스캔 비활성화 해주시고요...
		if (mThisLocCodeInfo==null || TextUtils.isEmpty(mThisLocCodeInfo.getLocCd()) 
				|| mThisLocCodeInfo.getLocCd().startsWith("VS")) {
			mDeviceIdText.setEnabled(false);
			mDeviceIdText.setText("");
		}
    }
    
    /**
     * 선택한 TreeNode의 바코드인포정보를 입력창에 보여준다.
     * 
     * @param model
     */
    public void choiceBarcodeDataDisplay(BarcodeListInfo model) {
    	if (model == null) return;

    	if (model.getPartType().equals("L") || model.getPartType().equals("D")) {
    		return;
    	}
    	mFacCdText.setText(model.getBarcode());
    	mPartTypeText.setText(model.getPartType());
    	
//    	if (mJobGubun.equals("고장등록")) {
//    		if (TextUtils.isEmpty(model.getCboPhotoUri())) {
//    			choicePhotoDisplay("");
//    		} else {
//    			choicePhotoDisplay(model.getCboPhotoUri());
//    		}
//    	}
    }

    /**
     * TreeView에서 선택한 Node의 해당하는 바코드 ScanValue를 초기화 한다.
     */
    private void cancelScanData() {
    	// 마지막 스캔된 정보를 초기화한다.
		Long selectedKey = getBarcodeTreeAdapter().getSelected();
		if (selectedKey == null) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
			return;
		}
		
		BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
		getBarcodeTreeAdapter().initItemValue();
		
		showSummaryCount();
		
		//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 true
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
		
		//-----------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_CANCEL_SCAN, "barcode", model.getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 선택한 바코드를 이동한다.
     */
    private void moveData() {
    	final Long selectedKey = getBarcodeTreeAdapter().getSelected();
    	if (selectedKey == null) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
    		return;
    	}
    	
    	BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    	if (barcodeInfo != null) {
    		if (barcodeInfo.getPartType().equals("L")) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드는 수정할 수 없습니다."));
        		return;
    		}
    	}
    	
    	if (getEditMode() == EDIT_MODE_MODIFY) return;
    	
    	if (getEditMode() == EDIT_MODE_NONE) {
    		setEditMode(EDIT_MODE_MOVE);
    		mEditModeComment.setText("이동할 상위 개체를 선택하세요. ");
    		mEditTreeKey = selectedKey;
    		mEditBarcodeInfo = barcodeInfo;
    		mCrudNoneInputbar.setVisibility(View.GONE);
    		mCrudEditModeInputbar.setVisibility(View.VISIBLE);
    	}
    	
    }
    
    /**
     * 대상 바코드를 타켓 바코드 자식노드로 이동한다.
     * 
     * @param selectedKey
     * @param targetKey
     */
    public void moveSubTree(final Long selectedKey, final Long targetKey) {
    	// 타켓대상 바코드인포 조회.
    	BarcodeListInfo targetBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(targetKey);
    	if (targetBarcodeInfo == null) return;
    	// 이동대상 바코드인포 조회.
    	BarcodeListInfo selectedBarcodeInfo =  getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    	if (selectedBarcodeInfo == null) return;
    	
    	String parentBarcode = compareTargetHierarchy(selectedBarcodeInfo, targetBarcodeInfo);
    	// 이동 불가 오류 처리
    	if (parentBarcode.isEmpty()) {
    		String partTypeName = selectedBarcodeInfo.getPartTypeName();
    		String devTypeName = selectedBarcodeInfo.getDevTypeName();
    		if (devTypeName.equals("단품")) partTypeName = devTypeName;
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "'" + targetBarcodeInfo.getPartTypeName() + "' 바코드의 하위에 \n\r '" + partTypeName + "' 바코드를 \n\r 이동하실 수 없습니다. "));
    		cancelEditMode();
        	return;
    	}
    	
    	List<BarcodeListInfo> targetList = new ArrayList<BarcodeListInfo>();
    	
    	// 타켓바코드 정보(상위바코드, 장치ID)로 정보 변경.
    	selectedBarcodeInfo.setuFacCd(targetBarcodeInfo.getBarcode());
    	selectedBarcodeInfo.setServerUFacCd(targetBarcodeInfo.getBarcode());
    	selectedBarcodeInfo.setDeviceId(targetBarcodeInfo.getDeviceId());
    	targetList.add(selectedBarcodeInfo);
    	
    	// 선택한 바코드 위에서 포함하고, getSubTreeList이것 사용.
    	List<Long> subKeys = getBarcodeTreeAdapter().getSubTreeList(selectedKey);
    	for (Long subkey : subKeys) {
    		BarcodeListInfo barcodeInfo =  getBarcodeTreeAdapter().getBarcodeListInfo(subkey);
    		// 타켓바코드의 장치ID를 반영한다.
    		barcodeInfo.setDeviceId(targetBarcodeInfo.getDeviceId());
    		targetList.add(barcodeInfo);
    	}
    	
    	// 이동할 바코드는 Tree에서 삭제한다.
    	getBarcodeTreeAdapter().removeNode(selectedKey);
    	// 선택한 바코드의 모든 하위까지 이동한다.
    	getBarcodeTreeAdapter().addChildItems(targetKey, targetList);
    	// 모드를 초기화 한다.
    	cancelEditMode();
    	
    	//-----------------------------------------------------------
        // TreeView FooterBar Summary Count
        //-----------------------------------------------------------
    	showSummaryCount();
    	
    	//-----------------------------------------------------------
        // 화면 초기 Setting후 변경 여부 Flag
        //-----------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    	
    	//-----------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().initItemParameter();
				GlobalData.getInstance().getJobActionManager().addItemParameter("selectBarcode", selectedBarcodeInfo.getBarcode());
				GlobalData.getInstance().getJobActionManager().addItemParameter("targetBarcode", targetBarcodeInfo.getBarcode());
				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_MOVE);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 수정모드 취소.
     */
    public void cancelEditMode() {
    	if (getEditMode() == EDIT_MODE_NONE) return;
    	
    	setEditMode(EDIT_MODE_NONE);
    	mEditModeComment.setText("");
		mEditTreeKey = null;
		mEditBarcodeInfo = null;
		mCrudNoneInputbar.setVisibility(View.VISIBLE);
		mCrudEditModeInputbar.setVisibility(View.GONE);
    }
    
    /**
     * TreeView에서 선택한 Node와 하위Node를 삭제한다.
     */
    private void deleteData() {
    	final Long selectedKey = getBarcodeTreeAdapter().getSelected();
    	if (selectedKey == null) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
    		return;
    	}
    	
    	// 조회된 설비바코드는 삭제하실 수 없습니다.
    	final BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    	if (!barcodeInfo.getScanValue().equals("2") && !barcodeInfo.getScanValue().equals("3")) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 설비바코드는\n\r삭제하실 수 없습니다."));
    		return;
    	}
    	
    	//-----------------------------------------------------------
    	// Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    	String message = "삭제하시겠습니까?";
		// 하위설비 있는지 체크
		if (getBarcodeTreeAdapter().getSubTreeList(selectedKey).size()>0) {
	    	message = "선택된 항목의 하위 설비를 포함하여\n\r삭제됩니다.\r\n삭제하시겠습니까?";
		}
    	
		final Builder builder = new AlertDialog.Builder(this); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getBaseContext());
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
            	
            	deleteData(selectedKey);
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
    
    private void deleteData(Long selectedKey) {
    	
    	if (selectedKey == null) return;
    	
    	BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    	
    	getBarcodeTreeAdapter().removeNode(selectedKey);
    	// 마지막 row 선택
    	getBarcodeTreeAdapter().selectLastPosition();
    	
    	showSummaryCount();
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 true
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);

		//-------------------------------------------------------------
    	// 단계별 작업을 삭제한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_DELETE, "barcode", barcodeInfo.getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 작업내용 저장.
     */
    private void saveWorkData() {
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
    		GlobalData.getInstance().showMessageDialog(
        			new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "작업을 진행중이므로 저장할수 없습니다."));
    		return;
    	}
    	
    	//-----------------------------------------------------------
    	// 고장내역을 텍스트 입력이여서 작업관리 저장시 추가한다.
    	//-----------------------------------------------------------
    	if (mJobGubun.equals("고장등록")) {
    		String cboReasonText = mCboReasonText.getText().toString().trim();
        	if (!cboReasonText.isEmpty()) {
        		try {
    				GlobalData.getInstance().getJobActionManager().setStepItem(
    						JobActionStepManager.JOBTYPE_CBO_REASON_TEXT, "cboReasonText", cboReasonText);
    			} catch (ErpBarcodeException e) {
    				e.printStackTrace();
    			}
        	}
    	}
    	

    	//-----------------------------------------------------------
    	// 작업인포 정보에 필요한 내역을 Set한다.
    	//-----------------------------------------------------------
    	String locCd = mLocCdText.getText().toString().trim();
    	String locName = mLocNameText.getText().toString().trim();
    	String wbsNo = mWbsNoText.getText().toString().trim();
    	String deviceId = mDeviceIdText.getText().toString().trim();
    	int recordCount = getBarcodeTreeAdapter().getCount();
    	String offlineYn = (SessionUserData.getInstance().isOffline() ? "Y" : "N");

    	try {
    		GlobalData.getInstance().getJobActionManager().saveWorkData(locCd, locName, wbsNo, deviceId, recordCount, offlineYn);
    	} catch (ErpBarcodeException e) {
    		e.printStackTrace();
    	}
    	
    	//-----------------------------------------------------------
    	// 작업관리에 저장후에는 JOB_APPEND 모드로 전환한다.
    	//-----------------------------------------------------------
    	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_APPEND);
    	
    	//-----------------------------------------------------------
    	// 작업관리 저장후에는 changeFlag는 false로 Set한다.
    	//-----------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(false);
    	
    	GlobalData.getInstance().showMessageDialog(
    			new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "저장하였습니다.", BarcodeSoundPlay.SOUND_ASTERISK));
    }
    
    /**
     * 전송결과 작업내역에 저장.
     * 
     * @param sendMessage
     */
    private void sendWorkResult(String sendMessage, boolean isInitScreen) {

    	//if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_GENERAL) 
    	//	return;
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
    		String locCd = mLocCdText.getText().toString().trim();
        	String locName = mLocNameText.getText().toString().trim();
        	String wbsNo = mWbsNoText.getText().toString().trim();
        	String deviceId = mDeviceIdText.getText().toString().trim();
        	int recordCount = getBarcodeTreeAdapter().getCount();
        	String offlineYn = (SessionUserData.getInstance().isOffline() ? "Y" : "N");

        	try {
        		GlobalData.getInstance().getJobActionManager().saveWorkData(locCd, locName, wbsNo, deviceId, recordCount, offlineYn);
        	} catch (ErpBarcodeException e) {
        		e.printStackTrace();
        	}
    	}
    	
    	//-----------------------------------------------------------
		// 단계별 작업을 여기서 처리한다.
		//-----------------------------------------------------------
    	try {
			GlobalData.getInstance().getJobActionManager().saveSendData("Y",  sendMessage);
		} catch (ErpBarcodeException e) {
			e.printStackTrace();
		}
		
		//-----------------------------------------------------------
    	// 전송후에는 JOB_GENERAL 모드로 전환한다.
    	//-----------------------------------------------------------
    	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
    	
    	//-----------------------------------------------------------
    	// 변경된 정보 있는지 여부는 false로 Set한다.
    	//-----------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(false);
		GlobalData.getInstance().setSendAvailFlag(false);
    	
    	//-----------------------------------------------------------
    	// 저장후 여기서 초기화 한다.
    	//-----------------------------------------------------------
    	if (isInitScreen) initScreen();
    }
    
    /**
     * 단계별 작업 실행.
     */
    private void jobNextExecutors() {
    	jobNextExecutors(0);
    }
    private void jobNextExecutors(int position) {
		Log.i(TAG, "jobNextExecutors  Start...");
		
		if (GlobalData.getInstance().getJobActionManager().getStepWorkCount() <= position) {
			GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_APPEND);
			
			WorkInfo stepWorkInfo = GlobalData.getInstance().getJobActionManager().getThisWorkInfo();
			if (stepWorkInfo.getTranYn().equals("Y")) {
				GlobalData.getInstance().setChangeFlag(false);
				GlobalData.getInstance().setSendAvailFlag(false);
			}
			return;
		}
		
		WorkItem workItem = new WorkItem();
		workItem = GlobalData.getInstance().getJobActionManager().startStepItem(position, new JobHandler(position));
		if (workItem == null) return;
		
		startStepWorkItem(workItem);
	}
    
    /**
     * 작업아이템 단계별 Start
     * 
     * @param workItem
     */
    private void startStepWorkItem(WorkItem workItem) {
    	workItem.setStepStatus(JobActionStepManager.JOB_STEP_NONE);
    	
    	Log.i(TAG, "startStepWorkItem  getJobType==>"+workItem.getJobType());
    	
    	if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_INITIAL_TASK)) {
    		String taskName = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "taskName");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_INITIAL_TASK==>"+taskName);
    		
    		if (taskName.equals("getMoveRequestData")) {
    			getMoveRequestData("","");
    		}
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_ORG)) {
    		String orgInfo = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "orgCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_ORG==>"+orgInfo);
        	mOrgCodeText.setText(orgInfo);
        	
        	GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_RECEIPT_ORG)) {
    		String orgInfo = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "orgCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_RECEIPT_ORG==>"+orgInfo);
        	mReceiptOrgCodeText.setText(orgInfo);
        	
        	// 접수팀간이면 부서간이동조회 호출
			if (mJobGubun.equals("접수(팀간)")) {
				String costCenter = orgInfo.split("/")[0];
				
				getMoveRequestData("", costCenter.trim());
			} else {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			}
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_LOC)) {
    		String locCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "locCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_LOC==>"+locCd);
    		changeLocCd(locCd);    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_WBS)) {
    		String wbsNo = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "wbsNo");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_WBS==>"+wbsNo);
    		setJobWorkWbsNo(wbsNo);
       	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_DEVICE)) {
    		String deviceId = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "deviceId");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_DEVICE==>"+deviceId);
    		changeDeviceId(deviceId);   	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC)) {
    		String facCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC==>"+facCd);
    		changeFacCd(facCd);   	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_UU_CHECK)) {
    		String check_yn = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "check_yn");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_UU_CHECK==>"+check_yn + "," + check_yn);
    		
    		boolean checked = (check_yn.equals("Y") ? true : false);
    		setJobWorkUUCheck(checked);
    		
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_UFAC)) {
    		String uFacCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "uFacCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_UFAC==>"+uFacCd);
    		changeUFacCd(uFacCd);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC_STATUS)) {
    		String facStatusCode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facStatusCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC_STATUS==>"+facStatusCode);
    		mTouchFacStatusCode = "****"; // 단지 수정정보로 처리하기 위해서.
    		setJobWorkFacStatus(facStatusCode);

    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CBO_STATUS)) {
    		String cboCode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "cboCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_CBO_STATUS==>"+cboCode);
    		mTouchCboCode = "****"; // 단지 수정정보로 처리하기 위해서.
    		setJobWorkCboCode(cboCode);

    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CBO_REASON_TEXT)) {
    		String cboReasonText = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "cboReasonText");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_CBO_REASON_TEXT==>"+cboReasonText);
    		
    		mCboReasonText.setText(cboReasonText);
    		
    		GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();

    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CHK_SCAN)) {
    		String chk_scan_yn = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "chk_scan_yn");
    		String dialog_yn = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "dialog_yn");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_CHK_SCAN==>"+chk_scan_yn+","+dialog_yn);
    		
    		if (chk_scan_yn.equals("Y")) {
    			mChkScanCheck.setChecked(true);
    			mDeleteButton.setEnabled(true);
    			
    		} else {
    			mChkScanCheck.setChecked(false);
    			
    			if (dialog_yn.equals("Y")) {
    				nextChangeChkScan();
    			} else {
    				mChkScanCheck.setChecked(true);
                	nextChangeChkScan();
    			}
    		}
    		
    		GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
    		
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CANCEL_SCAN)) {
    		String barcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "barcode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_CANCEL_SCAN==>"+barcode);
    		getBarcodeTreeAdapter().changeSelected(barcode);
    		cancelScanData();
    		
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_DELETE)) {
    		String barcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "barcode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_DELETE==>"+barcode);
    		
    		Long selectedKey = getBarcodeTreeAdapter().getBarcodeKey(barcode);
    		deleteData(selectedKey);
    		
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_MOVE)) {
    		String selectBarcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "selectBarcode");
    		String targetBarcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "targetBarcode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_MOVE==>"+selectBarcode + "," + targetBarcode);
    		
    		Long selectedKey = getBarcodeTreeAdapter().getBarcodeKey(selectBarcode);
    		Long targetKey = getBarcodeTreeAdapter().getBarcodeKey(targetBarcode);
    		moveSubTree(selectedKey, targetKey);

    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_HIERACHY_CHECK)) {
    		String check_yn = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "check_yn");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_HIERACHY_CHECK==>"+check_yn + "," + check_yn);
    		
    		boolean checked = (check_yn.equals("Y") ? true : false);
    		setJobWorkHierachyCheck(checked);

    	}
    }

	/**
	 * Job Handler
	 */
	private class JobHandler extends Handler {
		private int _position;
		public JobHandler(int position) {
			_position = position;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case JobActionStepManager.JOB_STEP_FINISHED:
            	jobNextExecutors(_position+1);
                break;
            case JobActionStepManager.JOB_STEP_ERROR:
            	jobNextExecutors(_position+1);
            	break;
            }
        }
	}

    /**
     *  전송시 유효성 체크 Class 
     */
    public class SendCheck {
    	public String orgCode = "";
    	public String receiptOrgCode = "";
    	public LocBarcodeInfo locBarcodeInfo = null;
		public String isCheckedOTD = null;
    	public boolean isDeviceId = false;
    	public String deviceId = "";
    	public String facCd = "";
    	public String partType = "";
    	public boolean isUFacCd = false;
    	public String UFacCd = "";
    	public String uPartType = "";
    	public String UFacCd_DeviceId_Yn = null;
    	public String instConfPartType = "";
    	public String notScanYn = null;
    	public String instConfInheritanceYn = null; // 운용조직 비교하기 여부
    	public String facStatusCode = "";
    	public String cboCode = "";
    }
    
    /**
     * 바코드 작업내용 전송하기..  execute
     */
    private void executeSendCheck(final SendCheck sendCheck) {
    	Log.i(TAG, "executeSendCheck  Validataion Check  Start...");
    	
    	// 0. 전송할 자료가 없으면 전송하지 않는다.
    	if (!GlobalData.getInstance().isSendAvailFlag() 
    			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}
    	// 1. 위치바코드 체크
		if (getLocInputbarVisibility()==View.VISIBLE && (sendCheck.locBarcodeInfo==null || sendCheck.locBarcodeInfo.getLocCd().isEmpty())) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mLocCdText);
			return;
		}
		
    	if (mReceiptOrgInputbar.getVisibility() == View.VISIBLE) {
    		if (mJobGubun.equals("송부(팀간)")) {
    			String receiptOrgInfo = mReceiptOrgCodeText.getText().toString().trim();
        		if (receiptOrgInfo.isEmpty()) {
        			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조직을 선택하세요."));
            		return;
        		}
    		}
    	}
    	
    	if (mDeviceInputbar.getVisibility() == View.VISIBLE && mDeviceIdText.isEnabled()) {
    		if (sendCheck.deviceId.isEmpty()) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mDeviceIdText);
        		return;
    		}
    		
    		if (sendCheck.deviceId.length() != 9) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드의 형식이 잘 못 되었습니다."));
    			mScannerHelper.focusEditText(mDeviceIdText);
        		return;
    		}
    	}
    	// 3. 상위바코드 체크
    	if (mUFacInputbar.getVisibility() == View.VISIBLE && mUFacCdText.isEnabled()) {
    		if(mJobGubun.equals("형상구성(창고내)")){
        		if(sendCheck.instConfPartType.equals("U") && sendCheck.UFacCd.isEmpty()){
        			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "형상구성하실 대상인\n\r상위바코드를 스캔하세요."));
                	mScannerHelper.focusEditText(mUFacCdText);
            		return;
        		}
        	} else{
        		if (sendCheck.instConfPartType.equals("U") && sendCheck.UFacCd.isEmpty()) {
        			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "실장하실 대상인\n\r상위바코드를 스캔하세요."));
                	mScannerHelper.focusEditText(mUFacCdText);
            		return;
        		}
        	}
    	}
    	
    	
    	
    	// 고장코드
    	if (mCboCodeInputbar.getVisibility()==View.VISIBLE && mSpinnerCboCode!=null && mSpinnerCboCode.getSelectedItemPosition()<1) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "고장코드를 선택하세요."));
    		return;
    	}

    	if (getBarcodeTreeAdapter().getCount() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
    		return;
    	}

    	// 실장 Validation ---------------------------------------------------------------------------------------
    	if (mJobGubun.equals("실장")) {
    		// 4. 상위바코드와 장치바코드 체크 - 장치바코드와 상위바코드 모두 스캔 시
    		if (!sendCheck.deviceId.isEmpty() && !sendCheck.UFacCd.isEmpty()) {
    			// 2. 스캔한 상위바코드가 Equipment 일 경우 (표준랙에 Shelf형 장비 실장)
                // 상위바코드의 장치바코드 ＝ 스캔한 장치바코드 -> Error 처리함.
                // (Equipment 및 Shelf형 장비는 각각 별개의 장치바코드를 가지고 있음)
                // ==> Error Message "스캔한 장치바코드와 상위바코드(Equipment)의 장치바코드가 동일하여
                //     처리할 수 없으므로 상위바코드를 스캔하지 않거나 스캔한 장치바코드를 확인하세요."
                // 이와 별개로 Shelf 실장 시 상위바코드가 Equipment 일 경우 E-S-U 의 형상을 구성하지 않고
                // 위치 밑에 S-U 형상으로 내부적으로 처리할려고 합니다. 작업자는 물리적인 위치에서 상위바코드가
                // Rack or Equipment 의 구분을 하지 않고 스캔을 하며,  Equipment의 하위에는 Shelf가 위치할 수 없기 때문입니다.
                // (Equipment는 Shelf 와 동일한 Level로 위치함) 
                // ==> Shelf 실장 시 스캔한 상위바코드가 Equipment 일 경우 상위바코드 필드에 Equipment 를 I/F 하지
                //      않으면 됩니다.
    			if (sendCheck.uPartType.equals("E")) {
                    if (mThisUFacInfo != null && sendCheck.deviceId.equals(mThisUFacInfo.getDeviceId())) {
                    	GlobalData.getInstance().showMessageDialog(
                    			new ErpBarcodeException(-1, "스캔하신 장치바코드('" + sendCheck.deviceId + "')와\n\r상위바코드의 장치바코드('" + mThisUFacInfo.getDeviceId() 
                    					+ "')가 동일하여\n\r처리할 수 없으므로 상위바코드를 스캔하지 않거나 스캔한 장치바코드를 확인하세요."));
                		return;
                    }
                } else {
                    // 1. 스캔한 상위바코드가 Part이며 Rack일 경우 (Rack형 장비에 Shelf 실장)
                    //    상위바코드의 장치바코드 ≠ 스캔한 장치바코드 -> Error 처리함. 
                    //    (Rack형 장비는 동일한 장치바코드를 가지고 있음)
                    //    ==> 공통부 set 이 존재하므로 Error 처리 대신에 Warning Message로 대체 처리함.
                    //    "스캔한 장치바코드와 상위바코드의 장치바코드가 상이합니다. 처리 하시겠습니까?"
                    //    Yes 선택한 경우 -> 전송 처리 
                    //    No 선택한 경우 -> 초기화
                	if (sendCheck.UFacCd_DeviceId_Yn == null) {
                		if (mThisUFacInfo != null && !sendCheck.deviceId.equals(mThisUFacInfo.getDeviceId())) {
                    		
                			//-----------------------------------------------------------
                			// Yes/No Dialog
                			//-----------------------------------------------------------
                			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
                			GlobalData.getInstance().setGlobalAlertDialog(true);
                			
                			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
                			String message = "스캔하신 장치바코드('" + sendCheck.deviceId + "')와\n\r상위바코드의 장치바코드('" + mThisUFacInfo.getDeviceId() + "')가 상이합니다.\n\r처리 하시겠습니까?";
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
                                	
                	            	sendCheck.UFacCd_DeviceId_Yn = "Y";
                					executeSendCheck(sendCheck);
                					return;
                                }
                            });
                            builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                	GlobalData.getInstance().setGlobalAlertDialog(false);
                                	return;
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return;
                        }
                	}
                }
    		}
    	}
    	
		// 마지막 스텝 - 설비바코드와 상위바코드의 운용조직을 비교한다.
    	if (mJobGubun.equals("실장") || mJobGubun.equals("형상구성(창고내)")) {
    		
			if (sendCheck.instConfInheritanceYn == null) {
				sendCheck.instConfInheritanceYn = "N";
				
				// 4. 조직 상속여부 셋팅 - 스캔한 상위바코드의 장치ID = 스캔한 장치ID 또는 Unit 실장일 경우 상위바코드의 조직을 상속함
				if ((mThisUFacInfo != null && sendCheck.deviceId.equals(mThisUFacInfo.getDeviceId())) || sendCheck.instConfPartType.equals("U")) {
    				sendCheck.instConfInheritanceYn = "Y";
    			}
				
				if((mThisUFacInfo != null && mJobGubun.equals("형상구성(창고내)")) && (!mThisUFacInfo.getOrgId().equals(sendCheck.orgCode))){
					sendCheck.instConfInheritanceYn = "Y";
				}

				// 5. 상속 받으면서 상위설비의 조직과 실장 대상 설비의 조직이 서로 상이할 경우 Validation - request by 정진우 2013.06.05
				if (sendCheck.instConfInheritanceYn.equals("Y")) {
					//---------------------------------------------------------
					// 여기서는 상위바코드와 최상위 설비바코드의 운용조직을 비교한다.
					// 상위바코드_장치ID = 장치바코드 같고 최상위 설비바코드의 PartType = "U"이면 
					// 상위바코드_운용조직 == 설비바코드_운용조직 비교 Yes/No
					
					Long selectKey = getBarcodeTreeAdapter().getItemId(0);
					BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectKey);
					
					if ((mThisUFacInfo != null && !mThisUFacInfo.getOrgId().equals(barcodeInfo.getOrgId()))) {
						
						//-----------------------------------------------------------
						// Yes/No Dialog
						//-----------------------------------------------------------
						if (GlobalData.getInstance().isGlobalAlertDialog()) return;
						GlobalData.getInstance().setGlobalAlertDialog(true);
						
						GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
						String message = mJobGubun + " 대상 설비의 운용조직\n\r(" + barcodeInfo.getOrgName() + ")을\n\r상위설비의 운용조직\n\r(" + mThisUFacInfo.getOrgName() + ")으로\n\r변경합니다.\n\r진행하시겠습니까?";
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
                            	
                            	//---------------------------------------------------
                            	// 운용조직을 "Y"으로 Set 한다.
                            	//---------------------------------------------------
                            	for (Long key : getBarcodeTreeAdapter().getAllNextNodeList()) {
                            		BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
                            		//if (!barcodeInfo.getScanValue().equals("0")) {
            	        				barcodeInfo.setCheckOrgYn("Y");
            	        			//}
                            	}
                            	executeSendCheck(sendCheck);
                            	return;
                            }
                        });
                        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            	GlobalData.getInstance().setGlobalAlertDialog(false);
                            	
                            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "상위설비를 정확히\n\r스캔하시기 바랍니다. "));
    							mScannerHelper.focusEditText(mUFacCdText);
                            	return;
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
					}

				} else {
					// 쉘프 실장 시 하위 포함 일괄 운용조직 변경 여부 셋팅
					if (sendCheck.instConfPartType.equals("S")) {
						Long selectKey = getBarcodeTreeAdapter().getItemId(0);
						BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectKey);
						
						if (!SessionUserData.getInstance().getOrgId().equals(barcodeInfo.getOrgId())) {
							
							//-----------------------------------------------------------
							// Yes/No Dialog
							//-----------------------------------------------------------
							if (GlobalData.getInstance().isGlobalAlertDialog()) return;
							GlobalData.getInstance().setGlobalAlertDialog(true);
							
							GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
							String message = "'" + barcodeInfo.getBarcode() + "' 의 운용조직은\n\r'" + barcodeInfo.getOrgName() + "' 입니다.\n\r운용조직을 '" + SessionUserData.getInstance().getOrgName() + "'(으)로\n\r변경하시겠습니까?";
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
	                            	
	                            	//---------------------------------------------------
	                            	// 운용조직을 "Y"으로 Set 한다.
	                            	//---------------------------------------------------
	                            	for (Long key : getBarcodeTreeAdapter().getAllNextNodeList()) {
	                            		BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
	                            		//if (!barcodeInfo.getScanValue().equals("0")) {
	            	        				barcodeInfo.setCheckOrgYn("Y");
	            	        			//}
	                            	}
	                            	executeSendCheck(sendCheck);
	                            	return;
	                            }
	                        });
	                        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
	                            public void onClick(DialogInterface dialog, int which) {
	                            	GlobalData.getInstance().setGlobalAlertDialog(false);
	                            	
	                            	//---------------------------------------------------
	                            	// 운용조직을 "N"으로 Set 한다.
	                            	//---------------------------------------------------
	                            	for (Long key : getBarcodeTreeAdapter().getAllNextNodeList()) {
	                            		BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
	                            		//if (!barcodeInfo.getScanValue().equals("0")) {
	            	        				barcodeInfo.setCheckOrgYn("N");
	            	        			//}
	                            	}
	                            	executeSendCheck(sendCheck);
	                            	return;
	                            }
	                        });
	                        AlertDialog dialog = builder.create();
	                        dialog.show();
	                        return;
						}
					}
				}
			}
    	}
		// -- 마지막 스텝 - 설비바코드와 상위바코드의 운용조직을 비교한다.

    	// 스캔필수 일때..
    	if (mChkScanCheck.getVisibility()==View.VISIBLE && mChkScanCheck.isChecked()) {
			if (sendCheck.notScanYn == null) {
    			sendCheck.notScanYn = "N";
    			
    			if (mJobGubun.equals("접수(팀간)")) {
    				if (getBarcodeTreeAdapter().isDownNodeNotScaned()) {
        				sendCheck.notScanYn = "Y";
        			}
    			} else {
    				if (getBarcodeTreeAdapter().isNotScaned()) {
        				sendCheck.notScanYn = "Y";
        			}
    			}
        		
        		if (sendCheck.notScanYn.equals("Y")) {
        			if (!mJobGubun.equals("납품취소") && !mJobGubun.equals("송부취소(팀간)")) {
	    				//-----------------------------------------------------------
	    				// Yes/No Dialog
	    				//-----------------------------------------------------------
	    				if (GlobalData.getInstance().isGlobalAlertDialog()) return;
	    				GlobalData.getInstance().setGlobalAlertDialog(true);
	    				
	    				if(mJobGubun.equals("형상구성(창고내)")){
	    					String message = "스캔하지 않은 설비가\n존재합니다.\n대상 설비 전체를 스캔하지 \n않을 경우 작업이 불가합니다";
	    					GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SCANBELOW);
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
		            		builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
		                        public void onClick(DialogInterface dialog, int which) {
		                        	GlobalData.getInstance().setGlobalAlertDialog(false);
			                    }
			                });
			                
			                AlertDialog dialog = builder.create();
			                dialog.show();
			                return;
	    					
	    				}else{
	    					GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SCANBELOW);
		    				String message = "스캔하지 않은 하위 설비가\n\r존재합니다.\n\r스캔하지 않은 하위 설비는 '분실위험'\n\r처리됩니다.\n\r그래도 전송하시겠습니까?";
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
		                    	
			                    	// 여기서 전송한다.
		                        	execSendDataInTask(sendCheck, false);
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
        			}
        		}
    		}
    	}
    	
    	//test : matsua
    	if(mJobGubun.equals("입고(팀내)")){
    		if(mThisLocCodeInfo.getRoomTypeCode().equals("06")){
    			String message = "설비바코드 \n";
    			ArrayList<String> codeList = new ArrayList<String>();
    			
    			for (int i=0; i<getBarcodeTreeAdapter().getCount(); i++) {
    				Long selectedKey = getBarcodeTreeAdapter().getTreeId(i);
    				BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    				String Zsetup = barcodeInfo.getZsetup();
    				if(!Zsetup.equals("0") && !Zsetup.equals("0.00") && !Zsetup.equals("") && Zsetup != null){
    					message += barcodeInfo.getBarcode() + "\n";
    					codeList.add(barcodeInfo.getBarcode());
    				}
    			}
    			message += "는 입고처리 전 철거관리 메뉴에서\n철거SCAN을 반드시 수행하셔야 합니다.\n철거관리 메뉴로 이동하시겠습니까?";
    			
    			if(codeList.size() > 0){
    				if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    				GlobalData.getInstance().setGlobalAlertDialog(true);
    		    	
    				GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
    		    	
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
    		            	//철거로 이동
    		            	GlobalData.getInstance().setJobGubun("철거");
    						mJobGubun = GlobalData.getInstance().getJobGubun();
    						setMenuLayout();
    				        setFieldVisibility();
    				        initScreen("all");
    		            }
    		        });
    		        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
    		            public void onClick(DialogInterface dialog, int which) {
    		            	GlobalData.getInstance().setGlobalAlertDialog(false);
    		            	//전송 진행 
    		            	execSendDataInTask(sendCheck, false);
    		            }
    		        });
    		        AlertDialog dialog = builder.create();
    		        dialog.show();
    		        return;
    			}
    		}
    	}
    	
    	Log.i(TAG, "executeSendCheck  Validataion Check  End...");
    	
		//---------------------------------------------------------------------
		// 최종 전송 Yes/No 확인 Dialog
    	//---------------------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
		String message = "전송하시겠습니까?";
		if (mChkScanCheck.getVisibility()==View.VISIBLE && !mChkScanCheck.isChecked()) {
			message = "하위 설비를 포함하여\n\r처리됩니다.\n\r전송하시겠습니까?";
		}
    	
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
            	
            	execSendDataInTask(sendCheck, false);
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
    
    private void execSendDataInTask(SendCheck sendCheck, boolean confirm) {
    	if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		//TODO. 입고(팀내), 설비상태변경 - 보증일이 남은경우 팝업창을 띄어 확인해준다. 
		if(!confirm){
			if(mJobGubun.equals("입고(팀내)") || mJobGubun.equals("설비상태변경")){
				SpinnerInfo facStatusSpinnerInfo = (SpinnerInfo) mFacStatusAdapter.getItem(mSpinnerFacStatus.getSelectedItemPosition());
				String spinner = facStatusSpinnerInfo.getCode();
				if(spinner.equals("0200")){ // 불용대기 
					ArrayList<BarcodeListInfo> gwlenInfo = new ArrayList<BarcodeListInfo>();
					final List<Long> sendTreeKeys = getBarcodeTreeAdapter().getAllNextNodeList();
					for (Long key : sendTreeKeys) {
						BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
						if(!barcodeInfo.getGwlen_o().equals("") && barcodeInfo.getGwlen_o() != null){
							if(Integer.parseInt(SystemInfo.getNowDate()) <= Integer.parseInt(barcodeInfo.getGwlen_o().replace("-", ""))){
								gwlenInfo.add(barcodeInfo);
							}
						}
					}
					
					if(gwlenInfo.size() > 0){
						setBarcodeProgressVisibility(false);
						mSendCheck = sendCheck;
						
						Intent intent = new Intent(getApplicationContext(), GwlenListActivity.class);
						intent.putExtra("list", gwlenInfo);
						startActivityForResult(intent, ACTION_REQUEST_GWLEN_O);
						return;
					}
				}
			}
		}
		
		mSendButton.setEnabled(false);
    	new SendDataInTask(sendCheck, this).execute();
    }
    
    // 전송 비동기 테스크
    private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;
    	private int _SendCount = 0;
    	private SendCheck _SendCheck;
    	private OutputParameter _OutputParameter;
    	private final Map<String, String> _OutputMaps = new HashMap<String, String>();
    	private Activity mActivity;
    	
    	public SendDataInTask(final SendCheck sendCheck, Activity activity) {
    		_SendCheck = sendCheck;
    		if (_SendCheck.UFacCd_DeviceId_Yn == null) _SendCheck.UFacCd_DeviceId_Yn = "N";
    		if (_SendCheck.instConfInheritanceYn == null) _SendCheck.instConfInheritanceYn = "N";
    		mActivity = activity; 
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
            mSendButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			Log.i(TAG, "SendDataInTask Hearder Parameter  Start...");

			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
			
			String instConfPartType = getBarcodeTreeAdapter().getInstConfPartType();
			Log.i(TAG, "SendDataInTask instConfPartType==>"+instConfPartType);
			
			String CHKSCAN = "";
			if (mChkScanCheck.isChecked()) CHKSCAN = "X";
			
            /*
             * PRCID 셋팅 - DR-2013-56563 - request by 김희선 2014.01.08
             * request by 양석훈
             *@ 프로세스유형을 아래와 같이 변경 부탁 드립니다.
            1.	설비상태변경 (모든 작업에 대하여)
              0005 0125  Multi미운용
            0009 0155  Multi예비
            0008 0185  Multi유휴
            0013 0230  Multi불용대기
            2.	탈장
            0006 0035  Multi탈장
            3.	출고(팀내)
            0018 0680  Multi출고
            4.	송부(팀간), 고장등록
            두 작업은 프로세스 유형 변경 없이 멀티로 전송
             */
    		try {
				if (mJobGubun.equals("납품입고")) {
					
				} else if (mJobGubun.equals("납품취소")) {
					// 스캔취소 가능여부 벨리데이션
					if (_SendCheck.isCheckedOTD==null) {
						jsonParam.put("I_ZPSTATU", "0021");
					} else {
						// 진짜 전송
						jsonParam.put("WORKID", "1008");
						jsonParam.put("PRCID", "1011");
						jsonParam.put("CHKSCAN", CHKSCAN);
					}
					
				} else if (mJobGubun.equals("배송출고")) {
					jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
					//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
					// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
					
					jsonParam.put("WORKID", "1007");
					jsonParam.put("PRCID", "1170");
					jsonParam.put("CHKSCAN", CHKSCAN);
					String zdocrt = "";
					if (mHierachyCheck.isChecked()) zdocrt="Y";
					jsonParam.put("ZDOCRT", zdocrt);		// 배송오더
	        	} else if (mJobGubun.equals("입고(팀내)")) {
	        		jsonParam.put("WORKID", "0019");     
                	jsonParam.put("PRCID", "0760");
	                      
	                jsonParam.put("CHKSCAN", CHKSCAN);
	                jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
	                //-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
	                // GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
	                
	        	} else if (mJobGubun.equals("출고(팀내)")) {
	        		jsonParam.put("WORKID", "0018");     
                	jsonParam.put("PRCID", "0680");
	        	} else if (mJobGubun.equals("실장")) {
	        		jsonParam.put("WORKID", "0007");
	                if (instConfPartType.equals("R")) {
	                	jsonParam.put("PRCID", "0250");
	                } else if (instConfPartType.equals("S")) {
	                	jsonParam.put("PRCID", "0260");
	                } else if (instConfPartType.equals("U")) {
	                	jsonParam.put("PRCID", "0270");
	                } else if (instConfPartType.equals("E")) {
	                	jsonParam.put("PRCID", "0271");
	                }
	        		/*
	                 * 스캔한 상위바코드의 장치ID = 스캔한 장치ID 또는 Unit 실장일 경우 상위바코드의 조직을 상속함
	                  . Header  정보의 ZDOCRT = 'X' 로 PDA I/F 처리
	                 * 2012.07.26 request by kang
	                 */
	        		if (_SendCheck.instConfInheritanceYn.equals("Y")) {
	        			jsonParam.put("ZDOCRT", "X");
	        		} else {
	        			jsonParam.put("ZDOCRT", "");
	        		}

	                Log.i(TAG, "SendDataInTask 실장 1");

		        	if (_SendCheck.isDeviceId) {
		        		jsonParam.put("DEVICEID", _SendCheck.deviceId);
		        	}
		        	
		        	if (_SendCheck.isUFacCd) {
		        		// 상위바코드 - 상위바코드가 E이면 I/F 하지 않음
		        		if (_SendCheck.uPartType.equals("E")) {
		        			jsonParam.put("UBARCODE", "");
	                    } else {
	                    	jsonParam.put("UBARCODE", _SendCheck.UFacCd);
	                    }
		        	}
		        	
	                // ZTPMN0210-CHKSTORT (창고형 위치바코드 여부)  PDA에서 위치바코드를 스캔하였을 때 실유형이 창고형일 경우 위 필드에 'X' 로 체크하여 전송 - REQUEST BY 강준석 2012.04.01
	                // 6월 4일까지 논리창고 개발 안 되면 그리드형으로 픽스
		        	Log.i(TAG, "SendDataInTask 실장 2");
                	if (_SendCheck.locBarcodeInfo.getLocCd().startsWith("VS")) {
                		jsonParam.put("CHKSTORT", "X");
                    } else {
                    	jsonParam.put("CHKSTORT", "");
                    }
	                
	                jsonParam.put("CHKSCAN", CHKSCAN);
	                jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
	                //-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
	                // GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
	        	} else if (mJobGubun.equals("탈장")) {
	        		jsonParam.put("WORKID", "0006");
                	jsonParam.put("PRCID", "0035");
	                jsonParam.put("CHKSCAN", CHKSCAN);
                } else if (mJobGubun.equals("송부(팀간)")) {
                	jsonParam.put("WORKID", "0016");
                	jsonParam.put("PRCID", "0310");
                	jsonParam.put("SRCSYS", "A");
                	jsonParam.put("SKOSTL", SessionUserData.getInstance().getOrgId());  // 코스트센터
                	jsonParam.put("RKOSTL", _SendCheck.receiptOrgCode);  // 접수운용조직
                	jsonParam.put("WERKS", "9200");
                	jsonParam.put("CENTER", "");
                	jsonParam.put("SZKOSTL", SessionUserData.getInstance().getOrgId()); // 코스트센터
                	
                	jsonParam.put("CHKSCAN", CHKSCAN);
                	
                } else if (mJobGubun.equals("송부취소(팀간)")) {

                	jsonParam.put("WORKID", "0016");
                	jsonParam.put("PRCID", "0315");
                	jsonParam.put("SRCSYS", "A");
                	jsonParam.put("KOSTL", SessionUserData.getInstance().getOrgId());
                	jsonParam.put("WERKS", "9200");
                	
                	//DR-2013-57935 : 송부취소(팀간)에서 위치코드 스캔 추가 - request by 김희선 2014.06.03 - modify by 양혜진,정수연,최미소  2014.06.03
                	jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
                	//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
                	// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
                	
                } else if (mJobGubun.equals("접수(팀간)")) {
					jsonParam.put("WORKID", "0016");
					jsonParam.put("PRCID", "0320");
					jsonParam.put("SRCSYS", "A");
					jsonParam.put("RKOSTL", SessionUserData.getInstance().getOrgId());  // 운용조직
					jsonParam.put("WERKS", "9200");
					jsonParam.put("KOSTL", SessionUserData.getInstance().getOrgId());	// 운용조직
					jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
                	//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
					// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
				} else if (mJobGubun.equals("설비상태변경")) {
	        		if (_SendCheck.facStatusCode.equals("0100")) { // 유휴
	    				jsonParam.put("WORKID", "0008");
	                	jsonParam.put("PRCID", "0185");
	    			} else if (_SendCheck.facStatusCode.equals("0110")) { // 예비
	    				jsonParam.put("WORKID", "0009");
	                	jsonParam.put("PRCID", "0155");
	    			} else if (_SendCheck.facStatusCode.equals("0070")) { // 미운용
	    				jsonParam.put("WORKID", "0005");
	                	jsonParam.put("PRCID", "0125");
	    			} else if (_SendCheck.facStatusCode.equals("0200")) { // 불용대기
	    				jsonParam.put("WORKID", "0013");
	                	jsonParam.put("PRCID", "0230");
	    			}
	                jsonParam.put("CHKSCAN", CHKSCAN);
				} else if (mJobGubun.equals("고장등록")) {
					jsonParam.put("PRCID", "0410");
					jsonParam.put("CHKSCAN", CHKSCAN);
				} else if (mJobGubun.equals("철거")) {
					jsonParam.put("WORKID", "0012");
					
	                if (SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) {
	                	// 직영철거
						jsonParam.put("FUNCNAME", "ZPMN_CONFIRM_REMOVAL_IN_PROC");
						// "0770' :  직영철거, '0775' :  직영철거송부"
	                    if (_SendCheck.receiptOrgCode.isEmpty()) {
	                    	jsonParam.put("PRCID", "0770");
	                    } else {
	                    	jsonParam.put("PRCID", "0775");
	                    }
	                } else {
	                	// 위탁철거
						jsonParam.put("FUNCNAME", "ZPMN_CONFIRM_REMOVAL_PROC");
						// "0710' :  철거, '0715' :  철거송부"
	                    if (_SendCheck.receiptOrgCode.isEmpty())
	                    	jsonParam.put("PRCID", "0710");
	                    else {
	                    	jsonParam.put("PRCID", "0715");
	                    }
	                }
	                
	                // 철거 송부 시 조직 정보
	                if (!_SendCheck.receiptOrgCode.isEmpty()) {
	                	jsonParam.put("KOSTL", SessionUserData.getInstance().getOrgId());
	                	jsonParam.put("SKOSTL", SessionUserData.getInstance().getOrgId());
	                	jsonParam.put("SZKOSTL", SessionUserData.getInstance().getOrgId());
	                	jsonParam.put("RKOSTL", _SendCheck.receiptOrgCode);
	                }

	                if (mWbsInputbar.getVisibility()==View.VISIBLE) 
	                	jsonParam.put("POSID", mWbsNoText.getText().toString());     //2012.1.9 WBS 헤더로 전송변경...
	                
                	jsonParam.put("CHKSCAN", CHKSCAN);
	            }else if(mJobGubun.equals("형상구성(창고내)")){
	            	jsonParam.put("WORKID", "0020");
	                if (instConfPartType.equals("R")) {
	                	jsonParam.put("PRCID", "0910");
	                } else if (instConfPartType.equals("S")) {
	                	jsonParam.put("PRCID", "0920");
	                } else if (instConfPartType.equals("U")) {
	                	jsonParam.put("PRCID", "0930");
	                } else if (instConfPartType.equals("E")) {
	                	jsonParam.put("PRCID", "0940");
	                }
	                
	            	if (_SendCheck.instConfInheritanceYn.equals("Y")) {
	        			jsonParam.put("ZDOCRT", "X");
	        		} else {
	        			jsonParam.put("ZDOCRT", "");
	        		}

		        	if (_SendCheck.isDeviceId) {
		        		jsonParam.put("DEVICEID", _SendCheck.deviceId);
		        	}
		        	
		        	if (_SendCheck.isUFacCd) {
		        		if (_SendCheck.uPartType.equals("E")) {
		        			jsonParam.put("UBARCODE", "");
	                    } else {
	                    	jsonParam.put("UBARCODE", _SendCheck.UFacCd);
	                    }
		        	}
		        	
                	if (_SendCheck.locBarcodeInfo.getLocCd().startsWith("VS")) {
                		jsonParam.put("CHKSTORT", "X");
                    } else {
                    	jsonParam.put("CHKSTORT", "");
                    }
	                
	                jsonParam.put("CHKSCAN", CHKSCAN);
	                jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
	            }else if(mJobGubun.equals("형상해제(창고내)")){
	            	jsonParam.put("WORKID", "0020");
                	jsonParam.put("PRCID", "0950");
	                jsonParam.put("CHKSCAN", CHKSCAN);
	            }
    		} catch (JSONException e) {
    			_ErpBarException = new ErpBarcodeException(-1, "헤더정보 생성중 오류가 발생했습니다. "+e.getMessage());
    			return false;
			}

    		JSONArray jsonParamList = new JSONArray();
    		jsonParamList.put(jsonParam);

    		
    		Log.i(TAG, "SendDataInTask Body Parameter  Start...");
    		//-------------------------------------------------------
			// 작업된 바코드 바디정보 생성.
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();
			final List<Long> sendTreeKeys = getBarcodeTreeAdapter().getAllNextNodeList();
			Log.i(TAG, "SendDataInTask Body Record Count==>" + sendTreeKeys.size());
			
    		long itemCount = 0;
    		if (mJobGubun.equals("접수(팀간)")) {
    			if (sendTreeKeys.size()>0) {
    				Long selectedKey = getBarcodeTreeAdapter().getTreeId(0);
    				BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    				if (barcodeInfo != null) {
    					itemCount = Long.valueOf(barcodeInfo.getItemNo()) + (sendTreeKeys.size() * 10);
    				}
        		}
    		}
    		Log.i(TAG, "SendDataInTask Body itemCount==>" + itemCount);

    		// 설비상태.
    		String ZPSTATU = "";
    		if (mJobGubun.equals("납품상태변경") || mJobGubun.equals("설비상태변경")
    				|| mJobGubun.equals("입고(팀내)") || mJobGubun.equals("접수(팀간)")
    				|| mJobGubun.equals("수리완료") || mJobGubun.equals("개조개량완료")) {
    			ZPSTATU = _SendCheck.facStatusCode;
    		}
    		
    		Log.i(TAG, "SendDataInTask Body ZPSTATU==>" + ZPSTATU);
    		
    		_SendCount = 0;
    		//SortedSet<Long> itemKeys = new TreeSet<Long>(sendBarcodeMaps.keySet());
			for (Long key : sendTreeKeys) {
            	JSONObject jsonSubParam = new JSONObject();
            	
    	        try {
    	        	BarcodeListInfo sendBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);

    	        	// 바코드 16자리 이하 오류 처리
    	        	if (sendBarcodeInfo.getBarcode().length() < 16) {
    	        		_ErpBarException = new ErpBarcodeException(-1, "'" + sendBarcodeInfo.getBarcode() + "'는 처리할 수 없는\n설비바코드 입니다.");
	    	        	jsonSubParamList = null;
	        			return false;
	    	        }

    	        	// 납품취소 OTD validation
					if (mJobGubun.equals("납품취소") && _SendCheck.isCheckedOTD==null) {
						jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
						jsonSubParamList.put(jsonSubParam);
						_SendCount++; // 전송건수.
						continue;
					}

					// 상위바코드 -----------------------------------------------
					// TreeView에서 부모Node의 바코드정보
					Long parentKey = getBarcodeTreeAdapter().getManager().getParent(key);
					BarcodeListInfo parentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
					String parentBarcode = "";
					// 첫번째 row이면 상위바코드는 무조건 ""처리한다.
					// 설비 자체의 상위바코드 말고 트리의 상위바코드를 I/F 해야 함! sungho
					if (parentBarcodeInfo!=null && _SendCount > 0) {
						parentBarcode = parentBarcodeInfo.getBarcode(); // 상위바코드 방법 1
						//parentBarcode = sendBarcodeInfo.getuFacCd(); 	// 상위바코드 방법 2
					}
					// -- 상위바코드 -----------------------------------------------

					// 부품종류
	    	        String partTypeCode = "";
	    	        if (sendBarcodeInfo.getPartType().equals("E")) {
	    	        	partTypeCode = "99";
	    	        } else {
	    	        	partTypeCode = SuportLogic.getPartTypeCode(sendBarcodeInfo.getPartType());
	    	        }
	    	        
	    	        //---------------------------------------------------------
	    	        // scanValue 처리..
	    	        String scanValue = "";
	    	        if (mChkScanCheck.isChecked() || mJobGubun.equals("접수(팀간)")) {
	    	        	scanValue = sendBarcodeInfo.getScanValue();
	    	        }
	    	        if (scanValue.equals("3")) scanValue = "1";
	    	        // 스캔필수여부 미체크일때는 scanValue 무조건 null한다.
	    	        if (!CHKSCAN.equals("X")) scanValue = "";
	    	        //---------------------------------------------------------
                	
					// 조직변경여부
					String CHKZKOSTL = sendBarcodeInfo.getCheckOrgYn();
					if (CHKSCAN.equals("X") && scanValue.equals("0")) CHKZKOSTL = "N";		// 스캔필수 체크 후 스캔하지 않으면 조직변경 NO...... 2013.09.16 request by 정진우팀장님
					if (CHKZKOSTL.equals("X") || CHKZKOSTL.equals("N")) CHKZKOSTL = "X";
					else CHKZKOSTL = "";
					// -- 조직변경여부

	    	        // 접수(팀간) 또는 송부취소(팀간)은 상위를 스캔하지 않고 자신도 스캔하지 않았으면 전송하지 않는다....
                    if (mJobGubun.equals("접수(팀간)") || mJobGubun.equals("송부취소(팀간)")) {
                    	String parentScanValue = "0";
                    	
                    	// 랙 접수일 경우 유닛의 상위 쉘프 위의 상위 랙 스캔 여부 체크
                        if (parentKey != null) {
                        	BarcodeListInfo parentParcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
                        	parentScanValue = parentParcodeInfo.getScanValue();
                        	
                        	if (parentScanValue.equals("0")) {
                        		Long parentParentKey = getBarcodeTreeAdapter().getManager().getParent(parentKey);
                        		if (parentParentKey != null) {
                        			BarcodeListInfo parentParentParcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentParentKey);
                                	if (parentParentParcodeInfo != null) {
                                		parentScanValue = parentParentParcodeInfo.getScanValue();
                                	}
                        		}
                            }
                        }
                        
                        // 상위 스캔하지 않고 자기도 스캔하지 않으면 전송하지 않음
                    	if (parentScanValue.equals("0") && scanValue.equals("0")) {
                    		continue;
                    	}
                    }
	    	        // -- 접수(팀간) 또는 송부취소(팀간)은 상위를 스캔하지 않고 자신도 스캔하지 않았으면 전송하지 않는다....

                    // 부품종류, 품목 구분 -------------------------------------------------------------------------------------
                	jsonSubParam.put("PARTTYPE", partTypeCode);
                    // 철거는 품목구분을 전송하지 않는다. 
                    if (!mJobGubun.equals("철거")) {
	                    if (sendBarcodeInfo.getPartType().equals("E")) {
	                    	jsonSubParam.put("DEVTYPE", "30");
		                } else {
		                	jsonSubParam.put("DEVTYPE", "40");
		                }
                    }
                    // -- 부품종류, 품목 구분 ---------------------------------------------------------------------------------
	    	        
					if (mJobGubun.equals("납품입고")) {
						jsonSubParam.put("EXBARCODE", sendBarcodeInfo.getBarcode());
						jsonSubParam.put("HEQUI", parentBarcode);

						jsonSubParam.put("DFLAG", "1");
						jsonSubParam.put("LOCCODE", mThisLocCodeInfo.getLocCd());
						jsonSubParam.put("MAKTX", sendBarcodeInfo.getBarcodeName());
						
					} else if (mJobGubun.equals("납품취소") || mJobGubun.equals("배송출고") || mJobGubun.equals("고장등록")) {
						jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
						jsonSubParam.put("EXBARCODE", parentBarcode);
						jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
						jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
						jsonSubParam.put("SCAN", scanValue);
						if (mJobGubun.equals("고장등록")) {
							jsonSubParam.put("FECOD", _SendCheck.cboCode);
							jsonSubParam.put("FETXT", mCboReasonText.getText());
						} else {
							// 설비상태
							String zpstatu = "";
							if (mJobGubun.equals("납품취소")) zpstatu="0021";
							else if (mJobGubun.equals("배송출고")) {
								zpstatu="0270";
								if (CHKSCAN.equals("X") && scanValue.equals("0")) zpstatu="0081";		// 스캔필수 체크하고 스캔하지 않으면 설비상태 분실위험으로 강제 셋팅
							}
							jsonSubParam.put("ZPSTATU", zpstatu);
						}
						
					} else if (mJobGubun.equals("설비상태변경")) {
                    	jsonSubParam.put("EXBARCODE", parentBarcode);	// 상위바코드
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());			// 바코드
	                    jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
	                    jsonSubParam.put("SCAN", scanValue);
	                    jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
	                    jsonSubParam.put("ZPSTATU", ZPSTATU);		// 설비상태
	                    
					} else if (mJobGubun.equals("입고(팀내)")
							|| mJobGubun.equals("출고(팀내)")
							|| mJobGubun.equals("탈장")
							|| mJobGubun.equals("철거")
							|| mJobGubun.equals("형상해제(창고내)")) 
					{
                    	jsonSubParam.put("EXBARCODE", parentBarcode);					// 상위바코드
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());		// 바코드
                    	
                    	if (mJobGubun.equals("철거")) {
                    		if (!_SendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
                    			jsonSubParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());			// 철거는 위치바코드까지 넘긴다.
                    		}
                    	}

                    	if (!mJobGubun.equals("철거")) {
		                    jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                    	}
	                    
	                    jsonSubParam.put("SCAN", scanValue);
                    	jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);

                    	// 설비상태 - 입고(팀내)는 위에서 셋팅
	                    if (mJobGubun.equals("출고(팀내)")) {
	                    	ZPSTATU = "0270";				// 출고 설비상태코드 강제 셋팅 - 출고중
	                    } else if (mJobGubun.equals("탈장")) {
	                    	ZPSTATU = "0080";				
	                    } else if (mJobGubun.equals("철거")) {
		                    if (!_SendCheck.receiptOrgCode.isEmpty()) {
		                    	ZPSTATU = "0140";	// 철거즉시 송부
		                    } else {
		                    	ZPSTATU = "0190";	// 철거확정
		                    }
	                    }else if(mJobGubun.equals("형상해제(창고내)")){
	                    	ZPSTATU = sendBarcodeInfo.getFacStatus();
	                    }
                    	jsonSubParam.put("ZPSTATU", ZPSTATU);	// 설비상태

                    } else if (mJobGubun.equals("실장")) {
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
                    	jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
                    	jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                    	jsonSubParam.put("EXBARCODE", parentBarcode);
                    	jsonSubParam.put("KOSTL", sendBarcodeInfo.getOrgId());
                    	jsonSubParam.put("SCAN", scanValue);
                    	jsonSubParam.put("ZPSTATU", "0060");
                    	
                    } else if (mJobGubun.equals("송부(팀간)")) {
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
                    	jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
                    	jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                    	jsonSubParam.put("EXBARCODE", parentBarcode);
                    	jsonSubParam.put("SCAN", scanValue);
                    	jsonSubParam.put("ZPSTATU", "0140");
                    	
                    } else if (mJobGubun.equals("송부취소(팀간)")) {
                    	jsonSubParam.put("DOCNO", sendBarcodeInfo.getTransNo());
                    	
                    	int cancel_sort = jsonSubParamList.length() + 1;
                    	jsonSubParam.put("ITEMNO", String.valueOf(cancel_sort));   //그냥 순서 1,2,3,4,5등등 조심
                    	jsonSubParam.put("ITEM", sendBarcodeInfo.getItemNo());     //DOCNO의 아이템 번호임 헷갈리니 조심
                    	jsonSubParam.put("EXBARCODE", parentBarcode);
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
	                    jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);					// 송부취소(팀간)도 조직변경 여부 SAP로 전송 - request by 김희선 - 2014.01.01 DR-2013-56706 - 수정 : 류성호 2014.02.25
                        jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());

                    } else if (mJobGubun.equals("접수(팀간)")) {
                    	jsonSubParam.put("DOCNO", sendBarcodeInfo.getTransNo());
                    	jsonSubParam.put("ITEMNO", String.valueOf(jsonSubParamList.length()+1));
                    	if (sendBarcodeInfo.getScanValue().equals("2")) {
                    		itemCount += 10;
                    		jsonSubParam.put("ITEM", String.valueOf(itemCount));
                    	} else {
                    		jsonSubParam.put("ITEM", sendBarcodeInfo.getItemNo());
                    	}
                    	
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
                    	jsonSubParam.put("SCAN", scanValue);
                   		jsonSubParam.put("EXBARCODE", parentBarcode);
                    	
                    	jsonSubParam.put("ZPSTATU", ZPSTATU);		    // 설비상태
                    }else if(mJobGubun.equals("형상구성(창고내)")){
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
                    	jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
                    	jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                    	jsonSubParam.put("EXBARCODE", parentBarcode);
                    	jsonSubParam.put("KOSTL", sendBarcodeInfo.getOrgId());
                    	jsonSubParam.put("SCAN", scanValue);
                    	jsonSubParam.put("ZPSTATU", sendBarcodeInfo.getFacStatus());
                    	System.out.println("형상구성 설비바코드 : " + sendBarcodeInfo.getBarcode() + "조직코드 " + CHKZKOSTL);
                    	System.out.println("형상구성 설비바코드 : " + sendBarcodeInfo.getBarcode() + "조직코드 " + sendBarcodeInfo.getOrgId());
                    }else if(mJobGubun.equals("형상해제(창고내)")){
                    	
                    }
    	        } catch (JSONException e1) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
    	        	jsonSubParamList = null;
        			return false;
    	        } catch (Exception e) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
    	        	jsonSubParamList = null;
        			return false;
    	        }
    	        
    	        jsonSubParamList.put(jsonSubParam);
    	        
    	        _SendCount++;  // 전송건수.
            } // 작업바코드 For End.
			
			
			if (mJobGubun.equals("접수(팀간)") || mJobGubun.equals("송부취소(팀간)")) {
                if (_SendCount == 0) {
                	_ErpBarException = new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.\r\n스캔확인 하지 않은 설비바코드는\n\r전송되지 않습니다.", BarcodeSoundPlay.SOUND_ERROR);
                	return false;
                }
            }
            
			// 전송 ----------------------------------------------------------------------------------
            Log.i(TAG, "SendDataInTask SendHttpController "+mJobGubun+" Start...");
            
            System.out.println("jsonParamList" + jsonParamList);
			System.out.println("jsonSubParamList" + jsonSubParamList);
            
            try {
        		SendHttpController sendhttp = new SendHttpController();
        		String sendPath = "";
				if (mJobGubun.equals("납품입고")) {
					sendPath = HttpAddressConfig.PATH_POST_BUYOUTINTO;
				} else if (mJobGubun.equals("납품취소")) {
					// 납품취소 최초 전송은 OTD validation
					if (_SendCheck.isCheckedOTD==null) {
						sendPath = HttpAddressConfig.PATH_GET_OUTINTOSTATUSCHANGECHECK;
					} else {
						sendPath = HttpAddressConfig.PATH_POST_OUTINTOSTATUS_CHANGE;
					}
				} else if (mJobGubun.equals("배송출고")) {
					sendPath = HttpAddressConfig.PATH_POST_DELIVERY_MM;
				} else if (mJobGubun.equals("고장등록")) {
					sendPath = HttpAddressConfig.PATH_POST_OOSREG;
				} else if (mJobGubun.equals("설비상태변경")) {
					sendPath = HttpAddressConfig.PATH_POST_STATUSCHANGE;
	            } else if (mJobGubun.equals("입고(팀내)")) {
					sendPath = HttpAddressConfig.PATH_POST_OUTINTO;
	            } else if (mJobGubun.equals("출고(팀내)")) {
					sendPath = HttpAddressConfig.PATH_POST_DELIVERY;
	            } else if (mJobGubun.equals("탈장")) {
					sendPath = HttpAddressConfig.PATH_POST_UNMOUNT;
	            } else if (mJobGubun.equals("실장")) {
					sendPath = HttpAddressConfig.PATH_POST_SECTION_CHIEF;
	            } else if (mJobGubun.equals("송부(팀간)")) {
					sendPath = HttpAddressConfig.PATH_POST_SEND_DEVICE_NEW;
	            } else if (mJobGubun.equals("송부취소(팀간)")) {
					sendPath = HttpAddressConfig.PATH_POST_SEND_DEVICE_CANCEL;
	            } else if (mJobGubun.equals("접수(팀간)")) {
					sendPath = HttpAddressConfig.PATH_POST_RECEPTSCAN_NEW;
	            } else if (mJobGubun.equals("철거")) {
					sendPath = HttpAddressConfig.PATH_POST_REMOVALSCAN;
	            } else if(mJobGubun.equals("형상구성(창고내)")){
	            	sendPath = HttpAddressConfig.PATH_POST_FORM_MAKE;
	            } else if(mJobGubun.equals("형상해제(창고내)")){
	            	sendPath = HttpAddressConfig.PATH_POST_FORM_CLEAR;
	            }
				
				_OutputParameter = sendhttp.sendToServer(sendPath, jsonParamList, jsonSubParamList);
            	
            	if (_OutputParameter == null) {
            		throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송중 오류가 발생했습니다." );
            	}
    			// -- 전송 ----------------------------------------------------------------------------------

            	// 납품입고/납품취소 OTD 결과 Display
				JSONArray jsonArray =_OutputParameter.getBodyResults();
				boolean errorFlag = false;
				if (mJobGubun.equals("납품입고") || mJobGubun.equals("납품취소") && _SendCheck.isCheckedOTD==null) {
					for (int i=0; i<jsonArray.length(); i++) {
						try {
							JSONObject jsonobj = jsonArray.getJSONObject(i);
							
							String barcode = "";
							String RSLT = jsonobj.getString("RSLT").trim();
							String resultMessage = jsonobj.getString("MESG").trim();
							if (mJobGubun.equals("납품취소")) {
	                            barcode = jsonobj.getString("BARCODE");
	                            if (RSLT.equals("S")) {
	                            	resultMessage = "유효성 검증 성공";
	                            } else {
	                            	errorFlag = true;
	                            }
							} else if (mJobGubun.equals("납품입고")) {
								if (RSLT.equals("S")) continue;
								barcode = jsonobj.getString("EXBARCODE");
							}
							
							Log.i(TAG, "_OutputMaps   barcode==>" + barcode);
							Log.i(TAG, "_OutputMaps   resultMessage==>" + resultMessage);
	
							if (!resultMessage.isEmpty()) {
								_OutputMaps.put(barcode, RSLT+":"+resultMessage);
							}
						} catch (JSONException e) {
							throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송결과 처리중 오류가 발생했습니다." + e.getMessage());
						}
					}
					if (errorFlag) {
						_ErpBarException = new ErpBarcodeException(-1, "변경하려는 설비(하위 포함)의 상태가\n\r처리 불가능한 값을 포함하고 있습니다.\n\r처리 불가능한 사유는\n\r각 행의 우측 끝에서 보실 수 있습니다.", BarcodeSoundPlay.SOUND_ERROR);
					}
				}
            } catch (ErpBarcodeException e) {
				Log.i(TAG, e.getErrMessage());
				_ErpBarException = e;
				return false;
			}
            
            try {
            	if (mJobGubun.equals("고장등록")) {
            		//final List<Long> sendTreeKeys = getBarcodeTreeAdapter().getAllNextNodeList();
    				for (Long key : sendTreeKeys) {
        	        	BarcodeListInfo sendBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(key);
        	        	if (!TextUtils.isEmpty(sendBarcodeInfo.getCboPhotoUri())) {
        	        		SendHttpController sendhttp_multipart = new SendHttpController();
        					sendhttp_multipart.imageFileWrite(sendBarcodeInfo.getBarcode(), sendBarcodeInfo.getCboPhotoUri());
        	        	}
    				}
    			}
            } catch (ErpBarcodeException e) {
    			Log.i(TAG, e.getErrMessage());
    			//_ErpBarException = e;
    			//return false;
    		}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mSendButton.setEnabled(true);

			if (result) {
				// OTD 서버로부터 받은 결과값 Display
				if (mJobGubun.equals("납품입고") || mJobGubun.equals("납품취소")) {
					for (String barcode : _OutputMaps.keySet()) {
						String resultMessage = _OutputMaps.get(barcode);
						
						TreeNodeInfo<Long> treeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(barcode);
						if (treeNode != null) {
							BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(treeNode.getId());
							barcodeInfo.setComment(resultMessage);
							getBarcodeTreeAdapter().setBarcodeListInfo(treeNode.getId(), barcodeInfo);
						}
					}
					getBarcodeTreeAdapter().refresh();
				}
				// -- OTD 서버로부터 받은 결과값 Display
				
				// 납품취소 유효성 검증 오류 시
				if (mJobGubun.equals("납품입고")) {
					String message = "# 전송건수 : " + _SendCount + "건 \n"
							+ "# 성공건수 : " + (_SendCount - _OutputMaps.size()) + "건 \n"
							+ "# 실패건수 : " + _OutputMaps.size() + "건 \n\n";
					
					final String sendMessage = _OutputParameter.getOutMessage();
					Log.i(TAG, " SendResult  sendMessage==>" + sendMessage);
					
					//-----------------------------------------------------------
					// Yes/No Dialog
					//-----------------------------------------------------------
					if (GlobalData.getInstance().isGlobalAlertDialog()) return;
					GlobalData.getInstance().setGlobalAlertDialog(true);
					
					GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_ASTERISK);
					final Builder builder = new AlertDialog.Builder(mActivity);
					builder.setIcon(android.R.drawable.ic_menu_info_details);
					builder.setTitle("알림");
					TextView msgText = new TextView(getBaseContext());
					msgText.setPadding(10, 30, 10, 30);
					msgText.setText(message);
					msgText.setGravity(Gravity.CENTER);
					msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					msgText.setTextColor(Color.BLACK);
					builder.setView(msgText);
					builder.setCancelable(false);
					builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	GlobalData.getInstance().setGlobalAlertDialog(false);
			        	
			            	boolean isInitScreen = false;
			            	if (_OutputMaps.size()==0) {
			            		isInitScreen = true;
			            	}
			            	
			            	sendWorkResult(sendMessage, isInitScreen);
			            	return;
			            }
			        });
			        AlertDialog dialog = builder.create();
			        dialog.show();
			        return;
					
				} else if (mJobGubun.equals("납품취소")) {
					// 1. 납품취소 validation
					if (_SendCheck.isCheckedOTD==null) {
						if (_ErpBarException != null) {
							GlobalData.getInstance().showMessageDialog(_ErpBarException);			// 납품취소 validation 오류 시
							return;
						} else {
							// 2. 진짜 전송
							_SendCheck.isCheckedOTD="check done!";
							new SendDataInTask(_SendCheck, mActivity).execute();
							return;
						}
					} else {
						// 3. 진짜 전송 결과
						String message = "# 전송건수 : " + _SendCount + "건 \n"
								+ "# 성공건수 : " + (_SendCount - _OutputMaps.size()) + "건 \n"
								+ "# 실패건수 : " + _OutputMaps.size() + "건 \n\n";
						
						final String sendMessage = _OutputParameter.getOutMessage();
						
						//-----------------------------------------------------------
						// Yes/No Dialog
						//-----------------------------------------------------------
						if (GlobalData.getInstance().isGlobalAlertDialog()) return;
						GlobalData.getInstance().setGlobalAlertDialog(true);
						
						GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_ASTERISK);
						final Builder builder = new AlertDialog.Builder(mActivity); 
						builder.setIcon(android.R.drawable.ic_menu_info_details);
						builder.setTitle("알림");
						TextView msgText = new TextView(getBaseContext());
						msgText.setPadding(10, 30, 10, 30);
						msgText.setText(message);
						msgText.setGravity(Gravity.CENTER);
						msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
						msgText.setTextColor(Color.BLACK);
						builder.setView(msgText);
						builder.setCancelable(false);
						builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int which) {
				            	GlobalData.getInstance().setGlobalAlertDialog(false);
				        	
				            	
				            	boolean isInitScreen = false;
				            	if (_OutputMaps.size()==0) {
				            		isInitScreen = true;
				            	}
				            	
				            	sendWorkResult(sendMessage, isInitScreen);
				            	return;
				            }
				        });
				        AlertDialog dialog = builder.create();
				        dialog.show();
				        return;
					}
				} else {
					// 실제 전송 후..............
					String message = "# 전송건수 : " + _SendCount + "건\n\n" + _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
					
					final String sendMessage = _OutputParameter.getOutMessage();
					
					//-----------------------------------------------------------
					// Yes/No Dialog
					//-----------------------------------------------------------
					if (GlobalData.getInstance().isGlobalAlertDialog()) return;
					GlobalData.getInstance().setGlobalAlertDialog(true);
					
					GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_ASTERISK);
					final Builder builder = new AlertDialog.Builder(mActivity); 
					builder.setIcon(android.R.drawable.ic_menu_info_details);
					builder.setTitle("알림");
					TextView msgText = new TextView(getBaseContext());
					msgText.setPadding(10, 30, 10, 30);
					msgText.setText(message);
					msgText.setGravity(Gravity.CENTER);
					msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					msgText.setTextColor(Color.BLACK);
					builder.setView(msgText);
					builder.setCancelable(false);
					builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	GlobalData.getInstance().setGlobalAlertDialog(false);
			        	
			            	sendWorkResult(sendMessage, true);
			            	return;
			            }
			        });
			        AlertDialog dialog = builder.create();
			        dialog.show();
			        return;
				}
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}

		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
    }

    /**
	 * 위치바코드 정보 조회.
	 */
	private void getLocBarcodeData(String barcode) {
		if (isBarcodeProgressVisibility()) return;
    	setBarcodeProgressVisibility(true);
		
		mThisLocCodeInfo = null;
		LocBarcodeService locbarcodeService = new LocBarcodeService(getApplicationContext(), mLocBarcodeHandler);
		locbarcodeService.search(barcode);
	}
	/**
	 * LocBarcodeInfo정보 조회 결과 Handler
	 */
    private final Handler mLocBarcodeHandler = new Handler() {
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
            	//---------------------------------------------------
            	// 위치바코드 조회 정보가 없으면 초기화한다.
            	//---------------------------------------------------
            	mScannerHelper.focusEditText(mLocCdText);
            	mLocNameText.setText("");

            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 위치코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
            	break;
            case LocBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	
            	//---------------------------------------------------
            	// 위치바코드 조회 정보가 없으면 초기화한다.
            	//---------------------------------------------------
            	mScannerHelper.focusEditText(mLocCdText);
            	mLocNameText.setText("");
            	
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void successLocBarcodeProcess() {
		mLocCdText.setText(mThisLocCodeInfo.getLocCd());
		mLocNameText.setText(mThisLocCodeInfo.getLocName());

		// WBS정보 화면을 호출한다.
		if (mWbsInputbar.getVisibility() == View.VISIBLE && mWbsNoText.isEnabled()) {
			//-------------------------------------------------------------
	    	// 단계별 작업을 여기서 처리한다.
	    	//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				//-------------------------------------------------------------
				// onActivityResult 에서 WBS_NO번호를 받을때 다음 Step진행하므로 여기서는 Stop한다.
				//     음영지역이면 WBS번호 호출화면을 Open한다.
				//-------------------------------------------------------------
				String offlineYn = GlobalData.getInstance().getJobActionManager().getThisWorkInfo().getOfflineYn();
				if (!offlineYn.equals("Y")) {
					GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
					return;
				}
			} else {
				try {
					GlobalData.getInstance().getJobActionManager().setStepItem(
							JobActionStepManager.JOBTYPE_LOC, "locCd", mThisLocCodeInfo.getLocCd());
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
			
			
			getWBSInfos(mThisLocCodeInfo.getLocCd(), "1");
			return;
		}
		
		//-------------------------------------------------------------
    	// 단계별 작업을 여기서 완료처리한다.
		//   위치바코드에 대해서..
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			//return;
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_LOC, "locCd", mThisLocCodeInfo.getLocCd());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}

		if (mDeviceInputbar.getVisibility() == View.VISIBLE) {
			// 위치바코드에 장치바코드로 조회한 경우 장치바코드를 조회한다.
			if (!mThisLocCodeInfo.getDeviceId().isEmpty()) {
				changeDeviceId(mThisLocCodeInfo.getDeviceId());
				return;
			}
		}
		
		//-----------------------------------------------------------
		// 바코드 입력 EditText들을 활성화/비활성화 처리한다.
		//-------------------------------------------------------
		setTextBoxEnable();
		mScannerHelper.focusEditText(mFacCdText);
//		getLocAddInfo(mThisLocCodeInfo.getLocCd(), "위치바코드");
    }
       
    /**
     * WBS정보 조회 화면을 오픈한다.
     * 
     * @param locCd
     */
    private void showSearchWbsCheckActivity(String locCd) {
    	if (locCd.isEmpty()) return;
    	
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SearchWbsCheckActivity.class);
		intent.putExtra(SearchWbsCheckActivity.INPUT_LOC_CD, locCd);
		intent.putExtra(SearchWbsCheckActivity.INPUT_WORK_CAT, "");		// 철거 wbs 조회는 work_cat empty
		startActivityForResult(intent, ACTION_SEARCHWBSCHECKACTIVITY);
    }
    
    /**
	 * WBS정보 조회.
	 */
    private void getWBSInfos(String locCd, String workCat) {
		if (isBarcodeProgressVisibility()) return;
    	setBarcodeProgressVisibility(true);

    	WBSBarcodeService wbsService = new WBSBarcodeService(mWBSInfoHandler);
    	wbsService.search(locCd, workCat);
	}
	
	/**
	 * WBS 정보 조회 결과 Handler
	 */
    private final Handler mWBSInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
        	//---------------------------------------------------
        	// WBS정보 조회후에는 무조건 설비바코드로 Focus이동한다.
        	//---------------------------------------------------
        	mScannerHelper.focusEditText(mFacCdText);
        	
            switch (msg.what) {
            case WBSBarcodeService.STATE_SUCCESS :
            	//String findedMessage = msg.getData().getString("message");

                //---------------------------------------------------
                // WBS정보 조회 화면을 오픈한다.
                //---------------------------------------------------
                showSearchWbsCheckActivity(mThisLocCodeInfo.getLocCd());
        		
                break;
            case WBSBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS번호가 없습니다. "));
            	break;
            case WBSBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    
	/**
	 * 장치바코드 정보 조회.
	 */
	private void getDeviceBarcodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
    	setBarcodeProgressVisibility(true);
    	
		DeviceBarcodeService devicebarcodeService = new DeviceBarcodeService(mDeviceBarcodeHandler);
		devicebarcodeService.search(deviceId);
	}
	/**
	 * DeviceBarcodeInfo정보 조회 결과 Handler
	 */
    private final Handler mDeviceBarcodeHandler = new Handler() {
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
        
        private void successDeviceBarcodeProcess(DeviceBarcodeInfo deviceBarcodeInfo) {
			Log.i(TAG, "장치바코드 getDeviceId==>"+deviceBarcodeInfo.getDeviceId());
			Log.i(TAG, "장치바코드 getItemCode==>"+deviceBarcodeInfo.getItemCode());
			Log.i(TAG, "장치바코드 getItemName==>"+deviceBarcodeInfo.getItemName());
			Log.i(TAG, "장치바코드 getOperationSystemCode==>"+deviceBarcodeInfo.getOperationSystemCode());
			
			mDeviceIdText.setText(deviceBarcodeInfo.getDeviceId());
			String deviceInfo = deviceBarcodeInfo.getItemCode() + "/" + deviceBarcodeInfo.getItemName() + "/"
					+ deviceBarcodeInfo.getLocationCode() + "/" + deviceBarcodeInfo.getDeviceStatusName();
			mDeviceIdInfoText.setText(deviceInfo);
		    mOprSysCdText.setText(deviceBarcodeInfo.getOperationSystemCode());
		    
		    // 위치바코드에 장치바코드를 조회했을때 장치바코드 입력창을 비활성화한다.
		    if(mJobGubun.equals("고장정보") || mJobGubun.equals("고장수리이력")){
		    	((EditText) findViewById(R.id.treescan_device_nm_failure)).setText(deviceBarcodeInfo.getItemName());
		    	if (mJobGubun.equals("고장수리이력")){
		    		if(!isFacQuMode){
		    			mFacCdText.setText("");
		    			((EditText) findViewById(R.id.treescan_fac_facNm_failure)).setText("");
		    			((EditText) findViewById(R.id.treescan_fac_facCode_failure)).setText("");
		    		}else{
		    			isFacQuMode = false;
		    		}
		    		getFailureListData();
        		}
		    }
		    else{
		    	if (!mThisLocCodeInfo.getDeviceId().isEmpty()) {
		    		mDeviceIdText.setEnabled(false);
		    	} else {
		    		mDeviceIdText.setEnabled(true);
		    	}
		    }
		    

			//-----------------------------------------------------------
			// 화면 초기 Setting후 변경여부 Flag
			//-------------------------------------------------------
			GlobalData.getInstance().setChangeFlag(true);
			GlobalData.getInstance().setSendAvailFlag(true);
		    
		    String thisFacCd = mFacCdText.getText().toString().trim();
		    String thisUFacCd = mUFacCdText.getText().toString().trim();
		    if (mUFacInputbar.getVisibility() == View.VISIBLE && mUFacCdText.isEnabled()
		    		&& !thisFacCd.isEmpty() && thisUFacCd.isEmpty()) {
		    	// 설비바코드가 값이 존재하고 상위바코드 값이 없을때 상위바코드로 Cursor Focus이동한다.
	    		mScannerHelper.focusEditText(mUFacCdText);
		    } else {
		    	// 설비바코드로 Cursor Focus이동한다.
		    	if(!mJobGubun.equals("고장수리이력")){
		    		mScannerHelper.focusEditText(mFacCdText);
		    	}
		    }

		    // 위치 주소 정보 조회 
	    	pLocCd_deviceId = deviceBarcodeInfo.getLocationCode();
			pLocNm_deviceId = deviceBarcodeInfo.getLocationShortName();
		    
		    //-------------------------------------------------------------
	    	// 단계별 작업을 여기서 처리한다.
	    	//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			} else {
				try {
					GlobalData.getInstance().getJobActionManager().setStepItem(
							JobActionStepManager.JOBTYPE_DEVICE, "deviceId", deviceBarcodeInfo.getDeviceId());
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
        }
    };
    
	/**
	 * 논리위치바코드(창고위치) 정보 조회.
	 */
	public void getLogicalLocationData() {
		if (mLogicalLocationInTask == null) {
			setProgressBarIndeterminateVisibility(true);
			
			mLogicalLocationInTask = new LogicalLocationInTask();
			mLogicalLocationInTask.execute((Void) null);
		}
	}
	
	private class LogicalLocationInTask extends AsyncTask<Void, Void, Boolean> {
		private JSONArray _JsonResults = null;
		private ErpBarcodeException _ErpBarException;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController lochttp = new LocationHttpController();
				_JsonResults = lochttp.getLogicalLocationCode();
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1, "논리위치바코드(창고위치) 결과 정보가 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.i(TAG, "논리위치바코드(창고위치)  서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
    			_JsonResults = null;
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setProgressBarIndeterminateVisibility(false);
			mLogicalLocationInTask = null;
			
			if (result) {
				nextLogicalLocationProcess(_JsonResults);
			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			setProgressBarIndeterminateVisibility(false);
			mLogicalLocationInTask = null;
			super.onCancelled();
		}
	}
	
	private void nextLogicalLocationProcess(JSONArray jsonResults) {
		boolean isLocFlag = false;
		roomTypeCode = "";
        String locCd = "";
        String locName = "";
		
		for (int i=0;i<jsonResults.length();i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				
				roomTypeCode = jsonobj.getString("storageType");
				locCd = jsonobj.getString("storageLocationCode");
				locName = jsonobj.getString("storageLocationName");
				
				Log.i(TAG, "논리위치바코드(창고위치)  roomTypeCode==>"+roomTypeCode);
				Log.i(TAG, "논리위치바코드(창고위치)  locCd==>"+locCd);
				Log.i(TAG, "논리위치바코드(창고위치)  locName==>"+locName);
				Log.i(TAG, "논리위치바코드(창고위치)  getOrgTypeCode==>"+SessionUserData.getInstance().getOrgTypeCode());
				
				if (!SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) {
                    if (roomTypeCode.equals("06")) {
                    	isLocFlag = true;
                    }
                } else {
                    if (roomTypeCode.equals("04")) {
                    	isLocFlag = true;
                        break;
                    }
                }
				
			} catch (JSONException e) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "사용자의 위치정보 유효성 체크(JSON)중 오류가 발생했습니다." + e.getMessage()));
				return;
			} catch (Exception e) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "사용자의 위치정보 유효성 체크중 오류가 발생했습니다." + e.getMessage()));
				return;
			}
		}
		
		if (true) return;	// DR-2014-31084 : 납품입고, 부외실물등록요청 - 위치바코드 필드 스캔원칙으로 변경 - request by 박장수 modify by 류성호 2014.07.03
		if (isLocFlag) {
		    mLocCdText.setText(locCd);
		    changeLocCd(locCd);
		    //mLocNameText.setText(locName);
		}
	}
	
	/**
	 * 상위바코드로 SAP에서 설비바코드정보를 조회한다.
	 * 
	 * @param barcode
	 */
    private void getUFacCdInfos(String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		//-----------------------------------------------------------
        // Screen Clear...
		//-----------------------------------------------------------
		mUFacCdText.setText("");
		mUPartTypeText.setText("");
		mThisUFacInfo = null;    // 현재 스캔한 상위바코드 정보 초기화.
		
    	SAPBarcodeService sapBarcodeService = new SAPBarcodeService(new UFacCdInfoHandler(barcode));
    	sapBarcodeService.search("", "", barcode);
	}
    private class UFacCdInfoHandler extends Handler {
    	private String _Barcode;
		
		public UFacCdInfoHandler(String barcode) {
			_Barcode = barcode;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

            	//-------------------------------------------------------------
            	// 상위바코드 유효성 체크한다.
            	//-------------------------------------------------------------
        		UFacCheck uFacCheck = new UFacCheck();
        		if (!_Barcode.isEmpty() && !_Barcode.equals(sapItems.get(0).getBarcode())) {
        			uFacCheck.change_barcode_yn = "Y";
                } else {
                	uFacCheck.change_barcode_yn = "N";
            	}
        		uFacCheck.barcodeItem = sapItems.get(0);
        		uFacCheck.partTypeCheckYn = null;
        		nextUFacCdProcess(uFacCheck);
                break;

            case SAPBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 상위바코드입니다."));
            	break;
            case SAPBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    }
    
    /**
     * 상위바코드 체크 Class
     */
    public class UFacCheck {
    	public String change_barcode_yn = "N";
    	public BarcodeListInfo barcodeItem = null;
    	public String partTypeCheckYn = null;
    }
    private void nextUFacCdProcess(final UFacCheck uFacCheck) {
		Log.i(TAG, "nextUFacCdProcess  mJobGubun==>"+mJobGubun);
		Log.i(TAG, "nextUFacCdProcess  barcode==>"+uFacCheck.barcodeItem.getBarcode());
		Log.i(TAG, "nextUFacCdProcess  getBarcodeName==>"+uFacCheck.barcodeItem.getBarcodeName());
		Log.i(TAG, "nextUFacCdProcess  getDeviceId==>"+uFacCheck.barcodeItem.getDeviceId());
		Log.i(TAG, "nextUFacCdProcess  getLocCd==>"+uFacCheck.barcodeItem.getLocCd());

		
		//-----------------------------------------------------------
		// 스캔한 바코드와 서버에서 조회된 바코드가 다를때 여기서 중복 체크한다.
		//-----------------------------------------------------------
		if (uFacCheck.change_barcode_yn.equals("Y")) {
			Long thisSelectedKey = getBarcodeTreeAdapter().getBarcodeKey(uFacCheck.barcodeItem.getBarcode());
	    	// 중복스캔입니다...............................
	    	if (thisSelectedKey != null) {
	        	GlobalData.getInstance().showMessageDialog(
	        			new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + uFacCheck.barcodeItem.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
	        	return;
	        }
		}
		
		
    	if (getLocInputbarVisibility() == View.VISIBLE) {
    		if (!uFacCheck.barcodeItem.getLocCd().trim().equals(mThisLocCodeInfo.getLocCd().trim())) {
        		GlobalData.getInstance().showMessageDialog(
        				new ErpBarcodeException(-1, "스캔하신 위치바코드와 상위바코드의\n\r위치바코드가 상이합니다.\n\r동일한 위치의 상위바코드를\n\r스캔하세요."));
        		mScannerHelper.focusEditText(mUFacCdText);
        		return;
        	}
    	}
    	
    	// 베이위치로 실장시 상위바코드는 무조건 운용 상태이어야 함
        // DR_2013_54598_실장의 모든위치에서 상위바코드 운용아니면 실장 불가_2014.01.15 - request by 김희선 2014.01.08
    	//if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && !mThisLocCodeInfo.getLocCd().endsWith("0000")) {
    	if(mJobGubun.equals("실장")){
    		if (!uFacCheck.barcodeItem.getFacStatus().equals("0060")) {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "상위 설비의 상태가 '" + uFacCheck.barcodeItem.getFacStatusName() + "'인\n\r상위 설비는 '" + mJobGubun + "' 작업을\n\r하실 수 없습니다."));
    			mScannerHelper.focusEditText(mUFacCdText);
    			return;
    		}
    		//}
    		
    		/******************************************************************
    		 * request by 강준석 - 2012.07.07
    		 *   실장 되는 설비의 상태가 불용관련된거 실장못하게 validation 걸었는데..
    		 *   상태 추가 좀 해주세요..
    		 *   0021 납품취소 -> 불가
    		 *   0080 탈장
    		 *   0120 고장
    		 *   0130 수리의뢰
    		 *   0160 수리완료송부  
    		 *   0200 불용대기
    		 *   0210 불용요청 -> 불가
    		 *   0240 불용확정 -> 불가
    		 *   0260 사용중지 -> 불가
    		 *   0270 출고중"
    		 *    * 탈장 -> 2012.08.01
    		 *   상위바코드가 탈장이면 실장 안되게...
    		 *   코드 0080
    		 */
    		if (uFacCheck.barcodeItem.getFacStatus().equals("0021") || uFacCheck.barcodeItem.getFacStatus().equals("0080") 
    				|| uFacCheck.barcodeItem.getFacStatus().equals("0120") || uFacCheck.barcodeItem.getFacStatus().equals("0130")
    				|| uFacCheck.barcodeItem.getFacStatus().equals("0160") || uFacCheck.barcodeItem.getFacStatus().equals("0200")
    				|| uFacCheck.barcodeItem.getFacStatus().equals("0210") || uFacCheck.barcodeItem.getFacStatus().equals("0240")
    				|| uFacCheck.barcodeItem.getFacStatus().equals("0260") || uFacCheck.barcodeItem.getFacStatus().equals("0270"))  {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "상위 설비의 상태가 '" + uFacCheck.barcodeItem.getFacStatusName() + "'인 설비는\n\r'" + mJobGubun + "' 작업을\n\r하실 수 없습니다."));
    			mScannerHelper.focusEditText(mUFacCdText);
    			return;
    		}
    		
    		if (uFacCheck.barcodeItem.getDeviceId().isEmpty()) {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "장치ID가 없는 상위설비입니다.\n\r상위설비부터 처리하신 후\n\r다시 실행하세요."));
    			mScannerHelper.focusEditText(mUFacCdText);
    			return;
    		}
    	}else{
    		if (uFacCheck.barcodeItem.getPartTypeCode().equals(""))  {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'단품'은 형상구성 대상 설비가 아닙니다."));
    			mScannerHelper.focusEditText(mUFacCdText);
    			return;
    		}
    		
    		if (!uFacCheck.barcodeItem.getFacStatus().equals("0100") && !uFacCheck.barcodeItem.getFacStatus().equals("0110") && !uFacCheck.barcodeItem.getFacStatus().equals("0020"))  {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'유휴/예비/납품입고' 상태만 \n\r형상구성이 가능합니다."));
    			mScannerHelper.focusEditText(mUFacCdText);
    			return;
    		}
    	}

        if (uFacCheck.barcodeItem.getOrgId().isEmpty()) {
        	GlobalData.getInstance().showMessageDialog(
        			new ErpBarcodeException(-1, "운용조직이 없는 상위설비입니다.\n\r상위설비부터 처리하신 후\n\r다시 실행하세요."));
    		mScannerHelper.focusEditText(mUFacCdText);
    		return;
        }
        
        // 실장 PartType - 랙실장인지 쉘프 실장인지...
        String partType_InstConf = getBarcodeTreeAdapter().getInstConfPartType();
        boolean errorFlag = false;
        if (partType_InstConf.equals("S")) {
            // E-S 가능 -> 단품 랙
        	if ((!uFacCheck.barcodeItem.getPartType().equals("R")) && (!uFacCheck.barcodeItem.getPartType().equals("E"))) {
        		errorFlag = true;
        	}
        } else if (partType_InstConf.equals("U")) {
            // E도 UNIT의 상위가 될 수 있음 
        	//  - request by 강준석 2012.06.14 -> 2012.07.26 오후 7:50강준석그 이후로.. 단품은 단독 레벨로 붙는다고.. 막아주세요.....
        	if ((!uFacCheck.barcodeItem.getPartType().equals("R"))
					&& (!uFacCheck.barcodeItem.getPartType().equals("S"))
					&& (!uFacCheck.barcodeItem.getPartType().equals("U"))) {
        		errorFlag = true;
        	}
        }
        
        if (errorFlag) {
    		String partTypeName = uFacCheck.barcodeItem.getPartTypeName();
    		String devTypeName = uFacCheck.barcodeItem.getDevTypeName();
    		if (devTypeName.equals("단품")) partTypeName = devTypeName;
        	GlobalData.getInstance().showMessageDialog(
        			new ErpBarcodeException(-1, "'" +  SuportLogic.getNodeStringType(partType_InstConf) + "'의 상위바코드로 '" 
        					+ partTypeName + "' 을\n\r스캔 하실 수 없습니다."));
    		mScannerHelper.focusEditText(mUFacCdText);
            return;
        }

        mUFacCdText.setText(uFacCheck.barcodeItem.getBarcode());
        mUPartTypeText.setText(uFacCheck.barcodeItem.getPartType());

        if(!mJobGubun.equals("형상구성(창고내)")){
        	// 실장일때 상위바코드 "U"일때 계속 진행할지 물어본다.
    		if (uFacCheck.barcodeItem.getPartType().equals("U")) {
            	if (uFacCheck.partTypeCheckYn == null) {
            		uFacCheck.partTypeCheckYn = "N";
            		
            		//-----------------------------------------------------------
            		// Yes/No Dialog
            		//-----------------------------------------------------------
            		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
            		GlobalData.getInstance().setGlobalAlertDialog(true);
            		
            		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
            		String message = "상위 설비로 '유닛'을 스캔하였습니다.\n\r진행하시겠습니까?";
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
                        	
                        	uFacCheck.partTypeCheckYn = "Y";
                        	nextUFacCdProcess(uFacCheck);
                        }
                    });
                    builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	GlobalData.getInstance().setGlobalAlertDialog(false);
                        	
                        	mUFacCdText.setText("");
                    		mUPartTypeText.setText("");
                    		mScannerHelper.focusEditText(mUFacCdText);
                    		return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
            	}
        	}
        }else{
        	if(partType_InstConf.equals(uFacCheck.barcodeItem.getPartType())){
        		String temp = "";
        		if(partType_InstConf.equals("S")){
        			temp = "Shelf";
        		}else if(partType_InstConf.equals("R")){
        			temp = "Rack";
        		}else if(partType_InstConf.equals("U")){
        			temp = "Unit";
        		}
        		
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'" +  temp + "'의 상위바코드로 '" + temp + "' 을\n\r스캔 하실 수 없습니다."));
        	}
        }
        
    	  
		//-----------------------------------------------------------
        // 현재 스캔한 상위설비바코드 정보를 저장한다.
        mThisUFacInfo = uFacCheck.barcodeItem;
        
        //-----------------------------------------------------------
        // 상위바코드로  Scan Focus 이동한다.
		mScannerHelper.focusEditText(mFacCdText);
		
		//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 true
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
		
		//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_UFAC, "uFacCd", mThisUFacInfo.getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 음영지역 위치바코드 작업.
     * @param barcode
     */
    private void getOfflineLocBarcodeData(String barcode) {
    	mLocCdText.setText(barcode);
    	mLocNameText.setText(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	
    	mThisLocCodeInfo = new LocBarcodeInfo();
    	mThisLocCodeInfo.setLocCd(barcode);
    	mThisLocCodeInfo.setLocName(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	
    	//-----------------------------------------------------------
		// 바코드 입력 EditText들을 활성화/비활성화 처리한다.
		setTextBoxEnable();
		mScannerHelper.focusEditText(mFacCdText);
		
		//-----------------------------------------------------
		// UUCheck 이벤트 발생하면 정보 수정으로 본다.
		//-----------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
    	
    	//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_LOC, "locCd", barcode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }

    /**
     * 음영지역 장치바코드 작업.
     * @param barcode
     */
    private void getOfflineDeviceBarcodeData(String barcode) {
    	
    	if (barcode.length() != 9) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드의 형식이 잘 못 되었습니다."));
			mScannerHelper.focusEditText(mDeviceIdText);
    		return;
		}
    	
    	mDeviceIdText.setText(barcode);
    	mDeviceIdInfoText.setText(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	
    	String thisFacCd = mFacCdText.getText().toString().trim();
	    String thisUFacCd = mUFacCdText.getText().toString().trim();
	    if (mUFacInputbar.getVisibility() == View.VISIBLE && mUFacCdText.isEnabled()
	    		&& !thisFacCd.isEmpty() && thisUFacCd.isEmpty()) {
	    	// 설비바코드가 값이 존재하고 상위바코드 값이 없을때 상위바코드로 Cursor Focus이동한다.
    		mScannerHelper.focusEditText(mUFacCdText);
	    } else {
	    	// 설비바코드로 Cursor Focus이동한다.
    		mScannerHelper.focusEditText(mFacCdText);
	    }
	    
	    //-------------------------------------------------------------
  		// UUCheck 이벤트 발생하면 정보 수정으로 본다.
	    //-------------------------------------------------------------
  		GlobalData.getInstance().setChangeFlag(true);
	    
	    //-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_DEVICE, "deviceId", barcode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 음영지역 자재바코드 작업.
     * @param barcode
     */
    private void getOfflineProductBarcodeData(String type, String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
    	PDABarcodeService pdaBarcodeService = null;
    	if (type.equals("fac")) {
    		pdaBarcodeService = 
    				new PDABarcodeService(getApplicationContext(), new OfflineFacToBarcodeInfoInfoHandler(barcode));
    	} else {
    		pdaBarcodeService = 
    				new PDABarcodeService(getApplicationContext(), new OfflineUFacToBarcodeInfoInfoHandler(barcode));
    	}
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_LOCAL_PRODUCT, barcode);
		} catch (ErpBarcodeException e) {
			setBarcodeProgressVisibility(false);
			GlobalData.getInstance().showMessageDialog(e);
			return;
		}
    }
    private class OfflineFacToBarcodeInfoInfoHandler extends Handler {
    	private String _barcode;
    	public OfflineFacToBarcodeInfoInfoHandler(String barcode) {
    		_barcode = barcode;
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

            	String goto_barcode = "";
            	if (mJobGubun.equals("납품취소")) {
                	goto_barcode = _barcode;
                }
            	
            	addBarcodeInfosTreeView(goto_barcode, pdaItems);
        		
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
    
    private class OfflineUFacToBarcodeInfoInfoHandler extends Handler {
    	private String _barcode;
    	public OfflineUFacToBarcodeInfoInfoHandler(String barcode) {
    		_barcode = barcode;
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

            	BarcodeListInfo barcodeInfo = pdaItems.get(0);
            	
            	mUFacCdText.setText(barcodeInfo.getBarcode());
                mUPartTypeText.setText(barcodeInfo.getPartType());
                
                // 현재 스캔한 상위설비바코드 정보를 저장한다.
                mThisUFacInfo = barcodeInfo;
                
                //-----------------------------------------------------
                // 설비바코드로 Cursor Focus 이동한다.
                //-----------------------------------------------------
                mScannerHelper.focusEditText(mFacCdText);
        		
        		//-----------------------------------------------------
          		// UUCheck 이벤트 발생하면 정보 수정으로 본다.
          		//-----------------------------------------------------
          		GlobalData.getInstance().setChangeFlag(true);
        	    
        	    //-------------------------------------------------------------
            	// 단계별 작업을 여기서 처리한다.
            	//-------------------------------------------------------------
        		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
        			try {
        				GlobalData.getInstance().getJobActionManager().setStepItem(
        						JobActionStepManager.JOBTYPE_UFAC, "uFacCd", _barcode);
        			} catch (ErpBarcodeException e) {
        				e.printStackTrace();
        			}
        		}
        		
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
			if (requestCode == ACTION_SELECTORGCODEACTIVITY) {
				String costCenter = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_CODE);
				String orgName = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_NAME);
				Log.i(TAG, "onActivityResult  costCenter==>"+costCenter.trim());
				Log.i(TAG, "onActivityResult  orgName==>"+orgName.trim());
				String receiptOrgInfo = costCenter.trim()+"/"+orgName.trim();
				
				setJobWorkReceiptOrgCode(receiptOrgInfo);
				
			} else if (requestCode == ACTION_SEARCHWBSCHECKACTIVITY) {
				String wbsNo = data.getExtras().getString(SearchWbsCheckActivity.OUTPUT_WBS_NO);
				Log.i(TAG, "ACTION_SEARCHWBSCHECKACTIVITY   wbsNo==>"+wbsNo);
				
				setJobWorkWbsNo(wbsNo);

			} 
//			else if (requestCode == ACTION_TAKE_GALLERY) {
//				Log.i(TAG, "ACTION_TAKE_GALLERY   Start...");
//	        	Uri imageUri = data.getData();
//	        	String imagePath = mStorageTools.getMediaStoreImageUri(imageUri);
//	        	Log.i(TAG, "ACTION_TAKE_GALLERY   imagePath==>"+imagePath);
//	        	choicePhotoDisplay(imagePath);
//	        	
//			} else if (requestCode == ACTION_TAKE_PHOTO) {
//				String imagePath = mCboPhotoUri.getPath();
//				mStorageTools.galleryAddPicture(imagePath);
//
//				choicePhotoDisplay(imagePath);
//			}
			else if(requestCode == ACTION_REQUEST_GWLEN_O){
//				TODO. gwlen next process
				execSendDataInTask(mSendCheck, true);
			}
		} else {
			if (requestCode == ACTION_SELECTORGCODEACTIVITY) {
				setJobWorkReceiptOrgCode("");
				
			} else if (requestCode == ACTION_SEARCHWBSCHECKACTIVITY) {
				mWbsNoText.setText("");
				mScannerHelper.focusEditText(mLocCdText);
				
				//-------------------------------------------------------------
		    	// 단계별 작업을 여기서 에러처리한다.
		    	//-------------------------------------------------------------
				if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
					GlobalData.getInstance().getJobActionManager().getJobStepManager().errorHandler();
					return;
				}
			} else if (requestCode == ACTION_TAKE_GALLERY) {
				Log.i(TAG, "ACTION_TAKE_GALLERY  ERROR   Start...");
			} else if (requestCode == ACTION_TAKE_PHOTO) {
				Log.i(TAG, "ACTION_TAKE_GALLERY  ERROR   Start...");
			}
		}
    	if (getLocInputbarVisibility()==View.VISIBLE && mLocCdText.getText().toString().isEmpty()) {
    		mScannerHelper.focusEditText(mLocCdText);
    	} else {
    		mScannerHelper.focusEditText(mFacCdText);
    	}
    }
}