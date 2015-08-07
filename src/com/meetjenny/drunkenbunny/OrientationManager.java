package com.meetjenny.drunkenbunny;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OrientationManager {

	// record the compass picture angle turned
	private float currentDegree = 0f;

	// device sensor manager
	private static SensorManager sensorManager;

    private static OrientationManager listener;
	
	private static Context aContext=null;
	
    /** indicates whether or not Orientation Sensor is supported */
    private static Boolean supported;	
    
    /** Accuracy configuration */
    private static float threshold  = 10.0f; 
    private static int interval     = 200;
    
    private static Sensor orientationSensor;
    
    /** indicates whether or not Orientation Sensor is running */
    private static boolean runningOrientation = false;
	
    /**
     * Returns true if at least one Orientation sensor is available
     */
    public static boolean isOrientationSupported(Context context) {
    	aContext = context;
        if (supported == null) {
            if (aContext != null) {
                sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
                
                // Get all sensors in device
                List<Sensor> orientation_sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);                           
                supported = new Boolean(orientation_sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
    
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum orientation variation for considering shaking
     * @param interval
     *             minimum interval between to spin events
     */
    public static void configure(int threshold, int interval) {
    	OrientationManager.threshold = threshold;
    	OrientationManager.interval = interval;
    }
    
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for orientation events
     */
    public static void startListening( OrientationManager accelerometerListener ) 
    {
        sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
        
        // Take all sensors in device
        List<Sensor> orientation_sensors = sensorManager.getSensorList(
        		Sensor.TYPE_ACCELEROMETER);
        
        if (orientation_sensors.size() > 0) {
        	orientationSensor = orientation_sensors.get(0);
            
            // Register Accelerometer Listener
        	runningOrientation = sensorManager.registerListener(sensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        	
            listener =  accelerometerListener;                       
            
        }
    }
    
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = 
            new SensorEventListener() {
     
            private long now = 0;
            private long timeDiff = 0;
            private long lastUpdate = 0;
            private long lastShake = 0;
     
            private float x = 0;
            private float y = 0;
            private float z = 0;
            private float lastX = 0;
            private float lastY = 0;
            private float lastZ = 0;
            private float force = 0;
     
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
     
            public void onSensorChanged(SensorEvent event) {

                // use the event timestamp as reference
                // so the manager precision won't depends 
                // on the OrientationListener implementation
                // processing time
                now = event.timestamp;
     
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
     

                // trigger change event
               // listener.onOrientationChanged(x, y, z);
                
                new Thread () {
                    @Override
                    public void run () {
                        try {
                            Thread.sleep(1000);
                            //flag = false;
                        } catch (Exception e) {
                        	Log.e("test", "thread error");                  
                        }
                    }
                }.start();
            }
     
        };

}
