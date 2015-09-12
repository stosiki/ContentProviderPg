package study.stosiki.com.contentproviderpg;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ListReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ListReportFragment.class.getSimpleName();

    private ViewGroup listHolder;
    private static final int EVENT_LIST_LOADER_ID = 3;

    private CursorAdapter cursorAdapter;
    private long[] lineIds;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        lineIds = getActivity().getIntent().getLongArrayExtra(DbSchema.COL_LINE_ID);

        // create cursor adapter
        cursorAdapter = new CursorAdapter(getActivity(), null, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return inflater.inflate(R.layout.event_list_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView timeView = (TextView)view.findViewById(R.id.event_time);
                TextView dataView = (TextView)view.findViewById(R.id.event_data);

                timeView.setText(new Date(cursor.getLong(1)).toString());
                dataView.setText(cursor.getString(2));
            }
        };

        ViewGroup layout = (ViewGroup)getActivity().
                getLayoutInflater().inflate(R.layout.plain_log_report_fragment, container, false);
        listView = (ListView)layout.findViewById(R.id.event_list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, this);

        return listView;
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
                cursorAdapter.swapCursor(cursor);
                cursorAdapter.notifyDataSetChanged();
                cursorAdapter.notifyDataSetInvalidated();
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}