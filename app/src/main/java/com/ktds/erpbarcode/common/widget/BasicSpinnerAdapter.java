package com.ktds.erpbarcode.common.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class BasicSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private Context mContext;
	private List<SpinnerInfo> mItems;

	public BasicSpinnerAdapter(Context context){
    	mContext = context;
        mItems = new ArrayList<SpinnerInfo>();
    }
	
    public BasicSpinnerAdapter(Context context, List<SpinnerInfo> spinneritems){
    	mContext = context;
        mItems = spinneritems;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public int getPosition(String code) {
    	for (int i=0; i<mItems.size(); i++) {
    		SpinnerInfo spinnerInfo = mItems.get(i);
			if (spinnerInfo.getCode().equals(code)) {
				return i;
			}
		}
        return -1;
    }
    
    public int getRangePosition(String code) {
    	for (int i=0; i<mItems.size(); i++) {
    		SpinnerInfo spinnerInfo = mItems.get(i);
			if (spinnerInfo.getCode().contains(code)) {
				return i;
			}
		}
        return -1;
    }
    
    public void addItems(List<SpinnerInfo> items) {
    	mItems.clear();
    	mItems.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void removeItem(String code) {
    	for (int i=0; i<mItems.size(); i++) {
    		SpinnerInfo spinnerInfo = mItems.get(i);
			if (spinnerInfo.getCode().equals(code)) {
				mItems.remove(i);
			}
		}
    	notifyDataSetChanged();
    }
    
    public void itemClear() {
    	mItems.clear();
    	notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
        	//LinearLayout textBarLayout = new LinearLayout(mContext);
        	//textBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	//textBarLayout.setOrientation(LinearLayout.VERTICAL);
        	
        	holder = new ViewHolder();
        	holder.title = new TextView(mContext);
        	holder.title.setPadding(20, 0, 10, 0);
        	holder.title.setGravity(Gravity.LEFT);
        	holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        	holder.title.setTextColor(Color.rgb(34, 34, 34));
    		
        	//textBarLayout.addView(holder.title);
        	convertView = holder.title;
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SpinnerInfo model = mItems.get(position);
        holder.title.setText(model.getTitle());
        
        return convertView;
    }   

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
        	//LinearLayout textBarLayout = new LinearLayout(mContext);
        	//textBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	//textBarLayout.setOrientation(LinearLayout.VERTICAL);
        	
        	holder = new ViewHolder();
        	holder.title = new TextView(mContext);
        	holder.title.setPadding(20, 10, 20, 10);
        	holder.title.setGravity(Gravity.LEFT);
        	holder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        	
        	//textBarLayout.addView(holder.title);
        	convertView = holder.title;
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SpinnerInfo model = mItems.get(position);
        holder.title.setText(model.getTitle());

        return convertView;
    }

    private class ViewHolder{
        //public ImageView icon;
        public TextView title;
    }
}
