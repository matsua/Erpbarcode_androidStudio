package com.ktds.erpbarcode.survey;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.BaseHttpController;
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
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.BpIItemQuery;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.treeview.InMemoryTreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.SearchLocCheckActivity;
import com.ktds.erpbarcode.infosearch.SelectFacDetailActivity;
import com.ktds.erpbarcode.infosearch.SelectOrgCodeActivity;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.ktds.erpbarcode.management.AddrInfoActivity;
import com.ktds.erpbarcode.management.model.SendHttpController;
import com.ktds.erpbarcode.survey.model.SurveyHttpController;

public class SpotCheckActivity extends Activity {
	
	private static final String TAG = "SpotCheckActivity";
	private static final int ACTION_SEARCHLOCACTIVITY = 1;
	private static final int ACTION_SELECTORGCODEACTIVITY = 2;

	private ScannerConnectHelper mScannerHelper;

	private EditText mOrgCodeText;
	private Button mOrgCodeButton;
	
	private EditText mLocCdText;
	private Button mLocCdButton;
	private EditText mLocNameText;
	//private EditText mGPSLocDataText;
	
	private LinearLayout mDeviceInputbar;
	private EditText mDeviceIdText;
	private TextView mOprSysCdText;
	private EditText mDeviceIdInfoText;
	private EditText mFacCdText;
	private TextView mPartTypeText;
	private CheckBox chkFacCDMatch, chkUFacCdMatch, chkOrgMatch, chkLocMatch, chkDevMatch;
	private Button mInitButton, mCancelScanButton, mSaveButton, mSendButton;
	private RelativeLayout mBarcodeProgress;
	private TextView mDBCountText;
	private TextView mScanCountText;
	private TextView mPercentText;
	
	// 작업바코드 ListView
	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mBarcodeTreeView;
	private BarcodeTreeAdapter mBarcodeTreeAdapter;
	
	private LocBarcodeInfo mThisLocCodeInfo = null;
	// 장비바코드로 위치바코드 정보 조회.
	private FindSpotCheckGetLocInTask mFindSpotCheckGetLocInTask;
	// 장치바코드 정보 조회.
	private DeviceBarcodeInfo mThisDeviceBarcodeInfo = null;
	// 현장점검 serial 번호 가져오기
	private String mSpotCheckSerial = "";
	private FindSpotCheckSerialInTask mFindSpotCheckSerialInTask;
	
	private List<String> gbicList;
	private FindGbicDataInTask mFindGbicDataInTask;
	// 자재마스터 정보 조회.
	private BpIItemQuery bpIItemQuery;
	
	private String mJobGubun;
	
	private int DB_TCount = 0;
	
	private Button mAddInfo;
	
	private Boolean isHiddenList = false;
	private Boolean isCheck = false;
	private String editOrgCode;
	private String editOrgName;

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

        bpIItemQuery = new BpIItemQuery(getApplicationContext());
        
        setMenuLayout();
        setContentView(R.layout.survey_spotcheck_activity);
        setLayout();
        setTreeView(savedInstanceState);
        setFieldVisibility();
        
        initScreen("org");
        initScreen("all");
        
        //-----------------------------------------------------------
        // 현장점검 serial번호 가져오기 : 음영지역일때 제외 :
        //-----------------------------------------------------------
        if (!SessionUserData.getInstance().isOffline()) {
        	if (isBarcodeProgressVisibility()) return;
    		setBarcodeProgressVisibility(true);
    		
            getSpotCheckSerial();
        }else{
        	//-----------------------------------------------------------
            // 단계별 작업을 여기서 처리한다.
            //-----------------------------------------------------------
            GlobalData.getInstance().initJobActionManager();
            if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
            	new Handler().postDelayed(
            			new Runnable() {
    		        		public void run() {
    		        			jobNextExecutors();  // 작업아이템 1번째부터 진행한다.
    		        		}
    		        	}, 1000);
            	return;
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

    private void setLayout() {
        // 운용조직
        mOrgCodeText = (EditText) findViewById(R.id.spotcheck_organization_orgCode);
    	
        // 조직코드 선택        
        mOrgCodeButton = (Button) findViewById(R.id.spotcheck_organization_search_button);        
        mOrgCodeButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showSelectOrgCodeActivity();
					}
				});

        // 장치바코드
        mDeviceInputbar = (LinearLayout) findViewById(R.id.spotcheck_device_inputbar);
        mDeviceIdText = (EditText) findViewById(R.id.spotcheck_device_deviceId);
        mDeviceIdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	Log.i(TAG, "mDeviceIdText.setOnTouchListener 장치바코드 Focused");
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
							changeDeviceId(barcode.trim());
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

        mOprSysCdText = (TextView) findViewById(R.id.spotcheck_device_oprSysCd);
        mDeviceIdInfoText = (EditText) findViewById(R.id.spotcheck_deviceInfo_deviceInfo);
    	
    	
    	// 위치코드        
        mLocCdText = (EditText) findViewById(R.id.spotcheck_location_locCd);
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
        mLocCdButton = (Button) findViewById(R.id.spotcheck_location_search_button);        
        mLocCdButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String deviceId = mDeviceIdText.getText().toString().trim();
						if (deviceId.isEmpty()) {
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
							mScannerHelper.focusEditText(mDeviceIdText);
							return;
						}
						
						showSearchLocCheckActivity(deviceId);
					}
				});
        // 위치코드명        
        mLocNameText = (EditText) findViewById(R.id.spotcheck_locInfo_locName);
        
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

        // GPS 위치데이타        
        //mGPSLocDataText = (TextView) findViewById(R.id.spotcheck_gps_currentGPS);

        // 바코드
        mFacCdText = (EditText) findViewById(R.id.spotcheck_fac_facCd);
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
						Log.i(TAG, "설비바코드 onTextChanged Start..  barcode==>" + barcode);
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							Log.i(TAG, "설비바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							changeFacCd(barcode.trim()); // 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
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
                	changeFacCd(barcode);
                    return true;
                }
                return false;
            }
        });

        mPartTypeText = (TextView) findViewById(R.id.spotcheck_fac_partType);
        
        // 현장점검 체크박스
        chkFacCDMatch = (CheckBox) findViewById(R.id.spotcheck_chk_FacCDMatch);
        chkUFacCdMatch = (CheckBox) findViewById(R.id.spotcheck_chk_UFacCdMatch);
        chkOrgMatch = (CheckBox) findViewById(R.id.spotcheck_chk_OrgMatch);
        chkLocMatch = (CheckBox) findViewById(R.id.spotcheck_chk_LocMatch);
        chkDevMatch = (CheckBox) findViewById(R.id.spotcheck_chk_DevMatch);
        
        // 버튼 ----------------------------------------------------------------------------------
        // 초기화        
        mInitButton = (Button) findViewById(R.id.spotcheck_crud_init);        
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

        // 삭제        
        mCancelScanButton = (Button) findViewById(R.id.spotcheck_crud_cancelScan);        
        mCancelScanButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						cancelScanData();
					}
				});

        // 저장        
        mSaveButton = (Button) findViewById(R.id.spotcheck_crud_save);        
        mSaveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveWorkData();
					}
				});

        // 전송        
        mSendButton = (Button) findViewById(R.id.spotcheck_crud_send);        
        mSendButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						SendCheck sendCheck = new SendCheck();
				    	if (mThisLocCodeInfo == null) {
				    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
				    	} else {
				    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
				    	}
				    	sendCheck.deviceId = mDeviceIdText.getText().toString().trim();
						
						executeSendCheck(sendCheck);
					}
				});
        
        // 바코드스캔시 조회때만 사용.
        mBarcodeProgress = (RelativeLayout)  findViewById(R.id.spotcheck_barcodeProgress);
    }

    @SuppressWarnings("unchecked")
	private void setTreeView(Bundle savedInstanceState) {
    	if (savedInstanceState == null) {
			mTreeManager = new InMemoryTreeStateManager<Long>();
        } else {
        	mTreeManager = (TreeStateManager<Long>) savedInstanceState.getSerializable("treeManager");
            if (mTreeManager == null) {
            	mTreeManager = new InMemoryTreeStateManager<Long>();
            }
            //newCollapsible = savedInstanceState.getBoolean("collapsible");
        }
		
		mBarcodeTreeAdapter = new BarcodeTreeAdapter(getApplicationContext(), mTreeManager);
		
		mBarcodeTreeView = (TreeViewList) findViewById(R.id.spotcheck_treeView);
		mBarcodeTreeView.setAdapter(mBarcodeTreeAdapter);
		mBarcodeTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "mBarcodeTreeView onItemClick   position==>" + position);
				
				//Long selectedKey = mBarcodeTreeAdapter.getTreeId(position);
				mBarcodeTreeAdapter.changeSelected(id);
				
				BarcodeListInfo model = mBarcodeTreeAdapter.getBarcodeListInfo(id);
				if (model != null) {
					choiceBarcodeDataDisplay(model);
				}
			}
		});
		
		mBarcodeTreeView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				mBarcodeTreeAdapter.changeSelected(id);
				
				BarcodeListInfo model = mBarcodeTreeAdapter.getBarcodeListInfo(id);
				if (model != null) {
					choiceBarcodeDataDisplay(model);
					
					Intent intent = new Intent(getApplicationContext(), SelectFacDetailActivity.class);
					intent.putExtra(SelectFacDetailActivity.INPUT_FAC_CD, model.getBarcode());
			        startActivity(intent);
				}
		        
				return true;
			}
		});
		
        mDBCountText = (TextView) findViewById(R.id.spotcheck_totalBar_DBCount);  
        mScanCountText = (TextView) findViewById(R.id.spotcheck_totalBar_ScanCount);  
        mPercentText = (TextView) findViewById(R.id.spotcheck_totalBar_Percent);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
        	return super.onOptionsItemSelected(item);
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

	private void changeFlagYesNoDialog() {
		// ---------------------------------------------------------------------
		// 전송여부 최종 확인..
		// ---------------------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		String message = "수정한 자료가 존재합니다.\n\r저장하지 않고 종료하시겠습니까?";

		//mBarcodeSoundPlay.play(BarcodeSoundPlay.sound_notify);
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

	@Override
	protected void onResume() {
		super.onResume();
		
		bpIItemQuery.open();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		bpIItemQuery.close();
	}
	
	@Override
	protected void onDestroy() {
		//-----------------------------------------------------------
		// Activity종료시 작업ID는 초기화한다.
		//-----------------------------------------------------------
		
        if (GlobalData.getInstance().getJobActionManager() != null) {
        	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
        }
		
		super.onDestroy();
	}

	private void setFieldVisibility() {
		
		//-----------------------------------------------------------
		// 장치바코드BAR 
		//-----------------------------------------------------------
		if (mJobGubun.equals("현장점검(창고/실)")) {
			mLocCdButton.setVisibility(View.GONE);
    		// 장치바코드, 장치바코드INFO
    		mDeviceInputbar.setVisibility(View.GONE);
        	((LinearLayout) findViewById(R.id.spotcheck_deviceInfo_inputbar)).setVisibility(View.GONE);
        	chkUFacCdMatch.setEnabled(false);
        	chkUFacCdMatch.setTextColor(getResources().getColor(R.color.style_light_grey));
        	chkDevMatch.setEnabled(false);
        	chkDevMatch.setTextColor(getResources().getColor(R.color.style_light_grey));
    	}
	}

    private void initScreen() {
    	initScreen("all");
    }
    
    private void initScreen(String step) {
    	if (step.equals("org")) {
    		// 운용조직명을 Setting한다.
        	String orgInfo = "";
        	editOrgCode = "";
        	editOrgName = "";
            if (SessionUserData.getInstance().isAuthenticated()) {
            	orgInfo = SessionUserData.getInstance().getOrgId() + "/"
            			+ SessionUserData.getInstance().getOrgName();
            	
            	editOrgCode = SessionUserData.getInstance().getOrgId();
            	editOrgName = SessionUserData.getInstance().getOrgName();
            }
            
        	mOrgCodeText.setText(orgInfo);
    	}
    	if (step.equals("all") || step.equals("base")) {
    		GlobalData.getInstance().setGlobalProgress(false);
    		GlobalData.getInstance().setGlobalAlertDialog(false);
    		GlobalData.getInstance().setChangeFlag(false);
            GlobalData.getInstance().setSendAvailFlag(false);
    	}
    	if (step.equals("all") || step.equals("loc")) {
    		// 입력 값 초기화
    		mThisLocCodeInfo = null;
        	mLocCdText.setText("");
        	mLocNameText.setText("");
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
    	}
    	if (step.equals("all") || step.equals("spot")) {
    		// 현장점검 체크박스
    		chkFacCDMatch.setChecked(false);
    		chkUFacCdMatch.setChecked(false);
    		chkOrgMatch.setChecked(false);
    		chkLocMatch.setChecked(false);
    		chkDevMatch.setChecked(false);
    	}
    	if (step.equals("all") || step.equals("crudbar")) {
    		mInitButton.setEnabled(true);
    		mCancelScanButton.setEnabled(true);
    		mSendButton.setEnabled(true);
    	}
    	if (step.equals("all") || step.equals("tree")) {
    		setBarcodeProgressVisibility(false);
    		mBarcodeTreeAdapter.itemClear();
    		
    		// 카운트 초기화
        	showDBCount();
        	showScanCount();
    	}
    	if (step.equals("all") || step.equals("scanfocus")) {
	    	// 초기에는 장치바코드로 Focus 위치한다.
	    	if (mJobGubun.equals("현장점검(창고/실)")) {
	    		mScannerHelper.focusEditText(mLocCdText);
	    	} else {
	    		mScannerHelper.focusEditText(mDeviceIdText);
	    	}
    	}
    }
    
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
    
    private void showSelectOrgCodeActivity() {
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	
    	if (SessionUserData.getInstance().isOffline()){
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "음영지역작업' 중에는\n조직을 선택할 수 없습니다."));
            return;
        }
    	
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SelectOrgCodeActivity.class);
		startActivityForResult(intent, ACTION_SELECTORGCODEACTIVITY);
    }
    
    private void showSearchLocCheckActivity(String deviceId) {
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SearchLocCheckActivity.class);
		intent.putExtra("p_deviceId", deviceId);
		startActivityForResult(intent, ACTION_SEARCHLOCACTIVITY);
    }
    
    public void choiceBarcodeDataDisplay(BarcodeListInfo barcodeInfo) {
    	String facCd = "";
    	String partType = "";
    	if (barcodeInfo!=null) {
    		facCd = barcodeInfo.getBarcode();
    		partType = barcodeInfo.getPartType();
        	setCheckBoxState(barcodeInfo);    	
    	}
    	mFacCdText.setText(facCd);
    	mPartTypeText.setText(partType);
    }
    
    // 장치바코드
    public void changeDeviceId(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineDeviceBarcodeData(barcode);
    		
    	} else {
    		getDeviceBarcodeData(barcode);
    	}
    }
    
    // 위치바코드
    public void changeLocCd(String barcode) {
    	
    	String deviceId = mDeviceIdText.getText().toString().trim();
    	if (mJobGubun.equals("현장점검(베이)") && deviceId.isEmpty()) {
    		initScreen("loc");
    		initScreen("fac");
    		
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mDeviceIdText);
			return;
    	}

    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineLocBarcodeData(barcode);
    	} else {
    		getLocBarcodeData(barcode);
    	}
    }

    // 설비바코드
    private void changeFacCd(String barcode) {
		if (mBarcodeProgress.getVisibility() == View.VISIBLE) return;
		
		if (mThisLocCodeInfo==null || mThisLocCodeInfo.getLocCd().isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mLocCdText);
			return;
		}
        
        barcode = barcode.toUpperCase();
        
        String facCd = mFacCdText.getText().toString().trim();
    	if (facCd.isEmpty()) {
    		mFacCdText.setText(barcode);
    	}

    	// 음영지역 작업.
    	if (SessionUserData.getInstance().isOffline()) {
    		getOfflineProductBarcodeData(barcode);
    		
    	} else {
        	// 기존 설비바코드 있는지 체크하여
        	// 없으면 추가하고, 있으면 상태값 변경한다.
        	Long thisBarcodeKey = mBarcodeTreeAdapter.getBarcodeKey(barcode);
    		Log.i(TAG, "doChkScan   바코드 있는지 여부==>" + barcode+"|"+thisBarcodeKey);
    		if (thisBarcodeKey != null) {
    			// 스캔한 바코드로  Position Selected 한다.
    			mBarcodeTreeAdapter.changeSelected(thisBarcodeKey);
    			
    			BarcodeListInfo barcodeInfo = mBarcodeTreeAdapter.getBarcodeListInfo(thisBarcodeKey);
    			
    			AgoCheck agoCheck = new AgoCheck();
        		agoCheck.barcodeItem = barcodeInfo;
    			
    			agoBarcodeInfoListView(agoCheck);
    		} else {
    			getSAPBarcodeInfos("", "", barcode);
    		}
    	}
    }
    
    private void setCheckBoxState(BarcodeListInfo barcodeInfo) {
    	String checkValue = barcodeInfo.getCheckValue();
    	String[] checkArray = checkValue.split("/");

    	chkFacCDMatch.setChecked(checkArray[0].equals("Y"));
        chkUFacCdMatch.setChecked(checkArray[1].equals("Y"));
        chkOrgMatch.setChecked(checkArray[2].equals("Y"));
        chkLocMatch.setChecked(checkArray[3].equals("Y"));
        chkDevMatch.setChecked(checkArray[4].equals("Y"));
        
        // 현장점검(창고/실)은 무조건 unCheck - request by 정진우
        if (mJobGubun.equals("현장점검(창고/실)")) {
            chkUFacCdMatch.setChecked(false);
            chkDevMatch.setChecked(false);
        }
    }
    
    // 스캔취소
    private void cancelScanData() {
    	// 마지막 스캔된 정보를 초기화한다.
		Long selectedKey = mBarcodeTreeAdapter.getSelected();
		if (selectedKey == null) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다."));
			return;
		}
		
		String workBarcode = mBarcodeTreeAdapter.getBarcodeListInfo(selectedKey).getBarcode();
		
		BarcodeListInfo model = new BarcodeListInfo();
		
		mBarcodeTreeAdapter.initItemValue();
	
		selectedKey = mBarcodeTreeAdapter.getSelected();
		if (selectedKey != null) {
			model = mBarcodeTreeAdapter.getBarcodeListInfo(selectedKey);
			choiceBarcodeDataDisplay(model);
		}
		showDBCount();
		showScanCount();

		//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 True
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
						JobActionStepManager.JOBTYPE_CANCEL_SCAN, "barcode", workBarcode);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }

    /**
     * 작업내용 저장.
     */
    private void saveWorkData() {
    	
    	if(GlobalData.getInstance().getJobActionManager() == null){
    		System.out.println("mJobGubun ::: GlobalData.getInstance().getJobActionManager() :: NULL: " );
    	}
    	GlobalData.getInstance().setJobGubun(mJobGubun);
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
    		GlobalData.getInstance().showMessageDialog(
        			new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "작업 진행중이므로 저장할수 없습니다."));
    		return;
    	}
    	
    	System.out.println("mJobGubun ::: SpotCheckActivity :: " + GlobalData.getInstance().getJobGubun());

		String locCd = mLocCdText.getText().toString().trim();
		String locName = mLocNameText.getText().toString().trim();
		String deviceId = mDeviceIdText.getText().toString().trim();
		int recordCount = mBarcodeTreeAdapter.getCount();
		String offlineYn = (SessionUserData.getInstance().isOffline() ? "Y" : "N");

    	//-------------------------------------------------------------
    	// 단계별 작업을 저장한다.
    	//-------------------------------------------------------------
    	try {
    		GlobalData.getInstance().getJobActionManager().saveWorkData(locCd, locName, "", deviceId, recordCount, offlineYn);
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
			int recordCount = mBarcodeTreeAdapter.getCount();
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
		
		//작업내용 재현 데이터 받는 동안 기다리다가 작업이 끝나면 다음 작업을 계속한다. matsua : TEST
		while(!isBarcodeProgressVisibility()) {
			System.out.println("while start!!");
			WorkItem workItem = new WorkItem();
			workItem = GlobalData.getInstance().getJobActionManager().startStepItem(position, new JobHandler(position));
			if (workItem == null) return;
			startStepWorkItem(workItem);
			break;
		}
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
    		mLocCdText.setText(locCd);
    		changeLocCd(locCd);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_DEVICE)) {
    		String deviceId = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "deviceId");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_DEVICE==>"+deviceId);
    		mDeviceIdText.setText(deviceId);
    		changeDeviceId(deviceId);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_SEARCH_TASK)) {
     		String name = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "getDevice_SAPBarcodeInfos");
     		Log.i(TAG, "startStepWorkItem JOBTYPE_SEARCH_TASK==>"+name);
     		Log.i(TAG, "startStepWorkItem JOBTYPE_SEARCH_TASK==>"+mThisLocCodeInfo.getLocCd().toString());
     		if (name.equals("getDevice_SAPBarcodeInfos")) {
     			if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && mThisLocCodeInfo.getLocCd().endsWith("0000")) {
        			String orgCode = "";
        			String thisOrgInfo = mOrgCodeText.getText().toString().trim();
        			if (thisOrgInfo.indexOf("/") > 0) {
        				orgCode = thisOrgInfo.split("/")[0];
        			}
        			getDevice_SAPBarcodeInfos(orgCode, mThisLocCodeInfo.getLocCd(), "", "");
        			
        		} else if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {
        			getDevice_SAPBarcodeInfos("", mThisLocCodeInfo.getLocCd(), "", "");
        			
        		} else {
        			String deviceId = mDeviceIdText.getText().toString().trim();
        			getDevice_SAPBarcodeInfos("", mThisLocCodeInfo.getLocCd(), deviceId, "");
        		}
     		}
     	
     	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC)) {
    		String facCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC==>"+facCd);
    		mFacCdText.setText(facCd);
    		changeFacCd(facCd);
    	
    	} else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CANCEL_SCAN)) {
    		String barcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "barcode");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_CANCEL_SCAN==>"+barcode);
    		mBarcodeTreeAdapter.changeSelected(barcode);
    		mFacCdText.setText(barcode);
    		cancelScanData();
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

		//-----------------------------------------------------------
		// 화면 초기화...
		//-----------------------------------------------------------
		mThisLocCodeInfo = null;
		mLocNameText.setText("");
		initScreen("fac");
		initScreen("spot");
		//initScreen("crudbar");
		initScreen("tree");
		isHiddenList = false;
		isCheck = false;
		
		LocBarcodeService locbarcodeService = new LocBarcodeService(getApplicationContext(), new LocBarcodeHandler(this));
		locbarcodeService.search(barcode);
	}
	
	/**
	 * LocBarcodeInfo정보 조회 결과 Handler
	 */
	private class LocBarcodeHandler extends Handler {
		private Activity _Activity;
		
		public LocBarcodeHandler(Activity activity) {
			_Activity = activity;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case LocBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	mThisLocCodeInfo = LocBarcodeConvert.jsonStringToLocBarcodeInfo(findedMessage);
            	handlerSuccessResult();
    			
                break;
                
            case LocBarcodeService.STATE_NOT_FOUND:
            	mScannerHelper.focusEditText(mFacCdText);

            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 위치코드입니다."));
            	break;

            case LocBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
            	
                break;
            }
        }
        
        private void handlerSuccessResult() {
    		// 현장점검(창고/실) 가상창고 운용조직 체크
    		if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {

    			String orgInfo = mOrgCodeText.getText().toString().trim();
        		String orgCode = orgInfo.split("/")[0];
        		String orgName = orgInfo.split("/")[1];
        		
                if (!orgCode.equals(mThisLocCodeInfo.getOrgId())) {
            		
                	//---------------------------------------------------------------------
            		// Yes/No 확인 Dialog
                	//---------------------------------------------------------------------
                	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
            		GlobalData.getInstance().setGlobalAlertDialog(true);
                	
        			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
                	String message = "운용조직\n\r(" + orgName + ") 정보와\n\r스캔한 가상창고의 조직\n\r(" + mThisLocCodeInfo.getOrgName() + ") 정보가\n\r상이합니다.\n\r진행하시겠습니까?";
            		final Builder builder = new AlertDialog.Builder(_Activity);
            		builder.setIcon(android.R.drawable.ic_menu_info_details);
            		builder.setTitle("알림");
            		TextView msgText = new TextView(_Activity);
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
                			successLocBarcodeProcess();
            			}
            		});
            		builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) {
            				//-----------------------------------------------------------
            			    // Dialog화면 호출시 Open/Close 처리하는 Flag
            			    //-----------------------------------------------------------
            				GlobalData.getInstance().setGlobalAlertDialog(false);
            				
            				initScreen("loc");
            				initScreen("fac");
            				initScreen("spot");
            				//initScreen("crudbar");
            				initScreen("tree");
    						mScannerHelper.focusEditText(mLocCdText);
    						
    						
    						//-------------------------------------------------------------
    		            	// 단계별 작업을 여기서 처리한다.
    		            	//-------------------------------------------------------------
    						if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
    							GlobalData.getInstance().getJobActionManager().getJobStepManager().errorHandler();
    						}
    		            	
    						return;
            			}
            		});
            		AlertDialog dialog = builder.create();
            		dialog.show();
            		return;
                }                
    		} else {
    			successLocBarcodeProcess();
    		}
//    		if (mJobGubun.equals("현장점검(창고/실)")) 
//    			successLocBarcodeProcess();
        }
	}

    private void successLocBarcodeProcess() {
		//-----------------------------------------------------------
		// 위치바코드 정보 항목에 대입..
		//-----------------------------------------------------------
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
		
		//-----------------------------------------------------------
		// 위치바코드, 장치ID에 해당하는 SAP설비바코드들 조회.
		//-----------------------------------------------------------
		String deviceId = mDeviceIdText.getText().toString().trim();
    	if (deviceId.isEmpty()) {
    		if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && mThisLocCodeInfo.getLocCd().endsWith("0000")) {
    			String orgCode = "";
    			String thisOrgInfo = mOrgCodeText.getText().toString().trim();
    			if (thisOrgInfo.indexOf("/") > 0) {
    				orgCode = thisOrgInfo.split("/")[0];
    			}
    			getDevice_SAPBarcodeInfos(orgCode, mThisLocCodeInfo.getLocCd(), "", "");
    			
    		} else if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {
    			getDevice_SAPBarcodeInfos("", mThisLocCodeInfo.getLocCd(), "", "");
    			
    		} else {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'베이' 점검은 '현장점검(베이)'\n\r메뉴를 사용하시기 바랍니다."));
    			mLocCdText.setText("");
    			mLocNameText.setText("");
    			return;
    		}

    	} else {
    		if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && mThisLocCodeInfo.getLocCd().endsWith("0000")) {
        		GlobalData.getInstance().showMessageDialog(
        				new ErpBarcodeException(-1, "'가상창고/실' 위치 점검은\n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
    			return;
    		} else if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'가상창고/실' 위치 점검은\n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
    			return;
    		}
    		
    		getDevice_SAPBarcodeInfos("", mThisLocCodeInfo.getLocCd(), deviceId, "");
    	}
    	
    	mScannerHelper.focusEditText(mFacCdText);
    	
    }

	/**
	 * 장치바코드 정보 조회.
	 */
	private void getDeviceBarcodeData(String deviceId) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		mThisDeviceBarcodeInfo = null;  // 꼭 전역장치바코드인포 변수를 초기화한다.
		mDeviceIdText.setText("");
		mOprSysCdText.setText("");
		mDeviceIdInfoText.setText("");
		
		// 현장점검(창고/실) 가상창고 운용조직 체크		
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
            case DeviceBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	mThisDeviceBarcodeInfo = DeviceBarcodeConvert.jsonStringToDeviceBarcodeInfo(findedMessage);

        		successDeviceBarcodeProcess();
        		
                break;
            case DeviceBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 장치바코드입니다."));
            	break;
            case DeviceBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
        
        private void successDeviceBarcodeProcess() {
			Log.i(TAG, "장치바코드 getDeviceId==>"+mThisDeviceBarcodeInfo.getDeviceId());
			Log.i(TAG, "장치바코드 getItemCode==>"+mThisDeviceBarcodeInfo.getItemCode());
			Log.i(TAG, "장치바코드 getItemName==>"+mThisDeviceBarcodeInfo.getItemName());
			
			String deviceInfo = mThisDeviceBarcodeInfo.getItemCode() + "/" + mThisDeviceBarcodeInfo.getItemName();
			mDeviceIdText.setText(mThisDeviceBarcodeInfo.getDeviceId());
			mDeviceIdInfoText.setText(deviceInfo);
		    mOprSysCdText.setText(mThisDeviceBarcodeInfo.getOperationSystemCode());

		    // 위치 주소 정보 조회 
	    	pLocCd_deviceId = mThisDeviceBarcodeInfo.getLocationCode();
			pLocNm_deviceId = mThisDeviceBarcodeInfo.getLocationShortName();
//			getLocAddInfo(pLocCd_deviceId, "장치바코드");
			
			System.out.println("mThisDeviceBarcodeInfo.getOperationSystemToken() ==> " + mThisDeviceBarcodeInfo.getOperationSystemToken());
			
		    //-----------------------------------------------------------
		    // wirelessFlag 무선장치바코드 Flag
			if (mThisDeviceBarcodeInfo.getOperationSystemToken().equals("04")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("08")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("09")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("10")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("99")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("69")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("79")
					|| mThisDeviceBarcodeInfo.getOperationSystemToken().equals("89")) {
				//getSpotCheckGetLoccodeData(mThisDeviceBarcodeInfo.getDeviceId());
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'가상창고/실' 위치 점검은 \n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
//				return;
				
				//-------------------------------------------------------------
		    	// 단계별 작업을 여기서 처리한다.
		    	//-------------------------------------------------------------
				if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
					GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
					return;
				} else {
					try {
						GlobalData.getInstance().getJobActionManager().setStepItem(
								JobActionStepManager.JOBTYPE_DEVICE, "deviceId", mThisDeviceBarcodeInfo.getDeviceId());
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
					return;
				} else {
					try {
						GlobalData.getInstance().getJobActionManager().setStepItem(
								JobActionStepManager.JOBTYPE_DEVICE, "deviceId", mThisDeviceBarcodeInfo.getDeviceId());
					} catch (ErpBarcodeException e) {
						e.printStackTrace();
					}
				}
				
				showSearchLocCheckActivity(mThisDeviceBarcodeInfo.getDeviceId());
			}
        }
    };
	
	/**
	 * 현장점검 장치아이디 하위 설비의 위치코드 리스트 가져오기
	 */
	public void getSpotCheckGetLoccodeData(String deviceId) {
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
			setBarcodeProgressVisibility(true);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				_LocBarcodeInfos = locationhttp.getSpotCheckGetLocDataByDeviceId(_DeviceId);
				
				if (_LocBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "장치바코드-위치코드 정보가 없습니다. ");
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
			setBarcodeProgressVisibility(false);
			mFindSpotCheckGetLocInTask = null;
			super.onPostExecute(result);
			
			if (result) {
                // 무선 장비아이디 스캔 구분 (PDA 소스에는 wirelessFlag)
				Log.i(TAG, "wirelessFlag getOperationSystemToken==>"+mThisDeviceBarcodeInfo.getOperationSystemToken());

				String locCd = "";
				String locName = "";
			
				for (LocBarcodeInfo locBarcodeInfo : _LocBarcodeInfos) {
					locCd = locBarcodeInfo.getLocCd();
					locName = locBarcodeInfo.getLocName();
					if (!locCd.isEmpty()) {
						break;
					}
				}
				Log.i(TAG, "wirelessFlag locCd==>"+locCd);
				Log.i(TAG, "wirelessFlag locName==>"+locName);
				mLocCdText.setText(locCd);
				mLocNameText.setText(locName);
				
				changeLocCd(locCd);
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
	
	/**
	 * 현장점검 serial번호 가져오기
	 */
	private void getSpotCheckSerial() {
		if (mFindSpotCheckSerialInTask == null) {
			mFindSpotCheckSerialInTask = new FindSpotCheckSerialInTask();
			mFindSpotCheckSerialInTask.execute((Void) null);
		}
	}
	public class FindSpotCheckSerialInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private JSONObject _JsonResult = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController spotcheckhttp = new SurveyHttpController();
				_JsonResult = spotcheckhttp.getSpotCheckGetSerial();
				
				if (_JsonResult == null) {
					throw new ErpBarcodeException(-1, "현장점검 serial번호 정보가 없습니다. ");
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
			mFindSpotCheckSerialInTask = null;
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			if (result) {
				try {
					mSpotCheckSerial = _JsonResult.getString("SERIAL");
				} catch (JSONException e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "현장점검 serial번호 정보 변환중 오류가 발생했습니다."));
				}
				
				if (!mSpotCheckSerial.equals("000000001")) {
//					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "본사 점검 기간입니다.\n\r점검 결과는 평가에 반영됩니다.\n\r신중히 진행하시기 바랍니다."));
					showMessageDialog("본사 점검 기간입니다.\n\r점검 결과는 평가에 반영됩니다.\n\r신중히 진행하시기 바랍니다.");
				}
				
				if (isBarcodeProgressVisibility()) return;
	    		setBarcodeProgressVisibility(true);
	    		
				getGbicDataList();
			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}
		
		@Override
		protected void onCancelled() {
			setBarcodeProgressVisibility(false);
			mFindSpotCheckSerialInTask = null;
			super.onCancelled();
		}
	}
	
	private void showMessageDialog(String msg){
			//-----------------------------------------------------------
			// Yes/No Dialog
			//-----------------------------------------------------------
			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
			GlobalData.getInstance().setGlobalAlertDialog(true);
			
			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);

			final Builder builder = new AlertDialog.Builder(this); 
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle("알림");
			TextView msgText = new TextView(this);
			msgText.setPadding(10, 30, 10, 30);
			msgText.setText(msg);
			msgText.setGravity(Gravity.CENTER);
			msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			msgText.setTextColor(Color.BLACK);
			builder.setView(msgText);
			builder.setCancelable(false);
			builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	GlobalData.getInstance().setGlobalAlertDialog(false);		        	
		        	
		        	//-----------------------------------------------------------
			        // 단계별 작업을 여기서 처리한다.
			        //-----------------------------------------------------------
			        GlobalData.getInstance().initJobActionManager();
			        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			        	new Handler().postDelayed(
			        			new Runnable() {
					        		public void run() {
					        			jobNextExecutors();  // 작업아이템 1번째부터 진행한다.
					        		}
					        	}, 1000);
			        	return;
			        }
		        }
		    });
		    
		    AlertDialog dialog = builder.create();
		    dialog.show();
	}
	
	private void getDevice_SAPBarcodeInfos(String orgCode, String locCode, String deviceId, String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
    	SAPBarcodeService sapBarcodeInfoService = new SAPBarcodeService(mDevice_SAPBarcodeInfoHandler);
    	sapBarcodeInfoService.search(orgCode, locCode, deviceId, barcode);
	}
	
	/**
	 * SAPBarcodeInfo 정보 조회 결과 Handler
	 */
    private final Handler mDevice_SAPBarcodeInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapBarcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);
            	List<BarcodeListInfo> barcodeListInfos = remakeList(sapBarcodeListInfos);
        		addBarcodeInfosListView(barcodeListInfos);
                break;
            case SAPBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 점검 대상 설비가 없습니다. "));
            	break;
            case SAPBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
        		
            	if (mJobGubun.contains("현장점검") && errorMessage.contains("1500건")){ 
            		isHiddenList = true;
            		locDataFacOverList();
            	}
            	
            	ErpBarcodeException erpbarException = ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
//  설비리스트 1500 개 이상일경우 페이지조회로 전체 데이터 내려오게 한다.  
    private void locDataFacOverList(){
    	
    }
    
    public List<BarcodeListInfo> remakeList(List<BarcodeListInfo> barcodeListInfos){
    	List<BarcodeListInfo> tempRemakeLists = new ArrayList<BarcodeListInfo>();
    	List<BarcodeListInfo> remakeLists = new ArrayList<BarcodeListInfo>();
    	tempRemakeLists = barcodeListInfos;
    	
    	for(int level = 1; level < 4; level++){
    		for(int index = 0; index < tempRemakeLists.size(); index++){
    			if(Integer.parseInt(tempRemakeLists.get(index).getLevel()) == level){
    				remakeLists.add(tempRemakeLists.get(index));
    			}
    		}
    	}
 
    	return remakeLists;
    }
    
    // 현장점검 대상 데이타 리스트에 표시
    private void addBarcodeInfosListView(List<BarcodeListInfo> barcodeListInfos) {

    	for (int i=barcodeListInfos.size()-1; i>=0; i--) {
			BarcodeListInfo newBarcodeInfo = barcodeListInfos.get(i);
			newBarcodeInfo.setCrudType("db");  // db.DB search
			
			Log.i(TAG, "addBarcodeInfosListView  getBarcode==>"+newBarcodeInfo.getBarcode());
			Log.i(TAG, "addBarcodeInfosListView  getBarcodeName==>"+newBarcodeInfo.getBarcodeName());

			// 불용확정 보이지 않게 - request by 김제민 2012.10.10
	        // 4. 변경 후 기능(개선 될 내용 기술) 
	        //    - 현장점검 메뉴에서 점검 대상 위치와 장치를 스캔했을때 "dummy, 사용중지, 
	        //      불용확정, 납품취소" 설비를 제외하고 점검 대상으로 호출 
			//    - request by 김희선 dr-no = DR-2013-13771 - 2013-03-27
			// -> 2014.10.21 : SAP에서 아래 설비는 더 이상 주지 않음
			/*
	        if (newBarcodeInfo.getFacStatus().equals("0260")            // 사용중지
	        		|| newBarcodeInfo.getFacStatus().equals("0240")     // 불용확정
	        		|| newBarcodeInfo.getFacStatus().equals("0021")) {  // 납품취소
	        	barcodeListInfos.remove(i);
	        	continue;
	        }
	        */
            
			// 실물 존재 가능성이 없는 상태값의 설비는 초록색으로 디폴트 표시하여 스캔한 것으로 간주 처리
	        // 인계작업취소,시설등록취소,인수거부,납품취소,탈장,고장,이동중,출고중,수리의뢰,수리완료송부,개조의뢰,개조완료송부
	        String checkValue = "N/N/N/N/N";
			String scanValue = "0";
			
			// 창고/실은 모든 자동스캔 로직 적용되지 않아야 함 : request by 정진우 2013.09.17
            // 현장점검(베이) 기능에서 자동스캔 적용 상태값 제외 -	인계작업취소,시설등록취소,인수거부,탈장,고장,철거확정,불용대기,불용요청 - request by 정진우 2013.09.24 
            // 현장점검(창고/실) 기능에서 자동스캔 적용 상태값 신규 정의 - request by 정진우 2013.09.24
    		if (newBarcodeInfo.getFacStatus().equals("0130")    // 수리의뢰
    				|| newBarcodeInfo.getFacStatus().equals("0140")   // 이동중
            		|| newBarcodeInfo.getFacStatus().equals("0160")   // 수리완료송부
            		|| newBarcodeInfo.getFacStatus().equals("0170")   // 개조의료
            		|| newBarcodeInfo.getFacStatus().equals("0171")   // 개조완료송부
    				|| newBarcodeInfo.getFacStatus().equals("0270"))  // 출고중
            {
                scanValue = "4";
                
            	//-----------------------------------------------------------
        	    // 화면 초기 Setting후 변경 여부 Flag를 True
        	    //-----------------------------------------------------------
        		GlobalData.getInstance().setChangeFlag(true);
                GlobalData.getInstance().setSendAvailFlag(true);
            }
    		
            // 베이점검이면 광모듈 스캔한 것 처럼 표시
//    		if (mJobGubun.equals("현장점검(베이)")) {
    		if(gbicList != null){
    			for (String _gbic_code : gbicList) {
    	    		if (_gbic_code.equals(newBarcodeInfo.getProductCode())) {
    	    			if(newBarcodeInfo.getFacStatus().equals("0060")){ // 운용중인 광모듈만 자동스캔 
	    	    			scanValue = "9";
	    	            	//-----------------------------------------------------------
	    	        	    // 화면 초기 Setting후 변경 여부 Flag를 True
	    	        	    //-----------------------------------------------------------
	    	        		GlobalData.getInstance().setChangeFlag(true);
	    	                GlobalData.getInstance().setSendAvailFlag(true);
    	    			}
    	    		}
    	    	}
    		}
    			
//    		}

			if (scanValue.equals("4") || scanValue.equals("9")) {
	            String matchZKOSTL = "N";
	            if (newBarcodeInfo.getOrgId().equals(editOrgCode)) {	
	            	matchZKOSTL = "Y";
	            }

	            checkValue = "Y/Y/" + matchZKOSTL + "/Y/Y";
	        }
			
			// 가상 쉘프
	        if (newBarcodeInfo.getProductCode().isEmpty() && newBarcodeInfo.getBarcode().startsWith("Z00")) {
	        	newBarcodeInfo.setProductName("가상쉘프");
	        	newBarcodeInfo.setPartType("S");
	        }
	        
	        Log.i(TAG, "getBarcodeInfoData  scanValue==>"+scanValue);
	        newBarcodeInfo.setScanValue(scanValue);
	        newBarcodeInfo.setCheckValue(checkValue);
	        
            barcodeListInfos.set(i, newBarcodeInfo);
		}
    	
    	mBarcodeTreeAdapter.addItems(barcodeListInfos);
    	
    	showDBCount();
    	showScanCount();

    	// 설비바코드로 바코드스케너 Focus 위치한다.
    	mScannerHelper.focusEditText(mFacCdText);
    	
    	//-------------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-------------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_SEARCH_TASK, "getDevice_SAPBarcodeInfos", "getDevice_SAPBarcodeInfos");
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /***************
	 * gbicData 조회.
	 */
	private void getGbicDataList() {
		if (mFindGbicDataInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindGbicDataInTask = new FindGbicDataInTask();
			mFindGbicDataInTask.execute((Void) null);
		}
	}
	
	public class FindGbicDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private List<String> _JsonResult = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_JsonResult = basehttp.getGbicList("");
				
				System.out.print("@@@@@@@@_JsonResult count:: " + _JsonResult.size());
				
				if (_JsonResult == null) {
					throw new ErpBarcodeException(-1,"gbic data 조회된 결과값이 없습니다.");
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
			setBarcodeProgressVisibility(false);
			mFindGbicDataInTask = null;
			super.onPostExecute(result);
			if (result) {
				gbicList = _JsonResult;
				//현장점검에 필한 기본적인 조회를 마친후 작업관리 실행
				//-----------------------------------------------------------
	            // 단계별 작업을 여기서 처리한다.
	            //-----------------------------------------------------------
	            GlobalData.getInstance().initJobActionManager();
	            if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
	            	new Handler().postDelayed(
	            			new Runnable() {
	    		        		public void run() {
	    		        			jobNextExecutors();  // 작업아이템 1번째부터 진행한다.
	    		        		}
	    		        	}, 1000);
	            	return;
	            }
			} 
			else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			mFindGbicDataInTask = null;
			super.onCancelled();
		}
	}
	
    private void getSAPBarcodeInfos(String locCd, String deviceId, String barcode) {
    	if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
    	SAPBarcodeService sapBarcodeService = new SAPBarcodeService(new SAPBarcodeInfoHandler());
    	sapBarcodeService.search(locCd, deviceId, barcode);
	}
    
    // 설비바코드 스캔시 SAP바코드 1건만 ListView에 추가한다.
    private class SAPBarcodeInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapBarcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

        		BarcodeListInfo barcodeInfo = sapBarcodeListInfos.get(0);
        		if(isHiddenList && barcodeInfo.getLocCd().equals(mLocCdText.getText().toString())){
        			isCheck = false;
        			barcodeInfo.setScanValue("0");     
        		}else{
        			barcodeInfo.setScanValue("5");// 5.설비마스터에 있는 설비 추가
        		}
        		barcodeInfo.setCrudType("add");    // add.형상에 추가
    			AgoCheck agoCheck = new AgoCheck();
        		agoCheck.barcodeItem = barcodeInfo;
        		agoBarcodeInfoListView(agoCheck);
                break;
            case SAPBarcodeService.STATE_NOT_FOUND:
            	// 만약 설비바코드가  SAPBarcodeInfo 조회시 없으면 자재마스터를 조회하여 추가한다.
            	String facCd = mFacCdText.getText().toString().trim();
            	getProductToBarcodeInfos(facCd);

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
    
    
    public class AgoCheck {
    	public String duplicate_check_yn = null;
    	public BarcodeListInfo barcodeItem = null;

    }
    
    private void agoBarcodeInfoListView(final AgoCheck agoCheck) {
        // 실물 존재 가능성이 없는 상태값의 설비는 초록색으로 디폴트 표시하여 스캔한 것으로 간주 처리
        // 인계작업취소,시설등록취소,인수거부,납품취소,탈장,고장,이동중,출고중,수리의뢰,수리완료송부,개조의뢰,개조완료송부
        String checkValue = "";
        Long thisBarcodeKey = mBarcodeTreeAdapter.getBarcodeKey(agoCheck.barcodeItem.getBarcode());
        
    	// 4.설비상태별 자동스캔, 5.설비마스터 존재하는 추가설비 여부, 6.설비마스터에 없는 설비 추가, 9.광모듈 자동스캔
        String scanValue = agoCheck.barcodeItem.getScanValue();
        
        Log.i(TAG, "getBarcodeInfoData  scanValue==>"+scanValue);
        
        // 리스트에 있는 설비 중 scalValue가 0, 4, 9가 아니면 중복 스캔입니다.
		if (thisBarcodeKey!=null && !scanValue.equals("0") && !scanValue.equals("4") && !scanValue.equals("9")) {
			if (agoCheck.duplicate_check_yn == null) {
				agoCheck.duplicate_check_yn = "N";
				
				//-----------------------------------------------------------
				// Yes/No Dialog
				//-----------------------------------------------------------
				// Modify by sesang 20190429 중복 스캔 메세지 박스 제거 
				if (GlobalData.getInstance().isGlobalAlertDialog()) return;
				//GlobalData.getInstance().setGlobalAlertDialog(true);
								
				GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_DUPLICATION);
				agoCheck.duplicate_check_yn = "Y";
            	agoBarcodeInfoListView(agoCheck);
            	return;
//				String message = "중복 스캔입니다.\n\r\n\r" + agoCheck.barcodeItem.getBarcode();
//	    		final Builder builder = new AlertDialog.Builder(this); 
//	    		builder.setIcon(android.R.drawable.ic_menu_info_details);
//	    		builder.setTitle("알림");
//	    		TextView msgText = new TextView(this);
//	    		msgText.setPadding(10, 30, 10, 30);
//	    		msgText.setText(message);
//	    		msgText.setGravity(Gravity.CENTER);
//	    		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
//	    		msgText.setTextColor(Color.BLACK);
//	    		builder.setView(msgText);
//	    		builder.setCancelable(false);
//	    		builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
//	                public void onClick(DialogInterface dialog, int which) {
//	                	GlobalData.getInstance().setGlobalAlertDialog(false);
//	            	
//	                	agoCheck.duplicate_check_yn = "Y";
//	                	agoBarcodeInfoListView(agoCheck);
//	                	return;
//	                }
//	            });
//	            AlertDialog dialog = builder.create();
//	            dialog.show();
//	            return;
            	// end            	
			}

		} else if (agoCheck.barcodeItem.getScanValue().equals("5") || agoCheck.barcodeItem.getScanValue().equals("6")) {
        	//-------------------------------------------------------
        	// 신규추가 형상일때 Sound Play한다.
        	//-------------------------------------------------------
        	GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_JUNG);
        }
        
        // ---- 스캔여부 처리. ----------------------------------------------
        // 최초 또는 설비상태에 따른 자동스캔 또는 광모듈 설비 자동스캔은 스캔하면 다시 1로 셋팅
        if (agoCheck.barcodeItem.getScanValue().isEmpty() 
        		|| agoCheck.barcodeItem.getScanValue().equals("0") 
        		|| agoCheck.barcodeItem.getScanValue().equals("4") 
        		|| agoCheck.barcodeItem.getScanValue().equals("9")) {
            scanValue = "1";
        }
        
		if (thisBarcodeKey != null) {
			BarcodeListInfo thisBarcodeInfo = mBarcodeTreeAdapter.getBarcodeListInfo(thisBarcodeKey);

            // 스캔한 위치바코드와 설비의 위치바코드가 동일할 때
            if (mThisLocCodeInfo.getLocCd().equals(agoCheck.barcodeItem.getLocCd())) {
                // 1. 가상창고이면 실물일치
                if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {
                    scanValue = "1";
                } else if (mThisLocCodeInfo.getLocCd().startsWith("VS") || mThisLocCodeInfo.getLocCd().endsWith("0000")) {
                    // 2. 실 위치일 경우 조직도 같아야 실물일치
                    if (agoCheck.barcodeItem.getOrgId().equals(editOrgCode)) {
                        scanValue = "1";
                    }
                }
            }

            // 실물일치
            if (thisBarcodeInfo.getBarcode().equals(agoCheck.barcodeItem.getBarcode()))
            	checkValue += "Y";
            else
            	checkValue += "N";
            
            // 상위일치
            if (!thisBarcodeInfo.getuFacCd().equals("") && thisBarcodeInfo.getuFacCd().equals(agoCheck.barcodeItem.getuFacCd() ))
            	checkValue += "/Y";
            else
            	checkValue += "/N";
            
            // 부서일치
            if (thisBarcodeInfo.getOrgId().equals(editOrgCode))
            	checkValue += "/Y";
            else
            	checkValue += "/N";

            // 위치일치
            if (agoCheck.barcodeItem.getLocCd().equals(mThisLocCodeInfo.getLocCd()))
            	checkValue += "/Y";
            else
            	checkValue += "/N";
            
            // 베이일때 장치일치
            if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && !mThisLocCodeInfo.getLocCd().endsWith("0000")
            		&& mThisDeviceBarcodeInfo.getDeviceId().equals(agoCheck.barcodeItem.getDeviceId())) {
            	checkValue += "/Y";
            } else {
            	checkValue += "/N";
            }
            
            agoCheck.barcodeItem.setScanValue(scanValue);
            agoCheck.barcodeItem.setCheckValue(checkValue);
            
            if(isHiddenList){
            	///////리스트 재조합(최상위일때 :상위바코드가 존재하지 않은때)/////////
            	if(agoCheck.barcodeItem.getHequnr().equals("")){
            		final List<Long> sendKeys = mBarcodeTreeAdapter.getManager().getAllNextNodeList();
            		for (Long sendKey : sendKeys) {
            			BarcodeListInfo hiddenBarcodeInfo = mBarcodeTreeAdapter.getBarcodeListInfo(sendKey);
            			
            			if(agoCheck.barcodeItem.getBarcode().equals(hiddenBarcodeInfo.getHequnr())){
            				Long key = mBarcodeTreeAdapter.getBarcodeKey(hiddenBarcodeInfo.getBarcode());
            				mBarcodeTreeAdapter.removeNode(key);
            				thisBarcodeKey = mBarcodeTreeAdapter.getBarcodeKey(agoCheck.barcodeItem.getBarcode());
            				mBarcodeTreeAdapter.addChildItem(thisBarcodeKey, hiddenBarcodeInfo);
            			}
            		}
            		thisBarcodeKey = mBarcodeTreeAdapter.getBarcodeKey(agoCheck.barcodeItem.getBarcode());
            	}
            }
            
            mBarcodeTreeAdapter.setBarcodeListInfo(thisBarcodeKey, agoCheck.barcodeItem);
            mBarcodeTreeAdapter.changeSelected(thisBarcodeKey);
            mBarcodeTreeAdapter.refresh();
            
            // 선택된 바코드로 커서 이동한다.
    		int barcodePosition = mBarcodeTreeAdapter.getKeyPosition(thisBarcodeKey);
			mBarcodeTreeView.setSelection(barcodePosition);  

		} else {
            // 실물일치
            checkValue += "N";

            // 상위일치
        	checkValue += "/N";

        	// 부서일치
            if (agoCheck.barcodeItem.getOrgId().equals(editOrgCode))
            	checkValue += "/Y";
            else
            	checkValue += "/N";

            // 위치일치
            if (agoCheck.barcodeItem.getLocCd().equals(mThisLocCodeInfo.getLocCd()))
            	checkValue += "/Y";
            else
            	checkValue += "/N";
            
            // 베이일때 장치일치
            if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && !mThisLocCodeInfo.getLocCd().endsWith("0000")
            		&& mThisDeviceBarcodeInfo.getDeviceId().equals(agoCheck.barcodeItem.getDeviceId())) {
            	checkValue += "/Y";
            } else {
            	checkValue += "/N";
            }
            
            Log.i(TAG, "getBarcodeInfoData  scanValue==>"+scanValue);
            agoCheck.barcodeItem.setScanValue(scanValue);
            agoCheck.barcodeItem.setCheckValue(checkValue);
            
            // TreeView에 추가한다.
            mBarcodeTreeAdapter.addItem(agoCheck.barcodeItem);
            Long itemKey = mBarcodeTreeAdapter.getBarcodeKey(agoCheck.barcodeItem.getBarcode());
        	if (itemKey != null) {
        		mBarcodeTreeAdapter.changeSelected(itemKey);
        		
        		// 선택된 바코드로 커서 이동한다.
        		int barcodePosition = mBarcodeTreeAdapter.getKeyPosition(itemKey);
    			mBarcodeTreeView.setSelection(barcodePosition);  
        	}
		}
        
        mPartTypeText.setText(agoCheck.barcodeItem.getPartType());
        setCheckBoxState(agoCheck.barcodeItem);
        
        // 스캔한  Total정보 다시 조회한다.
        showScanCount();
        
        //-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 True
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
						JobActionStepManager.JOBTYPE_FAC, "facCd", agoCheck.barcodeItem.getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
		
		if(isHiddenList && !isCheck){
			if(agoCheck.barcodeItem.getLocCd().equals(mLocCdText.getText().toString())){
				agoCheck.barcodeItem.setScanValue("0");
				isCheck = true;
				agoBarcodeInfoListView(agoCheck);
			}
		}
    }
    
    
    /**
     * 서버에서 자재정보를 조회하고, List<BarcodeListInfo>로 변경한다.
     * 
     * @param barcode
     */
    private void getProductToBarcodeInfos(String barcode) {
    	if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		PDABarcodeService pdaBarcodeService = 
				new PDABarcodeService(getApplicationContext(), new ProductToBarcodeInfoInfoHandler());
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO, barcode);
		} catch (ErpBarcodeException e) {
			setBarcodeProgressVisibility(false);
			GlobalData.getInstance().showMessageDialog(e);
			return;
		}
	}
    
    // 설비바코드 스캔시 자재정보는 1건만 ListView에 추가한다.
    private class ProductToBarcodeInfoInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> barcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);


        		BarcodeListInfo barcodeInfo = barcodeListInfos.get(0);
        		// 설비바코드 스캔값을 Set한다.
        		String facCd = mFacCdText.getText().toString().trim();
        		barcodeInfo.setBarcode(facCd);
        		barcodeInfo.setScanValue("6");    // 6.설비마스터에 없는 설비 추가
        		barcodeInfo.setCrudType("add");   // add.형상에 추가
        		
        		AgoCheck agoCheck = new AgoCheck();
        		agoCheck.barcodeItem = barcodeInfo;
        		
        		agoBarcodeInfoListView(agoCheck);
        		//mBarcodeSoundPlay.play(BarcodeSoundPlay.sound_notlocation);
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
    };
    
    private void showDBCount() {
    	int DB_RCount = mBarcodeTreeAdapter.getDBPartTypeCount("R");
    	int DB_SCount = mBarcodeTreeAdapter.getDBPartTypeCount("S");
    	int DB_UCount = mBarcodeTreeAdapter.getDBPartTypeCount("U");
    	int DB_ECount = mBarcodeTreeAdapter.getDBPartTypeCount("E");
    	DB_TCount = DB_RCount + DB_SCount + DB_UCount + DB_ECount;
    	String strDBCount = "DB:R(" + DB_RCount + ")S(" + DB_SCount + ")U(" + DB_UCount + ")E(" + DB_ECount + ")Total:" + DB_TCount + "건";
    	
		mDBCountText.setText(strDBCount);
		mScanCountText.setText("");
		mPercentText.setText("");
    }
    
    private void showScanCount() {
    	
    	int Scan_RCount = mBarcodeTreeAdapter.getScanPartTypeCount("R");
    	int Scan_SCount = mBarcodeTreeAdapter.getScanPartTypeCount("S");
    	int Scan_UCount = mBarcodeTreeAdapter.getScanPartTypeCount("U");
    	int Scan_ECount = mBarcodeTreeAdapter.getScanPartTypeCount("E");
    	int Scan_TCount = Scan_RCount + Scan_SCount + Scan_UCount + Scan_ECount;
    	String ScanCount = "스캔:R(" + Scan_RCount + ")S(" + Scan_SCount + ")U(" + Scan_UCount + ")E(" + Scan_ECount + ")Total:" + Scan_TCount + "건";
    	
    	int addCount = mBarcodeTreeAdapter.getAddCount();
    	int okCount = mBarcodeTreeAdapter.getOkCount();
    	
    	double percent = 0;
    	if (okCount > 0) {
    		percent = (1.0 * okCount / (DB_TCount + addCount)) * 100;
    	}
    	
    	if (percent < 50) mPercentText.setTextColor(Color.RED);
        else if (percent < 80) mPercentText.setTextColor(Color.rgb(128, 0, 128));  // Color.Purple;
        else if (percent < 90) mPercentText.setTextColor(Color.rgb(65, 105, 225)); // Color.RoyalBlue;
        else if (percent < 100) mPercentText.setTextColor(Color.BLUE);             // Color.Blue;
        else mPercentText.setTextColor(Color.GREEN);

    	//String percentString = "0.00%";
    	String percentString = "실물일치율:" + String.format("%.2f", percent) + "%";

		//mDBCountText.setText(DBCount);
		mScanCountText.setText(ScanCount);
		mPercentText.setText(percentString);
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
		// 위치바코드, 장치ID에 해당하는 SAP설비바코드들 조회.
		//-----------------------------------------------------------
		String deviceId = mDeviceIdText.getText().toString().trim();
    	if (deviceId.isEmpty()) {
    		if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && mThisLocCodeInfo.getLocCd().endsWith("0000")) {
    			
    		} else if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {

    		} else {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'베이' 점검은 '현장점검(베이)'\n\r메뉴를 사용하시기 바랍니다."));
    			mLocCdText.setText("");
    			mLocNameText.setText("");
    			return;
    		}

    	} else {
    		if (!mThisLocCodeInfo.getLocCd().startsWith("VS") && mThisLocCodeInfo.getLocCd().endsWith("0000")) {
        		GlobalData.getInstance().showMessageDialog(
        				new ErpBarcodeException(-1, "'가상창고/실' 위치 점검은\n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
    			return;
    		} else if (mThisLocCodeInfo.getLocCd().startsWith("VS")) {
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "'가상창고/실' 위치 점검은\n\r'현장점검(창고/실)'\n\r메뉴를 사용하시기 바랍니다."));
    			return;
    		}
    	}

    	//-------------------------------------------------------------
		// 설비바코드로 Cursor Focus이동한다.
		//-------------------------------------------------------------
		mScannerHelper.focusEditText(mFacCdText);
		
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

    	
    	//-------------------------------------------------------------
		// 위치바코드로 Cursor Focus이동한다.
		//-------------------------------------------------------------
		mScannerHelper.focusEditText(mLocCdText);
	    
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
    			new PDABarcodeService(getApplicationContext(), new OfflineFacToBarcodeInfoInfoHandler(barcode));

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
            	
            	BarcodeListInfo barcodeInfo = pdaItems.get(0);
            	
            	Long barcodeKey = mBarcodeTreeAdapter.getBarcodeKey(barcodeInfo.getBarcode());
            	if (barcodeKey != null) {
            		GlobalData.getInstance().showMessageDialog(
            				new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
                	return;
            	} else {
            		// TreeView에 추가한다.
                    mBarcodeTreeAdapter.addItem(barcodeInfo);
                    Long itemKey = mBarcodeTreeAdapter.getBarcodeKey(barcodeInfo.getBarcode());
                	if (itemKey != null) {
                		mBarcodeTreeAdapter.changeSelected(itemKey);
                		
                		// 선택된 바코드로 커서 이동한다.
                		int barcodePosition = mBarcodeTreeAdapter.getKeyPosition(itemKey);
            			mBarcodeTreeView.setSelection(barcodePosition);  
                	}
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
        						JobActionStepManager.JOBTYPE_FAC, "facCd", _barcode);
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

    
    /**
     *  전송시 유효성 체크 Class 
     */
    public class SendCheck {
    	public String orgCode = "";
    	public LocBarcodeInfo locBarcodeInfo = null;
    	public String deviceId = "";
    }

    /**
     *  전송시 유효성 체크...
     */
    private void executeSendCheck(final SendCheck sendCheck) {
    	
    	//-----------------------------------------------------------
        // 화면 초기 Setting후 변경 여부 Flag
    	// 변경없으면 자료 전송하지 않는다.
        //-----------------------------------------------------------
    	if (!GlobalData.getInstance().isSendAvailFlag() 
    			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}

		String orgInfo = mOrgCodeText.getText().toString().trim();
    	
    	if (orgInfo.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "운용조직을 선택하세요. "));
    		return;
    	};

		String orgCode = orgInfo.split("/")[0];
		String orgName = orgInfo.split("/")[1];
		sendCheck.orgCode = orgCode;
    	
    	if (sendCheck.locBarcodeInfo == null || sendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요. "));
    		mScannerHelper.focusEditText(mLocCdText);
    		return;
    	};
    	
    	// 베이일때만 장치ID를 비교한다.
    	if (!sendCheck.locBarcodeInfo.getLocCd().startsWith("VS") && !sendCheck.locBarcodeInfo.getLocCd().endsWith("0000")) {
    		if (sendCheck.deviceId.isEmpty()) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드를 스캔하세요. "));
        		mScannerHelper.focusEditText(mDeviceIdText);
        		return;
        	}
    	}
    	
    	if (mBarcodeTreeAdapter.getCount() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 바코드가 존재하지 않습니다. "));
    		return;
    	}
    	
    	if (mSpotCheckSerial.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "현장점검 serial번호 정보가 존재하지 않습니다. "));
    		return;
    	}
    	
    	//-----------------------------------------------------------
    	// Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
		String alertMessage = "점검한 운용조직은\n\r'" + orgName +"'\n\r입니다.\n\r\n\r전송하시겠습니까?";

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
    }
    
    /**
     * 현장점검 전송
     */
    private void execSendDataInTask(SendCheck sendCheck) {
    	if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		mSendButton.setEnabled(false);
    	new SendDataInTask(sendCheck).execute();
    }
    private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;
    	private SendCheck _SendCheck = null;
    	private int _SendCount;
    	private OutputParameter _OutputParameter;
		
    	public SendDataInTask(SendCheck sendCheck) {
    		_SendCheck = sendCheck;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.i(TAG, "SendDataInTask Hearder Parameter  Start...");
			
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
			try {
				jsonParam.put("WORKID", "0020");  
        		jsonParam.put("PRCID", "0800");
        		
        		String reqDate = String.format(SystemInfo.getNowDate().toString(), "yyyyMMdd");
        		jsonParam.put("REQ_DATE", reqDate);
				
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
			JSONArray jsonSubTempParamList = new JSONArray();
			
			_SendCount = 0;
			
			final List<Long> sendKeys = mBarcodeTreeAdapter.getManager().getAllNextNodeList();	// 트리뷰에 표시된 그대로 가져오기
			for (Long sendKey : sendKeys) {
				BarcodeListInfo sendBarcodeInfo = mBarcodeTreeAdapter.getBarcodeListInfo(sendKey);
            	
            	JSONObject jsonSubParam = new JSONObject();
            	JSONObject jsonSubTempParam = new JSONObject();
            	try {
            		String partTypeCode = SuportLogic.getPartTypeCode(sendBarcodeInfo.getPartType());    
            		
            		if(partTypeCode.equals("") || partTypeCode.equals("N/A")){
            			jsonSubTempParam.put("EXBARCODE", sendBarcodeInfo.getBarcode());
            			jsonSubTempParam.put("PARTTYPE", partTypeCode);
            			jsonSubTempParamList.put(jsonSubTempParam);
            		}
            		
            		// Tree Node Level을 구한다.
            		jsonSubParam.put("ZLEVEL", "1");
            		
            		if (sendBarcodeInfo.getPartType().equals("E")) {
            			jsonSubParam.put("DEVTYPE", "30");
                    } else {
                    	jsonSubParam.put("DEVTYPE", "40");
                    }
            		
            		jsonSubParam.put("PARTTYPE", partTypeCode);
            		jsonSubParam.put("EXBARCODE", sendBarcodeInfo.getBarcode());
            		jsonSubParam.put("ZPSTATU", sendBarcodeInfo.getFacStatus());
            		jsonSubParam.put("HEQUI", sendBarcodeInfo.getuFacCd());    // 상위바코드

            		jsonSubParam.put("DEVICEID", _SendCheck.deviceId);         // 검색조건의 장치바코드
            		jsonSubParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());  // 검색조건의 위치바코드
            		//-------------------------------------------------------------
    				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
    				//-------------------------------------------------------------
            		// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//    				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//    				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//    				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
    				//-------------------------------------------------------------
            		
            		jsonSubParam.put("ZKOSTL", _SendCheck.orgCode);           // 사용자가 선택한 운용조직 - 현장점검만 따로 - REQUEST BY 이승중 2012.09.11

                    String scanValue = sendBarcodeInfo.getScanValue();
                    
                    // 바코드 스캔된것만 보낸다.
                    if (mJobGubun.equals("현장점검(창고/실)")) {
                    	if (scanValue.equals("0")) continue;
                    }
                    
                    
                    // 설비마스터 존재 여부
                    String ZEQUIPCR = "Y";
                    if (scanValue.equals("5")) {
                        scanValue = "2";        // DB에 없는 데이타 추가로
                    } else if (scanValue.equals("6")) {
                        ZEQUIPCR = "N";         // DB에 없고 설비마스터 존재하지 않음
                        scanValue = "2";        // 추가로
                    } else if (scanValue.equals("4") || scanValue.equals("9")) {
                        // 설비상태 또는 광모듈이면 스캔한 것 처럼
                        scanValue = "1";
                    }

                        
                    // 스캔
                    jsonSubParam.put("SCAN", scanValue);

                    // 상태값
                    String checkValue = sendBarcodeInfo.getCheckValue();
                    jsonSubParam.put("ZEQUIPCB", checkValue.split("/")[0]);        // 실물
                    jsonSubParam.put("ZEQUIPCM", checkValue.split("/")[1]);        // 상위
                    jsonSubParam.put("ZEQUIPCF", checkValue.split("/")[2]);        // 조직
                    jsonSubParam.put("ZEQUIPCN", checkValue.split("/")[3]);        // 위치
                    jsonSubParam.put("ZEQUIPCU", checkValue.split("/")[4]);        // 장치

                    // 설비마스터존재여부
                    jsonSubParam.put("ZEQUIPCR", ZEQUIPCR);

                    // Serial NO
                    jsonSubParam.put("SUBDEVICEID", mSpotCheckSerial);
    				
        		} catch (JSONException e) {
        			_ErpBarException = new ErpBarcodeException(-1, "서브파라메터 대입중 오류가 발생했습니다. "+e.getMessage());
        			return false;
    			}
            	
            	jsonSubParamList.put(jsonSubParam);
            	
            	System.out.println("jsonSubTempParamList == " + jsonSubTempParamList.length());
            	
            	_SendCount++;
			}
			
			try {
				SendHttpController sendhttp = new SendHttpController();
				_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_SPOT_CHECK, 
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
			setBarcodeProgressVisibility(false);
			super.onPostExecute(result);
			mSendButton.setEnabled(true);
			
			if (result) {
				String message = "# 전송건수 : " + _SendCount + "건\n\n" + _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, message));
				
				//-----------------------------------------------------------
				// 단계별 작업을 여기서 처리한다.
				//-----------------------------------------------------------
				String sendMessage = _OutputParameter.getOutMessage();
				sendWorkResult(sendMessage);
				// Modify by sesang 20190620 전송 결과 저장 수정
				initScreen();
				// end sesang
				
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
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
    	// 현장점검(창고/실, 베이)로 스케너 Focus 지정한다.
		initScreen("scanfocus");
    	
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SEARCHLOCACTIVITY) {
				String locCd = data.getExtras().getString(SearchLocCheckActivity.RESULT_LOC_CODE);
				String locName = data.getExtras().getString(SearchLocCheckActivity.RESULT_LOC_NAME);
				
				Log.i(TAG, "ACTION_SEARCHLOCACTIVITY   locCd==>"+locCd);
				Log.i(TAG, "ACTION_SEARCHLOCACTIVITY   locName==>"+locName);
				
				mScannerHelper.focusEditText(mLocCdText);
				
				if (!locCd.isEmpty()) changeLocCd(locCd);
				
			} else if (requestCode == ACTION_SELECTORGCODEACTIVITY) {
				editOrgCode = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_CODE);
				editOrgName = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_NAME);
				String orgInfo = editOrgCode.trim()+"/"+editOrgName.trim();
				mOrgCodeText.setText(orgInfo);
				
				//-----------------------------------------------------------
				// 단계별 작업을 여기서 처리한다.
				//-----------------------------------------------------------
				if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
					try {
						GlobalData.getInstance().getJobActionManager().setStepItem(
								JobActionStepManager.JOBTYPE_ORG, "orgCode", orgInfo);
					} catch (ErpBarcodeException e) {
						e.printStackTrace();
					}
				}
				
				
				// 현장점검(창고/실, 베이)로 스케너 Focus 지정한다.
				initScreen("scanfocus");
			}
		} else {
			if (requestCode == ACTION_SEARCHLOCACTIVITY) {
				mScannerHelper.focusEditText(mLocCdText);
				
			} else if (requestCode == ACTION_SELECTORGCODEACTIVITY) {
				// 현장점검(창고/실, 베이)로 스케너 Focus 지정한다.
				initScreen("scanfocus");
			}
		}
    }
}
