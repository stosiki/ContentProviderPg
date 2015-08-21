package study.stosiki.com.contentproviderpg;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by User on 09/08/2015.
 * Given a name of an event line represent in some way its events
 *
 */
public class ReportActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ReportActivity.class.getSimpleName();

    private static final int EVENT_LIST_LOADER_ID = 2;

    private SimpleCursorAdapter cursorAdapter;
    private long lineId;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        lineId = getIntent().getLongExtra(DbSchema.COL_LINE_ID, -1);

        // create cursor adapter
        cursorAdapter = new EventListCursorAdapter(this);

        listView = (ListView)findViewById(R.id.event_list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LIST_LOADER_ID, null, this);

    }

    /** LoaderManager.LoaderCallbacks methods **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // get line id(s) from intent
        return new CursorLoader(
                this,
                EventLinesContract.Events.CONTENT_URI,
                EventLinesContract.Events.PROJECTION_ALL,
                DbSchema.COL_LINE_ID + "=?",
                new String[]{String.valueOf(lineId)},
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
