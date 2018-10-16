package com.ktds.erpbarcode.management;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.SAPBarcodeService;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.treeview.InMemoryTreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.common.treeview.TreeViewList;
import com.ktds.erpbarcode.infosearch.SelectFacDetailActivity;
import com.ktds.erpbarcode.job.JobActionManager;
import com.ktds.erpbarcode.job.JobActionStepManager;

public class TransferTreeFragment extends Fragment {

	private static final String TAG = "TransferTreeFragment";

	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mBarcodeTreeView;
	private BarcodeTreeAdapter mBarcodeTreeAdapter;
	private RelativeLayout mBarcodeProgress;

	private TextView mTotalCountText;
	private TextView mTotalScanCountText;
	
	private String mJobGubun;
	
    public TransferTreeFragment() { }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.management_transfertree_fragment, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mJobGubun = GlobalData.getInstance().getJobGubun();
		
		setLayout(savedInstanceState);
		
		//-----------------------------------------------------------
		// TreeView의 바닥 Summary 보여준다.
		//-----------------------------------------------------------
		showSummaryCount();
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
		
		mBarcodeTreeView = (TreeViewList) getActivity().findViewById(R.id.transfertree_treeView);
		mBarcodeTreeView.setAdapter(mBarcodeTreeAdapter);
		mBarcodeTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "mBarcodeTreeView onItemClick   position==>" + position);
				
				final BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(id);
				if (model == null) return;
				
				//---------------------------------------------------
				// 선택한 바코드 위치로 이동.
				//---------------------------------------------------
				if (getEditMode() == TransferActivity.EDIT_MODE_MOVE) {
					moveSubTreeYesNoDialog(id, model);
				}
				

				getBarcodeTreeAdapter().changeSelected(id);
				choiceBarcodeDataDisplay(model);
			}
		});
		
		mBarcodeTreeView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				getBarcodeTreeAdapter().changeSelected(id);
				BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(id);
				if (model != null) {
					choiceBarcodeDataDisplay(model);
					
					Intent intent = new Intent(getActivity().getApplicationContext(), SelectFacDetailActivity.class);
					intent.putExtra(SelectFacDetailActivity.INPUT_FAC_CD, model.getBarcode());
			        startActivity(intent);
				}
				
				return true;
			}
		});
		
		// 바코드스캔시 조회때만 사용.
        mBarcodeProgress = (RelativeLayout) getActivity().findViewById(R.id.transfertree_barcodeProgress);

		mTotalCountText = (TextView) getActivity().findViewById(R.id.transfertree_totalCount);
		mTotalScanCountText = (TextView) getActivity().findViewById(R.id.transfertree_totalScanCount);
		
		Log.d(TAG, "setLayout   End.");
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("treeManager", mTreeManager);
        super.onSaveInstanceState(outState);
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
    	((TransferActivity) getActivity()).setBarcodeProgressVisibility(show);
    }

    public boolean isBarcodeProgressVisibility() {
    	return ((TransferActivity) getActivity()).isBarcodeProgressVisibility();
    }
    
    public void initScreenTreeFragment() {
    	if (mJobGubun.equals("인수")) {
    		mTotalScanCountText.setVisibility(View.VISIBLE);
    	}
    	
    	if (getBarcodeTreeAdapter() != null) {
    		getBarcodeTreeAdapter().itemClear();
    	}

    	//-----------------------------------------------------------
		// TreeView의 바닥 Summary 보여준다.
		//-----------------------------------------------------------
    	showSummaryCount();
    }
    
    private void choiceBarcodeDataDisplay(BarcodeListInfo barcodeInfo) {
    	TransferActivity activity = (TransferActivity) getActivity();
		activity.choiceBarcodeDataDisplay(barcodeInfo);
    }

    public BarcodeTreeAdapter getBarcodeTreeAdapter() {
    	return mBarcodeTreeAdapter;
    }
    
    public TreeViewList getBarcodeTreeView() {
    	return mBarcodeTreeView;
    }
    
    /**
     *  EDIT_MODE 상태
     */
    public int getEditMode() {
		return ((TransferActivity) getActivity()).getEditMode();
	}

    /**
     *  EDIT_MODE 상태
     */
	public void setEditMode(int editMode) {
		((TransferActivity) getActivity()).setEditMode(editMode);
	}
	
	/**
     *  EDIT_MODE TreeKey
     */
	public Long getEditTreeKey() {
		return ((TransferActivity) getActivity()).getEditTreeKey();
	}

	/**
     *  EDIT_MODE TreeKey
     */
	public void setEditTreeKey(Long editTreeKey) {
		((TransferActivity) getActivity()).setEditTreeKey(editTreeKey);
	}
	
	/**
     * 수정모드 취소.
     */
	public void cancelEditMode() {
		((TransferActivity) getActivity()).cancelEditMode();
	}
	
	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public BarcodeListInfo getEditBarcodeInfo() {
		return ((TransferActivity) getActivity()).getEditBarcodeInfo();
	}

	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public void setEditBarcodeInfo(BarcodeListInfo editBarcodeInfo) {
		((TransferActivity) getActivity()).setEditBarcodeInfo(editBarcodeInfo);
	}
	
	/**
     * 대상 바코드를 타켓 바코드 자식노드로 이동한다.
     * 
     * @param selectedKey
     * @param targetKey
     */
	public void moveSubTree(final Long selectedKey, final Long targetKey) {
		((TransferActivity) getActivity()).moveSubTree(selectedKey, targetKey);
	}
	
    // 운용조직 다른지 체크하여 변경여부 선택한다.
    private void moveSubTreeYesNoDialog(final Long targetKey, final BarcodeListInfo model) {
    	
		if (getEditTreeKey() == null || getEditBarcodeInfo() == null) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "이동할 대상 바코드를 선택하세요."));
			return;
		}
		
		final Long selectedKey = getEditTreeKey();

		if (targetKey.longValue() == selectedKey.longValue() ) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "동일한 바코드로 이동 할 수 없습니다."));
			return;
		}
		
		
		//-----------------------------------------------------------
		// Yes/No Dialog
		//-----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);
		
		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		String message = "'" + model.getPartType() + ":" + model.getBarcode() + "'의 하위로 이동하시겠습니까?";
    	
    	final Builder builder = new AlertDialog.Builder(getActivity()); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getActivity());
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
            	
            	// 선택한 바코드를 이동한다.
            	moveSubTree(selectedKey, targetKey);
            }
        });
        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
            	cancelEditMode();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }
	
	public void doChkScan(String locCode, String deviceId, String barcode) {
		Log.d(TAG, "doChkScan   barcode==>" + barcode);

		TreeNodeInfo<Long> thisTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(barcode);
		if (thisTreeNode != null) {
			Log.d(TAG, "doChkScan   thisBarcodeKey==>" + barcode+"("+thisTreeNode.getId()+"|"+thisTreeNode.getLevel()+")");
			
			BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(thisTreeNode.getId());
			
			
			String scanValue = barcodeInfo.getScanValue();
			
			if (!scanValue.equals("1")) {
				// 상위바코드 자료를 검색한다.
				Long parentKey = getBarcodeTreeAdapter().getManager().getParent(thisTreeNode.getId());
				if (parentKey != null) {
					BarcodeListInfo parentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
					if (!parentBarcodeInfo.getPartType().equals("L") && !parentBarcodeInfo.getPartType().equals("D")) {
						if (mJobGubun.equals("인수")) {
							if (parentBarcodeInfo.getScanValue().equals("0")) {
								GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "스캔하신 형상이 인계스캔과\n\r상이합니다."));
		                        scanValue = "4";
							}
						} else {
							choiceBarcodeDataDisplay(barcodeInfo);
							GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "상위바코드를 스캔하세요."));
	                        return;
						}
					}
				}
			}

			getBarcodeTreeAdapter().changeSelected(thisTreeNode.getId());
			int thisPosition = getBarcodeTreeAdapter().getKeyPosition(thisTreeNode.getId());
			mBarcodeTreeView.setSelection(thisPosition);  // 선택된 바코드로 커서 이동한다.

			if (scanValue.equals("1") || scanValue.equals("2") || scanValue.equals("3")) {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
				return;
            }

			if (scanValue.equals("0")) scanValue = "1";
			
			barcodeInfo.setScanValue(scanValue);
			getBarcodeTreeAdapter().setBarcodeListInfo(thisTreeNode.getId(), barcodeInfo);
			
			choiceBarcodeDataDisplay(barcodeInfo);
			
			
			//-----------------------------------------------------------
			// TreeView의 바닥 Summary 보여준다.
			//-----------------------------------------------------------
			showSummaryCount();
			
			//-------------------------------------------------------
			// 화면 초기 Setting후 변경여부 Flag
			//-------------------------------------------------------
	    	GlobalData.getInstance().setChangeFlag(true);
	    	
	    	//-----------------------------------------------------------
			// 단계별 작업을 여기서 처리한다.
			//-----------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			} else {
				try {
					GlobalData.getInstance().getJobActionManager().setStepItem(
							JobActionStepManager.JOBTYPE_FAC, "facCd", barcodeInfo.getBarcode());
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (mJobGubun.equals("인수")) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'인수' 대상인 설비바코드가\n\r존재하지 않습니다."));
				return;
			}
            Log.d(TAG, "addScanData   Start....");
            addScanData(locCode, deviceId, barcode);
		}
	}
	
	public void addScanData(String locCd, String deviceId, String barcode) {
		getSAPBarcodeInfos(locCd, deviceId, barcode);
	}
	
	/**
	 * TreeView FooterBar Summary Count
	 */
    public void showSummaryCount() {
    	if (getBarcodeTreeAdapter() == null)
    		return;
    	
    	//-----------------------------------------------------------
    	// DB에서 조회된 정보로 Count한다.
    	//-----------------------------------------------------------
    	int DB_DCount = getBarcodeTreeAdapter().getDBPartTypeCount("D");
    	int DB_RCount = getBarcodeTreeAdapter().getDBPartTypeCount("R");
    	int DB_SCount = getBarcodeTreeAdapter().getDBPartTypeCount("S");
    	int DB_UCount = getBarcodeTreeAdapter().getDBPartTypeCount("U");
    	int DB_ECount = getBarcodeTreeAdapter().getDBPartTypeCount("E");
    	int DB_TCount = DB_DCount + DB_RCount + DB_SCount + DB_UCount + DB_ECount;
    	String DBCount = "D(" + DB_DCount + ") R(" + DB_RCount + ") S(" + DB_SCount + ") U(" + DB_UCount + ") E(" + DB_ECount + ") Total:" + DB_TCount + "건";
    	

    	//-----------------------------------------------------------
    	// 인계일때는 스캔한 값을 보여준다.
    	//-----------------------------------------------------------
    	if (mTotalScanCountText.getVisibility() == View.VISIBLE) {
    		
    		mTotalCountText.setText("인계:" + DBCount);
    		
    		int Scan_DCount = getBarcodeTreeAdapter().getScanPartTypeCount("D");
        	int Scan_RCount = getBarcodeTreeAdapter().getScanPartTypeCount("R");
        	int Scan_SCount = getBarcodeTreeAdapter().getScanPartTypeCount("S");
        	int Scan_UCount = getBarcodeTreeAdapter().getScanPartTypeCount("U");
        	int Scan_ECount = getBarcodeTreeAdapter().getScanPartTypeCount("E");
        	int Scan_TCount = Scan_DCount + Scan_RCount + Scan_SCount + Scan_UCount + Scan_ECount;
        	String ScanCount = "D(" + Scan_DCount + ") R(" + Scan_RCount + ") S(" + Scan_SCount + ") U(" + Scan_UCount + ") E(" + Scan_ECount + ") Total:" + Scan_TCount + "건";
        	
        	mTotalScanCountText.setText("인수:" + ScanCount);
    	} else {
    		mTotalCountText.setText(DBCount);
    	}
    }
	
	public void getSAPBarcodeInfos(String locCode, String deviceId, String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
    	SAPBarcodeService sapBarcodeInfoService = new SAPBarcodeService(new SAPBarcodeInfoHandler(barcode));
    	sapBarcodeInfoService.search(locCode, deviceId, barcode);
	}
	
	private class SAPBarcodeInfoHandler extends Handler {
		private String _Barcode;
		
		public SAPBarcodeInfoHandler(String barcode) {
			_Barcode = barcode;
		}
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS:
            	Log.d(TAG, "SAPBarcodeInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);


                AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(sapItems.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = sapItems;
                agoBarcodeInfosListView(agoCheck);
        		
                break;
            case SAPBarcodeService.STATE_NOT_FOUND:
            	//String notfoundMessage = msg.getData().getString("message");
            	GlobalData.getInstance().showMessageDialog(
            			new ErpBarcodeException(-1, "존재하지 않는 설비바코드입니다. ", BarcodeSoundPlay.SOUND_NOTEXISTS));
            	break;
            case SAPBarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }	
	}
    
    public class AgoCheck {
    	public String change_barcode_yn = "N";
    	public List<BarcodeListInfo> barcodeItems = null;
    	public String auto_parallel_hierarchy_yn = null;
    	public String auto_parent_barcode = "";
    }
    
    private void agoBarcodeInfosListView(final AgoCheck agoCheck) {
    	
    	BarcodeListInfo firstBarcodeInfo = agoCheck.barcodeItems.get(0);
    	Long barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(firstBarcodeInfo.getBarcode());
    	
        String O_DATA_C = firstBarcodeInfo.getO_data_c();
        String ZANLN1 = firstBarcodeInfo.getZanln1();  //자산번호
        String ANSDT = firstBarcodeInfo.getAnsdt();   //취득일
        String ZSETUP = firstBarcodeInfo.getZsetup();  //공사비

        if (mJobGubun.equals("인계") || mJobGubun.equals("인수")) {
            if (O_DATA_C.equals("2")) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "해당 설비바코드는 '인계'\n\r대상이 아닙니다.\n\r'시설등록'으로 처리 하시기 바랍니다."));
                return;
            }
            
            if(!ZANLN1.equals("") && !ANSDT.equals("") && !ZSETUP.equals("0")){
                GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "구품 설비는 철거 스캔 선행 후\n\r인계 작업을 진행 하시기 바랍니다."));
                //return;
            }
        }
        else if (mJobGubun.equals("시설등록")) {
            if (O_DATA_C.equals("") || O_DATA_C.equals("1")) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "해당 설비바코드는 '시설등록'\n\r대상이 아닙니다.\n\r'인계'로 처리 하시기 바랍니다."));
                return;
            }
        }

        
		if (mJobGubun.equals("인수")) {
			//---------------------------------------------------------------
	    	// 스캔한 바코드와 조회된 바코드가 다를때 중복체크 한번더 한다.
	    	//---------------------------------------------------------------
	    	if (agoCheck.change_barcode_yn.equals("Y")) {
	    		// 인수는 대상바코드가 없으면 메세지 처리한다.
	    		doChkScan("", "", firstBarcodeInfo.getBarcode());
    			return;
	    	}
		} else {
			//-----------------------------------------------------------
	    	// 스캔한 바코드와 서버에서 조회된 바코드가 상이한 경우 때문에 
	    	// 여기서 중복체크 한번더 한다.
	    	//-----------------------------------------------------------
			if (barcodeKey != null) {
				TreeNodeInfo<Long> thisTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(firstBarcodeInfo.getBarcode());
				getBarcodeTreeAdapter().changeSelected(thisTreeNode.getId());
				int thisPosition = getBarcodeTreeAdapter().getKeyPosition(thisTreeNode.getId());
				mBarcodeTreeView.setSelection(thisPosition);  // 선택된 바코드로 커서 이동한다.
				
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + firstBarcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
				return;
			}
		}
		
		if (agoCheck.auto_parallel_hierarchy_yn == null) {
			agoCheck.auto_parallel_hierarchy_yn = "N";

			//-------------------------------------------------------------
			// 병렬 설비 등록이 아닐때만..
			//-------------------------------------------------------------
			if (agoCheck.auto_parallel_hierarchy_yn.equals("N")) {
				
				Long thisSelectedKey = null;
	        	if (getBarcodeTreeAdapter().getCount() == 0) {
	        		thisSelectedKey = null;
	        	} else {
	        		thisSelectedKey = getBarcodeTreeAdapter().getSelected();
	        	}
	        	
	        	if (thisSelectedKey != null) {
	        		// 자동으로 선택된 형상의 가능한 상위바코드를 선택하여 준다. 없으면 병렬처리이므로 오류 처리
	    			agoCheck.auto_parent_barcode = getCheckParentBarcodeInfo(firstBarcodeInfo);
	    			
	    			if (agoCheck.auto_parent_barcode.isEmpty()) {
	    				BarcodeListInfo selectedBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(thisSelectedKey);
	    				
	    				//---------------------------------------------------------
	    				// 위치바코드 자식Node는 장치바코드만 가능하다.
	    				//---------------------------------------------------------
	    				if (selectedBarcodeInfo.getPartType().equals("L") && !firstBarcodeInfo.getPartType().equals("D")) {
	    	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드의 하위에 \n\r 설비를 추가하실 수 없습니다. "));
	    	            	return;
	    	    		}
	    				
	    				//---------------------------------------------------------
	    				// 장치바코드 자식Node에는 Unit설비바코드는 형상 구성이 불가능하다.
	    				//---------------------------------------------------------
	    				if (selectedBarcodeInfo.getPartType().equals("D") && firstBarcodeInfo.getPartType().equals("U")) {
	    	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드의 하위에 \n\r Unit 설비바코드를 \n\r 스캔하실 수 없습니다. "));
	    	            	return;
	    	    		}
	    				
	    				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "여러 설비를 동시에 '" + mJobGubun + "' 작업을\n\r하실 수 없습니다.\n\r먼저 전송 후 다시 시도하세요."));
	                	return;
	    			}
	        	}
			}
		}
		
    	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
			BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);

			//-----------------------------------------------------------
            // 스캔 바코드 상위, 하위 구분.
			//-----------------------------------------------------------
			String scanValue = "2";
            Log.d(TAG, "addBarcodeInfosTreeView  scanValue==>"+scanValue);
            barcodeInfo.setScanValue(scanValue);
            
            //---------------------------------------------------------
            // 화면에 보여주기 위해서 운용조직 여부 = "Y"
            //---------------------------------------------------------
            barcodeInfo.setCheckOrgYn("Y");
		}
    	
    	addBarcodeInfosTreeView(agoCheck);
    }

    /**
     * 음영지역 작업일때 사용함.
     */
	public void addBarcodeInfosTreeView(final List<BarcodeListInfo> barcodeInfos) {
		
		BarcodeListInfo firstBarcodeInfo = barcodeInfos.get(0);
		
    	//-----------------------------------------------------------
    	// addBarcodeInfosTreeView 에서 필요한 부분만 여기에 적용함.
    	//-----------------------------------------------------------
    	final AgoCheck agoCheck = new AgoCheck();
		agoCheck.barcodeItems = barcodeInfos;
		
		if (agoCheck.auto_parallel_hierarchy_yn == null) {
			agoCheck.auto_parallel_hierarchy_yn = "N";

			//-------------------------------------------------------------
			// 병렬 설비 등록이 아닐때만..
			//-------------------------------------------------------------
			if (agoCheck.auto_parallel_hierarchy_yn.equals("N")) {
				
				Long thisSelectedKey = null;
	        	if (getBarcodeTreeAdapter().getCount() == 0) {
	        		thisSelectedKey = null;
	        	} else {
	        		thisSelectedKey = getBarcodeTreeAdapter().getSelected();
	        	}
	        	
	        	if (thisSelectedKey != null) {
	        		// 자동으로 선택된 형상의 가능한 상위바코드를 선택하여 준다. 없으면 병렬처리이므로 오류 처리
	    			agoCheck.auto_parent_barcode = getCheckParentBarcodeInfo(firstBarcodeInfo);
	    			
	    			if (agoCheck.auto_parent_barcode.isEmpty()) {
	    				BarcodeListInfo selectedBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(thisSelectedKey);
	    				
	    				//---------------------------------------------------------
	    				// 위치바코드 자식Node는 장치바코드만 가능하다.
	    				//---------------------------------------------------------
	    				if (selectedBarcodeInfo.getPartType().equals("L") && !firstBarcodeInfo.getPartType().equals("D")) {
	    	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드의 하위에 \n\r 설비를 추가하실 수 없습니다. "));
	    	            	return;
	    	    		}
	    				
	    				//---------------------------------------------------------
	    				// 장치바코드 자식Node에는 Unit설비바코드는 형상 구성이 불가능하다.
	    				//---------------------------------------------------------
	    				if (selectedBarcodeInfo.getPartType().equals("D") && firstBarcodeInfo.getPartType().equals("U")) {
	    	    			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "장치바코드의 하위에 \n\r Unit 설비바코드를 \n\r 스캔하실 수 없습니다. "));
	    	            	return;
	    	    		}
	    				
	    				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "여러 설비를 동시에 '" + mJobGubun + "' 작업을\n\r하실 수 없습니다.\n\r먼저 전송 후 다시 시도하세요."));
	                	return;
	    			}
	        	}
			}
		}
		
    	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
			BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);

			//-----------------------------------------------------------
            // 스캔 바코드 상위, 하위 구분.
			//-----------------------------------------------------------
			String scanValue = "2";
            Log.d(TAG, "addBarcodeInfosTreeView  scanValue==>"+scanValue);
            barcodeInfo.setScanValue(scanValue);
            
            //---------------------------------------------------------
            // 화면에 보여주기 위해서 운용조직 여부 = "Y"
            //---------------------------------------------------------
            barcodeInfo.setCheckOrgYn("Y");
		}
    	
    	addBarcodeInfosTreeView(agoCheck);
		
	}
    
    private void addBarcodeInfosTreeView(final AgoCheck agoCheck) {
    	//-----------------------------------------------------------
        // U-U Check는 추가시는 초기화.
    	//  **주의** isUUCheck는 getCheckParentBarcodeInfo 에서 사용하므로 이함수보다 뒤에서 초기화 해야함.
        //-----------------------------------------------------------
    	((TransferActivity) getActivity()).setUUCheck(false);
    	
    	
    	List<BarcodeListInfo> newBarcodeInfos = agoCheck.barcodeItems;
    	
    	//-----------------------------------------------------------
        // 스캔한 설비에 대해 부품타입 보이기
        //-----------------------------------------------------------
    	choiceBarcodeDataDisplay(newBarcodeInfos.get(0));
    	
    	
    	//-----------------------------------------------------------
        // Tree에서 선택된 Row가 있는지 Key를 가져온다.
        //-----------------------------------------------------------
    	Long thisSelectedKey = null;
    	if (getBarcodeTreeAdapter().getCount() == 0) {
    		thisSelectedKey = null;
    	} else {
    		thisSelectedKey = getBarcodeTreeAdapter().getSelected();
    	}

    	if (thisSelectedKey != null) {
    		TreeNodeInfo<Long> selectedTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(thisSelectedKey);
    		if (selectedTreeNode != null) {
    			Log.d(TAG, "addBarcodeInfosTreeView  selectedTreeNode.getId()==>"+selectedTreeNode.getId() + " | " + selectedTreeNode.getLevel());
    			//BarcodeListInfo selectedBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedTreeNode.getId());
    			BarcodeListInfo firstBarcodeInfo = newBarcodeInfos.get(0);

    			
				//---------------------------------------------------------
				// Tree에서 대상 수정모드일때 처리한다.
				//---------------------------------------------------------
				if (getEditMode() == TransferActivity.EDIT_MODE_MODIFY) {
					
					editBarcodeCheck editCheck = new editBarcodeCheck();
					editCheck.barcodeItems = newBarcodeInfos;
					editBarcodeTreeView(editCheck);
					return;

                } else {
                	Long parentKey = getBarcodeTreeAdapter().getBarcodeKey(agoCheck.auto_parent_barcode);
    				BarcodeListInfo selectedParentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
    				if (selectedParentBarcodeInfo != null) {
    					
    					firstBarcodeInfo.setuFacCd(selectedParentBarcodeInfo.getBarcode());
    					firstBarcodeInfo.setDeviceId(selectedParentBarcodeInfo.getDeviceId());
                    	newBarcodeInfos.set(0, firstBarcodeInfo);
                    	
    					// 선택된 상위바코드로 Focus를 지정한다.
    					getBarcodeTreeAdapter().changeSelected(parentKey);
                    	Long parentSelectKey = getBarcodeTreeAdapter().getSelected();
                    	selectedTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(parentSelectKey);
                    	Log.d(TAG, "addBarcodeInfosTreeView  #2 selectedTreeNode.getId()==>"+selectedTreeNode.getId() + " | " + selectedTreeNode.getLevel());
    				}
    				
    				// TreeNode가 닫혀있으면 펼친다.
                	if (!selectedTreeNode.isExpanded()) {
                		getBarcodeTreeAdapter().getManager().expandDirectChildren(selectedTreeNode.getId());
                	}

                	// TreeView에 추가한다.
                	getBarcodeTreeAdapter().addChildItems(selectedTreeNode.getId(), newBarcodeInfos);
                }

            	
            	TreeNodeInfo<Long> barcodeTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(newBarcodeInfos.get(0).getBarcode());
            	if (barcodeTreeNode != null) {
            		getBarcodeTreeAdapter().changeSelected(barcodeTreeNode.getId());
            		int barcodePosition = getBarcodeTreeAdapter().getKeyPosition(barcodeTreeNode.getId());
            		mBarcodeTreeView.setSelection(barcodePosition);  // 선택된 바코드로 커서 이동한다.

                	//choiceBarcodeDataDisplay(treeNode.getId());
            	}
    		}
        } else {
        	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드를 선택후 스캔하세요."));
        	return;
        }

    	//-----------------------------------------------------------
		// TreeView의 바닥 Summary 보여준다.
		//-----------------------------------------------------------
    	showSummaryCount();
    	
    	//-------------------------------------------------------
		// 화면 초기 Setting후 변경여부 Flag
		//-------------------------------------------------------
    	GlobalData.getInstance().setChangeFlag(true);
    	
    	//-----------------------------------------------------------
		// 단계별 작업을 여기서 처리한다.
		//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().setStepItem(
						JobActionStepManager.JOBTYPE_FAC, "facCd", newBarcodeInfos.get(0).getBarcode());
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 바코드 대체 체크 Class
     */
    public class editBarcodeCheck {
    	public String edit_barcode_yn = null;
    	public List<BarcodeListInfo> barcodeItems = null;
    }
    
	/**
	 * Tree의 다른 설비바코드로 대체.
	 * 
	 * @param newBarcodeInfos
	 */
	public void editBarcodeTreeView(final editBarcodeCheck editCheck) {
		
		if (getEditTreeKey() == null || getEditBarcodeInfo() == null) return;
		
		final BarcodeListInfo firstBarcodeInfo = editCheck.barcodeItems.get(0);
		
		if (!firstBarcodeInfo.getPartType().equals(getEditBarcodeInfo().getPartType())) {
			GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "다른 종류의 바코드로 수정하실 수\n\r없습니다.\n\r\n\r같은 종류의 바코드를 다시\n\r스캔하세요."));
            return;
		}
		
		
		if (editCheck.edit_barcode_yn == null) {
			editCheck.edit_barcode_yn = "N";
			
			String nodeText = getBarcodeTreeAdapter().getDescription(getEditBarcodeInfo());
			
			//-----------------------------------------------------------
			// Yes/No Dialog
			//-----------------------------------------------------------
			if (GlobalData.getInstance().isGlobalAlertDialog()) return;
			GlobalData.getInstance().setGlobalAlertDialog(true);
			
			GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
			String message = "원본바코드:" + nodeText
	                + "\r\n" + "수정바코드:" + firstBarcodeInfo.getPartTypeCode() + ":" + firstBarcodeInfo.getBarcode() + ":" 
					+ firstBarcodeInfo.getPartType() + ":" + firstBarcodeInfo.getDevTypeName() + "\r\n로 수정하시겠습니까?";
	    	
	    	final Builder builder = new AlertDialog.Builder(getActivity()); 
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle("알림");
			TextView msgText = new TextView(getActivity());
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
	            	
	            	// 선택한 바코드를 스캔한 바코드로 대체한다.
	            	//editCheck.edit_barcode_yn = "Y";
	            	jobWorkEditBarcodeTreeView(editCheck.barcodeItems.get(0));
	            	return;
	            }
	        });
	        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	GlobalData.getInstance().setGlobalAlertDialog(false);
	            	
	            	// 수정모드 초기화한다.
	            	//editCheck.edit_barcode_yn = "N";
	            	cancelEditMode();
	            	return;
	            }
	        });
	        AlertDialog dialog = builder.create();
	        dialog.show();
	        return;
		}
	}
	
	public void jobWorkEditBarcodeTreeView(final BarcodeListInfo editBarcodeInfo) {

		//-----------------------------------------------------------
		// JobWork 단계별 작업데이터에 들어갈 정보들임.
        //-----------------------------------------------------------
		String selectBarcode = getEditBarcodeInfo().getBarcode();

		Gson gson = new Gson();
		String editJsonString = gson.toJson(editBarcodeInfo);
		
		//-----------------------------------------------------------
		// 스캔한 설비의 수정전 상위바코드를 Set한다.
        //-----------------------------------------------------------
		editBarcodeInfo.setuFacCd(getEditBarcodeInfo().getuFacCd());
		editBarcodeInfo.setServerUFacCd(getEditBarcodeInfo().getServerUFacCd());
		editBarcodeInfo.setDeviceId(getEditBarcodeInfo().getDeviceId());
		
		TreeNodeInfo<Long> oldTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(getEditTreeKey());
		if (oldTreeNode != null) {
			
			// 수정전 바코드의 직계 하위바코드를 조회하여 하위바코드의 New 상위바코드로 Set한다.
			if (oldTreeNode.isWithChildren()) {
				List<Long> childKeys = getBarcodeTreeAdapter().getSubTreeList(oldTreeNode.getId());
				for (Long childKey : childKeys) {
					BarcodeListInfo childBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(childKey);
					if (childBarcodeInfo != null) {
						childBarcodeInfo.setuFacCd(editBarcodeInfo.getBarcode());
						childBarcodeInfo.setServerUFacCd(editBarcodeInfo.getBarcode());
						childBarcodeInfo.setDeviceId(editBarcodeInfo.getDeviceId());
					}
				}
			}
			
			getBarcodeTreeAdapter().setBarcodeListInfo(oldTreeNode.getId(), editBarcodeInfo);
			getBarcodeTreeAdapter().refresh();
			
			
			//-----------------------------------------------------------
			// TreeView의 바닥 Summary 보여준다.
			//-----------------------------------------------------------
			showSummaryCount();
			
			//-----------------------------------------------------------
			// 화면 초기 Setting후 변경여부 Flag
			//-----------------------------------------------------------
	    	GlobalData.getInstance().setChangeFlag(true);
		}
		
    	//-----------------------------------------------------------
    	// 수정모드를 다시 초기화 한다.
    	//-----------------------------------------------------------
		cancelEditMode();
		
		//-----------------------------------------------------------
    	// 단계별 작업을 여기서 처리한다.
    	//-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
			GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
		} else {
			try {
				GlobalData.getInstance().getJobActionManager().initItemParameter();
				GlobalData.getInstance().getJobActionManager().addItemParameter("selectBarcode", selectBarcode);
				GlobalData.getInstance().getJobActionManager().addItemParameter("editJsonString", editJsonString);
				GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_EDIT);
			} catch (ErpBarcodeException e) {
				e.printStackTrace();
			}
		}
	}
    
    // 형상구성 가능한 상위바코드를 선택한다.
    private String getCheckParentBarcodeInfo(BarcodeListInfo barcodeInfo) {
    	List<BarcodeListInfo> parentBarcodeInfos = new ArrayList<BarcodeListInfo>(); 

    	Long selectedKey = getBarcodeTreeAdapter().getSelected();
    	if (selectedKey == null) return ""; 
    	
    	TreeNodeInfo<Long> selectedTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(selectedKey);
    	if (selectedTreeNode == null) return "";
    	
    	BarcodeListInfo selectBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(selectedTreeNode.getId());
		if (selectBarcodeInfo != null) {
			parentBarcodeInfos.add(selectBarcodeInfo);
			
			
			TreeNodeInfo<Long> parentTreeNode = selectedTreeNode;
			// Tree Level은 0부터 시작하므로 아래와 같이.
			//   만약 (selectedTreeNode.getLevel() == 4) 이면 0~3Level까지 부모 Node를 처리한다.
	    	for (int i=0; i<selectedTreeNode.getLevel(); i++) {
	    		parentTreeNode = getBarcodeTreeAdapter().getParentTreeNodeInfo(parentTreeNode.getId());
				// 부모TreeNode가 있을때까지 조회한다.
				if (parentTreeNode == null) {
					break;
				} else {
					BarcodeListInfo parentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentTreeNode.getId());
					if (parentBarcodeInfo != null) {
						parentBarcodeInfos.add(parentBarcodeInfo);
					}
				}
			}
		}

		String parentBarcode = "";
		//String partType_Scan = barcodeInfo.getPartType();
		for (int i=0; i<parentBarcodeInfos.size(); i++) {
			BarcodeListInfo parentBarcodeInfo = parentBarcodeInfos.get(i);
			
			parentBarcode = compareTargetHierarchy(barcodeInfo, parentBarcodeInfo);
			if (!parentBarcode.isEmpty()) return parentBarcode;
		}
		
		//-----------------------------------------------------------
		// 인계, 인수, 시실등록은 장치바코드 및으로 처리 되어야 한다.
		// 스캔된 설비바코드 형상중 최상위 Node의 바코드 정보를 가져온다.
		//BarcodeListInfo firstBarcodeInfo = parentBarcodeInfos.get(0);
		// 납품입고(병렬 처리 가능) 또는 U-U-U-U, E-E-E-E 형상은 허용 
		//if (partType_Scan.equals("U") && firstBarcodeInfo.getPartType().equals("U")  
		//	|| partType_Scan.equals("E") && firstBarcodeInfo.getPartType().equals("E") ) {
		//	return "add";
		//}
		
	    return "";
    }
    
	// DR-2014-50530 : E-U 형상 구성 가능하게 변경 - request by 정진우 팀장님 2014.02.01 - modify by 류성호 2014.05.08
    public String compareTargetHierarchy(BarcodeListInfo addBarcodeInfo, BarcodeListInfo targetBarcodeInfo) {
    	String partType_Scan = addBarcodeInfo.getPartType();
		String partType_Parent = targetBarcodeInfo.getPartType();
		
		if (partType_Scan.equals("R")) {
			if (partType_Parent.equals("D")) {
				return targetBarcodeInfo.getBarcode();
			}
		} else if (partType_Scan.equals("S")) {
			if (partType_Parent.equals("R") || partType_Parent.equals("D")) {
				return targetBarcodeInfo.getBarcode();
			}
		} else if (partType_Scan.equals("E")) {
			if (partType_Parent.equals("D")) {
				return targetBarcodeInfo.getBarcode();
			}
		} else if (partType_Scan.equals("U")) {
			if (partType_Parent.equals("R") || partType_Parent.equals("S") // || partType_Parent.equals("E") -> E-U 보류 - request by 박장수 : 2014.05.14 
					|| partType_Parent.equals("U") && ((TransferActivity) getActivity()).isUUCheck()) {
				return targetBarcodeInfo.getBarcode();
			}
		}
		return "";
    }
}
