package study.stosiki.com.contentproviderpg;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by User on 21/07/2015.
 */
public interface DbSchema {

    String TBL_EVENT_LINES = "event_lines";
    String TBL_EVENTS = "events";

    /* all tables */
    String COL_ID = BaseColumns._ID;

    /* table events */
    String COL_TIMESTAMP = "timestamp";
    String COL_LINE_ID = "line_id";

    /* table event lines */
    String COL_LINE_TYPE = "linetype";
    String COL_TITLE = "title";

    String DDL_CREATE_TBL_EVENTS =
            "CREATE TABLE " + TBL_EVENTS + " " +
            "(" + COL_ID + " INTEGER PRIMARY KEY " +
            COL_TIMESTAMP + " INTEGER NOT NULL " +
            COL_LINE_ID + " INTEGER NOT NULL " +
            "FOREIGN KEY(" + COL_LINE_ID + ") REFERENCES " + TBL_EVENT_LINES + "(" + COL_ID + ")";

    String DDL_CREATE_TBL_EVENT_LINES =
            "CREATE TABLE " + TBL_EVENT_LINES + " " +
            "(" + COL_ID + " INTEGER PRIMARY KEY " +
            COL_LINE_TYPE + " INTEGER NOT NULL " +
            COL_TITLE + " TEXT NOT NULL";

    String DDL_DROP_TBL_EVENTS =
            "DROP TABLE IF EXISTS " + TBL_EVENTS;

    String DDL_DROP_TBL_LINES =
            "DROP TABLE IF EXISTS " + TBL_EVENT_LINES;


    String DML_LINE_TITLE_AND_COUNT = "SELECT COUNT(*), " + TBL_EVENT_LINES + "." + COL_TITLE +
            " FROM " + TBL_EVENTS + " LEFT OUTER JOIN " + TBL_EVENT_LINES + " ON " +
            TBL_EVENTS + "." + COL_LINE_ID + "=" + TBL_EVENT_LINES + "." + COL_ID +
            " WHERE " + TBL_EVENT_LINES + "." + COL_ID + "=";
}
