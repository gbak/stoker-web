package com.gbak.sweb.common.base;

public class NameValueResult<T> {
	private String name;
	private T value;

	public NameValueResult()
	{
		
	}
	
	public NameValueResult(String name, T value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
}
