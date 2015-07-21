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

    /* selection clause for ID based queries */
    public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";

    public static final class Events {
        /* content URI for the events table */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(EventLinesContract.CONTENT_URI, "events");
        /* directory MIME type */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.stosiki.eventlines";
        /* single item type */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.stosiki.eventlines";
        /* projection of all columns in the events table */
        public static final String[] PROJECTION_ALL = {}
    }

    public static final class EventLines {

    }
}
