package study.stosiki.com.contentproviderpg;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ChartReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChartReportFragment.class.getSimpleName();

    private static final int EVENT_LIST_LOADER_ID = 2;

    private CursorAdapter cursorAdapter;
    private long[] lineIds;

    ViewGroup chartHolder;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        lineIds = getActivity().getIntent().getLongArrayExtra(DbSchema.COL_LINE_ID);

        // create cursor adapter
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, ChartReportFragment.this);

        chartHolder = (ViewGroup)
                getActivity().getLayoutInflater().inflate(R.layout.tab_1, container, false);
        return chartHolder;
    }

    private void saveChartAsImage() {
        if(isExternalStorageWritable() == false) {
            //TODO: give some error indication
            return;
        }

        View v = getView();
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getChartDirectory() + "/chart1.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
            fos = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public File getChartDirectory() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "EventLinesCharts");
        if (file.mkdirs() == false) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /** LoaderManager.LoaderCallbacks methods **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = null;
        String selection = null;
        if(lineIds.length == 1) {
            selectionArgs = new String[1];
            selection = "=?";
            selectionArgs[0] = String.valueOf(lineIds[0]);
        } else if(lineIds.length == 2) {
            selectionArgs = new String[2];
            selectionArgs[0] = String.valueOf(lineIds[0]);
            selectionArgs[1] = String.valueOf(lineIds[1]);
            selection = " in(?, ?)";
        }


        // get line id(s) from intent
        return new CursorLoader(
                getActivity(),
                EventLinesContract.EventGraphData.CONTENT_URI,
                EventLinesContract.EventGraphData.PROJECTION_ALL,
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
        TimeSeriesChartDemo01View mView = new TimeSeriesChartDemo01View(getActivity(), cursor);
        chartHolder.addView(mView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}