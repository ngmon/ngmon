package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONEventMapper;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONProcessorImpl implements MessageProcessor {
	@Override
	public List<byte[]> process(List<byte[]> message) {
		List<byte[]> outputMessage = new ArrayList<byte[]>();

		if (message.size() > 1) {
			System.err.println("Wrong format!!");
		}

		String json = (new String(message.get(0))).trim();
		Event event = null;
		try {
			event = JSONStringParser.stringToEvent(json);
			outputMessage.add(JSONEventMapper.eventToEntry(event).getData());
		} catch (JsonParseException e) {
			System.err.println(e.getMessage());
		} catch (JsonMappingException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		System.out.println(event.toString());

		return outputMessage;
	}
}
