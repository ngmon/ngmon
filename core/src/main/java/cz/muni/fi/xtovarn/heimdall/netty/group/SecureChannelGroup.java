/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cz.muni.fi.xtovarn.heimdall.netty.group;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class SecureChannelGroup extends DefaultChannelGroup implements ChannelGroup {

	private ConcurrentMap<String, Integer> usernameToId = new ConcurrentHashMap<String, Integer>();
	private ConcurrentMap<Integer, String> idToUsername = new ConcurrentHashMap<Integer, String>();
	// Set might be better for this, but there is no ConcurrentHashSet...
	private ConcurrentMap<String, Boolean> receivingUsers = new ConcurrentHashMap<>();

	/* Util methods */
	private int convertToId(String username) {
		return usernameToId.get(username);
	}

	private String convertToUsername(int id) {
		return idToUsername.get(id);
	}

	private void removeById(Integer id) {
		String username = convertToUsername(id);
		usernameToId.remove(username);
		idToUsername.remove(id);
		receivingUsers.remove(username);
	}

	private void removeByUsername(String username) {
		usernameToId.remove(username);
		int id = convertToId(username);
		idToUsername.remove(id);
		receivingUsers.remove(username);
	}

	public boolean add(String username, Channel channel) {
		boolean success = super.add(channel);

		usernameToId.put(username, channel.getId());
		idToUsername.put(channel.getId(), username);

		return success;
	}

	public Channel find(String username) {
		return super.find(convertToId(username));
	}

	public String getUsername(Channel channel) {
		return convertToUsername(channel.getId());
	}

	public boolean contains(String username) {
		if (!usernameToId.containsKey(username)) {
			return false;
		}

		return super.contains(convertToId(username));
	}

	@Override
	public boolean remove(Object o) {
		boolean success = super.remove(o);

		if (!success) {
			return false;
		}

		if (o instanceof Integer) {
			removeById((Integer) o);
		} else if (o instanceof String) {
			removeByUsername((String) o);
		} else if (o instanceof Channel) {
			removeById(((Channel) o).getId());
		}

		return true;
	}

	public boolean setReceiving(Channel channel, boolean receiving) {
		String username = getUsername(channel);
		if (username == null)
			return false;

		if (receiving)
			receivingUsers.put(username, receiving);
		else
			receivingUsers.remove(username);
			
		return true;
	}

	public Set<String> getReceivingUsers() {
		return Collections.unmodifiableSet(receivingUsers.keySet());
	}
}
