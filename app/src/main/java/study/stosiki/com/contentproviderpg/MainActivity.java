package study.stosiki.com.contentproviderpg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;

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

    private CursorAdapter cursorAdapter;
    private int eventLinePositionToAddEventTo;
    private ArrayList<Integer> selectedEventLinePositions;
//    private ActionMode actionMode;
    private ListView listView;
    private View undoContainer;
    private FloatingActionButton addEventLineControl;
    private Toolbar toolbar;
    private TextView clickToAddMsg;

    /** state of activity, determines which menu items are available and user actions permitted **/
    private int activityMode;

    private ObjectAnimator hideUndoAnimator;
    private Animation listItemCollapseAnimation;

    private int undoContainerHeight;

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
        clickToAddMsg = (TextView)findViewById(R.id.click_to_add_msg);

        activityMode = MODE_NORMAL;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        cursorAdapter = new CursorAdapter(this, null, 0) {
            @Override
            public Cursor swapCursor(Cursor newCursor) {
                if(newCursor == null || newCursor.getCount() == 0) {
                    clickToAddMsg.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    clickToAddMsg.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                resetSelection();
                return super.swapCursor(newCursor);
            }

            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return inflater.inflate(R.layout.event_line_list_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView idView = (TextView)view.findViewById(R.id._id);
                TextView titleView = (TextView)view.findViewById(R.id.line_title);
                TextView eventCountView = (TextView)view.findViewById(R.id.line_event_count);
                TextView lineTypeView = (TextView)view.findViewById(R.id.line_type);
                TextView aggregateView = (TextView)view.findViewById(R.id.aggregate);
                TextView colorView = (TextView)view.findViewById(R.id.color);
                ImageView symbolView = (ImageView)view.findViewById(R.id.symbol);

                idView.setText(cursor.getString(0));
                titleView.setText(cursor.getString(1));
                eventCountView.setText(cursor.getString(2));
                lineTypeView.setText(String.valueOf(cursor.getInt(3)));
                aggregateView.setText(String.valueOf(cursor.getInt(4)));
                symbolView.getDrawable().setColorFilter(cursor.getInt(5), PorterDuff.Mode.SRC_ATOP);
            }
        };

        undoContainer = (View)findViewById(R.id.undo_bar);
        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LINES_LOADER_ID, null, this);
        listView.setEmptyView(findViewById(R.id.click_to_add_msg));

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
        resetSelection();
        eventBus.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetSelection();
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);
        undoContainer.measure(widthMeasureSpec, heightMeasureSpec);
        undoContainerHeight = undoContainer.getMeasuredHeight();
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
            listView.getChildAt(i).invalidate();
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
            names.add(((TextView) listView.getChildAt(i).
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
        final View footer = findViewById(R.id.main_footer);
        undoContainer.setVisibility(View.VISIBLE);
        undoContainer.requestLayout();
        final int viewY = findCoords(R.id.main_footer)[1];

        ObjectAnimator showFooterAnimator = ObjectAnimator.ofFloat(footer, View.Y, viewY, viewY - undoContainerHeight);
        showFooterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideUndoAnimator.start();
            }
        });
        showFooterAnimator.setDuration(2000);
        showFooterAnimator.start();

        hideUndoAnimator = ObjectAnimator.ofFloat(footer, View.Y, viewY - undoContainerHeight, viewY);
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
                if (isCancelled == false) {
                    deleteEventLine(selectedEventLinePositions.get(0));
                }
                resetSelection();
            }

            private void restoreUndoContainerState() {
                footer.setY(viewY);
                undoContainer.setVisibility(View.INVISIBLE);
            }
        });
        hideUndoAnimator.setStartDelay(4000);
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
                cursorAdapter.swapCursor(cursor);
                cursorAdapter.notifyDataSetChanged();
                cursorAdapter.notifyDataSetInvalidated();
                resetListItemsBgColor();
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
            int color = ((CreateEventLineDialogFragment) dialog).getSelectedColor();
            int aggregate = ((CreateEventLineDialogFragment) dialog).getAggregate();
            intent.putExtra(DbSchema.COL_LINE_TYPE, lineType);
            intent.putExtra(DbSchema.COL_TITLE, lineTitle);
            intent.putExtra(DbSchema.COL_COLOR, color);
            intent.putExtra(DbSchema.COL_AGGREGATE, aggregate);
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
        resumeInput();
    }
}
