package com.ktds.erpbarcode.management;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.ism.model.IsmHttpController;

public class DeleteWhyInfoActivity extends Activity {
	private static final String TAG = "DeleteWhyInfoActivity";
	private IsmPrintSpinnerInTask mIsmPrintSpinnerInTask;
	private JSONArray mpdArray = new JSONArray();
	
	private Spinner mSpinnerCode;
	private String mTouchCode = null;
	private BasicSpinnerAdapter mCodeAdapter;
	private EditText mDetailText;
	private EditText mErrorText;
	
	public static final String OUTPUT_SEL_CODE = "selCode";
	public static final String OUTPUT_SEL_CODE_DETAIL = "selCodeDetail";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        
        setContentView(R.layout.deletewhyinfo_activity);

        getIsmPrintSpinnerData();
	}
        
	private void setLayout() {
		Button close = (Button)findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				finish();
		     }
	     });
        
        Button select = (Button)findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				if(mTouchCode.equals("ETC")){
					if(mDetailText.getText().toString().length() < 1){
						mErrorText.setText("상세사항을 입력하세요.");
						return;
					}
				}
				
				Intent intent = new Intent();
		        intent.putExtra(OUTPUT_SEL_CODE, mTouchCode);
		        intent.putExtra(OUTPUT_SEL_CODE_DETAIL, mDetailText.getText().toString());
				setResult(Activity.RESULT_OK, intent);
				finish();
		     }
	     });
        
        mSpinnerCode = (Spinner) findViewById(R.id.spinner1);
        mDetailText = (EditText) findViewById(R.id.editText2);
        mErrorText = (EditText)findViewById(R.id.errorTxt);
        
		List<SpinnerInfo> sspinneritems = new ArrayList<SpinnerInfo>();
		sspinneritems.add(new SpinnerInfo("", "선택하세요."));
		for(int s = 0; s < mpdArray.length(); s++){
			try {
				JSONObject mpObj = mpdArray.getJSONObject(s);
				sspinneritems.add(new SpinnerInfo(mpObj.optString("code"), mpObj.optString("name")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mCodeAdapter = new BasicSpinnerAdapter(getApplicationContext(), sspinneritems);
		mSpinnerCode.setAdapter(mCodeAdapter);
		mSpinnerCode.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	mErrorText.setText("");
	        	SpinnerInfo sCodeSpinnerInfo = (SpinnerInfo) mCodeAdapter.getItem(position);
	        	mSpinnerCode.setSelection(mCodeAdapter.getPosition(sCodeSpinnerInfo.getCode()));
	        	mTouchCode = sCodeSpinnerInfo.getCode();
	        }
	        public void onNothingSelected(AdapterView<?> arg0) {
	        	
	        }
	    });
		
		mDetailText.setOnTouchListener(
				new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	mErrorText.setText("");
			            return false;
			        }
			    });
	}
	
	
	/**
	 * spinner data 조회 
	 */
	public void getIsmPrintSpinnerData() {
		if (mIsmPrintSpinnerInTask == null) {
			mIsmPrintSpinnerInTask = new IsmPrintSpinnerInTask();
			mIsmPrintSpinnerInTask.execute((Void) null);
		}
	}
	
	public class IsmPrintSpinnerInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private JSONArray _jsonResults = new JSONArray();
		
		public IsmPrintSpinnerInTask() {
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				IsmHttpController ismhttp = new IsmHttpController();
				_jsonResults = ismhttp.getIsmPrintSpinner(3);
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "화면구성값 체크 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				_jsonResults = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mIsmPrintSpinnerInTask = null;
			JSONArray jsonArray_temp = new JSONArray();
			
			super.onPostExecute(result);
			if (result) {
				try {
					for(int i = 0; i < _jsonResults.length(); i++){
						JSONObject jsonobj = _jsonResults.getJSONObject(i);
						JSONObject jsonObject_temp = new JSONObject();
						jsonObject_temp.put("code", jsonobj.getString("commonCode"));
						jsonObject_temp.put("name", jsonobj.getString("commonCodeName"));
						jsonArray_temp.put(jsonObject_temp);
					}
					
					mpdArray = jsonArray_temp;
					setLayout();
				} catch (JSONException e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "화면구성값 대입중 오류가 발생했습니다." + e));
					return;
				} catch (Exception e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "화면구성값 대입중 오류가 발생했습니다." + e));
					return;
				}

			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mIsmPrintSpinnerInTask = null;
			super.onCancelled();
		}
	}
}
