package com.ktds.erpbarcode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ktds.erpbarcode.LoginActivity.UserLoginTask;
import com.ktds.erpbarcode.LoginActivity.UserLogoutServiceTask;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.database.UserInfo;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UpgradeActivity extends Activity {

	private static final String TAG = UpgradeActivity.class.toString();
	
	private Handler mTimerHandler = new Handler();
	private RelativeLayout mDownloadProgress;
	private TextView mMessageText;
	
	private DownloadManager mDownloadManager;
	private long mDownloadId;
	
	private Uri downloadFileUrl;
	
	private UserLogoutServiceTask mUserLogoutTask;
	
	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent i) {
			Uri apk_uri = mDownloadManager.getUriForDownloadedFile(mDownloadId);
//			installAPK(apk_uri);
			installAPK(downloadFileUrl);
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//setTheme(R.style.Theme_Sherlock_Light);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		setMenuLayout();
		setContentView(R.layout.base_upgrade_activity);
		setLayout();
		
		mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		
		IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(mDownloadReceiver, intentFilter);

		Log.i(TAG, "Activity  Create   Start...");

		if (!TextUtils.isEmpty(SessionUserData.getInstance().getNewAppUri()) 
				&& SessionUserData.getInstance().getNewAppVersionCode() > 0) {
			mUserLogoutTask = new UserLogoutServiceTask();
			mUserLogoutTask.execute((Void) null);
		}
	}
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("프로그램 업그레이드");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
		mDownloadProgress = (RelativeLayout) findViewById(R.id.upgreade_download_progress);
		
		mMessageText = (TextView) findViewById(R.id.upgreade_message);
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
		unregisterReceiver(mDownloadReceiver);
		
		super.onDestroy();
	}

	private void showDownloadProgress(boolean show) {
		mDownloadProgress.setVisibility((show ? View.VISIBLE : View.GONE));
	}
	
	/**
	 * 앱 업그레이드 시작...
	 * @param url
	 */
	private void startAppUpgeade(String apk_uri) {
		
		showDownloadProgress(true);
		
		String message = "현재 앱 버젼은 (" + GlobalData.getInstance().getAppVersionName() + ")입니다.";
		//message += "\n새로운 버젼(" + GlobalData.getInstance().getAppVersionName() + "." + SessionUserData.getInstance().getNewAppVersionCode() + ")으로 업그레이드 합니다.";
		message += "\n새로운 버젼으로 업그레이드 합니다.";
		
		mMessageText.setText(message);
		
		Log.i(TAG, "startAppUpgeade   download url==>"+apk_uri);
		
		// 기존 앱을 삭제한다.
		//removeApp();
		
		mTimerHandler.postDelayed(new UpdateTimeTask(apk_uri), 1000);
	}

	private class UpdateTimeTask implements Runnable {
		private String _apk_uri;
		
		public UpdateTimeTask(String apk_uri) {
			_apk_uri = apk_uri;
		}
		
		public void run() {
			downloadFile(_apk_uri);
		}
	}
	
	/**
	 * 로컬 APK 파일 생성(디렉토리도 같이)
	 * @return
	 */
	private File createAPKFile() {
		File storageDirectory = null;
		final String PROJECT_NAME = "erpbarcode";
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDirectory = new File(Environment.getExternalStorageDirectory(), PROJECT_NAME);
			if (storageDirectory != null) {
				if (!storageDirectory.mkdirs()) {
					if (!storageDirectory.exists()){
						Log.i(TAG, "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.i(TAG, "External storage is not mounted READ/WRITE.");
			return null;
		}
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile = new File(storageDirectory.getPath() + File.separator + PROJECT_NAME + "_" + timeStamp + ".apk");
		
		return mediaFile;
	}
	
	/**
	 * APK 앱 설치 파일 다운로드.
	 * 
	 * @param url
	 * @return destinationFile  다운로드 파일
	 */
	private void downloadFile(String url) {
		File destinationFile = createAPKFile();
	    Log.i(TAG, "destinationFile==>"+destinationFile.getPath());

	    Uri apk_uri = Uri.parse(url);
	    
        DownloadManager.Request request = new DownloadManager.Request(apk_uri);
        //mDownloadId = mDownloadManager.enqueue(request);
        
        //File destinationFile = new File (storageFile, apk_uri.getLastPathSegment());
        //mDestinationFileUri = Uri.fromFile(destinationFile);
        //Log.i(TAG, "mDestinationFileUri==>"+mDestinationFileUri.toString());
        
        downloadFileUrl = Uri.fromFile(destinationFile);
        request.setDestinationUri(downloadFileUrl);
        mDownloadId = mDownloadManager.enqueue(request);

		//return destinationFile;
	}
	
	/**
	 * 앱 설치하기.
	 * 
	 * @param apk_uri
	 */
	private void installAPK(Uri apk_uri) {
		
		showDownloadProgress(false);
		
		/*
		//-----------------------------------------------------------
		// apk 업그레이드 후 ErpBarcode 앱 다시 실행하기 위해서. 브로드캐스트리시버 등록한다.
		//-----------------------------------------------------------
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        //filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        //filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        //filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        //filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter.addDataScheme("package");
        registerReceiver(mConnectivityReceiver, filter);
        */
		
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(apk_uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
	
	/**
	 * 앱 삭제하기.
	 */
	public void removeApp() {
		String packageName = "com.ktds.erpbarcode";
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
        finish();
    }
	
	/**
	 * 로그아웃.
	 */
	public class UserLogoutServiceTask extends AsyncTask<Void, Void, Boolean> {
		private String _errorMessage = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SignHttpController signController = new SignHttpController();
				signController.logOut(SessionUserData.getInstance().getUserId());
			} catch (ErpBarcodeException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mUserLogoutTask = null;
			if (result) {
				startAppUpgeade(SessionUserData.getInstance().getNewAppUri());
			} else {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, _errorMessage, BarcodeSoundPlay.SOUND_ERROR));
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mUserLogoutTask = null;
			super.onCancelled();
		}
	}
}
