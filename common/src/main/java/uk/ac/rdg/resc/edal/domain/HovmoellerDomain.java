/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.domain;

import java.util.List;

import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.domain.HovmoellerDomain.HovmoellerCell;
import uk.ac.rdg.resc.edal.util.Array1D;

/**
 * A domain for measurement of a time series along points on a line string.
 * 
 * @author Nan
 */
public class HovmoellerDomain implements DiscreteDomain<GeoPosition, HovmoellerCell> {

    public static class HovmoellerCell {
        public final HorizontalPosition horizontalPosition;
        public final Extent<DateTime> timeExtent;

        HovmoellerCell(HorizontalPosition hPos, Extent<DateTime> tExtent) {
            horizontalPosition = hPos;
            timeExtent = tExtent;
        }
    }

    /**
     * An implementation of Array1D containing HovmoellerDomain entity:
     * HovmoellerCell.
     * 
     * @author Nan
     * 
     */
    private class HovmoellerCellArray extends Array1D<HovmoellerCell> {

        private HovmoellerCell[] data;

        public HovmoellerCellArray(int size) {
            super(size);
            data = new HovmoellerCell[size];
        }

        @Override
        public HovmoellerCell get(int... coords) {
            if (coords.length != 1) {
                throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                        + ") for this Array (needs 1)");
            }
            return data[coords[0]];
        }

        @Override
        public void set(HovmoellerCell value, int... coords) {
            if (coords.length != 1) {
                throw new IllegalArgumentException("Wrong number of co-ordinates (" + coords.length
                        + ") for this Array (needs 1)");
            }
            data[coords[0]] = value;
        }
    }

    // all points on a line string
    private List<HorizontalPosition> pointsOnLineString;
    private Array1D<HovmoellerCell> domainObjects;
    private CoordinateReferenceSystem crs;
    private TimeAxis tAxis;

    public HovmoellerDomain(List<HorizontalPosition> pointsOnLineString, TimeAxis tAxis) {
        /*
         * Points on a line string are derived by an external algorithm. We can
         * assume these points' crs is identical as the algorithm can covert
         * different crs into a common crs.
         */
        int numberOfTimeValues = tAxis.size();
        int numberOfPoints = pointsOnLineString.size();

        if (numberOfPoints > 0 && numberOfTimeValues > 0) {
            crs = pointsOnLineString.get(0).getCoordinateReferenceSystem();
            this.pointsOnLineString = pointsOnLineString;
            this.tAxis = tAxis;

            domainObjects = new HovmoellerCellArray(numberOfTimeValues * numberOfPoints);

            for (int i = 0; i < numberOfPoints; i++) {
                for (int j = 0; j < numberOfTimeValues; j++) {
                    // they are horizontal points so z values is set to null
                    domainObjects.set(
                            new HovmoellerCell(pointsOnLineString.get(i), tAxis
                                    .getCoordinateBounds(j)), j + i * numberOfPoints);
                }
            }
        } else {
            crs = null;
            this.pointsOnLineString = null;
            domainObjects = null;
        }

    }

    public boolean contains(GeoPosition p) {
        for (int i = 0; i < domainObjects.size(); i++) {
            if (domainObjects.get(i).horizontalPosition.equals(p.getHorizontalPosition())
                    && domainObjects.get(i).timeExtent.contains(p.getTime())) {
                return true;
            }
        }
        return false;
    }

    public Array1D<HovmoellerCell> getDomainObjects() {
        return domainObjects;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HovmoellerDomain other = (HovmoellerDomain) obj;
        if (pointsOnLineString == null) {
            if (other.pointsOnLineString != null)
                return false;
        } else if (!pointsOnLineString.equals(other.pointsOnLineString))
            return false;
        if (domainObjects == null) {
            if (other.domainObjects != null)
                return false;
        } else if (!domainObjects.equals(other.domainObjects))
            return false;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (tAxis == null) {
            if (other.tAxis != null)
                return false;
        } else if (!tAxis.equals(other.tAxis))
            return false;
        return true;
    }

    public int hashcode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((pointsOnLineString == null) ? 0 : pointsOnLineString.hashCode());
        result = prime * result + ((domainObjects == null) ? 0 : domainObjects.hashCode());
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
        result = prime * result + ((tAxis == null) ? 0 : tAxis.hashCode());
        return result;
    }

    public List<HorizontalPosition> getlPointsOnLineString() {
        return pointsOnLineString;
    }

    public TimeAxis getTimeAxis() {
        return tAxis;
    }
}
