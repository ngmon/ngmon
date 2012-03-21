package cz.muni.fi.xtovarn.heimdall.zeromq;

import java.util.List;

public interface MessageProcessor {
	public List<byte[]> process(List<byte[]> message);
}
