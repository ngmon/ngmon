package cz.muni.fi.xtovarn.heimdall.netty.message;

public abstract class AbstractMessage {

	public static final int LENGTH_BYTES = 2;
	public static final int DIRECTIVE_BYTES = 1;

	private final Directive directive;

	protected AbstractMessage(Directive directive) {
		this.directive = directive;
	}

	public Directive getDirective() {
		return directive;
	}

	public abstract int length();

	public abstract int size();
}
