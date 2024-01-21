package com.java.eventregistrationapp;

import static android.content.ContentValues.*;

import android.content.ContentValues;
import 	android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBNAME="Registration.db";
    private static final int DB_VERSION = 1;
    public DBHelper(Context context) {
        super(context, DBNAME,null, 1);
        try {
            copyDataBase(context); // Call the method here
        } catch (IOException e) {
            Log.e("DBHelper","error", e);
        }
    }
    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("create Table users(username TEXT primary key, email TEXT, number TEXT, college TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {
        MyDB.execSQL("drop Table if exists users");

    }

    private void copyDataBase(Context myContext) throws IOException {
        File dbFile = myContext.getDatabasePath(DBNAME);
        if (!dbFile.exists()) {
            try {
                // Open your local db as the input stream
                InputStream myInput = myContext.getAssets().open("databases/" + DBNAME);

                // Path to the just created empty db
                String outFileName = myContext.getApplicationInfo().dataDir + "/databases/" + DBNAME;
                OutputStream myOutput = new FileOutputStream(dbFile);

                // transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }

                // Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();

            } catch (Exception e) {

                Log.e("error", e.toString());

            }
        }
    }

    public Boolean insertData(String username, String email, String number, String college) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("email", email);
        contentValues.put("number", number);
        contentValues.put("college", college);
        long result = MyDB.insert("users", null, contentValues);
        if (result == -1) return false;
        else return true;
    }
    public Boolean checkusername(String username){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ?", new String[]{username});
        if (cursor.getCount()>0)
            return true;
        else
            return false;
    }
    public Boolean checkemail(String username,String email){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ?", new String[]{username, email});
        if (cursor.getCount()>0)
            return true;
        else
            return false;
    }
    public Boolean checknumber(String username, String number){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ?", new String[]{username, number});
        if (cursor.getCount()>0)
            return true;
        else
            return false;
    }
    public Boolean checkcollege(String username, String college){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ?", new String[]{username, college});
        if (cursor.getCount()>0)
            return true;
        else
            return false;
    }

}
