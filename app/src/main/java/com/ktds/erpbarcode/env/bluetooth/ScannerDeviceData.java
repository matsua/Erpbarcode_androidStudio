package com.ktds.erpbarcode.env.bluetooth;

public class ScannerDeviceData {
	private static ScannerDeviceData instance;
	
	private boolean connected;
	private String deviceName;
	private String deviceAddress;
	
	public static synchronized ScannerDeviceData getInstance() {
		if (instance==null) {
			instance=new ScannerDeviceData();
			instance.setConnected(false);
			instance.setDeviceName("");
			instance.setDeviceAddress("");
		}
		return instance;
	}
	
	public void clear() {
		instance.setConnected(false);
		instance.setDeviceName("");
		instance.setDeviceAddress("");
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
}
