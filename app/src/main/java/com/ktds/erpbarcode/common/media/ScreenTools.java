package com.ktds.erpbarcode.common.media;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

public class ScreenTools {
	
	private static final String TAG = "ScreenTools";
	
	private Activity mActivity;
	
	// The gesture threshold expressed in dp
	private static final float GESTURE_THRESHOLD_DP = 16.0f;

	// Get the screen's density scale
	final float mScale;
	
	// Convert the dps to pixels, based on density scale
	int mGestureThreshold = 0;

	public ScreenTools(Activity activity) {
		this.mActivity = activity;
		mScale = mActivity.getResources().getDisplayMetrics().density;
		mGestureThreshold = (int) (GESTURE_THRESHOLD_DP * mScale + 0.5f);
		Log.i(TAG, "Scale ==>"+mScale);
		Log.i(TAG, "GestureThreshold ==>"+mGestureThreshold);
	}
	
	public int getScreenWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		return metrics.widthPixels;
	}
	
	public int getScreenHeight() {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		return metrics.heightPixels;
	}
	
	public int toDips(int pixel) {
		float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, mActivity.getResources().getDisplayMetrics());
		
		return Math.round(dip);
	}
	  
	public int toPixels(int dip) {
		float pixel = dip * mActivity.getResources().getDisplayMetrics().density;

		return Math.round(pixel);
	}
	
	//public int toFontSize(int resId) {
	//	float fontSize = mActivity.getResources().getDimension(resId);
	//	return fontSize;
	//}

}
