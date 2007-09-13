package com.drap.select;


/**
 * Generic Interface to wrap method calls
 * @author David Rappoport 
 */
public interface Invoker {
    /**
     * Invokes an operation on the specified object and returns the 
     * result of the invocation. In the case of a void return value,
     * null can be returned.
     * @param o
     * @return
     */
    public Object invoke(Object o);
}
