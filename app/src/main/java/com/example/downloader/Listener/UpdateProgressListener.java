package com.example.downloader.Listener;

public interface UpdateProgressListener {
     void updateProgress(int progress,long sizeDownloaded, long speedDownload);
}
