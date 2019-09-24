package com.ktds.erpbarcode.env.bluetooth;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ktds.erpbarcode.R;

public class NewDevicesAdapter extends BaseAdapter {

	private static final String TAG = "NewDevicesAdapter";

    private List<DeviceInfo> mDeviceInfos;
    private LayoutInflater mInflater;
    
    
    public NewDevicesAdapter(Context context) {
    	mDeviceInfos = new ArrayList<DeviceInfo>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(DeviceInfo item) {
    	mDeviceInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<DeviceInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mDeviceInfos.clear();
    	mDeviceInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void itemClear() {
    	mDeviceInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mDeviceInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mDeviceInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.env_bluetooth_devicelist_itemrow, null);
            holder.deviceName = (TextView) convertView.findViewById(R.id.bluetooth_paired_devices_deviceName);
            holder.deviceAddress = (TextView) convertView.findViewById(R.id.bluetooth_paired_devices_deviceAddress);
            holder.deleteBtn = (Button) convertView.findViewById(R.id.bluetooth_paired_devices_delete_button);
            holder.deleteBtn.setVisibility(Button.GONE);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DeviceInfo model = (DeviceInfo) getItem(position);
        
		holder.deviceName.setText(model.getDeviceName());
		holder.deviceAddress.setText(model.getDeviceAddress());
		
        return convertView;
	}

	public class ViewHolder {
		public TextView deviceName;
		public TextView deviceAddress;
		public Button deleteBtn;
    }
}
