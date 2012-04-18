package cz.muni.fi.xtovarn.heimdalld.netty.message;

public interface Message {
	Directive getDirective();

	byte[] getBody();

	int length();

	int size();
}
