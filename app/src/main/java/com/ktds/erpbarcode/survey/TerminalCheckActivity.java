package com.ktds.erpbarcode.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.treeview.InMemoryTreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.ktds.erpbarcode.management.model.SendHttpController;
import com.ktds.erpbarcode.survey.model.CheckTerminalInfo;
import com.ktds.erpbarcode.survey.model.ProductSurveyInfo;
import com.ktds.erpbarcode.survey.model.SurveyHttpController;

public class TerminalCheckActivity extends Activity {
	
	private static final String TAG = "RentCheckActivity";
	
	// 작업관리
	private String mJobGubun = "";
	
	// 바코드스케너 Helper
	private ScannerConnectHelper mScannerHelper;
	
	// 점검년도
	private Spinner mYearSpinner;
    private BasicSpinnerAdapter mYearSpinnerAdapter;
    // 점검월
 	private Spinner mMonthSpinner;
 	private BasicSpinnerAdapter mMonthSpinnerAdapter;
 	// 플랜트
  	private Spinner mPlantSpinner;
  	private BasicSpinnerAdapter mPlantSpinnerAdapter;
  	// 저장위치
  	private Spinner mSLSpinner;
  	private BasicSpinnerAdapter mSLSpinnerAdapter;
  	
  	// 실사문서
  	private LinearLayout mIblnrInputBar;
  	private Spinner mIblnrSpinner;
  	private BasicSpinnerAdapter mIblnrSpinnerAdapter;
  	
  	// 자재
  	private LinearLayout mMaterialsInputBar;
  	private EditText mMaterialsText;
  	
  	// 단말코드
  	private LinearLayout mTerminalInputBar;
  	private EditText mTerminalCodeText;
  	// 초기화, 삭제, 저장, 전송버튼
  	private Button mInitButton, mDeleteButton, mSaveButton, mSendButton, mSearchButton, mCloseButton;
	
  	
  	// 상품단말실사 ListView
  	private HorizontalScrollView mOneScrollView;
	private ListView mProductSurveyListView;
	private ProductSurveyListAdapter mProductSurveyListAdapter;
	private ProductSurveyInfo thisProductSurveyInfo;
  	
  	// 상품단말실사 Check ListView
  	private HorizontalScrollView mTwoScrollView;
	private ListView mCheckTerminalListView;
	private CheckTerminalListAdapter mCheckTerminalListAdapter;
  	
	// 임대단말실사 TreeView
  	private HorizontalScrollView mThreeScrollView;
	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mBarcodeTreeView;
	private BarcodeTreeAdapter mBarcodeTreeAdapter;
	
	private TextView mListFooterTotalCount;
	
	// Progress
	private RelativeLayout mBarcodeProgress;
	
	// 조회 Task들
	private FindPlantInTask mFindPlantInTask;
	private FindSLInTask mFindSLInTask;
	private FindIBLNRInTask mFindIBLNRInTask;
	private FindProductSurveyInTask mFindProductSurveyInTask;
	private FindProductSurveyScanListInTask mFindProductSurveyScanListInTask;

	
    @Override    
    public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
        
        initBarcodeScanner();
        setContentView(R.layout.survey_terminalcheck_activity);
        setMenuLayout();
        setLayout();
        setTreeView(savedInstanceState);

        initScreen();
    }

	private void setMenuLayout() {
		mJobGubun = GlobalData.getInstance().getJobGubun();
		
    	ActionBar actionBar = getActionBar();
    	
    	// Action Bar 패턴컬러 적용.
        //BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.common_actionbar_bg);
        //bg.setTileModeXY(TileMode.REPEAT, TileMode.CLAMP);
        //actionBar.setBackgroundDrawable(bg);
    	
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

    
    /****************************************************************
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();

 		Log.d(TAG, "initBarcodeScanner  getState()==>" + mScannerHelper.getState());
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
    
    private void setLayout() {
    	//-----------------------------------------------------------
    	// 회계 년도
    	//-----------------------------------------------------------
		List<SpinnerInfo> yearItems = new ArrayList<SpinnerInfo>();
		String system_year = SystemInfo.getNowYear();
		for (int year=Integer.valueOf(system_year); year>=1990; year--) {
			yearItems.add(new SpinnerInfo(String.valueOf(year), String.valueOf(year)+"년"));
		}
		mYearSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext(), yearItems);
		mYearSpinner = (Spinner) findViewById(R.id.terminalcheck_year_spinner);
		mYearSpinner.setAdapter(mYearSpinnerAdapter);
		mYearSpinner.setSelection(0);
		mYearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mYearSpinnerAdapter.getItem(position);
				if (spinnerInfo != null) {
					// 플랜트 정보를 조회한다.
			        getPlantData();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		
		//-----------------------------------------------------------
    	// 회계 월
    	//-----------------------------------------------------------
		List<SpinnerInfo> monthItems = new ArrayList<SpinnerInfo>();
		monthItems.add(new SpinnerInfo("01", "1월"));
		monthItems.add(new SpinnerInfo("02", "2월"));
		monthItems.add(new SpinnerInfo("03", "3월"));
		monthItems.add(new SpinnerInfo("04", "4월"));
		monthItems.add(new SpinnerInfo("05", "5월"));
		monthItems.add(new SpinnerInfo("06", "6월"));
		monthItems.add(new SpinnerInfo("07", "7월"));
		monthItems.add(new SpinnerInfo("08", "8월"));
		monthItems.add(new SpinnerInfo("09", "9월"));
		monthItems.add(new SpinnerInfo("10", "10월"));
		monthItems.add(new SpinnerInfo("11", "11월"));
		monthItems.add(new SpinnerInfo("12", "12월"));

		//-----------------------------------------------------------
    	// 플랜트
    	//-----------------------------------------------------------
		mMonthSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext(), monthItems);
		mMonthSpinner = (Spinner) findViewById(R.id.terminalcheck_month_spinner);
		mMonthSpinner.setAdapter(mMonthSpinnerAdapter);
		String system_month = SystemInfo.getNowMonth();
		mMonthSpinner.setSelection(mMonthSpinnerAdapter.getPosition(system_month));
		mMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mMonthSpinnerAdapter.getItem(position);
				if (spinnerInfo != null) {
					
					initScreen("inputbar");  // 2.플랜트, 저장위치, 단말바코드 Clear
					initScreen("tree");   // 3.treeview Clear
					
					// 플랜트 정보를 조회한다.
			        getPlantData();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		mPlantSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mPlantSpinner = (Spinner) findViewById(R.id.terminalcheck_plant_spinner);
		mPlantSpinner.setAdapter(mPlantSpinnerAdapter);
		mPlantSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mPlantSpinnerAdapter.getItem(position);
				if (spinnerInfo != null) {
					// 저장위치 정보를 조회한다.
					getSLData(spinnerInfo.getCode());
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		//-----------------------------------------------------------
    	// 저장위치
    	//-----------------------------------------------------------
		mSLSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mSLSpinner = (Spinner) findViewById(R.id.terminalcheck_sl_spinner);
		mSLSpinner.setAdapter(mSLSpinnerAdapter);
		mSLSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo yearSpinnerInfo = (SpinnerInfo) mYearSpinnerAdapter.getItem(mYearSpinner.getSelectedItemPosition());
		    	SpinnerInfo monthSpinnerInfo = (SpinnerInfo) mMonthSpinnerAdapter.getItem(mMonthSpinner.getSelectedItemPosition());
		    	SpinnerInfo plantSpinnerInfo = (SpinnerInfo) mPlantSpinnerAdapter.getItem(mPlantSpinner.getSelectedItemPosition());
		    	SpinnerInfo slSpinnerInfo = (SpinnerInfo) mSLSpinnerAdapter.getItem(mSLSpinner.getSelectedItemPosition());

		    	String year = yearSpinnerInfo.getCode();
		    	String month = monthSpinnerInfo.getCode();
		    	String plant = plantSpinnerInfo.getCode();
		    	String slCode = slSpinnerInfo.getCode();
				
				// 실사문서번호 조회.
				getIBLNRData(year, month, plant, slCode);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		//-----------------------------------------------------------
    	// 실사문서
    	//-----------------------------------------------------------
		mIblnrInputBar = (LinearLayout) findViewById(R.id.terminalcheck_iblnr_inputbar);
		mIblnrSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mIblnrSpinner = (Spinner) findViewById(R.id.terminalcheck_iblnr_spinner);
		mIblnrSpinner.setAdapter(mIblnrSpinnerAdapter);
		mIblnrSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		//-----------------------------------------------------------
    	// 자재
    	//-----------------------------------------------------------
	  	mMaterialsInputBar = (LinearLayout) findViewById(R.id.terminalcheck_materials_inputbar);
	  	mMaterialsText = (EditText) findViewById(R.id.terminalcheck_materials_text);
		
	  	//-----------------------------------------------------------
    	// 단말바코드
    	//-----------------------------------------------------------
	  	mTerminalInputBar = (LinearLayout) findViewById(R.id.terminalcheck_terminal_inputbar);
		mTerminalCodeText = (EditText) findViewById(R.id.terminalcheck_terminalcode);
		mTerminalCodeText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mTerminalCodeText);
			                return true;
			            }
			            return true;
			        }
			    });
		mTerminalCodeText.addTextChangedListener(
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
							Log.d(TAG, "단말바코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeTerminalCode(barcode);
						}
					}
				});
		mTerminalCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeTerminalCode(barcode);
                    return true;
                }
                return false;
            }
        });
		
		//-----------------------------------------------------------
    	// 초기화, 삭제, 저장, 전송, 조회, 닫기 버튼들...
    	//-----------------------------------------------------------
		mInitButton = (Button) findViewById(R.id.terminalcheck_crud_init);
		mInitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						GlobalData.getInstance().initJobActionManager();
				        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_GENERAL) {
				        	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
				        }
				        
						initScreen();
					}
				});
		mDeleteButton = (Button) findViewById(R.id.terminalcheck_crud_delete);
		mDeleteButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						deleteCheckItems();
					}
				});
		mSaveButton = (Button) findViewById(R.id.terminalcheck_crud_save);
		mSaveButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveWorkData(null);
					}
				});
		mSendButton = (Button) findViewById(R.id.terminalcheck_crud_send);
		mSendButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						SendCheck sendCheck = new SendCheck();
						
						SpinnerInfo yearSpinnerInfo = (SpinnerInfo) mYearSpinnerAdapter.getItem(mYearSpinner.getSelectedItemPosition());
				    	SpinnerInfo monthSpinnerInfo = (SpinnerInfo) mMonthSpinnerAdapter.getItem(mMonthSpinner.getSelectedItemPosition());
				    	
				    	sendCheck.year = yearSpinnerInfo.getCode();
				    	sendCheck.month = monthSpinnerInfo.getCode();
				    	
				    	if (mPlantSpinnerAdapter.getCount() > 0) {
				    		SpinnerInfo plantSpinnerInfo = (SpinnerInfo) mPlantSpinnerAdapter.getItem(mPlantSpinner.getSelectedItemPosition());
				    		sendCheck.plant = plantSpinnerInfo.getCode();
				    	}
				    	
				    	if (mSLSpinnerAdapter.getCount() > 0) {
				    		SpinnerInfo slSpinnerInfo = (SpinnerInfo) mSLSpinnerAdapter.getItem(mSLSpinner.getSelectedItemPosition());
				    		sendCheck.slCode = slSpinnerInfo.getCode();
				    	}

				    	if (mJobGubun.equals("상품단말실사")) {
				    		
				    		if (mIblnrSpinnerAdapter.getCount() > 0) {
				    			SpinnerInfo iblnrSpinnerInfo = (SpinnerInfo) mIblnrSpinnerAdapter.getItem(mIblnrSpinner.getSelectedItemPosition());
					    		sendCheck.iblnr = iblnrSpinnerInfo.getCode();
				    		}
				    		
					    	if (thisProductSurveyInfo == null) {
					    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 실사문서 정보가 없습니다."));
					    		return;
					    	}
					    	sendCheck.itemNumber = thisProductSurveyInfo.getItemNumber();
				    	}
				    	
				    	executeSendCheck(sendCheck);
					}
				});
		mSearchButton = (Button) findViewById(R.id.terminalcheck_crud_search);
		mSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						int position = mIblnrSpinner.getSelectedItemPosition();
						if (position<0) {
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 실사문서 정보가 없습니다."));
							return;
						}
						SpinnerInfo yearSpinnerInfo = (SpinnerInfo) mYearSpinnerAdapter.getItem(mYearSpinner.getSelectedItemPosition());
						SpinnerInfo IblnrSpinnerInfo = (SpinnerInfo) mIblnrSpinnerAdapter.getItem(position);
						String year = yearSpinnerInfo.getCode();     // 회계년도
						String iblnr = IblnrSpinnerInfo.getCode();   // 실사문서번호

						getProductSurveyData(year, iblnr);
					}
				});
		mCloseButton = (Button) findViewById(R.id.terminalcheck_crud_close);
		mCloseButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						closeTerminal();
					}
				});
    }
    
    @SuppressWarnings("unchecked")
	private void setTreeView(Bundle savedInstanceState) {

    	//-----------------------------------------------------------
    	// 상품단말실사 ListView
    	//-----------------------------------------------------------
    	mOneScrollView = (HorizontalScrollView) findViewById(R.id.terminalcheck_one_scrollView);
    	mProductSurveyListAdapter = new ProductSurveyListAdapter(getApplicationContext());
    	mProductSurveyListView = (ListView) findViewById(R.id.terminalcheck_one_listView);
        
    	mProductSurveyListView.setAdapter(mProductSurveyListAdapter);
    	mProductSurveyListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				
				ProductSurveyInfo model = mProductSurveyListAdapter.getItem(position);
				choicePsItem(model);
			}
		});
    	
    	//-----------------------------------------------------------
    	// 상품단말실사 Check ListView
    	//-----------------------------------------------------------
    	mTwoScrollView = (HorizontalScrollView) findViewById(R.id.terminalcheck_two_scrollView);
    	mCheckTerminalListAdapter = new CheckTerminalListAdapter(getApplicationContext());
    	mCheckTerminalListView = (ListView) findViewById(R.id.terminalcheck_two_listView);
        
    	mCheckTerminalListView.setAdapter(mCheckTerminalListAdapter);
    	mCheckTerminalListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				
			}
		});
    	
    	//-----------------------------------------------------------
    	// 임대단말실사 TreeView
    	//-----------------------------------------------------------
    	mThreeScrollView = (HorizontalScrollView) findViewById(R.id.terminalcheck_three_scrollView);
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
		mBarcodeTreeView = (TreeViewList) findViewById(R.id.terminalcheck_three_treeView);
		mBarcodeTreeView.setAdapter(mBarcodeTreeAdapter);
		mBarcodeTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "mBarcodeTreeView onItemClick   position==>" + position);
				
				Long selectedKey = mBarcodeTreeAdapter.getTreeId(position);
				mBarcodeTreeAdapter.changeSelected(selectedKey);
				mBarcodeTreeAdapter.refresh();
			}
		});
		
		mListFooterTotalCount = (TextView) findViewById(R.id.terminalcheck_listFooter_totalCount);
		
		// Progree
		mBarcodeProgress = (RelativeLayout) findViewById(R.id.terminalcheck_barcodeProgress);
    }
    
    private void displayTabTree(String tab) {
    	if (tab.equals("one")) {
    		mOneScrollView.setVisibility(View.VISIBLE);
    		mTwoScrollView.setVisibility(View.GONE);
    		mThreeScrollView.setVisibility(View.GONE);
    	} else if (tab.equals("two")) {
    		mOneScrollView.setVisibility(View.GONE);
    		mTwoScrollView.setVisibility(View.VISIBLE);
    		mThreeScrollView.setVisibility(View.GONE);
    	} else {
    		mOneScrollView.setVisibility(View.GONE);
    		mTwoScrollView.setVisibility(View.GONE);
    		mThreeScrollView.setVisibility(View.VISIBLE);
    	}
    }
    
    private String getDisplayTabTree() {
    	String tab = "";
    	if (mOneScrollView.getVisibility() == View.VISIBLE) {
    		tab = "one";
    	} else if (mTwoScrollView.getVisibility() == View.VISIBLE) {
    		tab = "two";
    	} else if (mThreeScrollView.getVisibility() == View.VISIBLE) {
    		tab = "three";
    	}
    	return tab;
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return true;
		if (item.getItemId()==android.R.id.home) {
			if (getDisplayTabTree().equals("two")) {
				closeTerminal();
				return true;
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
		
		if (getDisplayTabTree().equals("two")) {
			closeTerminal();
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
    
    private void setFieldVisibility() {
    	if (mJobGubun.equals("임대단말실사")) {
    		mIblnrInputBar.setVisibility(View.GONE);
    		mMaterialsInputBar.setVisibility(View.GONE);
    		mTerminalInputBar.setVisibility(View.VISIBLE);
    		
    		mInitButton.setVisibility(View.VISIBLE);
    		mDeleteButton.setVisibility(View.VISIBLE);
    		mSaveButton.setVisibility(View.VISIBLE);
    		mSendButton.setVisibility(View.VISIBLE);
    		mSearchButton.setVisibility(View.GONE);
    		mCloseButton.setVisibility(View.GONE);
    		
    		// TreeView를 선택한다.
    		displayTabTree("three");
    	}
    	
    	if (mJobGubun.equals("상품단말실사")) {
    		mIblnrInputBar.setVisibility(View.VISIBLE);
    		mMaterialsInputBar.setVisibility(View.GONE);
    		mTerminalInputBar.setVisibility(View.GONE);
    		
    		mInitButton.setVisibility(View.VISIBLE);
    		mDeleteButton.setVisibility(View.GONE);
    		mSaveButton.setVisibility(View.GONE);
    		mSendButton.setVisibility(View.GONE);
    		mSearchButton.setVisibility(View.VISIBLE);
    		mCloseButton.setVisibility(View.GONE);
    		
    		// TreeView를 선택한다.
    		displayTabTree("one");
    	}
    }
    
    private void initScreen() {
    	if (mJobGubun.equals("임대단말실사")) {
    		setFieldVisibility();
    		initScreen("all");
    		
    		mScannerHelper.focusEditText(mTerminalCodeText);
    		
            // 플랜트 정보를 조회한다.
            getPlantData();
    		
    	} else if (mJobGubun.equals("상품단말실사")) {
    		if (getDisplayTabTree().equals("two")) {
    			mTerminalCodeText.setText("");
    			mCheckTerminalListAdapter.itemClear();
    			showSummaryCount();
    			
    		} else {
    			setFieldVisibility();
    			initScreen("all");
    			
    	        // 플랜트 정보를 조회한다.
    	        getPlantData();
    		}
    	}
    }
    
    private void initScreen(String step) {
    	if (step.equals("all") || step.equals("base")) {
    		GlobalData.getInstance().setGlobalProgress(false);
    		GlobalData.getInstance().setGlobalAlertDialog(false);
    		GlobalData.getInstance().setChangeFlag(false);
    		GlobalData.getInstance().setSendAvailFlag(false);

    		String system_year = SystemInfo.getNowYear();
        	mYearSpinner.setSelection(mYearSpinnerAdapter.getPosition(system_year));
        	mYearSpinner.setEnabled(false);
        	
        	String system_month = SystemInfo.getNowMonth();
    		mMonthSpinner.setSelection(mMonthSpinnerAdapter.getPosition(system_month));
    		mMonthSpinner.setEnabled(false);
    	}
    	// 2.플랜트, 저장위치, 단말바코드 Clear
    	if (mJobGubun.equals("임대단말실사") || mJobGubun.equals("상품단말실사")  
    			&& step.equals("all") || step.equals("inputbar")) {
    		mPlantSpinnerAdapter.itemClear();
    		mSLSpinnerAdapter.itemClear();
    		if (mJobGubun.equals("상품단말실사")) {
    			mIblnrSpinnerAdapter.itemClear();
        		mMaterialsText.setText("");
    		}
    		mTerminalCodeText.setText("");
    	}
    	// 3.crudbar
    	if (mJobGubun.equals("임대단말실사") || mJobGubun.equals("상품단말실사") 
    			&& step.equals("all") || step.equals("crudbar")) {
    		if (mJobGubun.equals("임대단말실사")) {
        		mInitButton.setVisibility(View.VISIBLE);
        		mDeleteButton.setVisibility(View.VISIBLE);
        		mSaveButton.setVisibility(View.VISIBLE);
        		mSendButton.setVisibility(View.VISIBLE);
        		mSearchButton.setVisibility(View.GONE);
        		mCloseButton.setVisibility(View.GONE);
    		}
    		if (mJobGubun.equals("상품단말실사")) {
        		mInitButton.setVisibility(View.VISIBLE);
        		mDeleteButton.setVisibility(View.GONE);
        		mSaveButton.setVisibility(View.GONE);
        		mSendButton.setVisibility(View.GONE);
        		mSearchButton.setVisibility(View.VISIBLE);
        		mCloseButton.setVisibility(View.GONE);
    		}
    	}
    	// 4.TreeView Clear
    	if (mJobGubun.equals("임대단말실사") || mJobGubun.equals("상품단말실사") 
    			&& step.equals("all") || step.equals("tree")) {
    		if (mJobGubun.equals("임대단말실사")) {
    			mBarcodeTreeAdapter.itemClear();
    		}
    		if (mJobGubun.equals("상품단말실사")) {
    			mProductSurveyListAdapter.itemClear();
        		mCheckTerminalListAdapter.itemClear();
    		}
    		showSummaryCount();
    	}
    }
    
    /**
     * 터미널 입력 Tab 종료한다.
     */
    private void closeTerminal() {
		mMaterialsInputBar.setVisibility(View.GONE);
	  	mTerminalInputBar.setVisibility(View.GONE);
	  	mInitButton.setVisibility(View.VISIBLE);
		mDeleteButton.setVisibility(View.GONE);
		mSaveButton.setVisibility(View.GONE);
		mSendButton.setVisibility(View.GONE);
		mSearchButton.setVisibility(View.VISIBLE);
		mCloseButton.setVisibility(View.GONE);
		
	  	displayTabTree("one");
	  	
		mMaterialsText.setText("");
	  	
		showSummaryCount();
    }
    
    private void changeTerminalCode(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String terminalCode = mTerminalCodeText.getText().toString();
    	if (terminalCode.isEmpty()) {
    		mTerminalCodeText.setText(barcode);
    	}
    	
    	
    	if (mJobGubun.equals("상품단말실사")) {
    		addListView(barcode);
    	} else {
    		addTreeNode(barcode);
    	}
    }
    
    private void addTreeNode(String barcode) {
    	Long thisBarcodeKey = mBarcodeTreeAdapter.getBarcodeKey(barcode);
    	if (thisBarcodeKey == null) {
    		BarcodeListInfo newBarcodeInfo = new BarcodeListInfo();
    		newBarcodeInfo.setBarcode(barcode);
    		
    		mBarcodeTreeAdapter.addItem(newBarcodeInfo);
    		TreeNodeInfo<Long> treeNode = mBarcodeTreeAdapter.getBarcodeTreeNodeInfo(barcode);
    		
        	if (treeNode != null) {
        		mBarcodeTreeAdapter.changeSelected(treeNode.getId());
        		int barcodePosition = mBarcodeTreeAdapter.getKeyPosition(treeNode.getId());
				mBarcodeTreeView.setSelection(barcodePosition);  // 선택된 바코드로 커서 이동한다.
        	}
        	
        	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			} 
        	else {
				try {
					GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_CHK_SCAN, "chk_scan", barcode);
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
        	
        	showSummaryCount();
    	} else {
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
    		return;
    	}
    	
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 True
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    }
    
    private void addListView(String barcode) {
    	CheckTerminalInfo terminalInfo = mCheckTerminalListAdapter.getCheckTerminalInfo(barcode);
    	if (terminalInfo == null) {
    		terminalInfo = new CheckTerminalInfo();
    		terminalInfo.setChecked(true);
    		terminalInfo.setNumber(mCheckTerminalListAdapter.getCount()+1);
    		terminalInfo.setTerminalCode(barcode);
    		terminalInfo.setSendYn("N");
    		
    		mCheckTerminalListAdapter.addItem(terminalInfo);
    		showSummaryCount();
    	} else {
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
    		return;
    	}
    	
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 True
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
    }
    
    /**
     * 실사자재 ListView 선택시 화면전환.
     */
    private void choicePsItem(ProductSurveyInfo model) {
    	if (model == null) return; 
    	
	  	mMaterialsInputBar.setVisibility(View.VISIBLE);
	  	mTerminalInputBar.setVisibility(View.VISIBLE);
	  	mInitButton.setVisibility(View.VISIBLE);
		mDeleteButton.setVisibility(View.VISIBLE);
		mSaveButton.setVisibility(View.GONE);
		mSendButton.setVisibility(View.VISIBLE);
		mSearchButton.setVisibility(View.GONE);
		mCloseButton.setVisibility(View.VISIBLE);
		
	  	displayTabTree("two");

		String psInfo = model.getProductCode() + ":" + model.getProductName();
		mMaterialsText.setText(psInfo);
		mTerminalCodeText.setText("");
		
		
		SpinnerInfo yearSpinnerInfo = (SpinnerInfo) mYearSpinnerAdapter.getItem(mYearSpinner.getSelectedItemPosition());
		SpinnerInfo iblnrSpinnerInfo = (SpinnerInfo) mIblnrSpinnerAdapter.getItem(mIblnrSpinner.getSelectedItemPosition());
		String year = yearSpinnerInfo.getCode();
		String iblnr = iblnrSpinnerInfo.getTitle();
		String itemNumber = model.getItemNumber();

		thisProductSurveyInfo = model;
		
		// 실사 스캔 리스트 조회.
		getProductSurveyScanListData(year, iblnr, itemNumber);
		
		mScannerHelper.focusEditText(mTerminalCodeText);
    }
    
    private void deleteCheckItems() {
    	
    	Log.i(TAG, "terminal>> deleteCheckItems  Start...!!"  + GlobalData.getInstance().getJobActionManager().getWorkStatus());

    	// 상품단말실사 
    	if (mOneScrollView.getVisibility() == View.VISIBLE) {
    		return;
    	
    	// 상품단말실사 
    	} else if (mTwoScrollView.getVisibility() == View.VISIBLE) {
    		if (mCheckTerminalListAdapter.getCheckedItems().size() == 0) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. ", BarcodeSoundPlay.SOUND_ERROR));
    			return;
    		}
    	// 임대단말실사
    	} else if (mThreeScrollView.getVisibility() == View.VISIBLE) {
    		Long selectedKey = mBarcodeTreeAdapter.getSelected();
    		if (selectedKey == null) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. ", BarcodeSoundPlay.SOUND_ERROR));
    			return;
    		}
    	}
    	
    	//-----------------------------------------------------------
    	// 최종 삭제 Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    	String message = "삭제하시겠습니까?";
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
            	
            	// 상품단말실사 
            	if (mTwoScrollView.getVisibility() == View.VISIBLE) {
            		mCheckTerminalListAdapter.removeCheckedItems();
            		showSummaryCount();
            		
            	// 임대단말실사
            	} else if (mThreeScrollView.getVisibility() == View.VISIBLE) {
            		Long selectedKey = mBarcodeTreeAdapter.getSelected();
            		if (selectedKey != null) {
            			mBarcodeTreeAdapter.removeNode(selectedKey);
            			
            			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
            				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
            			} else {
            				try {
            					GlobalData.getInstance().getJobActionManager().setStepItem(
            							JobActionStepManager.JOBTYPE_DELETE, "barcode", String.valueOf(selectedKey));
            				} catch (ErpBarcodeException e) {
            					e.printStackTrace();
            				}
            			}
            		}
            		showSummaryCount();
            	}
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
    
    
    /**
	 * 플랜트 조회.
	 */
    private void getPlantData() {
    	Log.d(TAG, "getPlantData  Next  Start...");
    	if (isBarcodeProgressVisibility()) return;
    	
    	mPlantSpinnerAdapter.itemClear();
    	
		if (mFindPlantInTask == null) {
			setBarcodeProgressVisibility(true);
    		mFindPlantInTask = new FindPlantInTask();
    		mFindPlantInTask.execute((Void) null);
		}
    }
    private class FindPlantInTask extends AsyncTask<Void, Void, Boolean> {
    	private JSONArray _plantJsonArray = null;
    	private ErpBarcodeException _ErpBarException;

    	@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController surveyhttp = new SurveyHttpController();
				_plantJsonArray = surveyhttp.getPlant();
				
				if (_plantJsonArray == null) {
					throw new ErpBarcodeException(-1, "플랜트 조회 결과가 없습니다. ");
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
    		mFindPlantInTask = null;
    		setBarcodeProgressVisibility(false);
    		
    		if (result) {
    			List<SpinnerInfo> plantItems = new ArrayList<SpinnerInfo>();
                for (int i=0;i<_plantJsonArray.length();i++) {
            		try {
    	            	JSONObject jsonobj = _plantJsonArray.getJSONObject(i);
    	            	String code = jsonobj.getString("PLANT");
    	            	String name = jsonobj.getString("PLANT_TXT");
    	            	
    	            	plantItems.add(new SpinnerInfo(code, code + ":" +name));
    	            	
            		} catch (JSONException e) {
            			Log.d(TAG, "FindPlantInTask  플랜트 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "플랜트 자료 변환중 오류가 발생했습니다.") );
    	    			return;
            		}
                }
                
                if (plantItems.size() > 0) {
                	mPlantSpinnerAdapter.addItems(plantItems);
                	// Plinner Change Event 발생되므로 상위에 
                	// getSLData 저장위치조회 로식 호출한다.
                	
                	mSLSpinnerAdapter.itemClear();
            		mIblnrSpinnerAdapter.itemClear();
            		mMaterialsText.setText("");
            		mTerminalCodeText.setText("");
                }
    		} else {
    			// 여기서는 메시지 출력하지 않는다.
    			GlobalData.getInstance().showMessageDialog(_ErpBarException);
    		}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindPlantInTask = null;
			setBarcodeProgressVisibility(false);
		}
    }
    
    /**
	 * 저장위치 조회.
	 */
    private void getSLData(String plant) {
    	Log.d(TAG, "getSLData  Next  Start...");
    	if (isBarcodeProgressVisibility()) return;
    	
    	mSLSpinnerAdapter.itemClear();
    	
		if (mFindSLInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindSLInTask = new FindSLInTask(plant);
			mFindSLInTask.execute((Void) null);
		}
		
      // 작업관리에서 진입했을경우 
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
    
    private class FindSLInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;
    	private String _plant;
    	private JSONArray _SLJsonArray = null;
    	
    	public FindSLInTask(String plant) {
    		_plant = plant;
    	}

    	@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController surveyhttp = new SurveyHttpController();
				_SLJsonArray = surveyhttp.getSL(_plant);
				
				if (_SLJsonArray == null) {
					throw new ErpBarcodeException(-1, "저장위치 조회 결과가 없습니다. ");
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
    		mFindSLInTask = null;
    		setBarcodeProgressVisibility(false);
    		
    		if (result) {
    			List<SpinnerInfo> slItems = new ArrayList<SpinnerInfo>();
                for (int i=0;i<_SLJsonArray.length();i++) {
            		try {
    	            	JSONObject jsonobj = _SLJsonArray.getJSONObject(i);
    	            	String code = jsonobj.getString("LGORT");
    	            	String name = jsonobj.getString("LGOBE");
    	            	
    	            	slItems.add(new SpinnerInfo(code, code + ":" + name));
    	            	
            		} catch (JSONException e) {
            			Log.d(TAG, "FindSLInTask  저장위치 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "저장위치 자료 변환중 오류가 발생했습니다."));
    	    			return;
            		}
                }
                
                if (slItems.size() > 0) {
                	mSLSpinnerAdapter.addItems(slItems);
                	
                	mIblnrSpinnerAdapter.itemClear();
            		mMaterialsText.setText("");
            		mTerminalCodeText.setText("");
                	
                	if (mJobGubun.equals("임대단말실사")) {
                		mScannerHelper.focusEditText(mTerminalCodeText);
                	}
                }
    		} else {
    			GlobalData.getInstance().showMessageDialog(_ErpBarException);
    		}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindSLInTask = null;
			setBarcodeProgressVisibility(false);
		}
    }

    /**
	 * 실사문서번호 조회.
	 */
    private void getIBLNRData(String year, String month, String plant, String slCode) {
    	Log.d(TAG, "getIBLNRData  Next  Start...");
    	
    	if (!mJobGubun.equals("상품단말실사")) return;
    	
    	if (year.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "회계년도를 선택하세요."));
    		return;
    	}
    	
    	if (month.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "회계월을 선택하세요."));
    		return;
    	}
    	
    	if (plant.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "플랜트를 선택하세요."));
    		return;
    	}
    	
    	if (slCode.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "저장위치를 선택하세요."));
    		return;
    	}
    	
    	if (isBarcodeProgressVisibility()) return;
    	
    	mBarcodeTreeAdapter.itemClear();
    	
		if (mFindIBLNRInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindIBLNRInTask = new FindIBLNRInTask(year, month, plant, slCode);
			mFindIBLNRInTask.execute((Void) null);
		}
    }
    private class FindIBLNRInTask extends AsyncTask<Void, Void, Boolean> {
    	private String _Year, _Month, _Plant, _SlCode;
    	private JSONArray _IBLNRJsonArray = null;
    	private ErpBarcodeException _ErpBarException;
    	
    	public FindIBLNRInTask(String year, String month, String plant, String slCode) {
    		_Year = year;
    		_Month = month;
    		_Plant = plant;
    		_SlCode = slCode;
    	}

    	@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController surveyhttp = new SurveyHttpController();
				_IBLNRJsonArray = surveyhttp.getIBLNR(_Year, _Month, _Plant, _SlCode);
				
				if (_IBLNRJsonArray == null) {
					throw new ErpBarcodeException(-1, "실사문서 조회 결과가 없습니다.");
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
    		mFindIBLNRInTask = null;
    		setBarcodeProgressVisibility(false);
    		
    		if (result) {
    			List<SpinnerInfo> iblnrItems = new ArrayList<SpinnerInfo>();
                for (int i=0;i<_IBLNRJsonArray.length();i++) {
            		try {
    	            	JSONObject jsonobj = _IBLNRJsonArray.getJSONObject(i);
    	            	String code = jsonobj.getString("IBLNR");
    	            	String name = jsonobj.getString("GJAHR");
    	            	
    	            	iblnrItems.add(new SpinnerInfo(code, code + ":" + name));
    	            	
            		} catch (JSONException e) {
            			Log.d(TAG, "FindSLInTask  실사문서 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "실사문서 자료 변환중 오류가 발생했습니다.") );
    	    			return;
            		}
                }
                
                if (iblnrItems.size() > 0) {
                	mIblnrSpinnerAdapter.addItems(iblnrItems);
                }
    		} else {
    			GlobalData.getInstance().showMessageDialog(_ErpBarException);
    		}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindIBLNRInTask = null;
			setBarcodeProgressVisibility(false);
		}
    }
    
    /**
	 * 실사자재 조회.
	 */
    private void getProductSurveyData(String year, String iblnr) {
    	Log.d(TAG, "getProductSurveyData  Start...");
    	
    	if (!mJobGubun.equals("상품단말실사")) return;
    	
    	if (iblnr.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "실사문서를 선택하세요."));
    		return;
    	}

    	if (isBarcodeProgressVisibility()) return;
    	
    	mBarcodeTreeAdapter.itemClear();
    	
		if (mFindProductSurveyInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindProductSurveyInTask = new FindProductSurveyInTask(year, iblnr);
			mFindProductSurveyInTask.execute((Void) null);
		}
    }
    private class FindProductSurveyInTask extends AsyncTask<Void, Void, Boolean> {
    	private String _Year, _Iblnr;
    	private JSONArray _PSJsonArray = null;
    	private ErpBarcodeException _ErpBarException;
    	
    	public FindProductSurveyInTask(String year, String iblnr) {
    		_Year = year;
    		_Iblnr = iblnr;
    	}

    	@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController surveyhttp = new SurveyHttpController();
				_PSJsonArray = surveyhttp.getProductSurveyList(_Year, _Iblnr);
				
				if (_PSJsonArray == null) {
					throw new ErpBarcodeException(-1, "실사자재 조회 결과가 없습니다. ");
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
    		mFindProductSurveyInTask = null;
    		setBarcodeProgressVisibility(false);
    		
    		if (result) {
    			List<ProductSurveyInfo> psItems = new ArrayList<ProductSurveyInfo>();
                for (int i=0;i<_PSJsonArray.length();i++) {
            		try {
    	            	JSONObject jsonobj = _PSJsonArray.getJSONObject(i);
    	            	String itemNumber = jsonobj.getString("ZEILI").trim();   // 항번
    	            	String productCode = jsonobj.getString("MATNR").trim();  // 자재코드
    	            	String productName = jsonobj.getString("MAKTX").trim();  // 자재명 
    	            	String quantity = jsonobj.getString("BUCHM").trim();     // 수량
    	            	String scanQuantity = jsonobj.getString("MENGE").trim(); // 스캔수량
    	            	String batchNumber = jsonobj.getString("CHARG").trim();  // 배치번호
    	            	
    	            	ProductSurveyInfo ps = new ProductSurveyInfo();
    	            	ps.setItemNumber(itemNumber);
    	            	ps.setProductCode(productCode);
    	            	ps.setProductName(productName);
    	            	ps.setQuantity(Integer.valueOf(quantity));
    	            	ps.setScanQuantity(Integer.valueOf(scanQuantity));
    	            	ps.setBatchNumber(batchNumber);
    	            	
    	            	psItems.add(ps);

            		} catch (JSONException e) {
            			Log.d(TAG, "FindSLInTask  실사자재 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "실사자재 자료 변환중 오류가 발생했습니다.") );
    	    			return;
            		}
                }
                if (psItems.size() > 0) {
            		initScreen("crudbar");
            		initScreen("tree");
                	
                	mProductSurveyListAdapter.addItems(psItems);
                	
                	showSummaryCount();
                }

    		} else {
    			GlobalData.getInstance().showMessageDialog(_ErpBarException);
    		}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindProductSurveyInTask = null;
			setBarcodeProgressVisibility(false);
		}
    }
    

    /**
	 * 실사 스캔 리스트 조회.
	 */
    private void getProductSurveyScanListData(String year, String iblnr, String itemNumber) {
    	Log.d(TAG, "getProductSurveyScanListData  Start...");
    	
    	if (!mJobGubun.equals("상품단말실사")) return;
    	
    	if (year.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "회계년도를 선택하세요."));
    		return;
    	}
    	
    	if (iblnr.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "실사문서를 선택하세요."));
    		return;
    	}
    	
    	if (itemNumber.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "항번을 선택하세요."));
    		return;
    	}

    	if (isBarcodeProgressVisibility()) return;
    	
    	mCheckTerminalListAdapter.itemClear();
    	showSummaryCount();
    	
		if (mFindProductSurveyScanListInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindProductSurveyScanListInTask = new FindProductSurveyScanListInTask(year, iblnr, itemNumber);
			mFindProductSurveyScanListInTask.execute((Void) null);
		}
    }
    private class FindProductSurveyScanListInTask extends AsyncTask<Void, Void, Boolean> {
    	private String _Year, _Iblnr, _ItemNumber;
    	private JSONArray _PSSJsonArray = null;
    	private ErpBarcodeException _ErpBarException;
    	
    	public FindProductSurveyScanListInTask(String year, String iblnr, String itemNumber) {
    		_Year = year;
    		_Iblnr = iblnr;
    		_ItemNumber = itemNumber;
    	}

    	@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SurveyHttpController surveyhttp = new SurveyHttpController();
				_PSSJsonArray = surveyhttp.getProductSurveyScanList(_Year, _Iblnr, _ItemNumber);
				
				if (_PSSJsonArray == null) {
					throw new ErpBarcodeException(-1, "실사 스캔 리스트 조회 결과가 없습니다. ");
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
    		mFindProductSurveyScanListInTask = null;
    		setBarcodeProgressVisibility(false);
    		
    		if (result) {
    			List<CheckTerminalInfo> pssItems = new ArrayList<CheckTerminalInfo>();
                for (int i=0;i<_PSSJsonArray.length();i++) {
            		try {
    	            	JSONObject jsonobj = _PSSJsonArray.getJSONObject(i);
    	            	String terminalCode = jsonobj.getString("BARCODE").trim();  // 단말바코드
    	            	
    	            	CheckTerminalInfo cti = new CheckTerminalInfo();
    	            	cti.setChecked(true);
    	            	cti.setNumber(i+1);                  // 번호는 1부터 증가한다.
    	            	cti.setTerminalCode(terminalCode);   // 단말바코드
    	            	cti.setSendYn("Y");                  // 전송여부
    	            	
    	            	pssItems.add(cti);

            		} catch (JSONException e) {
            			Log.d(TAG, "FindSLInTask  실사자재 자료 변환중 오류가 발생했습니다. >>>>>"+e.getMessage());
    					GlobalData.getInstance().showMessageDialog(
    							new ErpBarcodeException(-1, "실사자재 자료 변환중 오류가 발생했습니다.") );
    	    			return;
            		}
                }
                if (pssItems.size() > 0) {
                	mCheckTerminalListAdapter.addItems(pssItems);
                	
                	showSummaryCount();
                	
                	//-----------------------------------------------------------
            	    // 화면 초기 Setting후 변경 여부 Flag를 True
            	    //-----------------------------------------------------------
            		GlobalData.getInstance().setChangeFlag(true);
            		GlobalData.getInstance().setSendAvailFlag(true);
                }

    		} else {
    			GlobalData.getInstance().showMessageDialog(_ErpBarException);
    		}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindProductSurveyScanListInTask = null;
			setBarcodeProgressVisibility(false);
		}
    }

	/**
	 * List FooterBar Summary Tobal Count 정보를 조회한다.
	 */
	private void showSummaryCount() {
		int totalCount = 0;

		// 임대단말실사일때.
		if (getDisplayTabTree().equals("three")) {
			totalCount = mBarcodeTreeAdapter.getCount();
			
		// 상품단말실사 목록 조회.
		} else if (getDisplayTabTree().equals("one")) {
			totalCount = mProductSurveyListAdapter.getCount();
			
		// 상품단말실사 단말바코드 입력 List
		} else if (getDisplayTabTree().equals("two")) {
			totalCount = mCheckTerminalListAdapter.getCount();
		}
		
		mListFooterTotalCount.setText(String.valueOf(totalCount) + "건");
	}
    
    /**
     * 전송시 사용하는 체크데이터.
     */
    public class SendCheck {
    	public String year = "";
    	public String month = "";
    	public String plant = "";
    	public String slCode = "";
    	public String iblnr = "";
    	public String itemNumber = "";
    }
    
    private void executeSendCheck(final SendCheck sendCheck) {
    	
    	//-----------------------------------------------------------
        // 화면 초기 Setting후 변경 여부 Flag
    	// 변경없으면 자료 전송하지 않는다.
        //-----------------------------------------------------------
    	if (!GlobalData.getInstance().isSendAvailFlag()) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "기존에 전송한 자료와 동일하거나\n\r전송 할 자료가\n\r존재하지 않습니다.\n\r전송할 자료를 추가하거나\n\r변경하신 후\n\r다시 전송하세요."));
			return;
		}
    	
    	if (sendCheck.plant.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "플랜트를 선택하세요. "));
    		return;
    	}
    	if (sendCheck.slCode.isEmpty()) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "저장위치를 선택하세요. "));
    		return;
    	}
    	if (mJobGubun.equals("상품단말실사")) {
    		if (sendCheck.iblnr.isEmpty()) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "실사문서를 선택하세요. "));
        		return;
        	}
    		
    		if (sendCheck.itemNumber.isEmpty()) {
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 실사물서 정보가 없습니다. "));
        		return;
        	}
    		
    		if (mCheckTerminalListAdapter.getCheckedItems().size() == 0) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
        		return;
    		}
    	} else if (mJobGubun.equals("임대단말실사")) {
    		if (mBarcodeTreeAdapter.getAllItems().size()<1) {
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 자료가 없습니다. "));
    			return;
    		}
    	}
    	
		//-----------------------------------------------------------
		// 최종 전송 Yes/No 확인 Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
    	String message = "전송하시겠습니까?"; 
    	
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
    
    /**
     * 임대단말실사 전송
     */
	private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private SendCheck _SendCheck;
		private int _SendCount = 0;
		private OutputParameter _OutputParameter;
		
		public SendDataInTask(SendCheck sendCheck) {
	    	_SendCheck = sendCheck;
	    	setBarcodeProgressVisibility(true);
			mSendButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "SendDataInTask  Start...");

			//-----------------------------------------------------------
			// 여기부터 Header전문 처리.
	    	//-----------------------------------------------------------
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
			try {
				
				if (mJobGubun.equals("상품단말실사")) {
					jsonParam.put("GJAHR", _SendCheck.year);
					jsonParam.put("IBLNR", _SendCheck.iblnr);
					jsonParam.put("ZEILI", _SendCheck.itemNumber);
					
				} else if (mJobGubun.equals("임대단말실사")) {
					jsonParam.put("GJAHR", _SendCheck.year);
					jsonParam.put("ZZPI_IND", _SendCheck.month);
					jsonParam.put("WERKS", _SendCheck.plant);
					jsonParam.put("LGORT", _SendCheck.slCode);
				}
			} catch (JSONException e1) {
				_ErpBarException = new ErpBarcodeException(-1, "전문 생성중 오류가 발생했습니다. " + e1.getMessage());
				return false;
			}
			jsonParamList.put(jsonParam);
			
			
			//-----------------------------------------------------------
			// 여기부터 Body전문 처리.
	    	//-----------------------------------------------------------
			Log.d(TAG, "SendDataInTask Body Start..");
			JSONArray jsonSubParamList = new JSONArray();
			
			_SendCount = 0;
			if (mJobGubun.equals("상품단말실사")) {
				final List<CheckTerminalInfo> sendTerminalInfos = mCheckTerminalListAdapter.getAllItems();
				
				Log.d(TAG, "SendDataInTask Body Record Count==>"+ sendTerminalInfos.size());
				
				for (CheckTerminalInfo terminalInfo : sendTerminalInfos) {
					if (!terminalInfo.isChecked()) continue;
					if (terminalInfo.getSendYn().equals("Y")) continue;
					
					JSONObject jsonSubParam = new JSONObject();
					try {
						jsonSubParam.put("BARCODE", terminalInfo.getTerminalCode());

					} catch (JSONException e1) {
						_ErpBarException = new ErpBarcodeException(-1, "전문 생성중 오류가 발생했습니다. " + e1.getMessage());
						return false;
					}
					
					jsonSubParamList.put(jsonSubParam);

					_SendCount++; // 전송건수.
				}
				
			} else if (mJobGubun.equals("임대단말실사")) {
				final Map<Long, BarcodeListInfo> sendBarcodeMaps = mBarcodeTreeAdapter.getAllItems();
				
				Log.d(TAG, "SendDataInTask Body Record Count==>"+ sendBarcodeMaps.size());

				SortedSet<Long> itemKeys = new TreeSet<Long>(sendBarcodeMaps.keySet());
				for (Long key : itemKeys) {
					BarcodeListInfo sendBarcodeInfo = sendBarcodeMaps.get(key);
					
					// 임대단말실사에서는 바코드만 사용함.
					String barcode = sendBarcodeInfo.getBarcode();

					JSONObject jsonSubParam = new JSONObject();
					try {
						jsonSubParam.put("BARCODE", barcode);

					} catch (JSONException e1) {
						_ErpBarException = new ErpBarcodeException(-1, "전문 생성중 오류가 발생했습니다. " + e1.getMessage());
						return false;
					}
					
					jsonSubParamList.put(jsonSubParam);

					_SendCount++; // 전송건수.
				} // for end.
			}

			try {
				SendHttpController sendhttp = new SendHttpController();
				if (mJobGubun.equals("상품단말실사")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_PRODUCTSURVEYSEND, 
							jsonParamList, jsonSubParamList);
					
				} else if (mJobGubun.equals("임대단말실사")) {
					_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_INVENTORYSURVEYSEND, 
							jsonParamList, jsonSubParamList);
				}
				
				if (_OutputParameter == null) {
					throw new ErpBarcodeException(-1, "'" + GlobalData.getInstance().getJobGubun() + "' 정보 전송중 오류가 발생했습니다.");
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
			mSendButton.setEnabled(true);

			if (result) {
				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				saveWorkData(message);

			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mSendButton.setEnabled(true);
		}
	}
	
	/**
     * 작업내용 저장.
     */
    private void saveWorkData(String sendMessage) {
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
    		GlobalData.getInstance().showMessageDialog(
        			new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "작업 진행중이므로 저장할수 없습니다."));
    		return;
    	}
    	
        int recordCount = mBarcodeTreeAdapter.getCount();
        String offlineYn = (SessionUserData.getInstance().isOffline() ? "Y" : "N");

        try {
        	GlobalData.getInstance().getJobActionManager().saveWorkData("", "", "", "", recordCount, offlineYn);
        } catch (ErpBarcodeException e) {
        	e.printStackTrace();
        }
        
        if(sendMessage != null){
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
        	initScreen();
        }
        else{
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
    }
    
    private void jobNextExecutors() {
    	jobNextExecutors(0);
    }
    
    private void jobNextExecutors(int position) {
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
    	
    	if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_CHK_SCAN)) {
    		String barcode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "chk_scan");
    		mTerminalCodeText.setText(barcode);
    		addTreeNode(barcode);
    	} 
    	else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_DELETE)) {
    		String selectCode = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "barcode");
    		mBarcodeTreeAdapter.changeSelected(Long.parseLong(selectCode));
    		deleteCheckItems();
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
    
	
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    	if(GlobalData.getInstance().getJobActionManager() != null){
    		GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
    	}
		
		super.onDestroy();
	}
}
