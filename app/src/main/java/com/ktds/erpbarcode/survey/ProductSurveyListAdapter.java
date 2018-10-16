package com.ktds.erpbarcode.survey;

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
import com.ktds.erpbarcode.survey.model.ProductSurveyInfo;

public class ProductSurveyListAdapter extends BaseAdapter {

	private static final String TAG = "ProductSurveyListAdapter";

    private List<ProductSurveyInfo> mProductSurveyInfos = null;
    private LayoutInflater mInflater;
    
    
    public ProductSurveyListAdapter(Context context) {
    	mProductSurveyInfos = new ArrayList<ProductSurveyInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ProductSurveyInfo item) {
    	mProductSurveyInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<ProductSurveyInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mProductSurveyInfos.clear();
    	mProductSurveyInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mProductSurveyInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mProductSurveyInfos.size();
	}

	@Override
	public ProductSurveyInfo getItem(int position) {
		return mProductSurveyInfos.get(position);
	}
	
	public List<ProductSurveyInfo> getAllItems() {
		return mProductSurveyInfos;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void removeItem(String productCode) {
    	for (int i=0; i<mProductSurveyInfos.size(); i++) {
    		ProductSurveyInfo productSurveyInfo = mProductSurveyInfos.get(i);
			if (productSurveyInfo.getProductCode().equals(productCode)) {
				mProductSurveyInfos.remove(i);
			}
		}
    	notifyDataSetChanged();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	convertView = mInflater.inflate(R.layout.survey_productsurvey_list_itemrow, null);
        	holder.itemNumberText = (TextView) convertView.findViewById(R.id.productsurvey_list_itemNumber);
        	holder.productCodeText = (TextView) convertView.findViewById(R.id.productsurvey_list_productCode);
        	holder.productNameText = (TextView) convertView.findViewById(R.id.productsurvey_list_productName);
        	holder.quantityText = (TextView) convertView.findViewById(R.id.productsurvey_list_quantity);
        	holder.scanQuantityText = (TextView) convertView.findViewById(R.id.productsurvey_list_scanQuantity);
        	holder.batchNumberText = (TextView) convertView.findViewById(R.id.productsurvey_list_batchNumber);
        	
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        ProductSurveyInfo model = getItem(position);
        
        holder.itemNumberText.setText(model.getItemNumber());
        holder.productCodeText.setText(model.getProductCode());
        holder.productNameText.setText(model.getProductName());
        holder.quantityText.setText(String.valueOf(model.getQuantity()));
        holder.scanQuantityText.setText(String.valueOf(model.getScanQuantity()));
        holder.batchNumberText.setText(model.getBatchNumber());

        return convertView;
	}

	public class ViewHolder {
		public TextView itemNumberText;
		public TextView productCodeText;
		public TextView productNameText;
		public TextView quantityText;
		public TextView scanQuantityText;
		public TextView batchNumberText;
    }
}
