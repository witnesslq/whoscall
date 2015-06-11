package com.tianlupan.whoscall.mining;

import java.util.ArrayList;
import java.util.List;

import com.tianlupan.whoscall.mining.domains.*;
import org.ansj.util.MyStaticValue;

public class MiningSites {

	private List<MinableDomain> domains = new ArrayList<MinableDomain>();
	

	public MiningSites() {		
		// TODO 有处理能力的挖掘机都添加到这里
		//列表网，百姓网，美团网
		domains.add(new Domain1688());
		domains.add(new Domain58());
		domains.add(new DomainGanji());
		domains.add(new DomainDianping());
		domains.add(new DomainAibang());
		domains.add(new DomainZhaopin());
		//糯米团购
		domains.add(new DomainNuomi());
		domains.add(new DomainTuan800());
		//搜房网经纪挖掘机
		domains.add(new DomainSouFun());
		domains.add(new DomainFang());
		//百度地图  TODO 后面添加MapBar地图
		domains.add(new DomainBaiduMap());
	}

	/**
	 * 百度搜索结果先检查主域名是不是有挖掘的可能
	 */
	public boolean isMinableDomain(SearchResultItem item) {

		for (MinableDomain domain : domains) {
			if (domain.isMinableDomain(item))
				return true;
		}
		MyStaticValue.Log4j.info("域名："+item.getDomain()+" 不可挖掘");
		return false;
	}

	/**
	 * 百度搜索结果先检查网页是不是有挖掘的可能<BR />
	 * 如果主域名可挖掘，先通过BaiduLink触密加密后的Link,<BR />
	 * 然后再检查URl是否有可能挖掘
	 */
	public boolean isMinableUrl(SearchResultItem item) {
		for (MinableDomain domain : domains) {
			if (domain.isMinableUrl(item))
				return true;
		}
		MyStaticValue.Log4j.info("网址："+item.getRealUrl()+" 不可挖掘");
		return false;
	}

	/**
	 * 挖掘
	 * @return 如果挖掘不出结果，返回null
	 */
	public PhoneResult mine(SearchResultItem item) {
		if (!isMinableUrl(item))
			return null;

		for (MinableDomain domain : domains) {
			if (domain.isMinableUrl(item)) {
				PhoneResult phoneResult=domain.mine(item);
				MyStaticValue.Log4j.info("挖掘机:"+ domain.getClass().getSimpleName()+ ", 挖掘结果: "+phoneResult +"");
				return  phoneResult;
			}
		}

		return null;

	}

}
