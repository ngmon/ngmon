package cz.muni.fi.xtovarn.heimdall.zeromq;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.zeromq.deprecated.ZMQMessageProcessor;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONProcessor {

	public Event process(List<byte[]> message) throws ZMQException{
		if (message.size() > 1) {
			throw new ZMQException("The message has more parts than expected", (int) ZMQ.Error.ENOTSUP.getCode()); // TODO choose correct error CODE
		}

		String json = (new String(message.get(0))).trim();
		Event event = null;
		try {
			event = JSONStringParser.stringToEvent(json);
		} catch (JsonParseException e) {
			System.err.println(e.getMessage());
		} catch (JsonMappingException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		return event;
	}

}
