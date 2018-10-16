package com.ktds.erpbarcode.job;

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
import com.ktds.erpbarcode.common.database.WorkInfo;

public class JobListAdapter extends BaseAdapter {
	
	private static final String TAG = "JobListAdapter";

	private int selectedPosition = -1;
    private final List<WorkInfo> mWorkInfos;
    private LayoutInflater mInflater;
    
    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            Log.d(TAG, "^^--^^ onCheckedChanged   position==>"+position);
            Log.d(TAG, "^^--^^ onCheckedChanged   isChecked==>"+isChecked);
            WorkInfo workInfo = getItem(position);
            if (workInfo.isChecked() == isChecked) return;
            
            workInfo.setChecked(isChecked);
        }
    };
    
    public JobListAdapter(Context context) {
    	mWorkInfos = new ArrayList<WorkInfo>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getSelectPosition() {
		return selectedPosition;
	}
	
	public void changeSelectPosition(int position) {
		selectedPosition = position;
		notifyDataSetChanged();
	}
    
    public void addItem(WorkInfo item) {
    	mWorkInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<WorkInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mWorkInfos.clear();
    	mWorkInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mWorkInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mWorkInfos.size();
	}

	@Override
	public WorkInfo getItem(int position) {
		return mWorkInfos.get(position);
	}
	
	public List<WorkInfo> getAllItems() {
		return mWorkInfos;
	}
	
	public List<WorkInfo> getCheckedItems() {
		List<WorkInfo> tempItems = new ArrayList<WorkInfo>();
		for (WorkInfo workInfo : getAllItems()) {
    		if (workInfo.isChecked()) {
    			tempItems.add(workInfo);
    		}
    	}
		return tempItems;
	}
	
	public void setCheckedItemsAll(boolean isCheckedAll) {
		System.out.println("setCheckItem :: " + isCheckedAll);
		for (WorkInfo workInfo : getAllItems()) {
			workInfo.setChecked(isCheckedAll);
			System.out.println("setCheckItem :: " + workInfo.isChecked());
    	}
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}
	
	private void removeItem(int _id) {
		mWorkInfos.remove(_id);
	}
	
	public void removeCheckedItems() {
		List<WorkInfo> checkWorkInfos = getAllItems();
    	for (int i=checkWorkInfos.size()-1; i>=0; i--) {
    		WorkInfo workInfo = checkWorkInfos.get(i);
    		Log.d(TAG, "^^--^^ removeCheckedItems   position,getWorkName,getInputTime,isChecked==>"+i+","+workInfo.getWorkName()+","+workInfo.getInputTime()+","+workInfo.isChecked());
    		
    		if (workInfo.isChecked()) {
    			removeItem(i);
    		}
		}
    	changeSelectPosition(-1);
    	notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.job_jobmanage_list_itemrow, null);
            holder.checked = (CheckBox) convertView.findViewById(R.id.jobmanage_list_checked);
            holder.checked.setOnCheckedChangeListener(onCheckedChange);
            holder.workName = (TextView) convertView.findViewById(R.id.jobmanage_list_workName);
            holder.inputTime = (TextView) convertView.findViewById(R.id.jobmanage_list_inputTime);
            holder.tranYn = (TextView) convertView.findViewById(R.id.jobmanage_list_tranYn);
            holder.message = (TextView) convertView.findViewById(R.id.jobmanage_list_message);
            holder.locCd = (TextView) convertView.findViewById(R.id.jobmanage_list_locCd);
            holder.deviceId = (TextView) convertView.findViewById(R.id.jobmanage_list_deviceId);
            holder.wbsNo = (TextView) convertView.findViewById(R.id.jobmanage_list_wbsNo);
            holder.offlineYn = (TextView) convertView.findViewById(R.id.jobmanage_list_offlineYn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final WorkInfo model = getItem(position);
        
        if (selectedPosition == position) {
        	convertView.setBackgroundColor(Color.rgb(203, 249, 181));
        } else {
        	convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.checked.setTag(position);
        holder.checked.setChecked(model.isChecked());
        holder.workName.setText(model.getWorkName());
        holder.inputTime.setText(model.getInputTime());
        holder.tranYn.setText(model.getTranYn());
        holder.message.setText(model.getTranRslt());
        holder.locCd.setText(model.getLocCd());
        holder.deviceId.setText(model.getDeviceId());
        holder.wbsNo.setText(model.getWbsNo());
        holder.offlineYn.setText(model.getOfflineYn());
		
        return convertView;
	}

	public class ViewHolder {
		public CheckBox checked;              // CheckBox
		public TextView workName;             // 작업구분
		public TextView inputTime;            // 작업시간
		public TextView tranYn;               // 전송여부
		public TextView message;              // 메시지
		public TextView locCd;                // 위치바코드
		public TextView deviceId;             // 장치아이디
		public TextView wbsNo;                // WBS번호
		public TextView offlineYn;            // 음영지역작업 여부
    }

}
