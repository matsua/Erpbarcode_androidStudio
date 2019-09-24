package com.ktds.erpbarcode.management;

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

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.R;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;

public class RepairBarcodeListAdapter extends BaseAdapter {

	private static final String TAG = "RepairBarcodeListAdapter";

	private int selected = -1;
    private List<BarcodeListInfo> mBarcodeListInfos;
    private LayoutInflater mInflater;
    
    private final OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            final int position = (Integer) buttonView.getTag();
            Log.d(TAG, "^^--^^ onCheckedChanged   position==>"+position);
            Log.d(TAG, "^^--^^ onCheckedChanged   isChecked==>"+isChecked);
            BarcodeListInfo barcodeInfo = getItem(position);
            if (barcodeInfo.isChecked() == isChecked) return;
            
            barcodeInfo.setChecked(isChecked);
        }
    };
    
    
    public RepairBarcodeListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBarcodeListInfos = new ArrayList<BarcodeListInfo>();
    }

    public void addItem(BarcodeListInfo item) {
    	mBarcodeListInfos.add(item);
    	notifyDataSetChanged();
    }
    
    public void addItemsNotClear(List<BarcodeListInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	mBarcodeListInfos.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void addItems(List<BarcodeListInfo> items) {
    	Log.d(TAG, "^^--^^ addItems==>"+items.size());
    	selected = -1;
    	mBarcodeListInfos.clear();
    	mBarcodeListInfos.addAll(items);
    	notifyDataSetChanged();
    }

    
    public int addItemNoDataChange(BarcodeListInfo item) {
    	mBarcodeListInfos.add(item);
    	return mBarcodeListInfos.size()-1;
    }
    
    public void itemClear() {
    	selected = -1;
    	mBarcodeListInfos.clear();
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return mBarcodeListInfos.size();
	}

	@Override
	public BarcodeListInfo getItem(int position) {
		return mBarcodeListInfos.get(position);
	}
	
	public List<BarcodeListInfo> getAllItems() {
		return mBarcodeListInfos;
	}
	
	public List<BarcodeListInfo> getCheckedItems() {
		List<BarcodeListInfo> tempItems = new ArrayList<BarcodeListInfo>();
		for (BarcodeListInfo barcodeInfo : mBarcodeListInfos) {
    		if (barcodeInfo.isChecked()) {
    			tempItems.add(barcodeInfo);
    		}
    	}
		return tempItems;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void removeItem(int position) {
		mBarcodeListInfos.remove(position);
	}
	
	public void removeItem(String barcode) {
		for (int i=mBarcodeListInfos.size()-1;i>=0; i--) {
    		BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
    		if (barcodeInfo.getBarcode().equals(barcode)) {
    			removeItem(i);
    		}	
		}
		notifyDataSetChanged();
	}
	
	public void removeCheckedItems() {
		for (int i=mBarcodeListInfos.size()-1;i>=0; i--) {
    		BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
    		if (barcodeInfo.isChecked()) {
    			removeItem(i);
    		}	
		}
    	notifyDataSetChanged();
	}

	public int getBarcodePosition(String barcode) {
		//Log.d(TAG, "getBarcodePosition   barcode==>"+barcode);
		for (int i=0; i<getCount(); i++) {
			BarcodeListInfo barcodeInfo = mBarcodeListInfos.get(i);
			if (barcodeInfo.getBarcode().equals(barcode))
				return i;
		}
		return -1;
	}
	
	public BarcodeListInfo getBarcodeListInfo(String barcode) {
		for (BarcodeListInfo barcodeInfo : mBarcodeListInfos) {
			if (barcodeInfo.getBarcode().equals(barcode))
					return barcodeInfo;
		}
		return null;
	}

	public int getSelectedPosition() {
		return this.selected;
	}
	
	public void changeSelectedPosition(int position) {
		this.selected = position;
		notifyDataSetChanged();
	}
	
	public void setItem(int position, BarcodeListInfo barcodeListInfo) {
		mBarcodeListInfos.set(position, barcodeListInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        BarcodeListInfo model = getItem(position);
        
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.management_repair_list_itemrow, null);
            holder.checked = (CheckBox) convertView.findViewById(R.id.repair_list_isChecked);
            holder.checked.setOnCheckedChangeListener(onCheckedChange);
        	holder.barcodeText = (TextView) convertView.findViewById(R.id.repair_list_facCd);
        	holder.sncodeText = (TextView) convertView.findViewById(R.id.repair_list_snCd);
        	holder.fsstText = (TextView) convertView.findViewById(R.id.repair_list_fsstCd);
        	holder.productCodeText = (TextView) convertView.findViewById(R.id.repair_list_productCode);
        	holder.productNameText = (TextView) convertView.findViewById(R.id.repair_list_productName);
        	holder.devTypeText = (TextView) convertView.findViewById(R.id.repair_list_devTypeName);
        	holder.partTypeText = (TextView) convertView.findViewById(R.id.repair_list_partType);
        	holder.fsbCodeText = (TextView) convertView.findViewById(R.id.repair_list_fsbarCd);
        	// 고장/수리의뢰
        	holder.facStatusText = (TextView) convertView.findViewById(R.id.repair_list_facStatus);
        	holder.failureCodeText = (TextView) convertView.findViewById(R.id.repair_list_failureCode);
        	holder.failureNameText = (TextView) convertView.findViewById(R.id.repair_list_failureName);
        	holder.failureRegNoText = (TextView) convertView.findViewById(R.id.repair_list_failureRegNo);
        	holder.orgInfoText = (TextView) convertView.findViewById(R.id.repair_list_orgInfo);

        	holder.partnerCodeText = (TextView) convertView.findViewById(R.id.repair_list_partnerCode);    // 협력사코드
        	holder.partnerNameText = (TextView) convertView.findViewById(R.id.repair_list_partnerName);    // 협력사명
        	holder.repairRequestNoText = (TextView) convertView.findViewById(R.id.repair_list_repairRequestNo);    // 수리의뢰번호
        	// -- 고장/수리의뢰
        	
        	// 수리완료
        	holder.locCdText = (TextView) convertView.findViewById(R.id.repair_list_locCd);    // 위치바코드
        	holder.locNameText = (TextView) convertView.findViewById(R.id.repair_list_locName);    // 위치명
        	holder.urcodnText = (TextView) convertView.findViewById(R.id.repair_list_URCODN);    // 원인유형
        	holder.mncodnText = (TextView) convertView.findViewById(R.id.repair_list_MNCODN);    // 수리유형
        	holder.failureCode_2Text = (TextView) convertView.findViewById(R.id.repair_list_failureCode_2);    // 고장코드
        	holder.repairRequestNo_2Text = (TextView) convertView.findViewById(R.id.repair_list_repairRequestNo_2);    // 수리의뢰번호
        	holder.injuryBarcodeText = (TextView) convertView.findViewById(R.id.repair_list_injurybarcode);    // 기존바코드
        	holder.rreasonText = (TextView) convertView.findViewById(R.id.repair_list_rreason);    // 대체발행사유
        	// -- 수리완료
        	// 장치바코드
        	holder.deviceIdText = (TextView) convertView.findViewById(R.id.repair_list_deviceid);
        	holder.upgdocText = (TextView) convertView.findViewById(R.id.repair_list_upgdoc);

        	if (!GlobalData.getInstance().getJobGubun().equals("수리완료")) {
        		// 수리완료
            	holder.locCdText.setVisibility(View.GONE);    // 위치바코드
            	holder.locNameText.setVisibility(View.GONE);    // 위치명
            	holder.urcodnText.setVisibility(View.GONE);    // 원인유형
            	holder.mncodnText.setVisibility(View.GONE);    // 수리유형
            	holder.failureCode_2Text.setVisibility(View.GONE);    // 고장코드
            	holder.repairRequestNo_2Text.setVisibility(View.GONE);    // 수리의뢰번호
            	holder.injuryBarcodeText.setVisibility(View.GONE);    // 기존바코드
            	holder.rreasonText.setVisibility(View.GONE);    // 대체발행사유
        	}
        	// 개조개량이면 고장 관련 정보 숨기기
        	if (GlobalData.getInstance().getJobGubun().equals("수리완료") || GlobalData.getInstance().getJobGubun().startsWith("개조개량") || GlobalData.getInstance().getJobGubun().equals("설비S/N변경")) {
            	holder.facStatusText.setVisibility(View.GONE);
            	holder.failureCodeText.setVisibility(View.GONE);
            	holder.failureNameText.setVisibility(View.GONE);
            	holder.failureRegNoText.setVisibility(View.GONE);

    			holder.partnerCodeText.setVisibility(View.GONE);
        		holder.partnerNameText.setVisibility(View.GONE);
        		holder.repairRequestNoText.setVisibility(View.GONE);
        	}
        	// 개조개량
        	if (!GlobalData.getInstance().getJobGubun().startsWith("개조개량")) {
        		holder.deviceIdText.setVisibility(View.GONE);
        	}
        	// 개조개량의뢰에만 지시서번호가 있음
        	if (!GlobalData.getInstance().getJobGubun().equals("개조개량의뢰")) {
        		holder.upgdocText.setVisibility(View.GONE);
        	}
        	// 수리완료는 운용조직 변경 안보이게.
        	if (GlobalData.getInstance().getJobGubun().equals("수리완료")) {
        		holder.orgInfoText.setVisibility(View.GONE);
        	}
        	
        	if(!GlobalData.getInstance().getJobGubun().equals("설비S/N변경")){
        		holder.fsbCodeText.setVisibility(View.GONE);
        		holder.sncodeText.setVisibility(View.GONE);
        		holder.fsstText.setVisibility(View.GONE);
        	}

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if(GlobalData.getInstance().getJobGubun().equals("설비S/N변경")){
        	if (selected == position) {
        		convertView.setBackgroundColor(Color.LTGRAY);
        	} else {
        		convertView.setBackgroundColor(Color.TRANSPARENT);
        	}
        }

        holder.checked.setTag(position);
        holder.checked.setChecked(model.isChecked());
        holder.barcodeText.setText(model.getBarcode());              // 설비바코드
        holder.sncodeText.setText(model.getFacSergeNum());			 // sncode
        holder.fsstText.setText(model.getFacStatusName());			 // 설비상태(S/N)
        holder.productCodeText.setText(model.getProductCode());      // 자재코드
        holder.productNameText.setText(model.getProductName());      // 자재명
        holder.devTypeText.setText(model.getDevTypeName());          // 품목구분
        holder.partTypeText.setText(model.getPartTypeName());        // 부품종류
        holder.fsbCodeText.setText(model.getDeviceId());			 // 장치바코드
        holder.facStatusText.setText(model.getFacStatusName());      // 설비상태
        holder.failureCodeText.setText(model.getFailureCode());      // 고장코드
        holder.failureNameText.setText(model.getFailureName());      // 고장명
        holder.failureRegNoText.setText(model.getFailureRegNo());    // 고장등록번호
        
        // 수리의뢰취소
    	holder.partnerCodeText.setText(model.getPartnerCode());
    	holder.partnerNameText.setText(model.getPartnerName());
    	holder.repairRequestNoText.setText(model.getRepairRequestNo());
    	
    	// 수리완료
    	holder.locCdText.setText(model.getLocCd());    // 위치바코드
    	holder.locNameText.setText(model.getLocName());    // 위치명
    	holder.urcodnText.setText(model.getUrcodn());    // 원인유형
    	holder.mncodnText.setText(model.getMncodn());    // 수리유형
    	holder.failureCode_2Text.setText(model.getFailureCode());    // 고장코드
    	holder.repairRequestNo_2Text.setText(model.getRepairRequestNo());    // 수리의뢰번호
    	holder.injuryBarcodeText.setText(model.getuFacCd());    // 기존바코드
    	holder.rreasonText.setText(model.getRreason());    // 대체발행사유
    	
    	
    	// 개조개량
    	holder.deviceIdText.setText(model.getDeviceId());	// 장치바코드
    	holder.upgdocText.setText(model.getUpgdoc());		// 지시서번호
    	
    	
        // 운영부서정보
    	String orgInfo = model.getCheckOrgYn() + "_" + model.getOrgId() + "_" +  model.getOrgName();
    	// 개조개량완료 운용조직 ""로
    	if (GlobalData.getInstance().getJobGubun().equals("개조개량완료")) {
    		orgInfo = "";
    	}
    	holder.orgInfoText.setText(orgInfo); 
        return convertView;
	}

	public class ViewHolder {
		public CheckBox checked;              // CheckBox
		public TextView barcodeText;          // 설비바코드
		public TextView sncodeText;           // 설비바S/N
		public TextView fsstText;			  // 설비상태(S/N)
		public TextView productCodeText;      // 자재코드
		public TextView productNameText;      // 자재명
		public TextView devTypeText;          // 품목구분
		public TextView partTypeText;         // 부품종류
		public TextView fsbCodeText;          // 설비S/N변경일때 장치바코드 
		public TextView facStatusText;        // 설비상태
		public TextView failureCodeText;      // 고장코드
		public TextView failureNameText;      // 고장명
		public TextView failureRegNoText;     // 고장등록번호
		public TextView orgInfoText;          // 운영부서정보
		
		// 수리의뢰취소
		public TextView partnerCodeText;      // 협력사코드
		public TextView partnerNameText;      // 협력사명
		public TextView repairRequestNoText;  // 수리의뢰번호
		
		// 수리완료
    	public TextView locCdText;    // 위치바코드
    	public TextView locNameText;    // 위치명
    	public TextView urcodnText;    // 원인유형
    	public TextView mncodnText;    // 수리유형
    	public TextView failureCode_2Text;    // 고장코드
    	public TextView repairRequestNo_2Text;    // 수리의뢰번호
    	public TextView injuryBarcodeText;    // 기존바코드
    	public TextView rreasonText;    // 대체발행사유
		
		
		// 개조개량
		public TextView deviceIdText;			// 장치바코드
		public TextView upgdocText;				// 지시서번호
    }
    
    /**************************************************************************
     * DB에서 불러온 R:rack, S:shelf, U:unit, E:Equipment 타입에 따라 건수를 산출한다.
     * @param partType
     * @return Count
     */
    public int getDBPartTypeCount(String partType) {
    	if (partType.equals("T")) {
    		return getCount();
    	}
    	
    	int count = 0;
    	for (BarcodeListInfo barcodeInfo : mBarcodeListInfos) {
			if (barcodeInfo.getPartType().equals(partType)) {
				count++;
			}
		}
    	return count;
    }

}
