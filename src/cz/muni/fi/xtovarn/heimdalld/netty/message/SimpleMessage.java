package cz.muni.fi.xtovarn.heimdalld.netty.message;

public class SimpleMessage extends AbstractMessage implements Message {

	private final byte[] body;

	public SimpleMessage(Directive directive, byte[] body) {
		super(directive);
		this.body = body;
	}

	public byte[] getBody() {
		return body;
	}

	@Override
	public int length() {
		return DIRECTIVE_BYTES + body.length;
	}

	@Override
	public int size() {
		return LENGTH_BYTES + length();
	}
}
