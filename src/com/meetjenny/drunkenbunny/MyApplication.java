package com.meetjenny.drunkenbunny;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.acra.*;
import org.acra.annotation.*;

import com.meetjenny.drunkenbunny.R;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.SoundPool;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;

public class MyApplication extends Application {	
	private int score;
	public boolean musicLoaded = false;
	public boolean isFirstShake = true;
	private boolean shakeTime = false;		
	private MediaPlayer mp;
	private boolean isGameFinished = false;
	private SoundPool soundPool;
	private int my_sound1, my_sound2, my_sound3;
	
    @Override
    public void onCreate() {
        super.onCreate();
        setLogging();        
    }    
    
    // Return score value
    public int getScore() {
    	return score;
    }
    
    // Update score
    public void setScore() {
    	score++;
    }
    
    public boolean getShakeTime() {
    	return shakeTime;
    }
    
    public void setShakeTime(boolean val) {
    	shakeTime = val;
    }
    
    // Reture first time flag
    public boolean getFirstShakeFlag() {
    	return isFirstShake;
    }
    
    public void setFirstShakeFlag() {
    	isFirstShake = false;
    }
    
    public void loadSoundResources() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        my_sound1 = soundPool.load(this, R.raw.voice_right, 1);
        my_sound2 = soundPool.load(this, R.raw.shake1, 1);        
        my_sound3 = soundPool.load(this, R.raw.xylophone1, 1);           
        
        playBGM();

        soundPool1.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId,int status) {
            	loaded = true;
            }
        });     
    }
     
    public void playBGM() {
		// Play background music
        mp = MediaPlayer.create(this, R.raw.labamba_edit2);
        mp.setLooping(false);
        mp.start();
        
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
            	setGameFinished();
            }
       });        	
    }
    
    protected void setGameFinished()
    {
    	isGameFinished = true;
    }
    
    protected boolean getGameFinished()
    {
    	return isGameFinished;
    }
    
	public void setLogging() {
		String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); 		
		String temp = "drunkenbunny.txt";
		String fileName = "drunkenbunny.txt";		
		
		File root = Environment.getExternalStorageDirectory();		
		File appDir = new File(root + File.separator + "drunkenbunny");

	    if(!appDir.exists() && !appDir.isDirectory()) 
	    {
	        // create empty directory
	        if (appDir.mkdirs())
	        {
	            Log.i("MyApplication","App dir created");
	        }
	        else
	        {
	            Log.w("MyApplication","Unable to create app dir!");
	        }
	    }
	    else
	    {
	        Log.i("MyApplication","App dir already exists.");
	    }
		
		File outputFile = new File(root.getAbsolutePath() + "/drunkenbunny", fileName);

		@SuppressWarnings("unused")
		Process process;
		try {
			process = Runtime.getRuntime().exec("logcat -v time -f " + outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("MyApplication", "=================Logging is set===================");
	}
}