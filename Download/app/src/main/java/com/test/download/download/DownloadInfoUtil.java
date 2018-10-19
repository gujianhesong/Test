package com.test.download.download;

import com.test.download.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DownloadInfoUtil {

    /**
     * 读取下载信息
     *
     * @param file
     * @return
     */
    public static DownloadInfo readDownloadInfo(File file) {
        if (file.exists() && file.length() > 0) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte[] progressBuffer = new byte[DownloadApi.BUFFER_SIZE];
                int len = fis.read(progressBuffer);
                if (len == progressBuffer.length) {
                    try {
                        //将读取的固定大小文件头部数据转换为下载信息
                        String content = new String(progressBuffer, "utf-8");
                        LogUtil.i("readDownloadInfo content： " + content);
                        String[] array = content.split("<>");
                        if(array != null && array.length >= 4){
                            LogUtil.i("readDownloadInfo array： " + array[0] + ", " + array[1] + ", "
                                    + array[2] + ", " + array[3]);
                            DownloadInfo downloadInfo = new DownloadInfo();
                            downloadInfo.setTotalSize(Long.valueOf(array[0]));

                            //如果文件的实际下载大小和记录中的下载大小不一致，以文件的实际下载大小为准
                            long downloadSize = Long.valueOf(array[1]);
                            long downloadSize2 = file.length() - progressBuffer.length;
                            LogUtil.i("readDownloadInfo： " + downloadSize + ", " + downloadSize2);
                            if (downloadSize != downloadSize2) {
                                downloadSize = downloadSize2;
                            }

                            downloadInfo.setDownloadSize(downloadSize);
                            downloadInfo.setDownloadSize(file.length() - progressBuffer.length);
                            downloadInfo.setUrl(array[2]);
                            downloadInfo.setName(array[3]);

                            LogUtil.i("readDownloadInfo： " + downloadInfo.getDownloadSize() + ", " + downloadInfo.getTotalSize() + ", "
                                    + downloadInfo.getUrl() + ", " + downloadInfo.getName() + ", " + file.getAbsolutePath());

                            return downloadInfo;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    /**
     * 保存下载信息到文件头部
     *
     * @param fos
     * @param downloadInfo
     * @param progressBuffer
     */
    public static void saveDownloadInfo(RandomAccessFile fos, DownloadInfo downloadInfo, byte[] progressBuffer) {
        if (fos == null || downloadInfo == null) return;

        try {
            //LogUtil.i("bytes size : " + downloadInfo.getUrl() + ", " + downloadInfo.getName() + ", " + downloadInfo.getDownloadSize() + ", " + downloadInfo.getTotalSize());

            for (int i = 0; i < progressBuffer.length; i++) {
                progressBuffer[i] = 0;
            }

            //将下载信息转换为字节数组，并复制到固定大小的字节数组中，保存在文件头部
            String content = new StringBuilder()
                    .append(downloadInfo.getTotalSize()).append("<>")
                    .append(downloadInfo.getDownloadSize()).append("<>")
                    .append(downloadInfo.getUrl()).append("<>")
                    .append(downloadInfo.getName()).append("<>")
                    .toString();
            LogUtil.i("saveDownloadInfo content： " + content);
            byte[] bytes = content.getBytes("utf-8");
            System.arraycopy(bytes, 0, progressBuffer, 0, bytes.length);

            //LogUtil.i("bytes size : " + bytes.length + ", " + progressBuffer.length);

            //移动到文件头部，更新下载信息，覆盖模式
            fos.seek(0);
            fos.write(progressBuffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将完成下载的文件转换为真实的文件，需要将头部下载信息去除掉
     *
     * @param file
     * @param progressBuffer
     */
    public static void convertFileToRealFile(File file, byte[] progressBuffer) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            LogUtil.i("下载文件，src size : " + file.length() + ", " + file.getAbsolutePath());
            File newFile = new File(file.getPath() + ".temp");

            fis = new FileInputStream(file);
            fos = new FileOutputStream(newFile);

            byte[] buff = new byte[2048];
            int len = 0;

            //跳过头部下载信息数据
            fis.skip(progressBuffer.length);
            while ((len = fis.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }

            LogUtil.i("下载文件，dest size : " + newFile.length() + ", " + newFile.getAbsolutePath());

            //删除之前的下载文件，将转换后的文件重命名
            file.delete();
            newFile.renameTo(file);

            LogUtil.i("下载文件，dest size : " + newFile.length() + ", " + newFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取下载目录中的所有下载信息
     * @return
     */
    public static HashMap<String, DownloadInfo> getAllDownloadInfo(){
        HashMap<String, DownloadInfo> map = new HashMap<>();

        String downloadDir = DownloadApi.DOWNLOAD_DIR;
        File dir = new File(downloadDir);
        File[] files = dir.listFiles();
        if(files != null){
            for(File file : files){
                DownloadInfo downloadInfo = DownloadInfoUtil.readDownloadInfo(file);
                if(downloadInfo != null && downloadInfo.getUrl() != null){
                    map.put(downloadInfo.getUrl(), downloadInfo);
                }
            }
        }

        return map;
    }

}
