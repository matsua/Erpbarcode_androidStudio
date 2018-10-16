package com.ktds.erpbarcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;

public class ChangePasswordActivity extends Activity{
	private static final String TAG = "ChangePasswordActivity";
	
	// UI references.
	private LinearLayout contentsView, errMsgView;
	private LinearLayout contentsBtnView, contentsBtnView2;
	private EditText password, passwordConfirm;
	private Button btn1;
	private TextView errMsgText;

	private ChangePasswordInTask mChangePasswordInTask;
	
	private boolean change = false;
	
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.change_password_popup);
    	
    	contentsView = (LinearLayout) findViewById(R.id.changeView);
    	errMsgView = (LinearLayout) findViewById(R.id.errMsg);
    	errMsgText = (TextView) findViewById(R.id.errMsg_text);
    	
    	password = (EditText) findViewById(R.id.changePw1);
    	passwordConfirm = (EditText) findViewById(R.id.changePw2);
    	
    	contentsBtnView = (LinearLayout) findViewById(R.id.contentsBtnView);
    	contentsBtnView2 = (LinearLayout) findViewById(R.id.contentsBtnView2);
    	Button resultBtn = (Button) findViewById(R.id.resultBtn);
    	resultBtn.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(change){
//						로그아웃후 종료 
						finish();
					}
					else{
						contentsView.setVisibility(View.VISIBLE);
						errMsgView.setVisibility(View.GONE);
						contentsBtnView.setVisibility(View.VISIBLE);
						contentsBtnView2.setVisibility(View.GONE);
						errMsgText.setText("");
						password.setText("");
						passwordConfirm.setText("");
					}
				}
			});
    	
    	Button closeBtn = (Button) findViewById(R.id.closeBtn);
    	closeBtn.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
    	
    	btn1 = (Button) findViewById(R.id.confirmBtn1);
    	btn1.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!password.getText().toString().equalsIgnoreCase(passwordConfirm.getText().toString()))
					{
						msgCall("비밀번호가 일치하지 않습니다.");
		                return;
		            }
					if (password.getText().toString().length() < 5 ||
							(password.getText().toString().indexOf("*") < 0 && 
									password.getText().toString().indexOf("+") < 0 && 
									password.getText().toString().indexOf("!") < 0 && 
									password.getText().toString().indexOf("(") < 0 && 
									password.getText().toString().indexOf(")") < 0 && 
									password.getText().toString().indexOf("^") < 0 && 
									password.getText().toString().indexOf("#") < 0 && 
									password.getText().toString().indexOf("@") < 0 && 
									password.getText().toString().indexOf("\\") < 0))
		            {
						msgCall("비밀번호는 5자리 이상이며\n\r특수문자를 하나 이상 포함하시기 바랍니다.");
		                return;
		            }
					if (password.getText().toString().equals("1234!"))
					{
						msgCall("초기 비밀번호로 변경하실 수 없습니다.");
		                return;
		            }
					changePassword(password.getText().toString());
				}
			});
    	
    	Button btn2 = (Button) findViewById(R.id.confirmBtn2);
    	btn2.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
	}
	
	public void msgCall(String message){
		contentsView.setVisibility(View.GONE);
		errMsgView.setVisibility(View.VISIBLE);
		contentsBtnView.setVisibility(View.GONE);
		contentsBtnView2.setVisibility(View.VISIBLE);
		errMsgText.setText(message);
	}
	
	
	public void changePassword(String password) {
		if (mChangePasswordInTask == null) {
			mChangePasswordInTask = new ChangePasswordInTask(password);
			mChangePasswordInTask.execute((Void) null);
		}
	}
	
	public class ChangePasswordInTask extends AsyncTask<Void, Void, Boolean> {
		private String _password = "";
		private ErpBarcodeException _ErpBarException;
		
		public ChangePasswordInTask(String password) {
			_password = password;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController locationhttp = new LocationHttpController();
				locationhttp.changePassword(_password);
			} 
			catch (ErpBarcodeException e) {
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mChangePasswordInTask = null;
			super.onPostExecute(result);
			if (result) {
				contentsView.setVisibility(View.GONE);
				errMsgView.setVisibility(View.VISIBLE);
				contentsBtnView.setVisibility(View.GONE);
				contentsBtnView2.setVisibility(View.VISIBLE);
				
				errMsgText.setText("비밀번호를 성공적으로 변경하였습니다.\n\r새로운 비밀번호로 다시 로그인하시기 바랍니다.");
				change = true;
			} 
			else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mChangePasswordInTask = null;
			super.onCancelled();
		}
	}
	
	
}