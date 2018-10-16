package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.ProductInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.database.BpIItemQuery;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;


public class SelectProductActivity extends Activity {

	private static final String TAG = "SelectProductActivity";
	
	public static final String INPUT_MODE = "display_mode";
	public static final int INPUT_MODE_SEARCH = 1;
	public static final int INPUT_MODE_SEARCH_RESULT = 2;
	
	public static final String OUTPUT_PRODUCT_CODE = "productCode";
	public static final String OUTPUT_PRODUCT_NAME = "productName";
	
	private int p_mode = INPUT_MODE_SEARCH;
	
	private ScannerConnectHelper mScannerHelper;
	private BpIItemQuery bpIItemQuery;

	private EditText mProductCodeText;
	private EditText mProductNameText;
	private Button mSearchButton;
	private ProductInfosServerInTask mProductInfosServerInTask;
	private ListView mProductListView;
	private ProductListAdapter mProductListAdapter;
	
	private TextView mTotalCount;
	private Button mChoiceButton;
	private Button mCancelButton;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//-----------------------------------------------------------
    	// input parameter
    	//-----------------------------------------------------------
		p_mode = getIntent().getIntExtra(INPUT_MODE, INPUT_MODE_SEARCH);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		bpIItemQuery = new BpIItemQuery(getApplicationContext());
		bpIItemQuery.open();
		initBarcodeScanner();
		setMenuLayout();
		setContentView(R.layout.infosearch_selectproduct_activity);
		setLayout();
		initScreen();
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

	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
		// 자재코드
        mProductCodeText = (EditText) findViewById(R.id.selectproduct_productCode);
        mProductCodeText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
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
							barcode = barcode.trim();
							if (barcode.isEmpty()) return;
							changeProductCode(barcode.trim().toUpperCase());
						}
					}
				});
        mProductCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	String barcode = v.getText().toString().toUpperCase().trim();
                	mProductCodeText.setText(barcode);
                	getProductData();
                    return true;
                }
                return false;
            }
        });
        
        mProductNameText = (EditText) findViewById(R.id.selectproduct_productName);
        mProductNameText.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
        mProductNameText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
							mScannerHelper.focusEditText(mProductNameText);
			                return false;
			            }
			            return false;
			        }
			    });
        mProductNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	getProductData();
                    return true;
                }
                return false;
            }
        });
        
		mCancelButton = ((Button) findViewById(R.id.selectproduct_clear_button));
		mCancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						initScreen();
					}
				});
        mSearchButton = ((Button) findViewById(R.id.selectproduct_search_button));
        mSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						getProductData();
					}
				});
		mChoiceButton = ((Button) findViewById(R.id.selectproduct_choice_button));
		mChoiceButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						choiceProductInfo();
					}
				});

        mProductListAdapter = new ProductListAdapter(getApplicationContext());
		
		mProductListView = (ListView) findViewById(R.id.selectproduct_listView);
		mProductListView.setAdapter(mProductListAdapter);
		mProductListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				mProductListAdapter.changeSelectPosition(position);
			}
		});
		
		mTotalCount = (TextView) findViewById(R.id.selectproduct_listfloor_totalCount);
	}
	
	private void initScreen() {
    	Log.d(TAG, "initScreen Start...");
    	
    	mProductCodeText.setText("");
    	mProductNameText.setText("");
    	mTotalCount.setText("");
    	
    	if (p_mode == INPUT_MODE_SEARCH) {
    		mCancelButton.setVisibility(View.GONE);
    		mChoiceButton.setVisibility(View.GONE);
    	} else {
    		mCancelButton.setVisibility(View.VISIBLE);
    		mChoiceButton.setVisibility(View.VISIBLE);
    	}
    	
    	mProductListAdapter.itemClear();
    	
    	//mScannerHelper.focusEditText(mProductCodeText);
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
	@Override
	protected void onResume() {
		bpIItemQuery.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		bpIItemQuery.close();
		super.onPause();
	}
	
    /**
     *  Progress를 관리한다.
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
	
    /**
     * 자재코드 정보 변경시 처리.
     */
	private void changeProductCode(String barcode) {
		barcode = barcode.toUpperCase();

        String facCd = mProductCodeText.getText().toString().toUpperCase();
    	if (facCd.isEmpty()) {
    		mProductCodeText.setText(barcode);
    	}
    	
    	//-----------------------------------------------------------
    	// SoftKeyBoard 를 종료한다.
    	//-----------------------------------------------------------
    	InputMethodManager immProductCode = ( InputMethodManager ) mProductCodeText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
    	if (immProductCode.isActive())
    		immProductCode.hideSoftInputFromWindow(mProductCodeText.getApplicationWindowToken() , 0);
    	
    	getProductInfosServer(barcode, "", "");
	}
	
	/**
	 * 리스트에서 자재정보 선택시에 처리.
	 */
	private void choiceProductInfo() {
		int selectPosition = mProductListAdapter.getSelectPosition();
    	if (selectPosition < 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "자재을 선택하세요.") );
			return;
    	}
    	
    	ProductInfo productInfo = mProductListAdapter.getItem(selectPosition);
    	
    	if (!productInfo.getMtart().equals("ERSA") || !productInfo.getBarcd().equals("Y")) {
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "처리할 수 없는 자재코드입니다.\n\r(SAP 상의 자재유형이 'ERSA'가 아니거나 바코드라벨링이‘Y’가 아님)") );
			return;
    	}
		
		Intent intent = new Intent();
        intent.putExtra(OUTPUT_PRODUCT_CODE, productInfo.getProductCode());
        intent.putExtra(OUTPUT_PRODUCT_NAME, productInfo.getProductName());
		setResult(Activity.RESULT_OK, intent);   
		finish();
	}
	
	/**
	 * 자재 정보 조회.
	 */
	private void getProductData() {
		//-----------------------------------------------------------
    	// SoftKeyBoard 를 종료한다.
    	//-----------------------------------------------------------
    	InputMethodManager codeimm = ( InputMethodManager ) mProductCodeText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
    	if (codeimm.isActive())
    		codeimm.hideSoftInputFromWindow(mProductCodeText.getApplicationWindowToken() , 0);
    	
    	InputMethodManager nameimm = ( InputMethodManager ) mProductNameText.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
    	if (nameimm.isActive())
    		nameimm.hideSoftInputFromWindow(mProductNameText.getApplicationWindowToken() , 0);
		
		String matnr = mProductCodeText.getText().toString().toUpperCase().trim();
		String maktx = mProductNameText.getText().toString().toUpperCase().trim();
		mProductCodeText.setText(matnr);
		mProductNameText.setText(maktx);
		getProductInfosServer(matnr, maktx, "");
	}
	
	
	/**
	 * 자재 정보 조회.
	 */
	private void getProductInfosServer(String productCode, String productName, String bismt) {
		if (isBarcodeProgressVisibility()) return;
		mProductCodeText.setText(productCode.toUpperCase());
		mProductListAdapter.itemClear();
		mTotalCount.setText("");
		
		if (mProductInfosServerInTask == null) {
			setBarcodeProgressVisibility(true);
			mProductInfosServerInTask = new ProductInfosServerInTask(productCode, productName, bismt);
			mProductInfosServerInTask.execute((Void) null);
		}
	}
	
	public class ProductInfosServerInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		private String _ProductCode = "";
		private String _ProductName = "";
		private String _Bismt = "";
		
		private JSONArray _JsonResults;
		
		public ProductInfosServerInTask(String productCode, String productName, String bismt) {
			_ProductCode = productCode;
			_ProductName = productName;
			_Bismt = bismt;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				_JsonResults = barcodehttp.getBarcodeInfosServer(_ProductCode, _ProductName, _Bismt);
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 자재코드입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "자재정보조회 서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mProductInfosServerInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				if (_JsonResults.length() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "자재정보가 존재하지 않습니다."));
					return;
				}
				
				List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
				for (int i=0;i<_JsonResults.length();i++) {
					try {
						JSONObject jsonobj = _JsonResults.getJSONObject(i);
						ProductInfo productInfo = BarcodeInfoConvert.jsonToProductInfo(i, jsonobj);
						productInfos.add(productInfo);
					} catch (JSONException e) {
						GlobalData.getInstance().showMessageDialog(
								new ErpBarcodeException(-1, "자재정보 대입(JSON)중 오류가 발생했습니다." + e.getMessage()));
						return;
					}
				}
				if (productInfos.size() > 0) {
					mProductListAdapter.addItems(productInfos);
					
					// 조회건수를 보여준다.
					String totalCount = String.valueOf(mProductListAdapter.getCount());
					mTotalCount.setText(totalCount + "건");
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
			mProductInfosServerInTask = null;
		}
	}
	
}
