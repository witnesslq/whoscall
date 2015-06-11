package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;

/**
 * 搜房网挖掘机 sofun ,fang.com
 * 
 * @author laotian
 * 
 */
public class DomainFang extends MinableDomain {

	private final static String REGEX_DOMAIN = "^([a-z]{1,8}\\.){1,2}fang\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);
	// 房产经纪主页：http://esf.gz.fang.com/a/luo12363
	private final static Pattern PATTERN_AGENT = Pattern
			.compile("^http://([a-z]{1,8}\\.){1,2}fang\\.com/a/.+$");
	// 房产经纪二手房等二级页面：
	// http://esf.wuhan.fang.com/agent/Agentnew/AloneesfHList.aspx?&agentid=162969135&housetype=second
	private final static Pattern PATTERN_AGENT_SUBPAGE = Pattern
			.compile("^http://([a-z]{1,8}\\.){1,2}fang\\.com/agent/.+$");
	
	private final static Pattern PATTERN_FANG=Pattern.compile("^http://([a-z]{1,8}\\.){1,2}fang\\.com/(chushou|chuzu)/[\\w-]+\\.(htm|html)$");

	private final static Pattern PATTERN_AGENT_LINK=Pattern.compile("href=\"(http://([a-z]{1,8}\\.){1,2}fang\\.com/a/\\w+)\"");
	//经纪列表： http://esf.wuhan.fang.com/agenthome/-i33-j310/
	private final static Pattern PATTERN_AGENT_LIST=Pattern.compile("^(http://([a-z]{1,8}\\.){1,2}fang\\.com)/agenthome/.*$");
	
	


	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}

	private PhoneResult mineAgent(String  url) {
		if (!isMinableAgent(url))
			return null;

		PhoneResult phoneResult = new PhoneResult();

		String html = getHtml(url);
		if (TextUtils.isEmpty(html))
			return null;

		String floatHtml = TextUtils.getSubString(html,
				"<div class=\"Floating\">", "</div>");
		if (!TextUtils.isEmpty(floatHtml)) {
			String TAG_COM = "<dd style=\"padding-top:2px\\9;*padding-top:3px;\" >";
			String company = TextUtils
					.getSubString(floatHtml, TAG_COM, "</dd>");
			phoneResult.setJigou(company);

			String name = TextUtils.getSubString(floatHtml, "<dd>", "&nbsp;");
			if(!TextUtils.isEmpty(name))
				phoneResult.setChenghu(TextUtils.appendJob(name, "房产经纪"));

			String rz = TextUtils.getSubString(html, "<div class=\"rzren\">",
					"</div>");
			if (!TextUtils.isEmpty(rz)) {
				String avartar = TextUtils
						.getSubString(rz, "<img src=\"", "\"");
				phoneResult.setImage(avartar);
			}

			String address = TextUtils.getSubString(html,
					"<li>地<span class=\"pl24\">址</span>：", "</li>");
			phoneResult.setAddress(address);
			
			List<String> hangyeList=new ArrayList<String>();
			hangyeList.add("房屋中介");
			phoneResult.setHangyeList(hangyeList);

		}
		// 如果没有人头像，就试着用公司logo
		if (TextUtils.isEmpty(phoneResult.getImage())) {
			String componyLogoHtml = TextUtils.getSubString(html,
					"<li class=\"companylogo\">", "</li>");
			String componyLogo = TextUtils.getSubString(componyLogoHtml,
					"<img src=\"", "\"");
			phoneResult.setImage(componyLogo);
		}

		// 如果公司头像也没有，可能网页是另外一种格式的，如：
		if (TextUtils.isEmpty(phoneResult.getImage())) {
			String photoHtml = TextUtils.getSubString(html,
					"<div class=\"photo\">", "</div>");
			if (!TextUtils.isEmpty(photoHtml)) {
				String avartar = TextUtils.getSubString(photoHtml,
						"<img src=\"", "\"");
				phoneResult.setImage(avartar);
			}

		}

		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;
	}

	private boolean isMinableAgent(SearchResultItem item) {
		return isMinableAgent(item.getRealUrl());
	}
	
	private boolean isMinableAgentList(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_AGENT_LIST, item.getRealUrl());
	}
	
	private PhoneResult mineAgentList(SearchResultItem item)
	{
		if(!isMinableAgentList(item))
			return null;
		
		String html=getHtml(item.getRealUrl());
		if(TextUtils.isEmpty(html) || !html.contains(item.getPhone())) return null;
		List<String> list=TextUtils.getList(html, "<div id='list_", "<div class=\"clear\">");
		if(list !=null && list.size()>0)
		{
			for(String l:list)
			{
				if(l.contains(item.getPhone()))
				{
					String agent_href=TextUtils.getSubString(l, "<a href='", "'");
					if(!TextUtils.isEmpty(agent_href) && agent_href.startsWith("/"))
					{
						String domain=TextUtils.getMatchGroup(PATTERN_AGENT_LIST,item.getRealUrl());
						if(!TextUtils.isEmpty(domain))
						{
							String aLink=domain+agent_href;
							return mineAgent(aLink);
						}
					}

					
					break;
				}
			}
		}
		
		return null;
	}
	
	private boolean isMinableAgent(String url)
	{
			return TextUtils.isMatchReg(PATTERN_AGENT, url)
					|| TextUtils.isMatchReg(PATTERN_AGENT_SUBPAGE,
							url);
	}
	
	private PhoneResult mineFang(SearchResultItem item)
	{
		String html=getHtml(item);
		if(TextUtils.isEmpty(html))
			return null;
		String link=TextUtils.getMatchGroup(PATTERN_AGENT_LINK, html);
		System.out.println("link="+link);
		
		if(!TextUtils.isEmpty(link))
		{
			return mineAgent(link);
		}

		if(html.contains("100%个人房源"))
		{
			PhoneResult phoneResult=new PhoneResult();
			
			String chenghu=TextUtils.getSubString(html, "<span class=\"name floatl\" id=\"Span2\">", "</span>");
			if(!TextUtils.isEmpty(chenghu))
			{
				chenghu=chenghu.trim();
				chenghu=TextUtils.clearHuanhang(chenghu);
				if(!TextUtils.isEmpty(chenghu))
					phoneResult.setChenghu(TextUtils.appendJob(chenghu, "房东"));
				
				List<String> hangyeList=new ArrayList<String>();
				hangyeList.add("房屋出租或出售");
				phoneResult.setHangyeList(hangyeList);
				
			}
			
			//房产图片
			String imageHtml=TextUtils.getSubString(html, "<div class=\"slider\" id=\"thumbnail\">", "</div>");
			if (!TextUtils.isEmpty(imageHtml)) {
				String avartar = TextUtils.getSubString(imageHtml,
						"<img src=\"", "\"");
				phoneResult.setImage(avartar);
			}
			

			
			if(phoneResult.isFound())
				return phoneResult;

		}
		return null;

				
	}
	
	private boolean isMinableFang(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_FANG, item.getRealUrl());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableFang(item) || isMinableAgent(item) || isMinableAgentList(item);
	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		String url=item.getRealUrl();
		if(isMinableAgent(item))
		{
			return mineAgent(url);
		}
		else if(isMinableFang(item))
		{
			return mineFang(item);
		}
		else if(isMinableAgentList(item))
		{
			return mineAgentList(item);
		}
		return null;
	}

	public static void main(String[] args) {
		SearchResultItem item = new SearchResultItem();
		item.setRealUrl("http://esf.wuhan.fang.com/a/long878158?s=BingXML");
		item.setPhone("18707151870");
		item.setDomain("esf.wuhan.fang.com");

		DomainFang domainFang = new DomainFang();

		System.out.println(domainFang.mine(item));

	}

}
