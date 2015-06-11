package com.tianlupan.whoscall.db;

import java.util.HashMap;

/**
 * 在内存中缓存,实际可用的保存方式应该是关系数据库,最佳为NOSQL数据库
 */
public class TempSave extends IDatabase {

    private static IDatabase db=new TempSave();

    public static IDatabase getInstance(){
        return db;
    }


    private HashMap<String,String> phoneMap=new HashMap<String, String>();

    @Override
  public String get(String phone) {
        return phoneMap.get(phone);
    }

    @Override
   public  void save(String phone, String json) {
        phoneMap.put(phone,json);
    }
}
