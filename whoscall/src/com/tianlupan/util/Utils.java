package com.tianlupan.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.Preference;
import android.util.Log;

/**
 * Created by laotian on 2015/6/14.
 */
public class Utils {

    private static final String preference_show_on_call="show_on_call";
    private static final String preference_xml="who";
    private SharedPreferences preferences=null;

    public static final String preference_api_url="http://123.57.143.40:8080/api.action?phone=";

    private static SharedPreferences getPreference(Context context){
        return context.getSharedPreferences(preference_xml, Context.MODE_PRIVATE);
    }

    public static void setShowOnCall(Context context, Boolean _showOnCall){
        SharedPreferences preferences=getPreference(context);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(preference_show_on_call , _showOnCall);
        editor.commit();
    }

    public static boolean getShowOnCall(Context context){
        SharedPreferences preferences=getPreference(context);
        return preferences.getBoolean(preference_show_on_call,true);
    }


    /**
     * 获取连接状态
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(
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
}
