package com.runvision.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FaceDataHelper extends SQLiteOpenHelper {
    public static String TAG = FaceDataHelper.class.getSimpleName();

    //private static final String name = "/mnt/sdcard/temp/FaceAndroid.db";
   // private static final int version = 1; //数据库版本

    private final static String CREATE_USER_TABLE = "create table tUser(id INTEGER primary key autoincrement,name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,templateImageID TEXT,createTime INTEGER);";
    private final static String CREATE_RECORD_TABLE = "create table tRecord(id INTEGER primary key autoincrement,name TEXT,type TEXT, sex TEXT, age INTEGER,workNo TEXT,cardNo TEXT,createTime INTEGER,snapImageID TEXT,templateImageID TEXT,score TEXT,comperResult TEXT,comperType)";
    private final static String CREATE_ADMIN_TABLE = "create table tAdmin(id INTEGER primary key autoincrement,username TEXT,password TEXT)";

    public FaceDataHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_RECORD_TABLE);
        db.execSQL(CREATE_ADMIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
