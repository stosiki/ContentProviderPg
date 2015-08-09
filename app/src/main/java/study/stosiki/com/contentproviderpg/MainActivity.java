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
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;


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

    public static MainThreadBus eventBus = new MainThreadBus();

    private SimpleCursorAdapter cursorAdapter;
    private int selectedItemIndex;
    private int selectedForRemovalItemIndex;
    private ActionMode actionMode;
    private ListView listView;
    private View undoContainer;
    private FloatingActionButton addEventLineControl;

    private ObjectAnimator hideUndoAnimator;
    private Animation listItemCollapseAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
/*
                if(convertView != null) {
                    ((TextView)convertView.findViewById(R.id.line_title)).setText()
                }
                return  null;
*/

                View v = super.getView(position, convertView, parent);
                int lineType = Integer.parseInt(
                        (String) ((TextView) v.findViewById(R.id.line_type)).getText()
                );

                return v;
            }
        };

        undoContainer = (View)findViewById(R.id.undo_bar);


        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(EVENT_LINES_LOADER_ID, null, this);


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedForRemovalItemIndex = position;
                changeListItemBgColor(position, R.color.line_to_delete_bg);
                showContextActionBar();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(actionMode != null) {
                    actionMode.finish();
                }
                selectedItemIndex = position;
                int eventLineType = Integer.parseInt(
                        (String) ((TextView) view.findViewById(R.id.line_type)).getText());
                switch(eventLineType) {
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

        addEventLineControl = (FloatingActionButton)findViewById(R.id.add_event_line_widget);
        addEventLineControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventLine();
            }
        });

        eventBus.register(this);
    }

    private void changeListItemBgColor(int position, int highlightColor) {
        int color;
        for(int i=0; i<listView.getChildCount(); i++) {
            if(i == position) {
                color = highlightColor;
            } else {
                color = R.color.line_normal_bg;
            }
            listView.getChildAt(i).setBackgroundColor(getResources().getColor(color));
        }
    }

    private void addEventLine() {
        addEventLineControl.setEnabled(false);
        showAddEventLineDialog();
    }

    private void showAddEventLineDialog() {
        CreateEventLineDialogFragment createEventLineDialog = new CreateEventLineDialogFragment();
        FragmentManager fm = getFragmentManager();
        createEventLineDialog.show(fm, "createEventLine");
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
        final View selectedItemView = listView.getChildAt(selectedForRemovalItemIndex);
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
            @Override
            public void onAnimationCancel(Animator animation) {
                restoreUndoContainerState();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                restoreUndoContainerState();
                deleteEventLine(selectedForRemovalItemIndex);
            }

            private void restoreUndoContainerState() {
                undoContainer.setY(viewY);
                undoContainer.setVisibility(View.INVISIBLE);
            }
        });
        hideUndoAnimator.setStartDelay(2000);
        hideUndoAnimator.start();

    }

    public void onUndoClick(View controlView) {
        // stop undo animation - do it first, because it is undo animation listener
        // that ultimately affects the data structure
        hideUndoAnimator.end();

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
        cursorAdapter.getItem(selectedItemIndex);
        // if event is being added to a simple line, just do it, otherwise
        Intent intent = new Intent(MainActivity.this, DbAsyncOpsService.class);
        intent.setAction(DbAsyncOpsService.ACTION_CREATE_EVENT);
        long lineId = cursorAdapter.getItemId(selectedItemIndex);
        intent.putExtra(DbSchema.COL_LINE_ID, lineId);
        if(data != null) {
            if(data instanceof Integer) {
                intent.putExtra(DbSchema.COL_DATA, ((Integer) data).intValue());
            } else if(data instanceof String) {
                intent.putExtra(DbSchema.COL_DATA, (String) data);
            }
        }
        suspendInput();
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


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
