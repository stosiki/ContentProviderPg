package study.stosiki.com.contentproviderpg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import study.stosiki.com.contentproviderpg.db.DbAsyncOpsService;
import study.stosiki.com.contentproviderpg.db.DbSchema;
import study.stosiki.com.contentproviderpg.db.EventLinesContract;
import study.stosiki.com.contentproviderpg.events.EventLine;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        CreateEventLineDialogFragment.DialogListener,
        EventNumericPropertyDialogFragment.DialogListener,
        EventStringPropertyDialogFragment.DialogListener
{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int EVENT_LINES_LOADER_ID = 1;

    private static final long LIST_ITEM_COLLAPSE_ANIM_DURATION = 500;
    private static final long UNDO_BAR_HIDE_ANIM_DURATION = 1000;

    public static final int MAX_SELECTION_SIZE = 2;

    private static final int MODE_LINE_SELECTED = 1;
    private static final int MODE_NORMAL = 2;

    public static MainThreadBus eventBus = new MainThreadBus();

    private SimpleCursorAdapter cursorAdapter;
    private int eventLinePositionToAddEventTo;
    private ArrayList<Integer> selectedEventLinePositions;
//    private ActionMode actionMode;
    private ListView listView;
    private View undoContainer;
    private FloatingActionButton addEventLineControl;
    private Toolbar toolbar;

    /** state of activity, determines which menu items are available and user actions permitted **/
    private int activityMode;

    private ObjectAnimator hideUndoAnimator;
    private Animation listItemCollapseAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addEventLineControl = (FloatingActionButton)findViewById(R.id.add_event_line_widget);
        addEventLineControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventLine();
            }
        });

        activityMode = MODE_NORMAL;

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.event_line_list_item,
                null,
                new String[]{DbSchema.COL_ID, DbSchema.COL_TITLE, DbSchema.COL_EVENT_COUNT,
                        DbSchema.COL_LINE_TYPE},
                new int[]{R.id._id, R.id.line_title, R.id.line_event_count,
                        R.id.line_type},
                0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // TODO: ViewHolder etc.
                View v = super.getView(position, convertView, parent);
                Object o = getItem(position);
                return v;
            }
        };

        undoContainer = (View)findViewById(R.id.undo_bar);


        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LINES_LOADER_ID, null, this);

        selectedEventLinePositions = new ArrayList<Integer>();
        resetSelection();


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setSelected(position, !isSelected(position));
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                resetSelection();

                eventLinePositionToAddEventTo = position;
                int eventLineType = Integer.parseInt(
                        (String) ((TextView) view.findViewById(R.id.line_type)).getText());
                switch (eventLineType) {
                    case 0:
                        addEventToLine(null);
                        break;
                    case 1: // Numeric
                        // raise number input dialog, event added from the callback
                        showNumericPropertyDialog();
                        break;
                    case 2: // Comment
                        // raise string input dialog
                        showStringPropertyDialog();
                        break;
                }
            }
        });

        eventBus.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetSelection();
    }

    private void highlightSelectedItems() {
        int color;
        for(int i=0; i<listView.getChildCount(); i++) {
            if(isSelected(i)) {
                color = R.color.selected_event_line_bg;
            } else {
                color = R.color.event_line_item_bg;
            }
            listView.getChildAt(i).setBackgroundColor(getResources().getColor(color));
        }
    }

    private void resetListItemsBgColor() {
        for(int i=0; i<listView.getChildCount(); i++) {
            listView.getChildAt(i).setBackgroundColor(
                    getResources().getColor(R.color.event_line_item_bg));
        }
    }

    private void addEventLine() {
        addEventLineControl.setEnabled(false);
        showAddEventLineDialog();
    }

    private void showAddEventLineDialog() {
        CreateEventLineDialogFragment createEventLineDialog = new CreateEventLineDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("line_names", lineNames());
        createEventLineDialog.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        createEventLineDialog.show(fm, "createEventLine");
    }

    private ArrayList<String> lineNames() {
        ArrayList<String> names = new ArrayList<>();
        for(int i=0; i<listView.getChildCount(); i++) {
            names.add(((TextView)listView.getChildAt(i).
                    findViewById(R.id.line_title)).getText().toString());
        }
        return names;
    }

    private void showNumericPropertyDialog() {
        EventNumericPropertyDialogFragment propertyDialog =
                new EventNumericPropertyDialogFragment();
        FragmentManager fm = getFragmentManager();
        propertyDialog.show(fm, "numericPropertyDialog");
    }

    private void showStringPropertyDialog() {
        EventStringPropertyDialogFragment propertyDialog =
                new EventStringPropertyDialogFragment();
        FragmentManager fm = getFragmentManager();
        propertyDialog.show(fm, "stringPropertyDialog");
    }


    private void verticalCollapseSelectedListItem() {
        final View selectedItemView = listView.getChildAt(selectedEventLinePositions.get(0));

        Animation.AnimationListener collapseAnimationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                showUndo();
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        };

        collapse(selectedItemView, collapseAnimationListener);
    }

    private void collapse(final View v, Animation.AnimationListener al) {
        listItemCollapseAnimation = new ListItemCollapseAnimation(v, listView);
        if (al!=null) {
            listItemCollapseAnimation.setAnimationListener(al);
        }
        listItemCollapseAnimation.setDuration(LIST_ITEM_COLLAPSE_ANIM_DURATION);
        v.startAnimation(listItemCollapseAnimation);
    }

    private void showUndo() {
        undoContainer.setVisibility(View.VISIBLE);
        undoContainer.requestLayout();

        final int viewY = findCoords(R.id.undo_bar)[1];

        hideUndoAnimator = ObjectAnimator.ofFloat(
                undoContainer, View.Y, viewY, viewY + undoContainer.getHeight());
        hideUndoAnimator.addListener(new AnimatorListenerAdapter() {
            // this ugly flag is because onAnimationEnd is called either way
            private boolean isCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                animation.
                restoreUndoContainerState();
                if(isCancelled == false) {
                    deleteEventLine(selectedEventLinePositions.get(0));
                }
                resetSelection();
            }

            private void restoreUndoContainerState() {
                undoContainer.setY(viewY);
                undoContainer.setVisibility(View.INVISIBLE);
            }
        });
        hideUndoAnimator.setStartDelay(2000);
        hideUndoAnimator.start();

    }

    private void resetSelection() {
        selectedEventLinePositions.clear();
        setMode(MODE_NORMAL);
        highlightSelectedItems();
    }

    private boolean isSelected(int position) {
        for(Integer idx : selectedEventLinePositions) {
            if(idx == position) {
                return true;
            }
        }
        return false;
    }

    private void setSelected(int position, boolean selected) {
        if(selected && selectedEventLinePositions.size() <= MAX_SELECTION_SIZE) {
            selectedEventLinePositions.add(position);
        } else {
            // need to wrap int because otherwise treated as an index by remove
            selectedEventLinePositions.remove(new Integer(position));
        }

        setMode(selectedEventLinePositions.size() > 0 ? MODE_LINE_SELECTED : MODE_NORMAL);
        highlightSelectedItems();
    }

    private void setMode(int mode) {
        activityMode = mode;
        invalidateOptionsMenu();
        addEventLineControl.setEnabled(activityMode == MODE_NORMAL);
    }

    public void onUndoClick(View controlView) {
        // stop undo animation - do it first, because it is undo animation listener
        // that ultimately affects the data structure
        hideUndoAnimator.cancel();

        // stop collapse animation
        listItemCollapseAnimation.cancel();
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
        Log.d(TAG, "Event line title to be deleted: " + lineId);
        intent.setAction(DbAsyncOpsService.ACTION_DELETE_EVENT_LINE);
        intent.putExtra(BaseColumns._ID, lineId);
        suspendInput();
        MainActivity.this.startService(intent);
    }

    private void addEventToLine(Object data) {
        // if event is being added to a simple line, just do it, otherwise
        Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
        intent.setAction(DbAsyncOpsService.ACTION_CREATE_EVENT);
        long lineId = cursorAdapter.getItemId(eventLinePositionToAddEventTo);
        intent.putExtra(DbSchema.COL_LINE_ID, lineId);
        if(data != null) {
            if(data instanceof Integer) {
                intent.putExtra(DbSchema.COL_DATA, ((Integer) data).intValue());
                intent.putExtra(DbSchema.COL_LINE_TYPE, EventLine.LINE_TYPE_INTEGER);
            } else if(data instanceof String) {
                intent.putExtra(DbSchema.COL_DATA, (String) data);
                intent.putExtra(DbSchema.COL_LINE_TYPE, EventLine.LINE_TYPE_STRING);
            }
        }
        suspendInput();
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (activityMode) {
            case MODE_NORMAL:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
            case MODE_LINE_SELECTED:
                getMenuInflater().inflate(R.menu.action_mode_row_selected, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.menu_item_delete_eventline:
                //deleteEventLine(eventLinePositionToAddEventTo);
                // start animation to hide the cell
                verticalCollapseSelectedListItem();
                setMode(MODE_NORMAL);
                return true;
            case R.id.menu_item_show_report:
//                        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                long[] lineIds = new long[selectedEventLinePositions.size()];
                for(int i=0; i<selectedEventLinePositions.size(); i++) {
                    lineIds[i] = cursorAdapter.getItemId(selectedEventLinePositions.get(i));
                }
                intent.putExtra(DbSchema.COL_LINE_ID, lineIds);
                setMode(MODE_NORMAL);

                startActivity(intent);
                return true;
            default:
                return false;
        }    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
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
                resetListItemsBgColor();
                cursorAdapter.swapCursor(cursor);
                cursorAdapter.notifyDataSetChanged();
                cursorAdapter.notifyDataSetInvalidated();
                break;
            default:
                Log.e(TAG, "Unknown adapter id");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "Loader reset");
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
        Log.d(TAG, "Got a message with msgCode=" + msgCode);
        getLoaderManager().restartLoader(EVENT_LINES_LOADER_ID, null, this);
        Log.d(TAG, "Loader restarted");
        resumeInput();
        highlightSelectedItems();
    }

    private void resumeInput() {
        listView.setEnabled(true);
        addEventLineControl.setEnabled(true);
    }

    private void suspendInput() {
        listView.setEnabled(false);
        addEventLineControl.setEnabled(false);
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        if(dialog instanceof CreateEventLineDialogFragment) {
            // if event is being added to a simple line, just do it, otherwise
            Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
            intent.setAction(DbAsyncOpsService.ACTION_CREATE_EVENT_LINE);
            int lineType = ((CreateEventLineDialogFragment) dialog).getSelectedType();
            String lineTitle = ((CreateEventLineDialogFragment) dialog).getTitle();
            intent.putExtra(DbSchema.COL_LINE_TYPE, lineType);
            intent.putExtra(DbSchema.COL_TITLE, lineTitle);
            suspendInput();
            startService(intent);
        } else if(dialog instanceof EventNumericPropertyDialogFragment) {
            int data = ((EventNumericPropertyDialogFragment) dialog).getData();
            addEventToLine(data);
        }  else if(dialog instanceof EventStringPropertyDialogFragment) {
            String data = ((EventStringPropertyDialogFragment) dialog).getData();
            addEventToLine(data);
        }
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
