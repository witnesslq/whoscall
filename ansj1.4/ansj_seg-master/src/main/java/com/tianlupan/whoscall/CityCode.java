package com.tianlupan.whoscall;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.util.IOUtil;

public class CityCode {
	
	private static String CITY_AREA_CODE_FILE="library/city/CityFixedCode.txt";
	
   private static HashMap<String, CityItem> cityMap=new HashMap<String, CityItem>();
	
   private static Pattern PATTERN_CITY_AREA=Pattern.compile("^([\u4e00-\u9fa5]+)\\s+(\\d+)\\s*$");
   
   
	static{
		loadFixedCode();
	}
	
	private static void loadFixedCode(){
		final String TAG_PROVINCE="#";
		BufferedReader reader=null;
		String line = null;
		//用#做注释
		try {
			//默认目录是/library/nt
			reader=IOUtil.getReader(new FileInputStream(CITY_AREA_CODE_FILE), "UTF-8");
			String province="省";
			while ((line = reader.readLine()) != null) {
				if(TextUtils.isEmpty(line)) continue;				
				else if(line.startsWith(TAG_PROVINCE))
				{
					province=line.substring(TAG_PROVINCE.length()).trim();
				}
				else {
					if(TextUtils.isMatchReg(PATTERN_CITY_AREA, line))
					{
						CityItem item=new CityItem();
						item.provinceName=province;
						item.cityName=TextUtils.getMatchGroup(PATTERN_CITY_AREA, line,1);
						item.areaCode=TextUtils.getMatchGroup(PATTERN_CITY_AREA, line,2);
						cityMap.put(item.cityName, item);
					}
				}
			}
		} catch (IOException ex) {
			MyStaticValue.Log4j.error("Init area code dictionary failed, dic=" + CITY_AREA_CODE_FILE);
		} finally {
			try {
				if(reader!=null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<CityItem> getCityListByCode(String code)
	{
		List<CityItem> list=new ArrayList<CityItem>();
		Iterator<Entry<String, CityItem>> iterator =cityMap.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, CityItem> entry=iterator.next();
			if(entry.getValue().areaCode.equalsIgnoreCase(code))
				list.add(cityMap.get(entry.getKey()));
		}
		return list;
	}
	

	public static void main(String[] args) {
		System.out.println(getCityListByCode("0898"));
	}
	
	


}
