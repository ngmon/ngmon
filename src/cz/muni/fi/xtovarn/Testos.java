package cz.muni.fi.xtovarn;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;

import com.sleepycat.db.*;
import cz.muni.fi.xtovarn.heimdall.binding.BasicEventTupleBinding;
import cz.muni.fi.xtovarn.heimdall.store.EventStoreBDB;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class Testos {

	public static void main(String[] args) throws UnsupportedEncodingException, DatabaseException, FileNotFoundException {
		EventStoreBDB store = new EventStoreBDB();
		store.setup();
		Database db = store.getDatabase();
		SecondaryDatabase sdb = store.getSecondaryDatabase();

		DatabaseEntry theKey = new DatabaseEntry();
		DatabaseEntry theData = new DatabaseEntry();
		EntryBinding<Event> entryBinding = new BasicEventTupleBinding();

		int a = 10;
		while(a > 1) {
			Long id = store.getSequence().get(null, 1);

			LongBinding.longToEntry(id, theKey);


			Event evt1 = new Event(id, new Date(System.currentTimeMillis()), "com.microsoft.wi.ef", "xtovarn logged in");
			entryBinding.objectToEntry(evt1, theData);

			OperationStatus ops = db.put(null, theKey, theData);
			System.out.println(ops.toString());

			a--;

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}



		SecondaryCursor cursor = sdb.openSecondaryCursor(null, null);
		StringBinding.stringToEntry("com.microsoft.wi", theKey);

		OperationStatus retVal = cursor.getSearchKeyRange(theKey, theData, LockMode.DEFAULT);

		while (retVal == OperationStatus.SUCCESS) {
				System.out.println(StringBinding.entryToString(theKey) + ":/:" + entryBinding.entryToObject(theData).toString());

				retVal = cursor.getNextNoDup(theKey, theData, LockMode.DEFAULT);
			}

		cursor.close();
		store.close();
	}
}
