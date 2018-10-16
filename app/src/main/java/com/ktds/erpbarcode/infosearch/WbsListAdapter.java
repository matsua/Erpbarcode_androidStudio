package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.WBSInfo;


public class WbsListAdapter extends BaseAdapter {

	private static final String TAG = "WbsListAdapter";

    private List<WBSInfo> mWbsInfos = null;
    private LayoutInflater mInflater;
    private int selectedPosition = -1;
    
    
    public WbsListAdapter(Context context) {
    	mWbsInfos = new ArrayList<WBSInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(WBSInfo item) {
    	mWbsInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<WBSInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mWbsInfos.clear();
    	mWbsInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mWbsInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mWbsInfos.size();
	}

	@Override
	public WBSInfo getItem(int position) {
		return mWbsInfos.get(position);
	}
	
	public List<WBSInfo> getAllItems() {
		return mWbsInfos;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public WBSInfo getWbsInfo(String wbsNo) {
		for (WBSInfo wbsInfo : mWbsInfos) {
			if (wbsInfo.getWbsNo().equals(wbsNo))
					return wbsInfo;
		}
		return null;
	}
	
	public int getSelectPosition() {
		return selectedPosition;
	}
	
	public void changeSelectPosition(int position) {
		selectedPosition = position;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.infosearch_searchwbs_list_itemrow, null);
        	holder.contractorNameText = (TextView) convertView.findViewById(R.id.selectwbs_list_contractorName);
        	holder.wbsNoText = (TextView) convertView.findViewById(R.id.selectwbs_list_wbsNo);
        	holder.wbsHistoryText = (TextView) convertView.findViewById(R.id.selectwbs_list_wbsHistory);
        	holder.jpjtTypeText = (TextView) convertView.findViewById(R.id.selectwbs_list_jpjtType);
        	holder.wbsStatusNameText = (TextView) convertView.findViewById(R.id.selectwbs_list_wbsStatusName);
        	holder.kostlStatusText = (TextView) convertView.findViewById(R.id.selectwbs_list_kostlStatus);
        	
    		if (GlobalData.getInstance().getJobGubun().equals("철거") || GlobalData.getInstance().getJobGubun().equals("다중철거"))
            {
                holder.jpjtTypeText.setVisibility(View.GONE);
                holder.wbsStatusNameText.setVisibility(View.GONE);
                holder.kostlStatusText.setVisibility(View.GONE);
            }

    		convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        WBSInfo model = getItem(position);
        
        if (selectedPosition == position) {
        	convertView.setBackgroundColor(Color.rgb(203, 249, 181));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        
        holder.contractorNameText.setText(model.getContractorName());
        holder.wbsNoText.setText(model.getWbsNo());
        holder.wbsHistoryText.setText(model.getWbsHistory());
        holder.jpjtTypeText.setText(model.getJpjtType());
        holder.wbsStatusNameText.setText(model.getWbsStatusName());
        holder.kostlStatusText.setText(model.getKostlStatus());
        
        return convertView;
	}

	public class ViewHolder {
		public TextView contractorNameText;
		public TextView wbsNoText;
		public TextView jpjtTypeText;
		public TextView wbsHistoryText;
		public TextView wbsStatusNameText;
		public TextView kostlStatusText;
    }

}
