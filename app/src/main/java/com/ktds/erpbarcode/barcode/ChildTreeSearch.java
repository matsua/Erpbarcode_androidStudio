package com.ktds.erpbarcode.barcode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;

public class ChildTreeSearch {

	final List<BarcodeListInfo> mBarcodeInfos;
	//Set<Integer> tempChildSets = new HashSet<Integer>();
	Set<String> tempChildSets = new HashSet<String>();
	
	public ChildTreeSearch(List<BarcodeListInfo> barcodeInfos) {
		mBarcodeInfos = barcodeInfos;
	}
	
	public Set<String> searchChildNode(String barcode) {
		// 이미 스캔한 설비가 리스트에 있으면 삭제 request by 박장수 2013.08.27
		for (int i=mBarcodeInfos.size()-1;i>=0;i--) {
			if (mBarcodeInfos.get(i).getBarcode().equals(barcode)) {
				putChildBarcode(i);
			}
        }
		
		return tempChildSets;
	}
	
	private void putChildBarcode(final Integer position) {
		BarcodeListInfo barcodeInfo = mBarcodeInfos.get(position);
		if (barcodeInfo == null) return;
		
		//tempChildSets.add(position);
		tempChildSets.add(barcodeInfo.getBarcode());
		
    	// 자식Node가 있으면 자식에 자식이 있는지 추적한다.
		for (int i=0; i<mBarcodeInfos.size(); i++) {
			BarcodeListInfo childBarcodeInfo = mBarcodeInfos.get(i);
			if (barcodeInfo.getBarcode().equals(childBarcodeInfo.getuFacCd())) {
        		putChildBarcode(i);
        	}
		}
    }
}
