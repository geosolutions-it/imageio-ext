package com.drap.select;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class implements the Executor Interface and allows for the 
 * execution of a SelectStatement. 
 * @author David Rappoport 
 */
public class ExecutorImpl implements Executor {

    public static final Class[] EMPTY_CLASS_ARRAY = new Class[] {};
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
    public static final Invoker[] EMPTY_INVOKER_ARRAY = new Invoker[] {};
    public static final String[] EMPTY_STRING_ARRAY = new String[] {};

    /**
     * Executes a Statement. currently only SelectStatements are supported.
     * @param statement May not be null!
     * @return a ResultTable object containing 
     * a Object[] of (String) columnNames and an Object[][] of values returned
     */
    public ResultTable execute(Statement statement) {

        if (statement == null) {
            throw new IllegalArgumentException("Please supply a non null statement!");
        }

        if (statement instanceof SelectStatement) {
            return executeSelectStatement((SelectStatement) statement);
        }
        return null;
    }

    /**
     * Executes the SelectStatement, first filtering the data based on the conditions,
     * then ordering the filtered data based on the ordering instructions, then selecting
     * those values contained in the selectInstructions
     * @param selectStatement
     * @return a ResultTable object containing in the case of a select statement
     * a String[] of columnNames and an Object[][] of values returned
     */
    private ResultTable executeSelectStatement(SelectStatement selectStatement) {

        ResultTable resultTable = null;

        Object[] data = selectStatement.getData();
        SelectInstruction[] selectInstructions = selectStatement.getSelectInstructions();
        Condition[] conditions = selectStatement.getConditions();
        OrderInstruction[] orderInstructions = selectStatement.getOrderInstructions();

        if (data != null && data.length > 0 && selectInstructions.length > 0
                && selectInstructions[0] != null) {

            //We need the first object in the array to get the methods
            Class dataClass = data[0].getClass();

            //Get column names and method names from selectInstructions
            Invoker[] invokers = getInvokers(selectInstructions, dataClass);
            String[] columnNames = getColumnNames(selectInstructions, dataClass);

            if (invokers != null && columnNames != null) {

                //Filter all data in the array using the conditions, first make sure that each
                // object is from the same class!
                List list = checkClassAndApplyConditions(data, conditions, dataClass);

                //Now sort all object in the list using the orderInstructions
                if (orderInstructions != null && orderInstructions.length > 0) {

                    Comparator compositeComparator = createCompositeComparator(orderInstructions);
                    Collections.sort(list, compositeComparator);

                    resultTable = buildResultTable(columnNames, invokers, list);

                } else {//No sorting needed

                    resultTable = buildResultTable(columnNames, invokers, list);
                }
            }
        }
        return resultTable;
    }

    /**
     * @param selectInstructions
     * @param dataClass
     * @return a String[] containing columnNames. 
     * If only one SelectInstruction was defined and its column name is "*", then the names of
     * all getter methods (empty parameter methods starting with "get") are returned, without the "get"
     * (For example, if the method name was getColor, then the column name will be Color). 
     */
    private String[] getColumnNames(SelectInstruction[] selectInstructions, Class dataClass) {
        String[] columnNames = null;

        if (selectInstructions.length == 1
                && SelectInstruction.SELECT_ALL.equals(selectInstructions[0].getColumnName())) {
            columnNames = getAllColumns(dataClass);
        } else {
            columnNames = new String[selectInstructions.length];
            for (int i = 0; i < selectInstructions.length; i++) {
                SelectInstruction instruction = selectInstructions[i];
                if (instruction == null) {
                    throw new IllegalArgumentException("SelectInstruction at position: " + i
                            + " is null!");
                }
                columnNames[i] = instruction.getColumnName();
            }
        }
        return columnNames;
    }

    /**
     * @param objects
     * @param columnNames
     * @param invokers
     * @param list
     * @return builds the result table by calling the invoke() method of all the invokers 
     * (in the specified order) on all objects in the list. The resulting object[][] contains 
     * the values returned by these method invocations. The position in the first array 
     * denotes the row number, while the position in the second array denotes the column number.
     */
    private ResultTable buildResultTable(String[] columnNames, Invoker[] invokers, List list) {
        ResultTable resultTable;

        Object[][] resultData = new Object[list.size()][columnNames.length];
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            for (int e = 0; e < invokers.length; e++) {
                resultData[i][e] = invokers[e].invoke(o);
            }
        }

        resultTable = new ResultTableImpl(resultData, columnNames);
        return resultTable;
    }

    /**
     * @param orderInstructions
     * @param objectClass
     * @return a Comparator composed of all the explicit or implicit comparators
     * contained in the orderInstructions. For each comparison, all the comparators
     * are consulted in the order they were added to the array. If any of the comparators 
     * returns a value != 0, that value is returned and the later comparators are ignored.
     */
    private Comparator createCompositeComparator(final OrderInstruction[] orderInstructions) {

        Comparator compositeComparator;
        compositeComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                int result = 0;

                //the following for loop breaks when either the end of the orderInstruction
                //has been reached, or the result is not zero
                for (int i = 0; i < orderInstructions.length && result == 0; i++) {
                    OrderInstruction instruction = orderInstructions[i];
                    if (instruction == null) {
                        throw new IllegalArgumentException("OrderInstruction at position " + i
                                + " is null");
                    }
                    Object value1 = instruction.getInvoker().invoke(o1);
                    Object value2 = instruction.getInvoker().invoke(o2);

                    if (value1 == null && value2 == null)
                        return 0;
                    if (value1 == null)
                        return -1;
                    if (value2 == null)
                        return +1;

                    Comparator comparator = instruction.getComparator();
                    if (comparator != null) {
                        result = comparator.compare(value1, value2);
                    } else if (value1 instanceof Comparable) {
                        result = ((Comparable) value1).compareTo(value2);
                    }
                }
                return result;
            }
        };
        return compositeComparator;
    }

    /**
     * Checks if the object is of the same class and passes all the conditions.
     * @param objects
     * @param conditions
     * @param objectClass
     * @param list
     */
    private List checkClassAndApplyConditions(Object[] objects, Condition[] conditions,
            Class objectClass) {
        List list = new ArrayList();
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            if (o == null) {
                throw new IllegalArgumentException("Object in data at position " + i + " is null");
            }
            checkClass(objectClass, i, o);
            if (passesAllConditions(o, conditions)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * @param o
     * @param conditions
     * @return true if all conditions return true when invoking the passes() method with the 
     * object as parameter
     */
    private boolean passesAllConditions(Object o, Condition[] conditions) {
        boolean passedAllConditions = true;
        if (conditions != null && conditions.length > 0) {
            for (int e = 0; e < conditions.length; e++) {
                Condition condition = conditions[e];
                if (condition == null) {
                    throw new IllegalArgumentException("Condition at position " + e + " is null");
                }
                if (!condition.passes(o)) {
                    passedAllConditions = false;
                    break;
                }
            }
        }
        return passedAllConditions;
    }

    /**
     * Throws an IllegalArgumentException if the supplied object is not of the same class as 
     * objectClass
     * @param objectClass
     * @param i
     * @param o
     */
    private void checkClass(Class objectClass, int i, Object o) {
        if (!o.getClass().equals(objectClass)) {

            throw new IllegalArgumentException(
                    "Objects passed are not from the same class! Object at position 0 has class "
                            + objectClass.getName() + ", but object at position " + i
                            + " has class " + o.getClass().getName());
        }
    }

    /**
     * @param selectInstructions
     * @param objectClass
     * @param methods
     * @return a Invoker[] containing all the invokers culled from the selectInstructions. If
     * there was only one SelectInstruction and its columnName is "*" then all the getter 
     * methods of the object class are returned wrapped in an array
     */
    private Invoker[] getInvokers(SelectInstruction[] selectInstructions, Class objectClass) {

        Invoker[] invokers = null;

        if (selectInstructions.length == 1
                && SelectInstruction.SELECT_ALL.equals(selectInstructions[0].getColumnName())) {
            invokers = getAllGetters(objectClass);
        } else {
            invokers = new Invoker[selectInstructions.length];
            for (int i = 0; i < selectInstructions.length; i++) {
                invokers[i] = selectInstructions[i].getInvoker();
            }
        }

        return invokers;
    }

    /**
     * @param objectClass
     * @return an Invoker[] wrapping all the getters of the specified class
     */
    private Invoker[] getAllGetters(Class objectClass) {

        Invoker[] invokers;
        Method[] allMethods = objectClass.getMethods();

        List gettersList = new ArrayList();
        for (int i = 0; i < allMethods.length; i++) {
            final Method method = allMethods[i];
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                gettersList.add(new Invoker() {
                    public Object invoke(Object o) {
                        try {
                            return method.invoke(o, EMPTY_OBJECT_ARRAY);
                        } catch (IllegalAccessException e) {
                            return null;
                        } catch (InvocationTargetException e) {
                            return null;
                        }
                    }
                });
            }
        }
        invokers = (Invoker[]) gettersList.toArray(EMPTY_INVOKER_ARRAY);
        return invokers;
    }

    /**
     * 
     * @param objectClass
     * @return a String[] containing all the column names based on the getter methods
     */
    private String[] getAllColumns(Class objectClass) {
        String[] columns;
        Method[] allMethods = objectClass.getMethods();

        List gettersList = new ArrayList();
        for (int i = 0; i < allMethods.length; i++) {
            Method method = allMethods[i];
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                gettersList.add(method.getName().substring(3));
            }
        }
        columns = (String[]) gettersList.toArray(EMPTY_STRING_ARRAY);
        return columns;
    }

}