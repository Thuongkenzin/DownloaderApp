package com.example.downloader.DownloadChunk;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DownloadChunk extends Thread {
    private final static String TAG = DownloadChunk.class.getSimpleName();
    String urlDownload;
    private long start;
    private long end;
    FileChannel fileChannel;
    boolean pauseChunkDownload;
    Object lockObject;
    long totalDownload;
    String pathFile;
    public int state;
    private long id;
    private long idFileDownload;

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

    @Override
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
        try {
            RandomAccessFile file = new RandomAccessFile(pathFile,"rw");
            fileChannel = file.getChannel();
            URL url = new URL(urlDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            state = DownloadMultipleChunk.DOWNLOAD_RESUME;
            long fileSize = connection.getContentLength();
            Log.v(TAG, "length1:" + fileSize);

            InputStream in = connection.getInputStream();

            byte[] buffer = new byte[4000];
            int count = 0;
            long position = start;
            totalDownload =0;
            while((count = in.read(buffer))!=-1){
                //fos.write(buffer,0,count);
                totalDownload += count;
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,0,count);
                byteBuffer.rewind();
                synchronized (lockObject){
                    while(pauseChunkDownload){
                        lockObject.wait();
                    }
                }
                fileChannel.write(byteBuffer,position);
                start += count;
                position = position + count;
                if(state == DownloadMultipleChunk.DOWNLOAD_CANCEL){
                    break;
                }
            }
            Log.v(TAG,"file Lenght:"+ fileChannel.size());
            state = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onPause(){
        synchronized (lockObject) {
            pauseChunkDownload = true;
            Log.v(TAG,"Pause");
        }
    }
    public void onResume(){
        synchronized (lockObject){
            pauseChunkDownload = false;
            Log.v(TAG,"Resume");
            lockObject.notifyAll();
        }
    }
    public void cancelDownload(){
        state = DownloadMultipleChunk.DOWNLOAD_CANCEL;
    }
}
