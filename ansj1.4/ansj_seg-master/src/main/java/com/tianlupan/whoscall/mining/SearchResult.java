package com.tianlupan.whoscall.mining;

import com.tianlupan.whoscall.AnsjServlet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Steps:
 * SearchResult searchResult=new SearchResult(phone);
 * searchResult.loadFromSearchEngine();
 * 
 * 现在可以先返回客户端最初数据 
 * getPhoneResult()
 * 
 * 检查是否有挖掘可能
 * if(searchResult.isMinableDomain())
 * {
 * 		decodeBaiduLinks.decodeBaiduLinks();
 *     if(searchResult.isMinableUrl())
 *     {
 *     		 boolean hasResult=	searchResult.mine();
 *     		返回计算后的数据 
 *    		 if(hasResult)
 *        		 getPhoneResult()
 *     
 *     }
 * }
 * @author laotian
 *
 */
public class SearchResult {

	private List<SearchResultItem> searchItems = new ArrayList<SearchResultItem>();
	
	private List<SearchResultUserTagItem> tagItems=new ArrayList<SearchResultUserTagItem>();
	//同时挖四个
	private final static int MINE_POOL_SIZE=4;

	private String phone;
	
	private PhoneResult phoneResult;

	private SearchResult() {}

	public SearchResult(String phone) {
		this();
		this.phone = phone;
	}
	
	public  void loadFromSearchEngine(){
		 loadFromSearchEngine(new SearchEnginBaidu());
	}
	
	public  void loadFromSearchEngine(SearchEngine searchEngine)
	{
		searchEngine.doSearch(phone);
		searchItems = searchEngine.items;

		AnsjServlet.appendParseLog("searchItems:" + searchItems);
		
		tagItems=searchEngine.tags;
		computeResult();
	}
	
	private void computeResult(){
		//TODO 计算 
		SearchResultParser parser=new SearchResultParser(2);
		parser.parse(this);
		phoneResult=parser.getResult();
	}

	public List<SearchResultItem> getSearchItems() {
		return searchItems;
	}
	
	public List<SearchResultUserTagItem> getTags(){
		return tagItems;
	}

	public String getPhone() {
		return this.phone;
	}
	
	public PhoneResult getPhoneResult(){
		return phoneResult;
	}
	
	public boolean isMinableDomain(){
		for(SearchResultItem item:searchItems)
		{
			if(item.isMinableDomain())
				return true;
		}
		return false;
	}
	

	/**
	 * 开始数据挖掘,并发挖掘
	 * @return 如果得到了数据，则返回true,否则返回false
	 */
	public boolean mine(){

		if(!isMinableDomain())
			return false;
		
		
		ExecutorService executorService = Executors.newFixedThreadPool(MINE_POOL_SIZE);
		
		List<Future<Void>> list=new ArrayList<Future<Void>>();
		
		
		for(final SearchResultItem item:searchItems)
		{
			
			list.add(executorService.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					// TODO Auto-generated method stub
					item.mine();
					return null;
				}
			}));		

		}
		
		for(Future<Void> future:list)
		{
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		executorService.shutdownNow();
		
		boolean hasNew=false;
		for( SearchResultItem item:searchItems)
		{
				if(!hasNew && item.getMineResult()!=null)
					hasNew=true;
		}
		
		//TODO 处理 PhoneResult 并返回计算后的
		//处理 getPhoneResult
		if(hasNew)
		{
			System.out.println("hasNew");
			computeResult();
			return true;
		}
		
		return false;		
	}
	
	
	
	
	@Override
	public String toString() {
		return "{ phone:\""+phone+"\", items:"+getSearchItems()+", tags:"+getTags()+"}";
	}
	
	public static void main(String[] args)
	{
		SearchResult searchResult=new SearchResult("027-82753677");
		searchResult.loadFromSearchEngine();
		System.out.println(searchResult.toString());
	}

}
