package cz.muni.fi.xtovarn.heimdall.run;


import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import cz.muni.fi.xtovarn.heimdall.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MultiThreaded {
	static EventStore store;


	public static void main(String[] args) throws InterruptedException, FileNotFoundException, DatabaseException {
		store = EventStoreFactory.getInstance();

		Thread ww1 = new Thread(new WorkerWriter());
		Thread ww2 = new Thread(new WorkerWriter());
		Thread ww3 = new Thread(new WorkerWriter());
		Thread ww4 = new Thread(new WorkerWriter());
		Thread wr1 = new Thread(new WorkerReader());

		ww1.start();
		ww2.start();
		ww3.start();
		ww4.start();

		Thread.sleep(200);
		wr1.start();

		ww1.join();
		wr1.join();

		store.close();
	}

	static class WorkerWriter implements Runnable {

		@Override
		public void run() {
			int a = 5;
			while (a > 1) {
				Event event = new Event();

				event.setApplication("Cron");
				event.setHostname("domain.localhost.cz");
				event.setPriority(5);
				event.setProcess("proc_cron NAme");
				event.setProcessId("id005");
				event.setPriority(4);
				event.setSeverity(5);
				event.setOccurrenceTime(new Date(System.currentTimeMillis()));
				event.setType("org.linux.cron.Started");
				Payload payload = new Payload();
				payload.add("value", 4648);
				payload.add("value2", "aax4x46aeEF");
				event.setPayload(payload);

				OperationStatus ops = null;
				try {
					ops = store.put(event);
				} catch (DatabaseException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
				System.out.println(ops.toString());
				System.out.println("WRITER" + Thread.currentThread().getId() + "::>" + event.toString());

				a--;

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static class WorkerReader implements Runnable {

		@Override
		public void run() {
			List<Event> list = null;
			try {
				list = store.getAllRecords();
			} catch (DatabaseException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			for (Event entry : list) {
				System.out.println("READER" + Thread.currentThread().getId() + "::>" + entry.toString());
			}


		}
	}
}
