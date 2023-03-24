package com.example.team678_final;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class PedometerDB extends SQLiteOpenHelper {
    public PedometerDB(Context context){
        super(context, "pedometerDB", null, 1);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table pedometerTable(date TEXT PRIMARY KEY, count INTEGER);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists pedometerTable;");
        onCreate(db);
    }

    public int getCount(String date){
        int count = -1;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from pedometerTable where date = '" + date + "';", null);
        while(cursor.moveToNext()) count = cursor.getInt(1);
        return count;
    }

    public ArrayList<PedometerInfo> getCountList(){
        ArrayList<PedometerInfo> countList = new ArrayList<PedometerInfo>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from pedometerTable order by date desc limit 0, 7 ;", null);
        while(cursor.moveToNext()) {
            countList.add(new PedometerInfo(cursor.getString(0),cursor.getString(1)));
        }
        return countList;
    }
}