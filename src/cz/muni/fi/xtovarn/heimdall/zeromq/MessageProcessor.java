package cz.muni.fi.xtovarn.heimdall.zeromq;

import org.zeromq.ZMQException;

import java.util.List;

public interface MessageProcessor {
	public List<byte[]> process(List<byte[]> message) throws ZMQException;
}
