package sweb.common.base;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class RequestBase {
	
	@JsonProperty(value="authentication")
	private String authToken;

	public RequestBase() {}

	/*public RequestBase( String authToken)
	{
		this.setAuthToken(authToken);
	}
*/
	public RequestBase(String json) throws Exception
	{
		initialize(json, RequestBase.class);
	}

	public <T extends RequestBase> void initialize(String json, Class<T> obj) throws Exception
	{
		T temp = JacksonObjectMapper.INSTANCE.mapper.readValue(json, obj);
		this.setAuthToken(temp.getAuthToken());
	}
	
	public final String toJsonString() throws Exception
	{
		return JacksonObjectMapper.INSTANCE.mapper.writeValueAsString(this);
	}


	@JsonIgnore
	public String getAuthToken() {
		return authToken;
	}

	public final void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
}
