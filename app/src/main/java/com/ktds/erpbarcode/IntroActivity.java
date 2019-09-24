package com.ktds.erpbarcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

@SuppressLint("Override") public class IntroActivity extends Activity {
	
	private static final String TAG = "IntroActivity";
	
	private final int PERMISSIONS_REQUEST_RESULT1 = 100;
	private final int PERMISSIONS_REQUEST_RESULT2 = 200;
	
	
	private Handler mTimerHandler = new Handler();
	
	private TextView mAppVersionText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//-----------------------------------------------------------
	    // ActionBar를 hide처리한다.
	    //----------------------------------------------------------- 
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//-----------------------------------------------------------
	    // ActionBar에 progress를 활성화 시킨다.
	    //-----------------------------------------------------------
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
	    //-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		setContentView(R.layout.base_intro_activity);
	}
    
    private void ScreenLoad(){
    	//-----------------------------------------------------------
		// 화면을그린다.
		//-----------------------------------------------------------
		setLayout();
		initScreen();
		//-----------------------------------------------------------
		// 2초후에 로그인엑티비티로 이동한다.
		//-----------------------------------------------------------
		mTimerHandler.postDelayed(mStartActivityTask, 2000);
    }
    
//    //-----------------------------------------------------------
//    // Permission check.
//    //----------------------------------------------------------- 
//    public boolean requestPermissionCheck(){
//        int sdkVersion = Build.VERSION.SDK_INT;
// 
//        // 해당 단말기의 안드로이드 OS버전체크
//        if(sdkVersion >= 23) {
//            // 버전 6.0 이상일 경우
// 
//            // 해당 퍼미션이 필요한지 체크 - true : 퍼미션 동의가 필요한 권한일 때 , false : 퍼미션 동의가 필요하지 않은 권한일 때.
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || 
//            	ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || 
//            	ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || 
//            	ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//                // true : 퍼미션 동의가 필요한 권한일 때
// 
//                // 사용자가 최초 퍼미션 체크를 했는지 확인한다. - true : 사용자가 최초 퍼미션 요청시 '거부'했을 때, false : 퍼미션 요청이 처음일 경우
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || 
//                	ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
//                	ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) || 
//                	ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
//                    // true : 사용자가 최초 퍼미션 요청시 '거부'해서 재요청일 때
//                	ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,
//                    					 Manifest.permission.READ_PHONE_STATE,Manifest.permission.BLUETOOTH},PERMISSIONS_REQUEST_RESULT2);
//                 } else {
//                    // false : 퍼미션 요청이 처음일 경우.
//                    // 퍼미션의 동의 여부를 다이얼로그를 띄워 요청한다. 이 때 '동의', '거부'의 결과값이 onRequestPermissionsResult 으로 콜백된다.
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,
//                    					 Manifest.permission.READ_PHONE_STATE,Manifest.permission.BLUETOOTH},PERMISSIONS_REQUEST_RESULT1);
//                 }
//            }
//        	else {
//                // false : 퍼미션 동의가 필요하지 않은 권한일 때.
//            	ScreenLoad();
//            }
//        }else{
//            // version 6 이하일 때에는 별도의 작업이 필요없다.
//        	ScreenLoad();
//        }
//
//        return true;
//
//    }
//    
//    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        if (PERMISSIONS_REQUEST_RESULT1 == requestCode) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 퍼미션에 동의 했을 때 콜백. 다음 작업을 진행한다.
//            	ScreenLoad();
//            } else {
//            	ShowCloseMsg();
//            }
//            return;
//        }
//
//    }
//    
//    public void ShowCloseMsg(){
//		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
//		GlobalData.getInstance().setGlobalAlertDialog(true);
//
//		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
//		final Builder builder = new AlertDialog.Builder(this);
//		builder.setIcon(android.R.drawable.ic_menu_info_details);
//		builder.setTitle("알림");
//		TextView msgText = new TextView(this);
//		msgText.setPadding(10, 30, 10, 30);
//		msgText.setText("허용하지 않은 권한이 있습니다. 서비스를 종료합니다.");
//		msgText.setGravity(Gravity.CENTER);
//		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
//		msgText.setTextColor(Color.BLACK);
//		builder.setView(msgText);
//		builder.setCancelable(false);
//		builder.setNegativeButton("확인",
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						GlobalData.getInstance().setGlobalAlertDialog(false);
//						Intent intent = new Intent();
//						intent.setAction(Intent.ACTION_MAIN);
//						intent.addCategory(Intent.CATEGORY_HOME);
//						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent);
//						android.os.Process.killProcess(android.os.Process.myPid());
//						return;
//					}
//				});
//		AlertDialog dialog = builder.create();
//		dialog.show();
//		return;
//	}
    
	
	private void setLayout() {
		//-----------------------------------------------------------
		// 앱 버젼
		//-----------------------------------------------------------
		mAppVersionText = (TextView) findViewById(R.id.intro_appversion);
	}
	
	@Override
	protected void onResume(){
//		requestPermissionCheck();
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		ScreenLoad();
	}
	
	@Override
	protected void onDestroy() {
		mTimerHandler.removeCallbacks(mStartActivityTask);
		super.onDestroy();
	}
	
	private Runnable mStartActivityTask = new Runnable() {
		public void run() {
			if (checkPermission()) {
				goLogin();
			}
		}
	};
	
	private void initScreen() {
		String versionInfo = GlobalData.getInstance().getAppVersionName();
		mAppVersionText.setText(versionInfo);
	}

	private void goLogin() {

		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public boolean checkPermission() {

		if (Build.VERSION.SDK_INT >= 23) {
			if (!checkPermission(Manifest.permission.READ_PHONE_STATE) ||
					!checkPermission(Manifest.permission.CAMERA)) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA},1000);
				return false;
			}
		}
		return true;
	}

	public boolean checkPermission(String permission) {

		if (Build.VERSION.SDK_INT >= 23) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case 1000: {

				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					for( int result : grantResults) {
						if (result != PackageManager.PERMISSION_GRANTED) {
							processPermission(false);
							return;
						}
					}
					processPermission(true);
				} else {
					processPermission(false);
				}
				return;
			}
		}
	}

	private void processPermission(boolean granted) {

		if (granted) {
			goLogin();
		} else {
			String message = "필수 권한에 동의하지 않으면 앱을 사용할 수 없습니다.";

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
			builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					androidKillProcess();
					return;
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * 앱을 종료한다.
	 */
	private void androidKillProcess() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
