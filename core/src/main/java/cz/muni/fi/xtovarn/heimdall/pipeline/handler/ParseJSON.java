package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.json.JSONStringParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public class ParseJSON implements Handler {

	@Override
	public Object handle(Object o) {
		Event event = null;

		try {
			event = JSONStringParser.stringToEvent((String) o);

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
