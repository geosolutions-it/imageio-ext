package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.imageio.plugins.jhdf.HDFProducts;

public abstract class TOVSPathAProperties {
	public static class TOVSPathAProducts extends HDFProducts {

		//The following list is based on the documentation available at:
		//http://disc.sci.gsfc.nasa.gov/atmosphere/dynamics/data/tovs3a_dataset.shtml
		
		public TOVSPathAProducts() {
			super(17);
			HDFProduct temp = new HDFProduct("TEMP", 1);
			setHDFProduct(0, temp);

			HDFProduct cltemp = new HDFProduct("CLTEMP", 1);
			setHDFProduct(1, cltemp);

			HDFProduct prwat = new HDFProduct("PRWAT", 1);
			setHDFProduct(2, prwat);

			HDFProduct tsurf = new HDFProduct("TSURF", 1);
			setHDFProduct(3, tsurf);

			HDFProduct fcld = new HDFProduct("FCLD", 1);
			setHDFProduct(4, fcld);

			HDFProduct fcldp = new HDFProduct("FCLDP", 1);
			setHDFProduct(5, fcldp);

			HDFProduct pcld = new HDFProduct("PCLD", 1);
			setHDFProduct(6, pcld);

			HDFProduct tcld = new HDFProduct("TCLD", 1);
			setHDFProduct(7, tcld);
			
			HDFProduct toz = new HDFProduct("TOZ", 1);
			setHDFProduct(8, toz);
			
			HDFProduct olr = new HDFProduct("OLR", 1);
			setHDFProduct(9, olr);
			
			HDFProduct lcrf = new HDFProduct("LCRF", 1);
			setHDFProduct(10, lcrf);
			
			HDFProduct precip = new HDFProduct("PRECIP", 1);
			setHDFProduct(11, precip);

			HDFProduct sphum = new HDFProduct("SPHUM", 1);
			setHDFProduct(12, sphum);
			
			HDFProduct psurf = new HDFProduct("PSURF", 1);
			setHDFProduct(13, psurf);
			
			//TODO: The following 3 parameters Should be removed?
			HDFProduct zangle = new HDFProduct("ZANGLE", 1);
			setHDFProduct(14, zangle);
			
			HDFProduct time = new HDFProduct("TIME", 1);
			setHDFProduct(15, time);
			
			HDFProduct qflag = new HDFProduct("QFLAG", 1);
			setHDFProduct(16, qflag);
		}
	}

	public final static TOVSPathAProperties.TOVSPathAProducts tovsProducts = new TOVSPathAProperties.TOVSPathAProducts();

}
