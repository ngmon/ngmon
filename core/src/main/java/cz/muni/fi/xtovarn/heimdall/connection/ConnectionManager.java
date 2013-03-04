package cz.muni.fi.xtovarn.heimdall.connection;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

public class ConnectionManager {

	public static class UserConnection {
		private String login;
		private Long connectionId;
		private Channel channel;

		public UserConnection(String login, Long connectionId, Channel channel) {
			this.login = login;
			this.connectionId = connectionId;
			this.channel = channel;
		}

		public String getLogin() {
			return login;
		}

		public Long getConnectionId() {
			return connectionId;
		}

		public Channel getChannel() {
			return channel;
		}

	}

	private Long nextConnectionId = 0L;

	private Map<Channel, UserConnection> channelToConnectionMap = new HashMap<>();

	public Long addConnection(String login, Channel channel) {
		channelToConnectionMap.put(channel, new UserConnection(login, nextConnectionId, channel));
		return nextConnectionId++;
	}
	
	public UserConnection getUserConnection(Channel channel) {
		return this.channelToConnectionMap.get(channel);
	}
}
