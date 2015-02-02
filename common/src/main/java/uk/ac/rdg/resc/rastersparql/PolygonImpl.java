/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
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

package uk.ac.rdg.resc.rastersparql;

import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.AbstractPolygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * An implementation of {@link Polygon} based on {@link AbstractPolygon}.
 * 
 * @author Nan Lin
 * 
 */
public class PolygonImpl extends AbstractPolygon {
    /*
     * The data are stored in two forms: one is its x and y positions; the other
     * one is in a list of horizontal positions. The former one is easy to do
     * calculation but it needs extra field member to store the coordinate
     * reference system.
     */
    private double[] xPoints;
    private double[] yPoints;
    // The second form of Polygon
    private List<HorizontalPosition> vertices;
    private CoordinateReferenceSystem crs;

    /**
     * The constructor to create a polygon.
     * 
     * @param xPoints
     *            x positions of the polygon in the coordinate reference system.
     * @param yPoints
     *            y positions of the polygon in the coordinate reference system.
     * @param crs
     *            The coordinate reference system the polygon applies.
     */
    public PolygonImpl(double[] xPoints, double[] yPoints, CoordinateReferenceSystem crs) {
        if (xPoints.length != yPoints.length) {
            throw new IllegalArgumentException(
                    "The lengths of xPoints and yPoints arrays must be equal.");
        } else {
            this.crs = crs;
            this.xPoints = xPoints;
            this.yPoints = yPoints;
            vertices = new ArrayList<>();
            for (int i = 0; i < xPoints.length; i++) {
                vertices.add(new HorizontalPosition(xPoints[i], yPoints[i], crs));
            }
        }
    }

    /**
     * The constructor to create a polygon.
     * 
     * @param vertices
     *            Each vertices of the polygon.
     */
    public PolygonImpl(List<HorizontalPosition> vertices) {
        this.crs = vertices.get(0).getCoordinateReferenceSystem();
        this.vertices = vertices;
        int numberOfPoints = vertices.size();
        xPoints = new double[numberOfPoints];
        yPoints = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            xPoints[i] = vertices.get(i).getX();
            yPoints[i] = vertices.get(i).getY();
        }
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Test if the given {@link HorizontalPosition} is inside the polygon.
     * 
     * @param hPos
     *            The horizontal position to be test.
     * @return True if the position is inside the polygon. Otherwise false.
     */
    public boolean containPoints(HorizontalPosition hPos) {
        if (hPos == null) {
            return false;
        }
        /*
         * The test position should apply the identical coordinate reference
         * system the polygon applies. Otherwise convert it.
         */
        if (!crs.equals(hPos.getCoordinateReferenceSystem())) {
            hPos = GISUtils.transformPosition(hPos, crs);
        }
        /*
         * Points on the boundaries are neither inside nor outside the polygon. The
         * link below discuss the algorithm.
         * http://stackoverflow.com/questions/217578/
         * point-in-polygon-aka-hit-test/2922778#2922778
         */
        int i = 0;
        int j = 0;
        HorizontalPosition[] points = vertices.toArray(new HorizontalPosition[0]);
        int numberOfVertices = vertices.size();
        boolean insidePolygon = false;
        for (i = 0, j = numberOfVertices - 1; i < numberOfVertices; j = i++) {
            double testX = hPos.getX();
            double testY = hPos.getY();
            if (((points[i].getY() > testY) != (points[j].getY() > testY))
                    && (testX < (points[j].getX() - points[i].getX()) * (testY - points[i].getY())
                            / (points[j].getY() - points[i].getY()) + points[i].getX())) {
                ;
                insidePolygon = !insidePolygon;
            }
        }
        return insidePolygon;
    }

    public List<HorizontalPosition> getVertices() {
        return vertices;
    }

    public double[] getXPoints() {
        return xPoints;
    }

    public double[] getYPoints() {
        return yPoints;
    }
}
