package sweb.common.base;

import org.codehaus.jackson.map.ObjectMapper;

public enum JacksonObjectMapper {
	INSTANCE;
	
	public ObjectMapper mapper = new ObjectMapper();
	
	JacksonObjectMapper() {

	}
}
