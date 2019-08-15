package com.example.downloader.DownloadChunk;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.UpdateProgressListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    int id;
    String fileName ;
    String pathFile ;
    //String urlDownload = "https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
    String urlDownload ;
    private List<DownloadChunk> listChunkDownload = new ArrayList<>();
    ExecutorService poolChunkDownload = Executors.newFixedThreadPool(3);
    public static final int DOWNLOAD_PAUSE = 0;
    public static final int DOWNLOAD_RESUME = 1;
    public static final int DOWNLOAD_SUCCESS = 2;
    public static final int DOWNLOAD_CANCEL = 3;
    private int stateDownload;
    private int percent = 0;
    private long fileSize ;
    private UpdateProgressListener listener;
    private Handler mHandler;
    long sumDownload;
    long preSumDownload =0;
    public void setOnUpdateProgressListener(UpdateProgressListener listener) {
        this.listener = listener;
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

    public int getId() {
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
        stateDownload = DOWNLOAD_RESUME;
        this.fileName =  urlDownload.substring(urlDownload.lastIndexOf('/') + 1);
        pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+fileName;
    }

    @Override
    public void run() {
        try {
//            File fileDir = new File(pathFile);
//            RandomAccessFile file = new RandomAccessFile(pathFile,"rw");
//            FileChannel fileChannel = file.getChannel();
            URL url = new URL(urlDownload);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            long length = urlConnection.getContentLength();
            fileSize = length;
            Log.v("TAG", "Length:" + length);

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            //divideChunkToDownload(length,fileChannel);
            startDownloadMultipleChunk();
//
//            mHandler.post(updateUi);
//            while(!isComplete()){
//                mHandler.
//                sumDownload=getDownloadedSizeFromChunkFile();
//                percent = (int)(100*sumDownload/fileSize);
//                long speedDownload = (sumDownload-preSumDownload) *1000/800;
//                listener.updateProgress(percent,sumDownload,speedDownload);
//                preSumDownload= sumDownload;
//                Thread.sleep(800);
//            }

//            if(isComplete()){
//                stateDownload = DOWNLOAD_SUCCESS;
//                Log.v(TAG,"State Success");
//
//            }

            //Log.v(TAG,"length file: " +fileDir.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error when download: " + e.getMessage());
        }
    }

    public void divideChunkToDownload(String urlDownload) {
        long length = getLengthDownloadFile(urlDownload);
        DownloadChunk download_1 = new DownloadChunk(urlDownload,0,length/3,pathFile);
        listChunkDownload.add(download_1);
        DownloadChunk download_2 = new DownloadChunk(urlDownload,length/3+1,length/3*2,pathFile);
        listChunkDownload.add(download_2);
        DownloadChunk download_3 = new DownloadChunk(urlDownload,length/3*2 +1,length,pathFile);
        listChunkDownload.add(download_3);
    }
//    public long getLengthDownloadFile(final String urlDownload){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                URL url = null;
//                try {
//                    url = new URL(urlDownload);
//                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                    urlConnection.setRequestMethod("HEAD");
//                    long length = urlConnection.getContentLength();
//                    fileSize = length;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//    }

    public void startDownloadMultipleChunk(){
        for(DownloadChunk chunk : listChunkDownload){
            poolChunkDownload.execute(chunk);
        }
        mHandler.post(updateUi);
        poolChunkDownload.shutdown();
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
        for (DownloadChunk chunk: listChunkDownload){
            chunk.onPause();
        }
    }
    public void resumeChunkDownload(){
        for (DownloadChunk chunk: listChunkDownload){
            chunk.onResume();
        }
    }

    public void cancelChunkDownload(){
        for(DownloadChunk chunk: listChunkDownload){
            chunk.cancelDownload();
        }
        deleteFileInSDCard();
    }

    public void deleteFileInSDCard(){
        File file = new File(pathFile);
        file.delete();
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
            listener.updateProgress(percent, sumDownload, speedDownload);
            preSumDownload = sumDownload;
            if (!isComplete()) {
                mHandler.postDelayed(this, 1000);
            }else{
                stateDownload = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
                mHandler.removeCallbacks(this);
            }
        }
    };
}
