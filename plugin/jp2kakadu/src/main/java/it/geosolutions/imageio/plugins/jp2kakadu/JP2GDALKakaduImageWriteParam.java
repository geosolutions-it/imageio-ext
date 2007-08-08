/*
 * JImageIO-extension - OpenSource Java Image translation Library
 * http://www.geo-solutions.it/
 * (C) 2007, GeoSolutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALCreateOptionsHandler;
import it.geosolutions.imageio.gdalframework.GDALImageWriteParam;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlbeans.XmlException;
import org.openuri.easypo.JP2EncodingProfileDocument;
import org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile;
import org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Cblk;
import org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Cprecincts;
import org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Stiles;
import org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Cprecincts.Precinct;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;

public class JP2GDALKakaduImageWriteParam extends GDALImageWriteParam {
	
	/**{@link Logger} class.*/
	private final static Logger LOGGER =Logger.getLogger("it.geosolutions.imageio.plugins.jp2kakadu");
	
	JP2GDALKakaduCreateOptionsHandler myHandler = new JP2GDALKakaduCreateOptionsHandler();

	public JP2GDALKakaduImageWriteParam() {
		super(new J2KImageWriteParam(Locale.getDefault()));
		// TODO Auto-generated constructor stub
	}

	public JP2GDALKakaduImageWriteParam(final String xml) throws XmlException,
			IOException {
		super(new J2KImageWriteParam(Locale.getDefault()));
		// Bind the instance to the generated XMLBeans types.
		JP2EncodingProfileDocument jp2ProfileDoc = JP2EncodingProfileDocument.Factory
				.parse(xml);
		parseProfile(jp2ProfileDoc);
	}

	public JP2GDALKakaduImageWriteParam(final File file) throws XmlException,
			IOException {
		super(new J2KImageWriteParam(Locale.getDefault()));

		// Bind the instance to the generated XMLBeans types.
		JP2EncodingProfileDocument jp2ProfileDoc = JP2EncodingProfileDocument.Factory
				.parse(file);
		parseProfile(jp2ProfileDoc);
	}

	public JP2GDALKakaduImageWriteParam(final URL url) throws XmlException,
			IOException {
		super(new J2KImageWriteParam(Locale.getDefault()));

		// Bind the instance to the generated XMLBeans types.
		JP2EncodingProfileDocument jp2ProfileDoc = JP2EncodingProfileDocument.Factory
				.parse(url);
		parseProfile(jp2ProfileDoc);
	}

	/**
	 * @param jp2ProfileDoc
	 */
	private void parseProfile(JP2EncodingProfileDocument jp2ProfileDoc) {
		final JP2EncodingProfile profile = jp2ProfileDoc
				.getJP2EncodingProfile();

		// /////////////////////////////////////////////////////////////////////
		//
		// Setting Clayers JPEG2000 parameter
		//
		// /////////////////////////////////////////////////////////////////////
		final int cLayers = profile.getClayers();
		setClayers(cLayers);

		////
		//
		// Setting Clevels JPEG2000 parameter
		//
		////
		final int cLevels = profile.getClevels();
		setClevels(cLevels);

		////
		//
		// Setting Cblk JPEG2000 parameter
		//
		////
		final Cblk cBlk = profile.getCblk();
		if (cBlk != null) {

			final int cBlkH = cBlk.getHorizontal();
			final int cBlkV = cBlk.getVertical();
			final StringBuffer cBlcks = new StringBuffer("{").append(cBlkH)
					.append(",").append(cBlkV).append("}");
			setCblk(cBlcks.toString());
		}

		////
		//
		// Setting Corder JPEG2000 parameter
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Corder.Enum cOrder = profile
				.getCorder();
		if (cOrder != null)
			setCorder(cOrder.toString());

		////
		//
		// Setting Cycc JPEG2000 parameter
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Cycc.Enum cYcc = profile
				.getCycc();
		if (cYcc != null)
			setCycc(cYcc.toString());

		////
		//
		// Setting Cprecincts JPEG2000 parameter
		//
		////
		if (profile.isSetCprecincts()) {
			final Cprecincts cPrecincts = profile.getCprecincts();
			if (cPrecincts != null) {
				final Precinct[] precincts = cPrecincts.getPrecinctArray();
				final int numberOfPrecincts = precincts.length;
				final StringBuffer cPrecinctsValues = new StringBuffer();
				for (int i = 0; i < numberOfPrecincts; i++) {
					cPrecinctsValues.append("{").append(
							precincts[i].getVertical()).append(",").append(
							precincts[i].getHorizontal()).append("}");
					if (i != (numberOfPrecincts - 1))
						cPrecinctsValues.append(",");
				}
				setCprecincts(cPrecinctsValues.toString());
			}
		}

		////
		//
		// Setting Qguard JPEG2000 parameter
		//
		////
		if (profile.isSetQguard()) {
			final int qGuard = (profile.getQguard()).intValue();
			setQguard(qGuard);
		}

		////
		//
		// Setting Qstep JPEG2000 parameter
		//
		////
		if (profile.isSetQstep()) {
			final float qStep = profile.getQstep();
			setQstep(qStep);
		}

		////
		//
		// Setting ORGgen_tlm JPEG2000 parameter
		//
		////
		if (profile.isSetORGgenTlm()) {
			final int orgGenTlm = (profile.getORGgenTlm()).intValue();
			setORGgen_tlm(orgGenTlm);
		}

		////
		//
		// Setting ORGtparts JPEG2000 parameter
		//
		////
		if (profile.isSetORGtparts()) {
			final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.ORGtparts.Enum orgTparts = profile
					.getORGtparts();
			if (orgTparts != null)
				setORGtparts(orgTparts.toString());
		}

		////
		//
		// Setting ORGgen_plt JPEG2000 parameter
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.ORGgenPlt.Enum orgGenPlt = profile
				.getORGgenPlt();
		if (orgGenPlt != null)
			setORGgen_plt(orgGenPlt.toString());

		////
		//
		// Setting tiling
		//
		////
		if (profile.isSetStiles()) {
			final Stiles sTiles = profile.getStiles();
			if (sTiles != null) {
				final long tileWidth = sTiles.getTileWidth();
				final long tileHeight = sTiles.getTileHeight();
				setTiling((int) tileWidth, (int) tileHeight);
			}
		}

		////
		//
		// Setting GMLJP2 GDAL Create Option
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.GMLJP2.Enum gmlJP2 = profile
				.getGMLJP2();
		if (gmlJP2 != null)
			setGMLJp2(gmlJP2.toString());

		////
		//
		// Setting GeoJP2 GDAL Create Option
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.GeoJP2.Enum geoJP2 = profile
				.getGeoJP2();
		if (geoJP2 != null)
			setGeoJp2(geoJP2.toString());

		////
		//
		// Setting COMSEG GDAL Create Option
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Comseg.Enum comseg = profile
				.getComseg();
		if (comseg != null)
			setComseg(comseg.toString());

		////
		//
		// Setting FLUSH GDAL Create Option
		//
		////
		final org.openuri.easypo.JP2EncodingProfileDocument.JP2EncodingProfile.Flush.Enum flush = profile
				.getFlush();
		if (flush != null)
			setFlush(flush.toString());

		////
		//
		// Setting Quality GDAL Create Option
		//
		////
		final float quality = profile.getQuality();
		if (Math.abs(quality)>1E-1&&Math.abs(quality)<=1.0)
			setQuality(quality);
		else
			if(LOGGER.isLoggable(Level.INFO))
				LOGGER.info("The provided quality value is invalid (or it no value was provided at all!");

	}

	/**
	 * This information is based on the documentation available at
	 * {@link http://www.gdal.org/frmt_jp2kak.html}
	 * 
	 * Creation Options:<BR />
	 * 
	 * QUALITY=n: Set the compressed size ratio as a percentage of the size of
	 * the uncompressed image. The default is 20 indicating that the resulting
	 * image should be 20% of the size of the uncompressed image. A value of 100
	 * will result in use of the lossless compression algorithm . Actual final
	 * image size may not exactly match that requested depending on various
	 * factors.<BR />
	 * 
	 * BLOCKXSIZE=n: Set the tile width to use. Defaults to 20000.<BR />
	 * 
	 * BLOCKYSIZE=n: Set the tile height to use. Defaults to image height.<BR />
	 * 
	 * GMLJP2=YES/NO: Indicates whether a GML box conforming to the OGC GML in
	 * JPEG2000 specification should be included in the file. Defaults to YES.<BR />
	 * 
	 * GeoJP2=YES/NO: Indicates whether a GML box conforming to the GeoJP2
	 * (GeoTIFF in JPEG2000) specification should be included in the file.
	 * Defaults to YES.<BR />
	 * 
	 * LAYERS=n: Control the number of layers produced. These are sort of like
	 * resolution layers, but not exactly. The default value is 12 and this
	 * works well in most situations.<BR />
	 * 
	 * ROI=xoff,yoff,xsize,ysize: Selects a region to be a region of interest to
	 * process with higher data quality. The various "R" flags below may be used
	 * to control the amount better. For example the settings "ROI=0,0,100,100",
	 * "Rweight=7" would encode the top left 100x100 area of the image with
	 * considerable higher quality compared to the rest of the image.<BR />
	 * 
	 * COMSEG=YES/NO: Indicates wheter a comment segment should be emitted
	 * during the flushing of the written codestream. Defaults to YES.
	 * 
	 * FLUSH=YES/NO: Indicates wheter multiple flush operations should be
	 * enabled during tiles writing. Defaults to YES.
	 */

	public GDALCreateOptionsHandler getGDALCreateOptionsHandler() {
		return myHandler;
	}

	/**
	 * Setting GeoJP2 Create Option
	 */
	public void setGeoJp2(String geoJp2) {
		myHandler.setCreateOption("GeoJP2", geoJp2.toUpperCase());
	}

	/**
	 * Setting ORGgen_plt Create Option
	 */
	public void setORGgen_plt(String ORGgen_plt) {
		myHandler.setCreateOption("ORGgen_plt", ORGgen_plt.toLowerCase());
	}

	/**
	 * Setting ORGgen_tlm Create Option
	 */
	public void setORGgen_tlm(final int ORGgen_tlm) {
		myHandler.setCreateOption("ORGgen_tlm", ORGgen_tlm);
	}

	/**
	 * Setting ORGtparts Create Option
	 */
	public void setORGtparts(String orgTparts) {
		myHandler.setCreateOption("ORGtparts", orgTparts);
	}

	/**
	 * Setting GMLJp2 Create Option
	 */
	public void setGMLJp2(String gmlJp2) {
		myHandler.setCreateOption("GMLJP2", gmlJp2.toUpperCase());
	}

	/**
	 * Setting Clayers Create Option
	 */
	public void setLayers(final int layers) {
		myHandler.setCreateOption("Clayers", layers);
	}

	/**
	 * Setting Clayers Create Option
	 */
	public void setClayers(final int layers) {
		setLayers(layers);
	}

	/**
	 * Setting Cycc Create Option
	 */
	public void setCycc(String cycc) {
		myHandler.setCreateOption("Cycc", cycc);
	}

	/**
	 * Setting Clevels Create Option
	 */
	public void setClevels(final int clevels) {
		myHandler.setCreateOption("Clevels", clevels);
	}

	/**
	 * Setting Qguard Create Option
	 */
	public void setQguard(final int qguard) {
		myHandler.setCreateOption("Qguard", qguard);
	}

	/**
	 * Setting Qstep Create Option
	 */
	public void setQstep(final float qstep) {
		myHandler.setCreateOption("Qstep", qstep);
	}

	/**
	 * Setting ROI Create Option
	 */
	public void setRoi(String roi) {
		myHandler.setCreateOption("ROI", roi);
	}

	/**
	 * Setting Quality Create Option
	 * 
	 * @param quality
	 *            the compressed size ratio as a percentage of the size of the
	 *            uncompressed image.
	 */
	public void setQuality(final float quality) {
		myHandler.setCreateOption("QUALITY", (quality));
	}

	/**
	 * Setting Cprecincts Create Option
	 */
	public void setCprecincts(String cPrecincts) {
		myHandler.setCreateOption("Cprecincts", cPrecincts);
	}

	/**
	 * Setting Corder Create Option
	 */
	public void setCorder(String cOrder) {
		myHandler.setCreateOption("Corder", cOrder);
	}

	/**
	 * Setting Cblk Create Option
	 */
	public void setCblk(String cBlk) {
		myHandler.setCreateOption("Cblk", cBlk);
	}

	/**
	 * Setting SProfile Create Option
	 */

	public void setSProfile(final int sProfile) {
		final String profile;
		switch (sProfile) {
		case 0:
			profile = "PROFILE0";
			break;
		case 1:
			profile = "PROFILE1";
			break;
		case 2:
			profile = "PROFILE2";
			break;
		case 3:
			profile = "PART2";
			break;
		case 4:
			profile = "CINEMA2K";
			break;
		case 5:
			profile = "CINEMA4K";
			break;
		default:
			profile = "WRONG!!";
			break;
		}
		myHandler.setCreateOption("SProfile", profile);
	}

	public void setSProfile(final String sProfile) {
		myHandler.setCreateOption("SProfile", sProfile);
	}

	/**
	 * Setting Cmodes Create Option
	 */
	public void setCmodes(String cModes) {
		myHandler.setCreateOption("Cmodes", cModes);

	}

	/**
	 * Setting COMSEG Create Option
	 */
	public void setComseg(String comseg) {
		myHandler.setCreateOption("COMSEG", comseg);
	}

	/**
	 * Setting ROI Create Option
	 */
	public void setROI(String roi) {
		myHandler.setCreateOption("ROI", roi);
	}

	/**
	 * Setting Rshift Create Option
	 */
	public void setRshift(int rShift) {
		myHandler.setCreateOption("Rshift", rShift);
	}

	/**
	 * Setting Rlevels Create Option
	 */
	public void setRlevels(int rLevels) {
		myHandler.setCreateOption("Rlevels", rLevels);
	}

	/**
	 * Setting Rweight Create Option
	 */
	public void setRweight(float rWeight) {
		myHandler.setCreateOption("Rweight", rWeight);
	}

	/**
	 * Setting FLUSH Create Option
	 */
	public void setFlush(String flush) {
		myHandler.setCreateOption("FLUSH", flush);

	}

	/**
	 * Setting tile properites.
	 */
	public void setTiling(final int tileWidth, final int tileHeight) {
		super.setTiling(tileWidth, tileHeight);
		myHandler.setCreateOption("BLOCKXSIZE", Integer.toString(tileWidth));
		myHandler.setCreateOption("BLOCKYSIZE", Integer.toString(tileHeight));
	}

	public boolean canWriteTiles() {
		return true;
	}

}
