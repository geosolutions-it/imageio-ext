package it.geosolutions.imageio.stream;

import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

public class TestImageOutputStream extends TestCase {
	private final static Logger LOGGER = Logger
			.getLogger(TestImageOutputStream.class.toString());

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestImageOutputStream.class);
	}



	public TestImageOutputStream(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		ImageIO.setUseCache(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test(){}
}
