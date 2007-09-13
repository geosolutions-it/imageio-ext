package com.drap.select;

import java.util.*;

/**
 * Represents a complete SELECT statement containing a 
 * SELECT, FROM, WHERE and ORDER BY clause (the SELECT clause is represented
 * by an array of <code>SelectInstruction</code>, the FROM clause is
 * represented by the data, the WHERE clause is represented by the
 * <code>Condition</code> array and the ORDER BY clause is represented by
 * the <code>OrderInstruction</code> array.
 * @author David Rappoport 
 */
public class SelectStatement implements Statement{
    
    private SelectInstruction[] selectInstructions;
    private Object[] data;
    private Condition[] conditions;
    private OrderInstruction[] orderInstructions;
    
    /**
     * @param selectInstructions
     * @param data
     * @param conditions
     * @param orderInstructions
     */
    public SelectStatement(SelectInstruction[] selectInstructions, Object[] data,
            Condition[] conditions, OrderInstruction[] orderInstructions) {
        this(selectInstructions, data, conditions);
        this.orderInstructions = orderInstructions;
    }
    
    /**
     * @param selectInstructions
     * @param data
     */
    public SelectStatement(SelectInstruction[] selectInstructions, Object[] data){
        super();
        if(selectInstructions == null || selectInstructions.length == 0){
            throw new IllegalArgumentException("You must supply at least one select instruction!");
        }
        for(int i = 0; i < selectInstructions.length; i ++){
            if(selectInstructions[i] == null){
                throw new IllegalArgumentException("Select instruction " + i + " is null!");
            }
        }
        if(data == null){
            throw new IllegalArgumentException("Data cannot be null!");
        }
        this.selectInstructions = selectInstructions;
        this.data = data;
    }
    
    /**
     * 
     * @param selectInstructions
     * @param data
     * @param conditions
     */
    public SelectStatement(SelectInstruction[] selectInstructions, Object[] data,
            Condition[] conditions) {
        this(selectInstructions, data);
        this.conditions = conditions;
    }
    
    public SelectStatement(SelectInstruction[] selectInstructions, Object[] data,
            OrderInstruction[] orderInstructions){
        this(selectInstructions, data);
        this.orderInstructions = orderInstructions;
    }
    
    /**
     * @param selectInstructions
     * @param data
     * @param conditions
     * @param orderInstructions
     */
    public SelectStatement(SelectInstruction[] selectInstructions, Collection data,
            Condition[] conditions, OrderInstruction[] orderInstructions) {
        
        this(selectInstructions, data!= null?data.toArray():null, conditions, orderInstructions);
    }
    
    /**
     * @return Returns the conditions.
     */
    public Condition[] getConditions() {
        return this.conditions;
    }
    
    /**
     * @return Returns the data.
     */
    public Object[] getData() {
        return this.data;
    }
    
    /**
     * @return Returns the orderInstructions.
     */
    public OrderInstruction[] getOrderInstructions() {
        return this.orderInstructions;
    }
    
    /**
     * @return Returns the selectInstructions.
     */
    public SelectInstruction[] getSelectInstructions() {
        return this.selectInstructions;
    }
    
}
