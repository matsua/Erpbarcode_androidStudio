package com.ktds.erpbarcode.barcodeManagement;
  
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcodeManagement.TreeViewAdapter.TreeNode;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
  
public class SuperTreeViewAdapter extends BaseExpandableListAdapter{ 
	
    static public class SuperTreeNode {  
    	IsmBarcodeInfo parentObj;  
        List<TreeViewAdapter.TreeNode> childs = new ArrayList<TreeViewAdapter.TreeNode>();  
    }
    
    private static final String TAG = "SuperTreeViewAdapter";
  
    private List<SuperTreeNode> superTreeNodes = new ArrayList<SuperTreeNode>();  
    private Context parentContext;  
    private OnChildClickListener stvClickEvent;
    
    private List<IsmBarcodeInfo> mIsmBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
      
    public SuperTreeViewAdapter(Context view,OnChildClickListener stvClickEvent) {  
        parentContext = view;  
        this.stvClickEvent=stvClickEvent;  
    }
    
    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            
//            IsmBarcodeInfo ismBarcodeInfo = getItem(position);
//            ismBarcodeInfo.setChecked(isChecked);
            
            mIsmBarcodeInfos.get(position).setChecked(isChecked);
        }
    };
    
    public void addItem(IsmBarcodeInfo item) {
    	mIsmBarcodeInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<IsmBarcodeInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mIsmBarcodeInfos.clear();
    	mIsmBarcodeInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public IsmBarcodeInfo getItem(int position) {
		return mIsmBarcodeInfos.get(position);
	}
	
	public List<IsmBarcodeInfo> getAllItems() {
		return mIsmBarcodeInfos;
	}
    
    public List<IsmBarcodeInfo> getCheckedItems() {
		List<IsmBarcodeInfo> tempItems = new ArrayList<IsmBarcodeInfo>();
		for (IsmBarcodeInfo ismBarcodeInfo : mIsmBarcodeInfos) {
    		if (ismBarcodeInfo.isChecked()) {
    			System.out.println("ismBarcodeInfo >> " + ismBarcodeInfo.getLocCd());
    			tempItems.add(ismBarcodeInfo);
    		}
    	}
		return tempItems;
	}
  
    public List<SuperTreeNode> GetTreeNode() {  
        return superTreeNodes;  
    }  
  
    public void UpdateTreeNode(List<SuperTreeNode> node) {  
        superTreeNodes = node;  
    }
    
    public void RemoveAll()  
    {  
        superTreeNodes.clear();  
    }  
      
    public Object getChild(int groupPosition, int childPosition) {
        return superTreeNodes.get(groupPosition).childs.get(childPosition);  
    }  
  
    public int getChildrenCount(int groupPosition) {  
        return superTreeNodes.get(groupPosition).childs.size();  
    }  
  
    public ExpandableListView getExpandableListView() {  
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, TreeViewAdapter.ItemHeight);  
        ExpandableListView superTreeView = new ExpandableListView(parentContext);
        Drawable drawable = parentContext.getResources().getDrawable(R.drawable.style_expander_group);

        superTreeView.setGroupIndicator(drawable);
        superTreeView.setLayoutParams(lp);  
        return superTreeView;  
    }  
  
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ExpandableListView treeView = getExpandableListView();
        final TreeViewAdapter treeViewAdapter = new TreeViewAdapter(this.parentContext,0);  
        List<TreeNode> tmp = treeViewAdapter.GetTreeNode();
        final TreeNode treeNode = (TreeNode) getChild(groupPosition, childPosition);  
        
        Drawable drawable = parentContext.getResources().getDrawable(R.drawable.style_expander_group);
        if(treeNode.childs.size() > 0){
        	treeView.setGroupIndicator(drawable);
        }else{
        	treeView.setGroupIndicator(null);
        }
        
        tmp.add(treeNode);  
        treeViewAdapter.UpdateTreeNode(tmp);  
        treeView.setAdapter(treeViewAdapter);  
        treeView.setOnGroupExpandListener(new OnGroupExpandListener() {  
            @Override  
            public void onGroupExpand(int groupPosition) {  
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (treeNode.childs.size() + 1) * TreeViewAdapter.ItemHeight);  
                treeView.setLayoutParams(lp);  
            }  
        });  
          
        treeView.setOnGroupCollapseListener(new OnGroupCollapseListener() {  
            @Override  
            public void onGroupCollapse(int groupPosition) {  
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, TreeViewAdapter.ItemHeight);  
                treeView.setLayoutParams(lp);  
            }  
        });  
        treeView.setPadding(TreeViewAdapter.PaddingLeft, 0, 0, 0);  
        return treeView;  
    }  
  
	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
    	
        if (convertView == null) {
        	holder = new ViewHolder();
        	
        	LayoutInflater infalInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.loc_barcodemanagement_list_itemrow,parent,false);
        	
        	holder.checked = (CheckBox) convertView.findViewById(R.id.ism_mnt_list_isChecked);
        	holder.checked.setOnCheckedChangeListener(onCheckedChange);
        	
    		holder.item1 = (TextView) convertView.findViewById(R.id.loc_crm1);
    		holder.item2 = (TextView) convertView.findViewById(R.id.loc_crm2);
    		holder.item3 = (TextView) convertView.findViewById(R.id.loc_crm3);
    		holder.item4 = (TextView) convertView.findViewById(R.id.loc_crm4);
    		holder.item5 = (TextView) convertView.findViewById(R.id.loc_crm5);
    		holder.item6 = (TextView) convertView.findViewById(R.id.loc_crm6);
    		holder.item7 = (TextView) convertView.findViewById(R.id.loc_crm7);
    		holder.item8 = (TextView) convertView.findViewById(R.id.loc_crm8);
    		holder.item9 = (TextView) convertView.findViewById(R.id.loc_crm9);
    		holder.item10 = (TextView) convertView.findViewById(R.id.loc_crm10);
    		holder.item11 = (TextView) convertView.findViewById(R.id.loc_crm11);
    		holder.item12 = (TextView) convertView.findViewById(R.id.loc_crm12);
    		holder.item13 = (TextView) convertView.findViewById(R.id.loc_crm13);
    		holder.item14 = (TextView) convertView.findViewById(R.id.loc_crm14);
            // sesang 20190910 대표위치 정보 추가
            holder.item15 = (TextView) convertView.findViewById(R.id.loc_crm15);
            holder.item16 = (TextView) convertView.findViewById(R.id.loc_crm16);
            // end sesang
    		convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
    		
		IsmBarcodeInfo model = getGroup(position);
		holder.checked.setTag(model.getPosition());
		holder.checked.setChecked(model.isChecked());
		
		holder.item1.setText(model.getSilYuhung());
		holder.item2.setText(model.getLocCd());
		holder.item3.setText(model.getLocName());
		holder.item4.setText(model.getSilType());
		holder.item5.setText(model.getSilName());
		holder.item6.setText(model.getSilStatus());
		holder.item7.setText(model.getSilGubun());
		holder.item8.setText(model.getSilAddress());
		holder.item9.setText(model.getSilBulGb());
		holder.item10.setText(model.getSilBuType());
		holder.item11.setText(model.getSilKTBulType());
		holder.item12.setText(model.getSilKuksa());
		holder.item13.setText(model.getSilKuksaName());
		holder.item14.setText(model.getComment());

        // sesang 20190910 대표위치 정보 추가
        if (holder.item15 != null) holder.item15.setText(model.getRepLocCd());
        if (holder.item16 != null) holder.item16.setText(model.getRepLocNm());
        // end sesang
        return convertView;  
    }
    
    public long getChildId(int groupPosition, int childPosition) {  
        return childPosition;  
    }  
  
    public IsmBarcodeInfo getGroup(int groupPosition) {  
        return superTreeNodes.get(groupPosition).parentObj;  
    }  
  
    public int getGroupCount() {  
        return superTreeNodes.size();  
    }  
  
    public long getGroupId(int groupPosition) {  
        return groupPosition;  
    }  
  
    public boolean isChildSelectable(int groupPosition, int childPosition) {  
        return true;  
    }  
  
    public boolean hasStableIds() {  
        return true;  
    }  
    
    public class ViewHolder {
    	public CheckBox checked;
		public TextView item1;
		public TextView item2;
		public TextView item3;
		public TextView item4;
		public TextView item5;
		public TextView item6;
		public TextView item7;
		public TextView item8;
		public TextView item9;
		public TextView item10;
		public TextView item11;
		public TextView item12;
		public TextView item13;
		public TextView item14;
        // sesang 20190910 대표위치 정보 추가
        public TextView item15;
        public TextView item16;
        // end sesang
    }
}