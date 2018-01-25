package com.test.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Thread的interrupt方法测试
 *
 * @author hesong
 * @time 2018/1/16
 * @desc
 */

public class InterruptTest {
  //这里用来打印消耗的时间
  private static long time = 0;
  private static void resetTime(){
    time = System.currentTimeMillis();
  }
  private static void printContent(String content){
    System.out.println(content + "     时间：" + (System.currentTimeMillis() - time));
  }

  public static void main(String[] args) {

    test1();

    //test2();

  }

  private static void test1(){

    Thread1 thread1 = new Thread1();
    thread1.start();

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    thread1.interrupt();
    printContent("执行中断");

  }

  private static void test2(){

    ExecutorService service = Executors.newSingleThreadExecutor();
    Future task = service.submit(new Thread1());

    try {
      task.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
      task.cancel(true);
    }finally {
      task.cancel(true);
    }

    //测试，还是无法中断while循环

  }

  private static class Thread1 extends Thread{

    @Override public void run() {

      resetTime();

      Thread2 thread2 = new Thread2();
      thread2.start();

      int num = 0;
      while (true){
        if(isInterrupted()){
          printContent("当前线程 isInterrupted");
          break;
        }

        num++;
        //try {
        //  Thread.sleep(1);
        //} catch (InterruptedException e) {
        //  printContent("捕获异常：" + e.getMessage());
        //  printContent("当前线程 是否 isInterrupted : " + isInterrupted());
        //}

        //try {
        //  thread2.join();
        //} catch (InterruptedException e) {
        //  e.printStackTrace();
        //  printContent("捕获异常：" + e.getMessage());
        //  printContent("当前线程 是否 isInterrupted : " + isInterrupted());
        //}

        if(num % 100 == 0){
          printContent("num : " + num);
        }

        while(true){
          printContent("num : " + num);
        }
      }

    }

  }

  private static class Thread2 extends Thread{
    @Override public void run() {

      while(true){
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
          printContent("Thread2 捕获异常：" + e.getMessage());
          printContent("Thread2 当前线程 是否 isInterrupted : " + isInterrupted());
        }
      }


    }
  }


}
