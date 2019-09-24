package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.LocBarcodeInfo;

public class LocListAdapter extends BaseAdapter {

	private static final String TAG = "LocListAdapter";

    private List<LocBarcodeInfo> mLocBarcodeInfos = null;
    private LayoutInflater mInflater;
    private boolean showRep = false;
    
    
    public LocListAdapter(Context context) {
    	mLocBarcodeInfos = new ArrayList<LocBarcodeInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

	public LocListAdapter(Context context, boolean showRep) {
		mLocBarcodeInfos = new ArrayList<LocBarcodeInfo>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.showRep = showRep;
	}

	public void addItem(LocBarcodeInfo item) {
    	mLocBarcodeInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<LocBarcodeInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mLocBarcodeInfos.clear();
    	mLocBarcodeInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mLocBarcodeInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mLocBarcodeInfos.size();
	}

	@Override
	public LocBarcodeInfo getItem(int position) {
		return mLocBarcodeInfos.get(position);
	}
	
	public List<LocBarcodeInfo> getAllItems() {
		return mLocBarcodeInfos;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public LocBarcodeInfo getLocBarcodeInfo(String locCd) {
		for (LocBarcodeInfo locInfo : mLocBarcodeInfos) {
			if (locInfo.getLocCd().equals(locCd))
					return locInfo;
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.infosearch_selectloc_list_itemrow, null);
        	holder.locCdText = (TextView) convertView.findViewById(R.id.selectloc_list_locCd);
        	holder.locNameText = (TextView) convertView.findViewById(R.id.selectloc_list_locName);

        	if (showRep) {
				if (convertView.findViewById(R.id.selectloc_list_rep_locCd) != null)
					holder.repLocCdText = (TextView) convertView.findViewById(R.id.selectloc_list_rep_locCd);
				if (convertView.findViewById(R.id.selectloc_list_rep_locName) != null)
					holder.repLocNameText = (TextView) convertView.findViewById(R.id.selectloc_list_rep_locName);
			}
			convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        LocBarcodeInfo model = getItem(position);
        
        holder.locCdText.setText(model.getLocCd());
        holder.locNameText.setText(model.getLocName());

        if (showRep) {
			if (holder.repLocCdText != null) {
				holder.repLocCdText.setVisibility(View.VISIBLE);
				holder.repLocCdText.setText(model.getRepLocCd());
			}
			if (holder.repLocNameText != null) {
				holder.repLocNameText.setVisibility(View.VISIBLE);
				holder.repLocNameText.setText(model.getRepLocNm());
			}
		}
        return convertView;
	}

	public class ViewHolder {
		public TextView locCdText;
		public TextView locNameText;
		public TextView repLocCdText;
		public TextView repLocNameText;
	}

}
