package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.TextUtils;

public class Domain58 extends MinableDomain {

	
	private final static String REGEX_DOMAIN = "^[a-zA-Z]{1,8}\\.58\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	/*
	 * 从百度查号码，一般查到五八两种类型，一种是列表，一种是发布正文 
	 * 列表：http://wh.58.com/jiangjunlu/huitongkd/
	 * 详情：http://wh.58.com/kuaidi/15616250330118x.shtml?PGTID=14067286591780.23529994138516486&ClickID=2
	 * 或者：http://wh.58.com/kuaidi/15616250330118x.shtml
	 */

	private final static String REGEX_LIST = "^http://[a-zA-Z]{1,8}\\.58\\.com/(\\w+/)+$";
	private final static Pattern PATTERN_LIST = Pattern.compile(REGEX_LIST);

	private final static String REGEX_POST = "^(http://[a-zA-Z]{1,8}\\.58\\.com/(\\w+/)+\\d+x\\.shtml)(\\?.*)?$";
	private final static Pattern PATTERN_POST = Pattern.compile(REGEX_POST);


	
	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}
	
	/**
	 * 检查是否此域名对应的是商店列表页面
	 * 
	 * @param item
	 * @return
	 */
	private boolean isMinableList(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_LIST, item.getRealUrl());
	}
	
	private boolean isMinablePost(SearchResultItem item)
	{
		return isMinablePost(item.getRealUrl());
	}
	
	private boolean isMinablePost(String url)
	{
		return TextUtils.isMatchReg(PATTERN_POST,url);
	}
	
	/**
	 * 详情：http://wh.58.com/kuaidi/15616250330118x.shtml?PGTID=14067286591780.23529994138516486&ClickID=2
	 * 去掉某些详情页网址?之后的部分
	 */
	private String getSimpleUrl(String  url)
	{
		if(!isMinablePost(url)) return null;
		
		Matcher matcher = PATTERN_POST.matcher(url);
		while (matcher.find())
			return matcher.group(1);

		return null;
		
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableList(item) || isMinablePost(item);
	}
	
	/**
	 * 从列表页 http://wh.58.com/xxx/xxx/ 获取贴子网址
	 * @param item
	 * @return
	 */
	public String getPostUrl(SearchResultItem item)
	{
		if(!isMinableList(item)) return null;
		
		String html=getHtml(item.getRealUrl());
		if(TextUtils.isEmpty(html)) return null;
		
		final String TAG_BEGIN_TR="<tr logr=";
		final String TAG_END_TR="</tr>";
		List<String> list=TextUtils.getList(html, TAG_BEGIN_TR, TAG_END_TR);
		for(String content:list)
		{
			if(content.contains(item.getPhone()))
			{
				final String TAG_BEGIN_HREF="<a href='";
				final String TAG_END_HREF="'";
				String href=TextUtils.getSubString(content, TAG_BEGIN_HREF, TAG_END_HREF);
				return href;
			}
		}
		return null;
		
	}
	

	@Override
	public PhoneResult mine(SearchResultItem item) {
		
		if(!isMinableUrl(item)) return null;
		
		String url=null;
		if(isMinableList(item))
		{
			url=getPostUrl(item);
		}
		else if(isMinablePost(item))
		{
			url=item.getRealUrl();
		}
		
		if(TextUtils.isEmpty(url)) return null;
		
		url=getSimpleUrl(url);
		
		return getPhoneResult(url, item.getPhone());
	}
	
	
	private PhoneResult getHouseBroker(String url,String phone)
	{
	String html=getHtml(url);
	if(TextUtils.isEmpty(html)) return null;
	
	PhoneResult phoneResult=new PhoneResult();
	//联系人
	String linkman=TextUtils.getSubString(html, "linkman:'", "'");

	phoneResult.setChenghu(TextUtils.appendJob(linkman, "房产经纪"));
	
	List<String> hangeyeList=new ArrayList<String>();
	hangeyeList.add("房屋中介");
	phoneResult.setHangyeList(hangeyeList);
	
	String userID=TextUtils.getSubString(html, "userid:'","'");
	
	if(!TextUtils.isEmpty(userID))
	{
	//获取公司及头像信息
	//类似 http://user.58.com/userdata/?userid=24626622270470&type=10
	//最后的type可以从1到成26,尤其是26
		
	String jsonCompanyURL="http://user.58.com/userdata/?userid="+userID+"&type=10";
	//返回结果示例 ：{ name:'by19960308',authid:6,credit:160,face:'/m1/bigimage/n_s12350652990361713143.jpg',license:0,merchsel:0,corpname:'北京友谊光正房地产经纪有限公司',corpalias:'北京友谊光正房地产经纪有限公司',corpurl:'' }
	String jsonContent=getHtml(jsonCompanyURL);
	if(!TextUtils.isEmpty(jsonContent))
	{
		String face=TextUtils.getSubString(jsonContent, "face:'", "'");
		if(!TextUtils.isEmpty(face))
			phoneResult.setImage("http://pic.58.com"+face);
		
		String companyFullName=TextUtils.getSubString(jsonContent, "corpname:'", "'");
		phoneResult.setJigou(companyFullName);

	}	
	
	}
	if(phoneResult.isFound())
		return phoneResult;
	else return null;
	
	}
	
	
	private PhoneResult getPhoneResult(String url,String phone)
	{
		PhoneResult phoneResult=new PhoneResult();
		String html=getHtml(url);
		if(TextUtils.isEmpty(html)) return null;
		//房产经纪发布的房子信息标记
		final String TAG_HOUSE_BROKER="name:'房产信息";
		if(html.contains(TAG_HOUSE_BROKER))
			return getHouseBroker(url,phone);
		
		
		final String TAG_START_UL="<ul class=\"suUl\">";
		final String TAG_END_URL="</ul>";
		
		String ulHtml=TextUtils.getSubString(html, TAG_START_UL, TAG_END_URL);
		if(TextUtils.isEmpty(ulHtml)) return null;
	
		/*
		final String TAG_START_CHENGHU="<a target=\"_blank\" href=\"http://shop.58.com/";
		final String TAG_END_CHENGHU="/a>";
		String chenghuHtml=TextUtils.getSubString(ulHtml, TAG_START_CHENGHU, TAG_END_CHENGHU);
		if(!TextUtils.isEmpty(chenghuHtml))
		{
			String chenghu=TextUtils.getSubString(chenghuHtml, ">", "<");
			phoneResult.setChenghu(chenghu);
		}
		*/
		String chenghu=TextUtils.getSubString(html, "linkman:'", "'");
		phoneResult.setChenghu(chenghu);
		
		//主行业
		String mainHangye=TextUtils.getSubString(html, "<nobr>类别：</nobr></i>", "</li>");
		if(!TextUtils.isEmpty(mainHangye))
		{
			mainHangye=TextUtils.clearHuanhang(mainHangye);
		}
		
		String subHangye=TextUtils.getSubString(html, "<nobr>小类：</nobr></i>", "</li>");
		if(!TextUtils.isEmpty(subHangye))
		{
			subHangye=TextUtils.clearHuanhang(subHangye);
		}
		
		List<String > hangyeList=new ArrayList<String>();
		if(!TextUtils.isEmpty(mainHangye))
			hangyeList.add(mainHangye);
		if(!TextUtils.isEmpty(subHangye))
			hangyeList.add(subHangye);
		
		phoneResult.setHangyeList(hangyeList);
		
		String sectionHtml=TextUtils.getSubString(html, "<div class=\"userinfo\">", "</div>");
		if(!TextUtils.isEmpty(sectionHtml))
		{
			String addressHtml=TextUtils.getSubString(sectionHtml, "<i>地　　址：</i>", "</li>");
			if(!TextUtils.isEmpty(addressHtml))
			{
				String address=TextUtils.getSubString(addressHtml, "<p>", "</p>");
				phoneResult.setAddress(address);
			}
		}
		
		String company=TextUtils.getSubString(sectionHtml, "<h2>", "</h2>");
		if(!TextUtils.isEmpty(company))
		{
		company=StringUtil.rmHtmlTag(company);
		phoneResult.setJigou(company);
		}
		
		String image=TextUtils.getSubString(html, "img_list.push(\"", "\");");
		phoneResult.setImage(image);
		
		if(phoneResult.isFound())
			return phoneResult;
		else return null;
	}
	
	public static void main(String[] args)
	{
		SearchResultItem item=new SearchResultItem();
		item.setDomain("wh.58.com");
		item.setPhone("18064129619");
		item.setRealUrl("http://wh.58.com/yinshua/9479283284739x.shtml?asdfsd");
		Domain58 domain58=new Domain58();
		System.out.println(domain58.mine(item));
	}

}
