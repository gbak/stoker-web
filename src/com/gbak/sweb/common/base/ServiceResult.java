package com.gbak.sweb.common.base;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.type.TypeReference;


import java.util.Collection;

public class ServiceResult<T> {

	// ::TODO:: determine JSON that we want to return
	public static final String ExceptionResponse = "{\"feedback\":\"Service received unhandled exception.\",\"data\":null,\"success\":false}";

	@JsonProperty(value="success")
	public boolean success;

	@JsonProperty(value="feedback")
	public String feedback;

	@JsonProperty(value="data")
	//public ArrayList<T> data;
	public T data;

	public ServiceResult(){}

	public ServiceResult(String json, TypeReference<?> typeRef) throws Exception
	{
		//TypeReference<ServiceResult<Account>> typeRef = new TypeReference<ServiceResult<Account>>() {};
		ServiceResult<T> temp = JacksonObjectMapper.INSTANCE.mapper.readValue(json, typeRef);

		this.success = temp.success;
		this.feedback = temp.feedback;
		this.data = temp.data;
	}
	
	/*@JsonIgnore
	public T get1stDataEntry() {
		if (data == null || data.size() <= 0) {
			return null;
		}
		
		return data.get(0);
	}*/
	
	public String toJsonString() throws Exception
	{
		return JacksonObjectMapper.INSTANCE.mapper.writeValueAsString(this);
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	/*public Collection<T> getData() {
		return data;
	}*/
	public T getData() {
        return data;
    }
	
	/*public void setData(ArrayList<T> data) {
		this.data = data;
	}*/
	public void setData(T data) {
        this.data = data;
    }
	
	public String getFeedback() {
		return feedback;
	}

}
