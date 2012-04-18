package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

public class ServerPipelineFactory implements ChannelPipelineFactory {

	private final SecureChannelGroup secureChannelGroup;

	public ServerPipelineFactory(SecureChannelGroup secureChannelGroup) {
		this.secureChannelGroup = secureChannelGroup;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		pipeline.addLast("default-handler", new DefaultHandler(secureChannelGroup));

		return pipeline;
	}
}
