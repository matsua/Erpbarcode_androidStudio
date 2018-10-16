package com.ktds.erpbarcode.infosearch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.FailureListInfo;

public class FailureListAdapter extends BaseAdapter {

	private static final String TAG = "FailureListAdapter";

    private List<FailureListInfo> mFailureListInfo = null;
    private LayoutInflater mInflater;
    private String mode = "fcc";
    
    public FailureListAdapter(Context context) {
    	mFailureListInfo = new ArrayList<FailureListInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(FailureListInfo item) {
    	mFailureListInfo.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<FailureListInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mFailureListInfo.clear();
    	mFailureListInfo.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void setMode(String mode){
    	this.mode = mode;
    }
    
    public void itemClear() {
    	mFailureListInfo.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mFailureListInfo.size();
	}

	@Override
	public FailureListInfo getItem(int position) {
		return mFailureListInfo.get(position);
	}
	
	public List<FailureListInfo> getAllItems() {
		return mFailureListInfo;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public FailureListInfo getFailureListInfo(String barcode) {
		for (FailureListInfo failureInfo : mFailureListInfo) {
			if (failureInfo.getEqunr().equals(barcode)){
				return failureInfo;
			}
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		convertView = null;
    	holder = new ViewHolder();
    	convertView = mInflater.inflate(R.layout.infosearch_failure_list_itemrow, null);
    	
    	if(GlobalData.getInstance().getJobGubun().equals("고장수리이력")){
    		if(mode.equals("fcc")){
    			((LinearLayout)convertView.findViewById(R.id.fcc_list_item)).setVisibility(View.VISIBLE);
    			((LinearLayout)convertView.findViewById(R.id.dev_list_item)).setVisibility(View.GONE);
    			
    			holder.divId = (TextView) convertView.findViewById(R.id.failure_list_item1);
    			holder.erdat = (TextView) convertView.findViewById(R.id.failure_list_item2);
    			holder.ausbs = (TextView) convertView.findViewById(R.id.failure_list_item3);
    			holder.fecodn = (TextView) convertView.findViewById(R.id.failure_list_item4);
    			holder.urstx2 = (TextView) convertView.findViewById(R.id.failure_list_item5);
    			holder.lifnrn = (TextView) convertView.findViewById(R.id.failure_list_item6);
    		}
    		else if(mode.equals("dev")){
    			((LinearLayout)convertView.findViewById(R.id.dev_list_item)).setVisibility(View.VISIBLE);
    			((LinearLayout)convertView.findViewById(R.id.fcc_list_item)).setVisibility(View.GONE);
    			
    			holder.equnr = (TextView) convertView.findViewById(R.id.failure_list_dev_item1);
    			holder.matnr = (TextView) convertView.findViewById(R.id.failure_list_dev_item2);
    			holder.maktx = (TextView) convertView.findViewById(R.id.failure_list_dev_item3);
    			holder.erdat = (TextView) convertView.findViewById(R.id.failure_list_dev_item4);
    			holder.ausbs = (TextView) convertView.findViewById(R.id.failure_list_dev_item5);
    			holder.fecodn = (TextView) convertView.findViewById(R.id.failure_list_dev_item6);
    			holder.urstx2 = (TextView) convertView.findViewById(R.id.failure_list_dev_item7);
    			holder.lifnrn = (TextView) convertView.findViewById(R.id.failure_list_dev_item8);
    		}
    	}
    	else{
    		((LinearLayout)convertView.findViewById(R.id.dev_list_item)).setVisibility(View.GONE);
			((LinearLayout)convertView.findViewById(R.id.fcc_list_item)).setVisibility(View.VISIBLE);
			
    		holder.divId = (TextView) convertView.findViewById(R.id.failure_list_item1);
    		holder.erdat = (TextView) convertView.findViewById(R.id.failure_list_item2);
    		holder.ausbs = (TextView) convertView.findViewById(R.id.failure_list_item3);
    		holder.fecodn = (TextView) convertView.findViewById(R.id.failure_list_item4);
    		holder.urstx2 = (TextView) convertView.findViewById(R.id.failure_list_item5);
    		holder.lifnrn = (TextView) convertView.findViewById(R.id.failure_list_item6);
    	}
        convertView.setTag(holder);
        
        FailureListInfo model = getItem(position);
        
        if(holder.divId != null)holder.divId.setText(model.getZequipgc());
        if(holder.equnr != null)holder.equnr.setText(model.getEqunr());
        if(holder.matnr != null)holder.matnr.setText(model.getMatnr());
        if(holder.maktx != null)holder.maktx.setText(model.getMaktx());
        if(holder.erdat != null)holder.erdat.setText(model.getErdat());
        if(holder.ausbs != null)holder.ausbs.setText(model.getAusbs());
        if(holder.fecodn != null)holder.fecodn.setText(model.getFecodn());
        if(holder.urstx2 != null)holder.urstx2.setText(model.getUrstx2());
        if(holder.lifnrn != null)holder.lifnrn.setText(model.getLifnrn());
        
        return convertView;
	}

	public class ViewHolder {
		public TextView divId;
		public TextView equnr;
		public TextView matnr;
		public TextView maktx;
		public TextView erdat;
		public TextView ausbs;
		public TextView fecodn;
		public TextView urstx2;
		public TextView lifnrn;
    }

}
