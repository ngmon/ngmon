package cz.muni.fi.xtovarn.heimdall.pipeline;

import com.google.inject.Inject;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.pipeline.handler.*;

public class DefaultPipelineFactory implements PipelineFactory {

	private final EventStore eventStore;
	private final Dispatcher dispatcher;

	@Inject
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
