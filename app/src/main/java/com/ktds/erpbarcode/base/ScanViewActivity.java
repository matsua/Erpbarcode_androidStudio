package com.ktds.erpbarcode.base;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.manateeworks.cameraDemo.ActivityCapture;



public class ScanViewActivity extends Activity {
	
	private static final String TAG = "ScanViewActivity";
	public static final int REQ_CODE_SCAN_LOCATION = 1000;
	public static final int REQ_CODE_SCAN_FACILITY = 2000;
	public static final int REQ_CODE_SCAN_SNCODE = 3000;
	
	private ScannerConnectHelper mScannerHelper;
	private SearchBaseInTask mSearchBaseInTask;
	
	private String mJobGubun;
	private LinearLayout mLocInputbar;
	private Button mlocScan;
	private EditText mLocCdText;
	private Button mfacScan;
	private EditText mFacCdText;
	private Button mInquery;
	private Button mSend;
	private String type;
	private String bsnNo;
	private LinearLayout mSnInputbar;
	private EditText mSnCdText;
	private Button mSnCdScan;
	
	
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
        
        mLocInputbar = (LinearLayout) findViewById(R.id.locView);
        if(mJobGubun.contains("불용요청") || mJobGubun.contains("연식조회")){
        	mLocInputbar.setVisibility(View.GONE);
        }
        
        mSnInputbar = (LinearLayout) findViewById(R.id.snView);
        if(!mJobGubun.contains("납품확인") && !mJobGubun.contains("신규등록")){
        	mSnInputbar.setVisibility(View.GONE);
        }
        
        if(mJobGubun.contains("신규등록")) {
 			bsnNo = "0501";
 		}
 	    else if(mJobGubun.contains("관리자변경")) {
 	    	bsnNo = "0504";
 	    }
 	    else if(mJobGubun.contains("재물조사")) {
 	    	bsnNo = "0601";
 	    }
 	    else if(mJobGubun.contains("불용요청")) {
 	    	bsnNo = "0505";
 	    }
 	    else if(mJobGubun.contains("연식조회")) {
 	    	bsnNo = "0602";
 	    }
 	    else if(mJobGubun.contains("납품확인")) {
 	    	bsnNo = "0512";
 	    }
 	    else if(mJobGubun.contains("대여등록")) {
 	    	bsnNo = "0513";
 	    }
 	    else if(mJobGubun.contains("대여반납")) {
 	    	bsnNo = "0503";
 	    }
        
        mlocScan = (Button) findViewById(R.id.locCdScan);
        mlocScan.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						scan("loc");
					}
				});
        
        mLocCdText = (EditText) findViewById(R.id.scanview_locCd);
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
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							if (barcode.isEmpty()) return;
							requestCheck(barcode, "loc");
						}
					}
				});
        mLocCdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	if (barcode.isEmpty()) return true;
                	requestCheck(barcode, "loc");
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
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.trim();
							if (barcode.isEmpty()) return;
							requestCheck(barcode, "fac");
							
						}
					}
				});
        mFacCdText.setOnEditorActionListener(
        		new TextView.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	String barcode = v.getText().toString().trim();
		                	if (barcode.isEmpty()) return true;
		                	requestCheck(barcode, "fac");
		                    return true;
		                }
		                return false;
		            }
		        });
        
        mSnCdScan= (Button) findViewById(R.id.snCdScan);
        mSnCdScan.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						scan("sn");
					}
				});
        
        mSnCdText = (EditText) findViewById(R.id.scanview_snCd);
        mSnCdText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	mSnCdText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
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
							requestCheck(barcode, "sn");
							
						}
					}
				});
        mSnCdText.setOnEditorActionListener(
        		new TextView.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	String barcode = v.getText().toString().trim();
		                	if (barcode.isEmpty()) return true;
		                	requestCheck(barcode, "sn");
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
        
        mSend = (Button) findViewById(R.id.base_crud_send);
        mSend.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						send();
					}
				});
        
        GridLayout resultView = (GridLayout) findViewById(R.id.resultView);
        if(mJobGubun.contains("연식조회")){
        	mSend.setVisibility(View.GONE);
        	resultView.setVisibility(View.VISIBLE);
        }else{
        	mInquery.setVisibility(View.GONE);
        	resultView.setVisibility(View.GONE);
        }
        
        if(mLocInputbar.getVisibility() == View.VISIBLE){
        	mScannerHelper.focusEditText(mLocCdText);
        }else{
        	mScannerHelper.focusEditText(mFacCdText);
        }
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
    
    private void scan(String type){
    	if(type.equals("loc")){
    		Intent intent = new Intent(getApplicationContext(),ActivityCapture.class);
			startActivityForResult(intent, REQ_CODE_SCAN_LOCATION);
    	}else if(type.equals("fac")){
    		Intent intent = new Intent(getApplicationContext(),ActivityCapture.class);
			startActivityForResult(intent, REQ_CODE_SCAN_FACILITY);
    	}else{
    		Intent intent = new Intent(getApplicationContext(),ActivityCapture.class);
			startActivityForResult(intent, REQ_CODE_SCAN_SNCODE);
    	}
    }
    
 	@Override
 	protected void onActivityResult(int reqCode, int resCode, Intent data) {
 		if (resCode != RESULT_OK) return;
 		String barcode = data.getStringExtra("BARCODE");
 		
 		if (TextUtils.isEmpty(barcode))  return;

 		switch (reqCode) {
 			case REQ_CODE_SCAN_LOCATION:
 				if (barcode.length() > 0) {
 					requestCheck(barcode, "loc");
 				}
 				break;
 			case REQ_CODE_SCAN_FACILITY:
 				if (barcode.length() > 0) {
 					requestCheck(barcode, "fac");
 				} 
 				break;
 			case REQ_CODE_SCAN_SNCODE:
 				if (barcode.length() > 0) {
 					requestCheck(barcode, "sn");
 				} 
 		}
 	}
 	
 	private void inquery(){
 		requestCheck("", "all");
 	}
 	
 	private void requestCheck(String barcode, String check){
 		if(check.equals("all")){
 			if(mLocCdText.isShown()){
 				if(mLocCdText.getText().toString().length() < 1){
 					mScannerHelper.focusEditText(mLocCdText);
 	 				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔해 주세요.", BarcodeSoundPlay.SOUND_ERROR));
 	            	return;
 	 			}
 			}
 			
 			if(mFacCdText.isShown()){
 				if(mFacCdText.getText().toString().length() < 10){
 					mScannerHelper.focusEditText(mFacCdText);
 	 				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드를 스캔해 주세요.", BarcodeSoundPlay.SOUND_ERROR));
 	            	return;
 	 			}
 			}
 			getBaseBarcodeData();
 		}else if(check.equals("loc")){
        	if(barcode.length() > 10){
        		mLocCdText.setText("");
        		mScannerHelper.focusEditText(mLocCdText);
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 위치바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
            	return;
        	}
        	mLocCdText.setText(barcode);
        	mScannerHelper.focusEditText(mFacCdText);
 			
 		}else if(check.equals("fac")){
        	if(barcode.length() != 17 && barcode.length() != 18){
        		mFacCdText.setText("");
        		mScannerHelper.focusEditText(mFacCdText);
        		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다.", BarcodeSoundPlay.SOUND_ERROR));
            	return;
        	}
        	mFacCdText.setText(barcode);
        	if(mSnCdText.getVisibility() == View.VISIBLE){
        		mScannerHelper.focusEditText(mSnCdText);
        	}
 		}else if(check.equals("sn")){
 			mSnCdText.setText(barcode);
 			return;
 		}
 	}
 	
 	/**
	 * 검색 조회 
	 */
	public void getBaseBarcodeData() {
		String locationCode = "";
		String facCode = "";
		String snCode = "";
		int type = 2;
		
		if(mLocCdText.isShown()){
			locationCode = mLocCdText.getText().toString();
			type = 1;
		}
		
		if(mFacCdText.isShown()){
			facCode = mFacCdText.getText().toString();
			if(mJobGubun.contains("불용요청")){
				type = 1;
			}
		}
		
		if(mSnCdText.isShown()){
			snCode = mSnCdText.getText().toString();
			if(mJobGubun.contains("납품확인") ||mJobGubun.contains("신규등록")){
				type = 5;
			}
		}
		
		if (mSearchBaseInTask == null) {
			mSearchBaseInTask = new SearchBaseInTask(locationCode, facCode, bsnNo, type, snCode);
			mSearchBaseInTask.execute((Void) null);
		}
	}
	
	public class SearchBaseInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private List<BaseBarcodeInfo> _BaseBarcodeInfos;
		private JSONArray _JsonResults = null;
		String _locCd, _facCd, _bsnNo, _snCd;
		int _type;
		
		public SearchBaseInTask(String locCd, String facCd, String bsnNo, int type, String snCd) {
			_locCd = locCd;
			_facCd = facCd;
			_bsnNo = bsnNo;
			_type = type;
			_snCd = snCd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_JsonResults = basehttp.getBaseManagementRequest(_locCd,_facCd, _bsnNo, _type, type, _snCd);
				
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1,"조회된 결과값이 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "조회된 결과값이 없습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mSearchBaseInTask = null;
			if (result) {
				if(_JsonResults.length() == 0){
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 결과값이 없습니다.", BarcodeSoundPlay.SOUND_ERROR));
	            	return;
				}
				
				try {
	            	JSONObject jsonobj = _JsonResults.getJSONObject(0);
	            	
	            	if(mJobGubun.contains("연식조회")){
	            		if(jsonobj.has("T_MESS")){
	            			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, jsonobj.getString("T_MESS"), BarcodeSoundPlay.SOUND_ERROR));
	            		}else{
	            			TextView gridValue1 = (TextView) findViewById(R.id.gridValue1);
	            			gridValue1.setText(jsonobj.getString("STAT").trim());
	            			
	            			TextView gridValue2 = (TextView) findViewById(R.id.gridValue2);
	            			gridValue2.setText(jsonobj.getString("REGDATE").trim());
	            			
	            			TextView gridValue3 = (TextView) findViewById(R.id.gridValue3);
	            			gridValue3.setText(jsonobj.getString("STANDNAME").trim());
	            			
	            			TextView gridValue4 = (TextView) findViewById(R.id.gridValue4);
	            			gridValue4.setText(jsonobj.getString("STAND").trim());
	            			
	            			TextView gridValue5 = (TextView) findViewById(R.id.gridValue5);
	            			gridValue5.setText(jsonobj.getString("EQUIP").trim());
	            			
	            			TextView gridValue6 = (TextView) findViewById(R.id.gridValue6);
	            			gridValue6.setText(jsonobj.getString("PRODUCTREGDATE").trim());
	            			
	            			TextView gridValue7 = (TextView) findViewById(R.id.gridValue7);
	            			gridValue7.setText(jsonobj.getString("ITEMNM").trim());
	            			
	            			TextView gridValue8 = (TextView) findViewById(R.id.gridValue8);
	            			gridValue8.setText(jsonobj.getString("SN").trim());
	            			
	            			TextView gridValue9 = (TextView) findViewById(R.id.gridValue9);
	            			gridValue9.setText(jsonobj.getString("MNF").trim());
	            		}
	            		
	            	}else{
	            		String message = "";
	            		String e_type = jsonobj.getString("T_ERR_TYPE");
	            		if(e_type.equals("E")){
	            			message = jsonobj.getString("T_MESS");
	            		}else{
	            			message = "정상처리 되었습니다.";
	            		}
	            		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message, BarcodeSoundPlay.SOUND_ERROR));
	            	}
	    		} catch (JSONException e) {
	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요." ));
	    			return;
	    		}
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mSearchBaseInTask = null;
		}
	}
	
	public void send(){
		getBaseBarcodeData();
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
		super.onDestroy();
	}
}
