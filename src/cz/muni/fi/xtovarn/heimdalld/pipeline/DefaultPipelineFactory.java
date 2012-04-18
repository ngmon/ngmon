package cz.muni.fi.xtovarn.heimdalld.pipeline;

import cz.muni.fi.xtovarn.heimdalld.db.store.EventStore;
import cz.muni.fi.xtovarn.heimdalld.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdalld.pipeline.handler.*;

public class DefaultPipelineFactory implements PipelineFactory {

	private final EventStore eventStore;
	private final Dispatcher dispatcher;

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
