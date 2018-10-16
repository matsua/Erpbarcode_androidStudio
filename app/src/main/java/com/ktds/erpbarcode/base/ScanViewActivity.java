package com.ktds.erpbarcode.base;

import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
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
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcodeManagement.FindUserActivity;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkItem;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;
import com.manateeworks.cameraDemo.ActivityCapture;



public class ScanViewActivity extends Activity {
	
	private static final String TAG = "ScanViewActivity";
	public static final int REQ_CODE_SCAN_LOCATION = 1000;
	public static final int REQ_CODE_SCAN_COST = 2000;
	
	private ScannerConnectHelper mScannerHelper;
	
	private String mJobGubun;
	private EditText mOrgCodeText;
	private LinearLayout mLocInputbar;
	private Button mlocScan;
	private EditText mLocCdText;
	private Button mfacScan;
	private EditText mFacCdText;
	private Button mInquery;
	private Button mSave;
	private Button mSend;
	private String type;
	private String bsnNo;
	private WebView mWebView;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	mJobGubun = GlobalData.getInstance().getJobGubun();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        
        GlobalData.getInstance().setNowOpenActivity(this);
        
        initBarcodeScanner();
        
        setMenuLayout();
        setContentView(R.layout.scanview_activity);
        setLayout();
        
        GlobalData.getInstance().initJobActionManager();
        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
        	new Handler().postDelayed(
        			new Runnable() {
		        		public void run() {
		        			jobNextExecutors();
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
    
    private void setLayout() {
    	type = "OE";
        if(mJobGubun.contains("OA")){
        	type = "OA";
        }
        
        System.out.print("type >>>>>>>>>>>" + type);
        
        mLocInputbar = (LinearLayout) findViewById(R.id.locView);
        if(mJobGubun.equals("불용요청") || mJobGubun.equals("OA연식조회") || mJobGubun.equals("비품연식조회")){
        	mLocInputbar.setVisibility(View.GONE);
        }
        
        mOrgCodeText = (EditText) findViewById(R.id.base_organization_orgCode);
        String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);
        
        mlocScan = (Button) findViewById(R.id.locCdScan);
        mlocScan.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						scan("loc");
					}
				});
        
        mLocCdText = (EditText) findViewById(R.id.scanview_locCd);
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
							validity("loc", barcode.trim());
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
                	validity("loc", barcode);
                    return true;
                }
                return false;
            }
        });
        
        mfacScan= (Button) findViewById(R.id.facCdScan);
        mfacScan.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						scan("fac");
					}
				});
        mFacCdText = (EditText) findViewById(R.id.scanview_facCd);
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
							validity("fac", barcode.trim());
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
		                	validity("fac", barcode.trim());
		                    return true;
		                }
		                return false;
		            }
		        });

        mInquery= (Button) findViewById(R.id.base_crud_inquery);
        mInquery.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						inquery();
					}
				});
        
        mSave = (Button) findViewById(R.id.base_crud_save);
        mSave.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveWorkData();
					}
				});
        
        mSend = (Button) findViewById(R.id.base_crud_send);
        mSend.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						send();
					}
				});
	}
    
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
    
    private void scan(String type){
    	if(type.equals("loc")){
    		Intent intent = new Intent(getApplicationContext(),ActivityCapture.class);
			startActivityForResult(intent, REQ_CODE_SCAN_LOCATION);
    	}else{
    		Intent intent = new Intent(getApplicationContext(),ActivityCapture.class);
			startActivityForResult(intent, REQ_CODE_SCAN_COST);
    	}
    }
    
    private void validity(String type, String barcode){
    	if(type.equals("loc")){
    		barcode = barcode.toUpperCase();
        	
        	boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", barcode);
        	if(!flag){
        		mLocCdText.setText("");
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 위치바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
            	return;
        	}
        	mLocCdText.setText(barcode);
    	}else{
    		boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", barcode);
        	if(!flag){
        		mFacCdText.setText("");
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
            	return;
        	}
        	
            if (!barcode.isEmpty()) {
            	if (barcode.length() < 16 || barcode.length() > 18) {
            		mFacCdText.setText("");
            		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
                	return;
                }
            }
            
            mFacCdText.setText(barcode);
    	}
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				if(type.equals("loc")){
					GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_LOC, "locCd", barcode);
				}else{
					GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_FAC, "facCd", barcode);
				}
				
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
 	@Override
 	protected void onActivityResult(int reqCode, int resCode, Intent data) {
 		if (resCode != RESULT_OK) return;
 		String barcode = data.getStringExtra("BARCODE");
 		if (TextUtils.isEmpty(barcode)) {
 			return;
 		}
 		switch (reqCode) {
 			case REQ_CODE_SCAN_LOCATION:
 				if (barcode.length() > 0) {
 					validity("loc", barcode);
 				}
 				break;
 			case REQ_CODE_SCAN_COST:
 				if (barcode.length() > 0) {
 					validity("fac", barcode);
 				} 
 				break;
 		}
 	}
 	
 	private void inquery(){
 		if(mJobGubun.equals("신규등록")) bsnNo = "0501";
 	    else if(mJobGubun.equals("관리자변경")) bsnNo = "0504";
 	    else if(mJobGubun.equals("재물조사")) bsnNo = "0601";
 	    else if(mJobGubun.equals("불용요청")) bsnNo = "0505";
 	    else if(mJobGubun.equals("OA연식조회") || mJobGubun.equals("비품연식조회")) bsnNo = "0602";
 	    else if(mJobGubun.equals("납품확인")) bsnNo = "0512";
 	    else if(mJobGubun.equals("대여등록")) bsnNo = "0513";
 	    else if(mJobGubun.equals("대여반납")) bsnNo = "0503";
 		
 		String url = "http://base.kt.com/base/OA/smart";
		if(type.equals("OE")){
			url += "_OE";
		}
 		
 		if(mJobGubun.equals("신규등록") || mJobGubun.equals("관리자변경") || mJobGubun.equals("재물조사") || mJobGubun.equals("납품확인") || mJobGubun.equals("대여등록") || mJobGubun.equals("대여반납")){
				url += "/work_list.jsp?COM=" + bsnNo + "&USERID=91186176&SDID=" + mLocCdText.getText().toString() + "&BCID=001Z00911318010012&ACT=search";
		}else if(mJobGubun.equals("불용요청")){
			url += "/work_list.jsp?COM=" + bsnNo + "&USERID=91186176&BCID=001Z00911318010012&ACT=search";
		}else if(mJobGubun.equals("OA연식조회") || mJobGubun.equals("비품연식조회")){
			url += "/item_search.jsp?COM=" + bsnNo + "&USERID=91186176&BCID=001Z00911318010012";
		}
 		
 		mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
 
        mWebView.loadUrl(url);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClientClass());
        
        if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_SEARCH_TASK, "search_task", "");
				
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
 	}
 	
 	private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	System.out.print("check URL >>>>>> " + url);
            view.loadUrl(url);
            return true;
        }
    }
 	
 	private void send(){
 		
 	}
 	
 	/**
     * 작업내용 저장.
     */
    private void saveWorkData() {
    	
    	if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "작업을 진행중이므로 저장할수 없습니다."));
    		return;
    	}
    	
    	String locCd = mLocCdText.getText().toString().trim();
    	String offlineYn = (SessionUserData.getInstance().isOffline() ? "Y" : "N");

    	try {
    		GlobalData.getInstance().getJobActionManager().saveWorkData(locCd, "", "", "", 0, offlineYn);
    	} catch (ErpBarcodeException e) {
    		e.printStackTrace();
    	}
    	
    	GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_APPEND);
    	GlobalData.getInstance().setChangeFlag(false);
    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE, "저장하였습니다.", BarcodeSoundPlay.SOUND_ASTERISK));
    }
 	
 	 /**
     * 단계별 작업 실행.
     */
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
    	
    	Log.i(TAG, "startStepWorkItem  getJobType==>"+workItem.getJobType());
    	if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_LOC)) {
    		String locCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "locCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_LOC==>"+locCd);
    		validity("loc", locCd);
    	}else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_FAC)) {
    		String facCd = GlobalData.getInstance().getJobActionManager().getJsonStepValue(workItem.getJobData(), "facCd");
    		Log.i(TAG, "startStepWorkItem JOBTYPE_FAC==>"+facCd);
    		validity("fac", facCd);
    	}else if (workItem.getJobType().equals(JobActionStepManager.JOBTYPE_SEARCH_TASK)) {
    		inquery();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GlobalData.getInstance().isGlobalProgress()) return false;
		if (item.getItemId()==android.R.id.home) {
    		if (GlobalData.getInstance().isChangeFlag()) {
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
			return;
        }
		
		super.onBackPressed();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		GlobalData.getInstance().setNowOpenActivity(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		GlobalData.getInstance().getJobActionManager().setJobWorkMode(JobActionManager.JOB_GENERAL);
		super.onDestroy();
	}
}
