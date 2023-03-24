package com.example.team678_final;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MealInfoDB extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "mealInfoTable";

    private static final String CREATE_TABLE= "create table if not exists " + TABLE_NAME + "(" +
            "code integer primary key autoincrement, " +
            "name varchar(20), " +
            "calorie integer(10), " +
            "gram integer(10));";

    public MealInfoDB(Context context) {
        super(context, "mealInfoDB", null, 1);
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

    public ArrayList<String> setSearchWord() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + ";", null);
        ArrayList<String> searchWord = new ArrayList();
        while(cursor.moveToNext()) {
            searchWord.add(cursor.getString(1));
        }
        return searchWord;
    }

    public static ArrayList<Object> getmealInfo(SQLiteDatabase db, int code) {
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where code = " + code + ";", null);
        ArrayList<Object> mealInfo = new ArrayList();
        while(cursor.moveToNext()) {
            mealInfo.add(cursor.getString(1));
            mealInfo.add(cursor.getInt(2));
            mealInfo.add(cursor.getInt(3));
        }
        return mealInfo;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.disableWriteAheadLogging();
    }
}