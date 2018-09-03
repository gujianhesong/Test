package com.test.memory.common;

/**
 * Created by hesong-os on 2018/9/3.
 */

public class ControInfos {

    //是否测试内存相关问题
    public static boolean testMemory = false;
    //是否测试内存泄露问题
    public static boolean testMemoryLeak = false;
    //是否测试内存占用优化问题
    public static boolean testMemoryOptimize = false;

    /**
     * 重置
     */
    public static void reset(){
        testMemory = false;
        testMemoryLeak = false;
        testMemoryOptimize = false;
    }

}
