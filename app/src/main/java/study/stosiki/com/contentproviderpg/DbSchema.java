package study.stosiki.com.contentproviderpg;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by User on 21/07/2015.
 */
public interface DbSchema {

    String TBL_EVENT_LINES = "event_lines";
    String TBL_EVENTS = "events";
    String VIEW_LINE_LIST_ITEMS = "event_line_list_items";

    /* all tables */
    String COL_ID = BaseColumns._ID;

    /* table events */
    String COL_TIMESTAMP = "timestamp";
    String COL_LINE_ID = "line_id";

    /* table event lines */
    String COL_LINE_TYPE = "linetype";
    String COL_TITLE = "title";

    /* view event line list item */
    String COL_EVENT_COUNT = "event_count";

    String DBC_PRAGMA_FK = "PRAGMA foreign keys = ON;";

    String DDL_CREATE_TBL_EVENTS =
            "CREATE TABLE " + TBL_EVENTS + " " +
            "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TIMESTAMP + " INTEGER NOT NULL, " +
            COL_LINE_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COL_LINE_ID + ") REFERENCES " + TBL_EVENT_LINES + "(" + COL_ID + ")" +
            " ON DELETE CASCADE);";

    String DDL_CREATE_TBL_EVENT_LINES =
            "CREATE TABLE " + TBL_EVENT_LINES + " " +
            "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_LINE_TYPE + " INTEGER NOT NULL, " +
            COL_TITLE + " TEXT NOT NULL);";

    String DDL_CREATE_VIEW_EVENT_LINE_LIST_ITEMS =
            //create view event_line_list_items as select event_lines._id, event_lines.line_title,
            //count(events.line_id) as event_count from event_lines
            //left outer join events on event_lines._id=events.line_id group by event_lines._id;
            "CREATE VIEW " + VIEW_LINE_LIST_ITEMS + " AS SELECT " + TBL_EVENT_LINES + "." + COL_ID +
            ", " + TBL_EVENT_LINES + "." + COL_TITLE + ", " + TBL_EVENT_LINES + "." + COL_LINE_TYPE +
            ", COUNT(" + TBL_EVENTS + "." + COL_LINE_ID +
            ") AS " + COL_EVENT_COUNT + " FROM " + TBL_EVENT_LINES + " LEFT OUTER JOIN " +
            TBL_EVENTS + " ON " + TBL_EVENT_LINES + "." + COL_ID + "=" +
            TBL_EVENTS + "." + COL_LINE_ID + " GROUP BY " + TBL_EVENT_LINES + "." + COL_ID +
            ";";

    String DDL_DROP_TBL_EVENTS =
            "DROP TABLE IF EXISTS " + TBL_EVENTS;

    String DDL_DROP_TBL_LINES =
            "DROP TABLE IF EXISTS " + TBL_EVENT_LINES;

    String DDL_DROP_VIEW_LINE_LIST_ITEMS =
            "DROP VIEW IF EXISTS " + VIEW_LINE_LIST_ITEMS;

    String DML_LINE_TITLE_AND_COUNT = "SELECT COUNT(*), " + TBL_EVENT_LINES + "." + COL_TITLE +
            " FROM " + TBL_EVENTS + " LEFT OUTER JOIN " + TBL_EVENT_LINES + " ON " +
            TBL_EVENTS + "." + COL_LINE_ID + "=" + TBL_EVENT_LINES + "." + COL_ID +
            " WHERE " + TBL_EVENT_LINES + "." + COL_ID + "=";
}
