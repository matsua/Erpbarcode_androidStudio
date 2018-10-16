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
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;

public class GwlenListAdapter extends BaseAdapter {

	private static final String TAG = "GwlenListAdapter";

    private List<BarcodeListInfo> mBarcodeListInfos;
    private LayoutInflater mInflater;
    private int selected = -1;
    
    public GwlenListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBarcodeListInfos = new ArrayList<BarcodeListInfo>();
    }

    public void addItem(BarcodeListInfo item) {
    	mBarcodeListInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItemsNotClear(List<BarcodeListInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mBarcodeListInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<BarcodeListInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	selected = -1;
    	mBarcodeListInfos.clear();
    	mBarcodeListInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public int addItemNoDataChange(BarcodeListInfo item) {
    	mBarcodeListInfos.add(item);
    	return mBarcodeListInfos.size()-1;
    }
    
    public void itemClear() {
    	selected = -1;
    	mBarcodeListInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mBarcodeListInfos.size();
	}

	@Override
	public BarcodeListInfo getItem(int position) {
		return mBarcodeListInfos.get(position);
	}
	
	public List<BarcodeListInfo> getAllItems() {
		return mBarcodeListInfos;
	}
	
	public List<BarcodeListInfo> getCheckedItems() {
		List<BarcodeListInfo> tempItems = new ArrayList<BarcodeListInfo>();
		for (BarcodeListInfo barcodeInfo : mBarcodeListInfos) {
    		if (barcodeInfo.isChecked()) {
    			tempItems.add(barcodeInfo);
    		}
    	}
		return tempItems;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void removeItem(int position) {
		mBarcodeListInfos.remove(position);
	}
	
	public void removeItem(String barcode) {
		for (int i=mBarcodeListInfos.size()-1;i>=0; i--) {
    		BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
    		if (barcodeInfo.getBarcode().equals(barcode)) {
    			removeItem(i);
    		}	
		}
		notifyDataSetChanged();
	}
	
	public void removeCheckedItems() {
		for (int i=mBarcodeListInfos.size()-1;i>=0; i--) {
    		BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
    		if (barcodeInfo.isChecked()) {
    			removeItem(i);
    		}	
		}
    	notifyDataSetChanged();
	}

	public int getBarcodePosition(String barcode) {
		//Log.d(TAG, "getBarcodePosition   barcode==>"+barcode);
		for (int i=0; i<getCount(); i++) {
			BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
			if (barcodeInfo.getBarcode().equals(barcode))
				return i;
		}
		return -1;
	}
	
	public BarcodeListInfo getBarcodeListInfo(String barcode) {
		for (BarcodeListInfo barcodeInfo : mBarcodeListInfos) {
			if (barcodeInfo.getBarcode().equals(barcode))
					return barcodeInfo;
		}
		return null;
	}

	public int getSelectedPosition() {
		return this.selected;
	}
	
	public void changeSelectedPosition(int position) {
		this.selected = position;
		notifyDataSetChanged();
	}
	
	public void setItem(int position, BarcodeListInfo barcodeListInfo) {
		mBarcodeListInfos.set(position, barcodeListInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        BarcodeListInfo model = getItem(position);
        
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.gwlen_list_iemrow, null);
        	holder.barcodeText = (TextView) convertView.findViewById(R.id.list_fcc_no);
        	holder.gwlenText = (TextView) convertView.findViewById(R.id.list_gwlen_o);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.barcodeText.setText(model.getBarcode());              // 설비바코드
        holder.gwlenText.setText(model.getGwlen_o());                // 보증종료일
        
        System.out.println("====getView====[Barcode : " + model.getBarcode() + "] [Gwlen : " + model.getGwlen_o());
        return convertView;
	}

	public class ViewHolder {
		public TextView barcodeText;          // 설비바코드
		public TextView gwlenText;            // 보증종료일 
    }

	
}
