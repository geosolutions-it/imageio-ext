package it.geosolutions.imageio.plugins.cog;

import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;

/**
 * A subclass of TIFFImageReadParam to hold information about which RangeReader implementation to use.
 *
 * @author joshfix
 * Created on 2019-09-18
 */
public class CogImageReadParam extends TIFFImageReadParam {

    Class<? extends RangeReader> rangeReaderClass;

    public CogImageReadParam() {
        super();
    }

    public CogImageReadParam(Class<? extends RangeReader> rangeReaderClass) {
        super();
        this.rangeReaderClass = rangeReaderClass;
    }

    public Class<? extends RangeReader> getRangeReaderClass() {
        return rangeReaderClass;
    }

    public void setRangeReaderClass(Class<? extends RangeReader> rangeReaderClass) {
        this.rangeReaderClass = rangeReaderClass;
    }
}
