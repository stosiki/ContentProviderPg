/*
 * Content Provider
 * It intentionally doesn't support batch mode
 *
 */
package study.stosiki.com.contentproviderpg;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.sql.SQLException;


public class SomeContentProvider extends ContentProvider {
    /* helper constants for use with URI matcher */
    private static final int EVENT_DIR = 1;
    private static final int EVENT_ID = 2;
    private static final int EVENT_LINE_DIR = 3;
    private static final int EVENT_LINE_ID = 4;
    private static final int EVENT_LINE_LIST_DIR = 5;
    private static final int EVENT_LINE_LIST_ID = 6;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "events", EVENT_DIR);
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "events/#", EVENT_ID);
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "event_lines", EVENT_LINE_DIR);
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "event_lines/#", EVENT_LINE_ID);
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "event_list_items", EVENT_LINE_LIST_DIR);
        URI_MATCHER.addURI(EventLinesContract.AUTHORITY, "event_list_items/#", EVENT_LINE_LIST_ID);
    }

    private final ThreadLocal<Boolean> batchMode = new ThreadLocal<>();

    private SomeDatabaseHelper dbHelper;


    @Override
    public boolean onCreate() {
        dbHelper = new SomeDatabaseHelper(getContext());
        return true;
    }

    @Override
    /*
     * we can only delete event lines. Belonging events are deleted by the
     * db engine on cascade
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int delCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case EVENT_LINE_ID:
                String id = uri.getLastPathSegment();
                String where = DbSchema.COL_ID + " = " + id;
                delCount = database.delete(DbSchema.TBL_EVENT_LINES, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri.toString());
        }

        if(delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delCount;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case EVENT_DIR:
                return EventLinesContract.Events.CONTENT_TYPE;
            case EVENT_ID:
                return EventLinesContract.Events.CONTENT_ITEM_TYPE;
            case EVENT_LINE_DIR:
                return EventLinesContract.EventLines.CONTENT_TYPE;
            case EVENT_LINE_ID:
                return EventLinesContract.EventLines.CONTENT_ITEM_TYPE;
            case EVENT_LINE_LIST_DIR:
                return EventLinesContract.EventLineListItem.CONTENT_TYPE;
            case EVENT_LINE_LIST_ID:
                return EventLinesContract.EventLineListItem.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri.toString());
        }
    }

    @Override
    /*
     * @return null if insert is not successful
     */
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case EVENT_ID:
                long id = database.insert(DbSchema.TBL_EVENTS, null, values);
                return getUriForId(uri, id);
            case EVENT_LINE_ID:
                long lineId = database.insert(DbSchema.TBL_EVENT_LINES, null, values);
                return getUriForId(uri, lineId);
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri.toString());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri)) {
            case EVENT_DIR:
                queryBuilder.setTables(DbSchema.TBL_EVENTS);
                break;
            case EVENT_LINE_DIR:
                queryBuilder.setTables(DbSchema.TBL_EVENT_LINES);
                break;
            case EVENT_LINE_LIST_DIR:
                queryBuilder.setTables(DbSchema.VIEW_LINE_LIST_ITEMS);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri.toString());
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs,
                null, null, sortOrder);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Uri getUriForId(Uri uri, long id) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        return null;
    }
}
