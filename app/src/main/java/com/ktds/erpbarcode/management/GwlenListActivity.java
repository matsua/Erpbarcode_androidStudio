package com.ktds.erpbarcode.management;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;

public class GwlenListActivity extends Activity {
	private ArrayList<BarcodeListInfo> mBarcodeListInfos;
	private GwlenListAdapter mListAdapter;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        
        setContentView(R.layout.gwlen_list_activity);
        
        mBarcodeListInfos = (ArrayList<BarcodeListInfo>) getIntent().getSerializableExtra("list");
        
        Button close = (Button)findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				Intent intent = new Intent();
				setResult(Activity.RESULT_OK, intent);
				finish();
		     }
	     });
        
        
        TextView totalCount = (TextView)findViewById(R.id.totalCount);
        totalCount.setText("Total : " + mBarcodeListInfos.size() + " 건 ");
        
//        Button cancel = (Button)findViewById(R.id.cancelBtn);
//        cancel.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v){ 
//				finish();
//		     }
//	     });
//        
//        Button send = (Button)findViewById(R.id.sendBtn);
//        send.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v){ 
//				Intent intent = new Intent();
//				setResult(Activity.RESULT_OK, intent);
//				finish();
//		     }
//	     });
        
        mListView = (ListView) findViewById(R.id.listView);
        mListAdapter = new GwlenListAdapter(this);
        mListView.setAdapter(mListAdapter);
        mListAdapter.addItems(mBarcodeListInfos);
	}
	
}
