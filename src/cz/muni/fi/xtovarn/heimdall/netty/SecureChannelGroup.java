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
package cz.muni.fi.xtovarn.heimdall.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SecureChannelGroup extends DefaultChannelGroup {

	private ConcurrentMap<String, Integer> usernameToId = new ConcurrentHashMap<String, Integer>();
	private ConcurrentMap<Integer, String> idToUsername = new ConcurrentHashMap<Integer, String>();

	private int convert(String username) {
		return usernameToId.get(username);
	}

	private String convert(int id) {
		return idToUsername.get(id);
	}

	private void remove(Integer id) {
		usernameToId.remove(convert(id));
		idToUsername.remove(id);
	}

	private void remove(String username) {
		usernameToId.remove(username);
		idToUsername.remove(convert(username));
	}

	public boolean add(String username, Channel channel) {
		boolean success = super.add(channel);

		usernameToId.put(username, channel.getId());
		idToUsername.put(channel.getId(), username);

		return success;
	}

	@Override
	public boolean remove(Object o) {
		boolean success = super.remove(o);

		if (!success) {
			return false;
		}

		if (o instanceof Integer) {
			remove((Integer) o);
		} else if (o instanceof String) {
			remove((String) o);
		} else if (o instanceof Channel) {
			remove(((Channel) o).getId());
		}

		return true;
	}
}
