package com.ktds.erpbarcode.infosearch;

import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.SAPBarcodeService;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.treeview.InMemoryTreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeViewList;

public class SelectDeviceSubLocFragment extends Fragment {

	private static final String TAG = "SelectDeviceSubLocFragment";

	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mBarcodeTreeView;
	private BarcodeTreeAdapter mBarcodeTreeAdapter;
	private TextView mTotalCount;
	
	private RelativeLayout mBarcodeProgress;

	public static SelectDeviceSubLocFragment newInstance() {
		SelectDeviceSubLocFragment fragment = new SelectDeviceSubLocFragment();
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.infosearch_selectdevice_loc_fragment, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setLayout(savedInstanceState);
		initScreen();
		
		Log.d(TAG, "onActivityCreated   general...");
	}
	
	@SuppressWarnings("unchecked")
	private void setLayout(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mTreeManager = new InMemoryTreeStateManager<Long>();
        } else {
        	mTreeManager = (TreeStateManager<Long>) savedInstanceState.getSerializable("treeManager");
            if (mTreeManager == null) {
            	mTreeManager = new InMemoryTreeStateManager<Long>();
            }
            //newCollapsible = savedInstanceState.getBoolean("collapsible");
        }
		
		mBarcodeTreeAdapter = new BarcodeTreeAdapter(getActivity().getApplicationContext(), mTreeManager);
		
		mBarcodeTreeView = (TreeViewList) getActivity().findViewById(R.id.selectdevice_loc_treeView);
		mBarcodeTreeView.setAdapter(mBarcodeTreeAdapter);
		mBarcodeTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "mBarcodeTreeView onItemClick   position==>" + position);

				Long selectedKey = getBarcodeTreeAdapter().getTreeId(position);
				getBarcodeTreeAdapter().changeSelected(selectedKey);
				getBarcodeTreeAdapter().refresh();
			}
		});
		mBarcodeTreeView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(id);
				
				if (model == null || model.getBarcode().isEmpty()) return false;

				getBarcodeTreeAdapter().changeSelected(id);
				
				Intent intent = new Intent(getActivity().getApplicationContext(), SelectFacDetailActivity.class);
				intent.putExtra(SelectFacDetailActivity.INPUT_FAC_CD, model.getBarcode());
		        startActivity(intent);
		        
				return true;
			}
		});
		
		mTotalCount = (TextView) getActivity().findViewById(R.id.selectdevice_loc_bottomBar_totalCount);
		
		// 바코드스캔시 조회때만 사용.
        mBarcodeProgress = (RelativeLayout) getActivity().findViewById(R.id.selectdevice_loc_barcodeProgress);
	}
	
	public void initScreen() {
		getBarcodeTreeAdapter().itemClear();
		showSummaryCount();
	}
	
    // 바코드 조회용 ProgressBar Visibility
    public void setFragmentProgressVisibility(boolean show) {
    	if (show) {
    		mBarcodeProgress.setVisibility(View.VISIBLE);
    	} else {
    		mBarcodeProgress.setVisibility(View.GONE);
    	}
    }
    
    public void setBarcodeProgressVisibility(boolean show) {
    	((SelectDeviceActivity) getActivity()).setBarcodeProgressVisibility(show);
    }

    public boolean isBarcodeProgressVisibility() {
    	return ((SelectDeviceActivity) getActivity()).isBarcodeProgressVisibility();
    }
	
    public BarcodeTreeAdapter getBarcodeTreeAdapter() {
    	return mBarcodeTreeAdapter;
    }
	
	public void getSAPBarcodeInfos(String locCd, String deviceId, String barcode) {
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		
		getBarcodeTreeAdapter().itemClear();
		showSummaryCount();
		
    	SAPBarcodeService sapBarcodeInfoService = new SAPBarcodeService(mSAPBarcodeInfoHandler);
    	sapBarcodeInfoService.search(locCd, deviceId, barcode);
	}
	
	/**
	 * SAPBarcodeInfo 정보 조회 결과 Handler
	 */
    private final Handler mSAPBarcodeInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	Log.d(TAG, "SAPBarcodeInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapBarcodeListInfos = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

        		getBarcodeTreeAdapter().addItems(sapBarcodeListInfos);
        		
        		// 조회건수를 보여준다.
        		showSummaryCount();
        		
                break;
            case SAPBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "하위 설비가 존재하지 않습니다."));
            	break;
            case SAPBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    };
    
    private void showSummaryCount() {
    	mTotalCount.setText("");
    	
    	int DB_RCount = getBarcodeTreeAdapter().getDBPartTypeCount("R");
    	int DB_SCount = getBarcodeTreeAdapter().getDBPartTypeCount("S");
    	int DB_UCount = getBarcodeTreeAdapter().getDBPartTypeCount("U");
    	int DB_ECount = getBarcodeTreeAdapter().getDBPartTypeCount("E");
    	int DB_TCount = DB_RCount + DB_SCount + DB_UCount + DB_ECount;
    	String DBCount = "R(" + DB_RCount + ") S(" + DB_SCount + ") U(" + DB_UCount + ") E(" + DB_ECount + ") Total:" + DB_TCount + "건";
    	
    	mTotalCount.setText(DBCount);
    }
}
