package com.example.downloader.DownloadChunk;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

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

public class DownloadMultipleChunk implements Runnable {
    private static final String TAG = DownloadMultipleChunk.class.getSimpleName();
    public static final String ACTION_CANCEL_DOWNLOAD_TASK = "ACTION_CANCEL_DOWNLOAD_TASK";
    public static final String ACTION_PAUSE_DOWNLOAD_TASK = "ACTION_PAUSE_DOWNLOAD_TASK";
    public static final String ACTION_RESUME_DOWNLOAD_TASK = "action_resume_download_task";
    long id;
    String fileName ;
    String pathFile ;
    String urlDownload ;
    private List<DownloadChunk> listChunkDownload = new ArrayList<>();
    public static final int ACTION_PAUSE_DOWNLOAD_PENDING_INTENT_ID= 100;
    public static final int ACTION_CANCEL_DOWNLOAD_PENDING_INTENT_ID= 101;
    public static final int ACTION_RESUME_DOWNLOAD_PENDING_INTENT_ID = 102;
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
    Context context;
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

    public void setId(int id) {
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

    public DownloadMultipleChunk(String urlDownload,Context context) {
        this.urlDownload = urlDownload;
        mHandler = new Handler();
        stateDownload = MODE_RESUME;
        this.fileName =  urlDownload.substring(urlDownload.lastIndexOf('/') + 1);
        pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+
                "/"+fileName;
        this.context = context;
        notificationId= NotificationUtils.createNotificationId();

    }

    public DownloadMultipleChunk(String urlDownload) {
        this.urlDownload = urlDownload;
        mHandler = new Handler();
        stateDownload = MODE_RESUME;
        this.fileName =  urlDownload.substring(urlDownload.lastIndexOf('/') + 1);
        pathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+
                "/"+fileName;
        notificationId= NotificationUtils.createNotificationId();

    }
    public DownloadMultipleChunk(long id, String fileName, String pathFile, String urlDownload,
                                 int stateDownload, long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.pathFile = pathFile;
        this.urlDownload = urlDownload;
        this.stateDownload = stateDownload;
        this.fileSize = fileSize;
        this.mHandler = new Handler();

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

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    divideChunkToDownload(urlDownload, fileSize);
                }
                createNotificationForFileDownload();

                startDownloadMultipleChunk();
                while (!isComplete()) {
                    if (stateDownload == DOWNLOAD_PAUSE || stateDownload == DOWNLOAD_CANCEL) {
                        return;
                    }
                }

                Log.v(TAG,"end thread.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error when download: " + e.getMessage());
            }
        }
    }

    public void divideChunkToDownload(String urlDownload,long length) {
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
    }
    public void resumeChunkDownload(){
        stateDownload = MODE_RESUME;
        for (DownloadChunk chunk: listChunkDownload){
            //chunk.onResume();
            chunk.startChunkDownload(MODE_RESUME);
        }
    }

    public void cancelChunkDownload(){
        stateDownload = DOWNLOAD_CANCEL;
        for(DownloadChunk chunk: listChunkDownload){
            chunk.cancelDownload();
        }
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
            long speedDownload = (sumDownload - preSumDownload);

            if(listener!=null) {
                listener.updateProgress(percent, sumDownload, speedDownload);
                preSumDownload = sumDownload;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                builderNotification.setProgress(100,percent, false);
                builderNotification.setContentText("Downloading file ... "+ percent + "%");
                notificationManager.notify(notificationId,builderNotification.build());

            }
            if (!isComplete()) {
                mHandler.postDelayed(this, 1000);
            }else{
                stateDownload = DownloadMultipleChunk.DOWNLOAD_SUCCESS;
                mHandler.removeCallbacks(this);
                updateCompleteListener.notifyCompleteDownloadFile();
                notifyNotificationDownloadComplete();
            }
            if(stateDownload == DOWNLOAD_PAUSE){
                mHandler.removeCallbacks(this);
                //updateActionInNotification();
            }
            if(stateDownload == DOWNLOAD_CANCEL){
                mHandler.removeCallbacks(this);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, builderNotification.build());
        Log.v(TAG, fileName +"notification id:" + notificationId);

    }

    @SuppressLint("RestrictedApi")
    public void notifyNotificationDownloadComplete(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        builderNotification.setContentText("Download complete")
                .setProgress(0,0,false)
                .setOngoing(false);
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
        pauseIntent.putExtra("name", fileName);
        Log.v(TAG, "PAUSE " + fileName);
        PendingIntent pauseDownloadPendingIntent = PendingIntent.getService(
                context,
                0,
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
        cancelIntent.putExtra("name", fileName);
        Log.v(TAG, "CANCEL " + fileName);
        PendingIntent cancelDownloadPendingIntent = PendingIntent.getService(
                context,
                ACTION_CANCEL_DOWNLOAD_PENDING_INTENT_ID,
                cancelIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Action cancelNotificationAction = new NotificationCompat.Action(
                0,
                "Cancel",
                cancelDownloadPendingIntent);
        return cancelNotificationAction;
    }

    private NotificationCompat.Action resumeNotificationDownload(Context context){
        Intent resumeIntent = new Intent(context, DownloadService.class);
        resumeIntent.setAction(ACTION_RESUME_DOWNLOAD_TASK);
        resumeIntent.putExtra("name",fileName);
        Log.v(TAG, "RESUME :" + fileName);
        PendingIntent resumeDownloadPendingIntent = PendingIntent.getService(
                context,
                ACTION_RESUME_DOWNLOAD_PENDING_INTENT_ID,
                resumeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action resumeNotificationAction = new NotificationCompat.Action(
                0,
                "Resume",
                resumeDownloadPendingIntent);
        return resumeNotificationAction;
    }

    @SuppressLint("RestrictedApi")
    private void updateActionInNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if(stateDownload == DOWNLOAD_PAUSE) {
            builderNotification.mActions.remove(0);
            builderNotification.mActions.add(0,resumeNotificationDownload(context));
            notificationManager.notify(notificationId, builderNotification.build());
        }else if(stateDownload == MODE_RESUME){
            builderNotification.mActions.remove(0);
            builderNotification.mActions.add(0,pauseNotificationDownload(context));
            notificationManager.notify(notificationId,builderNotification.build());
        }
    }
}
