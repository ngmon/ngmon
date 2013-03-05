package cz.muni.fi.publishsubscribe.countingtree;

import java.util.Date;

public class DateRange extends Range<Date> implements Comparable<DateRange> {

	public DateRange(Date first, Date second) {
		super(first, second);
	}

	@Override
	public int compareTo(DateRange other) {
		if (super.getStart().compareTo(other.getStart()) != 0) {
			return super.getEnd().compareTo(other.getStart());
		} else {
			return super.getEnd().compareTo(other.getEnd());
		}
	}

}
