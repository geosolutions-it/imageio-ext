/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.nitronitf.wrapper;

import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities;

/**
 * Main Wrapper class recording encryption field, as well as security classification and security
 * classification system.
 * 
 * @author Daniele Romagnoli, GeoSolutions s.a.s.
 */
public class NITFObjectWrapper {

    protected NITFObjectWrapper() {

    }

    int encrypted = NITFUtilities.Consts.DEFAULT_ENCRYPTED;

    String securityClassificationSystem = NITFUtilities.Consts.DEFAULT_SECURITY_CLASSIFICATION_SYSTEM;

    String securityClassification = NITFUtilities.Consts.DEFAULT_SECURITY_CLASSIFICATION;

    public int getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(int encrypted) {
        this.encrypted = encrypted;
    }

    public String getSecurityClassificationSystem() {
        return securityClassificationSystem;
    }

    public void setSecurityClassificationSystem(String securityClassificationSystem) {
        this.securityClassificationSystem = securityClassificationSystem;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(String securityClassification) {
        this.securityClassification = securityClassification;
    }

}
