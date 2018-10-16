package com.ktds.erpbarcode.management;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.DeviceBarcodeService;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.TransferBarcodeService;
import com.ktds.erpbarcode.barcode.WBSBarcodeService;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
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
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.SearchWbsCheckActivity;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class TransferActivity extends Activity {

	private static final String TAG = "TransferActivity";
	private static final int ACTION_SEARCHWBSCHECKACTIVITY = 1;
	
	public static final int EDIT_MODE_NONE = 100;
	public static final int EDIT_MODE_MODIFY = 110;
	public static final int EDIT_MODE_MOVE = 120;

	private ScannerConnectHelper mScannerHelper;
	
	private Fragment mBarcodeTreeFragment;
	private Fragment mTransferArgumentFragment;
    
	//private LinearLayout mOrgInputbar;
    private EditText mOrgCodeText;
    
    //private LinearLayout mLocInputbar;
    private EditText mLocCdText;
    private EditText mLocNameText;
    private EditText mGPSLocDataText;

    private LinearLayout mWbsInputbar;
    private EditText mWbsNoText;

    //private LinearLayout mDeviceInputbar;
    private EditText mDeviceIdText;
    private TextView mOprSysCdText;
    private EditText mDeviceIdInfoText;
    
    //private LinearLayout mFacInputbar;
    private EditText mFacCdText;
    private TextView mPartTypeText;
    private CheckBox mUUCheck;

    private LinearLayout mCrudNoneInputbar;
    private Button mInitButton, mReScanRequestButton, mDeleteButton, mSaveButton, 
    	mSendButton, mArgumentConfirmButton;
    
    private FrameLayout mTtransferArgumentFrame;
    
	private LocBarcodeInfo mThisLocCodeInfo;  // 현재 스캔한 위치바코드 정보.

	// 최초 조회시 장치바코드정보만 저장.
	private final Map<String, String> mTransferFirstDeviceMaps = new HashMap<String, String>();
	// 장치바코드의 운영중인 하위설비정보 저장.
	private final Map<String, List<String>> mTransferDeviceMaps = new HashMap<String, List<String>>();
	
	//private FindDeviceIdBelowFacListInTask mFindDeviceIdBelowFacListInTask;
	private DeleteReScanDataInTask mDeleteReScanDataInTask;
	
	private String mJobGubun;
	private String mDocNo;
	
	//---------------------------------------------------------------
	// 수정, 이동 모드 관련 변수들..
	//---------------------------------------------------------------
    private LinearLayout mCrudEditModeInputbar;
    private Button mMoveButton, mModifyButton, mCancelEditModeButton;
	private TextView mEditModeComment;
	private int mEditMode = EDIT_MODE_NONE;
	private Long mEditTreeKey = null;
	private BarcodeListInfo mEditBarcodeInfo = null;

	private Button mAddInfo;
	
	// 위치정보 
	private String pLocCd_deviceId, pLocNm_deviceId;

    @Override    
    public void onCreate(Bundle savedInstanceState) {
    	
    	mJobGubun = GlobalData.getInstance().getJobGubun();

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
        
        initBarcodeScanner();
        setMenuLayout();
        setContentView(R.layout.management_transfer_activity);
        setTreeLayout(savedInstanceState);
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

	private void setMenuLayout() {
    	ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mJobGubun + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

    
    /****************************************************************
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
		if (savedInstanceState != null)
			mBarcodeTreeFragment = getFragmentManager().getFragment(savedInstanceState, "mBarcodeTreeFragment");
		if (mBarcodeTreeFragment == null) {
			mBarcodeTreeFragment = new TransferTreeFragment();
		}
		
		getFragmentManager().beginTransaction()
			.replace(R.id.transfer_barcodetree_frame, mBarcodeTreeFragment)
			.commit();

		
		if (savedInstanceState != null)
			mTransferArgumentFragment = getFragmentManager().getFragment(savedInstanceState, "mTransferArgumentFragment");
		if (mTransferArgumentFragment == null) {
			mTransferArgumentFragment = new TransferArgumentFragment();
		}
		
		getFragmentManager().beginTransaction()
		.replace(R.id.transfer_argument_frame, mTransferArgumentFragment)
		.commit();
	}
    
    private void setLayout() {
    	// 운용조직
    	//mOrgInputbar = (LinearLayout) findViewById(R.id.transfer_organization_inputbar);
    	mOrgCodeText = (EditText) findViewById(R.id.transfer_organization_orgCode);
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);

    	// 위치코드 
        //mLocInputbar = (LinearLayout) findViewById(R.id.transfer_loc_inputbar);
        mLocCdText = (EditText) findViewById(R.id.transfer_location_locCd);
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

        // 위치코드명        
        mLocNameText = (EditText) findViewById(R.id.transfer_locInfo_locName);
        
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
        		
        		if(pLocCd_deviceId.length() < 1){
        			pLocCd_deviceId = mDeviceIdText.getText().toString();
        		}
        		
        		Intent intent = new Intent(getApplicationContext(), AddrInfoActivity.class);
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARCD, pLocCd_deviceId);
				intent.putExtra(AddrInfoActivity.INPUT_ADDR_BARNM, pLocNm_deviceId);
				intent.putExtra(AddrInfoActivity.INPUT_GUBUN, "장치바코드");
				startActivity(intent);
        	}
        });

        // GPS 위치데이타        
        mGPSLocDataText = (EditText) findViewById(R.id.transfer_gps_currentGPS);
        mGPSLocDataText.setSelected(true);
        //##msg  mGPSLocDataText.setOnClickListener(onClickListener);
        
        // WBS번호
        mWbsInputbar = (LinearLayout) findViewById(R.id.transfer_wbs_inputbar);
        mWbsNoText = (EditText) findViewById(R.id.transfer_wbs_wbsNo);
        
        // 장치바코드
        //mDeviceInputbar = (LinearLayout) findViewById(R.id.transfer_device_inputbar);
        mDeviceIdText = (EditText) findViewById(R.id.transfer_device_deviceId);
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

        mOprSysCdText = (TextView) findViewById(R.id.transfer_device_oprSysCd);
        mDeviceIdInfoText = (EditText) findViewById(R.id.transfer_deviceInfo_deviceInfo);

        // 설비바코드
        //mFacInputbar = (LinearLayout) findViewById(R.id.transfer_fac_inputbar);
        mFacCdText = (EditText) findViewById(R.id.transfer_fac_facCd);
        mFacCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
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
        mFacCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        
        mPartTypeText = (TextView) findViewById(R.id.transfer_fac_partType);
        mUUCheck = (CheckBox) findViewById(R.id.transfer_fac_UU);
        mUUCheck.setOnClickListener(
    			new View.OnClickListener() {
		    		@Override
		    		public void onClick(View v) {
		    			setJobWorkUUCheck(mUUCheck.isChecked());
		    		}
		    	});
        
        // 버튼 -----------------------------------------------------------------
        mCrudNoneInputbar = (LinearLayout) findViewById(R.id.transfer_crud_none_buttonbar);
        mCrudEditModeInputbar = (LinearLayout) findViewById(R.id.transfer_crud_editmode_buttonbar);
        mEditModeComment = (TextView) findViewById(R.id.transfer_crud_editmodeComment);
        
        // 초기화        
        mInitButton = (Button) findViewById(R.id.transfer_crud_init);        
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
        // 재스캔요청      
        mReScanRequestButton = (Button) findViewById(R.id.transfer_crud_reScanRequest);        
        mReScanRequestButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
				    	SendCheck sendCheck = new SendCheck();
				    	if (mThisLocCodeInfo == null) {
				    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
				    	} else {
				    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
				    	}
				    	sendCheck.wbsNo = mWbsNoText.getText().toString().trim();
				    	sendCheck.deviceId = mDeviceIdText.getText().toString().trim();
				    	
						executeRescanRequestSendCheck(sendCheck);
					}
				});
        // 이동        
        mMoveButton = (Button) findViewById(R.id.transfer_crud_move);        
        mMoveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						moveData();
					}
				});
        
        // 수정        
        mModifyButton = (Button) findViewById(R.id.transfer_crud_modify);        
        mModifyButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						modifyData();
					}
				});
        // 수정모드 취소
        mCancelEditModeButton = (Button) findViewById(R.id.transfer_crud_cancelEditMode);        
        mCancelEditModeButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						cancelEditMode();
					}
				});
        
        // 삭제        
        mDeleteButton = (Button) findViewById(R.id.transfer_crud_delete);        
        mDeleteButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						deleteData();
					}
				});

        // 저장
        mSaveButton = (Button) findViewById(R.id.transfer_crud_save);        
        mSaveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveWorkData();
					}
				});

        // 인수확정, 등록확정      
        mArgumentConfirmButton = (Button) findViewById(R.id.transfer_crud_argumentConfirm);        
        mArgumentConfirmButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) { 
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "KT 내부 직원만 '인수확정'이\n\r가능합니다.", BarcodeSoundPlay.SOUND_ERROR));
							return;
						}
						showTransferArgument(true);
					}
				});
        // 전송
        mSendButton = (Button) findViewById(R.id.transfer_crud_send);        
        mSendButton.setOnClickListener(
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
				    	sendCheck.wbsNo = mWbsNoText.getText().toString().trim();
				    	
						executeSendCheck(sendCheck);
					}
				});
        
        mTtransferArgumentFrame = (FrameLayout) findViewById(R.id.transfer_argument_frame);
    }

    @Override
	public void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("mThisLocCodeInfo", mThisLocCodeInfo);
        super.onSaveInstanceState(outState);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return false;
		if (item.getItemId()==android.R.id.home) {
			// 인수확정 창이 열려 있으면 닫는다.
			if (mTtransferArgumentFrame.getVisibility()==View.VISIBLE) {
				showTransferArgument(false);
				return false;
			}
			
    		if (GlobalData.getInstance().isChangeFlag()) {
				changeFlagYesNoDialog();
				return true;
	        }

			finish();
		} else {
        	return super.onOptionsItemSelected(item);
        }
	    return false;
    }

	@Override
	public void onBackPressed() {
		if (GlobalData.getInstance().isGlobalProgress()) return;
		
		// 인수확정 창이 열려 있으면 닫는다.
		if (mTtransferArgumentFrame.getVisibility()==View.VISIBLE) {
			showTransferArgument(false);
			return;
		}
		
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

    private void setFieldVisibility() {
    	//-----------------------------------------------------------
		// 위치바코드BAR 
		//-----------------------------------------------------------
		//mLocInputbar.setVisibility(View.GONE);
		//((LinearLayout) findViewById(R.id.transfer_locInfo_inputbar)).setVisibility(View.GONE);

		// GPS현재주소
		((LinearLayout) findViewById(R.id.transfer_gps_inputbar)).setVisibility(View.GONE);

		
		//-----------------------------------------------------------
		// WBS BAR 
		//-----------------------------------------------------------
		if (mJobGubun.equals("시설등록") ) {
			mWbsInputbar.setVisibility(View.GONE);
		}
		
		//-----------------------------------------------------------
		// 장치바코드BAR
		//-----------------------------------------------------------
		if (mJobGubun.equals("인수")) {
			mDeviceIdText.setEnabled(false);		// 인수는 장치바코드 비활성화
		}
    	
    	//-----------------------------------------------------------
		// 설비바코드BAR
		//-----------------------------------------------------------
    	if (mJobGubun.equals("인수")) {
    		mUUCheck.setVisibility(View.GONE);
    	}

		//-----------------------------------------------------------
		// CRUD버튼BAR 
		//-----------------------------------------------------------
		if (mJobGubun.equals("인계")) {
			//((LinearLayout) findViewById(R.id.transfer_crud_buttonbar)).setVisibility(View.GONE);
			mReScanRequestButton.setVisibility(View.GONE);
			mArgumentConfirmButton.setVisibility(View.GONE);

		} else if (mJobGubun.equals("인수")) {
			mMoveButton.setVisibility(View.GONE);
			mModifyButton.setVisibility(View.GONE);
			mDeleteButton.setVisibility(View.GONE);
			mArgumentConfirmButton.setText(getResources().getText(R.string.transfer_crud_argumentConfirm1_lavel));

		} else if (mJobGubun.equals("시설등록")) {
			mReScanRequestButton.setVisibility(View.GONE);
			mArgumentConfirmButton.setText(getResources().getText(R.string.transfer_crud_argumentConfirm2_lavel));
		}
    }
    
    /**
     * 이동, 수정, 삭제, 재스캔, 전송, 확정, 취소수정모드 버튼을 활성화/비활성화 처리한다.
     * 
     * @param is
     */
    public void enabledArgumentConfirmButton(boolean is) {
        mReScanRequestButton.setEnabled(is);
        mMoveButton.setEnabled(is);
        mModifyButton.setEnabled(is);
        mDeleteButton.setEnabled(is);
    	mSendButton.setEnabled(is);
    	mArgumentConfirmButton.setEnabled(!is);		// 인수확정은 Enable
    	mCancelEditModeButton.setEnabled(is);
    }
    
    public void initScreen() {
    	initScreen("all");
    	
    	// 초기에는 위치바코드를 스케너Focus 지정한다.
    	mScannerHelper.focusEditText(mLocCdText);
    }
    
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
    		mThisLocCodeInfo = null;    // 현재 스캔한 위치바코드 정보 초기화.    		
    		mTtransferArgumentFrame.setVisibility(View.GONE);		// 인수확정, 시설등록 확정 창 숨기기
    		enabledArgumentConfirmButton(true);
    	}
    	if (step.equals("all") || step.equals("wbs")) {
    		mWbsNoText.setText("");
    		mDocNo = "";				// 재스캔요청에 필요한 인수요청번호
    	}
    	if (step.equals("all") || step.equals("device")) {
    		mDeviceIdText.setText("");
        	mOprSysCdText.setText("");
        	mDeviceIdInfoText.setText("");
	    	pLocCd_deviceId=pLocNm_deviceId="";	// 위치정보 초기화 
    	}
    	if (step.equals("all") || step.equals("fac")) {
    		mFacCdText.setText("");
        	mPartTypeText.setText("");
        	mUUCheck.setChecked(false);
    	}
    	if (step.equals("all") || step.equals("editmode")) {
    		setEditMode(EDIT_MODE_NONE);
    		mCrudNoneInputbar.setVisibility(View.VISIBLE);
    		mCrudEditModeInputbar.setVisibility(View.GONE);
    		mEditModeComment.setText("");
    		mEditTreeKey = null;
    		mEditBarcodeInfo = null;
    	}
    	if (step.equals("all") || step.equals("crudbar")) {
    		mDeleteButton.setEnabled(true);
    		mSendButton.setEnabled(true);
    		mArgumentConfirmButton.setEnabled(false);
    	}
    	if (step.equals("all") || step.equals("tree")) {
        	mTransferFirstDeviceMaps.clear();
        	mTransferDeviceMaps.clear();
        	
        	initScreenTreeFragment();
    	}
    	if (step.equals("all") || step.equals("progress")) {
    		setBarcodeProgressVisibility(false);
    	}
    }

    /**************************************************************************
     * 여기서부터 BarcodeTreeFragment 연계 메서드들..
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
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
    
    private void initScreenTreeFragment() {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.initScreenTreeFragment();
    	}
    }
    
    private void doChkScan(String locCd, String deviceId, String barcode) {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.doChkScan(locCd, deviceId, barcode);
    	}
    }
    private void addScanData(String locCd, String deviceId, String barcode) {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.addScanData(locCd, deviceId, barcode);
    	}
    }
    private BarcodeTreeAdapter getBarcodeTreeAdapter() {
    	BarcodeTreeAdapter barcodeTreeAdapter = null;
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			barcodeTreeAdapter = fragment.getBarcodeTreeAdapter();
    	}
		return barcodeTreeAdapter;
    }
    private TreeViewList getBarcodeTreeView() {
    	TreeViewList barcodeTreeView = null;
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			barcodeTreeView = fragment.getBarcodeTreeView();
    	}
		return barcodeTreeView;
    }
    private void showSummaryCount() {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.showSummaryCount();
    	}
    }
    private void jobWorkEditBarcodeTreeView(final BarcodeListInfo editBarcodeInfo) {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.jobWorkEditBarcodeTreeView(editBarcodeInfo);
    	}
    }
    // 인수확정, 시설등록확정 창 팝업
    public void showTransferArgument(boolean isVisible) {
    	if (isVisible) {
    		mTtransferArgumentFrame.setVisibility(View.VISIBLE);
        	TransferArgumentFragment fragment = 
        			(TransferArgumentFragment) getFragmentManager().findFragmentById(R.id.transfer_argument_frame);
    		if (fragment != null) {
    			
    			String locCd = mLocCdText.getText().toString().trim();
    			String wbsNo = mWbsNoText.getText().toString().trim();
    			
    			List<String> deviceIds = new ArrayList<String>();
    			
    			for (BarcodeListInfo barcodeInfo : getBarcodeTreeAdapter().getAllItems().values()) {
    				if (barcodeInfo.getPartType().equals("D")) {
    					deviceIds.add(barcodeInfo.getDeviceId());
    				}
    			}
    			
    			fragment.getArgumentScanConfirmData(wbsNo, locCd, deviceIds);
        	}
    		
    	} else {
    		mTtransferArgumentFrame.setVisibility(View.GONE);
    	}
    }
    private void addBarcodeInfosTreeView(final List<BarcodeListInfo> barcodeInfos) {
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			fragment.addBarcodeInfosTreeView(barcodeInfos);
    	}
    }
    private String compareTargetHierarchy(BarcodeListInfo selectBarcodeInfo, BarcodeListInfo targetBarcodeInfo) {
    	String parentBarcode = "";
    	
    	TransferTreeFragment fragment = (TransferTreeFragment) getFragmentManager().findFragmentById(R.id.transfer_barcodetree_frame);
		if (fragment != null) {
			parentBarcode = fragment.compareTargetHierarchy(selectBarcodeInfo, targetBarcodeInfo);
    	}
		return parentBarcode;
    }
    /**
     * 여기까지 BarcodeTreeFragment 연계 메서드들..
     *************************************************************************/
    
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
	
	public String getLocCd() {
		return mLocCdText.getText().toString().trim();
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

	public LocBarcodeInfo getThisLocCodeInfo() {
		return mThisLocCodeInfo;
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
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_WBS, "wbsNo", wbsNo);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}

		String locCd = mThisLocCodeInfo.getLocCd();
		getTransferBarcodeSubResultsData(locCd, wbsNo);

	}

    /**
     * 장치ID 변경시 장치바코드 조회.
     */
    public void changeDeviceId(String barcode) {
		initScreen("device");
		
		// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		String locCd = mLocCdText.getText().toString().trim();
    		if (TextUtils.isEmpty(locCd)) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mLocCdText);
        		return;
    		}
    		
    		getOfflineDeviceBarcodeData(barcode);
    		
    	} else {
    		if (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty()) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mLocCdText);
        		return;
        	}
    		
    		if (mWbsInputbar.getVisibility()==View.VISIBLE && mWbsNoText.getText().toString().isEmpty()) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS 번호를 선택하세요."));
        		mScannerHelper.focusEditText(mLocCdText);
        		return;
        	}
    		
    		getDeviceBarcodeInfo(barcode);
    	}
    }
    
    /**
     * 설비바코드 정보 변경시 처리.
     */
    private void changeFacCd(String barcode) {

    	barcode = barcode.toUpperCase();
    	
    	//-----------------------------------------------------------
    	// 설비바코드의 PartType만 Clear시킨다.
    	//-----------------------------------------------------------
    	mPartTypeText.setText("");
    	
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		String locCd = mLocCdText.getText().toString().trim();
    		if (TextUtils.isEmpty(locCd)) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mLocCdText);
    			return;
    		}
    		
    		if (getBarcodeTreeAdapter().getCount()<2) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
        		mScannerHelper.focusEditText(mDeviceIdText);
                return;
            }
    		
    		getOfflineProductBarcodeData(barcode);
    		
    	} else {
    		if (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty()) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
    			mScannerHelper.focusEditText(mLocCdText);
    			return;
    		}
            
    		if (mWbsInputbar.getVisibility()==View.VISIBLE && mWbsNoText.getText().toString().isEmpty()) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS 번호를 선택하세요."));
        		mScannerHelper.focusEditText(mLocCdText);
        		return;
        	}
        	
            if (getBarcodeTreeAdapter().getCount()<2) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
        		mScannerHelper.focusEditText(mDeviceIdText);
                return;
            }
            
            if (mJobGubun.equals("인계") ||mJobGubun.equals("시설등록")) {
    			if(getBarcodeTreeAdapter().getCount() >= 900){
    				GlobalData.getInstance().showMessageDialog(
    						new ErpBarcodeException(-1, "처리할 수 있는 설비 개수를\n\r넘었습니다.\n\r공사담당자께 문의하시기\n\r바랍니다."));
    				return;
    			}
    		}
    		
    		//-----------------------------------------------------------
        	// "인수"는  Tree에 바코드가 있는지 체크한다.
        	//-----------------------------------------------------------
        	if (mJobGubun.equals("인수")) {
        		doChkScan("", "", barcode);
        	} else {
        		addScanData("", "", barcode);
        	}
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

    public void choiceBarcodeDataDisplay(BarcodeListInfo barcodeInfo) {
    	if (barcodeInfo == null) return;

    	if (barcodeInfo.getPartType().equals("L")) {
    		return;
    	}
    	
    	if (barcodeInfo.getPartType().equals("D")) {
    		mDeviceIdText.setText(barcodeInfo.getBarcode());
    		//String deviceInfo = barcodeInfo.getItemCode() + "/" + barcodeInfo.getItemName() + "/"
    		//		+ barcodeInfo.getLocCd() + "/" + barcodeInfo.getDeviceStatusName();
    		//mDeviceIdInfoText.setText(deviceInfo);
    	    //mOprSysCdText.setText(barcodeInfo.getOperationSystemCode());
    	} else {
    		mFacCdText.setText(barcodeInfo.getBarcode());
        	mPartTypeText.setText(barcodeInfo.getPartType());
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
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드는 이동할 수 없습니다."));
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
    
    // 수정
    private void modifyData() {
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
    	
    	if (getEditMode() == EDIT_MODE_MOVE) return;
    	
    	if (getEditMode() == EDIT_MODE_NONE) {
    		setEditMode(EDIT_MODE_MODIFY);
    		mEditModeComment.setText("수정할 개체를 스캔하세요. ");
    		mEditTreeKey = selectedKey;
    		mEditBarcodeInfo = barcodeInfo;
    		mCrudNoneInputbar.setVisibility(View.GONE);
    		mCrudEditModeInputbar.setVisibility(View.VISIBLE);
    	}
    	
    	if (barcodeInfo.getPartType().equals("D")) {
    		mScannerHelper.focusEditText(mDeviceIdText);
        } else {
        	mScannerHelper.focusEditText(mFacCdText);
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


    // 삭제
    private void deleteData() {
    	final Long selectedKey = getBarcodeTreeAdapter().getSelected();
    	if (selectedKey == null) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
    		return;
    	}
    	
    	BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedKey);
    	if (barcodeInfo != null) {
    		if (barcodeInfo.getPartType().equals("L")) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드는 삭제할 수 없습니다."));
        		return;
    		}
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
            	
            	//-----------------------------------------------------------
                // 여기서 삭제한다.
                //-----------------------------------------------------------
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

    	//-----------------------------------------------------------
        // 장치바코드의 운영중인 하위설비정보 저장된 장치바코드를 삭제한다.
        //-----------------------------------------------------------
    	if (barcodeInfo != null) {
    		if (barcodeInfo.getPartType().equals("D")) {
    			mTransferDeviceMaps.remove(selectedKey);
    		}
    	}
    	
    	// 마지막 row 선택
    	getBarcodeTreeAdapter().selectLastPosition();
    	
    	//-----------------------------------------------------------
	    // Tree의 바닥 Summary Count를 재계산해서 보여준다.
	    //-----------------------------------------------------------
    	showSummaryCount();
    	
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 true
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
        GlobalData.getInstance().setSendAvailFlag(true);

		//-----------------------------------------------------------
    	// 단계별 작업을 삭제한다.
    	//-----------------------------------------------------------
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
    private void sendWorkResult(String sendMessage) {
    	
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
    	
    	if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_LOC)) {
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

    	}  else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_EDIT)) {
    		String selectBarcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "selectBarcode");
    		String editJsonString = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "editJsonString");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_MOVE==>"+selectBarcode + "," + editJsonString);
    		
    		Long selectedKey = getBarcodeTreeAdapter().getBarcodeKey(selectBarcode);
    		getBarcodeTreeAdapter().changeSelected(selectedKey);
    		// 수정모드로 변환한다.
    		modifyData();
    		
    		Gson gson = new Gson();
    		Type barcodeType = new TypeToken<BarcodeListInfo>(){}.getType();
    		BarcodeListInfo editBarcodeInfo = gson.fromJson(editJsonString, barcodeType);
    		jobWorkEditBarcodeTreeView(editBarcodeInfo);
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

		initScreen();
		mLocCdText.setText(barcode);
		
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
		mLocCdText.setText(mThisLocCodeInfo.getLocCd());
		mLocNameText.setText(mThisLocCodeInfo.getLocName());
		mDeviceIdText.setText(mThisLocCodeInfo.getDeviceId());
		mOprSysCdText.setText(mThisLocCodeInfo.getOperationSystemCode());
		
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
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_LOC, "locCd", mThisLocCodeInfo.getLocCd());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		// 여기는 시설등록에서만 사용된다.
		// 시설등록은 위치바코드 조회후에는 장치바코드로 Focus 이동한다.
		searchData(mThisLocCodeInfo.getLocCd(), "");
		return;
    }
    
      private void showSearchWbsCheckActivity(String locCd) {
    	if (locCd.isEmpty()) return;
    	
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SearchWbsCheckActivity.class);
		intent.putExtra(SearchWbsCheckActivity.INPUT_LOC_CD, locCd);
		intent.putExtra(SearchWbsCheckActivity.INPUT_WORK_CAT, "1");
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
            switch (msg.what) {
            case WBSBarcodeService.STATE_SUCCESS :
            	//String findedMessage = msg.getData().getString("message");
            	
            	//List<WBSInfo> wbsInfos = null;
                //try {
                //	wbsInfos = WBSConvert.jsonArrayStringToWBSInfos(findedMessage);
    			//} catch (ErpBarcodeException e) {
    			//	GlobalData.getInstance().showMessageDialog(e.getErrMessage());
                //	return;
    			//}

                //---------------------------------------------------
                // WBS정보 조회 화면을 오픈한다.
                //---------------------------------------------------
                showSearchWbsCheckActivity(mThisLocCodeInfo.getLocCd());
                
    			//-----------------------------------------------------------
    	        // 화면 초기 Setting후 변경 여부 Flag
    	        //-----------------------------------------------------------
    	    	GlobalData.getInstance().setChangeFlag(true);
    	        GlobalData.getInstance().setSendAvailFlag(true);
        		
                break;
            case WBSBarcodeService.STATE_NOT_FOUND:
            	//-----------------------------------------------------------
            	// WBS정보가 없으면 (인계, 인수만 해당)
    			// 바코드 스케너 포커스를 위치바코드로 이동한다.
    			mScannerHelper.focusEditText(mLocCdText);
            	
    			//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS번호가 없습니다. "));
            	
            	break;
            case WBSBarcodeService.STATE_ERROR:
            	//-----------------------------------------------------------
            	// WBS정보가 없으면 (인계, 인수만 해당)
    			// 바코드 스케너 포커스를 위치바코드로 이동한다.
    			mScannerHelper.focusEditText(mLocCdText);
            	
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
	private void getDeviceBarcodeInfo(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);

		DeviceBarcodeService devicebarcodeService = new DeviceBarcodeService(new DeviceBarcodeHandler(true));
		devicebarcodeService.search(deviceId);
	}
	private void getDeviceBarcodeInfoNoCheckProgress(String deviceId) {
		DeviceBarcodeService devicebarcodeService = new DeviceBarcodeService(new DeviceBarcodeHandler(false));
		devicebarcodeService.search(deviceId);
	}
    private class DeviceBarcodeHandler extends Handler {
    	private boolean mTaskJobWork;
    	public DeviceBarcodeHandler(boolean taskJobWork) {
    		mTaskJobWork = taskJobWork;
    	}
    	
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case DeviceBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	DeviceBarcodeInfo deviceInfo = DeviceBarcodeConvert.jsonStringToDeviceBarcodeInfo(findedMessage);
                
                successDeviceBarcodeProcess(deviceInfo, mTaskJobWork);
        		
                break;
            case DeviceBarcodeService.STATE_NOT_FOUND:
            	mScannerHelper.focusEditText(mDeviceIdText);
            	
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다."));
            	break;
            case DeviceBarcodeService.STATE_ERROR:
            	mScannerHelper.focusEditText(mDeviceIdText);
            	
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void successDeviceBarcodeProcess(DeviceBarcodeInfo deviceBarcodeInfo, boolean taskJobWork) {
		Log.i(TAG, "장치바코드 getDeviceId==>"+deviceBarcodeInfo.getDeviceId());
		Log.i(TAG, "장치바코드 getItemCode==>"+deviceBarcodeInfo.getItemCode());
		Log.i(TAG, "장치바코드 getItemName==>"+deviceBarcodeInfo.getItemName());
		Log.i(TAG, "장치바코드 getOperationSystemCode==>"+deviceBarcodeInfo.getOperationSystemCode());
		Log.i(TAG, "장치바코드 getLocationCode==>"+deviceBarcodeInfo.getLocationCode());
		
		mDeviceIdText.setText(deviceBarcodeInfo.getDeviceId());
		String deviceInfo = deviceBarcodeInfo.getItemCode() + "/" + deviceBarcodeInfo.getItemName() + "/"
				+ deviceBarcodeInfo.getLocationCode() + "/" + deviceBarcodeInfo.getDeviceStatusName();
		mDeviceIdInfoText.setText(deviceInfo);
	    mOprSysCdText.setText(deviceBarcodeInfo.getOperationSystemCode());
	    
	    // 위치 주소 정보 조회 
    	pLocCd_deviceId = deviceBarcodeInfo.getLocationCode();
		pLocNm_deviceId = deviceBarcodeInfo.getLocationShortName();
	    
	    // 장비바코드를 위치바코드 하위Node에 추가한다.
	    addTreeNodeDeviceBarcode(deviceBarcodeInfo);
	    
	    //-------------------------------------------------------
		// 화면 초기 Setting후 변경여부 Flag
		//-------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(true);
        GlobalData.getInstance().setSendAvailFlag(true);

		// 바코드 스케너 포커스를 설비바코드로 이동한다.
		mScannerHelper.focusEditText(mFacCdText);
		
	    //-------------------------------------------------------
		// 작업관리 진행 flag가 true일때만 진행한다.
		//-------------------------------------------------------
		if (taskJobWork) {
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
		} else {
			//-------------------------------------------------------------
	    	// 단계별 작업을 여기서 처리한다.
	    	//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			}
		}
    }
    
    public class ReScanCheck {
    	public boolean isRreScaned = false;
    	public String locCd = "";
    	public String wbsNo = "";
    	public String reScan_Yn = null;
    	public String transferDelete_Yn = null;
    }
    
    private void executeReScanCheck(final ReScanCheck reScanCheck, final JSONArray erpScanJsonArray) {
    	//-----------------------------------------------------------
    	// 재스캔 요청이면 Yes/No Dialog로 물어본다.
    	//-----------------------------------------------------------
    	if (reScanCheck.isRreScaned) {
    		if (reScanCheck.reScan_Yn == null) {
    			
    			//-----------------------------------------------------------
    			// Yes/No Dialog
    			//-----------------------------------------------------------
    			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    			GlobalData.getInstance().setGlobalAlertDialog(true);
    			
    			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
        		String message = "재스캔 요청이 들어왔습니다.\n\r정확히 스캔 하시기 바랍니다.\n\r기존 Data가 존재 합니다.\n\r기존 Data를 삭제 후\n\r신규 Scan하시겠습니까?";
    			
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
                    	
                    	reScanCheck.reScan_Yn = "Y";
                    	executeReScanCheck(reScanCheck, erpScanJsonArray);
                    	return;
                    }
                });
                builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	GlobalData.getInstance().setGlobalAlertDialog(false);
                    	
                    	reScanCheck.reScan_Yn = "N";
                    	executeReScanCheck(reScanCheck, erpScanJsonArray);
                    	return;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return;
    		}
    		
    		if (reScanCheck.reScan_Yn.equals("Y")) {
    			if (reScanCheck.transferDelete_Yn == null) {
    				
    				//-----------------------------------------------------------
    				// Yes/No Dialog
    				//-----------------------------------------------------------
    				if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    				GlobalData.getInstance().setGlobalAlertDialog(true);
    				
    				GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    				String message = "ERP에 저장된 인계스캔 Data 전체가\n\r삭제 됩니다.\n\r정말 삭제 하시겠습니까?";
        			
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
                        	
                        	// ERP 재스캔 데이터 삭제하고 여기서 종료한다.
                        	reScanCheck.transferDelete_Yn = "Y";
                        	deleteReScanData(reScanCheck, erpScanJsonArray);
                        	return;
                        }
                    });
                    builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	GlobalData.getInstance().setGlobalAlertDialog(false);
                        	
                        	reScanCheck.transferDelete_Yn = "N";
                        	executeReScanCheck(reScanCheck, erpScanJsonArray);
                        	return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;   
    			}
    		}
    	}

		if (mJobGubun.equals("인계") || mJobGubun.equals("인수")) {
			searchData(mThisLocCodeInfo.getLocCd(), reScanCheck.wbsNo);
		}
    }
    
	/**
	 * ERP 재스캔데이터 삭제.
	 */
	private void deleteReScanData(final ReScanCheck reScanCheck, JSONArray erpScanJsonArray) {
		if (isBarcodeProgressVisibility()) return;
		
		if (mDeleteReScanDataInTask == null) {
			setBarcodeProgressVisibility(true);
			mDeleteReScanDataInTask = new DeleteReScanDataInTask(reScanCheck, erpScanJsonArray);
			mDeleteReScanDataInTask.execute((Void) null);
		}
	}
	public class DeleteReScanDataInTask extends AsyncTask<Void, Void, Boolean> {
		private final ReScanCheck _ReScanCheck;
		private JSONArray _ErpScanJsonArray;
		private OutputParameter _OutputParameter;
		private int _SendCount = 0;
		private ErpBarcodeException _ErpBarException;
		
		public DeleteReScanDataInTask(final ReScanCheck reScanCheck, JSONArray erpScanJsonArray) {
			_ReScanCheck = reScanCheck;
			_ErpScanJsonArray = erpScanJsonArray;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.i(TAG, "DeleteReScanDataInTask  doInBackground  Start...");
			
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
    		try {
				jsonParam.put("WORKID", "0002");
        		jsonParam.put("PRCID", "0540");     // "인계취소"="재스캔요청"->"인수거부"
        		jsonParam.put("POSID", _ReScanCheck.wbsNo);
    		} catch (JSONException e) {
    			_ErpBarException = new ErpBarcodeException(-1, "헤더정보 생성중 오류가 발생했습니다. "+e.getMessage());
    			return false;
			}

    		JSONArray jsonParamList = new JSONArray();
    		jsonParamList.put(jsonParam);

    		
    		Log.i(TAG, "DeleteReScanDataInTask  Body Parameter  Start...");
    		//-------------------------------------------------------
			// 작업된 바코드 바디정보 생성.
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();

			Log.i(TAG, "DeleteReScanDataInTask Body Record Count==>" + _ErpScanJsonArray.length());

			_SendCount = 0;
			for (int i=0; i<_ErpScanJsonArray.length(); i++) {
				JSONObject jsonSubParam = new JSONObject();
    	        try {
    	        	JSONObject jsonobj = _ErpScanJsonArray.getJSONObject(i);
    	        	
    	        	String docNo = jsonobj.getString("DOCNO").trim();
    	        	String wbsNo = jsonobj.getString("POSID").trim();
    	        	String deviceId = jsonobj.getString("DEVICEID").trim();
    	        	
                	jsonSubParam.put("LOCCODE", _ReScanCheck.locCd);
                	jsonSubParam.put("DOCNO", docNo);
                	jsonSubParam.put("POSID", wbsNo);
                	jsonSubParam.put("DEVICEID", deviceId);
                	jsonSubParam.put("S_WORKID", SessionUserData.getInstance().getUserId());		// 인계자료 삭제시 인계자아이디를 본인 사번으로

        	        jsonSubParamList.put(jsonSubParam);
        	        _SendCount++;  // 전송건수.
    	        } catch (JSONException e1) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
        			return false;
    	        } catch (Exception e) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
        			return false;
    	        }
			}
            
            Log.i(TAG, "DeleteReScanDataInTask SendHttpController "+mJobGubun+" Start...");
            
            try {
        		SendHttpController sendhttp = new SendHttpController();
        		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_TRANSFERSCAN_DELETE,
        				jsonParamList, jsonSubParamList);

            	if (_OutputParameter == null) {
            		throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송중 오류가 발생했습니다." );
            	}
            } catch (ErpBarcodeException e) {
				Log.i(TAG, e.getErrMessage());
				_ErpBarException = e;
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mDeleteReScanDataInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				
				//---------------------------------------------------
				// 리스캔요청 정보 삭제후 초기화 한다.
				//---------------------------------------------------
				initScreen("fac");
				initScreen("tree");
				
				//---------------------------------------------------------
        		// 위치바코드에 무선바코드 스캔시 장치바코드를 조회하여 TreeNode에 추가한다.
        		//---------------------------------------------------------
				if (mThisLocCodeInfo.getDeviceId().isEmpty()) {
        			if (!mJobGubun.equals("인수")) {
        				addTreeNodeLocBarcode(mThisLocCodeInfo);
            			mScannerHelper.focusEditText(mDeviceIdText);
        			}
            	} else {
            		if (mJobGubun.equals("인수")) {
            			mDeviceIdText.setText(mThisLocCodeInfo.getDeviceId());
            		} else {
            			addTreeNodeLocBarcode(mThisLocCodeInfo);
            			// 장치바코드 정보 조회.
                		getDeviceBarcodeInfoNoCheckProgress(mThisLocCodeInfo.getDeviceId());
                		return;
            		}
            	}
				
				//-------------------------------------------------------------
		    	// 단계별 작업을 여기서 처리한다.
		    	//-------------------------------------------------------------
				if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
					GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
				}
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mDeleteReScanDataInTask = null;
			setBarcodeProgressVisibility(false);
		}
	}
    
	/**
	 * 인계에서 재스캔 요청 있는지 대상 목록 조회.
	 */
	private void getTransferBarcodeSubResultsData(String locCd, String wbsNo) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		TransferBarcodeService transferBarcodeService = new TransferBarcodeService(mTransferSubBarcodeHandler);
		transferBarcodeService.search(locCd, wbsNo, true);
	}
	
	/**
	 * 인계 재스캔 요청 서브결과 Handler
	 */
    private final Handler mTransferSubBarcodeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case TransferBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");

            	JSONArray subResults = null;
            	JSONTokener jsonTokener = new JSONTokener(findedMessage);
        		try {
        			subResults = (JSONArray) jsonTokener.nextValue();
        		} catch (JSONException e) {
        			subResults = null;
        		}
        		
        		if (subResults == null) {
        			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 인계서브결과 정보입니다. "));
        			return;
        		}
        		
        		Log.i(TAG, "mTransferSubBarcodeHandler    findedMessage==>" + findedMessage);
        		
        		boolean rescanFlag = false;
        		boolean workstepFlag = false;
        		
        		for (int i=0; i<subResults.length(); i++) {
        			JSONObject jsonobj = null;
					try {
						jsonobj = subResults.getJSONObject(i);
						
						String rescanReq = jsonobj.getString("RESCAN_REQ");
	        			String workStep = jsonobj.getString("WORKSTEP");
	        			mDocNo = jsonobj.getString("DOCNO");				// 재스캔요청에 필요한 인수요청번호
	        			
	        			// 재스캔요청이 있는지 Check
	        			if (!rescanReq.isEmpty()) {
	        				rescanFlag = true;
	        			}
	        			
	        			// 인수일때 확정버튼 활성화 Flag
	        			if (workStep.equals("02") || workStep.equals("03")) {
	        				workstepFlag = true;
	        			}
					} catch (JSONException e) {
						Log.i(TAG, "TransferSubBarcodeHandler 인계대상 결과 유효성 검증중 오류가 발행했습니다. ==>"+e.getMessage());
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "인계대상 결과 유효성 검증중 오류가 발행했습니다."));
	        			return;
					}
				}
				if (mJobGubun.equals("인계")) {
					ReScanCheck reScanCheck = new ReScanCheck();
					reScanCheck.isRreScaned = rescanFlag;
					reScanCheck.locCd = mThisLocCodeInfo.getLocCd();
					reScanCheck.wbsNo = mWbsNoText.getText().toString().trim();
					executeReScanCheck(reScanCheck, subResults);
					
				} else if (mJobGubun.equals("인수")) {
					// 인수예정이며 인수확정 버튼 활성화
					if (workstepFlag) {
						mArgumentConfirmButton.setEnabled(true);
					}					
					ReScanCheck reScanCheck = new ReScanCheck();
					reScanCheck.isRreScaned = false;
					reScanCheck.locCd = mThisLocCodeInfo.getLocCd();
					reScanCheck.wbsNo = mWbsNoText.getText().toString().trim();
					executeReScanCheck(reScanCheck, subResults);
				}
        		
                break;
            case TransferBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	
            	TransferCheck transferCheck = new TransferCheck();
            	transferNotFoundProcess(transferCheck);

            	break;
            case TransferBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
	
    /**
	 * 인계, 인수, 시설등록 대상 목록 조회.
	 */
    private void searchData(String locCd, String wbsNo) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);

		// 장치, 설비, Tree관련 초기화 한다.
		//initScreen("device");
    	initScreen("fac");
    	initScreen("tree");

		TransferBarcodeService transferBarcodeService = new TransferBarcodeService(mTransferBarcodeHandler);
		transferBarcodeService.search(locCd, wbsNo, false);
	}
	/**
	 * 인계대상정보 조회 결과 Handler
	 */
    private final Handler mTransferBarcodeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case TransferBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> transferBarcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

                addTreeNodeFirstLocDeviceFacInfos(transferBarcodeListInfos);
        		
                break;
            case TransferBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	
            	TransferCheck transferCheck = new TransferCheck();
            	transferNotFoundProcess(transferCheck);
            	
            	break;
            case TransferBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    /**
     *  인계/인수 데이터 유효성 체크 Class 
     */
    public class TransferCheck {
    	public String not_found_dialog_message_yn = null;
    	public String not_found_dialog_message = "";
    }
    private void transferNotFoundProcess(final TransferCheck transferCheck) {
    	
    	// 인계, 인수대당 정보가 없으면 위치바코드를 TreeNode에 추가한다.
    	// 설비, Tree관련 초기화 한다.
    	initScreen("fac");
    	initScreen("tree");
    	
    	if (transferCheck.not_found_dialog_message_yn == null) {
    		transferCheck.not_found_dialog_message_yn = "N";
    		
        	if (mJobGubun.equals("인계")) {
        		transferCheck.not_found_dialog_message = "첫 인계스캔 입니다.\n\r작업하신 장비를 장치바코드를 포함하여 정확하게 스캔하여 주시기 바랍니다.";

    		} else if (mJobGubun.equals("시설등록")) {
    			transferCheck.not_found_dialog_message = "첫 시설등록스캔 입니다.\n\r작업하신 장비를 장치바코드를 포함하여 정확하게 스캔하여 주시기 바랍니다.";
    			
    		} else {
    			transferCheck.not_found_dialog_message = "조회된 정보가 없습니다.";
    		}
        	
        	transferNextDialog(transferCheck);
        	return;
    	}

    	
    	//---------------------------------------------------------
		// 위치바코드에 무선바코드 스캔시 장치바코드를 조회하여 TreeNode에 추가한다.
		//---------------------------------------------------------
		if (mThisLocCodeInfo.getDeviceId().isEmpty()) {
			if (!mJobGubun.equals("인수")) {
				addTreeNodeLocBarcode(mThisLocCodeInfo);
    			mScannerHelper.focusEditText(mDeviceIdText);
			}
    	} else {
    		if (mJobGubun.equals("인수")) {
    			mDeviceIdText.setText(mThisLocCodeInfo.getDeviceId());
    		} else {
    			addTreeNodeLocBarcode(mThisLocCodeInfo);
    			// 장치바코드 정보 조회.
        		getDeviceBarcodeInfoNoCheckProgress(mThisLocCodeInfo.getDeviceId());
        		return;
    		}
    	}
		
		//-----------------------------------------------------------
		// 인수,인계 조회 정보가 없어도 변경을 본다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
        GlobalData.getInstance().setSendAvailFlag(true);
		
		//-----------------------------------------------------------
		// 단계별 작업을 여기서 처리한다.
		//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		}
    }
    
    /**
     * Next Message Dialog 화면을 오픈한다. 
     * @param transferCheck
     */
    private void transferNextDialog(final TransferCheck transferCheck) {
    	
    	//-----------------------------------------------------------
    	// Next Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);

    	GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    	final Builder builder = new AlertDialog.Builder(this); 
    	builder.setIcon(android.R.drawable.ic_menu_info_details);
    	builder.setTitle("알림");
    	TextView msgText = new TextView(getBaseContext());
    	msgText.setPadding(10, 30, 10, 30);
    	msgText.setText(transferCheck.not_found_dialog_message);
    	msgText.setGravity(Gravity.CENTER);
    	msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    	msgText.setTextColor(Color.BLACK);
    	builder.setView(msgText);
    	builder.setCancelable(false);
    	builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);

            	transferCheck.not_found_dialog_message_yn = "Y";
            	transferNotFoundProcess(transferCheck);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }
	
    /**
     * 위치바코드정보 Tree에 Node추가.
     * 
     * @param locBarcodeInfo   위치바코드인포
     */
    private void addTreeNodeLocBarcode(LocBarcodeInfo locBarcodeInfo) {
    	if (getBarcodeTreeAdapter().getCount() > 0) {
    		return;
    	}
    	
    	BarcodeListInfo locBarcode = new BarcodeListInfo(mJobGubun, "L", locBarcodeInfo.getLocCd(), locBarcodeInfo.getLocName());
    	
    	getBarcodeTreeAdapter().addItem(locBarcode);
    	TreeNodeInfo<Long> treeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(locBarcode.getBarcode());
    	if (treeNode != null) {
    		getBarcodeTreeAdapter().changeSelected(treeNode.getId());
    		int barcodePosition = getBarcodeTreeAdapter().getKeyPosition(treeNode.getId());
    		getBarcodeTreeView().setSelection(barcodePosition);  // 선택된 바코드로 커서 이동한다.
    	}
    	showSummaryCount();
    	
		//-----------------------------------------------------------
        // 화면 초기 Setting후 변경 여부 Flag
        //-----------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(true);
        GlobalData.getInstance().setSendAvailFlag(true);
    }
	
    /**
     * 장치바코드정보 Tree에 Node추가.
     * 
     * @param deviceBarcodeInfo   장치바코드인포
     */
    private void addTreeNodeDeviceBarcode(DeviceBarcodeInfo deviceBarcodeInfo) {
    	TreeNodeInfo<Long> treeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(deviceBarcodeInfo.getDeviceId());
    	if (treeNode == null) {
    		// 장치바코드 추가시는 무조건 Tree Focus를 최상위노드로 한다.
        	getBarcodeTreeAdapter().changeSelected(getBarcodeTreeAdapter().getItemId(0));
    		
        	String UFacBarcode = mThisLocCodeInfo.getLocCd();
        	BarcodeListInfo deviceBarcode = new BarcodeListInfo(mJobGubun, "D", deviceBarcodeInfo.getDeviceId(), deviceBarcodeInfo.getDeviceName(), UFacBarcode, 
        			deviceBarcodeInfo.getItemCode(), deviceBarcodeInfo.getItemName(), deviceBarcodeInfo.getDeviceStatusCode(), deviceBarcodeInfo.getDeviceStatusName(), 
        			deviceBarcodeInfo.getLocationCode(), "", deviceBarcodeInfo.getOperationSystemCode());

    		if (getEditMode() == EDIT_MODE_MODIFY) {
    			//----------------------------------------------------------
    	    	// 선택한 바코드를 다른 바코드로 수정한다.
    			editBarcodeCheck editCheck = new editBarcodeCheck();
				editCheck.barcodeItem = deviceBarcode;
    			editTransferTreeNode(editCheck);
    			return;
    			
            } else {
            	getBarcodeTreeAdapter().addItem(deviceBarcode);
            	getBarcodeTreeAdapter().changeSelected(deviceBarcode.getBarcode());
            	showSummaryCount();

            	//----------------------------------------------------------
            	// 음영지역이 아닐때 조회한다. (서버 전송시 체크로직으로 사용함.)
            	//----------------------------------------------------------
            	if (!SessionUserData.getInstance().isOffline()) {
            		// 신규로 장치바코드 추가시 
            		// 운영중인 하위설비 목록을 조회하여 Maps에 저장한다.
            		// 전송시에 하위설비 유무 체크로직에서 사용한다.
            		getDeviceBelowFacUpdate(deviceBarcodeInfo.getDeviceId());
            	}
        		
        		//-----------------------------------------------------------
                // 화면 초기 Setting후 변경 여부 Flag
                //-----------------------------------------------------------
            	GlobalData.getInstance().setChangeFlag(true);
                GlobalData.getInstance().setSendAvailFlag(true);
            }
    		
    	} else {
    		// 장치바코드 Tree Focus 이동한다.
        	getBarcodeTreeAdapter().changeSelected(deviceBarcodeInfo.getDeviceId());
    		
    		// WBS번호로 조회된 인계대상 조회 정보중에 장치ID가 있으면 messageDialog를 Display하지 않는다.
    		boolean deviceIdSearchFlag = false;
    		for (String deviceIdKey : mTransferFirstDeviceMaps.keySet()) {
    			if (deviceBarcodeInfo.getDeviceId().equals(deviceIdKey)) {
    				deviceIdSearchFlag = true;
    			}
    		}
    		if (!deviceIdSearchFlag) {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "장치바코드 중복 스캔입니다.\n\r\n\r" + deviceBarcodeInfo.getDeviceId(), BarcodeSoundPlay.SOUND_DUPLICATION));
    			return;
    		}
    	}
    }
    
    /**
     * 바코드 대체 체크 Class
     */
    public class editBarcodeCheck {
    	public String edit_barcode_yn = null;
    	public BarcodeListInfo barcodeItem = null;
    }
    
	/**
	 * Tree의 다른 바코드로 대체.
	 * 
	 * @param deviceBarcode
	 */
	public void editTransferTreeNode(final editBarcodeCheck editCheck) {
		
		if (getEditTreeKey() == null || getEditBarcodeInfo() == null) return;
		
		final BarcodeListInfo deviceBarcode = editCheck.barcodeItem;
		
		if (!deviceBarcode.getPartType().equals(getEditBarcodeInfo().getPartType())) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "다른 종류의 바코드로 수정하실 수\n\r없습니다.\n\r\n\r같은 종류의 바코드를 다시\n\r스캔하세요."));
            return;
		}
		
		if (editCheck.edit_barcode_yn == null) {
			editCheck.edit_barcode_yn = "N";

			//-----------------------------------------------------------
			// Yes/No Dialog
			//-----------------------------------------------------------
			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
			GlobalData.getInstance().setGlobalAlertDialog(true);
			
			//mBarcodeSoundPlay.play(BarcodeSoundPlay.sound_notify);
			String message = "장치ID '" + getEditBarcodeInfo().getBarcode() + "'\n\r(" + getEditBarcodeInfo().getBarcodeName() + ")를\n\r" +
		            "장치ID '" + deviceBarcode.getBarcode() + "'\n\r(" + deviceBarcode.getBarcodeName() + ")\n\r(으)로 수정하시겠습니까?";
	    	
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
	            	
	            	// 선택한 바코드를 스캔한 바코드로 대체한다.
	            	editCheck.edit_barcode_yn = "Y";
	            	editTransferTreeNode(editCheck);
	            	return;
	            }
	        });
	        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	GlobalData.getInstance().setGlobalAlertDialog(false);
	            	
	            	// 수정모드 초기화한다.
	            	editCheck.edit_barcode_yn = "N";
	            	cancelEditMode();
	            	return;
	            }
	        });
	        AlertDialog dialog = builder.create();
	        dialog.show();
	        return;
		}
		
		//-----------------------------------------------------------
		// 스캔한 설비의 수정전 상위바코드를 Set한다.
        //-----------------------------------------------------------
		deviceBarcode.setuFacCd(getEditBarcodeInfo().getuFacCd());
		deviceBarcode.setServerUFacCd(getEditBarcodeInfo().getServerUFacCd());
		
		TreeNodeInfo<Long> oldTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(mEditTreeKey);
		if (oldTreeNode != null) {
			
			// 수정전 바코드의 하위바코드를 조회하여 하위바코드의 New 상위바코로 Set한다.
			if (oldTreeNode.isWithChildren()) {
				List<Long> childKeys = getBarcodeTreeAdapter().getSubTreeList(oldTreeNode.getId());
				for (Long childKey : childKeys) {
					BarcodeListInfo childBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(childKey);
					if (childBarcodeInfo != null) {
						childBarcodeInfo.setuFacCd(deviceBarcode.getBarcode());
						childBarcodeInfo.setDeviceId(deviceBarcode.getBarcode());
						//getBarcodeTreeAdapter().setBarcodeListInfo(childKey, childBarcodeInfo);
					}
				}
			}
			
			getBarcodeTreeAdapter().setBarcodeListInfo(oldTreeNode.getId(), deviceBarcode);
			getBarcodeTreeAdapter().refresh();
			
			//-------------------------------------------------------
			// 장치바코드 수정일때는 기존 장치바코드를 삭제한다.
			mTransferDeviceMaps.remove(getEditBarcodeInfo().getDeviceId());
			
			//-------------------------------------------------------
			// 신규로 장치바코드 추가시 
    		// 운영중인 하위설비 목록을 조회하여 Maps에 저장한다.
    		// 전송시에 하위설비 유무 체크로직에서 사용한다.
    		getDeviceBelowFacUpdate(deviceBarcode.getDeviceId());
			
			//-----------------------------------------------------------
			// TreeView의 바닥 Summary 보여준다.
			//-----------------------------------------------------------
			showSummaryCount();
			
			//-----------------------------------------------------------
	        // 화면 초기 Setting후 변경 여부 Flag
	        //-----------------------------------------------------------
	    	GlobalData.getInstance().setChangeFlag(true);
	        GlobalData.getInstance().setSendAvailFlag(true);
		}
		
		//-----------------------------------------------------------
    	// 수정모드를 다시 초기화 한다.
    	//-----------------------------------------------------------
		cancelEditMode();
	}
    
    /**
     * 인계, 인수, 시설등록 대상정보 조회.
     * 
     * @param tempBarcodeListInfos  바코드인포정보 리스트
     */
    private void addTreeNodeFirstLocDeviceFacInfos(List<BarcodeListInfo> tempBarcodeInfos) {

    	List<BarcodeListInfo> barcodeInfos = new ArrayList<BarcodeListInfo>();

    	BarcodeListInfo locBarcode = new BarcodeListInfo(mJobGubun, "L", mThisLocCodeInfo.getLocCd(), mThisLocCodeInfo.getLocName());
    	barcodeInfos.add(locBarcode);
    	
    	for (BarcodeListInfo tempBarcodeInfo : tempBarcodeInfos) {
    		
            //---------------------------------------------------------
            // 시설등록일때만 운용조직 여부 = "Y"
            //---------------------------------------------------------
            if (mJobGubun.equals("시설등록")) {
            	tempBarcodeInfo.setCheckOrgYn("Y");
            }
    		
    		barcodeInfos.add(tempBarcodeInfo);
    		
    		//-------------------------------------------------------
    		// 최초 인계, 인수, 시설등록 조회시 장치바코드 정보는 따로 저장한다.
    		// 전송시에 삭제 전문 추가해야 되므로..
    		if (tempBarcodeInfo.getPartType().equals("D")) {
    			mTransferFirstDeviceMaps.put(tempBarcodeInfo.getDeviceId(), tempBarcodeInfo.getWbsNo());
    		}
    	}
    	
    	getBarcodeTreeAdapter().addItems(barcodeInfos);

    	// 무조건 1번째 장치바코드 Node로 이동한다.
    	if (mTransferFirstDeviceMaps.size() > 0) {
    		
    		String selectedDeviceId = "";
    		List<String> keylist = new ArrayList<String> (mTransferFirstDeviceMaps.keySet());
    		for (int i=keylist.size()-1; i>=0; i--) {
    			String deviceKey = keylist.get(i);
    			
    			// 신규로 장치바코드 추가시 
        		// 운영중인 하위설비 목록을 조회하여 Maps에 저장한다.
        		// 전송시에 하위설비 유무 체크로직에서 사용한다.
        		getDeviceBelowFacUpdate(deviceKey);
        		// 마지막 장치ID를 선택한다.
        		if (i==0) {
        			selectedDeviceId = deviceKey;
        		}
			}

    		// 선택한 장치ID 정보를 조회한다.
    		BarcodeListInfo lastDeviceIdInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedDeviceId);

			String deviceInfo = lastDeviceIdInfo.getItemCode() + "/" + lastDeviceIdInfo.getItemName();
    		mDeviceIdText.setText(lastDeviceIdInfo.getBarcode());
    		mDeviceIdInfoText.setText(deviceInfo);
 
        	// 최종바코드로
    		getBarcodeTreeAdapter().changeSelected(getBarcodeTreeAdapter().getItemId(getBarcodeTreeAdapter().getCount()-1));
    		getBarcodeTreeView().setSelection(getBarcodeTreeAdapter().getCount()-1);  // 최종 바코드로 이동
    		BarcodeListInfo lastBarcodeInfo = barcodeInfos.get(barcodeInfos.size()-1);
    		choiceBarcodeDataDisplay(lastBarcodeInfo);
    	}
    	
    	if (mJobGubun.equals("시설등록")) {
    		//-------------------------------------------------------
            // 시설등록은 불러와 졌다는 건 전송 했다는 뜻으로 등록확정 버튼 enable
            //-------------------------------------------------------
    		mArgumentConfirmButton.setEnabled(true);
    	}
    	
    	mScannerHelper.focusEditText(mFacCdText);
    	
    	//-----------------------------------------------------------
        // TreeView의 바닥 Summary 보여준다.
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
		}
    }

    /**
     * 장치ID의 하위 설비들 조회.
     *   (나중 전송시에 체크하기 위해서 장치바코드 조회시에 전역변수에 저장해 놓는다.)
     * 
     * @param deviceId
     */
	private void getDeviceBelowFacUpdate(String deviceId) {
		DeviceBelowFacUpdateInTask task = new DeviceBelowFacUpdateInTask(deviceId);
		task.execute((Void) null);
	}
	
	public class DeviceBelowFacUpdateInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _DeviceId = "";
		private List<String> _FacCds = null;
				
		public DeviceBelowFacUpdateInTask(String deviceId) {
			_DeviceId = deviceId;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				_FacCds = barcodehttp.getDeviceIdBelowFacListToBarcodeListInfos(_DeviceId);
				
				if (_FacCds == null) {
					throw new ErpBarcodeException(-1, "장치바코드 운영중인 하위설비 조회 결과가 없습니다. ");
				}
				
    		} catch (ErpBarcodeException e) {
    			Log.i(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (result) {
				Log.i(TAG, "DeviceBelowFacUpdateInTask   조회 결과 _DeviceId==>"+_FacCds.size() + "건");
				mTransferDeviceMaps.put(_DeviceId, _FacCds);
			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
	
    /**
     * 음영지역 위치바코드 작업.
     * @param barcode
     */
    private void getOfflineLocBarcodeData(String barcode) {
    	mLocNameText.setText(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	
    	mThisLocCodeInfo = new LocBarcodeInfo();
    	mThisLocCodeInfo.setLocCd(barcode);
    	mThisLocCodeInfo.setLocName(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	addTreeNodeLocBarcode(mThisLocCodeInfo);
    	
    	//-------------------------------------------------------------
		// 장치ID로 Cursor Focus이동한다.
		//-------------------------------------------------------------
		mScannerHelper.focusEditText(mDeviceIdText);
		
		//-------------------------------------------------------------
		// 위치바코드 조회되면 정보 수정으로 본다.
		//-------------------------------------------------------------
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
    	
    	DeviceBarcodeInfo deviceInfo = new DeviceBarcodeInfo();
    	deviceInfo.setDeviceId(barcode);
    	deviceInfo.setDeviceName(ErpBarcodeMessage.OFFLINE_MESSAGE);
    	
    	addTreeNodeDeviceBarcode(deviceInfo);
    	
    	//-------------------------------------------------------------
		// 설비바코드로 Cursor Focus이동한다.
		//-------------------------------------------------------------
		mScannerHelper.focusEditText(mFacCdText);
	    
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
            	
            	addBarcodeInfosTreeView(pdaItems);
        		
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
    	public String wbsNo = "";
    	public String docNo = "";
    	public String deviceId = "";
    	public String deviceId_belowFac_Yn = null;
    }
    
    /**
     * 바코드 작업내용 전송하기..
     */
    private void executeSendCheck(final SendCheck sendCheck) {
    	
    	//-----------------------------------------------------------
        // 화면 초기 Setting후 변경 여부 Flag
    	// 변경없으면 자료 전송하지 않는다.
        //-----------------------------------------------------------
    	if (getBarcodeTreeAdapter().getCount() < 3) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}
    	if (!GlobalData.getInstance().isSendAvailFlag()
    			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}
    	
		if (sendCheck.locBarcodeInfo==null || sendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mLocCdText);
			return;
		}

    	if (mJobGubun.equals("인계") || mJobGubun.equals("인수")) {
            if (sendCheck.wbsNo.trim().length() < 10) {
                GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS 번호가 없습니다."));
                return;
            }
        }

    	if (getBarcodeTreeAdapter().getCount() < 2) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
    		return;
    	}
    	
    	if (getBarcodeTreeAdapter().getCount() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
    		return;
    	}

    	// 아래에서 장치바코드별 하위 설비를 비교하기 위해서 선언한 Map변수임.
    	Map<Long, BarcodeListInfo> checkDeviceBarcodeMaps = new HashMap<Long, BarcodeListInfo>();
    	
    	final Map<Long, BarcodeListInfo> checkBarcodeMaps = getBarcodeTreeAdapter().getAllItems();
    	for (Long treeKey : checkBarcodeMaps.keySet()) {
    		BarcodeListInfo checkBarcodeInfo = checkBarcodeMaps.get(treeKey);
    		
    		if (!checkBarcodeInfo.getPartType().equals("L") && !checkBarcodeInfo.getPartType().equals("D") ) {
    			if (checkBarcodeInfo.getBarcode().length() < 16) {
                	GlobalData.getInstance().showMessageDialog(
                			new ErpBarcodeException(-1, "'" + checkBarcodeInfo.getBarcode() + "'는 처리할 수 없는\n설비바코드 입니다."));
        			return;
                }
    		}
    		
    		// 장치바코드  TreeNode만 Map에 대입한다.
    		if (checkBarcodeInfo.getPartType().equals("D")) {
    			checkDeviceBarcodeMaps.put(treeKey, checkBarcodeInfo);
    		}
    	}
    	
    	// 장치바코드의 운영중인 하위바코드 유효성 체크..
    	if (sendCheck.deviceId_belowFac_Yn == null) {
    		sendCheck.deviceId_belowFac_Yn = "N";
    		for (Long checkKey : checkDeviceBarcodeMaps.keySet()) {
        		BarcodeListInfo checkBarcodeInfo = checkDeviceBarcodeMaps.get(checkKey);
        		
        		// 장치아이디의 상태가 운용일 때 위치바코드 11자리가 틀리면........................
        		final String checkDeviceLocCd = 
        				(checkBarcodeInfo.getLocCd().length() < 11 ? "" : checkBarcodeInfo.getLocCd().substring(0, 11));
        		
        		if (checkBarcodeInfo.getDeviceStatusName().equals("운용")) {
        			if (!mThisLocCodeInfo.getLocCd().substring(0, 11).equals(checkDeviceLocCd)) {
        			
        				// 장치바코드의 하위 TreeNode를 조회하기 위해서 변수에 보관한다.
        				final Long belowTreeKey = checkKey;
	        			final String belowDeviceId = checkBarcodeInfo.getDeviceId();
	        			
	        			//-----------------------------------------------------------
	        			// Yes/No Dialog
	        			//-----------------------------------------------------------
	        			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
	        			GlobalData.getInstance().setGlobalAlertDialog(true);
	        			
	        			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
						String message = "장치ID('" + belowDeviceId + "') 는\n\r다른 위치('" + checkDeviceLocCd + 
								"')에서\n\r운용 중인 장치ID 입니다.\n\rSET 이동인 경우만 처리 가능합니다.\n\r진행하시겠습니까?";
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

	                        	Log.i(TAG, "mTransferFirstDeviceMaps Record Count==>" + mTransferFirstDeviceMaps.size());
	                        	Log.i(TAG, "mTransferDeviceMaps Record Count==>" + mTransferDeviceMaps.size());
	                        	
	                        	// 장치바코드 하위 설비 중 운용이면서 유닛 제외 리스트 가져와서
	                        	// TreeView의 장치바코드 하위 설비들을 비교하여 바코드가 있는지 체크하여 없으면 오류처리.
	                        	
	                        	// 장치바코드에 해당하는 자식 TreeNode 만 Loop처리하여 비교한다.
	                        	//List<Long> deviceChilds = getBarcodeTreeAdapter().getManager().getChildren(belowTreeKey);
	                        	List<Long> deviceChilds = getBarcodeTreeAdapter().getSubTreeList(belowTreeKey);
	                        	List<String> facCds = mTransferDeviceMaps.get(belowDeviceId);
                    			if (facCds != null && facCds.size()>0) {
                    				boolean existsFlagInDeviceId = false;
                    				for (String facCd : facCds) {
                    					for (Long deviceKey : deviceChilds) {
        	                        		BarcodeListInfo treeBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(deviceKey);
        	                        		if (facCd.equals(treeBarcodeInfo.getBarcode())) {
                            					existsFlagInDeviceId = true;
                            					break;
                            				}
        	                        	}
                        			}
                    				if (!existsFlagInDeviceId) {
    	                        		GlobalData.getInstance().showMessageDialog(
    	                        				new ErpBarcodeException(-1, "스캔하신\n\r'건물위치정보(" + mThisLocCodeInfo.getLocCd().substring(0, 11) + ")' 와\n\r장치ID(운용상태)의\n\r'건물위치정보(" + checkDeviceLocCd + ")' 가\n\r상이합니다.\n\r장치ID를 확인하시기 바랍니다."));
    	                        		return;
                    				}
                    			}

                    			sendCheck.deviceId_belowFac_Yn = "Y";
	                        	executeSendCheck(sendCheck);
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
    	
		if (mJobGubun.equals("인수")) {
	    	for (Long checkKey : checkBarcodeMaps.keySet()) {
	    		BarcodeListInfo checkBarcodeInfo = checkBarcodeMaps.get(checkKey);
    			// 장치바코드 하위 설비바코드가 있어야 되고. 스캔한게 있는지 체크한다.
    			if (checkBarcodeInfo.getBarcode().equals(checkBarcodeInfo.getDeviceId())) {
    				
    				int deviceScanCount = 0;
    				//List<Long> deviceChilds = getBarcodeTreeAdapter().getManager().getChildren(checkKey);
    				List<Long> deviceChilds = getBarcodeTreeAdapter().getSubTreeList(checkKey);
    				for (Long deviceKey : deviceChilds) {
    					BarcodeListInfo deviceBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(deviceKey);
    					if (deviceBarcodeInfo != null && !deviceBarcodeInfo.getScanValue().equals("0")) {
    						deviceScanCount++;
    					}
    				}
    				
    				if (deviceScanCount == 0) {
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "장치바코드 '" + checkBarcodeInfo.getDeviceId() + "'\n\r하위의 설비를 하나 이상\n\r스캔하셔야 합니다."));
    					getBarcodeTreeAdapter().changeSelected(checkKey);
                        return;
    				}
    			}
    		}
    	}
    	Log.i(TAG, "executeSendCheck  Validataion Check  8...");
		//---------------------------------------------------------------------
		// 전송여부 최종 확인..
    	//---------------------------------------------------------------------
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
            	
            	execSendDataInTask(sendCheck);
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

    private void execSendDataInTask(final SendCheck sendCheck) {
    	new SendDataInTask(sendCheck, this).execute();
    }
    // 전송 비동기 테스크
    private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;
    	private int _SendCount = 0;
    	private SendCheck _SendCheck;
    	private OutputParameter _OutputParameter;
    	private Activity mActivity;
    	
    	public SendDataInTask(final SendCheck sendCheck, Activity activity) {
    		_SendCheck = sendCheck;
    		mActivity = activity;
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setBarcodeProgressVisibility(true);
            mSendButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			Log.i(TAG, "SendDataInTask Hearder Parameter  Start...");
			
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
			
    		try {
	        	if (mJobGubun.equals("인계")) {
					jsonParam.put("WORKID", "0002");
	        		jsonParam.put("PRCID", "0510");
	        		jsonParam.put("POSID", _SendCheck.wbsNo);

	        	} else if (mJobGubun.equals("인수")) {
	        		jsonParam.put("WORKID", "0003");
	        		jsonParam.put("PRCID", "0520");
	        		jsonParam.put("POSID", _SendCheck.wbsNo);
	        		
	        	} else if (mJobGubun.equals("시설등록")) {
	        		jsonParam.put("WORKID", "0003");
	        		jsonParam.put("PRCID", "0525");
	        	}
            	//-------------------------------------------------------------
				// 작업위도,경도 정보도 헤더에 같이 전송한다.
				//-------------------------------------------------------------
	        	// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
				//-------------------------------------------------------------
	        	jsonParam.put("LOEVM", "");

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
			
			Log.i(TAG, "SendDataInTask Body Device Record Count==>" + mTransferFirstDeviceMaps.size());
			
			//-------------------------------------------------------
			// 기존 불러온 장치바코드 삭제 flag 3으로 추가
			//-------------------------------------------------------
			if (mJobGubun.equals("인계") || mJobGubun.equals("시설등록")) {
				for (String sendDeviceIdKey : mTransferFirstDeviceMaps.keySet()) {
					
					// 최초 조회한 WBS번호를 가져온다.
					String deviceWbsNo = mTransferFirstDeviceMaps.get(sendDeviceIdKey);
					
					JSONObject jsonSubParam = new JSONObject();
	    	        try {
                    	jsonSubParam.put("DFLAG", "3");
                    	jsonSubParam.put("LOCCODE", _SendCheck.locBarcodeInfo.getLocCd());
                    	jsonSubParam.put("POSID", deviceWbsNo);   // 위치바코드 스캔해서 선택한 wbsNo가 아니고 장치아이디의 wbsNO가 되어야 함 
                    	jsonSubParam.put("DEVICEID", sendDeviceIdKey);

                    	jsonSubParamList.put(jsonSubParam);
	    	        } catch (JSONException e1) {
	    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
	        			return false;
	    	        } catch (Exception e) {
	    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
	        			return false;
	    	        }
				}
			}
			
    		_SendCount = 0;
    		// 전송시만 getAllNextNodeList() 사용한다.
    		final List<Long> sendKeys = getBarcodeTreeAdapter().getManager().getAllNextNodeList();
    		Log.i(TAG, "SendDataInTask Body Record Count==>" + sendKeys.size());
    		Log.i(TAG, "SendDataInTask Body TreeView Count==>" + getBarcodeTreeAdapter().getCount());
			//SortedSet<Long> itemKeys = new TreeSet<Long>(sendBarcodeMaps.keySet());
            for (Long sendKey : sendKeys) {
            	JSONObject jsonSubParam = new JSONObject();
    	        try {
    	        	BarcodeListInfo sendBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(sendKey);
    	        	
    	        	Log.i(TAG, "SendDataInTask Body  getBarcode==>" + sendBarcodeInfo.getBarcode());
    	        	Log.i(TAG, "SendDataInTask Body  getuFacCd==>" + sendBarcodeInfo.getuFacCd());
    	        	Log.i(TAG, "SendDataInTask Body  getDeviceId==>" + sendBarcodeInfo.getDeviceId());
    	        	

    	        	if (sendBarcodeInfo.getPartType().equals("L") || sendBarcodeInfo.getPartType().equals("D")) {
    	        		continue;
    	        	}

    	        	// 인수는 스캔한 정보만 전송한다.
	    	        if (mJobGubun.equals("인수") && sendBarcodeInfo.getScanValue().equals("0")) {
	    	        	continue;
	    	        }
    	        	
	    	        String partTypeCode = "";
	    	        if (sendBarcodeInfo.getPartType().equals("E")) {
	    	        	partTypeCode = "99";
	    	        } else {
	    	        	partTypeCode = SuportLogic.getPartTypeCode(sendBarcodeInfo.getPartType());
	    	        }

	    	        String parentPartType = "";
	    	        BarcodeListInfo parentBarcodeInfo = getBarcodeTreeAdapter().getParentBarcodeListInfo(sendKey);
	    	        if (parentBarcodeInfo != null) {
    	        		parentPartType = parentBarcodeInfo.getPartType();
    	        	}
	    	        Log.i(TAG, "SendDataInTask Body  parentPartType==>" + parentPartType);
	    	        
                	jsonSubParam.put("DFLAG", "1");
                	jsonSubParam.put("LOCCODE", _SendCheck.locBarcodeInfo.getLocCd());
                	jsonSubParam.put("DEVICEID", sendBarcodeInfo.getDeviceId());
                	jsonSubParam.put("PARTTYPE", partTypeCode);
                	jsonSubParam.put("DEVTYPE", sendBarcodeInfo.getDevType());

                	if (parentPartType.equals("L") || parentPartType.equals("D"))  {
                	//if (sendBarcodeInfo.getPartType().equals("L") || sendBarcodeInfo.getPartType().equals("D"))  {
                        // 장치아이디에 붙는 바코드는 상위바코드 없게 한다.
                		jsonSubParam.put("SHELF", "");   // 상위바코드
                		jsonSubParam.put("UNIT", sendBarcodeInfo.getBarcode());  // 스캔바코드
                    } else {
                    	jsonSubParam.put("SHELF", sendBarcodeInfo.getuFacCd());
                		jsonSubParam.put("UNIT", sendBarcodeInfo.getBarcode());
                    }
                    
                	// 시설등록은 wbs번호도 넘긴다.
                    if (mJobGubun.equals("시설등록")) {
                		jsonSubParam.put("POSID", sendBarcodeInfo.getWbsNo());
                    }
    	        } catch (JSONException e1) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e1.getMessage());
        			return false;
    	        } catch (Exception e) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
        			return false;
    	        }
    	        
    	        jsonSubParamList.put(jsonSubParam);
    	        
    	        _SendCount++;  // 전송건수.
            } // for end sendBarcodeMaps.size()
            
            Log.i(TAG, "SendDataInTask SendHttpController "+mJobGubun+" Start...");

            try {
        		SendHttpController sendhttp = new SendHttpController();
            	if (mJobGubun.equals("인계")) {
            		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_TRANSFERSCAN, 
            				jsonParamList, jsonSubParamList);
            		
	            } else if (mJobGubun.equals("인수")) {
            		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ARGUMENTSCAN,
            				jsonParamList, jsonSubParamList);
	            	
	            } else if (mJobGubun.equals("시설등록")) {
            		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_INSTCONFSCAN,
            				jsonParamList, jsonSubParamList);
                }
            	
            	if (_OutputParameter == null) {
            		throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송중 오류가 발생했습니다." );
            	}
            } catch (ErpBarcodeException e) {
				Log.i(TAG, e.getErrMessage());
				_ErpBarException = e;
				return false;
			}

			return true;
		}
		
		// 인계,인수,시설등록 전송 시
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			
			if (result) {
				mSendButton.setEnabled(true);

				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				//GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));

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
						
		            	//-----------------------------------------------------------
				        // 전송시 초기화 / 확정버튼 활성화 한다.
				        //-----------------------------------------------------------
						if (mJobGubun.equals("인수") || mJobGubun.equals("시설등록")) {
							enabledArgumentConfirmButton(false);
						} else {
							initScreen();
						}
		            	return;
		            }
		        });
		        AlertDialog dialog = builder.create();
		        dialog.show();
		        return;
		    	
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
		}
    }
    
    /** 
     * 재스캔요청 전송
     */
    private void executeRescanRequestSendCheck(final SendCheck sendCheck) {
    	Log.i(TAG, "executeRescanRequestSendCheck  Validataion Check  1...");
    	// todo - 설비를 불러왔는지 체크
    	if (getBarcodeTreeAdapter().getAllItems().size()<1) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
    		return;
    	}
    	
    	execSendRequestRescanInTask(sendCheck);
    	
/*    	
    	Log.i(TAG, "executeSendCheck  Validataion Check  8...");
		//---------------------------------------------------------------------
		// 전송여부 최종 확인..
    	//---------------------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
    	String message = "전송하시겠습니까?"; 
    	
				GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
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
            	
            	execSendRequestRescanInTask(sendCheck);
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
*/
    }
    
    private void execSendRequestRescanInTask(SendCheck sendCheck) {
    	if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);

    	new SendRequestRescanInTask(sendCheck).execute();
    }
    
    // 전송 비동기 테스크
    private class SendRequestRescanInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;
    	private SendCheck _SendCheck;
    	private OutputParameter _OutputParameter;
    	
    	public SendRequestRescanInTask(SendCheck sendCheck) {
    		_SendCheck = sendCheck;
    	}
    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setBarcodeProgressVisibility(true);
            mSendButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			Log.i(TAG, "SendRequestRescanInTask Hearder Parameter  Start...");
			
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
			
    		try {
				jsonParam.put("LOCCODE", _SendCheck.locBarcodeInfo.getLocCd());
	    		jsonParam.put("POSID", _SendCheck.wbsNo);
	    		jsonParam.put("DOCNO", mDocNo);
	    		jsonParam.put("DEVICEID", _SendCheck.deviceId);
    		} catch (JSONException e) {
    			_ErpBarException = new ErpBarcodeException(-1,"헤더정보 생성중 오류가 발생했습니다. "+e.getMessage());
    			return false;
			}
    		JSONArray jsonParamList = new JSONArray();
    		jsonParamList.put(jsonParam);
    		
    		
    		//-------------------------------------------------------
			// 작업된 바코드 바디정보 생성.
			//-------------------------------------------------------
    		JSONArray jsonSubParamList = new JSONArray();
            try {
        		SendHttpController sendhttp = new SendHttpController();
           		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ARGUMENTSCAN_SENDMAIL, jsonParamList, jsonSubParamList);
            	
            	if (_OutputParameter == null) {
            		throw new ErpBarcodeException(-1, "'" + mJobGubun + "' 정보 전송중 오류가 발생했습니다." );
            	}
            } catch (ErpBarcodeException e) {
				Log.i(TAG, e.getErrMessage());
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

                String returnMessageFromSAP = _OutputParameter.getOutMessage();
                if (returnMessageFromSAP.isEmpty()) returnMessageFromSAP = "정상전송 되었습니다.";

                String message = "# 전송건수 : 1건\n\n" 
							+ _OutputParameter.getStatus() + "-" + returnMessageFromSAP;
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				initScreen();
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
		}
    }
    // -- 재스캔 요청 전송

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
			if (requestCode == ACTION_SEARCHWBSCHECKACTIVITY) {
				String wbsNo = data.getExtras().getString(SearchWbsCheckActivity.OUTPUT_WBS_NO);
				Log.i(TAG, "ACTION_SEARCHWBSCHECKACTIVITY   wbsNo==>"+wbsNo);

				setJobWorkWbsNo(wbsNo);
			}
		} else {
			if (requestCode == ACTION_SEARCHWBSCHECKACTIVITY) {

	    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "WBS 번호를 선택하세요."));
	    		
	    		// 인계, 인수대당 정보가 없으면 위치바코드를 TreeNode에 추가한다.
        		// 장치, 설비, Tree관련 초기화 한다.
            	initScreen("device");
            	initScreen("fac");
            	initScreen("tree");
            	
        		mScannerHelper.focusEditText(mLocCdText);
			}
		}
    }
}
