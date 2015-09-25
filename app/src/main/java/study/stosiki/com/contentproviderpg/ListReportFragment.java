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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import study.stosiki.com.contentproviderpg.db.DbSchema;
import study.stosiki.com.contentproviderpg.db.EventLinesContract;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ListReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ListReportFragment.class.getSimpleName();

    private static final int EVENT_LIST_LOADER_ID = 3;

    private CursorAdapter cursorAdapter;
    private long[] lineIds;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        lineIds = getActivity().getIntent().getLongArrayExtra(DbSchema.COL_LINE_ID);
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
                TextView lineTitleView = (TextView)view.findViewById(R.id.report_line_title);

                long timestamp = cursor.getLong(1);
                String data = cursor.getString(3);
                String lineTitle = cursor.getString(4);
                int lineType = cursor.getInt(5);

                timeView.setText(new Date(timestamp).toString());
                dataView.setText(data);
                lineTitleView.setText(lineTitle);
            }
        };

        ViewGroup layout = (ViewGroup)getActivity().
                getLayoutInflater().inflate(R.layout.plain_log_report_fragment, container, false);
        listView = (ListView)layout.findViewById(R.id.event_list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, this);

        return layout;
    }

    /** LoaderManager.LoaderCallbacks methods **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = new String[lineIds.length];
        for(int i=0; i<selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(lineIds[i]);
        }

        String selection = null;
        if(lineIds.length == 1) {
            selection = "=?";
        } else if(lineIds.length == 2) {
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

    public List<EventListEntry> getEventList() {
        List<EventListEntry> eventList = new ArrayList<>();
        Cursor c = cursorAdapter.getCursor();
        c.moveToFirst();
        for(int i=0; i<cursorAdapter.getCount(); i++) {
            long timestamp = c.getLong(1);
            String data = c.getString(3);
            String lineTitle = c.getString(4);
            int lineType = c.getInt(5);
            eventList.add(new EventListEntry(c.getLong(1), c.getString(3), lineTitle, lineType));
            c.moveToNext();
        }
        return eventList;
    }

    class EventListEntry {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        long timestamp;
        String data;
        String lineTitle;
        int lineType;

        EventListEntry(long timestamp, String data, String lineTitle, int lineType) {
            this.timestamp = timestamp;
            this.data = data;
            this.lineTitle = lineTitle;
            this.lineType = lineType;
        }

        public long getTimestamp() { return timestamp; }
        public String getData() { return data; }
        public String getLineTitle() { return lineTitle; }
        public int getLineType() { return lineType; }

        public String toCsvString() {
            StringBuilder sb = new StringBuilder();
            sb.append(lineTitle);
            sb.append(",");
            sb.append(dateFormat.format(new Date(timestamp)));
            sb.append(",");
            sb.append(data);
            sb.append("\n");
            return sb.toString();
        }
    }
}