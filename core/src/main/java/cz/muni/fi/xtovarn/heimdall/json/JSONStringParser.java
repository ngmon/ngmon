package cz.muni.fi.xtovarn.heimdall.json;

import com.fasterxml.jackson.databind.*;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;

import java.io.IOException;

// TODO add to pico
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
