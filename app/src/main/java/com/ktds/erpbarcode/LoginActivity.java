package com.ktds.erpbarcode;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.database.UserInfo;
import com.ktds.erpbarcode.common.database.UserInfoQuery;
import com.ktds.erpbarcode.common.http.HttpAddressConfig;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.NoticeActivity;
import com.ktds.erpbarcode.env.SettingPreferences;
import com.ktds.erpbarcode.env.model.NoticeInfo;
import com.ktds.erpbarcode.management.ResetPasswordActivity;

public class LoginActivity extends Activity {

	private static final String TAG = "LoginActivity";

	private static final int ACTION_REQUEST_CERTIFICATION = 2;
	private static final int ACTION_REQUEST_AGREE = 3;
	private static final int ACTION_REQUEST_NOTICE = 4;

	// Back Button 2번 클릭시 처리..
	private final long FINSH_INTERVAL_TIME = 2000;
	private long backPressedTime = 0;
	private long qaPressedTime = 0;
	private int qaPressedCount = 0;

	private SettingPreferences mSharedSetting;

	private String mUserId = "";
	private String mPassword = "";

	// UI references.
	private LinearLayout mButtonLayout;
	private EditText mUserIdView;
	private EditText mPasswordView;
	private View mStatusView;
	private CheckBox mOffline;

	private UserLoginTask mUserLoginTask;
	private UserLogoutServiceTask mUserLogoutTask;
	private boolean is_auth_need;

	// change_password open 여부
	private boolean changePassword = false;

	private static LoginActivity mMain = null;
	private static String mDetectName;
	private static String mReason;
	private static int mDetectCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// -----------------------------------------------------------
		// ActionBar에 progress를 활성화 시킨다.
		// -----------------------------------------------------------
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		// -----------------------------------------------------------
		// Open된 Activity를 Set한다.
		// -----------------------------------------------------------
		GlobalData.getInstance().setJobGubun("로그인");
		GlobalData.getInstance().setNowOpenActivity(this);

		// 운용서버로 디폴트 셋팅
		SessionUserData.getInstance().setAccessServer(
				HttpAddressConfig.APP_SERVER);
		// SessionUserData.getInstance().setAccessServer(HttpAddressConfig.QA_SERVER);

		mMain = LoginActivity.this;

		//Add by sesang 20190704 루팅 체크 추가 
		if (execCmd()) {

			showAlertDialog(GetDetectByName("ROOTED"), "루팅");
			return;
		}
		// end sesang

		// Scanrisk(1,"0x00200000", "ATRACE", "디버깅", "0.0.1");
		// Scanrisk(1,"0x00400000", "ROOTED", "루팅", "6.1");
		// -----------------------------------------------------------
		// 환경설정 XML파일을 제어하는 SettingPreferences 인스턴스 생성한다.
		// -----------------------------------------------------------
		mSharedSetting = new SettingPreferences(getApplicationContext());

		setMenuLayout();
		setContentView(R.layout.base_login_activity);

		setLayout();

		is_auth_need = true;
	}

	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.login_title);
		actionBar.setDisplayShowTitleEnabled(true);
		setProgressBarIndeterminateVisibility(false);
	}

	//Add by sesang 20190704 루팅 체크 추가
	public boolean execCmd() {
		if (BuildConfig.DEBUG) return false;
		boolean rootingFlag = false;

		try {
			Runtime.getRuntime().exec("su");
			rootingFlag = true;
		} catch (Exception e) {
			rootingFlag = false;
		}
		return rootingFlag;
	}
	// end sesang

	/**
	 * 화면 Layout.xml 설정한다.
	 */
	private void setLayout() {
		// Set up the login form.
		mButtonLayout = (LinearLayout) findViewById(R.id.login_button_layout);
		mButtonLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				qaChangeProcess();
			}
		});
		mUserIdView = (EditText) findViewById(R.id.login_userId);
		mPasswordView = (EditText) findViewById(R.id.login_password);

		mStatusView = findViewById(R.id.login_status);

		findViewById(R.id.login_signin).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		mOffline = (CheckBox) findViewById(R.id.login_offline_checkbox);

		mUserIdView.setText(mSharedSetting.getUserId());

		findViewById(R.id.login_password_reset).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						resetPassword("reset");
					}
				});
	}

	// =========================================================================================================================
	// Scanrisk: 메서드 이름과 파라메터가 아래와 동일해야 하며 이벤트 발생 시 자동으로 호출 됩니다.
	// 아래의 예제는 "(1) Scanrisk"에 대한 것 입니다.
	// -------------------------------------------------------------------------------------------------------------------------
	/*
	 *  (1) Scanrisk public static void Scanrisk( int policy, // 정책 (0: 허용, 1:
	 * 정책 위반) String code, // 그림 참조 String name, // 그림 참조 String reason, // 탐지
	 * 이유 String version) // 탐지 엔진(DxShield) 버전  (2) Scanrisk - plus public
	 * static void Scanrisk( int policy, // 정책 (0: 허용, 1: 정책 위반) String code, //
	 * 그림 참조 String name, // 그림 참조 String reason, // 탐지 이유 String version, // 탐지
	 * 엔진(DxShield) 버전 String packageMD5, // 패키지 코드 (패키지명의 MD5 값) String
	 * ansdoid_Id) // 안드로이드 ID  (3) Connect public static void Connect ( String
	 * packageMD5, // 패키지 코드 (패키지명의 MD5 값) String android_Id, // 안드로이드 ID String
	 * Model, // 단말기 모델명 String policyCode, // 현재 단말기의 정책 코드 String flags, // 접속
	 * 시점까지의 탐지 코드 String version, // 탐지 엔진(DxShield) 버전 String versionName, //
	 * 패키지 버전 이름 String BuildVersion) // 안드로이드 버전
	 */
	// =========================================================================================================================
	public static void Scanrisk(int policy, String code, String name,
								String reason, String version) {

		int ns = 0;
		while (mMain == null) {
			if (ns > 32) {
				ForceStop(0);
			}
			try {
				Thread.sleep(128);
			} catch (Throwable e) {
			}
			ns++;
		}

		// ---------------------------------------------------------------------------------------------------------------------
		// 정책 위반의 경우에 만 경고 창을 표시하는 예제 입니다.
		// 필요한 경우 policy가 0 일 때도 어떤 처리를 할 수 있습니다.
		// ---------------------------------------------------------------------------------------------------------------------
		if (policy == 1) {
			mDetectCount++;

			if (mDetectCount == 1) {
				// m_detectName = GetDetectByCode(code);
				mDetectName = GetDetectByName(name);
				mReason = reason;
				//
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						showAlertDialog(mDetectName, mReason);
					}
				};
				((LoginActivity) mMain).runOnUiThread(runnable);
			}
		}
	}

	// =========================================================================================================================
	// code 또는 name 을 Detect Name 으로 변화 시켜 줍니다.
	// =========================================================================================================================
	public static String GetDetectName(String code, int type) {
		String ids[] = { "0x00200000", // 0
				"0x00040000", // 1
				"0x40000000", // 2
				"0x00100000", // 3
				"0x00010000", // 4
				"0x00001000", // 5
				"0x00000800", // 6
				"0x00000400", // 7
				"0x00000200", // 8
				"0x01000000", // 9
				"0x02000000", // 10
				"0x00004000", // 11
				"0x00002000", // 12
				"0x00020000", // 13
				"0x00000100" // 14
		};

		String names[] = { "ATRACE", // 0
				"APIHOOK", // 1
				"DEXFILE", // 2
				"MEM", // 3
				"LIBMOD", // 4
				"VM", // 5
				"USBDEBUG", // 6
				"RISKWARE", // 7
				"MACRO", // 8
				"MIRRORING", // 9
				"RSH", // 10
				"ROOTED", // 11
				"ROOTABLE", // 12
				"LOCATION", // 13
				"HACKTOOL" // 14
		};

		String detectNames[] = { "디버깅", // 0
				"후킹", // 1
				"소스유출", // 2
				"메모리공격", // 3
				"SO파일변조", // 4
				"가상머신", // 5
				"USB디버깅", // 6
				"잠재적위험", // 7
				"매크로", // 8
				"미러링앱", // 9
				"루트쉘", // 10
				"루팅", // 11
				"루팅가능", // 12
				"위티조작", // 13
				"해킹툴" // 14
		};

		int i;

		if (type == 0) {
			for (i = 0; i < 15; i++) {
				if (code.equals(ids[i])) {
					break;
				}
			}
		} else {
			for (i = 0; i < 15; i++) {
				if (code.equals(names[i])) {
					break;
				}
			}
		}

		if (i < 15) {
			return detectNames[i];
		}

		return "UNKNOWN";
	}

	// =========================================================================================================================
	// code 를 Detect Name 으로 변환 시켜 줍니다.
	// =========================================================================================================================
	public static String GetDetectByCode(String id) {
		return GetDetectName(id, 0);
	}

	// =========================================================================================================================
	// name 을 Detect Name 으로 변환 시켜 줍니다.
	// =========================================================================================================================
	public static String GetDetectByName(String name) {
		return GetDetectName(name, 1);
	}

	// =========================================================================================================================
	// 경고 창을 표시 합니다.
	// =========================================================================================================================
	private static void showAlertDialog(String detectName, String reason) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMain);

		String message = null;
		String title = null;

		// ---------------------------------------------------------------------------------------------------------------------
		// 경고 창에 표시 할 메세지를 조합 합니다. 만약, 다국어를 지원 해야 하는 경우 Resource 사용을 권장 합니다.
		// ---------------------------------------------------------------------------------------------------------------------
		title = "안내";
		message = "[" + detectName + "] 사유로\n ERP Barcode 사용이 불가합니다.";
		// message += "Reason is " + reason;

		// Log.i("CallBack", message);

		builder.setTitle(title);
		builder.setMessage(message);

		// ---------------------------------------------------------------------------------------------------------------------
		// 확인 버튼을 눌렀을 경우 강제 종료 합니다.
		// ---------------------------------------------------------------------------------------------------------------------
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ForceStop(1);
			}
		});

		// ---------------------------------------------------------------------------------------------------------------------
		// 백(뒤로가기) 버튼을 눌렀을 경우 강제 종료 합니다.
		// ---------------------------------------------------------------------------------------------------------------------
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ForceStop(2);
			}
		});

		// ---------------------------------------------------------------------------------------------------------------------
		// 아무런 응답이 없을 경우 랜덤한 시간(약 8초 ~ 15초) 동안 기다린 후 자동 종료 합니다.
		// ---------------------------------------------------------------------------------------------------------------------
		Runnable runnable = new Runnable() {
			public void run() {
				ForceStop(3);
			}
		};
		WaitForAtuoStop(runnable);

		builder.setCancelable(true);
		builder.create();
		builder.show();
	}

	// =========================================================================================================================
	// 아무런 조치를 취하지 않을 경우 자동 종료를 위해 대기 합니다.
	// =========================================================================================================================
	private static void WaitForAtuoStop(Runnable runnable) {
		long wTimer;
		{
			wTimer = (System.currentTimeMillis() % 8) + 8;
			wTimer = (wTimer * 1000);

			Handler handler = new Handler();
			handler.postDelayed(runnable, wTimer);
		}
	}

	// =========================================================================================================================
	// 앱을 강제 종료 합니다.
	// =========================================================================================================================
	private static void ForceStop(int type) {
		// Log.i("CallBack", "Force Stop Code: " + type);

		ActivityCompat.finishAffinity(mMain);
	}

	// =========================================================================================================================
	// Scanrisk 호출 종료
	// =========================================================================================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			androidKillProcess();
		} else {
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		long tempTime = System.currentTimeMillis();
		long intervalTime = tempTime - backPressedTime;

		if (0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime) {
			androidKillProcess();
		} else {
			backPressedTime = tempTime;
			Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번더 누르시면 종료됩니다.",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 앱을 종료한다.
	 */
	private void androidKillProcess() {
		// ---------------------------------------------------------
		// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
		// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기 위해서..
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 여기는 Session Time
		// Out시 로그인 화면에서 종료시 모든 처리때문에..
		// ---------------------------------------------------------
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 바탕화면을 5번 터치하면 QA-SERVER로 변경한다.
	 */
	private void qaChangeProcess() {
		long tempTime = System.currentTimeMillis();
		long intervalTime = tempTime - qaPressedTime;
		Log.i(TAG, "qa click  tempTime,qaPressedTime==>" + tempTime + ","
				+ qaPressedTime);
		Log.i(TAG, "qa click  intervalTime==>" + intervalTime);

		if (0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime) {
			qaPressedCount++;
		} else {
			qaPressedTime = tempTime;
			qaPressedCount = 0;
		}
		Log.i(TAG, "qa click  qaPressedCount==>" + qaPressedCount);
		if (qaPressedCount > 4) {
			qaPressedCount = 0;

			if (SessionUserData.getInstance().getAccessServerName()
					.equalsIgnoreCase("운영")) {
				SessionUserData.getInstance().setAccessServer(
						HttpAddressConfig.QA_SERVER);
			} else {
				SessionUserData.getInstance().setAccessServer(
						HttpAddressConfig.APP_SERVER);
			}

			String message = "'"
					+ SessionUserData.getInstance().getAccessServerName()
					+ "' 서버로 전환되었습니다.";
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeMessage(1, message,
							BarcodeSoundPlay.SOUND_ASTERISK));
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mUserLoginTask != null) {
			return;
		}

		// Modify by sesang 20190425 비밀번호 변경 로직 삭제
		// if((mPasswordView.getText().toString()).equals("1234!")){
		// changePassword = true;
		// }

		// Reset errors.
		mUserIdView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUserId = mUserIdView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean isValied = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView
					.setError(getString(R.string.login_error_password_required));
			focusView = mPasswordView;
			isValied = true;
		} else if (mPassword.length() < 5) {
			mPasswordView
					.setError(getString(R.string.login_error_password_length));
			focusView = mPasswordView;
			isValied = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUserId)) {
			mUserIdView
					.setError(getString(R.string.login_error_userId_password));
			focusView = mUserIdView;
			isValied = true;
		}

		if (isValied) {
			focusView.requestFocus();
		} else {
			showProgress(true);

			// -------------------------------------------------------
			// 음영지역일때는 Local Sqlite에서 사용자 정보 비교한다.
			// -------------------------------------------------------
			if (mOffline.isChecked()) {
				offlineLogin(mUserId, mPassword);
			} else {
				// -------------------------------------------------------
				// 사용자ID, 비밀번호를 서버에서 비교한다.
				// -------------------------------------------------------
				mUserLoginTask = new UserLoginTask();
				mUserLoginTask.execute((Void) null);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */

	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private boolean _IsUpgraded = false;
		// Add by sesang 20190819 정합성 작업대상 수량 체크
		private int _consistencyCount = 0;
		// end sesang

		private List<NoticeInfo> _NoticeInfos;
		private boolean _IsNoticeCheck = false;
		private JSONArray _JsonResults = null;
		private String _errorMessage = "";

		@Override
		protected Boolean doInBackground(Void... params) {
			// -------------------------------------------------------
			// 로그인 체크.
			// -------------------------------------------------------
			String androidId = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
			String mPhoneNumber = "WIFI";
			TelephonyManager tm = (TelephonyManager) getApplicationContext()
					.getSystemService(getApplicationContext().TELEPHONY_SERVICE);
			if (tm.getLine1Number() != null) {
				mPhoneNumber = tm.getLine1Number();
				mPhoneNumber = mPhoneNumber.replace("+82", "0");
			}

			System.out.println("mPhoneNumber >>>>> " + mPhoneNumber);

			try {
				SignHttpController signController = new SignHttpController();
				Boolean dupulication = signController.logIn(mUserId, mPassword,
						androidId, mPhoneNumber);
				if (dupulication) {
					Log.i(TAG, "logIn  ErpBarcodeException ==> 중복로그인");
					mUserLoginTask = null;
					duplicationLogin();
					return true;
				}
			} catch (ErpBarcodeException e) {
				_errorMessage = e.getErrMessage();
				if (_errorMessage.contains("최근 접속 정보")) {
					_errorMessage = e.getErrMessage().split("/")[0];
				}
				Log.i(TAG, "logIn  ErpBarcodeException ==>" + _errorMessage);
				return false;
			}

			// -------------------------------------------------------
			// 로그인된 기기의 devId값을 DB저장
			// ** 주의 ** 이거는 성공/실패 여부는 불필요함.
			// -------------------------------------------------------
			try {
				SignHttpController signController = new SignHttpController();
				signController.setDeviceId(mUserId, androidId, mPhoneNumber);
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "서버에 요청중 오류가 발생했습니다. ==>" + e.getErrMessage());
			}

			// -------------------------------------------------------
			// 공지사항 있는지 체크.
			// ** 주의 ** 이거는 성공/실패 여부는 불필요함.
			// -------------------------------------------------------
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_NoticeInfos = basehttp.getNoticeInfos();
				if (_NoticeInfos == null) {
					throw new ErpBarcodeException(-1, "공지사항 조회 정보가 없습니다.");
				}
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "공지사항 서버에 요청중 오류가 발생했습니다. ==>" + e.getErrMessage());
				_IsNoticeCheck = false;
			}

			if (_NoticeInfos != null && _NoticeInfos.size() > 0) {
				_IsNoticeCheck = true;
			}

			// -------------------------------------------------------
			// 앱 버젼 체크.
			// ** 주의 ** 이거는 성공/실패 여부는 불필요함.
			// -------------------------------------------------------
			int appVersion = GlobalData.getInstance().getAppVersionNumber();
			// int appVersion = 300;

			try {
				SignHttpController signhttp = new SignHttpController();
				_JsonResults = signhttp.getProgramVersion(String
						.valueOf(appVersion));
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "logIn  ErpBarcodeException ==>" + e.getErrMessage());
			}

			if (_JsonResults != null && _JsonResults.length() > 0) {
				// -------------------------------------------------------------
				// 향후 자동다운로드를 아래 Url로 하면 됨.
				// -------------------------------------------------------------
				// {
				// "DOWN_URL":
				// "/file/download/get.do?path=PGM/39701/ErpBarcode_2013-12-20(16 11 59).apk",
				// "VERSION": "39701"
				// }
				// -------------------------------------------------------------
				try {
					JSONObject jsonobj = _JsonResults.getJSONObject(0);
					String newAppUri = jsonobj.getString("DOWN_URL").trim();
					int newAppApkVersion = jsonobj.getInt("VERSION");

					if (!newAppUri.isEmpty() && newAppApkVersion > 0) {
						// 로그아웃, 파일다운로드
						mUserLogoutTask = new UserLogoutServiceTask();
						mUserLogoutTask.loginProcessYn = false;
						mUserLogoutTask.execute((Void) null);

						if (SessionUserData.getInstance().getAccessServer()
								.equals(HttpAddressConfig.APP_SERVER)) {
							SessionUserData.getInstance().setNewAppUri(
									HttpAddressConfig.APP_DOWNLOAD_URL
											+ newAppUri);
						} else {
							SessionUserData.getInstance().setNewAppUri(
									HttpAddressConfig.QA_DOWNLOAD_URL
											+ newAppUri);
						}

						SessionUserData.getInstance().setNewAppVersionCode(
								newAppApkVersion);
						_IsUpgraded = true;
					}
				} catch (JSONException e) {
					Log.i(TAG, "logIn  App Version ==>" + e.getMessage());
					_IsUpgraded = false;
				}
			}
			// Add by sesang 20190819 정합성 작업대상 수량 체크
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_consistencyCount = basehttp.getConsistency(SessionUserData.getInstance().getOrgId());

			} catch (ErpBarcodeException e) {
				Log.i(TAG, "정합성 정보 서버에 요청중 오류가 발생했습니다. ==>" + e.getErrMessage());
			}
			// end sesang
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mUserLoginTask = null;
			showProgress(false);

			if (result) {
				// -----------------------------------------------------------
				// 로그인 기록을 파일에 저장한다. 로그인여부, 단말고유번호 - 중복로그인 체크
				// -----------------------------------------------------------
				mSharedSetting.setLoginYn(true, Secure.getString(
						getApplicationContext().getContentResolver(),
						Secure.ANDROID_ID));

				// -----------------------------------------------------------
				// 초기 비밀번호로 로그인했으므로 비밀번호 변경서비스로 이동
				// -----------------------------------------------------------
				if (changePassword) {
					Intent intent = new Intent(getApplicationContext(),
							ChangePasswordActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					changePassword = false;
					return;
				}

				if (SessionUserData.getInstance().isAuthenticated()) {
					mSharedSetting.setAutoLogin(false, SessionUserData
							.getInstance().getUserId(), "");

					SessionUserData.getInstance().setOffline(
							mOffline.isChecked()); // 음영지역 여부

					// ---------------------------------------------------------
					// 음영지역에서 사용할 사용자 정보를 저장한다.
					// ---------------------------------------------------------
					createUserInfo(mPassword);

					// ---------------------------------------------------------
					// 1일1회 마다 SMS 휴대폰 인증 여부 - 매접속시 개인인증으로 변경
					// ---------------------------------------------------------
					if (SessionUserData.getInstance().getConfirmationAdminYn()
							.equals("Y")) {
						is_auth_need = false;
					}

					// ---------------------------------------------------------
					// 3개월마다 강제 비밀번호 변경
					// ---------------------------------------------------------
					String passwdUpdateYn = SessionUserData.getInstance()
							.getPasswdUpdateYn();

					// ---------------------------------------------------------
					// 약관동의여부
					// ---------------------------------------------------------
					boolean agree = false;
					mSharedSetting = new SettingPreferences(
							getApplicationContext());
					if (mSharedSetting.getInfoAgree1()
							&& mSharedSetting.getInfoAgree2()
							&& mSharedSetting.getInfoAgree3()
							&& mSharedSetting.getInfoAgree4()) {
						agree = true;
					}
					// Modify by sesang 20190819 정합성 작업대상 수량 체크
					successLogInProcess(_IsNoticeCheck, _IsUpgraded,
							is_auth_need, passwdUpdateYn, agree, _consistencyCount);
					// end sesang

				} else {
					mPasswordView
							.setError(getString(R.string.login_error_userId_password));
					mPasswordView.requestFocus();
				}
			} else {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, _errorMessage,
								BarcodeSoundPlay.SOUND_ERROR));
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mUserLoginTask = null;
			super.onCancelled();
			showProgress(false);
		}
	}

	/**
	 * 중복로그인 Dialog
	 */
	private void duplicationLogin() {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			public void run() {
				dupulicationShowMsg();
			}
		}, 300);
	}

	public void dupulicationShowMsg() {
		if (GlobalData.getInstance().isGlobalAlertDialog())
			return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		final Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(this);
		msgText.setPadding(10, 30, 10, 30);
		// Modify by sesang 20190806 중복 로그인 경고 문구 수정
		//msgText.setText("비정상 종료 기록이 있습니다. \n\r로그아웃을 진행합니다. \n\r재로그인 해주세요.");
		msgText.setText("현재 중복접속 하였습니다.\n\r확인하시고 다시 접속 해주세요.");
		//end sesang
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);
				mUserLogoutTask = new UserLogoutServiceTask();
				mUserLogoutTask.loginProcessYn = false;
				mUserLogoutTask.execute((Void) null);
				return;
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}

	/**
	 * 음영지역일때 사용할 로그인 정보들을 Local Sqlite DB에 저장한다.
	 */
	private void createUserInfo(String password) {

		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(SessionUserData.getInstance().getUserId());
		userInfo.setPassword(password);
		userInfo.setUserName(SessionUserData.getInstance().getUserName());
		userInfo.setTelNo(SessionUserData.getInstance().getTelNo());
		userInfo.setPhoneNo(SessionUserData.getInstance().getUserCellPhoneNo());
		userInfo.setOrgId(SessionUserData.getInstance().getOrgId());
		userInfo.setOrgName(SessionUserData.getInstance().getOrgName());
		userInfo.setOrgCode(SessionUserData.getInstance().getOrgCode());
		userInfo.setOrgTypeCode(SessionUserData.getInstance().getOrgTypeCode());
		userInfo.setCompanyCode(SessionUserData.getInstance().getCompanyCode());

		UserInfoQuery userInfoQuery = new UserInfoQuery(getApplicationContext());
		userInfoQuery.open();

		UserInfo tempUserInfo = userInfoQuery.getUserInfoById(userInfo
				.getUserId());
		if (tempUserInfo != null) {
			userInfoQuery.deleteUserInfo(userInfo.getUserId());
		}

		userInfoQuery.createUserInfo(userInfo);

		userInfoQuery.close();
	}

	private void offlineLogin(String userId, String password) {
		UserInfoQuery userInfoQuery = new UserInfoQuery(getApplicationContext());
		userInfoQuery.open();

		UserInfo tempUserInfo = userInfoQuery.getUserInfoById(userId);
		if (tempUserInfo == null) {
			mUserIdView
					.setError(getString(R.string.login_error_userId_password));
			return;
		}

		if (!tempUserInfo.getPassword().equals(password)) {
			mPasswordView
					.setError(getString(R.string.login_error_password_required));
			return;
		}

		SessionUserData sessionData = SessionUserData.getInstance();
		sessionData.setOffline(mOffline.isChecked()); // 음영지역 여부
		sessionData.setAuthenticated(true);

		sessionData.setUserId(tempUserInfo.getUserId());
		sessionData.setUserName(tempUserInfo.getUserName());
		sessionData.setTelNo(tempUserInfo.getTelNo());
		sessionData.setUserCellPhoneNo(tempUserInfo.getPhoneNo());
		sessionData.setOrgId(tempUserInfo.getOrgId());
		sessionData.setOrgName(tempUserInfo.getOrgName());
		sessionData.setOrgCode(tempUserInfo.getOrgCode());
		sessionData.setOrgTypeCode(tempUserInfo.getOrgTypeCode());
		sessionData.setCompanyCode(tempUserInfo.getCompanyCode());

		// ---------------------------------------------------------
		// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
		// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기 위해서..
		// ---------------------------------------------------------
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
	// Modify by sesang 20190819 정합성 작업대상 수량 체크
	private void successLogInProcess(boolean isNoticeCheck, boolean isUpgraded,
									 boolean is_auth_need, String passwdUpdateYn, boolean isAgree, int consistencyCount) {
		if (isUpgraded) {
			// -----------------------------------------------------------
			// 이동 Dialog
			// -----------------------------------------------------------
			if (GlobalData.getInstance().isGlobalAlertDialog())
				return;
			GlobalData.getInstance().setGlobalAlertDialog(true);

			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
			String message = "프로그램이 변경되어 설치 페이지로 이동합니다.";
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
			builder.setNegativeButton("이동",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							GlobalData.getInstance()
									.setGlobalAlertDialog(false);

							// Intent intent = new Intent(Intent.ACTION_VIEW,
							// Uri.parse("http://nbaseqa.kt.com/nbase/m/init.do"));
							finish();
							Intent intent = new Intent(getApplicationContext(),
									UpgradeActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							return;
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
			return;
		}

		// -----------------------------------------------------------
		// 1일 1번 SMS본인 인증을 받는다.- (매접속시 개인인증으로 변경 2017.09) - (1일 1회 인증으로 변경
		// 2018.03)
		// 2016.12
		// -----------------------------------------------------------
		if (is_auth_need) {
			Intent intent = new Intent(getApplicationContext(),
					CertificationActivity.class);
			intent.putExtra(CertificationActivity.INPUT_PWUPDATE_YN,
					passwdUpdateYn);
			intent.putExtra(CertificationActivity.INPUT_NOTICE, isNoticeCheck);
			intent.putExtra(CertificationActivity.INPUT_AGREE, isAgree);
			// Add by sesang 20190819 정합성 작업대상 수량 체크
			intent.putExtra(CertificationActivity.INPUT_CONSISTENCY_COUNT, consistencyCount);
			// end sesang
			startActivityForResult(intent, ACTION_REQUEST_CERTIFICATION);
			return;
		}

		// -----------------------------------------------------------
		// 약괸동의를 받는다.
		// 2017.01
		// -----------------------------------------------------------
		if (!isAgree) {
			Intent intent = new Intent(getApplicationContext(),
					PersonalInfoAgreeActivity.class);
			intent.putExtra(PersonalInfoAgreeActivity.INPUT_PWUPDATE_YN,
					passwdUpdateYn);
			intent.putExtra(PersonalInfoAgreeActivity.INPUT_NOTICE,
					isNoticeCheck);
			// Add by sesang 20190819 정합성 작업대상 수량 체크
			intent.putExtra(PersonalInfoAgreeActivity.INPUT_CONSISTENCY_COUNT, consistencyCount);
			// end sesang
			startActivityForResult(intent, ACTION_REQUEST_AGREE);
			return;
		}

		// -----------------------------------------------------------
		// 3개월에 1번 강제 비밀번호 변경.
		// 2016.12
		// -----------------------------------------------------------
		if (passwdUpdateYn.equals("Y")) {
			resetPassword("update");
			return;
		}

		// -----------------------------------------------------------
		// 공지사항 있으므로 처리한다.
		// -----------------------------------------------------------
		if (isNoticeCheck) {
			String nowDate = SystemInfo.getNowDate();
			Log.i(TAG, "공지사항 오픈여부  ==>" + nowDate);
			Log.i(TAG, "공지사항 오픈여부  ==>" + mSharedSetting.isNotice_isNotOpen()
					+ "," + mSharedSetting.getNotice_NowDate());
			if (!mSharedSetting.isNotice_isNotOpen()
					|| !mSharedSetting.getNotice_NowDate().equals(nowDate)) {
				// -----------------------------------------------------------
				// 공지사항 화면을 Open한다.
				// -----------------------------------------------------------
				Intent intent = new Intent(getApplicationContext(),
						NoticeActivity.class);
				// Add by sesang 20190819 정합성 작업대상 수량 체크
				intent.putExtra(NoticeActivity.INPUT_CONSISTENCY_COUNT, consistencyCount);
				// end seang
				startActivityForResult(intent, ACTION_REQUEST_NOTICE);
				return;
			}
		}
		
		// Add by sesang 20190819 정합성 작업대상 수량 체크
		if (consistencyCount > 0) {
			if (GlobalData.getInstance().isGlobalAlertDialog())
				return;
			GlobalData.getInstance().setGlobalAlertDialog(true);

			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
			String message = "귀하의 조직 '"
					+ SessionUserData.getInstance().getOrgName()
					+ "'에\n복수 위치 정비 대상 장치ID\n"
					+ "수량은 '" + consistencyCount + "'건 입니다.\n"
					+ "세부내역은 바코드웹의\n"
					+ "정보조회 > '장치ID 정합성 확인'\n"
					+ "메뉴에서 확인하세요.";

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
							GlobalData.getInstance()
									.setGlobalAlertDialog(false);

							if (!SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) {
								checkOrg();
								return;
							}
							// ---------------------------------------------------------
							// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
							// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기
							// 위해서..
							// ---------------------------------------------------------
							finish();
							Intent intent = new Intent(getApplicationContext(),
									MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							
							return;
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
			return;
		}
		// ---------------------------------------------------------------------
		// DR-2014-15498 - PDA 및 스마트폰의 협력사 사용자 로그인 시 매핑된 KT운용조직 및 유지보수센터
		// 정보 제공하여 기계자산의 소유/운용부서 불일치 발생 방지 - request by 이종용 -> 2014.05.26 : 최미소
		// ---------------------------------------------------------------------
		if (!SessionUserData.getInstance().getOrgTypeCode().equals("INS_USER")) {
			checkOrg();
			return;
		}
		// end sesang
		// ---------------------------------------------------------
		// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
		// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기 위해서..
		// ---------------------------------------------------------
		finish();
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	protected void checkOrg() {
		
		if (GlobalData.getInstance().isGlobalAlertDialog())
			return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		String message = "귀하의 KT 조직은\n\r'"
				+ SessionUserData.getInstance().getSummaryOrgName()
				+ "'\n\r이며 유지보수센터는\n\r'"
				+ SessionUserData.getInstance().getCenterName() + "("
				+ SessionUserData.getInstance().getCenterId() + ")' 입니다.";

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
						GlobalData.getInstance()
								.setGlobalAlertDialog(false);

						// ---------------------------------------------------------
						// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
						// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기
						// 위해서..
						// ---------------------------------------------------------
						finish();
						Intent intent = new Intent(getApplicationContext(),
								MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						return;
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == ACTION_REQUEST_CERTIFICATION) {
				Boolean certification = false;
				certification = data.getExtras().getBoolean(
						CertificationActivity.CERTIFICATION);

				System.out.println("certification :: " + certification);

				if (certification) {
					is_auth_need = false;
					String passwdUpdateYn = data.getExtras().getString(
							CertificationActivity.INPUT_PWUPDATE_YN);
					Boolean isNotice = data.getExtras().getBoolean(
							CertificationActivity.INPUT_NOTICE, false);
					Boolean isAgree = data.getExtras().getBoolean(
							CertificationActivity.INPUT_AGREE, false);
					// Modify by sesang 20190819 정합성 작업대상 수량 체크
					int count = data.getExtras().getInt(CertificationActivity.INPUT_CONSISTENCY_COUNT, 0);
					successLogInProcess(isNotice, false, is_auth_need,
							passwdUpdateYn, isAgree, count);
					// end sesang
				} else {
					mPasswordView.setText("");
					mUserLogoutTask = new UserLogoutServiceTask();
					mUserLogoutTask.loginProcessYn = false;
					mUserLogoutTask.execute((Void) null);
				}
			} else if (requestCode == ACTION_REQUEST_AGREE) {
				String passwdUpdateYn = data.getExtras().getString(
						PersonalInfoAgreeActivity.INPUT_PWUPDATE_YN);
				Boolean isNotice = data.getExtras().getBoolean(
						PersonalInfoAgreeActivity.INPUT_NOTICE, false);
				// Modify by sesang 20190819 정합성 작업대상 수량 체크
				int count = data.getExtras().getInt(PersonalInfoAgreeActivity.INPUT_CONSISTENCY_COUNT, 0);

				successLogInProcess(isNotice, false, is_auth_need,
						passwdUpdateYn, true, count);
				// end sesang
			} else if (requestCode == ACTION_REQUEST_NOTICE) {
				// Modify by sesang 20190819 정합성 작업대상 수량 체크
				int count = data.getExtras().getInt(NoticeActivity.INPUT_CONSISTENCY_COUNT, 0);
				successLogInProcess(false, false, is_auth_need, "N", true, count);
				// end sesang
			}
		}
	}

	// Modify by sesang 20190425 비밀번호 초기화 변경
	public void resetPassword(String gb) {

		String message = "바코드 시스템의 패스워드가\nIDMS 로 통합되었습니다.\n패스워드 변경은 IDMS 를\n이용하시기 바랍니다.\nhttps://idms.kt.com\n* 사내망 PC 사용 필수";

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
				return;
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();

		// Intent intent = new Intent(getApplicationContext(),
		// ResetPasswordActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
		// Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.putExtra(ResetPasswordActivity.INPUT_PWCHANGE_TYPE, gb);
		// startActivity(intent);
	}

	public class UserLogoutServiceTask extends AsyncTask<Void, Void, Boolean> {
		private String _errorMessage = "";
		private boolean loginProcessYn = true;

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				SignHttpController signController = new SignHttpController();
				signController.logOut(mUserId);
			} catch (ErpBarcodeException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mUserLogoutTask = null;
			if (result) {
				if (loginProcessYn) {
					mUserLoginTask = new UserLoginTask();
					mUserLoginTask.execute((Void) null);
				}
			} else {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, _errorMessage,
								BarcodeSoundPlay.SOUND_ERROR));
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
