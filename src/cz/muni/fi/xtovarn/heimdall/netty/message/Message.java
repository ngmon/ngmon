package cz.muni.fi.xtovarn.heimdall.netty.message;

public interface Message {
	Directive getDirective();

	byte[] getBody();

	int length();

	int size();
}
