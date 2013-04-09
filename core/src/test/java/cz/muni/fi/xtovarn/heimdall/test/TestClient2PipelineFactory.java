package cz.muni.fi.xtovarn.heimdall.test;

import java.util.List;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import cz.muni.fi.xtovarn.heimdall.netty.codec.LengthDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageDecoder;
import cz.muni.fi.xtovarn.heimdall.netty.codec.MessageEncoder;
import cz.muni.fi.xtovarn.heimdall.test.TestClient2.MessageHandler;
import cz.muni.fi.xtovarn.heimdall.test.TestClient2.ResponseHandler;

public class TestClient2PipelineFactory implements ChannelPipelineFactory {

	private List<ResponseHandler> responseHandlers;
	private MessageHandler unsolicitedMessageHandler;

	public TestClient2PipelineFactory(List<ResponseHandler> responseHandlers, MessageHandler unsolicitedMessageHandler) {
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

		pipeline.addLast(TestClient2.MESSAGE_HANDLER_TITLE, new TestClient2Handler(responseHandlers,
				unsolicitedMessageHandler));

		return pipeline;
	}

}
