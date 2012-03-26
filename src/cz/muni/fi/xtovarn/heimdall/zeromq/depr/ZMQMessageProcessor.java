package cz.muni.fi.xtovarn.heimdall.zeromq.depr;

import org.zeromq.ZMQException;

import java.util.List;

public interface ZMQMessageProcessor {
	public List<byte[]> process(List<byte[]> message) throws ZMQException;
}
