package cz.muni.fi.xtovarn.heimdall.stage;

import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import static cz.muni.fi.xtovarn.heimdall.stage.PerClientSendStage.StageStatus.*;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PerClientSendStage implements Runnable {
	static enum StageStatus {STARTED,STOPPED,SUSPENDED}

	private StageStatus status = STOPPED;
	private final BlockingQueue<Event> inWorkQueue;
	private final Channel outputChannel;
	private final static ExecutorService executorService = Executors.newSingleThreadExecutor();

	public PerClientSendStage(BlockingQueue<Event> inWorkQueue, Channel outputChannel) {
		this.inWorkQueue = inWorkQueue;
		this.outputChannel = outputChannel;
	}

	public void start() {
		executorService.execute(this);
		Future fu = executorService.submit(this);
		status = STARTED;
	}

	@Override
	public void run() {
		while (status.equals(STARTED)) {
			try {
				Event event = inWorkQueue.take(); // BLOCKING!

				if (event.getId() == -1) { // POISON PILL SHUTDOWN
					break;
				}

				ChannelFuture future = outputChannel.write(event.toString());
				future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

			} catch (InterruptedException e) {
				System.err.println(this.getClass().getSimpleName() + ": InterruptedException logged");
				break;
			}
		}

		status = STOPPED;
	}

	public void stop() {
		/* Insert Poison Pill */
		Event e = new Event();
		e.setId(-1);
		inWorkQueue.add(e);
	}
}
