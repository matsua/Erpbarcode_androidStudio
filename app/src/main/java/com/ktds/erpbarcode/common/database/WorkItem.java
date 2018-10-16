package com.ktds.erpbarcode.common.database;

public class WorkItem {
	private int id;
	private int workId;
	private String jobType;
	private String jobData;
	private String inputTime;
	private int stepStatus;  // 작업 단계별 상태는 DB에서는 사용하지 않는다.

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getWorkId() {
		return workId;
	}
	public void setWorkId(int workId) {
		this.workId = workId;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getJobData() {
		return jobData;
	}
	public void setJobData(String jobData) {
		this.jobData = jobData;
	}
	public String getInputTime() {
		return inputTime;
	}
	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}
	public int getStepStatus() {
		return stepStatus;
	}
	public void setStepStatus(int stepStatus) {
		this.stepStatus = stepStatus;
	}
}
