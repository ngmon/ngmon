package cz.muni.fi.xtovarn.heimdalld.netty;

import cz.muni.fi.xtovarn.heimdalld.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdalld.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdalld.netty.codec.MessageEncoder;
import cz.muni.fi.xtovarn.heimdalld.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdalld.netty.handler.DefaultHandler;
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
