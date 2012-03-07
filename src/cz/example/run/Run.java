package cz.example.run;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.smile.SmileFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Run {
	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new SmileFactory());

		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);

		Event event = new Event();

		event.setApplication("Cron");
		event.setHostname("domain.localhost.cz");
		event.setId(123564);
		event.setPriority(5);
		event.setProcess("proc_cron NAme");
		event.setProcessId("id005");
		event.setPriority(4);
		event.setSeverity(5);
		event.setTime(new Date(System.currentTimeMillis()));
		event.setType("org.linux.cron.Started");
		Payload payload =	new Payload();
		payload.put("value", 4648);
		payload.put("value2", "aax4x46aeEF");
		event.setPayload(payload);

		mapper.writeValue(new File("sth.json"), mapper.readValue(mapper.writeValueAsBytes(event), Event.class));
	}
}
