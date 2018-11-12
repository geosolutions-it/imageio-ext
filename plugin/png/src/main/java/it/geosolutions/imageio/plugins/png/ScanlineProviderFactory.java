/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.png;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;

/**
 * Wraps a {@link RenderedImage} into a scaline provider optimized to turn its pixels into PNG
 * scanlines at the best performance
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class ScanlineProviderFactory {

    public static ScanlineProvider getProvider(RenderedImage image) {
        ColorModel cm = image.getColorModel();
        SampleModel sm = image.getSampleModel();

        Raster raster;
        if (image instanceof BufferedImage) {
            raster = ((BufferedImage) image).getRaster();
            // in case the raster has a parent, this is likely a subimage, we have to force
            // a copy of the raster to get a data buffer we can scroll over without issues
            if (raster.getParent() != null) {
                raster = image.getData(new Rectangle(0, 0, raster.getWidth(), raster.getHeight()));
            }
        } else {
            // TODO: we could build a tile oriented reader that fetches tiles in parallel here
            raster = image.getData();
        }

        // grab the right scanline extractor based on image features
        if (cm instanceof ComponentColorModel && sm.getDataType() == DataBuffer.TYPE_BYTE) {
            if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                return new RasterByteABGRProvider(raster, cm.hasAlpha());
            } else if (sm.getNumBands() == 2 && cm.hasAlpha()) {
                return new RasterByteGrayAlphaProvider(raster);
            } else if (sm.getNumBands() == 1) {
                if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    if (sm instanceof MultiPixelPackedSampleModel) {
                        if (cm.getPixelSize() == 8) {
                            return new RasterByteSingleBandProvider(raster, 8, raster.getWidth());
                        } else if (cm.getPixelSize() == 4) {
                            int scanlineLength = (raster.getWidth() + 1) / 2;
                            return new RasterByteSingleBandProvider(raster, 4, scanlineLength);
                        } else if (cm.getPixelSize() == 2) {
                            int scanlineLength = (raster.getWidth() + 2) / 4;
                            return new RasterByteSingleBandProvider(raster, 2, scanlineLength);
                        } else if (cm.getPixelSize() == 1) {
                            int scanlineLength = (raster.getWidth() + 4) / 8;
                            return new RasterByteSingleBandProvider(raster, 1, scanlineLength);
                        }
                    } else {
                        if (cm.getPixelSize() == 8) {
                            if (sm instanceof PixelInterleavedSampleModel &&
                                    (((PixelInterleavedSampleModel)sm).getPixelStride() != 1)) {
                                    return new RasterByteSingleBandSkippingBytesProvider(raster);
                                }
                            return new RasterByteSingleBandProvider(raster, 8, raster.getWidth());
                        } else if (cm.getPixelSize() == 4) {
                            int scanlineLength = (raster.getWidth() + 1) / 2;
                            return new RasterByteRepackSingleBandProvider(raster, 4, scanlineLength);
                        } else if (cm.getPixelSize() == 2) {
                            int scanlineLength = (raster.getWidth() + 2) / 4;
                            return new RasterByteRepackSingleBandProvider(raster, 2, scanlineLength);
                        } else if (cm.getPixelSize() == 1) {
                            int scanlineLength = (raster.getWidth() + 4) / 8;
                            return new RasterByteRepackSingleBandProvider(raster, 1, scanlineLength);
                        }
                    }
                }
            }
        } else if (cm instanceof ComponentColorModel && sm.getDataType() == DataBuffer.TYPE_USHORT) {
            if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                return new RasterShortABGRProvider(raster, cm.hasAlpha());
            } else if (sm.getNumBands() == 2 && cm.hasAlpha()) {
                return new RasterShortGrayAlphaProvider(raster);
            } else if (sm.getNumBands() == 1) {
                return new RasterShortSingleBandProvider(raster);
            }
        } else if (cm instanceof DirectColorModel && sm.getDataType() == DataBuffer.TYPE_INT) {
            if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                return new RasterIntABGRProvider(raster, cm.hasAlpha());
            }
        } else if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;
            int pixelSize = icm.getPixelSize();
            // the RGBA quantizer can generate pixel sizes which are not powers of two, 
            // re-align to powers of two
            if((pixelSize & (pixelSize - 1)) != 0) {
                int nextPower = (int) (Math.floor(Math.log(pixelSize) / Math.log(2)) + 1);
                pixelSize = (int) Math.pow(2, nextPower);
            }
            if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (sm instanceof MultiPixelPackedSampleModel) {
                    if (pixelSize == 8) {
                        return new RasterByteSingleBandProvider(raster, 8, raster.getWidth(), icm);
                    } else if (pixelSize == 4) {
                        int scanlineLength = (raster.getWidth() + 1) / 2;
                        return new RasterByteSingleBandProvider(raster, 4, scanlineLength, icm);
                    } else if (pixelSize == 2) {
                        int scanlineLength = (raster.getWidth() + 2) / 4;
                        return new RasterByteSingleBandProvider(raster, 2, scanlineLength, icm);
                    } else if (pixelSize == 1) {
                        int scanlineLength = (raster.getWidth() + 4) / 8;
                        return new RasterByteSingleBandProvider(raster, 1, scanlineLength, icm);
                    }
                } else {
                    if (pixelSize == 8) {
                        return new RasterByteSingleBandProvider(raster, 8, raster.getWidth(), icm);
                    } else if (pixelSize == 4) {
                        int scanlineLength = (raster.getWidth() + 1) / 2;
                        return new RasterByteRepackSingleBandProvider(raster, 4, scanlineLength,
                                icm);
                    } else if (pixelSize == 2) {
                        int scanlineLength = (raster.getWidth() + 2) / 4;
                        return new RasterByteRepackSingleBandProvider(raster, 2, scanlineLength,
                                icm);
                    } else if (pixelSize == 1) {
                        int scanlineLength = (raster.getWidth() + 4) / 8;
                        return new RasterByteRepackSingleBandProvider(raster, 1, scanlineLength,
                                icm);
                    }
                }
            } else if (sm.getDataType() == DataBuffer.TYPE_USHORT) {
                if (sm instanceof MultiPixelPackedSampleModel) {
                    if (pixelSize == 16) {
                        int scanlineLength = raster.getWidth() * 2;
                        return new RasterShortSingleBandProvider(raster, 16, scanlineLength, icm);
                    } else if (pixelSize == 8) {
                        int scanlineLength = raster.getWidth() + ((raster.getWidth() % 2 == 0) ? 0 : 1); 
                        return new RasterShortSingleBandProvider(raster, 8, scanlineLength, icm);
                    } else if (pixelSize == 4) {
                        int scanlineLength = (raster.getWidth() + 1) / 2;
                        return new RasterShortSingleBandProvider(raster, 4, scanlineLength, icm);
                    } else if (pixelSize == 2) {
                        int scanlineLength = (raster.getWidth() + 2) / 4;
                        return new RasterShortSingleBandProvider(raster, 2, scanlineLength, icm);
                    } else if (pixelSize == 1) {
                        int scanlineLength = (raster.getWidth() + 4) / 8;
                        return new RasterShortSingleBandProvider(raster, 1, scanlineLength, icm);
                    }
                }
            }
        }

        return null;
    }
}
