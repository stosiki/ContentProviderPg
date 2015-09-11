package study.stosiki.com.contentproviderpg;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;


/**
 * Created by Edwin on 15/02/2015.
 */
public class ChartReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChartReportFragment.class.getSimpleName();

    private static final int EVENT_LIST_LOADER_ID = 2;

    private CursorAdapter cursorAdapter;
    private long[] lineIds;

    ViewGroup chartHolder;
//    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "ChartReportFragment::onCreateView()");
        lineIds = getActivity().getIntent().getLongArrayExtra(DbSchema.COL_LINE_ID);
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, ChartReportFragment.this);
//        container.removeAllViews();

        chartHolder = (ViewGroup)inflater.inflate(R.layout.tab_1, container, false);
/*
        if(chartHolder.getParent() != null) {
            ((ViewGroup) chartHolder.getParent()).removeAllViews();
        }
*/

        return chartHolder;

//        return new View(getActivity());
    }


    @Nullable
    @Override
    public View getView() {
        return chartHolder;
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
                setChart(cursor);
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    private void setChart(Cursor cursor) {
        TimeSeriesChartDemo01View mView = new TimeSeriesChartDemo01View(getActivity(), cursor);
        ((ViewGroup)getView()).addView(mView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}