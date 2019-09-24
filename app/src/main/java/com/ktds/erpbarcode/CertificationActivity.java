package com.ktds.erpbarcode;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

public class CertificationActivity extends Activity {
	
	private static final String TAG = "CertificationActivity";
	
	public static final String CERTIFICATION = "certification";
	
	public static final String INPUT_PWUPDATE_YN = "passwdUpdateYn";
	private String p_passwdUpdateYn = "";
	public static final String INPUT_NOTICE = "passwdIsNotice";
	private boolean p_passwdIsNotice = false;	
	public static final String INPUT_AGREE = "informationIsagree";
	private boolean p_informationIsagree = false;
    //Add by sesang 20190819 정합성 작업대상 수량 체크
	public static final String INPUT_CONSISTENCY_COUNT = "consistencyCount";
	public int p_consistencyCount = 0;
    // end sesang

	private TextView mInfomationText;
	private EditText mPhoneNoText;
	private Button mRequestButton;
	private EditText mSmsNoText;
	private EditText mSecondsText;
	private Button mSendButton;
	private Button mCloseButton;
	
	private int mStartTime = 0;
	private Handler mTimerHandler = new Handler();
	
	private RequestCertificateNoTask mRequestCertificateNoTask;
	private String mCertificationSmsNo = "";
	private SendCertificationTask mSendCertificationTask;
	
	private boolean isCertified = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//-----------------------------------------------------------
	    // ActionBar에 progress를 활성화 시킨다.
	    //-----------------------------------------------------------
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
	    //-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		

		setMenuLayout();
		setContentView(R.layout.base_certification_activity);
		
		p_passwdUpdateYn = getIntent().getStringExtra(INPUT_PWUPDATE_YN);
    	p_passwdIsNotice = getIntent().getBooleanExtra(INPUT_NOTICE, false);
    	p_informationIsagree = getIntent().getBooleanExtra(INPUT_AGREE, false);
		//Add by sesang 20190819 정합성 작업대상 수량 체크
		p_consistencyCount = getIntent().getIntExtra(INPUT_CONSISTENCY_COUNT, 0);
		// end
		setLayout();
		
		initScreen();
	}
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.certification_title);
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	/**
     * 화면 Layout.xml 설정한다.
     */
	private void setLayout() {
		// Set up the login form.
		mInfomationText = (TextView) findViewById(R.id.certification_infomation_text);
		mPhoneNoText = (EditText) findViewById(R.id.certification_phoneNo);
		mRequestButton = (Button) findViewById(R.id.certification_request_button);
		mRequestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// SMS 인증번호 요청.
				execRequestCertificateNoTask();
			}
		});
		
		mSmsNoText = (EditText) findViewById(R.id.certification_smsNo);
		mSecondsText = (EditText) findViewById(R.id.certification_seconds);
		mSendButton = (Button) findViewById(R.id.certification_send_button);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// SMS 본인인증번호 전송.
				execSendCertificationTask();
			}
		});
		
		mCloseButton= (Button) findViewById(R.id.certification_close_button);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//-------------------------------------------------------
				// 본인인증 성공일때만.
				//-------------------------------------------------------
				returnData();
			}
		});

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			returnData();
		} else {
        	return true;
        }
	    return false;
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
	}
	
	
	
	@Override
	public void onBackPressed() {
		returnData();
		super.onBackPressed();
	}
	
	public void returnData(){
		Intent intent = new Intent();
        intent.putExtra(CERTIFICATION, isCertified);
        intent.putExtra(INPUT_PWUPDATE_YN, p_passwdUpdateYn);
        intent.putExtra(INPUT_NOTICE, p_passwdIsNotice);
        intent.putExtra(INPUT_AGREE, p_informationIsagree);
		// Add by sesang 20190819 정합성 작업대상 수량 체크
		intent.putExtra(INPUT_CONSISTENCY_COUNT, p_consistencyCount);
		// end
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

//	private void nextActivity() {
//		//---------------------------------------------------------
//		// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
//		// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기 위해서..
//		//---------------------------------------------------------
//		finish();
//		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//	}
	
	private void initScreen() {
		
		isCertified = false;
		
		String message = "'" + SessionUserData.getInstance().getUserName().replace(SessionUserData.getInstance().getUserName().charAt(SessionUserData.getInstance().getUserName().length() - 1),'*') + 
				"' 님은\n\rKT ERP Barcode System 접속을 위하여\n\r본인인증을 하셔야 합니다.";
		
		mInfomationText.setText(message);
		
		StringBuffer buf = new StringBuffer(SessionUserData.getInstance().getUserCellPhoneNo());
		mPhoneNoText.setText(String.valueOf(buf.replace(5, 8, "***")));
		
		mSecondsText.setText("");
		mSendButton.setEnabled(false);
	}
	
	/**
	 * 본인인증번호 요청.
	 */
	private void execRequestCertificateNoTask() {
		timerStart();
		
		if (mRequestCertificateNoTask == null) {
			setProgressBarIndeterminateVisibility(true);
			mRequestCertificateNoTask = new RequestCertificateNoTask();
			mRequestCertificateNoTask.execute((Void) null);
		}
	}
	public class RequestCertificateNoTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private JSONArray _JsonResults = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SignHttpController signhttp = new SignHttpController();
				_JsonResults = signhttp.getLoginMakeCertification();
				if (_JsonResults == null) {
					throw new ErpBarcodeException(-1, "인증번호생성 중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요.");
				}
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "logIn  ErpBarcodeException ==>"+e.getErrMessage());
				_ErpBarException = e;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mRequestCertificateNoTask = null;
			setProgressBarIndeterminateVisibility(false);

			if (result) {
				try {
	            	JSONObject jsonobj = _JsonResults.getJSONObject(0);
	            	
	            	mCertificationSmsNo = jsonobj.getString("certificationResult").trim();
	    		} catch (JSONException e) {
	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "인증번호생성 중\n\r오류가 발생하였습니다.\n\r잠시 후 다시 시도하세요." ));
	    			return;
	    		}
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mRequestCertificateNoTask = null;
			setProgressBarIndeterminateVisibility(false);
			super.onCancelled();
		}
	}
	
	/**
	 * 타이머 Start.
	 */
	private void timerStart() {
		isCertified = false;
		mSendButton.setEnabled(true);
		mSecondsText.setText("");
		mStartTime = 180;
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
        mTimerHandler.postDelayed(mUpdateTimeTask, 0);
	}
	private void timerStop() {
		//mSendButton.setEnabled(false);
		mSecondsText.setText("");
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
	}
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			int seconds = mStartTime;
			
			String counterTime = String.format("%03d", seconds) + "초";
			mSecondsText.setText(counterTime);
			
			//-------------------------------------------------------
			// 180초가 지나면 Stop한다.
			//-------------------------------------------------------
			if (seconds <= 0) {
				timerStop();
				return;
			}

			mTimerHandler.postDelayed(this, 1000);
			mStartTime--;
		}
	};
	
	/**
	 * 본인인증번호 전송.
	 */
	private void execSendCertificationTask() {
		timerStop();
		
		String smsNo = mSmsNoText.getText().toString().trim();
		if (smsNo.isEmpty()) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(1, "전송 받은 인증번호를 입력하세요."));
			return;
		}
		
		if (smsNo.equals(mCertificationSmsNo)) {
			if (mSendCertificationTask == null) {
				setProgressBarIndeterminateVisibility(true);
				mSendCertificationTask = new SendCertificationTask();
				mSendCertificationTask.execute((Void) null);
			}
			isCertified = true;
			successCertificationProcess();
		} else {
			String telNo = mPhoneNoText.getText().toString().trim();
			
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeMessage(1, "'" + SessionUserData.getInstance().getUserName().replace(SessionUserData.getInstance().getUserName().charAt(SessionUserData.getInstance().getUserName().length() - 1),'*') + 
							"' 님의 휴대전화\n\r'" + telNo + "' 로\n\r전송 받은 인증번호를\n\r정확히 입력하세요." ));
			return;
		}
	}
	public class SendCertificationTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		
		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				SignHttpController signhttp = new SignHttpController();
				signhttp.sendLoginSendCertification();
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "logIn  ErpBarcodeException ==>"+e.getErrMessage());
				_ErpBarException = e;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mRequestCertificateNoTask = null;
			setProgressBarIndeterminateVisibility(false);

			if (result) {
				successCertificationProcess();
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mRequestCertificateNoTask = null;
			setProgressBarIndeterminateVisibility(false);
			super.onCancelled();
		}
	}
	
	public void successCertificationProcess(){
//		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
//		GlobalData.getInstance().setGlobalAlertDialog(true);

		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		String message = "인증되었습니다.\n 1일1회 본인 인증 절차를 진행하오니\n업무에 참고하시기 바랍니다.";

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
		builder.setNegativeButton("확인",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						GlobalData.getInstance().setGlobalAlertDialog(false);
						returnData();
						return;
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}

}
