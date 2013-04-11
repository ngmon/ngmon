package cz.muni.fi.xtovarn.heimdall.test.util;

import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.junit.Assert;

import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;
import cz.muni.fi.xtovarn.heimdall.netty.protocol.Constants;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.ResponseHandler;

public class TestResponseHandlers {

	public static ObjectMapperWrapper mapper = new ObjectMapperWrapper();

	public static final ResponseHandler SUBSCRIBE_RESPONSE_HANDLER = new ResponseHandler() {

		@Override
		public Object processResponse(MessageEvent messageEvent) {
			SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
			Assert.assertEquals(Directive.ACK, message.getDirective());
			@SuppressWarnings("unchecked")
			Map<String, Number> subscriptionIdMap = (Map<String, Number>) mapper.readValueNoExceptions(
					message.getBody(), Map.class);
			Long subscriptionId = subscriptionIdMap.get(Constants.SUBSCRIPTION_ID_TITLE).longValue();
			Assert.assertNotNull(subscriptionId);
			return subscriptionId;
		}
	};

	public static final ResponseHandler CONNECT_RESPONSE_HANDLER = new ResponseHandler() {

		@Override
		public Object processResponse(MessageEvent messageEvent) {
			SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
			Assert.assertEquals(Directive.CONNECTED, message.getDirective());
			@SuppressWarnings("unchecked")
			Map<String, Number> connectionIdMap = (Map<String, Number>) mapper.readValueNoExceptions(message.getBody(),
					Map.class);
			Long connectionId = connectionIdMap.get(Constants.CONNECTION_ID_TITLE).longValue();
			Assert.assertNotNull(connectionId);
			return connectionId;
		}
	};

	public static final ResponseHandler ACK_RESPONSE_HANDLER = new ResponseHandler() {

		@Override
		public Object processResponse(MessageEvent messageEvent) {
			SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
			Assert.assertEquals(Directive.ACK, message.getDirective());
			return null;
		}
	};

	public static final ResponseHandler ERROR_RESPONSE_HANDLER = new ResponseHandler() {

		@Override
		public Object processResponse(MessageEvent messageEvent) {
			SimpleMessage message = (SimpleMessage) messageEvent.getMessage();
			Assert.assertEquals(Directive.ERROR, message.getDirective());
			return null;
		}
	};

}
