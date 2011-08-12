/* JP2K Kakadu Image Writer V. 1.0 
 * 
 * (c) 2008 Quality Nighthawk Teleradiology Group, Inc.
 * Contact: info@qualitynighthawk.com
 *
 * Produced by GeoSolutions, Eng. Daniele Romagnoli and Eng. Simone Giannecchini
 * GeoSolutions S.A.S. ---  Via Carignoni 51, 55041 Camaiore (LU) Italy
 * Contact: info@geo-solutions.it
 *
 * Released under the Gnu Lesser General Public License version 3. 
 * All rights otherwise reserved. 
 *
 * JP2K Kakadu Image Writer is distributed on an "AS IS" basis, 
 * WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  
 *
 * See the GNU Lesser General Public License version 3 for more details. 
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam.Compression;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam.ProgressionOrder;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.SubtractDescriptor;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import com.sun.media.jai.operator.ImageReadDescriptor;

public class JP2KKakaduQualityLayersWriteTest extends Assert {

    final private static String inputFileName = "c:\\11AUG04063808-S3DS_R29C5-052565658010_01_P001.ntf.JP2";
    
    public enum CompressionProfile {
        NPJE, EPJE
    }
    
    private final static double BPPPB[] = new double[] { 0.03125, 0.0625, 0.125, 0.25, 0.5, 0.6,
            0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.5, 1.7, 2.0, 2.3, 3.5, 3.9, 0 };
    
    private final static double THRESHOLD = 1E-6;
    
    private final static JP2KKakaduImageReaderSpi JP2K_RSPI = new JP2KKakaduImageReaderSpi();
    
    private final static JP2KKakaduImageWriterSpi JP2K_WSPI = new JP2KKakaduImageWriterSpi();
    
//    private final static TIFFImageReaderSpi TIFF_SPI = new TIFFImageReaderSpi();
    
    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2k");

    static {
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(768*1024*1024);
    };
    
    
    @Test
    @Ignore
    public void testWriteCompressionProfiles() throws IOException {
        final File file = new File(inputFileName);
        FileImageInputStream fis = null;
        new FileImageInputStream(file);
        ImageReader reader = null;
        try {
            fis = new FileImageInputStream(file);
            reader = JP2K_RSPI.createReaderInstance();
            RenderedImage ri = ImageReadDescriptor.create(fis, 0, false, false, false, 
                null, null, null, reader, null);
//            ImageIO.write(ri, "TIFF", new File("c:\\readback.tif"));
            write(inputFileName, ri, Compression.NUMERICALLY_LOSSLESS, CompressionProfile.NPJE);
            write(inputFileName, ri, Compression.NUMERICALLY_LOSSLESS, CompressionProfile.EPJE);
            write(inputFileName, ri, Compression.LOSSY, CompressionProfile.NPJE);
            write(inputFileName, ri, Compression.LOSSY, CompressionProfile.EPJE);
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (Throwable t){
                    
                }
            }
            
            if (reader != null){
                try {
                    reader.dispose();
                } catch (Throwable t){
                    
                }
            }
        }
        
    }
   
    
    private void write(
            final String inputFileName, 
            final RenderedImage originalImage, 
            final Compression type, 
            final CompressionProfile profile) throws IOException {
        
        JP2KKakaduImageWriter kakaduWriter = (JP2KKakaduImageWriter) JP2K_WSPI.createWriterInstance();
        ImageReader reader = JP2K_RSPI.createReaderInstance();
        FileImageOutputStream fos = null;
        FileImageInputStream fis = null;
        
        String suffix;
        switch (type) {
        case NUMERICALLY_LOSSLESS:
            suffix = "NL";
            break;
        case LOSSY:
            suffix = "VL";
            break;
        default:
            suffix = "Lossy";
        }
        
        final String outputFileName = inputFileName + "rewrittenAs_" + suffix + "_" + profile + ".jp2";
        final File outputFile = new File(outputFileName); 
        
        
        try {
            fos = new FileImageOutputStream(outputFile);
            kakaduWriter.setOutput(fos);
            JP2KKakaduImageWriteParam param = setupWriteParameter(kakaduWriter, type, profile);
            kakaduWriter.write(null, new IIOImage(originalImage, null, null), param);
            kakaduWriter.dispose();
            kakaduWriter = null;
            
            fis = new FileImageInputStream(outputFile);
            RenderedImage readBack = ImageReadDescriptor.create(fis, 0, false, false, false, 
                    null, null, null, reader, null);
            RenderedImage difference = SubtractDescriptor.create(readBack, originalImage, null);
            double[][] extrema = (double[][]) ExtremaDescriptor.create(difference, null, 1, 1, false, 1, null).getProperty("Extrema");
            
            if (type == Compression.NUMERICALLY_LOSSLESS){
                for (int i = 0; i < extrema.length; i++){
                    for (int j = 0; j < extrema[i].length; j++){
                        assertEquals(extrema[i][j], 0, THRESHOLD);
                    }
                }
                LOGGER.info("Numerically LossLess successfull: No differences with " +
                		"respect to the original image");
            } else {
                StringBuilder sb = new StringBuilder( "Extrema values on VISUALLY_LOSSLESS (LOSSY)" +
                		" compressions for: " + outputFileName);
                for (int i = 0; i < extrema.length; i++){
                    for (int j = 0; j < extrema[i].length; j++){
                        sb.append(extrema[i][j]).append(" ");
                    }
                }
                LOGGER.info(sb.toString());
            }
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (Throwable t){
                    
                }
            }
            
            if (kakaduWriter != null){
                try {
                    kakaduWriter.dispose();
                } catch (Throwable t){
                    
                }
            }
            
            if (fis != null){
                try {
                    fis.close();
                } catch (Throwable t){
                    
                }
            }
            
            if (reader != null){
                try {
                    reader.dispose();
                } catch (Throwable t){
                    
                }
            }

        }
    }


    private JP2KKakaduImageWriteParam setupWriteParameter(
            final JP2KKakaduImageWriter kakaduWriter, 
            final Compression type, 
            final CompressionProfile profile) {

        JP2KKakaduImageWriteParam param = (JP2KKakaduImageWriteParam) kakaduWriter.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        double bitRates[] = null;
        
        switch (type){
        case NUMERICALLY_LOSSLESS:
            param.setQuality(1);
            param.setQualityLayers(20);
            param.setCompression(Compression.NUMERICALLY_LOSSLESS);
            bitRates = new double[20];
            System.arraycopy(BPPPB, 0, bitRates, 0, 20);
            break;
        case LOSSY:
            param.setQualityLayers(19);
            bitRates = new double[19];
            param.setCompression(Compression.LOSSY);
            System.arraycopy(BPPPB, 0, bitRates, 0, 19);
            break;
        default:
            param.setQualityLayers(19);
            break;
        }
        
        switch (profile){
        case NPJE:
            param.setcOrder(ProgressionOrder.LRCP);
            break;
        case EPJE:
            param.setcOrder(ProgressionOrder.RLCP);
            break;
        }
        
        param.setCLevels(5);
        param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        param.setTiling(1024, 1024, 0,0);
        param.setsProfile(1);
        param.setOrgGen_plt(true);
        param.setOrgGen_tlm(1);
        param.setQualityLayersBitRates(bitRates);
        param.setWriteCodeStreamOnly(true);
        param.setAddCommentMarker(true);
        
        return param;
    }


    @Test
    @Ignore
    public void extractJP2() throws IOException {
        final String in = "SPECIFY A NITF FILE WITH JP2 COMPRESSION";
        final String out = in + ".jp2";
        FileInputStream fis = new FileInputStream(new File(in));
        FileOutputStream fos = new FileOutputStream(new File(out));
        
        // Provide start and end bytes within the stream (use an HEX editor by looking for 
        // SOC marker (FF4F) and EOC marker (FFD9) within the NITF file)
        final int start = Integer.parseInt("e7e" ,16);
        final int end = Integer.parseInt("1681cf0a" ,16);
        final int toBeRead = end - start;
        byte b[] = new byte [65536];
        fis.skip(start);
        int readd = 0;
        int totalRead = start + readd;
        while ((readd = (fis.read(b))) != -1){
            totalRead += readd;
            if (totalRead > toBeRead){
                fos.write(b, 0, totalRead - toBeRead);
                break;
            } else {
                fos.write(b, 0, readd);    
            }
            
        }
        fis.close();
        fos.close();
    }
}
