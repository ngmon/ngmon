package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UserQueues {
	private static Map<String, BlockingQueue<Event>> queues = new HashMap<String, BlockingQueue<Event>>(3);

	public static BlockingQueue<Event> queue(String id) {
		BlockingQueue<Event> queue = queues.get(id);

		if (queue == null) {
			queue = new ArrayBlockingQueue<Event>(10);
			queues.put(id, queue);
		}

		return queue;
	}

	protected void release(String id) {
		queues.remove(id);
	}
}
