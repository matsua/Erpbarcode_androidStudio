package com.ktds.erpbarcode.management;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.regex.*;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.LoginActivity;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

public class ResetPasswordActivity extends Activity {
	private static final String TAG = "ResetPasswordActivity";
	public static final String INPUT_PWCHANGE_TYPE = "pwchange_type";
	private String mUserId;
//	private String mUserNm;
	private String mUserPn;
	private String mUserEm;
	private String mUserAn;
	private String serverNumber;
	
	private LinearLayout passwordView;
	private LinearLayout inputView;
	private LinearLayout commentView;
	private TextView authCount;
	
	private RequestAuthNumInTask mRequestAuthNumInTask;
	private ConfirmAuthNumInTask mConfirmAuthNumInTask;
	private SavePasswordInTask mSavePasswordInTask;
	
	private int mStartTime = 0;
	private Handler mTimerHandler = new Handler();
	
	private String type = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.reset_password_popup);
		
		type = getIntent().getStringExtra(INPUT_PWCHANGE_TYPE);
		
		findViewById(R.id.closeBtn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();
					}
				});
		
		findViewById(R.id.userAuthNumReq).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						requestAuth();
					}
				});
		
		findViewById(R.id.userAuthNumCon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						confirmAuth();
					}
				});
		
		findViewById(R.id.save).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						savePassword();
					}
				});
		
		findViewById(R.id.close).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();
					}
				});
		
		passwordView = (LinearLayout)findViewById(R.id.passwordView);
		inputView = (LinearLayout)findViewById(R.id.inputView);
		commentView = (LinearLayout)findViewById(R.id.commentView);
		
		if(type.equals("reset")){
			passwordView.setVisibility(View.INVISIBLE);
			inputView.setVisibility(View.VISIBLE);
			commentView.setVisibility(View.VISIBLE);
			authCount = (TextView)findViewById(R.id.userAuthCountLbl);
		}else{
			passwordView.setVisibility(View.VISIBLE);
			inputView.setVisibility(View.GONE);
			commentView.setVisibility(View.INVISIBLE);
			messageAlert("3개월 이상 동일한 패스워드를 사용 중입니다.\n패스워드를 변경 하셔야 시스템 이용이 가능 합니다.");
		}
		
	}
	
	public void requestAuth(){
		mUserId = ((EditText) findViewById(R.id.userId)).getText().toString();
//		mUserNm = ((EditText) findViewById(R.id.userName)).getText().toString();
		mUserPn = ((EditText) findViewById(R.id.userPhoneNum)).getText().toString();
		mUserEm = ((EditText) findViewById(R.id.email)).getText().toString();
		
		String msg = "";
		if(mUserId.length() < 1) msg += "사용자ID";
//		if(mUserNm.length() < 1) msg += "사용자이름";
		if(mUserPn.length() < 1) msg += "휴대전화번호";
		if(mUserEm.length() < 1) msg += "사용자Email";
		if(msg.length() > 0){
			msg += "은 필수 입력 정보 입니다.";
			messageAlert(msg);
		}else{
			serverNumber = "";
			passwordView.setVisibility(View.INVISIBLE);
			requestAuthNum(mUserId, mUserPn, mUserEm);
		}
	}
	
	public void confirmAuth(){
		String msg = "";
		
		if( serverNumber == null || serverNumber.length() < 1){
			msg = "인증번호를 요청해주세요.";
			messageAlert(msg);
			return;
		}
		
		mUserAn = ((EditText) findViewById(R.id.userAuth)).getText().toString();
		
		if(mUserId.length() < 1) msg += "사용자ID";
		if(mUserAn.length() < 1) msg += "인증번호";
		
		if(msg.length() > 0){
			msg += "는 필수 입력 정보 입니다.";
			messageAlert(msg);
		}else{
			confirmAuthNum(mUserId, mUserAn);
		}
	}
	
	public void savePassword(){
		String newPassword = ((EditText) findViewById(R.id.changePw1)).getText().toString();
		String conNewPassword = ((EditText) findViewById(R.id.changePw2)).getText().toString();
		
		if(type.equals("update")){
			mUserId = SessionUserData.getInstance().getUserId();
		}
		
		boolean noCharValue = false;
		boolean noSpace = false;
		boolean noId = false;
		boolean noNext = false;
		
		int sameText = 0;
	    int sameChar = 0;
	    int bChar = -10;
	    int cChar = -10;
		
		//비밀번호, 비밀번호 확인 미일치
		if(!newPassword.equalsIgnoreCase(conNewPassword)){
			setMsg(true, false, false, false, false);
			return;
		}
		
		//8자리 이상 영문 + 특문 + 숫자
		Pattern p = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])");
		Matcher m = p.matcher(newPassword);
		
		if (!m.find()) noCharValue = true;
		if (newPassword.length() < 8) noCharValue = true;

		
		for(int i = 0; i < newPassword.length(); i++){
			String temp = newPassword.substring(i, i+1);
			
	        //공백 불가
	        if(temp.equals("")){
	            noSpace = true;
	        }
	        
	        // ID와 동일 문자가 4개이상 겹침 불가
	        for(int j = 0; j < mUserId.length(); j++){
	        	String tempj = mUserId.substring(j, j+1);
	        	
	            if(temp.equals(tempj)){
	                sameText++;
	                if(sameText > 3)
	                    noId = true;
	            }
	        }
	        
	        //숫자 또는 문자 4자리이상 연속/중복 불가(ex - 1234, abcd, 1111)
	        cChar = newPassword.charAt(i);
	        if(bChar == -10){
	            bChar = cChar;
	        }else{
	            if((bChar - cChar) == 1 || (bChar - cChar) == -1 || bChar == cChar)
	                sameChar++;
	            else
	                sameChar = 0;
	            
	            bChar = cChar;
	            
	            if(sameChar > 2)
	                noNext = true;
	        }
	    }
		
		if(!noCharValue && !noSpace && !noId && !noNext){
			savePassword(mUserId, conNewPassword, type);
		}else{
			setMsg(false, noCharValue, noSpace, noId, noNext);
		}
	}
	
	public void setMsg(boolean noMatch, boolean noCharValue, boolean noSpace, boolean noId, boolean noNext){
		String msg = "";
		if(noMatch){
			msg = "입력하신 비밀번호가 상이합니다.";
		}
		
		if(noCharValue){
	        msg = "비밀번호는 특수문자+숫자+영문 조합 8자 이상 입력하셔야 합니다.\n";
	    }
	    
	    if(noSpace){
	        msg += "비밀번호는 공백입력이 불가 합니다.\n";
	    }
	    
	    if(noId){
	        msg += "비밀번호는 ID와 동일 문자가 4개이상 입력 불가 합니다.\n";
	    }
	    
	    if(noNext){
	        msg += "숫자 또는 문자 4자리이상 연속/중복 입력 불가 합니다.\n";
	    }
		
		messageAlert(msg);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
		} else {
        	return super.onOptionsItemSelected(item);
        }
        return false;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void requestAuthNum(String userId, String phoneNum, String email) {
		if (mRequestAuthNumInTask == null) {
			mRequestAuthNumInTask = new RequestAuthNumInTask(userId, phoneNum, email);
			mRequestAuthNumInTask.execute((Void) null);
		}
	}
	
	public class RequestAuthNumInTask extends AsyncTask<Void, Void, Boolean> {
		private String _userId = "";
//		private String _userName = "";
		private String _phoneNum = "";
		private String _email = "";
		private ErpBarcodeException _ErpBarException;
		private JSONArray _JsonResults = null;
		
		public RequestAuthNumInTask(String userId, String phoneNum, String email) {
			_userId = userId;
//			_userName = userName;
			_phoneNum = phoneNum;
			_email = email;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.APP_SERVER);
//				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.QA_SERVER);
				LocationHttpController locationhttp = new LocationHttpController();
				_JsonResults = locationhttp.requestAuthNum(_userId, _phoneNum, _email);
				if (_JsonResults != null && _JsonResults.length() > 0) {
					JSONObject jsonobj;
					try {
						jsonobj = _JsonResults.getJSONObject(0);
						serverNumber = jsonobj.getString("certificationResult").trim();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} 
			catch (ErpBarcodeException e) {
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mRequestAuthNumInTask = null;
			super.onPostExecute(result);
			if (result) {
				String msg = "인증번호를 전송하였습니다.\n3분 이내로 입력해주세요";
				messageAlert(msg);
				timerStart();
			} 
			else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mRequestAuthNumInTask = null;
			super.onCancelled();
		}
	}
	
	private void timerStart() {
		authCount.setText("");
		mStartTime = 180;
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
        mTimerHandler.postDelayed(mUpdateTimeTask, 0);
	}
	private void timerStop() {
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
	}
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			int seconds = mStartTime;
			
			String counterTime = String.format("%03d", seconds) + "초";
			authCount.setText(counterTime);
			
			if (seconds <= 0) {
				timerStop();
				return;
			}

			mTimerHandler.postDelayed(this, 1000);
			mStartTime--;
		}
	};
	
	public void confirmAuthNum(String userId, String userAuth) {
		if (mConfirmAuthNumInTask == null) {
			mConfirmAuthNumInTask = new ConfirmAuthNumInTask(userId, userAuth);
			mConfirmAuthNumInTask.execute((Void) null);
		}
	}
	
	public class ConfirmAuthNumInTask extends AsyncTask<Void, Void, Boolean> {
		private String _userId = "";
		private String _userAuthNum = "";
		private ErpBarcodeException _ErpBarException;
		
		public ConfirmAuthNumInTask(String userId, String userAuth) {
			_userId = userId;
			_userAuthNum = userAuth;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.APP_SERVER);
//				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.QA_SERVER);
				LocationHttpController locationhttp = new LocationHttpController();
				locationhttp.confirmAuthNum(_userId, _userAuthNum, serverNumber);
			} 
			catch (ErpBarcodeException e) {
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mConfirmAuthNumInTask = null;
			super.onPostExecute(result);
			if (result) {
				passwordView.setVisibility(View.VISIBLE);
				timerStop();
			} 
			else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mConfirmAuthNumInTask = null;
			super.onCancelled();
		}
	}
	
	public void savePassword(String userId, String userPassword, String type) {
		if (mSavePasswordInTask == null) {
			mSavePasswordInTask = new SavePasswordInTask(userId, userPassword, type);
			mSavePasswordInTask.execute((Void) null);
		}
	}
	
	public class SavePasswordInTask extends AsyncTask<Void, Void, Boolean> {
		private String _userId = "";
		private String _userPassword = "";
		private String _type = "";
		private ErpBarcodeException _ErpBarException;
		
		public SavePasswordInTask(String userId, String userPassword, String type) {
			_userId = userId;
			_userPassword = userPassword;
			_type = type;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.APP_SERVER);
//				SessionUserData.getInstance().setAccessServer(HttpAddressConfig.QA_SERVER);
				LocationHttpController locationhttp = new LocationHttpController();
				locationhttp.savePassword(_userId, _userPassword, _type);
			} 
			catch (ErpBarcodeException e) {
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mSavePasswordInTask = null;
			super.onPostExecute(result);
			if (result) {
				String msg = "비밀번호를 변경하였습니다.\n변경한 비밀번호로 로그인하시기 바랍니다.";
				messageAlert(msg);
			} 
			else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mSavePasswordInTask = null;
			super.onCancelled();
		}
	}
	
	public void messageAlert(String message){
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
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

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
