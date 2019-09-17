package com.example.downloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.downloader.UI.FragmentCompleteDownload;
import com.example.downloader.UI.FragmentPendingDownload;
import com.example.downloader.UI.TabAdapter;
import com.example.downloader.Utilities.CheckForSDCard;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final com.example.downloader.DownloadManager downloadManager = com.example.downloader.DownloadManager.getInstance();

    private DrawerLayout mDrawerLayout;
    private TabAdapter mTabAdapter;
    private TabLayout mTabLayout;
    private ViewPager mPageViewer;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.browser_internet:
               String url = "https://google.com";
               startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
            case R.id.open_download:
                //startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                openDownloadFolder();
                break;
            case R.id.delete_list:
                downloadManager.clearListCompleteDownload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //turnOnStrictMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
       // actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        getPermission();
        if(downloadManager.getListDownloadFile().size() == 0) {
            downloadManager.getListDownloadFile().clear();
            downloadManager.getCompleteListDownload().clear();
            downloadManager.getFileDownloadFromDatabase(this.getApplicationContext());
        }
        createViewPagerLayout();

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
                        openDownloadFolder();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    private void createViewPagerLayout(){
        mPageViewer = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tabLayout);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mTabAdapter = new TabAdapter(fragmentManager);
        mTabAdapter.addFragment(new FragmentPendingDownload(),"Pending Download");
        mTabAdapter.addFragment(new FragmentCompleteDownload(),"File Downloaded");
        mPageViewer.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mPageViewer);
    }

    private void turnOnStrictMode(){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    private void openDownloadFolder(){
        if(new CheckForSDCard().isSDCardPresent()){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                Log.v("OpenDownload", "Path:" + uri.toString());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setDataAndType(uri, "*/*");
                //startActivity(Intent.createChooser(intent,"Open Download Folder"));
                startActivity(intent);
        }else {
            Toast.makeText(MainActivity.this, "There is no SD Card.", Toast.LENGTH_SHORT).show();
        }


    }

    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //do nothing
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

            }
        }
    }


}
