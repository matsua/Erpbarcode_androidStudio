package com.ktds.erpbarcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.SettingPreferences;
import com.ktds.erpbarcode.job.JobActionManager;

public class GlobalData {
	private static GlobalData instance;
	
	//-----------------------------------------------------------
    // 현재 오픈된 Activity를 Set한다.
    //-----------------------------------------------------------
	private Activity mActivity;
	
	//-----------------------------------------------------------
    // XML 환경설정 레퍼런스
    //-----------------------------------------------------------
	private SettingPreferences mSharedSetting = null;
	
	//-----------------------------------------------------------
    // 에러/안내 메지시 사운드처리 Class
    //-----------------------------------------------------------
	private BarcodeSoundPlay mBarcodeSoundPlay = null;

	//-----------------------------------------------------------
    // 작업명(서브메뉴명)
    //-----------------------------------------------------------
	private String jobGubun;
	//-----------------------------------------------------------
    // 작업관리 SqlLite WorkInfo Key
    //-----------------------------------------------------------
	private int workId;
	private JobActionManager mJobActionManager;
	
	//-----------------------------------------------------------
    // 백그라운드 작업시 2중작업 안되게 처리하는  Flag
    //-----------------------------------------------------------
	private boolean isGlobalProgress;
	//-----------------------------------------------------------
    // Dialog화면 호출시 Open/Close 처리하는 Flag
    //-----------------------------------------------------------
	private boolean isGlobalAlertDialog;

    //-----------------------------------------------------------
    // 화면 초기 Setting후 변경 여부 Flag
    //-----------------------------------------------------------
	private boolean changeFlag;
	
	// 전송 가능 여부 flag
	private boolean sendAvailFlag;
	
	//-----------------------------------------------------------
    // 앱 현재 버젼
    //-----------------------------------------------------------
	private Integer appVersionNumber;
	private String appVersionName;

	private boolean adminFlag;

	public static synchronized GlobalData getInstance() {
		if (instance == null) {
			instance = new GlobalData();
			instance.setJobGubun("");
			instance.isGlobalProgress = false;
			instance.isGlobalAlertDialog = false;
			instance.setWorkId(0);
			instance.setChangeFlag(false);
			instance.setSendAvailFlag(false);
		}
		return instance;
	}
	
	/**
	 * 현재 열려있는 Activity로 Get한다.
	 * 
	 * @param activity
	 */
	public Activity getNowOpenActivity() {
		return mActivity;
	}
	
	/**
	 * 현재 열려있는 Activity로 Set한다.
	 * 
	 * @param activity
	 */
	public void setNowOpenActivity(Activity activity) {
		mActivity = activity;
		if (mBarcodeSoundPlay == null) {
			mBarcodeSoundPlay = new BarcodeSoundPlay(activity.getApplicationContext());
		}
		if (mSharedSetting == null) {
			mSharedSetting = new SettingPreferences(activity.getApplicationContext());
			mSharedSetting.setSoundEffectsLock(true);		// App 구동 시 Sound 무조건 나게...
		}
	}

	public String getJobGubun() {
		return jobGubun;
	}

	public void setJobGubun(String jobGubun) {
		this.jobGubun = jobGubun;
	}

	public boolean isGlobalProgress() {
		return isGlobalProgress;
	}

	public void setGlobalProgress(boolean isGlobalProgress) {
		this.isGlobalProgress = isGlobalProgress;
	}

	public boolean isGlobalAlertDialog() {
		return isGlobalAlertDialog;
	}

	public void setGlobalAlertDialog(boolean isGlobalAlertDialog) {
		this.isGlobalAlertDialog = isGlobalAlertDialog;
	}

	/**
	 * 작업ID set,get
	 */
	public int getWorkId() {
		return workId;
	}
	public void setWorkId(int workId) {
		this.workId = workId;
	}
	
	/**
	 * 작업관리 엑션 매니져
	 */
	public void initJobActionManager() {
		mJobActionManager = new JobActionManager(mActivity.getApplicationContext());
	}
	
	public JobActionManager getJobActionManager() {
		return mJobActionManager;
	}

	/**
	 * 변경된 엑션 여부
	 */
	public boolean isChangeFlag() {
		return changeFlag;
	}

	public void setChangeFlag(boolean changeFlag) {
		this.changeFlag = changeFlag;
	}
	
	/**
	 * 앱 버젼 관리.
	 */
	public int getAppVersionNumber() {
		if (appVersionNumber == null) setSystemAppVersion();
		return appVersionNumber;
	}
	public String getAppVersionName() {
		if (appVersionName == null || appVersionName.isEmpty()) setSystemAppVersion();
		return appVersionName;
	}
	public void setSystemAppVersion() {
		int versionNumber = 0;
		String versionName = "0.0";
		
		PackageInfo pinfo = null;
		try {
			pinfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			pinfo = null;
		}
		if (pinfo != null) {
			versionNumber = pinfo.versionCode;
			versionName = pinfo.versionName;
			
			this.appVersionNumber = versionNumber;
			this.appVersionName = versionName;
		}
	}

	/**
	 * 강제로 로그인 화면으로 이동한다.
	 */
	public void gotoLoginActivity() {

		if (mActivity == null) return;

		//---------------------------------------------------------------------
		// 로그인화면으로 이동 Yes/No Dialog
    	//---------------------------------------------------------------------
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
    	String message = "세션이 종료되었습니다. \n\r재접속 하시겠습니까?\n\r(저장하지 않은 자료는 재 작업하셔야 합니다.)"; 
    	
    	final Builder builder = new AlertDialog.Builder(mActivity);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(mActivity.getApplicationContext());
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
            	
            	Intent intent = new Intent(mActivity, LoginActivity.class);
            	mActivity.startActivity(intent);
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
	
	public void showMessageDialog(final ErpBarcodeMessage erpBarcodeMessage) {
		if (mActivity == null) return;
		
		//-----------------------------------------------------------
		// 이미 AlertDialog가 열려있으면, 종료한다.
		//-----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		
		final String message = erpBarcodeMessage.getMessage();
		final int sound = erpBarcodeMessage.getSound();
		String title = "알림";
		if (sound == BarcodeSoundPlay.SOUND_NOT_PLAY) {
			soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		} else {
			soundPlay(sound);
		}
		
		//---------------------------------------------------------------------
		// 메시지 Close Dialog
    	//---------------------------------------------------------------------
		final Builder builder = new AlertDialog.Builder(mActivity); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(title);
        TextView msgText = new TextView(mActivity.getApplicationContext());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	//-------------------------------------------------------------
            	// ErpBarcodeMessage.getMessageCode() == 99900 은 
            	//                   메세지만 화면에 보여주고 정상적인 작업진행임.
            	// 단계별 작업을 여기서 취소한다.
            	//-------------------------------------------------------------
            	if (erpBarcodeMessage.getMessageCode() != ErpBarcodeMessage.NORMAL_PROGRESS_MESSAGE_CODE) {
            		if (GlobalData.getInstance().getJobActionManager() != null
            				&& GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
                		GlobalData.getInstance().getJobActionManager().getJobStepManager().errorHandler();
                	}
            	}
            }
        });
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}
	
	/**
     * 메세지 다이얼로그 
     * Modify by sesang 20190425 리스 자산 처리 : 메세지박스 중복 노출 가능하도록 수정 
     */
	public void showMessageDialog(ErpBarcodeException erpbarException) {
		
		showMessageDialog(erpbarException, false);
	}

	public void showMessageDialog(ErpBarcodeException erpbarException, final DialogInterface.OnClickListener onClickListener) {

		showMessageDialog(erpbarException, false, onClickListener);
	}

	public void showMessageDialog(ErpBarcodeException erpbarException, boolean duplicate) {
		showMessageDialog(erpbarException, false, null);
	}

	public void showMessageDialog(ErpBarcodeException erpbarException, boolean duplicate, final DialogInterface.OnClickListener onClickListener) {
		
		if (mActivity == null) return;
		
		//-----------------------------------------------------------
		// Session Time Out 이면 LogOut처리한다.
		//-----------------------------------------------------------
		if (erpbarException.getErrCode() == -99999) {
			gotoLoginActivity();
			return;
		}
		
		//-----------------------------------------------------------
		// 이미 AlertDialog가 열려있으면, 종료한다.
		//-----------------------------------------------------------
		
		if (!duplicate && GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		
		final String message = erpbarException.getErrMessage();
		final int sound = erpbarException.getSound();
		String title = "알림";

		if (sound == BarcodeSoundPlay.SOUND_NOT_PLAY) {
			if (message.contains("유효하지 않은") 
					|| message.contains("위치바코드를 스캔하세요")		 
					|| message.contains("자재을 선택하세요.") 
					|| message.contains("자재명을 입력하세요.") 
					|| message.contains("없습니다") 
					|| message.contains("처리할 수 없는") 
					|| message.contains("기존에 전송한") 
					|| message.contains("유선장비")		// 유선장비 일 경우 반드시 위치바코드를 스캔하세요. 
					|| message.contains("WBS 번호를")	 
					|| message.contains("장치ID를 확인하시기 바랍니다")	 
					|| message.contains("'인수' 대상인 설비바코드가")	 
					|| message.contains("SAP 내부 메세지")	 
					|| message.contains("최하위")	 
					|| message.contains("이동 중인 설비가")	 
					|| message.contains("요청사유를")	 
					|| message.contains("요청 중인")	 
					|| message.contains("요청개수를")	 
					|| message.contains("요청되지 않았거나")	 
					|| message.contains("자재코드를 입력하세요")	 
					|| message.contains("전송할 설비")	 
					|| message.contains("고장코드를")	 
					|| message.contains("선택된 항목이")
					|| message.contains("처리할 수 없으므로")
					|| message.contains("네트워크 상태를")
					|| message.contains("자재정보가 존재하지")
					|| message.contains("가상창고/실 위치 점검은")) {
				soundPlay(BarcodeSoundPlay.SOUND_ERROR);
			} else {
				soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
			}
		} else {
			soundPlay(sound);
		}
		
		
		//---------------------------------------------------------------------
		// 메시지 Close Dialog
    	//---------------------------------------------------------------------
		final Builder builder = new AlertDialog.Builder(mActivity); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(title);
        TextView msgText = new TextView(mActivity.getApplicationContext());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	//-------------------------------------------------------------
            	// 단계별 작업을 여기서 취소한다.
            	//-------------------------------------------------------------
            	if (GlobalData.getInstance().getJobActionManager() != null
            			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
            		GlobalData.getInstance().getJobActionManager().getJobStepManager().errorHandler();
            	}
				if (onClickListener != null) {
					onClickListener.onClick(dialog, which);
				}
            }
        });
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}
	
	public void showImageDialog(Bitmap bitmap) {
		
		if (mActivity == null) return;
		
		
		
		//-----------------------------------------------------------
		// 이미 AlertDialog가 열려있으면, 종료한다.
		//-----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
	
		String title = "알림";
		
		//---------------------------------------------------------------------
		// 메시지 Close Dialog
    	//---------------------------------------------------------------------
		final Builder builder = new AlertDialog.Builder(mActivity); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(title);
		ImageView img = new ImageView(mActivity.getApplicationContext());
		img.setImageBitmap(bitmap);
		builder.setView(img);
		builder.setCancelable(false);
		builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	//-------------------------------------------------------------
            	// 단계별 작업을 여기서 취소한다.
            	//-------------------------------------------------------------
            	if (GlobalData.getInstance().getJobActionManager() != null
            			&& GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
            		GlobalData.getInstance().getJobActionManager().getJobStepManager().errorHandler();
            	}
            }
        });
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}

	public void soundPlay(int sound) {
		//---------------------------------------------------------------------
		// 환경설정에서 사운드 잠금 여부 = true이면 재생 한다.
    	//---------------------------------------------------------------------
		if (mSharedSetting != null) {
			if (!mSharedSetting.isSoundEffectsLock()) return;
		}
		
		if (mBarcodeSoundPlay != null) {
			mBarcodeSoundPlay.play(sound);
		}
	}

	public boolean isSendAvailFlag() {
		// TODO Auto-generated method stub
		return sendAvailFlag;
	}

	public void setSendAvailFlag(boolean sendAvailFlag) {
		this.sendAvailFlag = sendAvailFlag;
	}

	public void setAdminFlag(boolean _adminFlag) {
		// TODO Auto-generated method stub
		adminFlag = _adminFlag;
	}
	public boolean getAdminFlag() {
		// TODO Auto-generated method stub
		return adminFlag;
	}
}
