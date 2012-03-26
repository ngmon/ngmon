package cz.muni.fi.xtovarn.heimdall.stage;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ParseJSONStage extends AbstractStage<List<byte[]>, Event> implements Stage<List<byte[]>, Event> {

	public ParseJSONStage(BlockingQueue<List<byte[]>> inWorkQueue, BlockingQueue<Event> outWorkQueue) {
		super(inWorkQueue, outWorkQueue);
	}

	@Override
	public Event work(List<byte[]> workItem) throws IOException {

		System.out.println(this.toString() + " bytes recieved");

		if (workItem.size() > 1) {
			throw new IOException("The message has more parts than expected");
		}

		String json = (new String(workItem.get(0))).trim();
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
