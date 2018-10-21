package com.test.download.util;

import java.text.DecimalFormat;

/**
 * Created by gujian on 2018-10-20.
 */

public class FormatUtil {

    /**
     *  转换文件大小
     *      
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.0");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1024 * 1024) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) fileS / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) fileS / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }

}
