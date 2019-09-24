package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.treeview.AbstractTreeViewAdapter;
import com.ktds.erpbarcode.common.treeview.TreeNodeInfo;
import com.ktds.erpbarcode.common.treeview.TreeStateManager;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
public class OrgCodeTreeAdapter extends AbstractTreeViewAdapter<Long> {
    
    private static final String TAG = "OrgCodeTreeAdapter";
    private static final int LEVEL_NUMBER = 10;
    
    //private String mJobGubun = "";
    
    private long _id = -1;
    private Long selected = null;
    private List<Long> cacheChildKeys = null;
    private Map<Long, OrgCodeInfo> mOrgMaps = new HashMap<Long, OrgCodeInfo>();
    
    public OrgCodeTreeAdapter(final Context context, final TreeStateManager<Long> treeStateManager) {
        super(context, treeStateManager, LEVEL_NUMBER);
        //mJobGubun = GJobData.getInstance().getJobGubun();
    }
    
    private long newId() {
    	if (_id == -1) _id = 1000000000;
    	else _id = _id + 1;

        return _id;
    }
    
    public void changeSelected(final Long id) {
        selected = id;
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
    
    public TreeNodeInfo<Long> getNodeInfo(Long key) {
    	return getManager().getNodeInfo(key);
    }

    public TreeNodeInfo<Long> getParentTreeNodeInfo(Long key) {
    	Long parentKey = getManager().getParent(key);
    	if (parentKey != null) {
    		TreeNodeInfo<Long> parentTreeNode = getNodeInfo(parentKey);
    		if (parentTreeNode != null) {
    			return parentTreeNode;
    		}
    	}
    	return null;
    }
    
    public Long getOrgCodeKey(String orgCode) {
    	for (Long key : getManager().getAllNodeKeys()) {
    		OrgCodeInfo orgInfo = getOrgCodeInfo(key);
    		if (orgInfo.getOrgCode().equals(orgCode)) {
    			return key;
    		}
		}
    	return null;
	}

    public OrgCodeInfo getOrgCodeInfo(Long key) {
    	if (key == null) return null;
		return mOrgMaps.get(key);
	}

    public Set<Long> getAllNodeKeys() {
    	return getManager().getAllNodeKeys();
    }
    
    public List<Long> getAllNextNodeList() {
    	return getManager().getAllNextNodeList();
    }
    
    public void addItem(OrgCodeInfo item) {
    	Long newId = newId();
        if (mOrgMaps.size() == 0) {
        	mOrgMaps.put(newId, item);
            addAfterChild(null,  newId, null);
            
        } else {
        	mOrgMaps.put(newId, item);
            
        	final Long selectedKey = getSelected();
            if (selectedKey == null) {
            	addAfterChild(null,  newId, null);
            } else {
            	Long parentKey = getOrgCodeKey(item.getCostCenter());
            	if (parentKey == null) {
            		addAfterChild(null, newId, null);
            	} else {
            		addAfterChild(parentKey, newId, null);
            	}
            }
        }
    }

    public void addItems(List<OrgCodeInfo> items) {
    	Map<Long, OrgCodeInfo> itemMaps = arrayItemsToMaps(items);
    	addItems(itemMaps);
    }
    
    private void addItems(Map<Long, OrgCodeInfo> itemMaps) {
        mOrgMaps.clear();
        selected = null;
        addItemsNotClear(itemMaps);
    }
    
    public void addItemsNotClear(List<OrgCodeInfo> items) {
    	addItemsNotClear(arrayItemsToMaps(items));
    }
    
    private void addItemsNotClear(Map<Long, OrgCodeInfo> itemMaps) {
    	Log.d(TAG, "addItemsNotClear   Start...");
        if (itemMaps.size() == 0) return;		// 가져온 데이타
        
        mOrgMaps.putAll(itemMaps); 			// mOrgMaps -> 트리맵 리스트

        Long beforeKey = (long) 0;
        OrgCodeInfo beforeOrgInfo = null;
        
        SortedSet<Long> itemKeys = new TreeSet<Long>(itemMaps.keySet());
        for (Long newKey : itemKeys) {
        	OrgCodeInfo newOrgInfo = itemMaps.get(newKey);
            
            Log.d(TAG, "addItemsNotClear   newKey==>" + newKey);
            
            // 첫번째 바코드 목록이면..
            if (beforeKey == 0) {
            	addAfterChild(null, newKey, null);
            } else {
            	if (beforeOrgInfo.getOrgCode().equals(newOrgInfo.getParentOrgCode())) {
                    addAfterChild(beforeKey, newKey, null);
                } else {
                    //Long parentKey = getManager().getParent(beforeKey);
                	Long parentKey = getOrgCodeKey(newOrgInfo.getCostCenter());
                	if (parentKey == null) {
                		addAfterChild(null, newKey, null);
                	} else {
                		addAfterChild(parentKey, newKey, null);
                	}
                }
            }
            
            beforeKey = newKey;
            beforeOrgInfo = newOrgInfo;
        }
    }
    
    public void addChildItem(Long parent, OrgCodeInfo barcode) {
    	Long newId = newId();
        mOrgMaps.put(newId, barcode);
        
        if (mOrgMaps.size() == 0) {
            addAfterChild(null,  newId, null);
        } else {
            addAfterChild(parent,  newId, null);
        }
    }
    
    public void addChildItems(Long parent, List<OrgCodeInfo> items) {
    	addChildItems(parent, arrayItemsToMaps(items));
    }

    private void addChildItems(Long parent, Map<Long, OrgCodeInfo> itemMaps) {
    	if (parent == null) return;
    	
    	Log.d(TAG, "addChildItems   Start...");
    	
    	final TreeNodeInfo<Long> parentNode = getNodeInfo(parent);
    	if (parentNode == null) return;
    	mOrgMaps.putAll(itemMaps);
    	
    	Long beforeKey = parentNode.getId();
    	OrgCodeInfo beforeOrgInfo = mOrgMaps.get(parentNode.getId());

    	SortedSet<Long> itemKeys = new TreeSet<Long>(itemMaps.keySet());
    	for (Long newKey : itemKeys) {
            OrgCodeInfo newOrgInfo = itemMaps.get(newKey);
            
            Log.d(TAG, "addChildItems   beforeKey==>" + beforeKey);
            Log.d(TAG, "addChildItems   newKey==>" + newKey);
            
            if (beforeOrgInfo.getOrgCode().equals(newOrgInfo.getParentOrgCode())) {
                addAfterChild(beforeKey, newKey, null);
            } else {
                Long parentKey = getManager().getParent(beforeKey);
                if (parentKey == null) {
            		addAfterChild(null, newKey, null);
            	} else {
            		addAfterChild(parentKey, newKey, null);
            	}
            }

            beforeKey = newKey;
            beforeOrgInfo = newOrgInfo;
        }
    }
    
    private void addAfterChild(final Long parent, final Long newChild, final Long afterChild) {
        getManager().addAfterChild(parent, newChild, afterChild);
    }
    
    //private void addBeforeChild(final Long parent, final Long newChild, final Long beforeChild) {
    //    getManager().addBeforeChild(parent, newChild, beforeChild);
    //}
    
    public void setOrgCodeInfo(Long key, OrgCodeInfo barcodeInfo) {
    	mOrgMaps.put(key, barcodeInfo);
	}
    
    // ArrayList<OrgCodeInfo> 를 HashMap<Long, OrgCodeInfo> 으로 변환한다.
    public Map<Long, OrgCodeInfo> arrayItemsToMaps(List<OrgCodeInfo> items) {
    	Map<Long, OrgCodeInfo> tempMaps = new HashMap<Long, OrgCodeInfo>();
    	for (OrgCodeInfo orgInfo : items) {
    		Long newId = newId();
    		tempMaps.put(newId, orgInfo);
    	}
    	return tempMaps;
    }
    
    public List<Long> getSubTreeList(final Long key) {
    	TreeNodeInfo<Long> treeNode = getNodeInfo(key);
    	if (treeNode == null) return null;
    	cacheChildKeys = new ArrayList<Long>();
    	putChildrenList(treeNode.getId());
    	return cacheChildKeys;
    }
    
    private void putChildrenList(final Long key) {
    	TreeNodeInfo<Long> treeNode = getNodeInfo(key);
    	if (!treeNode.isWithChildren()) return;
    	
    	// 자식Node가 있으면 자식에 자식이 있는지 추적한다.
    	List<Long> childKeys = getManager().getChildren(treeNode.getId());
    	if (childKeys != null && childKeys.size() > 0) {
    		cacheChildKeys.addAll(childKeys);
    	}
    	
        for (final Long childKey : childKeys) {
        	putChildrenList(childKey);
        }
    	return;
    }
    
    public void selectLastPosition(){
    	if (getCount()==0) return;
		changeSelected(getItemId(getCount()-1));
		refresh();
    }
    
    /**
     * 선택한 Node의 하위 Node들 모두 삭제한다.
     * 
     * @param key
     */
    public void removeNode(final long key) {
    	// mOrgMaps 하위 TreeNode 정보를 모두 삭제한다.
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
        TreeNodeInfo<Long> treeNode = getNodeInfo(key);
        if (treeNode != null) {
        	mOrgMaps.remove(key);
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
    	mOrgMaps.clear();
    }
    
    private String  getNodeText(OrgCodeInfo model) {
    	String nodeText = "";
 
    	nodeText = model.getOrgName() + ":"
				+ model.getOrgCode() + ":"
				+ model.getCostCenter();

		return nodeText;
	}

    private String getDescription(final OrgCodeInfo model) {
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
        
        OrgCodeInfo model = getOrgCodeInfo(treeNodeInfo.getId());

        descriptionView.setText(getDescription(model));

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

}