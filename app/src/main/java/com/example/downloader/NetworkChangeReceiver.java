package com.example.downloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = isConnectingToInternet(context);
        if(!isConnected){
            Intent pauseAllDownload = new Intent(context,DownloadService.class);
            pauseAllDownload.setAction(DownloadService.ACTION_PAUSE_ALL_DOWNLOAD);
            context.startService(pauseAllDownload);
        }
    }
    private boolean isConnectingToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = connectivityManager.getActiveNetworkInfo();
        if(netWorkInfo!=null && netWorkInfo.isConnected())
            return true;
        else
            return false;
    }
}
