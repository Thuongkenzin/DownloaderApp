package com.example.downloader.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.downloader.Database.DownloadContract.DownloadEntry;
import com.example.downloader.DownloadChunk.DownloadChunk;
import com.example.downloader.DownloadThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DownloadDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = DownloadDatabaseHelper.class.getSimpleName();
    private static  DownloadDatabaseHelper dataInstance;
    private static final String DATABASE_NAME = "DownloadDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_STEP= ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DownloadEntry.TABLE_NAME + " (" +
            DownloadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DownloadEntry.COLUMN_FILE_NAME + TEXT_TYPE + COMMA_STEP +
            DownloadEntry.COLUMN_FILE_URL + TEXT_TYPE + COMMA_STEP + DownloadEntry.COLUMN_FILE_DIR + TEXT_TYPE + COMMA_STEP +
            DownloadEntry.COLUMN_FILE_LENGTH + " INTEGER" + COMMA_STEP +
            DownloadEntry.COLUMN_FILE_STATE + " INTEGER" + " )";

    private static final String SQL_CREATE_CHUNK_DOWNLOAD= "CREATE TABLE "+ DownloadEntry.TABLE_DOWNLOAD_CHUNK + " ("+
            DownloadEntry.COLUMN_CHUNK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DownloadEntry.COLUMN_START_POSITION + " INTEGER" + COMMA_STEP +
            DownloadEntry.COLUMN_END_POSITION + " INTEGER" + COMMA_STEP+
            DownloadEntry.COLUMN_ID_FILE_DOWNLOAD_FK + " INTEGER REFERENCES "+ DownloadEntry.TABLE_NAME +" )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DownloadEntry.TABLE_NAME;
    private static final String SQL_DELETE_TABLE_CHUNK_DOWNLOADS = "DROP TABLE IF EXISTS " + DownloadEntry.TABLE_DOWNLOAD_CHUNK;
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
        db.execSQL(SQL_CREATE_CHUNK_DOWNLOAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_TABLE_CHUNK_DOWNLOADS);
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
    public void addChunkDownload(long id, List<DownloadChunk> list){
        SQLiteDatabase db = this.getWritableDatabase();
        for(DownloadChunk chunk : list){
            ContentValues values = new ContentValues();
            values.put(DownloadEntry.COLUMN_START_POSITION,chunk.getStart());
            values.put(DownloadEntry.COLUMN_END_POSITION,chunk.getEnd());
            values.put(DownloadEntry.COLUMN_ID_FILE_DOWNLOAD_FK,id);

            int row = db.update(DownloadEntry.TABLE_DOWNLOAD_CHUNK,values,DownloadEntry.COLUMN_CHUNK_ID +"=?",
                    new String[]{String.valueOf(chunk.getId())});
            if(row !=1) {
                long chunkId = db.insertOrThrow(DownloadEntry.TABLE_DOWNLOAD_CHUNK, null, values);
                chunk.setId(chunkId);
            }
        }
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
    public void deleteFileDownload(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DownloadEntry.TABLE_NAME,DownloadEntry._ID + "=?" ,new String[]{String.valueOf(id)});
    }

    public FileDownload getFileItemDownload(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {DownloadEntry._ID, DownloadEntry.COLUMN_FILE_NAME, DownloadEntry.COLUMN_FILE_URL,
                DownloadEntry.COLUMN_FILE_DIR,DownloadEntry.COLUMN_FILE_STATE};
        String selection = DownloadEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(DownloadEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
            FileDownload file = new FileDownload();
            file.set_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry._ID))));
            file.setFileName(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_NAME)));
            file.setState(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_STATE))));
            file.setUriFileDir(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_DIR)));
            file.setUrlDownload(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_URL)));
            return file;
        }
        cursor.close();
        return null;
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
                    file.set_id(Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloadEntry._ID))));
                    file.setFileName(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_NAME)));
                    file.setState(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_STATE))));
                    file.setUriFileDir(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_DIR)));
                    file.setFileLength(Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_FILE_LENGTH))));
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
        db.delete(DownloadEntry.TABLE_DOWNLOAD_CHUNK,null,null);
        db.delete(DownloadEntry.TABLE_NAME,null,null);
    }

    public void addOrUpdateChunkDownloadFile(long idFile,DownloadChunk chunk){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try{
            ContentValues values = new ContentValues();
            values.put(DownloadEntry.COLUMN_ID_FILE_DOWNLOAD_FK,idFile);
            values.put(DownloadEntry.COLUMN_START_POSITION,chunk.getStart());
            values.put(DownloadEntry.COLUMN_END_POSITION,chunk.getEnd());

            if(chunk.getId()!=-1){
                db.update(DownloadEntry.TABLE_DOWNLOAD_CHUNK,values,DownloadEntry.COLUMN_CHUNK_ID + "=?",
                        new String[]{String.valueOf(chunk.getId())});
                db.setTransactionSuccessful();
            }else {
                db.insertOrThrow(DownloadEntry.TABLE_DOWNLOAD_CHUNK, null, values);
                db.setTransactionSuccessful();
            }


        }catch (Exception e){
            Log.d(TAG, "Error while trying to add file chunk download to database");
        }finally {
            db.endTransaction();
        }
    }

    public long addOrUpdateFileDownload(FileDownload file){
        SQLiteDatabase db = getWritableDatabase();
        long fileId = -1;

        db.beginTransaction();
        try{

            ContentValues values = new ContentValues();
            values.put(DownloadEntry.COLUMN_FILE_NAME, file.getFileName());
            values.put(DownloadEntry.COLUMN_FILE_URL, file.getUrlDownload());
            values.put(DownloadEntry.COLUMN_FILE_STATE,file.getState());
            values.put(DownloadEntry.COLUMN_FILE_LENGTH,file.getFileLength());
            values.put(DownloadEntry.COLUMN_FILE_DIR,file.getUriFileDir());

            //First try to update in case the file download already exists in the database
            int rows = db.update(DownloadEntry.TABLE_NAME,values,DownloadEntry.COLUMN_FILE_NAME +"= ?",new String[]{file.getFileName()});

            //check if update success
            if(rows == 1){
                //get the primary key of the file download which just updated
                String fileSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?", DownloadEntry._ID,DownloadEntry.TABLE_NAME,DownloadEntry.COLUMN_FILE_NAME);
                Cursor cursor = db.rawQuery(fileSelectQuery, new String[]{file.getFileName()});
                try{
                    if(cursor.moveToFirst()){
                        fileId = cursor.getInt(cursor.getColumnIndex(DownloadEntry._ID));
                        db.setTransactionSuccessful();
                    }
                }finally {
                    if(cursor!=null && !cursor.isClosed()){
                        cursor.close();
                    }
                }
            }else {
                //file did not already exist, so insert new file
                fileId = db.insertOrThrow(DownloadEntry.TABLE_NAME,null,values);
                db.setTransactionSuccessful();
            }
        }catch (Exception e){
            Log.d(TAG, "Error while trying to update or insert file download");
        }finally {
            db.endTransaction();
        }

        return fileId;

    }
    public List<FileChunk> getAllChunkDownload(long idFileDownload){
        List<FileChunk> chunks = new ArrayList<>();
        String CHUNKS_SELECT_QUERY = String.format("SELECT * FROM %s WhERE %s = ?",
                DownloadEntry.TABLE_DOWNLOAD_CHUNK,
                DownloadEntry.COLUMN_ID_FILE_DOWNLOAD_FK);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CHUNKS_SELECT_QUERY,new String[]{String.valueOf(idFileDownload)});
        try{
            if(cursor.moveToFirst()){
                do{
                    FileChunk fileChunk = new FileChunk();
                    fileChunk.setStartPosition(Long.parseLong(cursor.getString(cursor.getColumnIndex(
                            DownloadEntry.COLUMN_START_POSITION))));
                    fileChunk.setEndPosition(Long.parseLong(cursor.getString(cursor.getColumnIndex(
                            DownloadEntry.COLUMN_END_POSITION))));
                    fileChunk.setId(Long.parseLong(cursor.getString(cursor.getColumnIndex(
                            DownloadEntry.COLUMN_CHUNK_ID))));
                    fileChunk.setIdFileDownload(Long.parseLong(cursor.getString(cursor.getColumnIndex(
                            DownloadEntry.COLUMN_ID_FILE_DOWNLOAD_FK))));
                    chunks.add(fileChunk);

                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            Log.d(TAG,"Error while trying to get chunks from database");
        }finally {
            if(cursor!= null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return chunks;
    }

}

