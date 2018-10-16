package com.ktds.erpbarcode.ism;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.ktds.erpbarcode.env.bluetooth.PairingActivity;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.infosearch.SelectOrgCodeActivity;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;
import com.ktds.erpbarcode.management.DeleteWhyInfoActivity;
import com.ktds.erpbarcode.management.model.SendHttpController;
import com.ktds.erpbarcode.print.zebra.BarcodePrintController;
import com.ktds.erpbarcode.print.zebra.PrinterSettingActivity;
import com.ktds.erpbarcode.print.zebra.SettingsHelper;

public class IsmManagementActivity extends Activity {
	private static final String TAG = "IsmManagementActivity";  
	private static final int ACTION_SELECTORGCODEACTIVITY = 1;
	private static final int ACTION_REQUEST_CANCEL = 2;
	private static final int ACTION_INFO_PRINTER = 3;
	
	private IsmPrintSpinnerInTask mIsmPrintSpinnerInTask;
	private JSONArray mpsArray = new JSONArray();
	private JSONArray mpwArray = new JSONArray();
	private JSONArray mplArray = new JSONArray();
	
	private ScannerConnectHelper mScannerHelper;
	private SettingPreferences mSharedSetting;
	
    private EditText mReceiptOrgCodeText;
    private Button mReceiptOrgSearchButton;
    private String stOrgCd;
    private OutputParameter _OutputParameter;
    private RelativeLayout mIsmProgress;
    
    private EditText mNewBarcode;
    private EditText mInjuryBarcode;
    
    private Spinner mSpinnerSttCode;
	private String mTouchSttCode = null;
	private BasicSpinnerAdapter mSttCodeAdapter;
	
	private Spinner mSpinnerRqrCode;
	private String mTouchRqrCode = null;
	private BasicSpinnerAdapter mRqrCodeAdapter;
	
	private Spinner mSpinnerLbtCode;
	private String mTouchLbtCode = null;
	private BasicSpinnerAdapter mLbtCodeAdapter;
	
	private String mTouchRccCode = null;
	private String mTouchRccCodeDetail = null;
	
	private EditText mDateSText, mDateEText;
	private Calendar mCalendar = Calendar.getInstance();
	private int type;
	
	private ListView mBarcodeListView;
	private IsmRequestListAdapter mIsmRequestListAdapter;
	private FindInstoreMarkingInTask mFindInstoreMarkingInTask;
	private FindInstoreMarkingCancelInTask mFindInstoreMarkingCancelInTask;
	private FindInstoreMarkingPrintCompleteInTask mFindInstoreMarkingPrintCompleteInTask;
	
	private Button mSearch, mCancel, mGenerate, mRepublish, mPrintTest, mPrint, mPrintSetting, mPrintSensor;
	private CheckBox mCheckBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		GlobalData.getInstance().setNowOpenActivity(this);
		
		setContentView(R.layout.ism_management_activity);
		getIsmPrintSpinnerData(0);
	}
	
	private void setMenuLayout() {
		System.out.println("setMenuLayout");
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
		System.out.println("setLayout");
		mReceiptOrgCodeText = (EditText) findViewById(R.id.treescan_organization_orgCode);
		
    	String orgInfo = "";
        if (SessionUserData.getInstance().isAuthenticated()) {
        	orgInfo = SessionUserData.getInstance().getOrgId() + "/" + SessionUserData.getInstance().getOrgName();
        	stOrgCd = SessionUserData.getInstance().getOrgCode();
        }
        
        mReceiptOrgCodeText.setText(orgInfo);
        
        mNewBarcode = (EditText) findViewById(R.id.ism_new_barcode);
        mInjuryBarcode = (EditText) findViewById(R.id.ism_injury_barcode);
        
    	mReceiptOrgSearchButton = ((Button) findViewById(R.id.treescan_receiptOrganization_search_button));
        mReceiptOrgSearchButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showSelectOrgCodeActivity();
					}
				});
        mIsmProgress = (RelativeLayout) findViewById(R.id.ism_barcodeProgress);
        
        mSpinnerSttCode = (Spinner) findViewById(R.id.ism_process_status);
        
		List<SpinnerInfo> sttspinneritems = new ArrayList<SpinnerInfo>();
		sttspinneritems.add(new SpinnerInfo("all", "전체"));
		for(int s = 0; s < mpsArray.length(); s++){
			try {
				JSONObject mpObj = mpsArray.getJSONObject(s);
				sttspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mSttCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), sttspinneritems);
		mSpinnerSttCode.setAdapter(mSttCodeAdapter);
		mSpinnerSttCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo sttCodeSpinnerInfo = (SpinnerInfo) mSttCodeAdapter.getItem(position);
	        	mSpinnerSttCode.setSelection(mSttCodeAdapter.getPosition(sttCodeSpinnerInfo.getCode()));
	        	mTouchSttCode = sttCodeSpinnerInfo.getCode();
	        }
	        public void onNothingSelected(AdapterView<?> arg0) {
	        }
	    });
		
		mSpinnerRqrCode = (Spinner) findViewById(R.id.ism_request_reason);
		List<SpinnerInfo> rqrspinneritems = new ArrayList<SpinnerInfo>();
		rqrspinneritems.add(new SpinnerInfo("all", "전체"));
		for(int s = 0; s < mpsArray.length(); s++){
			try {
				JSONObject mpObj = mpwArray.getJSONObject(s);
				rqrspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mRqrCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), rqrspinneritems);
		mSpinnerRqrCode.setAdapter(mRqrCodeAdapter);
		mSpinnerRqrCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo rqrCodeSpinnerInfo = (SpinnerInfo) mRqrCodeAdapter.getItem(position);
	        	mSpinnerRqrCode.setSelection(mRqrCodeAdapter.getPosition(rqrCodeSpinnerInfo.getCode()));
	        	mTouchRqrCode = rqrCodeSpinnerInfo.getCode();
	        }
	        public void onNothingSelected(AdapterView<?> arg0) {
	        }
	    });
		
		mSpinnerLbtCode = (Spinner) findViewById(R.id.ism_label_setting);
		List<SpinnerInfo> lbtspinneritems = new ArrayList<SpinnerInfo>();
		lbtspinneritems.add(new SpinnerInfo("", "선택하세요."));
		for(int s = 0; s < mplArray.length(); s++){
			try {
				JSONObject mpObj = mplArray.getJSONObject(s);
				lbtspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mLbtCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), lbtspinneritems);
		mSpinnerLbtCode.setAdapter(mLbtCodeAdapter);
		mSpinnerLbtCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo lbtCodeSpinnerInfo = (SpinnerInfo) mLbtCodeAdapter.getItem(position);
	        	mSpinnerLbtCode.setSelection(mLbtCodeAdapter.getPosition(lbtCodeSpinnerInfo.getCode()));
	        	mTouchLbtCode = lbtCodeSpinnerInfo.getCode();
	        }
	        public void onNothingSelected(AdapterView<?> arg0) {
	        }
	    });
		
		mDateSText = (EditText) findViewById(R.id.ism_request_date_start);
        mDateSText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	type = 0;
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			                return false;
			            case MotionEvent.ACTION_UP:
			            	new DatePickerDialog(IsmManagementActivity.this,
			            			mDateSetListener,
                                    mCalendar.get(Calendar.YEAR),
                                    mCalendar.get(Calendar.MONTH),
                                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
			                return true;
			            }
			            return false;
			        }
			    });
        
        mDateSText.setText(SystemInfo.getNowDate("-"));
        
        mDateEText = (EditText) findViewById(R.id.ism_request_date_end);
        mDateEText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	type = 1;
			            switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			                return false;
			            case MotionEvent.ACTION_UP:
			            	new DatePickerDialog(IsmManagementActivity.this,
			            			mDateSetListener,
                                    mCalendar.get(Calendar.YEAR),
                                    mCalendar.get(Calendar.MONTH),
                                    mCalendar.get(Calendar.DAY_OF_MONTH)).show();
			                return true;
			            }
			            return false;
			        }
			    });
        
        mDateEText.setText(SystemInfo.getNowDate("-"));
        
        //검색 
        mSearch = (Button) findViewById(R.id.ism_request);
        mSearch.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getInstoreMarkings();
					}
				});
        
        //요청취소 
        mCancel = (Button) findViewById(R.id.ism_request_cancel);
        mCancel.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						request(0);
					}
				});
        
        //발행 
        mGenerate = (Button) findViewById(R.id.ism_request_generate);
        mGenerate.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						request(1);
					}
				});
        
        //재발행 
        mRepublish = (Button) findViewById(R.id.ism_request_republish);
        mRepublish.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						request(2);
					}
				});
        
//        //프린트 설정  
//        mPrintSetting = (Button) findViewById(R.id.ism_print_setting);
//        mPrintSetting.setOnClickListener(
//				new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						printSetting();
//					}
//				});
//        
        //프린트 보정 
        mPrintSensor = (Button) findViewById(R.id.ism_print_sensor);
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
        
        //출력테스트 
        mPrintTest = (Button) findViewById(R.id.ism_print_test);
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
        mPrint = (Button) findViewById(R.id.ism_print);
        mPrint.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						request(4);
					}
				});
        
        
        mIsmRequestListAdapter = new IsmRequestListAdapter(this);
		
		mBarcodeListView = (ListView) findViewById(R.id.ismrequest_listView);
		mBarcodeListView.setAdapter(mIsmRequestListAdapter);
		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				mIsmRequestListAdapter.changeSelectedPosition(position);
			}
		});
		
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
	
	protected void selectAll() {
    	if (mIsmRequestListAdapter.getAllItems().size() == 0) {
    		return;
    	}
    	
    	for(int i=0; i<mIsmRequestListAdapter.getAllItems().size();i++){
    		mIsmRequestListAdapter.getItem(i).setChecked(!mCheckBox.isChecked());
    	}
    	
    	mCheckBox.setChecked(!mCheckBox.isChecked());
    	mIsmRequestListAdapter.notifyDataSetChanged();
		
	}

	DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                	mCalendar.set(year, monthOfYear, dayOfMonth);
                	String dateString = DateFormat.format("yyyy-MM-dd", mCalendar).toString();
                	if(type == 0){
                		mDateSText.setText(dateString);
                	}else{
                		mDateEText.setText(dateString);
                	}
                }
            };
            
	private void showSelectOrgCodeActivity() {
		Intent intent = new Intent(getApplicationContext(), SelectOrgCodeActivity.class);
		intent.putExtra(SelectOrgCodeActivity.INPUT_JSON_STRING, "");
		startActivityForResult(intent, ACTION_SELECTORGCODEACTIVITY);
    }
	
	private void printSetting() {
		Intent intent = new Intent(getApplicationContext(), PairingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PairingActivity.INPUT_PAIRING_KIND, "printer");
        startActivity(intent);
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		GlobalData.getInstance().setNowOpenActivity(this);
    	if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_SELECTORGCODEACTIVITY) {
				String costCenter = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_CODE);
				String orgName = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_NAME);
				stOrgCd = data.getExtras().getString(SelectOrgCodeActivity.OUTPUT_ORG_ORGCD);
				mReceiptOrgCodeText.setText(costCenter.trim()+"/"+orgName.trim());
			} 
			if (requestCode == ACTION_REQUEST_CANCEL) {
				mTouchRccCode = data.getExtras().getString(DeleteWhyInfoActivity.OUTPUT_SEL_CODE);
				mTouchRccCodeDetail = data.getExtras().getString(DeleteWhyInfoActivity.OUTPUT_SEL_CODE_DETAIL);
				executeSendCheck(0);
			}
    	}
    }
	
	/**
	 * spinner data 조회 
	 */
	public void getIsmPrintSpinnerData(int spinnerType) {
		if (mIsmPrintSpinnerInTask == null) {
			mIsmPrintSpinnerInTask = new IsmPrintSpinnerInTask(spinnerType);
			mIsmPrintSpinnerInTask.execute((Void) null);
		}
	}
	
	public class IsmPrintSpinnerInTask extends AsyncTask<Void, Void, Boolean> {
		private int _spinnerType = 0;
		private ErpBarcodeException _ErpBarException;
		private JSONArray _jsonResults = new JSONArray();
		
		public IsmPrintSpinnerInTask(int spinnerType) {
			_spinnerType = spinnerType;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				_jsonResults = ismhttp.getIsmPrintSpinner(_spinnerType);
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
			mIsmPrintSpinnerInTask = null;
			JSONArray jsonArray_temp = new JSONArray();
			
			super.onPostExecute(result);
			if (result) {
				try {
					for(int i = 0; i < _jsonResults.length(); i++){
						JSONObject jsonobj = _jsonResults.getJSONObject(i);
						JSONObject jsonObject_temp = new JSONObject();
						jsonObject_temp.put("code", jsonobj.getString("commonCode"));
						jsonObject_temp.put("name", jsonobj.getString("commonCodeName"));
						jsonArray_temp.put(jsonObject_temp);
					}
					
					//진행상태 0 , 요청사유 1 , 라벨용지 2
					if(_spinnerType == 0) mpsArray = jsonArray_temp;
					else if(_spinnerType == 1) mpwArray = jsonArray_temp;
					else mplArray = jsonArray_temp;
					
					if(_spinnerType < 2){
						_spinnerType++;
						getIsmPrintSpinnerData(_spinnerType);
					}else{
						setMenuLayout();
						setLayout();
					}
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
			mIsmPrintSpinnerInTask = null;
			super.onCancelled();
		}
	}
	
	/**
	 * 인스토어마킹 조회.
	 */
	private void getInstoreMarkings() {
		if (isBarcodeProgressVisibility()) return;
		
		String orgCode = stOrgCd;
		String sttCode = mTouchSttCode;
		String rqrCode = mTouchRqrCode;
		String newCode = mNewBarcode.getText().toString().trim();
		String ijrcode = mInjuryBarcode.getText().toString().trim();
		String bgiDate = mDateSText.getText().toString().replace("-", "");
		String endDate = mDateEText.getText().toString().replace("-", "");
//		String userId  = SessionUserData.getInstance().getUserId();
		
		if(Integer.parseInt(bgiDate) > Integer.parseInt(endDate)){
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "검색 시작일이 종료일보다 클수 없습니다."));	
			return;
		}
		
		mIsmRequestListAdapter.itemClear();
		
		if (mFindInstoreMarkingInTask == null) {
			setBarcodeProgressVisibility(true);
			
			mFindInstoreMarkingInTask = new FindInstoreMarkingInTask(orgCode, sttCode, rqrCode, newCode, ijrcode, bgiDate, endDate);
			mFindInstoreMarkingInTask.execute((Void) null);
		}
	}
	
	public class FindInstoreMarkingInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		String _orgCode, _sttCode, _rqrCode, _lbtCode, _newCode, _ijrcode, _bgiDate, _endDate;
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		
		public FindInstoreMarkingInTask(String orgCode, String sttCode, String rqrCode,
				String newCode, String ijrcode, String bgiDate, String endDate) {
			_orgCode = orgCode;
			_sttCode = sttCode;
			_rqrCode = rqrCode;
			_newCode = newCode;
			_ijrcode = ijrcode;
			_bgiDate = bgiDate;
			_endDate = endDate;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				_IsmBarcodeInfos = ismhttp.getInstoreMarkingToIsmManagementBarcodeInfos(_orgCode, _sttCode, _rqrCode, _newCode, _ijrcode, _bgiDate, _endDate);
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			Log.d(TAG, "FindInstoreMarkingInTask   for 건수:"+_IsmBarcodeInfos.size());
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingInTask = null;
			mCheckBox.setChecked(false);
			if (result) {
				if (_IsmBarcodeInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 인스토어마킹이 없습니다."));	
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
			mFindInstoreMarkingInTask = null;
		}
	}
	
	/**
	 * 인스토어마킹 요청.
	 */
	private void request(final int type) {
		//0:요청취소, 1:발행, 2:재발행, 3:출력테스트, 4:출력 
		
    	if (mIsmRequestListAdapter.getCheckedItems().size() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
			return;
		}
    	
    	if(type == 3 || type == 4){
    		if(mTouchLbtCode.length() < 1){
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 라벨 사이즈가 없습니다. "));
    			return;
    		}
    	}
    	
    	if(type == 0){
    		Intent intent = new Intent(getApplicationContext(), DeleteWhyInfoActivity.class);
    		startActivityForResult(intent, ACTION_REQUEST_CANCEL);
    	}
//    	else if(type == 3){
//    		int rscode = barcodePrintTest();
//			switch (rscode) {
//			case 1:
//				break;
//			case -1:
//				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "블루투스 연결에\n\r실패 했습니다.", BarcodeSoundPlay.SOUND_ERROR));
//				break;
//			default:
//				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 상태를\n\r확인 하세요.", BarcodeSoundPlay.SOUND_ERROR));
//				break;
//			}
//    	}
    	else{
    		executeSendCheck(type);
    	}
    }
	
	private void executeSendCheck(final int type) {
		//0:요청취소, 1:발행, 2:재발행, 3:출력테스트, 4:출력 
		
		final List<IsmBarcodeInfo> sendBarcodeInfos = mIsmRequestListAdapter.getCheckedItems();
		String resultMsg = "";
		for (IsmBarcodeInfo sendIsmBarcodeInfo : sendBarcodeInfos) {
			if(type == 0){
				if(sendIsmBarcodeInfo.getTransactionStatusCode().equals("BC_CPT")) {
					resultMsg += sendIsmBarcodeInfo.getNewBarcode() + "\n";
					sendIsmBarcodeInfo.setChecked(false);
				}
			}else if(type == 1){
				if(!sendIsmBarcodeInfo.getTransactionStatusCode().equals("BC_REQ")) {
					resultMsg += sendIsmBarcodeInfo.getNewBarcode() + "\n";
					sendIsmBarcodeInfo.setChecked(false);
				}
			}else if(type == 2){
				if(!(sendIsmBarcodeInfo.getTransactionStatusCode().equals("BC_PBL")||!sendIsmBarcodeInfo.getPublicationWhyCode() .equals("1"))) {
					resultMsg += sendIsmBarcodeInfo.getNewBarcode() + "\n";
					sendIsmBarcodeInfo.setChecked(false);
				}
			}else if(type == 4){
				if(!sendIsmBarcodeInfo.getTransactionStatusCode().equals("BC_CRT")) {
					resultMsg += sendIsmBarcodeInfo.getInjuryBarcode() + "\n";
					sendIsmBarcodeInfo.setChecked(false);
				}
			}
		}
		
		if(resultMsg.length() > 1){
			if(type == 0){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "완료건은 삭제불가 합니다."));
			}else if(type == 1){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "요청인 상태만 발행이 가능합니다."));
			}else if(type == 2){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "인쇄 상태고 훼손 사유만 재발행 할 수 있습니다."));
			}else if(type == 4){
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "발행인 상태만 출력 가능합니다."));
			}
//			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "바코드 \n" + resultMsg + "요청 가능한 상태가 아닙니다."));
			return;
		}
		
		if(mIsmRequestListAdapter.getCheckedItems().size() == 0){
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}
		
		
		// ---------------------------------------------------------------------
		// 전송여부 최종 확인..
		// ---------------------------------------------------------------------
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
				if(type == 4){
					dialog.cancel();
					int rscode = barcodePrint();
					switch (rscode) {
					case 1:
						break;
					case -1:
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 연결에\n\r실패 했습니다.", BarcodeSoundPlay.SOUND_ERROR));
						break;
					default:
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "프린터 상태를\n\r확인 하세요.", BarcodeSoundPlay.SOUND_ERROR));
						break;
					}
				}else{
					if (mFindInstoreMarkingCancelInTask == null) {
						setBarcodeProgressVisibility(true);
						mFindInstoreMarkingCancelInTask = new FindInstoreMarkingCancelInTask(type);
						mFindInstoreMarkingCancelInTask.execute((Void) null);
					}
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
		
		final List<IsmBarcodeInfo> sendBarcodeInfos = mIsmRequestListAdapter.getCheckedItems();
		
		BarcodePrintController bpc = new BarcodePrintController();
		String address = SettingsHelper.getBluetoothAddress(IsmManagementActivity.this);
		
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
	
	private int barcodePrintSensor(){
		int result, rscode = -1;
		
		BarcodePrintController bpc = new BarcodePrintController();
		String address = SettingsHelper.getBluetoothAddress(IsmManagementActivity.this);
		
		result = bpc.printOpen(address);
		if(result == 1){
			rscode = bpc.printSensor();
		}else{
			rscode = result;
		}
		return rscode;
	}
	
	// 전송 비동기 테스크
	public class FindInstoreMarkingCancelInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private OutputParameter _OutputParameter;
		private int _type = 0;
		
		public FindInstoreMarkingCancelInTask(int type) {
			//0:요청취소, 1:발행, 2:재발행, 3:출력테스트, 4:출력   
			_type = type;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
    		jsonParamList.put(jsonParam);

			JSONArray jsonSubParamList = new JSONArray();
			final List<IsmBarcodeInfo> sendBarcodeInfos = mIsmRequestListAdapter.getCheckedItems();
			for (IsmBarcodeInfo sendIsmBarcodeInfo : sendBarcodeInfos) {
				
				if (!sendIsmBarcodeInfo.isChecked()) continue;

				JSONObject jsonSubParam = new JSONObject();
				try {
					if(_type == 0){//요청취소
						jsonSubParam.put("generationRequestDetailSeq", sendIsmBarcodeInfo.getGenerationRequestDetailSeq());				//채번요청상세일련번호
						jsonSubParam.put("generationRequestSeq", sendIsmBarcodeInfo.getGenerationRequestSeq());							//채번요청일련번호
						jsonSubParam.put("deleteWhyCode", mTouchRccCode);																//취소사유코드
						jsonSubParam.put("deleteWhyDescription", mTouchRccCodeDetail);													//취소사유상세
						jsonSubParam.put("transactionStatusCode", sendIsmBarcodeInfo.getTransactionStatusCode());						//진행상태코드
						jsonSubParam.put("transactionUserId", sendIsmBarcodeInfo.getTransactionUserId());								//처리자ID
						jsonSubParam.put("injuryBarcode", sendIsmBarcodeInfo.getInjuryBarcode());										//훼손바코드
						jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getNewBarcode());												//신규바코드
						jsonSubParam.put("publicationWhyCode", sendIsmBarcodeInfo.getPublicationWhyCode());								//요청사유
					}else if(_type == 1 || _type == 2){//발행 //재발행
						jsonSubParam.put("generationRequestDetailSeq", sendIsmBarcodeInfo.getGenerationRequestDetailSeq());				//채번요청상세일련번호
						jsonSubParam.put("generationRequestSeq", sendIsmBarcodeInfo.getGenerationRequestSeq());							//채번요청일련번호
						jsonSubParam.put("itemCode", sendIsmBarcodeInfo.getProductCode());												//자재코드
						jsonSubParam.put("publicationWhyCode", sendIsmBarcodeInfo.getPublicationWhyCode());								//발행사유코드
						jsonSubParam.put("injuryBarcode", sendIsmBarcodeInfo.getInjuryBarcode());										//훼손바코드
						jsonSubParam.put("oldBarcodeYN", sendIsmBarcodeInfo.getOldBarcodeYN());											//구바코드여부
					}else if(_type == 4){//출력
						jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getNewBarcode());												//신규바코드
						jsonSubParam.put("userId", SessionUserData.getInstance().getUserId());											//처리자ID
						jsonSubParam.put("process", "I");																				//process
						jsonSubParam.put("oldBarcodeYN", sendIsmBarcodeInfo.getOldBarcodeYN());											//구바코드여부
					}
				} catch (JSONException e1) {
					return false;
				} catch (Exception e) {
					return false;
				}
				jsonSubParamList.put(jsonSubParam);
			}
			try {
				SendHttpController sendhttp = new SendHttpController();
				if(_type == 0) _OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ISM_PRT_INS_CANCEL, jsonSubParamList);
				else if(_type == 1) _OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ISM_PRT_INS_GENERATE, jsonSubParamList);
				else if(_type == 2) _OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ISM_PRT_INS_REPUBLISH, jsonSubParamList);
				else if(_type == 4) _OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ISM_PRN_STATUS, jsonSubParamList);

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
			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingCancelInTask = null;
			mCheckBox.setChecked(false);
			if (result) {
				mIsmRequestListAdapter.removeCheckedItems();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "정상 전송되었습니다."));
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingCancelInTask = null;
		}
	}
	
	public void statusChange(List<IsmBarcodeInfo> IsmBarcodeInfos){
//		setBarcodeProgressVisibility(true);
		mFindInstoreMarkingPrintCompleteInTask = new FindInstoreMarkingPrintCompleteInTask(IsmBarcodeInfos);
		mFindInstoreMarkingPrintCompleteInTask.execute((Void) null);
	}
	
	//인스토어킹 인쇄 완료 상태값 변경 
	// 전송 비동기 테스크
	public class FindInstoreMarkingPrintCompleteInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private OutputParameter _OutputParameter;
		private List<IsmBarcodeInfo> mSendIsmBarcodeInfos;
		
		public FindInstoreMarkingPrintCompleteInTask(List<IsmBarcodeInfo> sendIsmBarcodeInfos) {
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
					jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getNewBarcode());											//신규바코드
					jsonSubParam.put("userId", SessionUserData.getInstance().getUserId());											//처리자ID
					jsonSubParam.put("process", "I");																				//process
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
//			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingPrintCompleteInTask = null;
//			mCheckBox.setChecked(false);
			if (result) {
//				mIsmRequestListAdapter.removeCheckedItems();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "정상 전송되었습니다."));
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
//			setBarcodeProgressVisibility(false);
			mFindInstoreMarkingPrintCompleteInTask = null;
		}
	}
	
	public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
	
	//프로그레스바 유무 여부 
	public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	mIsmProgress.setVisibility(show ? View.VISIBLE : View.GONE);
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
	
}
