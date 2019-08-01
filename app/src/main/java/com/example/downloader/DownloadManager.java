package com.example.downloader;

import android.content.Context;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.Database.FileDownload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private static int THREAD_POOL_SIZES = 3;
    private static DownloadManager instance;
    private List<DownloadThread> listDownload ;
    private List <DownloadThread> completeDownloadedList;
    private UpdateListDownloadListener listenerUpdate;
    private DownloadManager(){
        listDownload = new ArrayList<DownloadThread>();
        completeDownloadedList = new ArrayList<DownloadThread>();
    }
    public static int ID =0;
    private ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZES);

    public void setOnUpdateListDownloadListener(UpdateListDownloadListener listener){
        this.listenerUpdate = listener;
    }
    public interface UpdateListDownloadListener{
        void updateList(DownloadThread downloadThread);
    }
    public List<DownloadThread> getCompleteDownloadedList() {
        return completeDownloadedList;
    }

    public static DownloadManager getInstance(){
        if(instance == null){
            instance = new DownloadManager();
        }
        return instance;
    }

    public List<DownloadThread> getListDownload() {
        return listDownload;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void startAllDownload(){
        for (DownloadThread thread : listDownload){
            pool.submit(thread);
        }
    }
    public void startUrlDownload(String url){
        DownloadThread newDownload = new DownloadThread(++ID,url);
        listDownload.add(0,newDownload);
        pool.submit(newDownload);

    }
    public void getListDownloadFromDatabase(Context context){
        DownloadDatabaseHelper dataInstance = DownloadDatabaseHelper.getInstance(context);
        List<FileDownload> fileList = dataInstance.getAllFileDownload();
        for(FileDownload file : fileList){
            if(file.getState() == DownloadContract.DownloadEntry.STATE_UNCOMPLETE){
                listDownload.add(new DownloadThread(file.get_id(),file.getUrlDownload()));
            }else{
                //add complete download file
            }
        }
    }

    public void cancelDownload(int id){
        for(int i =0;i<listDownload.size();i++){
            DownloadThread thread = listDownload.get(i);
            if(thread.getID() == id){
                thread.onCancelled();
                listDownload.remove(thread);
            }
        }
    }

    public void addDownloadThreadCompleteToTheList(DownloadThread downloadThread){
        completeDownloadedList.add(downloadThread);
        listenerUpdate.updateList(downloadThread);
        removeDownloadThreadCompleteOutPendingList(downloadThread.getID());
    }
    public void removeDownloadThreadCompleteOutPendingList(int id){
        for(int i =0;i<listDownload.size();i++){
            DownloadThread thread = listDownload.get(i);
            if(thread.getID() == id){
                listDownload.remove(thread);
            }
        }
    }
}
