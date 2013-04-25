package cz.muni.fi.xtovarn.heimdall.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.MessageEvent;

import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.MessageHandler;

/**
 * A simple handler which saves and counts the received messages (sent by the
 * server)
 */
public class TestMessageHandler implements MessageHandler {

	private int messageCount = 0;
	private CountDownLatch countDownLatch = null;
	private List<MessageEvent> messageEventList = new ArrayList<>();

	@Override
	public void processMessage(MessageEvent messageEvent) {
		messageCount++;
		messageEventList.add(messageEvent);
		System.out.println("MessageHandlerWithCounter.processMessage()");
		if (countDownLatch != null)
			countDownLatch.countDown();
	}

	public int getMessageCount() {
		return messageCount;
	}

	public List<MessageEvent> getMessageEventList() {
		return messageEventList;
	}

	public CountDownLatch setAndGetCountDownLatch(int count) {
		return this.countDownLatch = new CountDownLatch(count);
	}

}