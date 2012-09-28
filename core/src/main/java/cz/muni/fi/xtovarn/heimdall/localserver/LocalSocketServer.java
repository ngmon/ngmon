package cz.muni.fi.xtovarn.heimdall.localserver;

import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalSocketServer implements Startable {

	private final ExecutorService service = Executors.newCachedThreadPool();
	private final PipelineFactory pipelineFactory;
	private ServerSocket serverSocket;

//	@Inject
	public LocalSocketServer(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	@Override
	public void start() {
		try {
			serverSocket = new ServerSocket(5000, 50);

			while (true) {
				Socket socket = serverSocket.accept();
				service.submit(new LocalConnectionHandler(socket, pipelineFactory));
			}
		} catch (IOException e) {
			if (serverSocket != null && serverSocket.isClosed())
				; //Ignore if closed by stopServer() call
			else
				e.printStackTrace();
		} finally {
			serverSocket = null;
		}
	}

	@Override
	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(this.getClass() + " closed!");
	}
}
