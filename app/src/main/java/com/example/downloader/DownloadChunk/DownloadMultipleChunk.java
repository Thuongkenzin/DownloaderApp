package com.example.downloader.DownloadChunk;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Listener.UpdateCompleteDownloadListener;
import com.example.downloader.Listener.UpdateProgressListener;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    long id;
    String fileName ;
    String pathFile ;
    String urlDownload ;
    private List<DownloadChunk> listChunkDownload = new ArrayList<>();
    public static final int DOWNLOAD_PAUSE = 0;
    public static final int DOWNLOAD_SUCCESS = 2;
    public static final int DOWNLOAD_CANCEL = 3;

    public static final int MODE_NEW_DOWNLOAD = 4;
    public static final int MODE_RESTART = 5;
    public static final int MODE_RESUME = 6;

    public static int getMode() {
        return mMode;
    }

    public static void setMode(int mMode) {
        DownloadMultipleChunk.mMode = mMode;
    }

    private static int mMode;
    private int stateDownload;
    private int percent = 0;

    private long fileSize ;
    private UpdateProgressListener listener;
    private UpdateCompleteDownloadListener updateCompleteListener;
    private Handler mHandler;
    long sumDownload;
    long preSumDownload =0;
    long totalDownloaded ;
    public void setOnUpdateProgressListener(UpdateProgressListener listener) {
        this.listener = listener;
    }

    public void setOnUpdateCompleteDownloadListener(UpdateCompleteDownloadListener listener){
        this.updateCompleteListener = listener;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getStateDownload() {
        return stateDownload;
    }

    public List<DownloadChunk> getListChunkDownload() {
        return listChunkDownload;
    }

    public void setListChunkDownload(List<DownloadChunk> listChunkDownload) {
        this.listChunkDownload = listChunkDownload;
    }

    public int getPercent() {
        return percent;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public void setStateDownload(int stateDownload) {
        this.stateDownload = stateDownload;
    }

    public DownloadMultipleChunk(String urlDownload) {
        this.urlDownload = urlDownload;
        mHandler = new Handler();
        stateDownload = MODE_RESUME;
        this.fileName =  urlDownload.substring(urlDownload.lastIndexOf('/') + 1);
        pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+
                "/"+fileName;

    }

    public DownloadMultipleChunk(long id, String fileName, String pathFile, String urlDownload,
                                 int stateDownload, long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.pathFile = pathFile;
        this.urlDownload = urlDownload;
        this.stateDownload = stateDownload;
        this.fileSize = fileSize;
        this.mHandler = new Handler();

    }

    @Override
    public void run() {
        //tao HandlerThread

        if(stateDownload == DOWNLOAD_PAUSE){
            return;
        }
        if(stateDownload == MODE_RESUME) {
            try {
                Log.v(TAG,"begin thread");
                if (fileSize == 0) {
                    URL url = new URL(urlDownload);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("HEAD");
                    long length = urlConnection.getContentLength();
                    fileSize = length;
                    Log.v("TAG", "Length:" + length);

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    divideChunkToDownload(urlDownload, fileSize);
                }

                startDownloadMultipleChunk();

                while (!isComplete()) {
                    if (stateDownload == DOWNLOAD_PAUSE || stateDownload == DOWNLOAD_CANCEL) {
                        return;
                    }
                }

//                if (isComplete()) {
//                    stateDownload = DownloadContract.DownloadEntry.STATE_COMPLETE;
//                }
                //Log.v(TAG,"length file: " +fileDir.length());
                Log.v(TAG,"end thread.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error when download: " + e.getMessage());
            }
        }
    }

    public void divideChunkToDownload(String urlDownload,long length) {
        DownloadChunk download_1 = new DownloadChunk(urlDownload,0,length/3,pathFile);
        listChunkDownload.add(download_1);
        DownloadChunk download_2 = new DownloadChunk(urlDownload,length/3+1,length/3*2,pathFile);
        listChunkDownload.add(download_2);
        DownloadChunk download_3 = new DownloadChunk(urlDownload,length/3*2 +1,length,pathFile);
        listChunkDownload.add(download_3);
    }



    public void startDownloadMultipleChunk(){
        for(DownloadChunk chunk : listChunkDownload){
            chunk.startChunkDownload(MODE_RESUME);
        }
        mHandler.post(updateUi);
    }

    public boolean isComplete(){
        for(DownloadChunk chunk: listChunkDownload){
            if(chunk.state != DownloadMultipleChunk.DOWNLOAD_SUCCESS){
                return false;
            }
        }
        return true;
    }
    public void pauseChunkDownload(){
        stateDownload = DOWNLOAD_PAUSE;
        for (DownloadChunk chunk: listChunkDownload){
            //chunk.onPause();
            chunk.pauseChunkDownload();
        }
    }
    public void resumeChunkDownload(){
        stateDownload = MODE_RESUME;
        for (DownloadChunk chunk: listChunkDownload){
            //chunk.onResume();
            chunk.startChunkDownload(MODE_RESUME);
        }
    }

    public void cancelChunkDownload(){
        stateDownload = DOWNLOAD_CANCEL;
        for(DownloadChunk chunk: listChunkDownload){
            chunk.cancelDownload();
        }
        deleteFileInSDCard();
    }

    public long getTotalDownloadPrev(){
        long total =0;
        if(listChunkDownload.size()!= 0){
            for(DownloadChunk chunk: listChunkDownload){
                total += chunk.getTotalDownload();
            }
            return total;
        }
        return 0;
    }
    public void deleteFileInSDCard(){
        File file = new File(pathFile);
        if(file.exists()) {
            file.delete();
        }
    }

    public long getDownloadedSizeFromChunkFile(){
        long totalDownload =0;
        for(DownloadChunk chunk : listChunkDownload){
            totalDownload +=chunk.getTotalDownload();
        }
        return totalDownload;
    }

    private Runnable updateUi =new Runnable() {
        @Override
        public void run() {
            sumDownload = getDownloadedSizeFromChunkFile();
            percent = (int) (100 * sumDownload / fileSize);
            long speedDownload = (sumDownload - preSumDownload);

            if(listener!=null) {
                listener.updateProgress(percent, sumDownload, speedDownload);
                preSumDownload = sumDownload;
            }
            if (!isComplete()) {
                mHandler.postDelayed(this, 1000);
            }else{
                stateDownload = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
                mHandler.removeCallbacks(this);
                updateCompleteListener.notifyCompleteDownloadFile();
            }
            if(stateDownload == DOWNLOAD_CANCEL || stateDownload == DOWNLOAD_PAUSE){
                mHandler.removeCallbacks(this);
            }
        }
    };
}
