package com.game.supermario;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

public class LogUtil {
    /**
     * 设置打印日志是否可用
     */
    public static void setEnable(boolean logEnable) {
        hideLog = !logEnable;
    }

    public static boolean hideLog = false;

    private static final String TAG = "LogUtil";

    /**
     * 设置日志文件存储目录
     * @param dirPath
     * @throws Exception
     */
    public static void setLogDirPath(String dirPath) throws Exception {
        LogManager.setLogDirPath(dirPath);
    }

    /**
     * 设置最大日志文件数量
     * @param maxFileCount
     */
    public static void setLogFileMaxCount(int maxFileCount) {
        LogManager.setLogFileMaxCount(maxFileCount);
    }

    /**
     * 设置最大日志文件大小
     * @param maxFileSize
     */
    public static void setLogFileMaxSize(int maxFileSize) {
        LogManager.setLogFileMaxSize(maxFileSize);
    }

    /**
     * 设置是否开启获取logcat中的日志
     * @param openLogcatLog
     */
    public static void setOpenLogcatLog(boolean openLogcatLog) {
        LogManager.setOpenLogcatLog(openLogcatLog);
    }

    /**
     * @param message
     */
    public static void v(String message) {
        v("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void v(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.v(tag, message);
        Printer.print("verbose: " + message);
    }

    /**
     * @param message
     */
    public static void d(String message) {
        d("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.d(tag, message);
        Printer.print("debug: " + message);
    }

    /**
     * @param message
     */
    public static void w(String message) {
        w("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void w(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.w(tag, message);
        Printer.print("warn: " + message);
    }

    /**
     */
    public static void i() {
        i("", "");
    }

    /**
     * @param message
     */
    public static void i(String message) {
        i("", message);
    }

    /**
     * @param showLog
     * @param message
     */
    public static void i(boolean showLog, String message) {
        if (!showLog) {
            return;
        }

        i(message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void i(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.i(tag, message);
        Printer.print("info: " + message);
    }

    /**
     * @param message
     */
    public static void e(String message) {
        e(TAG, message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    /**
     * @param throwable
     */
    public static void e(final Throwable throwable) {
        e(TAG, "", throwable);
    }

    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, String message, final Throwable throwable) {
        if (hideLog) {
            return;
        }

        message = getFunctionName() + message;

        Log.e(tag, message, throwable);
        Printer.print("error: " + message);
    }

    /**
     * @param message
     */
    public static void printStack(String message) {
        try {
            throw new Exception("打印堆栈:" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取捕捉到的异常的字符串
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    public static String wrapMessage(String message){
        return getFunctionName() + message;
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return "";
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(LogUtil.class.getName())) {
                continue;
            }

            return "["
                    + Thread.currentThread().getId()
                    + ": "
                    + st.getFileName()
                    + " : "
                    + st.getLineNumber()
                    + " : "
                    + st.getMethodName()
                    + "]---";
        }

        return "";
    }

    public static class Printer{
        private static List<CharSequence> sMsgList = new LinkedList<>();
        private final static int MAX_MSG_NUM = 500;

        private static Handler mHandler = new Handler(Looper.getMainLooper());

        static WeakReference<PrinterListener> mPrinterListener;
        private static long lastPrintTime;
        private static boolean isDirty = false;
        private static boolean isRunning = false;

        public interface PrinterListener{
            void onPrintUpdate(List<CharSequence> list);
        }

        /**
         * 设置log内容输出监听
         * @param listener
         */
        public static void setPrinterListener(PrinterListener listener){
            if(listener == null){
                isRunning = false;
                mPrinterListener = null;
                return;
            }
            mPrinterListener = new WeakReference<>(listener);
            if(!isRunning){
                isRunning = true;
                new Thread(()->{
                    while (isRunning){
                        if(isDirty){
                            mHandler.post(mPrintRunnable);
                            isDirty = false;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        public static synchronized void clear(){
            sMsgList.clear();
            mHandler.post(mPrintRunnable);
        }

        public static void testPrint(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    while (i++ < 1000){
                        print("test message, current index = " + i);
                    }
                }
            }).start();
        }

        private static void print(String msg){
            if(TextUtils.isEmpty(msg)) return;

            //输出到日志文件
            if(!LogManager.openLogcatLog){
                //当开启了获取logcat日志后，就没必要把当前的日志再重复写入了
                LogManager.output2LogFile(msg);
            }

            //保存到消息列表
            msg = "<font color=red>>>></font>" + msg;
            String temp = null;
            synchronized (Printer.class){
                sMsgList.add(Html.fromHtml(msg));
                while(sMsgList.size() > MAX_MSG_NUM){
                    sMsgList.remove(0);
                }
            }
            isDirty = true;
        }

        private static Runnable mPrintRunnable = new Runnable() {
            @Override
            public void run() {
                if(mPrinterListener != null && mPrinterListener.get() != null){
                    //Log.i("LogUtil", "onPrintUpdate");
                    try {
                        mPrinterListener.get().onPrintUpdate(new ArrayList<>(sMsgList));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 日志管理，可以限制日志文件数量，单个日志文件大小，自动删除超出数量的最旧日志文件，并提供了相关性能测试方法
     */
    private static class LogManager{
        private static LinkedBlockingDeque<String> msgQueue = new LinkedBlockingDeque<>(5000);
        private static boolean isRunning = false;
        private static final String LOG_PREFIX = "log_";
        private static final String LOG_SUFFIX = ".txt";
        private static final String LOG_NAME_FORMAT = LOG_PREFIX + "%d" + LOG_SUFFIX;
        private static final int DEFAULT_MAX_FILE_COUNT = 10;
        private static final int DEFAULT_MAX_FILE_SIZE = 1*1024*1024;
        private static int maxFileCount = DEFAULT_MAX_FILE_COUNT;
        private static int maxFileSize = DEFAULT_MAX_FILE_SIZE;
        private static String logDirPath = "";
        private static boolean debugLog = true;
        //是否继续获取logcat日志
        private static boolean isFetchLogcat = true;
        //是否开启获取logcat中的日志
        private static boolean openLogcatLog = false;

        /**
         * 初始化
         */
        static void init(){
            if(isRunning){
                return;
            }
            isRunning = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileWriter osw = null;
                    String avalableLogPath = null;

                    showDebugLog("init and create thread to run write msg to file");

                    while (true){
                        try {
                            String msg = msgQueue.take();

                            if(osw == null){
                                avalableLogPath = getAvalableFilePath();
                                osw = new FileWriter(avalableLogPath, true);
                                showDebugLog("create OutputStreamWriter, avalableLogPath:" + avalableLogPath);
                            }
                            osw.write(msg + "\n");
                            osw.flush();
                            //showDebugLog("write msg, msg:" + msg);

                            //当前文件大小超出限制处理
                            if(new File(avalableLogPath).length() > maxFileSize){
                                showDebugLog("the avalableLogPath file size is over maxFileSize, avalableLogPath:" + avalableLogPath
                                    + ", size:" + new File(avalableLogPath).length());
                                if(osw != null){
                                    osw.flush();
                                    osw.close();
                                    osw = null;
                                    avalableLogPath = null;
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            //testPerformance();

        }

        /**
         * 性能测试，测试这种日志输出文件的模型是否能承受的住多线程并发高频次大量的日志输出需求
         */
        static void testPerformance(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<1000000; i++){
                        LogUtil.i("xxxxxxxxxxxxxxxxxx first thread, index:" + i + "  =====================================");
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<1000000; i++){
                        LogUtil.i("xxxxxxxxxxxxxxxxxx second thread, index:" + i + "  =====================================");
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0; i<1000000; i++){
                        LogUtil.i("xxxxxxxxxxxxxxxxxx third thread, index:" + i + "  =====================================");
                    }
                }
            }).start();
        }

        /**
         * 获取Logcat输出日志
         */
        static void fetchLogcatLog(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int pid = android.os.Process.myPid();
                    String cmds = null;
                    // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
                    cmds = "logcat  | grep \"(" + pid + ")\"";//打印所有日志信息
                    //cmds = "logcat";//打印所有日志信息
                    // cmds = "logcat -s way";//打印标签过滤信息
                    //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

                    Process logcatProc = null;
                    BufferedReader reader = null;
                    try {
                        logcatProc = Runtime.getRuntime().exec(cmds);
                        reader = new BufferedReader(new InputStreamReader(
                                logcatProc.getInputStream()), 1024);
                        String line = null;
                        while (isFetchLogcat) {
                            line = reader.readLine();
                            if(line == null || line.length() == 0){
                                Thread.sleep(1000);
                                continue;
                            }
                            output2LogFile(line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (logcatProc != null) {
                            logcatProc.destroy();
                            logcatProc = null;
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                                reader = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }

        /**
         * 输出日志消息日志文件
         * @param msg
         */
        static void output2LogFile(String msg){
            if(TextUtils.isEmpty(msg)){
                return;
            }
            if(!isRunning){
                return;
            }
            msgQueue.offer(msg);
        }

        /**
         * 设置日志文件存储目录，不设置的话不会生成日志文件
         * @param dirPath
         * @throws Exception
         */
        static void setLogDirPath(String dirPath) throws Exception {
            if(TextUtils.isEmpty(dirPath)){
                throw new Exception("the dirPath is null");
            }
            File logDir = new File(dirPath);
            if(!logDir.exists()){
                logDir.mkdirs();
            }
            if(!logDir.isDirectory()){
                throw new Exception("the dirPath is not a directory");
            }
            if(dirPath.endsWith("/")){
                logDirPath = dirPath;
            }else{
                logDirPath = dirPath + "/";
            }
            showDebugLog("setLogDirPath, dirPath:" + dirPath);
            init();
        }

        /**
         * 设置最大日志文件数量
         * @param maxFileCount
         */
        public static void setLogFileMaxCount(int maxFileCount) {
            if(maxFileCount > 0){
                LogManager.maxFileCount = maxFileCount;
            }
        }

        /**
         * 设置最大日志文件大小
         * @param maxFileSize
         */
        public static void setLogFileMaxSize(int maxFileSize) {
            if(maxFileSize > 0){
                LogManager.maxFileSize = maxFileSize;
            }
        }

        /**
         * 设置是否开启获取logcat中的日志
         * @param openLogcatLog
         */
        public static void setOpenLogcatLog(boolean openLogcatLog) {
            LogManager.openLogcatLog = openLogcatLog;
            //开启获取logcat日志
            if(openLogcatLog){
                isFetchLogcat = true;
                fetchLogcatLog();
            }else{
                isFetchLogcat = false;
            }
        }

        /**
         * 查询可写入的日志文件路径
         * @return
         * @throws Exception
         */
        static String getAvalableFilePath() throws Exception {
            if(TextUtils.isEmpty(logDirPath)){
                throw new Exception("the logDirPath is null");
            }

            File logDir = new File(logDirPath);
            if(!logDir.exists()){
                logDir.mkdirs();
            }

            String avalableLogPath = null;
            File lastFile = null;
            File[] logFiles = getLogFiles(logDirPath);
            if(logFiles != null && logFiles.length > 0){
                lastFile = logFiles[logFiles.length-1];
                showDebugLog("the last file path:" + lastFile.getAbsolutePath());
            }
            if(lastFile != null){
                if(lastFile.length() < maxFileSize){
                    avalableLogPath = lastFile.getAbsolutePath();
                    showDebugLog("the file size < maxFileSize, avalableLogPath:" + avalableLogPath + ", fileSize:" + lastFile.length());
                }else{
                    int indexNum = getLogFileIndexNum(lastFile.getName());
                    String newLogPath = logDirPath + String.format(LOG_NAME_FORMAT, indexNum+1);
                    new File(newLogPath).createNewFile();
                    avalableLogPath = newLogPath;
                    showDebugLog("the file size > maxFileSize, so create a new file, avalableLogPath:" + avalableLogPath);
                }
            }else{
                String newLogPath = logDirPath + String.format(LOG_NAME_FORMAT, 0);
                new File(newLogPath).createNewFile();
                avalableLogPath = newLogPath;
                showDebugLog("no log file, so create a new file, avalableLogPath:" + avalableLogPath);
            }

            //删除超出最大数量的旧文件
            logFiles = getLogFiles(logDirPath);
            if(logFiles != null && logFiles.length > maxFileCount){
                showDebugLog("the files count > maxFileCount, so delete the oldest file, filepath:" + logFiles[0].getAbsolutePath());
                logFiles[0].delete();
            }

            return avalableLogPath;
        }

        /**
         * 获取当前日志文件的序号
         * @param logFileName
         * @return
         */
        static int getLogFileIndexNum(String logFileName){
            if(logFileName.startsWith(LOG_PREFIX) && logFileName.endsWith(LOG_SUFFIX)){
                String nameNoSuffix = logFileName.substring(0, logFileName.length()-LOG_SUFFIX.length());
                String[] arr = nameNoSuffix.split("_");
                String numStr = arr[arr.length-1];
                try {
                    int num = Integer.valueOf(numStr);
                    return num;
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            return -1;
        }

        /**
         * 获取当然目录的所有日志文件，从小到大排序
         * @param logDirPath
         * @return
         */
        static File[] getLogFiles(String logDirPath){
            File logDir = new File(logDirPath);
            File[] childs = logDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int fileIndexNum = getLogFileIndexNum(name);
                    return fileIndexNum >= 0;
                }
            });
            if(childs != null){
                Arrays.sort(childs, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        int file1IndexNum = getLogFileIndexNum(file1.getName());
                        int file2IndexNum = getLogFileIndexNum(file2.getName());
                        return file1IndexNum - file2IndexNum;
                    }
                });
            }
            return childs;
        }

        /**
         * 输出debug日志
         * @param msg
         */
        static void showDebugLog(String msg){
            if(debugLog){
                Log.i("LogManager", msg);
            }
        }
    }
}
