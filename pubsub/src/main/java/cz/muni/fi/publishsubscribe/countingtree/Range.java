package cz.muni.fi.publishsubscribe.countingtree;

/**
 * Ranges of arbitrary values
 * A range has two attributes - a start value and an end value
 * The start value should be less than (or equal to) the end value
 * 
 * @param <T1> The type of the values used
 */
public class Range<T1 extends Comparable<T1>> {
    private final T1 start;
    private final T1 end;

    public Range(T1 first, T1 second) {
        this.start = first;
        this.end = second;
    }

    public T1 getStart() {
        return start;
    }

    public T1 getEnd() {
        return end;
    }
    
    public boolean contains(T1 value) {
        if ((start.compareTo(value) <= 0) && (end.compareTo(value) >= 0)) {
            return true;
        }
        return false;
    }
    
    public boolean intersects(Range<T1> other) {
        if ((start.compareTo(other.end) <= 0) && (end.compareTo(other.start) >= 0)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Range)) {
            return false;
        }

        Range other = (Range) o;

        if (!this.start.equals(other.start)) {
            return false;
        }
        if (!this.end.equals(other.end)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }
}