package com.pinery.audioedit.bean;

/**
 */

public class AudioMsg {

  public String type;
  public String path;
  public String msg;

  public AudioMsg(String type, String path, String msg){
    this.type = type;
    this.path = path;
    this.msg = msg;
  }

  public AudioMsg(String type, String msg){
    this.type = type;
    this.msg = msg;
  }

}
