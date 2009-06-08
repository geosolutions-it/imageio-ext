/*
 * Created on Aug 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.jgrib.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

import junit.framework.TestCase;
import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordGDS;
import net.sourceforge.jgrib.GribFile.AccessType;

/**
 * @author Simone Giannecchini
 * 
 */
public class GribUnifier extends TestCase {

	/**
	 * 
	 */
	public GribUnifier() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public GribUnifier(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public void testUnifier() throws Exception {

		// file to write out
		final GribFile file = GribFile.open(null, AccessType.RW);
		// creating a grib record
		GribRecord record = null;

		// checking all the grib files
		final File test_dir = new File("d:\\work\\gribs");// DataLoader.file(this,
		// ".");
		File files[] = test_dir.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return GribFile.canDecodeInput(file);
			}
		});
		final int numFiles = files.length;
		for (int i = 0; i < numFiles; i++) {

			// input file
			GribFile gribFile = GribFile.open(files[i],AccessType.R);
			gribFile.parseGribFile();

			final int length = gribFile.getRecordCount();
			for (int j = 1; j <= length; j++) {
				record = new GribRecord();
				// System.out.println(j);
				/* PDS */
				record
						.setPDS(
								gribFile.getRecord(j).getPDS()
										.getTableVersion(), // paramTableVersion,//currently
								// 3 for
								// internationa
								// exhange (2 is
								// still accepted)
								gribFile.getRecord(j).getPDS()
										.getOriginatingCenterID(), // byte
								// centerID,//code
								// table 0
								gribFile.getRecord(j).getPDS()
										.getGeneratingProcessID(), // byte
								// generatingProcessID,//allocated
								// by
								// originating
								// center
								gribFile.getRecord(j).getPDS().getGridID(), // byte
								// gridID
								// for
								// the
								// moment
								// we
								// require
								// gds
								// to
								// be
								// given!!!
								true, // boolean GDS,
								gribFile.getRecord(j).getPDS().bmsExists(), // boolean
								// BMS,
								gribFile.getRecord(j).getPDS().getParameter()
										.getNumber(), // byte paramID, //code
								// table 2
								gribFile.getRecord(j).getPDS().getLevel()
										.getIndex(), // byte levelID,
								gribFile.getRecord(j).getPDS().getLevel()
										.getValue1(), // short levelValue1,
								gribFile.getRecord(j).getPDS().getLevel()
										.getValue2(), // short levelValue2,
								gribFile.getRecord(j).getPDS().getGMTBaseTime(),
								gribFile.getRecord(j).getPDS()
										.getForecastTimeUnit(), // byte
								// forecastTimeUnitID,
								gribFile.getRecord(j).getPDS().getP1(), // byte
								// P1,
								gribFile.getRecord(j).getPDS().getP2(), // byte
								// P2,
								gribFile.getRecord(j).getPDS()
										.getTimeRangeIndicator(), // byte
								// timeRangeID,
								0, // short includedInAvrage,
								0, // byte missingFromAverage,
								gribFile.getRecord(j).getPDS().getSubcenterID(), // byte
								// subCenterID,
								gribFile.getRecord(j).getPDS()
										.getDecimalScale() // short
						// decimalScaleFactor
						);
				assertTrue(record.getPDS().equals(
						gribFile.getRecord(j).getPDS()));
				/* GDSLatLon */
				GribRecordGDS gds = net.sourceforge.jgrib.factory.GribGDSFactory.getGDS(0);
				// now we have to set each single field we need
				gds.setGridNX(gribFile.getRecord(j).getGDS().getGridNX());
				gds.setGridNY(gribFile.getRecord(j).getGDS().getGridNY());
				gds.setGridLat2(gribFile.getRecord(j).getGDS().getGridLat2());
				gds.setGridLat1(gribFile.getRecord(j).getGDS().getGridLat1());
				gds.setGridLon1(gribFile.getRecord(j).getGDS().getGridLon1());
				gds.setGridLon2(gribFile.getRecord(j).getGDS().getGridLon2());
				gds.setGridScanmode(
						gribFile.getRecord(j).getGDS().getGridDX() > 0,
						gribFile.getRecord(j).getGDS().getGridDY() > 0,
						gribFile.getRecord(j).getGDS().isAdiacent_i_Or_j());
				gds.setGridDX(gribFile.getRecord(j).getGDS().getGridDX());
				gds.setGridDY(gribFile.getRecord(j).getGDS().getGridDY());
				gds.setGridMode(gribFile.getRecord(j).getGDS().getGridMode()); // only
				// 128-0
				// is
				// suported
				// for
				// the
				// moment

				record.setGDS(gds);
				assertTrue(record.getGDS().equals(
						gribFile.getRecord(j).getGDS()));

				/* BMS */
				if (gribFile.getRecord(j).getPDS().bmsExists()) {
					record.setBMS(gribFile.getRecord(j).getBMS().getBitmap());
					assertTrue(record.getBMS().equals(
							gribFile.getRecord(j).getBMS()));
				}

				/* BDS */
				// data
				record.setBDS(gribFile.getRecord(j).getPDS().getDecimalScale(), // decimal
						// scale,
						// using
						// only
						// first
						// 16
						// bits
						gribFile.getRecord(j).getBDS().getNumBits(), // datum
						// point
						// bit
						// length
						// 0
						// stands
						// for
						// variable
						// bit
						// length
						gribFile.getRecord(j).getBDS().getValues(), // data
						gribFile.getRecord(j).getBDS().getIsConstant(), // is
						// constant?
						gribFile.getRecord(j).getBDS().getNumValidValues(),
						gribFile.getRecord(j).getBDS().getMaxValue(), gribFile
								.getRecord(j).getBDS().getMinValue());

				assertTrue(record.getBDS().equals(
						gribFile.getRecord(j).getBDS()));

				/* IS */
				record.setIS(gribFile.getRecord(j).getIS().getGribEdition(),
						gribFile.getRecord(j).getPDS().getLength(), gribFile
								.getRecord(j).getGDS().getLength(), gribFile
								.getRecord(j).getBMS() == null ? 0 : gribFile
								.getRecord(j).getBMS().getLength(), gribFile
								.getRecord(j).getBDS().getLength());
				// adding record to file
				file.addRecord(record);

			}
			// assertTrue(file.equals(gribFile));
		}
		// writing out
		// file to write to
		File finalFile = new File("d:\\work\\gribs\\alltogether.grb");
		// tempFile.deleteOnExit();
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(finalFile), 4096);
		file.writeTo(out);

	}

}
