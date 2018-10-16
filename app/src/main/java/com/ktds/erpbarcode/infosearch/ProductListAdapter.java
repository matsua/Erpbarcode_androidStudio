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

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.SuportLogic;
import com.ktds.erpbarcode.barcode.model.ProductInfo;

public class ProductListAdapter extends BaseAdapter {

	private static final String TAG = "ProductListAdapter";

    private List<ProductInfo> mProductInfos = null;
    private LayoutInflater mInflater;
    private int selectedPosition = -1;
    
    
    public ProductListAdapter(Context context) {
    	mProductInfos = new ArrayList<ProductInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ProductInfo item) {
    	mProductInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<ProductInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mProductInfos.clear();
    	mProductInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	selectedPosition = -1;
    	mProductInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mProductInfos.size();
	}

	@Override
	public ProductInfo getItem(int position) {
		return mProductInfos.get(position);
	}
	
	public List<ProductInfo> getAllItems() {
		return mProductInfos;
	}

	@Override
	public long getItemId(int position) {
		return position;
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
        	convertView = mInflater.inflate(R.layout.infosearch_selectproduct_list_itemrow, null);
        	holder.matnrText = (TextView) convertView.findViewById(R.id.selectproduct_list_matnr);
        	holder.maktxText = (TextView) convertView.findViewById(R.id.selectproduct_list_maktx);
        	holder.devTypeNameText = (TextView) convertView.findViewById(R.id.selectproduct_list_devTypeName);
        	holder.partTypeNameText = (TextView) convertView.findViewById(R.id.selectproduct_list_partTypeName);
        	holder.itemClassificationNameText = (TextView) convertView.findViewById(R.id.selectproduct_list_itemClassificationName);
        	holder.zemaft_nameText = (TextView) convertView.findViewById(R.id.selectproduct_list_zemaft_name);
        	holder.mtartText = (TextView) convertView.findViewById(R.id.selectproduct_list_mtart);
        	holder.barcdText = (TextView) convertView.findViewById(R.id.selectproduct_list_barcd);
        	
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (selectedPosition == position) {
        	convertView.setBackgroundColor(Color.rgb(203, 249, 181));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        
        ProductInfo model = getItem(position);
        
        String devTypeName = SuportLogic.getDevTypeName(model.getDevType());
		String partType = SuportLogic.getPartType(model.getPartTypeCode(), model.getDevType());
		String partTypeName = SuportLogic.getNodeStringType(partType);
		
		//-----------------------------------------------------------
		// 부품종류가 Equipment는 null처리한다.
		//-----------------------------------------------------------
        if (partTypeName.equals("단품")) {
        	partTypeName = "";
        }
		
		holder.matnrText.setText(model.getProductCode());
    	holder.maktxText.setText(model.getProductName());
    	holder.devTypeNameText.setText(devTypeName);
    	holder.partTypeNameText.setText(partTypeName);
    	holder.itemClassificationNameText.setText(model.getItemClassificationName());
    	holder.zemaft_nameText.setText(model.getZemaft_name());
    	holder.mtartText.setText(model.getMtart());
    	holder.barcdText.setText(model.getBarcd());

        return convertView;
	}

	public class ViewHolder {
		public TextView matnrText;         // 자재코드
		public TextView maktxText;         // 자재명
		public TextView devTypeNameText;   // 품목구분
		public TextView partTypeNameText;  // 부품종류
		public TextView itemClassificationNameText; // 자재분류
		public TextView zemaft_nameText;
		public TextView mtartText;
		public TextView barcdText;
    }

}
