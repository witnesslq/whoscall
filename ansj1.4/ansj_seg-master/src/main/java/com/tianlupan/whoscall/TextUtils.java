package com.tianlupan.whoscall;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {
	
	private final static String REGEX_CONTACT="联\\s*系\\s*人?\\s*[:：]\\s*([\u4e00-\u9fa5]{2,4})\\s*(<|\\s)";
	private final static Pattern PATTERN_CONTACT = Pattern.compile(REGEX_CONTACT);
	
	private final static String REGEX_ADDRESS="地址\\s*[:：]\\s*([^\\s|<|&]{6,40})";
	private final static Pattern PATTERN_ADDRESS = Pattern.compile(REGEX_ADDRESS);

    public static boolean isEmpty(String text) {
        return (text == null || text.length() == 0);
    }

    /**
     * 获取子字符串
     * 
     * @param content
     *            内容，如html源码
     * @param startTag
     *            开始标记，如<a href="
     * @param endTag
     *            结束标记，如 "">"
     * @param ignoreEmptySubString
     *            是否忽略空白的子字符串
     * @param getAll
     *            true为获取全部子字符串，false为第一个
     * @return 非null,但可能是empty的子符串队列
     */
    public static List<String> getList(String content, String startTag,
            String endTag, boolean ignoreEmptySubString, boolean getAll) {
        ArrayList<String> list = new ArrayList<String>();
        if (isEmpty(content) || isEmpty(startTag) || isEmpty(endTag))
            return list;
        int contentLength = content.length();
        int startTagLength = startTag.length();
        int endTagLength = endTag.length();
        int fromIndex = -1;
        do {
            fromIndex = content.indexOf(startTag, fromIndex);
            if (fromIndex < 0)
                break;

            fromIndex += startTagLength;
            if (fromIndex >= contentLength - 1)
                break;
            int toIndex = content.indexOf(endTag, fromIndex);
            if (toIndex < 0)
                break;
            String subString = content.substring(fromIndex, toIndex);
            if ((ignoreEmptySubString && !isEmpty(subString))
                    || !ignoreEmptySubString) {
                list.add(subString);
                // 如果只取第一个
                if (!getAll)
                    break;
            }
            fromIndex = toIndex + endTagLength;
            // 如果已经是最后了，退出循环
            if (fromIndex >= contentLength)
                break;

        } while (true);
        return list;
    }

    public static List<String> getList(String content, String startTag,
            String endTag) {
        return getList(content, startTag, endTag, true, true);
    }

    /**
     * 获得第一个匹配的子字符串
     * 
     * @return 如果未找到，返回null,否则返回子字符串
     */
    public static String getSubString(String content, String startTag,
            String endTag) {
        List<String> list = getList(content, startTag, endTag, true, false);
        if (list.size() == 1)
            return list.get(0);
        else {
            return null;
        }
    }
    
	public static boolean isNumber(String content)
	{
		if(TextUtils.isEmpty(content)) return false;
		Pattern p = Pattern.compile("^\\d{0,12}$");  
		Matcher m = p.matcher(content);  
		return m.matches();  
	}
	
	/**
	 * 检查字符串url是否符合正则表达式patter
	 */
	public static boolean isMatchReg(Pattern pattern,String url)
	{
		return (!TextUtils.isEmpty(url) && pattern.matcher(url).matches());
	}
	
	/**
	 * 检查字符串url是否符合正则表达式patter
	 */
	public static boolean isMathReg(String patternString,String url)
	{
		return isMatchReg(Pattern.compile(patternString), url);
	}
	
	/**
	 * 返回从Content中匹配pattern,并得到其中的group值
	 * @param pattern 例 REGEX_POST = "^(http://[a-zA-Z]{1,8}\\.58\\.com/(\\w+/)+\\d+x\\.shtml)(\\?.*)?$";
	 * @param group 从1起的整数
	 * @return
	 */
	public static String getMatchGroup(Pattern pattern,String content,int group)
	{
		if(TextUtils.isEmpty(content)) return null;
		Matcher matcher = pattern.matcher(content);
		while (matcher.find())
			return matcher.group(group);		
		return null;
	}
	
	public static String getMatchGroup(Pattern pattern,String content)
	{
		return getMatchGroup(pattern, content, 1);
	}
	
	
	
	/**
	 * 返回符合正则的所有组成员数列
	 */
	public static List<String> getMatches(Pattern pattern,String content)
	{
		List<String> list=new ArrayList<String>();
		if(TextUtils.isEmpty(content)) return null;
		Matcher matcher = pattern.matcher(content);
		while (matcher.find())
		{
			String item=matcher.group();		
			list.add(item);
		}
		return list;
	}
	
	public static List<String> getMatchGroupList(Pattern pattern,String content,int group)
	{
		List<String> list=new ArrayList<String>();
		if(TextUtils.isEmpty(content)) return list;
		Matcher matcher = pattern.matcher(content);
		while (matcher.find())
		{
			String item=matcher.group(group);		
			list.add(item);
		}
		return list;
	}
	
	
	/**
	 * 通用算 法，检索网页中的联系人:李经理
	 */
	public static String getContact(String html)
	{
		return getMatchGroup(PATTERN_CONTACT, html);
	}
	
	public static String getAddress(String html)
	{
		return getMatchGroup(PATTERN_ADDRESS, html);
	}
	
	/**
	 * 清除掉换 行符
	 * @param line
	 * @return
	 */
	public static String clearHuanhang(String line)
	{
		if(TextUtils.isEmpty(line)) return line;
	  String newContent=	line.replaceAll("\\n", "");
	  return newContent.trim();
	}
	
	/**
	 * 给联系人加上职位，如 周诚 (经纪人)
	 * @param chenghu
	 * @param job
	 * @return
	 */
	public static String appendJob(String chenghu,String job)
	{
		if(TextUtils.isEmpty(chenghu)) return null;
		if(TextUtils.isEmpty(job)) return chenghu;
		
		return chenghu+" ("+job+")";
	}
	
	
	
	

}
