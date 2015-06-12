package com.tianlupan.whoscall.db;


/**
 * 把查到的记录保存到数据库
 */
public interface IDatabase {

	/**
	 * 从数据库中获取json数据
	 * @param phone
	 * @return
	 */
	String get(String phone);

	/**
	 * 保存电话号码对应的信息
	 * @param phone
	 * @param json
	 */
	void save(String phone, String json);
}
