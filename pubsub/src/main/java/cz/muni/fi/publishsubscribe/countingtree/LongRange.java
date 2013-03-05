package cz.muni.fi.publishsubscribe.countingtree;

public class LongRange extends Range<Long> implements Comparable<LongRange> {

	public LongRange(Long first, Long second) {
		super(first, second);
	}

	@Override
	public int compareTo(LongRange other) {
		if (super.getStart().compareTo(other.getStart()) != 0) {
			return super.getEnd().compareTo(other.getStart());
		} else {
			return super.getEnd().compareTo(other.getEnd());
		}
	}
}
