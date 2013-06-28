package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Client interface; Object of this class is retrieved by using
 * ClientConnectionFactory
 * 
 * @see ClientConnectionFactory
 */
public interface Client {

	/**
	 * Subscribes for sensor events specified by the predicate
	 * 
	 * @param predicate
	 *            Predicate specifying which sensor events to receive
	 * @return Subscription ID
	 */
	public Future<Long> subscribe(Predicate predicate) throws InterruptedException, ExecutionException;

	/**
	 * Reverts effect of a previous subscribe() call
	 * 
	 * @param subscriptionId
	 *            ID of the subscription to cancel
	 * @return True if the operation was successful, false otherwise (for
	 *         example because of invalid server state or incorrect subscription
	 *         ID)
	 */
	public Future<Boolean> unsubscribe(Long subscriptionId) throws InterruptedException, ExecutionException;

	/**
	 * Starts receiving sensor events; These are processed using an object set
	 * by setEventReceivedHandler
	 * 
	 * @return True if the operation was successful, false otherwise (usually
	 *         because of invalid server state)
	 * 
	 * @see Client#setEventReceivedHandler(EventReceivedHandler)
	 */
	public Future<Boolean> ready();

	/**
	 * Stops the server from sending sensor events
	 * 
	 * @return True if the operation was successful, false otherwise (usually
	 *         because of invalid server state)
	 */
	public Future<Boolean> stopSending();

	/**
	 * Retrieves the sensor events which have not been received before (usually
	 * because the client was disconnected) NOT YET IMPLEMENTED!
	 */
	public Future<Boolean> get();

	/**
	 * Sets the object for processing sensor events
	 */
	public void setEventReceivedHandler(EventReceivedHandler handler);

	/**
	 * Sets the object for processing exceptions which occurred as a reaction on
	 * a server message
	 */
	public void setServerResponseExceptionHandler(ServerResponseExceptionHandler handler);

	/**
	 * Sends DISCONNECT message to server, letting him know we are going to
	 * disconnect
	 */
	public Future<Boolean> disconnect();

	/**
	 * Disconnects - closes the network channel
	 */
	public void stop();

	/**
	 * Returns to the previous state. For example when subscribe is called, but
	 * a confirmation from server is never sent, the client is "stuck" in a
	 * waiting state (so the other methods would fail). This can be used in
	 * these situations if we don't want to terminate the connection.
	 */
	public void reset();

}