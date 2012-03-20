package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQSimpleJSONParser;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException {
		ZMQ.Context context = ZMQ.context(1);

		//  Socket facing clients
		ZMQ.Socket reciver = context.socket(ZMQ.PULL);
		reciver.bind("tcp://*:359");

		ZMQ.Socket outer = context.socket(ZMQ.PUSH);

		ZMQSimpleJSONParser parser = new ZMQSimpleJSONParser(context, reciver, outer);

		parser.run();

		reciver.close();
		context.term();
	}
}
