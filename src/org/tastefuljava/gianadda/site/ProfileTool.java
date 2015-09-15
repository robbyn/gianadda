package org.tastefuljava.gianadda.site;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.ui.RectangleInsets;
import org.tastefuljava.gianadda.geo.EarthGeometry;
import org.tastefuljava.gianadda.geo.Mn03Point;
import org.tastefuljava.gianadda.geo.Summary;
import org.tastefuljava.gianadda.geo.TrackPoint;
import org.tastefuljava.gianadda.util.Configuration;

public class ProfileTool {
    private final Configuration conf;
    private int lastId;

    public ProfileTool(Configuration conf) {
        this.conf = conf;
    }

    public String svg(TrackPoint[] points, int width, int height)
            throws IOException {
        JFreeChart chart = createChart(points);
        SVGGraphics2D g = new SVGGraphics2D(width, height);
        chart.draw(g, new Rectangle(0, 0, width, height));
        try (StringWriter out = new StringWriter()) {
            // HACK: SVG injection to set the viewBox attribute
            int id = ++lastId;
            out.write(g.getSVGElement("chart"
                    + id + "\" viewBox=\"0 0 " + width + " " + height));
            return out.toString();
        }
    }

    public Summary summary(TrackPoint[] points) {
        return Summary.compute(points);
    }

    public String[] swissMaps(TrackPoint pts[]) {
        Set<String> maps = new HashSet<>();
        for (TrackPoint pt: pts) {
            maps.add(swissMap(pt));
        }
        String result[] = maps.toArray(new String[maps.size()]);
        Arrays.sort(result);
        return result;
    }

    public String swissMap(TrackPoint point) {
        Mn03Point pt = new Mn03Point(point);
        int num = 1000 + 20*(int)Math.floor((302000-pt.getY())/12000)
                + (int)Math.floor((pt.getX()-480000)/17500);
        return "CN" + num;
    }

    public JFreeChart createChart(TrackPoint[] points) {
        return createChart(createDataset(points));
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYAreaChart(
            null,  // chart title
            null,                       // domain axis label
            null,                       // range axis label
            dataset,                         // data
            PlotOrientation.VERTICAL,        // the plot orientation
            false,                            // legend
            false,                            // tooltips
            false                            // urls
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis)plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        DecimalFormatSymbols syms = new DecimalFormatSymbols();
        syms.setGroupingSeparator('\'');
        syms.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0'm'");
        format.setDecimalFormatSymbols(syms);
        axis.setNumberFormatOverride(format);
        axis = (NumberAxis)plot.getDomainAxis();
        format = new DecimalFormat("#,##0.#'km'");
        format.setDecimalFormatSymbols(syms);
        axis.setNumberFormatOverride(format);
        XYAreaRenderer renderer = (XYAreaRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);
        return chart;
    }

    private static XYDataset createDataset(TrackPoint[] points) {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        XYSeries series = new XYSeries("Series 1", true, false);
        TrackPoint prev = null;
        double d = 0;
        for (TrackPoint pt: points) {
            if (prev != null) {
                d += EarthGeometry.distance(pt, prev);
            }
            double km = d/1000.0;
            if (series.indexOf(km) < 0) {
                series.add(km, pt.getH());
            }
            prev = pt;
        }
        dataset.addSeries(series);
        return dataset;
    }
}
