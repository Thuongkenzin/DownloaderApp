package com.example.downloader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.Utilities.NotificationUtils;

public class DownloadService extends Service {

    public static final String TAG = DownloadService.class.getSimpleName();
    public static final int OPEN_ACTIVITY_DOWNLOAD_PENDING_INTENT_ID = 1002;
    public static final int STOP_SERVICE_DOWNLOAD_PENDING_INTENT_ID = 1004;
    public static final String ACTION_SEND_URL_DOWNLOAD = "action_send_url_download";
    public static final String URL_FILE_DOWNLOAD = "url_file_download";
    public static final String ACTION_UPDATE_LIST_DOWNLOAD_ADD = "ACTION_UPDATE_LIST_DOWNLOAD_ADD";
    public static final String ACTION_UPDATE_LIST_DOWNLOAD_PAUSE = "ACTION_UPDATE_LIST_DOWNLOAD_PAUSE";
    public static final String ACTION_UPDATE_LIST_DOWNLOAD_CANCEL = "ACTION_UPDATE_LIST_DOWNLOAD_CANCEL";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "action_stop_foreground_service";
    public static final String ACTION_UPDATE_LIST_DOWNLOAD_STOP_ALL_DOWNLOAD = "ACTION_UPDATE_LIST_DOWNLOAD_STOP_ALL_DOWNLOAD";

    DownloadManager downloadManager = DownloadManager.getInstance();
    public static int idNotificationForeground = 100;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationUtils.createNotificationChannel(getApplicationContext());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!= null){
            String action = intent.getAction();
            switch (action) {
                case ACTION_SEND_URL_DOWNLOAD:
                    String url = intent.getStringExtra(URL_FILE_DOWNLOAD);
                    downloadManager.startUrlDownload(url,getApplicationContext());

                    NotificationCompat.Builder builder = createNotificationForeground(getApplicationContext());
                    startForeground(idNotificationForeground,builder.build());

                    Intent updateListDownloadIntent = new Intent();
                    updateListDownloadIntent.setAction(ACTION_UPDATE_LIST_DOWNLOAD_ADD);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateListDownloadIntent);
                    break;
                    //send data to recycler view to update UI
                case DownloadMultipleChunk.ACTION_PAUSE_DOWNLOAD_TASK:
                    String name = intent.getStringExtra("name");
                    int position = downloadManager.pauseDownloadTask(name);
                    if (position != -1) {
                        Intent pauseIntent = new Intent();
                        pauseIntent.setAction(ACTION_UPDATE_LIST_DOWNLOAD_PAUSE);
                        pauseIntent.putExtra("position", position);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(pauseIntent);
                    }
                    break;
                case DownloadMultipleChunk.ACTION_CANCEL_DOWNLOAD_TASK:
                    String filename = intent.getStringExtra("name");
                    int pos = downloadManager.cancelDownloadTask(filename);
                    if (pos != -1) {
                        Intent cancelIntent = new Intent();
                        cancelIntent.setAction(ACTION_UPDATE_LIST_DOWNLOAD_CANCEL);
                        cancelIntent.putExtra("position", pos);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(cancelIntent);
                    }
                    break;
                case DownloadMultipleChunk.ACTION_RESUME_DOWNLOAD_TASK:
                    String fileName = intent.getStringExtra("name");
                    int resumePos = downloadManager.resumeDownloadMultipleChunk(fileName);
                    if(resumePos != -1){
                        Intent resumeIntent = new Intent(ACTION_UPDATE_LIST_DOWNLOAD_PAUSE);
                        resumeIntent.putExtra("position", resumePos);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resumeIntent);
                    }
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    downloadManager.cancelAllDownloadTask();
                    stopSelf();
                    NotificationUtils.clearAllNotifications(getApplicationContext());
                    Intent stopAllDownloadIntent = new Intent(ACTION_UPDATE_LIST_DOWNLOAD_STOP_ALL_DOWNLOAD);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(stopAllDownloadIntent);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public NotificationCompat.Builder createNotificationForeground(Context context){
        Intent stopServiceIntent = new Intent(context, DownloadService.class);
        stopServiceIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        PendingIntent stopForegroundPendingIntent = PendingIntent.getService(context,
                STOP_SERVICE_DOWNLOAD_PENDING_INTENT_ID,
                stopServiceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builderNotification = new NotificationCompat.Builder(context, NotificationUtils.DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_arrow_downward_black_24dp)
                .setContentTitle("Downloader")
                .setContentText("Downloading file ...")
                .setContentIntent(contentIntent(this))
                .addAction(0,"Stop service download",stopForegroundPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false);
        return builderNotification;
    }


    public PendingIntent contentIntent(Context context) {
        Intent startActivityDownloadIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context,
                OPEN_ACTIVITY_DOWNLOAD_PENDING_INTENT_ID,
                startActivityDownloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadManager.saveDownloadFileToDatabaseBeforeExit(getApplicationContext());
    }
}
