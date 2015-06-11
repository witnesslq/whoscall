package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.List;


import com.tianlupan.whoscall.TextUtils;

public class PhoneResult {
	
	
	public static final String JSON_JIGOU = "jigou";
	public static final String JSON_HANGYE = "hangye";
	public static final String JSON_USER_TAG = "userTag";
	public static final String JSON_CHENGHU = "chenghu";
	public static final String JSON_IMAGE_URL="image";
	public static final String JSON_ADDRESS="address";
	public static final String JSON_FOUND = "found";
	

	// 电话号码
	private String phone;
	// NT 机构名称
	private String jigou;
	// ND 行业列表
	private List<String> hangyeList=new ArrayList<String>();
	//用户称呼，如田经理
	private String chenghu;
	//搜索结果，各种助手标记
	private SearchResultUserTagItem userTag = null;
	//图片缩略图
	private String image;
	//地址
	private String address;


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getJigou() {
		return jigou;
	}

	public void setJigou(String jigou) {
		this.jigou = jigou;
	}

	public List<String> getHangyeList() {
		return hangyeList;
	}

	public void setHangyeList(List<String> hangyeList) {
		this.hangyeList = hangyeList;
	}

	public String getChenghu() {
		return chenghu;
	}

	public void setChenghu(String chenghu) {
		this.chenghu = chenghu;
	}

	public SearchResultUserTagItem getUserTag() {
		return userTag;
	}

	public void setUserTag(SearchResultUserTagItem userTag) {
		this.userTag = userTag;
	}
	
	public boolean isFound(){
		boolean found=false;
		if ((hangyeList.size() == 0 && TextUtils.isEmpty(chenghu)
				&& TextUtils.isEmpty(jigou) && userTag == null)) {
			found = false;
		} else {
			found = true;
		}
		return found;
	}

	

	private void appendBuilder(StringBuilder builder, String key, String value) {
		if (TextUtils.isEmpty(value))
			return;

		if (builder.length() > 0) {
			builder.append(", ");
		}

		builder.append( "" + key + ":");

		if (value.startsWith("{") || value.startsWith("[")  || value.equals("true") || value.equals("false")   ) {
			builder.append(value);
		} else {
			builder.append("\"" + value + "\"");
		}

	}

	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (userTag != null) {
			appendBuilder(builder, JSON_USER_TAG, userTag.toString());
		}

		appendBuilder(builder, JSON_JIGOU, jigou);

		if (hangyeList != null && hangyeList.size() > 0) {
			StringBuilder builderHanye = new StringBuilder();
			builderHanye.append("[");
			int index = 0;
			for (String hangye : hangyeList) {
				if (index > 0)
					builderHanye.append(",");
				builderHanye.append("\"" + hangye + "\"");
				index++;
			}
			builderHanye.append("]");
			appendBuilder(builder, JSON_HANGYE, builderHanye.toString());
		}

		appendBuilder(builder, JSON_CHENGHU, chenghu);
		
		appendBuilder(builder, JSON_IMAGE_URL, image);
		
		appendBuilder(builder, JSON_ADDRESS, address);

		//	// 信息有没有找到？是不是私人电话？false代表私人
		appendBuilder(builder, JSON_FOUND, isFound() + "");

		return "{" + builder.toString() + "}";

	}

	public static void main(String[] args) {
		PhoneResult phoneResult = new PhoneResult();

		// 电话号码
		phoneResult.setPhone("18064129619");

		// NT
		phoneResult.jigou = "武汉大学";
		// ND
		List<String> hangyeList = new ArrayList<String>();
		hangyeList.add("logo设计");
		hangyeList.add("公司形象设计");
		phoneResult.hangyeList = hangyeList;
		phoneResult.chenghu = "田经理";
		phoneResult.setImage("http://www.baidu.com/totoott.jpg");
		// 信息有没有找到？是不是私人电话？false代表私人
		SearchResultUserTagItem item=new SearchResultUserTagItem(SearchResultUserTagType.BAIDU, "骚扰电话", 1300);
		phoneResult.userTag=item;

		System.out.println(phoneResult);
	}

}
