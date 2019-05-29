/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.tiff.gdal;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/** Bean representing GDAL own metadata XML */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GDALMetadata")
public class GDALMetadata {

    @XmlElement(name = "Item", required = true)
    protected List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "GDALMetadata{" + "items=" + items + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDALMetadata that = (GDALMetadata) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    /**
     * Utility methods extracting an offset array from the items, if available. Will throw runtime
     * exceptions if the offset item values are not numbers.
     *
     * @return the offset array, or null if no items are present, or no offset was found
     */
    public Double[] getOffsets(int bands) {
        return getBandDoubleValues(bands, "offset");
    }

    /**
     * Utility methods extracting a scales array from the items, if available. Will throw runtime
     * exceptions if the scales item values are not numbers.
     *
     * @return the offset array, or null if no items are present, or no offset was found
     */
    public Double[] getScales(int bands) {
        return getBandDoubleValues(bands, "scale");
    }

    private Double[] getBandDoubleValues(int bands, String role) {
        if (items == null) {
            return null;
        }
        // get items with offset role, and a band compatible with expectation
        Map<Integer, Double> bandsToValues =
                items.stream()
                        .filter(
                                i ->
                                        Objects.equals(role, i.getRole())
                                                && i.getSample() != null
                                                && i.getSample() < bands)
                        .collect(toMap(i -> i.getSample(), i -> parseDouble(i.getValue())));
        if (bandsToValues.isEmpty()) {
            return null;
        }

        Double[] values = new Double[bands];
        for (int i = 0; i < values.length; i++) {
            values[i] = bandsToValues.get(i);
        }

        return values;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        @XmlValue String value;

        @XmlAttribute String name;

        @XmlAttribute Integer sample;

        @XmlAttribute String role;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSample() {
            return sample;
        }

        public void setSample(Integer sample) {
            this.sample = sample;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(value, item.value)
                    && Objects.equals(name, item.name)
                    && Objects.equals(sample, item.sample)
                    && Objects.equals(role, item.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, name, sample, role);
        }

        @Override
        public String toString() {
            return "Item{"
                    + "value='"
                    + value
                    + '\''
                    + ", name='"
                    + name
                    + '\''
                    + ", sample="
                    + sample
                    + ", role='"
                    + role
                    + '\''
                    + '}';
        }
    }
}
