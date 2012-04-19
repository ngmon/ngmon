package cz.muni.fi.xtovarn.heimdalld.localserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PushToResender implements Runnable {

	private final Socket socket;
	private final Resender resender;

	public PushToResender(Socket socket, Resender resender) {
		this.socket = socket;
		this.resender = resender;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String string;

			while ((string = in.readLine()) != null) {
				resender.pipeline(string);
			}

			in.close();
			socket.close();

		} catch (IOException e) {
		}
	}
}
