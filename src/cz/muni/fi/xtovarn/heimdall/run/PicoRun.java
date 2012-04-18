package cz.muni.fi.xtovarn.heimdall.run;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.netty.ServerPipelineFactory;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.runnable.NettyServer;
import cz.muni.fi.xtovarn.heimdall.runnable.SocketServer;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.OptInCaching;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.LifecycleComponentMonitor;

import java.io.IOException;

public class PicoRun {

	public static void main(String[] args) throws IOException, DatabaseException {
		MutablePicoContainer parent = new DefaultPicoContainer();
		MutablePicoContainer pico = new DefaultPicoContainer(new OptInCaching(), new StartableLifecycleStrategy(new LifecycleComponentMonitor()),parent);

		pico.as(Characteristics.SINGLE).addComponent(EventStoreIOLayer.class);
		pico.as(Characteristics.SINGLE).addComponent(Dispatcher.class);
		pico.as(Characteristics.SINGLE).addComponent(SecureChannelGroup.class);
		pico.as(Characteristics.SINGLE).addComponent(NettyServer.class);
		pico.as(Characteristics.SINGLE).addComponent(SocketServer.class);
		pico.addComponent(EventStoreImpl.class);
		pico.addComponent(ServerPipelineFactory.class);

		pico.start();
	}
}
