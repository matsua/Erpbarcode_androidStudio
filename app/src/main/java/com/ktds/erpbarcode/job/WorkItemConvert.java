package com.ktds.erpbarcode.job;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ktds.erpbarcode.common.database.WorkItem;

public class WorkItemConvert {
	//private static final String TAG = "LocBarcodeConvert";
	
	public static WorkItem jsonStringToWorkItem(String jsonString) {
		
		Gson gson = new Gson();
		Type workitemType = new TypeToken<WorkItem>(){}.getType();
		WorkItem workItem = gson.fromJson(jsonString, workitemType);

		return workItem;
	}
	
	public static String WorkItemToJsonString(WorkItem workItem) {
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(workItem);
		
		return jsonString;
	}
}
