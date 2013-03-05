package cz.muni.fi.publishsubscribe.countingtree;

import java.util.*;

/**
 * The Node class contains the RangeTree information for a single node
 *
 * @author ???
 */
public class RangeNode<T extends Comparable<T>> {

    /* TODO Spytat sa:
     1. Give credit where credit is due - ale ako presne? totiz, to bolo tak:
     * okopirovala som to z http://www.thekevindolan.com/2010/02/interval-tree/index.html (autor Kevin Dolan)
     * potom som to upravila pre genericke typy, opravila dokumentaciu a nazvy metod/premennych, ak sa mi nepacili, atd.
     * a getRanges* metody som odpisala zase odtialto: https://github.com/mbuchetics/RangeTree
     */
    
    private T centre;
    private SortedSet<Range<T>> ranges; //all ranges overlapping the centre
    private RangeNode<T> leftNode;
    private RangeNode<T> rightNode;

    public RangeNode() {
        ranges = new TreeSet<>();
        centre = null;
        leftNode = null;
        rightNode = null;
    }

    public RangeNode(Set<Range<T>> rangeSet) {
        ranges = new TreeSet<>();
        SortedSet<T> endpoints = new TreeSet<>();

        for (Range<T> r : rangeSet) {
            endpoints.add(r.getStart());
            endpoints.add(r.getEnd());
        }

        T median = getMedian(endpoints);
        centre = median;

        Set<Range<T>> left = new HashSet<>();
        Set<Range<T>> right = new HashSet<>();

        for (Range<T> r : rangeSet) {
            if (r.getEnd().compareTo(median) < 0) {
                left.add(r);
            } else {
                if (r.getStart().compareTo(median) > 0) {
                    right.add(r);
                } else {
                    ranges.add(r);
                }
            }
        }

        if (left.size() > 0) {
            leftNode = new RangeNode<>(left);
        }
        if (right.size() > 0) {
            rightNode = new RangeNode<>(right);
        }
    }

    public T getCentre() {
        return centre;
    }

    public void setCentre(T centre) {
        this.centre = centre;
    }

    public RangeNode<T> getLeft() {
        return leftNode;
    }

    public void setLeft(RangeNode<T> left) {
        this.leftNode = left;
    }

    public RangeNode<T> getRight() {
        return rightNode;
    }

    public void setRight(RangeNode<T> right) {
        this.rightNode = right;
    }

    /**
     * Perform a stabbing getRanges on the node
     *
     * @param value
     * @return all ranges containing value
     */
    public Set<Range<T>> getRangesContaining(T value) {
        Set<Range<T>> result = new HashSet<>();

	    if (this.ranges.isEmpty()) {
		    return Collections.emptySet();
	    }

        for (Range<T> r : ranges) {
            if (r.getStart().compareTo(value) > 0) {
                break;
            } else {
                if (r.contains(value)) {
                    result.add(r);
                }
            }
        }

        if (value.compareTo(centre) < 0 && leftNode != null) {
            result.addAll(leftNode.getRangesContaining(value));
        } else if (value.compareTo(centre) > 0 && rightNode != null) {
            result.addAll(rightNode.getRangesContaining(value));
        }

        return result;
    }

    /**
     * Perform a range intersection getRanges on the node
     *
     * @param target
     * @return all Ranges intersecting with target
     */
    public Set<Range<T>> getRangesIntersecting(Range<T> target) {
        Set<Range<T>> result = new HashSet<>();

        for (Range<T> r : ranges) {
            if (r.getStart().compareTo(target.getEnd()) > 0) {
                break;
            } else {
                if (r.intersects(target)) {
                    result.add(r);
                }
            }
        }

        if (target.getStart().compareTo(centre) < 0 && leftNode != null) {
            result.addAll(leftNode.getRangesIntersecting(target));
        }
        if (target.getEnd().compareTo(centre) > 0 && rightNode != null) {
            result.addAll(rightNode.getRangesIntersecting(target));
        }

        return result;
    }

    /**
     * @param set
     * @return the median of the set, not interpolated
     */
    private T getMedian(SortedSet<T> set) {
        int i = 0;
        int middle = set.size() / 2;
        for (T item : set) {
            if (i == middle) {
                return item;
            }
            i++;
        }
        return null;
    }
}
