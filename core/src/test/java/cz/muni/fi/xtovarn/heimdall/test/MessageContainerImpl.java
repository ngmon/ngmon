package cz.muni.fi.xtovarn.heimdall.test;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.heimdall.netty.message.Message;

public class MessageContainerImpl implements MessageContainer {

	private List<Message> messages = new ArrayList<>();
	private static ObjectMapper mapper = new ObjectMapper();

	public void addMessage(Message message) {
		messages.add(message);
	}

	@Override
	public List<Message> getMessages() {
		return messages;
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

}
