package com.drap.select;


/**
 * Represents a single part of a SELECT clause, namely a column and a column name. 
 * An entire SELECT clause is represented by an array of 
 * <code>SelectInstruction</code> objects
 * @author David Rappoport 
 */
public class SelectInstruction {
    
    public static final String SELECT_ALL = "*";
    
    private String columnName;
    private Invoker invoker;
    
    /**
     * @param columnName
     * @param invoker
     */
    public SelectInstruction(String columnName, Invoker invoker) {
        super();
        if(columnName == null){
            throw new IllegalArgumentException("Please supply a non null column name to use for select");
        }
        if(invoker == null && !columnName.equals(SELECT_ALL)){
            throw new IllegalArgumentException("Please supply a non null invoker to use for select");
        }
        this.columnName = columnName;
        this.invoker = invoker;
    }
    /**
     * @return Returns the columnName.
     */
    public String getColumnName() {
        return this.columnName;
    }
    /**
     * @return Returns the invoker.
     */
    public Invoker getInvoker() {
        return this.invoker;
    }
}
