package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.TextUtils;

public abstract class SearchEngine {

	protected List<SearchResultItem> items = new ArrayList<SearchResultItem>();

	protected List<SearchResultUserTagItem> tags = new ArrayList<SearchResultUserTagItem>();

	protected String phone;

	private static final Pattern PATTERN_DOMAIN = Pattern
			.compile("^([\\w-]+(\\.[\\w-]+)+).*$");

	
	private final static List<String> badDomainList=new ArrayList<String>();	
	private final static List<String> badTitleList=new ArrayList<String>();	
	
	static{
		badDomainList.add("sohao.org");
		badDomainList.add("yenwoop.com");
		badDomainList.add("ekaing.com");
		badDomainList.add("beyaiw.com");
		badDomainList.add("heagei.com");
		badDomainList.add("jihaoba.com");
		badDomainList.add("xysjk.com");
		badDomainList.add("chahaoba.com");
		badDomainList.add("jx.ip138.com");
			
		badTitleList.add("所有号码");
		badTitleList.add("固定电话号码");
		badTitleList.add("xxxx");
		badTitleList.add("查询号码");
		badTitleList.add("手机号码");
		badTitleList.add("号段");
		badTitleList.add("哪里");
		badTitleList.add("什么手机号码");
		badTitleList.add("查询电话号码");
		badTitleList.add("查询号码");
		badTitleList.add("什么号码");
		badTitleList.add("哪里的");
		badTitleList.add("手机号码列表");
		badTitleList.add("号段手机号码");
		badTitleList.add("归属地");
		badTitleList.add("归属");
		badTitleList.add("吉凶");
		badTitleList.add("吉详号");
		badTitleList.add("选号");
	}
	
	/**
	 * 检查搜索结果项是否有效
	 */
	private static  boolean checkValid(String title,String content,String domain)
	{
		//先检查域名
		if(!TextUtils.isEmpty(domain))
		{
			String lowDomainString=domain.toLowerCase();
			for(String badDomain:badDomainList)
			{
				if(lowDomainString.contains(badDomain))
					return false;
			}
		}
		
		if(!TextUtils.isEmpty(title))
		{
			for(String badTitle:badTitleList)
			{
				if(title.contains(badTitle))
					return false;
			}
		}
		
		if(TextUtils.isEmpty(content)) return false;
		else {
			//如果全是数字则认为无效
			//比如010-85560024 010-85560025 010-85560026 (010)-85560027 010-85560028 010-85560029 ...
			String	newContent=content.replaceAll("[\\s*|\\.|\\-|\\(|\\)|\\d*]", "");
			if(TextUtils.isEmpty(newContent) || newContent.length()<10)
					return false;
		}
		
		return true;
	}

	public abstract SearchEngineTypes getType();

	protected abstract void search(String phone);

	public void doSearch(String phone) {
		this.phone = phone;
		search(phone);
	}

	public List<SearchResultUserTagItem> getTags() {
		return tags;
	}

	public List<SearchResultItem> getItems() {
		return items;
	}

	protected void AddItem(String title, String content, String url,
			String domain, String date, String cacheUrl) {
		if (checkValid(title, content, url)) {
			SearchResultItem item = new SearchResultItem();
			item.setTitle(StringUtil.rmHtmlTag(title));
			item.setContent(StringUtil.rmHtmlTag(content));
			item.setUrl(url);
			item.setDate(date);
			item.setRealUrlChecked(false);
			item.setRealUrl(null);
			item.setDomain(domain);
			item.setPhone(phone);
			item.setCacheUrl(cacheUrl);
			
			//List<String> addressList=getAddressList(title, content);
/*			removeAddress(title, addressList);
			removeAddress(content, addressList);*/
			item.setAddressList( AddressParser.getAddressList(title, content));
			//  清除title,content,删除里面的地址信息，防止从中解析公司名称、人名等信息
			item.clearAddressNoise();
			items.add(item);
		}
		else {
			System.err.println("去除无效的搜索结果项, title="+title+", content="+content+", domain="+domain);
		}
	}



	protected void AddTag(SearchResultUserTagType type, String tag, int count) {
		tags.add(new SearchResultUserTagItem(type, tag, count));
	}

	protected String getDomain(String url) {
		return TextUtils.getMatchGroup(PATTERN_DOMAIN, url);
	}

	private String getStringTo(String url, String endTag) {
		if (TextUtils.isEmpty(url))
			return null;
		if (url.contains(endTag)) {
			int separatorIndex = url.indexOf(endTag);

			return url.substring(0, separatorIndex);
		}

		return url;
	}

	// 从原始的url中得到真正在 &nbsp;
	protected String getURL(String url) {
		final String TAG_SPACE = "&nbsp;";
		return getStringTo(url, TAG_SPACE);
	}

	@Override
	public String toString() {
		return "{ phone:\"" + phone + "\", items:" + getItems() + ", tags:\""
				+ getTags() + "\"}";
	}



}
