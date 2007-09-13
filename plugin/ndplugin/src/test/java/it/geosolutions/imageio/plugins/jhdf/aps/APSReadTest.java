package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.JHDFTest;
import it.geosolutions.imageio.plugins.jhdf.MetadataDisplay;
import it.geosolutions.imageio.plugins.slices2D.SliceImageReader;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;
import com.sun.media.jai.operator.ImageReadDescriptor;

public class APSReadTest extends JHDFTest {
		public APSReadTest(String name) {
			super(name);
		}
		
		/**
		 * Simple Metadata retrieving and displaying.
		 * 
		 * @throws IOException
		 */
		public void testMetadata() throws IOException {
			//this Test File is available at 
			//ftp://ftp.geo-solutions.it/incoming/MODPM2007027121858.L3_000_EAST_MED.zip
			//as anonymous ftp access.
			
			//TODO: build an utility to auto-download and unzip this file in 
			//the setUp method.
			final File file = TestData.file(this,"MODPM2007027121858.L3_000_EAST_MED.HDF");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
			"ImageRead");
			pbjImageRead.setParameter("Input", file);
			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			ImageReader reader = (ImageReader) image
					.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
		
			final IIOMetadata metadata = reader.getImageMetadata(2);
			IIOMetadataNode imageNode = (IIOMetadataNode) metadata
					.getAsTree(APSImageMetadata.nativeMetadataFormatName);
			System.out
					.println(MetadataDisplay.buildMetadataFromNode(imageNode));

			final IIOMetadata streamMetadata = reader.getStreamMetadata();
			IIOMetadataNode streamNode = (IIOMetadataNode) streamMetadata
					.getAsTree(APSStreamMetadata.nativeMetadataFormatName);
			System.out.println(MetadataDisplay
					.buildMetadataFromNode(streamNode));
		}
		
		/**
		 * 
		 * @throws IOException
		 */
		public void testJaiRead() throws IOException {
//			this Test File is available at 
			//ftp://ftp.geo-solutions.it/incoming/MODPM2007027121858.L3_000_EAST_MED.zip
			//as anonymous ftp access.
			
			//TODO: build an utility to auto-download and unzip this file in 
			//the setUp method.
			final File file = TestData.file(this,"MODPM2007027121858.L3_000_EAST_MED.HDF");
			for (int i = 0; i < 3; i++) {
				ImageReader reader = new APSImageReader(new APSImageReaderSpi());
				reader.setInput(file);
				final int imageIndex = ((SliceImageReader)reader).retrieveSlice2DIndex(i, null);
							
				final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
						"ImageRead");
				pbjImageRead.setParameter("Input", file);
				pbjImageRead.setParameter("reader", reader);
				pbjImageRead.setParameter("imageChoice", Integer.valueOf(imageIndex));
				
				final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
				
				final File outputFile = TestData.temp(this, "WriteHDFData"+i, false);
				final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
						"ImageWrite");
				pbjImageWrite.setParameter("Transcode",false);
				pbjImageWrite.setParameter("UseProperties",false);
				pbjImageWrite.setParameter("Output",outputFile);
				ImageWriter writer = new J2KImageWriterSpi()
						.createWriterInstance();
				pbjImageWrite.setParameter("Writer", writer);

				// Specifying image source to write
				pbjImageWrite.addSource(image);

				// Writing
				final RenderedOp writeOp = JAI.create("ImageWrite", pbjImageWrite);
				//USE AN EXTERNAL JP2K VIEWER TO VIEW WRITTEN IMAGE
				//The data of this test contains negative numbers.
			}
		}
		
		public static Test suite() {
			TestSuite suite = new TestSuite();

			suite.addTest(new APSReadTest("testJaiRead"));
			
			suite.addTest(new APSReadTest("testMetadata"));

			return suite;
		}

		public static void main(java.lang.String[] args) {
			junit.textui.TestRunner.run(suite());
		}
	}

