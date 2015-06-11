package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.List;

import com.tianlupan.whoscall.TextUtils;

public class SearchResultItem {
	private String title,content,url,realUrl,domain,date,phone,cacheUrl;
	
	private static final String REGEX_ADDRESS_PREFIX = "((搬迁至|公司地址|地址)\\s{0,3}[:：]?\\s{0,3})?";
	
	private List<String> addressList=new ArrayList<String>();
	
	private String address;
	
	public String getAddress() {
		return address;
	}

	public String getCacheUrl() {
		return cacheUrl;
	}

	public void setCacheUrl(String cacheUrl) {
		this.cacheUrl = cacheUrl;
	}

	@SuppressWarnings("unused")
	private List<String> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<String> addressList) {
		this.addressList = addressList;
		//注意，这里会变更title,content内容，去掉其中的地址信息，避免从地址中解析公司名称等信息
		if(addressList!=null && addressList.size()>0)
		{
			String maxScoreAddress=null;
			int maxLength=-1;
			for(int i=0;i<addressList.size();i++)
			{
				String add=addressList.get(i);
				if(add.length()>maxLength)
				{
					maxLength=add.length();
					maxScoreAddress=add;
				}
			}
			
			address=maxScoreAddress;		
			
		}
	}
	
	public void clearAddressNoise() {
		final String ADDRESS_REPLACE_TO = " ";
		for (String address : addressList) {
			if (!TextUtils.isEmpty(address)) {
					if(!TextUtils.isEmpty(title))
						title = title.replaceAll(REGEX_ADDRESS_PREFIX
								+ address, ADDRESS_REPLACE_TO);
					if(!TextUtils.isEmpty(content))
						content = content.replaceAll(REGEX_ADDRESS_PREFIX
								+ address, ADDRESS_REPLACE_TO);
			}
		}

	}
	


	//如果是百度，标记有没有已经检 查过url
	private boolean realUrlChecked=false;
	//是否已经挖掘过
	private boolean mineDone=false;
	//挖掘结果
	private PhoneResult mineResult=null;

	public boolean isMineDone() {
		return mineDone;
	}

	public PhoneResult getMineResult() {
		return mineResult;
	}
	
	private void decodeBaiduUrl(){
		if(!realUrlChecked)
		{
			realUrl=BaiduLink.getRealURL(url);
			realUrlChecked=true;
			System.out.println("decode baidu url,from:"+url+", to:"+realUrl);
		}
	}
	
	public boolean isMinableDomain(){
		MiningSites sites=new MiningSites();
			if(sites.isMinableDomain(this))
				return true;
		return false;
	}
	
	public void mine(){
		if( isMinableDomain() && !mineDone)
		{
			decodeBaiduUrl();
			MiningSites sites=new MiningSites();
			if(sites.isMinableUrl(this))
			{
				mineResult=sites.mine(this);
			}
			mineDone=true;
		}		
	}


	public boolean isRealUrlChecked() {
		return realUrlChecked;
	}

	public void setRealUrlChecked(boolean realUrlChecked) {
		this.realUrlChecked = realUrlChecked;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRealUrl() {
		return realUrl;
	}

	public void setRealUrl(String realUrl) {
		this.realUrl = realUrl;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	@Override
	public String toString() {
		return  "{ title:\""+title+"\", content:\""+content+"\", url:\""+url+"\", realUrl:\""+realUrl+"\", domain:\""+domain+"\", date:\""+date+"\", phone:\""+phone+"\", addressList:" +addressList+  "\"}";
	}
	
}
