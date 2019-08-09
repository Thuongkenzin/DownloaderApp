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

public class DownloadChunk implements Runnable {
    private final static String TAG = DownloadChunk.class.getSimpleName();
    String urlDownload;
    long start;
    long end;
    String pathFile;
    FileChannel fileChannel;

    public DownloadChunk(String urlDownload, long start, long end, String pathFile,FileChannel fileChannel) {
        this.urlDownload = urlDownload;
        this.start = start;
        this.end = end;
        this.pathFile = pathFile;
        this.fileChannel = fileChannel;
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
            File file = new File(pathFile);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4000];
            int count = 0;
            long position = start;
            while((count = in.read(buffer))!=-1){
                fos.write(buffer,0,count);
                ByteBuffer buff = ByteBuffer.allocate(4000);
                buff.clear();
                buff.put(buffer);
               // buff.put(buffer,0,buffer.length);
                buff.flip();
                fileChannel.write(buff,position);
                position = position + count;
            }
            Log.v(TAG,"Done "+ pathFile);
            Log.v(TAG,"file"+ pathFile+ "leng: "+ file.length());
            Log.v(TAG,"file Lenght:"+ fileChannel.size());
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
