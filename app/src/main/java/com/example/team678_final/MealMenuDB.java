package com.example.team678_final;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MealMenuDB extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "mealMenuTable";

    private static final String CREATE_TABLE= "create table if not exists " + TABLE_NAME + "(" +
            "date varchar(20) primary key, " +
            "num inteager(5), " +
            "code inteager(10)," +
            "time varchar(20));";

    public MealMenuDB(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public void deleteMenu(String date) {
        SQLiteDatabase db = getReadableDatabase();
        String str = "delete from " + TABLE_NAME + " where date = '" + date + "'";
        Log.e("sql", str);
        db.execSQL(str);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(database);
    }
}

