package com.sdcc.zychen.stockdiary.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DiaryDbHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "stockdiary.db";
    public static final String TABLE_DIARY = "diary";

    private static final String DIARY_CREATE_TABLE_SQL = "create table " + TABLE_DIARY + "("
            + "id integer primary key autoincrement,"
            + "title varchar(90) not null,"
            + "write_date varchar(90) not null,"
            + "content_file varchar(90) not null,"
            + "stocklist_file varchar(90) not null"
            + ");";


    public DiaryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DIARY_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
