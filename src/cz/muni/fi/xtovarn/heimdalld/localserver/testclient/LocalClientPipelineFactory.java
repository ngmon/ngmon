package cz.muni.fi.xtovarn.heimdalld.localserver.testclient;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class LocalClientPipelineFactory implements ChannelPipelineFactory {
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("decoder", new StringDecoder());

		return pipeline;
	}
}
