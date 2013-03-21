package cz.muni.fi.xtovarn.heimdall.client.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class ClientProtocolContext {

	private ObjectMapper mapper = new ObjectMapper();

	private ResultFuture<Boolean> connectResult = null;
	private ResultFuture<Long> subscribeResult = null;

	private Long connectionId = null;
	
	private List<Long> subscriptionIds = new ArrayList<>();
	private boolean lastSubscriptionSuccessful = false;

	public Future<Boolean> connectRequest(Channel channel, User user) {
		try {
			connectResult = new ResultFuture<>();
			channel.write(new SimpleMessage(Directive.CONNECT, mapper.writeValueAsBytes(user)));
		} catch (JsonProcessingException e) {
			// TODO - or throw RuntimeException?
			connectResult.put(false);
		}

		return connectResult;
	}

	public void connectResponse(MessageEvent e) {
		SimpleMessage message = (SimpleMessage) e.getMessage();
		if (message.getDirective().equals(Directive.ERROR)) {
			connectResult.put(false);
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, Number> connectionIdMap = (Map<String, Number>) mapper.readValue(message.getBody(), Map.class);
			connectionId = connectionIdMap.get(Constants.CONNECTION_ID_TITLE).longValue();
			// TODO - might be null if getNewConnectResult()
			// wasn't called
			connectResult.put(true);
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
			Map<String, Number> subscriptionIdMap = (Map<String, Number>) mapper.readValue(
					message.getBody(), Map.class);
			subscriptionId = subscriptionIdMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
			if (subscriptionId != null) {
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

}
