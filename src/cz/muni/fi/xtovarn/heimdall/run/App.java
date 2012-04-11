package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.ParseJSON;
import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;
import cz.muni.fi.xtovarn.heimdall.pipeline.SetDetectionTime;
import cz.muni.fi.xtovarn.heimdall.pipeline.Store;
import cz.muni.fi.xtovarn.heimdall.stage.LineFileReader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {
		System.out.println("Heimdall is starting...");
		EventStore eventStore = EventStoreFactory.getInstance();

		ExecutorService es = Executors.newSingleThreadExecutor();
		Pipeline pipeline = new Pipeline(new ParseJSON(), new SetDetectionTime(), new Store(eventStore));

		LineFileReader lfr = new LineFileReader(pipeline);
		es.execute(lfr);

		es.shutdown();
		es.awaitTermination(5, TimeUnit.MINUTES);

		System.out.println("Shutting down...");

		eventStore.close();
	}
}
//Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
