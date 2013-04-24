package cz.muni.fi.xtovarn.heimdall.client.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.client.ResultFuture;
import cz.muni.fi.xtovarn.heimdall.client.subscribe.Predicate;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;

/**
 * The "executive" part of the client - sends messages to server and processes
 * received messages (after they are preprocessed by DefaultClientHandler)
 */
public class ClientProtocolContext {

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * The objects of type Future which hold the results of the operations
	 */
	private ResultFuture<Boolean> connectResult = null;
	private ResultFuture<Long> subscribeResult = null;
	private ResultFuture<Boolean> unsubscribeResult = null;
	private ResultFuture<Boolean> readyRequest = null;
	private ResultFuture<Boolean> stopRequest = null;
	private ResultFuture<Boolean> disconnectRequest = null;
	private ResultFuture<Boolean> getRequest = null;

	private Long connectionId = null;

	private List<Long> subscriptionIds = new ArrayList<>();
	private boolean lastSubscriptionSuccessful = false;

	private ClientEvent lastRequest = null;

	/**
	 * Sends the CONNECT message to the server (with authentication data)
	 * 
	 * @param channel
	 *            Netty connection channel
	 * @param user
	 *            Stores authentication data
	 * @return True if the connection (authentication) was successful, false
	 *         otherwise
	 */
	public Future<Boolean> connectRequest(Channel channel, User user) {
		try {
			connectResult = new ResultFuture<>();
			// this must be done before the message is sent
			// (otherwise it might happen that after sending the request
			// to server, but before the lastRequest is set, the server
			// processes the request, sends back a reply, client handler
			// (messageReceived()) is called, and some methods (ackResponse(),
			// errorResponse()) will want to read the lastRequest value)
			// the same goes for similar methods, like subscribeRequest
			lastRequest = ClientEvent.REQUEST_CONNECT;
			channel.write(new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytes(user)));
		} catch (JsonProcessingException e) {
			// TODO - or throw RuntimeException?
			connectResult.put(false);
		}

		return connectResult;
	}

	/**
	 * Processes the server response to CONNECT
	 */
	public void connectResponse(MessageEvent e) {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		// authentication failed
		if (message.getDirective().equals(Directive.ERROR)) {
			connectResult.put(false);
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, Number> connectionIdMap = (Map<String, Number>) mapper.readValue(message.getBody(), Map.class);
			connectionId = connectionIdMap.get(Constants.CONNECTION_ID_TITLE).longValue();
			connectResult.put(true);
			// retrieving of the response failed
		} catch (IOException ex) {
			connectResult.put(false);
			throw new RuntimeException("Invalid response from server");
		}
	}

	public Long getConnectionId() {
		return this.connectionId;
	}

	public boolean isConnected() {
		return getConnectionId() != null;
	}

	public Future<Long> subscribeRequest(Channel channel, Predicate predicate) {
		try {
			subscribeResult = new ResultFuture<>();
			// this must be done before the message is sent
			lastRequest = ClientEvent.REQUEST_SUBSCRIBE;
			channel.write(new SimpleMessage(Directive.SUBSCRIBE, mapper.writeValueAsBytes(predicate.toStringMap())));
			return subscribeResult;
		} catch (JsonProcessingException e) {
			subscribeResult.put(null);
			return subscribeResult;
		}
	}

	public void subscribeResponse(MessageEvent e) {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		Long subscriptionId = null;
		try {
			@SuppressWarnings("unchecked")
			Map<String, Number> subscriptionIdMap = (Map<String, Number>) mapper
					.readValue(message.getBody(), Map.class);
			subscriptionId = subscriptionIdMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
			if (subscriptionId != null) {
				// this is for testing purposes only (client user should
				// keep track of subscription IDs)
				subscriptionIds.add(subscriptionId);
			}
		} catch (IOException ex) {
		}

		lastSubscriptionSuccessful = subscriptionId != null;
		subscribeResult.put(subscriptionId);
	}

	public boolean wasLastSubscriptionSuccessful() {
		return lastSubscriptionSuccessful;
	}

	public List<Long> getSubscriptionIds() {
		return Collections.unmodifiableList(subscriptionIds);
	}

	public Long getLastSubscriptionId() {
		if (subscriptionIds.isEmpty())
			return null;
		else
			return subscriptionIds.get(subscriptionIds.size() - 1);
	}

	public Future<Boolean> unsubscribeRequest(Channel channel, Long subscriptionId) {
		try {
			unsubscribeResult = new ResultFuture<>();
			Map<String, Long> unsubscribeMap = new HashMap<>();
			unsubscribeMap.put(Constants.SUBSCRIPTION_ID_TITLE, subscriptionId);
			// this must be done before the message is sent
			lastRequest = ClientEvent.REQUEST_UNSUBSCRIBE;
			channel.write(new SimpleMessage(Directive.UNSUBSCRIBE, mapper.writeValueAsBytes(unsubscribeMap)));
			return unsubscribeResult;
		} catch (JsonProcessingException e) {
			unsubscribeResult.put(false);
			return unsubscribeResult;
		}
	}

	public void unsubscribeResponse(MessageEvent e) {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		unsubscribeResult.put(message.getDirective().equals(Directive.ACK));
	}

	public ClientEvent getLastRequest() {
		return lastRequest;
	}

	/**
	 * Handle ACK server response
	 */
	public void ackResponse(MessageEvent e) {
		switch (getLastRequest()) {
		case REQUEST_SUBSCRIBE:
			subscribeResponse(e);
			break;
		case REQUEST_UNSUBSCRIBE:
			unsubscribeResponse(e);
			break;
		case REQUEST_READY:
			readyRequest.put(true);
			break;
		case REQUEST_STOP:
			stopRequest.put(true);
			break;
		case REQUEST_GET:
			getRequest.put(true);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Handle ERROR server response
	 */
	public void errorResponse(MessageEvent e) {
		// TODO - what if lastRequest == null?
		switch (getLastRequest()) {
		case REQUEST_CONNECT:
			connectResponse(e);
			break;
		case REQUEST_SUBSCRIBE:
			subscribeResponse(e);
			break;
		case REQUEST_UNSUBSCRIBE:
			unsubscribeResponse(e);
			break;
		case REQUEST_READY:
			readyRequest.put(false);
			break;
		case REQUEST_STOP:
			stopRequest.put(false);
			break;
		case REQUEST_GET:
			getRequest.put(false);
			break;
		}
	}

	/**
	 * Sends READY (ready to start receiving sensor events) to server
	 */
	public Future<Boolean> readyRequest(Channel channel) {
		readyRequest = new ResultFuture<>();
		lastRequest = ClientEvent.REQUEST_READY;
		channel.write(new SimpleMessage(Directive.READY, "".getBytes()));

		return readyRequest;
	}

	/**
	 * Sends STOP (stop sending sensor events) to server
	 */
	public Future<Boolean> stopRequest(Channel channel) {
		stopRequest = new ResultFuture<>();
		lastRequest = ClientEvent.REQUEST_STOP;
		channel.write(new SimpleMessage(Directive.STOP, "".getBytes()));

		return stopRequest;
	}

	/**
	 * Sends DISCONNECT request to server
	 */
	public Future<Boolean> disconnectRequest(Channel channel) {
		disconnectRequest = new ResultFuture<>();
		lastRequest = ClientEvent.REQUEST_DISCONNECT;
		channel.write(new SimpleMessage(Directive.DISCONNECT, "".getBytes()));

		return disconnectRequest;
	}

	public void disconnectResponse() {
		disconnectRequest.put(true);
	}

	/**
	 * Sends GET to server (NOT YET IMPLEMENTED!)
	 */
	public Future<Boolean> getRequest(Channel channel) {
		getRequest = new ResultFuture<>();
		lastRequest = ClientEvent.REQUEST_GET;
		channel.write(new SimpleMessage(Directive.GET, "".getBytes()));

		return getRequest;
	}

}
