package study.stosiki.com.contentproviderpg;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;


import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.axis.DateAxis;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.time.FixedMillisecond;
import org.afree.data.time.Month;
import org.afree.data.time.RegularTimePeriod;
import org.afree.data.time.TimeSeries;
import org.afree.data.time.TimeSeriesCollection;
import org.afree.data.xy.XYDataset;
import org.afree.graphics.SolidColor;
import org.afree.ui.RectangleInsets;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by User on 09/08/2015.
 * Given a name of an event line represent in some way its events
 *
 */
public class ChartReportActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChartReportActivity.class.getSimpleName();

    private static final int EVENT_LIST_LOADER_ID = 2;

    private CursorAdapter cursorAdapter;
    private long[] lineIds;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        lineIds = getIntent().getLongArrayExtra(DbSchema.COL_LINE_ID);

        // create cursor adapter
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, this);

    }

    /** LoaderManager.LoaderCallbacks methods **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = null;
        String selection = null;
        if(lineIds[0] != -1 && lineIds[1] == -1) {
            selectionArgs = new String[1];
            selection = "=?";
            selectionArgs[0] = String.valueOf(lineIds[0]);
        } else if(lineIds[1] != -1) {
            selectionArgs = new String[2];
            selectionArgs[0] = String.valueOf(lineIds[0]);
            selectionArgs[1] = String.valueOf(lineIds[1]);
            selection = " in(?, ?)";
        }


        // get line id(s) from intent
        return new CursorLoader(
                this,
                EventLinesContract.Events.CONTENT_URI,
                EventLinesContract.Events.PROJECTION_ALL,
                DbSchema.COL_LINE_ID + selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // swap cursor in the adapter
        switch(loader.getId()) {
            case EVENT_LIST_LOADER_ID:
//                final AFreeChart chart = createChart(createDataset(cursor));
                setChart(cursor);
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    private void setChart(Cursor cursor) {
        TimeSeriesChartDemo01View mView = new TimeSeriesChartDemo01View(this, cursor);
        setContentView(mView);
    }

    private AFreeChart createChart(XYDataset dataset) {
        AFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Legal & General Unit Trust Prices",  // title
                "Date",             // x-axis label
                "Price Per Unit",   // y-axis label
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
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

        return chart;
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
