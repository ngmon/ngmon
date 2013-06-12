package cz.muni.fi.xtovarn.heimdall.pipeline.handler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;

/**
 * Converts the event string to the Event object
 */
public class ParseJSON implements Handler {
	
	private static Logger logger = LogManager.getLogger(ParseJSON.class);

	@Override
	public Object handle(Object o) {
		Event event = null;

		try {
			event = JSONStringParser.stringToEvent((String) o);

		} catch (JsonParseException e) {

			logger.error(e.getMessage());

		} catch (JsonMappingException e) {

			logger.error(e.getMessage());

		} catch (IOException e) {

			logger.error(e.getMessage());
		}

		return event;
	}
}