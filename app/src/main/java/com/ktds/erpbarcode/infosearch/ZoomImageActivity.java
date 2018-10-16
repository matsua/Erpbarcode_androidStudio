package com.ktds.erpbarcode.infosearch;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.widget.ZoomImageView;

public class ZoomImageActivity extends Activity {

	//private static final String TAG = "ZoomImageActivity";
	public static final String INPUT_IMAGE_PATH = "image_path";
	
	private String p_imagePath = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//-----------------------------------------------------------
    	// input parameter
    	//-----------------------------------------------------------
		p_imagePath = getIntent().getStringExtra(INPUT_IMAGE_PATH);
		
		//-----------------------------------------------------------
	    // ActionBar를 hide처리한다.
	    //----------------------------------------------------------- 
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		//setMenuLayout();
		setContentView(R.layout.infosearch_zoomimage_activity);
		initScreen();
	}
	
	public void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun() + " [" + SessionUserData.getInstance().getAccessServerName() + " V" + GlobalData.getInstance().getAppVersionName()+"]");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

	private void initScreen() {
		ZoomImageView touchImageView = (ZoomImageView) findViewById(R.id.zoomimage_touchImageView);
		
		if (!TextUtils.isEmpty(p_imagePath)) {

			BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	        Bitmap bitmap = BitmapFactory.decodeFile(p_imagePath, options);
	        
	        //int image_degrees = StorageTools.getCameraOrientation(p_imagePath);
			//bitmap = StorageTools.getRotatedBitmap(bitmap, image_degrees);
			
	        touchImageView.setImageBitmap(bitmap);
			//bitmap.recycle();

			/*
			ImageLoader imageLoader = ErpVolley.getImageLoader();
            imageLoader.get(p_imagePath, 
                           ImageLoader.getImageListener(zoomImageView, 
                                                         R.drawable.common_button_bg_gray, 
                                                         R.drawable.common_button_bg_red));
            */
		}
	}
}
