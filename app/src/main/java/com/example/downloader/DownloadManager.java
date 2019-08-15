package com.example.downloader;

import android.content.Context;
import android.util.Log;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.Database.FileDownload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private static int THREAD_POOL_SIZES = 3;
    private static DownloadManager instance;
    private List<DownloadThread> listDownload;
    private List<FileDownload> completeListDownload;
    private UpdateListDownloadListener listenerUpdate;

    private List<DownloadMultipleChunk> listDownloadFile;
    private DownloadManager() {
       // listDownload = new ArrayList<DownloadThread>();
        listDownloadFile = new ArrayList<>();
        completeListDownload = new ArrayList<FileDownload>();
    }

    public static int ID = 0;
    private ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZES);

    public void setOnUpdateListDownloadListener(UpdateListDownloadListener listener) {
        this.listenerUpdate = listener;
    }

    public interface UpdateListDownloadListener {
        void updateList(FileDownload fileDownload);
    }

    public List<FileDownload> getCompleteListDownload() {
        return completeListDownload;
    }

    public void setCompleteListDownload(List<FileDownload> completeListDownload) {
        this.completeListDownload = completeListDownload;
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    public List<DownloadMultipleChunk> getListDownloadFile(){
        return listDownloadFile;
    }
//    public List<DownloadThread> getListDownload() {
//        return listDownload;
//    }

    public ExecutorService getPool() {
        return pool;
    }

    public void startAllDownload() {
        for (DownloadThread thread : listDownload) {
            pool.submit(thread);
            thread.onPause();
        }
    }

    public void startUrlDownload(String url) {
        DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(url);
        listDownloadFile.add(0,downloadTask);
        pool.submit(downloadTask);

    }

//    public void addFileDownloadToData(Context context, String url) {
//        DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
//        FileDownload fileDownload = new FileDownload(url, DownloadContract.DownloadEntry.STATE_UNCOMPLETE);
//        Log.d("DownloadManager", "fileDownloadid" + fileDownload.get_id());
//        databaseHelper.addFile(fileDownload);
//        fileDownload.set_id(databaseHelper.getLastItemIdDownload());
//        Log.d("DownloadManager", "fileDownloadIdAfter:" + fileDownload.get_id());
//        DownloadThread newDownload = new DownloadThread(fileDownload.get_id(), fileDownload.getUrlDownload());
//        listDownload.add(0, newDownload);
//        pool.submit(newDownload);
//
//    }


    public void getListDownloadFromDatabase(Context context) {
        DownloadDatabaseHelper dataInstance = DownloadDatabaseHelper.getInstance(context);
        List<FileDownload> fileList = dataInstance.getAllFileDownload();
        for (FileDownload file : fileList) {
            if (file.getState() == DownloadContract.DownloadEntry.STATE_UNCOMPLETE) {
                listDownload.add(new DownloadThread(file.get_id(), file.getUrlDownload()));
            } else {
                //add complete download file
                completeListDownload.add(file);
            }
        }
    }


    public void cancelDownload(int id, Context context) {
        for (int i = 0; i < listDownloadFile.size(); i++) {
            DownloadMultipleChunk thread = listDownloadFile.get(i);
            if (thread.getId() == id) {
                thread.cancelChunkDownload();
                //DownloadDatabaseHelper.getInstance(context).deleteFileDownload(id);
                //listDownload.remove(thread);
                listDownloadFile.remove(thread);
            }
        }
    }

    public void updateListDownloadComplete(FileDownload fileDownload) {
        completeListDownload.add(fileDownload);
        listenerUpdate.updateList(fileDownload);
        removeDownloadThreadCompleteOutPendingList(fileDownload.get_id());
    }

    public void removeDownloadThreadCompleteOutPendingList(int id) {
        for (int i = 0; i < listDownload.size(); i++) {
            DownloadThread thread = listDownload.get(i);
            if (thread.getID() == id) {
                listDownload.remove(thread);
            }
        }
    }
    public void startUrlDownloadFile(String url,Context context){
        //add file download to database
        DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
        DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(url);
        downloadTask.divideChunkToDownload(url);
        FileDownload fileDownload = new FileDownload(url,DownloadContract.DownloadEntry.STATE_UNCOMPLETE);
        fileDownload.setListChunks(downloadTask.getListChunkDownload());
        databaseHelper.addFile(fileDownload);
        //add list file download
        listDownloadFile.add(0,downloadTask);
        //start download
        pool.submit(downloadTask);
    }
}
