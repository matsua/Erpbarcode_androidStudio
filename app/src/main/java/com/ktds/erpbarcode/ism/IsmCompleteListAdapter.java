 package com.ktds.erpbarcode.ism;

import java.util.ArrayList;
import java.util.List;

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
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;

public class IsmCompleteListAdapter extends BaseAdapter {

	private static final String TAG = "IsmCompleteListAdapter";

	private int selected = -1;
    private List<IsmBarcodeInfo> mIsmBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
    private LayoutInflater mInflater;

    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            Log.d(TAG, "^^--^^ onCheckedChanged   position==>"+position);
            Log.d(TAG, "^^--^^ onCheckedChanged   isChecked==>"+isChecked);
            
            IsmBarcodeInfo ismBarcodeInfo = getItem(position);
            ismBarcodeInfo.setChecked(isChecked);
        }
    };
    
    public IsmCompleteListAdapter(Context context) {
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(IsmBarcodeInfo item) {
    	selected = -1;
    	mIsmBarcodeInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<IsmBarcodeInfo> items) {
    	selected = -1;
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mIsmBarcodeInfos.clear();
    	mIsmBarcodeInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void addItemsNotClear(List<IsmBarcodeInfo> items) {
    	mIsmBarcodeInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void itemClear() {
    	selected = -1;
    	mIsmBarcodeInfos.clear();
    	notifyDataSetChanged();
    }
    

	@Override
	public int getCount() {
		return mIsmBarcodeInfos.size();
	}

	@Override
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
    			tempItems.add(ismBarcodeInfo);
    		}
    	}
		return tempItems;
	}
	 
	public void removeCheckedItems() {
    	for (int i=mIsmBarcodeInfos.size()-1;i>=0; i--) {
    		IsmBarcodeInfo ismbarcodeInfo = mIsmBarcodeInfos.get(i);
    		if (ismbarcodeInfo.isChecked()) {
    			mIsmBarcodeInfos.remove(i);
    		}
		}
    	notifyDataSetChanged();
	}
	public int getBarcodePosition(String barcode) {
		//Log.d(TAG, "getBarcodePosition   barcode==>"+barcode);
		for (int i=0; i<getCount(); i++) {
			IsmBarcodeInfo barcodeInfo = mIsmBarcodeInfos.get(i);
			if (barcodeInfo.getNewBarcode().equals(barcode))
				return i;
		}
		return -1;
	}
	
	public IsmBarcodeInfo getBarcodeListInfo(String barcode) {
		for (IsmBarcodeInfo barcodeInfo : mIsmBarcodeInfos) {
			if (barcodeInfo.getNewBarcode().equals(barcode))
					return barcodeInfo;
		}
		return null;
	}

	public int getSelectedPosition() {
		return this.selected;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setItem(int position, IsmBarcodeInfo ismBarcodeInfo) {
		mIsmBarcodeInfos.set(position, ismBarcodeInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.ism_ismcomplete_list_itemrow, null);
        	holder.checked = (CheckBox) convertView.findViewById(R.id.ismcomplete_list_isChecked);
        	holder.checked.setOnCheckedChangeListener(onCheckedChange);
        	
    		holder.mNewBarcode = (TextView) convertView.findViewById(R.id.ismcomplete_list_newBarcode);		
    		holder.mItemCode = (TextView) convertView.findViewById(R.id.ismcomplete_list_itemCode);;
    		holder.mItemName = (TextView) convertView.findViewById(R.id.ismcomplete_list_itemName); 
    		holder.mDevType = (TextView) convertView.findViewById(R.id.ismcomplete_list_devType);
    		holder.mPartType = (TextView) convertView.findViewById(R.id.ismcomplete_list_partType);
    		holder.mInjuryBarcode = (TextView) convertView.findViewById(R.id.ismcomplete_list_injuryBarcode);
    		holder.mPublicationWhyCode = (TextView) convertView.findViewById(R.id.ismcomplete_list_publicationWhyCode);
    		holder.mLocationCode = (TextView) convertView.findViewById(R.id.ismcomplete_list_locationCode);
    		holder.mDeviceId = (TextView) convertView.findViewById(R.id.ismcomplete_list_deviceId);
    		holder.mMakerItemYn = (TextView) convertView.findViewById(R.id.ismcomplete_list_makerItemYn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        /*
        if (selected == position) {
        	convertView.setTextColor(Color.BLUE);
        } else {
        	convertView.setTextColor(Color.BLACK);
        }
        */
        
        IsmBarcodeInfo model = getItem(position);

        holder.checked.setTag(position);
        holder.checked.setChecked(model.isChecked());  

		holder.mNewBarcode.setText(model.getNewBarcode());		
		holder.mItemCode.setText(model.getItemCode());;
		holder.mItemName.setText(model.getItemName()); 
		holder.mDevType.setText(model.getDevTypeName());
		holder.mPartType.setText(model.getPartTypeName());
		holder.mInjuryBarcode.setText(model.getInjuryBarcode());

		// 교체사유코드+교체사유명 - request by 김희선 - 2014.01.03 : DR-2013-57961
		holder.mPublicationWhyCode.setText(model.getPublicationWhyCode()+"-"+model.getPublicationWhyName());
		holder.mLocationCode.setText(model.getLocCd());
		holder.mDeviceId.setText(model.getDeviceId());
		
		// 재조사물자여부 B, M -> Y, N으로 변경 - request by 김희선 - 2014.01.03 : DR-2013-57961
		String makerItemYn = model.getMakerItemYn();
		if (makerItemYn.equals("B")) makerItemYn = "Y";
		else makerItemYn = "N";
		holder.mMakerItemYn.setText(makerItemYn);

        return convertView;
	}
	 
	public class ViewHolder {
		public CheckBox checked;
		public TextView mNewBarcode;		
		public TextView mItemCode;
		public TextView mItemName; 
		public TextView mDevType;
		public TextView mPartType;
		public TextView mInjuryBarcode;
		public TextView mPublicationWhyCode;
		public TextView mLocationCode;
		public TextView mDeviceId;
		public TextView mMakerItemYn;
    }

	public void changeSelectedPosition(int position) {
		this.selected = position;
		notifyDataSetChanged();
	}
}
