package cz.muni.fi.xtovarn.heimdall.zeromq;

public interface MessageProcessor {
	public byte[][] process(byte[] message);
}
