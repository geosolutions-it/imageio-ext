package com.drap.select;

import java.util.*;

/**
 * Represents a single part of an ORDER BY clause. 
 * An entire ORDER BY clause is represented by an array of 
 * <code>OrderInstruction</code> objects. 
 * An object of this class consists of a mandatory <code>Invoker</code>
 * which should return the attribute to be compared, and an optional
 * <code>Comparator</code> that will be used to compare the result
 * of the invocation. If no <code>Comparator</code> is supplied,
 * the attribute will be compared using its own compareTo(Object o) 
 * method, if it implements <code>Comparable</code>
 * @author David Rappoport
 */
public class OrderInstruction {
    
    private Invoker invoker;
    private Comparator comparator; 
    
    /**
     * @param invoker
     * @param comparator
     */
    public OrderInstruction(Invoker invoker, Comparator comparator) {
        super();
        if(invoker == null){
            throw new IllegalArgumentException("Please supply a non null invoker to use for ordering");
        }
        this.invoker = invoker;
        this.comparator = comparator;
    }
    /**
     * @return Returns the comparator.
     */
    public Comparator getComparator() {
        return this.comparator;
    }
    /**
     * @return Returns the invoker.
     */
    public Invoker getInvoker() {
        return this.invoker;
    }
}
