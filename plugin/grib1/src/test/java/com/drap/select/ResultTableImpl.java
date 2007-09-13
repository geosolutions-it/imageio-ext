package com.drap.select;


/**
 * Implements the ResultTable interface
 * @author David Rappoport 
 */
public class ResultTableImpl implements ResultTable {
    
    private Object[][] resultData;
    private Object[] columns;
    
    public ResultTableImpl(Object[][] resultData, Object[] columns){
        this.resultData = resultData;
        this.columns = columns;
    }
    /**
     * @return Returns the columnNames.
     */
    public Object[] getColumnNames() {
        return this.columns;
    }
    
    /**
     * @return Returns the resultData.
     */
    public Object[][] getResultData() {
        return this.resultData;
    }
}
