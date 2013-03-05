package cz.muni.fi.xtovarn.heimdall.test;

import java.util.List;

import cz.muni.fi.xtovarn.heimdall.netty.message.Message;

public interface MessageContainer {
	
	List<Message> getMessages();

}
