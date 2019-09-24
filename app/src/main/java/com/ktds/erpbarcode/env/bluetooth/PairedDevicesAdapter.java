package com.ktds.erpbarcode.env.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;

public class PairedDevicesAdapter extends BaseAdapter {

	private static final String TAG = "PairedDevicesAdapter";

    private List<DeviceInfo> mDeviceInfos;
    private LayoutInflater mInflater;
    private String kind = "";
    
    public void setKind(String kind){
    	this.kind = kind;
    }
    
    public PairedDevicesAdapter(Context context) {
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

    public void removeItem(DeviceInfo item){
    	mDeviceInfos.remove(item);
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
            holder.deleteBtn.setOnClickListener(deleteButtonListener);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.deleteBtn.setTag(position);
        DeviceInfo model = (DeviceInfo) getItem(position);
        
		holder.deviceName.setText(model.getDeviceName());
		holder.deviceName.setTextColor(R.color.style_bluetooth_paired_devices_deviceName);
		holder.deviceAddress.setText(model.getDeviceAddress());

        return convertView;
	}

	public class ViewHolder {
		public TextView deviceName;
		public TextView deviceAddress;
		public Button deleteBtn;
    }
	
	final OnClickListener deleteButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			removePairedDeviceYesNoDialog(position);
		}
	};
	
	private void removePairedDevice(int position)
	{
		// ---------------------------------------------------
		// Paired 된 스캐너를 리스트에서 찾아 삭제 한다.
		// ---------------------------------------------------
		DeviceInfo mDeviceInfo =  mDeviceInfos.get(position);
        removeItem(mDeviceInfo);
        
		BluetoothAdapter mBluetoothAdapter;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		
        for (BluetoothDevice device : pairedDevices) {
        	//Unpair only KDC Bluetooth devices.
        	if(kind.equals("scanner")){
        		if(device.getName().toLowerCase().contains("kdc"))	
        		{
        			if(mDeviceInfo.getDeviceAddress().equals(device.getAddress()))
        			{
        				unpairDevice(device);
        			}
        		}
        	}else{
        		if(device.getName().toLowerCase().contains("zd500"))	
        		{
        			if(mDeviceInfo.getDeviceAddress().equals(device.getAddress()))
        			{
        				unpairDevice(device);
        			}
        		}
        	}
        }
	}
	
	private void unpairDevice(BluetoothDevice device) {
		try {
		    Method m = device.getClass()
		        .getMethod("removeBond", (Class[]) null);
		    m.invoke(device, (Object[]) null);
		} catch (Exception e) {
		    Log.e(TAG, e.getMessage());
		}
	}
	
	protected void removePairedDeviceYesNoDialog(final int position) {
		// -----------------------------------------------------------
		// Yes/No Dialog
		// -----------------------------------------------------------
		if (GlobalData.getInstance().isGlobalAlertDialog()) return;
		GlobalData.getInstance().setGlobalAlertDialog(true);

		String message ="";
		if(kind.equals("scanner")){
			message = "선택한 스캐너를 등록된 기기 리스트에서 삭제 하시겠습니까 ?";
		}else{
			message = "선택한 프린터를 등록된 기기 리스트에서 삭제 하시겠습니까 ?";
		}
		
		Activity mActivity = GlobalData.getInstance().getNowOpenActivity();
		
		final Builder builder = new AlertDialog.Builder(mActivity);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle("알림");
		TextView msgText = new TextView(mActivity);
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
				removePairedDevice(position);
			}
		});
		builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				GlobalData.getInstance().setGlobalAlertDialog(false);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		return;
	}
}
