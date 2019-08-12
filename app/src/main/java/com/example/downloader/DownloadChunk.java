package com.example.downloader;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadChunk extends Thread {
    private final static String TAG = DownloadChunk.class.getSimpleName();
    String urlDownload;
    long start;
    long end;
    FileChannel fileChannel;
    boolean pauseChunkDownload;
    Object lockObject;
    public DownloadChunk(String urlDownload, long start, long end,FileChannel fileChannel) {
        this.urlDownload = urlDownload;
        this.start = start;
        this.end = end;
        this.fileChannel = fileChannel;
        this.pauseChunkDownload = false;
        lockObject = new Object();
    }

    @Override
    public void run() {
        try {
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
            while((count = in.read(buffer))!=-1){
                //fos.write(buffer,0,count);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,0,count);
                byteBuffer.rewind();
                synchronized (lockObject){
                    while(pauseChunkDownload){
                        lockObject.wait();
                    }
                }
                fileChannel.write(byteBuffer,position);
                position = position + count;

            }
            Log.v(TAG,"file Lenght:"+ fileChannel.size());
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
}
