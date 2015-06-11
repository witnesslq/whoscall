package com.tianlupan.whoscall.mining.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.tianlupan.whoscall.PhoneNumber;
import com.tianlupan.whoscall.TextUtils;
import com.tianlupan.whoscall.mining.PhoneResult;
import com.tianlupan.whoscall.mining.SearchResultItem;

public class DomainNuomi extends MinableDomain {

	private final static String REGEX_DOMAIN = "^[a-zA-Z]{1,8}\\.nuomi\\.com$";

	private final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

	private final static String REGEX_DEAL = "^http://[a-zA-Z]{1,8}\\.nuomi\\.com/deal/[a-zA-Z0-9]+\\.html\\.*$";
	private final static Pattern PATTERN_DEAL = Pattern.compile(REGEX_DEAL);

	private final static String TAG_BEGIN_LOCATION = "<ul class=\"locations_list\">";
	private final static String TAG_END_LOCATION = "</ul>";

	private static class NuoMiAddress {
		public String dianMing;
		public String phone;
		public String address;
		
		@Override
		public String toString() {
			return "{dianMing:\""+dianMing+"\", address:\""+address+"\", phone:\""+phone+"\" }";
		}

	}

	@Override
	public PhoneResult mine(SearchResultItem item) {
		
		PhoneResult phoneResult=new PhoneResult();

		// 当前是处理 REGEX_DEAL
		List<NuoMiAddress> addresses = new ArrayList<DomainNuomi.NuoMiAddress>();

		String html = getHtml(item);
		
		if(TextUtils.isEmpty(html)) return null;

		String locations_list = TextUtils.getSubString(html,
				TAG_BEGIN_LOCATION, TAG_END_LOCATION);

		if (!TextUtils.isEmpty(locations_list)) {
			final String TAG_BEGIN_LI = "<li";
			final String TAG_END_LI = "</li>";
			List<String> li_list = TextUtils.getList(locations_list,
					TAG_BEGIN_LI, TAG_END_LI);

			for (String li : li_list) {
				NuoMiAddress nuoMiAddress = new NuoMiAddress();
				
				String h4Content=TextUtils.getSubString(li, "<h4>", "</h4>");
				if(!TextUtils.isEmpty(h4Content))
				{
					final String TAG_BEGIN_DIANMING="<a title=\"";
					final String TAG_END_DIANMING="\"";
					
					
					if(h4Content.contains(TAG_BEGIN_DIANMING))
					{
						nuoMiAddress.dianMing = TextUtils.getSubString(li,
								TAG_BEGIN_DIANMING,TAG_END_DIANMING);
					}
					else {
						nuoMiAddress.dianMing=h4Content.trim();
					}
				}
				
				
				nuoMiAddress.phone = TextUtils.getSubString(li, "电话：", "</p>");
				nuoMiAddress.address = TextUtils.getSubString(li,
						"title=\"地址：", "\"");
				addresses.add(nuoMiAddress);
				
				System.out.println(nuoMiAddress);
				
				//这就是和当前号码一致的 TODO 如果没有027怎么办？还得加上
				if(!TextUtils.isEmpty(nuoMiAddress.phone)  && new PhoneNumber(item.getPhone()).equals(new PhoneNumber(nuoMiAddress.phone))    )
				{
					phoneResult.setAddress(nuoMiAddress.address);
					phoneResult.setJigou(nuoMiAddress.dianMing);
				}
			}
		}
		
		final String TAG_BEGIN_ADDGOODS="_mvq.push(['$addGoods',";
		final String TAG_END_ADDGOODS=" _mvq.push(['$logData']);";
		
		String addGood=TextUtils.getSubString(html, TAG_BEGIN_ADDGOODS, TAG_END_ADDGOODS);
		if(!TextUtils.isEmpty(addGood))
		{
			List<String> addGoodList=TextUtils.getList(addGood, "'", "'",false,true);
			if(addGoodList.size()==12)
			{
				String url=addGoodList.get(5);
				String hangye=addGoodList.get(6);
				if(!TextUtils.isEmpty(url) && url.startsWith("http://"))
				{
					phoneResult.setImage(url);
				}
				
				if(!TextUtils.isEmpty(hangye))
				{
				    List<String> list=new ArrayList<String>();
				    list.add(hangye);
					phoneResult.setHangyeList(list);
				}
			}
		
		}

		return phoneResult;
	}

	@Override
	public boolean isMinableDomain(SearchResultItem item) {
		String domain = item.getDomain();
		if (!TextUtils.isEmpty(domain)
				&& PATTERN_DOMAIN.matcher(domain).matches())
			return true;

		return false;
	}

	@Override
	public boolean isMinableUrl(SearchResultItem item) {
		String url = item.getRealUrl();
		if (!TextUtils.isEmpty(url) && PATTERN_DEAL.matcher(url).matches())
			return true;

		return false;
	}

	public static void main(String[] args) {
		SearchResultItem item = new SearchResultItem();
		item.setRealUrl("http://www.nuomi.com/deal/oknvrdat.html?rmd_id=00408_175479&p=015-8-cuff21zm");
		item.setPhone("027-87770311");
		DomainNuomi domainNuomi = new DomainNuomi();

		System.out.println(domainNuomi.mine(item));


	}

}
