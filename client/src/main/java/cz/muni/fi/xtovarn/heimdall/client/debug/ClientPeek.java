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
import cz.muni.fi.xtovarn.heimdall.commons.json.JSONStringParser;
import cz.muni.fi.xtovarn.heimdall.util.NgmonLauncher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Various client tests
 */
public class ClientPeek {
	/**
	 * Simple EventReceivedHandler which counts and saves received sensor events
	 */
	private static class TestEventHandler implements EventReceivedHandler {

        private SimpleFileWriter output = new SimpleFileWriter("clientpeek.log");

		@Override
		public void handleEvent(Event event) {
            try {
                output.write(JSONStringParser.eventToString(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Client client = null;
        try {
            client = ClientConnectionFactory.getClient(VALID_USER_NAME, VALID_USER_PASSWORD, TIMEOUT_VALUE, TIMEOUT_TIME_UNIT);
        } catch (ConnectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("Connected");

        Long subscriptionId = client.subscribe(getPredicate()).get();

        for (int i = 0; i < 1000; i++) {
            Future f = client.subscribe(getPredicate(i));
            f.get();
        }


        TestEventHandler testEventHandler = new TestEventHandler();

        client.setEventReceivedHandler(testEventHandler);

        client.ready();

        System.out.println("Ready");

    }

	private static Predicate getPredicate() {
		Predicate predicate = new Predicate();
		predicate.addConstraint(new Constraint("level", Operator.GREATER_THAN, "5"));
		predicate.addConstraint(new Constraint("type", Operator.EQUALS, "login"));
		return predicate;
	}

    private static Predicate getPredicate(int pre) {
        Predicate predicate = new Predicate();
        predicate.addConstraint(new Constraint("level", Operator.GREATER_THAN, "5"));
        predicate.addConstraint(new Constraint("type", Operator.EQUALS, "log"+pre));
        return predicate;
    }


}
