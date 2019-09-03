package com.example.downloader.Database;

public class FileDownload {
    public long _id;
    public String fileName;
    public String urlDownload;
    public int state;
    public long fileLength;
    public String uriFileDir;


    public FileDownload(String urlDownload,String uriFileDir,long fileLength,int state, String fileName){
        this.urlDownload = urlDownload;
        this.state = state;
        this.uriFileDir = uriFileDir;
        this.fileLength = fileLength;
        this.fileName = fileName;

    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public FileDownload(){

    }

    public FileDownload(long _id, String fileName, String urlDownload, int state, String uriFileDir,long fileLength) {
        this._id = _id;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.urlDownload = urlDownload;
        this.state = state;
        this.uriFileDir = uriFileDir;
    }


    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
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
