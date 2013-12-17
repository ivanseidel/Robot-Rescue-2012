package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ToggleButton;
import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.IRTemperatureSensor;
import emerotecos.util.MapController;
import emerotecos.util.RobotError;
import emerotecos.util.RobotErrorAdapter;
import emerotecos.util.onRobotStatusChange;
import emerotecos.util.Util.Movement;
import emerotecos.view.SensorsView;

public class RescueB extends RobotActivity {

	/*
	 * Global Access
	 */
	public static File mainDir;
	
	TabHost tabHost;
	TabSpec checkAll;
	TabSpec viewAll;
	TabSpec runProgram;
	
	BaseRescueBLooper IOIO;
	
	// Tab Check All
	ListView errorsList;
	RobotErrorAdapter errorsAdapter;
	TextView status;
	
	// Tab View All
	SeekBar servopos;
	ToggleButton ledOn;
	
	ProgressBar temp1_pb;
	TextView temp1_text;
	
	ProgressBar temp2_pb;
	TextView temp2_text;
	
	ProgressBar temp3_pb;
	TextView temp3_text;
	
	ProgressBar light1_pb;
	TextView light1_text;
	
	Button btCheckAll;
	
	// Tab run Programs
	
	
	SensorsView view_sensors;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();

		checkAll = tabHost.newTabSpec("checkAll");
		viewAll = tabHost.newTabSpec("viewAll");
		runProgram = tabHost.newTabSpec("runProgram");

		viewAll.setContent(R.id.view_tab);
		viewAll.setIndicator("View All");
		tabHost.addTab(viewAll);

		checkAll.setContent(R.id.checkall_tab);
		checkAll.setIndicator("Check All");
		tabHost.addTab(checkAll);
		
		runProgram.setContent(R.id.run_program);
		runProgram.setIndicator("Run program");
		tabHost.addTab(runProgram);

		btCheckAll = (Button) findViewById(R.id.checkAll);
		
		actbar.setHomeButtonEnabled(false);
		actbar.setDisplayShowTitleEnabled(false);
		
		// Status button changing
		actbar.newTab();
		
		// Check All
		status = (TextView)findViewById(R.id.status);
		
		errorsList = (ListView) findViewById(R.id.list_erros);
		errorsAdapter = new RobotErrorAdapter(this, new ArrayList<RobotError>());
		errorsList.setAdapter(errorsAdapter);
		
		// View All
		servopos = (SeekBar) findViewById(R.id.servopos);
		servopos.setMax(180);
		
		ledOn = (ToggleButton) findViewById(R.id.ledOn);
		
		temp1_pb = (ProgressBar) findViewById(R.id.temp1_pb);
		temp1_text = (TextView) findViewById(R.id.temp1_text);
		temp1_pb.setMax(200);
		
		temp2_pb = (ProgressBar) findViewById(R.id.temp2_pb);
		temp2_text = (TextView) findViewById(R.id.temp2_text);
		temp2_pb.setMax(200);
		
		temp3_pb = (ProgressBar) findViewById(R.id.temp3_pb);
		temp3_text = (TextView) findViewById(R.id.temp3_text);
		temp3_pb.setMax(200);
		
		light1_pb = (ProgressBar) findViewById(R.id.light1_pb);
		light1_text = (TextView) findViewById(R.id.light1_text);
		
		view_sensors = (SensorsView) findViewById(R.id.view_sensors);
		view_sensors.setMapController(new MapController());
		
		// Run Program
		((Button) findViewById(R.id.runProgram1)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				IOIO.disconnected();
				RescueB.this.startActivity(new Intent(RescueB.this, Program1.class));
			}
		});
		
		((Button) findViewById(R.id.runProgram2)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				IOIO.disconnected();
				RescueB.this.startActivity(new Intent(RescueB.this, Program2.class));
			}
		});
		
		/*
		 * Finds the directory of sdCard and create all folders
		 */
		File sdcard = Environment.getExternalStorageDirectory();
		mainDir = new File(sdcard.getAbsolutePath()+"/Emerotecos/");
		mainDir.mkdirs();
		
	}
	
	public void changeadr(View v){
		IOIO.temp1.changeAddress((byte)0x20);
	}

	public void checkAll(View view){
		btCheckAll.setEnabled(false);
		
		new Thread(new Runnable() {
			
			public void run() {
				handler.post(new Runnable() {
					
					public void run() {
						final ArrayList<RobotError> errs = RescueBTester.performTests(IOIO);
						
						errorsAdapter.setErrors(errs);
						errorsAdapter.notifyDataSetChanged();
						
						btCheckAll.setEnabled(true);
					}
				});
			}
		}).start();
	}
	
	@Override
	public void notifyIOIOhasConnected() {
		super.notifyIOIOhasConnected();
		view_sensors.setup(IOIO);
	}
	
	class ConfigRescueBLooper extends BaseRescueBLooper {

		public ConfigRescueBLooper(RobotActivity activity) {
			super(activity);
			
			onRobotStatusChange a = new onRobotStatusChange() {
				public void onChangeStatus(final int actual_state) {
					changeRobotStatusIcon(actual_state == STOPPED? R.drawable.pause: R.drawable.play);
					try {
						if(oLedStatusGreen != null){
							oLedStatusGreen.write(actual_state==RUNNING);
							oLedStatusRed.write(actual_state==STOPPED);
						}
					} catch (ConnectionLostException e) {
						//TODO
						// ENTERS HERE IF CONNECTION IS LOST
					}
				}
			};
			setOnChangeStatusListner(a);
		}
		
		boolean ab = false, e = true;
		int c = 0, a = 0,b =0;
		double t1, t2, t3, t4, spd, luz1 = 0;
		String out = "Init";
		boolean lastbtState = false;
		double tS = 0;
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			ab = !ab;
			
			led_.write(ab);
			/*if(spd != ((double)servopos.getProgress()/25 - 2)){
				spd = ((double)servopos.getProgress()/25 - 2);
				
			}*/
			
			if(getStatus() == RUNNING){
				if(servo.getMilisToArrive() == 0){
					//b+=10;//30659449ook
					servo.setPosition(servopos.getProgress()-90);
					
				}
			}

			//Move.setAngle(servopos.getProgress()*2, (ledOn.isChecked()? 0.1: 0));
			
			oLedVictim.write(ledOn.isChecked());
			
			//Motors.setStandy((getStatus() == STOPPED? true: false));
			
			dist1.getDistance();
			dist2.getDistance();
			dist3.getDistance();
			dist4.getDistance();
			luz1 = light1.getValue();
			t1 = Math.round(temp1.getTemperature()*10)/10.0;
			t2 = Math.round(temp2.getTemperature()*10)/10.0;
			t3 = Math.round(temp3.getTemperature()*10)/10.0;
			t4 = Math.round(temp4.getTemperature()*10)/10.0;
			Thread.sleep(50);
			//a = Motors.getEncoderPosition(MotorController.MOTOR_2);
			/*if(ledOn.isChecked()){
				//IRTemperatureSensor.victimTemperature = Math.min(t1, t2) + 0.9;
				out = "in "+Motors.getEncoderPosition(MotorController.MOTOR_1);
			}*/
			
			if(lastbtState != ledOn.isChecked()){
				if(ledOn.isChecked())
					tS = Math.min(t1, t2);
				else
					IRTemperatureSensor.victimTemperature = (tS + Math.max(t1, t2))/2;
				lastbtState = ledOn.isChecked();
			}
			
			handler.post(new Runnable() {
				
				public void run() {
					try {
						//status.setText(out);//a + "|"+(isPressing? " PRESS+": " RELEASE+"));
						temp1_pb.setProgress((int) ((t1-20)*50));
						temp1_text.setText("Temp. 1: "+t1+"¼C");
						if(t1 > IRTemperatureSensor.victimTemperature)
							temp1_text.setTextColor(Color.RED);
						else
							temp1_text.setTextColor(Color.BLACK);
						
						temp2_pb.setProgress((int) ((t2-20)*50));
						temp2_text.setText("Temp. 2: "+t2+"¼C");
						if(t2 > IRTemperatureSensor.victimTemperature)
							temp2_text.setTextColor(Color.RED);
						else
							temp2_text.setTextColor(Color.BLACK);
						
						temp3_pb.setProgress((int) ((t3-20)*50));
						temp3_text.setText("Temp. 3: "+t3+"¼C");
						if(t3 > IRTemperatureSensor.victimTemperature)
							temp3_text.setTextColor(Color.RED);
						else
							temp3_text.setTextColor(Color.BLACK);
						
						status.setText("Temp. 4: "+t4+"¼C");
						if(t4 > IRTemperatureSensor.victimTemperature)
							status.setTextColor(Color.RED);
						else
							status.setTextColor(Color.BLACK);
						
						light1_pb.setProgress((int)luz1);
						light1_text.setText("Light 1: "+luz1);
						
						view_sensors.invalidate();
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
			});
			Thread.sleep(70);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		IOIO = new ConfigRescueBLooper(this);
		return IOIO;
	}

}