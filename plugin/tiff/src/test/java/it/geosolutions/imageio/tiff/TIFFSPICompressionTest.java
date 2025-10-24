/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    https://www.geosolutionsgroup.com/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageio.tiff;

import it.geosolutions.imageio.compression.*;
import it.geosolutions.imageio.compression.libdeflate.LibDeflateCompressor;
import it.geosolutions.imageio.compression.libdeflate.LibDeflateCompressorSpi;
import it.geosolutions.imageio.compression.libdeflate.LibDeflateDecompressor;
import it.geosolutions.imageio.compression.libdeflate.LibDeflateDecompressorSpi;
import it.geosolutions.imageio.compression.zipdeflate.ZipDeflateCompressor;
import it.geosolutions.imageio.compression.zipdeflate.ZipDeflateDecompressor;
import it.geosolutions.imageio.plugins.tiff.TIFFImageWriteParam;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriter;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import it.geosolutions.resources.TestData;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import me.steinborn.libdeflate.Libdeflate;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing SPI registration and priority for deflate compression on TIFF I/O.
 *
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFSPICompressionTest extends Assert {

    @BeforeClass
    public static void arrangePriorities() {
        // We are setting higher priority for libdeflate compressor
        CompressionRegistry registryInstance = CompressionRegistry.getDefaultInstance();

        Iterator<CompressorSpi> cSpis = registryInstance.getSPIs(CompressorSpi.class, true);
        while (cSpis.hasNext()) {
            CompressorSpi spi = cSpis.next();
            if (spi instanceof LibDeflateCompressorSpi) {
                LibDeflateCompressorSpi compSpi = ((LibDeflateCompressorSpi) spi);
                compSpi.setPriority(90);
                compSpi.onRegistration(registryInstance, CompressorSpi.class);

                break;
            }
        }

        // We are setting lower priority for libdeflate decompressor
        Iterator<DecompressorSpi> dSpis = registryInstance.getSPIs(DecompressorSpi.class, true);
        while (dSpis.hasNext()) {
            DecompressorSpi spi = dSpis.next();
            if (spi instanceof LibDeflateDecompressorSpi) {
                ((LibDeflateDecompressorSpi) spi).setPriority(20);
                LibDeflateDecompressorSpi decompSpi = (LibDeflateDecompressorSpi) spi;
                decompSpi.setPriority(20);
                decompSpi.onRegistration(registryInstance, DecompressorSpi.class);
                break;
            }
        }
    }

    @Test
    public void testPriorities() throws IOException {

        Compressor compressor = CompressionFinder.getCompressor(7, CompressionType.DEFLATE);
        Class<? extends Compressor> compressorClass =
                Libdeflate.isAvailable() ? LibDeflateCompressor.class : ZipDeflateCompressor.class;
        assertEquals(compressor.getClass(), compressorClass);

        Decompressor decompressor = CompressionFinder.getDecompressor(CompressionType.DEFLATE);
        Class<? extends Decompressor> decompressorClass =
                Libdeflate.isAvailable() ? LibDeflateDecompressor.class : ZipDeflateDecompressor.class;
        assertEquals(decompressor.getClass(), decompressorClass);
    }

    @Test
    public void testReadWrite() throws IOException {
        TIFFImageReaderSpi readerSpi = new TIFFImageReaderSpi();
        ImageReader reader = readerSpi.createReaderInstance();
        final File file = TestData.file(this, "test.tif");
        reader.setInput(new FileImageInputStream(file));

        BufferedImage image = reader.read(0, null);

        TIFFImageWriterSpi writerSpi = new TIFFImageWriterSpi();
        File outputFile = File.createTempFile("tempTIFF", ".tmp");
        final TIFFImageWriter writer = (TIFFImageWriter) writerSpi.createWriterInstance();
        final ImageWriteParam writeParam = new TIFFImageWriteParam(Locale.getDefault());
        writeParam.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setTiling(30, 26, 0, 0);
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType("Deflate");
        writeParam.setCompressionQuality(0.3f);

        writer.setOutput(new FileImageOutputStream(outputFile));
        writer.write(null, new IIOImage(image, null, null), writeParam);
        writer.dispose();

        reader.dispose();
        TIFFImageReader reader2 = (TIFFImageReader) readerSpi.createReaderInstance();
        reader2.setInput(new FileImageInputStream(outputFile));
        BufferedImage bi1 = reader2.read(0, null);
        Raster original = image.getData();
        Raster copy = bi1.getData();

        for (int i = 0; i < bi1.getWidth(); i++) {
            for (int j = 0; j < bi1.getHeight(); j++) {
                for (int b = 0; b < original.getNumBands(); b++) {
                    int pixel = original.getSample(i, j, b);
                    int pixelD = copy.getSample(i, j, b);
                    if (pixel != pixelD) {
                        throw new IllegalArgumentException("Images are different");
                    }
                }
            }
        }
    }
}
