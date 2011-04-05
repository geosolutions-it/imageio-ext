/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import org.junit.Assert;

/**
 * @author Simone Giannecchini, GeoSolutions.
 */
public class ImageIOExtTest {



	@org.junit.Test
    public void testImageReadMT() {
        ImageReadDescriptorMT.register(JAI.getDefaultInstance());
        final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageReadMT");
        Assert.assertNotNull(pbj);
    }

	@org.junit.Test
    public void testImageReadMTOperation() throws IOException {
        // final String opName = "ImageReadMT";
        // JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
        // JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
        // ImageReaderSpi spi = new GeoTiffImageReaderSpi();
        // ImageReader reader = spi.createReaderInstance();
        // final File file = new File(new String("C:/bogota.tif"));
        //
        // // //
        // //
        // // Setting Image Read Parameters
        // //
        // // //
        // final ImageReadParam param = new DefaultCloneableImageReadParam();
        // BufferedImage bi = new BufferedImage(512, 512,
        // BufferedImage.TYPE_BYTE_GRAY);
        // param.setDestination(bi);
        //
        // // //
        // //
        // // Preparing the ImageRead operation
        // //
        // // //
        // ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(opName);
        // pbjImageRead
        // .setParameter("Input", ImageIO.createImageInputStream(file));
        // pbjImageRead.setParameter("readParam", param);
        // pbjImageRead.setParameter("reader", reader);
        //
        // // //
        // //
        // // Setting a Layout
        // //
        // // //
        // final ImageLayout l = new ImageLayout();
        // l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
        // .setTileWidth(256);
        //
        // // //
        // //
        // // ImageReadMT operation
        // //
        // // //
        // RenderedOp image = JAI.create(opName, pbjImageRead, new
        // RenderingHints(
        // JAI.KEY_IMAGE_LAYOUT, l));
        // image.getTiles();
        // final JFrame jf = new JFrame();
        // jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // jf.getContentPane().add(new ScrollingImagePanel(bi, 800, 800));
        // jf.pack();
        // jf.setVisible(true);
    }
}
