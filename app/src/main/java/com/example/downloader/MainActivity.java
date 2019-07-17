package com.example.downloader;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button mDownloadBtn;
    Button mViewBtn;
    private RecyclerView mDownloadList;
    private DownloadAdapter downloadAdapter;
    ArrayList<DownloadTask> downloadData = new ArrayList<DownloadTask>();

    ProgressBar progressBar1,progressBar2,progressBar3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownloadBtn = findViewById(R.id.downloadURL);

        mViewBtn = findViewById(R.id.view_download);

        progressBar1 = findViewById(R.id.progress_bar);
        progressBar1.setContentDescription("Download file");
        progressBar2= findViewById(R.id.progress_bar2);
        progressBar3 = findViewById(R.id.progress_bar3);

        mDownloadList = findViewById(R.id.rv_numbers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDownloadList.setLayoutManager(layoutManager);

        downloadAdapter = new DownloadAdapter(downloadData);
        mDownloadList.setAdapter(downloadAdapter);


        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String url = "http://androhub.com/demo/demo.pdf";
                String url ="https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
                String url2 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                String url3 = "http://androhub.com/demo/demo.mp4";

                if(isConnectingToInternet()) {
                    downloadData.add(new DownloadTask(MainActivity.this, url));
                    downloadData.add(new DownloadTask(MainActivity.this,url2));
                    downloadData.add(new DownloadTask(MainActivity.this,url3));
                    mDownloadList.setAdapter(downloadAdapter);

                }else{
                    Toast.makeText(MainActivity.this, "There is no internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                //openDownloadFolder();

            }
        });
    }



    private void startDownloading(){
        //String url = urlText.getText().toString();
        //String url = "https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
        String url = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";

        //create download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));


        //set title download notification
        request.setDescription("Downloading file...");


        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

    }



    private void openDownloadFolder(){
        if(new CheckForSDCard().isSDCardPresent()){

            File apkStorage = new File(Environment.getExternalStorageDirectory()+"/" + "Android Download");
            Log.d("MainActivity","File Storage:" + apkStorage.getAbsolutePath());

            if(!apkStorage.exists()){
                Toast.makeText(this, "There is no directory", Toast.LENGTH_SHORT).show();
            }
            else{

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() +"/" +"Android Download");
                Uri uri2 = Uri.parse(Environment.DIRECTORY_DOWNLOADS);
                Log.d("MainActivity", "URI:" + uri.toString());
                intent.setDataAndType(uri2, "*/*");
                //startActivity(Intent.createChooser(intent,"Open Download Folder"));
                startActivity(intent);

            }
        }else {
            Toast.makeText(MainActivity.this, "There is no SD Card.", Toast.LENGTH_SHORT).show();
        }


    }
    private boolean isConnectingToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = connectivityManager.getActiveNetworkInfo();
        if(netWorkInfo!=null && netWorkInfo.isConnected())
            return true;
        else
            return false;
    }

}
