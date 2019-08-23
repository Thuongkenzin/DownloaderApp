package com.example.downloader.DownloadAsyncTask;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.downloader.R;

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
        TextView txtDownloadName,tvPercent,tvSizeFileDownload;
        ProgressBar pbDownload;
        Button btnDownload;
        ImageButton btnCancel;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDownloadName = itemView.findViewById(R.id.tv_item_number);
            pbDownload = itemView.findViewById(R.id.pb_item_number);
//            btnDownload = itemView.findViewById(R.id.btn_download);
//            tvPercent = itemView.findViewById(R.id.tv_percent);
//            tvSizeFileDownload = itemView.findViewById(R.id.tv_size_file);
//            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
        void bind(final DownloadTask downloadTask){
            txtDownloadName.setText(downloadTask.downloadFileName);
            downloadTask.progressBar = pbDownload;
            downloadTask.textViewPercent = tvPercent;
            downloadTask.sizeDownloaded = tvSizeFileDownload;
            tvSizeFileDownload.setText(String.valueOf(downloadTask.getFileSize()));
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = btnDownload.getText().toString();
                    if(text.equals("Pause")) {
                        btnDownload.setText("Resume");
                        downloadTask.onPause();
                    }else if(text.equals("Resume")) {
                        btnDownload.setText("Pause");
                        downloadTask.onResume();
                    }

                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadTask.onCancelled();
                }
            });
        }

    }
}
