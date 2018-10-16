package com.ktds.erpbarcode.print.zebra;

import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.barcodeManagement.BarcodeManagementLocActivity;
import com.ktds.erpbarcode.common.encryption.Caesar;
import com.ktds.erpbarcode.env.bluetooth.DeviceInfo;
import com.ktds.erpbarcode.ism.IsmManagementActivity;
import com.ktds.erpbarcode.ism.model.IsmBarcodeInfo;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;


public class BarcodePrintController {
	private static final String TAG = "BarcodePrintController";
	
	private Connection connection;
    private ZebraPrinter printer;
    private BluetoothAdapter mBluetoothAdapter;
    
//    private SettingPreferences mSharedSetting;
//    private int x_coordinate;
//    private int y_coordinate;
//    private int darkness;
    
    private String getBluetoothAdress(){
    	String address = "";
    	
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	if(device.getName().contains("ZD500") || device.getName().contains("40J")){
            		DeviceInfo deviceinfo = new DeviceInfo();
            		deviceinfo.setDeviceName(device.getName());
            		deviceinfo.setDeviceAddress(device.getAddress());
            		address = device.getAddress();
            	}
            }
        }
    	return address;
    }
    
    public int printOpen(String address){
//    	connection = new BluetoothConnection("AC:3F:A4:0E:45:7F");
//    	connection = new BluetoothConnection("AC:3F:A4:4A:65:B9");
    	
    	if(address.equals("")){
    		address = getBluetoothAdress();
    	}
    	
    	if(address.equals("")){
    		System.out.println("BRPE :: 프린트 연결 상태 확인");
    		return -1;
    	}
    	
    	int result = -1;
    	connection = null;
    	connection = new BluetoothConnection(address);
    	try {
			connection.open();
			result = 1;
			try {
				printer = ZebraPrinterFactory.getInstance(connection);
				result = 1;
			} catch (ZebraPrinterLanguageUnknownException e) {
				printClose();
				System.out.println("BRPE :: 프린트 연결 실패");
				result = -2;
			}
		} catch (ConnectionException e) {
			printClose();
			System.out.println("BRPE :: 블루투스 연결 실패");
			result = -1;
		}
    	return result;
    }
    
    public int printClose(){
    	int result = -1;
    	if (connection != null) {
    		try {
    			connection.close();
    			result = 1;
			} catch (ConnectionException e) {
				System.out.println("BRPE :: 블루투스 종료 실패");
				result = -1;
			}
        }
    	return result;
    }
    
    public int printBarcodeDirectCommandTest(final int mTouchLbtCode, final int x_coordinate, final int y_coordinate, final int darkness,final int count){
    	int result = -1;
    	String testBarcode = "TEST123412341234";
		String testPartType = "test barcode";
		String testPartType_sub = "";
		String testProductName = "test barcode";
		String barcode = "TEST123412341234";
    	
    	if(mTouchLbtCode == 7){
    		testBarcode = "TEST123412341234";
    		testPartType = "OO특별시 OO구";
    		testPartType_sub = "OO동 OO건물";
    		barcode = "TEST123412341234";
    	}
    	
		for(int i = 0; i < count; i++){
			String ZPLcommand = "";
			
//			ZPLcommand+="~JC";	//sc 
			ZPLcommand+="~SD" + darkness; //max 30 darkness
			ZPLcommand+="^XA" + "^PW1280" + "^LH0,0" + "^XZ";
			
			ZPLcommand+="^XA";
			ZPLcommand+= "^SEE:UHANGUL.DAT^FS"; 	//엔코더파일 지정 
			ZPLcommand+= "^CWQ,E:KFONT3.TTF^FS"; 	//폰트명 지정 
			ZPLcommand+= "^CI28"; 					//한글사용 명령어 utf-8
			
//			type -> 0:6x20 pdf417, 1:6x35 pdf417, 2:20x45 qrcode, 3:30x80 barcode, 5:6x58 pdf417, 6:7x50 pdf417, 7:30x80 barcode
			switch (mTouchLbtCode) {
			case 0:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^B7N,6,1,2,,N^FD"+testBarcode+"^FS";
				ZPLcommand+="^FO" + x_coordinate + "," + (y_coordinate + 45) + "^AQN,20,20^FD"+barcode+"^FS";
				break;
			case 1:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^B7N,14,1,5,,N^FD"+testBarcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcode+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 20) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+testPartType.substring(0,1) + "/" + "^FS";
					ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcode+"^FS";
				}
				break;
			case 2:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^BQN,2,5^FDQA,"+testBarcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 15) + "^AQN,30,30^FD"+testProductName+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 55) + "^AQN,30,30^FD"+testProductName+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 95) + "^AQN,25,25^FD"+barcode+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 10) + "^AQN,25,25^FD"+barcode+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 90) + "^AQN,25,25^FD"+testPartType+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 170) + "," + (y_coordinate + 90) + "^AQN,25,25^FD"+testProductName+"^FS";
				}
				break;
			case 3:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^BQN,2,7^FDQA,"+testBarcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 10) + "^AQN,50,50^FD"+testProductName+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 65) + "^AQN,50,50^FD"+testProductName+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 117) + "^AQN,50,50^FD"+barcode+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 6) + "^AQN,50,50^FD"+barcode+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 61) + "^AQN,50,50^FD"+testPartType+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 107) + "^AQN,50,50^FD"+testProductName+"^FS";
				}
				break;
			case 5:
				ZPLcommand+="^FO" + (x_coordinate + 20) + "," + y_coordinate + "^BY2,3^B7N,13,1,6,,N^FD"+testBarcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 40) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+testPartType.substring(0,1) + "/" +"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 70) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 450) + "," + (y_coordinate + 23) + "^AQN,25,25^FD"+testProductName+"^FS";
				break;
			case 6:
				ZPLcommand+="^FO" + (x_coordinate + 5) + "," + y_coordinate + "^BY2,3^B7N,15,1,5,,N^FD"+testBarcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 20) + "," + (y_coordinate + 50) + "^AQN,25,25^FD"+testPartType.substring(0,1) + "/" + "^FS";
				ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 50) + "^AQN,25,25^FD"+barcode+"^FS";
				break;
				
			case 7:
				ZPLcommand+="^FO" + (x_coordinate - 50) + "," + (y_coordinate - 15) + "^BCN,100,N,N,N^FD"+testBarcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 40) + "," + (y_coordinate + 85) + "^AQN,30,30^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate - 70) + "," + (y_coordinate - 150) + "^AQN,30,30^FD"+testPartType+"^FS";
				ZPLcommand+="^FO" + (x_coordinate - 70) + "," + (y_coordinate - 120) + "^AQN,30,30^FD"+testPartType_sub+"^FS";
				break;

			default:
				break;
			}
			
			ZPLcommand+="^XZ";
			
			System.out.println("ZPLcommand :: " + ZPLcommand);
			
			try {
				printer.sendCommand(ZPLcommand);
				result = 1;
			} catch (ConnectionException e) {
				System.out.println("BRPE :: 프린트 도중에 오류 발생");
				result = -1;
			}
		}
		printClose();
		return result;
    }
    
	public int printBarcodeDirectCommand(final List<IsmBarcodeInfo> sendBarcodeInfos,final int mTouchLbtCode, final int x_coordinate, final int y_coordinate, final int darkness) {
//		barcodeType :: 0:설비바코드 1:위치바코드 2:장치바코드 
		int rsCode = -1;
		String ZPLcommand = "";
		
		for (IsmBarcodeInfo sendIsmBarcodeInfo : sendBarcodeInfos) {
			String barcode= "";
			
//			String geoName = "";
//			String locationName = "";
			String barcodeLabel = "";
			String barcodeSubTitle1 = "";
			String barcodeSubTitle2 = ""; 
			
			if(mTouchLbtCode == 7){
				barcode = sendIsmBarcodeInfo.getLocCd();
				barcodeLabel = sendIsmBarcodeInfo.getLocCd();
				barcodeSubTitle1 = sendIsmBarcodeInfo.getSilAddress();
				barcodeSubTitle2 = sendIsmBarcodeInfo.getLocName();
			}else{
				barcode = sendIsmBarcodeInfo.getNewBarcode();
				barcodeLabel = sendIsmBarcodeInfo.getNewBarcode();
				barcodeSubTitle1 = sendIsmBarcodeInfo.getPartType();
				barcodeSubTitle2 = sendIsmBarcodeInfo.getProductName();
			}
			
			if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
				barcode = sendIsmBarcodeInfo.getDeviceId();
                barcodeLabel = sendIsmBarcodeInfo.getDeviceId();
                barcodeSubTitle1 = sendIsmBarcodeInfo.getItemName();
                barcodeSubTitle2 = sendIsmBarcodeInfo.getDeviceId();
			}else if(GlobalData.getInstance().getJobGubun().equals("소스마킹")){
				barcode = sendIsmBarcodeInfo.getFacCd();
                barcodeLabel = sendIsmBarcodeInfo.getFacCd();
                barcodeSubTitle1 = sendIsmBarcodeInfo.getPartKindName();
                if(!(barcodeSubTitle1.equals("Unit")) && !(barcodeSubTitle1.equals("Shelf")) && !(barcodeSubTitle1.equals("Rack")))
                {
                         barcodeSubTitle1 = "Equip";
                }
                barcodeSubTitle2 = sendIsmBarcodeInfo.getItemName();
			}
			
			if(barcode.subSequence(0, 2).equals("K9")){
				barcode = Caesar.encrypt(barcode);
				barcode = "+" + barcode;
			}
//			type -> 0:6x20 pdf417, 1:6x35 pdf417, 2:20x45 qrcode, 3:30x80 barcode, 5:6x58 pdf417, 6:7x50 pdf417
			
//			ZPLcommand+="~JC";	//sc 
			ZPLcommand+="~SD" + darkness; //max 30 darkness
			
			ZPLcommand+="^XA" + "^PW1280" + "^LH0,0" + "^XZ";
			ZPLcommand+="^XA";
			ZPLcommand+= "^SEE:UHANGUL.DAT^FS"; 	//엔코더파일 지정 
			ZPLcommand+= "^CWQ,E:KFONT3.TTF^FS"; 	//폰트명 지정 
			ZPLcommand+= "^CI28"; 					//한글사용 명령어 utf-8
			
			switch (mTouchLbtCode) {
			case 0:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^B7N,6,1,2,,N^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + x_coordinate + "," + (y_coordinate + 45) + "^AQN,20,20^FD"+barcodeLabel+"^FS";
				break;
			case 1:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^B7N,14,1,5,,N^FD"+barcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 20) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcodeSubTitle1.substring(0,1) + "/" + "^FS";
					ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
				}
				break;
			case 2:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^BQN,2,5^FDQA,"+barcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 15) + "^AQN,30,30^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 55) + "^AQN,30,30^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 95) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 10) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 110) + "," + (y_coordinate + 90) + "^AQN,25,25^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 170) + "," + (y_coordinate + 90) + "^AQN,25,25^FD"+barcodeSubTitle2+"^FS";
				}
				break;
				
			case 3:
				ZPLcommand+="^FO" + x_coordinate + "," + y_coordinate + "^BY2,3^BQN,2,7^FDQA,"+barcode+"^FS";
				if(GlobalData.getInstance().getJobGubun().equals("장치바코드")){
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 10) + "^AQN,50,50^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 65) + "^AQN,50,50^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 117) + "^AQN,50,50^FD"+barcode+"^FS";
				}else{
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 6) + "^AQN,50,50^FD"+barcodeLabel+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 61) + "^AQN,50,50^FD"+barcodeSubTitle1+"^FS";
					ZPLcommand+="^FO" + (x_coordinate + 160) + "," + (y_coordinate + 107) + "^AQN,50,50^FD"+barcodeSubTitle2+"^FS";
				}
				break;
			case 5:
				ZPLcommand+="^FO" + (x_coordinate + 20) + "," + y_coordinate + "^BY2,3^B7N,13,1,6,,N^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 40) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcodeSubTitle1.substring(0,1) + "/" +"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 70) + "," + (y_coordinate + 40) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 450) + "," + (y_coordinate + 23) + "^AQN,25,25^FD"+barcodeSubTitle2+"^FS";
				break;
			case 6:
				ZPLcommand+="^FO" + (x_coordinate + 5) + "," + y_coordinate + "^BY2,3^B7N,15,1,5,,N^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 20) + "," + (y_coordinate + 50) + "^AQN,25,25^FD"+barcodeSubTitle1.substring(0,1) + "/" + "^FS";
				ZPLcommand+="^FO" + (x_coordinate + 50) + "," + (y_coordinate + 50) + "^AQN,25,25^FD"+barcodeLabel+"^FS";
				break;
				
			case 7:
				ZPLcommand+="^FO" + (x_coordinate - 50) + "," + (y_coordinate - 15) + "^BCN,100,N,N,N^FD"+barcode+"^FS";
				ZPLcommand+="^FO" + (x_coordinate + 40) + "," + (y_coordinate + 85) + "^AQN,30,30^FD"+barcodeLabel+"^FS";
				ZPLcommand+="^FO" + (x_coordinate - 70) + "," + (y_coordinate - 150) + "^AQN,30,30^FD"+barcodeSubTitle1+"^FS";
				ZPLcommand+="^FO" + (x_coordinate - 70) + "," + (y_coordinate - 120) + "^AQN,30,30^FD"+barcodeSubTitle2+"^FS";
				break;
			default:
				break;
			}
			
			ZPLcommand+="^XZ";
		}
		
		System.out.println("ZPLcommand :: " + ZPLcommand);
		
		try {
			printer.sendCommand(ZPLcommand);
			rsCode = 1;
			if(GlobalData.getInstance().getJobGubun().equals("인스토어마킹관리")){
				IsmManagementActivity ismMa = new IsmManagementActivity();
				ismMa.statusChange(sendBarcodeInfos);
			}else if(GlobalData.getInstance().getJobGubun().equals("소스마킹")){
				BarcodeManagementLocActivity bmMa = new BarcodeManagementLocActivity();
				bmMa.statusChange(sendBarcodeInfos);
			}
		} catch (ConnectionException e) {
			System.out.println("BRPE :: 프린트 도중에 오류 발생");
			rsCode = -1;
		}
		printClose();
		return rsCode;
	}
	
	
	
	
	public int printSensor() {
		int result = -1;
		try {
			printer.sendCommand("~JC^XA^MF^XZ");
			result = 1;
		} catch (ConnectionException e) {
			System.out.println("BRPE :: 프린트 센싱 오류");
			result = -1;
		}
		printClose();
		return result;
	}
	
}