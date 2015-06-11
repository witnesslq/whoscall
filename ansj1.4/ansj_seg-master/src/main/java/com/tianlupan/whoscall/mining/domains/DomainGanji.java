package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;
import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.TextUtils;

/**
 * TODO 现在还有的问题：Ganji 会封锁频繁访问IP
 * 
 * @author laotian
 * 
 */
public class DomainGanji extends MinableDomain {

	private final static String REGEX_DOMAIN = "^[a-zA-Z]{1,8}\\.ganji\\.com$";
	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	// http://wh.ganji.com/fuwu_dian/296143/daba/
	private final static String REGEX_FUWU = "^(http://[a-zA-Z]{1,8}\\.ganji\\.com/fuwu_dian/\\d+x?/)(\\w+/)*$";
	private final static Pattern PATTERN_FUWU = Pattern.compile(REGEX_FUWU);

	// 是列表 http://wh.ganji.com/bangongshizhuangxiu/o3/
	private final static String REGEX_LIST = "^http://[a-zA-Z]{1,8}\\.ganji\\.com/([a-z]+/)+(o\\d+/)?$";
	@SuppressWarnings("unused")
	private final static Pattern PATTERN_LIST = Pattern.compile(REGEX_LIST);

	// TODO 房产 http://wh.ganji.com/fang1/tuiguang-65895220.htm
	// http://bj.ganji.com/fang_810084/fang1/
	private final static Pattern PATTERN_FANG = Pattern
			.compile("^http://[a-zA-Z]{1,8}\\.ganji\\.com/fang_?\\d+/.*$");

	// 没有称呼 的店主 店主277054
	private final static String REGEX_DIANZHU = "^店主\\d+$";
	private final static Pattern PATTERN_DIANZHU = Pattern
			.compile(REGEX_DIANZHU);

	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_DOMAIN, item.getDomain());
	}

	private boolean isMinableFang(SearchResultItem item) {
		return TextUtils.isMatchReg(PATTERN_FANG, item.getRealUrl());
	}

	private PhoneResult mineFangAgent(String url) {
		final String JOB_JINGJI = "房产经纪";

		List<String> hangyeList = new ArrayList<String>();
		hangyeList.add("房产中介");

		String html = getHtml(url);
		if (TextUtils.isEmpty(html))
			return null;

		PhoneResult phoneResult = new PhoneResult();
		String agentInfoHtml = TextUtils.getSubString(html,
				"<div class=\"agent-info\"", "<div class=\"col-line\"></div>");

		if (!TextUtils.isEmpty(agentInfoHtml)) {
			String imageString=TextUtils.getSubString(agentInfoHtml,
					"src=\"", "\"");
			//默认头相不保存 http://stacdn201.ganjistatic1.com/src/image/house/fangvip/noimg_head.gif
			if(!TextUtils.isEmpty(imageString) && !imageString.contains("noimg"))
				phoneResult.setImage(imageString);
			
			String chenghu = TextUtils.getSubString(agentInfoHtml,
					"<p class=\"person-name\"><a title=\"\" href=\"#\">",
					"</a></p>");
			if (!TextUtils.isEmpty(chenghu))
				phoneResult
						.setChenghu(TextUtils.appendJob(chenghu, JOB_JINGJI));

			phoneResult.setJigou(TextUtils.getSubString(agentInfoHtml,
					"<p class=\"company-name\" title=\"", "\">"));

			phoneResult.setHangyeList(hangyeList);

			return phoneResult;
		}

		if (TextUtils.isEmpty(agentInfoHtml)) {
			// http://bj.ganji.com/fang5/tuiguang-53925525.htm
			agentInfoHtml = TextUtils.getSubString(html, "<!--个人信息 start-->",
					"<!--个人信息 end-->");
			if (TextUtils.isEmpty(agentInfoHtml))
				return null;


			String imageString=TextUtils.getSubString(agentInfoHtml,
					"src=\"", "\"");
			//默认头相不保存 http://stacdn201.ganjistatic1.com/src/image/house/fangvip/noimg_head.gif
			if(!TextUtils.isEmpty(imageString) && !imageString.contains("noimg"))
				phoneResult.setImage(imageString);
			
			phoneResult.setJigou(TextUtils.getSubString(agentInfoHtml,
					"<p class=\"company-name\">", "</p>"));

			phoneResult.setHangyeList(hangyeList);

			String chenghuHtml = TextUtils.getSubString(agentInfoHtml,
					"<div class=\"person-name\">", "</div>");
			if (!TextUtils.isEmpty(chenghuHtml)) {
				String chenghu = StringUtil.rmHtmlTag(chenghuHtml);
				chenghu = TextUtils.clearHuanhang(chenghu);
				if (!TextUtils.isEmpty(chenghu))
					phoneResult.setChenghu(TextUtils.appendJob(chenghu,
							JOB_JINGJI));
			}

			return phoneResult;

		}

		return null;
	}

	/***
	 * 是否为列表页，例 如：http://wh.ganji.com/jiazheng/wuchang/
	 */
	private boolean isMinableList(SearchResultItem item) {

		return false;
		// 因为现在ganji封锁先获取列表项，再显示详情页，可能是由于速度太快等
		// 暂时不允许解析列表
		// TODO
		// return TextUtils.isMatchReg(PATTERN_LIST, item.getRealUrl());
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		return isMinableList(item) || isMinableFuwu(item)
				|| isMinableFang(item);
	}

	private boolean isMinableFuwu(SearchResultItem item) {
		return isMinableFuwu(item.getRealUrl());
	}

	private boolean isMinableFuwu(String url) {
		return TextUtils.isMatchReg(PATTERN_FUWU, url);
	}

	private String getFuwuUrl(String url) {
		if (!isMinableFuwu(url))
			return null;

		Matcher matcher = PATTERN_FUWU.matcher(url);
		while (matcher.find())
			return matcher.group(1);

		return null;
	}

	private String getFuwuUrlFromList(SearchResultItem item) {
		if (!isMinableList(item))
			return null;
		String phone = item.getPhone();
		if (TextUtils.isEmpty(phone))
			return null;

		String html = getHtml(item.getRealUrl());

		if (TextUtils.isEmpty(html))
			return null;

		final String TAG_START_LIST = "<li class=\"list-img\">";
		final String TAG_END_LIST = "</li>";

		List<String> list = TextUtils.getList(html, TAG_START_LIST,
				TAG_END_LIST);

		for (String line : list) {
			if (line.contains(phone)) {
				String HREF_TAG = "<a class=\"f14 list-info-title\" target=\"_blank\" href=\"";
				String href = TextUtils.getSubString(line, HREF_TAG, "\"");
				if (!TextUtils.isEmpty(href) && href.startsWith("/fuwu_dian")) {
					href = "http://www.ganji.com" + href;
					return href;
				}
			}

		}

		return null;

	}

	/**
	 * 使用的是类似 店主1234 的没有设定名称的默认称呼
	 * 
	 * @param chenghu
	 * @return
	 */
	private boolean isNoName(String chenghu) {
		return TextUtils.isMatchReg(PATTERN_DIANZHU, chenghu);
	}

	private PhoneResult getFuwu(String url, String phone) {
		String fuwuURL = getFuwuUrl(url);
		if (TextUtils.isEmpty(fuwuURL))
			return null;
		PhoneResult phoneResult = new PhoneResult();
		String html = getHtml(fuwuURL);
		if (TextUtils.isEmpty(html))
			return null;

		//System.out.println("fuwuURL=" + fuwuURL + ",  html=" + html);

		final String TAG_START_CONTACT = "<h3><ul><li class=\"active\">联系店主</li></ul></h3>";
		final String TAG_END_CONTACT = "</ul>";

		String contactHTML = TextUtils.getSubString(html, TAG_START_CONTACT,
				TAG_END_CONTACT);
		if (TextUtils.isEmpty(contactHTML))
			return null;

		String address = TextUtils.getSubString(contactHTML,
				"<li><span class=\"t\">商家地址：</span><p class=\"fl\">",
				"</p></li>");

		if (!TextUtils.isEmpty(address)) {
			address = StringUtil.rmHtmlTag(address);
			phoneResult.setAddress(address);
		}

		String contact = TextUtils.getSubString(contactHTML,
				"<span class=\"t\">联&ensp;系&ensp;人：</span>",
				"<!--webim start-->");

		if (!TextUtils.isEmpty(contact)) {
			contact = StringUtil.rmHtmlTag(contact);
			contact = contact.trim();
			if (!isNoName(contact))
				phoneResult.setChenghu(contact);
		}

		String comapny = TextUtils.getSubString(contactHTML,
				"<span class=\"t\">公司名称：</span>", "</div>");
		if (!TextUtils.isEmpty(comapny)) {
			comapny = TextUtils.getSubString(comapny, "<h1>", "</h1>");
			phoneResult.setJigou(comapny);
		} else {
			// 如果不是公司，把贴子标题加进去，当作机构名
			// TODO 将来想更好的方法
			// 最好从内容中提出公司信息
			String title = TextUtils.getSubString(contactHTML,
					"<li class=\"fb\">", "</li>");
			phoneResult.setJigou(title);
		}

		String fuwuHtml = TextUtils.getSubString(contactHTML,
				"<span class=\"t\">提供服务：</span>", " </li>");
		if (!TextUtils.isEmpty(fuwuHtml)) {
			List<String> hrefList = TextUtils.getList(fuwuHtml, "<a href=",
					"/a>");
			for (int i = 0; i < hrefList.size(); i++) {
				String href = hrefList.get(i);
				if (!TextUtils.isEmpty(href))
					hrefList.set(i, TextUtils.getSubString(href, ">", "<"));
			}

			phoneResult.setHangyeList(hrefList);

		}

		// 图片
		boolean picFound = false;
		String headLogo = TextUtils.getSubString(html, "<div class=\"pic\">",
				"</div>");
		if (!TextUtils.isEmpty(headLogo)) {
			headLogo = TextUtils.getSubString(headLogo, " <img src=\"", "\"");
			if (!TextUtils.isEmpty(headLogo)) {
				// 如果不是默认图片
				if (!headLogo.endsWith(".png") && !headLogo.contains("normal")) {
					picFound = true;
					phoneResult.setImage(headLogo);
				}

			}
		}
		// 如果是默认头像 ，则用第一张大图
		if (!picFound) {
			String bigImage = TextUtils.getSubString(html,
					"<img data-role=\"img\" src=\"", "\"");
			phoneResult.setImage(bigImage);
		}

		if (phoneResult.isFound())
			return phoneResult;
		else
			return null;

	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		if (!isMinableUrl(item))
			return null;

		String url = null;
		
		if(isMinableFang(item))
			return mineFangAgent(item.getRealUrl());

		if (isMinableList(item))
			url = getFuwuUrlFromList(item);
		else if (isMinableFuwu(item))
			url = item.getRealUrl();


		System.out.println("to fetch fuwu url=" + url);

		if (!TextUtils.isEmpty(url)) {
			return getFuwu(url, item.getPhone());
		} else
			return null;

	}

	public static void main(String[] args) {
		SearchResultItem item = new SearchResultItem();
		item.setPhone("13972665901 ");
		item.setDomain("wh.ganji.com");
		item.setRealUrl("http://wh.ganji.com/fang5/tuiguang-59989424.htm");
		DomainGanji domainGanji = new DomainGanji();
		System.out.println(domainGanji.mine(item));

	}

}
