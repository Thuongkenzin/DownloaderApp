package com.example.downloader.Database;

public class FileDownload {
    public int _id;
    public String fileName;
    public String urlDownload;
    public int state;
    public String uriFileDir;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getFileName() {
        return fileName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }


    public String getUriFileDir() {
        return uriFileDir;
    }

    public void setUriFileDir(String uriFileDir) {
        this.uriFileDir = uriFileDir;
    }
}
