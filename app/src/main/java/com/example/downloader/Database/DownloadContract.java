package com.example.downloader.Database;

import android.provider.BaseColumns;

public final class DownloadContract {
    private DownloadContract(){};

    public static final class DownloadEntry implements BaseColumns{
        public final static String TABLE_NAME = "downloads";
        public final static String TABLE_DOWNLOAD_CHUNK= "chunkdownloads";

        public final static String _ID =BaseColumns._ID;
        public final static String COLUMN_FILE_NAME = "filename";
        public final static String COLUMN_FILE_URL = "urlfile";
        public final static String COLUMN_FILE_DIR = "urldir";
        public final static String COLUMN_FILE_STATE = "state";
        public final static String COLUMN_FILE_LENGTH = "filelength";


        public final static String COLUMN_CHUNK_ID = BaseColumns._ID;
        public final static String COLUMN_START_POSITION = "start";
        public final static String COLUMN_END_POSITION = "end";
        public final static String COLUMN_ID_FILE_DOWNLOAD_FK = "idFileDownload";
        public final static String COLUMN_TOTAL_DOWNLOADED_CHUNK = "totalDownloaded";
        public static final int STATE_COMPLETE = 0;
        public static final int STATE_UNCOMPLETE = 1;
    }


}
