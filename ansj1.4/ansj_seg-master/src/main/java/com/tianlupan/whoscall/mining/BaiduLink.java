package com.tianlupan.whoscall.mining;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaiduLink {
	public static String getRealURL(String link) {
		// 10秒超时
		final int TIMEOUT = 10000;
		try {
			URL url = new URL(link);

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.setUseCaches(false);
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.connect();

			if (connection.getHeaderFields().containsKey("Location")) {
				String location = connection.getHeaderField("Location");
				return location;
			} else {
				return null;
			}
		} catch (IOException ex) {
			return null;
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println(getRealURL("http://www.baidu.com/link?url=J_0mXfu2uHKE-w0NQi9roI-NBl9Tlp9CpQptme7G4y_t7SBqMIqoqM-dfCcvbRpc"));
		
	}
	
	
}
