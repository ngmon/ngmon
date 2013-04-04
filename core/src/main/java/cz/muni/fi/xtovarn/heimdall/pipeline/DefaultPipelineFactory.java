package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.handler.*;
import cz.muni.fi.xtovarn.heimdall.pubsub.SubscriptionManager;
import cz.muni.fi.xtovarn.heimdall.storage.EventStore;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;

public class DefaultPipelineFactory implements PipelineFactory {

	private final EventStore eventStore;
	private final Dispatcher dispatcher;
	private SecureChannelGroup secureChannelGroup;
	private final SubscriptionManager subscriptionManager;

	// @Inject
	public DefaultPipelineFactory(EventStore eventStore, Dispatcher dispatcher, SecureChannelGroup scg,
			SubscriptionManager subscriptionManager) {
		this.eventStore = eventStore;
		this.dispatcher = dispatcher;
		this.secureChannelGroup = scg;
		this.subscriptionManager = subscriptionManager;
	}

	public Pipeline getPipeline(Object o) {
		Pipeline pipeline = new Pipeline(o);

		pipeline.addHandler(new ParseJSON());
		pipeline.addHandler(new SetDetectionTime());
		pipeline.addHandler(new Store(eventStore));
		pipeline.addHandler(new DetermineRecipient(secureChannelGroup, subscriptionManager));
		pipeline.addHandler(new SubmitToDispatcher(dispatcher));

		return pipeline;
	}
}
