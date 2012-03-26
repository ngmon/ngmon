package cz.muni.fi.xtovarn.heimdall.zeromq.deprecated;

import cz.muni.fi.xtovarn.heimdall.entity.Event;
import org.zeromq.ZMQException;

import java.util.List;

@Deprecated
public interface ZMQMessageProcessor {
	public List<byte[]> process(List<byte[]> message) throws ZMQException;
}
