package com.tianlupan.call;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PhoneResult {
	
	private String json=null;
	
	public PhoneResult(String json)
	{
		this.json=json;
		parseJson();
	}



	private void parseJson(){
		try {
			JSONTokener jsonParser = new JSONTokener(json);
			// 此时还未读取任何json文本，直接读取就是一个JSONObject对象。
			// 如果此时的读取位置在"name" : 了，那么nextValue就是"yuanzhifei89"（String）
			JSONObject phoneResult = (JSONObject) jsonParser.nextValue();
			// 接下来的就是JSON对象的操作了

			if(phoneResult.has("jigou")){
				this.jigou=phoneResult.getString("jigou");
			}

			if(phoneResult.has("chenghu")){
				this.chenghu=phoneResult.getString("chenghu");
			}

			if(phoneResult.has("address")){
				this.address=phoneResult.getString("address");
			}

			if(phoneResult.has("hangye")){
				this.hangye=phoneResult.getJSONArray("hangye").join(",");
			}

			if(phoneResult.has("image")){
				this.imageURL=phoneResult.getString("image");
			}

			if(phoneResult.has("found")){
				this.hasResult=phoneResult.getBoolean("found");
			}else{
				this.hasResult=false;
			}


		} catch (JSONException ex) {
			// 异常处理代码
			ex.printStackTrace();
		}
	}

	private String address;
	public String getAddress() {
		return address;
	}

	private String phoneNumber;
	
	private String hangye;
	
	private String jigou;
	
	
	private String chenghu;
	
	private String imageURL;
	
	private boolean hasResult;

	public String getHangye() {
		return hangye;
	}


	public String getJigou() {
		return jigou;
	}


	public String getChenghu() {
		return chenghu;
	}


	public String getImageURL() {
		return imageURL;
	}

	public boolean getHasResult()
	{
		return hasResult;
	}

	@Override
	public String toString() {
		return json;
	}
	
	
}
