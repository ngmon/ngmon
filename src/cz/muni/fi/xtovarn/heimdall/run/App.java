package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.runnables.NettyServer;
import cz.muni.fi.xtovarn.heimdall.runnables.SocketServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
		System.out.println("Heimdall is starting...");
		EventStore eventStore = EventStoreFactory.getSingleInstance();

		ExecutorService es = Executors.newSingleThreadExecutor();
		ExecutorService es2 = Executors.newSingleThreadExecutor();

		SocketServer lfr = new SocketServer(es2);
		es.execute(lfr);

		es.shutdown();

/*		Executors.newCachedThreadPool().execute(new NettyServer());
		*/
		es.awaitTermination(5, TimeUnit.MINUTES);

		System.out.println("Shutting down...");

	}
}
//Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
