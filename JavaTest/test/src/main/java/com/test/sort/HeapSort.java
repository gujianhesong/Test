package com.test.sort;

import java.util.Random;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/7
 * @desc
 * @version: 3.1.2
 */

public class HeapSort {

  public static void main(String args[]){

    int[] datas = new int[200];
    for(int i=0; i<datas.length; i++){
      datas[i] = new Random().nextInt(200);
    }

    print(datas);

    heapSort(datas, datas.length);

    print(datas);
  }

  /**
   *
   * @param datas
   * @param length
   */
  private static void heapSort(int[] datas, int length){

    //构建堆
    for(int i=length/2-1; i>=0; i--){
      adjustHeap(datas, i, length-1);
    }

    //交换堆顶元素，并调整堆，保证最大堆特性
    for(int i=length-1; i>=0; i--){
      swap(datas, 0, i);
      adjustHeap(datas, 0, i-1);
    }
  }

  private static void adjustHeap(int[] datas, int index, int high){
    int left = index * 2 + 1;

    while(left <= high){
      if(left+1 <= high && datas[left] < datas[left+1]){
        left++;
      }

      if(datas[index] < datas[left]){
        swap(datas, index, left);
      }

      //将子节点作为位当前节点，继续比较交换
      index = left;
      left = left * 2 + 1;
    }
  }

  private static void swap(int[] datas, int a, int b){
    int temp = datas[a];
    datas[a] = datas[b];
    datas[b] = temp;
  }

  private static void print(int[] datas){
    StringBuilder sb = new StringBuilder();
    for(int item : datas){
      sb.append(item).append(",");
    }
    System.out.println(sb.toString());
  }

}
