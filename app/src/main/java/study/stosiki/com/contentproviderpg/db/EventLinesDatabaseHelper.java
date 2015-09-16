package study.stosiki.com.contentproviderpg.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import study.stosiki.com.contentproviderpg.db.DbSchema;

/**
 * Created by User on 20/07/2015.
 */
public class EventLinesDatabaseHelper extends SQLiteOpenHelper implements DbSchema {
    private static final SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = null;
    private static final String DB_FILE_NAME = "event_lines.db";
    private static final int DB_VERSION = 10;

    public EventLinesDatabaseHelper(Context context) {
        super(context, DB_FILE_NAME, DEFAULT_CURSOR_FACTORY, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DbSchema.DBC_PRAGMA_FK);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENT_LINES);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_CREATE_VIEW_EVENT_LINE_LIST_ITEMS);
        db.execSQL(DbSchema.DDL_CREATE_VIEW_EVENT_REPORT);
        fakeData(db);
    }

    private void fakeData(SQLiteDatabase db) {
        String[] names = new String[]{
                "Smiles", "Nice Girls", "SingleSpeeds", "Cups of Tea", "White Cars", "New Kid"
        };

        String[] colors = new String[]{
                "#d32f2f", "#c2185b", "#7b1fa2", "#d50000", "#c51162", "#aa00ff",
                "#512da8", "#303f9f", "#1976d2", "#6200ea"
        };

        for(int i=0; i<names.length; i++) {
            db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title, color, aggregate) " +
                    "VALUES(" +
                    String.valueOf(i+1) + ", " +
                    "1, " +
                    "\'" + names[i] + "\', " +
                    "\'" + colors[i] + "\', " +
                    String.valueOf(1) +
                    ");");

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbSchema.DDL_DROP_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_DROP_TBL_LINES);
        db.execSQL(DbSchema.DDL_DROP_VIEW_LINE_LIST_ITEMS);
        db.execSQL(DbSchema.DDL_DROP_CHART_REPORT_VIEW);
        onCreate(db);
    }
}
