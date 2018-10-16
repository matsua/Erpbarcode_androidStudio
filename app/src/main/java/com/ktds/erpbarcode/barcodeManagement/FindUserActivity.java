package com.ktds.erpbarcode.barcodeManagement;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.infosearch.UserInfoListAdapter;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;

public class FindUserActivity extends Activity {
	private static final String TAG = "FindUserActivity";
	
	private SearchUserIdInTask mSearchUserIdInTask;
	private EditText mUserId;
	private EditText mUserNm;
	
	private UserInfoListAdapter mUserInfoListAdapter;
	private ListView mUserListView;
	
	public static final String OUTPUT_SEL_USER_ID = "selUserId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.find_user_activity);
        
        Button close = (Button)findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				finish();
		     }
	     });
        
        Button select = (Button)findViewById(R.id.selectBtn);
        select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				if(mUserInfoListAdapter.getCheckedItems().size() == 0){
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 사용자 정보가 없습니다."));
					return;
				}

				final List<OrgCodeInfo> userInfo = mUserInfoListAdapter.getCheckedItems();
				String userId = userInfo.get(0).getUserId();
				
				Intent intent = new Intent();
		        intent.putExtra(OUTPUT_SEL_USER_ID, userId);
				setResult(Activity.RESULT_OK, intent);
				finish();
		     }
	     });
        
        Button search = (Button)findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				getUserData();
		     }
	     });
        
        mUserId = (EditText) findViewById(R.id.barcode_userId);
        mUserNm = (EditText)findViewById(R.id.barcode_userNm);
        
        mUserInfoListAdapter = new UserInfoListAdapter(this);
        mUserListView = (ListView) findViewById(R.id.listView);
        mUserListView.setAdapter(mUserInfoListAdapter);
	}
        
	/**
	 * 검색 조회 
	 */
	public void getUserData() {
		if (mSearchUserIdInTask == null) {
			mSearchUserIdInTask = new SearchUserIdInTask(mUserId.getText().toString(), mUserNm.getText().toString());
			mSearchUserIdInTask.execute((Void) null);
		}
	}
	
	public class SearchUserIdInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private List<OrgCodeInfo> _OrgInfos;
		String _suerId, _userName;
		
		public SearchUserIdInTask(String suerId, String userName) {
			_suerId = suerId;
			_userName = userName;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_OrgInfos = basehttp.getUserInfo(_suerId,_userName);
				
				if (_OrgInfos == null) {
					throw new ErpBarcodeException(-1,"조회된 결과값이 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "조회된 결과값이 없습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mSearchUserIdInTask = null;
			if (result) {
				if (_OrgInfos.size() == 0) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조회된 사용자 정보가 없습니다."));	
				} else {
					mUserInfoListAdapter.addItems(_OrgInfos);
				}
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mSearchUserIdInTask = null;
		}
	}
	
	
}
