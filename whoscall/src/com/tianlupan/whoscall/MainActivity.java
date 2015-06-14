package com.tianlupan.whoscall;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

    TextView txtResult;

    private static final String TAG=MainActivity.class.getSimpleName();

    private void getNumber(String number) {

        Log.i(TAG, "getNumber=" + number);

        RemoteAPI.getPhone(number, new RemoteAPICallback() {

            @Override
            public void onResult(final PhoneResult phoneResult) {
                Log.i(TAG, "onResult" + phoneResult.toString());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        collapseSoftInputMethod();
                        FloatWindow.getInstance().show(MainActivity.this, new PhoneResult(phoneResult.toString()),20);
                    }
                });
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "onFinished");
            }
        });
    }

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

    private void initView() {

        txtPhone = (EditText) findViewById(R.id.txtPhone);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        txtResult = (TextView) findViewById(R.id.txtResult);

        chbOnCall=(CheckBox) findViewById(R.id.chbOnCall);
        //此版测试不显示设置
        chbOnCall.setChecked(Utils.getShowOnCall(this));
        chbOnCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Utils.setShowOnCall(MainActivity.this,checked);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Boolean isConnected = Utils.isConnected(MainActivity.this);

                if (TextUtils.isEmpty(txtPhone.getText().toString())) {
                    Toast.makeText(MainActivity.this, "设置你要查询的号码",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (isConnected) {
                    collapseSoftInputMethod();
                    getNumber(txtPhone.getText().toString());
                 } else {
                    Toast.makeText(MainActivity.this, "先连上网再测试",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
