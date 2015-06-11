package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

public class DomainZhaopin extends MinableDomain {

	private final static String REGEX_DOMAIN = "^(company|jobs)\\.zhaopin\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);
	
	
	//http://company.zhaopin.com/CC591972021.htm	
	private final static String REGEX_COMAPONY = "^http://company\\.zhaopin\\.com/(\\w+/)*([^_/]*_)?(\\w+)\\.htm$";
	private final static Pattern PATTERN_COMAPONY = Pattern.compile(REGEX_COMAPONY);
	
	//http://jobs.zhaopin.com/675759720250000.htm
	private final static String REGEX_JOBS = "^(http://jobs\\.zhaopin\\.com/\\d+\\.htm).*$";
	private final static Pattern PATTERN_JOBS = Pattern.compile(REGEX_JOBS);
	

	
	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}
	
	
	private boolean isMinableCompany(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_COMAPONY, item.getRealUrl());
	}
	
	private boolean isMinableJobs(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_JOBS, item.getRealUrl());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableCompany(item) || isMinableJobs(item);
	}
	
	private PhoneResult mineJobs(SearchResultItem item)
	{
		if(!isMinableJobs(item)) return null;
		PhoneResult phoneResult=new PhoneResult();
		String url=TextUtils.getMatchGroup(PATTERN_JOBS, item.getRealUrl());
		String html=getHtml(url);
		
		if(TextUtils.isEmpty(html)) return null;
		//判断是否该信息已经过期
		if(html.contains("class=\"expired-img\"")) return null;
		String companyBox=TextUtils.getSubString(html, "<div class=\"company-box\">", "</div>");
		if(TextUtils.isEmpty(companyBox)){
			String company=TextUtils.getSubString(html, "<h1 class=\"Terminal-title\">", "</h1>");
			phoneResult.setJigou(company);
		}
		else {
			//获取logo
			
			String logoHtml=TextUtils.getSubString(companyBox, " <p class=\"img-border\">", "</p>");
			if(!TextUtils.isEmpty(logoHtml))
			{
				String src=TextUtils.getSubString(logoHtml, "<img src=\"", "\"");
				phoneResult.setImage(src);
			}
			
			//包含companyBox 右侧公司信息
			String companyHtml=TextUtils.getSubString(companyBox, "<p class=\"company-name-t\">", "</p>");
			if(!TextUtils.isEmpty(companyHtml))
			{
				companyHtml=StringUtil.rmHtmlTag(companyHtml);
				phoneResult.setJigou(companyHtml);
				
				String hanyeHtml=TextUtils.getSubString(companyBox, "<li><span>公司行业：</span>", "</li>");
				if(!TextUtils.isEmpty(hanyeHtml))
				{
					List<String> hangyeList=TextUtils.getList(hanyeHtml, "\">", "</a>");
					phoneResult.setHangyeList(hangyeList);
				}
				
				String address=TextUtils.getSubString(companyBox, "<span>公司地址：</span><strong>", "</strong>");
				if(!TextUtils.isEmpty(address))
				{
					address=StringUtil.rmHtmlTag(address);
					address=address.trim();
					phoneResult.setAddress(address);
				}
			}
		}
		
		

			String contact=TextUtils.getContact(html);
			if(!TextUtils.isEmpty(contact))
			{
				contact+=" (HR)";
				phoneResult.setChenghu(contact);
			}
	

		
		if(phoneResult.isFound())
			return phoneResult;
		else return null;
	}
	
	
	private PhoneResult mineCompany(SearchResultItem item)
	{
		if(!isMinableCompany(item)) return null;
		String shop=TextUtils.getMatchGroup(PATTERN_COMAPONY, item.getRealUrl(),3);

		String url="http://company.zhaopin.com/"+shop+".htm";
		System.out.println("url="+url);
		String html=getHtml(url);
		if(TextUtils.isEmpty(html)) return null;
		PhoneResult phoneResult=new PhoneResult();
		
		String company=TextUtils.getSubString(html, "<h1 class=\"Terminal-title\">", "</h1>");
		phoneResult.setJigou(company);
		
		String companyLogoHtml=TextUtils.getSubString(html, "<dd class=\"company-logo\">", "</dd>");
		if(!TextUtils.isEmpty(companyLogoHtml))
		{
			String src=TextUtils.getSubString(companyLogoHtml, "<img src=\"", "\"");
			if(!TextUtils.isEmpty(src))
				phoneResult.setImage(src);
		}
		
		
		String detailHtml=TextUtils.getSubString(html, "<dt>公司行业：</dt>", "<dd class=\"clearfix\">");
		System.out.println("detailHtml="+detailHtml);
		if(!TextUtils.isEmpty(detailHtml))
		{
			String hangyeHtml=TextUtils.getSubString(detailHtml, "<dd>", "</dd>");
			System.out.println("hangyeHtml="+hangyeHtml);
			if(!TextUtils.isEmpty(hangyeHtml))
			{
				hangyeHtml=StringUtil.rmHtmlTag(hangyeHtml);
				List<String> hangyeList=new ArrayList<String>();
				if(hangyeHtml.contains(" "))
				{
					String[] list=hangyeHtml.split(" ");
					for(int i=0;i<list.length;i++)
						hangyeList.add(list[i]);
				}
				else {
					hangyeList.add(hangyeHtml);					
				}
				
				phoneResult.setHangyeList(hangyeList);
				System.out.println("hanyeHtml="+hangyeHtml);
				
			}
		}
		
		phoneResult.setChenghu(TextUtils.getContact(html));
		String address=TextUtils.getAddress(html);
		System.out.println("address="+address);
		phoneResult.setAddress(address);
		
		if(phoneResult.isFound())
			return phoneResult;
		else return null;
		
	}
	
	@Override
	public PhoneResult mine(SearchResultItem item) {
	 if(isMinableJobs(item))
		 return mineJobs(item);
	 
		if(isMinableCompany(item))
			return mineCompany(item);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		System.out.println("address="+TextUtils.getContact("\">联系人：沈小姐</p>"));
	/*	
	SearchResultItem item=new SearchResultItem();
	item.setDomain("jobs.zhaopin.com");
		item.setRealUrl("http://jobs.zhaopin.com/191015013250010.htm?ssidkey=y&ss=201&ff=03");
		DomainZhaopin domainZhaopin=new DomainZhaopin();
		System.out.println(domainZhaopin.isMinableDomain(item));*/
	}

}
