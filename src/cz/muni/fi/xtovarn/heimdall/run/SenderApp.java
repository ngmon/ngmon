package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Date;

public class SenderApp {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		ZMQ.Context context = ZMQ.context(1);

		//  Socket facing clients
		ZMQ.Socket sender = context.socket(ZMQ.PUSH);
		sender.connect("tcp://localhost:359");
		
		int n = 10000;
		
		while (!Thread.currentThread().isInterrupted()) {

			String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}";
			
/*			sender.send(json.getBytes(), ZMQ.SNDMORE);
			sender.send(json.getBytes(), ZMQ.SNDMORE);
			sender.send(json.getBytes(), ZMQ.SNDMORE);*/
			sender.send(json.getBytes(), 0);

//			System.out.println("Sending >>" + json);
//
			Thread.sleep(1);
			n--;
			if (n < 1) Thread.currentThread().interrupt();
		}

		//  We never get here but clean up anyhow
		sender.close();
		context.term();
		

		
	}
}
