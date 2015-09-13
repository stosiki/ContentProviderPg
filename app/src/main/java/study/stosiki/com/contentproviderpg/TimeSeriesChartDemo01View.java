package study.stosiki.com.contentproviderpg;

/**
 * Created by User on 27/08/2015.
 */
/* ===========================================================
 * AFreeChart : a free chart library for Android(tm) platform.
 *              (based on AFreeChart and JCommon)
 * ===========================================================
 *
 * (C) Copyright 2010, by ICOMSYSTECH Co.,Ltd.
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:
 *    AFreeChart: http://code.google.com/p/afreechart/
 *    JFreeChart: http://www.jfree.org/jfreechart/index.html
 *    JCommon   : http://www.jfree.org/jcommon/index.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * TimeSeriesChartDemo01View.java
 * -----------------
 * (C) Copyright 2010, 2011, by ICOMSYSTECH Co.,Ltd.
 *
 * Original Author:  Niwano Masayoshi (for ICOMSYSTECH Co.,Ltd);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 19-Nov-2010 : Version 0.0.1 (NM);
 */

import org.afree.chart.axis.DateTick;
import org.afree.chart.axis.NumberAxis;
import org.afree.chart.axis.TickUnit;
import org.afree.chart.axis.TickUnitSource;
import org.afree.chart.axis.ValueTick;
import org.afree.data.time.FixedMillisecond;
import org.afree.data.time.TimeSeriesCollection;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.axis.DateAxis;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.time.Month;
import org.afree.data.time.TimeSeries;
import org.afree.data.time.TimeSeriesCollection;
import org.afree.data.time.TimeSeriesDataItem;
import org.afree.data.xy.XYDataset;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.OvalShape;
import org.afree.graphics.geom.RectShape;
import org.afree.graphics.geom.Shape;
import org.afree.ui.RectangleInsets;
import org.afree.ui.TextAnchor;
import org.afree.util.ShapeUtilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;

/**
 * TimeSeriesChartDemo01View
 */
public class TimeSeriesChartDemo01View extends DemoView {

    private static final String TAG = TimeSeriesChartDemo01View.class.getSimpleName();

    private static final Shape CIRCLE_SHAPE = new OvalShape(-5, -5, 10, 10);
    private static final Shape SQUARE_SHAPE = new RectShape(-5, -5, 10, 10);
    private static final Shape[] NODE_SHAPES = new Shape[]{CIRCLE_SHAPE, SQUARE_SHAPE};

    /**
     * constructor
     *
     * @param context
     */
    public TimeSeriesChartDemo01View(Context context, Cursor cursor) {
        super(context);

        final AFreeChart chart = createChart(createDataset(cursor));

        setChart(chart);
    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     * @return A chart.
     */
    private static AFreeChart createChart(XYDataset dataset) {

        AFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",  // title
                "Date",             // x-axis label
                "",   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
        );

        chart.setBackgroundPaintType(new SolidColor(Color.WHITE));

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaintType(new SolidColor(Color.LTGRAY));
        plot.setDomainGridlinePaintType(new SolidColor(Color.WHITE));
        plot.setRangeGridlinePaintType(new SolidColor(Color.WHITE));
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

        // experimenting with settings
        plot.setDomainPannable(false);
        plot.setRangePannable(false);
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

//        plot.setDomainCrosshairVisible(true);
//        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        //TODO: graphical properties of the charts have to be adjusted according to the dataset
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
//            Shape cross = ShapeUtilities..createDiagonalCross(3, 1);
            renderer.setBaseShape(CIRCLE_SHAPE);
            for(int i=0; i<MainActivity.MAX_SELECTION_SIZE; i++) {
                renderer.setSeriesShape(i, NODE_SHAPES[i]);
                renderer.setSeriesShapesFilled(i, true);
                renderer.setSeriesShapesVisible(i, true);

                renderer.setSeriesStroke(i, 5.0f);
            }

//            renderer.setSeriesLinesVisible(0, false);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
//        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));


        return chart;
    }

    /**
     * @return The dataset.
     */
    private static XYDataset createDataset(Cursor cursor) {
        Map<Integer, TimeSeries> dataSeries = new HashMap<Integer, TimeSeries>();
        while (cursor.moveToNext()) {
            /*
                DbSchema.COL_ID,
                DbSchema.COL_TIMESTAMP,
                DbSchema.COL_LINE_ID,
                DbSchema.COL_DATA,
                DbSchema.COL_TITLE,
                DbSchema.COL_LINE_TYPE

             */
            long timestamp = cursor.getLong(1);
            int lineId = cursor.getInt(2);
            String data = cursor.getString(3);
            String lineTitle = cursor.getString(4);
            int lineType = cursor.getInt(5);

            if(dataSeries.containsKey(lineId) == false) {
                TimeSeries series = new TimeSeries(lineTitle);
                dataSeries.put(lineId, series);
            }
            TimeSeries series = dataSeries.get(lineId);
            if(data != null) {
                if(lineType == MainActivity.LINE_TYPE_INTEGER) {
                    series.add(new FixedMillisecond(timestamp), Integer.parseInt(data));
                } else if(lineType == MainActivity.LINE_TYPE_STRING) {
                    //TODO: subst hardcoded 1 for something meaningful, find a way to
                    // add string labels to the chart (is it a marker?)
                    series.add(new FixedMillisecond(timestamp), new Integer(1));
/*
                    series.add(new DateTick(new Date(timestamp), data.toString(),
                            TextAnchor.BASELINE_CENTER, TextAnchor.CENTER, 90.0));
*/
                }
            } else {
                series.add(new FixedMillisecond(timestamp), 0);
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for(TimeSeries series : dataSeries.values()) {
            dataset.addSeries(series);
        }

        return dataset;
    }
}

