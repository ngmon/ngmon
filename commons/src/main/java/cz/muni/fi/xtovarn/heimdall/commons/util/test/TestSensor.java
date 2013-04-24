package cz.muni.fi.xtovarn.heimdall.commons.util.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import cz.muni.fi.xtovarn.heimdall.commons.Constants;

/**
 * A simple sensor designed to be used in tests (for sending events to the
 * server)
 */
public class TestSensor {

	private static final String HOST_NAME = "localhost";

	private Socket socket;
	private BufferedWriter out;

	public TestSensor() {
		try {
			this.socket = new Socket(HOST_NAME, Constants.SENSOR_PORT);
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendString(String string) {
		try {
			out.write(string);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			out.close();
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
