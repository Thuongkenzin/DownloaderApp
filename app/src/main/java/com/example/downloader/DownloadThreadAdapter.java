package com.example.downloader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class DownloadThreadAdapter extends RecyclerView.Adapter<DownloadThreadAdapter.DownloadViewHolder> {

    List<DownloadThread> downloadList;
    DownloadManager downloadManager = DownloadManager.getInstance();

    public DownloadThreadAdapter() {
       this.downloadList = DownloadManager.getInstance().getListDownload();
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.number_list_items,viewGroup,false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder downloadViewHolder, int i) {
        downloadViewHolder.bind(downloadList.get(i));

    }


    @Override
    public int getItemCount() {
        if(downloadList == null){
            return 0;
        }
        return downloadList.size();
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
            btnDownload = itemView.findViewById(R.id.btn_download);
            tvPercent = itemView.findViewById(R.id.tv_percent);
            tvSizeFileDownload = itemView.findViewById(R.id.tv_size_file);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }

        void bind(final DownloadThread downloadThread){
            txtDownloadName.setText(downloadThread.getDownloadFileName());
            downloadThread.setOnUpdateProgressListener(new UpdateProgressListener() {
                @Override
                public void updateProgress(final int progress, final long sizeDownloaded) {
                    downloadThread.setPercent(progress);
                    pbDownload.setProgress(progress);
                    final String sizeDownload = DownloadUtil.getStringSizeLengthFile(sizeDownloaded);
                    final String totalSizeFile =DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize());
                    tvPercent.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvPercent.setText(progress +"%");
                            if(progress == 100){
                                pbDownload.invalidate();
                                //add thread download to the list DownloadComplete and remove in listDownloadPending when download complete
                                downloadManager.addDownloadThreadCompleteToTheList(downloadThread);
                                //notify adapter
                                notifyItemRemoved(getAdapterPosition());
                            }
                        }
                    },500);

                    tvSizeFileDownload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvSizeFileDownload.setText(sizeDownload +"/" + totalSizeFile);
                        }
                    },500);

                }
            });

            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = btnDownload.getText().toString();
                    if(text.equals("Pause")) {
                        btnDownload.setText("Resume");
                        downloadThread.onPause();
                    }else if(text.equals("Resume")) {
                        btnDownload.setText("Pause");
                        downloadThread.onResume();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadThread.onPause();
                    AlertDialog.Builder builder = new AlertDialog.Builder(pbDownload.getContext());
                    builder.setTitle("Do you want to stop downloading this file?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadManager.cancelDownload(downloadThread.getID());
                            notifyItemRemoved(getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            downloadThread.onResume();
                        }
                    });
                    builder.show();
                }
            });
        }
    }
}
