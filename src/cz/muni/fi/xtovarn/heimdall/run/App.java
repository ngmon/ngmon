package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.processor.Enrich;
import cz.muni.fi.xtovarn.heimdall.processor.Print;
import cz.muni.fi.xtovarn.heimdall.processor.Store;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQContextFactory;
import cz.muni.fi.xtovarn.heimdall.zeromq.ZMQEventProcessor;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException {
		EventStore eventStore = EventStoreFactory.getInstance();
		ZMQ.Context context = ZMQContextFactory.getInstance();

		// Socket facing clients
		ZMQ.Socket reciver = context.socket(ZMQ.PULL);
		reciver.bind("tcp://*:359");
		ZMQ.Socket outer = context.socket(ZMQ.PUSH);

		BlockingQueue<Event> queue1 = new ArrayBlockingQueue<Event>(10);

		ZMQEventProcessor parser = new ZMQEventProcessor(context, reciver, queue1);

		parser.run();

		reciver.close();
		context.term();

		eventStore.close();
	}
}
