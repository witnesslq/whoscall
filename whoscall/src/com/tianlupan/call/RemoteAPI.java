package com.tianlupan.call;

import android.util.Log;
import com.tianlupan.util.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class RemoteAPI {

	private final static String TAG = RemoteAPI.class.getSimpleName();

	public static void getPhone(String phoneNumber, RemoteAPICallback callback) {
		new GetPhoneThread(phoneNumber, callback).start();
	}

	private static class GetPhoneThread extends Thread {
		

		private String phoneNumber;
		private RemoteAPICallback callback;

		public GetPhoneThread(String phoneNumber, RemoteAPICallback callback) {
			this.phoneNumber = phoneNumber;
			this.callback = callback;
		}

		public void run() {

			try {
				URL url = new URL(Utils.preference_api_url+phoneNumber);
				URLConnection urlConnection = url.openConnection();

				urlConnection.setDoOutput(false);
				urlConnection.setDoInput(true);
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream(),
								Charset.forName("utf-8")));

				String line = null;
				while ((line = reader.readLine()) != null) {
					Log.d(TAG, "收到json=" + line);
					callback.onResult(new PhoneResult(line));
				}
				callback.onFinish();
			} catch (IOException exception) {
				Log.e(TAG, "GetPhoneThread 发生错误=" + exception);
			}

		}
	}

}
