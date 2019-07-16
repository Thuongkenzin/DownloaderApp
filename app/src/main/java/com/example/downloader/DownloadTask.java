package com.example.downloader;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask {
    private static final String TAG = "DownloadTask";
    private Context context;
    private String downloadUrl;
    String downloadFileName;

    int status;
    ProgressBar progressBar;
    double fileSize =0;
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


    private long isIncomplete(){
        File dir = new File(fileDir);
        File file = new File(dir, downloadFileName );
        if(file.exists()){
            Log.d("status", "Download is incomplete, file size:" + file.length());
            return file.length();
        }
        return 0;
    }
    public void onPause(){
        status = 0;
    }
    public boolean isPaused(){

        if(status == 0){
            return true;
        }
        return false;
    }

    public void onResume(){
       status = 1;
       startAsyncTaskInParallel(new DownloadingTask());

    }

    private class DownloadingTask extends AsyncTask<Void,Integer,Void>{
        File apkStorage;
        File outputFile;

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                status = 1;
                long downloadedSize =0;
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // connection.connect();

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

                long downloaded = isIncomplete();
                if(downloaded >0){
                    connection.setRequestProperty("Range","byte="+(downloaded)+"-");
                    downloadedSize = downloaded;
                    fileSize = downloaded;
                }
                connection.setDoOutput(true);

                connection.connect();
                fileSize += connection.getContentLength();

               // outputFile = new File(apkStorage, downloadFileName);

//                Log.d(TAG, "File download:" +outputFile.getAbsolutePath());
//
//                if(!outputFile.exists()){
//                    outputFile.createNewFile();
//                    Log.e(TAG, "File Created");
//                    Log.d(TAG, "File download:" +outputFile.getAbsolutePath());
//
//                }


                //int fileLength = connection.getContentLength();
                FileOutputStream fos;
                if(downloadedSize >0){
                fos = new FileOutputStream(new File(fileDir,downloadFileName),true);
                }else{
                    fos = new FileOutputStream(new File(fileDir,downloadFileName));
                }

                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int count = 0;
                while((count = is.read(buffer)) != -1){
                    if(isPaused()){
                        break;
                    }
                    downloadedSize +=count;
                    if(fileSize > 0){
                        //update progress
                        publishProgress((int)(100* downloadedSize/fileSize));
                    }

                    fos.write(buffer,0,count);
                }

            //close all connection to avoid memory
                fos.flush();
                fos.close();
                is.close();
                connection.disconnect();

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
