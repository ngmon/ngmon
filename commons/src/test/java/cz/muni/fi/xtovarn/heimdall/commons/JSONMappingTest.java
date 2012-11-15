package cz.muni.fi.xtovarn.heimdall.commons;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;


public class JSONMappingTest {

	long id = 0;
	Date occurrenceTime = ISO8601Utils.parse("2012-10-12T03:44:52.713+00:00"); // String.replace("+0000", "+00:00") -- bug in ISOUtils, cannot parse +0000
	Date detectionTime = ISO8601Utils.parse("2012-10-12T03:44:52.791+00:00");
	String hostname = "domain.localhost.cz";
	String type = "org.linux.cron.Started";
	String application = "Cron";
	String process = "cron";
	String processId = "4219";
	int level = 5;
	int priority = 4;
	String schema = "http://cron.org/1.0/events.jsch";
	String schemaVersion = "1.0";
	int value1 = 4648;
	String value2 = "Fax4x46aeEF%aax4x%46aeEF";

	private Event event;

	private final String expectedJson = "{\"Event\":{" +
			"\"id\":" + id + "," +
			"\"occurrenceTime\":" + "\"" + ISO8601Utils.format(occurrenceTime, true, TimeZone.getTimeZone("Z")).replace("Z", "+0000") + "\"," +
			"\"detectionTime\":" + "\"" + ISO8601Utils.format(detectionTime, true,TimeZone.getTimeZone("Z")).replace("Z", "+0000") + "\"," +
			"\"hostname\":" + "\"" + hostname + "\"," +
			"\"type\":" + "\"" + type + "\"," +
			"\"application\":" + "\"" + application + "\"," +
			"\"process\":" + "\"" + process + "\"," +
			"\"processId\":" + "\"" + processId + "\"," +
			"\"level\":" + level + "," +
			"\"priority\":" + priority + "," +
			"\"Payload\":{" +
//			"\"schema\":" + "\"" + schema + "\"," +
//			"\"schemaVersion\":" + "\"" + schemaVersion + "\"," +
			"\"value1\":" + value1 +	"," +
			"\"value2\":"  + "\"" + value2 + "\"" + "}}}";


	@Before
	public void setUp() throws Exception {
		event = new Event();
		event.setOccurrenceTime(occurrenceTime);
		event.setDetectionTime(detectionTime);
		event.setHostname(hostname);
		event.setType(type);
		event.setApplication(application);
		event.setProcess(process);
		event.setProcessId(processId);
		event.setLevel(level);
		event.setPriority(priority);

		Payload payload = new Payload();

		payload.setSchema(schema);
//		payload.setSchemaVersion(schemaVersion);
		payload.add("value1", value1);
		payload.add("value2", value2);

		event.setPayload(payload);
	}

	@Test
	public void testMappingEventToJsonString() throws Exception {

		String actualJson = JSONStringParser.eventToString(event);
		Assert.assertEquals(this.expectedJson, actualJson);
	}

	@Test
	public void testMappingJsonToEvent() throws Exception {

		Event actualEvent = JSONStringParser.stringToEvent(expectedJson);

		Assert.assertEquals(this.event, actualEvent);

	}

	@Test
	public void testMappingEventToJson() throws Exception {

	}
}
