package cz.muni.fi.xtovarn.heimdalld.pipeline.handler;

import cz.muni.fi.xtovarn.heimdalld.db.entity.Event;
import cz.muni.fi.xtovarn.heimdalld.json.JSONStringParser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

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
