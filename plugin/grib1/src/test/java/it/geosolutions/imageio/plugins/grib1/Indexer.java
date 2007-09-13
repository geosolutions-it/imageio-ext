/**
 * 
 */
package it.geosolutions.imageio.plugins.grib1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import com.drap.select.Condition;
import com.drap.select.Executor;
import com.drap.select.ExecutorImpl;
import com.drap.select.Invoker;
import com.drap.select.OrderInstruction;
import com.drap.select.ResultTable;
import com.drap.select.SelectInstruction;
import com.drap.select.SelectStatement;

/**
 * http://www.javaworld.com/javaworld/jw-11-2004/jw-1122-select.html
 * 
 * @author Fabiania
 * 
 */
public class Indexer {

	/**
	 * The main TreeMap.
	 */
	final TreeMap paramsList = new TreeMap();

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Indexer test = new Indexer();

		final Collection descriptors = new ArrayList();
		descriptors.add(new Descriptor(0, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(0.0),new Integer(0)));
		descriptors.add(new Descriptor(1, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(1.0),new Integer(0)));
		descriptors.add(new Descriptor(2, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(2.0),new Integer(0)));
		descriptors.add(new Descriptor(3, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(3.0),new Integer(0)));
		descriptors.add(new Descriptor(4, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(4.0),new Integer(0)));
		descriptors.add(new Descriptor(5, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(5.0),new Integer(0)));
		descriptors.add(new Descriptor(6, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(6.0),new Integer(0)));
		descriptors.add(new Descriptor(7, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(7.0),new Integer(0)));
		descriptors.add(new Descriptor(8, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(8.0),new Integer(0)));
		descriptors.add(new Descriptor(9, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(9.0),new Integer(0)));
		descriptors.add(new Descriptor(10, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(10.0),new Integer(0)));
		descriptors.add(new Descriptor(11, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(0.0),new Integer(1)));
		descriptors.add(new Descriptor(12, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(1.0),new Integer(1)));
		descriptors.add(new Descriptor(13, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(2.0),new Integer(1)));
		descriptors.add(new Descriptor(14, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(3.0),new Integer(1)));
		descriptors.add(new Descriptor(15, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(4.0),new Integer(1)));
		descriptors.add(new Descriptor(16, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(5.0),new Integer(1)));
		descriptors.add(new Descriptor(17, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(6.0),new Integer(1)));
		descriptors.add(new Descriptor(18, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(7.0),new Integer(1)));
		descriptors.add(new Descriptor(19, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(8.0),new Integer(1)));
		descriptors.add(new Descriptor(20, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(9.0),new Integer(1)));
		descriptors.add(new Descriptor(21, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(10.0),new Integer(1)));
		descriptors.add(new Descriptor(22, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(0.0),new Integer(2)));
		descriptors.add(new Descriptor(23, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(1.0),new Integer(2)));
		descriptors.add(new Descriptor(24, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(2.0),new Integer(2)));
		descriptors.add(new Descriptor(25, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(3.0),new Integer(2)));
		descriptors.add(new Descriptor(26, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(4.0),new Integer(2)));
		descriptors.add(new Descriptor(27, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(5.0),new Integer(2)));
		descriptors.add(new Descriptor(28, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(6.0),new Integer(2)));
		descriptors.add(new Descriptor(29, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(7.0),new Integer(2)));
		descriptors.add(new Descriptor(30, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(8.0),new Integer(2)));
		descriptors.add(new Descriptor(31, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(9.0),new Integer(2)));
		descriptors.add(new Descriptor(32, "param_01", sdf.parse("21/08/2007 00:00:00"), new Double(10.0),new Integer(2)));

		descriptors.add(new Descriptor(33, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(0.0),new Integer(0)));
		descriptors.add(new Descriptor(34, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(1.0),new Integer(0)));
		descriptors.add(new Descriptor(35, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(2.0),new Integer(0)));
		descriptors.add(new Descriptor(36, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(3.0),new Integer(0)));
		descriptors.add(new Descriptor(37, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(4.0),new Integer(0)));
		descriptors.add(new Descriptor(38, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(5.0),new Integer(0)));
		descriptors.add(new Descriptor(39, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(6.0),new Integer(0)));
		descriptors.add(new Descriptor(40, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(7.0),new Integer(0)));
		descriptors.add(new Descriptor(41, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(8.0),new Integer(0)));
		descriptors.add(new Descriptor(42, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(9.0),new Integer(0)));
		descriptors.add(new Descriptor(43, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(10.0),new Integer(0)));
		descriptors.add(new Descriptor(44, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(0.0),new Integer(1)));
		descriptors.add(new Descriptor(45, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(1.0),new Integer(1)));
		descriptors.add(new Descriptor(46, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(2.0),new Integer(1)));
		descriptors.add(new Descriptor(47, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(3.0),new Integer(1)));
		descriptors.add(new Descriptor(48, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(4.0),new Integer(1)));
		descriptors.add(new Descriptor(49, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(5.0),new Integer(1)));
		descriptors.add(new Descriptor(50, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(6.0),new Integer(1)));
		descriptors.add(new Descriptor(51, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(7.0),new Integer(1)));
		descriptors.add(new Descriptor(52, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(8.0),new Integer(1)));
		descriptors.add(new Descriptor(53, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(9.0),new Integer(1)));
		descriptors.add(new Descriptor(54, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(10.0),new Integer(1)));
		descriptors.add(new Descriptor(55, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(0.0),new Integer(2)));
		descriptors.add(new Descriptor(55, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(1.0),new Integer(2)));
		descriptors.add(new Descriptor(56, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(2.0),new Integer(2)));
		descriptors.add(new Descriptor(57, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(3.0),new Integer(2)));
		descriptors.add(new Descriptor(58, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(4.0),new Integer(2)));
		descriptors.add(new Descriptor(59, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(5.0),new Integer(2)));
		descriptors.add(new Descriptor(60, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(6.0),new Integer(2)));
		descriptors.add(new Descriptor(61, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(7.0),new Integer(2)));
		descriptors.add(new Descriptor(62, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(8.0),new Integer(2)));
		descriptors.add(new Descriptor(63, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(9.0),new Integer(2)));
		descriptors.add(new Descriptor(64, "param_01", sdf.parse("21/08/2007 03:00:00"), new Double(10.0),new Integer(2)));

		test.init(descriptors);

		/*
		 * for (Iterator<String> i=test.paramsList.keySet().iterator();
		 * i.hasNext(); ) { final String key = i.next(); System.out.println(key + " " +
		 * test.paramsList.get(key)); }
		 */

		// Example: find globalIndex at {param_01, 21/08/2007 03:00:00, 7.0m, res: 0}
		long ms0 = System.currentTimeMillis();
		System.out.println(((TreeMap) ((TreeMap) ((TreeMap) test.paramsList
				.get("param_01")).get(sdf.parseObject("21/08/2007 03:00:00")))
				.get(new Double(7.0))).get(new Integer(0)));
		long ms1 = System.currentTimeMillis();
		System.out.println("Time (ms): " + (ms1 - ms0) + "\n");

		// Example: find all globalIndexes at {param_01, 21/08/2007 00:00:00, 4.0m}
		ms0 = System.currentTimeMillis();
		System.out.println(((TreeMap) ((TreeMap) test.paramsList
				.get("param_01")).get(sdf.parseObject("21/08/2007 00:00:00")))
				.get(new Double(4.0)));
		ms1 = System.currentTimeMillis();
		System.out.println("Time (ms): " + (ms1 - ms0) + "\n");

		// Example: find all globalIndexes at {param_01, [21/08/2007 00:00:00; 21/08/2007 03:00:00], [4.0m; 6.0m], minResLevel}
		ms0 = System.currentTimeMillis();
		Collection globalIndexes = new ArrayList();
		TreeMap timeLevels = (TreeMap) test.paramsList.get("param_01");
		TreeMap t0 = (TreeMap) timeLevels.get(sdf.parse("21/08/2007 00:00:00"));
		TreeMap t1 = (TreeMap) timeLevels.get(sdf.parse("21/08/2007 03:00:00"));
		for (Iterator z0 = t0.keySet().iterator(); z0.hasNext();) {
			Double z = (Double) z0.next();
			if (4.0 <= z.doubleValue() && z.doubleValue() <= 6.0) {
				TreeMap res = (TreeMap) t0.get(z);
				globalIndexes.add(res.get(res.lastKey()));
			}
		}
		for (Iterator z1 = t1.keySet().iterator(); z1.hasNext();) {
			Double z = (Double) z1.next();
			if (4.0 <= z.doubleValue() && z.doubleValue() <= 6.0) {
				TreeMap res = (TreeMap) t1.get(z);
				globalIndexes.add(res.get(res.lastKey()));
			}
		}
		System.out.println(globalIndexes);
		ms1 = System.currentTimeMillis();
		System.out.println("Time (ms): " + (ms1 - ms0) + "\n");

		/**
		 * SELECT Test
		 */
		ms0 = System.currentTimeMillis();
		// select instructions
		SelectInstruction[] selectInstructions = new SelectInstruction[5];
		selectInstructions[0] = new SelectInstruction("Parameter",
				new Invoker() {
					public Object invoke(Object o) {
						return ((Descriptor) o).getPo();
					}
				});
		selectInstructions[1] = new SelectInstruction("Time", new Invoker() {
			public Object invoke(Object o) {
				return ((Descriptor) o).getTo();
			}
		});
		selectInstructions[2] = new SelectInstruction("Elevation",
				new Invoker() {
					public Object invoke(Object o) {
						return ((Descriptor) o).getZo();
					}
				});
		selectInstructions[3] = new SelectInstruction("Overview",
				new Invoker() {
					public Object invoke(Object o) {
						return ((Descriptor) o).getRo();
					}
				});
		selectInstructions[4] = new SelectInstruction("GlobalIndex",
				new Invoker() {
					public Object invoke(Object o) {
						return new Long(((Descriptor) o).getGlobalIndex());
					}
				});
		/*SelectInstruction[] selectInstructions = new SelectInstruction[] {
			new SelectInstruction("*", null)
		};*/
		
		// conditions
		Condition[] conditions = new Condition[1];
		/*conditions[0] = new Condition() {
			public boolean passes(Object o) {
				return ((Descriptor) o).getPo().equals("param_01");
			}
		};*/
		conditions[0] = new Condition() {
			public boolean passes(Object o) {
				try {
					return ((Descriptor) o).getPo().equals("param_01") && ((Descriptor) o).getTo().equals(sdf.parse("21/08/2007 03:00:00"));
				} catch (ParseException e) {
					return false;
				}
			}
		};

		// order by
		OrderInstruction[] orderInstructions = new OrderInstruction[3];
		orderInstructions[0] = new OrderInstruction(new Invoker() {
			public Object invoke(Object o) {
				return ((Descriptor) o).getPo();
			}
		}, null);
		orderInstructions[1] = new OrderInstruction(new Invoker() {
			public Object invoke(Object o) {
				return ((Descriptor) o).getTo();
			}
		}, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;
				return ((Date) o2).compareTo(o1);
			}
		});
		orderInstructions[2] = new OrderInstruction(new Invoker() {
			public Object invoke(Object o) {
				return ((Descriptor) o).getZo();
			}
		}, null);

		// Create slect statement
		SelectStatement selectStatement = new SelectStatement(
				selectInstructions, descriptors, conditions, orderInstructions);
		Executor executor = new ExecutorImpl();

		ResultTable resultTable = executor.execute(selectStatement);
		ms1 = System.currentTimeMillis();
		System.out.print("Columns:");
		for (int i = 0; i < resultTable.getColumnNames().length; i++) {
			System.out.print("\t");
			System.out.print(resultTable.getColumnNames()[i]);
		}

		Object[][] resultData = resultTable.getResultData();
		for (int i = 0; i < resultData.length; i++) {
			System.out.println();
			System.out.print("Row " + i + "  :");
			for (int e = 0; e < resultData[i].length; e++) {
				System.out.print("\t");
				System.out.print(resultData[i][e]);
			}

		}
		System.out.println("\nTime (ms): " + (ms1 - ms0) + "\n");
	}

	/**
	 * Initialization Method: builds the nested tree structure given a
	 * collection of Descriptors.
	 */
	private void init(final Collection descriptors) {
		// TODO create comparators

		for (Iterator i = descriptors.iterator(); i.hasNext();) {
			Descriptor desc = (Descriptor) i.next();
			if (!paramsList.containsKey(desc.getPo())) {
				// Initializing the params tree-map
				final TreeMap timesList = new TreeMap();
				paramsList.put(desc.getPo(), timesList);
			}

			final TreeMap timesList = (TreeMap) paramsList.get(desc.getPo());
			if (!timesList.containsKey(desc.getTo())) {
				// Initializing the times tree-map
				final TreeMap zList = new TreeMap();
				timesList.put(desc.getTo(), zList);
			}

			final TreeMap zList = (TreeMap) timesList.get(desc.getTo());
			if (!zList.containsKey(desc.getZo())) {
				// Initializing the z-Levels tree-map
				final TreeMap resList = new TreeMap();
				zList.put(desc.getZo(), resList);
			}

			final TreeMap resList = (TreeMap) zList.get(desc.getZo());
			if (!resList.containsKey(desc.getRo())) {
				resList.put(desc.getRo(), new Long(desc.getGlobalIndex()));
			} // else Error: too resolutions matching the same globalIndex
		}
	}
}

/**
 * Helper Class which represents the set of variables to be considered.
 */
class Descriptor {
	/*
	 * globalIndex plus 4 variables -> Param, Time, zLevel, resolution
	 * 
	 * globalIndex => {Po,To,Zo,Ro}
	 */
	// The Global Index
	private long globalIndex;

	// Parameter Name
	private String Po;

	// Time Position
	private Date To;

	// Z Level
	private Double Zo;

	// Overview Index (Resolution)
	private Integer Ro;

	public Descriptor(long globalIndex, String Px, Date Tx, Double Zx,
			Integer Rx) {
		this.globalIndex = globalIndex;
		this.Po = Px;
		this.To = Tx;
		this.Zo = Zx;
		this.Ro = Rx;
	}

	/**
	 * @return the globalIndex
	 */
	public long getGlobalIndex() {
		return globalIndex;
	}

	/**
	 * @return the po
	 */
	public String getPo() {
		return Po;
	}

	/**
	 * @return the ro
	 */
	public Integer getRo() {
		return Ro;
	}

	/**
	 * @return the to
	 */
	public Date getTo() {
		return To;
	}

	/**
	 * @return the zo
	 */
	public Double getZo() {
		return Zo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer output = new StringBuffer();
		output.append(globalIndex).append(" => ").append("{").append(this.Po)
				.append(";").append(this.To).append(";").append(this.Zo)
				.append(";").append(this.Ro).append("}");

		return output.toString();
	}
}