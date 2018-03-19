package com.test.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * CyclicBarrier
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/19
 * @desc
 * @version: 3.1.2
 */

public class CyclicBarrierTest {

  public static void main(String[] args){

    int num = 10;
    CyclicBarrier barrier = new CyclicBarrier(num);

    List<Passenger> passengers = new ArrayList<>();
    for(int i=1; i<=barrier.getParties(); i++){
      passengers.add(new Passenger("乘客" + i, barrier));
    }

    Executor executor = Executors.newCachedThreadPool();

    for(Passenger passenger : passengers){
      executor.execute(passenger);
    }

  }

  /**
   * 乘客
   */
  private static class Passenger implements Runnable{
    String name;
    CyclicBarrier barrier;

    public Passenger(String name, CyclicBarrier barrier){
      this.name = name;
      this.barrier = barrier;
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

      try {
        barrier.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BrokenBarrierException e) {
        e.printStackTrace();
      }

      System.out.println(name + ", 全部就绪，开始发车！");
    }
  }

}
