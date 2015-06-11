package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.TextUtils;

public class DomainTuan800 extends MinableDomain {

	private final static String REGEX_DOMAIN = "^(store|www)\\.tuan800\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);
	
	//未过期的团购：http://www.tuan800.com/deal/jinshou199_18504104
	private final static String REGEX_DEAL = "^http://www\\.tuan800\\.com/deal/[a-z0-9]+_\\d+$";
	private final static Pattern PATTERN_DEAL = Pattern.compile(REGEX_DEAL);
	
	 //商店：http://store.tuan800.com/shop_1489703
	private final static String REGEX_STORE = "^(http://store\\.tuan800\\.com/shop_\\d+)(/\\w+)?/?$";
	private final static Pattern PATTERN_STORE = Pattern.compile(REGEX_STORE);
	
	//标题中的括号括起来的店名，如 [东和足道]足疗套餐！专业足道体验，将所有疲惫烦恼一扫光！
	private final static Pattern PATTERN_KUOHAO=Pattern.compile("[\\[【]([\u4E00-\u9FA5\\w]+)[\\]】]");
	
	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}
	
	private boolean isMinableDeal(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_DEAL,item.getRealUrl());
	}
	
	private boolean isMinableStore(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_STORE, item.getRealUrl());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableDeal(item) || isMinableStore(item);
	}
	
	/**
	 * 去掉店名中无用的地方，【14店通用】真功夫 =>真功夫
	 */
	private String formatShopName(String shopName)
	{
		if(TextUtils.isEmpty(shopName))
			return shopName;
	 String newShopName=	shopName.replaceFirst("【\\d+店通用】", "");
	 newShopName=newShopName.replaceFirst("【[\u4E00-\u9FA5]+/[\u4E00-\u9FA5]+】", "");
	 	 return newShopName;
	 
	}
	
	
	private PhoneResult mineDeal(SearchResultItem item)
	{
		if(!isMinableDeal(item)) return null;
		
		String html=getHtml(item.getRealUrl());
		if(TextUtils.isEmpty(html)) return null;
		
		
		PhoneResult phoneResult=new PhoneResult();
		
		//如果已经卖光
		final String SHOP_TAG="商铺信息<em>";
		if(!html.contains(SHOP_TAG))
		{
			System.out.println("团购已经结束了");
			
			String shopHtml=TextUtils.getSubString(html, "<h1>", "</h1>");
			if(!TextUtils.isEmpty(shopHtml))
			{
				String shop=StringUtil.rmHtmlTag(shopHtml);
				shop=formatShopName(shop);
				if(shop.length()<25)
				{
					phoneResult.setJigou(shop);
				}
				else {
					String shopKuohao=getDianMing(shop);
					if(TextUtils.isEmpty(shopKuohao))
						phoneResult.setJigou(shop);
					else {
						phoneResult.setJigou(shopKuohao);
					}

				}
			}
			
			String detailHtml=TextUtils.getSubString(html, "<div class=\"deal_content\">", "<div class=\"fenx\">");
			if(!TextUtils.isEmpty(detailHtml))
			{
				String pic=TextUtils.getSubString(detailHtml, "<img src=\"", "\"");
				phoneResult.setImage(pic);
			}
		}
		else {
			
			String dealInfoHtml=TextUtils.getSubString(html, "id=\"dealinfo\"", "<div class=\"details\">");
			if(!TextUtils.isEmpty(dealInfoHtml))
			{
				String pic=TextUtils.getSubString(dealInfoHtml, "<img src=\"", "\"");
				phoneResult.setImage(pic);
			}
			//找分店
			String shopListHtml=TextUtils.getSubString(html, "<div class=\"mlist\">", "<div id=\"mappage\" class=\"map_page_div\">");
			if(!TextUtils.isEmpty(shopListHtml))
			{
				List<String> shopHtmlList=TextUtils.getList(shopListHtml, "<div class=\"item\"", "</p>");
				for(String shopHtml:shopHtmlList)
				{
					if(shopHtml.contains(item.getPhone()))
					{
						String address=TextUtils.getSubString(shopHtml, "<em class=\"addr\">", "</em>");
					      phoneResult.setAddress(address);
						
					      String shopTitle=TextUtils.getSubString(shopHtml, "<h2>", "</h2>");
					      if(!TextUtils.isEmpty(shopTitle))
					      {
					    	  String shop=TextUtils.getSubString(shopTitle, "\">", "</a>");
					    	  phoneResult.setJigou(shop);
					      }
						
					      break;
					}
				}
			}
			//找行业
			String hangyeHtml=TextUtils.getSubString(html, "<span><a js_trac=new_deal_page_left_tag", "</span>");
			if(!TextUtils.isEmpty(hangyeHtml))
			{
				String hangye=TextUtils.getSubString(hangyeHtml, "\">", "</a>");
				if(!TextUtils.isEmpty(hangye))
				{
					List<String> hangyeList=new ArrayList<String>();
					hangyeList.add(hangye);
					phoneResult.setHangyeList(hangyeList);
				}
			}
			
		}
		
		if(phoneResult.isFound())
			return phoneResult;
		else return null;
		
	}
	
	private PhoneResult mineStore(SearchResultItem item)
	{
		if(!isMinableStore(item)) return null;
		//System.out.println("item,url="+item.getRealUrl());
		String url=TextUtils.getMatchGroup(PATTERN_STORE, item.getRealUrl());
		//System.out.println("item,url2="+url);
		String html=getHtml(url);
		if(TextUtils.isEmpty(html)) return null;
		
		PhoneResult phoneResult=new PhoneResult();
		//System.out.println("html="+html);
		
		String shopHtml=TextUtils.getSubString(html, "<div class=\"open_map\" style=\"display:none\">", "</div>");
		
		if(TextUtils.isEmpty(shopHtml)) return null;
		
		String shop=TextUtils.getSubString(shopHtml, "<h2>", "</h2>");
		System.out.println("shop="+shop);
		phoneResult.setJigou(shop);
		String address=TextUtils.getSubString(shopHtml, "h3>地址：", "</h3>");
		if(!TextUtils.isEmpty(address))
		{
			address=address.trim();
			phoneResult.setAddress(address);
		}
		
		if(phoneResult.isFound())
			return phoneResult;
		else 
			return null;
		
		
	}
	
	public String  getDianMing(String url)
	{
		return TextUtils.getMatchGroup(PATTERN_KUOHAO, url);
	}
	

	@Override
	public PhoneResult mine(SearchResultItem item) {
		if(isMinableDeal(item))
			return mineDeal(item);
		else if(isMinableStore(item))
			return mineStore(item);
		else return null;
	}
	
	public static void main(String[] args)
	{
		SearchResultItem item=new SearchResultItem();
		item.setRealUrl("http://www.tuan800.com/deal/hezudaozul_6462891");
		item.setPhone("13971639103");
		
		DomainTuan800 domain=new DomainTuan800();
		System.out.print(domain.mine(item));
		//System.out.println(domain.formatShopName("【光谷/鲁巷】菲特堡自助经典"));
		
	}

}
