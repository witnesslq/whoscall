package cn.aolong.who.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import cn.aolong.who.WhoApplication;
import cn.aolong.who.call.PhoneResult;

/**
 * Created by osx on 15-6-12.
 */
public class FloatWindow {

    private static final FloatWindow mInstance=new FloatWindow();

    public static FloatWindow getInstance(){
        return mInstance;
    }

    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private TextView floatView;

    private String getFormattedUserInfo(PhoneResult phoneResult){
        StringBuilder builder=new StringBuilder();
        if(phoneResult!=null &&  phoneResult.getHasResult()){
            if(!TextUtils.isEmpty(phoneResult.getJigou())){
                builder.append("公司:"+phoneResult.getJigou()+"\n");
            }
            if(!TextUtils.isEmpty(phoneResult.getChenghu())){
                builder.append("姓名:"+phoneResult.getChenghu()+"\n");
            }
            if(!TextUtils.isEmpty(phoneResult.getHangye())){
                builder.append("行业:"+phoneResult.getHangye()+"\n");
            }
            if(!TextUtils.isEmpty(phoneResult.getAddress())){
                builder.append("地址:"+phoneResult.getAddress());
            }
        }else{
            builder.append("私人电话,未获取公开信息");
        }

        return builder.toString();
    }


    public void show(Context context, PhoneResult phoneResult) {
        closeFloatView();
        if(floatView==null){
            floatView = new TextView(context);
        }

        floatView.setText(getFormattedUserInfo(phoneResult));

        floatView.setBackgroundColor(Color.BLACK);
        floatView.setTextColor(Color.WHITE);

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity= Gravity.TOP;


//        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        params.format = PixelFormat.RGBA_8888;
//        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

    //    params.width = 50;
   //     params.height = 40;
        wm.addView(floatView, params);
    }

    public void closeFloatView(){
        if(floatView!=null && floatView.getParent()!=null){
            wm.removeViewImmediate(floatView);
            floatView=null;
        }
    }

}
