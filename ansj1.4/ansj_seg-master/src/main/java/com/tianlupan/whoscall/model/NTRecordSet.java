package com.tianlupan.whoscall.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.ansj.util.MyStaticValue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 根据用户提供的json解析出机构、人名、行业等信息 工具类
 * 
 * @author laotian
 * 
 */
public class NTRecordSet {
	
	/**
	 * TODO 长沙中星电站辅机设备制造有限公司,长沙中星电站辅机设备制造有限...
	 * 检查准确度，比如搜索010-85562356 出现了0731-85562356
传真电话:0731-85562356 邮箱:woshigg20@qq.com 公司地址:长沙市雨花区自然岭... 企业客服直线: 010-84450630 84450639 84450561 84450633 84926436 84926406 ...

	 */

	public final static String JSON_RECORDS = "records";
	
	public final static String JSON_PHONE_NUMBER="phoneNumber";
	
	public final static String JSON_SEARCH_ENGINE="searchEngine";

	/**
	 * 此列应该为json数组，包括下面三项内容，可选参数
	 */
	public final static String JSON_GUANJIA = "guanjia";
	public final static String JSON_GUANJIA_TYPE = "type";
	public final static String JSON_GUANJIA_BIAOJI = "biaoji";
	public final static String JSON_GUANJIA_BIAOJI_COUNT = "biaojiCount";
	
	

	private final static int MAX_HANGYE_COUNT = 4;

	public static void main(String[] args) {
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();

		table.put("标志设计", 10);
		table.put("网站制作", 20);
		table.put("VI设计", 15);

		List<TableItem> list = getMax(table, 2);

		System.out.println(list);

	}

	private static String getMax(Hashtable<String, Integer> table) {
		List<TableItem> list = getMax(table, 1);
		MyStaticValue.Log4j.debug("get Max list="+list);
		return list.size() > 0 ? list.get(0).getKey() : null;
	}

	private static class TableItem implements Comparable<TableItem> {

		private final String key;

		private final int value;

		public String getKey() {
			return key;
		}

		public int getValue() {
			return value;
		}

		public TableItem(String key, int value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(TableItem o) {
			return value > o.value ? -1 : (value == o.value ? 0 : 1);
		}

		@Override
		public String toString() {
			return getKey() + ":" + getValue();
		}

	}

	private static List<String> getMaxHangeye(List<TableItem> items) {
		List<String> hanyeList = new ArrayList<String>();

		for (int i = 0; i < items.size(); i++) {
			hanyeList.add(items.get(i).getKey());
		}

		return hanyeList;
	}

	private static List<TableItem> getMax(Hashtable<String, Integer> table,
			int maxCount) {
		List<TableItem> list = new ArrayList<TableItem>();
		Iterator<Entry<String, Integer>> iterator = table.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			TableItem item = new TableItem(entry.getKey(), entry.getValue());
			list.add(item);
		}

		Collections.sort(list);

		MyStaticValue.Log4j.debug("getMax list="+list);
		
		if (maxCount < list.size()) {
			list = list.subList(0, maxCount);
		}

		return list;

	}

	private static class GuanjiaItem implements Comparable<GuanjiaItem> {

		private final String type;

		private final String biaoji;

		private final int biaojiCount;

		public String getBiaoji() {
			return biaoji;
		}

		public String getType() {
			return type;
		}

		public int geBiaojiCount() {
			return biaojiCount;
		}

		public GuanjiaItem(String type, String biaoji, int biaojiCount) {
			this.type = type;
			this.biaojiCount = biaojiCount;
			this.biaoji = biaoji;
		}

		@Override
		public int compareTo(GuanjiaItem o) {
			return biaojiCount > o.biaojiCount ? -1
					: (biaojiCount == o.biaojiCount ? 0 : 1);
		}

		@Override
		public String toString() {
			return getType() + ":" + getBiaoji() + ":" + geBiaojiCount();
		}

	}
	
	public static String parseJson(JSONObject jsonObject)
	{
		return parseJson(jsonObject, 1);
	}
	
	/**
	 * 
	 * @param jsonObject
	 * @param method 1代表ToAnaylsis, 2代表NLp 自然语言
	 * @return
	 */
	public static String parseJson(JSONObject jsonObject,int method)
	{
		// 之后返回分值最大的 机构名，行业，人名
				NTDetail ntDetail = new NTDetail();
				
				if (jsonObject.has(JSON_RECORDS)) {
					JSONArray recordsArray = jsonObject.getJSONArray(JSON_RECORDS);

					Hashtable<String, Integer> ntList = new Hashtable<String, Integer>();
					Hashtable<String, Integer> hanyeList = new Hashtable<String, Integer>();
					Hashtable<String, Integer> chenghuList = new Hashtable<String, Integer>();

					for (int i = 0; i < recordsArray.size(); i++) {
						JSONObject record = recordsArray.getJSONObject(i);
						NTRecord ntRecord = new NTRecord(ntList, hanyeList, chenghuList);
						ntRecord.doParse(record,method);
					}

					// 解析比较出NTDetail
					// 比较相似的记录，如果存在则互相加分
					String nt = getMax(ntList);

					List<String> hList = getMaxHangeye(getMax(hanyeList,
							MAX_HANGYE_COUNT));

					String chenghu = getMax(chenghuList);

					ntDetail.nt = nt;
					ntDetail.hangyeList = hList;
					ntDetail.chenghu = chenghu;
				}
				// 解析管家信息
				if (jsonObject.has(JSON_GUANJIA)) {
					JSONArray guanjiaArray = jsonObject.getJSONArray(JSON_GUANJIA);
					if (guanjiaArray != null && guanjiaArray.size() > 0) {
						List<GuanjiaItem> list = new ArrayList<NTRecordSet.GuanjiaItem>();
						for (int i = 0; i < guanjiaArray.size(); i++) {
							JSONObject guanjia = guanjiaArray.getJSONObject(i);
							String type = guanjia.getString(JSON_GUANJIA_TYPE);
							String biaoji = guanjia.getString(JSON_GUANJIA_BIAOJI);
							int biaojiCount = guanjia.getInt(JSON_GUANJIA_BIAOJI_COUNT);
							GuanjiaItem item = new GuanjiaItem(type, biaoji,
									biaojiCount);
							list.add(item);

						}

						Collections.sort(list);

						if (list.size() > 0) {
							GuanjiaItem item = list.get(0);
							ntDetail.biaoji = item.getBiaoji();
							ntDetail.biaojiType = item.getType();
							ntDetail.biaojiCount = item.geBiaojiCount();
						}
					}
				}
				return ntDetail.toJson();
	}


	/**
	 * 从json中加载并解析内容
	 * 
	 * @param jsonObject
	 */
	public static String parse(String jsonString) {
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		return parseJson(jsonObject);
		

		// JSONOBJECT 格式
		/*
		 * {searchEngine='baidu',phoneNumber='18064129619',records:[ {
		 * title:'【顶创设计工作室】 - 网站建设 - 北京赶集网',content:'联系电话: 18064129619
		 * 联系我时,请说明在赶集服务看到的 商家地址: 北京-海淀-北大清华 北大附中 短信发送至手机 收藏该店铺 手机也能上赶集 短信分享 分享
		 * ...',domain:'bj.ganji.com' }
		 * 
		 * 
		 * ]}, guanjia:[ {type:'baidu',biaoji:'电话营销',biaojiCount:1000}
		 * 
		 * ]}
		 */

	}
}
