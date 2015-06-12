package cn.aolong.who.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.aolong.who.WhoApplication;

public class ConnectivityHelper {

	private static String TAG=ConnectivityHelper.class.getSimpleName();
	
	private static Method sMethodGetMobileDataEnabled;
	private static Method sMethodSetMobileDataEnabled;

	static {
		initReflectionMethod();
	}

	public static boolean isConnected() {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) WhoApplication
					.getInstance().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	private static void initReflectionMethod() {
		Class<ConnectivityManager> clazz = ConnectivityManager.class;
		try {
			sMethodGetMobileDataEnabled = clazz.getMethod(
					"getMobileDataEnabled", new Class[0]);
			sMethodGetMobileDataEnabled.setAccessible(true);
			sMethodSetMobileDataEnabled = clazz.getMethod(
					"setMobileDataEnabled", new Class[] { Boolean.TYPE });
			sMethodSetMobileDataEnabled.setAccessible(true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the value of the setting for enabling Mobile data.
	 * 
	 * @param manager
	 *            the {@link ConnectivityManager}
	 * 
	 * @return Whether mobile data is enabled.
	 */
	public static boolean getMobileDataEnabled(ConnectivityManager manager) {
		try {
			return (Boolean) sMethodGetMobileDataEnabled.invoke(manager,
					new Object[0]);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets the persisted value for enabling/disabling Mobile data.
	 * 
	 * @param manager
	 *            the {@link ConnectivityManager}
	 * 
	 * @param enabled
	 *            Whether the mobile data connection should be used or not.
	 */
	private static void setMobileDataEnabled(ConnectivityManager manager,
			boolean enabled) {
		try {
			sMethodSetMobileDataEnabled.invoke(manager,
					new Object[] { Boolean.valueOf(enabled) });
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setMobileEnable(boolean enable){
        ConnectivityManager manager =  (ConnectivityManager) WhoApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE); 
        setMobileDataEnabled(manager, enable);
        Log.d(TAG, "set moblie enabled =" +enable);
	}
	
	public static void setWIFIEnable(boolean enable){
        WifiManager      mWiFiManager = (WifiManager)
        		WhoApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
        mWiFiManager.setWifiEnabled(enable);
        Log.d(TAG, "set wifi enabled = "+ enable);
	}
	
	public static void toggleWIFIState(){
        WifiManager      mWiFiManager = (WifiManager)
        		WhoApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
        
        switch (mWiFiManager.getWifiState()) {
        case WifiManager.WIFI_STATE_DISABLED:
            mWiFiManager.setWifiEnabled(true);
            Log.d(TAG, "set wifi enabled");
            break;
        case WifiManager.WIFI_STATE_ENABLED:
            mWiFiManager.setWifiEnabled(false);
            Log.d(TAG, "set wifi disabled;");
        default:
            break;
        };
	}
	
}
