package it.geosolutions.imageio.plugins.ecw;

import javax.media.jai.JAI;
import junit.framework.TestCase;

public class AbstractECWTestCase extends TestCase {

	public AbstractECWTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// general settings
		JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(4);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				180 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
		
		
	}
}
