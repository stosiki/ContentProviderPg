package study.stosiki.com.contentproviderpg;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.squareup.otto.Subscribe;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int EVENT_LINES_LOADER_ID = 1;

    private static final long LIST_ITEM_REMOVE_ANIM_DURATION = 1000;
    private static final long UNDO_BAR_HIDE_ANIM_DURATION = 1000;

    public static MainThreadBus eventBus = new MainThreadBus();

    private SimpleCursorAdapter cursorAdapter;
    private int selectedItemIndex;
    private ActionMode actionMode;
    private ListView listView;
    private View undoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.event_line_list_item,
                null,
                new String[]{DbSchema.COL_TITLE, DbSchema.COL_EVENT_COUNT},
                new int[]{R.id.line_title, R.id.line_event_count},
                0
        ) {
            @Override
            protected void onContentChanged() {
                super.onContentChanged();
                Log.d(TAG, "SimpleCursorAdapter::onContentChanged() called");
            }
        };

        undoContainer = (View)findViewById(R.id.undo_bar);


        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LINES_LOADER_ID, null, this);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemIndex = position;
                showContextActionBar();
//                deleteEventLine(position);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemIndex = position;
                addEventToLine(position);
            }
        });

        eventBus.register(this);
    }

    private void showContextActionBar() {
        actionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.action_mode_row_selected, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_delete_eventline:
                        //deleteEventLine(selectedItemIndex);
                        // start animation to hide the cell
                        verticalCollapseSelectedListItem();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                selectedItemIndex = -1;
            }
        });

    }


    //https://github.com/paraches/ListViewCellDeleteAnimation/blob/master/src/com/example/myanimtest/MainActivity.java
    private void verticalCollapseSelectedListItem() {
//        final View selectedItemView = listView.getSelectedView();
        final View selectedItemView = listView.getChildAt(selectedItemIndex);
        final int initialHeight = selectedItemView.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    selectedItemView.setVisibility(View.GONE);
                }
                else {
                    selectedItemView.getLayoutParams().height =
                            initialHeight - (int)(initialHeight * interpolatedTime);
                    selectedItemView.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };


        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                // show undo bottom bar
                showUndo();
                //

//                mMyAnimListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        anim.setDuration(LIST_ITEM_REMOVE_ANIM_DURATION);
        selectedItemView.startAnimation(anim);
        Log.d(TAG, "Starting animation");
    }

    private void showUndo() {
        undoContainer.setVisibility(View.VISIBLE);
        Rect rect = new Rect();
        int[] screenLoc = new int[2];
        undoContainer.getLocationInWindow(screenLoc);
        undoContainer.getLocalVisibleRect(rect);
        Log.d(TAG, "x=" + screenLoc[0]);
        Log.d(TAG, "y=" + screenLoc[1]);
        /*
        int[] coords = new int[2];
        undoContainer.getLocationOnScreen(coords);
        ObjectAnimator animator = ObjectAnimator.ofFloat(undoContainer, View.Y,
                coords[1], coords[1] + undoContainer.getHeight());
        animator.setDuration(UNDO_BAR_HIDE_ANIM_DURATION);
        // keep on showing it
        animator.setStartDelay(500);
        // then slide it from the view
        animator.start();
        // and finally remove the current item from the cursor
        */
    }

    private void deleteEventLine(int position) {
        Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
        long lineId = cursorAdapter.getItemId(position);
        intent.setAction(DbAsyncOpsService.ACTION_DELETE_EVENT_LINE);
        intent.putExtra(BaseColumns._ID, lineId);
        MainActivity.this.startService(intent);
    }


    private void addEventToLine(int position) {
        cursorAdapter.getItem(position);
        // if event is being added to a simple line, just do it, otherwise
        Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
        intent.setAction(DbAsyncOpsService.ACTION_CREATE_EVENT);
        long lineId = cursorAdapter.getItemId(position);
        intent.putExtra(DbSchema.COL_LINE_ID, lineId);
        MainActivity.this.startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
//                EventLinesContract.EventLines.CONTENT_URI,
                EventLinesContract.EventLineListItem.CONTENT_URI,
                EventLinesContract.EventLineListItem.PROJECTION_ALL,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()) {
            case EVENT_LINES_LOADER_ID:
                cursorAdapter.swapCursor(cursor);
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        switch(loader.getId()) {
            case EVENT_LINES_LOADER_ID:
                cursorAdapter.swapCursor(null);
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    @Subscribe
    public void getMessage(String msg) {

    }

    @Subscribe
    public void getMessage(Integer msgCode) {
        Log.d(TAG, "Got a message, restarting loader");
        getLoaderManager().restartLoader(EVENT_LINES_LOADER_ID, null, this);
    }
}
