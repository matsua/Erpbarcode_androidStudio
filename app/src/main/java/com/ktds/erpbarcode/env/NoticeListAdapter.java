package com.ktds.erpbarcode.env;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.env.model.NoticeInfo;

public class NoticeListAdapter extends BaseAdapter {

	private static final String TAG = "NoticeListAdapter";

	private int selectedPosition = -1;

    private List<NoticeInfo> mNoticeInfos = null;
    private LayoutInflater mInflater;
    
    
    public NoticeListAdapter(Context context) {
    	mNoticeInfos = new ArrayList<NoticeInfo>();
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getSelectPosition() {
		return selectedPosition;
	}
	
	public void changeSelectPosition(int position) {
		selectedPosition = position;
		notifyDataSetChanged();
	}
	
	public void showOpenMessage(ViewGroup layout) {
		
	}

    public void addItem(NoticeInfo item) {
    	mNoticeInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<NoticeInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mNoticeInfos.clear();
    	mNoticeInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mNoticeInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mNoticeInfos.size();
	}

	@Override
	public NoticeInfo getItem(int position) {
		return mNoticeInfos.get(position);
	}
	
	public List<NoticeInfo> getAllItems() {
		return mNoticeInfos;
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
        	convertView = mInflater.inflate(R.layout.env_notice_list_itemrow, null);
        	holder.titleLayout = (RelativeLayout) convertView.findViewById(R.id.notice_list_title_layout);
        	holder.titleText = (TextView) convertView.findViewById(R.id.notice_list_title);
        	holder.messageText = (TextView) convertView.findViewById(R.id.notice_list_message);
        	
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        NoticeInfo model = getItem(position);
        
        holder.titleText.setText(model.getTitle());
        holder.messageText.setText(model.getMessage());

        return convertView;
	}

	public class ViewHolder {
		public RelativeLayout titleLayout;
		public TextView titleText;
		public TextView messageText;
    }
}
