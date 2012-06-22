package cz.muni.fi.xtovarn.heimdall.benchmarking;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.json.JSONStringParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bench {

	private static List<String> list = new ArrayList<String>(10000);

	public static void main(String[] args) throws IOException {
		int i = 80000;
		while (i > 0) {
			i--;
			String json = "{\"Event\":{\"id\":" + i + ",\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"detectionTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis() + 1), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"cron\",\"processId\":\"4219\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":\"http://www.linux.org/schema/monitoring/cron/3.1/events.xsd\",\"schemaVersion\":\"3.1\",\"value\":4648,\"value2\":\"Fax4x46aeEF%aax4x%46aeEF\"}}}";
			list.add(json);
		}

		double start = System.nanoTime();

		for (String s : list) {
			Event e = JSONStringParser.stringToEvent(s);
			String str = (String) e.getPayload().getValue("value2");
		}

		double stop = System.nanoTime();

		System.out.println((stop - start) / 1000000000);

	}
}

