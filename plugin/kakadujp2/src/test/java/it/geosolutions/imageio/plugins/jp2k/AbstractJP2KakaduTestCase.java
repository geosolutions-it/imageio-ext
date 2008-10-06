package it.geosolutions.imageio.plugins.jp2k;

import java.awt.image.RenderedImage;

import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.TestCase;

public class AbstractJP2KakaduTestCase extends TestCase {
	public AbstractJP2KakaduTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// general settings
//		JAI.getDefaultInstance().getTileScheduler().setParallelism(2);
//		JAI.getDefaultInstance().getTileScheduler().setPriority(6);
//		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
//		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(1);
//		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
//				64 * 1024 * 1024);
//		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
		// final TCTool tool= new TCTool();
	}
	
	public static void visualize(RenderedImage ri, String title) {
		visualize(ri, 800, 600);
	}

	public static void visualize(RenderedImage ri, int width, int height) {
		final JFrame jf = new JFrame("");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(ri, 800, 600));
		jf.pack();
		jf.setVisible(true);

	}
}
