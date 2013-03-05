package cz.muni.fi.xtovarn.heimdall.netty.protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.fi.xtovarn.fsm.AbstractFiniteStateMachine;
import cz.muni.fi.xtovarn.fsm.action.Action;
import cz.muni.fi.xtovarn.heimdall.entities.User;
import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
import cz.muni.fi.xtovarn.heimdall.netty.message.Directive;
import cz.muni.fi.xtovarn.heimdall.netty.message.SimpleMessage;

public class ServerFSM extends AbstractFiniteStateMachine<ServerState, ServerEvent, ServerContext> {
	
	private static class UserStore {
		
		private Map<String, String> userMap = new HashMap<>();
		
		public UserStore() {
			for (int i = 0; i < 10; i++) {
				userMap.put("user" + i, "password" + i);
			}
		}
		
		public boolean verifyLogin(String login, String passcode) {
			String userPasscode = userMap.get(login);
			return userPasscode != null && passcode.equals(userPasscode);
		}
		
	}
	
	private ObjectMapper mapper = new ObjectMapper();
	private UserStore userStore = new UserStore();

	public ServerFSM(final SecureChannelGroup secureChannelGroup) {
		super(ServerState.CREATED, new ServerState[]{ServerState.DISCONNECTED}, ServerState.class, true);

		buildTransitions(secureChannelGroup);
	}

	public void buildTransitions(final SecureChannelGroup secureChannelGroup) {
		this.addTransition(ServerState.CREATED, ServerEvent.NETTY_TCP_CONNECTED, ServerState.PRE_CONNECTED, null);

		this.addTransition(ServerState.PRE_CONNECTED, ServerEvent.RECEIVED_CONNECT, ServerState.CONNECTED, new Action<ServerContext>() {

			@Override
			public boolean perform(ServerContext context) {
				SimpleMessage message = (SimpleMessage) context.getMessageEvent().getMessage();
				boolean verified = false;
				Channel channel = context.getMessageEvent().getChannel();
				Map<String, Integer> connectionIdMap = new HashMap<>();
				try {
					User user = mapper.readValue(message.getBody(), User.class);
					verified = userStore.verifyLogin(user.getLogin(), user.getPasscode());
					if (verified) {
						secureChannelGroup.add(user.getLogin(), channel);
						// TODO - extract the string constant
						connectionIdMap.put("connectionId", channel.getId());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					SimpleMessage replyMessage = new SimpleMessage(verified ? Directive.CONNECTED : Directive.ERROR,
							verified ? mapper.writeValueAsBytes(connectionIdMap) : "".getBytes());
					ChannelFuture future = channel.write(replyMessage);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}

				return verified;
			}
		});
	}
}
