package com.tianlupan.whoscall.mining;

import com.tianlupan.whoscall.TextUtils;

public enum SearchResultUserTagType {
	
	
	BAIDU("百度手机卫士"),SOGOU("搜狗号码通"),TAG360("360手机助手"),CHUBAO("触宝号码助手");
	private String tag;
	SearchResultUserTagType(String tag)
	{
		this.tag=tag;
	}
	
	public static SearchResultUserTagType get(String tag)
	{
		if(TextUtils.isEmpty(tag)) return BAIDU;
		
		if(tag.contains("百度"))
			return BAIDU;
		else if(tag.contains("触宝"))
			return CHUBAO;
		else if(tag.contains("搜狗"))
			return SOGOU;
		else if(tag.contains("360"))
			return TAG360;
		return BAIDU;
	}
	
		@Override
		public String toString() {
			return tag;
		}

}
