package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.BaiduLink;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import net.sf.json.JSONObject;

public class DomainBaiduMap extends MinableDomain {

	// 有两种，一种是列表，一种是uid

	private final static String REGEX_DOMAIN = "^map.baidu.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	// 指定某个商店
	// uid :
	// http://map.baidu.com/?third_party=seo&poiShareUid=ae376e00348b5d2b01294f5b
	// uid json: http://map.baidu.com/?qt=inf&uid=ae376e00348b5d2b01294f5b
	private final static String REGEX_UID = "^http://map.baidu.com/\\?third_party=seo&poiShareUid=([a-z0-9]{24})$";
	private final static Pattern PATTERN_UID = Pattern.compile(REGEX_UID);
	private final static String URL_PREFIX_UID = "http://map.baidu.com/?qt=inf&uid=";

	// 百度地图搜索关键词列表 http://map.baidu.com/place/15b4d2125555d7c8_218/10/56_5
	//注意用手机浏览器看列表网址和web版不一样
	// 会跳转为下面的网址
	// http://map.baidu.com/?third_party=seo&s=con%26wd%3D%E5%9F%B9%E8%AE%AD%E5%AD%A6%E6%A0%A1%26c%3D218
	// mineList,item url=http://map.baidu.com/place/1cdba8c39fed5190_218/8/27_4
	private final static String REGEX_LIST = "^http://map.baidu.com/place/([a-z0-9_]+/?)+$";
	// private final static String REGEX_LIST=
	// "^http://map.baidu.com/?third_party=seo&s=([\\w%]+)$";
	private final static Pattern PATTERN_LIST = Pattern.compile(REGEX_LIST);

	private final static String REG_LIST_ITEM = "<div style=\"border-top:1px solid #ccc; margin-top:15px; overflow:hidden; padding-top:10px;\">(.|\r|\n)+?</div>[\\s|\r|\n]+</div>";
	private final static Pattern PATTERN_LIST_ITEM = Pattern
			.compile(REG_LIST_ITEM);
	
	//用以去除行业中的错误行业“全是数字”
	private final static Pattern PATTERN_NUMBER=Pattern.compile("^\\d+$");
	
	
	private final static Pattern PATTERN_FIXEDPHONE=Pattern.compile("^(0\\d{2,3})-(\\d{7,8})$");

	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableUID(item) || isMinableList(item);
	}

	private boolean isMinableUID(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_UID, item.getRealUrl());
	}

	private PhoneResult mineUID(SearchResultItem item) {
		if (!isMinableUID(item))
			return null;

		PhoneResult phoneResult = new PhoneResult();

		String uid = TextUtils.getMatchGroup(PATTERN_UID, item.getRealUrl());

		String url = URL_PREFIX_UID + uid;

		String html = getHtml(url);
		if (TextUtils.isEmpty(html))
			return null;

		JSONObject jsonObject = JSONObject.fromObject(html);
		System.out.println(jsonObject);
		if (jsonObject != null && jsonObject.containsKey("content")) {
			JSONObject jsonContent = jsonObject.getJSONObject("content");
			phoneResult.setAddress(jsonContent.getString("addr"));
			phoneResult.setJigou(jsonContent.getString("name"));
		}

		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;

	}

	private boolean isMinableList(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_LIST, item.getRealUrl());
	}
	
	
	private PhoneResult mineListHtml(String url,String phone)
	{
		PhoneResult phoneResult = new PhoneResult();
		
		String html = getHtml(url);
		
		//System.out.println("html="+html);
		
		if(TextUtils.isEmpty(html)) return null;

		if (TextUtils.isEmpty(html))
			return null;

		Matcher matcher = PATTERN_LIST_ITEM.matcher(html);
		while (matcher.find()) {
			String content = matcher.group();
			
			//System.out.println("content="+content);
			
			String shop = TextUtils.getSubString(content, "<h1><em>",
					"</em></h1>");
			String address = TextUtils.getSubString(content, "<div>地址：",
					"</div>");
			
			if(containPhone(content,phone)) {
				phoneResult.setJigou(shop);
				phoneResult.setAddress(address);

				String hangye = TextUtils.getSubString(content,
						"<div><strong>", "</strong></div>");
				if (!TextUtils.isEmpty(hangye)) {
					String[] list = hangye.split(",");

					if (list != null && list.length>0 ) {

						List<String> hangyeList = new ArrayList<String>();
							if(list.length>1)
							{
							// 去除最后的 一个，最后的一个是地名，如：丽人,美容/SPA,古田路
							for (int i = 0; i < list.length - 1; i++) {
								//如果不是纯数字，则添加进去
								if(!TextUtils.isEmpty(list[i]) &&  !TextUtils.isMatchReg(PATTERN_NUMBER, list[i]))
									hangyeList.add(list[i]);
							}
							}
							else if(list.length==1){
								hangyeList.add(list[0]);
							}
							phoneResult.setHangyeList(hangyeList);
						}
				}
				break;
			}

		}

		if(phoneResult.isFound())
			return phoneResult;
		else
			return null;
	}

	private PhoneResult mineList(SearchResultItem item) {

		
		System.out.println("mineList,url="+item.getRealUrl());
		
		if (!isMinableList(item))
			return null;

		PhoneResult phoneResult=null;
		String phone=item.getPhone();
		//先从cache里查，因为有些是老数据，只在cache里能找到
		System.out.println("从百度缓存中查找");
	    if(!TextUtils.isEmpty(item.getCacheUrl()))
	    {
	    	phoneResult=mineListHtml(item.getCacheUrl(), phone);
	    	if(phoneResult!=null) return phoneResult;
	    }
	    System.out.println("从百度地图List中查找");
		//然后从页面里查
	    return mineListHtml(item.getRealUrl(), phone);

	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		
		if(isMinableUID(item))
			return mineUID(item);
		
		if(isMinableList(item))
			return mineList(item);
		
		
		return null;
	}
	

	

	private boolean containPhone(String content,String phone)
	{
		if(content.contains(phone))
			return true;
		// 固话
		if(TextUtils.isMatchReg(PATTERN_FIXEDPHONE, phone))
		{
			Matcher matcher=PATTERN_FIXEDPHONE.matcher(phone);
			while(matcher.find())
			{
				//027
				String zipCode=matcher.group(1);
				//86640000
				String localNumber=matcher.group(2);
				
				String baiduFormat= "("+matcher.group(1)+")"+matcher.group(2);
				if(content.contains(baiduFormat))
					return true;
				
				if(content.contains(zipCode) && content.contains(localNumber))
					return true;

			}

		}
		
		return false;
		
	}
	

	public static void main(String[] args) {
		String baiduLInk = "http://www.baidu.com/link?url=94BBVCsILwFZC8XJbgcQX1TUtSlihCtfJ0VCDVQu6GEpga7frlVLh2rveUk9iMI8v13YTpaJ72m5CGnuPtEmk6_jSrTXT0IQwdvCBm_RZKu";
		SearchResultItem item = new SearchResultItem();
		item.setPhone("15671566966");
		item.setCacheUrl("http://cache.baiducontent.com/c?m=9d78d513d9c046ab5dee937a091dd3620d13c03d338d96533dc3923b8e7904120571e3cc77714b5294d27c1050f21641a8ae6523601e23e6cd98db408cba997c78de20377b1e914666d31aa5d64523dc20954deedb1ee7&p=8d73dd15d9c241ef04be9b7c5f0e&newp=8634de15d9c241e80be296314e5d92695c02dc3351d4d11538&user=baidu&fm=sc&query=15671566966&qid=&p1=2");
		
		item.setRealUrl(BaiduLink.getRealURL(baiduLInk));
		DomainBaiduMap baiduMap = new DomainBaiduMap();
		System.out.println(baiduMap.mine(item));
	}

}
