package com.tianlupan.whoscall.mining.domains;

import java.util.regex.Pattern;

import org.ansj.util.MyStaticValue;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.io.IOUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;

public class DomainSouFun extends MinableDomain {
	
	private final static String REGEX_DOMAIN = "^([a-z]{1,8}\\.){1,2}soufun\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);
	
	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableDomain(item);
	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		
		String redirectURL=IOUtils.getRedirectLocation(item.getRealUrl());
		MyStaticValue.Log4j.info(item.getRealUrl()+"==>"+redirectURL);
		if(!TextUtils.isEmpty(redirectURL) && redirectURL.contains("fang.com"))
		{
			item.setRealUrl(redirectURL);
			
			return new DomainFang().mine(item);
			
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args){
		SearchResultItem item=new SearchResultItem();
		item.setDomain("esf.wuhan.soufun.com");
		
		DomainSouFun domainSouFun=new DomainSouFun();
		System.out.println(domainSouFun.isMinableDomain(item));
		
	}

}
