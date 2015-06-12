package cn.aolong.who;

import android.app.Application;

public class WhoApplication extends Application {

	private static WhoApplication application;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		application=this;
	}
	
	
	public  static WhoApplication getInstance(){
		return application;
	}
	
	
}
