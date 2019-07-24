package com.example.downloader;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button mDownloadBtn;
    Button mViewBtn;
    private RecyclerView mDownloadList;
    private FloatingActionButton addLink;
//    private DownloadAdapter downloadAdapter;
//    List<DownloadTask> downloadData = new ArrayList<DownloadTask>();
    private DownloadThreadAdapter downloadThreadAdapter;
    List<DownloadThread> downloadList = new ArrayList<DownloadThread>();
    private DrawerLayout mDrawerLayout;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownloadBtn = findViewById(R.id.downloadURL);
        addLink = findViewById(R.id.fab_add_link);
        mViewBtn = findViewById(R.id.view_download);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);


        mDownloadList = findViewById(R.id.rv_numbers);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mDownloadList.addItemDecoration(itemDecoration);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDownloadList.setLayoutManager(layoutManager);

//        downloadAdapter = new DownloadAdapter(downloadData);
//        mDownloadList.setAdapter(downloadAdapter);

        final com.example.downloader.DownloadManager downloadManager = com.example.downloader.DownloadManager.getInstance();
        downloadList = downloadManager.getListDownload();
        downloadThreadAdapter = new DownloadThreadAdapter();
        mDownloadList.setAdapter(downloadThreadAdapter);

        //handle download file
        addLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //init a dialog to get link
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add link to download:");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Type or paste link");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        downloadManager.startUrlDownload(url);
                        downloadThreadAdapter.notifyItemInserted(0);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url ="https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
                String url2 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                String url3 = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
                if(isConnectingToInternet()){
                   downloadManager.startUrlDownload(url);
                   downloadManager.startUrlDownload(url2);
                   //downloadManager.startUrlDownload(url3);
                   downloadThreadAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(MainActivity.this, "There is no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //open download folder
        mViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                //openDownloadFolder();

            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()){
                    case R.id.download_dir:
                        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

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
