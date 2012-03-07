package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.binding.BasicEventTupleBinding;
import cz.muni.fi.xtovarn.heimdall.entity.Event;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreBDB;

import java.io.FileNotFoundException;
import java.util.Date;


public class MultiThreaded {
	static EventStoreBDB store;


	public static void main(String[] args) throws FileNotFoundException, DatabaseException, InterruptedException {
		store = new EventStoreBDB();
		store.setup();

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
		Database db = store.getDatabase();
		SecondaryDatabase sdb = store.getSecondaryDatabase();
		DatabaseEntry theKey = new DatabaseEntry();
		DatabaseEntry theData = new DatabaseEntry();
		EntryBinding<Event> entryBinding = new BasicEventTupleBinding();


		@Override
		public void run() {
			int a = 5;
			while (a > 1) {

				Long id = null;
				try {
					id = store.getSequence().get(null, 1);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}

				LongBinding.longToEntry(id, theKey);


				Event evt1 = new Event(id, new Date(System.currentTimeMillis()), "com.microsoft.wi.ef", "xtovarn logged in");
				entryBinding.objectToEntry(evt1, theData);

				OperationStatus ops = null;
				try {
					ops = db.put(null, theKey, theData);
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				System.out.println("WRITER" + Thread.currentThread().getId() + "::>" + evt1.toString());

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
		Database db = store.getDatabase();
		SecondaryDatabase sdb = store.getSecondaryDatabase();
		DatabaseEntry theKey = new DatabaseEntry();
		DatabaseEntry theData = new DatabaseEntry();
		EntryBinding<Event> entryBinding = new BasicEventTupleBinding();


		@Override
		public void run() {
			Cursor cursor = null;

			try {
				cursor = db.openCursor(null, null);
			} catch (DatabaseException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			assert cursor != null;
			try {
				while (cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					System.out.println("READER" + Thread.currentThread().getId() + "::>" + entryBinding.entryToObject(theData).toString());
					Thread.sleep(300);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
}
