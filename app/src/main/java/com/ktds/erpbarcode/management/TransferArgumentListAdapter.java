package com.ktds.erpbarcode.management;

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
import com.ktds.erpbarcode.management.model.ArgumentConfirmInfo;

public class TransferArgumentListAdapter extends BaseAdapter {

	private static final String TAG = "TransferArgumentListAdapter";

    private List<ArgumentConfirmInfo> mArgumentConfirmInfos = null;
    private LayoutInflater mInflater;
    
    public TransferArgumentListAdapter(Context context) {
    	mArgumentConfirmInfos = new ArrayList<ArgumentConfirmInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ArgumentConfirmInfo item) {
    	mArgumentConfirmInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<ArgumentConfirmInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mArgumentConfirmInfos.clear();
    	mArgumentConfirmInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mArgumentConfirmInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mArgumentConfirmInfos.size();
	}

	@Override
	public ArgumentConfirmInfo getItem(int position) {
		return mArgumentConfirmInfos.get(position);
	}
	
	public List<ArgumentConfirmInfo> getAllItems() {
		return mArgumentConfirmInfos;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setItem(int position, ArgumentConfirmInfo ismBarcodeInfo) {
		mArgumentConfirmInfos.set(position, ismBarcodeInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.management_transferargument_list_itemrow, null);

        	holder.deviceIdText = (TextView) convertView.findViewById(R.id.transferargument_list_deviceId);
        	holder.Level1NameText = (TextView) convertView.findViewById(R.id.transferargument_list_l1);
        	holder.Level2NameText = (TextView) convertView.findViewById(R.id.transferargument_list_l2);
        	holder.Level3NameText = (TextView) convertView.findViewById(R.id.transferargument_list_l3);
        	holder.Level4NameText = (TextView) convertView.findViewById(R.id.transferargument_list_l4);
        	holder.Level1CodeText = (TextView) convertView.findViewById(R.id.transferargument_list_l11);
        	holder.Level2CodeText = (TextView) convertView.findViewById(R.id.transferargument_list_l22);
        	holder.Level3CodeText = (TextView) convertView.findViewById(R.id.transferargument_list_l33);
        	holder.Level4CodeText = (TextView) convertView.findViewById(R.id.transferargument_list_l44);
        	holder.docNoText = (TextView) convertView.findViewById(R.id.transferargument_list_docNo);
        	holder.locCdText = (TextView) convertView.findViewById(R.id.transferargument_list_locCd);
        	holder.daoriText = (TextView) convertView.findViewById(R.id.transferargument_list_daori);
        	holder.wbsNoText = (TextView) convertView.findViewById(R.id.transferargument_list_wbsNo);
        	
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        ArgumentConfirmInfo model = getItem(position);

    	holder.deviceIdText.setText(model.getDeviceId());
    	holder.Level1NameText.setText(model.getDeviceBarcodeInfo().getLevel1Name());
    	holder.Level2NameText.setText(model.getDeviceBarcodeInfo().getLevel2Name());
    	holder.Level3NameText.setText(model.getDeviceBarcodeInfo().getLevel3Name());
    	holder.Level4NameText.setText(model.getDeviceBarcodeInfo().getLevel4Name());
    	holder.Level1CodeText.setText(model.getDeviceBarcodeInfo().getLevel1Code());
    	holder.Level2CodeText.setText(model.getDeviceBarcodeInfo().getLevel2Code());
    	holder.Level3CodeText.setText(model.getDeviceBarcodeInfo().getLevel3Code());
    	holder.Level4CodeText.setText(model.getDeviceBarcodeInfo().getLevel4Code());
    	holder.docNoText.setText(model.getDocNo());
    	holder.locCdText.setText(model.getLocCd());
    	holder.daoriText.setText(model.getDaori());
    	holder.wbsNoText.setText(model.getWbsNo());
    	
        return convertView;
	}

	public class ViewHolder {
		public TextView deviceIdText;
		public TextView Level1NameText;
		public TextView Level2NameText;
		public TextView Level3NameText;
		public TextView Level4NameText;
		public TextView Level1CodeText;
		public TextView Level2CodeText;
		public TextView Level3CodeText;
		public TextView Level4CodeText;
		public TextView docNoText;
		public TextView locCdText;
		public TextView daoriText;
		public TextView wbsNoText;

    }
}
