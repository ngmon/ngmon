package cz.muni.fi.xtovarn.heimdall.netty.group;

import org.jboss.netty.channel.group.ChannelGroup;

public class ChannelGroups {
	private static ChannelGroup instance;

	public static ChannelGroup getSingleInstance() {
		if (instance == null) {
			instance = new SecureChannelGroup();
		}
		return instance;
	}

}
