package net.sourceforge.jgrib.test;

import it.geosolutions.resources.TestData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordBDS;
import net.sourceforge.jgrib.GribRecordBMS;
import net.sourceforge.jgrib.GribRecordGDS;
import net.sourceforge.jgrib.GribRecordIS;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.GribFile.AccessType;
import net.sourceforge.jgrib.factory.GribGDSFactory;
import net.sourceforge.jgrib.gdsgrids.GribGDSLambert;
import net.sourceforge.jgrib.gdsgrids.GribGDSPolarStereo;
import net.sourceforge.jgrib.gdsgrids.GribGDSRotatedLatLon;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alessio Fabiani
 * @author Simone Giannecchini
 * 
 */
public class GriBTest  {

	private final static Logger LOGGER = Logger.getLogger(GriBTest.class.toString());

	private GribFile gribFile = null;

	@Test
	public void testGrib() throws FileNotFoundException, IOException
			 {

		// /////////////////////////////////////////////////////////////////////
		//
		// open the grib file
		//
		// /////////////////////////////////////////////////////////////////////
		GribFile gribFile1 = null;
		final File test_dir = TestData.file(GriBTest.class, ".");
		final File files[] = test_dir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (!new File(dir.getAbsolutePath() + File.separator + name)
						.isDirectory()
						&& (name.toLowerCase().endsWith(".grb") || name
								.toLowerCase().endsWith(".grib")))
					return true;
				return false;
			}
		});
		final int length = files.length;

		// /////////////////////////////////////////////////////////////////////
		//
		// Main loop
		//
		// /////////////////////////////////////////////////////////////////////
		for (int i = 0; i < length; i++) {

			// //
			//
			// input file
			//
			// //
			final StringBuilder buffer = new StringBuilder("Parsing file ").append(files[i]).append("\n");
			gribFile = GribFile.open(files[i],AccessType.R);
			gribFile.parseGribFile();
			// file to write out
			final GribFile file = GribFile.open(null, AccessType.RW);

			// creating a grib record
			final int recordCount = gribFile.getRecordCount();
			buffer.append("\n\tnumber of records ").append(recordCount).append("\n");
			LOGGER.info(buffer.toString());
			for (int j = 1; j <= recordCount; j++) {
				
				LOGGER.info("testing record "+j);

				final GribRecord oldRecord = gribFile.getRecord(j);
				Assert.assertNotNull((oldRecord));
				final GribRecord record = new GribRecord();

				// /////////////////////////////////////////////////////////////
				//
				// PDS
				//
				// /////////////////////////////////////////////////////////////
				// get the old pds
				final GribRecordPDS oldPDS = oldRecord.getPDS();

				// set the new one
				record.setPDS(new GribRecordPDS(oldPDS.getTableVersion(), oldPDS
						.getOriginatingCenterID(), oldPDS
						.getGeneratingProcessID(), oldPDS.getGridID(), true,
						oldPDS.bmsExists(), oldPDS.getParameter().getNumber(),
						oldPDS.getLevel().getIndex(), oldPDS.getLevel()
								.getValue1(), oldPDS.getLevel().getValue2(),
						oldPDS.getGMTBaseTime(), oldPDS.getForecastTimeUnit(),
						oldPDS.getP1(), oldPDS.getP2(), oldPDS
								.getTimeRangeIndicator(), 0, 0, oldPDS
								.getSubcenterID(), oldPDS.getDecimalScale()));
				// check for equality
				Assert.assertTrue(record.getPDS().equals(
						gribFile.getRecord(j).getPDS()));

				// /////////////////////////////////////////////////////////////
				//
				// GDS
				//
				// /////////////////////////////////////////////////////////////
				// get the old gds
				final GribRecordGDS oldGDS = oldRecord.getGDS();
				final GribRecordGDS gds = GribGDSFactory.getGDS(oldGDS.getGridType());
				gds.setGridType(oldGDS.getGridType());
				gds.setGridMode(oldGDS.getGridMode());
				gds.setLength(oldGDS.getLength());
				gds.setGridNX(oldGDS.getGridNX());
				gds.setGridNY(oldGDS.getGridNY());
				gds.setGridLat2(oldGDS.getGridLat2());
				gds.setGridLat1(oldGDS.getGridLat1());
				gds.setGridLon1(oldGDS.getGridLon1());
				gds.setGridLon2(oldGDS.getGridLon2());
				gds.setGridScanmode(oldGDS.getGridDX() > 0,oldGDS.getGridDY() > 0, oldGDS.isAdiacent_i_Or_j());
				gds.setGridDX(oldGDS.getGridDX());
				gds.setGridDY(oldGDS.getGridDY());
				gds.setGridMode(oldGDS.getGridMode());
				gds.setGridLatSP(oldGDS.getGridLatSP());
				gds.setGridLonSP(oldGDS.getGridLonSP());
				gds.setGridRotAngle(oldGDS.getGridRotAngle());

				switch (oldGDS.getGridType()) {
				case GribGDSRotatedLatLon.ROTATED_LATLON_GRID_TYPE:
					((GribGDSRotatedLatLon)gds).setGridLatSPST(((GribGDSRotatedLatLon)oldGDS).getGridLatSPST());
					((GribGDSRotatedLatLon)gds).setGridLonSPST(((GribGDSRotatedLatLon)oldGDS).getGridLonSPST());
					((GribGDSRotatedLatLon)gds).setGridStretchingFactor(((GribGDSRotatedLatLon)oldGDS).getGridStretchingFactor());
					break;
				case GribGDSPolarStereo.POLAR_STEREO_GRID_TYPE:
					break;
				case GribGDSLambert.LAMBERT_GRID_TYPE:
					((GribGDSLambert)gds).setGridLatin1(((GribGDSLambert)oldGDS).getGridLatin1());
					((GribGDSLambert)gds).setGridLatin2(((GribGDSLambert)oldGDS).getGridLatin2());
					((GribGDSLambert)gds).setProjCenter(((GribGDSLambert)oldGDS).getProjCenter());
					((GribGDSLambert)gds).setRhoRef(((GribGDSLambert)oldGDS).getRhoRef());
					break;
				default:
					break;
				}
				record.setGDS(gds);
				//
				Assert.assertTrue(record.getGDS().equals(oldGDS));

				// /////////////////////////////////////////////////////////////
				//
				//
				// BMS
				//
				//
				// /////////////////////////////////////////////////////////////
				final GribRecordBMS oldBMS = gribFile.getRecord(j).getBMS();
				if (oldPDS.bmsExists()) {
					record.setBMS(new GribRecordBMS(oldBMS.getBitmap()));
					//
					Assert.assertTrue(record.getBMS().equals(oldBMS));
				}

				// /////////////////////////////////////////////////////////////
				//
				//
				// BDS
				//
				//
				// /////////////////////////////////////////////////////////////
				// data
				final GribRecordBDS oldBDS = oldRecord.getBDS();
				record.setBDS(
						new GribRecordBDS(
								oldPDS.getDecimalScale(), 
								oldBDS.getNumBits(),
								oldBDS.getValues(), 
								oldBDS.getIsConstant(), 
								oldBDS.getMaxValue(), 
								oldBDS.getMinValue(),
								oldBDS.getNumValidValues(),
								oldGDS,
								oldBMS)
				);

				Assert.assertTrue("BDS sections differ",record.getBDS().equals(gribFile.getRecord(j).getBDS()));

				// /////////////////////////////////////////////////////////////
				//
				//
				// IS
				//
				//
				// /////////////////////////////////////////////////////////////
				final GribRecordIS oldIS = oldRecord.getIS();
				record.setIS(new GribRecordIS(oldIS.getGribEdition(), oldPDS.getLength(), oldGDS.getLength(), oldBMS == null ? 0 : oldBMS.getLength(),oldBDS.getLength()));
				// adding record to file
				file.addRecord(record);

			}
			Assert.assertTrue(file.equals(gribFile));

			// writing out
			// file to write to
			final File tempFile = File.createTempFile("tempfile", ".grib");
			tempFile.deleteOnExit();
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile), 4096);
			file.writeTo(out);
			out.flush();
			out.close();

			gribFile1 = GribFile.open(tempFile,AccessType.R);
			gribFile1.parseGribFile();


			if (!gribFile1.equals(gribFile)) {
				LOGGER.severe("errore grib file " + files[i].getName());
				throw new RuntimeException("errore grib file "+ files[i].getName());

			}
			
			gribFile1.dispose();
			gribFile1=null;
			gribFile.dispose();
			gribFile=null;
		}
	}


}
