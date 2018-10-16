package com.ktds.erpbarcode.management;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.http.OutputParameter;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.management.model.ArgumentConfirmInfo;
import com.ktds.erpbarcode.management.model.SendHttpController;
import com.ktds.erpbarcode.management.model.TransferHttpController;

public class TransferArgumentFragment extends Fragment {

	private static final String TAG = "TransferArgumentFragment";

	
	private ListView mArgumentListView;
	private TransferArgumentListAdapter mTransferArgumentListAdapter;
	private Button mArgumentSendButton;
	private Button mArgumentCancelButton;
	
	private FindArgumentScanConfirmInTask mFindArgumentScanConfirmInTask;
	
	private RelativeLayout mArgumentProgress;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //mBarcodeSoundPlay = new BarcodeSoundPlay(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.management_transferargument_fragment, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setLayout(savedInstanceState);

		initFragmentScreen();
	}
	
	private void setLayout(Bundle savedInstanceState) {
		
		// ListView
        mTransferArgumentListAdapter = new TransferArgumentListAdapter( getActivity().getApplicationContext());
		
        mArgumentListView = (ListView) getActivity().findViewById(R.id.transferargument_listView);
        mArgumentListView.setAdapter(mTransferArgumentListAdapter);
        mArgumentListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);

			}
		});
        
        mArgumentSendButton = (Button) getActivity().findViewById(R.id.transferargument_send_button);
        mArgumentSendButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
		            	SendCheck sendCheck = new SendCheck();
		            	sendCheck.orgCode = SessionUserData.getInstance().getOrgId();
		            	sendCheck.locBarcodeInfo = ((TransferActivity) getActivity()).getThisLocCodeInfo();
		            	if (sendCheck.locBarcodeInfo == null) {
		            		sendCheck.locBarcodeInfo = new LocBarcodeInfo();
		            	}
						executeSendCheck(sendCheck);
					}
				});
        
        mArgumentCancelButton = (Button) getActivity().findViewById(R.id.transferargument_cancel_button);
        mArgumentCancelButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						initArgumentScreen();
						((TransferActivity) getActivity()).showTransferArgument(false);
					}
				});

		// 바코드스캔시 조회때만 사용.
		mArgumentProgress = (RelativeLayout) getActivity().findViewById(R.id.transferargument_barcodeProgress);

		//mTotalCountText = (TextView) getActivity().findViewById(R.id.transfertree_totalCount);
		
		Log.d(TAG, "setLayout   End.");
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }
	
	// 인수확정 창만 클리어
	public void initArgumentScreen() {
		mTransferArgumentListAdapter.itemClear();
		mArgumentSendButton.setEnabled(false);
		mArgumentProgress.setVisibility(View.GONE);
	}
    
    // 바코드 조회용 ProgressBar Visibility
    public void setBarcodeProgressVisibility(boolean visibility) {
    	if (visibility) {
    		mArgumentProgress.setVisibility(View.VISIBLE);
    	} else {
    		mArgumentProgress.setVisibility(View.GONE);
    	}
    }
    
    public boolean isBarcodeProgressVisibility() {
    	if (mArgumentProgress.getVisibility() == View.VISIBLE) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    private void initFragmentScreen() {
    	mTransferArgumentListAdapter.itemClear();
    }
    
    public void getArgumentScanConfirmData(String wbsNo, String locCd, List<String> deviceIds) {
    	if (isBarcodeProgressVisibility()) return;
    	initFragmentScreen();
    	
    	if (mFindArgumentScanConfirmInTask == null) {
			setBarcodeProgressVisibility(true);
			
			mFindArgumentScanConfirmInTask = new FindArgumentScanConfirmInTask(wbsNo, locCd, deviceIds);
			mFindArgumentScanConfirmInTask.execute((Void) null);
		}
    }
   
	public class FindArgumentScanConfirmInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _WbsNo, _LocCd;
		private List<String> _DeviceIds;
		private List<ArgumentConfirmInfo> _ArgumentConfirmInfos = new ArrayList<ArgumentConfirmInfo>();
		
		public FindArgumentScanConfirmInTask(String wbsNo, String locCd, List<String> deviceIds) {
			_WbsNo = wbsNo;
			_LocCd = locCd;
			_DeviceIds = deviceIds;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {

			if (!GlobalData.getInstance().getJobGubun().equals("인수")) {
				_WbsNo = "";
	    	}
			
			JSONArray jsonResults = null;
			for (String devideId : _DeviceIds) {
				try {
					TransferHttpController transferhttp = new TransferHttpController();
					jsonResults = transferhttp.getArgumentScanConfirmData(_WbsNo, devideId);
					if (jsonResults == null) {
						throw new ErpBarcodeException(-1, "유효하지 않은 인수확정 정보입니다.");
					}
	    		} catch (ErpBarcodeException e) {
	    			Log.d(TAG, e.getErrMessage());
	    			_ErpBarException = e;
	    			return false;
	    		}
				
				Log.d(TAG, "FindArgumentScanConfirmInTask   for 건수:"+jsonResults.length());
				for (int i=0;i<jsonResults.length();i++) {
					
					ArgumentConfirmInfo argumentConfirmInfo = new ArgumentConfirmInfo();
		    		try {
		            	JSONObject jsonobj = jsonResults.getJSONObject(i);

		            	argumentConfirmInfo.setDeviceId(jsonobj.getString("DEVICEID"));
		            	argumentConfirmInfo.setLocCd(jsonobj.getString("LOCCODE"));
		            	argumentConfirmInfo.setDocNo(jsonobj.getString("DOCNO"));
		            	argumentConfirmInfo.setDaori(jsonobj.getString("DAORI"));
		            	argumentConfirmInfo.setWbsNo(jsonobj.getString("POSID"));
		            	
		            	argumentConfirmInfo.setHostYn(jsonobj.getString("HOST_YN"));
		            	argumentConfirmInfo.setUsgYn(jsonobj.getString("USG_YN"));
		            	
		            	if (!_LocCd.equals(argumentConfirmInfo.getLocCd())) {
        					continue;
        				}
		            	
		            	DeviceBarcodeInfo deviceInfo = new DeviceBarcodeInfo();
		            	try {
		            		BarcodeHttpController barcodehttp = new BarcodeHttpController();
		                	deviceInfo = barcodehttp.getDeviceBarcodeData(argumentConfirmInfo.getDeviceId());
		    				
		    				if (deviceInfo == null) {
		    					throw new ErpBarcodeException(-1, "장치바코드 조회 결과가 없습니다. ");
		    				}
		    			} catch (ErpBarcodeException e) {
		    				Log.e(TAG, "DeviceBarcodeThread  서버로 장치바코드 조회 요청중 오류가 발생했습니다.==>" + e.getErrMessage());
		    				throw new ErpBarcodeException(-1, "장치바코드 정보 조회중 오류가 발생했습니다." + e.getErrMessage());
		    			}
		            	
		            	argumentConfirmInfo.setDeviceBarcodeInfo(deviceInfo);
		            	_ArgumentConfirmInfos.add(argumentConfirmInfo);
		    		} catch (ErpBarcodeException e) {
		    			_ErpBarException = e;
		    			return false;
					} catch (JSONException e) {
						Log.e(TAG, "인수확정 정보 변환중 오류가 발생했습니다. ==>" + e.getMessage());
						_ErpBarException = new ErpBarcodeException(-1, "인수확정 정보 변환중 오류가 발생했습니다." + e.getMessage());
	    				return false;
		    		}
		        } // for End..
			} // for End.

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindArgumentScanConfirmInTask = null;
			
			if (result) {
				if (_ArgumentConfirmInfos.size() > 0) {
					mTransferArgumentListAdapter.addItems(_ArgumentConfirmInfos);
					
					mArgumentSendButton.setEnabled(true);
					
					// 조회건수를 보여준다.
					//String totalCount = String.valueOf(mTransferArgumentListAdapter.getCount());
					//mTotalCount.setText(totalCount + "건");
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
			mFindArgumentScanConfirmInTask = null;
		}
	}
	
	public class SendCheck {
    	public String orgCode = "";
    	public LocBarcodeInfo locBarcodeInfo = null;
    	public String wbsNo = "";
    	public String docNo = "";
    	public String deviceId = "";
    	public String host_check = "N";
    	public String usg_check = "N";
    }

    /**
     * 인수, 시설등록확정 전송
     */
    private void executeSendCheck(final SendCheck sendCheck) {
    	
    	if (mTransferArgumentListAdapter.getCount() == 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "전송할 데이터가 없습니다."));
    		return;
    	}
    	
    	String hostYn = "Y";
    	String usgYn = "Y";
    	for(int i=0;i<mTransferArgumentListAdapter.getCount();i++){
    		if(mTransferArgumentListAdapter.getItem(i).getHostYn().equals("N")){
    			hostYn = "N";
    		}
    		
    		if(mTransferArgumentListAdapter.getItem(i).getUsgYn().equals("N")){
    			usgYn = "N";
    		}
    	}
    	
    	if(sendCheck.host_check.equals("N")){
    		if(hostYn.equals("N")){
    			final Builder builder = new AlertDialog.Builder(getActivity()); 
    			builder.setIcon(android.R.drawable.ic_menu_info_details);
    			builder.setTitle("알림");
    			TextView msgText = new TextView(getActivity());
    			msgText.setPadding(10, 30, 10, 30);
    			msgText.setText("서버 호스트매핑 완료 후 실장 등록 진행하시기 바랍니다.\n\r자세한사항은 http://itam.kt.com 초기화면 하단 공지사항\n\r‘전사 서버 IT자산관리 정책’을 참고하세요.\n\r문의처: itam@kt.com");
    			msgText.setGravity(Gravity.CENTER);
    			msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    			msgText.setTextColor(Color.BLACK);
    			builder.setView(msgText);
    			builder.setCancelable(false);
    			builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					sendCheck.host_check = "Y";
    					executeSendCheck(sendCheck);
    				}
    			});
    			AlertDialog dialog_host = builder.create();
    			dialog_host.show();
    			return;
    		}
    	}
    	
    	if(sendCheck.usg_check.equals("N")){
    		if(usgYn.equals("N")){
    			final Builder builder = new AlertDialog.Builder(getActivity()); 
    			builder.setIcon(android.R.drawable.ic_menu_info_details);
    			builder.setTitle("알림");
    			TextView msgText = new TextView(getActivity());
    			msgText.setPadding(10, 30, 10, 30);
    			msgText.setText("ITAM시스템(itam.kt.com)에 성능정보가 수집이 되고 있지 않습니다.\n\rITAM 로그인 후 > 하단 성능정보 수집\n\r매뉴얼을 참고하시어 현행화 부탁 드립니다.\n\r문의사항 : itam@kt.com");
    			msgText.setGravity(Gravity.CENTER);
    			msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    			msgText.setTextColor(Color.BLACK);
    			builder.setView(msgText);
    			builder.setCancelable(false);
    			builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					sendCheck.usg_check = "Y";
    					executeSendCheck(sendCheck);
    				}
    			});
    			AlertDialog dialog_host = builder.create();
    			dialog_host.show();
    			return;
    		}
    	}
    	
    	//if (sendCheck.locBarcodeInfo==null || sendCheck.locBarcodeInfo.getLocCd().isEmpty()) {
		//	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드를 스캔하세요."));
		//	return;
		//}

		//---------------------------------------------------------------------
		// 전송여부 최종 확인..
    	//---------------------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
    	String message = "전송하시겠습니까?"; 
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_SENDQUESTION);
    	final Builder builder = new AlertDialog.Builder(getActivity()); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getActivity());
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
    	if (isBarcodeProgressVisibility()) return;
    	setBarcodeProgressVisibility(true);
		mArgumentSendButton.setEnabled(false);
    	
    	new SendDataInTask(sendCheck).execute();
    }
    
    /**
     * 인수, 시설등록 확정 서버로 전송.
     */
    private class SendDataInTask extends AsyncTask<Void, Void, Boolean> {
    	//private final SendCheck _SendCheck;
    	private int _SendCount = 0;
    	private OutputParameter _OutputParameter;
    	private ErpBarcodeException _ErpBarException;

		public SendDataInTask(SendCheck sendCheck) {
			//_SendCheck = sendCheck;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "SendDataInTask Hearder Parameter  Start...");
			
			//-------------------------------------------------------
			// 작업된 바코드 헤더정보 생성.
			//-------------------------------------------------------
			JSONObject jsonParam = new JSONObject();
    		try {
	        	jsonParam.put("WORKID", "0003");
	        	jsonParam.put("PRCID", "0530");
	        	jsonParam.put("DOCTYPE", "0020");
	        	jsonParam.put("WERKS", "9200");
	        	jsonParam.put("DATE_ENTERED", SystemInfo.getNowDate());
	        	jsonParam.put("TIME_ENTERED", SystemInfo.getNowTime());

    		} catch (JSONException e) {
    			_ErpBarException = new ErpBarcodeException(-1, "헤더정보 생성중 오류가 발생했습니다. "+e.getMessage());
    			return false;
			}

    		JSONArray jsonParamList = new JSONArray();
    		jsonParamList.put(jsonParam);

    		
    		Log.d(TAG, "SendDataInTask Body Parameter  Start...");
    		//-------------------------------------------------------
			// 작업된 바코드 바디정보 생성.
			//-------------------------------------------------------
			JSONArray jsonSubParamList = new JSONArray();

			List<ArgumentConfirmInfo> mArgumentConfirmInfos = mTransferArgumentListAdapter.getAllItems();
			
    		_SendCount = 0;
            for (int i=0; i<mArgumentConfirmInfos.size(); i++) {
            	
            	ArgumentConfirmInfo sendArgumentConfirmInfo = mArgumentConfirmInfos.get(i);
            	
            	JSONObject jsonSubParam = new JSONObject();
    	        try {
                    jsonSubParam.put("DOCNO", sendArgumentConfirmInfo.getDocNo());
                    jsonSubParam.put("DFLAG", "1");
                    jsonSubParam.put("POSID", sendArgumentConfirmInfo.getWbsNo());
                    jsonSubParam.put("LOCCODE", sendArgumentConfirmInfo.getLocCd());
                    jsonSubParam.put("DEVICEID", sendArgumentConfirmInfo.getDeviceId());
                    jsonSubParam.put("DAORI", sendArgumentConfirmInfo.getDaori());
                    
    	        } catch (JSONException e) {
    	        	_ErpBarException = new ErpBarcodeException(-1, "작업데이터 파라메터서브리스트 JSON대입중 오류가 발생했습니다. " + e.getMessage());
        			return false;
    	        }
    	        
    	        jsonSubParamList.put(jsonSubParam);
    	        
    	        _SendCount++;  // 전송건수.
            } // for end sendBarcodeMaps.size()
            
            Log.d(TAG, "SendDataInTask SendHttpController "+GlobalData.getInstance().getJobGubun()+" Start...");
            
            try {
        		SendHttpController sendhttp = new SendHttpController();
        		_OutputParameter = sendhttp.sendToServer(HttpAddressConfig.PATH_POST_ARGUMENTSCAN_CONFIRM,
        				jsonParamList, jsonSubParamList);

            	if (_OutputParameter == null) {
            		throw new ErpBarcodeException(-1, "'" + GlobalData.getInstance().getJobGubun() + "' 정보 전송중 오류가 발생했습니다." );
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
				mArgumentSendButton.setEnabled(true);
				String message = "# 전송건수 : " + _SendCount + "건 \n\n" 
							+ _OutputParameter.getStatus() + "-" + _OutputParameter.getOutMessage();
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, message));
				// 초기화 후 닫기
				initFragmentScreen();
				
				// 인수확정/시설등록확정 성공시 화면 클리어
				((TransferActivity) getActivity()).initScreen();			
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

}
