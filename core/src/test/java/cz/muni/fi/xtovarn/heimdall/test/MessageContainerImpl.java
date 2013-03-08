package cz.muni.fi.xtovarn.heimdall.test;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageContainerImpl implements MessageContainer {

	private List<SimpleMessageWrapper> messages = new ArrayList<>();
	private static ObjectMapper mapper = new ObjectMapper();

	public void addMessage(SimpleMessageWrapper message) {
		messages.add(message);
	}

	@Override
	public List<SimpleMessageWrapper> getMessages() {
		return messages;
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

}
