package com.example.downloader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.downloader.Database.DownloadDatabaseHelper;
import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.Database.FileDownload;

import java.util.List;

public class DownloadThreadAdapter extends RecyclerView.Adapter<DownloadThreadAdapter.DownloadViewHolder> {

    //List<DownloadThread> downloadList;
    List<DownloadMultipleChunk> downloadList;
    DownloadManager downloadManager = DownloadManager.getInstance();

    public DownloadThreadAdapter() {
        //this.downloadList = DownloadManager.getInstance().getListDownload();
        this.downloadList = DownloadManager.getInstance().getListDownloadFile();
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.number_list_items, viewGroup, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder downloadViewHolder, int i) {
        downloadViewHolder.bind(downloadList.get(i));
    }


    @Override
    public int getItemCount() {
        if (downloadList == null) {
            return 0;
        }
        return downloadList.size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder {

        TextView txtDownloadName, tvPercent, tvSizeFileDownload, tvSpeedDownload;
        ProgressBar pbDownload;
        Button btnDownload;
        ImageButton btnCancel;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDownloadName = itemView.findViewById(R.id.tv_item_number);
            pbDownload = itemView.findViewById(R.id.pb_item_number);
            btnDownload = itemView.findViewById(R.id.btn_download);
            tvPercent = itemView.findViewById(R.id.tv_percent);
            tvSpeedDownload = itemView.findViewById(R.id.tv_speed_download);
            tvSizeFileDownload = itemView.findViewById(R.id.tv_size_file);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }

        void bind(final DownloadMultipleChunk downloadThread) {
            pbDownload.setProgress(downloadThread.getPercent());
            if (downloadThread.getStateDownload() == DownloadMultipleChunk.DOWNLOAD_PAUSE) {
                btnDownload.setText("Resume");
            } else if(downloadThread.getStateDownload() == DownloadMultipleChunk.DOWNLOAD_RESUME) {
                btnDownload.setText("Pause");
            }
            txtDownloadName.setText(downloadThread.getFileName());
            downloadThread.setOnUpdateProgressListener(new UpdateProgressListener() {
                @Override
                public void updateProgress(final int progress, final long sizeDownloaded, final long speedDownload) {

                    pbDownload.setProgress(progress);

                    tvSpeedDownload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvSpeedDownload.setText(DownloadUtil.getStringSizeLengthFile(speedDownload) + "/s");
                        }
                    }, 500);
                    tvPercent.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvPercent.setText(progress + "%");
                            if (downloadThread.getStateDownload() == DownloadMultipleChunk.DOWNLOAD_SUCCESS) {
                                pbDownload.invalidate();
                                //update database download complete;
//                                DownloadDatabaseHelper instance = DownloadDatabaseHelper.getInstance(pbDownload.getContext());
//                                DownloadDatabaseHelper.getInstance(pbDownload.getContext()).updateFileDownload(new FileDownload(downloadThread.getID(),
//                                        downloadThread.getDownloadFileName(), downloadThread.getUrlDownload(), downloadThread.getmState(), downloadThread.filePathDownload));
//                                Log.d("IDownload", "ID: " + downloadThread.getID());
//                                //add thread download to the list DownloadComplete and remove in listDownloadPending when download complete
//                                downloadManager.updateListDownloadComplete(instance.getFileItemDownload(downloadThread.getID()));
                                //notify adapter
                                downloadList.remove(downloadThread);
                                notifyItemRemoved(getAdapterPosition());
                            }
                        }
                    },500);

                    tvSizeFileDownload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvSizeFileDownload.setText(DownloadUtil.getStringSizeLengthFile(sizeDownloaded) + "/" + DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize()));
                        }
                    },500);

                }
            });

            if(downloadThread.getStateDownload() == DownloadMultipleChunk.DOWNLOAD_SUCCESS){
                tvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadList.remove(downloadThread);
                        notifyItemRemoved(getAdapterPosition());
                    }
                });
            }


            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = btnDownload.getText().toString();
                    if (text.equals("Pause")) {
                        btnDownload.setText("Resume");
                        downloadThread.pauseChunkDownload();
                    } else if (text.equals("Resume")) {
                        btnDownload.setText("Pause");
                        downloadThread.resumeChunkDownload();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadThread.pauseChunkDownload();
                    AlertDialog.Builder builder = new AlertDialog.Builder(pbDownload.getContext());
                    builder.setTitle("Do you want to stop downloading this file?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadManager.cancelDownload(downloadThread.getId(), btnCancel.getContext());
                            notifyItemRemoved(getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            downloadThread.resumeChunkDownload();
                        }
                    });
                    builder.show();
                }
            });
        }
    }
}
