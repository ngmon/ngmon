package cz.muni.fi.xtovarn.heimdall.netty.message;

public class StringMessage extends Message {

	private final String body;

	public StringMessage(Directive directive, String body) {
		super(directive);
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	@Override
	public int length() {
		return DIRECTIVE_BYTES + body.length();
	}

	@Override
	public int size() {
		return LENGTH_BYTES + length();
	}
}
