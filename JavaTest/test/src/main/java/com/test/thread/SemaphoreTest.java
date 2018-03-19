package com.test.thread;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Semaphore信号量
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/19
 * @desc
 * @version: 3.1.2
 */

public class SemaphoreTest {

  public static void main(String[] args){

    int num = 100;
    Semaphore semaphore = new Semaphore(5);

    Executor executor = Executors.newCachedThreadPool();

    for(int i=1; i<=100; i++){
      TicketBuyer buyTicket1 = new TicketBuyer("购票者" + i, semaphore);

      executor.execute(buyTicket1);
    }

  }

  /**
   * 购票者
   */
  private static class TicketBuyer implements Runnable{
    String name;
    Semaphore semaphore;

    public TicketBuyer(String name, Semaphore semaphore){
      this.name = name;
      this.semaphore = semaphore;
    }

    @Override public void run() {

      try {
        semaphore.acquire();

        int time = new Random().nextInt(3000);

        System.out.println(name + ", 开始购票");

        try {
          Thread.sleep(time);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        System.out.println(name + ", 完成购票, 耗时" + time);


      } catch (InterruptedException e) {
        e.printStackTrace();
      }finally {
        semaphore.release();
      }

    }
  }

}
