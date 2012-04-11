package cz.muni.fi.xtovarn.heimdall.util;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;

public class JSONStringParser {

	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
	}
	
	public static Event stringToEvent(String jsonString) throws IOException {

		return mapper.readValue(jsonString, Event.class);
	}
}
