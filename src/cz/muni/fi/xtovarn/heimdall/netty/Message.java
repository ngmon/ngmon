package cz.muni.fi.xtovarn.heimdall.netty;

public abstract class Message {

	public static final int LENGTH_BYTES = 2;
	public static final int DIRECTIVE_BYTES = 1;

	private final Directive directive;

	protected Message(Directive directive) {
		this.directive = directive;
	}

	public Directive getDirective() {
		return directive;
	}

	public abstract int length();

	public abstract int size();
}
