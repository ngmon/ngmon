package cz.muni.fi.xtovarn.heimdall.commons.json;

import com.fasterxml.jackson.databind.*;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import  com.fasterxml.jackson.dataformat.smile.SmileFactory;

import java.io.IOException;

// TODO add to pico
public class JSONEventMapper {

	private static final ObjectMapper mapper = new ObjectMapper(new SmileFactory());

	static {
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	}

	public static byte[] eventAsBytes(Event event) throws IOException {

		return mapper.writeValueAsBytes(event);
	}

	public static Event bytesToEvent(byte[] data) throws IOException {

		return mapper.readValue(data, Event.class);
	}
}
