package cn.aolong.who.preference;

import android.text.TextUtils;
import cn.aolong.who.WhoApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class Preference {
	   private SharedPreferences preferences=null;
		private static final String preference_auto_network="auto_network";
		private static final String preference_remote_api_url="api_url";
		private static final String preference_show_on_call="show_on_call";
		
		private static String TAG=Preference.class.getSimpleName();

	/**
	 * 测试用的电话查询APi网址<br />
	 * 请下载源码后部署到自己的服务器
	 */
	private static final String DEMO_API_URL="http://logomaker.com.cn:8080/api.action?phone=";
		
	private static Preference mInstance=null;
	
	public Preference(){
		preferences=WhoApplication.getInstance().getSharedPreferences("who", Context.MODE_PRIVATE);
		//这里采用默认的地址
		if(TextUtils.isEmpty(remoteURL())){
			remoteURL(DEMO_API_URL);
			//默认不启用自动联网
			autoNetwork(false);
			showOnCall(true);
		}
	}

	public boolean showOnCall(){
		return preferences.getBoolean(preference_show_on_call, true);
	}


	public void showOnCall(Boolean _showOnCall){
	   putBoolean(preference_show_on_call, _showOnCall);
	}


	public synchronized  static Preference getInstance(){
		if(mInstance==null)
		{
			if(WhoApplication.getInstance()==null)
			{
				Log.e(TAG,"getInstance is null");
			}
			mInstance=new Preference();
		}
		return mInstance;
	}
	
	public boolean autoNetwork(){
		return preferences.getBoolean(preference_auto_network, false);
	}
	
	public void autoNetwork(boolean value)
	{
		putBoolean(preference_auto_network, value);
	}
	
	private void putBoolean(String setting,boolean value)
	{
		Editor editor=preferences.edit();
		editor.putBoolean(setting, value);
		editor.commit();
	}
	
	private void putString(String setting,String value)
	{
		Editor editor=preferences.edit();
		editor.putString(setting, value);
		editor.commit();
	}


	public String remoteURL(){
		return preferences.getString(preference_remote_api_url, "");
	}
	
	public void remoteURL(String url)
	{
		putString(preference_remote_api_url,url);
	}

	
}
