package com.test.sort;

import java.util.Random;
import java.util.Stack;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/3/7
 * @desc
 * @version: 3.1.2
 */

public class QuickSort {

  public static void main(String args[]){

    int[] datas = new int[100];
    for(int i=0; i<datas.length; i++){
      datas[i] = new Random().nextInt(200);
    }

    print(datas);

    //quickSort(datas, 0, datas.length-1);
    //quickSort2(datas, 0, datas.length-1);
    quickSort3(datas, 0, datas.length-1);

    print(datas);
  }

  /**
   * 容易搞迷糊left, right, low, high
   * @param datas
   * @param low
   * @param high
   */
  private static void quickSort(int[] datas, int low, int high){
    if(low < high){

      int left = low;
      int right = high;
      int base = datas[left];

      while(left < right){
        while(left < right && datas[right] >= base){
          right--;
        }
        datas[left] = datas[right];

        while (left < right && datas[left] <= base){
          left++;
        }
        datas[right] = datas[left];
      }
      datas[left] = base;

      quickSort(datas, low, left-1);
      //特别注意，是high，不是right
      quickSort(datas, left+1, high);

    }
  }

  private static void quickSort2(int[] datas, int low, int high){
    if(low < high){

      int pivot = partition(datas, low, high);

      quickSort2(datas, low, pivot-1);
      quickSort2(datas, pivot+1, high);

    }
  }

  private static void quickSort3(int[] datas, int low, int high){
    //栈
    Stack<Integer> stack = new Stack<>();
    if(low < high){
      stack.push(low);
      stack.push(high);

      while(!stack.empty()){
        //这里特别注意弹出顺序，先high，在low
        high = stack.pop();
        low = stack.pop();

        int pivot = partition(datas, low, high);

        if(low < pivot-1){
          stack.push(low);
          stack.push(pivot-1);
        }
        if(pivot+1 < high){
          stack.push(pivot+1);
          stack.push(high);
        }

      }
    }

  }

  private static int partition(int[] datas, int left, int right){
    int base = datas[left];

    while(left < right){
      while(left < right && datas[right] >= base){
        right--;
      }
      datas[left] = datas[right];

      while (left < right && datas[left] <= base){
        left++;
      }
      datas[right] = datas[left];
    }
    datas[left] = base;

    return left;
  }

  private static void print(int[] datas){
    StringBuilder sb = new StringBuilder();
    for(int item : datas){
      sb.append(item).append(",");
    }
    System.out.println(sb.toString());
  }

}
