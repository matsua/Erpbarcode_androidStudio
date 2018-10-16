package com.ktds.erpbarcode.job;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.database.WorkInfo;
import com.ktds.erpbarcode.common.database.WorkInfoQuery;
import com.ktds.erpbarcode.common.database.WorkItemQuery;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.management.RepairActivity;
import com.ktds.erpbarcode.management.TransferActivity;
import com.ktds.erpbarcode.management.TreeScanActivity;
import com.ktds.erpbarcode.survey.SpotCheckActivity;
import com.ktds.erpbarcode.survey.TerminalCheckActivity;

public class JobMenageActivity extends Activity {

	private static final String TAG = "JobMenageActivity";

	private WorkInfoQuery mWorkInfoQuery;
	private WorkItemQuery mWorkItemQuery;
	
	private Spinner mJobGubunSpinner;
	private BasicSpinnerAdapter mJobGubunSpinnerAdapter;
	private Spinner mSendGubunSpinner;
	private BasicSpinnerAdapter mSendGubunSpinnerAdapter;
	private Spinner mJobDateSpinner;
	private BasicSpinnerAdapter mJobDateSpinnerAdapter;
	
	private Button mDeleteButton, mConfirmButton;
	
	private CheckBox mCheckButton;

	private ListView mListView;
	private JobListAdapter mJobListAdapter;
	
	private TextView mTotalCountText;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
		
		mWorkInfoQuery = new WorkInfoQuery(getApplicationContext());
		mWorkInfoQuery.open();
		mWorkItemQuery = new WorkItemQuery(getApplicationContext());
		mWorkItemQuery.open();
		setMenuLayout();
		setContentView(R.layout.job_jobmanage_activity);
		setLayout();
		
		initScreen();
	}

	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("작업관리");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

	private void setLayout() {
		mJobGubunSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mJobGubunSpinner = (Spinner) findViewById(R.id.jobmanage_jobgubun);
		mJobGubunSpinner.setAdapter(mJobGubunSpinnerAdapter);
		mJobGubunSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mJobGubunSpinnerAdapter.getItem(position);
				if (spinnerInfo == null) return;
				//if (spinnerInfo.getCode().isEmpty()) return;
				// 작업내역을 조회한다.
				getWorkInfos();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		
		mSendGubunSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mSendGubunSpinner = (Spinner) findViewById(R.id.jobmanage_sendgubun);
		mSendGubunSpinner.setAdapter(mSendGubunSpinnerAdapter);
		mSendGubunSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mSendGubunSpinnerAdapter.getItem(position);
				if (spinnerInfo == null) return;
				//if (spinnerInfo.getCode().isEmpty()) return;
				// 작업내역을 조회한다.
				getWorkInfos();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		
		mJobDateSpinnerAdapter = new BasicSpinnerAdapter(getApplicationContext());
		mJobDateSpinner = (Spinner) findViewById(R.id.jobmanage_jobdate);
		mJobDateSpinner.setAdapter(mJobDateSpinnerAdapter);
		mJobDateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerInfo spinnerInfo = (SpinnerInfo) mJobDateSpinnerAdapter.getItem(position);
				if (spinnerInfo == null) return;
				//if (spinnerInfo.getCode().isEmpty()) return;
				// 작업내역을 조회한다.
				getWorkInfos();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		mCheckButton = (CheckBox) findViewById(R.id.workList_isChecked);
		mCheckButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						setCheckItem();
					}
				});
		
		mDeleteButton = (Button) findViewById(R.id.jobmanage_crud_delete);
		mDeleteButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						deleteCheckItems();
					}
				});
		mConfirmButton = (Button) findViewById(R.id.jobmanage_crud_confirm);
		mConfirmButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
//						executeWorkItems();
						selectWorkItems();
					}
				});
		
		
		mJobListAdapter = new JobListAdapter(getApplicationContext());
		mListView = (ListView) findViewById(R.id.jobmanage_listView);
		mListView.setAdapter(mJobListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				mJobListAdapter.changeSelectPosition(position);
				//WorkInfo model = mJobListAdapter.getItem(position);
			}
		});
		
		mTotalCountText = (TextView) findViewById(R.id.jobmanage_listfooter_totalCount);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else {
			return super.onOptionsItemSelected(item);
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
		
		// 작업내역을 조회한다.
		getWorkInfos();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void initScreen() {
		// 작업 Spinner데이터를 불러온다.
		initScreen("spinner");
		
		// 작업내역을 조회한다.
		getWorkInfos();
	}
	
	private void initScreen(String step) {
		if (step.equals("all") || step.equals("spinner")) {
			// 작업구분 Spinner
			List<SpinnerInfo> jobgubunItems = mWorkInfoQuery.getWorkJobGubuns();
			mJobGubunSpinnerAdapter.addItems(jobgubunItems);
			mJobGubunSpinner.setSelection(0);
			
			// 전송구분 Spinner
			List<SpinnerInfo> sendgubunItems = mWorkInfoQuery.getWorkSendGubuns();
			mSendGubunSpinnerAdapter.addItems(sendgubunItems);
			mSendGubunSpinner.setSelection(0);
			
			// 작업일자 Spinner
			List<SpinnerInfo> jobdateItems = mWorkInfoQuery.getWorkJobDates();
			mJobDateSpinnerAdapter.addItems(jobdateItems);
			mJobDateSpinner.setSelection(0);
		}
		if (step.equals("all") || step.equals("tree")) {
			mJobListAdapter.itemClear();
			mTotalCountText.setText("");
		}
	}

	/**
	 * 저장된 작업내용 목록을 조회한다.
	 */
	private void getWorkInfos() {
		
		// Tree내용을 Clear한다.
		initScreen("tree");
		
		SpinnerInfo jobgubunSpinnerInfo = (SpinnerInfo) mJobGubunSpinnerAdapter.getItem(mJobGubunSpinner.getSelectedItemPosition());
		SpinnerInfo sendgubunSpinnerInfo = (SpinnerInfo) mSendGubunSpinnerAdapter.getItem(mSendGubunSpinner.getSelectedItemPosition());
		SpinnerInfo jobdateSpinnerInfo = (SpinnerInfo) mJobDateSpinnerAdapter.getItem(mJobDateSpinner.getSelectedItemPosition());
		
		String jobGubun = jobgubunSpinnerInfo.getCode();
		String sendGubun = sendgubunSpinnerInfo.getCode();
		String jobDate = jobdateSpinnerInfo.getCode();
		
		if (jobGubun == null) jobGubun = "";
		if (sendGubun == null) sendGubun = "";
		if (jobDate == null) jobDate = "";
		
		Log.i(TAG, "getWorkInfos jobGubun==>" + jobGubun);
		Log.i(TAG, "getWorkInfos sendGubun==>" + sendGubun);
		Log.i(TAG, "getWorkInfos jobDate==>" + jobDate);
		
		List<WorkInfo> workItems = getWorkInfos(jobGubun, sendGubun, jobDate);
		if (workItems != null && workItems.size() > 0) {
			mJobListAdapter.addItems(workItems);
		}
	}
	
	/**
	 * SqlLite DB에서 작업내용을 조회한다.
	 * 
	 * @param jobGubun   작업구분
	 * @param sendGubun  전송구분
	 * @param jobDate    작업일자
	 */
	private List<WorkInfo> getWorkInfos(String jobGubun, String sendGubun, String jobDate) {
		return mWorkInfoQuery.getWorkInfos(jobGubun, sendGubun, jobDate);
	}
	
	/**
	 * 체크박스 전체 선택 해제 
	 */
	private void setCheckItem(){
		System.out.println("setCheckItem :: " + mCheckButton.isChecked());
		mJobListAdapter.setCheckedItemsAll(mCheckButton.isChecked());
	}
	
	/**
	 * 체크된 작업들을 삭제한다.
	 */
    private void deleteCheckItems() {

    	if (mJobListAdapter.getCheckedItems().size() == 0) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택된 항목이 없습니다. "));
			return;
		}
    	
    	//-----------------------------------------------------------
    	// 최종 삭제 Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
    	GlobalData.getInstance().setGlobalAlertDialog(true);
    	
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
    	String message = "삭제하시겠습니까?";
		
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
		builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	for (WorkInfo workInfo : mJobListAdapter.getCheckedItems()) {
            		mWorkInfoQuery.deleteWorkInfo(workInfo.getId());
            	}
            	
            	mJobListAdapter.removeCheckedItems();
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
    
    /**
     * 선택한 작업 해당 Activity로 이동해서 실행한다.
     */ 
    
    private void selectWorkItems(){
    	if (mJobListAdapter.getCount() == 0) {
    		return;
    	}
    	
    	WorkInfo model = null;
    	
    	if (mJobListAdapter.getSelectPosition() < 0) {
    		int selectCheckIndex = 0;
    		for(WorkInfo workInfo : mJobListAdapter.getCheckedItems()){
    			selectCheckIndex++;
    			model = workInfo;
    			if(selectCheckIndex > 1){
    				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "작업내역은 한개의 작업만 선택 가능 합니다. "));
    	    		return;
    			}
    		}
    		
    		if(selectCheckIndex == 0){
    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "선택한 자료가 없습니다. "));
    			return;
    		}
    		
    	}else{
    		model = mJobListAdapter.getItem(mJobListAdapter.getSelectPosition());
    	}
    	
    	executeWorkItems(model);
    }
    
    private void executeWorkItems(WorkInfo model) {
    	//if (model.getTranYn().equals("Y")) {
    	//	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "이미 전송된 작업입니다. "));
    	//	return;
    	//}
    	
    	GlobalData.getInstance().setJobGubun(model.getWorkName());
		GlobalData.getInstance().setWorkId(model.getId());
		
    	if (model.getWorkName().equals("납품입고") || model.getWorkName().equals("납품취소")
    			|| model.getWorkName().equals("배송출고")
    			|| model.getWorkName().equals("입고(팀내)") || model.getWorkName().equals("출고(팀내)")
    			|| model.getWorkName().equals("실장") || model.getWorkName().equals("탈장")
    			|| model.getWorkName().equals("송부(팀간)") || model.getWorkName().equals("송부취소(팀간)")
    			|| model.getWorkName().equals("접수(팀간)")
    			|| model.getWorkName().equals("철거") || model.getWorkName().equals("설비상태변경")
    			|| model.getWorkName().equals("고장등록") || model.getWorkName().equals("형상구성(창고내)") || model.getWorkName().equals("형상해제(창고내)")) {
    		Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
            startActivity(intent);
            finish();
        
    	} else if (model.getWorkName().equals("인계") || model.getWorkName().equals("인수")
    			|| model.getWorkName().equals("시설등록")) {
    		Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
            startActivity(intent);
            finish();
            
    	} else if (model.getWorkName().equals("고장등록취소") || model.getWorkName().equals("수리의뢰취소")
    			|| model.getWorkName().equals("수리완료") || model.getWorkName().equals("개조개량의뢰")
    			|| model.getWorkName().equals("개조개량의뢰취소") || model.getWorkName().equals("개조개량완료") || model.getWorkName().equals("설비S/N변경")) {
    		Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
            startActivity(intent);
            finish();
    			
    	} else if (model.getWorkName().startsWith("현장점검")) {
    		Intent intent = new Intent(getApplicationContext(), SpotCheckActivity.class);
            startActivity(intent);
            finish();
    	}
    	else if (model.getWorkName().startsWith("임대단말실사")) {
    		Intent intent = new Intent(getApplicationContext(), TerminalCheckActivity.class);
            startActivity(intent);
            finish();
    	}
		
    }

}
