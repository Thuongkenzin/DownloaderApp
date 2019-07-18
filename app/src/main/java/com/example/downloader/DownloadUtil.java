package com.example.downloader;

import android.os.SystemClock;

public class DownloadUtil {

    public static float convertSize(long size) {
        if (size > 1024 && size < 1024 * 1024) {
            return (float)(size / 1024);
        } else if (size >= 1024 * 1024) {
            return (float)(size / (1024* 1024));
        }
        return size;
    }

    public static int createNotificationId(){
        int id = (int) SystemClock.uptimeMillis();
        return id;
    }
}
