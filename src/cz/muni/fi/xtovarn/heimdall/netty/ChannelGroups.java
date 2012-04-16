package cz.muni.fi.xtovarn.heimdall.netty;

import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;

public class ChannelGroups {
	private static SecureChannelGroup instance;

	public static SecureChannelGroup getSingleInstance() {
		if (instance == null) {
			instance = new SecureChannelGroup();
		}
		return instance;
	}

}
