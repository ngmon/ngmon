package cz.muni.fi.xtovarn.heimdall.stage;


import java.util.concurrent.BlockingQueue;

public abstract class AbstractStage<T_in, T_out> implements Runnable {
	private final BlockingQueue<T_in> inWorkQueue;
	private final BlockingQueue<T_out> outWorkQueue;

	protected AbstractStage(BlockingQueue<T_in> inWorkQueue, BlockingQueue<T_out> outWorkQueue) {
		this.inWorkQueue = inWorkQueue;
		this.outWorkQueue = outWorkQueue;
	}

	@Override
	public void run() {
		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STARTED");

		T_in incomingWork;
		T_out outcomingWork;

		while (!Thread.currentThread().isInterrupted()) {
			try {
				incomingWork = inWorkQueue.take();
				outcomingWork = work(incomingWork);

				if (outcomingWork != null) {
					outWorkQueue.put(outcomingWork);
				}

			} catch (InterruptedException e) {
				System.err.println(this.getClass().getSimpleName() + ": InterruptedException logged");
				break;
			}
		}

		System.out.println(String.format("%-78s", this.getClass().getSimpleName()).replace(" ", ".") + "STOPPED");
	}

	public abstract T_out work(T_in workItem);
}
