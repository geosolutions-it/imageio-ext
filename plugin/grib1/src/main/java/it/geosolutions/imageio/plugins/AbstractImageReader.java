package it.geosolutions.imageio.plugins;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

/**
 * Abstract class extending Java's ImageReader.
 * Furhter methods should be added afterwards
 * 
 * @author Daniele Romagnoli
 */
public abstract class AbstractImageReader extends ImageReader {

	protected AbstractImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}
}
