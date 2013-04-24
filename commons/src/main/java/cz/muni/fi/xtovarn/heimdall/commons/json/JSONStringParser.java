package cz.muni.fi.xtovarn.heimdall.commons.json;

import com.fasterxml.jackson.databind.*;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;

import java.io.IOException;

/**
 * Converts Event to String and vice versa
 */
public class JSONStringParser {

	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	}
	
	public static Event stringToEvent(String jsonString) throws IOException {

		return mapper.readValue(jsonString, Event.class);
	}

	public static String eventToString(Event event) throws IOException {

		return mapper.writeValueAsString(event);
	}
}
