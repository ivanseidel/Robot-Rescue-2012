package emerotecos.rescue.b;

import ioio.lib.util.android.IOIOActivity;
import android.app.ActionBar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class RobotActivity extends IOIOActivity implements IOIOEvents, SensorEventListener {
	protected Handler handler = new Handler();
	protected ActionBar actbar;
	protected Menu menu;

	//public static boolean isConnected = false;
	
	/*
	 * Android SENSORS
	 */
	public static SensorManager sensorManager;
	public static Sensor accellSensor;
	public static float[] accellVals = {0,0,0};
	
	public static Sensor angleSensor;
	public static float[] angleVals = {0,0,0};
	
	public static Sensor gyroSensor;
	public static float[] gyroVals = {0,0,0};
	public static double[] gyroIntVals = {0,0,0};
	
	public static void resetOnRamp(){
		gyroIntVals[1] = 0;
	}
	public static boolean isOnRamp(){
		
		if(Math.abs(gyroIntVals[1]) < 13){
			return false;
		}else{
			return true;
		}
		
	}
	
	public MenuItem ioio_status;
	public MenuItem robot_status;
	public MenuItem servo_status;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Configure Android Sensors
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		angleSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		accellSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);

		actbar = this.getActionBar();
		actbar.setDisplayShowTitleEnabled(false);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	protected void onPause() {
	    // Unregister the listener on the onPause() event to preserve battery life;
	    super.onPause();
	    sensorManager.unregisterListener(this);
	    Log.i("RescueB.RobotActivity", "Sensors Listners removed");
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    //sensorManager.registerListener(this, accellSensor, SensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
	    Log.i("RescueB.RobotActivity", "Sensors Listners added");
	}
	
	// IOIO Methods
	public void notifyIOIOhasConnected() {
		handler.post(new Runnable() {

			public void run() {
				ioio_status.setIcon(R.drawable.ioio_connected);
				robot_status.setVisible(true);
			}
		});
		//isConnected = true;
	}

	public void notifyIOIOhasDisconnected() {
		handler.post(new Runnable() {

			public void run() {
				if(ioio_status != null){
					ioio_status.setIcon(R.drawable.ioio_disconnected);
					robot_status.setVisible(false);
				}
			}
		});
		//isConnected = false;
	}

	public void changeRobotStatusIcon(final int Drawable){
		handler.post(new Runnable() {

			public void run() {
				if(robot_status != null)
					robot_status.setIcon(Drawable);
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		ioio_status = (MenuItem) menu.findItem(R.id.ioio_status);
		ioio_status.setEnabled(false);
		ioio_status.setIcon(R.drawable.ioio_disconnected);
		
		robot_status = (MenuItem) menu.findItem(R.id.robot_status);
		robot_status.setEnabled(false);
		
		/*servo_status = (MenuItem) menu.findItem(R.id.servo_status);
		servo_status.setIcon(R.drawable.refresh);
		servo_status.setEnabled(false);
		*/
		this.menu = menu;
		
		return true;
	}

	// Sensors Listners
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public long lastGyro = System.nanoTime();
	//long nowGyro, delta;
	public double k = 1.15385;
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor == angleSensor){
			angleVals[0] = event.values[0];
			angleVals[1] = event.values[1];
			angleVals[2] = event.values[2];
		}else if(event.sensor == accellSensor){
			accellVals[0] = event.values[0];
			accellVals[1] = event.values[1];
			accellVals[2] = event.values[2];
		}else if(event.sensor == gyroSensor){
			gyroVals[0] = event.values[0];
			gyroVals[1] = event.values[1];
			gyroVals[2] = event.values[2];
			
			/*nowGyro = System.nanoTime();
			delta = nowGyro - lastGyro;
			lastGyro = nowGyro;*/
			gyroIntVals[0] += /*delta */ gyroVals[0] * k;
			gyroIntVals[1] += /*delta */ gyroVals[1] * k;
			gyroIntVals[2] += /*delta */ gyroVals[2] * k;
		}
		//Log.i("RescueB.RobotActivity", "New samples from sensor!");
	}
}
