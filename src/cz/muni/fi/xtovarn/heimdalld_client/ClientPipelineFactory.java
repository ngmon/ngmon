package cz.muni.fi.xtovarn.heimdalld_client;

import cz.muni.fi.xtovarn.heimdalld.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdalld.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdalld.netty.codec.MessageEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

public class ClientPipelineFactory implements ChannelPipelineFactory {
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		pipeline.addLast("default-handler", new ClientHandler());

		return pipeline;
	}
}
