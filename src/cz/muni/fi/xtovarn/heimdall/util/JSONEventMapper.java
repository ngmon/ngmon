package cz.muni.fi.xtovarn.heimdall.util;

import com.sleepycat.db.DatabaseEntry;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.smile.SmileFactory;

import java.io.IOException;

public class JSONEventMapper {

	private static final ObjectMapper mapper = new ObjectMapper(new SmileFactory());

	static {
		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
	}

	public static byte[] eventAsBytes(Event event) throws IOException {

		return mapper.writeValueAsBytes(event);
	}

	public static Event bytesToEvent(byte[] data) throws IOException {

		return mapper.readValue(data, Event.class);
	}
}