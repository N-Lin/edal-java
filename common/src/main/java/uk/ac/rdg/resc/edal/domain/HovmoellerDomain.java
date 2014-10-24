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

import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.domain.HovmoellerDomain.HovmoellerCell;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.util.Array2D;

/**
 * A domain for measurement of a time series along points on a line string.
 * 
 * @author Nan
 */
public class HovmoellerDomain implements DiscreteDomain<GeoPosition, HovmoellerCell> {

    /**
     * An entity in a Hovmoeller domain.
     * 
     */
    public static class HovmoellerCell {
        public final HorizontalPosition horizontalPosition;
        public final Extent<DateTime> timeExtent;

        HovmoellerCell(HorizontalPosition hPos, Extent<DateTime> tExtent) {
            horizontalPosition = hPos;
            timeExtent = tExtent;
        }
    }

    // all points on a line string
    private List<HorizontalPosition> pointsOnLineString;
    private Array2D<HovmoellerCell> domainObjects;
    private CoordinateReferenceSystem crs;
    private TimeAxis tAxis;

    public HovmoellerDomain(List<HorizontalPosition> pointsOnLineString, TimeAxis tAxis) {
        /*
         * Points on a line string are derived by an external algorithm. We can
         * assume these points' crs is identical as the algorithm can covert
         * different crs into a common crs.
         */
        final int numberOfTimeValues = tAxis.size();
        final int numberOfPoints = pointsOnLineString.size();

        if (numberOfPoints > 0 && numberOfTimeValues > 0) {
            crs = pointsOnLineString.get(0).getCoordinateReferenceSystem();
            this.pointsOnLineString = pointsOnLineString;
            this.tAxis = tAxis;

            domainObjects = new Array2D<HovmoellerCell>(numberOfTimeValues, numberOfPoints) {
                private HovmoellerCell[][] data = new HovmoellerCell[numberOfTimeValues][numberOfPoints];

                @Override
                public HovmoellerCell get(int... coords) {
                    if (coords.length != 2) {
                        throw new IllegalArgumentException("Wrong number of co-ordinates ("
                                + coords.length + ") for this Array (needs 2)");
                    }
                    return data[coords[Y_IND]][coords[X_IND]];
                }

                @Override
                public void set(HovmoellerCell value, int... coords) {
                    if (coords.length != 2) {
                        throw new IllegalArgumentException("Wrong number of co-ordinates ("
                                + coords.length + ") for this Array (needs 2)");
                    }
                    data[coords[Y_IND]][coords[X_IND]] = value;
                }
            };

            for (int i = 0; i < numberOfPoints; i++) {
                for (int j = 0; j < numberOfTimeValues; j++) {
                    domainObjects.set(
                            new HovmoellerCell(pointsOnLineString.get(i), tAxis
                                    .getCoordinateBounds(j)), j, i);
                }
            }
        } else {
            crs = null;
            this.pointsOnLineString = null;
            domainObjects = null;
        }

    }

    /**
     * Test if the Hovmoeller domain contains a given {@link GeoPosition}.
     * 
     * @param p
     *            A given GeoPosition.
     * @return true if the Hovmoeller domain contains the GeoPosition otherwise
     *         false.
     */
    public boolean contains(GeoPosition p) {
        for (HovmoellerCell cell : domainObjects) {
            if (cell.horizontalPosition.equals(p.getHorizontalPosition())
                    && cell.timeExtent.contains(p.getTime())) {
                return true;
            }
        }
        return false;
    }

    public Array2D<HovmoellerCell> getDomainObjects() {
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

    public int getNumberOfPoints() {
        return pointsOnLineString.size();
    }

    public int getNumberOfTimes() {
        return tAxis.size();
    }

    /**
     * Get the line string in the Hovmoeller domain.
     * 
     * @return An object of {@link LineString}.
     */
    public LineString getLineString() {
        if (pointsOnLineString == null || pointsOnLineString.size() == 0) {
            return null;
        } else {
            StringBuilder pointsString = new StringBuilder();
            for (HorizontalPosition pos : pointsOnLineString) {
                pointsString.append(pos.getX() + " " + pos.getY() + ",");
            }
            // delete the last unnecessary ","
            pointsString.deleteCharAt(pointsString.length() - 1);
            try {
                return new LineString(pointsString.toString(), crs);
            } catch (InvalidLineStringException ie) {
                System.out.println("Encounter an invalid line string.");
                ie.printStackTrace();
                return null;
            } catch (InvalidCrsException e) {
                System.out.println("Not a valid CRS!");
                return null;
            }
        }
    }
}
