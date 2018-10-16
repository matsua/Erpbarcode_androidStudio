package com.ktds.erpbarcode.ism;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.LocBarcodeService;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeConvert;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.barcode.model.ProductInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.SelectProductActivity;
import com.ktds.erpbarcode.management.AddrInfoActivity;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class EtcEquipmentActivity extends Activity {

	private static final String TAG = "EtcEquipmentActivity";
	private static final int ACTION_SELECTPRODUCTACTIVITY = 1;
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mOrgCodeText;
	//private Button mLocSearchButton;
    private EditText mLocCdText;
    private EditText mLocNameText;
    
    private LocBarcodeInfo mThisLocCodeInfo;
    
    private EditText mProductCodeText;
    private Button mProductSearchButton;
    private EditText mProductNameText;
    
    private ProductInfo mThisProductInfo;
    
    private EditText mProductClassText;   // 자재분류
    private EditText mAssetLargeText;     // 자산분류(대)
    private EditText mAssetMiddleText;    // 자산분류(중)
    private EditText mAssetSmallText;     // 자산분류(소)
    private EditText mAssetDetailText;     // 자산분류(세)
    private EditText mManufacturerText;   // 제조사
    private EditText mManufacturerSNText; // 제조사SN
    private EditText mItemTypeText;       // 품목구분
    private EditText mPartTypeText;       // 부품종류
    private EditText mRegDateText;        // 등록일자
    private Calendar mCalendar = Calendar.getInstance();
    private Switch mMaterialYn;
    private EditText mRequestNumberText;  // 요청건수
    
	private LogicalLocationInTask mLogicalLocationInTask;

	private Button mRequestButton;
    
    private RelativeLayout mBarcodeProgress;
    
	private Button mAddInfo;

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
		setContentView(R.layout.ism_etcequipment_activity);
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
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

    private void setLayout() {
    	// 운용조직
    	//mOrgInputbar = (LinearLayout) findViewById(R.id.etcequipment_organization_inputbar);
    	mOrgCodeText = (EditText) findViewById(R.id.etcequipment_organization_orgCode);
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        }
    	mOrgCodeText.setText(orgInfo);

    	// 위치코드 
        mLocCdText = (EditText) findViewById(R.id.etcequipment_loc_locCd);
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
							barcode = barcode.toUpperCase().trim();
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
        mLocNameText = (EditText) findViewById(R.id.etcequipment_loc_locName);
        
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
        
        // 자재코드
        mProductCodeText = (EditText) findViewById(R.id.etcequipment_product_productCode);
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
						String barcode = s.toString().toUpperCase();
						
						// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
						if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
							barcode = barcode.toUpperCase().trim();
							Log.d(TAG, "자재코드 Chang Event  barcode==>" + barcode);
							if (barcode.isEmpty()) return;
							// 바코드정보는 Enter값이 추가되어 있다. 꼭 절사바람.
							changeProductCode(barcode.trim());
						}
					}
				});
        mProductCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().trim();
                	Log.d(TAG, "IME_ACTION_SEARCH   barcode==>" + barcode);
                	if (barcode.isEmpty()) return true;
                	changeProductCode(barcode);
                    return true;
                }
                return false;
            }
        });
        mProductSearchButton = (Button) findViewById(R.id.etcequipment_product_search_button);
        mProductSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showSelectProductActivity();
					}
				});
        mProductNameText = (EditText) findViewById(R.id.etcequipment_product_productName);
        
        mProductClassText = (EditText) findViewById(R.id.etcequipment_product_productClass);
        mAssetLargeText = (EditText) findViewById(R.id.etcequipment_product_assetLarge);
        mAssetMiddleText = (EditText) findViewById(R.id.etcequipment_product_assetMedium);
        mAssetSmallText = (EditText) findViewById(R.id.etcequipment_product_assetSmall);
        mAssetDetailText = (EditText) findViewById(R.id.etcequipment_product_assetDetail);
        mManufacturerText = (EditText) findViewById(R.id.etcequipment_product_manufacturer);
        mManufacturerSNText = (EditText) findViewById(R.id.etcequipment_product_manufacturerSN);
        mItemTypeText = (EditText) findViewById(R.id.etcequipment_product_itemType);
        mPartTypeText = (EditText) findViewById(R.id.etcequipment_product_partType);
        mRegDateText = (EditText) findViewById(R.id.etcequipment_regDate);
        mRegDateText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			                return false;
			            case MotionEvent.ACTION_UP:
			            	new DatePickerDialog(EtcEquipmentActivity.this,
			            			mDateSetListener,
                                    mCalendar.get(Calendar.YEAR),
                                    mCalendar.get(Calendar.MONTH),
                                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
			                return true;
			            }
			            return false;
			        }
			    });
        
        mMaterialYn = (Switch) findViewById(R.id.etcequipment_materialYn);
        mRequestNumberText = (EditText) findViewById(R.id.etcequipment_requestNumber);
        
        mRequestButton = (Button) findViewById(R.id.etcequipment_crud_request);
        mRequestButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						int requestNumber = 0;
						try {
							requestNumber = Integer.valueOf(mRequestNumberText.getText().toString().trim());
						} catch (Exception e) {
							requestNumber = 0;
						}
						
						SendCheck sendCheck = new SendCheck();
						sendCheck.orgCode = SessionUserData.getInstance().getOrgId();
						if (mThisLocCodeInfo == null) {
				    		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
				    	} else {
				    		sendCheck.locBarcodeInfo = mThisLocCodeInfo;
				    	}
						sendCheck.productInfo = mThisProductInfo;
						sendCheck.requestNumber = requestNumber;
						sendCheck.manufacturerSN = mManufacturerSNText.getText().toString().trim();
						sendCheck.regDate = mRegDateText.getText().toString().trim().replace("-", "");
						sendCheck.materialYn = mMaterialYn.isChecked();
						executeSendCheck(sendCheck);
					}
				});
        
        mBarcodeProgress = (RelativeLayout) findViewById(R.id.etcequipment_barcodeProgress);
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



	DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                	mCalendar.set(year, monthOfYear, dayOfMonth);
                	
                	String dateString = DateFormat.format("yyyy-MM-dd", mCalendar).toString();
                	
                	mRegDateText.setText(dateString);
                }
            };
    
    /**
     * 화면 초기화..
     */
    private void initScreen() {
    	initScreen("all");
    	mScannerHelper.focusEditText(mLocCdText);
    }   
    private void initScreen(String step) {
    	if (step.equals("all") || step.equals("base")) {
			GlobalData.getInstance().setGlobalProgress(false);
			GlobalData.getInstance().setGlobalAlertDialog(false);
			GlobalData.getInstance().setChangeFlag(false);
		}
		if (step.equals("all") || step.equals("loc")) {
    		mLocCdText.setText("");
    		mLocNameText.setText("");
    		getLogicalLocationData();	// 조직 논리 창고 자동 셋팅			
    	}
		if (step.equals("all") || step.equals("product")) {
			mThisProductInfo = null;
    		mProductCodeText.setText("");
    		mProductNameText.setText("");
    		mProductClassText.setText("");
            mAssetLargeText.setText("");
            mAssetMiddleText.setText("");
            mAssetSmallText.setText("");
            mAssetDetailText.setText("");
            mManufacturerText.setText("");
            mManufacturerSNText.setText("");
            mItemTypeText.setText("");
            mPartTypeText.setText("");
    	}
		if (step.equals("all") || step.equals("req")) {
    		mRegDateText.setText(SystemInfo.getNowDate("-"));
            mRequestNumberText.setText("1");
    	}
    }
    
	/**
	 * 논리위치바코드(창고위치) 정보 조회.
	 */
	public void getLogicalLocationData() {
		if (mLogicalLocationInTask == null) {
			mLogicalLocationInTask = new LogicalLocationInTask();
			mLogicalLocationInTask.execute((Void) null);
		}
	}
	
	private class LogicalLocationInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private JSONArray _JsonResults = null;
		
		public LogicalLocationInTask() {
			setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController lochttp = new LocationHttpController();
				_JsonResults = lochttp.getLogicalLocationCode();
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1, "논리위치바코드(창고위치) 결과 정보가 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "논리위치바코드(창고위치)  서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
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
				Log.d(TAG, _ErpBarException.getErrMessage());
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
		
		String roomTypeCode = "";
        String locCd = "";
        String locName = "";
		
		for (int i=0;i<jsonResults.length();i++) {
			try {
				JSONObject jsonobj = jsonResults.getJSONObject(i);
				
				roomTypeCode = jsonobj.getString("storageType");
				locCd = jsonobj.getString("storageLocationCode");
				locName = jsonobj.getString("storageLocationName");
				
				Log.d(TAG, "논리위치바코드(창고위치)  roomTypeCode==>"+roomTypeCode);
				Log.d(TAG, "논리위치바코드(창고위치)  locCd==>"+locCd);
				Log.d(TAG, "논리위치바코드(창고위치)  locName==>"+locName);
				Log.d(TAG, "논리위치바코드(창고위치)  getOrgTypeCode==>"+SessionUserData.getInstance().getOrgTypeCode());
				
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
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "사용자의 위치정보 유효성 체크(JSON)중 오류가 발생했습니다." + e.getMessage()));
				return;
			} catch (Exception e) {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "사용자의 위치정보 유효성 체크중 오류가 발생했습니다." + e.getMessage()));
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
	    
    // 위치바코드
    private void changeLocCd(String barcode) {
    	if (barcode.isEmpty()) return;
    	
    	barcode = barcode.toUpperCase();
    	
    	getLocBarcodeData(barcode);
    }
    
    private void changeProductCode(String barcode) {
    	if (barcode.isEmpty()) return;
    	setBarcodeProgressVisibility(true);
    	
    	barcode = barcode.toUpperCase();
    	
		initScreen("product");
		initScreen("req");
    	
    	String productCode = mProductCodeText.getText().toString().toUpperCase();
    	if (productCode.isEmpty()) {
    		mProductCodeText.setText(barcode);
    	}
    	
    	
    	getProductInfoData(barcode);
    }
    
    private void showSelectProductActivity() {
    	//-----------------------------------------------------------
    	// Activity가 열릴때 스캔처리하면 오류 발생될수 있으므로 null처리한다.
    	//   ** 해당Activity에서 스캔 들어오면 Error 발생됨.
    	//-----------------------------------------------------------
    	mScannerHelper.focusEditText(null);
    	
    	Intent intent = new Intent(getApplicationContext(), SelectProductActivity.class);
		intent.putExtra(SelectProductActivity.INPUT_MODE, SelectProductActivity.INPUT_MODE_SEARCH_RESULT);
		startActivityForResult(intent, ACTION_SELECTPRODUCTACTIVITY);
    }
    
    /****************************************************************
	 * 위치바코드 정보 조회.
	 */
	private void getLocBarcodeData(String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);

		mThisLocCodeInfo = null;
		LocBarcodeService locbarcodeService = new LocBarcodeService(getApplicationContext(), mLocBarcodeHandler);
		locbarcodeService.search(barcode);
	}
	/**************************************************************************
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
		Log.d(TAG, "위치바코드 getLocCd==>"+mThisLocCodeInfo.getLocCd());
		Log.d(TAG, "위치바코드 getLocName==>"+mThisLocCodeInfo.getLocName());
		Log.d(TAG, "위치바코드 getDeviceId==>"+mThisLocCodeInfo.getDeviceId());
		Log.d(TAG, "위치바코드 getRoomTypeCode==>"+mThisLocCodeInfo.getRoomTypeCode());
		Log.d(TAG, "위치바코드 getRoomTypeName==>"+mThisLocCodeInfo.getRoomTypeName());
		Log.d(TAG, "위치바코드 getOperationSystemCode==>"+mThisLocCodeInfo.getOperationSystemCode());
		
		mLocCdText.setText(mThisLocCodeInfo.getLocCd());
		mLocNameText.setText(mThisLocCodeInfo.getLocName());
		
		//-----------------------------------------------------------
		// 자재코드로 바코드 Focus 이동한다.
		//-----------------------------------------------------------
		mScannerHelper.focusEditText(mProductCodeText);
    }
    
    private void getProductInfoData(String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
		PDABarcodeService pdaBarcodeService = new PDABarcodeService(getApplicationContext(), new ProductInfosInfoHandler());
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_EXPANSION_PRODUCT_AND_ASSETCLASS, barcode);
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
            	Log.i(TAG, "mProductInfosInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<ProductInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToProductInfos(findedMessage);


            	mThisProductInfo = pdaItems.get(0);
            	agoProductInfoCheck();

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
    
    //public class AgoCheck {
    //	public ProductInfo productItem = null;
    //}
    private void agoProductInfoCheck() {
    	
		String partTypeCode = mThisProductInfo.getPartTypeCode();
		String devType = mThisProductInfo.getDevType();
		if (partTypeCode.equals("40") && devType.equals("40")) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "부품종류가 존재하지 않습니다.\n\r기준정보 관리자(MDM)에게\n\r문의하세요."));
			return;
		}
		
		if (!mThisProductInfo.getMtart().equals("ERSA") || !mThisProductInfo.getBarcd().equals("Y")) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "처리할 수 없는 자재코드입니다.\n\r(SAP 상의 자재유형이 'ERSA'가 아니거나 바코드라벨링이‘Y’가 아님)"));
			return;
		}
		
		String partType = SuportLogic.getPartType(partTypeCode, devType);
		String partTypeName = SuportLogic.getNodeStringType(partType);
    	String devTypeName = SuportLogic.getDevTypeName(devType);      // 품목구분코드명
    	
		
		mProductNameText.setText(mThisProductInfo.getProductName());  // 자재명
		mProductClassText.setText(mThisProductInfo.getItemClassificationName());   // 자재분류
		mAssetLargeText.setText(mThisProductInfo.getAssetClassInfo().getAssetLargeName());          // 자산분류(대)
		mAssetMiddleText.setText(mThisProductInfo.getAssetClassInfo().getAssetMiddleName());        // 자산분류(중)
		mAssetSmallText.setText(mThisProductInfo.getAssetClassInfo().getAssetSmallName());          // 자산분류(소)
		mAssetDetailText.setText(mThisProductInfo.getAssetClassInfo().getAssetDetailName());        // 자산분류(세)
		mManufacturerText.setText(mThisProductInfo.getZemaft_name());              // 제조사
		mManufacturerSNText.setText("");      // 제조사SN
		mItemTypeText.setText(devTypeName);   // 품목구분
		mPartTypeText.setText(partTypeName);  // 부품종류
    }

    /**
     * 전송시 유효성 체크 Class
     */
	public class SendCheck {
		public String orgCode = "";
		public LocBarcodeInfo locBarcodeInfo = null;
		public ProductInfo productInfo = null;
		public int requestNumber = 0;
		public String manufacturerSN = "";
		public String regDate = "";
		public boolean materialYn = false;
	}
	
	/**
	 * 바코드 작업내용 전송하기..
	 */
	private void executeSendCheck(final SendCheck sendCheck) {
		Log.d(TAG, "executeSendCheck  Start...");
		
		if (sendCheck.locBarcodeInfo == null || sendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
			mScannerHelper.focusEditText(mLocCdText);			 
			return;
		}
		
		if (sendCheck.productInfo == null) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "자재코드를 입력하세요."));
			mScannerHelper.focusEditText(mProductCodeText);
			return;
		}
		
		if (sendCheck.productInfo.getAssetClassInfo() == null) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "자산분류 정보가 없습니다."));
			mScannerHelper.focusEditText(mProductCodeText);
			return;
		}
		
		int requestNumber = 0;
		try {
			requestNumber = Integer.valueOf(mRequestNumberText.getText().toString().trim());
		} catch (Exception e) {
			requestNumber = 0;
		}
		
		if (requestNumber == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "요청개수를 정수로 입력하세요."));
			return;
		}
		if (requestNumber > 30) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "요청개수를 30개 이하로\n\r입력하세요"));
			return;
		}

		//---------------------------------------------------------------------
		// 전송여부 최종 확인.
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
				
				threadPoolExecutors(sendCheck);
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
	
	private void threadPoolExecutors(SendCheck sendCheck) {

		setBarcodeProgressVisibility(true);
		//---------------------------------------------------------------------
		// **주의** for 1부터 시작한다.
		//---------------------------------------------------------------------
		ExecutorService executor = Executors.newFixedThreadPool(1);
		for (int i=1; i<=sendCheck.requestNumber; i++) {
			SendDataInTask task = new SendDataInTask(sendCheck);
			executor.submit(task);
		}
		
		executor.shutdown();
		
		// 모든 쓰레드가 완료되었는지 체크하여 Progress 종료한다.
		boolean isTerminated = false;
		while (!isTerminated) {
			if (executor.isTerminated()) {
				Log.d(TAG, "threadPoolExecutors  success...");
				setBarcodeProgressVisibility(false);

				isTerminated = true;
                String message = "# 전송건수 : " + sendCheck.requestNumber + "건\n\n1-정상 전송되었습니다."; 
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				initScreen();
			}
		}
	}
	
	private class SendDataInTask implements Runnable {
		final SendCheck _SendCheck;
		OutputParameter _OutputParameter;
		
		public SendDataInTask(SendCheck sendCheck) {
			_SendCheck = sendCheck;
		}

			
		@Override
		public void run() {
			Log.d(TAG, "SendDataInTask  Start...");

			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
			try {
				jsonParam.put("ZKOSTL", _SendCheck.orgCode);      // 운용조직
				jsonParam.put("PBLS_CNT", "1");
				jsonParam.put("GNRT_REQ_TP_CD", "IMD");
				jsonParam.put("GNRT_TARG_CD", "INS");
				jsonParam.put("PBLS_WHY_CD", "2");
				jsonParam.put("ZEQART1", _SendCheck.productInfo.getAssetClassInfo().getAssetLargeCode());  // 자산분류(대)
				jsonParam.put("ZEQART2", _SendCheck.productInfo.getAssetClassInfo().getAssetMiddleCode()); // 자산분류(중)
				jsonParam.put("ZEQART3", _SendCheck.productInfo.getAssetClassInfo().getAssetSmallCode());  // 자산분류(소)
				jsonParam.put("ZEQART4", _SendCheck.productInfo.getAssetClassInfo().getAssetDetailCode()); // 자산분류(세)
				jsonParam.put("SUBMT", _SendCheck.productInfo.getProductCode());   // 자재코드
				jsonParam.put("SERGE", _SendCheck.manufacturerSN);                 // 제조사S/N
				jsonParam.put("OBT_DAY", _SendCheck.regDate);                      // 등록일자
				jsonParam.put("ZPTART", _SendCheck.productInfo.getPartTypeCode()); // 부품종류
				jsonParam.put("PGUBUN", _SendCheck.productInfo.getDevType());      // 품목분류
				
				jsonParam.put("ZEQUIPLP", _SendCheck.locBarcodeInfo.getLocCd());   // 위치바코드
				//-------------------------------------------------------------
				// device의 위도,경도 정보를 위치바코드와 같이 전송한다.
				//-------------------------------------------------------------
				// GPS 위치조회 하지 않는 방법으로 변경. 16.11.22
//				jsonParam.put("LATITUDE", String.valueOf(SessionUserData.getInstance().getLatitude()));
//				jsonParam.put("LONGTITUDE", String.valueOf(SessionUserData.getInstance().getLongitude()));
//				jsonParam.put("DIFF_TITUDE", String.valueOf(_SendCheck.locBarcodeInfo.getDiffTitude()));
				//-------------------------------------------------------------
				
				
				String materialYn = "B";
				if (_SendCheck.materialYn) materialYn = "M";
				jsonParam.put("ZKEQUI", materialYn);              // 제조사물자여부

			} catch (JSONException e) {
				Log.d(TAG, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e.getMessage());
				return;
			} catch (Exception e) {
				Log.d(TAG, "작업데이터 파라메터서브리스트 대입중 오류가 발생했습니다. " + e.getMessage());
				return;
			}
			jsonParamList.put(jsonParam);

			try {
				SendHttpController sendhttp = new SendHttpController();
				_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_INSTOREMARKING, jsonParamList, null);
				
				if (_OutputParameter == null) {
					throw new ErpBarcodeException(-1, "'" + GlobalData.getInstance().getJobGubun() + "' 정보 전송중 오류가 발생했습니다.");
				}

			} catch (ErpBarcodeException e) {
				Log.d(TAG, e.getErrMessage());
				return;
			}
			return;
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
    	
        //------------------------------------------------------------
    	// PopUp Activity 호출후에는 무조건 스케너 Focus를 지정한다.
    	//------------------------------------------------------------
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SELECTPRODUCTACTIVITY) {
				String productCode = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_CODE);
				String productName = data.getExtras().getString(SelectProductActivity.OUTPUT_PRODUCT_NAME);
				Log.d(TAG, "ACTION_SELECTPRODUCTACTIVITY   productCode==>"+productCode);
				Log.d(TAG, "ACTION_SELECTPRODUCTACTIVITY   productName==>"+productName);
				if (!productCode.isEmpty()) {
					mProductCodeText.setText(productCode);
					changeProductCode(productCode);
				}
			}
		} else {
			if (requestCode == ACTION_SELECTPRODUCTACTIVITY) {
				mScannerHelper.focusEditText(mProductCodeText);
			}
		}
    }
}
