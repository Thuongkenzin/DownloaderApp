package com.example.downloader;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.downloader.Database.FileDownload;

import java.util.List;


public class FragmentCompleteDownload extends Fragment {
    private static final String TAG = "FragmentList";
    private ListView listView;
    private List<FileDownload> listFileDownloaded = DownloadManager.getInstance().getCompleteListDownload();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complete_download, container, false);
        listView = view.findViewById(R.id.list_item_download_complete);
        Log.v(TAG,"size Download:" +listFileDownloaded.size());
        final DownloadCompleteAdapter adapter = new DownloadCompleteAdapter(getContext(),listFileDownloaded);
        DownloadManager.getInstance().setOnUpdateListDownloadListener(new DownloadManager.UpdateListDownloadListener() {
            @Override
            public void updateList(FileDownload fileDownload) {
               adapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(adapter);
        return view;
    }

}
