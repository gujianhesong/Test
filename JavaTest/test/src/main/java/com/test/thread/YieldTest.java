package com.test.thread;

import java.util.Vector;

import static java.awt.SystemColor.info;
import static java.lang.Thread.sleep;
import static java.lang.Thread.yield;

/**
 * Thread的yeild方法测试
 *
 * 得出结论：yield会作出让位的操作，给其他线程机会
 *
 *
 * @author hesong
 * @time 2018/1/9
 * @desc
 */
public class YieldTest{

  private static final int DEST_NUM = 50;

  public static void main(String[] args) {
    new YieldTest();
  }

  public YieldTest(){

    ThreadDemo yt1 = new ThreadDemo("张三");
    ThreadDemo yt2 = new ThreadDemo("李四");
    ThreadDemo yt3 = new ThreadDemo("王五");

    yt1.setPriority(10);
    yt1.setPriority(5);
    yt3.setPriority(1);

    yt1.start();
    yt2.start();
    yt3.start();

  }

  public class ThreadDemo extends Thread{

    public ThreadDemo(String name){
      setName(name);
    }

    @Override public void run() {

      for (int i = 1; i <= DEST_NUM; i++) {

        // 当i为30时，该线程就会把CPU时间让掉，让其他或者自己的线程执行（也就是谁先抢到谁执行）
        if (i % 5 == 0) {
          System.out.println("" + this.getName() + "-----" + i + "    yeild一下");
          yield();
        }else{
          System.out.println("" + this.getName() + "-----" + i);
        }
      }

    }

  }

}

//public class YieldTest{
//
//  private static final int DEST_NUM = 100;
//
//  public static void main(String[] args) {
//    new YieldTest();
//  }
//
//  public YieldTest(){
//    mVector.clear();
//
//    ThreadDemo yt1 = new ThreadDemo("张三");
//    ThreadDemo yt2 = new ThreadDemo("李四");
//    ThreadDemo yt3 = new ThreadDemo("王五");
//
//    //yt1.setPriority(10);
//    //yt1.setPriority(5);
//    //yt3.setPriority(1);
//
//    yt1.start();
//    yt2.start();
//    yt3.start();
//
//    try {
//      sleep(5000);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//
//    printResult();
//  }
//
//  public class ThreadDemo extends Thread{
//
//    public ThreadDemo(String name){
//      setName(name);
//    }
//
//    @Override public void run() {
//
//      runWithSasticate();
//
//      //for (int i = 1; i <= 100; i++) {
//      //
//      //  // 当i为30时，该线程就会把CPU时间让掉，让其他或者自己的线程执行（也就是谁先抢到谁执行）
//      //  if (i % 5 == 0) {
//      //    System.out.println("" + this.getName() + "-----" + i + "    yeild一下");
//      //    yield();
//      //  }else{
//      //    System.out.println("" + this.getName() + "-----" + i);
//      //  }
//      //}
//
//    }
//
//    private void runWithSasticate(){
//      for (int i = 1; i <= 100; i++) {
//        // 当i为30时，该线程就会把CPU时间让掉，让其他或者自己的线程执行（也就是谁先抢到谁执行）
//        if (i % 5 == 0) {
//          //System.out.println("" + this.getName() + "-----" + i + "    yeild一下");
//          sasticate(getName(), i, true);
//          yield();
//        }else{
//          //System.out.println("" + this.getName() + "-----" + i);
//          sasticate(getName(), i, false);
//        }
//      }
//    }
//
//  }
//
//  private void printResult(){
//    //System.out.println("打印列表中的结果。。。。。。");
//    //for(PrintInfo info : mVector){
//    //  if(info.yeild){
//    //    System.out.println("" + info.name + "-----" + info.value + "    yeild一下");
//    //  }else{
//    //    System.out.println("" + info.name + "-----" + info.value);
//    //  }
//    //}
//
//    String lastName = null;
//    for(PrintInfo info : mVector){
//
//      if(lastName != null && lastName.equals(info.name)){
//        System.out.println("惊呆了-----------------" + info.name + " yeild 之后，又继续执行了！" + info.value);
//      }
//
//      if(info.yeild){
//        System.out.println("" + info.name + "-----" + info.value + "    yeild一下");
//
//        lastName = info.name;
//      }else{
//        System.out.println("" + info.name + "-----" + info.value);
//
//        lastName = null;
//      }
//    }
//
//  }
//
//  private Vector<PrintInfo> mVector = new Vector<>();
//  private void sasticate(String name, int value, boolean yeild){
//    mVector.add(new PrintInfo(name, value, yeild));
//  }
//
//  private class PrintInfo{
//    public String name;
//    public int value;
//    public boolean yeild;
//    public PrintInfo(String name, int value, boolean yeild){
//      this.name = name;
//      this.value = value;
//      this.yeild = yeild;
//    }
//  }
//
//}