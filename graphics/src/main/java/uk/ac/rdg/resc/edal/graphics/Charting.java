/**
 * Copyright (c) 2013, 2014 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.InvalidCrsException;
import uk.ac.rdg.resc.edal.exceptions.InvalidLineStringException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.HovmoellerFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.util.ValuesArray2D;

/**
 * Code to produce various types of chart.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 * @author Kevin X. Yang
 */
final public class Charting {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Locale US_LOCALE = new Locale("us", "US");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(4);
    }

    public static JFreeChart createVerticalProfilePlot(
            Collection<? extends ProfileFeature> features, HorizontalPosition hPos)
            throws MismatchedCrsException {
        XYSeriesCollection xySeriesColl = new XYSeriesCollection();

        Set<String> plottedVarList = new HashSet<String>();
        String xAxisLabel = "";
        VerticalCrs vCrs = null;
        boolean invertYAxis = false;
        for (ProfileFeature feature : features) {
            if (vCrs == null) {
                vCrs = feature.getDomain().getVerticalCrs();
            } else {
                if (!vCrs.equals(feature.getDomain().getVerticalCrs())) {
                    throw new MismatchedCrsException(
                            "All vertical CRSs must match to plot multiple profile plots");
                }
            }
            for (String varId : feature.getParameterIds()) {
                plottedVarList.add(varId);
                List<Double> elevationValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                String legend = feature.getName() + "("
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getX()) + ","
                        + NUMBER_FORMAT.format(feature.getHorizontalPosition().getY()) + ") - "
                        + TimeUtils.formatUtcHumanReadableDateTime(feature.getTime());
                XYSeries series = new XYSeries(legend, true);
                series.setDescription(feature.getParameter(varId).getDescription());
                for (int i = 0; i < elevationValues.size(); i++) {
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.add(elevationValues.get(i), val);
                }

                xAxisLabel = getAxisLabel(feature, varId);

                xySeriesColl.addSeries(series);
            }
        }

        NumberAxis elevationAxis = getZAxis(vCrs);
        elevationAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (invertYAxis) {
            elevationAxis.setInverted(true);
        }
        elevationAxis.setAutoRangeIncludesZero(false);
        elevationAxis.setNumberFormatOverride(NUMBER_FORMAT);

        NumberAxis valueAxis = new NumberAxis(xAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);
        valueAxis.setNumberFormatOverride(NUMBER_FORMAT);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < features.size(); i++) {
            renderer.setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            renderer.setSeriesShapesVisible(i, true);
        }

        XYPlot plot = new XYPlot(xySeriesColl, elevationAxis, valueAxis, renderer);
        plot.setNoDataMessage("There is no data for your choice");
        plot.setNoDataMessageFont(new Font("sansserif", Font.BOLD, 20));
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        StringBuilder title = new StringBuilder();

        if (plottedVarList.size() > 0) {
            StringBuilder varList = new StringBuilder();
            for (String varId : plottedVarList) {
                varList.append(varId);
                varList.append(", ");
            }
            varList.delete(varList.length() - 2, varList.length() - 1);
            title.append("Vertical profile of ");
            if (plottedVarList.size() > 1) {
                title.append(" variables: ");
            }
            title.append(varList.toString());
        } else {
            title.append("No data to plot at ");
            title.append(hPos.toString());
        }

        /*
         * Use default font and create a legend if there are multiple lines
         */
        return new JFreeChart(title.toString(), null, plot, xySeriesColl.getSeriesCount() > 1);
    }

    /**
     * Creates a vertical axis for plotting the given elevation values from the
     * given layer
     */
    private static NumberAxis getZAxis(VerticalCrs vCrs) {
        /*
         * We can deal with three types of vertical axis: Height, Depth and
         * Pressure. The code for this is very messy in ncWMS, sorry about
         * that... We should improve this but there are possible knock-on
         * effects, so it's not a very easy job.
         */
        final String zAxisLabel;
        final boolean invertYAxis;
        if (vCrs != null && vCrs.isPositiveUpwards()) {
            zAxisLabel = "Height";
            invertYAxis = false;
        } else if (vCrs != null && vCrs.isPressure()) {
            zAxisLabel = "Pressure";
            invertYAxis = true;
        } else {
            zAxisLabel = "Depth";
            invertYAxis = true;
        }

        String units = "";
        if (vCrs != null) {
            units = " (" + vCrs.getUnits() + ")";
        }
        NumberAxis zAxis = new NumberAxis(zAxisLabel + units);
        zAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        zAxis.setInverted(invertYAxis);

        return zAxis;
    }

    public static JFreeChart createTimeSeriesPlot(
            Collection<? extends PointSeriesFeature> features, HorizontalPosition hPos)
            throws MismatchedCrsException {

        Chronology chronology = null;
        List<String> plottedVarList = new ArrayList<String>();
        Map<String, TimeSeriesCollection> phenomena2timeseries = new HashMap<String, TimeSeriesCollection>();
        for (PointSeriesFeature feature : features) {
            if (chronology == null) {
                chronology = feature.getDomain().getChronology();
            } else {
                if (!chronology.equals(feature.getDomain().getChronology())) {
                    throw new MismatchedCrsException(
                            "All chronologies must match to plot multiple time series plots");
                }
            }
            for (String varId : feature.getParameterIds()) {
                Parameter parameter = feature.getParameter(varId);
                TimeSeriesCollection collection;
                String phenomena = parameter.getStandardName() + " (" + parameter.getUnits() + ")";
                if (phenomena2timeseries.containsKey(phenomena)) {
                    collection = phenomena2timeseries.get(phenomena);
                } else {
                    collection = new TimeSeriesCollection();
                    phenomena2timeseries.put(phenomena, collection);
                }
                plottedVarList.add(varId);
                List<DateTime> timeValues = feature.getDomain().getCoordinateValues();

                /*
                 * This is the label used for the legend.
                 */
                TimeSeries series = new TimeSeries(parameter.getTitle());
                series.setDescription(feature.getParameter(varId).getDescription());
                for (int i = 0; i < timeValues.size(); i++) {
                    Number val = feature.getValues(varId).get(i);
                    if (val == null || Double.isNaN(val.doubleValue())) {
                        /*
                         * Don't add NaNs to the series
                         */
                        continue;
                    }
                    series.addOrUpdate(new Millisecond(new Date(timeValues.get(i).getMillis())),
                            val);
                }

                collection.addSeries(series);
            }
        }

        StringBuilder title = new StringBuilder();
        if (plottedVarList.size() > 0) {
            StringBuilder varList = new StringBuilder();
            for (String varId : plottedVarList) {
                varList.append(varId);
                varList.append(", ");
            }
            varList.delete(varList.length() - 2, varList.length() - 1);
            title.append("Time series of ");
            if (plottedVarList.size() > 1) {
                title.append("variables: ");
            }
            title.append(varList.toString());
        } else {
            title.append("No data to plot at ");
            title.append(hPos.toString());
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(title.toString(), "Date / time",
                null, null, true, false, false);

        XYPlot plot = chart.getXYPlot();

        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        timeAxis.setAutoRangeIncludesZero(false);

        int i = 0;
        boolean legendNeeded = false;
        for (Entry<String, TimeSeriesCollection> entry : phenomena2timeseries.entrySet()) {
            if (i > 0) {
                legendNeeded = true;
            }
            TimeSeriesCollection coll = entry.getValue();
            NumberAxis valueAxis = new NumberAxis();
            valueAxis.setAutoRangeIncludesZero(false);
            valueAxis.setAutoRange(true);
            valueAxis.setNumberFormatOverride(NUMBER_FORMAT);
            valueAxis.setLabel(entry.getKey());
            plot.setDataset(i, coll);
            plot.setRangeAxis(i, valueAxis);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            for (int j = 0; j < coll.getSeriesCount(); j++) {
                renderer.setSeriesShape(j, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
                renderer.setSeriesShapesVisible(j, true);
                if (j > 0) {
                    legendNeeded = true;
                }
            }
            plot.setRenderer(i, renderer);
            plot.mapDatasetToRangeAxis(i, i);
            i++;
        }

        plot.setNoDataMessage("There is no data for your choice");
        plot.setNoDataMessageFont(new Font("sansserif", Font.BOLD, 20));
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOrientation(PlotOrientation.VERTICAL);

        /*
         * Use default font and create a legend if there are multiple lines
         */
        return new JFreeChart(title.toString(), null, plot, legendNeeded);
    }

    /**
     * Creates a plot of {@link TrajectoryFeature}s which have been extracted
     * along a transect.
     * 
     * All {@link TrajectoryFeature}s must have been extracted along the same
     * {@link LineString} for this graph to be correctly displayed.
     * 
     * @param features
     *            A {@link List} of {@link TrajectoryFeature}s to plot
     * @param transectDomain
     *            The transect domain along which *all* features must have been
     *            extracted.
     * @param hasVerticalAxis
     * @param copyrightStatement
     *            A copyright notice to display under the graph
     * @return The plot
     */
    public static JFreeChart createTransectPlot(List<TrajectoryFeature> features,
            LineString transectDomain, boolean hasVerticalAxis, String copyrightStatement) {
        JFreeChart chart;
        XYPlot plot;

        XYSeriesCollection xySeriesColl = new XYSeriesCollection();

        StringBuilder title = new StringBuilder("Trajectory plot of ");
        StringBuilder yLabel = new StringBuilder();
        int size = 0;
        boolean multiplePlots = false;
        if (features.size() > 1) {
            multiplePlots = true;
        }
        for (TrajectoryFeature feature : features) {
            if (feature.getParameterIds().size() > 1) {
                multiplePlots = true;
            }
            for (String paramId : feature.getParameterIds()) {
                XYSeries series = new XYSeries(feature.getName() + ":" + paramId, true);
                double k = 0;
                Array1D<Number> values = feature.getValues(paramId);
                size = (int) values.size();
                for (int i = 0; i < size; i++) {
                    series.add(k++, values.get(i));
                }

                xySeriesColl.addSeries(series);
                yLabel.append(getAxisLabel(feature, paramId));
                yLabel.append("; ");
                title.append(paramId);
                title.append(", ");
            }
        }
        if (yLabel.length() > 1) {
            yLabel.deleteCharAt(yLabel.length() - 1);
            yLabel.deleteCharAt(yLabel.length() - 1);
        }
        if (title.length() > 1) {
            title.deleteCharAt(title.length() - 1);
            title.deleteCharAt(title.length() - 1);
        }

        /*
         * If we have a layer with more than one elevation value, we create a
         * transect chart using standard XYItem Renderer to keep the plot
         * renderer consistent with that of vertical section plot
         */
        if (hasVerticalAxis) {
            final XYItemRenderer renderer1 = new StandardXYItemRenderer();
            final NumberAxis rangeAxis1 = new NumberAxis(yLabel.toString());
            plot = new XYPlot(xySeriesColl, new NumberAxis(), rangeAxis1, renderer1);
            plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinePaint(Color.white);
            for (int i = 0; i < xySeriesColl.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            }
            plot.setOrientation(PlotOrientation.VERTICAL);
            chart = new JFreeChart(null, null, plot, multiplePlots);
        } else {
            /*
             * If we have a layer which only has one elevation value, we simply
             * create XY Line chart
             */
            chart = ChartFactory.createXYLineChart(title.toString(),
                    "Distance along transect (arbitrary units)", yLabel.toString(), xySeriesColl,
                    PlotOrientation.VERTICAL, multiplePlots, false, false);
            plot = chart.getXYPlot();
            for (int i = 0; i < xySeriesColl.getSeriesCount(); i++) {
                plot.getRenderer().setSeriesShape(i, new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            }
        }

        if (copyrightStatement != null && !hasVerticalAxis) {
            final TextTitle textTitle = new TextTitle(copyrightStatement);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            chart.addSubtitle(textTitle);
        }
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        rangeAxis.setAutoRangeIncludesZero(false);
        plot.setNoDataMessage("There is no data for what you have chosen.");

        /* Iterate through control points to show segments of transect */
        Double prevCtrlPointDistance = null;
        for (int i = 0; i < transectDomain.getControlPoints().size(); i++) {
            double ctrlPointDistance = transectDomain.getFractionalControlPointDistance(i);
            if (prevCtrlPointDistance != null) {
                /*
                 * Determine start end end value for marker based on index of
                 * ctrl point
                 */
                IntervalMarker target = new IntervalMarker(size * prevCtrlPointDistance, size
                        * ctrlPointDistance);
                target.setLabel("["
                        + printTwoDecimals(transectDomain.getControlPoints().get(i - 1).getY())
                        + ","
                        + printTwoDecimals(transectDomain.getControlPoints().get(i - 1).getX())
                        + "]");
                target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
                /*
                 * Alter colour of segment and position of label based on
                 * odd/even index
                 */
                if (i % 2 == 0) {
                    target.setPaint(new Color(222, 222, 255, 128));
                    target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                    target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                } else {
                    target.setPaint(new Color(233, 225, 146, 128));
                    target.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
                    target.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                }
                /* Add marker to plot */
                plot.addDomainMarker(target);
            }
            prevCtrlPointDistance = transectDomain.getFractionalControlPointDistance(i);
        }

        return chart;
    }

    /**
     * Plot a vertical section chart
     * 
     * @param features
     *            A {@link List} of evenly-spaced {@link ProfileFeature}s making
     *            up this vertical section. All features <i>must</i> have been
     *            extracted onto the same {@link VerticalAxis}. They must each
     *            only contain a single parameter.
     * @param horizPath
     *            The {@link LineString} along which the {@link ProfileFeature}s
     *            have been extracted
     * @param colourScheme
     *            The {@link ColourScheme} to use for the plot
     * @param zValue
     *            The elevation at which a matching transect is plotted (will be
     *            marked on the chart) - can be <code>null</code>
     * @return The resulting chart
     */
    public static JFreeChart createVerticalSectionChart(List<ProfileFeature> features,
            LineString horizPath, ColourScheme colourScheme, Double zValue) {
        if (features == null || features.size() == 0) {
            throw new IllegalArgumentException(
                    "You need at least one profile to plot a vertical section.");
        }

        VerticalSectionDataset dataset = new VerticalSectionDataset(features);

        NumberAxis xAxis = new NumberAxis("Distance along path (arbitrary units)");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        PaintScale scale = createPaintScale(colourScheme);

        NumberAxis colourScaleBar = new NumberAxis();
        Range colorBarRange = new Range(colourScheme.getScaleMin(), colourScheme.getScaleMax());
        colourScaleBar.setRange(colorBarRange);

        PaintScaleLegend paintScaleLegend = new PaintScaleLegend(scale, colourScaleBar);
        paintScaleLegend.setPosition(RectangleEdge.BOTTOM);

        XYBlockRenderer renderer = new XYBlockRenderer();
        double elevationResolution = dataset.getElevationResolution();
        renderer.setBlockHeight(elevationResolution);
        renderer.setPaintScale(scale);

        XYPlot plot = new XYPlot(dataset, xAxis, getZAxis(features.get(0).getDomain()
                .getVerticalCrs()), renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);

        /* Iterate through control points to show segments of transect */
        Double prevCtrlPointDistance = null;
        int xAxisLength = features.size();
        for (int i = 0; i < horizPath.getControlPoints().size(); i++) {
            double ctrlPointDistance = horizPath.getFractionalControlPointDistance(i);
            if (prevCtrlPointDistance != null) {
                /*
                 * Determine start end end value for marker based on index of
                 * ctrl point
                 */
                IntervalMarker target = new IntervalMarker(xAxisLength * prevCtrlPointDistance,
                        xAxisLength * ctrlPointDistance);
                target.setPaint(TRANSPARENT);
                /* Add marker to plot */
                plot.addDomainMarker(target);
                /* Add line marker to vertical section plot */
                if (zValue != null) {
                    final Marker verticalLevel = new ValueMarker(Math.abs(zValue));
                    verticalLevel.setPaint(Color.lightGray);
                    verticalLevel.setLabel("at " + zValue + "  level ");
                    verticalLevel.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
                    verticalLevel.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                    plot.addRangeMarker(verticalLevel);
                }

            }
            prevCtrlPointDistance = horizPath.getFractionalControlPointDistance(i);
        }

        JFreeChart chart = new JFreeChart(null, plot);
        chart.removeLegend();
        chart.addSubtitle(paintScaleLegend);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    public static JFreeChart addVerticalSectionChart(JFreeChart transectChart,
            JFreeChart verticalSectionChart) {
        /*
         * Create the combined chart with both the transect and the vertical
         * section
         */
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis(
                "Distance along path (arbitrary units)"));
        plot.setGap(20.0);
        plot.add(transectChart.getXYPlot(), 1);
        plot.add(verticalSectionChart.getXYPlot(), 1);
        plot.setOrientation(PlotOrientation.VERTICAL);
        String title = transectChart.getTitle().getText();
        String copyright = null;
        for (int i = 0; i < transectChart.getSubtitleCount(); i++) {
            Title subtitle = transectChart.getSubtitle(i);
            if (subtitle instanceof TextTitle) {
                copyright = ((TextTitle) transectChart.getSubtitle(0)).getText();
                break;
            }
        }

        JFreeChart combinedChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        /*
         * This is not ideal. We have already added the copyright label to the
         * first chart, but then we extract the actual plot, so we need to add
         * it again here
         */
        if (copyright != null) {
            final TextTitle textTitle = new TextTitle(copyright);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            transectChart.addSubtitle(textTitle);
        }

        /* Set left margin to 10 to avoid number wrap at color bar */
        RectangleInsets r = new RectangleInsets(0, 10, 0, 0);
        transectChart.setPadding(r);

        /* Use the legend from the vertical section chart */
        transectChart.addSubtitle(verticalSectionChart.getSubtitle(0));

        return combinedChart;
    }

    private static String getAxisLabel(Feature<?> feature, String memberName) {
        Parameter parameter = feature.getParameter(memberName);
        return parameter.getTitle() + " (" + parameter.getUnits() + ")";
    }

    /**
     * Prints a double-precision number to 2 decimal places
     * 
     * @param d
     *            the double
     * @return rounded value to 2 places, as a String
     */
    private static String printTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        /*
         * We need to set the Locale properly, otherwise the DecimalFormat
         * doesn't work in locales that use commas instead of points. Thanks to
         * Justino Martinez for this fix!
         */
        DecimalFormatSymbols decSym = DecimalFormatSymbols.getInstance(US_LOCALE);
        twoDForm.setDecimalFormatSymbols(decSym);
        return twoDForm.format(d);
    }

    /**
     * An {@link XYZDataset} that is created by interpolating a set of values
     * from a discrete set of elevations.
     */
    private static class VerticalSectionDataset extends AbstractXYZDataset {
        private static final long serialVersionUID = 1L;
        private final int horizPathLength;
        private final List<ProfileFeature> features;
        private final String paramId;
        private final List<Double> elevationValues;
        private final double minElValue;
        private final double elevationResolution;
        private final int numElevations;

        public VerticalSectionDataset(List<ProfileFeature> features) {
            this.features = features;
            this.horizPathLength = features.size();

            double minElValue = 0.0;
            double maxElValue = 1.0;
            VerticalAxis vAxis = features.get(0).getDomain();

            if (vAxis.size() > 0) {
                minElValue = vAxis.getCoordinateValue(0);
                maxElValue = vAxis.getCoordinateValue(vAxis.size() - 1);
            }

            /* Sometimes values on the axes are reversed */
            if (minElValue > maxElValue) {
                double temp = minElValue;
                minElValue = maxElValue;
                maxElValue = temp;
            }
            this.minElValue = minElValue;

            double minGap = Double.MAX_VALUE;
            for (int i = 1; i < vAxis.size(); i++) {
                minGap = Math.min(minGap,
                        Math.abs(vAxis.getCoordinateValue(i) - vAxis.getCoordinateValue(i - 1)));
            }
            this.numElevations = (int) ((maxElValue - minElValue) / minGap);
            this.elevationResolution = (maxElValue - minElValue) / numElevations;

            this.paramId = features.get(0).getParameterIds().iterator().next();
            this.elevationValues = vAxis.getCoordinateValues();
        }

        public double getElevationResolution() {
            return elevationResolution;
        }

        @Override
        public int getSeriesCount() {
            return 1;
        }

        @Override
        public String getSeriesKey(int series) {
            checkSeries(series);
            return "Vertical section";
        }

        @Override
        public int getItemCount(int series) {
            checkSeries(series);
            return horizPathLength * numElevations;
        }

        @Override
        public Integer getX(int series, int item) {
            checkSeries(series);
            /*
             * The x coordinate is just the integer index of the point along the
             * horizontal path
             */
            return item % horizPathLength;
        }

        /**
         * Gets the value of elevation, assuming linear variation between min
         * and max.
         */
        @Override
        public Double getY(int series, int item) {
            checkSeries(series);
            int yIndex = item / horizPathLength;
            return minElValue + yIndex * elevationResolution;
        }

        /**
         * Gets the data value corresponding with the given item, interpolating
         * between the recorded data values using nearest-neighbour
         * interpolation
         */
        @Override
        public Float getZ(int series, int item) {
            checkSeries(series);
            int xIndex = item % horizPathLength;
            double elevation = getY(series, item);
            /*
             * What is the index of the nearest elevation in the list of
             * elevations for which we have data?
             */
            int nearestElevationIndex = -1;
            double minDiff = Double.MAX_VALUE;
            for (int i = 0; i < elevationValues.size(); i++) {
                double el = elevationValues.get(i);
                double diff = Math.abs(el - elevation);
                if (diff < minDiff) {
                    minDiff = diff;
                    nearestElevationIndex = i;
                }
            }

            Number number = features.get(xIndex).getValues(paramId).get(nearestElevationIndex);
            if (number != null) {
                return number.floatValue();
            } else {
                return null;
            }
        }

        /**
         * @throws IllegalArgumentException
         *             if the argument is not zero.
         */
        private static void checkSeries(int series) {
            if (series != 0) {
                throw new IllegalArgumentException("Series must be zero");
            }
        }
    }

    /**
     * Creates and returns a JFreeChart {@link PaintScale} that converts data
     * values to {@link Color}s.
     */
    public static PaintScale createPaintScale(final ColourScheme colourScheme) {
        return new PaintScale() {
            @Override
            public double getLowerBound() {
                return colourScheme.getScaleMin();
            }

            @Override
            public double getUpperBound() {
                return colourScheme.getScaleMax();
            }

            @Override
            public Color getPaint(double value) {
                return colourScheme.getColor(value);
            }
        };
    }

    /**
     * Creates a Hovmoeller plot about one variable in the
     * {@link HovmoellerFeature} which has been extracted from its
     * {@link HovmoellerDomain}.
     * 
     * 
     * @param varId
     *            A variable name whose the corresponding Hovmoeller feature
     *            will be plotted.
     * @param feature
     *            A Hovmoeller feature
     * 
     * @throws InvalidLineStringException
     *             if the given line string is not a proper line string.
     * 
     * @throws InvalidCrsException
     *             if the given crs is not a right crs.
     * 
     * @return The plot.
     */
    public static JFreeChart plotHovmoellerFeature(final String varId,
            final HovmoellerFeature feature, final ColourScheme colourscheme,
            String copyrightStatement) throws InvalidLineStringException, InvalidCrsException {

        final long HOUR = 60L * 60L * 1000L;
        final long DAY = 24L * HOUR;

        LineString lineString = feature.getDomain().getLineString();
        int numberOfPointsOnLineString = lineString.getControlPoints().size();
        /*
         * an array stores the distance among adjacent points. The first one is
         * always ZERO.
         */
        double[] distancesBetweenAdjacentPoints = new double[numberOfPointsOnLineString];

        for (int i = 0; i < numberOfPointsOnLineString; i++) {
            distancesBetweenAdjacentPoints[i] = lineString.getFractionalControlPointDistance(i);
        }
        /*
         * Project the points of the line string onto a line. The distance
         * between two points on the line is identical to the distance on the
         * line string. An array stores the mid points among these points.
         */
        double[] midPoints = getDistanceBetweenAdjacentMidPointsOnLineString(distancesBetweenAdjacentPoints);

        /* An array stores the distances between two adjacent mid points. */
        double[] distancesBetweenMidPoints = new double[numberOfPointsOnLineString];
        distancesBetweenMidPoints[0] = midPoints[0];
        /*
         * A variable records the minimum distance between mid points. the value
         * is used as the base unit of the Hovmoeller strip. It means the width
         * of Hovmoeller strips is multiple (in integer) of the unit.
         */
        double minMidPointDistance = Double.MAX_VALUE;
        for (int i = 1; i < numberOfPointsOnLineString; i++) {
            distancesBetweenMidPoints[i] = midPoints[i] - midPoints[i - 1];
            if (distancesBetweenMidPoints[i] < minMidPointDistance) {
                minMidPointDistance = distancesBetweenMidPoints[i];
            }
        }
        /*
         * An integer is used to time the minimum distance (always less than
         * one) between mid points. After the operation, the result should be
         * greater than one.
         */
        int multipBase = 100;
        // Tick unit on the x-axis of the plot.
        double tickUnit = 25.0;

        if (minMidPointDistance > 0.001 && minMidPointDistance < 0.01) {
            multipBase = 1000;
            tickUnit = 250.0;
        }
        if (minMidPointDistance > 0.0001 && minMidPointDistance < 0.001) {
            multipBase = 10000;
            tickUnit = 2000.0;
        }
        // An array store the Hovmoeller strips' width.
        int[] columnsWidthInIntegerUnits = new int[numberOfPointsOnLineString];

        for (int i = 0; i < numberOfPointsOnLineString; i++) {
            columnsWidthInIntegerUnits[i] = (int) (distancesBetweenMidPoints[i] * multipBase);
        }

        TimeAxis domainTimeAxis = feature.getDomain().getTimeAxis();
        // Use Coordinate extent instead of axis values extent
        Extent<DateTime> domainTimeExtent = domainTimeAxis.getCoordinateExtent();

        final long datasetTimeFrom = domainTimeExtent.getLow().getMillis();
        final long datasetTimeTo = domainTimeExtent.getHigh().getMillis();
        // The time interval of the domain time axis

        final long tStep = (datasetTimeTo - datasetTimeFrom) / (domainTimeAxis.size());
        // The data needs to be plotted.
        final Array2D<Number> data = feature.getValues(varId);

        /* The data is used by an XYZDataset implementation. */
        final Array2D<Number> paddedData = padData(data, columnsWidthInIntegerUnits);

        XYZDataset dataset = new AbstractXYZDataset() {
            private static final long serialVersionUID = 1L;

            // The values on the Z-axis.
            // private final Array2D<Number> paddeddata = paddedData;

            // The values of numbers on X-axis.
            private final int xSize = paddedData.getXSize();
            // The values of numbers on Y-axis.

            private final int ySize = paddedData.getYSize();

            // The starting value on the Y-axis.
            private final long beginTime = datasetTimeFrom;
            // The interval between the adjacent values on the Y-axis.
            private final long timeStep = tStep;

            @Override
            public int getSeriesCount() {
                return 1;
            }

            @Override
            public String getSeriesKey(int series) {
                checkSeries(series);
                return "Hovmoeller section";
            }

            @Override
            public int getItemCount(int series) {
                checkSeries(series);
                return xSize * ySize;
            }

            @Override
            public Number getX(int series, int item) {
                checkSeries(series);

                /*
                 * The x coordinate is just the integer index of the point along
                 * the horizontal path.
                 */
                return item % xSize;
            }

            /**
             * Get the value of time represented by a Long number.
             */
            @Override
            public Number getY(int series, int item) {
                checkSeries(series);
                int yIndex = item / xSize;
                return yIndex * timeStep + beginTime;
            }

            /**
             * Gets the data value corresponding with the given Horizontal
             * position and the time.
             */
            @Override
            public Number getZ(int series, int item) {
                checkSeries(series);
                int xIndex = item % xSize;
                int yIndex = item / xSize;

                return paddedData.get(yIndex, xIndex);
            }

            /**
             * @throws IllegalArgumentException
             *             if the argument is not zero.
             */
            private void checkSeries(int series) {
                if (series != 0)
                    throw new IllegalArgumentException("One Series only. It must be zero");
            }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:00");

        DateAxis tAxis = new DateAxis("Date");

        tAxis.setRange(new Date(datasetTimeFrom), new Date(datasetTimeTo));
        if (tStep == DAY || tStep > DAY) {
            tAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, (int) (tStep / DAY)));
        } else {
            tAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, (int) (tStep / HOUR)));
        }
        tAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        tAxis.setDateFormatOverride(sdf);

        NumberAxis locationAxis = new NumberAxis("Distance along path (arbitrary units)");

        locationAxis.setRange(0, multipBase);
        locationAxis.setTickUnit(new NumberTickUnit(tickUnit));
        // locationAxis.setTickLabelsVisible(false);

        // The min value of the data
        Number minValue = getMinValueOfArray2D(data);
        // The max value of the data
        Number maxValue = getMaxValueOfArray2D(data);

        PaintScale paintscale = Charting.createPaintScale(colourscheme);

        org.jfree.data.Range colorBarRange = new org.jfree.data.Range(minValue.floatValue(),
                maxValue.floatValue());

        NumberAxis scaleAxis = new NumberAxis(feature.getParameter(varId).getUnits());
        scaleAxis.setAutoRange(false);
        scaleAxis.setRange(colorBarRange);
        PaintScaleLegend paintScaleLegend = new PaintScaleLegend(paintscale, scaleAxis);
        paintScaleLegend.setHeight(0.05);
        paintScaleLegend.setPosition(RectangleEdge.RIGHT);

        XYBlockRenderer xyblockrenderer = new XYBlockRenderer();

        double yRange = xyblockrenderer.findRangeBounds(dataset).getLength();
        xyblockrenderer.setBlockHeight(yRange);
        xyblockrenderer.setPaintScale(paintscale);
        // The default anchor position is RectangleAnchor.CENTER. Change it!
        xyblockrenderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);

        XYPlot plot = new XYPlot(dataset, locationAxis, tAxis, xyblockrenderer);
        plot.setDomainGridlinesVisible(false);

        // A starting position of an interval marker.
        double start = 0.0;
        /*
         * A half width of Hovmoeller strip which is in front of the current
         * strip.
         */
        double exgap = 0.0;

        for (int i = 0; i < numberOfPointsOnLineString; i++) {
            // The current half width of Hovmoeller strip.
            double gap = columnsWidthInIntegerUnits[i] / 2.0;

            IntervalMarker target = new IntervalMarker(start, start + exgap + gap);
            target.setPaint(TRANSPARENT);
            String label = "[" + printTwoDecimals(lineString.getControlPoints().get(i).getY())
                    + "," + printTwoDecimals(lineString.getControlPoints().get(i).getX()) + "]";
            target.setLabel(label);
            target.setLabelFont(new Font("SansSerif", Font.ITALIC, 8));
            if (i % 2 == 0) {
                target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            } else {
                target.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
                target.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
            }
            // add marker to plot
            plot.addDomainMarker(target);
            start = start + exgap + gap;
            exgap = gap;
        }

        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.addSubtitle(paintScaleLegend);
        if (copyrightStatement != null) {
            final TextTitle textTitle = new TextTitle(copyrightStatement);
            textTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            textTitle.setPosition(RectangleEdge.BOTTOM);
            textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            chart.addSubtitle(textTitle);
        }

        return chart;
    }

    /**
     * Get the minimum value of {@link Array2D}.
     * 
     * @param data
     *            An Array2D array.
     * 
     * @return The minimum value of the given array.
     */
    private static Number getMinValueOfArray2D(Array2D<Number> data) {
        Number minValue = Double.MAX_VALUE;
        Iterator<Number> iterator = data.iterator();
        while (iterator.hasNext()) {
            Number value = iterator.next();
            if (value != null && value.doubleValue() < minValue.doubleValue()) {
                minValue = value;
            }
        }
        return minValue;
    }

    /**
     * Get the maximum value of {@link Array2D}.
     * 
     * @param data
     *            An Array2D array.
     * 
     * @return The maximum value of the given array.
     */
    private static Number getMaxValueOfArray2D(Array2D<Number> data) {
        Number maxValue = Double.MIN_VALUE;
        Iterator<Number> iterator = data.iterator();
        while (iterator.hasNext()) {
            Number value = iterator.next();
            if (value != null && value.doubleValue() > maxValue.doubleValue()) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    /**
     * A helper method returning mid points on the line string.
     * 
     * @param distances
     *            store each point's distance to the origin point on the line
     *            string.
     * @return an array stores the mid point's distance to the origin point on
     *         the line string.
     */
    private static double[] getDistanceBetweenAdjacentMidPointsOnLineString(double[] distances) {
        int numberOfDistances = distances.length;
        double[] midPointsOnLineString = new double[numberOfDistances];
        for (int i = 0; i < numberOfDistances - 1; i++) {
            midPointsOnLineString[i] = (distances[i] + distances[i + 1]) / 2.0;
        }
        midPointsOnLineString[numberOfDistances - 1] = distances[numberOfDistances - 1];
        return midPointsOnLineString;
    }

    /**
     * In the give array, data, the index numbers on the x-axis reflect the
     * point numbers on the line string. As such, the width of the Hovmoeller
     * plot will be identical. Some data have to be padded into the array to
     * reflect the various width according to the distances among the points.
     * For example, a line sting is made up of three points. The distance
     * between point 0 and 1 is two while the distance between point 1 and 2 is
     * three. The x size of the new created array will increase from 2 to 5. The
     * y size need not be changed. For data (x, y) in the new array and the data
     * (m ,n) in the original array, the relation is: (x, y) equals (m,n) (x
     * =0,1 ,m=0, and y=n); (x, y) equals (m,n) (x =2,3,4 ,m=1, and y=n).
     * 
     * @param data
     *            The original Array2D array containing the Hovmoeller feature
     *            data.
     * 
     * @param columnsWidthInIntegerUnits
     *            An array stores the Hovmoeller strips' width in arbitrary
     *            integer units.
     * 
     * @return An Array2D array based on the input 'data'. Its size on x-axis
     *         reflects the Hovmoeller strips' widths.
     */
    private static Array2D<Number> padData(final Array2D<Number> data,
            int[] columnsWidthInIntegerUnits) {
        int expandedXSize = 0;
        for (int i = 0; i < columnsWidthInIntegerUnits.length; i++) {
            expandedXSize += columnsWidthInIntegerUnits[i];
        }
        int ySize = data.getYSize();
        ValuesArray2D results = new ValuesArray2D(ySize, expandedXSize);

        for (int i = 0; i < ySize; i++) {
            int posOffset = 0;
            for (int k = 0; k < data.getXSize(); k++) {
                Number temp = data.get(i, k);
                int newWidth = columnsWidthInIntegerUnits[k];
                for (int j = 0; j < newWidth; j++) {
                    results.set(temp, i, posOffset + j);
                }
                posOffset += newWidth;
            }
        }
        return results;
    }
}
