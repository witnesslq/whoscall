package com.tianlupan.whoscall.mining;


public class SearchResultUserTagItem {
	
	private String tag;
	private int count;
	private SearchResultUserTagType tagType;
	
	public SearchResultUserTagItem(SearchResultUserTagType tagType,String tag,int count)
	{
		this.tagType=tagType;
		this.tag=tag;
		this.count=count;
	}

	public String getTagType() {
		return tagType.toString();
	}

	public int getCount() {
		return count;
	}

	public String getTagName() {
		return tag;
	}
	
	@Override
	public String toString() {
		
		return "{ tag:\""+tag+"\", type:\""+getTagType()+"\", count:"+count+"}";
		
	}
	
	
	
	
}
