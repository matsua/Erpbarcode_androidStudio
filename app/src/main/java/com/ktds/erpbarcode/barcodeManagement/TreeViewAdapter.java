package com.ktds.erpbarcode.barcodeManagement;

  
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcodeManagement.SuperTreeViewAdapter.ViewHolder;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
  
  
public class TreeViewAdapter extends BaseExpandableListAdapter{  
	private static final String TAG = "TreeViewAdapter";
    public static final int ItemHeight = 120;
    public static final int PaddingLeft = 36; 
    private int myPaddingLeft = 0;
    private boolean showRep = false;
    
    static public class TreeNode{  
    	IsmBarcodeInfo parentObj; 
        List<IsmBarcodeInfo> childs = new ArrayList<IsmBarcodeInfo>();
    }  
      
    List<TreeNode> treeNodes = new ArrayList<TreeNode>();  
    Context parentContext;  
    
    public TreeViewAdapter(Context view,int myPaddingLeft, boolean showRep)  
    {  
        parentContext = view;  
        this.myPaddingLeft = myPaddingLeft;  
        this.showRep = showRep;
    }
    
    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            
            IsmBarcodeInfo ismBarcodeInfo = getGroup(position);
            ismBarcodeInfo.setChecked(isChecked);
        }
    };
    
    public List<TreeNode> GetTreeNode()  
    {  
        return treeNodes;  
    }  
      
    public void UpdateTreeNode(List<TreeNode> nodes)  
    {  
        treeNodes = nodes;  
    }  
      
    public void RemoveAll()  
    {  
        treeNodes.clear();  
    }  
      
    public IsmBarcodeInfo getChild(int groupPosition, int childPosition) {  
        return (IsmBarcodeInfo) treeNodes.get(groupPosition).childs.get(childPosition);  
    }  
  
    public int getChildrenCount(int groupPosition) {  
        return treeNodes.get(groupPosition).childs.size();  
    }  
  
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) { 
    	ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	
        	LayoutInflater infalInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.loc_barcodemanagement_list_third_row,parent,false);
        	
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
    		if (showRep) {
    			if (convertView.findViewById(R.id.loc_crm15) != null) holder.item15 = (TextView) convertView.findViewById(R.id.loc_crm15);
    			if (convertView.findViewById(R.id.loc_crm16) != null) holder.item16 = (TextView) convertView.findViewById(R.id.loc_crm16);
    		}
            // end sesang
    		convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
		IsmBarcodeInfo model = getChild(groupPosition, childPosition);
		holder.checked.setTag(groupPosition*childPosition);
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
		if (holder.item15 != null) {				
			if (showRep) {
				holder.item15.setVisibility(View.VISIBLE);
				holder.item15.setText(model.getRepLocCd());
			} else {
				holder.item15.setVisibility(View.GONE);
			}
		}
		if (holder.item16 != null) {
			if (showRep) {
				holder.item16.setVisibility(View.VISIBLE);
				holder.item16.setText(model.getRepLocNm());
			} else {
				holder.item16.setVisibility(View.GONE);
			}			
		}
        // end sesang
        return convertView;
    }  
  
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	
        	LayoutInflater infalInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.loc_barcodemanagement_list_child_row,parent,false);
        	
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
    		if (showRep) {
    			if (convertView.findViewById(R.id.loc_crm15) != null) holder.item15 = (TextView) convertView.findViewById(R.id.loc_crm15);
    			if (convertView.findViewById(R.id.loc_crm16) != null) holder.item16 = (TextView) convertView.findViewById(R.id.loc_crm16);
    		}
    		convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
		IsmBarcodeInfo model = getGroup(groupPosition);
		holder.checked.setTag(groupPosition);
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
		if (holder.item15 != null) {				
			if (showRep) {
				holder.item15.setVisibility(View.VISIBLE);
				holder.item15.setText(model.getRepLocCd());
			} else {
				holder.item15.setVisibility(View.GONE);
			}
		}
		if (holder.item16 != null) {
			if (showRep) {
				holder.item16.setVisibility(View.VISIBLE);
				holder.item16.setText(model.getRepLocNm());
			} else {
				holder.item16.setVisibility(View.GONE);
			}			
		}		
        // end sesang
		
        return convertView;
    }  
  
    public long getChildId(int groupPosition, int childPosition) {  
        return childPosition;  
    }  
  
    public IsmBarcodeInfo getGroup(int groupPosition) {  
        return treeNodes.get(groupPosition).parentObj;  
    }  
  
    public int getGroupCount() {  
        return treeNodes.size();  
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