package cz.muni.fi.xtovarn.heimdalld.dispatcher;

import cz.muni.fi.xtovarn.heimdalld.db.entity.Event;
import cz.muni.fi.xtovarn.heimdalld.netty.group.SecureChannelGroup;
import org.picocontainer.Startable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dispatcher implements Startable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final SecureChannelGroup secureChannelGroup;

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
		executor.shutdown();
	}
}
