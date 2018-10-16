package com.ktds.erpbarcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.SettingPreferences;

public class PersonalInfoAgreeActivity extends Activity{
	private static final String TAG = "PersonalInfoAgreeActivity";
	
	public static final String INPUT_PWUPDATE_YN = "passwdUpdateYn";
	private String p_passwdUpdateYn = "";
	
	public static final String INPUT_NOTICE = "passwdIsNotice";
	private boolean p_passwdIsNotice = false;
	private SettingPreferences mSharedSetting;
	private Button mAgreeButton, mConfirm, mClose;
	private CheckBox mAgreeCheck1, mAgreeCheck2, mAgreeCheck3, mAgreeCheck4;
	
	
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.infoagree_activity);
    	
    	p_passwdUpdateYn = getIntent().getStringExtra(INPUT_PWUPDATE_YN);
    	p_passwdIsNotice = getIntent().getBooleanExtra(INPUT_NOTICE, false);
    	
    	mSharedSetting = new SettingPreferences(getApplicationContext());
		mAgreeCheck1 = (CheckBox) findViewById(R.id.checkBox1);
		mAgreeCheck1.setChecked(mSharedSetting.getInfoAgree1());
		mAgreeCheck2 = (CheckBox) findViewById(R.id.checkBox11);
		mAgreeCheck2.setChecked(mSharedSetting.getInfoAgree2());
		mAgreeCheck3 = (CheckBox) findViewById(R.id.checkBox2);
		mAgreeCheck3.setChecked(mSharedSetting.getInfoAgree3());
		mAgreeCheck4 = (CheckBox) findViewById(R.id.checkBox22);
		mAgreeCheck4.setChecked(mSharedSetting.getInfoAgree4());
		
		mAgreeButton = (Button) findViewById(R.id.allAgreeBtn);
		mAgreeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mAgreeCheck1.setChecked(true);
				mAgreeCheck2.setChecked(true);
				mAgreeCheck3.setChecked(true);
				mAgreeCheck4.setChecked(true);
			}
		});
		
		mConfirm = (Button)findViewById(R.id.confirm);
		mConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setData();
			}
		});
		
		mClose = (Button)findViewById(R.id.close);
		mClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	public void setData(){
		if(!mAgreeCheck1.isChecked() || !mAgreeCheck2.isChecked() || !mAgreeCheck3.isChecked() || !mAgreeCheck4.isChecked()){
			Toast.makeText(getApplicationContext(), "필수 약관내용에 동의를 해주셔야 서비스 이용 가능합니다.",Toast.LENGTH_SHORT).show();
			return;
		}
		
		mSharedSetting.setInfoAgree1(true);
		mSharedSetting.setInfoAgree2(true);
		mSharedSetting.setInfoAgree3(true);
		mSharedSetting.setInfoAgree4(true);
		
		Intent intent = new Intent();
        intent.putExtra(INPUT_PWUPDATE_YN, p_passwdUpdateYn);
        intent.putExtra(INPUT_NOTICE, p_passwdIsNotice);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}