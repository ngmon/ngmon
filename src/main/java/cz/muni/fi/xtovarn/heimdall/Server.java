package cz.muni.fi.xtovarn.heimdall;

import com.sleepycat.db.DatabaseException;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreIOLayer;
import cz.muni.fi.xtovarn.heimdall.db.store.EventStoreImpl;
import cz.muni.fi.xtovarn.heimdall.dispatcher.Dispatcher;
import cz.muni.fi.xtovarn.heimdall.localserver.LocalSocketServer;
import cz.muni.fi.xtovarn.heimdall.localserver.Resender;
import cz.muni.fi.xtovarn.heimdall.netty.NettyServer;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.pipeline.DefaultPipelineFactory;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.OptInCaching;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.LifecycleComponentMonitor;

import java.io.IOException;

public class Server {

	public static void main(String[] args) throws IOException, DatabaseException, InterruptedException {

		MutablePicoContainer parent = new DefaultPicoContainer();
		final MutablePicoContainer pico = new DefaultPicoContainer(new OptInCaching(), new StartableLifecycleStrategy(new LifecycleComponentMonitor()),parent);

		pico.as(Characteristics.SINGLE).addComponent(EventStoreIOLayer.class);
		pico.as(Characteristics.SINGLE).addComponent(Dispatcher.class);
		pico.as(Characteristics.SINGLE).addComponent(SecureChannelGroup.class);
		pico.as(Characteristics.SINGLE).addComponent(NettyServer.class);
		pico.as(Characteristics.SINGLE).addComponent(Resender.class);
		pico.as(Characteristics.SINGLE).addComponent(LocalSocketServer.class);
		pico.addComponent(EventStoreImpl.class);
		pico.addComponent(DefaultPipelineFactory.class);

		System.out.println(":sahs" +System.getProperty("java.library.path"));

		System.out.println("Heimdall is starting...");
		pico.start();

		class ShutdownHandler implements Runnable {
			@Override
			public void run() {
				pico.stop();
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHandler()));
	}
}
