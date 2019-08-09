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

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    String fileName = "206402main_jsc2007e113280_hires.jpg";
    String pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+fileName;
    String urlDownload = "https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";

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
            Log.v(TAG,"length file: " +fileDir.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error when download: " + e.getMessage());
        }
    }

    public void divideChunkToDownload(final long length,FileChannel fileChannel) {
        Thread download_1 = new Thread(new DownloadChunk(urlDownload,0,length/2,pathFile+ ".01",fileChannel));
        download_1.start();
        Thread download_2 = new Thread(new DownloadChunk(urlDownload,length/2+1,length,pathFile + ".02",fileChannel));
        //download_2.start();
    }


    public void join(String filePath){
        long lengthInFile = 0;
        long length =0;
        int count = 1;
        try{
            File filename = new File(filePath);
            RandomAccessFile outFile = new RandomAccessFile(filename,"rw");
            while(true){
                filename = new File(filePath + count +".sp");
                if(filename.exists()){

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

