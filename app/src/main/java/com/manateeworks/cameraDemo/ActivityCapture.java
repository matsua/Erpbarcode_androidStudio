/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * A Derivative Work, changed by Manatee Works, Inc.
 *
 */

package com.manateeworks.cameraDemo;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
//import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.manateeworks.BarcodeScanner;
import com.manateeworks.cameraDemo.camera.CameraManager;

/**
 * The barcode reader activity itself. This is loosely based on the CameraPreview example included in the Android SDK.
 */
public final class ActivityCapture extends Activity implements SurfaceHolder.Callback {

//	private static final int ABOUT_ID = Menu.FIRST;
	private static final int MAX_RESULT_IMAGE_SIZE = 150;
	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;

	private static final String PACKAGE_NAME = "com.manateeworks.cameraDemo";
	private ActivityCaptureHandler handler;

	private View statusView;
	private View resultView;
	private static MediaPlayer mediaPlayer;
	private byte[] lastResult;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private static boolean playBeep;
	private static boolean vibrate = true;
	private boolean copyToClipboard;
	private String versionName;
	private ImageView debugImage;
	public static String lastStringResult;

	public static final Rect a = new Rect(2, 40, 96, 20);
	public static final Rect b = new Rect(2, 30, 96, 40);
	public static final Rect c = new Rect(20, 10, 60, 80);
	public static final Rect d = new Rect(20, 10, 60, 80);
	public static final Rect e = new Rect(5, 30, 90, 40);
	public static final Rect f = new Rect(20, 5, 60, 90);
	public static final Rect g = new Rect(2, 10, 96, 80);

	private final DialogInterface.OnClickListener aboutListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.license_url)));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(intent);
			finish();
		}
	};

	private final DialogInterface.OnClickListener moreListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mobi_url)));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(intent);
			finish();
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);

		int res = -1;

		// register your copy of library with given user/password
//		res = BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_PDF, "BarcodeScanner", "0B86884C502D71C818074425486D4BC8");
//		res = BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_QR, "BarcodeScanner", "3712B4808B982B7E67DAABF31C1C0150");
		res = BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_PDF, "MindwareWorksPDFAndroid1DL", "5E3787115E3A24140369BEC984F5560C69482D0AE4D8C917DB97B308A41FD8D0");
		res = BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_QR, "MindwareWorksQRAndroidUDL", "936842DB051AE0CB41927E54C1931A3DE33FA12759F1B5437849D9373BE83B3F");
		res = BarcodeScanner.MWBregisterCode(32, "BarcodeScanner", "A366682C25DFF7394373DEC5CDF4DD31");
		res = BarcodeScanner.MWBregisterCode(8, "BarcodeScanner", "A1646A2E270BD5C406541CDAE808D227");
		res = BarcodeScanner.MWBregisterCode(2, "BarcodeScanner", "B86D6155250BFCD73E2B5A426DC5E8F9");
		res = BarcodeScanner.MWBregisterCode(16, "BarcodeScanner", "491CCA8E8D9C057C6CDBB0C9E4193F44");
		res = BarcodeScanner.MWBregisterCode(4, "BarcodeScanner", "AA7F6F2B2D3D74F3E50A413E57684CCF");

		// choose code type or types you want to search for
		// res = BarcodeScanner.MWBsetActiveCodes(BarcodeScanner.MWB_CODE_MASK_PDF | BarcodeScanner.MWB_CODE_MASK_QR );
		res = BarcodeScanner.MWBsetActiveCodes(127);

		// set scanning rect (in percentages)
		res = BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, 2, 10, 96, 80);
		res = BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR, 20, 5, 60, 90);
		BarcodeScanner.MWBsetScanningRect(4, c);
		BarcodeScanner.MWBsetScanningRect(16, d);
		BarcodeScanner.MWBsetScanningRect(32, b);
		BarcodeScanner.MWBsetScanningRect(8, a);
		BarcodeScanner.MWBsetScanningRect(2, f);

		// set decoder effort level (1 - 5)
		res = BarcodeScanner.MWBsetLevel(3);

		// check documentation for other scanning modes
		// res = BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL);
		// res = BarcodeScanner.MWBsetFlags(0, BarcodeScanner.MWB_CFG_GLOBAL_HORIZONTAL_SHARPENING | BarcodeScanner.MWB_CFG_GLOBAL_ROTATE90 );

		CameraManager.init(getApplication());
		resultView = findViewById(R.id.result_view);
		statusView = findViewById(R.id.status_view);

		handler = null;
		lastResult = null;
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			if (lastResult != null) {
				resetStatusView();
				if (handler != null) {
					handler.sendEmptyMessage(R.id.restart_preview);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		menu.add(0, ABOUT_ID, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
//		return true;
//	}

	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case ABOUT_ID:
	// try {
	// PackageInfo info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
	// // Since we're paying to talk to the PackageManager anyway, it
	// // makes sense to cache the app
	// // version name here for display in the about box later.
	// this.versionName = info.versionName;
	// } catch (PackageManager.NameNotFoundException e) {
	// }
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle(getString(R.string.title_about));
	// builder.setMessage(getString(R.string.msg_about));
	// builder.setIcon(R.drawable.launcher_icon);
	// builder.setPositiveButton(R.string.button_open_license, aboutListener);
	// builder.setNeutralButton(R.string.button_open_mobi, moreListener);
	// builder.setNegativeButton(R.string.button_cancel, null);
	// builder.show();
	// break;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */

	public void handleDecode(byte[] rawResult) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		statusView.setVisibility(View.GONE);
		resultView.setVisibility(View.VISIBLE);

		TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
		formatTextView.setVisibility(View.VISIBLE);
		formatTextView.setText(getString(R.string.msg_default_format));

		TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
		String s = "";
		for (int i = 0; i < rawResult.length; i++)
			s = s + (char) rawResult[i];
		contentsTextView.setText(s);

		int bcType = BarcodeScanner.MWBgetLastType();
		String typeName = "";
		switch (bcType) {
			case BarcodeScanner.FOUND_128:
				typeName = "Code 128";
				break;
			case BarcodeScanner.FOUND_39:
				typeName = "Code 39";
				break;
			case BarcodeScanner.FOUND_DM:
				typeName = "Datamatrix";
				break;
			case BarcodeScanner.FOUND_EAN_13:
				typeName = "EAN 13";
				break;
			case BarcodeScanner.FOUND_EAN_8:
				typeName = "EAN 8";
				break;
			case BarcodeScanner.FOUND_NONE:
				typeName = "None";
				break;
			case BarcodeScanner.FOUND_RSS_14:
				typeName = "Databar 14";
				break;
			case BarcodeScanner.FOUND_RSS_14_STACK:
				typeName = "Databar 14 Stacked";
				break;
			case BarcodeScanner.FOUND_RSS_EXP:
				typeName = "Databar Expanded";
				break;
			case BarcodeScanner.FOUND_RSS_LIM:
				typeName = "Databar Limited";
				break;
			case BarcodeScanner.FOUND_UPC_A:
				typeName = "UPC A";
				break;
			case BarcodeScanner.FOUND_UPC_E:
				typeName = "UPC E";
				break;
			case BarcodeScanner.FOUND_PDF:
				typeName = "PDF417";
				break;
		}
		if (bcType >= 0) formatTextView.setText("Format: " + typeName);

		if (vibrate) {
			doVibrate();
		}
		// if (copyToClipboard) {
		// ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		// clipboard.setText(s);
		// }

		Intent data = new Intent();
		data.putExtra("BARCODE", s);
		this.setResult(RESULT_OK, data);
		finish();
	}

	private void doVibrate() {
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(VIBRATE_DURATION);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit();
			return;
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			displayFrameworkBugMessageAndExit();
			return;
		}
		if (handler == null) {
			handler = new ActivityCaptureHandler(this);
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				finish();
			}
		});
		builder.show();
	}

	private void resetStatusView() {
		resultView.setVisibility(View.GONE);
		statusView.setVisibility(View.VISIBLE);
		statusView.setBackgroundColor(getResources().getColor(R.color.status_view));

		TextView textView = (TextView) findViewById(R.id.status_text_view);
		textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		textView.setTextSize(14.0f);
		textView.setText(R.string.msg_default_status);
		lastResult = null;
	}

}
