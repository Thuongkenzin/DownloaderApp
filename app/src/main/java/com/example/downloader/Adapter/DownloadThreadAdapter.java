package com.example.downloader.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.DownloadManager;
import com.example.downloader.Listener.UpdateCompleteDownloadListener;
import com.example.downloader.Utilities.DownloadUtil;
import com.example.downloader.Listener.UpdateProgressListener;
import com.example.downloader.R;

import java.util.List;

public class DownloadThreadAdapter extends RecyclerView.Adapter<DownloadThreadAdapter.DownloadViewHolder> {

    private List<DownloadMultipleChunk> downloadList ;
    DownloadManager downloadManager = DownloadManager.getInstance();

    public DownloadThreadAdapter() {
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

        TextView mDownloadNameTextView, mDescriptionTextView;
        ProgressBar mProgressBarDownload;
        View mCancelButton;
        ImageView mPlayPauseButton;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            mDownloadNameTextView = itemView.findViewById(R.id.tv_item_number);
            mProgressBarDownload = itemView.findViewById(R.id.pb_item_number);
            mCancelButton = itemView.findViewById(R.id.btn_cancel);
            mDescriptionTextView = itemView.findViewById(R.id.tv_description);
            mPlayPauseButton = itemView.findViewById(R.id.btn_control_download);

        }


        void bind(final DownloadMultipleChunk downloadThread) {
            mDownloadNameTextView.setText(downloadThread.getFileName());
            if(downloadThread.getFileSize() !=0){
                int percent = (int)(downloadThread.getTotalDownloadPrev()*100/downloadThread.getFileSize());
                mProgressBarDownload.setProgress(percent);
                mDescriptionTextView.setText(percent+"% - "+"0.00KB/s"+" - "+
                        DownloadUtil.getStringSizeLengthFile(downloadThread.getTotalDownloadPrev())+"/" +
                        DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize()));

            }
            if(downloadThread.getStateDownload() == DownloadMultipleChunk.MODE_RESUME){
                mPlayPauseButton.setImageResource(R.drawable.ic_pause_24dp);
            }else{
                mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_24dp);
            }

            downloadThread.setOnUpdateProgressListener(new UpdateProgressListener() {
                @Override
                public void updateProgress(final int progress, final long sizeDownloaded, final long speedDownload) {

                    mDescriptionTextView.setText(progress + "% - " +
                            DownloadUtil.getStringSizeLengthFile(speedDownload) + "/s" + " - " +
                            DownloadUtil.getStringSizeLengthFile(sizeDownloaded) + "/" +
                            DownloadUtil.getStringSizeLengthFile(downloadThread.getFileSize()));
                    mProgressBarDownload.setProgress(progress);
                }
            });

            downloadThread.setOnUpdateCompleteDownloadListener(new UpdateCompleteDownloadListener() {
                @Override
                public void notifyCompleteDownloadFile() {
                    downloadManager.addFileDownloadDoneToListComplete(downloadThread);
                    downloadManager.updateFileDownloadCompleteToDatabase(downloadThread.getContextDownload().getApplicationContext(),
                            downloadThread.getId());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(downloadThread.getStateDownload() == DownloadMultipleChunk.MODE_RESUME){
                        mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_24dp);
                        downloadThread.pauseChunkDownload();
                    }else if(downloadThread.getStateDownload() == DownloadMultipleChunk.DOWNLOAD_PAUSE){
                        mPlayPauseButton.setImageResource(R.drawable.ic_pause_24dp);
                        downloadManager.resumeDownloadMultipleChunk(downloadThread);
                    }

                }
            });

            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(downloadThread.getStateDownload() != DownloadMultipleChunk.DOWNLOAD_PAUSE) {
                        downloadThread.pauseChunkDownload();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(mProgressBarDownload.getContext());
                    builder.setTitle("Do you want to stop downloading this file?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadManager.cancelDownload(downloadThread);
                            downloadManager.deleteFileFromDatabase(mCancelButton.getContext().getApplicationContext(),
                                    downloadThread.getId());
                            notifyItemRemoved(getAdapterPosition());
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            downloadManager.resumeDownloadMultipleChunk(downloadThread);
                        }
                    });
                    builder.show();
                }
            });
        }
    }


}
