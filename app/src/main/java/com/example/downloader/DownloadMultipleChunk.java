package com.example.downloader;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    String fileName = "BigBuckBunny.mp4";
    String pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+fileName;
    //String urlDownload = "https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
    String urlDownload ="http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    List<DownloadChunk> listChunkDownload = new ArrayList<>();

    @Override
    public void run() {
        try {
            File fileDir = new File(pathFile);
            RandomAccessFile file = new RandomAccessFile(pathFile,"rw");
            FileChannel fileChannel = file.getChannel();
            URL url = new URL(urlDownload);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            long length = urlConnection.getContentLength();
            Log.v("TAG", "Length:" + length);

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            divideChunkToDownload(length,fileChannel);
            startDownloadMultipleChunk();
            Log.v(TAG,"length file: " +fileDir.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error when download: " + e.getMessage());
        }
    }

    public void divideChunkToDownload(final long length,FileChannel fileChannel) {
        DownloadChunk download_1 = new DownloadChunk(urlDownload,0,length/3,fileChannel);
        listChunkDownload.add(download_1);
        DownloadChunk download_2 = new DownloadChunk(urlDownload,length/3+1,length/3*2,fileChannel);
        listChunkDownload.add(download_2);
        DownloadChunk download_3 = new DownloadChunk(urlDownload,length/3*2 +1,length,fileChannel);
        listChunkDownload.add(download_3);
    }

    public void startDownloadMultipleChunk(){
        for(DownloadChunk chunk : listChunkDownload){
            chunk.start();
        }
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

}

