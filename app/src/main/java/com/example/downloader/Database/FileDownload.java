package com.example.downloader.Database;

import com.example.downloader.DownloadChunk.DownloadChunk;
import com.example.downloader.DownloadChunk.DownloadMultipleChunk;
import com.example.downloader.DownloadThread;

import java.util.ArrayList;
import java.util.List;

public class FileDownload {
    public int _id;
    public String fileName;
    public String urlDownload;
    public int state;
    public String uriFileDir;
    private List<DownloadChunk> listChunks;

    public FileDownload(String urlDownload,int state){
        this.urlDownload = urlDownload;
        this.state = state;
        this.fileName = urlDownload.substring(urlDownload.lastIndexOf('/')+1);
        this.listChunks = new ArrayList<>();

    }
    public FileDownload(){

    }

    public FileDownload(int _id, String fileName, String urlDownload, int state, String uriFileDir) {
        this._id = _id;
        this.fileName = fileName;
        this.urlDownload = urlDownload;
        this.state = state;
        this.uriFileDir = uriFileDir;
    }

    public List<DownloadChunk> getListChunks() {
        return listChunks;
    }

    public void setListChunks(List<DownloadChunk> listChunks) {
        this.listChunks = listChunks;
    }

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
