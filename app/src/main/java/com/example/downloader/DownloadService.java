package com.example.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {

    public static final String ACTION_SEND_URL_DOWNLOAD = "action_send_url_download";
    public static final String URL_FILE_DOWNLOAD = "url_file_download";
    DownloadManager downloadManager = DownloadManager.getInstance();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager.getFileDownloadFromDatabase(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction()!= null && intent.getAction().equals(ACTION_SEND_URL_DOWNLOAD)){
            String url = intent.getStringExtra(URL_FILE_DOWNLOAD);
            downloadManager.startUrlDownload(url);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadManager.saveDownloadFileToDatabaseBeforeExit(getApplicationContext());
    }
}
