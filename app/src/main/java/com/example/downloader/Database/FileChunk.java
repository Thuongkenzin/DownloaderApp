package com.example.downloader.Database;

public class FileChunk {
    long startPosition;
    long endPosition;
    long id;
    long idFileDownload;
    long totalDownloaded;

    public long getTotalDownloaded() {
        return totalDownloaded;
    }

    public void setTotalDownloaded(long totalDownloaded) {
        this.totalDownloaded = totalDownloaded;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdFileDownload() {
        return idFileDownload;
    }

    public void setIdFileDownload(long idFileDownload) {
        this.idFileDownload = idFileDownload;
    }
}
