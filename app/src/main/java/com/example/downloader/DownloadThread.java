package com.example.downloader;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
    private static final String TAG = "DownloadThread";
    private UpdateProgressListener listener;
    String urlDownload;
    String downloadFileName;
    private int percent ;
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mCancelled;
    private boolean mFinished;
    int ID;
    String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    long fileSize = 0;

    public void setOnUpdateProgressListener(UpdateProgressListener listener){
        this.listener = listener;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getPercent() {
        return percent;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public String getDownloadFileName() {
        return downloadFileName;
    }

    public int getID() {
        return ID;
    }

    public long getFileSize() {
        return fileSize;
    }

    public DownloadThread(int ID, String url) {
        this.ID = ID;
        mPauseLock = new Object();
        mPaused = false;
        mCancelled = false;
        this.urlDownload = url;
        this.downloadFileName = url.substring(url.lastIndexOf('/')+1);
        Log.v(TAG, "File Name: " + this.downloadFileName);
    }

    @Override
    public void run() {
        long downloadedSize = 0;
        try {
            //init URl to download file
            URL url = new URL(urlDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            //Check file exist or not
            File dir = new File(fileDir);
            File file = new File(dir, downloadFileName);
            if(file.exists()){
                downloadedSize = file.length();
                connection.setRequestProperty("Range","bytes="+ downloadedSize +"-");
            }else{
                file.createNewFile();
            }

            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();

            fileSize = downloadedSize + connection.getContentLength();
            Log.v(TAG, "length:" +downloadedSize);
            Log.v(TAG,"total:" +fileSize);

            FileOutputStream fos;
            if(downloadedSize > 0){
                fos = new FileOutputStream(file,true);
            }else{
                fos = new FileOutputStream(file);
            }
            InputStream is = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int count;
            while((count = is.read(buffer)) != -1){
                downloadedSize += count;
                if(fileSize >0){
                    //update process
                    percent = (int)(100*downloadedSize/fileSize);

                    if(listener != null) {
                        listener.updateProgress(percent,downloadedSize);
                    }

                    synchronized (mPauseLock){
                        while(mPaused){
                            mPauseLock.wait();
                        }
                    }

                }
                fos.write(buffer,0,count);
                if(mCancelled){
                    file.delete();
                    break;
                }
            }

            fos.flush();
            fos.close();
            is.close();
            if(connection != null){
                connection.disconnect();
            }
            Log.e(TAG, "Done");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG," Download Error Exception " +e.getMessage());

        }

    }
    public void onPause(){
        synchronized (mPauseLock){
            mPaused = true;
        }
    }

    public void onResume(){
        synchronized (mPauseLock){
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }
    public void onCancelled(){
        if(mPaused == true){
            onResume();
        }
        mCancelled = true;
    }
}
