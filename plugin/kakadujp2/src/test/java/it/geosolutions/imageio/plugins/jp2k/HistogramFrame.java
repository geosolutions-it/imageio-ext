package it.geosolutions.imageio.plugins.jp2k;

/*
 * $RCSfile: HistogramFrame.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:40:06 $
 * $State: Exp $
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.jai.Histogram;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.sun.media.jai.widget.DisplayJAI;

/**
 * This class is defined to display the statistics or the histogram in a
 * <code>JFrame</code>.
 * 
 * When the histogram is displayed, the color of the bar for each bin is
 * corresponding to the color of the pixels whose values fall into this bin.
 * 
 * When the statistic parameters are displayed, the following parameters of a
 * rectangular ROI are listed in a table: the area, the width, the height, the
 * maximum, the minimum, the mean, the standard deviation and the entropy of the
 * values of the pixels located in this ROI.
 * 
 */

@SuppressWarnings("serial")
class HistogramFrame extends JFrame {

	/**
	 * The histogram to be displayed or to be used to calculate the statistics.
	 */
	private Histogram histogram;

	/**
	 * Constructor.
	 * 
	 * It accepts four parameters as defined below:
	 * 
	 * @param h
	 *            The histogram object to display or to calculate the
	 *            statistics.
	 * @param displayHistogram
	 *            Indicates to display histogram or statistics.
	 * @param lut
	 *            The lut for window/level operation. Used to define the color
	 *            of each bin of the histogram when display.
	 * @param roi
	 *            The ROI in the original image coordinate system.
	 */

	public HistogramFrame(Histogram h, boolean displayHistogram) {
		this.histogram = h;

		if (displayHistogram) {
			// display histogram
			this.setTitle("Histogram");
			DisplayJAI pane = new DisplayJAI(displayHistogram(h));
			this.getContentPane().add(pane);
		} else {
			// display statistics
			this.setTitle("Statistics");
			JTable table = getStatistics(h);
			JScrollPane scrollpane = new JScrollPane(table);
			this.getContentPane().add(scrollpane);
		}
	}

	/**
	 * Create the table of the statistic parameters. Currently, the following
	 * parameters are calculated and displayed: the area, the width, the height,
	 * the maximum, the minimum, the mean, the standard deviation and the
	 * entropy of the values of the pixels located in the ROI.
	 */

	private JTable getStatistics(Histogram h) {
		double[] mean = h.getMean();
		mean[0] = ((int) (mean[0] * 10)) / 10.0;
		int[] minValue = getMinValue();
		int[] maxValue = getMaxValue();
		double[] stdev = h.getStandardDeviation();
		stdev[0] = ((int) (stdev[0] * 10)) / 10.0;

		double[] entropy = h.getEntropy();
		entropy[0] = ((int) (entropy[0] * 10)) / 10.0;

		String[] heading = new String[] { "Parameter", "Value" };
		String[][] content = new String[][] { { "Max", "" + maxValue[0] },
				{ "Min", "" + minValue[0] }, { "Mean", "" + mean[0] },
				{ "StDev", "" + stdev[0] }, { "Entropy", "" + entropy[0] } };

		return new JTable(content, heading);
	}

	/**
	 * Calculate the area of the ROI, in <code>mm<sup>2</sup></code>, based
	 * on the path iterator.
	 */
	private double getArea(Area roi) {
		PathIterator path = roi.getPathIterator(new AffineTransform(), 0.1);
		double[] coordinates = new double[6];
		double x1 = 0.0, y1 = 0.0;
		double area = 0.0;

		while (path.isDone() == false) {
			int type = path.currentSegment(coordinates);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				x1 = coordinates[0];
				y1 = coordinates[1];
				break;

			case PathIterator.SEG_LINETO:
				area += (coordinates[1] - y1) * (x1 + coordinates[0]) / 2.0;
				x1 = coordinates[0];
				y1 = coordinates[1];
				break;
			}
			path.next();
		}
		area = Math.abs(((int) (area * 10)) / 10.0);
		return area;
	}

	/** Compute the X-direction extension of the ROI, in <code>mm</code>. */
	private double getWidth(Area roi) {
		PathIterator path = roi.getPathIterator(new AffineTransform(), 0.1);
		double[] coordinates = new double[6];
		double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;

		while (path.isDone() == false) {
			int type = path.currentSegment(coordinates);
			if (coordinates[0] > maxX)
				maxX = coordinates[0];
			if (coordinates[0] < minX)
				minX = coordinates[0];
			path.next();
		}
		return ((int) Math.abs(maxX - minX) * 10) / 10.0;
	}

	/** Compute the Y-direction extension of the ROI, in <code>mm</code>. */
	private double getHeight(Area roi) {
		PathIterator path = roi.getPathIterator(new AffineTransform(), 0.1);
		double[] coordinates = new double[6];
		double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

		while (path.isDone() == false) {
			int type = path.currentSegment(coordinates);
			if (coordinates[1] > maxY)
				maxY = coordinates[1];
			if (coordinates[1] < minY)
				minY = coordinates[1];
			path.next();
		}
		return ((int) Math.abs(maxY - minY) * 10) / 10.0;
	}

	/** Return the minimum pixel value in the provided ROI. */
	private int[] getMinValue() {
		int numBands = histogram.getNumBands();
		int numBins = histogram.getNumBins(0);
		int[] minValue = new int[numBands];
		double[] lowValue = histogram.getLowValue();
		double[] highValue = histogram.getHighValue();

		for (int b = 0; b < numBands; b++) {
			int[] bins = histogram.getBins(b);
			for (int i = 0; i < numBins; i++) {
				if (bins[i] > 0) {
					minValue[b] = (int) (i * (highValue[b] - lowValue[b])
							/ numBins + lowValue[b]);
					break;
				}
			}
		}

		return minValue;
	}

	/** Return the maximum pixel value of the provided ROI. */
	private int[] getMaxValue() {
		int numBands = histogram.getNumBands();
		int numBins = histogram.getNumBins(0);
		int[] maxValue = new int[numBands];
		double[] lowValue = histogram.getLowValue();
		double[] highValue = histogram.getHighValue();

		for (int b = 0; b < numBands; b++) {
			int[] bins = histogram.getBins(b);
			for (int i = numBins - 1; i >= 0; i--) {
				if (bins[i] > 0) {
					maxValue[b] = (int) (i * (highValue[b] - lowValue[b])
							/ numBins + lowValue[b]);
					break;
				}
			}
		}

		return maxValue;
	}

	/**
	 * Draw the histogram in a RenderedImage for display. The color of the bar
	 * for each bin is defined by the provided LUT.
	 * 
	 * @throws RuntimeException
	 *             When the bands have different number of bins.
	 */
	private RenderedImage displayHistogram(Histogram h) {
		int numBands = h.getNumBands();
		int numBins = h.getNumBins(0);
		for (int b = 1; b < numBands; b++) {
			if (h.getNumBins(b) != numBins) {
				throw new RuntimeException("All bands must have same numBins.");
			}
		}

		// compute the maximum count
		double maxCount = 0;
		for (int b = 0; b < numBands; b++) {
			int[] bins = h.getBins(b);
			for (int i = 0; i < numBins; i++) {
				if (bins[i] > maxCount) {
					maxCount = bins[i];
				}
			}
		}

		// define the size of the image and create the image
		int width = 2 * numBins + 70;
		int height = numBins + 30;
		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);

		// set background color
		Graphics2D g = bi.createGraphics();
		int maxVal = numBins - 1;
		g.setColor(new Color(10, 128, 128));
		g.fillRect(0, 0, width, height);

		// set the area for the rulers to the background color
		g.setColor(new Color(10, 128, 128));
		g.fillRect(0, maxVal + 1, width, height);

		// draw the coordinate axes
		g.setColor(Color.green);
		g.drawLine(40, 0, 40, maxVal);
		g.drawLine(35, 0, 40, 0);
		g.drawLine(35, maxVal, 40, maxVal);
		g.drawString("0", 10, maxVal);
		g.drawString("" + ((int) maxCount), 0, 10);

		// draw the ticks and the labels
		g.drawLine(40, maxVal, width - 30, maxVal);
		for (int i = 0; i <= 8; i++) {
			int x = 40 + i * numBins / 4;
			g.drawLine(x, maxVal, x, maxVal + 5);
			String label = "" + (i * ((int) h.getHighValue()[0]) / 8);
			g.drawString(label, x - label.length() * 3, maxVal + 18);
		}

		g.dispose();

		return bi;
	}
}
