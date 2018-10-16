package com.ktds.erpbarcode.common;

public interface ErpBarEventListener {
	void onYesEvent(ErpBarEventMessage erpBarEventMessage);
	void onNoEvent(ErpBarEventMessage erpBarEventMessage);
}
