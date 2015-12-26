package com.chandani.android.salesgoal.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;


/**
 * Created by chandani on 11/20/15.
 */
public class SalesDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sales.db";
    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TBL_GOAL =
            "CREATE TABLE "+ DbEntry.TABLE_GOAL + " ("+
            DbEntry._ID + " INTEGER PRIMARY KEY," +
            DbEntry.COLUMN_NAME_GOAL + TEXT_TYPE + COMMA_SEP +
            DbEntry.COLUMN_NAME_START + TEXT_TYPE + COMMA_SEP +
            DbEntry.COLUMN_NAME_DEADLINE + TEXT_TYPE + COMMA_SEP +
            DbEntry.COLUMN_NAME_TOTAL_AMOUNT + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_TBL_PLAN =
            "CREATE TABLE "+ DbEntry.TABLE_ITEM + " ("+
            DbEntry._ID + " INTEGER PRIMARY KEY," +
            DbEntry.COLUMN_NAME_ITEM + TEXT_TYPE + COMMA_SEP +
            DbEntry.COLUMN_NAME_GOAL + " INTEGER" + COMMA_SEP +
            DbEntry.COLUMN_NAME_PRICE + TEXT_TYPE + COMMA_SEP +
            DbEntry.COLUMN_NAME_PLAN_COUNT + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_TBL_INVEST =
            "CREATE TABLE "+ DbEntry.TABLE_INVEST + " ("+
                    DbEntry._ID + " INTEGER PRIMARY KEY," +
                    DbEntry.COLUMN_NAME_ITEM_ID + " INTEGER" + COMMA_SEP +
                    DbEntry.COLUMN_NAME_GOAL + " INTEGER" + COMMA_SEP +
                    DbEntry.COLUMN_NAME_INVEST_DATE + TEXT_TYPE + COMMA_SEP +
                    DbEntry.COLUMN_NAME_SALES_COUNT + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_TBL_GOAL =
            "DROP TABLE IF EXISTS " + DbEntry.TABLE_GOAL;

    private static final String SQL_DELETE_TBL_ITEM =
            "DROP TABLE IF EXISTS " + DbEntry.TABLE_ITEM;

    private static final String SQL_DELETE_TBL_INVEST =
            "DROP TABLE IF EXISTS " + DbEntry.TABLE_INVEST;

    public SalesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TBL_GOAL);
        db.execSQL(SQL_CREATE_TBL_PLAN);
        db.execSQL(SQL_CREATE_TBL_INVEST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TBL_GOAL);
        db.execSQL(SQL_DELETE_TBL_ITEM);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
