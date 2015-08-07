package com.meetjenny.drunkenbunny;

import java.util.Random;

import com.meetjenny.drunkenbunny.R;

import android.R.bool;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math;

public class MainAccelerometer extends Activity implements
		AccelerometerListener, SensorEventListener {

	private int gameMode; // Indicator when to shake
	private Handler handler;
	private int gameScore;
	private int chance = 5;
	private int soundno = 0;
	private int actionTypeNum;
	private MyApplication myApp;
	private int shakeDuration = 1025 * 2;
	private int spinDuration = 3075;
	private int waitDuration = 550 * 2;
	private int pauseTime;
	
	private boolean shakeStartFlag = false;
	private boolean spinStartFlag = false;
	private boolean increaseSpinFlag = true;
	
	private SoundPool sp;
	private SoundPool voice_sp;
	float currentDegree = 0f;

	TextView gameScoreTxt;
	TextView chanceTxt;
	ImageView imageIV;

	boolean voice_loaded = false;
	boolean loaded = false;

	// Spin points
	float spinStartPoint;
	float oppositePoint;
	float currentPoint;
	float nextPoint;
	float pointGap;
	float endPoint;
	Vibrator vib;
	RelativeLayout rl;

	// device sensor manager
	private SensorManager oSensorManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerometer_example_main);
		
		rl = (RelativeLayout)findViewById(R.id.defaultLayout);
		
		gameScoreTxt = (TextView) findViewById(R.id.tvShakeScore);
		
		chanceTxt = (TextView) findViewById(R.id.tvChance);

		imageIV = (ImageView) findViewById(R.id.imageViewCompass); 	// spin image view
		
		myApp = ((MyApplication) getApplicationContext()); // Global status
															// class.

		// initialize your android device sensor capabilities
		oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);		
		
		// Get instance of Vibrator from current Context
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);		 
		
		// Play BGM
		myApp.playBGM();

		manageMode();
	}

	public void manageMode() {
		// Thread for checking the game status.
		// When it's time to shake, the status is 'shake'.
		// If it's not, the status is 'wait'.
		Thread update = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					
					if( !myApp.getGameFinished() )
					{
						if (gameMode == 0) {
							// When it's time to wait.
	
							// put status message to bundle
							Bundle values = new Bundle();
							values.putString("status", "wait");
							Message msg = new Message();
							msg.setData(values);
	
							// send message
							handler.sendMessage(msg);
	
							// Delay for shake duration.
							try {
								Thread.sleep(waitDuration);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	
							gameMode = 1;
						} else if (gameMode == 1) {
							// When it's time to action.
	
							Random rand = new Random(); 
							actionTypeNum = rand.nextInt(3);
							
							// put to message according to action type number.
							// 0 is shake. 1 is spin.
							Bundle values = new Bundle();
							
							if (actionTypeNum == 0)
							{
								Log.e("status", "shake");
								values.putString("status", "shake");
								pauseTime = shakeDuration;
							}
							else if (actionTypeNum == 1) 
							{
								values.putString("status", "spin_right");	
								pauseTime = spinDuration;
							}
							else if (actionTypeNum == 2) 
							{
								values.putString("status", "spin_left");	
								pauseTime = spinDuration;
							}												
							
							Message msg = new Message();
							msg.setData(values);
	
							// send message
							handler.sendMessage(msg);
	
							// Delay for wait duration.
							try {
								Thread.sleep(pauseTime);
								// Release sound pool 
								//voice_sp.release();
								//sp.release();
							} catch (Exception e) {
								e.printStackTrace();
							}
	
							gameMode = 0;
						}
					} 
					else
					{
						// put to message according to action type number.
						// 0 is shake. 1 is spin.
						Bundle values = new Bundle();
						values.putString("status", "finish");	
						
						Message msg = new Message();
						msg.setData(values);

						// send message
						handler.sendMessage(msg);
						
						// Delay for wait duration.
						try {
							Thread.sleep(3000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						// Back to the menu if the game is finished.
						System.exit(0);
					}
				}
			}
		});

		// Handler to display information as the update thread sent
		handler = new Handler() {						
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle values = msg.getData(); // Get passed value from the update thread
				String textStr = values.getString("status"); // Get game status - time to shake or not?
				gameScore = myApp.getScore(); // Score saved in global status								
				
				if (textStr.equals("shake")) {
					// Show mission : shake		
					setRotationZero();
					shakeStartFlag = true;
					imageIV.setImageResource(R.drawable.shake);
					rl.setBackgroundColor(Color.parseColor("#e7be54"));
					playSound(0);
				}
				else if (textStr.equals("spin_right")) {
					// Show mission : spin right
					spinStartFlag = true;
					imageIV.setImageResource(R.drawable.spin_right);
					rl.setBackgroundColor(Color.parseColor("#4ed5bd"));					
					playSound(1);
				}
				else if (textStr.equals("spin_left")) {
					// Show mission : spin left
					spinStartFlag = true;
					imageIV.setImageResource(R.drawable.spin_left);
					rl.setBackgroundColor(Color.parseColor("#5cc0e5"));					
					playSound(2);
				}				
				else if (textStr.equals("finish")) {
					setRotationZero();
					imageIV.setImageResource(R.drawable.gamefinish);	
					rl.setBackgroundColor(Color.parseColor("#ed7b7b"));					
				}
				else {
					// Show it's time to wait.
					setRotationZero();
					imageIV.setImageResource(R.drawable.wait);
					rl.setBackgroundColor(Color.parseColor("#ed7b7b"));
				}
				
				gameScoreTxt.setText("score: " + gameScore);
			}
		};
		update.start();
	}
	
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isSupported(this)) {

			// Start Accelerometer Listening
			AccelerometerManager.startListening(this);
		}

		// for the system's orientation sensor registered listeners
		oSensorManager.registerListener(this,
				oSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// to stop the listener and save battery
		oSensorManager.unregisterListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isListening()) {

			// Start Accelerometer Listening
			AccelerometerManager.stopListening();

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("Sensor", "Service  distroy");

		// Check device supported Accelerometer sensor or not
		if (AccelerometerManager.isListening()) {

			// Start Accelerometer Listening
			AccelerometerManager.stopListening();
		}
	}
	
	private void loadSoundSources() {
		
	}
	
	private void playSound(int option) {
		int sound_id = 0;
				
		// Load the sound
		voice_sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		voice_sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				voice_loaded = true;
			}
		});		
		
		Log.e("play voice", "play voice");
		
		// Use in whatever method is used to load the sounds
		try {

			switch (option) {
			case 0:
				sound_id = voice_sp.load(this, R.raw.voice_shake, 1); // in 2nd param u have to pass your desire ringtone
				voice_sp1.play
				break;
			case 1:
				sound_id = voice_sp.load(this, R.raw.voice_right, 1); // in 2nd param u have to pass your desire ringtone
				
				break;
			case 2:
				sound_id = voice_sp.load(this, R.raw.voice_left, 1); // in 2nd param u have to pass your desire ringtone
				
				break;
			case 3:
				sound_id = voice_sp.load(this, R.raw.bell1, 1); // in 2nd param u have to pass your desire ringtone
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		voice_sp.play(sound_id, 1, 1, 0, 0, 1);
	}

	/**
	 * Function to call when the user shake the phone
	 */
	public void onShake(float force) {
		boolean isFirstShake = myApp.getFirstShakeFlag();

		// Play sound on when it's shake time.
		if ( ( gameMode == 1 ) && ( actionTypeNum == 0 ) ) 
		{

			if ( shakeStartFlag == true )
			{
				// Load the sound
				sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
				sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
					@Override
					public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
						loaded = true;
					}
				});
	
				int iTmp = 0;
	
				// sound file number keeps turn
				soundno++;
				if (soundno == 4)
					soundno = 0;
	
				// Use in whatever method is used to load the sounds
				try {
	
					switch (soundno) {
					case 1:
						iTmp = sp.load(this, R.raw.bell1, 1); // in 2nd param u have to pass your desire ringtone
						break;
					case 2:
						iTmp = sp.load(this, R.raw.xylophone1, 1); // in 2nd param u have to pass your desire ringtone
						break;
					case 3:
						iTmp = sp.load(this, R.raw.xylophone2, 1); // in 2nd param u have to pass your desire ringtone
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				if (loaded) {
					sp.play(iTmp, 1, 1, 0, 0, 1);
	
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(100);
								// flag = false;
							} catch (Exception e) {
								// Log.e("test", "thread error");
							}
						}
					}.start();
					// sp.release();
				}
	
				// When it's time to shake, increase the score.
				myApp.setScore();
				
				// Speed up
				speedUp();
				
				shakeStartFlag = false;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		// get the angle around the z-axis rotated
		float degree = Math.round(event.values[0]);

		//tvCurrentDegree.setText("Current Degree: " + Float.toString(degree) + " degrees");

		if ( (gameMode == 1) && ( (actionTypeNum == 1) ||  (actionTypeNum == 2) ) )
		{
			if (spinStartFlag == true)
			{				
				increaseSpinFlag = true;
				spinStartPoint = degree;
				currentPoint = spinStartPoint;
				//tvspinStartPoint.setText("start degree" + degree);
				spinStartFlag = false;
			}
			else 
			{
				// set score
				spinScore(degree);
			}
		}
	}
	
	public void setRotationZero() {
		// create a rotation animation (reverse turn degree degrees)
		RotateAnimation ra = new RotateAnimation(
				0, 
				0,
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF,
				0.5f);

		// how long the animation will take place
		ra.setDuration(1);

		// set the animation after the end of the reservation status
		ra.setFillAfter(true);

		// Start the animation
		imageIV.startAnimation(ra);
	}

	public void spinScore(float degree) {
		// record the compass picture angle turned

		// When it's time to spin right
		if ( ( gameMode == 1 ) && ( actionTypeNum == 1 ) )
		{
			pointGap = 30;			

			//If it spins full lap then recalculate
			oppositePoint = currentPoint - pointGap;			
			if( oppositePoint < 0 )
			{
				oppositePoint = 360 + oppositePoint;
			}
			
			nextPoint = currentPoint + pointGap;
			if ( nextPoint > 360 )
			{
				nextPoint = nextPoint - 360;
			}
			
			endPoint = spinStartPoint + ( pointGap * 3 );
			if ( endPoint > 360 )
			{
				endPoint = endPoint - 360;
			}						
			
			// create a rotation animation (reverse turn degree degrees)
			RotateAnimation ra = new RotateAnimation(
					currentDegree, 
					degree,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF,
					0.5f);
	
			// how long the animation will take place
			ra.setDuration(210);
	
			// set the animation after the end of the reservation status
			ra.setFillAfter(true);
	
			// Start the animation
			imageIV.startAnimation(ra);
			currentDegree = degree;
		
			if ( increaseSpinFlag == true )
			{				
				if ( ( nextPoint -4 <= degree ) && ( degree < nextPoint + 4 ) )
				{
					if ( ( endPoint -4 <= degree ) && ( degree < endPoint + 4 ) )
					{
						// Increase score if the motion is right
						myApp.setScore();
						
						// Speed up
						speedUp();
						
						gameScore = myApp.getScore(); // Score saved in global status				
						gameScoreTxt.setText("score: " + gameScore);		
					
						increaseSpinFlag = false;					
						playSound(3);
						Log.d("spin right", "right direction");
					}
					else
					{
						currentPoint = nextPoint;
					}
				}
				else if ( ( oppositePoint - 10 <= degree ) && ( degree < oppositePoint + 4 ) )
				{
					Log.d("spin right", "wrong direction");
					
					// Wrong direction.
					// Vibrate for 300 milliseconds.
					vib.vibrate(300);
					// Decrease chance numbers
					chance--;
					chanceTxt.setText("          Chance : " + chance);
					increaseSpinFlag = false;
					if ( chance == 0 )
					{
						myApp.setGameFinished();
					}
				}
			}				
		}		
		// When it's time to spin left
		else if ( ( gameMode == 1 ) && ( actionTypeNum == 2 ) )
		{
			pointGap = 30;			

			//If it spins full lap then recalculate
			oppositePoint = currentPoint + pointGap;			
			if ( oppositePoint > 360 )
			{
				oppositePoint = oppositePoint - 360;
			}
			
			nextPoint = currentPoint - pointGap;			
			if( nextPoint < 0 )
			{
				nextPoint = nextPoint + 360;
			}
			
			endPoint = spinStartPoint - ( pointGap * 3 );
			if ( endPoint < 0 )
			{
				endPoint = endPoint + 360;
			}
			
			// create a rotation animation (reverse turn degree degrees)
			RotateAnimation ra = new RotateAnimation(
					currentDegree, 
					degree,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF,
					0.5f);
	
			// how long the animation will take place
			ra.setDuration(210);
	
			// set the animation after the end of the reservation status
			ra.setFillAfter(true);
	
			// Start the animation
			imageIV.startAnimation(ra);
			currentDegree = degree;			

			if ( increaseSpinFlag == true )
			{				
				if ( ( nextPoint -4 <= degree ) && ( degree < nextPoint + 4 ) )
				{
					if ( ( endPoint -4 <= degree ) && ( degree < endPoint + 4 ) )
					{
						// Increase score if the motion is right
						myApp.setScore();
						
						// Speed up
						speedUp();
						
						gameScore = myApp.getScore(); // Score saved in global status				
						gameScoreTxt.setText("score: " + gameScore);		
					
						increaseSpinFlag = false;					
						playSound(3);
						Log.d("spin right", "right direction");
					}
					else
					{
						currentPoint = nextPoint;
					}
				}
				else if ( ( oppositePoint - 4 <= degree ) && ( degree < oppositePoint + 10 ) )
				{
					Log.d("spin right", "wrong direction");
					
					// Wrong direction.
					// Vibrate for 300 milliseconds.
					vib.vibrate(300);
					// Decrease chance numbers
					chance--;
					chanceTxt.setText("          Chance : " + chance);
					increaseSpinFlag = false;	
					
					
					if ( chance == 0 )
					{
						myApp.setGameFinished();
					}
				}
			}
		}			
	}	
	
	private void speedUp() {
		shakeDuration = (int) (shakeDuration * 0.95);
		spinDuration = (int) (spinDuration * 0.95);
		waitDuration = (int) (waitDuration * 0.95);		
	}
	
	public void closeApp(View v) {		
		// Back to the menu
		System.exit(0);
	}
}
