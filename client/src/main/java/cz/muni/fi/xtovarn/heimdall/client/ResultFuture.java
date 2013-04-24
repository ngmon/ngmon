package cz.muni.fi.xtovarn.heimdall.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of the Future interface
 * 
 * @author Klaus Brunner (https://gist.github.com/KlausBrunner/4110226),
 *         modifications by Svata Novak
 * 
 */

public final class ResultFuture<T> implements Future<T> {
	private final CountDownLatch latch = new CountDownLatch(1);
	private T value;

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}

	@Override
	public T get() throws InterruptedException {
		latch.await();
		return value;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		if (latch.await(timeout, unit)) {
			return value;
		} else {
			throw new TimeoutException();
		}
	}

	// calling this more than once doesn't make sense, and won't work properly
	// in this implementation. so: don't.

	// this method is public, so it's a good idea to "hide" it from the client
	// by returning Future instead of ResultFuture
	public void put(T result) {
		value = result;
		latch.countDown();
	}
}