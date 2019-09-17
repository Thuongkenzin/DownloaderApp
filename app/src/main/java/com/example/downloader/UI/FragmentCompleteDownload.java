package com.example.downloader.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.downloader.Adapter.DownloadCompleteAdapter;
import com.example.downloader.Database.FileDownload;
import com.example.downloader.DownloadManager;
import com.example.downloader.MyApplication;
import com.example.downloader.R;
import com.squareup.leakcanary.RefWatcher;

import java.util.List;


public class FragmentCompleteDownload extends Fragment {
    private static final String TAG = "FragmentList";
    private ListView listView;
    private List<FileDownload> listFileDownloaded = DownloadManager.getInstance().getCompleteListDownload();
    private DownloadManager.UpdateListDownloadListener mUpdateListener;
    private DownloadCompleteAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_download, container, false);
        listView = view.findViewById(R.id.list_item_download_complete);
        Log.v(TAG,"size Download:" +listFileDownloaded.size());
        adapter = new DownloadCompleteAdapter(getContext(),listFileDownloaded);
        mUpdateListener = DownloadManager.getInstance().setOnUpdateListDownloadListener(new DownloadManager.UpdateListDownloadListener() {
            @Override
            public void updateList() {
               adapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        return view;
    }

    @Override
    public void onDestroy() {

        if(mUpdateListener != null){
            mUpdateListener =  DownloadManager.getInstance().setOnUpdateListDownloadListener(null);
        }
        super.onDestroy();
    }
}
