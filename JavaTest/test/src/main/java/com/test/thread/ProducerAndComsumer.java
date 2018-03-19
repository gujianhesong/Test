package com.test.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者消费者模型，三种实现方式。
 *
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/19
 * @desc
 * @version: 3.1.2
 */

public class ProducerAndComsumer {

  public static void main(String[] args) {

    //生产者消费者三种实现方式

    Storage storage = null;
    storage = new SynchronizeStorage();
    //storage = new LockStorage();
    //storage = new BlockingStorage();

    // 生产者对象
    List<Producer> producerList = new ArrayList<>();
    producerList.add(new Producer(storage).setName("生产者1").setNum(10));
    producerList.add(new Producer(storage).setName("生产者2").setNum(20));
    producerList.add(new Producer(storage).setName("生产者3").setNum(30));

    // 消费者对象
    List<Consumer> consumerList = new ArrayList<>();
    consumerList.add(new Consumer(storage).setName("消费者1").setNum(10));
    consumerList.add(new Consumer(storage).setName("消费者2").setNum(20));
    consumerList.add(new Consumer(storage).setName("消费者3").setNum(20));
    consumerList.add(new Consumer(storage).setName("消费者4").setNum(20));

    Executor executor = Executors.newCachedThreadPool();

    for (Producer producer : producerList) {
      executor.execute(producer);
    }

    for (Consumer consumer : consumerList) {
      executor.execute(consumer);
    }
  }

  private static class Producer implements Runnable {
    // 名称
    private String name;

    // 每次生产的产品数量
    private int num;

    // 所在放置的仓库
    private Storage storage;

    // 构造函数，设置仓库
    public Producer(Storage storage) {
      this.storage = storage;
    }

    // 线程run函数
    @Override public void run() {
      produce(num);
    }

    // 调用仓库Storage的生产函数
    public void produce(int num) {
      storage.produce(name, num);
    }

    // get/set方法
    public int getNum() {
      return num;
    }

    public Producer setNum(int num) {
      this.num = num;
      return this;
    }

    public Producer setName(String name) {
      this.name = name;
      return this;
    }

    public Storage getStorage() {
      return storage;
    }

    public Producer setStorage(Storage storage) {
      this.storage = storage;
      return this;
    }
  }

  private static class Consumer implements Runnable {
    // 名称
    private String name;

    // 每次消费的产品数量
    private int num;

    // 所在放置的仓库
    private Storage storage;

    // 构造函数，设置仓库
    public Consumer(Storage storage) {
      this.storage = storage;
    }

    // 线程run函数
    @Override public void run() {
      consume(num);
    }

    // 调用仓库Storage的生产函数
    public void consume(int num) {
      storage.consume(name, num);
    }

    // get/set方法
    public int getNum() {
      return num;
    }

    public Consumer setNum(int num) {
      this.num = num;
      return this;
    }

    public Consumer setName(String name) {
      this.name = name;
      return this;
    }

    public Storage getStorage() {
      return storage;
    }

    public Consumer setStorage(Storage storage) {
      this.storage = storage;
      return this;
    }
  }

  private static interface Storage {

    // 仓库最大存储量
    int MAX_SIZE = 100;

    /**
     * 生产num个产品
     */
    void produce(String name, int num);

    /**
     * 消费num个产品
     */
    void consume(String name, int num);
  }

  /**
   * synchronized，wait, notify实现方式
   */
  private static class SynchronizeStorage implements Storage {
    // 仓库存储的载体
    private LinkedList<Object> list = new LinkedList<Object>();

    @Override public void produce(String name, int num) {

      while (true) {

        // 同步代码段
        synchronized (list) {
          // 如果仓库剩余容量不足
          while (list.size() + num > MAX_SIZE) {
            System.out.println(
                name + " 【要生产的产品数量】:" + num + " 【库存量】:" + list.size() + " 满啦，暂时不能执行生产任务!");
            try {
              // 由于条件不满足，生产阻塞
              list.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          // 仓库剩余容量充足，即生产条件满足情况下，生产num个产品
          for (int i = 1; i <= num; ++i) {
            list.add(new Object());
          }

          System.out.println(name + "【已经生产产品数】:" + num + " 【现仓储量为】:" + list.size());

          list.notifyAll(); //生产完产品后，通知其他被阻塞的线程
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    @Override public void consume(String name, int num) {

      while (true) {

        // 同步代码段
        synchronized (list) {
          // 如果仓库存储量不足
          while (list.size() < num) {
            System.out.println(
                name + "【要消费的产品数量】:" + num + " 【库存量】:" + list.size() + " 空啦， 暂时不能执行消费任务!");
            try {
              // 由于条件不满足，消费阻塞
              list.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          // 消费条件满足情况下，消费num个产品
          for (int i = 1; i <= num; ++i) {
            list.remove();
          }

          System.out.println(name + "【已经消费产品数】:" + num + " 【现仓储量为】:" + list.size());

          list.notifyAll();//消费完后，释放锁，通知其他被阻塞的线程
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * ReentrantLock, Condition实现方式
   */
  private static class LockStorage implements Storage {
    // 仓库存储的载体
    private LinkedList<Object> list = new LinkedList<Object>();

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    @Override public void produce(String name, int num) {

      while (true) {

        lock.lock();

        try {

          // 如果仓库剩余容量不足
          while (list.size() + num > MAX_SIZE) {
            System.out.println(
                name + " 【要生产的产品数量】:" + num + " 【库存量】:" + list.size() + " 满啦，暂时不能执行生产任务!");
            try {
              // 由于条件不满足，生产阻塞
              condition.await();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          // 仓库剩余容量充足，即生产条件满足情况下，生产num个产品
          for (int i = 1; i <= num; ++i) {
            list.add(new Object());
          }

          System.out.println(name + "【已经生产产品数】:" + num + " 【现仓储量为】:" + list.size());

          condition.signalAll(); //生产完产品后，通知其他被阻塞的线程
        } catch (Exception ex) {
          ex.printStackTrace();
        } finally {
          lock.unlock();
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    @Override public void consume(String name, int num) {

      while (true) {

        lock.lock();

        try {

          // 如果仓库存储量不足
          while (list.size() < num) {
            System.out.println(
                name + "【要消费的产品数量】:" + num + " 【库存量】:" + list.size() + " 空啦， 暂时不能执行消费任务!");
            try {
              // 由于条件不满足，消费阻塞
              condition.await();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          // 消费条件满足情况下，消费num个产品
          for (int i = 1; i <= num; ++i) {
            list.remove();
          }

          System.out.println(name + "【已经消费产品数】:" + num + " 【现仓储量为】:" + list.size());

          condition.signalAll();//消费完后，释放锁，通知其他被阻塞的线程
        } catch (Exception ex) {
          ex.printStackTrace();
        } finally {
          lock.unlock();
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * BlockingStorage实现方式
   */
  private static class BlockingStorage implements Storage {
    // 仓库存储的载体
    private BlockingDeque<Object> list = new LinkedBlockingDeque<>(MAX_SIZE);

    @Override public void produce(String name, int num) {

      while (true) {

        try {

          // 仓库剩余容量充足，即生产条件满足情况下，生产num个产品
          for (int i = 1; i <= num; ++i) {
            list.put(new Object());
          }

          System.out.println(name + "【已经生产产品数】:" + num + " 【现仓储量为】:" + list.size());
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    @Override public void consume(String name, int num) {

      while (true) {

        try {

          // 仓库剩余容量充足，即生产条件满足情况下，生产num个产品
          for (int i = 1; i <= num; ++i) {
            list.take();
          }

          System.out.println(name + "【已经消费产品数】:" + num + " 【现仓储量为】:" + list.size());
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        try {
          Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
