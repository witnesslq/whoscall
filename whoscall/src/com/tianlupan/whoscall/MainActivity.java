package com.tianlupan.whoscall;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.tianlupan.call.PhoneResult;
import com.tianlupan.call.RemoteAPI;
import com.tianlupan.call.RemoteAPICallback;
import com.tianlupan.util.Utils;

public class MainActivity extends Activity {
    CheckBox chbOnCall;

    Button btnSearch;

    EditText txtPhone;

    private static final String TAG=MainActivity.class.getSimpleName();


    public void collapseSoftInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView()
                    .getWindowToken(), 0);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
    }

    private void searchPhone(){
        Boolean isConnected = Utils.isConnected(MainActivity.this);

        if (TextUtils.isEmpty(txtPhone.getText().toString())) {
            Toast.makeText(MainActivity.this, "������Ҫ��ѯ�ĺ���",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (isConnected) {
            collapseSoftInputMethod();
            String number=txtPhone.getText().toString();
            Log.i(TAG, "getNumber=" + number);

            RemoteAPI.getPhone(number, new RemoteAPICallback() {

                @Override
                public void onResult(final PhoneResult phoneResult) {
                    Log.i(TAG, "onResult" + phoneResult.toString());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            collapseSoftInputMethod();
                            FloatWindow.getInstance().show(MainActivity.this, new PhoneResult(phoneResult.toString()), 20);
                        }
                    });
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "onFinished");
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "���������ٲ���",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {

        txtPhone = (EditText) findViewById(R.id.txtPhone);
        btnSearch = (Button) findViewById(R.id.btnSearch);

        chbOnCall=(CheckBox) findViewById(R.id.chbOnCall);
        //�˰���Բ���ʾ����
        chbOnCall.setChecked(Utils.getShowOnCall(this));
        chbOnCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Utils.setShowOnCall(MainActivity.this, checked);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchPhone();
            }
        });

        txtPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean done=false;
                switch(actionId){
                    case EditorInfo.IME_ACTION_SEARCH:
                        done=true;
                        searchPhone();
                        break;
                    }
                    return done;
            }
        });

    }

}
