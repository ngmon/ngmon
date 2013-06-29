package cz.muni.fi.xtovarn.heimdall.client.debug;

import cz.muni.fi.xtovarn.heimdall.client.Client;
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory;
import cz.muni.fi.xtovarn.heimdall.client.ClientConnectionFactory.ConnectionException;
import cz.muni.fi.xtovarn.heimdall.client.ClientImpl;
import cz.muni.fi.xtovarn.heimdall.client.EventReceivedHandler;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Constraint;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Operator;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.commons.entity.Event;
import cz.muni.fi.xtovarn.heimdall.util.NgmonLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Various client tests
 */
public class ClientPeek {
	/**
	 * Simple EventReceivedHandler which counts and saves received sensor events
	 */
	private static class TestEventHandler implements EventReceivedHandler {
		private AtomicInteger count = new AtomicInteger(0);
		private List<Event> events = new ArrayList<>();

		@Override
		public void handleEvent(Event event) {
			count.incrementAndGet();
			addEventToList(event);
            System.out.println(event);
        }

		private synchronized void addEventToList(Event event) {
			events.add(event);
		}

		public AtomicInteger getCount() {
			return count;
		}

		public List<Event> getEvents() {
			return events;
		}

	}

	private static final String VALID_USER_PASSWORD = "password0";
	private static final String VALID_USER_NAME = "user0";

	/**
	 * How long to wait for server response
	 */
	private static final int TIMEOUT_VALUE = 5;
	private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;
	/**
	 * The amount of time to wait for all sensor events
	 */
	private static final long EVENT_TIMEOUT_IN_MILLIS = 1000;

	private TestEventHandler testEventHandler = new TestEventHandler();

	private NgmonLauncher ngmon = null;

	private ClientImpl client = null;

    public static void main(String[] args) throws ConnectionException, ExecutionException, InterruptedException {
        Client client = ClientConnectionFactory.getClient(VALID_USER_NAME, VALID_USER_PASSWORD, TIMEOUT_VALUE, TIMEOUT_TIME_UNIT);

        Long subscriptionId = client.subscribe(getPredicate()).get();

        TestEventHandler testEventHandler = new TestEventHandler();

        client.setEventReceivedHandler(testEventHandler);

        client.ready();

    }

	private static Predicate getPredicate() {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("level", Operator.LESS_THAN, "5"));
		return predicate;
	}


}
