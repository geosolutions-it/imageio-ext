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

import java.util.Map;

/**
 * Wrapper class related to a Main Header of a NITF file.
 * 
 * @author Daniele Romagnoli, GeoSolutions s.a.s.
 */
public class HeaderWrapper extends IdentifiableNITFObjectWrapper {

    public String getOriginStationId() {
        return originStationId;
    }

    public void setOriginStationId(String originStationId) {
        this.originStationId = originStationId;
    }

    public String getOriginatorName() {
        return originatorName;
    }

    public void setOriginatorName(String originatorName) {
        this.originatorName = originatorName;
    }

    public String getOriginatorPhone() {
        return originatorPhone;
    }

    public void setOriginatorPhone(String originatorPhone) {
        this.originatorPhone = originatorPhone;
    }

    public byte[] getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(byte[] backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    private String originStationId;

    private String originatorName;

    private String originatorPhone;

    private byte[] backgroundColor;

    private Map<String, Map<String, String>> tres;

    public Map<String, Map<String, String>> getTres() {
        return tres; // Should we clone this or made it immutable to avoid changing it
    }

    public void setTres(Map<String, Map<String, String>> tres) {
        this.tres = tres;
    }

}
