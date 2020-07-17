package com.bernard.murder.model;

public class Action implements Cloneable{
	
	String action;
	long basetime;
	long triggertime;
	
	public Action(String action, long basetime) {
		this.action = action;
		this.basetime = basetime;
		this.triggertime = 0;
	}
	
	public Action(String action, long basetime, long triggertime) {
		this.action = action;
		this.basetime = basetime;
		this.triggertime = triggertime;
	}



	@Override
	public Action clone() {
		Action actions = new Action(action, basetime);
		actions.triggertime = triggertime;
		return actions;
	}

	@Override
	public String toString() {
		return "Action [action=" + action + ", basetime=" + basetime + ", triggertime=" + triggertime + ", transient id=" + System.identityHashCode(this) + "]";
	}

	public String getName() {
		return action;
	}

	public boolean canBeLaunched() {
		return System.currentTimeMillis()-triggertime-basetime>0;
	}

	public void launch() {
		triggertime=System.currentTimeMillis();
	}
	
	public boolean hasFinished() {
		return triggertime + basetime - System.currentTimeMillis()<0;
	}
	
	public long timeToWaitLeft() {
		return basetime - System.currentTimeMillis() + triggertime;
	}
	
	public long dateReset() {
		return triggertime + basetime;
	}

	public long getBasetime() {
		return basetime;
	}

	public long getTriggertime() {
		return triggertime;
	}
	
	
	
}
