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

public class MergeSort {

  public static void main(String args[]){

    int[] datas = new int[100];
    for(int i=0; i<datas.length; i++){
      datas[i] = new Random().nextInt(200);
    }

    print(datas);

    int[] temp = new int[datas.length];
    mergeSort(datas, 0, datas.length-1, temp);

    print(datas);
  }

  /**
   *
   * @param datas
   * @param low
   * @param high
   */
  private static void mergeSort(int[] datas, int low, int high, int[] temp){
    if(low < high){

      int mid = (low + high) / 2;

      mergeSort(datas, low, mid, temp);
      mergeSort(datas, mid+1, high, temp);

      mergeArray(datas, low, mid, high, temp);

    }
  }

  private static void mergeArray(int[] datas, int low, int mid, int high, int[] temp){
    int i = low;
    int j = mid + 1;
    int k = 0;

    while(i <= mid && j <= high){
      if(datas[i] < datas[j]){
        temp[k++] = datas[i++];
      }else{
        temp[k++] = datas[j++];
      }
    }

    while (i <= mid){
      temp[k++] = datas[i++];
    }

    while (j <= high){
      temp[k++] = datas[j++];
    }

    for(i = 0; i < k; i++){
      datas[low + i] = temp[i];
    }
  }

  private static void print(int[] datas){
    StringBuilder sb = new StringBuilder();
    for(int item : datas){
      sb.append(item).append(",");
    }
    System.out.println(sb.toString());
  }

}
