package com.ktds.erpbarcode.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.barcode.BarcodeTreeAdapter;
import com.ktds.erpbarcode.barcode.ChildTreeSearch;
import com.ktds.erpbarcode.barcode.PDABarcodeService;
import com.ktds.erpbarcode.barcode.SAPBarcodeService;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;
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

public class TreeScanTreeFragment extends Fragment {

	private static final String TAG = "TreeScanTreeFragment";
	
	private TreeStateManager<Long> mTreeManager = null;
	private TreeViewList mBarcodeTreeView;
	private BarcodeTreeAdapter mBarcodeTreeAdapter;
	private RelativeLayout mBarcodeProgress;
	
	private FindMoveRequestDataInTask mFindMoveRequestDataInTask;
	private TextView mTotalCountText;
	
	
	private BreakDownCheckInTask mBreakDownCheckInTask;
	private String changeBarcode;
	
    public TreeScanTreeFragment() { }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.management_treescantree_fragment, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		setLayout(savedInstanceState);
		
		//-----------------------------------------------------------
        // 작업관리에서 호출이 아니면 실행한다.
        //-----------------------------------------------------------
		if (GlobalData.getInstance().getJobActionManager().getWorkStatus() != JobActionManager.JOB_WORKING) {
			if (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)") || GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
	    		getMoveRequestData("", "");
	    	}
			showSummaryCount();
		}
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
		
		mBarcodeTreeView = (TreeViewList) getActivity().findViewById(R.id.treescantree_treeView);
		mBarcodeTreeView.setAdapter(mBarcodeTreeAdapter);
		mBarcodeTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "mBarcodeTreeView onItemClick   position==>" + position);

				final BarcodeListInfo model = getBarcodeTreeAdapter().getBarcodeListInfo(id);
				if (model == null) return;
				
				//---------------------------------------------------
				// 선택한 바코드 위치로 이동.
				//---------------------------------------------------
				if (getEditMode() == TreeScanActivity.EDIT_MODE_MOVE) {
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

		mTotalCountText = (TextView) getActivity().findViewById(R.id.treescantree_totalCount);
		
		// 바코드스캔시 조회때만 사용.
        mBarcodeProgress = (RelativeLayout) getActivity().findViewById(R.id.treescantree_barcodeProgress);
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
    	((TreeScanActivity) getActivity()).setBarcodeProgressVisibility(show);
    }

    public boolean isBarcodeProgressVisibility() {
    	return ((TreeScanActivity) getActivity()).isBarcodeProgressVisibility();
    }
    
    public void initScreenTreeFragment() {
    	if (getBarcodeTreeAdapter() != null) {
    		getBarcodeTreeAdapter().itemClear();
    	}
    	
    	showSummaryCount();
    }
    
    private void choiceBarcodeDataDisplay(BarcodeListInfo model) {
    	TreeScanActivity activity = (TreeScanActivity) getActivity();
		activity.choiceBarcodeDataDisplay(model);
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
		return ((TreeScanActivity) getActivity()).getEditMode();
	}

    /**
     *  EDIT_MODE 상태
     */
	public void setEditMode(int editMode) {
		((TreeScanActivity) getActivity()).setEditMode(editMode);
	}
	
	/**
     *  EDIT_MODE TreeKey
     */
	public Long getEditTreeKey() {
		return ((TreeScanActivity) getActivity()).getEditTreeKey();
	}

	/**
     *  EDIT_MODE TreeKey
     */
	public void setEditTreeKey(Long editTreeKey) {
		((TreeScanActivity) getActivity()).setEditTreeKey(editTreeKey);
	}
	
	/**
     * 수정모드 취소.
     */
	public void cancelEditMode() {
		((TreeScanActivity) getActivity()).cancelEditMode();
	}
	
	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public BarcodeListInfo getEditBarcodeInfo() {
		return ((TreeScanActivity) getActivity()).getEditBarcodeInfo();
	}

	/**
     *  EDIT_MODE BarcodeListInfo
     */
	public void setEditBarcodeInfo(BarcodeListInfo editBarcodeInfo) {
		((TreeScanActivity) getActivity()).setEditBarcodeInfo(editBarcodeInfo);
	}
	
	/**
     * 대상 바코드를 타켓 바코드 자식노드로 이동한다.
     * 
     * @param selectedKey
     * @param targetKey
     */
	public void moveSubTree(final Long selectedKey, final Long targetKey) {
		((TreeScanActivity) getActivity()).moveSubTree(selectedKey, targetKey);
	}
	
	/**
	 * 선택한 설비를 타켓설비로 이동한다.
	 * 
	 * @param targetKey     이동될 타켓  Key
	 * @param model         선택한 BarcodeListInfo
	 */
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
		
		//-----------------------------------------------------------
		// Sound를 Play한다.
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
    
    public int getUFacInputbarVisibility() {
    	return ((TreeScanActivity) getActivity()).getUFacInputbarVisibility();
    }
    
    public EditText getUFacCdText() {
    	return ((TreeScanActivity) getActivity()).getUFacCdText();
    }
	
    /**
     * Tree에 설비바코드가 있는지 체크한다.
     * 
     * @param locCode
     * @param deviceId
     * @param barcode
     * @param iFlag
     * @param onlyFacFlag
     */
	public void doChkScan(String locCode, String deviceId, String barcode, String iFlag, boolean onlyFacFlag) {
		Log.i(TAG, "doChkScan   barcode==>" + barcode);
		
        // 실장 - 상위바코드와 중복 체크
    	if (getUFacInputbarVisibility() == View.VISIBLE) {
    		String UFacCd = getUFacCdText().getText().toString().trim();
    		if (!UFacCd.isEmpty()) {
    			if (UFacCd.equals(barcode)) {
    				GlobalData.getInstance().showMessageDialog(
    						new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
    	        	return;
    			}
    		}
    	}
		
		TreeNodeInfo<Long> barcodeTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(barcode);
		
		if (barcodeTreeNode != null) {
			Log.i(TAG, "doChkScan   thisBarcodeKey==>" + barcode+"("+barcodeTreeNode.getId()+"|"+barcodeTreeNode.getLevel()+")");
			
			// 스캔한 바코드로  Position Selected 한다.
			getBarcodeTreeAdapter().changeSelected(barcodeTreeNode.getId());
			BarcodeListInfo barcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(barcodeTreeNode.getId());
			int barcodePosition = getBarcodeTreeAdapter().getKeyPosition(barcodeTreeNode.getId());
			mBarcodeTreeView.setSelection(barcodePosition);
			
			if (barcodeInfo.getScanValue().equals("1") || barcodeInfo.getScanValue().equals("2") || barcodeInfo.getScanValue().equals("3")) {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
            	return;
			}
			
			String scanValue = barcodeInfo.getScanValue();
			
			// 접수(팀간) 이 아닐 경우 상위바코드 먼저 스캔 유도..................
			if (!GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {	
				if (!barcodeInfo.getScanValue().equals("1")) {
					// 상위바코드 자료를 검색한다.
					Long parentKey = getBarcodeTreeAdapter().getManager().getParent(barcodeTreeNode.getId());
					if (parentKey != null) {
						BarcodeListInfo parentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
						
						if (parentBarcodeInfo.getScanValue().equals("0")) {
	            			choiceBarcodeDataDisplay(barcodeInfo);
	                    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "상위바코드를 스캔하세요."));
	                    	return;
						}
					}
				}
			}
			// -- 다중처리 불가 체크 

			if (scanValue.equals("0")) barcodeInfo.setScanValue("1");

			
			choiceBarcodeDataDisplay(barcodeInfo);
			
			//-------------------------------------------------------
			// 화면 초기 Setting후 변경여부 Flag
			//-------------------------------------------------------
			GlobalData.getInstance().setChangeFlag(true);
			GlobalData.getInstance().setSendAvailFlag(true);
	    	
	    	//-----------------------------------------------------------
			// 단계별 작업을 여기서 처리한다.
			//-----------------------------------------------------------
			if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
				GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
			} else {
				try {
					GlobalData.getInstance().getJobActionManager().setStepItem(JobActionStepManager.JOBTYPE_FAC, "facCd", barcodeInfo.getBarcode());
					if(GlobalData.getInstance().getJobGubun().equals("고장등록")){
						mBreakDownCheckInTask = new BreakDownCheckInTask(barcodeInfo.getBarcode());
						mBreakDownCheckInTask.execute((Void) null);
					}
				} catch (ErpBarcodeException e) {
					e.printStackTrace();
				}
			}
		} else {
            // 온라인이면서 송부취소나 인수일 경우 없으면 오류 처리
            // 송부조직이 달라도 스캔할 경우 송부중 설비라면 표시함으로 변경 - request by 박장수 . 2013.06.24 -> 개발 중
			// DR-2014-37505 송부취소, 접수 시 1000건만 불러오기, 리스트에 없는 접수 대상 건 스캔시 추 - request by 박장수 2014.08.18 -> 2014.08.19 - 류성호...
			if (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)") || GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
				// 송부취소(팀간) -> 이동중 설비 조회해서 add
				getMoveRequestData(barcode, "");
				return;
			}

            Log.i(TAG, "addScanData   Start....");
            addScanData(locCode, deviceId, barcode, iFlag, onlyFacFlag);
		}
	}
	
	public void addScanData(String locCd, String deviceId, String barcode, String iFlag, boolean onlyFacFlag) {

        // 실장 - 상위바코드와 중복 체크
    	if (getUFacInputbarVisibility() == View.VISIBLE) {
    		String UFacCd = getUFacCdText().getText().toString().trim();
    		if (!UFacCd.isEmpty()) {
    			if (UFacCd.equals(barcode)) {
    				GlobalData.getInstance().showMessageDialog(
    						new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
    	        	return;
    			}
    		}
    	}
		
		if (!barcode.isEmpty()) {
			Long barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(barcode);
			if (barcodeKey != null) {
    			getBarcodeTreeAdapter().changeSelected(barcodeKey);
    			mBarcodeTreeView.setSelection(getBarcodeTreeAdapter().getKeyPosition(barcodeKey));
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
    			return;
    		}
		}
		
		if (!SessionUserData.getInstance().isOffline()) {
			if (GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'송부' 하지 않은 설비는\n\r'접수' 하실 수 없습니다."));
				return;
			}
		}

		if (onlyFacFlag) {
			getSAPBarcodeInfos(locCd, deviceId, barcode);
		} else {
			getSingleServerBarcodeInfos(barcode);
		}
	}
	
	/**
	 * 작업바코드 ListView 건수 Bar
	 */
    public void showSummaryCount() {
    	if (getBarcodeTreeAdapter() == null) 
    		return;
    	
    	int DB_RCount = getBarcodeTreeAdapter().getDBPartTypeCount("R");
    	int DB_SCount = getBarcodeTreeAdapter().getDBPartTypeCount("S");
    	int DB_UCount = getBarcodeTreeAdapter().getDBPartTypeCount("U");
    	int DB_ECount = getBarcodeTreeAdapter().getDBPartTypeCount("E");
    	int DB_TCount = DB_RCount + DB_SCount + DB_UCount + DB_ECount;
    	String DBCount = "R(" + DB_RCount + ") S(" + DB_SCount + ") U(" + DB_UCount + ") E(" + DB_ECount + ") Total:" + DB_TCount + "건";
    	
    	mTotalCountText.setText(DBCount);
    }
	
	public void getSAPBarcodeInfos(String locCode, String deviceId, String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
		if (isBarcodeProgressVisibility()) return;
		setBarcodeProgressVisibility(true);
		SAPBarcodeService sapBarcodeInfoService = new SAPBarcodeService(new SAPBarcodeInfoHandler(barcode));
		sapBarcodeInfoService.search(locCode, deviceId, barcode);
	}
	
	/**
	 * SAPBarcodeInfo 정보 조회 결과 Handler
	 */
	private class SAPBarcodeInfoHandler extends Handler {
		String _Barcode = "";

		public SAPBarcodeInfoHandler(String barcode) {
			_Barcode = barcode;
		}
		
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
            switch (msg.what) {
            case SAPBarcodeService.STATE_SUCCESS :
            	Log.i(TAG, "SAPBarcodeInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> sapItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);

        		
                AgoCheck agoCheck = new AgoCheck();
                if (!_Barcode.isEmpty() && !_Barcode.equals(sapItems.get(0).getBarcode())) {
                	agoCheck.change_barcode_yn = "Y";
                } else {
            		agoCheck.change_barcode_yn = "N";
            	}
                agoCheck.barcodeItems = sapItems;
                if (GlobalData.getInstance().getJobGubun().equals("납품취소")) {
                	agoCheck.goto_barcode = _Barcode;
                }
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
    	public LocBarcodeInfo locBarcodeInfo = ((TreeScanActivity) getActivity()).getThisLocCodeInfo();		// 스캔한 위치바코드 
    	public String check_org_yn = null;
    	public String check_host_yn = null;
    	public String check_org_message = "";
    	public String goto_barcode = "";
    	public String auto_parallel_hierarchy_yn = null;
    	public String auto_parent_barcode = "";
    }

    private void agoBarcodeInfosListView(final AgoCheck agoCheck) {
    	
    	BarcodeListInfo firstBarcodeInfo = agoCheck.barcodeItems.get(0);

    	TreeNodeInfo<Long> thisTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(firstBarcodeInfo.getBarcode());
    	//---------------------------------------------------------------
    	// 스캔한 바코드와 조회된 바코드가 다를때 중복체크 한번더 한다.
    	//---------------------------------------------------------------
    	if (agoCheck.change_barcode_yn.equals("Y")) {
    		//-----------------------------------------------------------
        	// 스캔한 바코드와 서버에서 조회된 바코드가 상이한 경우 때문에 
        	// 여기서 중복체크 한번더 한다.
        	//-----------------------------------------------------------
    		if (thisTreeNode != null) {
    			int barcodePosition = getBarcodeTreeAdapter().getKeyPosition(thisTreeNode.getId());
    			getBarcodeTreeAdapter().changeSelected(thisTreeNode.getId());
    			mBarcodeTreeView.setSelection(barcodePosition);
    			GlobalData.getInstance().showMessageDialog(
    					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + firstBarcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
    			return;
    		}
    	}

    	// 스캔한 위치코드
		String locCd = "";
		if (agoCheck.locBarcodeInfo!=null) locCd = agoCheck.locBarcodeInfo.getLocCd();
    	// 창고 실장이면 설비바코드의 위치도 창고이어야 함.. request by 오종윤 2012.09.13
        if (GlobalData.getInstance().getJobGubun().equals("실장")) {
            if (locCd.startsWith("VS") && !firstBarcodeInfo.getLocCd().startsWith("VS")) {
            	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "위치바코드 유형이 '창고'인\n\r설비바코드를 스캔하세요."));
            	return;
            }
        }
		
		if (GlobalData.getInstance().getJobGubun().equals("탈장")) {
			if (firstBarcodeInfo.getLocCd().startsWith("VS")) {
				GlobalData.getInstance().showMessageDialog(
						new ErpBarcodeException(-1, "'창고(" + firstBarcodeInfo.getLocCd() + ")'\n\r설비바코드는\n\r'" + GlobalData.getInstance().getJobGubun() + "' 작업을\n\r하실 수 없습니다."));
    			return;
			}
		}
		
		if (GlobalData.getInstance().getJobGubun().startsWith("송부(팀간)")) {
			for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
				BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
				
				if (!barcodeInfo.getFacStatus().equals("0100") && !barcodeInfo.getFacStatus().equals("0110")) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'예비/유휴'상태가 아닌 설비는\n\r'" + GlobalData.getInstance().getJobGubun() + "' 작업을\n\r하실 수 없습니다."));
	            	return;
				}
			}
		}
		
		if(GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)") || GlobalData.getInstance().getJobGubun().equals("형상해제(창고내)")){
			for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
				BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
				
				System.out.println("LocCd>>>>>>>>>>>>>>" + ((TreeScanActivity)getActivity()).isLocCd());
				System.out.println("LocCd>>>>>>>>>>>>>>" + barcodeInfo.getLocCd());
				System.out.println("LocCd>>>>>>>>>>>>>>" + barcodeInfo.getPartTypeCode());
				System.out.println("LocCd>>>>>>>>>>>>>>" + barcodeInfo.getOrgName());
				
				if(GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)")){
					String tempLocBarcode = ((TreeScanActivity)getActivity()).isLocCd();
					if(!tempLocBarcode.equals(barcodeInfo.getLocCd())){
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "설비바코드의 위치가 스캔한 위치바코드와 다릅니다. 입고(팀내) 작업 후 형상구성하세요."));
		            	return;
					}
					
					if(barcodeInfo.getPartTypeCode().equals("")){
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'단품'은 형상구성 대상 설비가 아닙니다."));
		            	return;
					}
				}
				
				if (!barcodeInfo.getFacStatus().equals("0100") && !barcodeInfo.getFacStatus().equals("0110") && !barcodeInfo.getFacStatus().equals("0020")) {
					String msg = "";
					if(GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)")){
						msg = "''유휴/예비/납품입고' 상태만 \n\r형상구성이 가능합니다.";
					}else{
						msg = "''유휴/예비/납품입고' 상태만 \n\r형상해제가 가능합니다.";
					}
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, msg));
	            	return;
				}
				
				String tempBarcode = barcodeInfo.getLocCd();
				if (!tempBarcode.startsWith("VS") && tempBarcode.length() > 18 && !tempBarcode.substring(17).equals("0000")) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "'베이' 에 위치해 있는 설비로는 '" + GlobalData.getInstance().getJobGubun() + "'\n작업을 하실 수 없습니다."));
	            	return;
	        	}
				
				if(agoCheck.barcodeItems.size() > 1){
					for(int index = 0; index < agoCheck.barcodeItems.size(); index++){
						String barcode = agoCheck.barcodeItems.get(index).getBarcode();
						Long barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(barcode);
						if (barcodeKey != null) {
			    			getBarcodeTreeAdapter().changeSelected(barcodeKey);
			    			mBarcodeTreeView.setSelection(getBarcodeTreeAdapter().getKeyPosition(barcodeKey));
			    			GlobalData.getInstance().showMessageDialog(
			    					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + barcode, BarcodeSoundPlay.SOUND_DUPLICATION));
			    			return;
			    		}
					}
				}
			}
        }
		
		// 병렬처리 가능 여부 체크
		if (agoCheck.auto_parallel_hierarchy_yn == null) {
			agoCheck.auto_parallel_hierarchy_yn = "Y";			// 병렬 처리 가능 - 형상 구성 하지 않음
			
			//-------------------------------------------------------------
			// 여러 설비 유효성 체크.
			// 고장등록-김소연과장 멀티 허용
			// 입고(팀내), 출고(팀내), 탈장, 송부(팀간), 설비상태변경, 고장, 철거 병렬 처리 허용 및 형상 구성 하지 않음 - request by 정진우팀장님 2014.01.01 
			//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobGubun().equals("납품입고") && ((TreeScanActivity)getActivity()).isHierachyCheck() || GlobalData.getInstance().getJobGubun().equals("실장") || GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)"))
			{
				agoCheck.auto_parallel_hierarchy_yn = "N";	// 형상 구성 및 실장일 경우 병렬 처리 불가 처리
				
				Long thisSelectedKey = null;
	        	if (getBarcodeTreeAdapter().getCount() == 0) {
	        		thisSelectedKey = null;
	        	} else {
	        		thisSelectedKey = getBarcodeTreeAdapter().getSelected();
		        	if (thisSelectedKey != null) {
		        		// 자동으로 선택된 형상의 가능한 상위바코드를 선택하여 준다. 없으면 병렬처리이므로 오류 처리
		    			agoCheck.auto_parent_barcode = getCheckParentBarcodeInfo(firstBarcodeInfo);
		    			
		    			if (agoCheck.auto_parent_barcode.isEmpty()) {
		    				GlobalData.getInstance().showMessageDialog(
		    						new ErpBarcodeException(-1, "여러 설비를 동시에 '" + GlobalData.getInstance().getJobGubun() + "' 작업을\n\r하실 수 없습니다.\n\r먼저 전송 후 다시 시도하세요."));
		    	        	return;
		    			}
		        	}
	        	}
			}
		}
		// -- 병렬처리 가능 여부 체크
		
		//-------------------------------------------------------------
		// 조직변경 여부 Yes/No Dialog
		//-------------------------------------------------------------
		if (agoCheck.check_org_yn == null) {
			boolean DontCheckOperationOrganizationFlag = false;
			if (GlobalData.getInstance().getJobGubun().equals("설비정보") || GlobalData.getInstance().getJobGubun().equals("납품입고") || GlobalData.getInstance().getJobGubun().equals("납품취소") || GlobalData.getInstance().getJobGubun().equals("배송출고") || GlobalData.getInstance().getJobGubun().equals("접수(팀간)")) {
				DontCheckOperationOrganizationFlag = true;
			}
			if (!GlobalData.getInstance().getJobGubun().equals("배송출고")) {
				if (locCd.equals("VS0000000000004110000") || locCd.equals("VS0000000000004120000") || locCd.equals("VS0000000000004130000") || locCd.equals("VS0000000000004140000") || locCd.equals("VS0000000000004160000") || locCd.equals("VS0000000000004180000")) {
					DontCheckOperationOrganizationFlag = true;
				}
			}
            // 유닛이나 쉘프 실장은 전송 직전에 운용조직 변경 여부 물어 본다......................... 상속 받으면 물어 보지 않는다....
            if (GlobalData.getInstance().getJobGubun().equals("실장") || GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)")) {
                String rootPartTypeName = getBarcodeTreeAdapter().getInstConfPartType();
                
               System.out.println("rootPartTypeName         ===========         " + rootPartTypeName);
                
                if (rootPartTypeName.isEmpty()) rootPartTypeName = firstBarcodeInfo.getPartType();
                if (rootPartTypeName.equals("U") || rootPartTypeName.equals("S"))
                {
                    DontCheckOperationOrganizationFlag = true;
                }
            }
            
            // 송부(팀간)도 조직변경 여부 팝업 request by 김희선 - 2014.01.01 DR-2013-56706 - 수정 : 류성호 2014.02.25
            /*
			if (GlobalData.getInstance().getJobGubun().equals("송부(팀간)")) {
				DontCheckOperationOrganizationFlag = true;
			}
			*/
            
            if(agoCheck.check_host_yn == null){
            	if (GlobalData.getInstance().getJobGubun().equals("실장")){
            		for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
            			final BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
            			if(barcodeInfo.getHostYn().equals("N")){
            				final Builder builder = new AlertDialog.Builder(getActivity()); 
            				builder.setIcon(android.R.drawable.ic_menu_info_details);
            				builder.setTitle("알림");
            				TextView msgText = new TextView(getActivity());
            				msgText.setPadding(10, 30, 10, 30);
            				msgText.setText("서버 호스트매핑 완료 후 실장 등록 진행하시기 바랍니다.\n\r자세한사항은 http://itam.kt.com 초기화면 하단 공지사항\n\r‘전사 서버 IT자산관리 정책’을 참고하세요.\n\r문의처: itam@kt.com");
            				msgText.setGravity(Gravity.CENTER);
            				msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            				msgText.setTextColor(Color.BLACK);
            				builder.setView(msgText);
            				builder.setCancelable(false);
            				builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            					public void onClick(DialogInterface dialog, int which) {
            						GlobalData.getInstance().setGlobalAlertDialog(false);
            						if(barcodeInfo.getUsgYn().equals("N")){
            							final Builder builder = new AlertDialog.Builder(getActivity()); 
            							builder.setIcon(android.R.drawable.ic_menu_info_details);
            							builder.setTitle("알림");
            							TextView msgText = new TextView(getActivity());
            							msgText.setPadding(10, 30, 10, 30);
            							msgText.setText("ITAM시스템(itam.kt.com)에 성능정보가 수집이 되고 있지 않습니다.\n\rITAM 로그인 후 > 하단 성능정보 수집\n\r매뉴얼을 참고하시어 현행화 부탁 드립니다.\n\r문의사항 : itam@kt.com");
            							msgText.setGravity(Gravity.CENTER);
            							msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            							msgText.setTextColor(Color.BLACK);
            							builder.setView(msgText);
            							builder.setCancelable(false);
            							builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            								public void onClick(DialogInterface dialog, int which) {
            									GlobalData.getInstance().setGlobalAlertDialog(false);
            									agoCheck.check_host_yn = "Y";
            									agoBarcodeInfosListView(agoCheck);
            								}
            							});
            							AlertDialog dialog_usg = builder.create();
            							dialog_usg.show();
            						}else{
            							agoCheck.check_host_yn = "Y";
            							agoBarcodeInfosListView(agoCheck);
            						}
            					}
            				});
            				AlertDialog dialog_host = builder.create();
            				dialog_host.show();
            				return;
            			}else{
            				if(barcodeInfo.getUsgYn().equals("N")){
            					final Builder builder = new AlertDialog.Builder(getActivity()); 
            					builder.setIcon(android.R.drawable.ic_menu_info_details);
            					builder.setTitle("알림");
            					TextView msgText = new TextView(getActivity());
            					msgText.setPadding(10, 30, 10, 30);
            					msgText.setText("ITAM시스템(itam.kt.com)에 성능정보가 수집이 되고 있지 않습니다.\n\rITAM 로그인 후 > 하단 성능정보 수집\n\r매뉴얼을 참고하시어 현행화 부탁 드립니다.\n\r문의사항 : itam@kt.com");
            					msgText.setGravity(Gravity.CENTER);
            					msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            					msgText.setTextColor(Color.BLACK);
            					builder.setView(msgText);
            					builder.setCancelable(false);
            					builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            						public void onClick(DialogInterface dialog, int which) {
            							GlobalData.getInstance().setGlobalAlertDialog(false);
            							agoCheck.check_host_yn = "Y";
            							agoBarcodeInfosListView(agoCheck);
            						}
            					});
            					AlertDialog dialog = builder.create();
            					dialog.show();
            					return;
            				}
            			}
            		}
            	}
            }
            
            /**
             * 2. 출고/탈장
             * → 조직 변경 하지 않기로 하였습니다.
             *  .조직정보 변경 관련 PDA Message 삭제
             *  .ITEM 정보의  CHKZKOSTL = 'X' 로 PDA I/F 처리
             * 2012.07.26 request by kang
             * 출고(팀내) 다시 조직변경 여부 묻는걸로 변경 - 2013.12.04 request by 정진우
             * 탈장 다시 조직변경 여부 묻는걸로 변경 - 2013.12.06 request by 정진우
             */
			if (DontCheckOperationOrganizationFlag) {
				// 무조건 묻지 않고 조직변경 하지 않도록..............................................
				for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					barcodeInfo.setCheckOrgYn("N");
					if(GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)")){
						barcodeInfo.setCheckOrgYn("");
					}
				}
			} else if (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)")) {
				// 운용조직 다르면 변경여부 선택한다.
				if (!firstBarcodeInfo.getOrgId().equals(SessionUserData.getInstance().getOrgId())) {
					if (firstBarcodeInfo.getOrgName().indexOf("폐지") > 0) {
						agoCheck.check_org_message =  "'" + firstBarcodeInfo.getBarcode() + "' 의 운용조직은\n\r'" + firstBarcodeInfo.getOrgName() 
	    								+ "' 으로 폐지 조직입니다.처리 시 조직은\n\r'" 
	    								+ SessionUserData.getInstance().getOrgName() + "'(으)로\n\r변경됩니다.\n\r처리 하시겠습니까?";

	    			} else {
	    				agoCheck.check_org_message = "'" + firstBarcodeInfo.getBarcode() + "' 의 운용조직은\n\r'" 
										+ firstBarcodeInfo.getOrgName() + "' 입니다.\n\r운용조직을 '" 
										+ SessionUserData.getInstance().getOrgName() + "'(으)로\n\r변경하시겠습니까?";
	    			}
    				checkOrgYesNoDialog(agoCheck);
            		return;
				}
			} else {
				// 운용조직 다르면 변경여부 선택한다.
				
//				if(!GlobalData.getInstance().getJobGubun().equals("형상해제(창고내)")){
					if (!firstBarcodeInfo.getOrgId().isEmpty() &&
							!firstBarcodeInfo.getOrgId().equals(SessionUserData.getInstance().getOrgId())) {
						agoCheck.check_org_message = "'" + firstBarcodeInfo.getBarcode() + "' 의 운용조직은\n\r'" 
										+ firstBarcodeInfo.getOrgName() + "' 입니다.\n\r운용조직을 '" 
										+ SessionUserData.getInstance().getOrgName() + "'(으)로\n\r변경하시겠습니까?";
		        		checkOrgYesNoDialog(agoCheck);
		        		return;
		        	}
//				}
				
			}
		}
		
		// 조직변경 여부 팝업 후 다시 들어와서 리스트 뷰에 추가
    	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
			BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
			
			if(GlobalData.getInstance().getJobGubun().equals("설비상태변경")){
				if(barcodeInfo.getFacStatus().equals("0240")){
					agoCheck.barcodeItems.remove(i);
					continue;
				}
			}

			//-------------------------------------------------------
            // 스캔 바코드 상위, 하위 구분.
			String scanValue = "0";
			if (GlobalData.getInstance().getJobGubun().equals("납품취소")) {
				scanValue = "1";
			} else if (i == 0) {	// 스캔한 설비바코드일 경우
            	Log.i(TAG, "nextSAPBarcodeProcess  2번째바코드 중 1뻔째~~  barcodeInfo.getuFacCd()==>"+barcodeInfo.getuFacCd());

    			if (agoCheck.auto_parent_barcode.isEmpty() || agoCheck.auto_parent_barcode.equals("add")) {
                	scanValue = "3";   // 상위바코드가 없으면 스캔값 "3" 으로 셋팅 후 전송시 "1"로 변경하여 전송.........
                } else {
                	scanValue = "2";   // 상위바코드가 있으면서 스캔한 설비바코드이면 추가
                }
            } else {
            	// 송부취소(팀간)은 하위 모두 스캔한 것으로 간주 - 2014.02.25 류성호
            	if (GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)")) {
            		scanValue = "1";
            	} else {
            		scanValue = "0";       // 불러온 바코드는 "0"
            	}
            }
            Log.i(TAG, "nextSAPBarcodeProcess  scanValue==>"+scanValue);
            barcodeInfo.setScanValue(scanValue);
		}

		// SAPBarcode ListView에 추가한다.
		addBarcodeInfosTreeView(agoCheck);
		
		// 장치ID(mDeviceIdText), 상위바코드mUFacCdText) 활성/비활성화 처리.
		((TreeScanActivity) getActivity()).setTextBoxEnable();
    }
    
    // 운용조직 다른지 체크하여 변경여부 선택한다.
    private void checkOrgYesNoDialog(final AgoCheck agoCheck) {
    	
    	//-----------------------------------------------------------
    	// Yes/No Dialog
    	//-----------------------------------------------------------
    	if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		GlobalData.getInstance().soundPlay(BarcodeSoundPlay.SOUND_NOTIFY);
		final Builder builder = new AlertDialog.Builder(getActivity()); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getActivity());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText(agoCheck.check_org_message);
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
				String orgId="", orgName = "";		// 최초 스캔한 설비의 운용조직
            	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					if (i==0) {
						orgId = barcodeInfo.getOrgId();
						orgName = barcodeInfo.getOrgName();
					}
					// 운용조직변경 여부 물을 때 최초설비의 운용조직으로 바꿔서 Display
					barcodeInfo.setCheckOrgYn("Y");
					if(!GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)") && !GlobalData.getInstance().getJobGubun().equals("형상해제(창고내)")
							&& !GlobalData.getInstance().getJobGubun().equals("탈장")){
						barcodeInfo.setOrgId(orgId);
						barcodeInfo.setOrgName(orgName);
					}
					//agoCheck.barcodeItems.set(i, barcodeInfo);
				}
            	agoCheck.check_org_yn = "Y";
            	agoBarcodeInfosListView(agoCheck);
            }
        });
        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GlobalData.getInstance().setGlobalAlertDialog(false);
            	
				String orgId="", orgName = "";		// 최초 스캔한 설비의 운용조직
            	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
					BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);
					if (i==0) {
						orgId = barcodeInfo.getOrgId();
						orgName = barcodeInfo.getOrgName();
					}
					// 운용조직변경 여부 물을 때 최초설비의 운용조직으로 바꿔서 Display
					barcodeInfo.setCheckOrgYn("N");
					if(!GlobalData.getInstance().getJobGubun().equals("형상구성(창고내)") && !GlobalData.getInstance().getJobGubun().equals("형상해제(창고내)") 
							&& !GlobalData.getInstance().getJobGubun().equals("탈장")){
						barcodeInfo.setOrgId(orgId);
						barcodeInfo.setOrgName(orgName);
					}
					//agoCheck.barcodeItems.set(i, barcodeInfo);
				}
            	agoCheck.check_org_yn = "N";
            	agoBarcodeInfosListView(agoCheck);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }
    
    //입고(팀내), 설비상태변경 불용대기 상태변경시 알림 
    private void stateChangeAlert(){
    	final Builder builder = new AlertDialog.Builder(getActivity()); 
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(getActivity());
		msgText.setPadding(10, 30, 10, 30);
		msgText.setText("<무상교환/수리 확(보증기간 내 설비)>");
		msgText.setGravity(Gravity.CENTER);
		msgText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		msgText.setTextColor(Color.BLACK);
		builder.setView(msgText);
		builder.setCancelable(false);
		builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
    }
    
    private void getSingleServerBarcodeInfos(String barcode) {
		// 설비바코드 스캔 체크 mSingleSAPBarcodeInfoHandler로 바코드 조회내역을 보낸다.
		PDABarcodeService pdaBarcodeService = 
				new PDABarcodeService(getActivity().getApplicationContext(), new ProductToBarcodeInfoInfoHandler());
		// 무조건 서버에서 자재마스터 정보 조회한다.
		try {
			pdaBarcodeService.search(PDABarcodeService.SEARCH_EXPANSION_PRODUCT_TO_BARCODELISTINFO, barcode);
		} catch (ErpBarcodeException e) {
			setBarcodeProgressVisibility(false);
			GlobalData.getInstance().showMessageDialog(e);
			return;
		}
	}
    
    private class ProductToBarcodeInfoInfoHandler extends Handler {
    	//private String _Barcode;
		
        @Override
        public void handleMessage(Message msg) {
        	setBarcodeProgressVisibility(false);
        	
            switch (msg.what) {
            case PDABarcodeService.STATE_SUCCESS:
            	Log.i(TAG, "mProductToBarcodeInfoInfoHandler  SAPBarcodeService.STATE_SUCCESS");
            	String findedMessage = msg.getData().getString("message");
            	List<BarcodeListInfo> pdaItems = BarcodeInfoConvert.jsonArrayStringToBarcodeListInfos(findedMessage);


            	AgoCheck agoCheck = new AgoCheck();
           		agoCheck.change_barcode_yn = "N";		// 자재정보 조회 시 다시 중복체크 할 필요 없음
                agoCheck.barcodeItems = pdaItems;
                agoBarcodeInfosListView(agoCheck);

                break;
            case PDABarcodeService.STATE_NOT_FOUND:
            	String notfoundMessage = msg.getData().getString("message");
            	ErpBarcodeException notfoundException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(notfoundMessage);
            	GlobalData.getInstance().showMessageDialog(notfoundException);
            	break;
            case PDABarcodeService.STATE_ERROR:
            	String errorMessage = msg.getData().getString("message");
            	ErpBarcodeException erpbarException = 
            			ErpBarcodeExceptionConvert.jsonStringToErpBarcodeException(errorMessage);
            	GlobalData.getInstance().showMessageDialog(erpbarException);
                break;
            }
        }
    }
    
    /**
     * 음영지역 작업일때 사용함.
     */
	public void addBarcodeInfosTreeView(String goto_barcode, final List<BarcodeListInfo> barcodeInfos) {

		BarcodeListInfo firstBarcodeInfo = barcodeInfos.get(0);
		
		Long barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(firstBarcodeInfo.getBarcode());
    	if (barcodeKey != null) {
    		getBarcodeTreeAdapter().changeSelected(firstBarcodeInfo.getBarcode());
			getBarcodeTreeView().setSelection(getBarcodeTreeAdapter().getBarcodePosition(firstBarcodeInfo.getBarcode()));  // 선택된 바코드로 커서 이동한다.
			
    		GlobalData.getInstance().showMessageDialog(
					new ErpBarcodeException(-1, "중복 스캔입니다.\n\r\n\r" + firstBarcodeInfo.getBarcode(), BarcodeSoundPlay.SOUND_DUPLICATION));
    		return;
    	}
		
    	//-----------------------------------------------------------
    	// addBarcodeInfosTreeView 에서 필요한 부분만 여기에 적용함.
    	//-----------------------------------------------------------
    	final AgoCheck agoCheck = new AgoCheck();
		agoCheck.barcodeItems = barcodeInfos;
		agoCheck.goto_barcode = goto_barcode;

		if (agoCheck.auto_parallel_hierarchy_yn == null) {
			agoCheck.auto_parallel_hierarchy_yn = "N";
			
			//-------------------------------------------------------------
			// 여러 설비 유효성 체크.
			// 고장등록-김소연과장 멀티 허용
			//-------------------------------------------------------------
			if (GlobalData.getInstance().getJobGubun().equals("납품입고") && !((TreeScanActivity)getActivity()).isHierachyCheck()
					|| GlobalData.getInstance().getJobGubun().equals("입고(팀내)")
					|| GlobalData.getInstance().getJobGubun().equals("고장등록")
					|| GlobalData.getInstance().getJobGubun().equals("철거") ) 
			{
				agoCheck.auto_parallel_hierarchy_yn = "Y";
			}
			
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
	    				GlobalData.getInstance().showMessageDialog(
	    						new ErpBarcodeException(-1, "여러 설비를 동시에 '" + GlobalData.getInstance().getJobGubun() + "' 작업을\n\r하실 수 없습니다.\n\r먼저 전송 후 다시 시도하세요."));
	    	        	return;
	    			}
	        	}
			}
		}
		
		// 조직변경 여부 팝업 후 다시 들어와서 리스트 뷰에 추가
    	for (int i=0; i<agoCheck.barcodeItems.size(); i++) {
			BarcodeListInfo barcodeInfo = agoCheck.barcodeItems.get(i);			
			
			if(GlobalData.getInstance().getJobGubun().equals("설비상태변경")){
				if(barcodeInfo.getFacStatus().equals("0240")){
					agoCheck.barcodeItems.remove(i);
					continue;
				}
			}
			
            //-------------------------------------------------------
            // 스캔 바코드 상위, 하위 구분.
			String scanValue = "0";
			if (GlobalData.getInstance().getJobGubun().equals("납품취소")) {
				scanValue = "1";
			} else if (i == 0) {	// 스캔한 설비바코드일 경우
            	Log.i(TAG, "nextSAPBarcodeProcess  2번째바코드 중 1뻔째~~  barcodeInfo.getuFacCd()==>"+barcodeInfo.getuFacCd());

    			if (agoCheck.auto_parent_barcode.isEmpty() || agoCheck.auto_parent_barcode.equals("add")) {
                	scanValue = "3";   // 상위바코드가 없으면 스캔값 "3" 으로 셋팅 후 전송시 "1"로 변경하여 전송.........
                } else {
                	scanValue = "2";   // 상위바코드가 있으면서 스캔한 설비바코드이면 추가
                }
            } else {
            	scanValue = "0";       // 불러온 바코드는 "0"
            }
            Log.i(TAG, "nextSAPBarcodeProcess  scanValue==>"+scanValue);
            barcodeInfo.setScanValue(scanValue);
		}

		// SAPBarcode ListView에 추가한다.
		addBarcodeInfosTreeView(agoCheck);
		
		// 장치ID(mDeviceIdText), 상위바코드mUFacCdText) 활성/비활성화 처리.
		((TreeScanActivity) getActivity()).setTextBoxEnable();
		
	}
    
    /**
     * 설비내역을 Tree에 추가한다.
     * 
     * @param agoCheck
     */
    private void addBarcodeInfosTreeView(final AgoCheck agoCheck) {
    	
    	final List<BarcodeListInfo> newBarcodeInfos = agoCheck.barcodeItems;
    	
    	//-----------------------------------------------------------
    	// 병렬처리
    	//-----------------------------------------------------------
    	if (agoCheck.auto_parallel_hierarchy_yn.equals("Y")) {
    		// 기존 스캔한 바코드 삭제 처리 
        	// Tree Node에 스캔한 바코드가 있는지 체크.
    		//             스캔한 바코드의 하위 Node까지 체크한다.
        	//-----------------------------------------------------------
    		List<Long> treeItemKeys = getBarcodeTreeAdapter().getAllNextNodeList();
    		List<BarcodeListInfo> presentBarcodeInfos = new ArrayList<BarcodeListInfo>();
    		if (treeItemKeys!=null && treeItemKeys.size()>0) {
    			for (BarcodeListInfo newBarcodeInfo : newBarcodeInfos) {
					for (Long key : treeItemKeys) {
			            BarcodeListInfo treeItem = getBarcodeTreeAdapter().getBarcodeListInfo(key);
						if (treeItem.getBarcode().equals(newBarcodeInfo.getBarcode())) {
							presentBarcodeInfos.add(treeItem);
						}
					}
				}
    		}
    		//-----------------------------------------------------------
			// 스캔한 0번째 바코드와 Tree Node의 체크한 바코드와 다를때만 처리한다.
			//-----------------------------------------------------------
			if (presentBarcodeInfos!=null) {
				for (BarcodeListInfo presentBarcodeInfo : presentBarcodeInfos) {
					if (presentBarcodeInfo.getBarcode().equals(newBarcodeInfos.get(0).getBarcode())) continue;
					
					TreeNodeInfo<Long> presentBarcodeTreeNode = getBarcodeTreeAdapter().getBarcodeTreeNodeInfo(presentBarcodeInfo.getBarcode());
					if (presentBarcodeTreeNode != null) {
						TreeNodeInfo<Long> topTreeNode = presentBarcodeTreeNode;
						
						// Tree Level은 0부터 시작하므로 아래와 같이.
				    	for (int j=0; j<presentBarcodeTreeNode.getLevel(); j++) {
				    		topTreeNode = getBarcodeTreeAdapter().getParentTreeNodeInfo(topTreeNode.getId());
				    	}
				    	
				    	//-----------------------------------------------------------
						// 스캔된 바코드가 존재하는 Node에 해당하는 최상위 Node의 하위 Node까지 모두 삭제한다. 
						//-----------------------------------------------------------
				    	getBarcodeTreeAdapter().removeNode(topTreeNode.getId());	
				    	
						//-----------------------------------------------------------
						// 스캔된 걸로 처리함.
						//-----------------------------------------------------------
				    	for (BarcodeListInfo newBarcodeInfo : newBarcodeInfos) {
							if (presentBarcodeInfo.getBarcode().equals(newBarcodeInfo.getBarcode())) {
								newBarcodeInfo.setScanValue("1");
								newBarcodeInfo.setCheckOrgYn(presentBarcodeInfo.getCheckOrgYn());	// 조직변경여부도 셋팅함 
							}
				    	}
					}
				}
			}
    		// -- 기존 스캔한 바코드 삭제 처리

    		getBarcodeTreeAdapter().addItemsNotClear(newBarcodeInfos);
    		Long barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(newBarcodeInfos.get(0).getBarcode());
    		if (barcodeKey != null) {
    			getBarcodeTreeAdapter().changeSelected(barcodeKey);
    			int barcodePosition = getBarcodeTreeAdapter().getKeyPosition(barcodeKey);
				mBarcodeTreeView.setSelection(barcodePosition);  // 선택된 바코드로 커서 이동한다.
				
            	choiceBarcodeDataDisplay(newBarcodeInfos.get(0));
    		}
    	} else {
    		// 단일 처리 - hierarchy 구성
        	Long thisSelectedKey = null;
        	if (getBarcodeTreeAdapter().getCount() == 0) {
        		thisSelectedKey = null;
        	} else {
        		thisSelectedKey = getBarcodeTreeAdapter().getSelected();
        	}
        	
        	if (thisSelectedKey == null) {
            	// ListView에 추가한다.
            	getBarcodeTreeAdapter().addItemsNotClear(newBarcodeInfos);
            	
            	
            	//---------------------------------------------------
            	// 납품취소일때 스캔한 바코드로 이동처리한다.
            	//---------------------------------------------------
            	Long barcodeKey = null;
            	if (agoCheck.goto_barcode.isEmpty()) {
            		barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(newBarcodeInfos.get(0).getBarcode());
            	} else {
            		barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(agoCheck.goto_barcode);
            	}
            	if (barcodeKey != null) {
            		getBarcodeTreeAdapter().changeSelected(barcodeKey);
    				mBarcodeTreeView.setSelection(getBarcodeTreeAdapter().getKeyPosition(barcodeKey));  // 선택된 바코드로 커서 이동한다.
                	choiceBarcodeDataDisplay(getBarcodeTreeAdapter().getBarcodeListInfo(barcodeKey));
            	}

            } else {
            	TreeNodeInfo<Long> selectedTreeNode = getBarcodeTreeAdapter().getTreeNodeInfo(thisSelectedKey);
                if (selectedTreeNode != null) {
                	//BarcodeListInfo selectedBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(thisSelectedKey);
                	BarcodeListInfo firstBarcodeInfo = newBarcodeInfos.get(0);
                	
                	// 자동으로 선택된 형상의 가능한 상위바코드를 선택하여 준다. 없으면 병렬처리이므로 오류 처리
        			if (agoCheck.auto_parent_barcode.equals("add")) {
        				firstBarcodeInfo.setuFacCd("");
        				firstBarcodeInfo.setServerUFacCd("");
        				newBarcodeInfos.set(0, firstBarcodeInfo);
        				
        				// TreeView에 추가한다.
        				getBarcodeTreeAdapter().addItemsNotClear(newBarcodeInfos);

        			} else {
        				Long parentKey = getBarcodeTreeAdapter().getBarcodeKey(agoCheck.auto_parent_barcode);
        				BarcodeListInfo selectedParentBarcodeInfo = getBarcodeTreeAdapter().getBarcodeListInfo(parentKey);
        				if (selectedParentBarcodeInfo != null) {
        					
        					firstBarcodeInfo.setuFacCd(selectedParentBarcodeInfo.getBarcode());
        					if (selectedParentBarcodeInfo.getPartType().equals("D")) {
        						firstBarcodeInfo.setDeviceId(selectedParentBarcodeInfo.getBarcode());
        					}
                        	newBarcodeInfos.set(0, firstBarcodeInfo);
                        	
        					// 선택된 상위바코드로 Focus를 지정한다.
        					getBarcodeTreeAdapter().changeSelected(parentKey);
                        	Long parentSelectKey = getBarcodeTreeAdapter().getSelected();
                        	selectedTreeNode = getBarcodeTreeAdapter().getManager().getNodeInfo(parentSelectKey);
        				}
        				
        				// ListView에 추가한다.
                    	getBarcodeTreeAdapter().addChildItems(selectedTreeNode.getId(), newBarcodeInfos);
        			}
        			
        			// TreeNode가 닫혀있으면 펼친다.
                	if (!selectedTreeNode.isExpanded()) {
                		getBarcodeTreeAdapter().getManager().expandDirectChildren(selectedTreeNode.getId());
                	}
                	
                	Long barcodeKey = null;
                	if (agoCheck.goto_barcode.isEmpty()) {
                		barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(firstBarcodeInfo.getBarcode());
                	} else {
                		barcodeKey = getBarcodeTreeAdapter().getBarcodeKey(agoCheck.goto_barcode);
                	}
                	if (barcodeKey != null) {
                		getBarcodeTreeAdapter().changeSelected(barcodeKey);
        				mBarcodeTreeView.setSelection(getBarcodeTreeAdapter().getKeyPosition(barcodeKey));  // 선택된 바코드로 커서 이동한다.
                    	choiceBarcodeDataDisplay(firstBarcodeInfo);
                	}
                }
            }
    	}
    	
    	//-----------------------------------------------------------
    	// U-U Check는 무조건 초기화한다.
    	//-----------------------------------------------------------
    	((TreeScanActivity) getActivity()).setUUCheck(false);
    	
    	//-----------------------------------------------------------
    	// 하단 Summary 계산하여 보여준다.
    	showSummaryCount();
    	
    	//-----------------------------------------------------------
	    // 화면 초기 Setting후 변경 여부 Flag를 true
	    //-----------------------------------------------------------
		GlobalData.getInstance().setChangeFlag(true);
		GlobalData.getInstance().setSendAvailFlag(true);
		
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
		
		if(GlobalData.getInstance().getJobGubun().equals("고장등록")){
			mBreakDownCheckInTask = new BreakDownCheckInTask(newBarcodeInfos.get(0).getBarcode());
			mBreakDownCheckInTask.execute((Void) null);
		}
    }

	public void getMoveRequestData(String barcode, String KOSTL) {
		if (mBarcodeProgress.getVisibility() == View.VISIBLE) return;

		if (!barcode.isEmpty() && (barcode.length()<16 || barcode.length()>18)) {
			GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "처리할 수 없는 설비바코드입니다."));
        	return;
        }
		
		setBarcodeProgressVisibility(true);
		
		if (mFindMoveRequestDataInTask == null) {
			mFindMoveRequestDataInTask = new FindMoveRequestDataInTask(barcode, KOSTL);
			mFindMoveRequestDataInTask.execute((Void) null);
		}
	}
	
	/** matsua : test
	 * 고장등록 기준수 초과 체크 
	 */
	public class BreakDownCheckInTask extends AsyncTask<Void, Void, Boolean> {
		private String _barcode = "";
		private JSONArray _jsonResults = null;
		
		private ErpBarcodeException _ErpBarException;
		
		public BreakDownCheckInTask(String barcode) {
			_barcode = barcode;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				_jsonResults = barcodehttp.getBreakDownCheck(_barcode);
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "고장 기준수 초과 체크 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				_jsonResults = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mBreakDownCheckInTask = null;
			setBarcodeProgressVisibility(false);
			if (result) {
				try {
					JSONObject jsonobj = _jsonResults.getJSONObject(0);
					String info = jsonobj.getString("E_INFO");
					int cnt = Integer.parseInt(jsonobj.getString("E_CTL_CNT"));
					
					if(info.equals("X")){
						((TreeScanActivity) getActivity()).breakDownYesNoDialog(_barcode, cnt);
					}
					
				} catch (JSONException e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "부서간이동정보 대입중 오류가 발생했습니다." + e.getMessage()));
					return;
				} catch (Exception e) {
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "부서간이동정보 대입중 오류가 발생했습니다." + e.getMessage()));
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
			mBreakDownCheckInTask = null;
			super.onCancelled();
		}
	}
	
	/**
	 * 부서간이동정보 목록 조회.
	 */
	public class FindMoveRequestDataInTask extends AsyncTask<Void, Void, Boolean> {
		private String _barcode = "";
		private String _KOSTL = "";
		private JSONArray _jsonResults = null;
		
		private ErpBarcodeException _ErpBarException;
		
		public FindMoveRequestDataInTask(String barcode, String KOSTL) {
			_barcode = barcode;
			_KOSTL = KOSTL;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				_jsonResults = barcodehttp.getMoveRequestData(_barcode, _KOSTL);
			} catch (ErpBarcodeException e) {
				Log.i(TAG, "부서간이동정보 서버에 요청중 오류가 발생했습니다. ==>"+e.getErrMessage());
				_ErpBarException = e;
				_jsonResults = null;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mFindMoveRequestDataInTask = null;
			setBarcodeProgressVisibility(false);
			
			if (result) {
				Log.i(TAG, "FindMoveRequestDataInTask  jsonresults.length()==>"+_jsonResults.length());
				
				// 송부취소(팀간) -> 이동중 설비 조회해서 add
				if (!_barcode.isEmpty()) {
					if (_jsonResults.length() == 0) {
						GlobalData.getInstance().showMessageDialog(
								new ErpBarcodeException(-1, "'" + _barcode + "' 는\n\r이동 중인 설비가 아닙니다.\n\r확인 후 처리하시기 바랍니다."));
						return;
					}
				}
				
				List<BarcodeListInfo> newBarcodeInfos = new ArrayList<BarcodeListInfo>();
				for (int i=0;i<_jsonResults.length();i++) {
					try {
						JSONObject jsonobj = _jsonResults.getJSONObject(i);

						String barcode = jsonobj.getString("BARCODE");
						String barcodeName = jsonobj.getString("MAKTX");
						
						String UFacCd = jsonobj.getString("EXBARCODE");
						
						Log.i(TAG, "FindMoveRequestDataInTask  barcode==>"+barcode);
						Log.i(TAG, "FindMoveRequestDataInTask  barcodeName==>"+barcodeName);
						Log.i(TAG, "FindMoveRequestDataInTask  UFacCd==>"+UFacCd);
						
						
						String partTypeCode = jsonobj.getString("PARTTYPE");
						String devType = jsonobj.getString("DEVTYPE");
						String partType = SuportLogic.getPartType(partTypeCode, devType);
						String devTypeName = SuportLogic.getDevTypeName(devType);
						
						String ZPSTATU = jsonobj.getString("NZPSTATU");
						
						// 오류....... DataTable에 NZPSTATU 필드가 없습니다...........
						String FACStatusName = SuportLogic.getFACStatusName(ZPSTATU);
						Log.i(TAG, "FindMoveRequestDataInTask  FACStatusName==>"+FACStatusName);
	                    if (!FACStatusName.equals("이동중")) {
	                    	Log.i(TAG, "FindMoveRequestDataInTask  이동중이 아님"+barcode);
	                    	continue;
	                    }
	                    	
	                    
	                    String deviceId = jsonobj.getString("EXDEVICEID");
	                    String itemNo = jsonobj.getString("ITEMNO");

	                    String sloss = jsonobj.getString("SLOSS");
	                    if (sloss.equals("X") && GlobalData.getInstance().getJobGubun().equals("접수(팀간)"))  
	                    	continue;
	                    
	                    String orgId = jsonobj.getString("EXZKOSTL");       // 운영부서코드
	                    String orgName = jsonobj.getString("EXZKOSTLTXT");  // 운영부서명
	                    String skostl = jsonobj.getString("SKOSTL");        // 접수(팀간) 에서 SelectOrgCodeActivity에서 사용.
	                    String skostlTxt = jsonobj.getString("SKOSTLTXT");  // 접수(팀간) 에서 SelectOrgCodeActivity에서 사용.
	                    
	                    String transNo = jsonobj.getString("TRANSNO");      // docno
	                    
	                    String scanValue = "0";
	                    if (barcode.equals(_barcode)) scanValue = "3";       // 최초 스캔
	                    

						// 이미 스캔한 설비가 리스트에 있으면 삭제 request by 박장수 2013.08.27
						ChildTreeSearch childTreeSearch = new ChildTreeSearch(newBarcodeInfos);
						Set<String> childSets = childTreeSearch.searchChildNode(barcode);
						for (String _barcode: childSets) {
							for (int j = newBarcodeInfos.size()-1;j>=0;j--) {
								if (newBarcodeInfos.get(j).getBarcode().equals(_barcode)) {
									newBarcodeInfos.remove(j);
								}
							}
						}

	                    //-----------------------------------------------------
	                    // 변수 대입.
						BarcodeListInfo barcodeInfo = new BarcodeListInfo();
						barcodeInfo.setJobGubun(GlobalData.getInstance().getJobGubun());
						barcodeInfo.setPartType(partType);
						barcodeInfo.setBarcode(barcode);
						barcodeInfo.setBarcodeName(barcodeName);
						barcodeInfo.setLocCd("");
						barcodeInfo.setuFacCd(UFacCd);
						barcodeInfo.setuFacCdName("");
						
						barcodeInfo.setFacStatus(ZPSTATU);
						barcodeInfo.setFacStatusName(FACStatusName);
						
						barcodeInfo.setDevType(devType);
						barcodeInfo.setDevTypeName(devTypeName);
						barcodeInfo.setDeviceId(deviceId);
						
						barcodeInfo.setScanValue(scanValue);
						barcodeInfo.setCheckValue("");
						barcodeInfo.setWbsNo("");
						barcodeInfo.setCheckOrgYn("N");
						barcodeInfo.setOrgId(orgId);
						barcodeInfo.setOrgName(orgName);

						barcodeInfo.setItemNo(itemNo);
						barcodeInfo.setSloss(sloss);
						barcodeInfo.setTransNo(transNo);
						barcodeInfo.setProductCode("");
						barcodeInfo.setProductName("");
						barcodeInfo.setSkostl(skostl);
						barcodeInfo.setSkostlTxt(skostlTxt);
						
						newBarcodeInfos.add(barcodeInfo);

					} catch (JSONException e) {
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "부서간이동정보 대입중 오류가 발생했습니다." + e.getMessage()));
						return;
					} catch (Exception e) {
						GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "부서간이동정보 대입중 오류가 발생했습니다." + e.getMessage()));
						return;
					}
				}
				
				// 송부취소(팀간) -> 이동중 설비 조회해서 add
				if (!_barcode.isEmpty()) {
					if (newBarcodeInfos.size() == 0) {
						GlobalData.getInstance().showMessageDialog(
								new ErpBarcodeException(-1, "'" + _barcode + "' 는\n\r이동 중인 설비가 아닙니다.\n\r확인 후 처리하시기 바랍니다."));
						return;
					}
				}

				Log.i(TAG, "FindMoveRequestDataInTask  Record Count==>"+newBarcodeInfos.size());
				if (newBarcodeInfos.size() == 0) return;
				
					
				// 송부취소(팀간) -> 이동중 설비 조회해서 add
				if (_barcode.isEmpty()) {
					getBarcodeTreeAdapter().addItems(newBarcodeInfos);
					
					//-------------------------------------------------------------
			    	// 단계별 작업을 여기서 처리한다.
			    	//-------------------------------------------------------------
					if (GlobalData.getInstance().getJobActionManager().getWorkStatus() == JobActionManager.JOB_WORKING) {
						GlobalData.getInstance().getJobActionManager().getJobStepManager().finishedHandler();
					} else {
						if (!SessionUserData.getInstance().isOffline()) {
							try {
								GlobalData.getInstance().getJobActionManager().setStepItem(
										JobActionStepManager.JOBTYPE_INITIAL_TASK, "taskName", "getMoveRequestData");
							} catch (ErpBarcodeException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					// _barcode = 스캔한 이동중 설비
					AgoCheck agoCheck = new AgoCheck();
	                agoCheck.change_barcode_yn = "N";
	                agoCheck.barcodeItems = newBarcodeInfos;
	                agoBarcodeInfosListView(agoCheck);
				}


				// 하단 Summary 계산하여 보여준다.
				showSummaryCount();
				
				
				if (_barcode.isEmpty()) {
					//-----------------------------------------------------------
		    	    // 화면 초기 Setting후 변경 여부 Flag를
					// 최초 조회이기때문에 여기서는 false한다.
		    	    //-----------------------------------------------------------
					GlobalData.getInstance().setChangeFlag(false);
				} else {
					//-----------------------------------------------------------
		    	    // 화면 초기 Setting후 변경 여부 Flag를
					// 바코드로 조회하면 변경여부 여기서는 true한다.
		    	    //-----------------------------------------------------------
					GlobalData.getInstance().setChangeFlag(true);
					GlobalData.getInstance().setSendAvailFlag(true);
				}

			} else {
				Log.i(TAG, _ErpBarException.getErrMessage());
				GlobalData.getInstance().showMessageDialog(_ErpBarException);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mFindMoveRequestDataInTask = null;
			super.onCancelled();
		}
	}
	
    // 형상구성시 가능한 상위바코드를 선택한다.
    public String getCheckParentBarcodeInfo(BarcodeListInfo addBarcodeInfo) {

    	List<BarcodeListInfo> parentBarcodeInfos = new ArrayList<BarcodeListInfo>(); 

    	Long selectedKey = getBarcodeTreeAdapter().getSelected();
    	if (selectedKey == null || GlobalData.getInstance().getJobGubun().equals("납품취소")) return "";		// 납품취소는 형상 구성 하지 않고 무조건 최상위로.... 
    	
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
		String partType_Scan = addBarcodeInfo.getPartType();
		for (int i=0; i<parentBarcodeInfos.size(); i++) {
			BarcodeListInfo parentBarcodeInfo = parentBarcodeInfos.get(i);
			
			parentBarcode = compareTargetHierarchy(addBarcodeInfo, parentBarcodeInfo);
			if (!parentBarcode.isEmpty()) return parentBarcode;
		}
		
		//-----------------------------------------------------------
		// 스캔된 설비바코드 형상중 최상위 Node의 바코드 정보를 가져온다.
		BarcodeListInfo firstBarcodeInfo = parentBarcodeInfos.get(0);
		// 납품입고, 송부취소(팀간)(병렬 처리 가능) 또는 U-U-U-U, E-E-E-E 형상은 허용
		if (GlobalData.getInstance().getJobGubun().equals("납품입고") || GlobalData.getInstance().getJobGubun().equals("송부취소(팀간)")
			|| partType_Scan.equals("U") && firstBarcodeInfo.getPartType().equals("U")  
			|| partType_Scan.equals("E") && firstBarcodeInfo.getPartType().equals("E") ) {
			return "add";
		}
		
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
			if (partType_Parent.equals("R") || partType_Parent.equals("S") //|| partType_Parent.equals("E") -> E-U 보류 - request by 박장수 : 2014.05.14
					|| ((TreeScanActivity) getActivity()).isUUCheck() && partType_Parent.equals("U")) {
				return targetBarcodeInfo.getBarcode();
			}
		}
		return "";
    }

}
