package com.ktds.erpbarcode.infosearch;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.infosearch.model.OrgCodeInfo;

public class UserInfoListAdapter extends BaseAdapter {

	private static final String TAG = "UserInfoListAdapter";

	private int selected = -1;
    private LayoutInflater mInflater;
    private List<OrgCodeInfo> mUserInfos = new ArrayList<OrgCodeInfo>();
    

    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            OrgCodeInfo userInfo = getItem(position);
            
            if (userInfo.isChecked() == isChecked) return;
            
            buttonInit();
            
            if (userInfo.isChecked()) {
            	userInfo.setChecked(false);
            	setItem(position, userInfo);
            } else {
            	buttonView.setChecked(true);
            	userInfo.setChecked(true);
            	setItem(position, userInfo);
            }
        }
    };
    
    public void buttonInit(){
    	for (OrgCodeInfo userInfo : mUserInfos) {
    		if (userInfo.isChecked()) {
    			userInfo.setChecked(false);;
    		}
    	}
    	notifyDataSetChanged();
    }
    
    public UserInfoListAdapter(Activity activity) {
    	mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getSelectedPosition() {
		return this.selected;
	}
	
	public void changeSelectedPosition(int position) {
		this.selected = position;
		//notifyDataSetChanged();
	}
    
    public void addItem(OrgCodeInfo item) {
    	mUserInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<OrgCodeInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mUserInfos.clear();
    	mUserInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mUserInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mUserInfos.size();
	}
 

	@Override
	public OrgCodeInfo getItem(int position) {
		return mUserInfos.get(position);
	}
	
	public List<OrgCodeInfo> getAllItems() {
		return mUserInfos;
	}
	
	public void removeItem(int position) {
		mUserInfos.remove(position);
	}
	
	public List<OrgCodeInfo> getCheckedItems() {
		List<OrgCodeInfo> tempItems = new ArrayList<OrgCodeInfo>();
		for (OrgCodeInfo userInfo : mUserInfos) {
    		if (userInfo.isChecked()) {
    			tempItems.add(userInfo);
    		}
    	}
		return tempItems;
	}
	
	public void removeCheckedItems() { 
		
		for (int i=mUserInfos.size()-1;i>=0; i--) {
			OrgCodeInfo userInfo = mUserInfos.get(i);
			if (userInfo.isChecked()) {
    			removeItem(i);
    		}	
		}
    	notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setItem(int position, OrgCodeInfo userInfo) {
		mUserInfos.set(position, userInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.user_info_item, null);
    		holder.checked = (CheckBox) convertView.findViewById(R.id.user_info_isChecked);
    		holder.checked.setOnCheckedChangeListener(onCheckedChange);
    		holder.usetId = (TextView) convertView.findViewById(R.id.userId);
    		holder.userName = (TextView) convertView.findViewById(R.id.userName);
    		holder.orgName = (TextView) convertView.findViewById(R.id.userOrgName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        OrgCodeInfo model = getItem(position);
        holder.checked.setTag(position);
        holder.checked.setChecked(model.isChecked());
        holder.usetId.setText(model.getUserId());
        holder.userName.setText(model.getUserNm());
        holder.orgName.setText(model.getOrgName());
        return convertView;
	}

	public class ViewHolder {
		public CheckBox checked;
		public TextView usetId;
		public TextView userName;
		public TextView orgName;
    }
}
