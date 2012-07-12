package cz.muni.fi.xtovarn.heimdall.dispatcher;

import com.google.inject.Inject;
import cz.muni.fi.xtovarn.heimdall.db.entity.Event;
import cz.muni.fi.xtovarn.heimdall.guice.Startable;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dispatcher implements Startable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final SecureChannelGroup secureChannelGroup;

	@Inject
	public Dispatcher(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
	}

	@Override
	public void start() {

	}

	public boolean submit(Subscription subscription) {

		for (String recipient : subscription.getRecipients()) {
			dispatch(recipient, subscription.getEvent());
		}

		return true;
	}

	private void dispatch(String recipient, Event event) {
		if (secureChannelGroup.contains(recipient)) {
			executor.submit(new Dispatch(secureChannelGroup.find(recipient), event));
		} else {
//			System.out.println("Written to tempDB");
		}
	}

	@Override
	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		executor.shutdown();
	}
}
