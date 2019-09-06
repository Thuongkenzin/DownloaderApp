package com.example.downloader.DownloadChunk;

import android.media.MediaScannerConnection;
import android.util.Log;

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

    }
    public DownloadChunk(long id, long idFileDownload,long start, long end, long totalDownload){
        this.id = id;
        this.idFileDownload = idFileDownload;
        this.start = start;
        this.end = end;
        this.totalDownload = totalDownload;
        this.pauseChunkDownload = true;
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
//        switch (mMode) {
//            case DownloadMultipleChunk.MODE_NEW_DOWNLOAD:
//                break;
//            case DownloadMultipleChunk.MODE_RESTART:
//                totalDownload = 0;
//                break;
//            case DownloadMultipleChunk.MODE_RESUME:
//                state = DownloadMultipleChunk.MODE_RESUME;
//                break;
//            default:
//                break;
//        }
        mMode = DownloadMultipleChunk.MODE_RESUME;
        state = DownloadMultipleChunk.MODE_RESUME;
        runDownload();
    }

    private void runDownload() {

        if (start >= end) {
            state = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
            return;
        }

        try {
            RandomAccessFile file = new RandomAccessFile(pathFile, "rw");
            fileChannel = file.getChannel();
            URL url = new URL(urlDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            Log.v(TAG, "start:" + start + ", end:" + end);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL){
                releaseNetwork(connection);
                return;
            }
            long fileSize = connection.getContentLength();
            Log.v(TAG, "Chunk length:" + fileSize);

            InputStream in =connection.getInputStream();
            byte[] buffer = new byte[4000];
            int count = 0;
            long position = start;

            while ((count = in.read(buffer)) != -1) {
                totalDownload += count;
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, count);
                byteBuffer.rewind();
                fileChannel.write(byteBuffer, position);
                start += count;
                position = position + count;
                Log.v(TAG,position +"");

                if (isPausedOrCancelled()) {
                    in.close();
                    fileChannel.close();
                    releaseNetwork(connection);
                    return;
                }
            }

            Log.v(TAG, "file Length:" + fileChannel.size());

            state = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
            Log.v("Success","id"+id);
            Log.v("Success","SUCCESS");
            in.close();
            fileChannel.close();
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

    private void releaseNetwork(HttpURLConnection connection) {
        try {
            connection.disconnect();
        } catch (Exception ignored) {
        }
    }



    public void pauseChunkDownload(){
        state = DownloadMultipleChunk.DOWNLOAD_PAUSE;
    }
    public void cancelDownload(){
        state = DownloadMultipleChunk.DOWNLOAD_CANCEL;
    }
}
