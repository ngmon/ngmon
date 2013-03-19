package cz.muni.fi.xtovarn.heimdall.test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.Message;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class TestClient {

	private ChannelPipelineFactory channelPipelineFactory;
	private MessageContainer messageContainer;
	
	private Channel channel;

	public TestClient(ChannelPipelineFactory channelPipelineFactory,
			MessageContainer messageContainer) {
		this.channelPipelineFactory = channelPipelineFactory;
		this.messageContainer = messageContainer;
	}

	public void run() {
		ChannelFactory factory = new NioClientSocketChannelFactory(
				Executors.newSingleThreadExecutor(),
				Executors.newSingleThreadExecutor());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(channelPipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(
				NettyServer.SERVER_PORT));
		future.awaitUninterruptibly();
		channel = future.getChannel();
		for (SimpleMessageWrapper message : messageContainer.getMessages()) {
			future = channel.write(message.getMessage());
			future.awaitUninterruptibly();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			channel = future.getChannel();
		}
		/*-future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				System.out.println("Connected");
				Channel channel = future.getChannel();
				ChannelFuture future2 = channel.write(new SimpleMessage(
						Directive.CONNECT, "danos".getBytes()));
			}
		});
		future.awaitUninterruptibly();*/
		System.out.println("After awaitUninterruptibly()");
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
		}
		//future.getChannel().close();
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
		System.out.println("testConnect() end");
	}
	
	public void stop() {
		// TODO - check for null?
		channel.close();
	}

}
