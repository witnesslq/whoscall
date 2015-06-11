package com.tianlupan.whoscall.mining.domains;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

public class DomainDianping extends MinableDomain {

	private final static String REGEX_DOMAIN = "^[a-zA-Z]{1,8}\\.dianping\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	/*
	 * 
	 * http://www.dianping.com/shop/2108134
	 * 
	 * http://www.dianping.com/shop/13848367/map
	 * http://www.dianping.com/shop/2108134?xxxxxx
	 * http://www.dianping.com/review/56556363
	 * http://www.dianping.com/search/keyword
	 * /16/0_%E5%A9%9A%E7%BA%B1%E5%BD%B1%E6%A5%BC/
	 * http://www.dianping.com/search/category/16/55/g6699r1874n1
	 * http://www.dianping.com/topic/s_c_16_163_r112_x6122
	 */

	private final static String REGEX_LIST = "^http://[a-zA-Z]{1,8}\\.dianping\\.com/(search/(keyword|category)|topic)/.*$";
	private final static Pattern PATTERN_LIST = Pattern.compile(REGEX_LIST);

	private final static String REGEX_REVIEW = "^http://[a-zA-Z]{1,8}\\.dianping\\.com/review/\\d+$";
	private final static Pattern PATTERN_REVIEW = Pattern.compile(REGEX_REVIEW);

	private final static String REGEX_SHOP = "^http://[a-zA-Z]{1,8}\\.dianping\\.com/shop/\\d+$";
	private final static Pattern PATTERN_SHOP = Pattern.compile(REGEX_SHOP);

	// 后面可能是/map 或/photos 等
	private final static String REGEX_SHOP_MAP = "^(http://[a-zA-Z]{1,8}\\.dianping\\.com/shop/\\d+).*$";
	private final static Pattern PATTERN_SHOP_MAP = Pattern
			.compile(REGEX_SHOP_MAP);

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

	/**
	 * 检查此网址是否为Review页
	 */
	private boolean isMinableReview(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_REVIEW, item.getRealUrl());
	}

	/**
	 * 检 查此网址是否为商店页
	 */
	private boolean isMinableShop(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_SHOP, item.getRealUrl());
	}

	/**
	 * 检 查此网址是否为商店地图页
	 */
	private boolean isMinableShopMap(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_SHOP_MAP, item.getRealUrl());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableShop(item) || isMinableShopMap(item)
				|| isMinableList(item) || isMinableReview(item);
	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		if (!isMinableUrl(item))
			return null;

		String realURL = item.getRealUrl();
		if (TextUtils.isEmpty(realURL))
			return null;

		String shopURL = null;
		if (isMinableShop(item)) {
			shopURL = realURL;
		} else if (isMinableShopMap(item)) {
			shopURL = getShopUrlFromMap(item);
		} else if (isMinableList(item)) {
			shopURL = getShopUrlFromList(item);
		} else {
			shopURL = getShopUrlFromReview(item);
		}

		// 如果从列表或回复页中获取shopURL失败，则直接返回
		if (TextUtils.isEmpty(shopURL))
			return null;

		return getPhoneResultFromShop(item.getPhone(), shopURL);

	}

	/**
	 * 从商店页获取信息
	 */
	private PhoneResult getPhoneResultFromShop(String phone, String shopURL) {
		if (TextUtils.isEmpty(shopURL))
			return null;
		if (!shopURL.startsWith("http://"))
			return null;

		String html = getHtml(shopURL);
		if (TextUtils.isEmpty(html))
			return null;

		PhoneResult phoneResult = new PhoneResult();

		final String TAG_START_NAV_CONTAINER = "<div class=\"breadcrumb\">";
		final String TAG_END_NAV_CONTAINER = "</div>";
		String navContainer = TextUtils.getSubString(html,
				TAG_START_NAV_CONTAINER, TAG_END_NAV_CONTAINER);

		final String TAG_START_SHOPNAME = "<strong>";
		final String TAG_END_SHOPNAME = "</strong>";

		String shopName = TextUtils.getSubString(navContainer,
				TAG_START_SHOPNAME, TAG_END_SHOPNAME);
		phoneResult.setJigou(shopName);

		final String TAG_START_NAV = "<span class=\"bread-name\" itemprop=\"title\">";
		final String TAG_END_NAV = "</span>";

		List<String> hangyeList = TextUtils.getList(navContainer,
				TAG_START_NAV, TAG_END_NAV);

		if (hangyeList != null && hangyeList.size() > 0) {

			// 只保留j最后的导航做行业
			for (int i = 0; i < hangyeList.size() - 1; i++)
				hangyeList.remove(0);
		}

		phoneResult.setHangyeList(hangyeList);

		// 获取图片

		final String TAG_START_IMAG_CONTAINER = "的图片\"";
		final String TAG_END_IMAGE_CONTAINER = "/>";
		String imageContainer = TextUtils.getSubString(html,
				TAG_START_IMAG_CONTAINER, TAG_END_IMAGE_CONTAINER);

		if (!TextUtils.isEmpty(imageContainer)) {
			String image = TextUtils.getSubString(imageContainer, "src=\"",
					"\"");
			phoneResult.setImage(image);
		}
		
		//http://www.dianping.com/shop/2399530_p5
		final String TAG_START_ADDRESS_COMMON="<li><em>地址：</em>";
		final String TAG_END_ADDRESS_COMMON="<div";
		if(html.contains(TAG_START_ADDRESS_COMMON))
		{
			String addressHtml=TextUtils.getSubString(html, TAG_START_ADDRESS_COMMON, TAG_END_ADDRESS_COMMON);
			if(!TextUtils.isEmpty(addressHtml))
			{
				String address=StringUtil.rmHtmlTag(addressHtml);
				phoneResult.setAddress(address);
			}
		}
		else {
			final String TAG_START_ADDRESS = "<div class=\"shop-addr\">";
			final String TAG_END_ADDRESS = "</div>";
			
			String addressHtml = TextUtils.getSubString(html, TAG_START_ADDRESS,
					TAG_END_ADDRESS);
			if (!TextUtils.isEmpty(addressHtml)) {
				final String TAG_START_ADDRESS2 = "<em>地址：</em>";
				final String TAG_END_ADDRESS2 = "</span>";
				String address = TextUtils.getSubString(addressHtml,
						TAG_START_ADDRESS2, TAG_END_ADDRESS2);
				if (!TextUtils.isEmpty(address)) {
					address = StringUtil.rmHtmlTag(address);
					phoneResult.setAddress(address);
				}
			}
		}


		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;
	}

	private String getShopUrlFromList(SearchResultItem item) {
		String html = getHtml(item.getRealUrl());
		if (TextUtils.isEmpty(html))
			return null;
		String TAG_SHOP_BEGIN = "<li class=\"shopname\">";
		String TAG_SHOP_END = "</li>";

		String TAG_SHOP_LINK_BEGIN = "<a href=\"";
		String TAG_SHOP_LINK_END = "\"";

		List<String> shops = TextUtils.getList(html, TAG_SHOP_BEGIN,
				TAG_SHOP_END);

		for (String shop : shops) {
			if (shop.contains(item.getPhone())) {
				String href = TextUtils.getSubString(shop, TAG_SHOP_LINK_BEGIN,
						TAG_SHOP_LINK_END);
				return formatLink(href);
			}
		}

		return null;
	}

	private String formatLink(String href) {
		if (TextUtils.isEmpty(href))
			return null;

		if (href.startsWith("/")) {
			href = "http://www.dianping.com" + href;
			return href;
		} else if (href.startsWith("http://")) {
			return href;
		} else
			return null;
	}

	private String getShopUrlFromReview(SearchResultItem item) {
		String html = getHtml(item);
		if (TextUtils.isEmpty(html))
			return null;

		String TAG_START_SHOP = "<div class=\"box reviewShop\">";
		String TAG_END_SHOP = "</div>";

		String shop = TextUtils
				.getSubString(html, TAG_START_SHOP, TAG_END_SHOP);

		if (TextUtils.isEmpty(shop))
			return null;

		String link = TextUtils.getSubString(shop, "<a href=\"", "\"");
		return formatLink(link);

	}

	/**
	 * 去掉最后的http://www.dianping.com/shop/13848367/map 最后的/map
	 */
	private String getShopUrlFromMap(SearchResultItem item) {
		Matcher matcher = PATTERN_SHOP_MAP.matcher(item.getRealUrl());
		while (matcher.find())
			return matcher.group(1);

		return null;
	}

	public static void main(String[] args) {

		
		
		
		SearchResultItem item = new SearchResultItem();
		DomainDianping domainDianping = new DomainDianping();

		item.setRealUrlChecked(true);
		item.setRealUrl("http://www.dianping.com/shop/13848367/photoes");
		item.setPhone("4000908682");

		if (domainDianping.isMinableUrl(item)) {
			System.out.println("yes,link="
					+ domainDianping.getShopUrlFromMap(item));
		} else {
			System.out.println("no");
		}

		// String content=
		// StringUtil.rmHtmlTag("<a onclick=\"pageTracker._trackPageview('dp_newshop_dizhi_xingzhengqu');\" href=\"/search/category/16/55/r110\" class=\"region\">江岸区</a>中山大道635号(南京路口)");
		// System.out.println("content="+content);

		PhoneResult phoneResult = domainDianping.getPhoneResultFromShop(
				"4000908682", "http://www.dianping.com/shop/13848367");
		System.out.println("phoneResult=" + phoneResult);

	}

}
