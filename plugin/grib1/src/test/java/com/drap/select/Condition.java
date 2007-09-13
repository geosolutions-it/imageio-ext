package com.drap.select;

/**
 * 
 * Represents a condition by which to filter an object. 
 * An implementation will usually cast the Object to the
 * desired type and then invoke some kind of test on it.
 * @author David Rappoport 
 */
public interface Condition {
    
    /**
     * Should return true if the Object supplied passes this condition
     * @param o
     * @return
     */
    public boolean passes(Object o);
}
