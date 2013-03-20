package cz.muni.fi.xtovarn.heimdall.client.protocol;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.client.ResultFuture;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;

public class ClientProtocolContext {

	private ObjectMapper mapper = new ObjectMapper();

	private ResultFuture<Boolean> connectResult = null;

	private Long connectionId = null;

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

}
