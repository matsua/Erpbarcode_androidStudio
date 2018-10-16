package com.ktds.erpbarcode.infosearch;

import java.lang.reflect.Type;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.BaseHttpController;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.treeview.InMemoryTreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.env.bluetooth.ScannerDeviceData;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;

public class SelectOrgCodeActivity extends Activity {

	private static final String TAG = "SelectOrgCodeActivity";
	
	public static final String INPUT_JSON_STRING = "jsonstring";
	public static final String OUTPUT_ORG_CODE = "orgCode";
	public static final String OUTPUT_ORG_NAME = "orgName";
	public static final String OUTPUT_ORG_ORGCD = "orgCd";
	
	private String p_jsonString;

	private ScannerConnectHelper mScannerHelper;
	
	private LinearLayout mOrgInputbar;
	private EditText mOrgName;
	
	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mOrgTreeView;
	private OrgCodeTreeAdapter mOrgCodeTreeAdapter;
	private FindOrgCodeInTask mFindOrgCodeInTask;
	private FindOrgNameInTask mFindOrgNameInTask;
	
	private TextView mTotalCount;
	
	private Boolean selectable = false;
	
	// Progress
	private RelativeLayout mBarcodeProgress;

	@Override    
	protected void onCreate(Bundle savedInstanceState) {
		
		/**********************************************************************
    	 * input parameter
    	 **********************************************************************/
    	p_jsonString = getIntent().getStringExtra(INPUT_JSON_STRING);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
    	super.onCreate(savedInstanceState);
    	
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
    	
    	initBarcodeScanner();
    	setContentView(R.layout.infosearch_selectorgcode_activity);
    	setMenuLayout();
    	setTreeLayout(savedInstanceState);
    	setLayout();
    	
    	initScreen();
	}

	/****************************************************************
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();
 		
 		if (ScannerDeviceData.getInstance().isConnected()) {
 			if ((mScannerHelper.getState() == BluetoothService.STATE_CONNECTING) ||
 				(mScannerHelper.getState() == BluetoothService.STATE_CONNECTED)) {
 				// 바코드 스케너가 연결된 상태이면...
 			} else {
 				boolean isInitBluetooth = mScannerHelper.initBluetooth(getApplicationContext());
 				if (isInitBluetooth) mScannerHelper.deviceConnect();
 			}
 		}
    }
	
	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("조직 선택");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
	}

	private void setLayout() {
		mOrgInputbar = (LinearLayout) findViewById(R.id.selectorgcode_organization_inputbar);
        mOrgName = (EditText) findViewById(R.id.selectorgcode_orgName);
        mOrgName.setTag(ScannerConnectHelper.SOFT_KEY_BOARD_OPEN);  // 무조건 소프트키보드를 활성화 한다.
        mOrgName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	getOrgNames();
                    return false;
                }
                return false;
            }
        });

		// 조직코드 검색        
		Button mSearchButton = (Button) findViewById(R.id.selectorgcode_search_button);  
		mSearchButton.setOnClickListener(new OnClickListener() {            
			public void onClick(View v) {
				getOrgNames();
			}        
        });

        // 조직코드 선택
		Button mChoiceButton = (Button) findViewById(R.id.selectorgcode_choice_button);        
        mChoiceButton.setOnClickListener(new OnClickListener() {            
			public void onClick(View v) {
				choiceOrgCode();
           }        
        });        

        // 조직코드 선택 취소
		Button mCancelButton = (Button) findViewById(R.id.selectorgcode_cancel_button);        
		mCancelButton.setOnClickListener(new OnClickListener() {            
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED, null);
				finish();
			}        
        });

	}
	
	@SuppressWarnings("unchecked")
	private void setTreeLayout(Bundle savedInstanceState) {
		
		if (savedInstanceState == null) {
			mTreeManager = new InMemoryTreeStateManager<Long>();
        } else {
        	mTreeManager = (TreeStateManager<Long>) savedInstanceState.getSerializable("treeManager");
            if (mTreeManager == null) {
            	mTreeManager = new InMemoryTreeStateManager<Long>();
            }
            //newCollapsible = savedInstanceState.getBoolean("collapsible");
        }
		
		mOrgCodeTreeAdapter = new OrgCodeTreeAdapter(getApplicationContext(), mTreeManager);
		mOrgTreeView = (TreeViewList) findViewById(R.id.selectorgcode_treeView);
		mOrgTreeView.setAdapter(mOrgCodeTreeAdapter);
		mOrgTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "mBarcodeTreeView onItemClick   position, id==>" + position + "," + id);

				mOrgCodeTreeAdapter.changeSelected(id);
				OrgCodeInfo model = mOrgCodeTreeAdapter.getOrgCodeInfo(id);
				if (model == null) return;
				
				// 서버에서 조회했으면 다시 조회하지 않는다.
				if (model.isSearched() && !selectable) return;
				// Child Tree를 추가한다.
				getOrgCodes("subtree", id, model.getOrgCode());
			}
		});
		
		mTotalCount = (TextView) findViewById(R.id.selectorgcode_bottom_count);
		
		// Progree
		mBarcodeProgress = (RelativeLayout) findViewById(R.id.selectorgcode_barcodeProgress);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			InputMethodManager imm = ( InputMethodManager ) mOrgName.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
			imm.hideSoftInputFromWindow(mOrgName.getApplicationWindowToken() , 0);
			finish();
		} else {
        	return true;
        }
	    return false;
    }
	
    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		InputMethodManager imm = ( InputMethodManager ) mOrgName.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
		imm.hideSoftInputFromWindow(mOrgName.getApplicationWindowToken() , 0);
		super.onBackPressed();
	}

    @Override
	protected void onRestart() {
		super.onRestart();
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
	}

    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
     *  Progress를 관리한다.
     */
    public void setBarcodeProgressVisibility(boolean show) {
    	GlobalData.getInstance().setGlobalProgress(show);
    	
    	setProgressBarIndeterminateVisibility(show);
    	mBarcodeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    public boolean isBarcodeProgressVisibility() {
    	boolean isChecked = false;
    	if (GlobalData.getInstance().isGlobalProgress()) isChecked = true;
    	if (GlobalData.getInstance().isGlobalAlertDialog()) isChecked = true;
    	
		return isChecked;
    }
    
    private void initScreen() {
    	initScreen("all");

    	if (p_jsonString==null || p_jsonString.isEmpty()) {
    		mOrgInputbar.setVisibility(View.VISIBLE);
    		getOrgCodes("tree", null, "000001");
    		
    	} else {
    		mOrgInputbar.setVisibility(View.GONE);
    		// 조회된 부서간이동정보중 송부한 운용조직 정보가 있으면..
    		appendOrgCodeInfos(p_jsonString);
    	}
    }
    
    private void initScreen(final String step) {
    	if (step.equals("all") || step.equals("base")) {
			GlobalData.getInstance().setGlobalProgress(false);
			GlobalData.getInstance().setGlobalAlertDialog(false);
		}
    	// 운용조직명 Clear
    	if (step.equals("all") || step.equals("org")) {
    		mOrgName.setText("");
    	}
    	// Tree정보 Clear
    	if (step.equals("all") || step.equals("tree")) {
    		mOrgCodeTreeAdapter.itemClear();
    		mTotalCount.setText("");
    	}
    }
    
    private void appendOrgCodeInfos(String jsonArrayString) {
	
    	Gson gson = new Gson();
		Type listType = new TypeToken<List<OrgCodeInfo>>(){}.getType();
		List<OrgCodeInfo> orgInfos = gson.fromJson(jsonArrayString, listType);
		
		if (orgInfos != null && orgInfos.size() > 0) {
			mOrgCodeTreeAdapter.addItems(orgInfos);
		}
    }
    
    private void choiceOrgCode() {
    	
    	Long selectedKey = mOrgCodeTreeAdapter.getSelected();
    	if (selectedKey == null) {
    		GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "조직을 선택하세요."));
    		return;
    	}

		TreeNodeInfo<Long> treeNode = mOrgCodeTreeAdapter.getNodeInfo(selectedKey);
		if (treeNode != null) {
			OrgCodeInfo orgInfo = mOrgCodeTreeAdapter.getOrgCodeInfo(treeNode.getId());
			if (orgInfo != null) {
				if (orgInfo.getOrgName().indexOf("TFT") > 0) {
					Intent intent = new Intent();
			        intent.putExtra(OUTPUT_ORG_CODE, orgInfo.getCostCenter());
			        intent.putExtra(OUTPUT_ORG_NAME, orgInfo.getOrgName() + "/" + orgInfo.getOrgCode());
					setResult(Activity.RESULT_OK, intent);
					finish();
					return;
				}
				
				if (treeNode.isWithChildren()) {
					boolean isSubTreeTFT = false;
					
					List<Long> subtreeKeys = mOrgCodeTreeAdapter.getSubTreeList(treeNode.getId());
					for (Long childKey : subtreeKeys) {
						OrgCodeInfo childOrgInfo = mOrgCodeTreeAdapter.getOrgCodeInfo(childKey);
						if (childOrgInfo != null) {
							if (childOrgInfo.getOrgName().indexOf("TFT") > 0 || childOrgInfo.getOrgName().indexOf("TF") > 0 || childOrgInfo.getOrgName().indexOf("CTF") > 0) {
								isSubTreeTFT = true;
								break;
							}
						}
					}
					//---------------------------------------------------------
					// 선택한 TreeNode의 하위Node중에 "TFT"이름이 있으면 무조건 선택한다. 
					//---------------------------------------------------------
					if (isSubTreeTFT) {
						Intent intent = new Intent();
				        intent.putExtra(OUTPUT_ORG_CODE, orgInfo.getCostCenter());
				        intent.putExtra(OUTPUT_ORG_NAME, orgInfo.getOrgName() + "/" + orgInfo.getOrgCode());
						setResult(Activity.RESULT_OK, intent);
						finish();
						return;
					}
				}
			}

			// 송부(팀간), 철거는 송부조직을 하위 Node가 있으면 선택할수 없다.
			if (GlobalData.getInstance().getJobGubun().equals("송부(팀간)") || GlobalData.getInstance().getJobGubun().equals("철거")) { 
				if (orgInfo.getCostCenter().equals(SessionUserData.getInstance().getOrgId())) {
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "동일한 조직으로\n\r송부하실 수 없습니다.\n\r'출고/입고' 메뉴로 처리해\n\r주시기 바랍니다."));
					return;
				}
				
				if (treeNode.isWithChildren()) {
					GlobalData.getInstance().showMessageDialog(
							new ErpBarcodeException(-1, "최하위 조직으로만\n\r송부하실 수 있습니다."));
	    			return;
				}
			}
			
			Intent intent = new Intent();
	        intent.putExtra(OUTPUT_ORG_CODE, orgInfo.getCostCenter());
	        intent.putExtra(OUTPUT_ORG_NAME, orgInfo.getOrgName() + "/" + orgInfo.getOrgCode());
	        intent.putExtra(OUTPUT_ORG_ORGCD, orgInfo.getOrgCode());
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
    }

	/****************************************************************
	 * 운용조직코드 조회하여 Tree구성.
	 */
	private void getOrgCodes(String searchType, Long parentKey, String orgCode) {
		if (orgCode.isEmpty()) return;
		if (isBarcodeProgressVisibility()) return;

		if (searchType.equals("tree")) {
			initScreen("tree");
		}
		
		if (mFindOrgCodeInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindOrgCodeInTask = new FindOrgCodeInTask(searchType, parentKey, orgCode);
			mFindOrgCodeInTask.execute((Void) null);
		}
	}
	
	public class FindOrgCodeInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _SearchType = "";
		private Long _ParentKey = null;
		private String _OrgCode = "";
		
		private List<OrgCodeInfo> _OrgInfos;
		
		public FindOrgCodeInTask(String type, Long parentKey, String orgCode) {
			_SearchType = type;
			_ParentKey = parentKey;
			_OrgCode = orgCode;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_OrgInfos = basehttp.getOrgTreeSearchToOrgCodeInfos(_OrgCode);
				
				if (_OrgInfos==null) {
					throw new ErpBarcodeException(-1,"운용조직이름으로 조회된 결과값이 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "운용조직정보 서버에서 조회중 오류가 발생했습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindOrgCodeInTask = null;
			
			if (result) {
				selectable = false;
				if (_SearchType.equals("tree")) {
					// 최상위 Node에 추가한다.
					OrgCodeInfo orgInfo = new OrgCodeInfo();
					orgInfo.setOrgCode("000001");
					orgInfo.setOrgName("주식회사 케이티");
					orgInfo.setCostCenter("C0000001");
					orgInfo.setLevel(0);
					orgInfo.setParentOrgCode("");
					orgInfo.setSearched(true);
					mOrgCodeTreeAdapter.addItem(orgInfo);
					mOrgCodeTreeAdapter.refresh();
					
					Long firstKey = mOrgCodeTreeAdapter.getItemId(0);
					mOrgCodeTreeAdapter.addChildItems(firstKey, _OrgInfos);
	        	} else {  // subTree
	        		OrgCodeInfo orgInfo = mOrgCodeTreeAdapter.getOrgCodeInfo(_ParentKey);
	        		orgInfo.setSearched(true);
	        		mOrgCodeTreeAdapter.setOrgCodeInfo(_ParentKey, orgInfo);
	        		mOrgCodeTreeAdapter.addChildItems(_ParentKey, _OrgInfos);
	        	}
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			
			showCount();
		}

		@Override
		protected void onCancelled() {
			setBarcodeProgressVisibility(false);
			mFindOrgCodeInTask = null;
			super.onCancelled();
		}
	}
	
	/****************************************************************
	 * 운용조직명으로 조회하여 Tree구성.
	 */
	private void getOrgNames() {
		
		String orgName = mOrgName.getText().toString().trim();
		
		if (orgName.isEmpty()) return;
		if (isBarcodeProgressVisibility()) return;

		initScreen("tree");
		
		if (mFindOrgNameInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindOrgNameInTask = new FindOrgNameInTask(orgName);
			mFindOrgNameInTask.execute((Void) null);
		}
	}
	public class FindOrgNameInTask extends AsyncTask<Void, Void, Boolean> {
		private ErpBarcodeException _ErpBarException;
		private String _OrgName = "";
		
		private List<OrgCodeInfo> _OrgInfos;
		
		public FindOrgNameInTask(String orgName) {
			_OrgName = orgName;
			setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BaseHttpController basehttp = new BaseHttpController();
				_OrgInfos = basehttp.getOrgNameSearchToOrgCodeInfos(_OrgName);
				
				if (_OrgInfos==null) {
					throw new ErpBarcodeException(-1,"조직명으로 조회된 결과값이 없습니다.");
				}
    		} catch (ErpBarcodeException e) {
    			Log.d(TAG, "조직명으로 조회된 결과값이 없습니다. ==>"+e.getErrMessage());
    			_ErpBarException = e;
    			return false;
    		}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindOrgNameInTask = null;
			
			if (result) {
				mOrgCodeTreeAdapter.addItems(_OrgInfos);
				selectable = true;
				
				//-----------------------------------------------------------
		    	// SoftKeyBoard 를 종료한다.
		    	//-----------------------------------------------------------
		    	InputMethodManager immOrgName = ( InputMethodManager ) mOrgName.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
		    	if (immOrgName.isActive())
		    		immOrgName.hideSoftInputFromWindow(mOrgName.getApplicationWindowToken() , 0);
			} else {
				Log.d(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			
			showCount();
		}

		@Override
		protected void onCancelled() {
			setBarcodeProgressVisibility(false);
			mFindOrgNameInTask = null;
			super.onCancelled();
		}
	}

	private void showCount() {
		String totalCount = String.valueOf(mOrgCodeTreeAdapter.getCount());
		mTotalCount.setText("Total:" + totalCount);
    }

}