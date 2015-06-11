package com.tianlupan.whoscall.mining;

import java.util.List;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.CityCode;
import com.tianlupan.whoscall.CityItem;
import com.tianlupan.whoscall.PhoneNumber;
import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.io.HttpDownload;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.util.StringUtil;

public class SearchEnginBaidu extends SearchEngine {

	private final String RECORD_TAG_START = "<div class=\"result c-container \"";
	private final String RECORD_TAG_END = "</div></div>";

	private final String RECORD_TAG_TITLE_START = "<h3 class=\"t\">";
	private final String RECORD_TAG_TITLE_END = "</h3>";

	private final String RECORD_TAG_TITLE_START1 = ">";
	private final String RECORD_TAG_TITLE_End1 = "</a>";

	private final String RECORD_TAG_CONENT_START = "<div class=\"c-abstract\">";
	private final String RECORD_TAG_CONTENT_END = "</div>";

	private final String RECORD_TAG_URL_START = "<span class=\"g\">";
	private final String RECORD_TAG_URL_END = "</span>";

	private final String BIAOJI_TAG_START = "<div class=\"op_liarphone2_word\">";
	private final String BIAOJI_TAG_END = "</div>";

	private final String BIAOJI_SPACE = "&nbsp;";

	private String url;

	private final String WWW_URL = "http://www.baidu.com/s?word=";
	
	private final String CACHE_TAG_START="<a data-nolog href=\"";
	private final String CACHE_TAG_END="\"";

	public static void main(String[] args) {
		SearchEnginBaidu searchEnginBaidu=new SearchEnginBaidu();
		searchEnginBaidu.search("027-59378532");
		System.out.println(searchEnginBaidu.getTags());
	}
	
	/*
	 * 检查搜索内容中是否包含电话号码
	 */
	private boolean isValid(String phone,String title,String content)
	{
		MatchLevel matchLevelTitle=matchPhone(phone, title);
		MatchLevel matchLevelContent=matchPhone(phone, content);
		
		if(matchLevelTitle==MatchLevel.MATCH || matchLevelContent==MatchLevel.MATCH)
			return true;
		
		if(matchLevelTitle==MatchLevel.NOMATCH && matchLevelContent==MatchLevel.NOMATCH)
			return false;
		
		PhoneNumber phoneNumber=new PhoneNumber(phone);
		if(phoneNumber.getType()== PhoneNumber.PhoneType.FIXEDPHONE)
		{
			if(matchLevelTitle==MatchLevel.CITY && matchLevelContent==MatchLevel.PHONE_NO_CITY_CODE)
				return true;
			else if(matchLevelTitle==MatchLevel.PHONE_NO_CITY_CODE && matchLevelContent==MatchLevel.CITY)
				return true;
			else{
				//System.out.println("matchTitle="+matchLevelTitle+", matchContent="+matchLevelContent);
				return false;
			}
		}
		
		return false;
	}
	
	private MatchLevel matchPhone(String phone,String content )
	{
		PhoneNumber phoneNumber=new PhoneNumber(phone);
		PhoneNumber.PhoneType phoneType=phoneNumber.getType();
		
		final String MATCH_BEGIN_TAG="<em>";
		final String MATCH_END_TAG="</em>";
		
		
		
		List<String> matchList= TextUtils.getList(content, MATCH_BEGIN_TAG, MATCH_END_TAG);
		
		boolean cityMatch=false;
		if(phoneType== PhoneNumber.PhoneType.FIXEDPHONE)
		{
			List<CityItem> cityList= CityCode.getCityListByCode(phoneNumber.getCode());
			for(CityItem cityItem:cityList)
			{
				if(content.contains(cityItem.cityName))
				{
					cityMatch=true;
					break;
				}
			}
		}
		
		if(matchList.size()==0 && !cityMatch ) return MatchLevel.NOMATCH;
		
		//把红色匹配内容中最长的当作有效的来检查
		String mPhone="";
		for(int i=0;i<matchList.size();i++)
		{
			if(matchList.get(i).length()>mPhone.length())
				mPhone=matchList.get(i);
		}
		
		
	
		
		if(phoneType== PhoneNumber.PhoneType.CELLPHONE)
		{
			//移动电话，只有匹配和不匹配两种情况
			return mPhone.equals(phone) ? MatchLevel.MATCH : MatchLevel.NOMATCH;			
		}
		else if(phoneType== PhoneNumber.PhoneType.FIXEDPHONE)
		{
			//固定电话
	
			//如果没匹号
			if(TextUtils.isEmpty(phoneNumber.getCode())) return MatchLevel.NOMATCH;
			//号码匹配完全成功
			String phoneNumber1=phoneNumber.getCode()+"-"+phoneNumber.getFixedNumberWithoutCode();
			String phoneNumber2=phoneNumber.getCode()+phoneNumber.getFixedNumberWithoutCode();
			if(mPhone.equals(phoneNumber1) || mPhone.equals(phoneNumber2) )
				return MatchLevel.MATCH;
			//如果没有区号的号码 如027-86640000 只有86640000
			boolean fixedMatch=mPhone.equals(phoneNumber.getFixedNumberWithoutCode());
			
			if(fixedMatch)
			{
				//如果包含的号码尾号相同，区号不同，仍认为是无效的
				String contentText=StringUtil.rmHtmlTag(content);
				Pattern pattern_phone=Pattern.compile("(0\\d{2,3})-?"+phoneNumber.getFixedNumberWithoutCode());
				String matchCode=TextUtils.getMatchGroup(pattern_phone, contentText);
				if(!TextUtils.isEmpty(matchCode))
				{
					//System.out.println("matchCode="+matchCode);
					if(!matchCode.equals(phoneNumber.getCode()))
						fixedMatch=false;
				}
			}

			
			if(fixedMatch)
			{
				if(cityMatch) return MatchLevel.MATCH;
				else return MatchLevel.PHONE_NO_CITY_CODE;
			}
			else {
				if(cityMatch) return MatchLevel.CITY;
				else return MatchLevel.NOMATCH;
			}
		}
		else {
			//去掉phone中的-,和红色内容匹配
			String phone1=phone.replace("-", "");
			String mPhone1=mPhone.replace("-", "");
			return mPhone1.equalsIgnoreCase(phone1) ? MatchLevel.MATCH : MatchLevel.NOMATCH;
		}	
		
	} 
	

	
	private static enum MatchLevel{
	    MATCH, //全部匹配，比如号码全匹配
	    NOMATCH, //完全不匹配
	    CITY, //和号码所在的城市匹配 如027-xxxxxx  ，正文中仅有武汉
	    PHONE_NO_CITY_CODE; //没有区号的号码 如027-86640000 只有86640000
	}
	

	@Override
	public SearchEngineTypes getType() {
		return SearchEngineTypes.BAIDU;
	}

	@Override
	protected void search(String phone) {

		url = WWW_URL + phone;

		HttpDownload httpDownload = new HttpDownload();
		String html = httpDownload.getHtml(url);
		if(!TextUtils.isEmpty(html) && html.length()<1024)
			System.out.println("html=" + html);

		List<String> recordList = TextUtils.getList(html, RECORD_TAG_START,
				RECORD_TAG_END);

		for (String content : recordList) {
			String titleWrapper = TextUtils.getSubString(content,
					RECORD_TAG_TITLE_START, RECORD_TAG_TITLE_END);
			
			String url=TextUtils.getSubString(titleWrapper, "href = \"//", "\"");
			
			if(!TextUtils.isEmpty(url) && !url.startsWith("http"))
			{
				url="http://"+url;
			}

			String title = TextUtils.getSubString(titleWrapper,
					RECORD_TAG_TITLE_START1, RECORD_TAG_TITLE_End1);

			String detail = TextUtils.getSubString(content,
					RECORD_TAG_CONENT_START, RECORD_TAG_CONTENT_END);

			// 例子：www.dianping.com/searc...&nbsp;2014-04-17&nbsp;
			String urlOrginal = TextUtils.getSubString(content,
					RECORD_TAG_URL_START, RECORD_TAG_URL_END);


			String domain = getDomain(urlOrginal);

			String date = TextUtils.getSubString(urlOrginal, BIAOJI_SPACE,
					BIAOJI_SPACE);
			
			String cacheUrl=TextUtils.getSubString(content, CACHE_TAG_START, CACHE_TAG_END);
			
			if(isValid(phone, title, detail))
			{
				AddItem(title, detail, url, domain, date, cacheUrl);
				MyStaticValue.Log4j.debug("匹配号码成功，phone="+phone+", title="+title+", content="+detail);
			}
			else {
				MyStaticValue.Log4j.debug("无效的搜索内容项，匹配号码不成功，phone="+phone+", title="+title+", content="+detail);
			}
		}

		// 用户标记
		List<String> biaojiList = TextUtils.getList(html, BIAOJI_TAG_START,
				BIAOJI_TAG_END);
		for (String biaojiFull : biaojiList) {

			if (!TextUtils.isEmpty(biaojiFull)) {
				// TODO 目前暂时只放置第一个管家的信息

				String biaojiCountString = TextUtils.getSubString(biaojiFull,
						"被", "个");

				int biaojiCount = 0;

				if (TextUtils.isNumber(biaojiCountString)) {
					biaojiCount = Integer.valueOf(biaojiCountString);
				}

				String biaoji = TextUtils.getSubString(biaojiFull, ">", "</a>");

				String biaojiType = TextUtils.getSubString(biaojiFull,
						"<strong>\"", "\"</strong>");

				SearchResultUserTagType type = SearchResultUserTagType
						.get(biaoji);

				AddTag(type, biaojiType, biaojiCount);
			}

		}

	}
	


}
