package com.tianlupan.whoscall.db;

import com.tianlupan.whoscall.TextUtils;

/**
 * 把查到的记录保存到数据库
 */
public abstract class IDatabase {
	protected String formatPhone(String phone)
	{
			if (TextUtils.isEmpty(phone))
				return null;
			String number = phone.replace("-", "");
			return number;
	}

	/**
	 * 从数据库中获取json数据
	 * @param phone
	 * @return
	 */
	public abstract String get(String phone);

	/**
	 * 保存电话号码对应的信息
	 * @param phone
	 * @param json
	 */
	public abstract void save(String phone, String json);
}
