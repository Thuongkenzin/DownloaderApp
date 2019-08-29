package com.example.downloader.Utilities;

import android.os.SystemClock;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DownloadUtil {

    public static String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1000.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;


        if(size < sizeMb)
            return df.format(size / sizeKb)+ " KB";
        else if(size < sizeGb)
            return df.format(size / sizeMb) + " MB";
        else if(size < sizeTerra)
            return df.format(size / sizeGb) + " GB";

        return "";
    }

    public static int createNotificationId(){
        int id = (int) SystemClock.uptimeMillis();
        return id;
    }
    public static String getDayMonthYear(Date date){
        // Date format : "dow mon dd hh:mm:ss zzz yyyy"
        String dateTime = date.toString();

        String[] dateFormat = dateTime.split(" ");
        String dayMonYear = dateFormat[0] +" "+ dateFormat[1] + " " + dateFormat[2] + " " +
                dateFormat[5];
        return  dayMonYear;

    }
    public static String getMimeTypeFile(File file) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        return map.getMimeTypeFromExtension(ext);
    }

}
