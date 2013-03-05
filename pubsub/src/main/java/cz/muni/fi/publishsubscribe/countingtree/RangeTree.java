package cz.muni.fi.publishsubscribe.countingtree;

import java.util.HashSet;
import java.util.Set;

/**
 * A RangeTree is essentially a map from ranges to objects, which can be
 * queried for all data associated with a particular range
 *
 * @author Kevin Dolan (http://www.thekevindolan.com/2010/02/interval-tree/index.html), adapted
 */
public class RangeTree<T extends Comparable<T>> {
    
    //TODO testy!

    /* TODO Spytat sa:
       1. "build() if out of sync" bolo v originali - ak to nechceme, premazat vsetko, co sa toho tyka
       2. mam dopisat removeRange()? alebo to budeme riesit prestavanim stromu? alebo nebude povolene mazanie? :)
    */
    
    private RangeNode<T> root;
    private Set<Range<T>> rangeSet;
    private boolean inSync;
    private int size;

    public RangeTree() {
        this.root = new RangeNode<>();
        this.rangeSet = new HashSet<>();
        this.inSync = true;
        this.size = 0;
    }

    /**
     * Instantiate and build a RangeTree with a preset set of Ranges
     *
     * @param rangeSet the set of Ranges to use
     */
    public RangeTree(Set<Range<T>> rangeSet) {
        this.root = new RangeNode<>(rangeSet);
        this.rangeSet = new HashSet<>();
        this.rangeSet.addAll(rangeSet);
        this.inSync = true;
        this.size = rangeSet.size();
    }
    
    /**
     * Add a Range object to the RangeTree's set
     * Will not rebuild the tree until the next getRangesContaining or call to build
     *
     * @param range the Range object to add
     */
    public void addRange(Range<T> range) {
        rangeSet.add(range);
        inSync = false;
    }

    /**
     * Add a Range object to the RangeTree's set
     * Will not rebuild the tree until the next getRangesContaining or call to build
     *
     * @param start the start of the Range
     * @param end the end of the Range
     */
    public void addRange(T start, T end) {
        rangeSet.add(new Range<>(start, end));
        inSync = false;
    }

    /**
     * Perform a stabbing getRanges, returning Range objects
     * Will rebuild the tree if out of sync
     *
     * @param value
     * @return all Ranges that contain value
     */
    public Set<Range<T>> getRangesContaining(T value) {
        build();
        return root.getRangesContaining(value);
    }

    /**
     * Perform a range getRanges, returning Range objects
     * Will rebuild the tree if out of sync
     *
     * @param start the start of the Range to check
     * @param end the end of the Range to check
     * @return	all Ranges that intersect target
     */
    public Set<Range<T>> getRangesIntersecting(T start, T end) {
        build();
        return root.getRangesIntersecting(new Range<>(start, end));
    }
    
    public Set<Range<T>> getRangesIntersecting(Range range) {
        build();
        return root.getRangesIntersecting(range);
    }

    /**
     * Determine whether this RangeTree is currently a reflection of all Ranges in the Range set
     *
     * @return true if no changes have been made since the last build
     */
    public boolean isInSync() {
        return inSync;
    }

    /**
     * Build the RangeTree to reflect the set of Ranges
     * 
     */
    public void build() {
        if (!inSync) {
            root = new RangeNode<>(rangeSet);
            inSync = true;
            size = rangeSet.size();
        }
    }

    /**
     * @return the number of entries in the currently built RangeTree
     */
    public int getCurrentTreeSize() {
        return size;
    }

    /**
     * @return the number of entries in the Range set, equal to .size() if inSync()
     */
    public int getSetSize() {
        return rangeSet.size();
    }
}