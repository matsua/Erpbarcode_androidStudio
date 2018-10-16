package com.ktds.erpbarcode.infosearch;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.FailureListInfo;
import com.ktds.erpbarcode.barcode.model.LocationHttpController;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.infosearch.model.InfoHttpController;


public class SelectFacDetailActivity extends Activity {

	private static final String TAG = "SelectFacDetailActivity";
	public static final String INPUT_FAC_CD = "facCd";
	
	private String p_facCd = "";
	private Button mTabGeneralButton;
	private Button mTabLocButton;
	private Button mTabOrgButton;
	private Button mTabNetworkButton;
	private Button mTabFailureButton;
	private Button mTabRepairButton;
	
	private ScrollView mTab1;
	private ScrollView mTab2;
	private ScrollView mTab3;
	private ScrollView mTab4;
	private ScrollView mTab5;
	private LinearLayout mTab6;
	
	private FindFacBarcodeDetailInTask mFindFacBarcodeDetailInTask;
	private JSONObject mFacJsonResult;
	
	private FailureListInTask mFailureListInTask;
	private List<FailureListInfo> mFailureListInfo;
	private ListView mFailureListView;
	private FailureListAdapter mFailureListAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		/**********************************************************************
    	 * input parameter
    	 **********************************************************************/
    	p_facCd = getIntent().getStringExtra(INPUT_FAC_CD);
    	
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
	    super.onCreate(savedInstanceState);
	    
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
	    
	    setContentView(R.layout.infosearch_selectfacdetail_activity);
	    setMenuLayout();
		initScreen();
	}

	private void setMenuLayout() {
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("설비상세정보");
        actionBar.setDisplayShowTitleEnabled(true);
        setProgressBarIndeterminateVisibility(false);
		 
		//-----------------------------------------------------------
		mTabGeneralButton = (Button)findViewById(R.id.selectfacdetail_tab_general_button);
		mTabGeneralButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("general");
					}
				});
		mTabLocButton = (Button)findViewById(R.id.selectfacdetail_tab_loc_button);
		mTabLocButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("loc");
					}
				});
		mTabOrgButton = (Button) findViewById(R.id.selectfacdetail_tab_org_button);
		mTabOrgButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("org");
					}
				});
		mTabNetworkButton = (Button)  findViewById(R.id.selectfacdetail_tab_network_button);
		mTabNetworkButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("network");
					}
				});
		mTabFailureButton = (Button)  findViewById(R.id.selectfacdetail_tab_failure_button);
		mTabFailureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("failure");
					}
				});
		mTabRepairButton = (Button)  findViewById(R.id.selectfacdetail_tab_failureList_button);
		mTabRepairButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("repairList");
					}
				});
		
		mTab1 = (ScrollView)findViewById(R.id.selectfacdetail_tab1);
		mTab2 = (ScrollView)findViewById(R.id.selectfacdetail_tab2);
		mTab3 = (ScrollView)findViewById(R.id.selectfacdetail_tab3);
		mTab4 = (ScrollView)findViewById(R.id.selectfacdetail_tab4);
		mTab5 = (ScrollView)findViewById(R.id.selectfacdetail_tab5);
		mTab6 = (LinearLayout) findViewById(R.id.listLinear);
		
	    mFailureListAdapter = new FailureListAdapter(getApplicationContext());
        mFailureListView = (ListView) findViewById(R.id.listView);
        mFailureListView.setAdapter(mFailureListAdapter);
	}
			
	private void switchTab(String tag) {
		if (tag.equals("general")) {
			mTabGeneralButton.setSelected(true);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			mTabFailureButton.setSelected(false);
			mTabRepairButton.setSelected(false);
			
			mTab1.setVisibility(View.VISIBLE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
			mTab5.setVisibility(View.GONE);
			mTab6.setVisibility(View.GONE);
		} else if (tag.equals("loc")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(true);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			mTabFailureButton.setSelected(false);
			mTabRepairButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.VISIBLE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
			mTab5.setVisibility(View.GONE);
			mTab6.setVisibility(View.GONE);
		} else if (tag.equals("org")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(true);
			mTabNetworkButton.setSelected(false);
			mTabFailureButton.setSelected(false);
			mTabRepairButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.VISIBLE);
			mTab4.setVisibility(View.GONE);
			mTab5.setVisibility(View.GONE);
			mTab6.setVisibility(View.GONE);
		} else if (tag.equals("network")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(true);
			mTabFailureButton.setSelected(false);
			mTabRepairButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.VISIBLE);
			mTab5.setVisibility(View.GONE);
			mTab6.setVisibility(View.GONE);
		}
		else if (tag.equals("failure")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			mTabFailureButton.setSelected(true);
			mTabRepairButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
			mTab5.setVisibility(View.VISIBLE);
			mTab6.setVisibility(View.GONE);
		}
		else if (tag.equals("repairList")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			mTabFailureButton.setSelected(false);
			mTabRepairButton.setSelected(true);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
			mTab5.setVisibility(View.GONE);
			mTab6.setVisibility(View.VISIBLE);
			
			setBarcodeProgressVisibility(true);
			mFailureListInTask = new FailureListInTask(p_facCd);
			mFailureListInTask.execute((Void) null);
		}
	}
	
	private void initScreen() {
		mTabFailureButton.setVisibility(View.GONE);
		if(GlobalData.getInstance().getJobGubun().equals("고장등록")){
			switchTab("repairList");
		}
		else{
			switchTab("general");
		}
		
		if (!p_facCd.isEmpty()) {
			if (!p_facCd.isEmpty() && (p_facCd.length()<16 || p_facCd.length()>18)) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다."));
	        	return;
	        }
			
			getFacBarcodeDetailData(p_facCd);
		}
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

	private void getFacBarcodeDetailData(String facBarCode) {
		if (mFindFacBarcodeDetailInTask == null) {
			setBarcodeProgressVisibility(true);
			mFindFacBarcodeDetailInTask = new FindFacBarcodeDetailInTask(facBarCode);
			mFindFacBarcodeDetailInTask.execute((Void) null);
		}
	}
	
	/****************************************************************
	 * SAP장치바코드 정보 조회.
	 */
	private class FindFacBarcodeDetailInTask extends AsyncTask<Void, Void, Boolean> {
		private String _FacCd = "";
		private ErpBarcodeException _ErpBarException;
		
		public FindFacBarcodeDetailInTask(String facCd) {
			_FacCd = facCd;
			mFacJsonResult = null;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				InfoHttpController infohttp = new InfoHttpController();
				mFacJsonResult = infohttp.getFacBarcodeDetailData(_FacCd);
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "설비바코드상세정보 서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				mFacJsonResult = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFindFacBarcodeDetailInTask = null;
			
			if (result) {
				showBarcodeInfoDisplay();		// 일반 설비정보 상세 내역 display 				
			} else {
				mFacJsonResult = null;
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFindFacBarcodeDetailInTask = null;
			mFacJsonResult = null;
			setBarcodeProgressVisibility(false);
		}
	}
	
	/****************************************************************
	 * 고장이력조회.
	 */
	private class FailureListInTask extends AsyncTask<Void, Void, Boolean> {
		private String _FacCd = "";
		private ErpBarcodeException _ErpBarException;
		
		public FailureListInTask(String facCd) {
			_FacCd = facCd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				LocationHttpController listhttp = new LocationHttpController();
				mFailureListInfo = listhttp.getFailureList(_FacCd,"");
				if (mFailureListInfo == null) {
					throw new ErpBarcodeException(-1, "고장이력 정보가 없습니다. ");
				}
			} catch (ErpBarcodeException e) {
				Log.d(TAG, e.getErrMessage());
				_ErpBarException = e;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setBarcodeProgressVisibility(false);
			mFailureListInTask = null;
			if (result) {
				mFailureListAdapter.addItems(mFailureListInfo);
				((TextView) findViewById(R.id.listCount)).setText(mFailureListAdapter.getCount() + "건");
			} else {
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mFailureListInTask = null;
			setBarcodeProgressVisibility(false);
		}
	}
	
	public void showBarcodeInfoDisplay() {
		
		Log.d(TAG, "설비바코드상세정보 nextFacBarcodeDetail  Start...");
		if (mFacJsonResult == null) {
			return;
		}
		
		try {
			((EditText) findViewById(R.id.selectfacdetail_BARCODE)).setText(mFacJsonResult.getString("EQUNR"));
			((EditText) findViewById(R.id.selectfacdetail_BARCODEName)).setText(mFacJsonResult.getString("EQKTX"));
			((EditText) findViewById(R.id.selectfacdetail_UBARCODE)).setText(mFacJsonResult.getString("HEQUNR"));
			((EditText) findViewById(R.id.selectfacdetail_UBARCODEName)).setText(mFacJsonResult.getString("HEQKTX"));
			((EditText) findViewById(R.id.selectfacdetail_ZDESC)).setText(mFacJsonResult.getString("ZDESC"));
			((EditText) findViewById(R.id.selectfacdetail_ZPSTATU)).setText(mFacJsonResult.getString("ZPSTATU"));
			((EditText) findViewById(R.id.selectfacdetail_ZCODENAME)).setText(mFacJsonResult.getString("ZCODENAME"));
			((EditText) findViewById(R.id.selectfacdetail_SERGE)).setText(mFacJsonResult.getString("SERGE"));
			((EditText) findViewById(R.id.selectfacdetail_AULDT)).setText(mFacJsonResult.getString("AULDT"));
			((EditText) findViewById(R.id.selectfacdetail_ANSDT)).setText(mFacJsonResult.getString("ANSDT"));
			((EditText) findViewById(R.id.selectfacdetail_SUBMT)).setText(mFacJsonResult.getString("SUBMT"));
			((EditText) findViewById(R.id.selectfacdetail_MAKTX)).setText(mFacJsonResult.getString("MAKTX"));
			((EditText) findViewById(R.id.selectfacdetail_ZATEXT01)).setText(mFacJsonResult.getString("ZATEXT01"));
			((EditText) findViewById(R.id.selectfacdetail_ZATEXT02)).setText(mFacJsonResult.getString("ZATEXT02"));
			((EditText) findViewById(R.id.selectfacdetail_ZATEXT03)).setText(mFacJsonResult.getString("ZATEXT03"));
			((EditText) findViewById(R.id.selectfacdetail_ZATEXT04)).setText(mFacJsonResult.getString("ZATEXT04"));
			((EditText) findViewById(R.id.selectfacdetail_ZEQART1)).setText(mFacJsonResult.getString("ZEQART1"));
			((EditText) findViewById(R.id.selectfacdetail_ZEQART2)).setText(mFacJsonResult.getString("ZEQART2"));
			((EditText) findViewById(R.id.selectfacdetail_ZEQART3)).setText(mFacJsonResult.getString("ZEQART3"));
			((EditText) findViewById(R.id.selectfacdetail_ZEQART4)).setText(mFacJsonResult.getString("ZEQART4"));
			((EditText) findViewById(R.id.selectfacdetail_PTXT1)).setText(mFacJsonResult.getString("PTXT1"));
			((EditText) findViewById(R.id.selectfacdetail_ZPGUBUN)).setText(mFacJsonResult.getString("ZPGUBUN"));
			((EditText) findViewById(R.id.selectfacdetail_PTXT2)).setText(mFacJsonResult.getString("PTXT2"));
			((EditText) findViewById(R.id.selectfacdetail_ZPPART)).setText(mFacJsonResult.getString("ZPPART"));
			// 설비처리구분
			((EditText) findViewById(R.id.selectfacdetail_ZKEQUI_TXT)).setText(mFacJsonResult.getString("ZKEQUI_TXT"));
			((EditText) findViewById(R.id.selectfacdetail_ZKEQUI)).setText(mFacJsonResult.getString("ZKEQUI"));
			// 자산번호
			((EditText) findViewById(R.id.selectfacdetail_ZANLN1)).setText(mFacJsonResult.getString("ZANLN1"));
			// 최종변경인
			((EditText) findViewById(R.id.selectfacdetail_ZPDAUSER)).setText(mFacJsonResult.getString("ZPDAUSER"));
			((EditText) findViewById(R.id.selectfacdetail_ZPDAUSERNM)).setText(mFacJsonResult.getString("ZPDAUSERNM"));
	
			// 최종변경일
			((EditText) findViewById(R.id.selectfacdetail_AEDAT)).setText(mFacJsonResult.getString("AEDAT") + "  " + mFacJsonResult.getString("CHANGED_TIME")); //최종변경일
			
			// 교체전 설비
			((EditText) findViewById(R.id.selectfacdetail_ZEQBR)).setText(mFacJsonResult.getString("ZEQBR"));
			// 교체 후 설비
			((EditText) findViewById(R.id.selectfacdetail_ZEQRR)).setText(mFacJsonResult.getString("ZEQRR"));
			
			/* 위치 */
			// 국사코드
			((EditText) findViewById(R.id.selectfacdetail_ZKUKCO)).setText(mFacJsonResult.getString("ZKUKCO"));
			// 유무선구분
			((EditText) findViewById(R.id.selectfacdetail_EQUIPGD_TXT)).setText(mFacJsonResult.getString("EQUIPGD_TXT"));
			// 장치아이디
			((EditText) findViewById(R.id.selectfacdetail_ZEQUIPGC)).setText(mFacJsonResult.getString("ZEQUIPGC"));
			((EditText) findViewById(R.id.selectfacdetail_DEVICEGB)).setText(mFacJsonResult.getString("DEVICEGB"));
			// 공급업체
			((EditText) findViewById(R.id.selectfacdetail_ZLIFNR)).setText(mFacJsonResult.getString("ZLIFNR"));
			((EditText) findViewById(R.id.selectfacdetail_ZLIFNM)).setText(mFacJsonResult.getString("ZLIFNM"));
			// 위치바코드
			((EditText) findViewById(R.id.selectfacdetail_ZEQUIPLP)).setText(mFacJsonResult.getString("ZEQUIPLP"));
			((EditText) findViewById(R.id.selectfacdetail_PLOCNAME)).setText(mFacJsonResult.getString("PLOCNAME"));
			// 기능위치
			((EditText) findViewById(R.id.selectfacdetail_TPLNR)).setText(mFacJsonResult.getString("TPLNR"));
			((EditText) findViewById(R.id.selectfacdetail_PLTXT)).setText(mFacJsonResult.getString("PLTXT"));
			
			/* 조직 */
			// 운용조직
			((EditText) findViewById(R.id.selectfacdetail_ZKTEXT)).setText(mFacJsonResult.getString("ZKTEXT"));
			// 코스트센터
			((EditText) findViewById(R.id.selectfacdetail_ZKOSTL)).setText(mFacJsonResult.getString("ZKOSTL"));
			
			// WBS요소
			((EditText) findViewById(R.id.selectfacdetail_PROID)).setText(mFacJsonResult.getString("PROID"));
	
			// 유지보수 센터
			((EditText) findViewById(R.id.selectfacdetail_CENTER_NAME)).setText(mFacJsonResult.getString("CENTER_NAME"));
			((EditText) findViewById(R.id.selectfacdetail_ZCENTER)).setText(mFacJsonResult.getString("ZCENTER"));
	
			// 운용시스템 구분
			((EditText) findViewById(R.id.selectfacdetail_ZEQUIPGL_TXT)).setText(mFacJsonResult.getString("ZEQUIPGL_TXT"));
	
			/* 보증 */
			((EditText) findViewById(R.id.selectfacdetail_ABCTX)).setText(mFacJsonResult.getString("ABCTX"));
			((EditText) findViewById(R.id.selectfacdetail_ABCKZ)).setText(mFacJsonResult.getString("ABCKZ"));
	
			// 신규설비
			((EditText) findViewById(R.id.selectfacdetail_GWLDT_O)).setText(mFacJsonResult.getString("GWLDT_O"));
			((EditText) findViewById(R.id.selectfacdetail_GWLEN_O)).setText(mFacJsonResult.getString("GWLEN_O"));
			// 개조설비
			((EditText) findViewById(R.id.selectfacdetail_GWLDT_I)).setText(mFacJsonResult.getString("GWLDT_I"));
			((EditText) findViewById(R.id.selectfacdetail_GWLEN_I)).setText(mFacJsonResult.getString("GWLEN_I"));
		} catch (JSONException e) {
			Log.d(TAG, "설비바코드 상세 정보 필드에 대입중 오류가 발생했습니다. " + e.getMessage());
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드 상세 정보 필드에 대입중 오류가 발생했습니다. "));
			return;
		}
	}
}