package cz.muni.fi.xtovarn.heimdall.pipeline;

import cz.muni.fi.xtovarn.heimdall.pipeline.handler.*;
import cz.muni.fi.xtovarn.heimdall.storage.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;

public class DefaultPipelineFactory implements PipelineFactory {

	private final EventStore eventStore;
	private final Dispatcher dispatcher;

//	@Inject
	public DefaultPipelineFactory(EventStore eventStore, Dispatcher dispatcher) {
		this.eventStore = eventStore;
		this.dispatcher = dispatcher;
	}

	public Pipeline getPipeline(Object o) {
		Pipeline pipeline = new Pipeline(o);

		pipeline.addHandler(new ParseJSON());
		pipeline.addHandler(new SetDetectionTime());
		pipeline.addHandler(new Store(eventStore));
		pipeline.addHandler(new DetermineRecipient());
		pipeline.addHandler(new SubmitToDispatcher(dispatcher));

		return pipeline;
	}
}
