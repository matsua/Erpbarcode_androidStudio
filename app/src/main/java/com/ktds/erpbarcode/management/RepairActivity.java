package com.ktds.erpbarcode.management;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.SAPBarcodeService;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.ktds.erpbarcode.management.model.RemodelBarcodeInfo;
import com.ktds.erpbarcode.management.model.RepairHttpController;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class RepairActivity extends Activity {

	private static final String TAG = "RepairActivity";
	
	private String mJobGubun = "";
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mOrgCodeText;
	//private Button mLocSearchButton;
    private EditText mLocCdText;
    private EditText mLocNameText;
    
    // 위치바코드정보
    private LocBarcodeInfo mThisLocCodeInfo;
    
    //private LinearLayout mFacInputbar;
    private EditText mFacCdText;
    private TextView mPartTypeText;
    
    private LinearLayout mFacStatusInputbar;
    private Spinner mSpinnerFacStatus;
    private String mTouchFacStatusCode = null;
    private BasicSpinnerAdapter mFacStatusAdapter;
    
    private Button mInitButton, mDeleteButton, mSaveButton, mSendButton;
    private TextView mTotalCountText;
    
    private LinearLayout repairLocInputbar, repairLocInfoInputbar, repairSnInputbar, repairOrganizationOrgCodeTextbar;
    private EditText mSnCdText;
    
    //---------------------------------------------------------------
    // Start. 리스트 머리글 항목들 설정.
    //---------------------------------------------------------------
    // 고장등록/수리의뢰
    private TextView mFacStatusText;
    private TextView mFailureCodeText;
    private TextView mFailureNameText;
    private TextView mFailureRegNoText;
    // 수리완료
    private TextView mLocCd_listText;        // 위치바코드
    private TextView mLocName_listText;      // 위치명
    private TextView mUrcodnText;            // 원인유형
    private TextView mMncodnText;            // 수리유형
    private TextView mFailureCode_2Text;     // 고장코드
    private TextView mRepairRequestNo_2Text; // 수리의뢰번호
    private TextView mInjuryBarcodeText;     // 기존바코드
    private TextView mRreasonText;           // 대체발행사유
    private TextView mSnCd_listText;		 // 설비 s/n
    private TextView mFsCd_listText;		 // 장치바코드 
    private TextView mFsStCd_listText;		 // 설비상태
    // 수리의뢰취소
	private TextView mPartnerCodeText;
	private TextView mPartnerNameText;
	private TextView mRepairRequestNoText;
	// 개조개량
	private TextView mDeviceIdText;
	private TextView mUpgdocText;
	
	//sn변경시에만 이벤트 부여 : checkBox - 전체선택/해제 
	private TextView mCheckTxt;
	private CheckBox mCheckBox;
	//---------------------------------------------------------------
    // End. 리스트 머리글 항목들 설정.
    //---------------------------------------------------------------
	
	// 작업바코드 ListView
	private ListView mBarcodeListView;
	private TextView mListHeaderOrgInfoText;
	private RepairBarcodeListAdapter mRepairBarcodeListAdapter;
	private FindGetOutOfServiceListInTask mFindGetOutOfServiceListInTask;
	private FindGetRepairDoneListInTask mFindGetRepairDoneListInTask;
	private FindGetRemodelBarcodeListInTask mFindGetRemodelBarcodeListInTask;
	
	private List<RemodelBarcodeInfo> mRemodelBarcodeInfos = new ArrayList<RemodelBarcodeInfo>(); // 개조개량 자재리스트
    
	private RelativeLayout mBarcodeProgress;
	
	private Button mAddInfo;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	
        mJobGubun = GlobalData.getInstance().getJobGubun();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		setMenuLayout();
		setContentView(R.layout.management_repair_activity);
		
		setLayout();
		setFieldVisibility();
		initScreen();
		
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
 				// 바코드 스캐너가 연결된 상태이면...
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
    	mOrgCodeText = (EditText) findViewById(R.id.repair_organization_orgCode);
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);

    	// 위치코드 
        mLocCdText = (EditText) findViewById(R.id.repair_loc_locCd);
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
        mLocNameText = (EditText) findViewById(R.id.repair_locInfo_locName);
        
//        matsua : 주소조회 변경. 
        mAddInfo = (Button) findViewById(R.id.addInfoBtn);
 		mAddInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
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
        
        // 설비바코드
        //mFacInputbar = (LinearLayout) findViewById(R.id.repair_fac_inputbar);
        mFacCdText = (EditText) findViewById(R.id.repair_fac_facCd);
        mFacCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mFacCdText);
							mSnCdText.setText("");
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
							Log.d(TAG, "설비바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeFacCd(barcode.trim());
						}
					}
				});
        mFacCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeFacCd(barcode.trim());
                    return true;
                }
                return false;
            }
        });
        
        mPartTypeText = (TextView) findViewById(R.id.repair_fac_partType);
        
        //설비 S/N
        repairSnInputbar = (LinearLayout)findViewById(R.id.repair_sn_inputbar);
        mSnCdText = (EditText) findViewById(R.id.repair_sn_facCd);
        mSnCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);
        mSnCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mSnCdText);
			                return true;
			            }
			            return true;
			        }
			    });
        mSnCdText.addTextChangedListener(
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
							changeSnCd(barcode.trim());
						}
					}
				});
        mSnCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	if (barcode.isEmpty()) return true;
                	changeSnCd(barcode.trim());
                    return true;
                }
                return false;
            }
        });
        
        // 설비상태 Spinner 셋팅
        mFacStatusInputbar = (LinearLayout) findViewById(R.id.repair_zp_inputbar);
 		mSpinnerFacStatus = (Spinner) findViewById(R.id.repair_zp_zpStatus);
 		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
 		if (mJobGubun.equals("고장등록취소") || mJobGubun.equals("수리완료") || mJobGubun.equals("개조개량완료")) {
 			spinneritems.add(new SpinnerInfo("0100", "유휴"));
			spinneritems.add(new SpinnerInfo("0110", "예비"));
			if (mJobGubun.equals("고장등록취소")) 
				spinneritems.add(new SpinnerInfo("0070", "미운용"));
			if (mJobGubun.equals("고장등록취소") || mJobGubun.equals("수리완료")) 
				spinneritems.add(new SpinnerInfo("0200", "불용대기"));
 		}
 		mFacStatusAdapter = new BasicSpinnerAdapter(getApplicationContext(), spinneritems);
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
 		
 		
 		mInitButton = (Button) findViewById(R.id.repair_crud_init);
 		mInitButton.setOnClickListener(new View.OnClickListener() {
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
 		mDeleteButton = (Button) findViewById(R.id.repair_crud_delete);
 		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				deleteData();
			}
		});
 		mSaveButton = (Button) findViewById(R.id.repair_crud_save);
 		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				saveWorkData();
			}
		});
 		mSendButton = (Button) findViewById(R.id.repair_crud_send);
 		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SendCheck sendCheck = new SendCheck();
				sendCheck.orgCode = SessionUserData.getInstance().getOrgId();
				if (mThisLocCodeInfo == null) {
		    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
		    	} else {
		    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
		    	}
    	        if (mFacStatusInputbar.getVisibility() == View.VISIBLE) {
    	        	if (mFacStatusAdapter.getCount() > 0) {
    	        		SpinnerInfo facStatusValue = (SpinnerInfo) mSpinnerFacStatus.getSelectedItem();
    	        		sendCheck.facStatusCode = facStatusValue.getCode();
    	        	}
    	        }
    	        
				executeSendCheck(sendCheck);
			}
		});
 		
 		mTotalCountText = (TextView) findViewById(R.id.repair_listfooter_totalCount);
 		
 		mRepairBarcodeListAdapter = new RepairBarcodeListAdapter(this);
		mBarcodeListView = (ListView) findViewById(R.id.repair_listView);
		mBarcodeListView.setAdapter(mRepairBarcodeListAdapter);
		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				mRepairBarcodeListAdapter.changeSelectedPosition(position);
				
				if(mJobGubun.equals("설비S/N변경")){
					mFacCdText.setText(mRepairBarcodeListAdapter.getItem(position).getBarcode());
					mScannerHelper.focusEditText(mSnCdText);
					mSnCdText.setText("");
				}
				
				//choiceBarcodeDataDisplay(position);
			}
		});

		mBarcodeProgress = (RelativeLayout) findViewById(R.id.repair_barcodeProgress);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return false;
		if (item.getItemId()==android.R.id.home) {
    		if (GlobalData.getInstance().isChangeFlag()) {
				changeFlagYesNoDialog();
				return true;
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
		
		if (GlobalData.getInstance().isChangeFlag()) {
			changeFlagYesNoDialog();
			return;
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

	/**
	 * Input Bar Component 화면에 보이고/안보이고 처리.
	 */
	private void setFieldVisibility() {
		// 수리의뢰취소는 설비상태 spinner 없음
		if (mJobGubun.equals("수리의뢰취소")||mJobGubun.equals("개조개량의뢰")||mJobGubun.equals("개조개량의뢰취소")) {
			mFacStatusInputbar.setVisibility(View.GONE);
		}
		// 장치아이디는 개조개량의뢰에만 있음
		if (!mJobGubun.startsWith("개조개량")) {
			mDeviceIdText = (TextView) findViewById(R.id.repair_listheader_deviceid);
			mDeviceIdText.setVisibility(View.GONE);
		}
		// 지시서번호는 개조개량의뢰에만 있음
		if (!mJobGubun.equals("개조개량의뢰")) {
			mUpgdocText = (TextView) findViewById(R.id.repair_listheader_upgdoc);
			mUpgdocText.setVisibility(View.GONE);
		}

		// 고장 관련 필드 숨기기
		if (mJobGubun.equals("수리완료") || mJobGubun.startsWith("개조개량")) {
			mFacStatusText = (TextView) findViewById(R.id.repair_listheader_facStatus);
        	mFacStatusText.setVisibility(View.GONE);

        	mFailureCodeText = (TextView) findViewById(R.id.repair_listheader_failureCode);
        	mFailureCodeText.setVisibility(View.GONE);

        	mFailureNameText = (TextView) findViewById(R.id.repair_listheader_failureName);
        	mFailureNameText.setVisibility(View.GONE);

        	mFailureRegNoText = (TextView) findViewById(R.id.repair_listheader_failureRegNo);
        	mFailureRegNoText.setVisibility(View.GONE);

			mPartnerCodeText = (TextView) findViewById(R.id.repair_listheader_partnerCode);
			mPartnerNameText = (TextView) findViewById(R.id.repair_listheader_partnerName);
			mRepairRequestNoText = (TextView) findViewById(R.id.repair_listheader_repairRequestNo);
			
			mPartnerCodeText.setVisibility(View.GONE);
			mPartnerNameText.setVisibility(View.GONE);
			mRepairRequestNoText.setVisibility(View.GONE);
		}
    	if (!mJobGubun.equals("수리완료")) {
        	// 수리완료
        	mLocCd_listText = (TextView) findViewById(R.id.repair_listheader_locCd);    // 위치바코드
        	mLocName_listText = (TextView) findViewById(R.id.repair_listheader_locName);    // 위치명
        	mUrcodnText = (TextView) findViewById(R.id.repair_listheader_URCODN);    // 원인유형
        	mMncodnText = (TextView) findViewById(R.id.repair_listheader_MNCODN);    // 수리유형
        	mFailureCode_2Text = (TextView) findViewById(R.id.repair_listheader_failureCode_2);    // 고장코드
        	mRepairRequestNo_2Text = (TextView) findViewById(R.id.repair_listheader_repairRequestNo_2);    // 수리의뢰번호
        	mInjuryBarcodeText = (TextView) findViewById(R.id.repair_listheader_injurybarcode);    // 기존바코드
        	mRreasonText = (TextView) findViewById(R.id.repair_listheader_rreason);    // 대체발행사유

        	mLocCd_listText.setVisibility(View.GONE);    // 위치바코드
        	mLocName_listText.setVisibility(View.GONE);    // 위치명
        	mUrcodnText.setVisibility(View.GONE);    // 원인유형
        	mMncodnText.setVisibility(View.GONE);    // 수리유형
        	mFailureCode_2Text.setVisibility(View.GONE);    // 고장코드
        	mRepairRequestNo_2Text.setVisibility(View.GONE);    // 수리의뢰번호
        	mInjuryBarcodeText.setVisibility(View.GONE);    // 기존바코드
        	mRreasonText.setVisibility(View.GONE);    // 대체발행사유
    	}
    	if (mJobGubun.equals("수리완료")) {
    		mListHeaderOrgInfoText = (TextView) findViewById(R.id.repair_listheader_orgInfo);
			
    		mListHeaderOrgInfoText.setVisibility(View.GONE);
    	}
    	
    	if (mJobGubun.equals("설비S/N변경")) {
//    		contents 영역 setVisibility
    		repairOrganizationOrgCodeTextbar = (LinearLayout)findViewById(R.id.repair_organization_orgCode_textbar); //운용조직
    		repairLocInputbar = (LinearLayout)findViewById(R.id.repair_loc_inputbar); //위치바코드 
        	repairLocInfoInputbar = (LinearLayout)findViewById(R.id.repair_locInfo_inputbar);  //위치바코드명
        	
        	repairOrganizationOrgCodeTextbar.setVisibility(View.GONE);
        	repairLocInputbar.setVisibility(View.GONE);
        	repairLocInfoInputbar.setVisibility(View.GONE);
        	mFacStatusInputbar.setVisibility(View.GONE);
//        	list 영역 setVisibility
        	mFailureCode_2Text = (TextView) findViewById(R.id.repair_listheader_failureCode_2);    // 고장코드
        	mFailureCode_2Text.setVisibility(View.GONE);
        	mFailureNameText = (TextView) findViewById(R.id.repair_listheader_failureName);			//고장명 
        	mFailureNameText.setVisibility(View.GONE);
        	mPartnerCodeText = (TextView) findViewById(R.id.repair_listheader_partnerCode);			//협력사코드 
        	mPartnerCodeText.setVisibility(View.GONE);
			mPartnerNameText = (TextView) findViewById(R.id.repair_listheader_partnerName);			//협력사명 
			mPartnerNameText.setVisibility(View.GONE);
			mFailureCodeText = (TextView) findViewById(R.id.repair_listheader_failureCode);			//고장코드 
        	mFailureCodeText.setVisibility(View.GONE);
			mFailureRegNoText = (TextView) findViewById(R.id.repair_listheader_failureRegNo);		//고장등록번호 
        	mFailureRegNoText.setVisibility(View.GONE);
			mRepairRequestNoText = (TextView) findViewById(R.id.repair_listheader_repairRequestNo);	//수리의뢰번호 
			mRepairRequestNoText.setVisibility(View.GONE);
			mFacStatusText = (TextView) findViewById(R.id.repair_listheader_facStatus);
        	mFacStatusText.setVisibility(View.GONE);
        	
        	mCheckTxt = (TextView)findViewById(R.id.repair_checkTxt);
        	mCheckTxt.setVisibility(View.GONE);
        	
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
        	
    	}
    	else{
    		mSnCd_listText = (TextView) findViewById(R.id.repair_listheader_snCd);
    		mFsCd_listText = (TextView) findViewById(R.id.repair_listheader_fsCd);
    		mFsStCd_listText = (TextView) findViewById(R.id.repair_listheader_fsStatus);
    		mCheckBox = (CheckBox)findViewById(R.id.repair_checkBox);
    		mSnCd_listText.setVisibility(View.GONE);
    		mFsCd_listText.setVisibility(View.GONE);
    		mFsStCd_listText.setVisibility(View.GONE);
    		repairSnInputbar.setVisibility(View.GONE);
    		mCheckBox.setVisibility(View.GONE);
    	}
	}
	
	/**
	 * 화면 초기화.
	 */
	private void initScreen() {
		initScreen("all");
		mScannerHelper.focusEditText(mLocCdText);
		
		if(mJobGubun.equals("설비S/N변경")){
			mScannerHelper.focusEditText(mFacCdText);
		}
	}
	/**
	 * 작업별 화면 초기화.
	 */
	private void initScreen(String step) {
		if (step.equals("all") || step.equals("base")) {
			GlobalData.getInstance().setGlobalProgress(false);
			GlobalData.getInstance().setGlobalAlertDialog(false);
	        GlobalData.getInstance().setChangeFlag(false);
			GlobalData.getInstance().setSendAvailFlag(false);
		}
    	if (step.equals("all") || step.equals("loc")) {
    		mLocCdText.setText("");
    		mLocNameText.setText("");
    	}
    	if (step.equals("all") || step.equals("fac")) {
    		mFacCdText.setText("");
    		mPartTypeText.setText("");
    		mSnCdText.setText(""); 					//설비 S/N
    	}
    	if (step.equals("all") || step.equals("facstatus")) {
    		if (mFacStatusInputbar.getVisibility()==View.VISIBLE) {
    			mSpinnerFacStatus.setSelection(0);		// 설비상태
    		}
    	}
    	if (step.equals("all") || step.equals("tree")) {
    		mRepairBarcodeListAdapter.itemClear();
    		showSummaryCount();
    	}
    	if (step.equals("all") || step.equals("crudbar")) {
    		mSendButton.setEnabled(true);
    	}
	}
	
	public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
	public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	mBarcodeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    /**
     * 위치바코드 정보 변경시 처리.
     */
    private void changeLocCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String locCd = mLocCdText.getText().toString();
    	if (locCd.isEmpty()) {
    		mLocCdText.setText(barcode);
    	}
    	
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineLocBarcodeData(barcode);
    	} else {
    		getLocBarcodeData(barcode);
    	}
    }
    /**
     * S/N 변경시 처리.
     */
    private void changeSnCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	    	
    	String facCd = mFacCdText.getText().toString();
    	BarcodeListInfo barcodeInfo = mRepairBarcodeListAdapter.getBarcodeListInfo(facCd);
    	barcodeInfo.setFacSergeNum(barcode);
    	
    	mFacCdText.setText("");
    	mSnCdText.setText("");
    	mScannerHelper.focusEditText(mFacCdText);
    	
    	mRepairBarcodeListAdapter.notifyDataSetChanged();
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
	    		GlobalData.getInstance().getJobActionManager().addItemParameter("changeIdx", facCd);
				GlobalData.getInstance().getJobActionManager().addItemParameter("changeValue", barcode);
				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_SN_CHANGE);
			} 
	    	catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 설비바코드 정보 변경시 처리.
     */
    private void changeFacCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
		if (isBarcodeProgressVisibility()) return;
		
		mFacCdText.setText(barcode);
        
		if(!mJobGubun.equals("설비S/N변경")){
			if (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty()) {
				initScreen("fac");
				
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요.", BarcodeSoundPlay.SOUND_ERROR));
				mScannerHelper.focusEditText(mLocCdText);
				return;
			}
		}
		
		if (barcode.length()<16 || barcode.length()>18) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
        	return;
		}
		
		//-----------------------------------------------------------
		// 기존 설비바코드 있는지 체크하여
    	// 없으면 추가하고, 있으면 상태값 변경한다.
		//-----------------------------------------------------------
    	int thisSelectedPosition = mRepairBarcodeListAdapter.getBarcodePosition(barcode);
		if (thisSelectedPosition >= 0) {
			// 스캔한 바코드로  Position Selected 한다.
			mRepairBarcodeListAdapter.changeSelectedPosition(thisSelectedPosition);
			mBarcodeListView.setSelection(thisSelectedPosition);  // 선택된 바코드로 커서 이동한다.
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
        	return;
		}
		
		// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineProductBarcodeData(barcode);
    		
    	} else {
    		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
    		if (mJobGubun.equals("고장등록취소") || mJobGubun.equals("수리의뢰취소")) {
    			getOutOfServiceList(barcode);
    		} else if (mJobGubun.startsWith("수리완료")) {
    			getRepairDoneList(barcode);
    		} else if (mJobGubun.startsWith("개조개량")) {
    			if (mJobGubun.equals("개조개량완료")) {
    				boolean existsFlag = false;
                    String EQUIPMENT = "";
                    String UPGEQUNR = "";
                    for (RemodelBarcodeInfo remodelBarcodeInfo : mRemodelBarcodeInfos) {
                		EQUIPMENT = remodelBarcodeInfo.getProductCode();      
                        UPGEQUNR = remodelBarcodeInfo.getUpgdoc();         	  
                        if (UPGEQUNR.isEmpty() && barcode.equals(EQUIPMENT) || barcode.equals(UPGEQUNR)) {
                        	existsFlag = true;
                            break;
                        }
                    }

                    if (!existsFlag) {
                    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'개조개량완료' 대상 설비바코드가 아닙니다.", BarcodeSoundPlay.SOUND_ERROR));
                        return;
                    }
                    
                    // 개조개량완료 - 개조후바코드 있을때는 납품입고처럼 자재정보만으로 조회
                    if (!UPGEQUNR.isEmpty()) {
                    	// 자재정보 조회
                    	getServerProductInfos(barcode);
    					return;
                    }
    			}
    			// 위의 개조개량완료 조건에 만족하지 않으면 SAP 바코드 조회
    			getSAPBarcodeInfos("", "", barcode);
    		} else if(mJobGubun.equals("설비S/N변경")){
    			getSAPBarcodeInfos("", "", barcode);
    		}
    	}
    }
    
    public void setJobWorkFacStatus(String facStatusCode) {
    	if (mTouchFacStatusCode == null) 
    		return;
    	if (TextUtils.isEmpty(mTouchFacStatusCode) && TextUtils.isEmpty(facStatusCode))
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
    
    /**
     * List항목 전체 선택 해제 _ sn변경시에만 부여됨. 
     */
    
    private void selectAll(){
    	if (mRepairBarcodeListAdapter.getAllItems().size() == 0) {
    		return;
    	}
    	
    	System.out.println("selectAll check >> " + mCheckBox.isChecked());
    	
    	for(int i=0; i<mRepairBarcodeListAdapter.getAllItems().size();i++){
    		mRepairBarcodeListAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    	}
    	
    	mCheckBox.setChecked(!mCheckBox.isChecked());
    	mRepairBarcodeListAdapter.notifyDataSetChanged();
    }

    /**
     *  List에서 체크된 항목들 모두 삭제.
     */
    private void deleteData() {
    	if (mRepairBarcodeListAdapter.getCheckedItems().size() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
    		return;
    	}
    	
    	//-----------------------------------------------------------
    	// Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    	String message = "삭제하시겠습니까 ?";
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
				
				
				List<String> barcodeList = new ArrayList<String>();
				for (BarcodeListInfo barcodeInfo : mRepairBarcodeListAdapter.getCheckedItems()) {
					barcodeList.add(barcodeInfo.getBarcode());
				}

				deleteData(barcodeList);
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
    
    private void deleteData(List<String> barcodeList) {
    	
    	mRepairBarcodeListAdapter.removeCheckedItems();
    	showSummaryCount();
    	
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 초기화 하고..
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
		
		//-------------------------------------------------------------
    	// 단계별 작업을 삭제한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			Gson gson = new Gson();
			String jsonArrayString = gson.toJson(barcodeList);
			
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_DELETE, "jsonBarcodeList", jsonArrayString);
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
    	// 작업인포 정보에 필요한 내역을 Set한다.
    	//-----------------------------------------------------------
    	String locCd = mLocCdText.getText().toString().trim();
    	String locName = mLocNameText.getText().toString().trim();
    	String wbsNo = "";
    	String deviceId = "";
    	int recordCount = mRepairBarcodeListAdapter.getCount();
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
    private void sendWorkResult(String sendMessage) {
    	
    	//if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_GENERAL) 
    	//	return;
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
    		String locCd = mLocCdText.getText().toString().trim();
        	String locName = mLocNameText.getText().toString().trim();
        	String wbsNo = "";
        	String deviceId = mDeviceIdText.getText().toString().trim();
        	int recordCount = mRepairBarcodeListAdapter.getCount();
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
    	
    	if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_ORG)) {
    		String orgInfo = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "orgCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_ORG==>"+orgInfo);
        	mOrgCodeText.setText(orgInfo);
        	
        	GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_LOC)) {
    		String locCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "locCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_LOC==>"+locCd);
    		changeLocCd(locCd);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_SEARCH_TASK)) {
     		String taskName = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "taskName");
     		String locCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "locCd");
     		Log.i(TAG, "startStepWorkItem JOBTYPE_SEARCH_TASK==>"+locCd);
     		if (taskName.equals("getRemodelBarcodeList")) {
     			getRemodelBarcodeList(locCd);
     		}
     		
     	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC)) {
    		String facCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC==>"+facCd);
    		changeFacCd(facCd);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC_STATUS)) {
    		String facStatusCode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facStatusCode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC_STATUS==>"+facStatusCode);
    		mTouchFacStatusCode = "****"; // 단지 수정정보로 처리하기 위해서.
    		setJobWorkFacStatus(facStatusCode);

    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_DELETE)) {
    		String jsonBarcodeList = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "jsonBarcodeList");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_DELETE==>"+jsonBarcodeList);
    		
    		
    		Gson gson = new Gson();
    		Type listType = new TypeToken<List<String>>(){}.getType();
    		List<String> barcodeList = gson.fromJson(jsonBarcodeList, listType);
    		
    		for (String barcode : barcodeList) {
    			BarcodeListInfo barcodeInfo = mRepairBarcodeListAdapter.getBarcodeListInfo(barcode);
    			if (barcodeInfo != null) {
    				barcodeInfo.setChecked(true);
    			}
    		}
    		
    		deleteData(barcodeList);
    	}
    	
    	else if(workItem.getJobType().equals(JobActionStepManager.JOBTYPE_SN_CHANGE)){
    		String changeValue = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "changeValue");
    		String changeIdx = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "changeIdx");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_SN_CHANGE==!!>"+ changeIdx + changeValue);
    		
    		BarcodeListInfo barcodeInfo = mRepairBarcodeListAdapter.getBarcodeListInfo(changeIdx);
    		barcodeInfo.setFacSergeNum(changeValue);
    		mRepairBarcodeListAdapter.notifyDataSetChanged();
    		
    		GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
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
	 * 위치바코드 정보 조회.
	 */
	private void getLocBarcodeData(String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		mLocNameText.setText("");
		mThisLocCodeInfo = null;
		LocBarcodeService locbarcodeService = new LocBarcodeService(mLocBarcodeHandler);
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
    
    // 위치바코드 조회 성공 시
    private void successLocBarcodeProcess() {
		Log.d(TAG, "위치바코드 getLocCd==>"+mThisLocCodeInfo.getLocCd());
		Log.d(TAG, "위치바코드 getLocName==>"+mThisLocCodeInfo.getLocName());
		
		mLocCdText.setText(mThisLocCodeInfo.getLocCd());
		mLocNameText.setText(mThisLocCodeInfo.getLocName());
		
		//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			return;
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_LOC, "locCd", mThisLocCodeInfo.getLocCd());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		// 개조개량의뢰/개조개량완료는 개조개량 대상 리스트 가져온다. - SearchRemodelBarcodeList()
		if (mJobGubun.equals("개조개량의뢰") || mJobGubun.equals("개조개량완료")) {
			getRemodelBarcodeList(mThisLocCodeInfo.getLocCd());
		}
		
		mScannerHelper.focusEditText(mFacCdText);		
    }

	/**
	 * 고장취소 대상 조회.
	 */
	private void getOutOfServiceList(String barcode) {
		if (isBarcodeProgressVisibility()) return;

		if (mFindGetOutOfServiceListInTask == null) {
			setBarcodeProgressVisibility(true);			
			mFindGetOutOfServiceListInTask = new FindGetOutOfServiceListInTask(barcode);
			mFindGetOutOfServiceListInTask.execute((Void) null);
		}
	}
	public class FindGetOutOfServiceListInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		private String _Barcode;
		private List<BarcodeListInfo> _BarcodeListInfos = null;
		
		public FindGetOutOfServiceListInTask(String barcode) {
			_Barcode = barcode;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				RepairHttpController repairhttp = new RepairHttpController();
				_BarcodeListInfos = repairhttp.getOutOfServiceListToBarcodeListInfos(_Barcode);
				if (_BarcodeListInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 고장등록취소 정보입니다.");
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
			mFindGetOutOfServiceListInTask = null;
			
			if (result) {
				AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(_BarcodeListInfos.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = _BarcodeListInfos;

                agoBarcodeInfosListView(agoCheck);
				
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mFindGetOutOfServiceListInTask = null;
		}
	}
	
	/**
	 * 수리완료 대상 조회.
	 */
	private void getRepairDoneList(String barcode) {
		if (isBarcodeProgressVisibility()) return;

		if (mFindGetRepairDoneListInTask == null) {
			setBarcodeProgressVisibility(true);			
			mFindGetRepairDoneListInTask = new FindGetRepairDoneListInTask(barcode);
			mFindGetRepairDoneListInTask.execute((Void) null);
		}
	}
	public class FindGetRepairDoneListInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		private String _Barcode;
		private List<BarcodeListInfo> _BarcodeListInfos = null;
		
		public FindGetRepairDoneListInTask(String barcode) {
			_Barcode = barcode;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String newBarcode = _Barcode;
				String injuryBarcode = "";
				String RREASON = "";
				
				// 먼저 인스토어마킹 조회.....................
				IsmHttpController ismhttp = new IsmHttpController();
				JSONArray ismResults = ismhttp.getRepairReceipt_IM(mThisLocCodeInfo.getLocCd(), _Barcode);
				IsmBarcodeInfo ismBarcodeInfo = new IsmBarcodeInfo();
				
				if (ismResults.length() > 0) {
					injuryBarcode = ismResults.getJSONObject(0).getString("injuryBarcode");        	// 훼손바코드
					newBarcode = ismResults.getJSONObject(0).getString("newBarcode");           	// 신규바코드
					RREASON = ismResults.getJSONObject(0).getString("publicationWhyCode");   		// 교체사유
					
					ismBarcodeInfo.setNewBarcode(ismResults.getJSONObject(0).getString("newBarcode"));
					ismBarcodeInfo.setLocCd(ismResults.getJSONObject(0).getString("locationCode"));
					ismBarcodeInfo.setDeviceId(ismResults.getJSONObject(0).getString("deviceId"));
					ismBarcodeInfo.setItemCode(ismResults.getJSONObject(0).getString("itemCode"));
					ismBarcodeInfo.setItemName(ismResults.getJSONObject(0).getString("itemName"));
					ismBarcodeInfo.setItemCategoryCode(ismResults.getJSONObject(0).getString("itemCategoryCode"));	// zpgubun - 품목구분
					ismBarcodeInfo.setItemCategoryName(ismResults.getJSONObject(0).getString("itemCategoryName"));
					ismBarcodeInfo.setPartKindCode(ismResults.getJSONObject(0).getString("partKindCode"));
					ismBarcodeInfo.setPartKindName(ismResults.getJSONObject(0).getString("partKindName"));
					ismBarcodeInfo.setItemLargeClassificationCode(ismResults.getJSONObject(0).getString("itemLargeClassificationCode"));
					ismBarcodeInfo.setItemMiddleClassificationCode(ismResults.getJSONObject(0).getString("itemMiddleClassificationCode"));
					ismBarcodeInfo.setItemSmallClassificationCode(ismResults.getJSONObject(0).getString("itemSmallClassificationCode"));
					ismBarcodeInfo.setItemDetailClassificationCode(ismResults.getJSONObject(0).getString("itemDetailClassificationCode"));
					ismBarcodeInfo.setInjuryBarcode(ismResults.getJSONObject(0).getString("injuryBarcode"));
					ismBarcodeInfo.setPublicationWhyCode(ismResults.getJSONObject(0).getString("publicationWhyCode"));	// 대체발행사유코드
					ismBarcodeInfo.setPublicationWhyName(ismResults.getJSONObject(0).getString("publicationWhyName"));	// 대체발행사유명
					ismBarcodeInfo.setSupplierCode(ismResults.getJSONObject(0).getString("supplierCode"));
					ismBarcodeInfo.setDeptCode(ismResults.getJSONObject(0).getString("deptCode"));
					ismBarcodeInfo.setOperationDeptCode(ismResults.getJSONObject(0).getString("operationDeptCode"));
					ismBarcodeInfo.setMakerCode(ismResults.getJSONObject(0).getString("makerCode"));
					ismBarcodeInfo.setMakerSerial(ismResults.getJSONObject(0).getString("makerSerial"));
					//ismBarcodeInfo.setMakerNational(ismResults.getJSONObject(0).getString("makerNational"));	// SAP에서 넘어오지 않음
					ismBarcodeInfo.setMakerNational("");
					ismBarcodeInfo.setObtainDay(ismResults.getJSONObject(0).getString("obtainDay"));
					ismBarcodeInfo.setGenerationRequestSeq(ismResults.getJSONObject(0).getString("generationRequestSeq"));
				}
				// -- 먼저 인스토어마킹 조회.....................
				RepairHttpController repairhttp = new RepairHttpController();
				_BarcodeListInfos = repairhttp.getRepairReceiptListToBarcodeListInfos(mThisLocCodeInfo.getLocCd(), injuryBarcode, newBarcode, RREASON);
				if (_BarcodeListInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 수리완료 정보입니다.");
				}
				_BarcodeListInfos.get(0).setIsmBarcodeInfo(ismBarcodeInfo);		// 인스토어마킹 정보 삽입
				
			} catch (JSONException e1) {
				_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
				return false;
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
			mFindGetRepairDoneListInTask = null;
			
			if (result) {
				AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(_BarcodeListInfos.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = _BarcodeListInfos;

                agoBarcodeInfosListView(agoCheck);
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mFindGetRepairDoneListInTask = null;
		}
	}
	
	/**
	 * 개조개량의뢰/개조개량완료 대상 조회.
	 */
	private void getRemodelBarcodeList(String locCd) {
		if (isBarcodeProgressVisibility()) return;
		
		if (mFindGetRemodelBarcodeListInTask == null) {
			setBarcodeProgressVisibility(true);
			mRemodelBarcodeInfos.clear();			// 초기화

			mFindGetRemodelBarcodeListInTask = new FindGetRemodelBarcodeListInTask(locCd);
			mFindGetRemodelBarcodeListInTask.execute((Void) null);
		}
	}
	public class FindGetRemodelBarcodeListInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _locCd;
				
		public FindGetRemodelBarcodeListInTask(String locCd) {
			_locCd = locCd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				RepairHttpController repairhttp = new RepairHttpController();
				mRemodelBarcodeInfos = repairhttp.getRemodelBarcodeList(_locCd);
				if (mRemodelBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 개조개량의뢰/개조개량완료 정보입니다.");
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
			mFindGetOutOfServiceListInTask = null;
			
			if (result) {
				// 개조개량 자재 리스트 가져오기 성공이면 아무 것도 하지 않는다.
				
				mScannerHelper.focusEditText(mFacCdText);
				
				//-------------------------------------------------------------
		    	// 단계별 작업을 여기서 처리한다.
		    	//-------------------------------------------------------------
				if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
					GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
				} else {
					try {
						GlobalData.getInstance().getJobActionManager().addItemParameter("taskName", "getRemodelBarcodeList");
						GlobalData.getInstance().getJobActionManager().addItemParameter("locCd", _locCd);
						GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_SEARCH_TASK);
					} catch (ErpBarcodeException e) {
						e.printStackTrace();
					}
				}
				
			} else {
				mScannerHelper.focusEditText(mFacCdText);
				
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mFindGetOutOfServiceListInTask = null;
		}
	}
	
    /**
     * SAP에서 설비바코드 정보 조회.
     * 
     * @param locCode
     * @param deviceId
     * @param barcode
     */
	public void getSAPBarcodeInfos(String locCode, String deviceId, String barcode) {
		if (isBarcodeProgressVisibility()) return;
		
		setBarcodeProgressVisibility(true);
    	SAPBarcodeService sapBarcodeInfoService = new SAPBarcodeService(new SAPBarcodeInfoHandler(barcode));
    	sapBarcodeInfoService.search(locCode, deviceId, barcode);
	}
	
	/**
	 * SAPBarcodeInfo 정보 조회 결과 Handler
	 */
	private class SAPBarcodeInfoHandler extends Handler {
		String _Barcode = "";

		public SAPBarcodeInfoHandler(String barcode) {
			_Barcode = barcode;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	Log.d(TAG, "SAPBarcodeInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> barcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

                
				AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(barcodeListInfos.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = barcodeListInfos;
                agoBarcodeInfosListView(agoCheck);
                                
                break;
            case SAPBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(
            			new ErpBarcodeException(-1, "존재하지 않는 설비바코드입니다. ", BarcodeSoundPlay.SOUND_NOTEXISTS));
            	break;
            case SAPBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void getServerProductInfos(String barcode) {
		if (isBarcodeProgressVisibility()) return;
		
		setBarcodeProgressVisibility(true);
		PDABarcodeService pdaBarcodeService = new PDABarcodeService(getApplicationContext(), new ProductToBarcodeInfoInfoHandler(barcode));
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO, barcode);
		} catch (ErpBarcodeException e) {
			setBarcodeProgressVisibility(false);
			GlobalData.getInstance().showMessageDialog(e);
			return;
		}
	}
    
    /**
	 * ProductToBarcodeInfoInfo 정보 조회 결과 Handler
	 */
	private class ProductToBarcodeInfoInfoHandler extends Handler {
		String _Barcode = "";

		public ProductToBarcodeInfoInfoHandler(String barcode) {
			_Barcode = barcode;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> barcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);


            	AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(barcodeListInfos.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = barcodeListInfos;

                agoBarcodeInfosListView(agoCheck);

                break;
            case PDABarcodeService.STATE_NOT_FOUND:
            	String notfoundMessage = msg.getData().getString("message");
            	ErpBarcodeException notfounException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(notfoundMessage);
            	GlobalData.getInstance().showMessageDialog(notfounException);
            	break;
            case PDABarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };

    // 운용조직 다른지 체크하여 변경여부 선택한다.
    private void checkOrgYesNoDialog(final AgoCheck agoCheck) {
    	
    	//-----------------------------------------------------------
    	// Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		final Builder builder = new AlertDialog.Builder(this); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getApplicationContext());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(agoCheck.check_org_message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					barcodeInfo.setCheckOrgYn("Y");
					//agoCheck.barcodeItems.set(i, barcodeInfo);
				}
            	agoCheck.check_org_yn = "Y";
            	agoBarcodeInfosListView(agoCheck);
            }
        });
        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					barcodeInfo.setCheckOrgYn("N");
					//agoCheck.barcodeItems.set(i, barcodeInfo);
				}
            	agoCheck.check_org_yn = "N";
            	agoBarcodeInfosListView(agoCheck);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }
    
    public class AgoCheck {
    	public String change_barcode_yn = "N";
    	public List<BarcodeListInfo> barcodeItems = null;
    	public LocBarcodeInfo check_locCodeInfo = mThisLocCodeInfo;		// 스캔한 위치바코드 
    	public String check_org_yn = null;
    	public String check_org_message = "";
    }
    
    private void agoBarcodeInfosListView(final AgoCheck agoCheck) {
    	
    	BarcodeListInfo firstBarcodeInfo = agoCheck.barcodeItems.get(0);
    	
    	int thisSelectedPosition = mRepairBarcodeListAdapter.getBarcodePosition(firstBarcodeInfo.getBarcode());

    	//---------------------------------------------------------------
    	// 스캔한 바코드와 조회된 바코드가 다를때 중복체크 한번더 한다.
    	//---------------------------------------------------------------
    	if (agoCheck.change_barcode_yn.equals("Y")) {
    		//-----------------------------------------------------------
        	// 스캔한 바코드와 서버에서 조회된 바코드가 상이한 경우 때문에 
        	// 여기서 중복체크 한번더 한다.
        	//-----------------------------------------------------------
    		if (thisSelectedPosition >= 0) {
    			mRepairBarcodeListAdapter.changeSelectedPosition(thisSelectedPosition);
    			mBarcodeListView.setSelection(thisSelectedPosition);  // 선택된 바코드로 커서 이동한다.
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + firstBarcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
    			return;
    		}
    	}
    	

		if (mJobGubun.equals("개조개량의뢰") || mJobGubun.equals("개조개량의뢰취소") || mJobGubun.equals("개조개량완료")) {
    		String submt = firstBarcodeInfo.getProductCode();
    		String UPGDOC = "";
            if (true)		// !Public.OFFLINE_FLAG
            {
            	if (mJobGubun.equals("개조개량의뢰")||mJobGubun.equals("개조개량의뢰취소")) {
                    String ZKEQUI = firstBarcodeInfo.getZkequi();        // 설비처리구분
                    String ZANLN1 = firstBarcodeInfo.getZanln1();  		 // 자산상태
                    if (!ZKEQUI.equals("B") && ZANLN1.isEmpty() )
                    {
                    	GlobalData.getInstance().showMessageDialog(
                    			new ErpBarcodeException(-1, "자산화가 안 된 설비바코드는\n\r'" + mJobGubun + "' 작업을\n\r하실 수 없습니다.", BarcodeSoundPlay.SOUND_ERROR));
                        return;
                    }
            	}
            	
            	// 개조개량 대상인지 체크
                if (mJobGubun.equals("개조개량의뢰")) {
                    boolean existsFlag = false;
                    String _submt = "";
                    for (RemodelBarcodeInfo remodelBarcodeInfo : mRemodelBarcodeInfos) {
                        _submt = remodelBarcodeInfo.getProductCode();         // 자재번호
                        UPGDOC = remodelBarcodeInfo.getUpgdoc();         	  // 지시서번호
                        if (submt.equals(_submt)) {
                            existsFlag = true;
                            break;
                        }
                    }

                    if (!existsFlag) {
                    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'개조개량의뢰' 대상 자재코드가 아닙니다.", BarcodeSoundPlay.SOUND_ERROR));
                        return;
                    }
                    // 지시서번호 셋팅
                    firstBarcodeInfo.setUpgdoc(UPGDOC);
                }
            	// -- 개조개량 대상인지 체크
            }
        }

		//-------------------------------------------------------------
		// 조직변경 여부 Yes/No Dialog
		//-------------------------------------------------------------
		if (agoCheck.check_org_yn == null) {
			boolean DontCheckOperationOrganizationFlag = false;
			if (!mJobGubun.equals("배송출고") && !mJobGubun.equals("설비S/N변경")) {
				String locCd = agoCheck.check_locCodeInfo.getLocCd();
				if (locCd.equals("VS0000000000004110000") || locCd.equals("VS0000000000004120000") || locCd.equals("VS0000000000004130000") || locCd.equals("VS0000000000004140000") || locCd.equals("VS0000000000004160000") || locCd.equals("VS0000000000004180000")) {
					DontCheckOperationOrganizationFlag = true;
				}
			}
			
			if (mJobGubun.equals("송부(팀간)") || mJobGubun.equals("설비S/N변경")) {
				DontCheckOperationOrganizationFlag = true;
			}
			
            /**
             * 2. 출고/탈장
             * → 조직 변경 하지 않기로 하였습니다.
             *  .조직정보 변경 관련 PDA Message 삭제
             *  .ITEM 정보의  CHKZKOSTL = 'X' 로 PDA I/F 처리
             * 2012.07.26 request by kang
             * 출고(팀내) 다시 조직변경 여부 묻는걸로 변경 - 2013.12.04 request by 정진우
             * 탈장 다시 조직변경 여부 묻는걸로 변경 - 2013.12.06 request by 정진우
             */
			if (DontCheckOperationOrganizationFlag) {
				// 무조건 묻지 않고 조직변경 하지 않도록..............................................
				for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					barcodeInfo.setCheckOrgYn("N");
					agoCheck.barcodeItems.set(i, barcodeInfo);
				}
			} else {
				// 운용조직 다르면 변경여부 선택한다.
				if (!firstBarcodeInfo.getOrgId().isEmpty() &&
						!firstBarcodeInfo.getOrgId().equals(SessionUserData.getInstance().getOrgId())) {
					agoCheck.check_org_message = "'" + firstBarcodeInfo.getBarcode() + "' 의 운용조직은\n\r'" 
									+ firstBarcodeInfo.getOrgName() + "' 입니다.\n\r운용조직을 '" 
									+ SessionUserData.getInstance().getOrgName() + "'(으)로\n\r변경하시겠습니까?";
	        		checkOrgYesNoDialog(agoCheck);
	        		return;
	        	}
			}
		}
		
		
		// SAPBarcode ListView에 추가한다.
		addBarcodeInfosListView(agoCheck);
    }
    
    /**
     * 음영지역 작업일때 사용함.
     */
    private void addBarcodeInfosListView(final List<BarcodeListInfo> barcodeInfos) {
    	AgoCheck agoCheck = new AgoCheck();
    	agoCheck.barcodeItems = barcodeInfos;
    	
    	addBarcodeInfosListView(agoCheck);
    }
    
    /**
     * ListView에 추가한다.
     */
    private void addBarcodeInfosListView(final AgoCheck agoCheck) {
    	
    	final List<BarcodeListInfo> newBarcodeInfos = agoCheck.barcodeItems;
    	
		BarcodeListInfo checkBarcodeInfo = newBarcodeInfos.get(0);
		mPartTypeText.setText(checkBarcodeInfo.getPartType());			// 부품종류
		
    	for (int i=0; i<newBarcodeInfos.size(); i++) {
    		BarcodeListInfo barcodeInfo = newBarcodeInfos.get(i);
    		barcodeInfo.setChecked(true);

    		newBarcodeInfos.set(i, barcodeInfo);
    	}
		mRepairBarcodeListAdapter.addItemsNotClear(newBarcodeInfos);
		
		showSummaryCount();
		
		//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 초기화 하고..
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
						JobActionStepManager.JOBTYPE_FAC, "facCd", newBarcodeInfos.get(0).getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
	/**
	 * 작업바코드 ListView 건수 Bar
	 */
    public void showSummaryCount() {
    	int DB_RCount = mRepairBarcodeListAdapter.getDBPartTypeCount("R");
    	int DB_SCount = mRepairBarcodeListAdapter.getDBPartTypeCount("S");
    	int DB_UCount = mRepairBarcodeListAdapter.getDBPartTypeCount("U");
    	int DB_ECount = mRepairBarcodeListAdapter.getDBPartTypeCount("E");
    	int DB_TCount = DB_RCount + DB_SCount + DB_UCount + DB_ECount;
    	String DBCount = "R(" + DB_RCount + ") S(" + DB_SCount + ") U(" + DB_UCount + ") E(" + DB_ECount + ") Total:" + DB_TCount + "건";
    	
    	mTotalCountText.setText(DBCount);
    	if(mJobGubun.equals("설비S/N변경"))
    		mScannerHelper.focusEditText(mSnCdText);
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
		// 설비바코드로 Cursor Focus 이동한다.
    	//-----------------------------------------------------------
		mScannerHelper.focusEditText(mFacCdText);
		
		//-----------------------------------------------------------
		// UUCheck 이벤트 발생하면 정보 수정으로 본다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    	
    	//-----------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_LOC, "locCd", barcode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		// 개조개량의뢰/개조개량완료는 개조개량 대상 리스트 가져온다. - SearchRemodelBarcodeList()
		if (mJobGubun.equals("개조개량의뢰") || mJobGubun.equals("개조개량완료")) {
			//-------------------------------------------------------------
	    	// 단계별 작업을 여기서 처리한다.
	    	//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
				try {
					GlobalData.getInstance().getJobActionManager().addItemParameter("taskName", "getRemodelBarcodeList");
					GlobalData.getInstance().getJobActionManager().addItemParameter("locCd", mThisLocCodeInfo.getLocCd());
					GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_SEARCH_TASK);
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    /**
     * 음영지역 자재바코드 작업.
     * @param barcode
     */
    private void getOfflineProductBarcodeData(String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
    	PDABarcodeService pdaBarcodeService = 
    			new PDABarcodeService(getApplicationContext(), new OfflineFacToBarcodeInfoInfoHandler());

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
    	//private String _barcode;
    	//public OfflineFacToBarcodeInfoInfoHandler(String barcode) {
    	//	_barcode = barcode;
    	//}
    	
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);
            	
            	addBarcodeInfosListView(pdaItems);
        		
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
    
    
	public class SendCheck {
		public String orgCode = "";
		public LocBarcodeInfo locBarcodeInfo = null;
		public String facStatusCode = "";
	}
	
	/**
	 * 바코드 작업내용 전송하기..
	 */
	private void executeSendCheck(final SendCheck sendCheck) {
		Log.d(TAG, "executeSendCheck  Start...");
		
		if (!GlobalData.getInstance().isSendAvailFlag()
    			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}
		
		if(!mJobGubun.equals("설비S/N변경")){
			if (sendCheck.locBarcodeInfo==null || sendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
				mScannerHelper.focusEditText(mLocCdText);
				return;
			}
		}
		
		if (mRepairBarcodeListAdapter.getCount() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}
		
		// 선택된 항목이 있는지 체크.
		final List<BarcodeListInfo> sendBarcodeListInfos = mRepairBarcodeListAdapter.getAllItems();
		int checkSendCount = 0;
		for (BarcodeListInfo sendBarcodeInfo : sendBarcodeListInfos) {
			if (sendBarcodeInfo.isChecked()) {
				checkSendCount++;
			}
		}
		if (checkSendCount == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}
		
		if(mJobGubun.equals("설비S/N변경")){
			for(int i=0; i < mRepairBarcodeListAdapter.getCount(); i++){
				if(mRepairBarcodeListAdapter.getItem(i).getFacSergeNum().length() < 1){
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "입력하지 않은 S/N가 존재합니다.", BarcodeSoundPlay.SOUND_ERROR));
		        	return;
				}
			}
		}


		// ---------------------------------------------------------------------
		// 전송여부 최종 확인..
		// ---------------------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
		String message = "전송하시겠습니까?";
		if (mJobGubun.equals("개조개량의뢰")) {
			message="전송하시겠습니까?\n\r(개조개량 의뢰된 설비 하위에 설비가 존재하는 경우 분실위험 처리 됩니다. 존재하는 하위 설비에 대해 '실장' 혹은 '입고' 처리를 하여 분실위험을 해소 하시기 바랍니다.)";
		} else if (mJobGubun.equals("개조개량의뢰취소")) {
			message="전송하시겠습니까?\n\r('개조개량의뢰취소'된 설비를 '실장' 혹은 '입고' 처리하여 실물 정보와 일치시켜 주시기 바랍니다.)";
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
				
				if (mJobGubun.equals("수리완료")) {
					execSendInstoreMarkingDataInTask(sendCheck);		// 수리완료는 인스토어마킹 부터 먼저 태운다....
				} else {
					execSendDataInTask(sendCheck);
				}
			}
		});
		builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);
				
				if (mJobGubun.equals("개조개량의뢰") || mJobGubun.equals("개조개량의뢰취소")) {
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "취소하였습니다.", BarcodeSoundPlay.SOUND_NOTIFY));
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}
	
	/**
	 * 데이터 서버로 전송
	 * 
	 * @param sendCheck
	 */
	private void execSendDataInTask(SendCheck sendCheck) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		mSendButton.setEnabled(false);
		
		new SendDataInTask(sendCheck, this).execute();
	}
	// 전송 비동기 테스크
	private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private final SendCheck _SendCheck;
		private int _SendCount = 0;
		private OutputParameter _OutputParameter;
		private Activity mActivity;
		
		public SendDataInTask(SendCheck sendCheck, Activity activity) {
			_SendCheck = sendCheck;
			mActivity = activity;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "SendDataInTask  Start...");

			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
    		try {
	        	if (mJobGubun.equals("고장등록취소")) {
    				jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
    				//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
    				// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
    				
	        		jsonParam.put("WORKID", "0010");
                	jsonParam.put("PRCID", "0430");
	        	} else if (mJobGubun.equals("수리의뢰취소")) {
    				jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
    				//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
    				// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
    				
	        		jsonParam.put("WORKID", "0010");
                	jsonParam.put("PRCID", "0430");
	        	} else if (mJobGubun.equals("수리완료")) {
                	jsonParam.put("PRCID", "0470");
    	        	jsonParam.put("DATE_ENTERED", SystemInfo.getNowDate());
    	        	jsonParam.put("TIME_ENTERED", SystemInfo.getNowTime());
	        	} else if (mJobGubun.equals("개조개량의뢰")) {
	        		jsonParam.put("WORKID", "0011");
                	jsonParam.put("PRCID", "0590");
	        	} else if (mJobGubun.equals("개조개량의뢰취소")) {
	        		jsonParam.put("WORKID", "0011");
                	jsonParam.put("PRCID", "0595");
	        	} else if (mJobGubun.equals("개조개량완료")) {
    				jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());
    				//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
    				// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
    				
	        		// workid는 없다....
                	jsonParam.put("PRCID", "0765");
	        	}else if(mJobGubun.equals("설비S/N변경")){
	        		jsonParam.put("WORKID", "0017");
                	jsonParam.put("PRCID", "0240");
	        	}
    		} catch (JSONException e) {
    			_ErpBarException = new ErpBarcodeException(-1, "헤더정보 생성중 오류가 발생했습니다. "+e.getMessage());
    			return false;
			}
    		jsonParamList.put(jsonParam);
    		//-------------------------------------------------------
    		// 헤더정보 입력 End.
    		//-------------------------------------------------------
			
    		
    		//-------------------------------------------------------
			// 바디정보 입력 Start..
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();
			final List<BarcodeListInfo> sendBarcodeInfos = mRepairBarcodeListAdapter.getAllItems();

			_SendCount = 0;
			for (BarcodeListInfo sendBarcodeInfo : sendBarcodeInfos) {

				if (!sendBarcodeInfo.isChecked()) continue;
				
				String partTypeCode = "";
    	        if (sendBarcodeInfo.getPartType().equals("E")) {
    	        	partTypeCode = "99";
    	        } else {
    	        	partTypeCode = SuportLogic.getPartTypeCode(sendBarcodeInfo.getPartType());
    	        }

				JSONObject jsonSubParam = new JSONObject();
				try {
					if (mJobGubun.equals("고장등록취소")||mJobGubun.equals("수리의뢰취소")) {
						jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
						jsonSubParam.put("DFLAG", "3");
						jsonSubParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());	                   // 스캔한 위치코드
						//-------------------------------------------------------------
	    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
	    				//-------------------------------------------------------------
						// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//	    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//	    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//	    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
	    				//-------------------------------------------------------------
						
						if (mJobGubun.equals("고장등록취소")) {
							jsonSubParam.put("DOCNO", sendBarcodeInfo.getFailureRegNo());  // 고장등록번호
							jsonSubParam.put("REGNO", sendBarcodeInfo.getFailureRegNo());  // 고장등록번호
							jsonSubParam.put("ZPSTATU", _SendCheck.facStatusCode);         // 설비상태
						}
						else if (mJobGubun.equals("수리의뢰취소")) {
							jsonSubParam.put("DOCNO", sendBarcodeInfo.getRepairRequestNo()); // 수리의뢰번호
							jsonSubParam.put("REGNO", sendBarcodeInfo.getFailureRegNo());  	 // 고장등록번호
						}
					} else if (mJobGubun.equals("수리완료")) {
						jsonSubParam.put("EXBARCODE", sendBarcodeInfo.getuFacCd());	         // 기존훼손바코드(바코드 대체시 활용)
						jsonSubParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());	                     // 스캔한 위치코드
						//-------------------------------------------------------------
	    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
	    				//-------------------------------------------------------------
						// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//	    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//	    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//	    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
	    				//-------------------------------------------------------------
						
						jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());								// 신규바코드 (Scan 된 바코드)
						jsonSubParam.put("RREASON", sendBarcodeInfo.getRreason());									// 대체발행사유
						jsonSubParam.put("ZPSTATU", _SendCheck.facStatusCode);               // 설비상태
						jsonSubParam.put("DOCNO", sendBarcodeInfo.getRepairRequestNo());     // 수리의뢰번호
					} else if (mJobGubun.equals("개조개량의뢰")||mJobGubun.equals("개조개량의뢰취소")) {
						jsonSubParam.put("LOCCODE", _SendCheck.locBarcodeInfo.getLocCd());
						jsonSubParam.put("EQUIPMENT", sendBarcodeInfo.getBarcode());
						jsonSubParam.put("MATNR", sendBarcodeInfo.getProductCode());
					} else if (mJobGubun.equals("개조개량완료")) {
                    	jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
                        jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                        jsonSubParam.put("DEVTYPE", sendBarcodeInfo.getDevType());
                        jsonSubParam.put("PARTTYPE", partTypeCode);
                        
                        // 개조개량 바코드 및 신규 바코드 
                        String EQUIPMENT = "";	// 개조개량바코드
                        String UPGEQUNR = "";	// 개조개량신규바코드
                        for (RemodelBarcodeInfo remodelBarcodeInfo : mRemodelBarcodeInfos)
                        {
                    		EQUIPMENT = remodelBarcodeInfo.getProductCode();      
                            UPGEQUNR = remodelBarcodeInfo.getUpgdoc();         	  
                            if (UPGEQUNR.isEmpty() && sendBarcodeInfo.getBarcode().equals(EQUIPMENT) || sendBarcodeInfo.getBarcode().equals(UPGEQUNR))
                            {
                            	jsonSubParam.put("EQUIPMENT", EQUIPMENT);
                            	jsonSubParam.put("UPGEQUNR", UPGEQUNR);
                                break;
                            }
                        }
                    	
                        // -- 개조개량 바코드 및 신규 바코드 
						jsonSubParam.put("ZPSTATU", _SendCheck.facStatusCode);  // 설비상태
					}else if(mJobGubun.equals("설비S/N변경")){
						jsonSubParam.put("BARCODE", sendBarcodeInfo.getBarcode());
						jsonSubParam.put("DEVTYPE", sendBarcodeInfo.getDevType());
						jsonSubParam.put("KOSTL", sendBarcodeInfo.getOrgId());
						jsonSubParam.put("PARTTYPE", partTypeCode);
						jsonSubParam.put("SERGE", sendBarcodeInfo.getFacSergeNum());
						jsonSubParam.put("ZPSTATU", "0110");
						
						System.out.println("SEND SERGE >> " + sendBarcodeInfo.getFacSergeNum());
					}
					// 수리완료는 운용조직변경 없음
					if (!mJobGubun.equals("수리완료") && !mJobGubun.equals("설비S/N변경")) {
						String CHKZKOSTL = sendBarcodeInfo.getCheckOrgYn();
						if (CHKZKOSTL.equals("N")) CHKZKOSTL = "X";       // 운용조직 상이한 데 운용 조직 변경 하시겠습니까? N 선택시
	                    else CHKZKOSTL = "";
						if (mJobGubun.equals("개조개량완료")) {
							CHKZKOSTL = "";
						}
						jsonSubParam.put("CHKZKOSTL", CHKZKOSTL);
					}

				} catch (JSONException e1) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
					return false;
				} catch (Exception e) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
					return false;
				}

				jsonSubParamList.put(jsonSubParam);

				_SendCount++; // 전송건수.
			} // for end.

			
			try {
				SendHttpController sendhttp = new SendHttpController();
				if (mJobGubun.equals("고장등록취소")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_OUTOFSERVICECANCEL, jsonParamList, jsonSubParamList);
				} else if (mJobGubun.equals("수리의뢰취소")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_OUTOFSERVICECANCEL2, jsonParamList, jsonSubParamList);
				} else if (mJobGubun.equals("수리완료")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_REPAIRRECEIPT, jsonParamList, jsonSubParamList);
				} else if (mJobGubun.equals("개조개량의뢰")|| mJobGubun.equals("개조개량의뢰취소")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_REMODELIMPROV_RQ_NEW, jsonParamList, jsonSubParamList);
				} else if (mJobGubun.equals("개조개량완료")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_REMODELIMPROV_CP_NEW, jsonParamList, jsonSubParamList);
				}else if (mJobGubun.equals("설비S/N변경")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_SERGE_CHANGE, jsonParamList, jsonSubParamList);
				}
				
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
			setBarcodeProgressVisibility(false);

			if (result) {
				mSendButton.setEnabled(true);

				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				
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
		        	
		            	sendWorkResult(sendMessage);
						
						initScreen();
		            	return;
		            }
		        });
		        AlertDialog dialog = builder.create();
		        dialog.show();
		        return;

			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			mSendButton.setEnabled(true);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
	
	/**
	 * 인스토어마킹 데이터 서버로 전송
	 * 
	 * @param sendCheck
	 */
	private void execSendInstoreMarkingDataInTask(SendCheck sendCheck) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		mSendButton.setEnabled(false);
		new SendInstoreMarkingDataInTask(sendCheck, this).execute();
	}

	// 먼저 수리완료 인스토어마킹 전송 비동기 테스크
	private class SendInstoreMarkingDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private final SendCheck _SendCheck;
		private int _SendCount = 0;
		private OutputParameter _OutputParameter;
		private Activity mActivity;
		
		public SendInstoreMarkingDataInTask(SendCheck sendCheck, Activity activity) {
			_SendCheck = sendCheck;
			mActivity = activity; 
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "SendDataInTask  Start...");

			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
    		jsonParamList.put(jsonParam);

			
    		//-------------------------------------------------------
    		// 바디정보 입력 Start.
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();
			final List<BarcodeListInfo> sendBarcodeInfos = mRepairBarcodeListAdapter.getAllItems();
			
			Log.d(TAG, "SendDataInTask Body Record Count==>"+ sendBarcodeInfos.size());

			_SendCount = 0;
			for (BarcodeListInfo sendBarcodeInfo : sendBarcodeInfos) {

				if (!sendBarcodeInfo.isChecked()) continue;
				IsmBarcodeInfo sendIsmBarcodeInfo = sendBarcodeInfo.getIsmBarcodeInfo(); 
				if (sendIsmBarcodeInfo.getInjuryBarcode().isEmpty()) continue;				// 인스토어마킹 정보가 없으면 continue;
				
				JSONObject jsonSubParam = new JSONObject();
				try {
					jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getNewBarcode());
					jsonSubParam.put("locationCode", sendIsmBarcodeInfo.getLocCd());
					jsonSubParam.put("deviceId", sendIsmBarcodeInfo.getDeviceId());
					jsonSubParam.put("itemCode", sendIsmBarcodeInfo.getItemCode());
					jsonSubParam.put("itemName", sendIsmBarcodeInfo.getItemName());
					jsonSubParam.put("itemCategoryCode", sendIsmBarcodeInfo.getItemCategoryCode());
					//jsonSubParam.put("itemCategoryName", sendIsmBarcodeInfo.getItemCategoryName());
					jsonSubParam.put("partKindCode", sendIsmBarcodeInfo.getPartKindCode());
					//jsonSubParam.put("partKindName", sendIsmBarcodeInfo.getPartKindName());
					jsonSubParam.put("itemLargeClassificationCode", sendIsmBarcodeInfo.getItemLargeClassificationCode());
					jsonSubParam.put("itemMiddleClassificationCode", sendIsmBarcodeInfo.getItemMiddleClassificationCode());
					jsonSubParam.put("itemSmallClassificationCode", sendIsmBarcodeInfo.getItemSmallClassificationCode());
					jsonSubParam.put("itemDetailClassificationCode", sendIsmBarcodeInfo.getItemDetailClassificationCode());
					jsonSubParam.put("injuryBarcode", sendIsmBarcodeInfo.getInjuryBarcode());
					jsonSubParam.put("publicationWhyCode", sendIsmBarcodeInfo.getPublicationWhyCode());
					jsonSubParam.put("supplierCode", sendIsmBarcodeInfo.getSupplierCode());
					jsonSubParam.put("deptCode", sendIsmBarcodeInfo.getDeptCode());
					jsonSubParam.put("operationDeptCode", sendIsmBarcodeInfo.getOperationDeptCode());
					jsonSubParam.put("makerCode", sendIsmBarcodeInfo.getMakerCode());
					jsonSubParam.put("makerSerial", sendIsmBarcodeInfo.getMakerSerial());
					jsonSubParam.put("makerNational", sendIsmBarcodeInfo.getMakerNational());
					jsonSubParam.put("obtainDay", sendIsmBarcodeInfo.getObtainDay());
					jsonSubParam.put("generationRequestSeq", sendIsmBarcodeInfo.getGenerationRequestSeq());
				} catch (JSONException e1) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
					return false;
				} catch (Exception e) {
					_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
					return false;
				}

				jsonSubParamList.put(jsonSubParam);

				_SendCount++; // 전송건수.
			} // for end.

			if (_SendCount>0) {
				try {
					SendHttpController sendhttp = new SendHttpController();
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_REPAIRRECEIPT_IM, jsonParamList, jsonSubParamList);
					
					if (_OutputParameter == null) {
						throw new ErpBarcodeException(-1, "'수리완료 인스토어마킹 완료' 정보 전송중 오류가 발생했습니다.");
					}
	
				} catch (ErpBarcodeException e) {
					Log.d(TAG, e.getErrMessage());
					_ErpBarException = e;
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);

			if (result) {
				if (_SendCount>2) _SendCount=2;
				// 1초 후에 실제 수리완료 전송
				new Handler().postDelayed(
						new Runnable() { 
							public void run() {
								new SendDataInTask(_SendCheck, mActivity).execute();
							}
					}, _SendCount*1000);
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			mSendButton.setEnabled(true);
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

    }
}
