package it.geosolutions.imageio.plugins.jp2k;

import java.awt.image.RenderedImage;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.TestCase;

public class AbstractJP2KakaduTestCase extends TestCase {
	protected static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jp2k");

	/** A simple flag set to true in case the JP2 kakadu libraries are available */
	protected static boolean isLibraryAvailable;

	private final static String msg = "JP2K Direct Kakadu Tests are skipped due to missing libraries.\n"
			+ "Be sure the required Kakadu libraries libs are in the classpath";

	static {
		try {
			System.loadLibrary("kdu_jni");
			isLibraryAvailable = true;
		} catch (UnsatisfiedLinkError error) {
			isLibraryAvailable = false;
		}
	}

	public AbstractJP2KakaduTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		if (!isLibraryAvailable) {
			LOGGER.warning(msg);
			return;
		}
		// general settings
		JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(6);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(1);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(1);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

	}

	public static void visualize(RenderedImage ri, int width, int height) {
		final JFrame jf = new JFrame("");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(ri, 800, 600));
		jf.pack();
		jf.setVisible(true);
	}
}
