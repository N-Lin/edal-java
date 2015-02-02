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

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;

public final class Clipper {
    /**
     * Clips the given HorizontalGrid to the given polygon.
     * 
     * @param grid
     *            The grid to clip
     * @param polygon
     *            The polygon that is clipping the grid
     * @return a {@link Array1D} of grid cells from {@code grid} that fall
     *         within the given polygon. We consider a grid cell to be within a
     *         polygon if its central point is within the polygon.
     * @see GridCell2D#getCentre()
     */
    /*
     * The implementation assumes grid is a regular grid, polygon is a
     * rectangle.
     */
    public static Array1D<GridCell2D> clip(HorizontalGrid grid, Polygon polygon) {
        RegularGrid rGrid = (RegularGrid) grid;
        BoundingBox bbox = (BoundingBox) polygon;
        double minX = bbox.getMinX();
        double minY = bbox.getMinY();
        double maxX = bbox.getMaxX();
        double maxY = bbox.getMaxY();

        double gridMinX = rGrid.getBoundingBox().getMinX();
        double gridMinY = rGrid.getBoundingBox().getMinY();
        double gridMaxX = rGrid.getBoundingBox().getMaxX();
        double gridMaxY = rGrid.getBoundingBox().getMaxY();

        // If bbox not intersect with rGrid, return null.
        if (maxX < gridMinX || gridMaxX < minX || maxY < gridMinY || gridMaxY < minY) {
            return null;
        } else {
            int searchXfrom = rGrid.getXAxis().findIndexOf(minX);
            if (searchXfrom < 0) {
                searchXfrom = 0;
            }
            int searchXTo = rGrid.getXAxis().findIndexOf(maxX);
            if (searchXTo < 0) {
                searchXTo = rGrid.getXSize() - 1;
            }
            int searchYfrom = rGrid.getYAxis().findIndexOf(minY);
            if (searchYfrom < 0) {
                searchYfrom = 0;
            }
            int searchYTo = rGrid.getYAxis().findIndexOf(maxY);
            if (searchYTo < 0) {
                searchYTo = rGrid.getYSize() - 1;
            }

            Array<GridCell2D> domainObjects = rGrid.getDomainObjects();
            /*
             * ids store the grid cell ids whose central positions are inside
             * the polygon.
             */
            List<GridCell2D> ids = new ArrayList<>();

            for (int i = searchXfrom; i < searchXTo + 1; i++) {
                for (int j = searchYfrom; j < searchYTo + 1; j++) {
                    GridCell2D cell = domainObjects.get(j, i);
                    if (bbox.contains(cell.getCentre())) {
                        ids.add(cell);
                    }
                }
            }
            return new ImmutableArray1D<>(ids.toArray(new GridCell2D[0]));
        }
    }

    /**
     * Implementation of Sutherland Hodgman Algorithm, a general method to get
     * the result when a polygon clips another polygon.
     * 
     * @param grid
     *            the subject polygon
     * @param polygon
     *            the clip polygon
     * @return a polygon that is the intersection of the subject and clip
     *         polygon
     */
    public static PolygonImpl SutherlandHodgmanAlgo(PolygonImpl grid, PolygonImpl polygon) {
        List<HorizontalPosition> subject = new ArrayList<>(grid.getVertices());
        List<HorizontalPosition> result = new ArrayList<>(subject);
        List<HorizontalPosition> clipper = new ArrayList<>(polygon.getVertices());

        int clipperLength = clipper.size();
        for (int i = 0; i < clipperLength; i++) {
            int resultSize = result.size();
            List<HorizontalPosition> input = result;
            result = new ArrayList<>(resultSize);

            HorizontalPosition clipLineEnd = clipper.get((i + clipperLength - 1) % clipperLength);
            HorizontalPosition clipLineHead = clipper.get(i);
            for (int j = 0; j < resultSize; j++) {
                HorizontalPosition subjectLineEnd = input.get((j + resultSize - 1) % resultSize);
                HorizontalPosition subjectLineHead = input.get(j);

                if (isInside(clipLineEnd, clipLineHead, subjectLineHead)) {
                    if (!isInside(clipLineEnd, clipLineHead, subjectLineEnd))
                        result.add(intersection(clipLineEnd, clipLineHead, subjectLineEnd,
                                subjectLineHead));
                    result.add(subjectLineHead);
                } else if (isInside(clipLineEnd, clipLineHead, subjectLineEnd)) {
                    result.add(intersection(clipLineEnd, clipLineHead, subjectLineEnd,
                            subjectLineHead));
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        } else {
            return new PolygonImpl(result);
        }
    }

    /**
     * Judge if a given position is inside of a line segment of the clip
     * polygon.
     * 
     * @param a
     *            The end of a line segment of the clip polygon.
     * @param b
     *            The head of a line segment of the clip polygon.
     * @param c
     *            A position on the subject polygon.
     * @return
     */
    private static boolean isInside(HorizontalPosition a, HorizontalPosition b, HorizontalPosition c) {
        return (a.getX() - c.getX()) * (b.getY() - c.getY()) > (a.getY() - c.getY())
                * (b.getX() - c.getX());
    }

    /**
     * Get the intersection of two lines A and B.
     * 
     * @param a
     *            The end of line segment A.
     * @param b
     *            The head of line segment A.
     * @param p
     *            The end of line segment B.
     * @param q
     *            The head of line segment B.
     * @return The intersection points of lines A and B.
     */
    private static HorizontalPosition intersection(HorizontalPosition a, HorizontalPosition b,
            HorizontalPosition p, HorizontalPosition q) {
        double A1 = b.getY() - a.getY();
        double B1 = a.getX() - b.getX();
        double C1 = A1 * a.getX() + B1 * a.getY();

        double A2 = q.getY() - p.getY();
        double B2 = p.getX() - q.getX();
        double C2 = A2 * p.getX() + B2 * p.getY();

        double det = A1 * B2 - A2 * B1;
        double x = (B2 * C1 - B1 * C2) / det;
        double y = (A1 * C2 - A2 * C1) / det;

        return new HorizontalPosition(x, y, a.getCoordinateReferenceSystem());
    }
}
