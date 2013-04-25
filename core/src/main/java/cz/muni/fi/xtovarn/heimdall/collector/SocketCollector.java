package cz.muni.fi.xtovarn.heimdall.collector;

import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
import cz.muni.fi.xtovarn.heimdall.commons.Constants;
import cz.muni.fi.xtovarn.heimdall.commons.Startable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The service for processing sensor events
 */
public class SocketCollector implements Startable {

	private final ExecutorService service = Executors.newCachedThreadPool();
	private final PipelineFactory pipelineFactory;
	private ServerSocket serverSocket;

	// @Inject
	public SocketCollector(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}

	private void closeServerSocket(boolean setToNull) {
		if (serverSocket != null) {
			if (!serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (setToNull)
				serverSocket = null;
		}
	}

	/**
	 * Opens the server socket and processes incoming sensor data
	 */
	@Override
	public void start() {
		try {
			serverSocket = new ServerSocket(Constants.SENSOR_PORT, 50);

			while (true) {
				Socket socket = serverSocket.accept();
				service.submit(new SensorConnectionHandler(socket, pipelineFactory));
			}
		} catch (IOException e) {
			if (serverSocket != null && serverSocket.isClosed())
				; // Ignore if closed by stopServer() call
			else
				e.printStackTrace();
		} finally {
			closeServerSocket(true);
		}
	}

	@Override
	public void stop() {
		System.out.println("Closing " + this.getClass() + "...");

		closeServerSocket(false);

		System.out.println(this.getClass() + " closed!");
	}
}
