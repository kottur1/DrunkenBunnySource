package com.meetjenny.drunkenbunny;

import com.meetjenny.drunkenbunny.R;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.Intent;

public class LogoScreen extends Activity {
	// private handler
	private Handler handler;
	
	// private delay time
	private int delayedTime = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_screen_layout);
		
		Log.e("LogoScreen", "Splash began");
	}		
		
	public void startGame(View v) {
		Intent it = new Intent(this, MainAccelerometer.class); 
		startActivity(it);
	}
}
