package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQ;

public class ZMQContextFactory {

	private static final int THREAD_POOL_SIZE = 1;
	private static ZMQ.Context instance = null;

	public static ZMQ.Context getInstance() {
		if (instance == null) {
			instance = ZMQ.context(THREAD_POOL_SIZE);
		}

		return instance;
	}
}
