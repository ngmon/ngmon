package cz.muni.fi.xtovarn.heimdall.netty;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class ChannelGroups {
	private static final ChannelGroup group = new DefaultChannelGroup("channel.childs");

	public static ChannelGroup group() {
		return group;
	}
}
