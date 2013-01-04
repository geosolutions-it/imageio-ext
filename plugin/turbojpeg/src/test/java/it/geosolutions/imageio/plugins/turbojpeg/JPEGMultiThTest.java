/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import it.geosolutions.imageio.utilities.ImageOutputStreamAdapter2;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assume.*;

/**
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 */
public class JPEGMultiThTest extends BaseTest {

    private static final Logger LOGGER = Logger.getLogger(JPEGMultiThTest.class.toString());    
    
    private static ImageWriteParam param = new TurboJpegImageWriteParam();

    /**
     * TODO JUNIT tests
     * 
     * @param args
     * @throws IOException
     */
    @Test
    @Ignore
    public void multithreadedTest() throws IOException {

        assumeTrue(!SKIP_TESTS);

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.75f);

        ThreadPoolExecutor ex = null;
        try {
            final int TH = 20;
            LinkedBlockingQueue<Runnable> queueX = new LinkedBlockingQueue<Runnable>();
            ex = new ThreadPoolExecutor(TH, TH, 10000L, TimeUnit.SECONDS, queueX);
            ex.prestartAllCoreThreads();

            final int LOOP = 1000;
            List<Callable<String>> queue = new ArrayList<Callable<String>>(LOOP);
            for (int i = 0; i < LOOP; i++) {
                Callable<String> a = new WritingTest(SAMPLE_IMAGE, OUTPUT_FOLDER + "___" + i
                        + "___" + Math.random() + ".jpeg");
                queue.add(a);
            }
            ex.invokeAll(queue);
            ex.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
            // } catch (ExecutionException e) {
            // e.printStackTrace();
        } finally {
            if (ex != null) {
                ex.shutdown();
            }
        }

        return;
    }

    private class WritingTest implements Callable<String> {

        RenderedImage bi;

        String file;

        public WritingTest(RenderedImage bi, final String outputFile) {
            this.bi = bi;
            file = outputFile;

        }

        public String call() {
            ImageOutputStreamAdapter2 out1 = null;
            TurboJpegImageWriter writer1 = null;
            try {
                FileOutputStream fos = new FileOutputStream(new File(file));
                System.out.println("writing on " + file);

                out1 = new ImageOutputStreamAdapter2(fos);
                writer1 = (TurboJpegImageWriter) turboSPI.createWriterInstance();
                writer1.setOutput(out1);
                writer1.write(null, new IIOImage(bi, null, null), param);
                return file;
            } catch (Exception e) {
                LOGGER.severe(e.getLocalizedMessage());

                return "ERROR";
            } finally {
                if (writer1 != null){
                    try {
                        writer1.dispose();   
                    } catch (Throwable t){
                        
                    }
                }
                if (out1 != null){
                    try {
                        out1.close();   
                    } catch (Throwable t){
                        
                    }
                }
            }
        }
    }

}
