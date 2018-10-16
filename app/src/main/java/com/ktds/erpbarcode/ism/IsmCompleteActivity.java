package com.ktds.erpbarcode.ism; 

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;
import com.ktds.erpbarcode.management.model.SendHttpController;

public class IsmCompleteActivity extends Activity {

	private static final String TAG = "IsmCompleteActivity";  
	
	private ScannerConnectHelper mScannerHelper;
	
	private EditText mFacCdText;
	private TextView mPartTypeText;
	private ListView mBarcodeListView;
	private IsmCompleteListAdapter mIsmCompleteListAdapter;
	private FindInstoreMarkingInTask mFindInstoreMarkingInTask;
	private TextView mListFooterTotalCount;
	
	private Button mDeleteButton, mSendButton;
	
	private RelativeLayout mBarcodeProgress; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		initBarcodeScanner();
		setContentView(R.layout.ism_ismcomplete_activity);
		setMenuLayout();
		setLayout();
		initScreen();
		
		Log.d(TAG, "Create  Start...");
	} 
	 

	private void initBarcodeScanner() {
		// TODO Auto-generated method stub
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

		mFacCdText = (EditText) findViewById(R.id.ismcomplete_fac_facCd);

		mFacCdText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mScannerHelper.focusEditText(mFacCdText);
					return false;
				}
				return false;
			}
		});
		mFacCdText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String barcode = s.toString();

				// 바코드 스케너로 넘어온 데이터는 Enter Key Value가 있는것만 change한다.
				if (barcode.indexOf("\n") > 0 || barcode.indexOf("\r") > 0) {
					barcode = barcode.trim();
					Log.d(TAG, "설비바코드 Chang Event  barcode==>" + barcode);
					if (barcode.isEmpty())
						return;
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
		mPartTypeText = (TextView) findViewById(R.id.ismcomplete_fac_partType);

		mIsmCompleteListAdapter = new IsmCompleteListAdapter(getApplicationContext());
		mBarcodeListView = (ListView) findViewById(R.id.ismcomplete_listView);
		// 여기에서 리스트 아이템 추가
		mBarcodeListView.setAdapter(mIsmCompleteListAdapter);
		mBarcodeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				
				/*
				IsmBarcodeInfo ismBarcodeInfo = mIsmCompleteListAdapter.getItem(position);
				if (ismBarcodeInfo.isChecked()) {
					ismBarcodeInfo.setChecked(false);
				} else {
					if (ismBarcodeInfo.getFacStatus().equals("0130")
							|| ismBarcodeInfo.getFacStatus().equals("0160")) {
						GlobalData.getInstance().showMessageDialog("설비의 상태가 '"
								+ ismBarcodeInfo.getFacStatusName()
								+ "'인 설비는\n\r처리할 수 없습니다.");
						return;
					}
					if (ismBarcodeInfo.getFacStatusName().equals("납품취소")
							|| ismBarcodeInfo.getFacStatusName().equals("사용중지")
							|| ismBarcodeInfo.getFacStatusName().equals("불용요청")
							|| ismBarcodeInfo.getFacStatusName().equals("불용확정")
							|| ismBarcodeInfo.getFacStatusName().equals("인계완료")
							|| ismBarcodeInfo.getFacStatusName().equals("인수예정")) {
						GlobalData.getInstance().showMessageDialog("구바코드('"
								+ ismBarcodeInfo.getInjuryBarcode()
								+ "')의\n\r설비상태가 '"
								+ ismBarcodeInfo.getFacStatusName()
								+ "'인 설비는\n\r처리할 수 없습니다.");

					}

					ismBarcodeInfo.setChecked(true);
				}
				mIsmCompleteListAdapter.setItem(position, ismBarcodeInfo);
				*/
			}
		});

		// 버튼 기능추가
		mDeleteButton = (Button) findViewById(R.id.ismcomplete_crud_delete);
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				deleteData();
			}
		});

		mSendButton = (Button) findViewById(R.id.ismcomplete_crud_send);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				SendCheck sendCheck = new SendCheck();
				sendCheck.facCd = mFacCdText.getText().toString().trim();
				executeSendCheck(sendCheck);
			}

		});

		mListFooterTotalCount = (TextView) findViewById(R.id.ismcomplete_listfooter_totalCount);

		mBarcodeProgress = (RelativeLayout) findViewById(R.id.ismcomplete_barcodeProgress);
	}
	
	 

	public void initScreen() {
		GlobalData.getInstance().setGlobalProgress(false);
		GlobalData.getInstance().setGlobalAlertDialog(false);
		
		mFacCdText.setText("");
		mPartTypeText.setText("");
    	mIsmCompleteListAdapter.itemClear();
    	showSummaryCount();

    	mScannerHelper.focusEditText(mFacCdText);
	}
	 
		
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
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
    
    private void changeFacCd(String barcode) {
    	barcode = barcode.toUpperCase();
    	
    	String facCd = mFacCdText.getText().toString().trim();
    	if (facCd.isEmpty()) {
    		mFacCdText.setText(barcode);
    	}
    	
		// 기존 설비바코드 있는지 체크하여
    	// 없으면 추가하고, 있으면 상태값 변경한다.
    	int thisSelectedPosition = mIsmCompleteListAdapter.getBarcodePosition(barcode);
		Log.d(TAG, "doChkScan   바코드 있는지 여부==>" + barcode+"|"+thisSelectedPosition);
		if (thisSelectedPosition >= 0) {
			// 스캔한 바코드로  Position Selected 한다.
			mIsmCompleteListAdapter.changeSelectedPosition(thisSelectedPosition);
			mBarcodeListView.setSelection(thisSelectedPosition);  // 선택된 바코드로 커서 이동한다.
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
        	return;
		}

		getInstoreMarkings(barcode);
    }
    
    private void deleteData() {
    	if (mIsmCompleteListAdapter.getCheckedItems().size() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
			return;
		}
		mIsmCompleteListAdapter.removeCheckedItems();
		showSummaryCount();
    }

	private void getInstoreMarkings(String barcode) {
		if (isBarcodeProgressVisibility()) return;

		if (mFindInstoreMarkingInTask == null) {
			setBarcodeProgressVisibility(true);
			
			mFindInstoreMarkingInTask = new FindInstoreMarkingInTask(barcode);
			mFindInstoreMarkingInTask.execute((Void) null);
		}
    }
    
    public class FindInstoreMarkingInTask extends AsyncTask<Void, Void, Boolean> {
    	private ErpBarcodeException _ErpBarException;

		String _FacCd; 
		List<IsmBarcodeInfo> _IsmBarcodeInfos;
		List<BarcodeListInfo> barcodeListInfos;

		public FindInstoreMarkingInTask(String facCd) {
			_FacCd=facCd;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				_IsmBarcodeInfos = ismhttp.getInstoreMarkingCompleteToIsmBarcodeInfos(_FacCd);
				if (_IsmBarcodeInfos == null) {
					throw new ErpBarcodeException(-1, "유효하지 않은 인스토어마킹 정보입니다.");
				}
				
				for (IsmBarcodeInfo ismBarcodeInfo : _IsmBarcodeInfos) {

	                if (ismBarcodeInfo.getPublicationWhyCode().equals("5")) {
	                	_ErpBarException = new ErpBarcodeException(-1, "해당 바코드는 '개조개량완료'\n\r작업으로 처리 하시기 바랍니다.");
	                	return false;
	                }

	                // 구바코드가 있으면 설비정보에서서 설비상태별(수리의뢰, 수리완료송부 오류 처리)로 Validation
					if (!ismBarcodeInfo.getInjuryBarcode().isEmpty()) {
						
						BarcodeHttpController barcodehttp = new BarcodeHttpController();
						barcodeListInfos = barcodehttp.getSAPBarcodeDataToBarcodeListInfos("", "", "", ismBarcodeInfo.getInjuryBarcode(), "H");
						
						System.out.println("barcodeListInfos :: " + barcodeListInfos.get(0).getFacStatus() + " :: " + barcodeListInfos.get(0).getFacStatusName() + " :: " + barcodeListInfos.get(0).getProductCode());
						
						if (barcodeListInfos == null || barcodeListInfos.size() == 0) {
							
						} else {
							BarcodeListInfo injuryBarcodeInfo = barcodeListInfos.get(0);
							
							if (injuryBarcodeInfo.getFacStatus().equals("0130") || injuryBarcodeInfo.getFacStatus().equals("0160")) {
								_ErpBarException = new ErpBarcodeException(-1, "설비의 상태가 '" + injuryBarcodeInfo.getFacStatusName() + "'인 설비는\n\r처리할 수 없습니다.");
			                	return false;
							}
							
							if (injuryBarcodeInfo.getFacStatusName().equals("납품취소")
									|| injuryBarcodeInfo.getFacStatusName().equals("사용중지")
									|| injuryBarcodeInfo.getFacStatusName().equals("불용요청")
									|| injuryBarcodeInfo.getFacStatusName().equals("불용확정")
									|| injuryBarcodeInfo.getFacStatusName().equals("인계완료")
									|| injuryBarcodeInfo.getFacStatusName().equals("인수예정")
									|| injuryBarcodeInfo.getFacStatusName().equals("시설등록완료")) {
								_ErpBarException = new ErpBarcodeException(-1, "구바코드('" +ismBarcodeInfo.getInjuryBarcode() + "')의\n\r설비상태가 '" + injuryBarcodeInfo.getFacStatusName() + "'진행중입니다.\n\r해당 작업완료 or취소 후,\n\r인스토어마킹 프로세스\n\r진행 가능합니다.");
			                	return false;
							}
						}
	                }
	                // -- 구바코드가 있으면 설비정보해서 설비상태별(수리의뢰, 수리완료송부 오류 처리)로 Validation
	                
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
			mFindInstoreMarkingInTask = null;
	
			if (result) {
				mPartTypeText.setText(_IsmBarcodeInfos.get(0).getPartType());
				
				mIsmCompleteListAdapter.addItemsNotClear(_IsmBarcodeInfos);
				showSummaryCount();				
				
				if(barcodeListInfos != null){
					try {
						SuportLogic.chkZPSTATU(_IsmBarcodeInfos.get(0).getFacStatus(), _IsmBarcodeInfos.get(0).getFacStatusName(), _IsmBarcodeInfos.get(0).getProductCode());
					} catch (ErpBarcodeException e) {
						_ErpBarException = e;
						GlobalData.getInstance().showMessageDialog(_ErpBarException);
					}
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
	 * List FooterBar Summary Tobal Count 정보를 조회한다.
	 */
	private void showSummaryCount() {
		// 조회건수를 보여준다.
		int totalCount = mIsmCompleteListAdapter.getCount();
		mListFooterTotalCount.setText(String.valueOf(totalCount) + "건");
	}
		 
    //여기까지 검색작업
    public class SendCheck {
		public String facCd = "";
    }

	private void executeSendCheck(final SendCheck sendCheck) {
		if (mIsmCompleteListAdapter.getCheckedItems().size() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 설비바코드가\n\r존재하지 않습니다.", BarcodeSoundPlay.SOUND_ERROR));
			return;
		}
		
		//-----------------------------------------------------------
		// 전송여부 최종 확인..
		//-----------------------------------------------------------
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
				
				new SendDataInTask().execute();
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

	// 전송 비동기 테스크
	public class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private int _SendCount = 0;
		private OutputParameter _OutputParameter;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setBarcodeProgressVisibility(true);
			mSendButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONArray jsonParamList = new JSONArray();
			JSONObject jsonParam = new JSONObject();
    		jsonParamList.put(jsonParam);

			
    		//-------------------------------------------------------
			// 바디정보 입력 Start..
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();
			final List<IsmBarcodeInfo> sendBarcodeInfos = mIsmCompleteListAdapter.getAllItems();
			for (IsmBarcodeInfo sendIsmBarcodeInfo : sendBarcodeInfos) {

				if (!sendIsmBarcodeInfo.isChecked()) continue;

				JSONObject jsonSubParam = new JSONObject();
				try {
					jsonSubParam.put("newBarcode", sendIsmBarcodeInfo.getNewBarcode());
					jsonSubParam.put("locationCode", sendIsmBarcodeInfo.getLocCd());
					jsonSubParam.put("deviceId", sendIsmBarcodeInfo.getDeviceId());
					jsonSubParam.put("itemCode", sendIsmBarcodeInfo.getItemCode());
					jsonSubParam.put("itemName", sendIsmBarcodeInfo.getItemName());
					jsonSubParam.put("itemCategoryCode", sendIsmBarcodeInfo.getItemCategoryCode());
					jsonSubParam.put("partKindCode", sendIsmBarcodeInfo.getPartKindCode());
					jsonSubParam.put("injuryBarcode", sendIsmBarcodeInfo.getInjuryBarcode());
					jsonSubParam.put("publicationWhyCode", sendIsmBarcodeInfo.getPublicationWhyCode());
					jsonSubParam.put("itemLargeClassificationCode", sendIsmBarcodeInfo.getItemLargeClassificationCode());
					jsonSubParam.put("itemMiddleClassificationCode", sendIsmBarcodeInfo.getItemMiddleClassificationCode());
					jsonSubParam.put("itemSmallClassificationCode", sendIsmBarcodeInfo.getItemSmallClassificationCode());
					jsonSubParam.put("itemDetailClassificationCode", sendIsmBarcodeInfo.getItemDetailClassificationCode());
					jsonSubParam.put("supplierCode", sendIsmBarcodeInfo.getSupplierCode());
					jsonSubParam.put("deptCode", sendIsmBarcodeInfo.getDeptCode());
					jsonSubParam.put("operationDeptCode", sendIsmBarcodeInfo.getOperationDeptCode());
					jsonSubParam.put("makerCode", sendIsmBarcodeInfo.getMakerCode());
					jsonSubParam.put("makerSerial", sendIsmBarcodeInfo.getMakerSerial());
					jsonSubParam.put("makerNational", sendIsmBarcodeInfo.getMakerNational());
					jsonSubParam.put("generationRequestSeq", sendIsmBarcodeInfo.getGenerationRequestSeq());
					jsonSubParam.put("facilityCategory", sendIsmBarcodeInfo.getFacilityCategory());
					jsonSubParam.put("makerItemYn", sendIsmBarcodeInfo.getMakerItemYn());
				} catch (JSONException e1) {
					return false;
				} catch (Exception e) {
					return false;
				}
				jsonSubParamList.put(jsonSubParam);

				_SendCount++; // 전송건수.
			} // for end.

			try {
				// 주소를 하나 만든다
				SendHttpController sendhttp = new SendHttpController();
				_OutputParameter = sendhttp.sendToServer(
						HttpAddressConfig.PATH_POST_IM_COMPLETESCAN, jsonParamList, jsonSubParamList);

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
			mSendButton.setEnabled(true);

			if (result) {
                String message = "# 전송건수 : " + _SendCount + "건\n\n1-정상 전송되었습니다.";
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				initScreen();
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
    	mScannerHelper.focusEditText(mFacCdText);
	}
}