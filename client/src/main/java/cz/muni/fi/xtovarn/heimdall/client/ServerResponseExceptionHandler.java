package cz.muni.fi.xtovarn.heimdall.client;

/**
 * For handling exceptions caused by unexpected server messages
 */
public interface ServerResponseExceptionHandler {
	
	public void handleException(Throwable throwable);

}
