package cz.muni.fi.xtovarn.heimdall.client.test.util;

import cz.muni.fi.xtovarn.heimdall.NgmonServer;

public class ShutdownHandler implements Runnable {
	
	private NgmonServer ngmonServer;
	
	public ShutdownHandler(NgmonServer ngmonServer) {
		this.ngmonServer = ngmonServer;
	}
	
	@Override
	public void run() {
		System.out.println("Shutting down...");
		ngmonServer.stop();
	}
}