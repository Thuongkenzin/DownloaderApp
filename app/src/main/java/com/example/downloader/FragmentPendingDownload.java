package com.example.downloader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.downloader.Database.DownloadContract;
import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.Database.FileDownload;


public class FragmentPendingDownload extends Fragment {
    Button mDownloadBtn;
    Button mViewBtn;
    final com.example.downloader.DownloadManager downloadManager = com.example.downloader.DownloadManager.getInstance();
    private RecyclerView mDownloadList;
    private FloatingActionButton mAddLink;
    private DownloadThreadAdapter downloadThreadAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_download,container,false);

        mDownloadBtn = view.findViewById(R.id.downloadURL);
        mViewBtn = view.findViewById(R.id.view_download);
        mAddLink = view.findViewById(R.id.fab_add_link);

        mDownloadList = view.findViewById(R.id.rv_numbers);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mDownloadList.addItemDecoration(itemDecoration);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mDownloadList.setLayoutManager(layoutManager);

        downloadThreadAdapter = new DownloadThreadAdapter();
        mDownloadList.setAdapter(downloadThreadAdapter);

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url ="https://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
                String url2 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                String url3 = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
                if(isConnectingToInternet()){
//                    downloadManager.startUrlDownload(url);
//                    downloadManager.startUrlDownload(url2);
//                    downloadManager.startUrlDownload(url3);
//                    downloadManager.startUrlDownload("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4");
                    //  downloadManager.startUrlDownload("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_2mb.mp4");
                    //      downloadManager.startUrlDownload("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4");
                    downloadThreadAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //open download folder
        mViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS));
                //openDownloadFolder();

            }
        });
        mAddLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //init a dialog to get link
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add link to download:");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Type or paste link");
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        if(URLUtil.isValidUrl(url)) {
                            //them vao database
                            //FileDownload fileDownload = new FileDownload(url,DownloadContract.DownloadEntry.STATE_UNCOMPLETE);
//                            DownloadDatabaseHelper.getInstance(getContext()).addFile(fileDownload);
//                            downloadManager.startUrlDownload(url,fileDownload._id);
                            downloadManager.addFileDownloadToData(getContext(),url);
                            downloadThreadAdapter.notifyItemInserted(0);
                        }else{
                            Toast.makeText(getContext(), "Url is invalid, please try again!", Toast.LENGTH_SHORT).show();
                        }
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
}
