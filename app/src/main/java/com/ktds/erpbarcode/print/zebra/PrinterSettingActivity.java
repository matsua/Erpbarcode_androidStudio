package com.ktds.erpbarcode.print.zebra;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.widget.BasicSpinnerAdapter;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;
import com.ktds.erpbarcode.env.SettingPreferences;

public class PrinterSettingActivity extends Activity implements OnSeekBarChangeListener{
	private static final String TAG = "PrinterSettingActivity";
	
	public static final String INPUT_PRINT_TYPE = "print_type";
	private SettingPreferences mSharedSetting;
	private int x_coordinate;
	private int y_coordinate;
	private int xu_coordinate;
	private int yu_coordinate;
	private int darkness;
	
	private String type = "";
	private int barcodeType = 0;
	
	private EditText coordinateXEd, coordinateYEd;
	private EditText darknessEd;
	private SeekBar seekBar;
	
	private Spinner countSpinner;
	private String count = "";
	private BasicSpinnerAdapter spinnerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        
//		type -> 0:6x20 pdf417, 1:6x35 pdf417, 2:20x45 qrcode, 3:30x80 barcode, 5:6x58 pdf417, 6:7x50 pdf417, 7:30x80 barcode
        type = getIntent().getStringExtra(INPUT_PRINT_TYPE);
        
        setContentView(R.layout.printersetting_activity);
        mSharedSetting = new SettingPreferences(getApplicationContext());
        
        if(mSharedSetting.getPrinterUserInfo(type, "x") == null || (mSharedSetting.getPrinterUserInfo(type, "x")).equals("")){
        	try {
				mSharedSetting.setDefaultPrinterUserInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        setLayout();
	}
        
	private void setLayout() {
		x_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(type, "x"));
        y_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(type, "y"));
        
        xu_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(type, "xu"));
        yu_coordinate = Integer.parseInt(mSharedSetting.getPrinterUserInfo(type, "yu"));
        darkness = Integer.parseInt(mSharedSetting.getPrinterUserInfo(type, "sd"));
        
        if(xu_coordinate == 0) xu_coordinate = x_coordinate;
        if(yu_coordinate == 0) yu_coordinate = y_coordinate;
        
		Button close = (Button)findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				finish();
		     }
	     });
        
        coordinateXEd = (EditText)findViewById(R.id.editText1);
        coordinateXEd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	String value = v.getText().toString().trim();
                	if(value.length() < 1){
                		coordinateXEd.setText("" + xu_coordinate);
                		Toast toast = Toast.makeText(getApplicationContext(),"정확한 좌표를 입력해 주세요.", Toast.LENGTH_LONG);
                		toast.setGravity(Gravity.CENTER, 0, 0);
                		toast.show();
                		return false;
                	}
                    return true;
                }
                return false;
            }
        });

        coordinateYEd = (EditText)findViewById(R.id.editText1_1);
        coordinateYEd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	String value = v.getText().toString().trim();
                	if(value.length() < 1){
                		coordinateYEd.setText("" + yu_coordinate);
                		Toast toast = Toast.makeText(getApplicationContext(),"정확한 좌표를 입력해 주세요.", Toast.LENGTH_LONG);
                		toast.setGravity(Gravity.CENTER, 0, 0);
                		toast.show();
                		return false;
                	}
                    return true;
                }
                return false;
            }
        });
        
        coordinateXEd.setText("" + xu_coordinate);
        coordinateYEd.setText("" + yu_coordinate);
        
        darknessEd = (EditText)findViewById(R.id.editText2);
        darknessEd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	String value = v.getText().toString().trim();
                	if(value.length() < 1){
                		darknessEd.setText("" + darkness);
                		Toast toast = Toast.makeText(getApplicationContext(),"정확한 명암값을 입력해 주세요.", Toast.LENGTH_LONG);
                		toast.setGravity(Gravity.CENTER, 0, 0);
                		toast.show();
                		return false;
                	}
                	
                	if(Integer.parseInt(value) > 30){
                		darknessEd.setText("" + darkness);
                		Toast toast = Toast.makeText(getApplicationContext(),"명암의 범위는 1-30 입니다.", Toast.LENGTH_LONG);
                		toast.setGravity(Gravity.CENTER, 0, 0);
                		toast.show();
                		return false;
                	}
                	seekBar.setProgress(Integer.parseInt(value));
                	darkness = Integer.parseInt(value);
                    return true;
                }
                return false;
            }
        });
        
        darknessEd.setText("" + darkness);
        
        Button left = (Button)findViewById(R.id.button1);
        left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				coordinateChangeBtn(0);
		     }
	     });
        
        Button top = (Button)findViewById(R.id.button2);
        top.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				coordinateChangeBtn(1);
		     }
	     });
        
        Button bottom = (Button)findViewById(R.id.button3);
        bottom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				coordinateChangeBtn(2);
		     }
	     });
        
        Button right = (Button)findViewById(R.id.button4);
        right.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				coordinateChangeBtn(3);
		     }
	     });
        
        seekBar=(SeekBar)findViewById(R.id.seekBar1); 
        seekBar.setMax(30);
        seekBar.setProgress(darkness);
        seekBar.setOnSeekBarChangeListener(this);  
        
        //초기화
        Button initBtn = (Button)findViewById(R.id.button5);
        initBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				init();
		     }
	     });
        
        //저장
        Button saveBtn = (Button)findViewById(R.id.button6);
        saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				save();
		     }
	     });
        
        //출력
        Button printBtn = (Button)findViewById(R.id.button7);
        printBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ 
				print();
		     }
	     });
        
        countSpinner = (Spinner) findViewById(R.id.spinner1);
        count = "1";
		List<SpinnerInfo> spinneritems = new ArrayList<SpinnerInfo>();
		for(int c = 1; c < 21; c++){
			spinneritems.add(new SpinnerInfo(String.valueOf(c), String.valueOf(c)));
		}
		
		spinnerAdapter = new BasicSpinnerAdapter(getApplicationContext(), spinneritems);
		countSpinner.setAdapter(spinnerAdapter);
		countSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
	        	SpinnerInfo spinnerInfo = (SpinnerInfo) spinnerAdapter.getItem(position);
	        	countSpinner.setSelection(spinnerAdapter.getPosition(spinnerInfo.getCode()));
	        	count = spinnerInfo.getCode();
	        }
	        public void onNothingSelected(AdapterView<?> arg0) {
	        }
	    });
		
		
	}
	
	private void coordinateChangeBtn(int gb){
		if(gb == 0) //top
	        yu_coordinate++;
	    else if (gb == 1) //left 
	        xu_coordinate--;
	    else if (gb == 2) //right
	        xu_coordinate++;
	    else //bottom
	        yu_coordinate--;
		
		coordinateXEd.setText("" + xu_coordinate);
		coordinateYEd.setText("" + yu_coordinate);
	}
	
	private void init(){
		mSharedSetting.setPrinterUserInfo(type, "xu", String.valueOf(x_coordinate));
		mSharedSetting.setPrinterUserInfo(type, "yu", String.valueOf(y_coordinate));
		mSharedSetting.setPrinterUserInfo(type, "sd", "25");
		
		coordinateXEd.setText("" + x_coordinate);
		coordinateYEd.setText("" + y_coordinate);
		darknessEd.setText("25");
		
		Toast toast = Toast.makeText(getApplicationContext(),"설정 초기화를 완료 했습니다.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	private void save(){
		mSharedSetting.setPrinterUserInfo(type, "xu", String.valueOf(coordinateXEd.getText().toString()));
		mSharedSetting.setPrinterUserInfo(type, "yu", String.valueOf(coordinateYEd.getText().toString()));
		mSharedSetting.setPrinterUserInfo(type, "sd", darknessEd.getText().toString());
		
		Toast toast = Toast.makeText(getApplicationContext(),"설정 저장을 완료 했습니다. ", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	private void print(){
		int result, rscode = -1;
		
		BarcodePrintController bpc = new BarcodePrintController();
		String address = SettingsHelper.getBluetoothAddress(PrinterSettingActivity.this);
		
		result = bpc.printOpen(address);
		if(result == 1){
			rscode = bpc.printBarcodeDirectCommandTest(Integer.parseInt(type),xu_coordinate,yu_coordinate,darkness,Integer.parseInt(count));
		}else{
			rscode = result;
		}
		
		System.out.println("rscode :: " + rscode);
	}
	
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		darkness = seekBar.getProgress();
        darknessEd.setText("" + darkness);
    }
}
