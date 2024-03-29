package com.example.downloader.DownloadAsyncTask;

import android.os.Environment;
import android.util.Log;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Listener.UpdateProgressListener;

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
    private int percent;
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mCancelled;
    private int mState;
    int ID;
    String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    public String filePathDownload;
    File fileDownload;
    long fileSize = 0;


    public void setOnUpdateProgressListener(UpdateProgressListener listener) {
        this.listener = listener;
    }

    public boolean ismCancelled() {
        return mCancelled;
    }

    public void setmCancelled(boolean mCancelled) {
        this.mCancelled = mCancelled;
    }

    public boolean ismPaused() {
        return mPaused;
    }

    public void setmPaused(boolean mPaused) {
        this.mPaused = mPaused;
    }

    public int getmState() {
        return mState;
    }

    public void setmState(int mState) {
        this.mState = mState;
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
        this.downloadFileName = url.substring(url.lastIndexOf('/') + 1);
        filePathDownload = fileDir + "/" + downloadFileName;
        this.mState = DownloadContract.DownloadEntry.STATE_UNCOMPLETE;
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
            fileDownload = file;
            filePathDownload = file.getAbsolutePath();
            if (file.exists()) {
                downloadedSize = file.length();
                connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
            } else {
                file.createNewFile();
            }

            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();

            fileSize = downloadedSize + connection.getContentLength();
            Log.v(TAG, "length:" + downloadedSize);
            Log.v(TAG, "total:" + fileSize);

            FileOutputStream fos;
            if (downloadedSize > 0) {
                fos = new FileOutputStream(file, true);
            } else {
                fos = new FileOutputStream(file);
            }
            InputStream is = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int prevPercent = 0;
            int count;
            long startTime;
            long speedDownload = 0;
            long sizeDownloadIntervalTime = 0;
            if (fileSize > 0) {
                startTime = System.currentTimeMillis();
                while ((count = is.read(buffer)) != -1) {
                    downloadedSize += count;
                    sizeDownloadIntervalTime += count;
                    //update process
                    percent = (int) (100 * downloadedSize / fileSize);

                    if (listener != null && prevPercent != percent ) {
                        long endTime = System.currentTimeMillis();
                        if( endTime != startTime) { // prevent exception divide by zero
                            speedDownload = sizeDownloadIntervalTime * 1000 / (endTime - startTime);
                        }

                        listener.updateProgress(percent, downloadedSize, speedDownload);
                        prevPercent = percent;
                        sizeDownloadIntervalTime = 0;
                        startTime = endTime;
                    }

                    synchronized (mPauseLock) {
                        while (mPaused) {
                            mPauseLock.wait();
                        }
                    }

                    fos.write(buffer, 0, count);
                    if (mCancelled) {
                        file.delete();
                        break;
                    }
                }
            }

            if (percent == 100) {
                mState = DownloadContract.DownloadEntry.STATE_COMPLETE;
            }

            fos.flush();
            fos.close();
            is.close();
            if (connection != null) {
                connection.disconnect();
            }
            Log.e(TAG, "Done");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " Download Error Exception " + e.getMessage());

        }

    }

    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    public void onCancelled() {
        if (mPaused == true) {
            onResume();
        }
        mCancelled = true;
    }
}
