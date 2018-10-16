package com.ktds.erpbarcode.survey.model;

public class CheckTerminalInfo {
	private boolean isChecked;       // checkBox
	private int number;              // 번호
	private String terminalCode;     // 단말바코드
	private String sendYn;           // 전송여부

	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getTerminalCode() {
		return terminalCode;
	}
	public void setTerminalCode(String terminalCode) {
		this.terminalCode = terminalCode;
	}
	public String getSendYn() {
		return sendYn;
	}
	public void setSendYn(String sendYn) {
		this.sendYn = sendYn;
	}
}
