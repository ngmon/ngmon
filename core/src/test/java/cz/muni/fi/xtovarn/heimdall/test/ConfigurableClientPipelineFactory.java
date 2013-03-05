package cz.muni.fi.xtovarn.heimdall.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import cz.muni.fi.xtovarn.heimdall.commons.util.Pair;
import cz.muni.fi.xtovarn.heimdall.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageEncoder;

public class ConfigurableClientPipelineFactory implements ChannelPipelineFactory {
	
	private List<Pair<String, ChannelHandler>> additionalHandlers = new ArrayList<>();
	
	public void addHandler(String handlerName, ChannelHandler handler) {
		this.additionalHandlers.add(new Pair<>(handlerName, handler));
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

		pipeline.addLast("sniffer", new Sniffer());

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());
		
		for (Pair<String, ChannelHandler> handlerPair : additionalHandlers) {
			pipeline.addLast(handlerPair.getFirst(), handlerPair.getSecond());
		}

		return pipeline;
	}

}
