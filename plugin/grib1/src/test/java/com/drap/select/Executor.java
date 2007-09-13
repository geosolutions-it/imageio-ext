package com.drap.select;

/**
 * Executes a <code>Statement</code>
 * @author David Rappoport 
 */
public interface Executor {
    
    /**
     * Executes a Statement and returns a ResultTable
     * @param statement
     * @return
     */
    public ResultTable execute(Statement statement);
}
