package com.test.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * CountDownLatch
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/19
 * @desc
 * @version: 3.1.2
 */

public class CountDownLatchTest {

  public static void main(String[] args){

    int num = 10;
    CountDownLatch countDownLatch = new CountDownLatch(num);

    List<Passenger> passengers = new ArrayList<>();
    for(int i=1; i<=num; i++){
      passengers.add(new Passenger("乘客" + i, countDownLatch));
    }

    new Thread(new Driver(passengers, countDownLatch)).start();

  }

  private static class Driver implements Runnable{
    List<Passenger> passengers;
    CountDownLatch countDownLatch;

    public Driver(List<Passenger> passengers, CountDownLatch countDownLatch){
      this.passengers = passengers;
      this.countDownLatch = countDownLatch;
    }

    @Override public void run() {
      System.out.println("司机开门，等待乘客排队上车");

      Executor executor = Executors.newCachedThreadPool();

      for(Passenger passenger : passengers){
        executor.execute(passenger);
      }

      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      System.out.println("全部乘客坐好，司机开车啦！");

    }
  }

  /**
   * 乘客
   */
  private static class Passenger implements Runnable{
    String name;
    CountDownLatch countDownLatch;

    public Passenger(String name, CountDownLatch countDownLatch){
      this.name = name;
      this.countDownLatch = countDownLatch;
    }

    @Override public void run() {
      System.out.println(name + ", 排队上车");

      int time = new Random().nextInt(1000);
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      System.out.println(name + ", 已经上车坐好, 等待发车，耗时" + time);

      countDownLatch.countDown();
    }
  }

}
