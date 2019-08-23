package com.example.downloader.DownloadChunk;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DownloadChunk implements Runnable {
    private final static String TAG = DownloadChunk.class.getSimpleName();
    String urlDownload;
    private long start;
    private long end;
    FileChannel fileChannel;
    boolean pauseChunkDownload;
    Object lockObject;
    long totalDownload = 0;
    String pathFile;
    public int state;
    private long id;
    private long idFileDownload;
    private Thread mThread;

    private int mMode;

    public void startChunkDownload(int mode) {
        mMode = mode;
        mThread = new Thread(this);
        mThread.start();
    }

    public DownloadChunk(String urlDownload, long start, long end,String pathFile) {
        this.urlDownload = urlDownload;
        this.start = start;
        this.end = end;
        this.pathFile = pathFile;
        this.id =-1;
        this.pauseChunkDownload = false;
        lockObject = new Object();
    }
    public DownloadChunk(long id, long idFileDownload,long start, long end){
        this.id = id;
        this.idFileDownload = idFileDownload;
        this.start = start;
        this.end = end;
        this.pauseChunkDownload = true;
        lockObject = new Object();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdFileDownload() {
        return idFileDownload;
    }

    public void setIdFileDownload(long idFileDownload) {
        this.idFileDownload = idFileDownload;
    }

    public DownloadChunk(){}

    public long getTotalDownload() {
        return totalDownload;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public void setTotalDownload(long totalDownload) {
        this.totalDownload = totalDownload;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public void run() {
        switch (mMode) {
            case DownloadMultipleChunk.MODE_NEW_DOWNLOAD:
                break;
            case DownloadMultipleChunk.MODE_RESTART:
                totalDownload = 0;
                break;
            case DownloadMultipleChunk.MODE_RESUME:
                state = DownloadMultipleChunk.MODE_RESUME;
                break;
            default:
                break;
        }
        runDownload();
    }

    private void runDownload() {

//        if (totalDownload > end - 1) {
//            state = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
//            return;
//        }

        try {
            RandomAccessFile file = new RandomAccessFile(pathFile, "rw");
            fileChannel = file.getChannel();
            URL url = new URL(urlDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            long fileSize = connection.getContentLength();
            Log.v(TAG, "length1:" + fileSize);

            InputStream in = connection.getInputStream();

            byte[] buffer = new byte[4000];
            int count = 0;
            long position = start;


            while ((count = in.read(buffer)) != -1) {
                totalDownload += count;
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, count);
                byteBuffer.rewind();
//                synchronized (lockObject) {
//                    while (pauseChunkDownload) {
//                        lockObject.wait();
//                    }
//                }
                fileChannel.write(byteBuffer, position);
                start += count;
                position = position + count;
                Log.v(TAG,"total count:" + totalDownload);

                if (isPausedOrCancelled()) {
                    release(connection);
                    return;
                }
            }

            Log.v(TAG, "file Lenght:" + fileChannel.size());

            state = DownloadMultipleChunk.DOWNLOAD_SUCCESS;

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPausedOrCancelled() {
        if(state == DownloadMultipleChunk.DOWNLOAD_CANCEL || state == DownloadMultipleChunk.DOWNLOAD_PAUSE){
            return  true;
        }
        return false;

    }

    private void release(HttpURLConnection connection) {
        try {
            connection.disconnect();
        } catch (Exception ignored) {
        }
    }

    public void onPause() {
        synchronized (lockObject) {
            pauseChunkDownload = true;
            Log.v(TAG, "Pause");
        }
    }

    public void onResume() {
        synchronized (lockObject) {
            pauseChunkDownload = false;
            Log.v(TAG, "Resume");
            lockObject.notifyAll();
        }
    }

    public void pauseChunkDownload(){
        state = DownloadMultipleChunk.DOWNLOAD_PAUSE;
    }
    public void cancelDownload(){
        state = DownloadMultipleChunk.DOWNLOAD_CANCEL;
    }
}
