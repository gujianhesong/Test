package com.pinery.audioedit.util;

import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 */
public class CommonFunction {
  public static String FormatRecordTime(int recordTime) {
    int minute = recordTime / 60;
    int second = recordTime % 60;

    String formatRecordTime = "";

    if (minute != 0) {
      formatRecordTime += String.valueOf(minute) + "′";
    }

    formatRecordTime += String.valueOf(second) + "″";

    return formatRecordTime;
  }

  public static String GetDate() {
    long time = System.currentTimeMillis();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    String date = simpleDateFormat.format(time);
    return date;
  }

  public static String GetDate(long time) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    String date = simpleDateFormat.format(time);
    return date;
  }

  public static boolean notEmpty(CharSequence text) {
    return !isEmpty(text);
  }

  public static boolean isEmpty(CharSequence text) {
    if (text == null || text.length() == 0) {
      return true;
    }

    return false;
  }

  public static byte[] GetBytes(short shortValue, boolean bigEnding) {
    byte[] byteArray = new byte[2];

    if (bigEnding) {
      byteArray[1] = (byte) (shortValue & 0x00ff);
      shortValue >>= 8;
      byteArray[0] = (byte) (shortValue & 0x00ff);
    } else {
      byteArray[0] = (byte) (shortValue & 0x00ff);
      shortValue >>= 8;
      byteArray[1] = (byte) (shortValue & 0x00ff);
    }

    return byteArray;
  }

  public static short[] GetShorts(int value, boolean bigEnding) {
    short[] byteArray = new short[2];

    if (bigEnding) {
      byteArray[1] = (byte) (value & 0x00ffff);
      value >>= 8 * 2;
      byteArray[0] = (byte) (value & 0x00ffff);
    } else {
      byteArray[0] = (byte) (value & 0x00ffff);
      value >>= 8 * 2;
      byteArray[1] = (byte) (value & 0x00ffff);
    }

    return byteArray;
  }

  public static short GetShort(byte firstByte, byte secondByte, boolean bigEnding) {
    short shortValue = 0;

    if (bigEnding) {
      shortValue |= (firstByte & 0x00ff);
      shortValue <<= 8;
      shortValue |= (secondByte & 0x00ff);
    } else {
      shortValue |= (secondByte & 0x00ff);
      shortValue <<= 8;
      shortValue |= (firstByte & 0x00ff);
    }

    return shortValue;
  }

  public static short GetInt(byte firstByte, byte secondByte, byte thirdByte, byte fourthByte,
      boolean bigEnding) {
    short shortValue = 0;

    if (bigEnding) {
      shortValue |= (firstByte <<= 8 * 3);
      shortValue |= (secondByte <<= 8 * 2);
      shortValue |= (thirdByte <<= 8 * 1);
      shortValue |= (fourthByte <<= 8 * 0);
    } else {
      shortValue |= (firstByte <<= 8 * 0);
      shortValue |= (secondByte <<= 8 * 1);
      shortValue |= (thirdByte <<= 8 * 2);
      shortValue |= (fourthByte <<= 8 * 3);
    }

    return shortValue;
  }

  public static byte[] AverageShortByteArray(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte, boolean bigEnding) {
    short firstShort = CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return CommonFunction.GetBytes((short) (firstShort / 2 + secondShort / 2), bigEnding);
  }

  public static short AverageShort(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte, boolean bigEnding) {
    short firstShort = CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return (short) (firstShort / 2 + secondShort / 2);
  }

  public static short WeightShort(byte firstShortHighByte, byte firstShortLowByte,
      byte secondShortHighByte, byte secondShortLowByte, float firstWeight, float secondWeight,
      boolean bigEnding) {
    short firstShort = CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
    short secondShort = CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
    return (short) (firstShort * firstWeight + secondShort * secondWeight);
  }

  public static boolean IsInMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static byte[] GetByteBuffer(short[] shortArray, boolean bigEnding) {
    return GetByteBuffer(shortArray, shortArray.length, bigEnding);
  }

  public static byte[] GetByteBuffer(short[] shortArray, int shortArrayLength, boolean bigEnding) {
    int actualShortArrayLength = shortArray.length;

    if (shortArrayLength > actualShortArrayLength) {
      shortArrayLength = actualShortArrayLength;
    }

    short shortValue;
    byte[] byteArray = new byte[2 * shortArrayLength];

    for (int i = 0; i < shortArrayLength; i++) {
      shortValue = shortArray[i];

      if (bigEnding) {
        byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
        shortValue >>= 8;
        byteArray[i * 2] = (byte) (shortValue & 0x00ff);
      } else {
        byteArray[i * 2] = (byte) (shortValue & 0x00ff);
        shortValue >>= 8;
        byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
      }
    }

    return byteArray;
  }
}
