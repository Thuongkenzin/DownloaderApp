package com.example.downloader;

import android.content.Context;
import android.util.Log;

import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.Database.FileChunk;
import com.example.downloader.DownloadChunk.DownloadChunk;
import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.Database.FileDownload;
import com.example.downloader.Database.DownloadContract.DownloadEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private static int THREAD_POOL_SIZES = 3;
    private static DownloadManager instance;
    private List<FileDownload> completeListDownload;
    private UpdateListDownloadListener listenerUpdate;
    private List<DownloadMultipleChunk> listDownloadFile;
    private DownloadManager() {

        listDownloadFile = new ArrayList<>();
        completeListDownload = new ArrayList<>();
    }

    private ExecutorService requestDbExecutor = Executors.newSingleThreadExecutor();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZES,THREAD_POOL_SIZES,60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>());

    public UpdateListDownloadListener setOnUpdateListDownloadListener(UpdateListDownloadListener listener) {
        this.listenerUpdate = listener;
        return this.listenerUpdate;
    }

    public interface UpdateListDownloadListener {
        void updateList();
    }

    public List<FileDownload> getCompleteListDownload() {
        return completeListDownload;
    }

    public void setCompleteListDownload(List<FileDownload> completeListDownload) {
        this.completeListDownload = completeListDownload;
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    public List<DownloadMultipleChunk> getListDownloadFile(){
        return listDownloadFile;
    }

    public ExecutorService getRequestDbExecutor() {
        return requestDbExecutor;
    }

    public void resumeDownloadMultipleChunk(DownloadMultipleChunk downloadMultipleChunk){
        downloadMultipleChunk.setStateDownload(DownloadMultipleChunk.MODE_RESUME);
        threadPoolExecutor.execute(downloadMultipleChunk);
    }

    public void addFileDownloadDoneToListComplete(DownloadMultipleChunk chunk){
        FileDownload fileDownload = new FileDownload(chunk.getId(),chunk.getFileName(),chunk.getUrlDownload(),
                chunk.getStateDownload(),chunk.getPathFile(),chunk.getFileSize());
        completeListDownload.add(fileDownload);
        listDownloadFile.remove(chunk);
        listenerUpdate.updateList();
    }

    public void startUrlDownload(String url,Context context) {
        int count = checkCountFileDownloaded(url);
        String fileName = url.substring(url.lastIndexOf('/')+1);
        if(count >0) {
            String extensionCount = "("+count+")";
            char[] extCountArr = extensionCount.toCharArray();
            StringBuilder nameBuilder = new StringBuilder();
            nameBuilder.append(fileName);
            int pos = fileName.indexOf('.');
            nameBuilder.insert(pos,extCountArr,0,extCountArr.length);
            fileName = nameBuilder.toString();
        }
        DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(url,fileName, context);
        listDownloadFile.add(0, downloadTask);
        threadPoolExecutor.execute(downloadTask);
    }

    public void cancelDownload(DownloadMultipleChunk downloadTask) {
        for (int i = 0; i < listDownloadFile.size(); i++) {
            DownloadMultipleChunk chunk = listDownloadFile.get(i);
            if (chunk.equals(downloadTask)) {
                chunk.cancelChunkDownload();
                listDownloadFile.remove(chunk);
                break;
            }
        }
    }

    public void deleteFileFromDatabase(final Context context, final long idFileDelete){
        requestDbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
                try {
                    databaseHelper.deleteFileDownload(idFileDelete);
                    Log.v("Database:","deleteFile:" + idFileDelete);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.v("Cannot delete file","id:" + idFileDelete);
                }
            }
        });

    }

    public void updateFileDownloadCompleteToDatabase(final Context context, final long idFile){
        requestDbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
                for(int i=0;i<completeListDownload.size();i++){
                    FileDownload fileDownload = completeListDownload.get(i);
                    if(fileDownload.get_id() == idFile){
                        databaseHelper.updateFileDownload(fileDownload);
                    }
                }
            }
        });
    }
    public void saveDownloadFileToDatabase(final Context context){
        requestDbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
                //avoid error java.util.ConcurrentModificationException.
                for (int i = 0; i < listDownloadFile.size(); i++) {
                    DownloadMultipleChunk chunk = listDownloadFile.get(i);
                    Log.v("Database", "write" + chunk.getFileName());
                    FileDownload fileDownload = new FileDownload(chunk.getUrlDownload(), chunk.getPathFile(),
                            chunk.getFileSize(), DownloadEntry.STATE_UNCOMPLETE, chunk.getFileName());
                    long idFile = databaseHelper.addOrUpdateFileDownload(fileDownload);
                    chunk.setId(idFile);
                    Log.v("Database", "write id File:" + idFile);
                    Log.v("Database", "chunk id file:" + chunk.getId());
                    List<DownloadChunk> listDownloadChunk = chunk.getListChunkDownload();
                    for (DownloadChunk fileSmallChunk : listDownloadChunk) {
                        databaseHelper.addOrUpdateChunkDownloadFile(idFile, fileSmallChunk);
                    }

                }
                for(int i = 0;i<completeListDownload.size();i++){
                    FileDownload fileDownload = completeListDownload.get(i);
                    databaseHelper.addOrUpdateFileDownload(fileDownload);

                }
            }
        });

    }
    public void getFileDownloadFromDatabase(final Context context){
        requestDbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
                List<FileDownload> fileDownloadList =databaseHelper.getAllFileDownload();

                for(FileDownload fileDownload: fileDownloadList){
                    //check file have downloaded done
                    if(fileDownload.getState() == DownloadEntry.STATE_UNCOMPLETE){
                        DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(fileDownload.get_id(),
                                fileDownload.fileName, fileDownload.getUriFileDir(),fileDownload.getUrlDownload(),
                                fileDownload.getState(),fileDownload.fileLength,context);
                        //get file chunk of file download from database
                        List<FileChunk> fileChunkList = databaseHelper.getAllChunkDownload(fileDownload.get_id());
                        List<DownloadChunk> fileDownloadChunk = convertFileChunkToDownloadChunk(fileChunkList, downloadTask);
                        downloadTask.getListChunkDownload().addAll(fileDownloadChunk);
                        downloadTask.setStateDownload(DownloadMultipleChunk.DOWNLOAD_PAUSE);
                        listDownloadFile.add(downloadTask);
                    }else{
                        completeListDownload.add(fileDownload);
                    }
                }
            }
        });

    }
    public List<DownloadChunk> convertFileChunkToDownloadChunk(List<FileChunk> fileChunkList,DownloadMultipleChunk downloadTask){
        List<DownloadChunk> downloadChunkList = new ArrayList<>();
        for(FileChunk chunk: fileChunkList){
            //if file chunk is not done, add to the list to continue downloading.
            if(chunk.getStartPosition() < chunk.getEndPosition()){
                DownloadChunk downloadChunk = new DownloadChunk(chunk.getId(),chunk.getIdFileDownload(),
                        chunk.getStartPosition(),chunk.getEndPosition(),chunk.getTotalDownloaded());
                downloadChunk.setUrlDownload(downloadTask.getUrlDownload());
                downloadChunk.setPathFile(downloadTask.getPathFile());
                downloadChunkList.add(downloadChunk);
            }
        }
        return downloadChunkList;
    }
    public int pauseDownloadTask(long idFile){
        for(int i=0;i<listDownloadFile.size();i++){
            DownloadMultipleChunk chunk = listDownloadFile.get(i);
            if(chunk.getId()==idFile){
                chunk.pauseChunkDownload();
                return i;
            }
        }
        return -1;
    }
    public int cancelDownloadTask(long idFile){
        for(int i=0;i<listDownloadFile.size();i++){
            DownloadMultipleChunk chunk = listDownloadFile.get(i);
            if(chunk.getId()==idFile){
                chunk.cancelChunkDownload();
                listDownloadFile.remove(chunk);
                return i;
            }
        }
        return -1;
    }

    public int resumeDownloadMultipleChunk(long idFile){
        for(int i=0;i<listDownloadFile.size();i++){
            DownloadMultipleChunk chunk = listDownloadFile.get(i);
            if(chunk.getId() == idFile){
                chunk.setStateDownload(DownloadMultipleChunk.MODE_RESUME);
                threadPoolExecutor.execute(chunk);
                return i;
            }
        }
        return -1;
    }

    public void cancelAllDownloadTask(Context context){
        for (DownloadMultipleChunk downloadTask:
             listDownloadFile) {
            downloadTask.cancelChunkDownload();
            deleteFileFromDatabase(context,downloadTask.getId());
        }
        listDownloadFile.clear();
    }

    private int checkCountFileDownloaded(String url){
        int count =0;
        for(DownloadMultipleChunk downloadTask : listDownloadFile){
            if(downloadTask.getUrlDownload().equals(url))
                count++;
        }

        for(FileDownload fileDownload : completeListDownload){
            if(fileDownload.getUrlDownload().equals(url)){
                count++;
            }
        }
        return count;
    }
    public void clearListCompleteDownload(){
        completeListDownload.clear();
        listenerUpdate.updateList();
    }
    public void pauseAllDownloadTask(){
        for(DownloadMultipleChunk chunk: listDownloadFile){
            chunk.pauseChunkDownload();
        }
    }
}
