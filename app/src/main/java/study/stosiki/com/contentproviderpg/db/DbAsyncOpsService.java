package study.stosiki.com.contentproviderpg.db;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Date;

import study.stosiki.com.contentproviderpg.events.EventLine;

/**
 * Created by mike on 7/23/2015.
 */
public class DbAsyncOpsService extends IntentService {
    /**
     * Actions
     */
    public static final String ACTION_CREATE_EVENT = "actionCreateEvent";
    public static final String ACTION_CREATE_EVENT_LINE = "actionCreateEventLine";
    public static final String ACTION_DELETE_EVENT_LINE = "actionDeleteEventLine";

    public static final Object ERROR_CREATING_EVENT = 1;
    public static final Object ERROR_CREATING_EVENT_LINE = 2;
    public static final Object ERROR_DELETING_EVENT_LINE = 3;

    public DbAsyncOpsService() {
        super("DbAsyncOpsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(action.equals(ACTION_CREATE_EVENT)) {
            onActionCreateEvent(intent);
        } else if(action.equals(ACTION_CREATE_EVENT_LINE)) {
            onActionCreateEventLine(intent);
        } else if(action.equals(ACTION_DELETE_EVENT_LINE)) {
            onActionDeleteEventLine(intent);
        }
    }

    private void onActionCreateEvent(Intent intent) {
//        Uri insertUri = ContentUris.withAppendedId(EventLinesContract.Events.CONTENT_URI);
        long lineId = intent.getLongExtra(DbSchema.COL_LINE_ID, -1);
        if(lineId == -1) {
            throw new IllegalArgumentException("Wrong line _id");
        } else {
            ContentValues values = new ContentValues();
            values.put(DbSchema.COL_LINE_ID, lineId);
            values.put(DbSchema.COL_TIMESTAMP, new Date().getTime());
            int lineType = intent.getIntExtra(DbSchema.COL_LINE_TYPE, -1);
            switch (lineType) {
                case EventLine.LINE_TYPE_INTEGER:
                    Integer intData = intent.getIntExtra(DbSchema.COL_DATA, -1);
                    values.put(DbSchema.COL_DATA, intData);
                    break;
                case EventLine.LINE_TYPE_STRING:
                    String stringData = intent.getStringExtra(DbSchema.COL_DATA);
                    values.put(DbSchema.COL_DATA, stringData);
                    break;
            }

            getContentResolver().insert(EventLinesContract.Events.CONTENT_URI, values);
        }
    }

    private void onActionCreateEventLine(Intent intent) {
        int lineType = intent.getIntExtra(DbSchema.COL_LINE_TYPE, -1);
        String lineTitle = intent.getStringExtra(DbSchema.COL_TITLE);
        ContentValues values = new ContentValues();
        values.put(DbSchema.COL_LINE_TYPE, lineType);
        values.put(DbSchema.COL_TITLE, lineTitle);
        getContentResolver().insert(EventLinesContract.EventLines.CONTENT_URI, values);
    }

    private void onActionDeleteEventLine(Intent intent) {
        long lineId = intent.getLongExtra(BaseColumns._ID, -1);
        if(lineId == -1) {
            throw new IllegalArgumentException("Wrong line _id");
        } else {
            Uri delUri =
                    ContentUris.withAppendedId(EventLinesContract.EventLines.CONTENT_URI, lineId);
            getContentResolver().delete(delUri, null, null);
        }
    }
}
