package cz.muni.fi.xtovarn.heimdall.netty;

public class ChannelGroups {
	private static SecureChannelGroup instance;

	public static SecureChannelGroup getSingleInstance() {
		if (instance == null) {
			instance = new SecureChannelGroup();
		}
		return instance;
	}

}
