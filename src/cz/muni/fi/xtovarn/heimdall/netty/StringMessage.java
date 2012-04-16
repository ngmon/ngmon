package cz.muni.fi.xtovarn.heimdall.netty;

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
		return Message.DIRECTIVE_BYTES + body.length();
	}

	@Override
	public int size() {
		return Message.LENGTH_BYTES + length();
	}
}
