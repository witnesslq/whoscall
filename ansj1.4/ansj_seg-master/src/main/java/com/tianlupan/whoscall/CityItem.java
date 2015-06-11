package com.tianlupan.whoscall;

public class CityItem {
	public String provinceName;
	public String cityName;
	public String areaCode;
	@Override
	public String toString() {
		return "city:"+cityName+", province:"+provinceName+", areaCode:"+areaCode;
	}
}
