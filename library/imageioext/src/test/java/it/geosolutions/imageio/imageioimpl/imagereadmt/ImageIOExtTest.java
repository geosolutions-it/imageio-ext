/**
 * 
 */
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author Simone Giannecchini
 *
 */
public class ImageIOExtTest extends TestCase {

	

	/**
	 * @param name
	 */
	public ImageIOExtTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static void main(String[] args){
		TestRunner.run(ImageIOExtTest.class);
	}

	public void testImageReadMT(){
		final JAI jai = JAI.getDefaultInstance();
		ImageReadDescriptorMT.register(jai);
		
		final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageReadMT");
		
		assertNotNull(pbj);
	}
}
