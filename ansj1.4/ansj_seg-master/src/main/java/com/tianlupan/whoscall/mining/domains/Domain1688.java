package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;

import org.nlpcn.commons.lang.util.StringUtil;

public class Domain1688 extends MinableDomain {

	//private final static String REGEX_DOMAIN = "^[a-z0-9]{4,}\\.1688\\.com$";
	private final static String REGEX_DOMAIN = "^[a-z0-9]{4,}\\.1688(\\.)?c?o?m?$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	// http://detail.1688.com/offer/1198564773.html
	private final static String REGEX_DETAIL_PAGE = "^http://detail\\.1688\\.com/offer/\\d+\\.html$";
	private final static Pattern PATTERN_DETAIL_PAGE = Pattern
			.compile(REGEX_DETAIL_PAGE);

	// 从detail.1688.com 商品页中根据 “首页” 找到 商店名
	private final static String REGEX_SHOP_NAME = "<a href=\"(http://[a-z0-9]{4,}\\.1688\\.com)\">首页</a>";
	private final static Pattern PATTERN_SHOP_NAME = Pattern
			.compile(REGEX_SHOP_NAME);

	// http://wwm16888.1688.com/page/creditdetail.htm
	private final static String REGEX_URL = "^http://([a-z0-9]{4,})\\.1688\\.com(/page/\\.+\\.html)?$";
	private final static Pattern PATTERN_URL = Pattern.compile(REGEX_URL);
	
	//http://www.1688.com/company/tianlupan.html
	private final static String REGEX_FREE_COMPANY="^http://www\\.1688\\.com/company/([a-z]+/)*([a-z0-9]{4,})\\.html$";
	private final static Pattern PATTERN_FREE_COMPANY=Pattern.compile(REGEX_FREE_COMPANY);
	
	
	private static final String[] NOT_VALID_SHOP_DOMAINS = { "detail.1688.com",
			"trade.1688.com", "login.1688.com", "work.1688.com",
			"order.1688.com", "offer.1688.com", "club.1688.com",
			"sycm.1688.com", "page.1688.com", "rate.1688.com",
			"baike.1688.com", "member.1688.com", "p4p.1688.com",
			"purchase.1688.com", "me.1688.com", "design.1688.com",
			"huopin.1688.com", "lomon1688.1688.com" };

	/**
	 * 去除1688 特殊域名，用以确定域名是否为商城
	 * 
	 * @param domain
	 * @return
	 */
	private boolean isValidShopDomain(String domain) {
		if (TextUtils.isEmpty(domain))
			return false;
		for (int i = 0; i < NOT_VALID_SHOP_DOMAINS.length; i++) {
			if (NOT_VALID_SHOP_DOMAINS[i].equals(domain))
				return false;
		}
		return true;
	}
	
	private boolean isDetailDomain(String domain)
	{
		String detail_domain="detail.1688.com";
		return detail_domain.equals(domain);
	}
	
	private boolean isFreeCompanyDomain(String domain)
	{
		String free_domain="www.1688.com";
		return free_domain.equals(domain);
	}

	private boolean isDetailPage(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DETAIL_PAGE, item.getRealUrl());
	}

	private boolean isShopPage(SearchResultItem item) {
		return isShopPage(item.getRealUrl());
	}

	private boolean isShopPage(String url) {
		return TextUtils.isMatchReg(PATTERN_URL, url);
	}
	
	private boolean isFreeCompany(SearchResultItem item)
	{
		return TextUtils.isMatchReg(PATTERN_FREE_COMPANY, item.getRealUrl());
	}

	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		//如果是免费公司信息，如http://www.1688.com/company/tianlupan.html
		if(isFreeCompanyDomain(item.getDomain()))
			return true;
		
		//如果是detail.1688.com
		if(isDetailDomain(item.getDomain()))
			return true;
		//如果商店 是shopxxxx.1688.com
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain())
				&& isValidShopDomain(item.getDomain());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isShopPage(item) || isDetailPage(item) || isFreeCompany(item);
	}

	private String getShopFromDetailPage(SearchResultItem item) {
		if (!isDetailPage(item))
			return null;
		String html = getHtml(item.getRealUrl());
		if (TextUtils.isEmpty(html))
			return null;
		Matcher matcher = PATTERN_SHOP_NAME.matcher(html);
		while (matcher.find())
			return matcher.group(1);
		return null;
	}
	
	/**
	 * 从域名中获取店名
	 * 例 ：http://www.1688.com/company/detail/intro/tianlupan.html => tianlupan
	 */
	private String getShopFromFreeCompany(SearchResultItem item) {
		if (!isFreeCompany(item))
			return null;
		
		Matcher matcher = PATTERN_FREE_COMPANY.matcher(item.getRealUrl());
		while (matcher.find())
			return matcher.group(2);
		return null;
	}
	
	/**
	 * 从免费公司信息中挖掘，如http://www.1688.com/company/tianlupan.html
	 * @param item
	 * @return
	 */
	private PhoneResult mineFreeCompany(SearchResultItem item)
	{
		if(!isFreeCompany(item)) return null;
		
		PhoneResult phoneResult=new PhoneResult();
		//http://www.1688.com/company/tianlupan.html?fromSite=company_site&tab=companyWeb_contact
		String realURL="http://www.1688.com/company/"+getShopFromFreeCompany(item)+".html";
		String url=realURL+"?fromSite=company_site&tab=companyWeb_contact";
		System.out.println("url="+url);
		String html=getHtml(url);
		if(TextUtils.isEmpty(html)) return null;
		
		String company=TextUtils.getSubString(html, "<b class=\"compay-name\">", "</b>");
		phoneResult.setJigou(company);
		
		String contact=TextUtils.getSubString(html, "<div class=\"contact-name\">", "</div>");
		if(!TextUtils.isEmpty(contact))
		{
			contact=contact.replace("&nbsp;", " ");
			contact=StringUtil.rmHtmlTag(contact);
			contact=contact.replace("（", "(");
			contact=contact.replace("）", ")");
			contact=contact.trim();

			phoneResult.setChenghu(contact);
		}
		
		String addressHtml=TextUtils.getSubString(html, "<p>地址 :</p>", "</tr>");
		if(!TextUtils.isEmpty(addressHtml))
		{
			String address=StringUtil.rmHtmlTag(addressHtml);
			address=address.trim();
			phoneResult.setAddress(address);
		}
		
		//查找行业
		html=getHtml(realURL);
		if(!TextUtils.isEmpty(html))
		{
			String hangyeHtml=TextUtils.getSubString(html, "<p>主营行业:</p>", "</tr>");
			
			if(!TextUtils.isEmpty(hangyeHtml))
			{
				List<String> hangyeList=TextUtils.getList(hangyeHtml, "target=\"_blank\" >", ";</a>");
				phoneResult.setHangyeList(hangyeList);
			}
		}
		
		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;
	}
	

	private PhoneResult mineShop(String shopUrl, String phone) {
		if (!isShopPage(shopUrl))
			return null;

		PhoneResult phoneResult = new PhoneResult();
		// 例 如： http://hshkj0668.1688.com/page/contactinfo.htm
		final String contact_url = shopUrl + "/page/contactinfo.htm";

		// 公司档案页，如：http://hshkj0668.1688.com/page/creditdetail.htm
		final String credit_url = shopUrl + "/page/creditdetail.htm";

		String contactHtml = getHtml(contact_url);
		if (TextUtils.isEmpty(contactHtml))
			return null;

		contactHtml = TextUtils.getSubString(contactHtml, "<h3>联系方式</h3>",
				"<dt>公司主页：</dt>");
		if (TextUtils.isEmpty(contactHtml))
			return null;

		String company = TextUtils.getSubString(contactHtml, "<h4>", "</h4>");
		phoneResult.setJigou(company);

		String dd = TextUtils.getSubString(contactHtml, "<dd>", "</dd>");

		if (!TextUtils.isEmpty(dd)) {
			String chenghu = TextUtils.getSubString(dd, "target=\"_blank\">",
					"</a>");

			String job = TextUtils.getSubString(dd, "</a>&nbsp;", "<a");
			if (!TextUtils.isEmpty(job)) {
				job = job.replace("&nbsp;", " ");
				job = job.replace("（", "(");
				job = job.replace("）", ")");
				job = job.replace(") ", ")");
				job=job.trim();
				job = StringUtil.rmHtmlTag(job);
			}

			if (!TextUtils.isEmpty(chenghu) && !TextUtils.isEmpty(job)) {
				phoneResult.setChenghu(TextUtils.appendJob(chenghu, job));
				//phoneResult.setChenghu(chenghu + " " + job);
			}

		}

		String address = TextUtils.getSubString(contactHtml,
				"<dd class = \"address\">", "</dd>");

		if (!TextUtils.isEmpty(address)) {
			address = TextUtils.clearHuanhang(address);
			phoneResult.setAddress(address);
		}

		// 加载行业信息
		String creditHtml = getHtml(credit_url);
		if (!TextUtils.isEmpty(creditHtml)) {
			String zhuYingHtml = TextUtils.getSubString(creditHtml,
					"<td class=\"item-title ta1\" >主营行业</td>", "</tr><tr>");
			if (!TextUtils.isEmpty(zhuYingHtml)) {
				String hanye = TextUtils.getSubString(zhuYingHtml,
						"<td class=\"ta2\" >", "</td>");
				if (!TextUtils.isEmpty(hanye)) {
					String[] list = hanye.split(";");

					ArrayList<String> hList = new ArrayList<String>();
					if (list != null && list.length > 0) {
						for (int i = 0; i < list.length; i++) {
							if (!TextUtils.isEmpty(list[i]))
								hList.add(list[i]);
						}

						if (hList.size() > 0)
							phoneResult.setHangyeList(hList);
					}
				}
			}
		}

		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;

	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		
		//如果是免费公司信息
		if(isFreeCompany(item))
			return mineFreeCompany(item);
		
		String shopURL = null;
		if (isDetailPage(item)) {
			shopURL = getShopFromDetailPage(item);
		} else if (isShopPage(item)) {
			shopURL = item.getRealUrl();
		}

		return mineShop(shopURL, item.getPhone());

	}

	public static void main(String[] args) {

		SearchResultItem item = new SearchResultItem();
		item.setRealUrl("http://www.1688.com/company/aizhishu.html");
		Domain1688 domain1688 = new Domain1688();
		// System.out.println("isDeatilpage="+domain1688.isDetailPage(item));
		// String url=domain1688.getShopFromDetailPage(item);
		System.out.println(domain1688.mine(item));
	}

}
