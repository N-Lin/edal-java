package uk.ac.rdg.resc.rastersparql;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class PolygonImplTest {
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    /*
     * In the following test cases, the polygon and the positions to be tested
     * use the same coordinate reference system. Other tests have tested the
     * cases if they use different systems.
     */
    @Test
    public void testContainPoints() {
        double[] rectangleXPoints = { 10.0, 10.0, 20.0, 20.0 };
        double[] rectangleyPoints = { 5.0, 10.0, 10.0, 5.0 };
        PolygonImpl polygon = new PolygonImpl(rectangleXPoints, rectangleyPoints, crs);
        assertTrue(polygon.containPoints(new HorizontalPosition(10.0, 8.0, crs)));
        assertTrue(polygon.containPoints(new HorizontalPosition(10.0, 5.0, crs)));
        assertFalse(polygon.containPoints(new HorizontalPosition(5.0, 10.0, crs)));
        assertFalse(polygon.containPoints(new HorizontalPosition(100.9, 34.0, crs)));
        assertTrue(polygon.containPoints(new HorizontalPosition(15.0, 7.0, crs)));
        assertTrue(polygon.contains(18.0, 5.0));
        assertTrue(polygon.contains(15.0, 7.0));

        List<HorizontalPosition> vertices = new ArrayList<>();
        vertices.add(new HorizontalPosition(0.0, 100.0, crs));
        vertices.add(new HorizontalPosition(30.0, 0.0, crs));
        vertices.add(new HorizontalPosition(80.0, 20.0, crs));
        vertices.add(new HorizontalPosition(100.0, 120.0, crs));
        vertices.add(new HorizontalPosition(60.0, 130.0, crs));
        polygon = new PolygonImpl(vertices);
        assertFalse(polygon.containPoints(new HorizontalPosition(10.0, 10.0, crs)));
        assertTrue(polygon.containPoints(new HorizontalPosition(20.0, 90.0, crs)));
        assertFalse(polygon.containPoints(new HorizontalPosition(70.0, 10.0, crs)));
        assertFalse(polygon.containPoints(new HorizontalPosition(80.0, 125.0, crs)));
        assertTrue(polygon.containPoints(new HorizontalPosition(70.0, 60.0, crs)));
        assertTrue(polygon.containPoints(new HorizontalPosition(40.0, 50.0, crs)));
    }

}
