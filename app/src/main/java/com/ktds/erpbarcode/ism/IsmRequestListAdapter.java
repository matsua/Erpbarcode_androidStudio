package com.ktds.erpbarcode.ism;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;

public class IsmRequestListAdapter extends BaseAdapter {

	private static final String TAG = "IsmRequestListAdapter";

	private int selected = -1;
    private LayoutInflater mInflater;
    private List<IsmBarcodeInfo> mIsmBarcodeInfos = new ArrayList<IsmBarcodeInfo>();
    

    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            Log.d(TAG, "^^--^^ onCheckedChanged   position==>"+position);
            Log.d(TAG, "^^--^^ onCheckedChanged   isChecked==>"+isChecked);
            
            String jobGubun = GlobalData.getInstance().getJobGubun();
            IsmBarcodeInfo ismBarcodeInfo = getItem(position);
            Log.d(TAG, "^^--^^ onCheckedChanged   ismBarcodeInfo.isChecked()==>"+ismBarcodeInfo.isChecked());

            if (ismBarcodeInfo.isChecked() == isChecked) return;
            
            if (ismBarcodeInfo.isChecked()) {
            	ismBarcodeInfo.setChecked(false);
            	setItem(position, ismBarcodeInfo);
            } else {
            	if (jobGubun.equals("바코드대체요청")) {
                    if (ismBarcodeInfo.getExistingProductCode().equals("Y")) {
                        // 인스토어마킹요청 요청상태
                    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "이미 요청 중인 바코드입니다."));
                    	buttonView.setChecked(false);
                    	ismBarcodeInfo.setChecked(false);
                    	setItem(position, ismBarcodeInfo);
                        return;
                    }
                    
                    // 자재코드 사용중지여부 따지지 않게 변경 - request by 김희선 - 2014.01.08
                    /*
                    if (ismBarcodeInfo.getUseStopYn().equals("사용중지") 
                    		&& ismBarcodeInfo.getReplaceProductCode().isEmpty()) {
                    	GlobalData.getInstance().showMessageDialog(new ErpBarcodeException(-1, "사용중지된 자재은 처리할 수 없습니다."));
                    	buttonView.setChecked(false);
                    	ismBarcodeInfo.setChecked(false);
                    	setItem(position, ismBarcodeInfo);
                        return;
                    }
                    */

                    if (ismBarcodeInfo.getDevType().equals("40") && ismBarcodeInfo.getPartTypeCode().equals("40")) {
                    	GlobalData.getInstance().showMessageDialog(
                    			new ErpBarcodeException(-1, "부품종류가 존재하지 않습니다.\n\r기준정보 관리자(MDM)에게\n\r문의하세요."));
                    	ismBarcodeInfo.setChecked(false);
                    	setItem(position, ismBarcodeInfo);
                    	return;
                    }

                    if (ismBarcodeInfo.getFacStatusName().equals("납품취소") 
                    		|| ismBarcodeInfo.getFacStatusName().equals("사용중지")
                    		|| ismBarcodeInfo.getFacStatusName().equals("불용요청")  
                    		|| ismBarcodeInfo.getFacStatusName().equals("불용확정")
                    		|| ismBarcodeInfo.getFacStatusName().equals("인계완료") 
                    		|| ismBarcodeInfo.getFacStatusName().equals("인수예정")
                    		|| ismBarcodeInfo.getFacStatusName().equals("납품입고")
                    		|| ismBarcodeInfo.getFacStatusName().equals("이동중")
                    		|| ismBarcodeInfo.getFacStatusName().equals("시설등록완료")
                    )
                    {
                    	GlobalData.getInstance().showMessageDialog(
                    			new ErpBarcodeException(-1, "설비의 상태가 '" + ismBarcodeInfo.getFacStatusName() + "'인 설비는\n\r처리할 수 없습니다."));
                    	buttonView.setChecked(false);
                    	ismBarcodeInfo.setChecked(false);
                    	setItem(position, ismBarcodeInfo);
                    	return;
                    }
                }else if(jobGubun.equals("장치바코드")){
                	if (ismBarcodeInfo.getConditions().equals("20") || ismBarcodeInfo.getConditions().equals("30")) {
                		GlobalData.getInstance().showMessageDialog(
                    			new ErpBarcodeException(-1, "표준서비스코드상태가 \n\r종료진행,종료 일경우 \n\r출력할 수 없습니다."));
                    	ismBarcodeInfo.setChecked(false);
                    	setItem(position, ismBarcodeInfo);
                    	return;
                	}
                }
            	
            	buttonView.setChecked(true);
            	ismBarcodeInfo.setChecked(true);
            	setItem(position, ismBarcodeInfo);
            }
        }
    };
    
    public IsmRequestListAdapter(Activity activity) {
    	mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getSelectedPosition() {
		return this.selected;
	}
	
	public void changeSelectedPosition(int position) {
		this.selected = position;
		//notifyDataSetChanged();
	}

    public void addItem(IsmBarcodeInfo item) {
    	mIsmBarcodeInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<IsmBarcodeInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mIsmBarcodeInfos.clear();
    	mIsmBarcodeInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public void itemClear() {
    	mIsmBarcodeInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mIsmBarcodeInfos.size();
	}
 

	@Override
	public IsmBarcodeInfo getItem(int position) {
		return mIsmBarcodeInfos.get(position);
	}
	
	public List<IsmBarcodeInfo> getAllItems() {
		return mIsmBarcodeInfos;
	}
	
	public void removeItem(int position) {
		mIsmBarcodeInfos.remove(position);
	}
	
	public List<IsmBarcodeInfo> getCheckedItems() {
		List<IsmBarcodeInfo> tempItems = new ArrayList<IsmBarcodeInfo>();
		for (IsmBarcodeInfo ismBarcodeInfo : mIsmBarcodeInfos) {
    		if (ismBarcodeInfo.isChecked()) {
    			tempItems.add(ismBarcodeInfo);
    		}
    	}
		return tempItems;
	}
	
	public void removeCheckedItems() { 
		
		for (int i=mIsmBarcodeInfos.size()-1;i>=0; i--) {
    		IsmBarcodeInfo ismbarcodeInfo = mIsmBarcodeInfos.get(i);
			if (ismbarcodeInfo.isChecked()) {
    			removeItem(i);
    		}	
		}
    	notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setItem(int position, IsmBarcodeInfo ismBarcodeInfo) {
		mIsmBarcodeInfos.set(position, ismBarcodeInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String jobGubun = GlobalData.getInstance().getJobGubun();
		ViewHolder holder = null;
        if (convertView == null) {
        	holder = new ViewHolder();
        	if (jobGubun.equals("인스토어마킹관리")) {
        		convertView = mInflater.inflate(R.layout.ism_ismmanegement_list_itemrow, null);
        		holder.checked = (CheckBox) convertView.findViewById(R.id.ism_mnt_list_isChecked);
        		holder.checked.setOnCheckedChangeListener(onCheckedChange);
        		
        		holder.newBarcode = (TextView) convertView.findViewById(R.id.ism_mnt_newBarcode);
        		holder.injuryBarcode = (TextView) convertView.findViewById(R.id.ism_mnt_injuryBarcode);
        		holder.productCode = (TextView) convertView.findViewById(R.id.ism_mnt_itemCode);
        		holder.productName = (TextView) convertView.findViewById(R.id.ism_mnt_itemName);
        		holder.devType = (TextView) convertView.findViewById(R.id.ism_mnt_itemCategoryName);
        		holder.partType = (TextView) convertView.findViewById(R.id.ism_mnt_partKindName);
        		holder.transactionDate = (TextView) convertView.findViewById(R.id.ism_mnt_transactionDate);
        		holder.transactionTime = (TextView) convertView.findViewById(R.id.ism_mnt_transactionTime);
        		holder.transactionStatusName = (TextView) convertView.findViewById(R.id.ism_mnt_transactionStatusName);
        		holder.deviceId = (TextView) convertView.findViewById(R.id.ism_mnt_deviceId);
        		holder.locCd = (TextView) convertView.findViewById(R.id.ism_mnt_locationCode);
        		holder.locName = (TextView) convertView.findViewById(R.id.ism_mnt_locationName);
        		holder.itemClassificationName = (TextView) convertView.findViewById(R.id.ism_mnt_itemClassificationName);
        		holder.assetClassName = (TextView) convertView.findViewById(R.id.ism_mnt_assetClassificationName);
        		holder.manufacturerName = (TextView) convertView.findViewById(R.id.ism_mnt_makerName);
        		holder.manufacturerSN = (TextView) convertView.findViewById(R.id.ism_mnt_makerSerial);
        		holder.transactionUserName = (TextView) convertView.findViewById(R.id.ism_mnt_transactionUserName);
        		holder.publicationWhyName = (TextView) convertView.findViewById(R.id.ism_mnt_publicationWhyName);
        		holder.orgName = (TextView) convertView.findViewById(R.id.ism_mnt_orgName);
        	}else if(jobGubun.equals("장치바코드")){
        		convertView = mInflater.inflate(R.layout.barcode_management_device_item, null);
        		holder.checked = (CheckBox) convertView.findViewById(R.id.device_isChecked);
        		holder.checked.setOnCheckedChangeListener(onCheckedChange);
        		
        		holder.deviceId = (TextView) convertView.findViewById(R.id.device_item1);
        		holder.deviceName = (TextView) convertView.findViewById(R.id.device_item2);
        		holder.projectNo = (TextView) convertView.findViewById(R.id.device_item3);
        		holder.wbsCode = (TextView) convertView.findViewById(R.id.device_item4);
        		holder.operationSystemTokenName = (TextView) convertView.findViewById(R.id.device_item5);
        		holder.operationSystemCode = (TextView) convertView.findViewById(R.id.device_item6);
        		holder.operationSystemName = (TextView) convertView.findViewById(R.id.device_item7);
        		holder.locationIdTokenName = (TextView) convertView.findViewById(R.id.device_item8);
        		holder.deviceStatusName = (TextView) convertView.findViewById(R.id.device_item9);
        		holder.itemCode = (TextView) convertView.findViewById(R.id.device_item10);
        		holder.argumentDecisionName = (TextView) convertView.findViewById(R.id.device_item11);
        		holder.active = (TextView) convertView.findViewById(R.id.device_item12);
        		holder.registerDate = (TextView) convertView.findViewById(R.id.device_item13);
        		holder.comment = (TextView) convertView.findViewById(R.id.device_item14);
        		holder.conditions = (TextView) convertView.findViewById(R.id.device_item15);
        	}else if(jobGubun.equals("소스마킹")){
        		convertView = mInflater.inflate(R.layout.barcode_management_sm_item, null);
        		holder.checked = (CheckBox) convertView.findViewById(R.id.sm_isChecked);
        		holder.checked.setOnCheckedChangeListener(onCheckedChange);
        		
        		holder.barcode = (TextView) convertView.findViewById(R.id.sm_item1);
        		holder.itemCode = (TextView) convertView.findViewById(R.id.sm_item2);
        		holder.purchaseLineNumber = (TextView) convertView.findViewById(R.id.sm_item3);
        		holder.itemName = (TextView) convertView.findViewById(R.id.sm_item4);
        		holder.partKindName = (TextView) convertView.findViewById(R.id.sm_item5);
        		holder.productTypeName = (TextView) convertView.findViewById(R.id.sm_item6);
        		holder.comment = (TextView) convertView.findViewById(R.id.sm_item7);
        	}else if(jobGubun.equals("기지국/중계기 위치바코드")){
				convertView = mInflater.inflate(R.layout.barcode_management_nms_loc_item, null);
				holder.checked = (CheckBox) convertView.findViewById(R.id.device_isChecked);
				holder.checked.setOnCheckedChangeListener(onCheckedChange);

				holder.locCd = (TextView) convertView.findViewById(R.id.device_item1);
				holder.locName = (TextView) convertView.findViewById(R.id.device_item2);
				holder.repLocCd = (TextView) convertView.findViewById(R.id.device_item3);
				holder.repLocName = (TextView) convertView.findViewById(R.id.device_item4);
				holder.deviceId = (TextView) convertView.findViewById(R.id.device_item5);
				holder.itemCode = (TextView) convertView.findViewById(R.id.device_item6);
				holder.deviceName = (TextView) convertView.findViewById(R.id.device_item7);

			}else{
        		convertView = mInflater.inflate(R.layout.ism_ismrequest_list_itemrow, null);
        		holder.checked = (CheckBox) convertView.findViewById(R.id.ismrequest_list_isChecked);
        		holder.checked.setOnCheckedChangeListener(onCheckedChange);
        		
        		holder.facCdText = (TextView) convertView.findViewById(R.id.ismrequest_list_facCd);
        		holder.facStatusText = (TextView) convertView.findViewById(R.id.ismrequest_list_facStatus);
        		holder.productCodeText = (TextView) convertView.findViewById(R.id.ismrequest_list_productCode);
        		holder.productNameText = (TextView) convertView.findViewById(R.id.ismrequest_list_productName);
        		holder.devTypeNameText = (TextView) convertView.findViewById(R.id.ismrequest_list_devTypeName);
        		holder.partTypeText = (TextView) convertView.findViewById(R.id.ismrequest_list_partType);
        		holder.locCdText = (TextView) convertView.findViewById(R.id.ismrequest_list_locCd);
        		holder.locNameText = (TextView) convertView.findViewById(R.id.ismrequest_list_locName);
        		holder.assetClassText = (TextView) convertView.findViewById(R.id.ismrequest_list_assetClass);
        		holder.assetClassNameText = (TextView) convertView.findViewById(R.id.ismrequest_list_assetClassName);
        		holder.manufacturerNameText = (TextView) convertView.findViewById(R.id.ismrequest_list_manufacturerName);
        		holder.manufacturerSNText = (TextView) convertView.findViewById(R.id.ismrequest_list_manufacturerSN);
        		holder.deviceIdText = (TextView) convertView.findViewById(R.id.ismrequest_list_deviceId);
        		holder.useStopYnText = (TextView) convertView.findViewById(R.id.ismrequest_list_useStopYn);
        		holder.replaceProductCodeText = (TextView) convertView.findViewById(R.id.ismrequest_list_replaceProductCode);
        		holder.existingProductCodeText = (TextView) convertView.findViewById(R.id.ismrequest_list_existingProductCode);
        		holder.costCenterText = (TextView) convertView.findViewById(R.id.ismrequest_list_costCenter);
        	}
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (jobGubun.equals("인스토어마킹관리")) {
        	IsmBarcodeInfo model = getItem(position);
    		holder.checked.setTag(position);
    		holder.checked.setChecked(model.isChecked());
    		holder.newBarcode.setText(model.getNewBarcode());
    		holder.injuryBarcode.setText(model.getInjuryBarcode());
    		holder.productCode.setText(model.getProductCode());
    		holder.productName.setText(model.getProductName());
    		holder.devType.setText(model.getDevType());
    		holder.partType.setText(model.getPartType());
    		holder.transactionDate.setText(model.getTransactionDate());
    		holder.transactionTime.setText(model.getTransactionTime());
    		holder.transactionStatusName.setText(model.getTransactionStatusName());
    		holder.deviceId.setText(model.getDeviceId());
    		holder.locCd.setText(model.getLocCd());
    		holder.locName.setText(model.getLocName());
    		holder.itemClassificationName.setText(model.getItemClassificationName());
    		holder.assetClassName.setText(model.getAssetClassName());
    		holder.manufacturerName.setText(model.getManufacturerName());
    		holder.manufacturerSN.setText(model.getManufacturerSN());
    		holder.transactionUserName.setText(model.getTransactionUserName());
    		holder.publicationWhyName.setText(model.getPublicationWhyName());
    		holder.orgName.setText(model.getOrgName());
        }else if(jobGubun.equals("장치바코드")){
        	IsmBarcodeInfo model = getItem(position);
    		holder.checked.setTag(position);
    		holder.checked.setChecked(model.isChecked());
        	holder.deviceId.setText(model.getDeviceId());
    		holder.deviceName.setText(model.getItemName());
    		holder.projectNo.setText(model.getProductCode());
    		holder.wbsCode.setText(model.getWbsCode());
    		holder.operationSystemTokenName.setText(model.getOperationDeptCode());
    		holder.operationSystemCode.setText(model.getDevType());
    		holder.operationSystemName.setText(model.getDevTypeName());
    		holder.locationIdTokenName.setText(model.getAssetClassName());
    		holder.deviceStatusName.setText(model.getFacStatus());
    		holder.itemCode.setText(model.getItemCode());
    		holder.argumentDecisionName.setText(model.getAssetClass());
    		holder.active.setText(model.getItemDetailClassificationCode());
    		holder.registerDate.setText(model.getTransactionDate());
    		holder.comment.setText(model.getComment());
    		holder.conditions.setText("");
    		if(model.getConditions().equals("10")){
    			holder.conditions.setText("운용");
    		}else if(model.getConditions().equals("20")){
    			holder.conditions.setText("종료진행");
    		}else if(model.getConditions().equals("30")){
    			holder.conditions.setText("종료");
    		}
        }else if(jobGubun.equals("소스마킹")){
        	IsmBarcodeInfo model = getItem(position);
    		holder.checked.setTag(position);
    		holder.checked.setChecked(model.isChecked());
        	holder.barcode.setText(model.getFacCd());
    		holder.itemCode.setText(model.getItemCode());
    		holder.purchaseLineNumber.setText(model.getFacilityCategory());
    		holder.itemName.setText(model.getItemName());
    		holder.partKindName.setText(model.getPartKindName());
    		holder.productTypeName.setText(model.getProductCode());
    		holder.comment.setText(model.getComment());
    		if(model.getSilStatus().equals("BC_CPT")){
    			convertView.setBackgroundColor(0xFFFFC0CB);
    			holder.checked.setEnabled(false);
    			holder.comment.setText("바코드 발행 불가");
    		}else{
    			convertView.setBackgroundColor(0xFFFFFFFF);
    			holder.checked.setEnabled(true);
    		}
        }else if(jobGubun.equals("기지국/중계기 위치바코드")){
			IsmBarcodeInfo model = getItem(position);
			holder.checked.setTag(position);
			holder.checked.setChecked(model.isChecked());
			holder.locCd.setText(model.getLocCd());
			holder.locName.setText(model.getLocName());
			holder.repLocCd.setText(model.getRepLocCd());
			holder.repLocName.setText(model.getRepLocNm());
			holder.deviceId.setText(model.getDeviceId());
			holder.itemCode.setText(model.getNmsDeviceId());
			holder.deviceName.setText(model.getDeviceName());
		}else{
    		IsmBarcodeInfo model = getItem(position);
    		holder.checked.setTag(position);
    		holder.checked.setChecked(model.isChecked());
    		holder.facCdText.setText(model.getFacCd());
    		holder.facStatusText.setText(model.getFacStatusName());
    		holder.productCodeText.setText(model.getProductCode());
    		holder.productNameText.setText(model.getProductName());
    		holder.devTypeNameText.setText(model.getDevTypeName());
    		holder.partTypeText.setText(model.getPartTypeName());
    		holder.locCdText.setText(model.getLocCd());
    		holder.locNameText.setText(model.getLocName());
    		holder.assetClassText.setText(model.getAssetClass());
    		holder.assetClassNameText.setText(model.getAssetClassName());
    		holder.manufacturerNameText.setText(model.getManufacturerName());
    		holder.manufacturerSNText.setText(model.getManufacturerSN());
    		holder.deviceIdText.setText(model.getDeviceId());
    		holder.useStopYnText.setText(model.getUseStopYn());
    		holder.replaceProductCodeText.setText(model.getReplaceProductCode());
    		holder.existingProductCodeText.setText(model.getExistingProductCode());
    		holder.costCenterText.setText(model.getCostCenter());
    	}
        
        return convertView;
	}

	public class ViewHolder {
		public CheckBox checked;
		public TextView facCdText;
		public TextView facStatusText;
		public TextView productCodeText;
		public TextView productNameText;
		public TextView devTypeText;
		public TextView devTypeNameText;
		public TextView partTypeCodeText;
		public TextView partTypeText;
		public TextView locCdText;
		public TextView locNameText;
		public TextView assetClassText;
		public TextView assetClassNameText;
		public TextView manufacturerNameText;
		public TextView manufacturerSNText;
		public TextView deviceIdText;
		public TextView useStopYnText;
		public TextView replaceProductCodeText;
		public TextView existingProductCodeText;
		public TextView costCenterText;
		
		
		public TextView newBarcode;
		public TextView injuryBarcode;
		public TextView productCode;
		public TextView productName;
		public TextView devType;
		public TextView partType;
		public TextView transactionDate;
		public TextView transactionTime;
		public TextView transactionStatusName;
		public TextView deviceId;
		public TextView locCd;
		public TextView locName;
		public TextView itemClassificationName;
		public TextView assetClassName;
		public TextView manufacturerName;
		public TextView manufacturerSN;
		public TextView transactionUserName;
		public TextView publicationWhyName;
		public TextView orgName;
		
		public TextView deviceName;
		public TextView projectNo;
		public TextView wbsCode;
		public TextView operationSystemTokenName;
		public TextView operationSystemCode;
		public TextView operationSystemName;
		public TextView locationIdTokenName;
		public TextView deviceStatusName;
		public TextView itemCode;
		public TextView argumentDecisionName;
		public TextView active;
		public TextView registerDate;
		public TextView comment;
		public TextView conditions;
		
		public TextView barcode;
		public TextView purchaseLineNumber;
		public TextView itemName;
		public TextView partKindName;
		public TextView productTypeName;

		public TextView repLocCd;
		public TextView repLocName;
		
    }
}
