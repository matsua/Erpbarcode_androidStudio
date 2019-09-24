package com.ktds.erpbarcode.infosearch;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.DeviceBarcodeInfo;

public class SelectDeviceSubFragment extends Fragment {

	private static final String TAG = "SelectDeviceSubFragment";

	
	private Button mTabGeneralButton;
	private Button mTabLocButton;
	private Button mTabOrgButton;
	private Button mTabNetworkButton;
	
	private ScrollView mTab1;
	private ScrollView mTab2;
	private ScrollView mTab3;
	private ScrollView mTab4;
	
	
	public static SelectDeviceSubFragment newInstance() {
		SelectDeviceSubFragment fragment = new SelectDeviceSubFragment();
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.infosearch_selectdevice_device_fragment, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//mScreenTools = new ScreenTools(getActivity());
		setLayout();
		initScreen();
		
		Log.d(TAG, "onActivityCreated   general...");
	}
	
	private void setLayout() {
		
		//-----------------------------------------------------------
		// 상위 Tab관련
		//-----------------------------------------------------------
		mTabGeneralButton = (Button) getActivity().findViewById(R.id.selectdevice_device_tab_general_button);
		mTabGeneralButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("general");
					}
				});
		mTabLocButton = (Button) getActivity().findViewById(R.id.selectdevice_device_tab_loc_button);
		mTabLocButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("loc");
					}
				});
		mTabOrgButton = (Button) getActivity().findViewById(R.id.selectdevice_device_tab_org_button);
		mTabOrgButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("org");
					}
				});
		mTabNetworkButton = (Button) getActivity().findViewById(R.id.selectdevice_device_tab_network_button);
		mTabNetworkButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						switchTab("network");
					}
				});
		
		mTab1 = (ScrollView) getActivity().findViewById(R.id.selectdevice_device_tab1);
		mTab2 = (ScrollView) getActivity().findViewById(R.id.selectdevice_device_tab2);
		mTab3 = (ScrollView) getActivity().findViewById(R.id.selectdevice_device_tab3);
		mTab4 = (ScrollView) getActivity().findViewById(R.id.selectdevice_device_tab4);
	}
	
	private void initScreen() {
		switchTab("general");
	}
	
	private void switchTab(String tag) {
		if (tag.equals("general")) {
			mTabGeneralButton.setSelected(true);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			
			mTab1.setVisibility(View.VISIBLE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
		} else if (tag.equals("loc")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(true);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.VISIBLE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.GONE);
		} else if (tag.equals("org")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(true);
			mTabNetworkButton.setSelected(false);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.VISIBLE);
			mTab4.setVisibility(View.GONE);
		} else if (tag.equals("network")) {
			mTabGeneralButton.setSelected(false);
			mTabLocButton.setSelected(false);
			mTabOrgButton.setSelected(false);
			mTabNetworkButton.setSelected(true);
			
			mTab1.setVisibility(View.GONE);
			mTab2.setVisibility(View.GONE);
			mTab3.setVisibility(View.GONE);
			mTab4.setVisibility(View.VISIBLE);
		}
	}
	
	public void showDeviceInfoDisplay(DeviceBarcodeInfo deviceInfo) {
		Log.d(TAG, "장치바코드정보 showDeviceInfoDisplay  Start...");

		// 일반 TAB1
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_deviceId)).setText(deviceInfo.getDeviceId());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_deviceName)).setText(deviceInfo.getDeviceName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_projectNo)).setText(deviceInfo.getProjectNo());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_wbsNo)).setText(deviceInfo.getProjectCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_operationSystemToken)).setText(deviceInfo.getOperationSystemTokenName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_operationSystemCode)).setText(deviceInfo.getOperationSystemCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_locationIdToken)).setText(deviceInfo.getLocationIdToken());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_deviceStatus)).setText(deviceInfo.getDeviceStatusCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_deviceStatusName)).setText(deviceInfo.getDeviceStatusName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_itemCode)).setText(deviceInfo.getItemCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_itemName)).setText(deviceInfo.getItemName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_assetClassificationCode)).setText(deviceInfo.getAssetClassificationCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_assetClassificationName)).setText(deviceInfo.getAssetClassificationName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_migrationTypeCode)).setText(deviceInfo.getMigrationTypeCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_migrationTypeName)).setText(deviceInfo.getMigrationTypeName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_argumentDecisionName)).setText(deviceInfo.getArgumentDecisionName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_standardServiceCode)).setText(deviceInfo.getStandardServiceCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_standardServiceName)).setText(deviceInfo.getStandardServiceName());
		// 위치 TAB2
		// sesang 20190910 대표주소정보 추가
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_repLocationCode)).setText(deviceInfo.getRepLocationCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_repLocationName)).setText(deviceInfo.getRepLocationName());
		// end sesang
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_locationCode)).setText(deviceInfo.getLocationCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_locationName)).setText(deviceInfo.getLocationName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_detailAddress)).setText(deviceInfo.getDetailAddress());

		// 조직 TAB3
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_operationOrgCode)).setText(deviceInfo.getOperationOrgCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_operationOrgName)).setText(deviceInfo.getOperatioinOrgName());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_ownOrgCode)).setText(deviceInfo.getOwnOrgCode());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_ownOrgName)).setText(deviceInfo.getOwnOrgName());

		// 네트워크장비 속성 TAB4
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level1Code)).setText(deviceInfo.getLevel1Code());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level1Name)).setText(deviceInfo.getLevel1Name());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level2Code)).setText(deviceInfo.getLevel2Code());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level2Name)).setText(deviceInfo.getLevel2Name());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level3Code)).setText(deviceInfo.getLevel3Code());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level3Name)).setText(deviceInfo.getLevel3Name());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level4Code)).setText(deviceInfo.getLevel4Code());
		((EditText) getActivity().findViewById(R.id.selectdevice_device_info_level4Name)).setText(deviceInfo.getLevel4Name());

	}

}
