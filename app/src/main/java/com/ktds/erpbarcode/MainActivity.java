package com.ktds.erpbarcode;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.ktds.erpbarcode.barcodeManagement.BarcodeManagementLocActivity;
import com.ktds.erpbarcode.base.ScanViewActivity;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeMessage;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.media.ScreenTools;
import com.ktds.erpbarcode.env.SettingActivity;
import com.ktds.erpbarcode.env.SettingPreferences;
import com.ktds.erpbarcode.env.bluetooth.BluetoothService;
import com.ktds.erpbarcode.env.bluetooth.PairingActivity;
import com.ktds.erpbarcode.env.bluetooth.ScannerConnectHelper;
import com.ktds.erpbarcode.infosearch.SelectDeviceActivity;
import com.ktds.erpbarcode.infosearch.SelectLocActivity;
import com.ktds.erpbarcode.infosearch.SelectProductActivity;
import com.ktds.erpbarcode.ism.EtcEquipmentActivity;
import com.ktds.erpbarcode.ism.IsmCompleteActivity;
import com.ktds.erpbarcode.ism.IsmManagementActivity;
import com.ktds.erpbarcode.ism.IsmRequestActivity;
import com.ktds.erpbarcode.job.JobMenageActivity;
import com.ktds.erpbarcode.management.RepairActivity;
import com.ktds.erpbarcode.management.TransferActivity;
import com.ktds.erpbarcode.management.TreeScanActivity;
import com.ktds.erpbarcode.survey.SpotCheckActivity;
import com.ktds.erpbarcode.survey.TerminalCheckActivity;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	// Back Button 2번 클릭시 처리..
	private final long FINSH_INTERVAL_TIME = 2000;
	private long backPressedTime = 0;
	
	private ScannerConnectHelper mScannerHelper;
	//private ErpbarDialog mErpbarDialog;
	private ScreenTools mScreenTools;
	private LinearLayout mRootMenuView;
	
	private EditText mUserNameText;
	
	private ViewGroup mOpenMenuView;
	private ViewGroup mOpenSubMenuView;
	private boolean isOpenSubMenu = false;
	//private ImageView mOpenSubMenuImageView;
	private ArrayList<GroupMenuItem> mGroupItems;
	private ArrayList<ArrayList<ChildMenuItem>> mChildItems;
	
	private UserLogoutServiceTask mUserLogoutTask;
	private SettingPreferences mSharedSetting;
	private ScrollView scrollView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//-----------------------------------------------------------
	    // ActionBar를 hide처리한다.
	    //----------------------------------------------------------- 
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//-----------------------------------------------------------
	    // ActionBar에 progress를 활성화 시킨다.
	    //-----------------------------------------------------------
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_main_activity);
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
	    //-----------------------------------------------------------
		GlobalData.getInstance().setJobGubun("메인");
		GlobalData.getInstance().setNowOpenActivity(this);
		
		//-----------------------------------------------------------
	    // 바코드스캐너를 연결한다.
	    //-----------------------------------------------------------
		initBarcodeScanner();
		
		mScreenTools = new ScreenTools(this);
		mRootMenuView = (LinearLayout) findViewById(R.id.main_rootView);

		//mErpbarDialog = new ErpbarDialog(this);

		menuDataSetting(); // 먼저 메뉴Data를 생성한다.
		createMenuView();
		
		setLayout();
		
		initScreen();
	}
	
    /**
     * 바코드스케너를 연결한다.
     */
    private void initBarcodeScanner() {
 		mScannerHelper = ScannerConnectHelper.getInstance();

 		//if (ScannerDeviceData.getInstance().isConnected()) {
 			Log.i(TAG, "initBarcodeScanner  getState()==>"+mScannerHelper.getState());
 			if ((mScannerHelper.getState() == BluetoothService.STATE_CONNECTING) ||
 				(mScannerHelper.getState() == BluetoothService.STATE_CONNECTED)) {
 				// 바코드 스케너가 연결된 상태이면...
 			} else {
 				boolean isInitBluetooth = mScannerHelper.initBluetooth(getApplicationContext());
 				if (isInitBluetooth) mScannerHelper.deviceConnect();
 			}
 		//}
    }
	
    /**
     * 화면 Layout.xml 설정한다.
     */
	private void setLayout() {
		scrollView = (ScrollView) findViewById(R.id.main_scrollView);
		
		findViewById(R.id.main_bottom_connectscanner_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//-------------------------------------------
						// 스캐너 연결은 작업구분 입력하지 않는다.
						//-------------------------------------------
						//GlobalData.getInstance().setJobGubun("스캐너 연결");
						
						Intent intent = new Intent(getApplicationContext(), PairingActivity.class);
				        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				        intent.putExtra(PairingActivity.INPUT_PAIRING_KIND, "scanner");
			            startActivity(intent);
					}
				});
		findViewById(R.id.main_bottom_taskmanagement_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//-------------------------------------------
						// 작업관리는 작업구분 입력하지 않는다.
						//-------------------------------------------
						//GlobalData.getInstance().setJobGubun("작업관리");
						
						// 작업관리 운영 막음
						//if (!SessionUserData.getInstance().getAccessServer().equals(HttpAddressConfig.APP_SERVER)) {
							Intent intent = new Intent(getApplicationContext(), JobMenageActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						//}
					}
				});
		findViewById(R.id.main_bottom_barcodesearch_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						GlobalData.getInstance().setJobGubun("설비정보");
						
						Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				        startActivity(intent);
					}
				});
		findViewById(R.id.main_bottom_setting_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						GlobalData.getInstance().setJobGubun("환경설정");
						
						Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			            startActivity(intent);
					}
				});
	}
	
	@Override
	public void onBackPressed() {
	    long tempTime        = System.currentTimeMillis();
	    long intervalTime    = tempTime - backPressedTime;

	    if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
	    	androidKillProcess();
	    }
	    else {
	        backPressedTime = tempTime; 
	        Toast.makeText(getApplicationContext(),"'뒤로'버튼을 한번더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
	    }
	}
	/**
	 * 앱을 로그라웃후 종료한다. 
	 */
	private void androidKillProcess() {
		//---------------------------------------------------------
		// 메인화면 호출시는 LoginActivity는 종료하고 호출한다.
		// **왜? 메인화면에서 종료하고 앱 다시 실행하면 IntroActivity이 항시 실행하기 위해서..
		//---------------------------------------------------------
    	mUserLogoutTask = new UserLogoutServiceTask();
    	mUserLogoutTask.execute((Void) null);
	}

	@Override
	protected void onResume() {
 		mScannerHelper.focusEditText(null);		// 메인으로 오면 스캐너 null 처리
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
				
		//------------------------------------------------------------
		// Main Activity 에서는 스케너 Focus를 삭제한다.
		//------------------------------------------------------------
		mScannerHelper.focusEditText(null);
	}

	@Override
	protected void onDestroy() {
		// 바코드 스케너를 종료한다.
		if (mScannerHelper!=null) {
			mScannerHelper.stop();
		}
				
		super.onDestroy();
	}
	
	private void initScreen() {

	}

	private GroupMenuItem getGroupItem(int position) {
		GroupMenuItem groupMenuItem = null;
		try {
			groupMenuItem = mGroupItems.get(position);
		} catch (Exception e) {
			groupMenuItem = null;
		}
		return groupMenuItem;
	}
	
	private ArrayList<ChildMenuItem> getChildItems(int position) {
		ArrayList<ChildMenuItem> childItems = null;
		try {
			childItems = mChildItems.get(position);
		} catch (Exception e) {
			childItems = null;
		}
		return childItems;
	}
	
	private ChildMenuItem getChildItem(int position, int subPosition) {
		ArrayList<ChildMenuItem> childItemsContent = getChildItems(position);
		if (childItemsContent==null) {
			return null;
		}
		ChildMenuItem childMenuItem = null;
		try {
			childMenuItem = childItemsContent.get(subPosition);
		} catch (Exception e) {
			childMenuItem = null;
		}
		return childMenuItem;
	}
	
	public class MenuOnClickListener implements OnClickListener {
		private int _Position;
		private GroupMenuItem _GroupMenuItem;

		public MenuOnClickListener(int position) {
			_Position = position;
			_GroupMenuItem = getGroupItem(position);
		}

		@Override
		public void onClick(View view) {
			if (mOpenMenuView != null) {
				int childCount = mOpenMenuView.getChildCount();
				for (int i=0; i<childCount; i++) {
					if (mOpenMenuView.getChildAt(i) instanceof ImageView ) {
						// 메인 이미지뷰에 선택이미지로 바꾼다.
						ImageView imageView = (ImageView) mOpenMenuView.getChildAt(i);
						int iconId = (Integer) imageView.getTag();
						imageView.setImageResource(iconId);
					}
				}
			}

			// 서브메뉴 열려 있으면 닫는다.
			if (isOpenSubMenu) {
				isOpenSubMenu = false;
				mOpenSubMenuView.setVisibility(View.GONE);
				mOpenSubMenuView.removeAllViews();
				
				if (mOpenMenuView == ((ViewGroup) view)) {
					return;
				}
			} 
			
			// 운영으로 접속 시 '정보조회도우미' 만 허용
			//if (SessionUserData.getInstance().getAccessServer().equals(HttpAddressConfig.APP_SERVER)) {
			//	if (!_GroupMenuItem.getGroupCode().equals("H")) {
			//		String message = "'정보조회도우미' 외 기능은\n\r2014.01.03 (금) 09:00시\n\r정식 오픈 후 사용 가능합니다.";
			//		GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(0, message, BarcodeSoundPlay.SOUND_NOTIFY));
			//		return;
			//	}
			//}
			
			mOpenMenuView = (ViewGroup) view;
			int childCount = mOpenMenuView.getChildCount();
			for (int i=0; i<childCount; i++) {
				if (mOpenMenuView.getChildAt(i) instanceof ImageView ) {
					// 메인 이미지뷰에 선택이미지로 바꾼다.
					ImageView imageView = (ImageView) mOpenMenuView.getChildAt(i);
					imageView.setImageResource(_GroupMenuItem.getUpIconId());
					imageView.setTag(_GroupMenuItem.getIconId());
				}
			}

			// 철거/설비상태변경은 바로 실행
			if (_GroupMenuItem.getGroupCode().equals("E")) {
				String jobGubun = _GroupMenuItem.getGroupName();
				if (_GroupMenuItem.getGroupCode().equals("E")) jobGubun="철거";
//				else if (_GroupMenuItem.getGroupCode().equals("F")) jobGubun="설비상태변경";

				//---------------------------------------------------
				// 철거, 설비상태변경 Activity를 오픈한다.
				//---------------------------------------------------
				GlobalData.getInstance().setJobGubun(jobGubun);
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
		        return;
			}

			// style_button_normal.xml 에서 제어할때 사용하면 됨.
			//view.setSelected(true);

			View parent = (View) view.getParent().getParent();
			LinearLayout clickRowView = (LinearLayout) parent;
			// SubMenuLayout 를 가져온다.
			LinearLayout childView = (LinearLayout) clickRowView.getChildAt(1);
			if (childView == null) {
				//
			} else {
				createSubMenuView(childView, _Position);
				if(parent.getY() > 900){
					scrollToView(parent,scrollView,0);
				}
			}
		}

	};
	
	public class SubMenuOnClickListener implements OnClickListener {
		private ChildMenuItem mChildMenuItem;

		public SubMenuOnClickListener(ChildMenuItem menuItem) {
			mChildMenuItem = menuItem;
		}

		@Override
		public void onClick(View view) {
			// 작업관리 싱글톤 Class에 값을 대입한다.
			GlobalData.getInstance().setJobGubun(mChildMenuItem.getChildName());
			
			/*
			if (mOpenSubMenuImageView != null) {
				int iconId = (Integer) mOpenSubMenuImageView.getTag();
				mOpenSubMenuImageView.setImageResource(iconId);
			}
			
			
			ViewGroup buttonView = (ViewGroup) view;
			
			int childCount = buttonView.getChildCount();
			for (int i=0; i<childCount; i++) {
				if (buttonView.getChildAt(i) instanceof ImageView ) {
					// 메인 이미지뷰에 선택이미지로 바꾼다.
					ImageView imageView = (ImageView) buttonView.getChildAt(i);
					imageView.setImageResource(mChildMenuItem.getUpIconId());
					imageView.setTag(mChildMenuItem.getIconId());
					
					mOpenSubMenuImageView = imageView;
				}
			} 
			*/
			
			
			if (mChildMenuItem.getChildCode().equals("a1")) {  // 납품입고
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("a2")) {  // 납품취소
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("a3")) {  // 배송출고
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("a4")) {  // 인계
				Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("a5")) {  // 인수
				
				// 음영지역은 인수메뉴는 사용못함.
				if (SessionUserData.getInstance().isOffline()) {
					String message = "음영지역에서는 사용하실 수 없습니다.";
					GlobalData.getInstance().showMessageDialog(new ErpBarcodeMessage(0, message, BarcodeSoundPlay.SOUND_NOTIFY));
					return;
				}
				
				Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("a6")) {  // 시설등록
				Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);

			} else if (mChildMenuItem.getChildCode().equals("b1")) {  // 입고(팀내)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b2")) {  // 출고(팀내)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b3")) {  // 실장
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b4")) {  // 탈장
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b5")) {  // 송부(팀간)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b6")) {  // 송부취소(팀간)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b7")) {  // 접수(팀간)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b8")) {  // 형상구성(창고내)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("b9")) {  // 형상해제(창고내)
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("c1")) {  // 인스토어마킹_바코드대체요청
				Intent intent = new Intent(getApplicationContext(), IsmRequestActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("c2")) {  // 인스토어마킹_부외실물등록요청
				Intent intent = new Intent(getApplicationContext(), EtcEquipmentActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("c3")) {  // 인스토어마킹_인스토어마킹완료
				Intent intent = new Intent(getApplicationContext(), IsmCompleteActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("c4")) {  // 인스토어마킹_인스토어마킹관리
				Intent intent = new Intent(getApplicationContext(), IsmManagementActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("d1")) {  // 고장등록
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d2")) {  // 고장등록취소
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d3")) {  // 수리의뢰취소
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d4")) {  // 수리완료
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d5")) {  // 개조개량의뢰
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d6")) {  // 개조개량의뢰취소
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("d7")) {  // 개조개량완료
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("e1")) {  // 철거
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("f1")) {  // 설비상태변경
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("f2")) {  // 설비S/N변경 
				Intent intent = new Intent(getApplicationContext(), RepairActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("g1")) {  // 현장점검(창고/실)
				Intent intent = new Intent(getApplicationContext(), SpotCheckActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("g2")) {  // 현장점검(베이)
				Intent intent = new Intent(getApplicationContext(), SpotCheckActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("g3")) {  // 상품단말실사
				Intent intent = new Intent(getApplicationContext(), TerminalCheckActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("g4")) {  // 임대단말실사
				Intent intent = new Intent(getApplicationContext(), TerminalCheckActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("h1")) {  // 자재정보
				Intent intent = new Intent(getApplicationContext(), SelectProductActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("h2")) {  // 설비정보
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("h3")) {  // 장치바코드정보
				Intent intent = new Intent(getApplicationContext(), SelectDeviceActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("h4")) {  // 위치바코드정보
				Intent intent = new Intent(getApplicationContext(), SelectLocActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
			} else if (mChildMenuItem.getChildCode().equals("h5")) {  // 고장수리이력
				Intent intent = new Intent(getApplicationContext(), TreeScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("i1")) {  // 위치바코드
				Intent intent = new Intent(getApplicationContext(), BarcodeManagementLocActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("i2")) {  // 장치바코드 
				Intent intent = new Intent(getApplicationContext(), BarcodeManagementLocActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}else if (mChildMenuItem.getChildCode().equals("i3")) {  // 소스마킹 
				Intent intent = new Intent(getApplicationContext(), BarcodeManagementLocActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			// sesang 20190910 기지국/중계기 위치바코드
			else if (mChildMenuItem.getChildCode().equals("i4")) {  // 기지국/중계기 위치바코드
				Intent intent = new Intent(getApplicationContext(), BarcodeManagementLocActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			// end sesang
            else if (mChildMenuItem.getChildCode().equals("j1") ||    // OA - 신규등록
                    mChildMenuItem.getChildCode().equals("j2") ||     // OA - 관리자변경
                    mChildMenuItem.getChildCode().equals("j4") ||    // OA - 불용요청
                    mChildMenuItem.getChildCode().equals("j5") ||    // OA - OA연식조회
                    mChildMenuItem.getChildCode().equals("j6") ||     // OA - 납품확인
                    mChildMenuItem.getChildCode().equals("j7") ||    // OA - 대여등록
                    mChildMenuItem.getChildCode().equals("j8") ||     // OA - 대여반납
                    mChildMenuItem.getChildCode().equals("k1") ||     // OE - 신규등록
                    mChildMenuItem.getChildCode().equals("k2") ||     // OE - 관리자변경
                    mChildMenuItem.getChildCode().equals("k4") ||     // OE - 불용요청
                    mChildMenuItem.getChildCode().equals("k5") ||     // OE - OE연식조회
                    mChildMenuItem.getChildCode().equals("k6") ||     // OE - 납품확인
                    mChildMenuItem.getChildCode().equals("k7") ||     // OE - 대여등록
                    mChildMenuItem.getChildCode().equals("k8")) {   // OE - 대여반납
                Intent intent = new Intent(getApplicationContext(), ScanViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
		}
	};
	
	public static void scrollToView(View view, final ScrollView scrollView, int count) {
	     if (view != null && view != scrollView) {
	         count += view.getTop();
	         scrollToView((View) view.getParent(), scrollView, count);
	     } else if (scrollView != null) {
	         final int finalCount = count;
	         new Handler().postDelayed(new Runnable() {
	  
	             @Override
	             public void run() {
	                 scrollView.smoothScrollTo(0, finalCount);
	             }
	         }, 200);
	     }
	 }

	private void createMenuView() {

		int screenWidths = mScreenTools.getScreenWidth();
		int blank = mScreenTools.toPixels(3+3);  // 좌,우 padding값을 뺀다.
		int menuBlankSize = (screenWidths / 3) - blank;

		
		RelativeLayout.LayoutParams headerLy1 = new RelativeLayout.LayoutParams(200, 42);
		headerLy1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		//-----------------------------------------------------------
		// 메인메뉴 동적 생성.
		//-----------------------------------------------------------
		int menuNumberLines = getMenuNumberLines(mGroupItems.size());

		int row = 0;
		for (int i = 0; i < menuNumberLines; i++) {
			
			LinearLayout mainRowView = new LinearLayout(getBaseContext());
			mainRowView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, menuBlankSize));
			mainRowView.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout subRowView = new LinearLayout(getBaseContext());
			subRowView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			subRowView.setOrientation(LinearLayout.VERTICAL);
			subRowView.setVisibility(View.GONE);

			LinearLayout rowLayout = new LinearLayout(getBaseContext());
			rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rowLayout.setOrientation(LinearLayout.VERTICAL);
			int padding = mScreenTools.toPixels(10);
			rowLayout.setPadding(padding, 0, padding, 0);
			
			rowLayout.addView(mainRowView);
			rowLayout.addView(subRowView);

			for (int ii = 0; ii < 3; ii++) {
				GroupMenuItem groupMenuItem = getGroupItem(row);

				LinearLayout.LayoutParams ly = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
				ly.weight = Float.valueOf("1");

				RelativeLayout menuLayoutView = new RelativeLayout(getBaseContext());
				menuLayoutView.setLayoutParams(ly);
				menuLayoutView.setBackgroundColor(Color.WHITE);
				
				//---------------------------------------------------
				// 상위 라인을 그린다.
				//---------------------------------------------------
				if (i==0) {
					RelativeLayout.LayoutParams lineLy1 = 
							new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mScreenTools.toPixels(1));
					lineLy1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					
					LinearLayout grayline1 = new LinearLayout(getBaseContext());
					grayline1.setOrientation(LinearLayout.VERTICAL);
					grayline1.setBackgroundColor(Color.rgb(243, 243, 243));
					
					menuLayoutView.addView(grayline1, lineLy1);
				}
				
				//---------------------------------------------------
				// 세로 라인을 그린다.
				//---------------------------------------------------
				if (ii < 2) {
					RelativeLayout.LayoutParams lineLy2 = 
							new RelativeLayout.LayoutParams(mScreenTools.toPixels(1), LayoutParams.MATCH_PARENT);
					lineLy2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					
					LinearLayout grayline2 = new LinearLayout(getBaseContext());
					grayline2.setOrientation(LinearLayout.VERTICAL);
					grayline2.setBackgroundColor(Color.rgb(243, 243, 243));
					
					menuLayoutView.addView(grayline2, lineLy2);
				}
				
				//---------------------------------------------------
				// 아래 라인을 그린다.
				//---------------------------------------------------
				RelativeLayout.LayoutParams lineLy3 = 
						new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mScreenTools.toPixels(1));
				lineLy3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				
				LinearLayout grayline3 = new LinearLayout(getBaseContext());
				grayline3.setOrientation(LinearLayout.VERTICAL);
				grayline3.setBackgroundColor(Color.rgb(243, 243, 243));
				
				menuLayoutView.addView(grayline3, lineLy3);
				
				//---------------------------------------------------
				// 메뉴 이미지를 등록하고 이미지버튼  Event를 설정한다.
				//---------------------------------------------------
				if (groupMenuItem != null) {
					if (!groupMenuItem.getGroupCode().equals("VERSION")) {
						RelativeLayout.LayoutParams mainimageLy = 
								new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
						mainimageLy.addRule(RelativeLayout.CENTER_IN_PARENT);
						
						ImageView imageView = new ImageView(getBaseContext());
						imageView.setLayoutParams(mainimageLy);
						imageView.setScaleType(ImageView.ScaleType.CENTER);
						imageView.setImageResource(groupMenuItem.getIconId());
	
						menuLayoutView.addView(imageView);
						menuLayoutView.setFocusable(true);
						menuLayoutView.setClickable(true);
						
						MenuOnClickListener menuOnClickListener = new MenuOnClickListener(row);
						menuLayoutView.setOnClickListener(menuOnClickListener);
					} else {
						//---------------------------------------------------
						// 접속 서버(운영, QA, 개발)명을 표기한다.
						// 어플리케이션 버젼.
						//---------------------------------------------------
						String appVersion = "Version ";
						appVersion += GlobalData.getInstance().getAppVersionName();
						
						//---------------------------------------------------
						// 접속 서버(운영, QA, 개발) TextView
						//---------------------------------------------------
						TextView serverView = new TextView(getApplicationContext());
						serverView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						serverView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
						serverView.setTextColor(Color.BLACK);
						serverView.setTypeface(null, Typeface.BOLD);
						serverView.setText(SessionUserData.getInstance().getAccessServerName());
						
						Space spaceView = new Space(getBaseContext());
						spaceView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, mScreenTools.toPixels(16)));
						
						//---------------------------------------------------
						// 어플리케이션 버젼. TextView
						//---------------------------------------------------
						TextView versionView = new TextView(getBaseContext());
						versionView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						versionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
						versionView.setTextColor(Color.BLACK);
						versionView.setText(appVersion);
						
						if (SessionUserData.getInstance().getAccessServerName().equals("운영")) {
							serverView.setTextColor(Color.BLUE);
							versionView.setTextColor(Color.BLUE);
						}
						
						
						mUserNameText = (EditText) findViewById(R.id.main_username);
				    	String userName = "";
				    	
				    	if (SessionUserData.getInstance().isAuthenticated()) {
				    		userName = SessionUserData.getInstance().getUserName().replace(SessionUserData.getInstance().getUserName().charAt(SessionUserData.getInstance().getUserName().length() - 1),'*') +"님 환영합니다.";
				    	}
				        
				        mUserNameText.setText(userName);
						
						//---------------------------------------------------
						// 중앙으로 표기하기 위해서 RelativeLayout.LayoutParams으로 설정한다.
						//---------------------------------------------------
						RelativeLayout.LayoutParams versionLy = 
								new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
						versionLy.addRule(RelativeLayout.CENTER_IN_PARENT);
						
						LinearLayout versionLayout = new LinearLayout(getBaseContext());
						versionLayout.setLayoutParams(versionLy);
						versionLayout.setOrientation(LinearLayout.VERTICAL);
						versionLayout.setGravity(Gravity.CENTER_HORIZONTAL);
						
						versionLayout.addView(serverView);
						versionLayout.addView(spaceView);
						versionLayout.addView(versionView);
						
						menuLayoutView.addView(versionLayout);
					}
				}
				
				mainRowView.addView(menuLayoutView);

				row++;
			}

			mRootMenuView.addView(rowLayout);
		}

		//-----------------------------------------------------------
		// 바닥글
		//-----------------------------------------------------------
		LinearLayout blank2BaseView = new LinearLayout(getBaseContext());
		blank2BaseView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, mScreenTools.toPixels(0)));
		blank2BaseView.setOrientation(LinearLayout.VERTICAL);
		blank2BaseView.setBackgroundColor(Color.WHITE);
		mRootMenuView.addView(blank2BaseView);
	}

	private void createSubMenuView(LinearLayout subView, int row) {
		
		ArrayList<ChildMenuItem> childItems = getChildItems(row);
		
		if (childItems==null) return;
		if (childItems.size() == 0) return;
		
		// 서버메뉴View 상태값과 화면을 활성화 한다.
		isOpenSubMenu = true;
		subView.setVisibility(View.VISIBLE);
		subView.setBackgroundColor(Color.rgb(215, 215, 215));  // style_white_smoke
		
		//-----------------------------------------------------------
		// 서브메뉴 동적 생성.
		//-----------------------------------------------------------
		int totalSubLineCount = getMenuNumberLines(childItems.size());

		int subrow = 0;
		for (int i = 0; i < totalSubLineCount; i++) {
			int padding = mScreenTools.toPixels(4);
			LinearLayout submenuLayout = new LinearLayout(getBaseContext());
			submenuLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			submenuLayout.setOrientation(LinearLayout.HORIZONTAL);
			submenuLayout.setPadding(padding, padding, padding, 0);
			
			for (int ii = 0; ii < 3; ii++) {
				ChildMenuItem childMenuItem = getChildItem(row, subrow);

				int margin = mScreenTools.toPixels(2);
				LinearLayout.LayoutParams sub_ly = new LinearLayout.LayoutParams(0, mScreenTools.toPixels(50));
				sub_ly.weight = Float.valueOf("1");
				sub_ly.setMargins(margin, margin, margin, 0);

				LinearLayout subbtnLayout = new LinearLayout(getBaseContext());
				subbtnLayout.setLayoutParams(sub_ly);
				subbtnLayout.setOrientation(LinearLayout.VERTICAL);

				if (childMenuItem != null) {

					RelativeLayout.LayoutParams subimage_ly = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					subimage_ly.addRule(RelativeLayout.CENTER_IN_PARENT);
					
//					ImageView imageView = new ImageView(getBaseContext());
//					imageView.setLayoutParams(subimage_ly);
//					imageView.setScaleType(ImageView.ScaleType.CENTER);
//					imageView.setImageResource(childMenuItem.getIconId());

					TextView textView = new TextView(getBaseContext());
					textView.setLayoutParams(subimage_ly);
					textView.setGravity(Gravity.CENTER);
					textView.setText(childMenuItem.getChildName());
					textView.setTextSize(13);
					textView.setTextColor(0xff827880);// 130, 120, 128
					//imageView.setScaleType(ImageView.ScaleType.CENTER);
					//imageView.setImageResource(childMenuItem.getIconId());

					RelativeLayout subbutton = new RelativeLayout(getBaseContext());
					subbutton.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					subbutton.setBackgroundResource(R.drawable.style_button_bg_submenu);
					subbutton.addView(textView);
					subbutton.setFocusable(true);
					subbutton.setClickable(true);
					

					SubMenuOnClickListener subMenuOnClickListener = new SubMenuOnClickListener(childMenuItem);
					subbutton.setOnClickListener(subMenuOnClickListener);
					
					subbtnLayout.addView(subbutton);
				}
				
				submenuLayout.addView(subbtnLayout);
				
				subrow++;
			}
			
			subView.addView(submenuLayout);
		}
		
		// 6dp 공백을 추가한다.
		LinearLayout sub_blank = new LinearLayout(getBaseContext());
		sub_blank.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mScreenTools.toPixels(6)));
		sub_blank.setOrientation(LinearLayout.VERTICAL);
		subView.addView(sub_blank);
		
		
		int menuNumberLines = getMenuNumberLines(mGroupItems.size());
		int numberLine = getMenuNumberLines(row+1);
		
		//Log.i(TAG, "createSubMenuView  row==>"+row);
		//Log.i(TAG, "createSubMenuView  menuNumberLines,numberLine==>"+menuNumberLines+","+numberLine);
		if (menuNumberLines > numberLine) {
			// 서브메뉴 바닥라인을 그린다.
			LinearLayout grayline4 = new LinearLayout(getBaseContext());
			grayline4.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mScreenTools.toPixels(1)));
			grayline4.setOrientation(LinearLayout.VERTICAL);
			grayline4.setBackgroundColor(Color.rgb(243, 243, 243));
			subView.addView(grayline4);
		}
		
		mOpenSubMenuView = subView;
	}
	
	private int getMenuNumberLines(int recordCount) {
		if (recordCount==0) return 0;
		
		int lineCount = recordCount / 3;
		int lineRemainder = recordCount - (lineCount * 3);
		if (lineRemainder > 0) {
			lineCount = lineCount + 1;
		}
		
		return lineCount;
	}

	private void menuDataSetting() {

		mGroupItems = new ArrayList<GroupMenuItem>();
		mChildItems = new ArrayList<ArrayList<ChildMenuItem>>();

		GroupMenuItem groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_a);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_a_up);
		groupMenuItem.setGroupCode("A");
		groupMenuItem.setGroupName("입고/인계인수");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_b);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_b_up);
		groupMenuItem.setGroupCode("B");
		groupMenuItem.setGroupName("유동관리");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_c);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_c_up);
		groupMenuItem.setGroupCode("C");
		groupMenuItem.setGroupName("인스토어마킹");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_d);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_d_up);
		groupMenuItem.setGroupCode("D");
		groupMenuItem.setGroupName("수리/개조개량");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_e);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_e_up);
		groupMenuItem.setGroupCode("E");
		groupMenuItem.setGroupName("철거관리");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_f);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_f_up);
		groupMenuItem.setGroupCode("F");
		groupMenuItem.setGroupName("상태변경");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_g);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_g_up);
		groupMenuItem.setGroupCode("G");
		groupMenuItem.setGroupName("재물조사");
		mGroupItems.add(groupMenuItem);

		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_h);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_h_up);
		groupMenuItem.setGroupCode("H");
		groupMenuItem.setGroupName("정보조회도우미");
		mGroupItems.add(groupMenuItem);
		
		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(R.drawable.main_button_menu_i);
		groupMenuItem.setUpIconId(R.drawable.main_button_menu_i_up);
		groupMenuItem.setGroupCode("I");
		groupMenuItem.setGroupName("바코드관리");
		mGroupItems.add(groupMenuItem);
		
        groupMenuItem = new GroupMenuItem();
        groupMenuItem.setIconId(R.drawable.main_button_menu_j);
        groupMenuItem.setUpIconId(R.drawable.main_button_menu_j_up);
        groupMenuItem.setGroupCode("J");
        groupMenuItem.setGroupName("OA");
        mGroupItems.add(groupMenuItem);
        
        groupMenuItem = new GroupMenuItem();
        groupMenuItem.setIconId(R.drawable.main_button_menu_k);
        groupMenuItem.setUpIconId(R.drawable.main_button_menu_k_up);
        groupMenuItem.setGroupCode("k");
        groupMenuItem.setGroupName("OE");
        mGroupItems.add(groupMenuItem);
		
//		groupMenuItem = new GroupMenuItem();
//		groupMenuItem.setIconId(0);
//		groupMenuItem.setUpIconId(0);
//		groupMenuItem.setGroupCode("");
//		groupMenuItem.setGroupName("");
//		mGroupItems.add(groupMenuItem);
		
		groupMenuItem = new GroupMenuItem();
		groupMenuItem.setIconId(0);
		groupMenuItem.setUpIconId(0);
		groupMenuItem.setGroupCode("VERSION");
		groupMenuItem.setGroupName("버젼정보");
		mGroupItems.add(groupMenuItem);

		
		// ---------------------------------------------------------------------
		// 서브메뉴 ( 입고/인계인수 )
		ArrayList<ChildMenuItem> childItemsContent = new ArrayList<ChildMenuItem>();
		ChildMenuItem childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a1);
		childMenuItem.setChildCode("a1");
		childMenuItem.setChildName("납품입고");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a2);
		childMenuItem.setChildCode("a2");
		childMenuItem.setChildName("납품취소");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a3);
		childMenuItem.setChildCode("a3");
		childMenuItem.setChildName("배송출고");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a4);
		childMenuItem.setChildCode("a4");
		childMenuItem.setChildName("인계");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a5);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a5);
		childMenuItem.setChildCode("a5");
		childMenuItem.setChildName("인수");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_a6);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_a6);
		childMenuItem.setChildCode("a6");
		childMenuItem.setChildName("시설등록");
		childItemsContent.add(childMenuItem);

		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 유동관리 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b1);
		childMenuItem.setChildCode("b1");
		childMenuItem.setChildName("입고(팀내)");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b2);
		childMenuItem.setChildCode("b2");
		childMenuItem.setChildName("출고(팀내)");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b3);
		childMenuItem.setChildCode("b3");
		childMenuItem.setChildName("실장");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b4);
		childMenuItem.setChildCode("b4");
		childMenuItem.setChildName("탈장");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b5);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b5);
		childMenuItem.setChildCode("b5");
		childMenuItem.setChildName("송부(팀간)");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b6);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b6);
		childMenuItem.setChildCode("b6");
		childMenuItem.setChildName("송부취소(팀간)");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b7);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b7);
		childMenuItem.setChildCode("b7");
		childMenuItem.setChildName("접수(팀간)");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b8);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b8);
		childMenuItem.setChildCode("b8");
		childMenuItem.setChildName("형상구성(창고내)");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_b9);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_b9);
		childMenuItem.setChildCode("b9");
		childMenuItem.setChildName("형상해제(창고내)");
		childItemsContent.add(childMenuItem);

		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 인스토어마킹 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_c1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_c1);
		childMenuItem.setChildCode("c1");
		childMenuItem.setChildName("바코드대체요청");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_c2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_c2);
		childMenuItem.setChildCode("c2");
		childMenuItem.setChildName("부외실물등록요청");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_c3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_c3);
		childMenuItem.setChildCode("c3");
		childMenuItem.setChildName("인스토어마킹완료");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_i1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_i1);
		childMenuItem.setChildCode("c4");
		childMenuItem.setChildName("인스토어마킹관리");
		childItemsContent.add(childMenuItem);
		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 수리/개조개량 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d1);
		childMenuItem.setChildCode("d1");
		childMenuItem.setChildName("고장등록");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d2);
		childMenuItem.setChildCode("d2");
		childMenuItem.setChildName("고장등록취소");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d3);
		childMenuItem.setChildCode("d3");
		childMenuItem.setChildName("수리의뢰취소");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d4);
		childMenuItem.setChildCode("d4");
		childMenuItem.setChildName("수리완료");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d5);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d5);
		childMenuItem.setChildCode("d5");
		childMenuItem.setChildName("개조개량의뢰");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d6);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d6);
		childMenuItem.setChildCode("d6");
		childMenuItem.setChildName("개조개량의뢰취소");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_d7);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_d7);
		childMenuItem.setChildCode("d7");
		childMenuItem.setChildName("개조개량완료");
		childItemsContent.add(childMenuItem);
		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 철거관리 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.ic_launcher);
		childMenuItem.setUpIconId(R.drawable.ic_launcher);
		childMenuItem.setChildCode("e1");
		childMenuItem.setChildName("철거");
		childItemsContent.add(childMenuItem);

		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 상태변경 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_f1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_f1);
		childMenuItem.setChildCode("f1");
		childMenuItem.setChildName("설비상태변경");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_f2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_f2);
		childMenuItem.setChildCode("f2");
		childMenuItem.setChildName("설비S/N변경");
		childItemsContent.add(childMenuItem);

		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 재물조사 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_g1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_g1);
		childMenuItem.setChildCode("g1");
		childMenuItem.setChildName("현장점검(창고/실)");
		childItemsContent.add(childMenuItem);
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_g2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_g2);
		childMenuItem.setChildCode("g2");
		childMenuItem.setChildName("현장점검(베이)");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_g3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_g3);
		childMenuItem.setChildCode("g3");
		childMenuItem.setChildName("상품단말실사");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_g4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_g4);
		childMenuItem.setChildCode("g4");
		childMenuItem.setChildName("임대단말실사");
		childItemsContent.add(childMenuItem);

		mChildItems.add(childItemsContent);

		// ---------------------------------------------------------------------
		// 서브메뉴 ( 정보조회도우미 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_h1);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_h1);
		childMenuItem.setChildCode("h1");
		childMenuItem.setChildName("자재정보");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_h2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_h2);
		childMenuItem.setChildCode("h2");
		childMenuItem.setChildName("설비정보");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_h3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_h3);
		childMenuItem.setChildCode("h3");
		childMenuItem.setChildName("장치바코드정보");
		childItemsContent.add(childMenuItem);
				
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_h4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_h4);
		childMenuItem.setChildCode("h4");
		childMenuItem.setChildName("위치바코드정보");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_h5);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_h5);
		childMenuItem.setChildCode("h5");
		childMenuItem.setChildName("고장수리이력");
		childItemsContent.add(childMenuItem);
	
		mChildItems.add(childItemsContent);
		
		// ---------------------------------------------------------------------
		// 서브메뉴 ( 바코드관리 )
		childItemsContent = new ArrayList<ChildMenuItem>();
		
		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_i3);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_i3);
		childMenuItem.setChildCode("i1");
		childMenuItem.setChildName("위치바코드");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_i2);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_i2);
		childMenuItem.setChildCode("i2");
		childMenuItem.setChildName("장치바코드");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_i4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_i4);
		childMenuItem.setChildCode("i3");
		childMenuItem.setChildName("소스마킹");
		childItemsContent.add(childMenuItem);

		childMenuItem = new ChildMenuItem();
		childMenuItem.setIconId(R.drawable.main_button_menu_i4);
		childMenuItem.setUpIconId(R.drawable.main_button_menu_i4);
		childMenuItem.setChildCode("i4");
		childMenuItem.setChildName("기지국/중계기 위치바코드");
		childItemsContent.add(childMenuItem);
	
		mChildItems.add(childItemsContent);
		
		// ---------------------------------------------------------------------
		// 서브메뉴 ( BASE_OA )
        childItemsContent = new ArrayList<ChildMenuItem>();
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk1);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk1);
        childMenuItem.setChildCode("j1");
        childMenuItem.setChildName("OA신규등록");
        childItemsContent.add(childMenuItem);

        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk2);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk2);
        childMenuItem.setChildCode("j2");
        childMenuItem.setChildName("OA관리자변경");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk4);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk4);
        childMenuItem.setChildCode("j4");
        childMenuItem.setChildName("OA불용요청");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_j5);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_j5);
        childMenuItem.setChildCode("j5");
        childMenuItem.setChildName("OA연식조회");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk6);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk6);
        childMenuItem.setChildCode("j6");
        childMenuItem.setChildName("OA납품확인");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk7);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk7);
        childMenuItem.setChildCode("j7");
        childMenuItem.setChildName("OA대여등록");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk8);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk8);
        childMenuItem.setChildCode("j8");
        childMenuItem.setChildName("OA대여반납");
        childItemsContent.add(childMenuItem);
        
        mChildItems.add(childItemsContent);
        
        // ---------------------------------------------------------------------
        // 서브메뉴 ( BASE_OE )
        childItemsContent = new ArrayList<ChildMenuItem>();
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk1);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk1);
        childMenuItem.setChildCode("k1");
        childMenuItem.setChildName("OE신규등록");
        childItemsContent.add(childMenuItem);

        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk2);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk2);
        childMenuItem.setChildCode("k2");
        childMenuItem.setChildName("OE관리자변경");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk4);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk4);
        childMenuItem.setChildCode("k4");
        childMenuItem.setChildName("OE불용요청");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_k5);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_k5);
        childMenuItem.setChildCode("k5");
        childMenuItem.setChildName("OE연식조회");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk6);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk6);
        childMenuItem.setChildCode("k6");
        childMenuItem.setChildName("OE납품확인");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk7);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk7);
        childMenuItem.setChildCode("k7");
        childMenuItem.setChildName("OE대여등록");
        childItemsContent.add(childMenuItem);
        
        childMenuItem = new ChildMenuItem();
        childMenuItem.setIconId(R.drawable.main_button_menu_jk8);
        childMenuItem.setUpIconId(R.drawable.main_button_menu_jk8);
        childMenuItem.setChildCode("k8");
        childMenuItem.setChildName("OE대여반납");
        childItemsContent.add(childMenuItem);
        
        mChildItems.add(childItemsContent);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult  여기오나~~");
		
		//-----------------------------------------------------------
		// Open된 Activity를 Set한다.
		//-----------------------------------------------------------
		GlobalData.getInstance().setNowOpenActivity(this);
				
		//------------------------------------------------------------
		// Main Activity 에서는 스케너 Focus를 삭제한다.
		//------------------------------------------------------------
		mScannerHelper.focusEditText(null);
	}
	
	public class UserLogoutServiceTask extends AsyncTask<Void, Void, Boolean> {
		private String _errorMessage = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			// -------------------------------------------------------
			// 로그아웃 - Destroy 외에 로그아웃, 서비스 기능 종료
			// -------------------------------------------------------
			try {
				SignHttpController signController = new SignHttpController();
				signController.logOut(SessionUserData.getInstance().getUserId());
			} catch (ErpBarcodeException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mUserLogoutTask = null;
			if (result) {
				mSharedSetting = new SettingPreferences(getApplicationContext());
				mSharedSetting.setLoginYn(false,"");
				
				Intent intent = new Intent();
		    	intent.setAction(Intent.ACTION_MAIN);
		    	intent.addCategory(Intent.CATEGORY_HOME);
		    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		    	startActivity(intent);
		    	
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, _errorMessage, BarcodeSoundPlay.SOUND_ERROR));
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			mUserLogoutTask = null;
			super.onCancelled();
		}
	}
	
	
}
