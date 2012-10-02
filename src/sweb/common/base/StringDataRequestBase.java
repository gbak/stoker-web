package sweb.common.base;

import org.codehaus.jackson.annotate.JsonProperty;


public class StringDataRequestBase extends RequestBase {
	@JsonProperty(value="data")
	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public StringDataRequestBase(String json) throws Exception
	{
		StringDataRequestBase temp = JacksonObjectMapper.INSTANCE.mapper.readValue(json, StringDataRequestBase.class);
		this.setAuthToken(temp.getAuthToken());
		this.setData(temp.getData());
	}
	
	public StringDataRequestBase()
	{
		super();
	}
	
	public StringDataRequestBase(String authToken, String mapUid, String data)
	{
		setAuthToken(authToken);
		setData(data);
	}
}
