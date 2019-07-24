package com.example.downloader;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private static DownloadManager instance;
    private List<DownloadThread> listDownload ;
    private DownloadManager(){
        listDownload = new ArrayList<DownloadThread>();
    }
    public static int ID =0;


    public static DownloadManager getInstance(){
        if(instance == null){
            instance = new DownloadManager();
        }
        return instance;
    }

    public List<DownloadThread> getListDownload() {
        return listDownload;
    }

    public void startAllDownload(){
        for (DownloadThread thread : listDownload){
            thread.start();
        }
    }
    public void startUrlDownload(String url){
        DownloadThread newDownload = new DownloadThread(++ID,url);
        listDownload.add(0,newDownload);
        newDownload.start();
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
}
