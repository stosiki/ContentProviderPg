package study.stosiki.com.contentproviderpg;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by User on 20/07/2015.
 */
public class SomeDatabaseHelper extends SQLiteOpenHelper implements DbSchema {
    private static final SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = null;
    private static final String DB_FILE_NAME = "some.db";
    private static final int DB_VERSION = 1;

    public SomeDatabaseHelper(Context context) {
        super(context, DB_FILE_NAME, DEFAULT_CURSOR_FACTORY, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbSchema.DBC_PRAGMA_FK);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENT_LINES);
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_CREATE_VIEW_EVENT_LINE_LIST_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbSchema.DDL_DROP_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_DROP_TBL_LINES);
        db.execSQL(DbSchema.DDL_DROP_VIEW_LINE_LIST_ITEMS);
        onCreate(db);
    }
}
