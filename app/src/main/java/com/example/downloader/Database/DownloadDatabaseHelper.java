package com.example.downloader.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.downloader.Database.DownloadContract.DownloadEntry;
import com.example.downloader.DownloadThread;

import java.util.ArrayList;
import java.util.List;

public class DownloadDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = DownloadDatabaseHelper.class.getSimpleName();
    private static  DownloadDatabaseHelper dataInstance;
    private static final String DATABASE_NAME = "DownloadDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_STEP= ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DownloadEntry.TABLE_NAME + " (" +
            DownloadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DownloadEntry.COLUMN_FILE_NAME + TEXT_TYPE + COMMA_STEP
            + DownloadEntry.COLUMN_FILE_URL + TEXT_TYPE + COMMA_STEP + DownloadEntry.COLUMN_FILE_DIR + TEXT_TYPE +
            COMMA_STEP + DownloadEntry.COLUMN_FILE_STATE + " INTEGER" + " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DownloadEntry.TABLE_NAME;
    private DownloadDatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    public static synchronized DownloadDatabaseHelper getInstance(Context context){
        if(dataInstance == null){
            dataInstance = new DownloadDatabaseHelper(context.getApplicationContext());
        }
        return dataInstance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addFile(FileDownload file){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DownloadEntry.COLUMN_FILE_NAME, file.getFileName());
        values.put(DownloadEntry.COLUMN_FILE_URL, file.getUrlDownload());
        values.put(DownloadEntry.COLUMN_FILE_STATE,file.getState());
        values.put(DownloadEntry.COLUMN_FILE_DIR,file.getUriFileDir());

        db.insert(DownloadEntry.TABLE_NAME,null, values);


    }

    public int getLastItemIdDownload(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + DownloadEntry.TABLE_NAME;

        int positionId = -1;
        Cursor cursor = db.rawQuery(selectQuery, null);
        try{
            if(cursor.moveToLast()){
                positionId =Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry._ID)));
            }
        }catch (Exception e){
            Log.d(TAG, "Error while trying to get file list from database");
        }finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return positionId;
    }

    public int updateFileDownload(FileDownload file){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DownloadEntry.COLUMN_FILE_NAME, file.getFileName());
        values.put(DownloadEntry.COLUMN_FILE_URL, file.getUrlDownload());
        values.put(DownloadEntry.COLUMN_FILE_STATE,file.getState());
        values.put(DownloadEntry.COLUMN_FILE_DIR,file.getUriFileDir());

        return db.update(DownloadEntry.TABLE_NAME,values,DownloadEntry._ID + "=?" ,new String[]{String.valueOf(file.get_id())});

    }
    public void deleteFileDownload(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DownloadEntry.TABLE_NAME,DownloadEntry._ID + "=?" ,new String[]{String.valueOf(id)});
    }

    public List<FileDownload> getAllFileDownload(){
        List<FileDownload> fileLists = new ArrayList<FileDownload>();
        String selectQuery = "SELECT * FROM " + DownloadEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    FileDownload file = new FileDownload();
                    file.set_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry._ID))));
                    file.setFileName(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_NAME)));
                    file.setState(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_STATE))));
                    file.setUriFileDir(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_DIR)));
                    file.setUrlDownload(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_URL)));

                    fileLists.add(file);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get file list from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return fileLists;

    }

    public void deleteAllFileDownload(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DownloadEntry.TABLE_NAME,null,null);
    }
    //Database info
//    private static final String TAG = "DownloadDataHelper";
//    private static final String DATABASE_NAME = "DownloadDatabase";
//    private static final int DATABASE_VERSION = 1;
//
//    //Table name
//    private static final String TABLE_FILE = "filedownloads";
//
//    //file table column
//    private static final String FILE_ID ="_id";
//    private static final String FILE_NAME = "file_name";
//    private static final String FILE_URL = "url_file";
//    private static  DownloadDatabaseHelper dataInstance;
//
//    public static synchronized DownloadDatabaseHelper getInstance(Context context){
//        if(dataInstance == null){
//            dataInstance = new DownloadDatabaseHelper(context.getApplicationContext());
//        }
//        return dataInstance;
//    }
//    private DownloadDatabaseHelper(Context context){
//        super(context,DATABASE_NAME,null,DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String CREATION_DOWNLOAD_TABLE = "CREATE TABLE " + TABLE_FILE + "(" + FILE_ID + " INTEGER PRIMARY KEY,"
//                + FILE_NAME + " TEXT," + FILE_URL + " TEXT" +")";
//        db.execSQL(CREATION_DOWNLOAD_TABLE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if(oldVersion != newVersion) {
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE);
//            onCreate(db);
//        }
//    }
//
//    public void addFile(FileDownload fileDownload){
//        SQLiteDatabase db = getWritableDatabase();
//
//        db.beginTransaction();
//
//    }
//
//    public long addOrUpdateFile(FileDownload fileDownload){
//        SQLiteDatabase db = getWritableDatabase();
//        long fileId = -1;
//
//        db.beginTransaction();
//        try {
//            ContentValues values = new ContentValues();
//            values.put(FILE_NAME, fileDownload.fileName);
//            values.put(FILE_URL, fileDownload.urlDownload);
//
//            int rows = db.update(TABLE_FILE, values, FILE_NAME + "= ?", new String[]{fileDownload.fileName});
//
//            //check if update succeeded
//            if (rows == 1) {
//                String fileSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?", FILE_ID, TABLE_FILE, FILE_NAME);
//                Cursor cursor = db.rawQuery(fileSelectQuery, new String[]{String.valueOf(fileDownload.fileName)});
//                try {
//                    if (cursor.moveToFirst()) {
//                        fileId = cursor.getInt(0);
//                        db.setTransactionSuccessful();
//                    }
//                } finally {
//                    if (cursor != null && !cursor.isClosed()) {
//                        cursor.close();
//                    }
//                }
//            } else {
//                fileId = db.insertOrThrow(TABLE_FILE, null, values);
//                db.setTransactionSuccessful();
//            }
//
//        } catch (Exception e) {
//            Log.d(TAG, "Error while trying to add or update user");
//        } finally {
//            db.endTransaction();
//        }
//        return fileId;
//    }
//
//
//    public List<FileDownload> getAllFileDownload(){
//        List<FileDownload> listDownload = new ArrayList<>();
//
//
//    }

}

