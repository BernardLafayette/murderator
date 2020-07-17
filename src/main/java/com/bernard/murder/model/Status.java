package com.bernard.murder.model;

public class Status {
	String name;

	public Status(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Status [name=" + name + "]";
	}
	
	
	
}
