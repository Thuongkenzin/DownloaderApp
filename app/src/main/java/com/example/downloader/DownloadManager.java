package com.example.downloader;

import android.content.Context;
import android.util.Log;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.Database.FileChunk;
import com.example.downloader.DownloadChunk.DownloadChunk;
import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.Database.FileDownload;
import com.example.downloader.Database.DownloadContract.DownloadEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private static int THREAD_POOL_SIZES = 3;
    private static DownloadManager instance;
    private List<DownloadThread> listDownload;
    private List<FileDownload> completeListDownload;
    private UpdateListDownloadListener listenerUpdate;

    private List<DownloadMultipleChunk> listDownloadFile;
    private DownloadManager() {

        listDownloadFile = new ArrayList<>();
        completeListDownload = new ArrayList<>();
    }

    private ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZES);

    public void setOnUpdateListDownloadListener(UpdateListDownloadListener listener) {
        this.listenerUpdate = listener;
    }

    public interface UpdateListDownloadListener {
        void updateList(FileDownload fileDownload);
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

    public ExecutorService getPool() {
        return pool;
    }

    public void startAllDownload() {
        if(listDownloadFile.size() !=0) {
            for (DownloadMultipleChunk thread : listDownloadFile) {
                pool.submit(thread);
                thread.pauseChunkDownload();
            }
        }
    }

    public void startUrlDownload(String url) {
        DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(url);
        listDownloadFile.add(0,downloadTask);
        pool.submit(downloadTask);

    }


    public void cancelDownload(DownloadMultipleChunk downloadTask) {
        for (int i = 0; i < listDownloadFile.size(); i++) {
            DownloadMultipleChunk thread = listDownloadFile.get(i);
            if (thread.equals(downloadTask)) {
                thread.cancelChunkDownload();
                listDownloadFile.remove(thread);
                break;
            }
        }
    }



    public void saveDownloadFileToDatabaseBeforeExit(Context context){
        DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
        for(DownloadMultipleChunk chunk: listDownloadFile){
            FileDownload fileDownload = new FileDownload(chunk.getUrlDownload(),chunk.getPathFile(),
                    chunk.getFileSize(),DownloadEntry.STATE_UNCOMPLETE);
            long idFile =databaseHelper.addOrUpdateFileDownload(fileDownload);
            List<DownloadChunk>  listDownloadChunk = chunk.getListChunkDownload();
            for(DownloadChunk fileSmallChunk: listDownloadChunk){
                databaseHelper.addOrUpdateChunkDownloadFile(idFile,fileSmallChunk);
            }

        }
        for(FileDownload fileDownload: completeListDownload){
            databaseHelper.addOrUpdateFileDownload(fileDownload);
        }
    }
    public void getFileDownloadFromDatabase(Context context){
        DownloadDatabaseHelper databaseHelper = DownloadDatabaseHelper.getInstance(context);
        List<FileDownload> fileDownloadList =databaseHelper.getAllFileDownload();
        for(FileDownload fileDownload: fileDownloadList){
            //check file have downloaded done
            if(fileDownload.getState() == DownloadEntry.STATE_UNCOMPLETE){
                DownloadMultipleChunk downloadTask = new DownloadMultipleChunk(fileDownload.get_id(),
                        fileDownload.fileName, fileDownload.getUriFileDir(),fileDownload.getUrlDownload(),
                        fileDownload.getState(),fileDownload.fileLength);
                //get file chunk of file download from database
                List<FileChunk> fileChunkList = databaseHelper.getAllChunkDownload(fileDownload.get_id());
                List<DownloadChunk> fileDownloadChunk = convertFileChunkToDownloadChunk(fileChunkList, downloadTask);
                downloadTask.getListChunkDownload().addAll(fileDownloadChunk);
                listDownloadFile.add(downloadTask);
            }else{
                completeListDownload.add(fileDownload);
            }
        }
    }
    public List<DownloadChunk> convertFileChunkToDownloadChunk(List<FileChunk> fileChunkList,DownloadMultipleChunk downloadTask){
        List<DownloadChunk> downloadChunkList = new ArrayList<>();
        for(FileChunk chunk: fileChunkList){
            //if file chunk is not done, add to the list to continue downloading.
            if(chunk.getStartPosition() != chunk.getEndPosition()){
                DownloadChunk downloadChunk = new DownloadChunk(chunk.getId(),chunk.getIdFileDownload(),
                        chunk.getStartPosition(),chunk.getEndPosition());
                downloadChunk.setUrlDownload(downloadTask.getUrlDownload());
                downloadChunk.setPathFile(downloadTask.getPathFile());
                downloadChunkList.add(downloadChunk);
            }
        }
        return downloadChunkList;
    }
}
