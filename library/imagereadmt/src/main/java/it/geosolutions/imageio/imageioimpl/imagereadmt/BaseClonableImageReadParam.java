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

import it.geosolutions.imageio.imageioimpl.EnhancedImageReadParam;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

/**
 * The base abstract class for cloning {@link ImageReadParam}s.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 * @deprecated use {@link EnhancedImageReadParam} instead.
 */
public abstract class BaseClonableImageReadParam extends
        CloneableImageReadParam {
    /**
     * Performs a narrow clone of this {@link ImageReadParam}.
     * 
     * @param param
     *                the {@link ImageReadParam} instance containing the clone.
     * @return the narrow clone of this {@link ImageReadParam}.
     */
    protected Object narrowClone(ImageReadParam param) {
        param.setDestination(this.getDestination());
        int[] destBands = this.getDestinationBands();
        if (destBands != null)
            param.setDestinationBands((int[]) destBands.clone());
        Point p = this.getDestinationOffset();
        if (p != null) {
            param.setDestinationOffset((Point) p.clone());
        }

        if (this.getDestinationType() != null)
            param.setDestinationType(this.getDestinationType());
        int[] srcBands = this.getSourceBands();
        if (srcBands != null)
            param.setSourceBands((int[]) srcBands.clone());

        param.setSourceProgressivePasses(this.getSourceMinProgressivePass(),
                this.getSourceNumProgressivePasses());
        Rectangle srcRegion = this.getSourceRegion();
        if (srcRegion != null) {
            param.setSourceRegion((Rectangle) srcRegion.clone());
        }

        param.setSourceSubsampling(this.getSourceXSubsampling(), this
                .getSourceYSubsampling(), this.getSubsamplingXOffset(), this
                .getSubsamplingYOffset());
        param.setController(this.getController());
        Dimension d = this.getSourceRenderSize();
        if (d != null) {
            param.setSourceRenderSize((Dimension) d.clone());
        }
        return param;
    }
}
