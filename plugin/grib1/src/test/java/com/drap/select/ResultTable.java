package com.drap.select;

/**
 * Represents the data returned by executing a <code>Statement</code>. 
 * It contains the result data in the form of an <code>Object[][]</code>, 
 * allowing you to easily traverse and display the data.
 * 
 * @author David Rappoport 
 */
public interface ResultTable {
    /**
     * @return Returns the columnNames.
     */
    public abstract Object[] getColumnNames();

    /**
     * @return Returns the resultData. 
     * The first array represents the rows in the table, 
     * the second one the columns
     */
    public abstract Object[][] getResultData();
}