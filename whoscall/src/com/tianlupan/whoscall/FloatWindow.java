package com.tianlupan.whoscall;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.tianlupan.call.PhoneResult;

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
    private View floatView;

    private static final int MESSAGE_CLEAR=0x1;

    /**
     * 默认关闭时间,10秒
     */
    private static final int DELAY_CLOSE=10;

    private Handler mHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_CLEAR:
                    closeFloatView();
                    break;
                default:
                    break;
            }

        }
    };

    private void setTextContent(int id,String content){
        if(floatView!=null) {
            TextView textView = (TextView) floatView.findViewById(id);
            if (TextUtils.isEmpty(content)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(content);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setImage(int id,String imgURL){
        if(floatView!=null){
                ImageView imageView=(ImageView)floatView.findViewById(id);
            if(TextUtils.isEmpty(imgURL)){
                imageView.setVisibility(View.GONE);
            }else{
                imageView.setImageURI(Uri.parse(imgURL));
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void show(Context context,PhoneResult phoneResult){
        show(context,phoneResult,DELAY_CLOSE);
    }

    private void showFound(boolean found){
        if(floatView!=null){
            TextView noFoundTextView= (TextView) floatView.findViewById(R.id.onCallNotFound);
            noFoundTextView.setVisibility(found ? View.GONE : View.VISIBLE);
        }
    }


    public void show(Context context, PhoneResult phoneResult,int seconds) {
        closeFloatView();
        if(floatView==null){
            floatView = View.inflate(context, R.layout.oncall,null);
        }

        if(phoneResult.getHasResult()){
            setTextContent(R.id.onCallChenghu,phoneResult.getChenghu());
            setTextContent(R.id.onCallJigou,phoneResult.getJigou());
            setTextContent(R.id.onCallHangye,phoneResult.getHangye());
            setTextContent(R.id.onCallAddress, phoneResult.getAddress());
            showFound(true);
        }else{
            setTextContent(R.id.onCallChenghu,null);
            setTextContent(R.id.onCallJigou,null);
            setTextContent(R.id.onCallHangye,null);
            setTextContent(R.id.onCallAddress, null);
            showFound(false);
        }


        setImage(R.id.onCallImage, phoneResult.getImageURL());

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity= Gravity.TOP;
        wm.addView(floatView, params);

        mHandler.obtainMessage(MESSAGE_CLEAR);
        mHandler.sendEmptyMessageDelayed(MESSAGE_CLEAR,seconds*1000);
    }

    public void closeFloatView(){
        mHandler.removeCallbacksAndMessages(null);
        if(floatView!=null && floatView.getParent()!=null){
            wm.removeViewImmediate(floatView);
            floatView=null;
        }
    }

}
