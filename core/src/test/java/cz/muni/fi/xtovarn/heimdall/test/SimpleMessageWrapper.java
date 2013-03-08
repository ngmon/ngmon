package cz.muni.fi.xtovarn.heimdall.test;

import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class SimpleMessageWrapper {

	public interface PrepareMessageAction {
		public Message perform(Object object);
	}

	private PrepareMessageAction action = null;
	private Object object = null;

	private SimpleMessage simpleMessage = null;

	public SimpleMessageWrapper(Directive directive, byte[] body) {
		this.simpleMessage = new SimpleMessage(directive, body);
	}

	public SimpleMessageWrapper(PrepareMessageAction action, Object object) {
		this.action = action;
		this.object = object;
	}

	private Message prepare() {
		return action.perform(this.object);
	}

	public Message getMessage() {
		return action != null ? prepare() : simpleMessage;
	}

}
