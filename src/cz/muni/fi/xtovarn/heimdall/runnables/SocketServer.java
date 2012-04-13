package cz.muni.fi.xtovarn.heimdall.runnables;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreFactory;
import cz.muni.fi.xtovarn.heimdall.pipeline.HandlerSequence;
import cz.muni.fi.xtovarn.heimdall.pipeline.Pipeline;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.DetermineRecipient;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.ParseJSON;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.SetDetectionTime;
import cz.muni.fi.xtovarn.heimdall.pipeline.handlers.Store;

import java.io.*;
import java.util.concurrent.ExecutorService;

public class SocketServer implements Runnable {

	private final ExecutorService executor;
	private final EventStore eventStore;

	public SocketServer(ExecutorService executor) throws FileNotFoundException, DatabaseException {
		this.executor = executor;
		eventStore = EventStoreFactory.getSingleInstance();
	}

	@Override
	public void run() {
		try {
			read("events.json");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			eventStore.close(); // ALAWAYS close EventStore from Thread which created it!
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	private void read(String filename) throws IOException, InterruptedException {
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String s;

		while ((s = in.readLine()) != null) {
			processWithPipeline(s);
		}

		in.close();
		executor.shutdown();
	}

	private void processWithPipeline(String json) {
		HandlerSequence sequence = new HandlerSequence(new ParseJSON(), new SetDetectionTime(), new Store(eventStore)/*, new DetermineRecipient()*/);
		executor.submit(new Pipeline(json, sequence));
	}
}
