package cz.muni.fi.xtovarn.heimdall.client;

import cz.muni.fi.xtovarn.heimdall.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class ClientPipelineFactory implements ChannelPipelineFactory {
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

//		pipeline.addLast("sniffer", new Sniffer());
//
//		pipeline.addLast("decompress", new ZlibDecoder());
//		pipeline.addLast("compress", new ZlibEncoder());

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		pipeline.addLast("default-handler", new DefaultClientHandler());

		return pipeline;
	}
}
