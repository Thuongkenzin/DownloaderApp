package com.example.downloader.UI;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloader.Adapter.DownloadThreadAdapter;
import com.example.downloader.DownloadService;
import com.example.downloader.R;

//Link download example
//                String url ="https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
//                String url2 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
//                String url3 = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";

//                 https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4");
//                  "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_2mb.mp4");
//"https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4");

public class FragmentPendingDownload extends Fragment {
    final com.example.downloader.DownloadManager downloadManager = com.example.downloader.DownloadManager.getInstance();
    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;
    private FloatingActionButton mAddLinkButton;
    private DownloadThreadAdapter downloadThreadAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_download,container,false);

        mAddLinkButton = view.findViewById(R.id.fab_add_link);

        mRecyclerView = view.findViewById(R.id.rv_numbers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mEmptyTextView = view.findViewById(R.id.empty_text_view);
        downloadThreadAdapter = new DownloadThreadAdapter();
        mRecyclerView.setAdapter(downloadThreadAdapter);
        if(downloadThreadAdapter.getItemCount() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        downloadThreadAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();

            }
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            public void checkEmpty(){
                if(downloadThreadAdapter.getItemCount() == 0){
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }else{
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyTextView.setVisibility(View.GONE);
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_ADD);
        intentFilter.addAction(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_PAUSE);
        intentFilter.addAction(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_CANCEL);
        intentFilter.addAction(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_STOP_ALL_DOWNLOAD);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdateListDownloadReceiver,
                intentFilter);


        mAddLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //init a dialog to get link
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add link to download:");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Type or paste link");
                input.setText("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        if (isConnectingToInternet()) {
                            if (URLUtil.isValidUrl(url)) {
                                //service running
                                Intent intentDownload = new Intent(getContext(), DownloadService.class)
                                        .setAction(DownloadService.ACTION_SEND_URL_DOWNLOAD);
                                intentDownload.putExtra(DownloadService.URL_FILE_DOWNLOAD, url);
                                getContext().startService(intentDownload);
                                //testDownload();
                            } else {
                                Toast.makeText(getContext(), "Url is invalid, please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getContext(), "There is no internet connection.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        testDownload();
                    }
                });
                builder.show();
            }
        });
        return view;
    }
    private boolean isConnectingToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = connectivityManager.getActiveNetworkInfo();
        if(netWorkInfo!=null && netWorkInfo.isConnected())
            return true;
        else
            return false;
    }

    private BroadcastReceiver mUpdateListDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_ADD)){
                downloadThreadAdapter.notifyItemInserted(0);
            }
            if(intent.getAction().equals(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_PAUSE)){
                int position = intent.getIntExtra("position",0);
                downloadThreadAdapter.notifyItemChanged(position);
            }
            if(intent.getAction().equals(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_CANCEL)){
                int position = intent.getIntExtra("position",0);
                downloadThreadAdapter.notifyItemRemoved(position);
            }
            if(intent.getAction().equals(DownloadService.ACTION_UPDATE_LIST_DOWNLOAD_STOP_ALL_DOWNLOAD)){
                downloadThreadAdapter.notifyDataSetChanged();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateListDownloadReceiver);
    }

    public void testDownload(){
        Intent intentDownload2 = new Intent(getContext(), DownloadService.class)
                .setAction(DownloadService.ACTION_SEND_URL_DOWNLOAD);
        String url2 = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
        intentDownload2.putExtra(DownloadService.URL_FILE_DOWNLOAD, url2);
        getContext().startService(intentDownload2);

        Intent intentDownload3 = new Intent(getContext(), DownloadService.class)
                .setAction(DownloadService.ACTION_SEND_URL_DOWNLOAD);
        String url3 = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_2mb.mp4";
        intentDownload3.putExtra(DownloadService.URL_FILE_DOWNLOAD, url3);
        getContext().startService(intentDownload3);
    }
}
