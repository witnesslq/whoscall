package com.tianlupan.whoscall.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class IOUtils {
	
	public static String getRedirectLocation(String link) {
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

}
