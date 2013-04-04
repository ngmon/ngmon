package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageEncoder;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.handler.DefaultServerHandler;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

public class ServerPipelineFactory implements ChannelPipelineFactory {

	private final SecureChannelGroup secureChannelGroup;
	private final SubscriptionManager subscriptionManager;

	public ServerPipelineFactory(SecureChannelGroup secureChannelGroup, SubscriptionManager subscriptionManager) {
		this.secureChannelGroup = secureChannelGroup;
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

//		pipeline.addLast("decompress", new ZlibDecoder());
//		pipeline.addLast("compress", new ZlibEncoder());

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		pipeline.addLast("default-handler", new DefaultServerHandler(secureChannelGroup, subscriptionManager));

		return pipeline;
	}
}
