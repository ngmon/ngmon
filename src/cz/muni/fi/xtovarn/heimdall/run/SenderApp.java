package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SenderApp {

	public static void main(String[] args) throws IOException, DatabaseException {

		ZMQ.Context context = ZMQ.context(1);

		//  Socket facing clients
		ZMQ.Socket sender = context.socket(ZMQ.PUSH);
		sender.connect("tcp://localhost:359");

		System.out.println("Press Enter when the workers are ready: ");
//		System.in.read();

		int task_nbr;

		for (task_nbr = 0; task_nbr < 4; task_nbr++) {
			
			String json = "{\"Event\":{\"id\":123564,\"time\":\"2012-03-20T13:54:22.039+0000\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}";
			
/*			sender.send(json.getBytes(), ZMQ.SNDMORE);
			sender.send(json.getBytes(), ZMQ.SNDMORE);
			sender.send(json.getBytes(), ZMQ.SNDMORE);*/
			sender.send(json.getBytes(), 0);


			System.out.println("Sending >>" + json);
		}

		//  We never get here but clean up anyhow
		sender.close();
		context.term();
		

		
	}
}
