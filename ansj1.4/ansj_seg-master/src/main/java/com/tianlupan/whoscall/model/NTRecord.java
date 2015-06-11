package com.tianlupan.whoscall.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.tianlupan.whoscall.NTUtil;
import net.sf.json.JSONObject;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import com.tianlupan.whoscall.TextUtils;

public class NTRecord {
	
	public static final String JSON_RECORD_TITLE="title";
	public static final String JSON_RECORD_CONTENT="content";
	public static final String JSON_RECORD_DOMAIN="domain";
	
	public static final int SCORE_TITLE_ADD=2;
	
	public static final int SCORE_NT_DEFAULT=5;
	public static final int SCORE_NT_MAX=15;
	
	public static final int SCORE_NR_DEFAULT=5;
	
	public static final int SCORE_ND_DEFAULT=5;
	public static final int SCORE_ND_MAX=10;
	
	public static final int SCORE_SIMILITY=6;
	
	//每重复一次加分数量
	public static final int SCORE_REPEATE=5;
	
	//机构按照长度加分，初始分数最大值
    //public static final int SCORE_NT_LENGTH_MAX=10;
	
	private Hashtable<String,Integer> ntList;	
	private Hashtable<String,Integer> hanyeList;	
	private Hashtable<String,Integer> chenghuList;	

	
	private final static List<String> badDomainList=new ArrayList<String>();
	
	private final static List<String> badTitleList=new ArrayList<String>();
	
	
	static{
		badDomainList.add("sohao.org");
		badDomainList.add("yenwoop.com");
		badDomainList.add("ekaing.com");
		badDomainList.add("beyaiw.com");
		badDomainList.add("heagei.com");
		badDomainList.add("jihaoba.com");
		badDomainList.add("xysjk.com");
		badDomainList.add("chahaoba.com");
		badDomainList.add("jx.ip138.com");
		
		badTitleList.add("所有号码");
		badTitleList.add("固定电话号码");
		badTitleList.add("xxxx");
		badTitleList.add("查询号码");
		//badTitleList.add("号码");
		badTitleList.add("手机号码");
		badTitleList.add("号段");
		badTitleList.add("哪里");
		badTitleList.add("什么手机号码");
		badTitleList.add("查询电话号码");
		badTitleList.add("查询号码");
		badTitleList.add("什么号码");
		badTitleList.add("哪里的");
		badTitleList.add("手机号码列表");
		badTitleList.add("号段手机号码");
		badTitleList.add("归属地");
		badTitleList.add("归属");
		badTitleList.add("吉凶");
		badTitleList.add("吉详号");
		badTitleList.add("选号");
		
	}
	
	public NTRecord(Hashtable<String,Integer> ntList,Hashtable<String,Integer> hanyeList, Hashtable<String,Integer> chenghuList)
	{
		this.ntList=ntList;
		this.hanyeList=hanyeList;
		this.chenghuList=chenghuList;		

	}
	
	/*
	 * 解析json
	 * { title:'【顶创设计工作室】 - 网站建设 - 北京赶集网',content:'联系电话: 18064129619 联系我时,请说明在赶集服务看到的 商家地址: 北京-海淀-北大清华 北大附中 短信发送至手机 收藏该店铺 手机也能上赶集 短信分享 分享 ...',domain:'bj.ganji.com' }
	 */
	public void doParse(JSONObject jsonObject,int method)
	{
			String title=jsonObject.getString(JSON_RECORD_TITLE);
			String content=jsonObject.getString(JSON_RECORD_CONTENT);
			String domain=jsonObject.getString(JSON_RECORD_DOMAIN);
			if(checkValid(title,content,domain))
			{
				parse(title, true,method);		
				parse(content, false,method);
			}
			else {
				System.err.println("不是有效的record:"+title);
			}

	}
	
	public static  boolean checkValid(String title,String content,String domain)
	{
		//先检查域名
		if(!TextUtils.isEmpty(domain))
		{
			String lowDomainString=domain.toLowerCase();
			for(String badDomain:badDomainList)
			{
				if(lowDomainString.contains(badDomain))
					return false;
			}
		}
		
		if(!TextUtils.isEmpty(title))
		{
			for(String badTitle:badTitleList)
			{
				if(title.contains(badTitle))
					return false;
			}
		}
		
		if(TextUtils.isEmpty(content)) return false;
		else {
			//如果全是数字则认为无效
			//比如010-85560024 010-85560025 010-85560026 (010)-85560027 010-85560028 010-85560029 ...
			String	newContent=content.replaceAll("[\\s*|\\.|\\-|\\(|\\)|\\d*]", "");
			if(TextUtils.isEmpty(newContent) || newContent.length()<10)
					return false;
		}
		
		return true;
	}
	
	
	/**
	 * 解析内容并给个初始分值
	 * @param content
	 * @param isTitle
	 */
	private void parse(String content,boolean isTitle,int method)
	{
		
		//此处处理请求，解析字符串
		//List<Term> terms =  NlpAnalysis.parse(content);
	 //测试用新的
		
		List<Term> terms= method==1 ?    NlpAnalysis.parse(content) : ToAnalysis.parse(content);
		//修正或合并，关键点
		NTUtil.recognizeNT(terms, method);
		
		
		computeScore(terms, "nt", ntList);
		
		computeScore(terms, "nd", hanyeList);
		
		computeScore(terms, "nr", chenghuList);		
		
	}
	
	private void addScore(Hashtable<String,Integer> table, String key,int addScore)
	{
		if(table.containsKey(key))
		{
			table.put(key, table.get(key)+addScore);
		}
	}
	
	private void computeScore(List<Term> terms, String nature,Hashtable<String,Integer> table)
	{
	
		
		HashSet<String> processedList=new HashSet<String>();
		
		for(Term term:terms)
		{
			if(term.getNatureStr().equals(nature))
			{
				String termName=term.getName();
				
				//不能在一个记录里重复核算多次同一内容，如  联合集团有限公司  主页 >联合集团有限公司  公司名称>联合集团有限公司
				if(processedList.contains(termName))
				{
					System.out.println("同一记录出现多次，跳 过="+termName);
					continue;
				}
				
				processedList.add(termName);
				
				int initScore=getInitScore(termName, nature);
				if(initScore<=0) continue;
				
				if(!table.containsKey(termName))
				{
	     			Iterator<Entry<String, Integer>> iterator=table.entrySet().iterator();
					int similarMaxScore=0;

					while(iterator.hasNext())
					{
						Entry<String, Integer> entry=iterator.next();
						
						int similarScore=getSimilarScore(entry.getKey(),termName,nature);
						
						if(similarScore>0)
						{
							MyStaticValue.Log4j.debug("term加分， term="+entry.getKey()+", 分数="+similarScore);
							addScore(table,entry.getKey(),similarScore);
							//找到最大相似度
							if(similarScore>similarMaxScore)
								similarMaxScore=similarScore;
							}
					}
					
					//设置为初始分值+相似度最大值
					MyStaticValue.Log4j.debug("添加新的term="+termName+", 类型;"+nature);
					table.put(termName,initScore+similarMaxScore);
				}
				else {
					//如果已经包括，则加重复分
					MyStaticValue.Log4j.debug("term包含 一模一样的， term="+termName+", 加分 ="+SCORE_REPEATE);
					addScore(table, termName, SCORE_REPEATE);
				}
			}
		}
		
	}
	
	/**
	 * 计算相似分，应该 修改此算法,使得oldTerm和newTerm分数不一定相同，
	 * @param oldTerm
	 * @param newTerm
	 * @return
	 */
	private int getSimilarScore(String oldTerm,String newTerm,String nature)
	{
		if(nature.equals("nr"))
		{
			//如果是 田鲁攀 vs 田经理 ，则认为是同一个
			if(oldTerm.substring(0,1).equals(newTerm.substring(0, 1)))
			{
				return SCORE_SIMILITY;
			}
			else return 0;
		}
		else {
			double score=  Similarity.SimilarDegree(oldTerm, newTerm);
			if(score<0.2) return 0;
			else {
				int similarScore=  (int)(score * SCORE_SIMILITY);
				return similarScore;
			}
		}
	}
	
	private int getInitScore(String  termName ,String nature)
	{
		//人名
		if(nature.equals("nr"))
		{
			if(termName.length()>=2 && termName.length()<=3)
			{
				return SCORE_NR_DEFAULT;
			}
		}
		else if(nature.equals("nt")){
			if(termName.length()>=2)
			{
				return   Math.min(termName.length(), SCORE_NT_MAX);
			}
		}
		else if(nature.equals("nd"))
		{
			if(termName.length()>=2)
			{
				return  Math.min(termName.length(), SCORE_ND_MAX);
			}
		}
		
		return 0;
		
	}
	

}
