package cz.muni.fi.xtovarn.heimdall.commons;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;


public class JSONMappingTest {

	private final String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"cron\",\"processId\":\"4219\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":\"http://www.linux.org/schema/monitoring/cron/3.1/events.xsd\",\"schemaVersion\":\"3.1\",\"value\":4648,\"value2\":\"Fax4x46aeEF%aax4x%46aeEF\"}}}";

	@Test
	public void testMapping() throws Exception {

		Event event = JSONStringParser.stringToEvent(json);


		Assert.assertEquals("Fax4x46aeEF%aax4x%46aeEF", event.getPayload().getValue("value2").toString());

	}
}
