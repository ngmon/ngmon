package cz.muni.fi.xtovarn.heimdall.test;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.MessageEvent;

import cz.muni.fi.xtovarn.heimdall.test.TestClient2.MessageHandler;

class MessageHandlerWithCounter implements MessageHandler {

	private int messageCount = 0;
	private CountDownLatch countDownLatch = null;

	@Override
	public void processMessage(MessageEvent messageEvent) {
		messageCount++;
		System.out.println("MessageHandlerWithCounter.processMessage()");
		if (countDownLatch != null)
			countDownLatch.countDown();
	}

	public int getMessageCount() {
		return messageCount;
	}

	public CountDownLatch setAndGetCountDownLatch(int count) {
		return this.countDownLatch = new CountDownLatch(count);
	}

}