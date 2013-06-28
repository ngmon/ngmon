package cz.muni.fi.xtovarn.heimdall.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.muni.fi.xtovarn.heimdall.NgmonServer;

public class ShutdownHandler implements Runnable {
	
	private NgmonServer ngmonServer;
	
	private static Logger logger = LogManager.getLogger(ShutdownHandler.class);
	
	public ShutdownHandler(NgmonServer ngmonServer) {
		this.ngmonServer = ngmonServer;
	}
	
	@Override
	public void run() {
		logger.info("Shutting down...");
		ngmonServer.stop();
	}
}