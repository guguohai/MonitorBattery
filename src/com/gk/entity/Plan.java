package com.gk.entity;

/**
 * Plan实体类
 * @author guohai@Live.com
 *
 */

public class Plan {
	public int id;
	private String planName;
	private int runState;
	private String createTime;
	private String updateTime;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getPlanName() {
		return planName;
	}
	
	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public int getRunState() {
		return runState;
	}

	public void setRunState(int runState) {
		this.runState = runState;
	}
	
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

}