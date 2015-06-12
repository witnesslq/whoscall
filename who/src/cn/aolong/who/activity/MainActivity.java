package cn.aolong.who.activity;

import cn.aolong.who.R;
import cn.aolong.who.WhoApplication;
import cn.aolong.who.call.PhoneResult;
import cn.aolong.who.call.RemoteAPI;
import cn.aolong.who.call.RemoteAPICallback;
import cn.aolong.who.network.ConnectivityHelper;
import cn.aolong.who.preference.Preference;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	CheckBox chbAutoNetwork;
	EditText txtApiURL;

	Button btnOK;

	Button btnSearch;

	EditText txtPhone;

	TextView txtResult;

	boolean isWaitingConnect = false;
	String mWaitingSearchNumber = "";

	CheckBox chbOnCall;

	Button btnShowFloat;

	private boolean isFloatShown=false;

	private static final String TAG=MainActivity.class.getSimpleName();

	private void getNumber(String number) {

		Log.i("laotian", "getNumber=" + number);

		RemoteAPI.getPhone(number, new RemoteAPICallback() {

			@Override
			public void onResult(final PhoneResult phoneResult) {
				// TODO Auto-generated method stub
				Log.i("laotian", "onResult" + phoneResult.toString());
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						collapseSoftInputMethod();
						//txtResult.setText(phoneResult.toString());
						FloatWindow.getInstance().show(MainActivity.this,new PhoneResult(phoneResult.toString()));
					}
				});

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				Log.i("laotian", "onFinished");
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivy_main);
		initView();
	}

	public void collapseSoftInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView()
					.getWindowToken(), 0);
		}
	}

	private void initView() {

		final Preference preference = Preference.getInstance();
		if (WhoApplication.getInstance() == null) {
			Log.e("laotian", "getInstance is null");
		}

		chbAutoNetwork = (CheckBox) findViewById(R.id.chbAutoNetwork);
		txtApiURL = (EditText) findViewById(R.id.txtApiURL);
		txtPhone = (EditText) findViewById(R.id.txtPhone);
		txtResult = (TextView) findViewById(R.id.txtResult);
		btnOK = (Button) findViewById(R.id.btnOK);
		btnSearch = (Button) findViewById(R.id.btnSearch);

		btnShowFloat=(Button)findViewById(R.id.btnFloatWindow);

		btnShowFloat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(isFloatShown){
					FloatWindow.getInstance().closeFloatView();
					isFloatShown=false;
				}else{
					FloatWindow.getInstance().show(MainActivity.this, null);
					isFloatShown=true;
				}
			}
		});


		chbOnCall=(CheckBox) findViewById(R.id.chbOnCall);
		//此版测试不显示设置
		chbOnCall.setChecked(preference.showOnCall());
		chbOnCall.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				Log.d(TAG,"来电提醒="+b);
				preference.showOnCall(b);
			}
		});

		chbAutoNetwork.setChecked(preference.autoNetwork());
		chbAutoNetwork
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						preference.autoNetwork(isChecked);

					}
				});

		txtApiURL.setText(Preference.getInstance().remoteURL());

		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				preference.remoteURL(txtApiURL.getText().toString());
				Toast.makeText(MainActivity.this, "已保存", Toast.LENGTH_SHORT)
						.show();
			}
		});

		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				isWaitingConnect = false;
				mWaitingSearchNumber = "";

				Boolean isConnected = ConnectivityHelper.isConnected();

				if (TextUtils.isEmpty(txtPhone.getText().toString())) {
					Toast.makeText(MainActivity.this, "设置你要查询的号码",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (!isConnected) {
					if (Preference.getInstance().autoNetwork()) {

						isWaitingConnect = true;
						mWaitingSearchNumber = txtPhone.getText().toString();

//						if (txtApiURL.getText().toString().contains("192")) {
//							ConnectivityHelper.setWIFIEnable(true);
//						} else
//							ConnectivityHelper.setMobileEnable(true);
					} else {
						if (!isConnected) {
							Toast.makeText(MainActivity.this, "先连上网再测试",
									Toast.LENGTH_LONG).show();
							return;
						}
					}
				} else {
					collapseSoftInputMethod();
					getNumber(txtPhone.getText().toString());
				}

			}
		});

	}
}
