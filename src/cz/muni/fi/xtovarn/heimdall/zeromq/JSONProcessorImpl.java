package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;

public class JSONProcessorImpl implements MessageProcessor {
	@Override
	public byte[][] process(byte[][] message) {
		byte outputMessage[][] = null;

		if (message.length > 1) {
			System.err.println("Wrong format!!");
		}

		String json = (new String(message[0])).trim();
		Event event = null;
		try {
			event = JSONStringParser.stringToEvent(json);
			outputMessage[0] = JSONEventMapper.eventToEntry(event).getData();
		} catch (JsonParseException e) {
			System.err.println(e.getMessage());
		} catch (JsonMappingException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return outputMessage;
	}
}
