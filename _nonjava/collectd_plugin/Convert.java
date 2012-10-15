package cz.muni.fi.xtovarn.heimdall.commons;

import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;

import java.io.*;
import java.util.Date;

public class Convert {

	public static void main(String[] args) throws IOException {

		// Open the file that is the first
		// command line parameter
		FileInputStream fstream = new FileInputStream("collectd_python.txt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		int counter = 0;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			// Print the content on the console
			counter++;

			String[] attrs = strLine.split(";");

			String tmstp = attrs[0];

			Long timestamp = new Long(tmstp.split(".0")[0]) + 1350000000000L;

			String type = attrs[1].split(":")[1];

			String subtype = null;

			String[] temparr = attrs[2].split(":");
			if (temparr.length > 1) {
				subtype = temparr[1];
			}

			String valstr = attrs[3].split(":")[1];
			String[] valuesarr = valstr.substring(1,valstr.length()-1).split(",");

			String metastr = attrs[4].split(":")[1];
			String[] metaarr = metastr.substring(1,metastr.length()-1).split(",");

			Payload payload = new Payload();

			for (int i = 0; i < metaarr.length; i++) {

				payload.add(metaarr[i].trim().replace("'", ""), (new Double(valuesarr[i].trim())).doubleValue());
			}

			Event event = new Event();
			event.setId(counter);
			event.setApplication("collectd");
			event.setOccurrenceTime(new Date(timestamp));
			event.setDetectionTime(new Date(timestamp + counter % 6));
			event.setHostname("lykomedes.fi.muni.cz");
			event.setPriority(counter % 3);
			event.setSeverity(counter % 6);
			event.setProcess("collectd");
			event.setProcessId("6589");

			String temptype = type;

			if (subtype != null) {
				temptype = type + "[" + subtype + "]";
			}

			event.setType(temptype);
			event.setPayload(payload);

			System.out.println(JSONStringParser.eventToString(event));

		}
		//Close the input stream
		in.close();

	}
}
