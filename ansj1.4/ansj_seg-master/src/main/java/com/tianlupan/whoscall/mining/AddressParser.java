package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.TextUtils;

public class AddressParser {
	
	//地址匹配，屌炸天的复杂度;室和楼单独拉出来是防止把机构名匹配成地址，如武汉市洪山区标志工作室，武汉市婚妙影楼
	//如果后面有标注，如 705房间 （ＸＸ大酒店对面）　，括号内的内容也会匹配上
	private static final String REGEX_ADDRESS = "(([\u4e00-\u9fa5]{0,5}([\u4e00-\u9fa5\\w]{1,5}(省|市|区|镇|乡|小区|路|大道|大厦|广场|对面|旁边|房间|幢|内|层|([\\d一二三四五六七八九]+[楼|室|号])))+)\\d{0,5}((\\s{0,2}([\\(\\（][^\\s]{2,10}[\\)\\）]))|(ENDTAG))?)";
	//地址后面可以是 :   705房间 （ＸＸ大酒店对面）,或者跟着其它几个字符，如：地址:北京市海淀区清华科技...  清华科技即是要捕获的
	//private static final String REGEX_ADDRESS_END="((\\s{0,2}([\\(\\（][^\\s]{2,10}[\\)\\）]))|([^\\s\\.]{0,6}))?";
	
	private static final String ENDTAGNAME="ENDTAG";
	private static final String ENDTAGVALUE="[\u4e00-\u9fa5]{0,4}\\.+";
	private static  String REGEX_ADDRESS_PREFIX = "(搬迁至|公司地址|地址)\\s*[:：]?\\s*";
	
	
	private static  Pattern PATTERN_ADDRESS ;
	// 没有公司地址：等前缀，必须以空白符开始和结束
	private static final Pattern PATTERN_ADDRESS_NO_PREFIX ;
	
	static{
		PATTERN_ADDRESS = Pattern
				.compile(REGEX_ADDRESS_PREFIX + REGEX_ADDRESS.replace(ENDTAGNAME, ENDTAGVALUE));
		//如果不以地址等开头，则后面不能再加其它的，如北京市海淀区清华科技... 中的清华科技...
		PATTERN_ADDRESS_NO_PREFIX = Pattern
				.compile("\\s+" + REGEX_ADDRESS.replace(ENDTAGNAME, "") + "\\s+");
		}
	

	
	//匹配上，但是属于错误的地址符号
	private static  String[] BAD_ADDRESS_LIST={"专区","资讯社区"};
	
	public static List<String> getAddressList(String title, String content) {
		List<String> addressListTitle = getAddressList(title);
		List<String> addressListContent = getAddressList(content);

		return joinAddressList(addressListTitle, addressListContent);

	}

	private static List<String> joinAddressList(List<String> addressList1,
			List<String> addressList2) {

		List<String> list = new ArrayList<String>();
		for (String address : addressList1) {
			if (!list.contains(address))
				list.add(address);
		}

		for (String address : addressList2) {
			if (!list.contains(address))
				list.add(address);
		}

		return list;
	}
	
	private static void removeBadAddress(List<String> addressList)
	{
		if(addressList==null ||addressList.size()==0) return;
		Iterator<String> iterator=addressList.iterator();
		while(iterator.hasNext())
		{
			String address=iterator.next();
			for(int i=0;i<BAD_ADDRESS_LIST.length;i++)
			{
				if(address.contains(BAD_ADDRESS_LIST[i]))
					iterator.remove();
			}
		}
	}

	public static List<String> getAddressList(String content) {
		String contentText = StringUtil.rmHtmlTag(content);

		List<String> listAddress = TextUtils.getMatchGroupList(PATTERN_ADDRESS,
				contentText, 2);
		removeBadAddress(listAddress);
		List<String> listAddressNoPrefix = TextUtils.getMatchGroupList(
				PATTERN_ADDRESS_NO_PREFIX, contentText, 1);
		removeBadAddress(listAddressNoPrefix);
		return joinAddressList(listAddress, listAddressNoPrefix);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String content="欢迎来我公司，地址：武汉市洪山区特别要1...";
		System.out.println(getAddressList(content));
	}

}
