package com.ktds.erpbarcode.survey;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
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
import com.ktds.erpbarcode.survey.model.CheckTerminalInfo;

public class CheckTerminalListAdapter extends BaseAdapter {

	private static final String TAG = "CheckTerminalListAdapter";

	private int selected = -1;
    private List<CheckTerminalInfo> mCheckTerminalInfos;
    private LayoutInflater mInflater;
    
    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            Log.d(TAG, "^^--^^ onCheckedChanged   position==>"+position);
            Log.d(TAG, "^^--^^ onCheckedChanged   isChecked==>"+isChecked);
            CheckTerminalInfo terminalInfo = getItem(position);
            if (terminalInfo.isChecked() == isChecked) return;
            
            terminalInfo.setChecked(isChecked);
            mCheckTerminalInfos.set(position, terminalInfo);
        }
    };
    
    
    public CheckTerminalListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCheckTerminalInfos = new ArrayList<CheckTerminalInfo>();
    }

    public void addItem(CheckTerminalInfo item) {
    	mCheckTerminalInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItemsNoClear(List<CheckTerminalInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mCheckTerminalInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<CheckTerminalInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	selected = -1;
    	mCheckTerminalInfos.clear();
    	mCheckTerminalInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public int addItemNoDataChange(CheckTerminalInfo item) {
    	mCheckTerminalInfos.add(item);
    	return mCheckTerminalInfos.size()-1;
    }
    
    public void itemClear() {
    	selected = -1;
    	mCheckTerminalInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mCheckTerminalInfos.size();
	}

	@Override
	public CheckTerminalInfo getItem(int position) {
		return mCheckTerminalInfos.get(position);
	}
	
	public List<CheckTerminalInfo> getAllItems() {
		return mCheckTerminalInfos;
	}
	
	public CheckTerminalInfo getCheckTerminalInfo(String barcode) {
		for (CheckTerminalInfo checkTerminalInfo : mCheckTerminalInfos) {
    		if (checkTerminalInfo.getTerminalCode().equals(barcode)) {
    			return checkTerminalInfo;
    		}
    	}
		return null;
	}
	
	public List<CheckTerminalInfo> getCheckedItems() {
		List<CheckTerminalInfo> tempItems = new ArrayList<CheckTerminalInfo>();
		for (CheckTerminalInfo checkTerminalInfo : mCheckTerminalInfos) {
    		if (checkTerminalInfo.isChecked()) {
    			tempItems.add(checkTerminalInfo);
    		}
    	}
		return tempItems;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void removeItem(int position) {
		mCheckTerminalInfos.remove(position);
	}
	
	public void removeCheckedItems() {
		List<CheckTerminalInfo> checkTerminalInfos = getAllItems();
    	for (int i=checkTerminalInfos.size()-1; i>=0; i--) {
    		CheckTerminalInfo checkTerminalInfo = checkTerminalInfos.get(i);
    		if (checkTerminalInfo.isChecked()) {
    			removeItem(i);
    		}	
		}
    	notifyDataSetChanged();
	}

	public int getSelectedPosition() {
		return this.selected;
	}
	
	public void changeSelectedPosition(int position) {
		this.selected = position;
		notifyDataSetChanged();
	}
	
	public void setItem(int position, CheckTerminalInfo barcodeListInfo) {
		mCheckTerminalInfos.set(position, barcodeListInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        CheckTerminalInfo model = getItem(position);
        
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.survey_checkterminal_list_itemrow, null);
            holder.checked = (CheckBox) convertView.findViewById(R.id.checkterminal_list_checked);
        	holder.checked.setOnCheckedChangeListener(onCheckedChange);
        	holder.numberText = (TextView) convertView.findViewById(R.id.checkterminal_list_number);
        	holder.terminalCodeText = (TextView) convertView.findViewById(R.id.checkterminal_list_terminalCode);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (selected == position) {
        	convertView.setBackgroundColor(Color.BLUE);
        } else {
        	convertView.setBackgroundColor(Color.WHITE);
        }

        holder.checked.setTag(position);
        holder.checked.setChecked(model.isChecked());
        holder.numberText.setText(String.valueOf(model.getNumber()));  // 번호
        holder.terminalCodeText.setText(model.getTerminalCode());      // 단말바코드

        return convertView;
	}

	public class ViewHolder {
		public CheckBox checked;              // CheckBox
		public TextView numberText;           // 번호
		public TextView terminalCodeText;     // 단말바코드
    }
}
