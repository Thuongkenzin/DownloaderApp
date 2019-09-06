package com.example.downloader.DownloadChunk;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.downloader.DownloadManager;
import com.example.downloader.DownloadService;
import com.example.downloader.Listener.UpdateCompleteDownloadListener;
import com.example.downloader.Listener.UpdateProgressListener;
import com.example.downloader.R;
import com.example.downloader.Utilities.DownloadUtil;
import com.example.downloader.Utilities.NotificationUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    public static final String ACTION_CANCEL_DOWNLOAD_TASK = "ACTION_CANCEL_DOWNLOAD_TASK";
    public static final String ACTION_PAUSE_DOWNLOAD_TASK = "ACTION_PAUSE_DOWNLOAD_TASK";
    public static final String ACTION_RESUME_DOWNLOAD_TASK = "action_resume_download_task";
    private long id;
    private String fileName ;
    private String pathFile ;
    private String urlDownload ;
    private List<DownloadChunk> listChunkDownload = new ArrayList<>();
    ExecutorService chunkPool = Executors.newFixedThreadPool(3);
    public static final int DOWNLOAD_PAUSE = 0;
    public static final int DOWNLOAD_SUCCESS = 2;
    public static final int DOWNLOAD_CANCEL = 3;

    public static final int MODE_NEW_DOWNLOAD = 4;
    public static final int MODE_RESTART = 5;
    public static final int MODE_RESUME = 6;

    public static int getMode() {
        return mMode;
    }

    public static void setMode(int mMode) {
        DownloadMultipleChunk.mMode = mMode;
    }

    private static int mMode;
    private int stateDownload;
    private int percent = 0;

    private long fileSize ;
    private UpdateProgressListener listener;
    private UpdateCompleteDownloadListener updateCompleteListener;
    private Handler mHandler;
    long sumDownload;
    long preSumDownload =0;
    private int notificationId;
    private NotificationCompat.Builder builderNotification;
    private NotificationManagerCompat notificationManager;
    private Context context;
    List<Future> futureList = new ArrayList<>();
    public void setOnUpdateProgressListener(UpdateProgressListener listener) {
        this.listener = listener;
    }

    public void setOnUpdateCompleteDownloadListener(UpdateCompleteDownloadListener listener){
        this.updateCompleteListener = listener;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getStateDownload() {
        return stateDownload;
    }

    public List<DownloadChunk> getListChunkDownload() {
        return listChunkDownload;
    }

    public void setListChunkDownload(List<DownloadChunk> listChunkDownload) {
        this.listChunkDownload = listChunkDownload;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getPercent() {
        return percent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public void setStateDownload(int stateDownload) {
        this.stateDownload = stateDownload;
    }
    public Context getContextDownload(){
        return context;
    }

    public DownloadMultipleChunk(String urlDownload,String fileName,Context context) {
        this.urlDownload = urlDownload;
        mHandler = new Handler(Looper.getMainLooper());
        stateDownload = MODE_RESUME;
        this.fileName =  fileName;
        pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+
                "/"+fileName;
        this.context = context;
        notificationId= NotificationUtils.createNotificationId();
        notificationManager = NotificationManagerCompat.from(context);

    }


    public DownloadMultipleChunk(long id, String fileName, String pathFile, String urlDownload,
                                 int stateDownload, long fileSize,Context context) {
        this.id = id;
        this.fileName = fileName;
        this.pathFile = pathFile;
        this.urlDownload = urlDownload;
        this.stateDownload = stateDownload;
        this.fileSize = fileSize;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.notificationId = NotificationUtils.createNotificationId();

    }

    @Override
    public void run() {
        //tao HandlerThread

        if(stateDownload == DOWNLOAD_PAUSE){
            return;
        }
        if(stateDownload == MODE_RESUME) {
            try {
                Log.v(TAG,"begin thread");
                if (fileSize == 0) {
                    URL url = new URL(urlDownload);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("HEAD");
                    long length = urlConnection.getContentLength();
                    fileSize = length;
                    Log.v("TAG", "Length:" + length);

                    urlConnection.disconnect();

                    divideChunkToDownload(urlDownload, fileSize);
                }
                createNotificationForFileDownload();

                //startDownloadMultipleChunk();

                for(DownloadChunk chunk : listChunkDownload){
                     Future future = chunkPool.submit(chunk);
                     futureList.add(future);
                }
                mHandler.post(updateUi);
                checkDownloadChunkDone();
//                while (!isComplete()) {
//                    if (stateDownload == DOWNLOAD_PAUSE || stateDownload == DOWNLOAD_CANCEL) {
//                        return;
//                    }
//                }
                if(isComplete()){
                    MediaScannerConnection.scanFile(context, new String[]{pathFile}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
                }

                Log.v(TAG,"end thread.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error when download: " + e.getMessage());
            }
        }
    }

    private void divideChunkToDownload(String urlDownload,long length) {
        DownloadChunk download_1 = new DownloadChunk(urlDownload,0,length/3,pathFile);
        listChunkDownload.add(download_1);
        DownloadChunk download_2 = new DownloadChunk(urlDownload,length/3+1,length/3*2,pathFile);
        listChunkDownload.add(download_2);
        DownloadChunk download_3 = new DownloadChunk(urlDownload,length/3*2 +1,length,pathFile);
        listChunkDownload.add(download_3);
    }



    public void startDownloadMultipleChunk(){
        for(DownloadChunk chunk : listChunkDownload){
            chunk.startChunkDownload(MODE_RESUME);
        }
        mHandler.post(updateUi);
    }

    private void checkDownloadChunkDone() throws ExecutionException, InterruptedException {
        for(Future future:futureList){
            future.get();
        }
    }

    public boolean isComplete(){
        for(DownloadChunk chunk: listChunkDownload){
            if(chunk.state != DownloadMultipleChunk.DOWNLOAD_SUCCESS){
                return false;
            }
        }
        return true;
    }
    public void pauseChunkDownload(){
        stateDownload = DOWNLOAD_PAUSE;
        for (DownloadChunk chunk: listChunkDownload){
            chunk.pauseChunkDownload();
        }
        mHandler.post(updateUi);
    }
    public void resumeChunkDownload(){
        stateDownload = MODE_RESUME;
        for (DownloadChunk chunk: listChunkDownload){
            chunk.startChunkDownload(MODE_RESUME);
        }
    }

    public void cancelChunkDownload(){
        stateDownload = DOWNLOAD_CANCEL;
        for(DownloadChunk chunk: listChunkDownload){
            chunk.cancelDownload();
        }
        notificationManager.cancel(notificationId);
        deleteFileInSDCard();

    }

    public long getTotalDownloadPrev(){
        long total =0;
        if(listChunkDownload.size()!= 0){
            for(DownloadChunk chunk: listChunkDownload){
                total += chunk.getTotalDownload();
            }
            return total;
        }
        return 0;
    }
    public void deleteFileInSDCard(){
        File file = new File(pathFile);
        if(file.exists()) {
            file.delete();
        }
    }

    public long getDownloadedSizeFromChunkFile(){
        long totalDownload =0;
        for(DownloadChunk chunk : listChunkDownload){
            totalDownload +=chunk.getTotalDownload();
        }
        return totalDownload;
    }

    private Runnable updateUi =new Runnable() {
        @Override
        public void run() {
            sumDownload = getDownloadedSizeFromChunkFile();
            percent = (int) (100 * sumDownload / fileSize);
            long speedDownload = (sumDownload - preSumDownload);//interval 1000ms
            if(percent % 10 ==0){
                DownloadManager.getInstance().saveDownloadFileToDatabase(context);
                createNotificationForFileDownload();
            }
            if(listener!=null) {
                listener.updateProgress(percent, sumDownload, speedDownload);
                preSumDownload = sumDownload;
                builderNotification.setProgress(100,percent, false);
                builderNotification.setContentText("Downloading file ... "+ percent + "%");
                notificationManager.notify(notificationId,builderNotification.build());

            }
            mHandler.postDelayed(this,1000);
            if (isComplete()) {
                stateDownload = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
                mHandler.removeCallbacks(this);
                updateCompleteListener.notifyCompleteDownloadFile();
                notifyNotificationDownloadComplete();
            }
            if(stateDownload == DOWNLOAD_PAUSE){
//                mHandler.postDelayed(this,500);
                listener.updateProgress(percent,sumDownload,0);
                mHandler.post(this);
                mHandler.removeCallbacks(this);
                updateActionInNotification();
            }
            if(stateDownload == DOWNLOAD_CANCEL){
                mHandler.removeCallbacks(this);
                notificationManager.cancel(notificationId);
            }
        }
    };

    public void createNotificationForFileDownload(){
        builderNotification = new NotificationCompat.Builder(context, NotificationUtils.DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_arrow_downward_black_24dp)
                .setContentTitle(fileName)
                .setContentText("Downloading file ...")
                .setProgress(100,0,false)
                .setContentIntent(openFileDownloadIntent(context))
                .addAction(pauseNotificationDownload(context))
                .addAction(cancelNotificationDownload(context))
                .setOngoing(true)
                .setAutoCancel(false);

        notificationManager.notify(notificationId, builderNotification.build());
        Log.v(TAG, fileName +"notification id:" + notificationId);
    }

    @SuppressLint("RestrictedApi")
    public void notifyNotificationDownloadComplete(){
        builderNotification.setContentText("Download complete")
                .setProgress(0,0,false)
                .setOngoing(false)
                .setAutoCancel(true);
        builderNotification.mActions.clear();
        notificationManager.notify(notificationId, builderNotification.build());
    }

    public PendingIntent openFileDownloadIntent(Context context) {
        try {
            File file = new File(pathFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeTypeFile = DownloadUtil.getMimeTypeFile(file);
            intent.setDataAndType(Uri.fromFile(file), mimeTypeFile);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Cannot support format file!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public NotificationCompat.Action pauseNotificationDownload(Context context){
        Intent pauseIntent = new Intent(context, DownloadService.class);
        pauseIntent.setAction(ACTION_PAUSE_DOWNLOAD_TASK);
        pauseIntent.putExtra("idFile", id);
        Log.v(TAG, "PAUSE " + fileName);
        PendingIntent pauseDownloadPendingIntent = PendingIntent.getService(
                context,
                notificationId + 1,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action pauseNotificationAction = new NotificationCompat.Action(
                0,
                "Pause",
                pauseDownloadPendingIntent);

        return pauseNotificationAction;

    }

    public NotificationCompat.Action cancelNotificationDownload(Context context) {
        Intent cancelIntent = new Intent(context, DownloadService.class);
        cancelIntent.setAction(ACTION_CANCEL_DOWNLOAD_TASK);
        cancelIntent.putExtra("idFile", id);
        Log.v("Database:", "idFileCancel:"+ id);
        Log.v(TAG, "CANCEL " + fileName);
        PendingIntent cancelDownloadPendingIntent = PendingIntent.getService(
                context,
                notificationId + 2,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelNotificationAction = new NotificationCompat.Action(
                0,
                "Cancel",
                cancelDownloadPendingIntent);
        return cancelNotificationAction;
    }

    private NotificationCompat.Action resumeNotificationDownload(Context context){
        Intent resumeIntent = new Intent(context, DownloadService.class);
        resumeIntent.setAction(ACTION_RESUME_DOWNLOAD_TASK);
        resumeIntent.putExtra("idFile",id);
        Log.v(TAG, "RESUME :" + fileName);
        PendingIntent resumeDownloadPendingIntent = PendingIntent.getService(
                context,
                notificationId +3,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action resumeNotificationAction = new NotificationCompat.Action(
                0,
                "Resume",
                resumeDownloadPendingIntent);
        return resumeNotificationAction;
    }

    @SuppressLint("RestrictedApi")
    private void updateActionInNotification(){
        if(stateDownload == DOWNLOAD_PAUSE) {
            builderNotification.mActions.remove(0);
            builderNotification.mActions.add(0,resumeNotificationDownload(context));
            builderNotification.setOngoing(false);
            notificationManager.notify(notificationId, builderNotification.build());
        }
    }
}
