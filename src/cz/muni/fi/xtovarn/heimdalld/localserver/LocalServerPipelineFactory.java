package cz.muni.fi.xtovarn.heimdalld.localserver;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.string.StringDecoder;

public class LocalServerPipelineFactory implements ChannelPipelineFactory {

	private final Resender resender;

	public LocalServerPipelineFactory(Resender resender) {
		this.resender = resender;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("handler", new SimpleLocalHandler(resender));

		return pipeline;
	}
}
