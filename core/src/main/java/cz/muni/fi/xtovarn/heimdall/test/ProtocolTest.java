package cz.muni.fi.xtovarn.heimdall.test;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class ProtocolTest {
	
	@Before
	public void before() {
		MyAssert.clearAssertionError();
	}
	
	@After
	public void after() {
		MyAssert.throwAssertionErrorIfAny();
	}

	private static class ConnectMessageContainer extends MessageContainerImpl {

		public enum USER_TYPE {
			VALID, INVALID_USER, INVALID_PASSWORD
		}

		private static final User validUser = new User("user0", "password0");
		private static final User invalidUser = new User("incorrectUser",
				"password0");
		private static final User invalidPassword = new User("user0",
				"invalidPassword");

		public ConnectMessageContainer() throws JsonProcessingException {
			this(USER_TYPE.VALID);
		}

		public ConnectMessageContainer(USER_TYPE userType)
				throws JsonProcessingException {
			super();

			User user = null;
			switch (userType) {
			case VALID:
				user = validUser;
				break;
			case INVALID_USER:
				user = invalidUser;
				break;
			case INVALID_PASSWORD:
				user = invalidPassword;
				break;
			}
			this.addMessage(new SimpleMessage(Directive.CONNECT, getMapper()
					.writeValueAsBytes(user)));
		}
	}

	private static class ConnectHandler extends TestClientHandler {

		private static final int MESSAGES_PROCESSED_BY_HANDLER = 1;

		private Directive expectedDirective;
		private Long connectionId;

		public ConnectHandler() {
			this(Directive.CONNECTED);
		}

		public ConnectHandler(Directive expectedDirective) {
			this.expectedDirective = expectedDirective;
		}

		@Override
		public void processReceivedMessage(ChannelHandlerContext ctx,
				MessageEvent e) throws Exception {
			// super.messageReceived(ctx, e);

			// TODO - won't work when getMessagesProcessed... is overriden
			// (add private method)
			if (getMessageCount() >= super.getMessagesProcessedByHandler()
					&& getMessageCount() < getMessagesProcessedByHandler()) {
				SimpleMessage message = (SimpleMessage) e.getMessage();
				MyAssert.assertEquals(expectedDirective, message.getDirective());
				if (expectedDirective.equals(Directive.CONNECTED)) {
					@SuppressWarnings("unchecked")
					Map<String, Number> connectionIdMap = (Map<String, Number>) getMapper()
							.readValue(message.getBody(), Map.class);
					connectionId = connectionIdMap.get("connectionId").longValue();
					MyAssert.assertNotNull(connectionId);
					System.out.println("Connection ID: " + connectionId);
				}
			}
		}

		@Override
		public int getMessagesProcessedByHandler() {
			return super.getMessagesProcessedByHandler()
					+ MESSAGES_PROCESSED_BY_HANDLER;
		}

		public Long getConnectionId() {
			return connectionId;
		}

	}

	@Test
	public void testConnect() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler());
		TestClient client = new TestClient(pipelineFactory,
				new ConnectMessageContainer());
		client.run();

		/*-ChannelFactory factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ClientPipelineFactory());
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(
				NettyServer.SERVER_PORT));
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				System.out.println("Connected");
				Channel channel = future.getChannel();
				ChannelFuture future2 = channel.write(new SimpleMessage(
						Directive.CONNECT, "danos".getBytes()));
				future2.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						// System.out.println("Closing...");
						// future.getChannel().close();
					}
				});
			}
		});
		future.awaitUninterruptibly();
		System.out.println("After awaitUninterruptibly()");
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
		}
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
		System.out.println("testConnect() end");*/
	}

	@Test
	public void testIncorrectUser() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler(
				Directive.ERROR));
		TestClient client = new TestClient(pipelineFactory,
				new ConnectMessageContainer(
						ConnectMessageContainer.USER_TYPE.INVALID_USER));
		client.run();
	}

	@Test
	public void testIncorrectPassword() throws JsonProcessingException {
		ConfigurableClientPipelineFactory pipelineFactory = new ConfigurableClientPipelineFactory();
		pipelineFactory.addHandler("connect", new ConnectHandler(
				Directive.ERROR));
		TestClient client = new TestClient(pipelineFactory,
				new ConnectMessageContainer(
						ConnectMessageContainer.USER_TYPE.INVALID_PASSWORD));
		client.run();
	}
}
