package com.example.memorymap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    //open database connection
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    //close database connection
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    //put all info in column into a string list
    public List<String> getInfoColumn(int column) {
        List<String> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM memory_table", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(column));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    //insert/ update info, add image blob after selecting new icon
    public void addBlobInfo(int row, byte[] add){
        //String name = getInfoColumn(0).get(row);
        String[] rowArray = {String.valueOf(row)};

        ContentValues cv = new ContentValues();
        cv.put("Icon", add);
        database.update("memory_table", cv, "RowId = ?", rowArray);
    }

    public void addTag(int row, String tag){
        //String[] name = {getInfoColumn(0).get(row)};
        String[] rowArray = {String.valueOf(row)};

        ContentValues cv = new ContentValues();
        cv.put("Tag", tag);
        //database.update("memory_table", cv, "Location = ?", name);
        database.update("memory_table", cv, "RowId = ?", rowArray);

    }

    public void addConnInfo(int row, String connNum){
        String[] rowArray = {String.valueOf(row)};

        ContentValues cv = new ContentValues();

        //cv.put("Connection", connNum);
        String existing = getInfoColumn(6).get(row);
        cv.put("Connection", existing + connNum + ",");

        //database.update("memory_table", cv, "Location = ?", name);
        //database.update("memory_table", cv, "RowId = ?", rowArray);

        database.beginTransaction();
        try {
            int out = database.update("memory_table", cv, "RowId = ?", rowArray);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
        } finally {
            database.endTransaction();
        }
    }

    public void updateRow (int row, String prasang, String triggers, String tag){
        //String[] name = {getInfoColumn(0).get(row)};
        String[] rowArray = {String.valueOf(row)};

        ContentValues cv = new ContentValues();

        cv.put("Prasang", prasang);
        cv.put("Trigger", triggers);
        cv.put("Tag", tag);

        try {
            database.update("memory_table", cv, "RowId = ?", rowArray);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
        } finally {
            database.endTransaction();
        }
    }

    public List<Bitmap> getBitmaps(int column){
        List<Bitmap> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM memory_table", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try{
                byte[] bitmapdata = cursor.getBlob(column);
                if (bitmapdata!= null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                    list.add(bitmap);
                }
            } catch(Exception e){}
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public void addNewLocation(double latitude, double longitude, String title, String tag, String trigger, String prasang){
        int row = getInfoColumn(0).size();
        try{
            ContentValues cv = new ContentValues();
            cv.put("Location", title);
            cv.put("Latitude", latitude);
            cv.put("Longitude", longitude);
            cv.put("Tag", tag);
            cv.put("Trigger", trigger);
            cv.put("Prasang", prasang);
            cv.put("RowId", row);
            cv.put("Connection", "NULL");
            database.insert("memory_table", null, cv);
        }catch(Exception e){}

    }

    public byte[] getBlob(int row){
        Cursor cursor = database.rawQuery("SELECT * FROM memory_table", null);
        cursor.moveToFirst();
        int place = 0;
        while (place != (row)) {
            place++;
            cursor.moveToNext();
        }
        byte[] blob = cursor.getBlob(4);
        return blob;
    }

    public void deleteRow (int row){
        String[] rowArray = {String.valueOf(row)};
        database.delete("memory_table", "RowId = ?", rowArray);
    }
}
