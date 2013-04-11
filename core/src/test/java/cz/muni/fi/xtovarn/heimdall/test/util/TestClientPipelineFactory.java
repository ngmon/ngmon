package cz.muni.fi.xtovarn.heimdall.test.util;

import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import cz.muni.fi.xtovarn.heimdall.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageEncoder;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.MessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.util.TestClient.ResponseHandler;

public class TestClientPipelineFactory implements ChannelPipelineFactory {

	private List<ResponseHandler> responseHandlers;
	private MessageHandler unsolicitedMessageHandler;

	public TestClientPipelineFactory(List<ResponseHandler> responseHandlers, MessageHandler unsolicitedMessageHandler) {
		this.responseHandlers = responseHandlers;
		this.unsolicitedMessageHandler = unsolicitedMessageHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

		pipeline.addLast("sniffer", new Sniffer());

		pipeline.addLast("framer", new LengthDecoder());

		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());

		pipeline.addLast(TestClient.MESSAGE_HANDLER_TITLE, new TestClientHandler(responseHandlers,
				unsolicitedMessageHandler));

		return pipeline;
	}

}
