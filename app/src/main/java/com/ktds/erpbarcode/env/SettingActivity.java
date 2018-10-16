package com.ktds.erpbarcode.env;

import java.util.ArrayList;
import java.util.List;

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
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.database.BpIItemQuery;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.bluetooth.KTSyncData;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;

public class SettingActivity extends Activity {
	
	private static final String TAG = "SettingActivity";
	private static final int ACTION_DEVICESCANACTIVITY = 1;  // 스케너바코드 연결
	
    public static final int HANDLER_MESSAGE_SUCCESS = 1;     // 성공..
    public static final int HANDLER_MESSAGE_ERROR = -1;      // 오류발생..
	
	private BpIItemQuery bpIItemQuery;
	private SettingPreferences mSharedSetting;
	
	//-----------------------------------------------------------
	// 앱 버젼 관리.
	//-----------------------------------------------------------
	private TextView mAppVersionTitleText;
	private TextView mAppVersionSummaryText;
	
	//-----------------------------------------------------------
	// 자재마스터 업데이트.
	//-----------------------------------------------------------
	private ProductMasterTotalCountInTask mTotalCountInTask;
	private TextView mSurveyMasterSummaryText;
	private ProgressBar mProductMasterUpdateProgress;

	
	//-----------------------------------------------------------
	// 소프트키보드 활성 여부.
	//-----------------------------------------------------------
	private CheckBox mBarcodeScannerCheckbox;
	private CheckBox mScannerSoftKeyboardCheckbox;

	//-----------------------------------------------------------
	// 사운드 및 효과음 활성 여부.
	//-----------------------------------------------------------
	private CheckBox mSoundEffectsLockCheckbox;
	
	//-----------------------------------------------------------
	// KDC Settings
	//-----------------------------------------------------------
	private CheckBox mAutoTrigger;
	//private CheckBox mReReadDelay;
	private CheckBox mConnectAlert;
	private CheckBox mBeepSound;
	private CheckBox mBeepOnScan;
	private CheckBox mBeepVolumeHigh;
	private CheckBox mVibrator;
	private CheckBox mAutoErase;
	private CheckBox mKeyPad;
	
	private boolean autoTriggerBackup;
	private int 	reReadDelayBackup;
	private boolean connectAlertBackup;
	private boolean beepSoundBackup;
	private boolean beepOnScanBackup;
	private boolean beepVolumeHighBackup;
	private boolean vibratorBackup;
	private boolean autoEraseBackup;
	private boolean keyPadBackup;
	
	private String strBTAddress;
	private String strFWVersion;
	private String strBTVersion;
	private String strSerial;
	private String strBatterryLevel;
	private String strFCCData;
	private String strMemoryLeft;
	private String strStoredBarocode;
	
	private TextView mBTAddress;
	private TextView mBTFWversion;
	
	private TextView mMemoryLeft;
	private TextView mStoredBarcode;
	
	private TextView mFWVersion;
	private TextView mSerial;
	private TextView mFCCBarcode;
	private TextView mBatteryLevel;
	
    private Spinner mSpinnerAutoTriggerDelay;
    private BasicSpinnerAdapter mAutoTriggerDelayAdapter;
    
    private GetKDCSettingTask mGetKDCSettingTask;
    
    
    private RelativeLayout mProgressBar;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setJobGubun("환경설정");
		GlobalData.getInstance().setNowOpenActivity(this);
		
		
        //mBarcodeSoundPlay = new BarcodeSoundPlay(getApplicationContext());
        bpIItemQuery = new BpIItemQuery(getApplicationContext());
		bpIItemQuery.open();
        setMenuLayout();
        setContentView(R.layout.env_setting_activity);
        setLayout();
        
        Log.i(TAG, "환경설정  Start...");
        mSharedSetting = new SettingPreferences(getApplicationContext());
        initPreferences();
    }
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.setting_title);
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
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
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

	private void setLayout() {
		//-----------------------------------------------------------
		// 앱 버젼 관리.
		//-----------------------------------------------------------
        ViewGroup appVersionButton = (ViewGroup) findViewById(R.id.setting_appversion_update_button);
        appVersionButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
					}
				});
        mAppVersionTitleText = (TextView) findViewById(R.id.setting_appversion_update_title);
        mAppVersionSummaryText = (TextView) findViewById(R.id.setting_appversion_update_summary);
		
		//-----------------------------------------------------------
		// 자재마스터 업데이트.
		//-----------------------------------------------------------
        ViewGroup surveyMasterButton = (ViewGroup) findViewById(R.id.setting_surveymaster_update_button);
        surveyMasterButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						surveyMasterYesNoDialog();
					}
				});
        mSurveyMasterSummaryText = (TextView) findViewById(R.id.setting_surveymaster_update_summary);
        mProductMasterUpdateProgress = (ProgressBar)  findViewById(R.id.setting_surveymaster_update_progress);
		
		//-----------------------------------------------------------
		// 스캐너 활성 여부
		//-----------------------------------------------------------
        mScannerSoftKeyboardCheckbox = (CheckBox) findViewById(R.id.setting_bluetooth_softkeyboard_checkbox);
        ViewGroup scannerSoftKeyboardButton = (ViewGroup) findViewById(R.id.setting_bluetooth_softkeyboard_button);
        scannerSoftKeyboardButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mScannerSoftKeyboardCheckbox.isChecked()) {
							mScannerSoftKeyboardCheckbox.setChecked(false);
							mSharedSetting.removeScannerSoftKeyboard();
							mSharedSetting.setScannerSoftKeyboard(false);
						} else {
							mScannerSoftKeyboardCheckbox.setChecked(true);
							mSharedSetting.setScannerSoftKeyboard(true);
						}
					}
				});
        if (!SessionUserData.getInstance().getAccessServer().equals(HttpAddressConfig.QA_SERVER)) {
        	scannerSoftKeyboardButton.setVisibility(View.GONE);
        }
        
        //-----------------------------------------------------------
  		// 사운드 및 효과음 활성 여부
  		//-----------------------------------------------------------
        mSoundEffectsLockCheckbox = (CheckBox) findViewById(R.id.setting_soundeffects_lock_checkbox);
        ViewGroup soundEffectsLockButton = (ViewGroup) findViewById(R.id.setting_soundeffects_lock_button);
        soundEffectsLockButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mSoundEffectsLockCheckbox.isChecked()) {
							mSoundEffectsLockCheckbox.setChecked(false);
							mSharedSetting.removeSoundEffectsLock();
							mSharedSetting.setSoundEffectsLock(false);
						} else {
							mSoundEffectsLockCheckbox.setChecked(true);
							mSharedSetting.setSoundEffectsLock(true);
						}
					}
				});
        
		//-----------------------------------------------------------
		// 미연결 경고설정
		//-----------------------------------------------------------
        mConnectAlert = (CheckBox) findViewById(R.id.setting_kdc_connect_alert_checkbox);
        ViewGroup connectAlertButton = (ViewGroup) findViewById(R.id.setting_kdc_connect_alert);
        connectAlertButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mConnectAlert.isChecked()) {
							mConnectAlert.setChecked(false);
						} else {
							mConnectAlert.setChecked(true);

						}
					}
				});
        
		//-----------------------------------------------------------
		// 자동 스캔 설정
		//-----------------------------------------------------------
		mAutoTrigger = (CheckBox) findViewById(R.id.setting_kdc_autotrigger_checkbox);
        ViewGroup autoTriggerButton = (ViewGroup) findViewById(R.id.setting_kdc_autotrigger);
        autoTriggerButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mAutoTrigger.isChecked()) {
							mAutoTrigger.setChecked(false);
						} else {
							mAutoTrigger.setChecked(true);

						}
					}
				});
        
        //-----------------------------------------------------------
  		// 자동스캔 간격 Spinner 셋팅
  		//-----------------------------------------------------------
        mSpinnerAutoTriggerDelay = (Spinner) findViewById(R.id.setting_autotrigger_delay_value);
        mSpinnerAutoTriggerDelay.setOnItemSelectedListener(
        		new OnItemSelectedListener() {
 					@Override
 					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 					}
 					@Override
 					public void onNothingSelected(AdapterView<?> parent) {
 					}
 				});

		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
		spinneritems.add(new SpinnerInfo("0", "연속 스캔"));
		spinneritems.add(new SpinnerInfo("1", "빠른 속도"));
		spinneritems.add(new SpinnerInfo("2", "중간 속도"));
		spinneritems.add(new SpinnerInfo("3", "느린 속도"));
		spinneritems.add(new SpinnerInfo("4", "아주 느린 속도"));
		
		mAutoTriggerDelayAdapter = new BasicSpinnerAdapter(getApplicationContext(), spinneritems);
		mSpinnerAutoTriggerDelay.setAdapter(mAutoTriggerDelayAdapter);
		mSpinnerAutoTriggerDelay.setSelection(0);

 		
        //-----------------------------------------------------------
  		// Auto Erase 설정
  		//-----------------------------------------------------------
		mAutoErase = (CheckBox) findViewById(R.id.setting_kdc_autoerase_checkbox);
        ViewGroup autoEraseButton = (ViewGroup) findViewById(R.id.setting_kdc_autoerase);
        autoEraseButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mAutoErase.isChecked()) {
							mAutoErase.setChecked(false);
						} else {
							mAutoErase.setChecked(true);

						}
					}
				});
        
        //-----------------------------------------------------------
  		// Beep Sound 설정
  		//-----------------------------------------------------------
        mBeepSound = (CheckBox) findViewById(R.id.setting_kdc_beepsound_checkbox);
        ViewGroup beepSoundButton = (ViewGroup) findViewById(R.id.setting_kdc_beepsound);
        beepSoundButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mBeepSound.isChecked()) {
							mBeepSound.setChecked(false);
						} else {
							mBeepSound.setChecked(true);

						}
					}
				});
        
        //-----------------------------------------------------------
  		// Beep On Scan 설정
  		//-----------------------------------------------------------
		mBeepOnScan = (CheckBox) findViewById(R.id.setting_kdc_beeponscan_checkbox);
        ViewGroup beepOnSoundButton = (ViewGroup) findViewById(R.id.setting_kdc_beeponscan);
        beepOnSoundButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mBeepOnScan.isChecked()) {
							mBeepOnScan.setChecked(false);
						} else {
							mBeepOnScan.setChecked(true);

						}
					}
				});
        
        //-----------------------------------------------------------
  		// Beep Volume High 설정
  		//-----------------------------------------------------------
		mBeepVolumeHigh = (CheckBox) findViewById(R.id.setting_kdc_beepvolumehigh_checkbox);
        ViewGroup beepVolumeHighButton = (ViewGroup) findViewById(R.id.setting_kdc_beepvolumehigh);
        beepVolumeHighButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mBeepVolumeHigh.isChecked()) {
							mBeepVolumeHigh.setChecked(false);
						} else {
							mBeepVolumeHigh.setChecked(true);

						}
					}
				});
        
        //-----------------------------------------------------------
  		// 진동 모드 설정
  		//-----------------------------------------------------------
		mVibrator = (CheckBox) findViewById(R.id.setting_kdc_vibrator_checkbox);
        ViewGroup vibratorButton = (ViewGroup) findViewById(R.id.setting_kdc_vibrator);
        vibratorButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mVibrator.isChecked()) {
							mVibrator.setChecked(false);
						} else {
							mVibrator.setChecked(true);

						}
					}
				});

        //-----------------------------------------------------------
  		// 키패드 활성화 설정
  		//-----------------------------------------------------------
		mKeyPad = (CheckBox) findViewById(R.id.setting_kdc_keypad_checkbox);
        ViewGroup keyPadButton = (ViewGroup) findViewById(R.id.setting_kdc_keypad);
        keyPadButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mKeyPad.isChecked()) {
							mKeyPad.setChecked(false);
						} else {
							mKeyPad.setChecked(true);

						}
					}
				});
     
        //-----------------------------------------------------------
  		// 메모리 리셋.
  		//-----------------------------------------------------------
        ViewGroup memoryResetButton = (ViewGroup) findViewById(R.id.setting_kdc_memoryreset_button);
        memoryResetButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(KTSyncData.bIsConnected)
							memoryResetYesNoDialog();
						else
					        Toast.makeText(getApplicationContext(),"스캐너와 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
					}
				});
        
        //-----------------------------------------------------------
  		// 시간 동기화.
  		//-----------------------------------------------------------
        ViewGroup clockSyncButton = (ViewGroup) findViewById(R.id.setting_kdc_clocksync_button);
        clockSyncButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(KTSyncData.bIsConnected)
							clockSyncYesNoDialog();
						else
					        Toast.makeText(getApplicationContext(),"스캐너와 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
					}
				});

        //-----------------------------------------------------------
  		// 공장 초기화.
  		//-----------------------------------------------------------
        ViewGroup factoryDefaultButton = (ViewGroup) findViewById(R.id.setting_kdc_factorydefault_button);
        factoryDefaultButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(KTSyncData.bIsConnected)
							factoryDefalutYesNoDialog();
						else
					        Toast.makeText(getApplicationContext(),"스캐너와 연결되지 않았습니다.",Toast.LENGTH_SHORT).show();
					}
				});
        
        
        mProgressBar = (RelativeLayout) findViewById(R.id.setting_progress);
		
	}
	

    public boolean isProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
    public void setProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	if (show) 
    		mProgressBar.setVisibility(View.VISIBLE);
    	else
    		mProgressBar.setVisibility(View.GONE);
    }
	
	private void initPreferences() {
		//-----------------------------------------------------------
  		// 앱 버젼 관리.
  		//-----------------------------------------------------------
		int versionNumber = GlobalData.getInstance().getAppVersionNumber();
		int serverVersionNumber = SessionUserData.getInstance().getNewAppVersionCode();
		String versionName = GlobalData.getInstance().getAppVersionName();
		String appversionSummary = "현재 버젼(" + versionName + ")이고, ";
		if (versionNumber < serverVersionNumber) {
			appversionSummary += "새로운 버젼이 업데이트 되었습니다.";
		} else {
			appversionSummary += " 최신 버젼입니다.";
		}
		
		mAppVersionTitleText.setText("어플리케이션 업데이트");
		mAppVersionSummaryText.setText(appversionSummary);

		//-----------------------------------------------------------
  		// 자재마스터 업데이트 마지막 일자를 보여준다.
  		//-----------------------------------------------------------
		execTotalCountInTask();
		
		
		//-----------------------------------------------------------
  		// 소프트키보드 활성 여부
  		//-----------------------------------------------------------
		if (mSharedSetting.isScannerSoftKeyboard()) {
			mScannerSoftKeyboardCheckbox.setChecked(true);
		} else {
			mScannerSoftKeyboardCheckbox.setChecked(false);
		}
		
		//-----------------------------------------------------------
  		// 사운드 및 효과음 활성 여부
  		//-----------------------------------------------------------
		if (mSharedSetting.isSoundEffectsLock()) {
			mSoundEffectsLockCheckbox.setChecked(true);
		} else {
			mSoundEffectsLockCheckbox.setChecked(false);
		}
		
		//-----------------------------------------------------------
		// 바코드 스케너 설정 정보를 가져화서 보여준다.
		//-----------------------------------------------------------
		if(KTSyncData.bIsConnected)
			execGetKDCSettingTask();
		else
			initKDCSetting();
		
	}

	protected void setDefaultKDCSettings() {
		
			//환경설정
			//------------------------------ 블루투스 설정---------------------------------------------
			//미연결경고
			mConnectAlert.setChecked(true);
			
			//------------------------------ 스캔 설정---------------------------------------------
			//자동 스캔
			mAutoTrigger.setChecked(false);
			mSpinnerAutoTriggerDelay.setSelection(1);

			
			//자동 삭제
			mAutoErase.setChecked(true);
	
			//비프 사운드
			mBeepSound.setChecked(true);
					
			//스캔 사운드
			mBeepOnScan.setChecked(true);
			
			//사운드 볼륨
			mBeepVolumeHigh.setChecked(false);
			
			//진동모드
			mVibrator.setChecked(false);
			
			//키패드활성화
			mKeyPad.setChecked(false);
	}
	
	protected void initKDCSetting() {
		
		if(KTSyncData.bIsConnected)
		{
			//환경설정
			//------------------------------ 블루투스 설정---------------------------------------------
			//미연결경고
			mConnectAlert.setChecked((KTSyncData.KDCSettings & KTSyncData.CONNECTALERT_MASK) != 0);
			connectAlertBackup = mConnectAlert.isChecked();
			
			//------------------------------ 스캔 설정---------------------------------------------
			//자동 스캔
			mAutoTrigger.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTO_TRIGGER_MASK) != 0);
			autoTriggerBackup = mAutoTrigger.isChecked();
			
			//자동 스캔 간격
			reReadDelayBackup = KTSyncData.RereadDelay;
			mSpinnerAutoTriggerDelay.setSelection(reReadDelayBackup);
			
			//------------------------------ 블루투스 설정---------------------------------------------
			//블루투스 정보
			strBTAddress	= new String(KTSyncData.MACAddress, 0, 12);
			strBTAddress	= "블루투스 주소 : " + strBTAddress;
			strBTVersion	= new String(KTSyncData.BTVersion, 0, 5);
			strBTVersion	= "블루투스 FW 버전 : " + strBTVersion;
			
			mBTAddress = (TextView) findViewById(R.id.setting_kdc_bluetooth_macaddress);
			mBTAddress.setText(strBTAddress);
			
			mBTFWversion = (TextView) findViewById(R.id.setting_kdc_bluetooth_fwversion);
			mBTFWversion.setText(strBTVersion);
			
			//------------------------------ 시스템 설정---------------------------------------------
			//메모리 정보  setting_kdc_memoryinfo_nstored
			strStoredBarocode = String.format("%d 개 데이터 저장됨",KTSyncData.StoredBarcode);
			strMemoryLeft = String.format("%d KB 남음",KTSyncData.MemoryLeft);
			
			mStoredBarcode = (TextView) findViewById(R.id.setting_kdc_memoryinfo_nstored);
			mStoredBarcode.setText(strStoredBarocode);
			
			mMemoryLeft = (TextView) findViewById(R.id.setting_kdc_memoryinfo_sizeleft);
			mMemoryLeft.setText(strMemoryLeft);
			
			//버전
			strFWVersion	 = new String(KTSyncData.FWVersion, 0, 10);
			strFWVersion 	 = "펌웨어 버전 : "+strFWVersion;
			strSerial		 = new String(KTSyncData.SerialNumber, 0, 10);
			strSerial		 = "시리얼 번호 : "+ strSerial;
			strFCCData	     = new String(KTSyncData.FCCNumber, 0, 16);
			strFCCData	     = "설비바코드 : "+ strFCCData;
			strBatterryLevel = String.format("%d 퍼센트 남음", KTSyncData.BatteryValue);
			strBatterryLevel = "배터리 잔량 : "+ strBatterryLevel;
			
			mFWVersion = (TextView) findViewById(R.id.setting_kdc_version_firmware);
			mFWVersion.setText(strFWVersion);
			
			mSerial = (TextView) findViewById(R.id.setting_kdc_version_serial);
			mSerial.setText(strSerial);
			
			mFCCBarcode = (TextView) findViewById(R.id.setting_kdc_version_fccnum);
			mFCCBarcode.setText(strFCCData);
			
			mBatteryLevel = (TextView) findViewById(R.id.setting_kdc_version_batteryleft);
			mBatteryLevel.setText(strBatterryLevel);
			
			//자동 삭제
			mAutoErase.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTOERASE_MASK) != 0);
			autoEraseBackup = mAutoErase.isChecked();
	
			//비프 사운드
			mBeepSound.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPSOUND_MASK) != 0);
			beepSoundBackup = mBeepSound.isChecked();
					
			//스캔 사운드
			mBeepOnScan.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPONSCAN_MASK) != 0);
			beepOnScanBackup = mBeepOnScan.isChecked();
			
			//사운드 볼륨
			mBeepVolumeHigh.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPVOLUME_MASK) != 0);
			beepVolumeHighBackup = mBeepVolumeHigh.isChecked();
			
			//진동모드
			mVibrator.setChecked((KTSyncData.KDCSettings & KTSyncData.VIBRATOR_MASK) != 0);
			vibratorBackup = mVibrator.isChecked();
			
			//키패드활성화
			mKeyPad.setChecked((KTSyncData.KDCSettings & KTSyncData.KEYPAD_MASK) != 0);
			keyPadBackup = mKeyPad.isChecked();
		}
		else
		{
			//------------------------------ 블루투스 설정---------------------------------------------
			//블루투스 정보
			strBTAddress	= "블루투스 주소 : 알수없음";
			strBTVersion	= "블루투스 FW 버전 : 알수없음";
			
			mBTAddress = (TextView) findViewById(R.id.setting_kdc_bluetooth_macaddress);
			mBTAddress.setText(strBTAddress);
			
			mBTFWversion = (TextView) findViewById(R.id.setting_kdc_bluetooth_fwversion);
			mBTFWversion.setText(strBTVersion);
			
			//------------------------------ 시스템 설정---------------------------------------------
			//메모리 정보  setting_kdc_memoryinfo_nstored
			strStoredBarocode = String.format("알수없음");
			strMemoryLeft = String.format("알수없음");
			
			mStoredBarcode = (TextView) findViewById(R.id.setting_kdc_memoryinfo_nstored);
			mStoredBarcode.setText(strStoredBarocode);
			
			mMemoryLeft = (TextView) findViewById(R.id.setting_kdc_memoryinfo_sizeleft);
			mMemoryLeft.setText(strMemoryLeft);
			
			//버전
			strFWVersion 	 = "펌웨어 버전 : 알수없음";
			strSerial		 = "시리얼 번호 : 알수없음";
			strFCCData	     = "설비바코드 : 알수없음";
			strBatterryLevel = "배터리잔량 : 알수없음";
			
			mFWVersion = (TextView) findViewById(R.id.setting_kdc_version_firmware);
			mFWVersion.setText(strFWVersion);
			
			mSerial = (TextView) findViewById(R.id.setting_kdc_version_serial);
			mSerial.setText(strSerial);
			
			mFCCBarcode = (TextView) findViewById(R.id.setting_kdc_version_fccnum);
			mFCCBarcode.setText(strFCCData);
			
			mBatteryLevel = (TextView) findViewById(R.id.setting_kdc_version_batteryleft);
			mBatteryLevel.setText(strBatterryLevel);
			
		}
	}
	
    @Override
    public void onDestroy() {
    	Log.i(TAG, "onDestry ");
 
    	if(KTSyncData.bIsConnected)
    	{
	    	KTSyncData.mKScan.WakeupCommand();        
	    	
	    	if(connectAlertBackup != mConnectAlert.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("bTaS", (mConnectAlert.isChecked()) ? 1: 0  );
	    	
	    	if(autoTriggerBackup != mAutoTrigger.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GtSM", (mAutoTrigger.isChecked()) ? 1: 0  );
	    	
	    	if(reReadDelayBackup != mSpinnerAutoTriggerDelay.getSelectedItemPosition())
	    		KTSyncData.mKScan.SendCommandWithValue("GtSD",mSpinnerAutoTriggerDelay.getSelectedItemPosition());
	    	
	    	if(autoEraseBackup != mAutoErase.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GnES", (mAutoErase.isChecked()) ? 1: 0  );
	    	
	    	if(beepSoundBackup != mBeepSound.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("Gb", (mBeepSound.isChecked()) ? 1: 0  );
	    	
	    	if(beepOnScanBackup != mBeepOnScan.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GbSS", (mBeepOnScan.isChecked()) ? 1: 0  );
	    	
	    	if(beepVolumeHighBackup != mBeepVolumeHigh.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GbV", (mBeepVolumeHigh.isChecked()) ? 1: 0  );
	    	
	    	if(vibratorBackup != mVibrator.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GnVS", (mVibrator.isChecked()) ? 1: 0  );
	 
	    	if(keyPadBackup != mKeyPad.isChecked())
	         	KTSyncData.mKScan.SendCommandWithValue("GkNS", (mKeyPad.isChecked()) ? 1: 0  );
			
	    	KTSyncData.mKScan.FinishCommand();
    	}
    	
        super.onDestroy();
    }
    
    
    private void execTotalCountInTask() {
    	if (mTotalCountInTask == null) {
    		mTotalCountInTask = new ProductMasterTotalCountInTask();
    		mTotalCountInTask.execute((Void) null);
    	}
    }
    
    /**
	 * 자재마스터 총건수 Display
	 */
	public class ProductMasterTotalCountInTask extends AsyncTask<Void, Void, Boolean> {
		private int _TotCount = 0;
		private ErpBarcodeException _ErpBarException;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				_TotCount = bpIItemQuery.totalCount();
			} catch (Exception e) {
				_ErpBarException = new ErpBarcodeException(-1, "자재마스터 총건수 조회중 오류가 발생했습니다.");
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (result) {
				String workDttm = mSharedSetting.getSurveyMasterUpdate_WorkDttm();
				if (_TotCount == 0) {
					mSurveyMasterSummaryText.setText("자재마스터 정보가 없습니다. 꼭 업데이트 하십시요.");
					mSurveyMasterSummaryText.setTextColor(Color.RED);
				} else {
					mSurveyMasterSummaryText.setText(_TotCount + "건의 자재정보가 있으며, " + workDttm + " 업데이트 하셨습니다.");
					mSurveyMasterSummaryText.setTextColor(Color.BLACK);
				}
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

	protected void memoryResetYesNoDialog() {
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		String message = "스캐너의 메모리를 전부 삭제 하시겠습니까?";

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

				// ---------------------------------------------------
				// 스캐너의 메모리를 리셋한다.
				// ---------------------------------------------------
				if(KTSyncData.bIsConnected)
				{
					KTSyncData.mKScan.ClearMemory();
				}else{
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "스캐너와 연결되지 않았습니다. 스캐너를 연결 하십시오"));
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
	   
	protected void clockSyncYesNoDialog() {
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		String message = "스캐너의 시간을 안드로이드폰 시간과 동기화 시키겠습니까 ?";

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

				// ---------------------------------------------------
				// 스캐너의시간을 안드로이드폰의 시간과 동기화 시킨다.
				// ---------------------------------------------------
				if(KTSyncData.bIsConnected)
				{
					KTSyncData.mKScan.SyncClock(true);
				}else{
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "스캐너와 연결되지 않았습니다. 스캐너를 연결 하십시오"));
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
	   
	protected void factoryDefalutYesNoDialog() {
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		String message = "스캐너의 설정을 초기화 하시겠습니까 ?";

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

				// ---------------------------------------------------
				// 스캐너의 설정을 초기화 한다..
				// ---------------------------------------------------
				if(KTSyncData.bIsConnected)
				{
					KTSyncData.mKScan.FactoryDefault();
					setDefaultKDCSettings();
				}else{
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "스캐너와 연결되지 않았습니다. 스캐너를 연결 하십시오"));
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
	   
	protected void surveyMasterYesNoDialog() {
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		String message = "자재마스터를 업데이트 하시겠습니까?";

		// mBarcodeSoundPlay.play(BarcodeSoundPlay.sound_notify);
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
		builder.setNegativeButton("모두", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);

				// ---------------------------------------------------
				// 자재마스터 정보 서버에서 조회하여 Update한다.
				// ---------------------------------------------------
				execOnlyOneSurveyMasterInTask("all");
			}
		});
		builder.setNeutralButton("추가/변경", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);

				// ---------------------------------------------------
				// 자재마스터 정보 서버에서 조회하여 Update한다.
				// ---------------------------------------------------
				execOnlyOneSurveyMasterInTask("append");
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
    
    /**
	 * 자재마스터 업데이트.
	 */
    private void execOnlyOneSurveyMasterInTask(String type) {
    	
    	mSurveyMasterSummaryText.setText("자재마스터 업데이트중입니다.");
		mProductMasterUpdateProgress.setVisibility(View.VISIBLE);
		
    	
		String workDttm = "";
		if (type.equals("all")) {
			workDttm = "00000000000000000";
		} else {
			workDttm = mSharedSetting.getSurveyMasterUpdate_WorkDttm();
		}
 
    	int totCount = bpIItemQuery.totalCount();
    	if (totCount == 0 || TextUtils.isEmpty(workDttm)) {
    		workDttm = "00000000000000000";
    	}
    	
    	ProductMasterUpdateService productMasterUpdate = 
    			new ProductMasterUpdateService(getApplicationContext(), new ProductMasterHandler());
    	productMasterUpdate.startUpdate("", workDttm);

    }

    private class ProductMasterHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case ProductMasterUpdateService.STATE_MAX_PROGRESS:
            		int max_count = msg.getData().getInt("count");
            		mProductMasterUpdateProgress.setMax(max_count);

            		break;
            	case ProductMasterUpdateService.STATE_INCREMENT_PROGRESS:
            		int increment_count = msg.getData().getInt("count");
            		mProductMasterUpdateProgress.incrementProgressBy(increment_count);
            		
            		break;
                case ProductMasterUpdateService.STATE_SUCCESS:
                	Log.i(TAG, "ProductMasterHandler  -------STATE_SUCCESS------");

                	String workDttm = SystemInfo.getNowDate() + SystemInfo.getNowTime() + "000";
        	    	mSharedSetting.setSurveyMasterUpdate(workDttm);
                	
                	//---------------------------------------------------
        			// 자재마스터 업데이트 마지막 일자를 보여준다.
        			//---------------------------------------------------
        			ProductMasterTotalCountInTask totalCountInTask = new ProductMasterTotalCountInTask();
        			totalCountInTask.execute((Void) null);
                	
                    break;
                case ProductMasterUpdateService.STATE_ERROR:
                	String errorMessage = msg.getData().getString("message");
                	ErpBarcodeException erpbarException = 
                			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
                	GlobalData.getInstance().showMessageDialog(erpbarException);
                    break;
            }
        }
    }


	/**
	 * 바코드 스케너 설정 정보 가져오기..*/
	private void execGetKDCSettingTask() {
		if (mGetKDCSettingTask == null) {
			setProgressVisibility(true);
			mGetKDCSettingTask = new GetKDCSettingTask();
			mGetKDCSettingTask.execute((Void) null);
		}
	}
	private class GetKDCSettingTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
					KTSyncData.bIsReadyForMenu = false;
					KTSyncData.mKScan.GetKDCSettings(getApplicationContext());
					for(int count=20; count>0; count--)
					{
						if(KTSyncData.bIsReadyForMenu == true)
						{
							KTSyncData.bIsReadyForMenu = false;
							break;								
						}
						KTSyncData.mKScan.Sleep(500);
					}
				
			} catch (Exception e) {
				_ErpBarException = new ErpBarcodeException(-1, "바코드 스케너 설정정보 가져오는 중 오류가 발생했습니다.");
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mGetKDCSettingTask = null;
			setProgressVisibility(false);

			if (result) {
				KTSyncData.KDCSettingsBackup = KTSyncData.KDCSettings;
		        initKDCSetting();
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mGetKDCSettingTask = null;
			setProgressVisibility(false);
		}
	}
	 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_DEVICESCANACTIVITY) {
				//---------------------------------------------------
				// 스케너바코드 연결	
				//---------------------------------------------------
				if (ScannerDeviceData.getInstance().isConnected()) {
					if (mSharedSetting.isBarcodeScannerConnect()) {
						mBarcodeScannerCheckbox.setChecked(true);
					} else {
						mBarcodeScannerCheckbox.setChecked(false);
					}
				} else {
					mBarcodeScannerCheckbox.setChecked(false);
				}
			}
		}
	}
}
