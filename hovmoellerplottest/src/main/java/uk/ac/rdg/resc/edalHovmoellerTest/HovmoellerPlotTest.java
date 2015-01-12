package uk.ac.rdg.resc.edalHovmoellerTest;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.dataset.AbstractGridDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.domain.HovmoellerDomain;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.feature.HovmoellerFeature;
import uk.ac.rdg.resc.edal.graphics.Charting;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.util.ColourPalette;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.TimeAxisImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

public class HovmoellerPlotTest {

    public static void main(String[] args) throws IOException, EdalException,
            InvalidLineStringException, InvalidCrsException {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        Chronology chrnology = ISOChronology.getInstance();

        // You can get test files from N-drive/ReSC_data/SST_data
        String location = "/home/yv903893/release_test_data/sst/*";
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();

        long testStart = System.currentTimeMillis();

        AbstractGridDataset dataset = (AbstractGridDataset) datasetFactory.createDataset(
                "testdataset", location);

        // Variable used in this test
        String varId = "sst_anomaly";

        /*
         * This performance tests use up to 12 files; each file contains data
         * for a month of the year. As a result, the time period can vary from
         * one month to twelve months. A fixed test area in the grid is chosen.
         * Four sets of points are used, which contain 500, 1000, 2500, and 5000
         * points. Each test applies one fixed number of points with a fixed
         * number of months. There are 48 test runs.
         */

        // The first date value
        DateTime dt = new DateTime(2010, 01, 5, 10, 00, chrnology);

        // Values on the time axis
        List<DateTime> tAxisValues = new ArrayList<>();
        tAxisValues.add(dt);

        /*
         * Even a test uses one month data, two dates in the same month are
         * needed.
         */
        DateTime dt01 = new DateTime(2010, 01, 29, 10, 00, chrnology);
        // tAxisValues.add(dt01);

        DateTime dt02 = new DateTime(2010, 02, 25, 10, 00, chrnology);
        tAxisValues.add(dt02); // the test will use 2 months data

        DateTime dt03 = new DateTime(2010, 03, 18, 10, 00, chrnology);
        // tAxisValues.add(dt03); // the test will use 3 months data

        DateTime dt04 = new DateTime(2010, 04, 8, 20, 00, chrnology);
        // tAxisValues.add(dt04); // the test will use 4 months data

        DateTime dt05 = new DateTime(2010, 05, 18, 14, 00, chrnology);
        // tAxisValues.add(dt05); // the test will use 5 months data

        DateTime dt06 = new DateTime(2010, 06, 13, 23, 00, chrnology);
        // tAxisValues.add(dt06); // the test will use 6 months data

        DateTime dt07 = new DateTime(2010, 07, 30, 01, 00, chrnology);
        // tAxisValues.add(dt07); // the test will use 7 months data

        DateTime dt08 = new DateTime(2010, 8, 14, 11, 00, chrnology);
        // tAxisValues.add(dt08); // the test will use 8 months data

        DateTime dt09 = new DateTime(2010, 9, 11, 23, 00, chrnology);
        // tAxisValues.add(dt09); // the test will use 9 months data

        DateTime dt10 = new DateTime(2010, 10, 18, 10, 00, chrnology);
        // tAxisValues.add(dt10); // the test will use 10 months data

        DateTime dt11 = new DateTime(2010, 11, 8, 18, 00, chrnology);
        // tAxisValues.add(dt11); // the test will use 11 months data

        DateTime dt12 = new DateTime(2010, 12, 28, 12, 00, chrnology);
        // tAxisValues.add(dt12); // the test will use 12 months data

        TimeAxis t = new TimeAxisImpl("time", tAxisValues);

        List<HorizontalPosition> samplePoints = new ArrayList<>();
        /*
         * an area on the grid is chosen. Given different resolutions, different
         * numbers of points are generated.
         */
        int sampleXsize = 10;
        int sampleYsize = 50;

        for (int i = 0; i < sampleXsize; i++) {
            for (int j = 0; j < sampleYsize; j++) {
                // for 1000 points
                // HorizontalPosition hPos =new HorizontalPosition(-48+0.25*i,
                // 55-j, crs);

                // for 500 points
                HorizontalPosition hPos = new HorizontalPosition(-48 + 0.5 * i, 55 - j, crs);

                // for 2500 points
                // HorizontalPosition hPos =new HorizontalPosition(-48+0.5*i,
                // 55-0.2*j, crs);

                // for 5000 points
                // HorizontalPosition hPos = new HorizontalPosition(-48 + 0.25 *
                // i, 55 - 0.2 * j, crs);
                samplePoints.add(hPos);
            }
        }
        Set<String> ids = new HashSet<String>();
        ids.add(varId);

        HovmoellerDomain domain = new HovmoellerDomain(samplePoints, t);

        HovmoellerFeature hovmoellerFeature = dataset.extractHovmollerFeatures(ids, domain);
        long timeAfterExtractingData = System.currentTimeMillis();
        boolean logarithmic = false;
        float minValue = Charting.getMinValueOfArray2D(hovmoellerFeature.getValues(varId))
                .floatValue();
        float maxValue = Charting.getMaxValueOfArray2D(hovmoellerFeature.getValues(varId))
                .floatValue();

        ColourScale colourscale = new ColourScale(minValue, maxValue, logarithmic);
        // Use the default rainbow colour scheme.
        ColourScheme colourscheme = new SegmentColourScheme(colourscale, Color.blue, Color.red,
                Color.yellow, ColourPalette.DEFAULT_PALETTE_NAME, 60);
        BufferedImage image = Charting.plotHovmoellerFeature(varId, hovmoellerFeature,
                colourscheme, "@ReSC").createBufferedImage(800, 600);

        // Currently, we use naked eyes to check the image.
        try {
            // save result on hard drive
            File outputfile = new File("/home/yv903893/sst500_2.png");
            ImageIO.write(image, "png", outputfile);
            long testEnd = System.currentTimeMillis();
            // Time used for extracting data
            System.out.print(timeAfterExtractingData - testStart);
            System.out.print("   ");
            // Time used for extracting and plotting data
            System.out.println(testEnd - testStart);
        } catch (IOException e) {
            System.out.println("wrong");
        }
    }
}
