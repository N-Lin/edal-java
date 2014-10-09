package uk.ac.rdg.resc.edal.graphics;

import static org.junit.Assert.*;

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
import uk.ac.rdg.resc.edal.feature.HovmoellerFeature;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

public class HovmoellerPlotTest {
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private Chronology chrnology = ISOChronology.getInstance();
    private HovmoellerDomain domain;
    private HovmoellerFeature feature;
    
    @Before
    public void setUp() throws Exception {
        //The number of time values
        int ysize =9;
        //The number of positions values
        int xsize =2;
        
        //A simple line string making up of two points.
        HorizontalPosition h1 =new HorizontalPosition(102.2, 33.6, crs);
        HorizontalPosition h2 =new HorizontalPosition(122.4, 46.8, crs);
        List<HorizontalPosition> lineString = new ArrayList<>();
        lineString.add(h1);
        lineString.add(h2);

        List<DateTime> tAxisValues = new ArrayList<>();

        for (int i = 0; i < ysize; i++) {
            tAxisValues.add(new DateTime(1994, 02, 02 +i, 12, 00, chrnology));
        }
        TimeAxis t = new TimeAxisImpl("time", tAxisValues);  
        
        domain =new HovmoellerDomain(lineString, t);
        
        Array2D<Number> data =new ValuesArray2D(ysize, xsize);
        //Inject dummy data
        for(int i=0; i<xsize; i++){
            float fnumber =10.0f+ i* 20.0f;
            
            for(int j=0; j<ysize; j++){
                data.set(fnumber +j*1.0f, j, i) ;
            }
        }
        
        Map<String, Parameter> parameters =new HashMap<>();
        parameters.put("vLon", null);
        Map<String, Array2D<Number>> values =new HashMap<>();
        values.put("vLon", data);
        feature =new HovmoellerFeature("test", "test", "test", domain, parameters, values);
    }

    @Test
    public void test() {
        BufferedImage image =Charting.plotHovmoellerFeature("vLon", feature);
        //Currently, we use naked eyes to check the image.
        try {
            // retrieve image
            File outputfile = new File("/home/yv903893/test.png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            System.out.println("wrong");
        }
    }

}
