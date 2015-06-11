package com.tianlupan.whoscall.mining.domains;

import com.tianlupan.whoscall.io.HttpDownload;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;

public abstract class MinableDomain {
	
	/**
	 * 通过域名、网址正则表达式，标记是否可挖掘
	 */
	public abstract boolean isMinableDomain(SearchResultItem item);
	
	public abstract boolean isMinableUrl(SearchResultItem item);
	
	/**
	 * 开挖
	 */
	public abstract PhoneResult mine(SearchResultItem item);
	
	
	protected String getHtml(SearchResultItem item)
	{
		return new HttpDownload().getHtml(item.getRealUrl());
	}
	
	protected String getHtml(String url)
	{
		return new HttpDownload().getHtml(url);
	}
	
	
}
