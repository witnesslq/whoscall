package com.tianlupan.whoscall.model;

import java.io.Serializable;
import java.util.List;

import com.tianlupan.whoscall.TextUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NTDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//电话号码
	public String phone;
	
	//NT
	public String nt;
	//ND
	public List<String> hangyeList;
	public String chenghu;
	//信息有没有找到？是不是私人电话？false代表私人
	public boolean found=false;
	public String biaoji="";
	//私人，快递，电话营销；电话录音营销；诈骗等
	public String biaojiType="";
	//有多少人标记此电话
	public int biaojiCount=0;
	
	public String searchTitle;
	public String searchContent;
	
	private void jsonPut(JSONObject jsonObject,String keyName,String value)
	{
		if(!TextUtils.isEmpty(value))
			jsonObject.put(keyName, value);
	}
	
	public String toJson(){
		JSONObject jsonObject=new JSONObject();
		
		if(!TextUtils.isEmpty(biaoji))
		{
			jsonPut(jsonObject, "biaoji", biaoji);
			jsonPut(jsonObject,"biaojiType",biaojiType);
			jsonObject.put("biaojiCount", biaojiCount);
		}
		
		jsonPut(jsonObject,"jigou",nt);
		
		if(hangyeList!=null && hangyeList.size()>0)
		{
			JSONArray hangyeArray=new JSONArray();
			for(String hangye:hangyeList )
			{
				hangyeArray.add(hangye);
			}
			
			jsonObject.put("hangye", hangyeArray);
		}

		jsonPut(jsonObject,"chenghu",chenghu);
		jsonPut(jsonObject,"searchTitle",searchTitle);
		jsonPut(jsonObject,"searchContent",searchContent);

		if((hangyeList.size()==0  && TextUtils.isEmpty(chenghu) && TextUtils.isEmpty(nt) && TextUtils.isEmpty(biaojiType) ) )
		{
			found=false;
		}
		else {
			found=true;
		}
		
		jsonObject.put("found", found);
		
		return jsonObject.toString();
		
	}

}
