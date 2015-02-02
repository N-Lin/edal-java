package uk.ac.rdg.resc.rastersparql;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;

public class ClipperTest {
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    /*
     * Given a subject regular gird, put the clip bounding box onto nine
     * positions to test if the method works right.
     */
    @Test
    public void testClip() {
        BoundingBox bbox = new BoundingBoxImpl(105.0, 22.3, 110.0, 24.8, crs);
        BoundingBox subject = new BoundingBoxImpl(100, 20, 130.0, 50.0, crs);

        RegularGrid grid = new RegularGridImpl(subject, 60, 60);

        Array1D<GridCell2D> clippings = Clipper.clip(grid, bbox);
        /*
         * Since GridCell2D with related classes do not implement hash code and
         * equals methods, we have to print out the results comparing with the
         * expected results manually.
         */
        for (GridCell2D cell : clippings) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }
        bbox = new BoundingBoxImpl(65.0, 22.3, 90.0, 24.8, crs);
        assertNull(Clipper.clip(grid, bbox));

        bbox = new BoundingBoxImpl(65.0, 10.3, 90.0, 18.8, crs);
        assertNull(Clipper.clip(grid, bbox));

        bbox = new BoundingBoxImpl(11.0, 10.3, 150.0, 18.8, crs);
        assertNull(Clipper.clip(grid, bbox));

        // case 1
        bbox = new BoundingBoxImpl(90.0, 22.3, 112.0, 24.8, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 2
        bbox = new BoundingBoxImpl(105.0, 12.3, 110.0, 24.8, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 3
        bbox = new BoundingBoxImpl(90.0, 12.3, 135.0, 55, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 4
        bbox = new BoundingBoxImpl(125.0, 12.3, 135.0, 24.8, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 5
        bbox = new BoundingBoxImpl(95.0, 45.2, 105.0, 60.3, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 6
        bbox = new BoundingBoxImpl(108.0, 45.2, 110.0, 60.3, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 7
        bbox = new BoundingBoxImpl(128.0, 45.2, 140.0, 60.3, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 8
        bbox = new BoundingBoxImpl(90.0, 25.0, 110.0, 27.0, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }

        // case 9
        bbox = new BoundingBoxImpl(128.0, 25.0, 140.0, 27.0, crs);
        for (GridCell2D cell : Clipper.clip(grid, bbox)) {
            System.out.println(cell.getGridCoordinates().getX() + "  "
                    + cell.getGridCoordinates().getY());
        }
    }

    @Test
    public void testSutherlandHodgmanAlgo() {
        HorizontalPosition subjectLeftLow = new HorizontalPosition(30.0, -10.0, crs);
        HorizontalPosition subjectLeftHigh = new HorizontalPosition(30.0, 50.0, crs);
        HorizontalPosition subjectRightLow = new HorizontalPosition(70.0, -10.0, crs);
        HorizontalPosition subjectRightHigh = new HorizontalPosition(70.0, 50.0, crs);
        List<HorizontalPosition> grid = new ArrayList<>();
        grid.add(subjectLeftLow);
        grid.add(subjectRightLow);
        grid.add(subjectRightHigh);
        grid.add(subjectLeftHigh);
        PolygonImpl subject = new PolygonImpl(grid);

        HorizontalPosition clipperLeftLow = new HorizontalPosition(10.0, 0.0, crs);
        HorizontalPosition clipperLeftHigh = new HorizontalPosition(10.0, 80.0, crs);
        HorizontalPosition clipperRightLow = new HorizontalPosition(40.0, 0.0, crs);
        HorizontalPosition clipperRightHigh = new HorizontalPosition(40.0, 80.0, crs);
        List<HorizontalPosition> clippergrid = new ArrayList<>();
        clippergrid.add(clipperLeftLow);
        clippergrid.add(clipperRightLow);
        clippergrid.add(clipperRightHigh);
        clippergrid.add(clipperLeftHigh);
        PolygonImpl clipper = new PolygonImpl(clippergrid);

        for (HorizontalPosition hPos : Clipper.SutherlandHodgmanAlgo(subject, clipper)
                .getVertices()) {
            /*
             * We print the calculation result out comparing with the known
             * result. Equals method implementation in Polygon is quite tricky.
             */

            System.out.println(hPos.getX() + "  " + hPos.getY());
        }

        double[] xPoints = { 50, 200, 350, 350, 250, 200, 150, 100, 100 };
        double[] yPoints = { 150, 50, 150, 300, 300, 250, 350, 250, 200 };
        subject = new PolygonImpl(xPoints, yPoints, crs);

        double[] clipXPoints = { 100.0, 300.0, 300.0, 100.0 };
        double[] clipYPoints = { 100.0, 100.0, 300.0, 300.0 };
        clipper = new PolygonImpl(clipXPoints, clipYPoints, crs);

        for (HorizontalPosition hPos : Clipper.SutherlandHodgmanAlgo(subject, clipper)
                .getVertices()) {
            System.out.println(hPos.getX() + "  " + hPos.getY());
        }
    }
}
