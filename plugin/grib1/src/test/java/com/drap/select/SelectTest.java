package com.drap.select;

import java.util.*;

import junit.framework.*;

/**
 * Just one test case implemented currently
 * @author David Rappoport 
 */
public class SelectTest extends TestCase {
    
    private SelectStatement selectStatement;
    private Executor executor;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SelectTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        //data
        Object[] data = new Object[3];
        data[0] = new RollerTestClass(200, "Vespa", 2);
        data[1] = new RollerTestClass(45, "NRG", 2);
        data[2] = new RollerTestClass(120, "Speedo", 1);
        
        //select instructions
        SelectInstruction[] selectInstructions = new SelectInstruction[3];
        selectInstructions[0] = new SelectInstruction("NAME", new Invoker(){
            public Object invoke(Object o){
                return ((RollerTestClass)o).getName();
            }
        });
        selectInstructions[1] = new SelectInstruction("SPEED", new Invoker(){
            public Object invoke(Object o){
                return new Integer(((RollerTestClass)o).getSpeed());
            }
        });
        selectInstructions[2] = new SelectInstruction("SEATS", new Invoker(){
            public Object invoke(Object o){
                return new Integer(((RollerTestClass)o).getNrOfSeats());
            }
        });
        
        //conditions
        Condition[] conditions = new Condition[1];
        conditions[0] = new Condition(){
          public boolean passes(Object o){
              return ((RollerTestClass)o).getNrOfSeats() > 0;
          }
        };
        
        //order by
        OrderInstruction[] orderInstructions = new OrderInstruction[3];
        orderInstructions[0] = new OrderInstruction(new Invoker(){
            public Object invoke(Object o){
                return new Integer(((RollerTestClass)o).getSpeed());
            }
        }, null);
        orderInstructions[1] = new OrderInstruction(new Invoker(){
            public Object invoke(Object o){
                return new Integer(((RollerTestClass)o).getNrOfSeats());
            }
        }, new Comparator(){
           public int compare(Object o1, Object o2){
               if(o1 == null && o2 == null)return 0;
               if(o1 == null)return -1;
               if(o2 == null)return 1;
               return ((Integer)o2).compareTo(o1);               
           }
        });
        orderInstructions[2] = new OrderInstruction(new Invoker(){
            public Object invoke(Object o){
                return ((RollerTestClass)o).getName();
            }
        }, null);
        
        //Create slect statement
        selectStatement = new SelectStatement(selectInstructions, data, conditions, orderInstructions);
        executor = new ExecutorImpl();
        
    }
    
    public void testSelect(){
        
        ResultTable resultTable = executor.execute(selectStatement);
        assertNotNull(resultTable);
        System.out.print("Columns:");
        for(int i = 0; i < resultTable.getColumnNames().length; i ++){
            System.out.print("\t");
            System.out.print(resultTable.getColumnNames()[i]);
        }
        
        Object[][] resultData = resultTable.getResultData();
        for(int i = 0; i < resultData.length; i ++){
            System.out.println();
            System.out.print("Row " + i + "  :");
            for(int e = 0; e < resultData[i].length; e ++){
                System.out.print("\t");
                System.out.print(resultData[i][e]);
            }
            
        }
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public class RollerTestClass{
        private int speed;
        private String name;
        private int nrOfSeats;
        
        /**
         * @param speed
         * @param name
         * @param nrOfSeats
         */
        public RollerTestClass(int speed, String name, int nrOfSeats) {
            super();
            this.speed = speed;
            this.name = name;
            this.nrOfSeats = nrOfSeats;
        }
        /**
         * @return Returns the name.
         */
        public String getName() {
            return this.name;
        }
        /**
         * @return Returns the nrOfSeats.
         */
        public int getNrOfSeats() {
            return this.nrOfSeats;
        }
        /**
         * @return Returns the speed.
         */
        public int getSpeed() {
            return this.speed;
        }
    }

}
