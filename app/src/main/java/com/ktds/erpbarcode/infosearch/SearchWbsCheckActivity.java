package com.ktds.erpbarcode.infosearch;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.WBSBarcodeService;
import com.ktds.erpbarcode.barcode.model.WBSConvert;
import com.ktds.erpbarcode.barcode.model.WBSInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;


public class SearchWbsCheckActivity extends Activity {

	private static final String TAG = "SearchWbsCheckActivity";
	//---------------------------------------------------------------
	// Input Parameter
	//---------------------------------------------------------------
	public static final String INPUT_LOC_CD = "locCd";
	public static final String INPUT_WORK_CAT = "workCat";
	//---------------------------------------------------------------
	// Output Parameter
	//---------------------------------------------------------------
	public static final String OUTPUT_WBS_NO = "wbsNo";
	
	private String p_locCd = "";
	private String p_workCat = "";
	
	private ListView mWbsListView;
	private WbsListAdapter mWbsListAdapter;

	private TextView mJpjtType_header;
	private TextView mWbsStatusName_header;
	private TextView mKostlStatus_header;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//---------------------------------------------------------------
    	// input parameter
    	//---------------------------------------------------------------
		p_locCd = getIntent().getStringExtra(INPUT_LOC_CD);
		p_workCat = getIntent().getStringExtra(INPUT_WORK_CAT);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		setContentView(R.layout.infosearch_searchwbscheck_activity);
		setMenuLayout();
		setLayout();
		initScreen();
	}


	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("WBS 선택");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}
	
	private void setLayout() {
		Log.d(TAG, "setLayout  Next  Start...");
        
        mWbsListAdapter = new WbsListAdapter(getApplicationContext());
       	mWbsListView = (ListView) findViewById(R.id.searchwbscheck_listView);
        
        mWbsListView.setAdapter(mWbsListAdapter);
        mWbsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick   position==>" + position);
				mWbsListAdapter.changeSelectPosition(position);
			}
		});

        // WBS 선택 버튼
		Button mChoiceButton = (Button) findViewById(R.id.searchwbscheck_choice_button);        
		mChoiceButton.setOnClickListener(new OnClickListener() {            
			public void onClick(View v) {
				choiceWbs();
		    }        
		});        

		 // 취소 버튼
		Button mCancelButton = (Button) findViewById(R.id.searchwbscheck_cancel_button);        
		mCancelButton.setOnClickListener(new OnClickListener() {            
			public void onClick(View v) {
				cancelWbs();
			}        
		});
	}
	
	private void initScreen() {
		Log.d(TAG, "initScreen  Next  Start...");

		// 동적으로 인계/인수/시설등록과 철거 wbs 리스트 나누기
		mJpjtType_header = (TextView) findViewById(R.id.selectwbs_header_jpjtType);
		mWbsStatusName_header = (TextView) findViewById(R.id.selectwbs_header_wbsStatusName);
		mKostlStatus_header = (TextView) findViewById(R.id.selectwbs_header_kostlStatus);
		
    	if (GlobalData.getInstance().getJobGubun().equals("철거") || GlobalData.getInstance().getJobGubun().equals("다중철거"))
        {
    		mJpjtType_header.setVisibility(View.GONE);
    		mWbsStatusName_header.setVisibility(View.GONE);
    		mKostlStatus_header.setVisibility(View.GONE);
        }
		
    	getWBSInfos(p_locCd, p_workCat);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
		} else {
        	return true;
        }
	    return false;
    }
	
    @Override
	protected void onRestart() {
		super.onRestart();
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
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
	
    private void choiceWbs() {
    	int selectPosition = mWbsListAdapter.getSelectPosition();
    	if (selectPosition < 0) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "항목을 선택하세요."));
			return;
    	}
    	
    	WBSInfo wbsInfo = mWbsListAdapter.getItem(selectPosition);
    	if (wbsInfo.getKostlStatus().startsWith("X")) {
    		GlobalData.getInstance().showMessageDialog(
    				new ErpBarcodeException(-1, "WBS번호 '" + wbsInfo.getWbsNo() + "'의\n\r코스트센터 상태가 '폐지' 이므로\n\r선택하실 수 없습니다.\n\r공사 담당자에게 문의하시기 바랍니다.") );
    		return;
    	}
    	
    	Intent intent = new Intent();
        intent.putExtra(OUTPUT_WBS_NO, wbsInfo.getWbsNo());
		setResult(Activity.RESULT_OK, intent);   
		finish();
    }
    
    private void cancelWbs() {
    	finish();
    }
    
    
    /**
	 * WBS정보 조회.
	 */
    private void getWBSInfos(String locCd, String workCat) {
    	
		if (isBarcodeProgressVisibility()) return;
    	setBarcodeProgressVisibility(true);
    	
    	mWbsListAdapter.itemClear();
    	
    	WBSBarcodeService wbsService = new WBSBarcodeService(new WBSInfoHandler());
    	wbsService.search(locCd, workCat);
	}
	
	/**
	 * WBSInfoHandler 정보 조회 결과 Handler
	 */
    private class WBSInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case WBSBarcodeService.STATE_SUCCESS :
            	String findedMessage = msg.getData().getString("message");
            	List<WBSInfo> wbsInfos = WBSConvert.jsonArrayStringToWBSInfos(findedMessage);

        		
                mWbsListAdapter.addItems(wbsInfos);
                mWbsListAdapter.changeSelectPosition(0);
        		
                break;
            case WBSBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "유효하지 않은 WBS번호 입니다. "));
            	break;
            case WBSBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    }

}
