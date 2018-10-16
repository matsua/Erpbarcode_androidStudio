package com.ktds.erpbarcode.env;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.CertificationActivity;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.MainActivity;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SystemInfo;
import com.ktds.erpbarcode.UpgradeActivity;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.env.model.NoticeInfo;

public class NoticeActivity extends Activity {
	
	private static final String TAG = "NoticeActivity";
	
	//---------------------------------------------------------------
	// Input Parameter
	//---------------------------------------------------------------
	private SettingPreferences mSharedSetting;
	
	private NoticeInTask mNoticeInTask;
	private ListView mNoticeListView;
	private NoticeListAdapter mNoticeListAdapter;
	
	private CheckBox mNotOpenCheck;
	private Button mCloseButton;
	
	//private TextView mTotalCount;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
		//---------------------------------------------------------------
    	// input parameter
    	//---------------------------------------------------------------
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setJobGubun("공지사항");
		//GlobalData.getInstance().setNowOpenActivity(this);
		
		mSharedSetting = new SettingPreferences(getApplicationContext());
		
		setMenuLayout();
		setContentView(R.layout.env_notice_activity);
		
		setLayout();
		
		initScreen();
	}
	
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GlobalData.getInstance().getJobGubun());
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
		mNoticeListAdapter = new NoticeListAdapter(this);
		
        mNoticeListView = (ListView) findViewById(R.id.notice_listView);
        mNoticeListView.setAdapter(mNoticeListAdapter);
        mNoticeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
			}
		});
		
    	mNotOpenCheck = (CheckBox) findViewById(R.id.notice_notOpenCheckBox);
    	mNotOpenCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        @Override
	        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
	        	if (isChecked) {
	        		String nowDate = SystemInfo.getNowDate();
	        		mSharedSetting.setNoticeNowNotOpen(nowDate, true);
	        	} else {
	        		mSharedSetting.removeNoticeNowNotOpen();
	        	}
	        }
	    });
    	mCloseButton  = (Button) findViewById(R.id.notice_close_button);
    	mCloseButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent();
						setResult(Activity.RESULT_OK, intent);
						finish();
					}
				});
	}
	
    /**
     *  Progress를 관리한다.
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
		} else {
        	return super.onOptionsItemSelected(item);
        }
	    return false;
    }

    @Override
	public void onBackPressed() {
    	Intent intent = new Intent();
		setResult(Activity.RESULT_OK, intent);
		finish();
    	
		super.onBackPressed();
	}

	private void initScreen() {
    	getNoticeInfos();
    }

	/**
	 * 공지사항 목록 조회.
	 */
	private void getNoticeInfos() {
		if (isBarcodeProgressVisibility()) return;
		
		mNoticeListAdapter.itemClear();
		
		if (mNoticeInTask == null) {
			setBarcodeProgressVisibility(true);
			mNoticeInTask = new NoticeInTask();
			mNoticeInTask.execute((Void) null);
		}
	}
	
	public class NoticeInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private List<NoticeInfo> _NoticeInfos;

		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_NoticeInfos = basehttp.getNoticeInfos();
				if (_NoticeInfos == null) {
					throw new ErpBarcodeException(-1, "공지사항 조회 정보가 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mNoticeInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				mNoticeListAdapter.addItems(_NoticeInfos);
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mNoticeInTask = null;
			setBarcodeProgressVisibility(false);
		}
	}
	
}
