package com.example.downloader;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
    private List<DownloadTask> downloadData;

    public DownloadAdapter(List<DownloadTask> data) {
        this.downloadData = data;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.number_list_items,viewGroup,false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder downloadViewHolder, int i) {
        downloadViewHolder.bind(downloadData.get(i));
    }

    @Override
    public int getItemCount() {
        return downloadData.size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder{
        TextView txtDownloadName;
        ProgressBar pbDownload;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDownloadName = itemView.findViewById(R.id.tv_item_number);
            pbDownload = itemView.findViewById(R.id.pb_item_number);
        }
        void bind(DownloadTask downloadTask){
            txtDownloadName.setText(downloadTask.downloadFileName);
            downloadTask.progressBar = pbDownload;
        }

    }
}