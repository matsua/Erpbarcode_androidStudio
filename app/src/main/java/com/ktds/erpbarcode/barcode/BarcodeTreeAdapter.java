package com.ktds.erpbarcode.barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.treeview.AbstractTreeViewAdapter;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
public class BarcodeTreeAdapter extends AbstractTreeViewAdapter<Long> {
    
    private static final String TAG = "BarcodeTreeAdapter";
    private static final int LEVEL_NUMBER = 10;
    
    private String mJobGubun = "";
    
    private long _id = -1;
    private Long selected = null;
    private List<Long> cacheChildKeys = null;
    private final Map<Long, BarcodeListInfo> mBarcodeMaps = new HashMap<Long, BarcodeListInfo>();
    
    public BarcodeTreeAdapter(final Context context, final TreeStateManager<Long> treeStateManager) {
        super(context, treeStateManager, LEVEL_NUMBER);
        mJobGubun = GlobalData.getInstance().getJobGubun();
    }
    
    private long newId() {
    	if (_id == -1) _id = 1000000000;
    	else _id = _id + 1;

        return _id;
    }
    
    public void changeSelected(final int position) {
        selected = getItemId(position);
        refresh();
    }
    
    public void changeSelected(final Long id) {
        selected = id;
        refresh();
    }
    
    public void changeSelected(final String barcode) {
    	Long barcodeKey = getBarcodeKey(barcode);
    	if (barcodeKey != null) {
    		selected = barcodeKey;
    	}
        refresh();
    }
    
    public void selectLastPosition(){
    	if (getCount()==0) return;
		changeSelected(getItemId(getCount()-1));
		refresh();
    }
    
    public Long getSelected() {
        return selected;
    }
    
    @Override
    public long getItemId(final int position) {
        return getTreeId(position);
    }
    
    public int getKeyPosition(Long key) {
    	List<Long> visibleList = getManager().getVisibleList();
    	for (int i=0; i<visibleList.size(); i++) {
    		Long visibleKey = visibleList.get(i);
    		
    		if (visibleKey == key) return i;
		}
    	return -1;
    }
    
    public int getBarcodePosition(String barcode) {
    	List<Long> visibleList = getManager().getVisibleList();
    	for (int i=0; i<visibleList.size(); i++) {
    		Long key = visibleList.get(i);
    		
    		BarcodeListInfo barcodeInfo = getBarcodeListInfo(key);
    		if (barcodeInfo.getBarcode().equals(barcode)) {
    			return i;
    		}
		}
    	return -1;
    }
    
    public Long getBarcodeKey(String barcode) {
    	for (Long key : getManager().getAllNodeKeys()) {
    		BarcodeListInfo barcodeInfo = getBarcodeListInfo(key);
    		if (barcodeInfo.getBarcode().equals(barcode)) {
    			return key;
    		}
		}
    	return null;
	}
    
    public TreeNodeInfo<Long> getTreeNodeInfo(Long key) {
    	return getManager().getNodeInfo(key);
    }
    
    public TreeNodeInfo<Long> getBarcodeTreeNodeInfo(String barcode) {
    	for (Long key : getManager().getAllNodeKeys()) {
    		BarcodeListInfo barcodeInfo = getBarcodeListInfo(key);
    		if (barcodeInfo.getBarcode().equals(barcode)) {
    			return getTreeNodeInfo(key);
    		}
		}
    	return null;
	}
    
    public TreeNodeInfo<Long> getParentTreeNodeInfo(Long key) {
    	Long parentKey = getManager().getParent(key);
    	if (parentKey != null) {
    		TreeNodeInfo<Long> parentTreeNode = getTreeNodeInfo(parentKey);
    		if (parentTreeNode != null) {
    			return parentTreeNode;
    		}
    	}
    	return null;
    }
    
    public BarcodeListInfo getParentBarcodeListInfo(Long key) {
    	Long parentKey = getManager().getParent(key);
    	if (parentKey != null) {
    		BarcodeListInfo barentBarcodeInfo = getBarcodeListInfo(parentKey);
    		if (barentBarcodeInfo != null) {
    			return barentBarcodeInfo;
    		}
    	}
    	return null;
    }
    
    public BarcodeListInfo getBarcodeListInfo(Long key) {
    	if (key == null) return null;
		return mBarcodeMaps.get(key);
	}
    
    public BarcodeListInfo getBarcodeListInfo(String barcode) {
    	Long key = getBarcodeKey(barcode);
    	if (key != null) {
    		return mBarcodeMaps.get(key);
    	}
    	return null;
	}
    
    public Map<Long, BarcodeListInfo> getAllItems() {
    	return mBarcodeMaps;
    }
    
    public List<Long> getAllNextNodeList() {
    	return getManager().getAllNextNodeList();
    }
    
    public void addItem(final BarcodeListInfo item) {
    	Long newId = newId();
        if (mBarcodeMaps.size() == 0) {
            mBarcodeMaps.put(newId, item);
            addAfterChild(null,  newId, null);
            
        } else {
            mBarcodeMaps.put(newId, item);
            
        	final Long selectedKey = getSelected();
            if (selectedKey == null) {
            	addAfterChild(null,  newId, null);
            } else {
            	Long parentKey = getBarcodeKey(item.getuFacCd());
            	if (parentKey == null) {
            		addAfterChild(null, newId, null);
            	} else {
            		addAfterChild(parentKey, newId, null);
            	}
            }
        }
        
        refresh();
    }
    
    public void addItems(final List<BarcodeListInfo> items) {
    	Map<Long, BarcodeListInfo> itemMaps = arrayItemsToMaps(items);
    	addItems(itemMaps);
    }
    
    private void addItems(final Map<Long, BarcodeListInfo> itemMaps) {
        mBarcodeMaps.clear();
        selected = null;
        addItemsNotClear(itemMaps);
    }
    
    /**
     * 신규로 최상위 TreeNode를 구성한다.
     * 
     * @param items
     */
    public void addItemsNotClear(final List<BarcodeListInfo> items) {
    	addItemsNotClear(arrayItemsToMaps(items));
    }
    
    /**
     * 신규로 최상위 TreeNode를 구성한다.
     * 
     * @param items
     */
    private void addItemsNotClear(final Map<Long, BarcodeListInfo> itemMaps) {
    	Log.d(TAG, "addItemsNotClear   Start...");
        if (itemMaps.size() == 0) return;		// 가져온 데이타
        
        mBarcodeMaps.putAll(itemMaps); 			// mBarcodeMaps -> 트리맵 리스트

        Long beforeKey = (long) 0;
        BarcodeListInfo beforeBarcodeInfo = null;
        
        SortedSet<Long> itemKeys = new TreeSet<Long>(itemMaps.keySet());
        for (Long newKey : itemKeys) {
        	BarcodeListInfo newBarcodeInfo = itemMaps.get(newKey);
            
            Log.d(TAG, "addItemsNotClear   newKey==>" + newKey);
            Log.d(TAG, "addItemsNotClear   getBarcode==>" + newBarcodeInfo.getBarcode());
            Log.d(TAG, "addItemsNotClear   getBarcodeName==>" + newBarcodeInfo.getBarcodeName());
            Log.d(TAG, "addItemsNotClear   getuFacCd==>" + newBarcodeInfo.getuFacCd());
            Log.d(TAG, "addItemsNotClear   getServerUFacCd==>" + newBarcodeInfo.getServerUFacCd());
            
            // 첫번째 바코드 목록이면..
            if (beforeKey == 0) {
            	addAfterChild(null, newKey, null);
            } else {
                if (beforeBarcodeInfo.getBarcode().equals(newBarcodeInfo.getuFacCd())) {
                    addAfterChild(beforeKey, newKey, null);
                } else {
                    //Long parentKey = getManager().getParent(beforeKey);
                	Long parentKey = getBarcodeKey(newBarcodeInfo.getuFacCd());
                	if (parentKey == null) {
                		addAfterChild(null, newKey, null);
                	} else {
                		addAfterChild(parentKey, newKey, null);
                	}
                }
            }
            
            beforeKey = newKey;
            beforeBarcodeInfo = newBarcodeInfo;
        }
        
        refresh();
    }
    
    /**
     * 선택한 Node의 자식 TreeNode를 싱글로 구성한다.
     * 
     * @param items
     */
    public void addChildItem(Long parent, BarcodeListInfo barcode) {
    	Long newId = newId();
        mBarcodeMaps.put(newId, barcode);
        
        if (mBarcodeMaps.size() == 0) {
            addAfterChild(null,  newId, null);
        } else {
            addAfterChild(parent,  newId, null);
        }
        
        refresh();
    }
    
    /**
     * 선택한 Node의 자식 TreeNode를 구성한다.
     * 
     * @param items
     */
    public void addChildItems(final Long parent, final List<BarcodeListInfo> items) {
    	addChildItems(parent, arrayItemsToMaps(items));
    }

    /**
     * 선택한 Node의 자식 TreeNode를 구성한다.
     * 
     * @param items
     */
    private void addChildItems(final Long parent, final Map<Long, BarcodeListInfo> itemMaps) {
    	addChildItems(parent, itemMaps, null);
    }
    
    /**
     * 선택한 Node의 자식 TreeNode를 구성한다.
     * 
     * @param items
     */
    public void addChildItems(final Long parent, final Map<Long, BarcodeListInfo> itemMaps, Long afterChild) {
    	if (parent == null) return;
    	
    	Log.d(TAG, "addChildItems   Start...");
    	
    	final TreeNodeInfo<Long> parentNode = getTreeNodeInfo(parent);
    	if (parentNode == null) return;
    	mBarcodeMaps.putAll(itemMaps);
    	
    	Long beforeKey = parentNode.getId();
    	BarcodeListInfo beforeBarcodeInfo = mBarcodeMaps.get(parentNode.getId());

    	SortedSet<Long> itemKeys = new TreeSet<Long>(itemMaps.keySet());
    	for (Long newKey : itemKeys) {
            BarcodeListInfo newBarcodeInfo = itemMaps.get(newKey);

            if (afterChild != null) {
            	if (itemKeys.first() == newKey) {
                	// 
                } else {
                	afterChild = null;
                }
            }
            
            if (beforeBarcodeInfo.getBarcode().equals(newBarcodeInfo.getuFacCd())) {
                addAfterChild(beforeKey, newKey, afterChild);
            } else {
                Long parentKey = getManager().getParent(beforeKey);
                if (parentKey == null) {
            		addAfterChild(null, newKey, afterChild);
            	} else {
            		addAfterChild(parentKey, newKey, afterChild);
            	}
            }

            beforeKey = newKey;
            beforeBarcodeInfo = newBarcodeInfo;
        }
    	
    	refresh();
    }
    
    /**
     * Tree Manager에서 싱글로 TreeNode를 구성한다.
     * 
     * @param parent
     * @param newChild
     * @param afterChild   선택한 다음 Node순서로 TreeNode를 추가한다.
     */
    private void addAfterChild(final Long parent, final Long newChild, final Long afterChild) {
        getManager().addAfterChild(parent, newChild, afterChild);
    }
    
    /**
     * Tree Manager에서 싱글로 TreeNode를 구성한다.
     * 
     * @param parent
     * @param newChild
     * @param afterChild   선택한 이전 Node순서로 TreeNode를 추가한다.
     */
    //private void addBeforeChild(final Long parent, final Long newChild, final Long beforeChild) {
    //    getManager().addBeforeChild(parent, newChild, beforeChild);
    //}
    
    
    /**
     * 새로운 BarcodeListInfo 정보를 Map에 반영한다.
     * 
     * @param key
     * @param barcodeInfo
     */
    public void setBarcodeListInfo(Long key, BarcodeListInfo barcodeInfo) {
    	mBarcodeMaps.put(key, barcodeInfo);
	}
    
    /**
     * ArrayList<BarcodeListInfo> 형태를 HashMap<Long, BarcodeListInfo> 으로 변환한다.
     *    이때 key는 신규Key는 새로운 번호로 생성된다.
     * 
     * @param items
     * @return
     */
    // ArrayList<BarcodeListInfo> 를 HashMap<Long, BarcodeListInfo> 으로 변환한다.
    public Map<Long, BarcodeListInfo> arrayItemsToMaps(List<BarcodeListInfo> items) {
    	Map<Long, BarcodeListInfo> tempMaps = new HashMap<Long, BarcodeListInfo>();
    	for (BarcodeListInfo barcodeInfo : items) {
    		Long newId = newId();
    		tempMaps.put(newId, barcodeInfo);
    	}
    	return tempMaps;
    }
    
    /**
     * 선택한 바코드는 제외하고, 하위 Node들만 조회한다.
     * 
     * @param key
     */
    public List<Long> getSubTreeList(final Long key) {
    	TreeNodeInfo<Long> treeNode = getTreeNodeInfo(key);
    	if (treeNode == null) return null;
    	cacheChildKeys = new ArrayList<Long>();
    	putChildrenList(treeNode.getId());
    	return cacheChildKeys;
    }
    
    /**
     * 선택한 바코드 포함하여 하위 Node들을 조회한다.
     * 
     * @param key
     */
    public List<Long> getChildrenList(final Long key) {
    	TreeNodeInfo<Long> treeNode = getTreeNodeInfo(key);
    	if (treeNode == null) return null;
    	cacheChildKeys = new ArrayList<Long>();
    	cacheChildKeys.add(key);
    	putChildrenList(treeNode.getId());
    	return cacheChildKeys;
    }
    private void putChildrenList(final Long key) {
    	TreeNodeInfo<Long> treeNode = getTreeNodeInfo(key);
    	if (!treeNode.isWithChildren()) return;
    	
    	// 자식Node가 있으면 자식에 자식이 있는지 추적한다.
    	List<Long> childKeys = getManager().getChildren(treeNode.getId());
    	if (childKeys != null && childKeys.size() > 0) {
    		cacheChildKeys.addAll(childKeys);
    	}
    	
        for (final Long childKey : childKeys) {
        	putChildrenList(childKey);
        }
    }
    
    /**
     * 선택한 노드와 하위노드들을 모두 삭제한다.
     * 
     * @param key
     */
    public void removeNode(final long key) {
    	// mBarcodeMaps 하위 TreeNode 정보를 모두 삭제한다.
    	removeBarcodeMaps(key);
    	
    	// TreeView 하위 TreeNode를 모두 삭제한다.
        getManager().removeNodeRecursively(key);
        
        selected = null;		// 선택된 key -1로 초기화
        
        refresh();
    }
    private boolean removeBarcodeMaps(final Long key) {
        boolean visibleNodeChanged = false;
        List<Long> childKeys = getManager().getChildren(key);
        for (final Long childKey : childKeys) {
            if (removeBarcodeMaps(childKey)) {
                visibleNodeChanged = true;
            }
        }
        TreeNodeInfo<Long> treeNode = getTreeNodeInfo(key);
        if (treeNode != null) {
        	mBarcodeMaps.remove(key);
            if (treeNode.isWithChildren()) {
                visibleNodeChanged = true;
            }
        }
        return visibleNodeChanged;
    }
    
    public void itemClear() {
    	_id = -1;
    	selected = null;
    	getManager().clear();
    	mBarcodeMaps.clear();
    }
    
    private String getNodeText(BarcodeListInfo model) {
    	String nodeText = "";
 
    	if (mJobGubun.startsWith("현장점검")) {
        	nodeText = model.getPartType() + ":"
    				+ model.getBarcode() + ":"
    				+ model.getFacStatusName() + ":"
    				+ (model.getProductName().isEmpty() ? model.getBarcodeName() : model.getProductName()) + ":"
    				+ model.getuFacCd() + ":"
    				+ model.getLocCd() + ":" 
    				+ model.getScanValue() + ":" 
    				+ (model.getOrgId().isEmpty() ? "" : model.getCheckOrgYn() + "_" + model.getOrgId() + "_" + model.getOrgName()) + ":"
    				+ model.getCheckValue();
    	} else if (mJobGubun.startsWith("임대단말실사")) {
    		nodeText = model.getBarcode();
    	} else {
        	if (model.getPartType().equals("L")) {
        		nodeText = model.getPartType() + ":" 
    					+ model.getBarcode(); 
    					//+ model.getBarcodeName();

        	} else if (model.getPartType().equals("D")) {
        		String eqshape = "R";
        		
        		nodeText = model.getPartType() + ":" 
    					+ model.getBarcode() + ":" 
    					+ model.getBarcodeName() + ":" 
    					+ model.getItemCode() + ":" 
    					+ model.getItemName() + ":" 
    					+ eqshape + ":"
    					+ model.getLocCd() + ":" 
    					+ model.getDeviceStatusName();
        	} else {
        		String checkOrgInfo = model.getCheckOrgYn() + "_" ;
    			if (!model.getOrgId().isEmpty()) {
    				checkOrgInfo += model.getOrgId() + "_" + model.getOrgName();
    			}
        		nodeText = model.getPartType() + ":" 
    					+ model.getBarcode() + ":" 
    					+ model.getFacStatusName() + ":"
    					+ (model.getProductName().isEmpty() ? model.getBarcodeName() : model.getProductName()) + ":"
    					+ model.getDevTypeName() + ":" 
    					+ model.getDeviceId() + ":" 
    					+ model.getScanValue() + ":" 
    					+ checkOrgInfo + ":";
        		if (!mJobGubun.startsWith("실장")) {
        			if (model.getComment().isEmpty()) {
        				nodeText += model.getWbsNo();
        			} else {
        				nodeText += model.getWbsNo() + ":" + model.getComment();
        			}
        		}
        	}
    	}

		return nodeText;
	}

    public String getDescription(final BarcodeListInfo model) {
        String nodeText = getNodeText(model);
        
        //final Integer[] hierarchy = getManager().getHierarchyDescription(id);
        //return nodeText + " --" + id + "." + Arrays.asList(hierarchy) + "--";
        return nodeText;
    }

    @Override
    public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
    	LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout viewLayout = (LinearLayout) layoutInflater.inflate(R.layout.barcode_barcodetree_itemrow, null);
        return updateView(viewLayout, treeNodeInfo);
    }

    @Override
    public LinearLayout updateView(final View view, final TreeNodeInfo<Long> treeNodeInfo) {
    	//Log.d(TAG, "<<<< updateView >>>>>    treeNodeInfo.getId()==>"+treeNodeInfo.getId());

        final LinearLayout viewLayout = (LinearLayout) view;
        final TextView descriptionView = (TextView) viewLayout.findViewById(R.id.barcodetree_lineText);
        
        // 바코드 인포정보를 가져온다.
        BarcodeListInfo model = getBarcodeListInfo(treeNodeInfo.getId());
        
        String description = getDescription(model);
        descriptionView.setText(description);

        
        if (mJobGubun.startsWith("현장점검")) {
    		// 현장점검에서의 ScanValue 정의
    		//   0.기본    (기본 연두색 LightGreen)
    		//   3.최초스캔 (기본 연두색 LightGreen)
    		//   4.설비상태 (기본 연두색 LightGreen)
    		//   5.설비마스터에 있는 설비 추가 (분홍색.Pink)
    		//   6.설비마스터에 없는 설비 추가 (빨강색.Red)
    		//   9.광모듈  (기본 연두색 LightGreen)

    		if (model.getScanValue().equals("5")) {
    			viewLayout.setBackgroundColor(Color.rgb(255, 192, 203));  // pink
    		} else if (model.getScanValue().equals("6")) {
    			viewLayout.setBackgroundColor(Color.RED);
    		} else if (model.getScanValue().equals("1") || model.getScanValue().equals("3") 
    				|| model.getScanValue().equals("4") || model.getScanValue().equals("9")) {
    			viewLayout.setBackgroundColor(Color.rgb(203, 249, 181));		// green
    		} else {
    			viewLayout.setBackgroundColor(Color.WHITE);
    		}
        } else {
        	if (model.getPartType().equals("L")) {
            	viewLayout.setBackgroundColor(Color.YELLOW);
        	} else if (model.getPartType().equals("D")) {
                viewLayout.setBackgroundColor(Color.rgb(203, 249, 181));
        	} else if (model.getScanValue().equals("1")) {
            	viewLayout.setBackgroundColor(Color.rgb(203, 249, 181));
            } else if (model.getScanValue().equals("2") || model.getScanValue().equals("3")) {
            	viewLayout.setBackgroundColor(Color.rgb(203, 249, 181));
            } else if (model.getScanValue().equals("4")) {
            	viewLayout.setBackgroundColor(Color.rgb(255, 192, 203));
    		} else {
    			viewLayout.setBackgroundColor(Color.WHITE);
    		}
            
            if (!model.getComment().isEmpty()) {
            	viewLayout.setBackgroundColor(Color.rgb(255, 192, 203));  // Pink
            }
        }
        if (selected!=null && selected.longValue() == treeNodeInfo.getId().longValue()) {
        	descriptionView.setTextColor(Color.BLUE);
        } else {
        	descriptionView.setTextColor(Color.BLACK);
        }

        return viewLayout;
    }

    @Override
    public void handleItemClick(final View view, final int position, final Object id) {
        final Long longId = (Long) id;
        Log.d(TAG, "<<<< handleItemClick >>>>>    longId==>"+longId);
        
        final TreeNodeInfo<Long> nodeInfo = getParentTreeNodeInfo(longId);
        if (nodeInfo.isWithChildren()) {
            super.handleItemClick(view, position, id);
        } else {
            //final ViewGroup vg = (ViewGroup) view;
            //final CheckBox cb = (CheckBox) vg.findViewById(R.id.demo_list_checkbox);
            //cb.performClick();
        }
        
        changeSelected(longId);
        refresh();
    }
    
    public String getInstConfPartType() {
		if (getCount() > 0) {
			Long selectKey = getItemId(0);
			if (selectKey != null) {
				BarcodeListInfo barcodeInfo = getBarcodeListInfo(selectKey);
				return barcodeInfo.getPartType();
			}
		}
        return "";
    }
    
    /**
     * DB에서 불러온 R:rack, S:shelf, U:unit, E:Equipment 타입에 따라 건수를 산출한다.
     * @param partType
     * @return Count
     */
    public int getDBPartTypeCount(String partType) {
    	if (partType.equals("T")) {
    		return getCount();
    	}
    	
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeMaps.values()) {
    		if (barcodeInfo.getPartType().equals(partType)) {
				count++;
			}
    	}
    	return count;
    }
    
    /**
     * 스캔이 이루어진 R:rack, S:shelf, U:unit, E:Equipment 타입에 따라 건수를 산출한다.
     * @param partType
     * @return Count
     */
    public int getScanPartTypeCount(String partType, String barcode) {
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeMaps.values()) {
    		if (!barcodeInfo.getBarcode().equals(barcode) && !barcodeInfo.getScanValue().equals("0")) {
    			if (barcodeInfo.getPartType().equals(partType)) {
    				count++;
    			}
    		}
    	}
    	return count;
    }
    public int getScanPartTypeCount(String partType) {
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeMaps.values()) {
    		if (!barcodeInfo.getScanValue().equals("0")) {
    			if (barcodeInfo.getPartType().equals(partType)) {
    				count++;
    			}
    		}
    	}
    	return count;
    }
    public int getAddCount() {
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeMaps.values()) {
    		if (barcodeInfo.getCrudType().equals("add")) {
				count++;
    		}
		}
    	return count;
    }
    public int getOkCount() {
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeMaps.values()) {
    		if (barcodeInfo.getScanValue().equals("4") || barcodeInfo.getScanValue().equals("9")) {
				count++;
    		} else if (barcodeInfo.getCheckValue().startsWith("Y")) {
    			count++;
    		}
		}
    	return count;
    }
    
    /**
     * Selected 된 바코드 스캔값을 초기화 한다.
     */
    public void initItemValue() {
		Long selectedKey = getSelected();
		if (selectedKey != null) {
			Long parentKey = getManager().getParent(selectedKey);
			
			BarcodeListInfo selectedBarcodeInfo = getBarcodeListInfo(selectedKey);
			
			// 광모듈 자동 스캔은 스캔취소 없음
			if (selectedBarcodeInfo.getScanValue().equals("4") || selectedBarcodeInfo.getScanValue().equals("9")) return;			

			String checkValue = "N/N/N/N/N";
			selectedBarcodeInfo.setCheckValue(checkValue);
			
			if (selectedBarcodeInfo.getScanValue().equals("3") || selectedBarcodeInfo.getScanValue().equals("5") || selectedBarcodeInfo.getScanValue().equals("6")) {
				removeNode(selectedKey);
				if (parentKey != null) {
					changeSelected(parentKey);
				} else {
					selectLastPosition();
				}
			} else if (!selectedBarcodeInfo.getScanValue().equals("0")) {
				selectedBarcodeInfo.setScanValue("0");
				setBarcodeListInfo(selectedKey, selectedBarcodeInfo);
			}
		}
		refresh();
	}
    
    /**
     * Tree의 모든 하위Node가 스캔된지않은 경우 Check
     */
    public boolean isNotScaned() {
    	for (Long key : getAllNextNodeList()) {
    		BarcodeListInfo barcodeInfo = getBarcodeListInfo(key);
        	if (barcodeInfo == null || barcodeInfo.getScanValue().equals("0")) {
        		return true;
        	}
    	}
    	return false;
    }
    
    /**
     * Tree의 모든 Node의 바코드는 스캔되었는데.  하위Node가 스캔된지않은 경우 Check
     */
    public boolean isDownNodeNotScaned() {
    	for (Long key : getAllNextNodeList()) {
    		if (isChildNodeNotScaned(key)) {
    			return true;
    		}
    	}
    	return false;
    }
    private boolean isChildNodeNotScaned(final Long key) {
    	TreeNodeInfo<Long> treeNode = getTreeNodeInfo(key);
    	if (!treeNode.isWithChildren()) return false;
    	
    	BarcodeListInfo barcodeInfo = getBarcodeListInfo(treeNode.getId());
    	if (barcodeInfo == null || barcodeInfo.getScanValue().equals("0")) {
    		return false;
    	}
    	
    	// 자식Node가 있으면 자식에 자식이 있는지 추적한다.
    	List<Long> childKeys = getManager().getChildren(treeNode.getId());
        for (final Long childKey : childKeys) {
        	BarcodeListInfo childBarcodeInfo = getBarcodeListInfo(childKey);
        	if (childBarcodeInfo == null || childBarcodeInfo.getScanValue().equals("0")) {
        		return true;
        	}
        }
    	return false;
    }
}