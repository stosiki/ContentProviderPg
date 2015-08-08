package study.stosiki.com.contentproviderpg;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by User on 20/07/2015.
 */
public class EventLinesDatabaseHelper extends SQLiteOpenHelper implements DbSchema {
    private static final SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = null;
    private static final String DB_FILE_NAME = "some.db";
    private static final int DB_VERSION = 6;

    public EventLinesDatabaseHelper(Context context) {
        super(context, DB_FILE_NAME, DEFAULT_CURSOR_FACTORY, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DbSchema.DBC_PRAGMA_FK);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENT_LINES);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_CREATE_VIEW_EVENT_LINE_LIST_ITEMS);
        fakeData(db);
    }

    private void fakeData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(1, 1, \"Smiles\");");
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(2, 1, \"Nice Girls\");");
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(3, 1, \"Single-speeds\");");
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(4, 1, \"Cups of tea\");");
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(5, 1, \"White cars\");");
        db.execSQL("INSERT INTO EVENT_LINES(_id, linetype, title) "+
               "VALUES(6, 1, \"New Kid\");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbSchema.DDL_DROP_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_DROP_TBL_LINES);
        db.execSQL(DbSchema.DDL_DROP_VIEW_LINE_LIST_ITEMS);
        onCreate(db);
    }
}
