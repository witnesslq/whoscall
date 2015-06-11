package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.tianlupan.whoscall.NTUtil;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.model.Similarity;

/**
 * 处理搜索及挖掘结果核心类
 * @author laotian
 *
 */
public class SearchResultParser {
	
	
	Hashtable<String, Integer> ntList = new Hashtable<String, Integer>();
	Hashtable<String, Integer> hangyeList = new Hashtable<String, Integer>();
	Hashtable<String, Integer> chenghuList = new Hashtable<String, Integer>();
	
	private final int method;
	
	public static final int SCORE_TITLE_ADD=2;
	
	public static final int SCORE_NT_DEFAULT=5;
	public static final int SCORE_NT_MAX=15;	
	public static final int SCORE_NR_DEFAULT=5;	
	public static final int SCORE_ND_DEFAULT=5;
	public static final int SCORE_ND_MAX=10;
	
	public static final int SCORE_SIMILITY=6;
	
	//每重复一次加分数量
	public static final int SCORE_REPEATE=5;
	
	//最多返回几个行业
	private final static int MAX_HANGYE_COUNT = 4;
	
	//如果属于挖掘结果，默认分数
	private final static int SCORE_MINE_RESULT=20;
	
	//解析后的最终结果
	private PhoneResult phoneResult=new PhoneResult();

	
	public SearchResultParser(int method)
	{
		this.method=method;
	}
	

	
	private void parseItem(SearchResultItem item)
	{
		if(item.getMineResult()!=null)
		{
			parseMineResult(item.getMineResult());
		}
		else {
			String title=item.getTitle();
			String content=item.getContent();
			parseContent(title, true);		
			parseContent(content, false);
			if(!TextUtils.isEmpty(item.getAddress()))
				setAddress(item.getAddress());
		}

	}
	
	
	private void addMineScore(Hashtable<String, Integer> map,String key )
	{
		if(TextUtils.isEmpty(key)) return;
		else {
			if(map.containsKey(key))
			{
				int orgianlScore=map.get(key);
				map.put(key, orgianlScore+SCORE_REPEATE);
			}
			else {
				map.put(key, SCORE_MINE_RESULT);
			}
		}
		
		
	}
	
	/**
	 * 设置地址，现在只是简单的比较长度
	 */
	private void setAddress(String newAddress)
	{
		if(!TextUtils.isEmpty(newAddress))
		{
			if(TextUtils.isEmpty(phoneResult.getAddress()) || phoneResult.getAddress().length()<newAddress.length() )
			phoneResult.setAddress(newAddress.trim());
		}
	}
	
	private void parseMineResult(PhoneResult mPhoneResult)
	{
		if(mPhoneResult==null || !mPhoneResult.isFound()) return;
		
		String chenghu=mPhoneResult.getChenghu();

		String jigou=mPhoneResult.getJigou();
		addMineScore(ntList,jigou);
		
		if(mPhoneResult.getHangyeList()!=null && mPhoneResult.getHangyeList().size()>0)
		{
			for(String hangye:mPhoneResult.getHangyeList())
				addMineScore(hangyeList, hangye);
		}
		
		addMineScore(chenghuList, chenghu);
		
		//TODO 现在只是简单的用挖出的图片替换原始的
		if(!TextUtils.isEmpty(mPhoneResult.getImage()))
		{
			
		}
		
		//TODO 现在只是简单的比较长度
		if(!TextUtils.isEmpty(mPhoneResult.getAddress()))
		{
			setAddress(mPhoneResult.getAddress());
		}
		
		/*
		 * 最好替换为下面的方法
		computeScore(terms, "nt", ntList);
		computeScore(terms, "nd", hangyeList);		
		computeScore(terms, "nr", chenghuList);	
		*/
	}
	

	
	
	/**
	 * 解析内容并给个初始分值
	 * @param content
	 * @param isTitle
	 */
	private void parseContent(String content,boolean isTitle)
	{
	//此处处理请求，解析字符串
		//List<Term> terms =  NlpAnalysis.parse(content);
	 //测试用新的		
		List<Term> terms= method==1 ?    NlpAnalysis.parse(content) : ToAnalysis.parse(content);
		//修正或合并，关键点
		NTUtil.recognizeNT(terms, method);
		computeScore(terms, "nt", ntList);
		computeScore(terms, "nd", hangyeList);		
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
			if(termName.length()>=2 )
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
	
	
	private static List<String> getMaxHangeye(List<TableItem> items) {
		List<String> hanyeList = new ArrayList<String>();

		int lastValue=-1;
		//只保留value值 大于前一个value的1/2
		for (int i = 0; i < items.size(); i++) {
			String key=items.get(i).getKey();
			int value=items.get(i).getValue();
			if(value>(lastValue * 0.5))
			{
				lastValue=value;
				hanyeList.add(key);
			}
		}
		

		
		
		boolean found=false;
		do{
			found=false;
			//删除相互包含量的行业； 如"logo设计"包含于"logo,vi设计"
			int totalLength=hanyeList.size();
			LOOP:
			for(int i=0;i<totalLength;i++)
			{
				String hangyeItem=hanyeList.get(i);
				for(int j=0;j<totalLength && j!=i ;j++)
				{
					String compareItem=hanyeList.get(j);
					if(!TextUtils.isEmpty(compareItem) &&  (TextUtils.isEmpty(hangyeItem)  ||  compareItem.equals(hangyeItem) || compareItem.contains(hangyeItem)))
					{
						System.out.println("删除行业，原行业: \""+ hangyeItem+"\" 包含于 \""+compareItem+"\"");
						hanyeList.remove(i);
						found=true;
						break LOOP;
					}
				}
			}
		}while(found);
		
		return hanyeList;
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
	
	private static String getMax(Hashtable<String, Integer> table) {
		List<TableItem> list = getMax(table, 1);
		MyStaticValue.Log4j.debug("get Max list="+list);
		return list.size() > 0 ? list.get(0).getKey() : null;
	}
	
	public PhoneResult getResult(){
		return phoneResult;
	}
	
	
	
	public void parse(SearchResult searchResult)
	{
		
		// 之后返回分值最大的 机构名，行业，人名
		phoneResult=new PhoneResult();
		
		if (searchResult.getSearchItems().size()>0) {

			for (int i = 0; i <searchResult.getSearchItems().size(); i++) {
				SearchResultItem item=searchResult.getSearchItems().get(i);
				parseItem(item);
			}

			// 解析比较出NTDetail
			// 比较相似的记录，如果存在则互相加分
			String nt = getMax(ntList);

			List<String> hList = getMaxHangeye(getMax(hangyeList,
					MAX_HANGYE_COUNT));

			String chenghu = getMax(chenghuList);

			phoneResult.setJigou(nt);
			phoneResult.setHangyeList(hList);
			phoneResult.setChenghu(chenghu);
			

		}
		
		// 解析管家信息
		if (searchResult.getTags()!=null && searchResult.getTags().size()>0) {
			
			Collections.sort(searchResult.getTags(),new Comparator<SearchResultUserTagItem>(){

				@Override
				public int compare(SearchResultUserTagItem tag1,
						SearchResultUserTagItem tag2) {
					//TODO 需要测试
					return tag1.getCount()>tag2.getCount() ? 1 : (tag1.getCount()<tag2.getCount() ? -1 : 0);
				}
			});
			
			phoneResult.setUserTag(searchResult.getTags().get(0));

		}
		
	//设置图片
		//TODO 更改算法
		Iterator<SearchResultItem> iterator=  searchResult.getSearchItems().iterator();
		while(iterator.hasNext())
		{
			SearchResultItem item=iterator.next();
			if(item.getMineResult()!=null &&!TextUtils.isEmpty(item.getMineResult().getImage()))
			{
				phoneResult.setImage(item.getMineResult().getImage());
				break;
			}
		}
		
	}
	
	

}
