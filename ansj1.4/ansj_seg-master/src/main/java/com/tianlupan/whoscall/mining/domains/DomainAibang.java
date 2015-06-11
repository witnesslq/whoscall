package com.tianlupan.whoscall.mining.domains;

import java.util.List;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

public class DomainAibang extends MinableDomain {

	private final static String REGEX_DOMAIN = "^www\\.aibang\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);
	
	private final static String REGEX_DETAIL="^(http://www.aibang.com/detail/\\d+-\\d+)(/(\\w+/)+)?$";
	private  final static Pattern PATTERN_DETAIL=Pattern.compile(REGEX_DETAIL);
	
	//列表 http://www.aibang.com/puyang/xizangcai/
	private final static String REGEX_LIST="^http://www.aibang.com/(\\w+/)+$";
	private  final static Pattern PATTERN_LIST=Pattern.compile(REGEX_LIST);
	
	//现在没有抓取连锁店页面，把这个也处理下 TODO
	//http://www.aibang.com/wuhan/liansuo-fayuandifaxingsheji/
	
	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}
	
	private boolean isMinableDetail(SearchResultItem item)
	{
		return isMinableDetail(item.getRealUrl());
	}
	
	private boolean isMinableDetail(String url)
	{
		return TextUtils.isMatchReg(PATTERN_DETAIL, url);
	}
	
	private boolean isMinableList(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_LIST, item.getRealUrl());
	}
	

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableList(item) || isMinableDetail(item);
	}
	
	private String getDetailUrlFromList(SearchResultItem item)
	{
		if(!isMinableList(item)) return null;
		String listURL=item.getRealUrl();
		String html=getHtml(listURL);
		if(TextUtils.isEmpty(html)) return null;
		
		final String TAG_START_LIST="<div class=\"aside\">";
		final String TAG_END_LIST="<div class=\"clear\"></div>";
		
		List<String> listItem=TextUtils.getList(html, TAG_START_LIST, TAG_END_LIST);
		
		for(String content:listItem)
		{
			if(content.contains(item.getPhone()))
			{
				String href=TextUtils.getSubString(content, "<a href=\"", "\"");
				if(!TextUtils.isEmpty(href))
					return href;
			}
		}
		
		return null;
		
	}
	
	private PhoneResult mineDetail(String url,String phone)
	{
		if(TextUtils.isEmpty(url)) return null;
		if(!isMinableDetail(url)) return null;
		
		String realURL=TextUtils.getMatchGroup(PATTERN_DETAIL, url);
		if(TextUtils.isEmpty(realURL)) return null;
		
		String html=getHtml(realURL);
		
		PhoneResult phoneResult=new PhoneResult();
		String shop=TextUtils.getSubString(html, "<h1>", "</h1>");
		phoneResult.setJigou(shop);
		
		//酒店的网页用的这种模板，例 ：http://www.aibang.com/detail/828018686-695423224
		String detailHtml=TextUtils.getSubString(html, "<dl class=\"detail_list\">", "</dl>");
		if(!TextUtils.isEmpty(detailHtml))
		{
			String addressHtml=TextUtils.getSubString(detailHtml, "址：</dt>", "<span");
			if(!TextUtils.isEmpty(addressHtml))
			{
				String address=StringUtil.rmHtmlTag(addressHtml);
				address=TextUtils.clearHuanhang(address);
				phoneResult.setAddress(address);
			}
			
			String tags=TextUtils.getSubString(detailHtml, "签：</dt>", "</dd>");
			if(!TextUtils.isEmpty(tags))
			{
				List<String> hangye=TextUtils.getList(tags, "\">", "</a>");
				phoneResult.setHangyeList(hangye);		
			}
		}
		else{
			//其它类型的，这种常见 http://www.aibang.com/detail/2032460463-420173991
			detailHtml=TextUtils.getSubString(html, "<ul class=\"details_content\">", "</ul>");
			if(!TextUtils.isEmpty(detailHtml))
			{
				String addressHtml=TextUtils.getSubString(detailHtml, "地址：</div>", "<span");
				if(!TextUtils.isEmpty(addressHtml))
				{
					String address=StringUtil.rmHtmlTag(addressHtml);
					address=TextUtils.clearHuanhang(address);
					phoneResult.setAddress(address);
				}
				
				String tags=TextUtils.getSubString(detailHtml, "标签：</div>", "</div>");
				if(!TextUtils.isEmpty(tags))
				{
					tags=tags.replace("<div class=\"details_r details_r_w2\">", "");
					List<String> hangye=TextUtils.getList(tags, "\">", "</a>");
					phoneResult.setHangyeList(hangye);		
				}
			}
		}
		
		
		//缩略图
		String imageHtml=TextUtils.getSubString(html, "<a id=\"imgBox\"", "</a>");
		if(!TextUtils.isEmpty(imageHtml))
		{
			String image=TextUtils.getSubString(imageHtml, "url(", ")");
			phoneResult.setImage(image);
		}
		
		
		if(phoneResult.isFound())
			return phoneResult;
		else return null;
		
	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		if(!isMinableUrl(item)) return null;
		String url=null;
		if(isMinableList(item))
		{
			url=getDetailUrlFromList(item);
		}
		else if(isMinableDetail(item))
		{
			url=item.getRealUrl();
		}
		if(TextUtils.isEmpty(url)) return null;
		return mineDetail(url, item.getPhone());
	}
	
	public static void main(String[] args)
	{
		SearchResultItem item=new SearchResultItem();
		item.setRealUrl("http://www.aibang.com/detail/2032460463-420173991");
		item.setPhone("0393-8705618");
		DomainAibang domainAibang=new DomainAibang();
		System.out.println(domainAibang.mine(item));
		
	}

}
