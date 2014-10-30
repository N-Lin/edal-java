package uk.ac.rdg.resc.edal.graphics;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.HovmoellerDomain;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.feature.HovmoellerFeature;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;

public class HovmoellerPlotTest {
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private Chronology chrnology = ISOChronology.getInstance();
    private HovmoellerDomain domain;
    private HovmoellerFeature feature;

    @Before
    public void setUp() throws Exception {
        // The number of time values

        int gridXsize = 3;
        int gridYsize = 1;
        // The number of positions values
        int hovmoellerYsize = 5;
        int hovmoellerXsize = gridXsize * gridYsize;
        //int hovmoellerXsize = 3;

        List<HorizontalPosition> lineString = new ArrayList<>();
        
        HorizontalPosition h1 = new HorizontalPosition(100.0, 30.0, crs);
        lineString.add(h1);
        
        HorizontalPosition h2 = new HorizontalPosition(102.0, 30.0, crs);
        lineString.add(h2);
        
        HorizontalPosition h3 = new HorizontalPosition(105.0, 30.0, crs);
        lineString.add(h3);
        
        // A simple line string making up of two points.
        /*for (int i = 0; i < gridXsize; i++) {
            for (int j = 0; j < gridYsize; j++) {
                HorizontalPosition hPos = new HorizontalPosition(100.0 + i * 2.0 , 30 + j * 1.0 +i, crs);
                lineString.add(hPos);
            }
        }*/

        List<DateTime> tAxisValues = new ArrayList<>();

        /*for (int i = 0; i < hovmoellerYsize; i++) {
            tAxisValues.add(new DateTime(1994, 02, 02 + i, 12, 00, chrnology));
        }*/
        tAxisValues.add(new DateTime(1994, 02, 02 , 12, 00, chrnology));
        tAxisValues.add(new DateTime(1994, 02, 03 , 12, 00, chrnology));
        tAxisValues.add(new DateTime(1994, 02, 05 , 12, 00, chrnology));
        tAxisValues.add(new DateTime(1994, 02, 12 , 12, 00, chrnology));
        tAxisValues.add(new DateTime(1994, 02, 22 , 12, 00, chrnology));
        TimeAxis t = new TimeAxisImpl("time", tAxisValues);

        domain = new HovmoellerDomain(lineString, t);

        Array2D<Number> data = new ValuesArray2D(hovmoellerYsize, hovmoellerXsize);
        // Inject dummy data
        for (int i = 0; i < hovmoellerXsize; i++) {
            float fnumber = 25.0f + i * 20.0f;

            for (int j = 0; j < hovmoellerYsize; j++) {
                data.set(fnumber + j * 8.0f, j, i);
            }
        }
        //data.set(null, 3, 4);
        Map<String, Parameter> parameters = new HashMap<>();
        Parameter degree =new Parameter("101", "temp degree", "Temperature", "degree", "Temperature");
        parameters.put("vLon", degree);
        Map<String, Array2D<Number>> values = new HashMap<>();
        values.put("vLon", data);
        feature = new HovmoellerFeature("test", "test", "test", domain, parameters, values);
    }

    @Test
    public void test() throws InvalidLineStringException, InvalidCrsException{
        boolean logarithmic = false;
        ColourScale colourscale = new ColourScale(25.0f, 120.0f, logarithmic);
        // Use the default rainbow colour scheme.
        ColourScheme colourscheme = new SegmentColourScheme(colourscale, Color.blue, Color.red,
                Color.yellow, ColourPalette.DEFAULT_PALETTE_NAME, 60);
        BufferedImage image = Charting.plotHovmoellerFeature("vLon", feature, colourscheme, "@ReSC").createBufferedImage(800,  600);
        // Currently, we use naked eyes to check the image.
        try {
            // retrieve image
            File outputfile = new File("/home/yv903893/test.png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            System.out.println("wrong");
        }
    }

}
