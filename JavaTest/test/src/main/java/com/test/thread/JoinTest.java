package com.test.thread;

/**
 * Thread的join方法测试
 *
 * 得出结论：
 *      1.当调用了threadA.join()之后，会等待threadA线程执行完成之后，才会往下执行
 *      2.当调用了threadA.join(2000)之后，会最多等待threadA线程2000时间，就会往下执行
 *      3.当调用了threadA.join(6000)之后，因为设定的等待时间大于线程的执行时间，所以等线程执行完后就会往下执行了，和1效果一样
 *      4.当同时调用了上面三个方法，则结果是等线程执行完后，才会往下执行，和1效果一样
 *
 * @author hesong
 * @time 2018/1/9
 * @desc
 */
public class JoinTest {
  private static long time = 0;
  private static void resetTime(){
    time = System.currentTimeMillis();
  }
  private static void printContent(String content){
    System.out.println(content + "     时间：" + (System.currentTimeMillis() - time));
  }

  public static void main(String[] argg){
    resetTime();

    printContent("main 方法开始");

    ThreadA threadA = new ThreadA();
    threadA.start();

    //这里调用了threadA.join()之后，会等待threadA线程执行完成之后，才会往下执行
    try {
      threadA.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //这里join的等待时间大于线程的执行时间，所以等线程执行完后，就会往下执行，和threadA.join()效果是一样的
    //try {
    //  threadA.join(6000);
    //} catch (InterruptedException e) {
    //  e.printStackTrace();
    //}

    //这里调用了threadA.join(2000)之后，会最多等待threadA线程2000时间，就会往下执行
    //try {
    //  threadA.join(2000);
    //} catch (InterruptedException e) {
    //  e.printStackTrace();
    //}

    printContent("main 方法完成");
  }

  public static class ThreadA extends Thread{

    public ThreadA(){
      setName("ThreadA");
    }

    @Override public void run() {

      printContent(String.format("%s 开始执行", getName()));

      try {
        Thread.sleep(4000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      printContent(String.format("%s 完成了", getName()));
    }
  }



}
