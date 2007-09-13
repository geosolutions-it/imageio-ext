package it.geosolutions.imageio.plugins;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

public abstract class AbstractImageReader extends ImageReader {

	protected AbstractImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}
}
