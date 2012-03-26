package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.processor.Enrich;
import cz.muni.fi.xtovarn.heimdall.processor.Print;
import cz.muni.fi.xtovarn.heimdall.processor.Store;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQEventProcessor;
import cz.muni.fi.xtovarn.heimdall.zeromq.deprecated.JSONMessageProcessor;
import cz.muni.fi.xtovarn.heimdall.zeromq.deprecated.ZMQProcessorDevice;
import org.zeromq.ZMQ;

import java.io.IOException;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException {
		EventStore eventStore = new EventStore();

		ZMQ.Context context = ZMQ.context(1);

		// Socket facing clients
		ZMQ.Socket reciver = context.socket(ZMQ.PULL);
		reciver.bind("tcp://*:359");
		ZMQ.Socket outer = context.socket(ZMQ.PUSH);

		Enrich action = new Enrich(new Store(new Print(), eventStore));

		ZMQEventProcessor parser = new ZMQEventProcessor(context, reciver, outer, action);

		parser.run();

		reciver.close();
		context.term();

		eventStore.close();
	}
}
