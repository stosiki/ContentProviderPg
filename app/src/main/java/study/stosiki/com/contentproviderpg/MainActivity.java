package study.stosiki.com.contentproviderpg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.squareup.otto.Subscribe;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        CreateEventLineDialogFragment.CreateEventLineDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int EVENT_LINES_LOADER_ID = 1;

    private static final long LIST_ITEM_COLLAPSE_ANIM_DURATION = 1000;
    private static final long UNDO_BAR_HIDE_ANIM_DURATION = 1000;

    public static MainThreadBus eventBus = new MainThreadBus();

    private SimpleCursorAdapter cursorAdapter;
    private int selectedItemIndex;
    private ActionMode actionMode;
    private ListView listView;
    private View undoContainer;
    private FloatingActionButton addEventLineControl;

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

        addEventLineControl = (FloatingActionButton)findViewById(R.id.add_event_line_widget);
        addEventLineControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventLine();
            }
        });

        eventBus.register(this);
    }

    private void addEventLine() {
        addEventLineControl.setEnabled(false);
        showAddEventLineDialog();
    }

    private void showAddEventLineDialog() {
        CreateEventLineDialogFragment createEventLineDialog = new CreateEventLineDialogFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(createEventLineDialog, "createEventLine").commit();
        createEventLineDialog.show(fm, "createEventLine");
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
//                selectedItemIndex = -1;
            }
        });

    }


    private void verticalCollapseSelectedListItem() {
        final View selectedItemView = listView.getChildAt(selectedItemIndex);
        Animation.AnimationListener collapseAnimationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
/*
                listView.remove(selectedItemIndex);
                ViewHolder vh = (ViewHolder)v.getTag();
                vh.needInflate = true;
                mMyAnimListAdapter.notifyDataSetChanged();
*/
                showUndo();
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        };
        collapse(selectedItemView, collapseAnimationListener);
    }

    // https://github.com/paraches/ListViewCellDeleteAnimation/blob/master/src/com/example/myanimtest/MainActivity.java
    private void collapse(final View v, Animation.AnimationListener al) {
        final int initialHeight = v.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                }
                else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        if (al!=null) {
            anim.setAnimationListener(al);
        }
        anim.setDuration(LIST_ITEM_COLLAPSE_ANIM_DURATION);
        v.startAnimation(anim);
    }


    private void showUndo() {
        undoContainer.setVisibility(View.VISIBLE);
        undoContainer.requestLayout();

        final int viewY = findCoords(R.id.undo_bar)[1];

        ObjectAnimator hideUndoAnimator = ObjectAnimator.ofFloat(
                undoContainer, View.Y, viewY, viewY + undoContainer.getHeight());
        hideUndoAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // restore visibility and position of the undo bar
                undoContainer.setVisibility(View.INVISIBLE);
                undoContainer.setY(viewY);
                // make sure undo bar is no longer visible
                // and remove the event line
                deleteEventLine(selectedItemIndex);
            }
        });
        hideUndoAnimator.setStartDelay(2000);
        hideUndoAnimator.start();
    }

    private int[] findCoords(int viewId) {
        View view = findViewById(viewId);
        int[] coords = new int[2];
        if(view != null) {
            coords[0] = (int)(view.getX());
            coords[1] = (int)(view.getY());
        }
        return coords;
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
        startService(intent);
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
                Log.d(TAG, "Swapping the cursor");
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
        Log.d(TAG, "Got a message with msgCode=" + msgCode + ", restarting the loader");
        if(msgCode.intValue() == 3) {
            getLoaderManager().restartLoader(EVENT_LINES_LOADER_ID, null, this);
        }
        cursorAdapter.notifyDataSetChanged();
        addEventLineControl.setEnabled(true);
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        // if event is being added to a simple line, just do it, otherwise
        Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
        intent.setAction(DbAsyncOpsService.ACTION_CREATE_EVENT_LINE);
        int lineType = ((CreateEventLineDialogFragment)dialog).getSelectedType();
        String lineTitle = ((CreateEventLineDialogFragment)dialog).getTitle();
        intent.putExtra(DbSchema.COL_LINE_TYPE, lineType);
        intent.putExtra(DbSchema.COL_TITLE, lineTitle);
        startService(intent);
    }

    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
