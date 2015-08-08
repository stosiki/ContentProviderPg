package study.stosiki.com.contentproviderpg;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by User on 21/07/2015.
 */
public class EventLinesContract {
    /* the authority of the eventlines provider */
    public static final String AUTHORITY = "com.stosiki.eventlines";

    /* content URI for the top-level eventlines authority */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static final String MIME_SUBTYPE_EVENT = "/event";
    private static final String MIME_SUBTYPE_EVENTLINE = "/eventline";
    private static final String MIME_SUBTYPE_EVENTLINE_LIST_ITEM = "/eventline_list_item";

    /* selection clause for ID based queries */
    public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";

    public static final class Events {
        /* content URI for the events table */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(EventLinesContract.CONTENT_URI, DbSchema.TBL_EVENTS);
        /* directory MIME type */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_SUBTYPE_EVENT;
        /* single item type */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_SUBTYPE_EVENT;
        /* projection of all columns in the events table */
        public static final String[] PROJECTION_ALL = {
                DbSchema.COL_ID,
                DbSchema.COL_TIMESTAMP,
                DbSchema.COL_DATA,
                DbSchema.COL_LINE_ID
        };
    }

    public static final class EventLines {
        /* content URI for the event_lines table */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(EventLinesContract.CONTENT_URI, DbSchema.TBL_EVENT_LINES);
        /* directory MIME type */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_SUBTYPE_EVENTLINE;
        /* single item type */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_SUBTYPE_EVENTLINE;
        /* projection of all columns in the event_lines table */
        public static final String[] PROJECTION_ALL = {
                DbSchema.COL_ID,
                DbSchema.COL_LINE_TYPE,
                DbSchema.COL_TITLE
        };
    }

    /* constants for a join of events and event_lines tables, used by listview */
    public static final class EventLineListItem {
        /* content URI for the event_lines table */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(EventLinesContract.CONTENT_URI, DbSchema.VIEW_LINE_LIST_ITEMS);
        /* directory MIME type */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_SUBTYPE_EVENTLINE_LIST_ITEM;
        /* single item type */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_SUBTYPE_EVENTLINE_LIST_ITEM;
        /* projection of all columns in the event_lines table */
        public static final String[] PROJECTION_ALL = {
                DbSchema.COL_ID,
                DbSchema.COL_TITLE,
                DbSchema.COL_EVENT_COUNT,
                DbSchema.COL_LINE_TYPE
        };
    }
}
