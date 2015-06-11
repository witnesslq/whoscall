package com.tianlupan.whoscall.io;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.model.NTRecordSet;

public class BaiduFetch extends ISearchEngine {

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

	private String url;

	private final String WWW_URL = "http://www.baidu.com/s?word=";

	private final String SEARCH_ENGINE = "baidu";

	public static void main(String[] args) {
		BaiduFetch baiduFetch = new BaiduFetch();
		JSONObject jsonObject = baiduFetch.fectchNumber("075787614365");
		System.out.println("result=" + jsonObject.toString());
		

		
	}

	@Override
	public JSONObject fectchNumber(String number) {
		url = WWW_URL + number;

		HttpDownload httpDownload = new HttpDownload();
		String html = httpDownload.getHtml(url);

	 System.out.println("html="+html);

		List<String> recordList = TextUtils.getList(html, RECORD_TAG_START,
				RECORD_TAG_END);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(NTRecordSet.JSON_SEARCH_ENGINE, SEARCH_ENGINE);
		jsonObject.put(NTRecordSet.JSON_PHONE_NUMBER, number);

		JSONArray jsonArray = new JSONArray();

		for (String content : recordList) {
			String titleWrapper = TextUtils.getSubString(content,
					RECORD_TAG_TITLE_START, RECORD_TAG_TITLE_END);

			String title = TextUtils.getSubString(titleWrapper,
					RECORD_TAG_TITLE_START1, RECORD_TAG_TITLE_End1);

			String detail = TextUtils.getSubString(content,
					RECORD_TAG_CONENT_START, RECORD_TAG_CONTENT_END);

			String url = TextUtils.getSubString(content, RECORD_TAG_URL_START,
					RECORD_TAG_URL_END);

			AddToRecords(jsonArray, title, detail, url);
		}

		jsonObject.put(NTRecordSet.JSON_RECORDS, jsonArray);

		// 用户标记
		/*
		 * <div class="op_liarphone2_word">
		 */
		String biaojiFull = TextUtils.getSubString(html, BIAOJI_TAG_START,
				BIAOJI_TAG_END);
		if (!TextUtils.isEmpty(biaojiFull)) {
			//TODO 目前暂时只放置第一个管家的信息
			JSONArray guanjiArray = new JSONArray();

			String biaojiCountString = TextUtils.getSubString(biaojiFull, "被",
					"个");

			int biaojiCount = 0;

			if (TextUtils.isNumber(biaojiCountString)) {
				biaojiCount = Integer.valueOf(biaojiCountString);
			}

			String biaoji = TextUtils.getSubString(biaojiFull, ">", "</a>");

			if (!TextUtils.isEmpty(biaoji)
					&& (biaoji.length() > 6 || biaoji.length() < 2))
				biaoji = null;

			String biaojiType = TextUtils.getSubString(biaojiFull,
					"<strong>\"", "\"</strong>");

			AddToGuanjia(guanjiArray, biaoji, biaojiType, biaojiCount);

			jsonObject.put(NTRecordSet.JSON_GUANJIA, guanjiArray);
		}

		return jsonObject;
	}

}
