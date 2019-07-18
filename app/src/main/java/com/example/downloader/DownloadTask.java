package com.example.downloader;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownloadTask {
    private static final String TAG = "DownloadTask";
    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    public static final int PAUSE = 2;
    public static final int CANCEL = 3;
    private static final String CHANNEL_ID = "Downloader_App";
    private Context context;
    private String downloadUrl;
    String downloadFileName;
    TextView textViewPercent;
    TextView sizeDownloaded;


    int status;//status state downloading
    ProgressBar progressBar;
    long fileSize ;
    String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

    public void initProgressBar(Context context){
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(lp);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);

    }

    public DownloadTask(Context context, String downloadUrl,ProgressBar progressBar){
        this.context = context;
        //this.viewGroup = viewGroup;
        //initProgressBar(context);
        //viewGroup.addView(progressBar);
        this.progressBar = progressBar;
        this.downloadUrl = downloadUrl;
        this.downloadFileName= downloadUrl.substring(downloadUrl.lastIndexOf('/')+1);
        startAsyncTaskInParallel(new DownloadingTask());
    }
    public double getFileSize(){
        return fileSize;
    }

    public DownloadTask(Context context,String downloadUrl){
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.downloadFileName= downloadUrl.substring(downloadUrl.lastIndexOf('/')+1);
        startAsyncTaskInParallel(new DownloadingTask());
    }
    // allow AsyncTask execute parallel
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(DownloadingTask task) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }


    public void onPause(){
        status = PAUSE;
    }
    public boolean isPaused(){

        if(status == DownloadTask.PAUSE){
            return true;
        }
        return false;
    }

    public void onResume(){
       status = DOWNLOADING;
       startAsyncTaskInParallel(new DownloadingTask());

    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class DownloadingTask extends AsyncTask<Void,Integer,Void>{
        File apkStorage;
        File outputFile;
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder builder;
        int notificationId;
        int PROGRESS_MAX =100;
        int PROGRESS_CURRENT = 0;
        @Override
        protected void onPostExecute(Void aVoid) {
            if(status==COMPLETE) {
                Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                builder.setContentText("Download Complete")
                        .setProgress(0,0,false);
                notificationManager.notify(notificationId,builder.build());

            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
           progressBar.setProgress(values[0]);
            textViewPercent.setText(values[0] +"%");
            sizeDownloaded.setText(values[1] +"/" +fileSize);
            PROGRESS_CURRENT = values[0];

//            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
//            notificationManager.notify(notificationId, builder.build());

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createNotificationChannel();
            builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_arrow_downward_black_24dp)
                    .setContentTitle(downloadFileName)
                    .setContentText("Downloading file ...")
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            builder.setProgress(PROGRESS_MAX,PROGRESS_CURRENT,true);
            notificationManager = NotificationManagerCompat.from(context);
            notificationId = DownloadUtil.createNotificationId();
            notificationManager.notify(notificationId,builder.build());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                status = DOWNLOADING;
                long downloadedSize =0;
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

//                if (connection.getResponseCode() !=HttpURLConnection.HTTP_OK){
//                    Log.e(TAG, "Server return HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
//                }
//
//                if (new CheckForSDCard().isSDCardPresent()) {
//
//                    //apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + "Android Download");
//                   // apkStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)  , "/Android_Download");
//                   apkStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
//                }
//
//
//                if(!apkStorage.exists()){
//                    apkStorage.mkdir();
//                    Log.e(TAG,"Directory created.");
//                }
//

                File dir = new File(fileDir);
                File file = new File(dir, downloadFileName );
                if(file.exists()){
                    downloadedSize = file.length();
                    connection.setRequestProperty("Range","bytes="+ downloadedSize +"-");
                }else{
                    file.createNewFile();
                }

                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();

                fileSize = connection.getContentLength() + file.length();
                Log.d(TAG,"length: " +connection.getContentLength());
                Log.d(TAG, "fileSize:" +fileSize);

               // outputFile = new File(apkStorage, downloadFileName);

//                Log.d(TAG, "File download:" +outputFile.getAbsolutePath());
//
//                if(!outputFile.exists()){
//                    outputFile.createNewFile();
//                    Log.e(TAG, "File Created");
//                    Log.d(TAG, "File download:" +outputFile.getAbsolutePath());
//
//                }


                FileOutputStream fos;
                if(downloadedSize >0 ){
                    fos = new FileOutputStream(file,true); //if file download incomplete, continue downloading and  append existed file.
                }else{
                    fos = new FileOutputStream(file); //create new file.
                }

                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[8192];
                int count;
                int percent =0;
                while((count = is.read(buffer)) != -1 && (!isPaused())){

                    downloadedSize +=count;
                    if(fileSize > 0){
                        //update progress
                        percent = (int)(100*downloadedSize/fileSize);
                        publishProgress(percent, (int)downloadedSize );

                    }

                    fos.write(buffer,0,count);

                }

                if(percent == 100){
                    status = COMPLETE;
                }
            //close all connection to avoid leak memory
                fos.flush();
                fos.close();
                is.close();
                if(connection !=null) {
                    connection.disconnect();
                }

                Log.e(TAG,"Done");
            } catch (Exception e) {
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG," Download Error Exception " +e.getMessage());
            }
            return null;
        }

    }

}
