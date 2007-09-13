package it.geosolutions.imageio.plugins.grib1;

import javax.imageio.metadata.IIOMetadataNode;

import net.sourceforge.jgrib.GribFile;
import it.geosolutions.imageio.plugins.ImageMetadata;
import it.geosolutions.imageio.plugins.Utilities;

public class GRIB1ImageMetadata extends ImageMetadata{
	
	public GRIB1ImageMetadata(final int imageIndex, Object source){
		this.imageIndex=imageIndex;
		if (source instanceof GribFile){
			sourceURI = (Utilities.getURIFromSource(((GribFile)source).getOriginatingSource())).toString();
		}
	}

	protected IIOMetadataNode appendBandsNode() {
		IIOMetadataNode bandsNode = new IIOMetadataNode("bands");
				
		//TODO: add BandsNode
		return bandsNode;
	}

}
