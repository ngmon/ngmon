package cz.muni.fi.xtovarn.heimdall.client;

import org.codehaus.jackson.map.util.ISO8601Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

public class LocalClient {

	public static void main(String[] args) throws IOException, InterruptedException {

		Socket socket = new Socket("localhost", 5000);

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		int i = 5000;
		while (i > 0) {
			Thread.sleep(1000);
			i--;
			String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}";
			out.write(json);
			out.newLine();
			out.flush();
		}

		out.close();
		socket.close();
	}

}
