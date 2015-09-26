package study.stosiki.com.contentproviderpg.charts;

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

import org.afree.chart.axis.NumberAxis;
import org.afree.chart.axis.ValueAxis;
import org.afree.chart.text.TextUtilities;
import org.afree.data.Range;
import org.afree.data.time.Day;
import org.afree.data.time.FixedMillisecond;
import org.afree.data.time.TimeSeriesCollection;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.afree.chart.AFreeChart;
import org.afree.chart.axis.DateAxis;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.time.TimeSeries;
import org.afree.data.xy.XYDataset;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.Font;
import org.afree.graphics.geom.OvalShape;
import org.afree.graphics.geom.RectShape;
import org.afree.graphics.geom.Shape;
import org.afree.ui.RectangleInsets;
import org.afree.ui.TextAnchor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import study.stosiki.com.contentproviderpg.MainActivity;
import study.stosiki.com.contentproviderpg.events.EventLine;
import study.stosiki.com.contentproviderpg.events.IntegerEvent;
import study.stosiki.com.contentproviderpg.events.SimpleEvent;
import study.stosiki.com.contentproviderpg.events.StringEvent;

/**
 * TimeSeriesChartDemo01View
 */
public class ChartView extends DemoView {

    private static final String TAG = ChartView.class.getSimpleName();

    //TODO: all pixel values has to be converted to dp
    private static final Shape CIRCLE_SHAPE = new OvalShape(-5, -5, 10, 10);
    private static final Shape SQUARE_SHAPE = new RectShape(-5, -5, 10, 10);
    private static final Shape COMMENT_TICK_SHAPE = new RectShape(-1,-5,2,10);
    private static final Shape[] NODE_SHAPES = new Shape[]{CIRCLE_SHAPE, SQUARE_SHAPE};
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Typeface.NORMAL, 24);
    private static final double CCW_90 = Math.toRadians(-90);

    public ChartView(Context context, Cursor cursor) {
        super(context);
        final AFreeChart chart = createChart(collectData(cursor));
        setChart(chart);
    }

    private AFreeChart createChart(Map<Integer, EventLine> data) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        AFreeChart chart = createTimeSeriesChart(
                "",  // title
                "Date",             // x-axis label
                "",   // y-axis label
                dataset,            // data
                true,               // create legend?
                false,               // generate tooltips?
                false               // generate URLs?
        );

        chart.setBackgroundPaintType(new SolidColor(Color.WHITE));

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaintType(new SolidColor(Color.LTGRAY));
        plot.setDomainGridlinePaintType(new SolidColor(Color.WHITE));
        plot.setRangeGridlinePaintType(new SolidColor(Color.WHITE));
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Typeface.NORMAL, 24));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Typeface.NORMAL, 24));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Typeface.NORMAL, 24));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Typeface.NORMAL, 24));

        // experimenting with settings
        plot.setDomainPannable(false);
        plot.setRangePannable(false);
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setOutlineVisible(false);
//        ((DateAxis)plot.getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("dd-MM-yy hh:mm"));
        plot.getDomainAxis().setTickMarkInsideLength(10.0f);

        // sort through data,
        // if contains any LINE_TYPE_STRING series, extract it and convert to annotations
        // else add it to the data set
        String chartTitle = null;

        List<SeriesRenderProps> seriesRenderPropsList = new ArrayList<>();

        // we need to traverse the data twice, because chartMinDomainValue, and chartMaxDomainValue,
        // are required to create annotations and determine domain values for SimpleEvents
        int chartMinDomainValue = 0;
        int chartMaxDomainValue = 0;
        int simpleLinesCount = 0;
        boolean integerLinePresent = false;

        for(EventLine eventLine : data.values()) {
            if(chartTitle == null) {
                chartTitle = eventLine.getTitle();
            } else {
                chartTitle = chartTitle + ", " + eventLine.getTitle();
            }
            if(eventLine.getType() == EventLine.LINE_TYPE_INTEGER) {
                integerLinePresent = true;
                TimeSeries series = new TimeSeries(eventLine.getTitle());
                Map<Day, Integer> seriesData = new HashMap<>();
                for(SimpleEvent event : eventLine.getEvents()) {
                    int value = ((IntegerEvent)event).getValue();
                    if(chartMinDomainValue == 0 || chartMinDomainValue > value) {
                        chartMinDomainValue = value;
                    }
                    if(chartMaxDomainValue < value) {
                        chartMaxDomainValue = value;
                    }
                    long timestamp = event.getTimestamp();
                    if(eventLine.getAggregate() == EventLine.AGGREGATE_DAILY) {
                        Day d = new Day(new Date(timestamp));
                        if (seriesData.containsKey(d) == false) {
                            seriesData.put(d, 0);
                        }
                        seriesData.put(d, seriesData.get(d).intValue() + value);
                    } else {
                        series.add(new FixedMillisecond(timestamp), value);
                    }
                }
                if(eventLine.getAggregate() == EventLine.AGGREGATE_DAILY) {
                    for (Day day : seriesData.keySet()) {
                        series.add(day, seriesData.get(day));
                    }
                }
                dataset.addSeries(series);
                seriesRenderPropsList.add(new SeriesRenderProps(
                        eventLine.getType(), eventLine.getTitle(), eventLine.getColor(), eventLine.getAggregate()));
            } else if(eventLine.getType() == EventLine.LINE_TYPE_SIMPLE) {
                simpleLinesCount++;
            }
        }

        // collect and aggregate Simple events
        // we need to do it in a separate pass because domain range determining positions
        for(EventLine eventLine : data.values()) {
            if(eventLine.getType() == EventLine.LINE_TYPE_SIMPLE) {
                DateAxis dateAxis = (DateAxis)plot.getDomainAxis();
                TimeSeries series = new TimeSeries(eventLine.getTitle());
                Map<Day, Integer> seriesData = new HashMap<>();
                for(SimpleEvent event : eventLine.getEvents()) {
                    long timestamp = event.getTimestamp();
                    if(eventLine.getAggregate() == EventLine.AGGREGATE_DAILY) {
                        Day d = new Day(new Date(timestamp));
                        if (seriesData.containsKey(d) == false) {
                            seriesData.put(d, 0);
                        }
                        seriesData.put(d, seriesData.get(d).intValue() + 1);
                    }  else {
                        // here we have to assign value in such a way that
                        // in case there's already an integer series in a data set, simple series is located in the middle
                        // otherwise value = simpleLinesCount--;
                        if(integerLinePresent) {
                            series.add(new FixedMillisecond(timestamp),
                                    (int) ((chartMaxDomainValue + chartMinDomainValue) / 2));
                        } else {
                            series.add(new FixedMillisecond(timestamp), simpleLinesCount);
                        }
                    }
                }
                if(eventLine.getAggregate() == EventLine.AGGREGATE_DAILY) {
                    for (Day day : seriesData.keySet()) {
                        series.add(day, seriesData.get(day));
                    }
                }
                seriesRenderPropsList.add(new SeriesRenderProps(
                        eventLine.getType(), eventLine.getTitle(), eventLine.getColor(), eventLine.getAggregate()));
                dataset.addSeries(series);
                simpleLinesCount--;

            }
        }

        for(EventLine eventLine : data.values()) {
            if(eventLine.getType() == EventLine.LINE_TYPE_STRING) {
                DateAxis dateAxis = (DateAxis)plot.getDomainAxis();
                TimeSeries series = new TimeSeries(eventLine.getTitle());

                for(SimpleEvent event : eventLine.getEvents()) {
                    MyXYTextAnnotation annotation = new MyXYTextAnnotation(
                            ((StringEvent) event).getComment(),
                            event.getTimestamp(),
                            chartMinDomainValue
                    );
                    if(event.getTimestamp() < dateAxis.getMinimumDate().getTime() ||
                            dateAxis.getMinimumDate().getTime() == 0) {
                        dateAxis.setMinimumDate(new Date(event.getTimestamp()));

                    }
                    if(event.getTimestamp() > dateAxis.getMaximumDate().getTime()) {
                        dateAxis.setMaximumDate(new Date(event.getTimestamp()));
                    }
                    annotation.setFont(ANNOTATION_FONT);
                    annotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                    annotation.setRotationAnchor(TextAnchor.BOTTOM_LEFT);
                    annotation.setRotationAngle(CCW_90);
                    annotation.setPaintType(new SolidColor(Color.BLACK));
                    plot.addAnnotation(annotation);

                    series.add(new FixedMillisecond(event.getTimestamp()),
                            (int) ((chartMaxDomainValue + chartMinDomainValue) / 2));
                }

                long r1 = dateAxis.getMaximumDate().getTime() - dateAxis.getMinimumDate().getTime();
                //TODO: hardcoded, good for most devices, but would be better to figure width of an actual window
                int width = 800;
                double r2 = r1*(width + ANNOTATION_FONT.getSize()) / width;
                dateAxis.setMinimumDate(new Date(dateAxis.getMinimumDate().getTime() - (long) (r2 - r1)));

                seriesRenderPropsList.add(new SeriesRenderProps(
                        eventLine.getType(), eventLine.getTitle(), eventLine.getColor(), eventLine.getAggregate()));
                dataset.addSeries(series);
            }
        }

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setBaseShape(CIRCLE_SHAPE);
            for(int i=0; i<seriesRenderPropsList.size(); i++) {
                SeriesRenderProps props = seriesRenderPropsList.get(i);
                if(props.getType() == EventLine.LINE_TYPE_STRING) {
                    renderer.setSeriesShape(i, COMMENT_TICK_SHAPE);
                } else {
                    renderer.setSeriesShape(i, NODE_SHAPES[i]);
                }
                renderer.setSeriesShapesFilled(i, true);
                renderer.setSeriesShapesVisible(i, true);
                renderer.setSeriesStroke(i, 5.0f);
                renderer.setSeriesPaintType(i, new SolidColor(Integer.parseInt(props.getColor())));

                if(props.getType() == EventLine.LINE_TYPE_SIMPLE &&
                        props.getAggregated() == EventLine.AGGREGATE_NONE
                        || props.getType() == EventLine.LINE_TYPE_STRING) {
                    renderer.setSeriesLinesVisible(i, false);
                    renderer.setSeriesShapesVisible(i, true);
                }

            }
            renderer.setDrawSeriesLineAsPath(true);
        }



        chart.setTitle(chartTitle);
        return chart;
    }

    private static AFreeChart createTimeSeriesChart(String title,
                                                   String timeAxisLabel,
                                                   String valueAxisLabel,
                                                   XYDataset dataset,
                                                   boolean legend,
                                                   boolean tooltips,
                                                   boolean urls) {

        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setLowerMargin(0.02);  // reduce the default margins
        timeAxis.setUpperMargin(0.02);
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeIncludesZero(false);  // override default
        XYPlotWoAnnotationClutter plot = new XYPlotWoAnnotationClutter(dataset, timeAxis, valueAxis, null);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        AFreeChart chart = new AFreeChart(title, AFreeChart.DEFAULT_TITLE_FONT, plot, legend);
//        currentTheme.apply(chart);
        return chart;

    }

    /**
     * @return The dataset.
     */
    private Map<Integer, EventLine> collectData(Cursor cursor) {
        //Map<Integer, EventLineTimeSeries> dataSeries = new HashMap<>();
        Map<Integer, EventLine> data = new HashMap<>();

        while (cursor.moveToNext()) {
            long timestamp = cursor.getLong(1);
            int lineId = cursor.getInt(2);
            // can be either number or string
            String value = cursor.getString(3);
            String lineTitle = cursor.getString(4);
            int lineType = cursor.getInt(5);
            String color = cursor.getString(6);
            int aggregate = cursor.getInt(7);

            if(data.containsKey(lineId) == false) {
                //TODO: "true" in the constructor is hardcoded, it should be
                EventLine eventLine = new EventLine(lineTitle, lineType, color, aggregate);
                data.put(lineId, eventLine);
            }
            EventLine eventLine = data.get(lineId);

            switch(lineType) {
                case EventLine.LINE_TYPE_SIMPLE:
                    eventLine.addEvent(new SimpleEvent(timestamp));
                    break;
                case EventLine.LINE_TYPE_INTEGER:
                    eventLine.addEvent(new IntegerEvent(timestamp, Integer.parseInt(value)));
                    break;
                case EventLine.LINE_TYPE_STRING:
                    eventLine.addEvent(new StringEvent(timestamp, value));
                    break;
            }
        }

        return data;
    }

    class SeriesRenderProps {
        String title;
        String color;
        int type;
        int aggregated;

        SeriesRenderProps(int type, String title, String color, int aggregated) {
            this.type = type;
            this.title = title;
            this.color = color;
            this.aggregated = aggregated;
        }

        public String getColor() {
            return color;
        }

        public String getTitle() {
            return title;
        }

        public int getAggregated() {
            return  aggregated;
        }

        public int getType() {
            return type;
        }
    }

    class EventLineTimeSeries extends TimeSeries {
        private int lineType;
        EventLineTimeSeries(String title, int lineType) {
            super(title);
            this.lineType = lineType;
        }
    }
}

