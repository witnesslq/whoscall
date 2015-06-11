package com.tianlupan.whoscall.io;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.model.NTRecord;
import com.tianlupan.whoscall.model.NTRecordSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public abstract class ISearchEngine {
	public abstract JSONObject fectchNumber(String number);
	
	public void AddToRecords(JSONArray jsonArray,String title,String content,String url)
	{
		if(NTRecord.checkValid(title, content, url))
		{
			JSONObject record=new JSONObject();
			record.put(NTRecord.JSON_RECORD_TITLE, removeTag(title));
			record.put(NTRecord.JSON_RECORD_CONTENT, removeTag(content));
			record.put(NTRecord.JSON_RECORD_DOMAIN, getDomain(removeTag(url)));
			
			jsonArray.add(record);
		}
		
	}
	
	public void AddToGuanjia(JSONArray jsonArray,String biaoji,String type,int biaojiCount)
	{
		
		JSONObject record=new JSONObject();
		record.put(NTRecordSet.JSON_GUANJIA_BIAOJI, biaoji);
		record.put(NTRecordSet.JSON_GUANJIA_BIAOJI_COUNT, biaojiCount);
		record.put(NTRecordSet.JSON_GUANJIA_TYPE,type);
		
		jsonArray.add(record);
		
	}

	
	//TODO 这是搜索引擎已经给标记出来的命中结果，将来需要分析这个
	protected String removeTag(String content)
	{
		if(TextUtils.isEmpty(content)) return content;
		String newContent=content.replace("<em>", "");
		newContent=content.replace("</em>", "");
		newContent=content.replaceAll("（", "(");
		newContent=content.replaceAll("）", ")");
		return newContent;		
	}
	
	public String getDomain(String url)
	{
		if(TextUtils.isEmpty(url)) return null;
		if(url.contains("/"))
		{
			int separatorIndex= url.indexOf("/");
			
			return url.substring(0, separatorIndex);
		}
		
		return url;
		
	}
	
	
}
