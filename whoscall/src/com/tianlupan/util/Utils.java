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
     * ��ȡ����״̬
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        // ��ȡ�ֻ��������ӹ�����󣨰�����wi-fi,net�����ӵĹ���
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(
                            Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // ��ȡ�������ӹ���Ķ���
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // �жϵ�ǰ�����Ƿ��Ѿ�����
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
