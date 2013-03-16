package cz.muni.fi.xtovarn.heimdall.client.subscribe;

public enum Operator {

	EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL;

	@Override
	public String toString() {
		switch (this) {
		case EQUALS:
			return "#eq";
		case LESS_THAN:
			return "#lt";
		case LESS_THAN_OR_EQUAL:
			return "#le";
		case GREATER_THAN:
			return "#gt";
		case GREATER_THAN_OR_EQUAL:
			return "#ge";
		default:
			throw new IllegalArgumentException();
		}
	}

}
