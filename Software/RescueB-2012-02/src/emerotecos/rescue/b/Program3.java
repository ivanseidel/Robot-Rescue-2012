package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.onRobotStatusChange;
import emerotecos.view.SensorsView;

public class Program3 extends RobotActivity{
	
	Program3Looper IOIO;
//	SensorsView view_sensors;

	boolean runProgram3 = true;
	boolean isFirstOn = true;
	public Runnable Program3Loop = new Runnable() {
		public void run() {
			Log.i("RescueB", "Program3 Started");
			
			double error = 0;
			double frontError = 0;
			double frontDist = 0;
			double angularError = 0;
			
			while (runProgram3) {

				if(IOIO.getStatus() == BaseRescueBLooper.RUNNING && isFirstOn){
					isFirstOn = false;
					IOIO.setStatus(BaseRescueBLooper.STOPPED);
				}else if (IOIO.getStatus() == BaseRescueBLooper.RUNNING) {
				
					
					
					/*for(int i = 0; i < 9; i++){
						try {
							IOIO.oLedVictim.write((i%2) == 0);
							Thread.sleep(200);
						} catch (ConnectionLostException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}*/
					
					try {
						error = Math.min(IOIO.dist4.getDistance() - 14, 17) * -0.08;
						frontDist = IOIO.dist1.getDistance();
						
						angularError = Math.abs(RobotActivity.accellVals[2] - 9.98) * 0.1;
						
						frontError = (frontDist < 21? 21-frontDist: 0) * 0.15;
						
						IOIO.Move.setAngle(180, 0.6, error + frontError + angularError);
					} catch (Exception e) {
						e.printStackTrace();
					}
					/*if(IOIO.temp1.victimExist() || IOIO.temp2.victimExist()){
						IOIO.Move.stop();
						for(int i = 0; i < 9; i++){
							try {
								IOIO.oLedVictim.write((i%2) == 0);
								Thread.sleep(200);
							} catch (ConnectionLostException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}*/
					
				}else if(IOIO.Move != null){
					IOIO.Move.stop();
				}
			}
			Log.i("RescueB", "Program3 Stopped");
		}
	};
	
	Thread Program3Thread;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program3);

		actbar.setHomeButtonEnabled(false);
		actbar.setDisplayShowTitleEnabled(false);

		Program3Thread = new Thread(Program3Loop);
		Program3Thread.setName("Program1LooperThread");
	}

	@Override
	protected void onPause() {
		/*
		 * runProgram1 = false; try { Program1Thread.join(); } catch
		 * (InterruptedException e) { e.printStackTrace(); }
		 */
		super.onPause();
	}

	@Override
	protected void onResume() {
		sensorManager.registerListener(this, accellSensor, SensorManager.SENSOR_DELAY_GAME);
		
		runProgram3 = true;
		try {
			Program3Thread.start();
		} catch (Exception e) {
			Log.i("RescueB.Program1", "Thread already running");
		}
		super.onResume();
	}

	@Override
	public void notifyIOIOhasConnected() {
		super.notifyIOIOhasConnected();
		IOIO.servo.setPosition(25);
//		view_sensors.setup(IOIO);
	}

	public class Program3Looper extends BaseRescueBLooper {

		public Program3Looper(RobotActivity activity) {
			super(activity);

			onRobotStatusChange a = new onRobotStatusChange() {
				public void onChangeStatus(final int actual_state) {
					changeRobotStatusIcon(actual_state == STOPPED ? R.drawable.pause
							: R.drawable.play);
					try {
						if (oLedStatusGreen != null) {
							oLedStatusGreen.write(actual_state == RUNNING);
							oLedStatusRed.write(actual_state == STOPPED);
						}
					} catch (ConnectionLostException e) {
					}
				}
			};

			setOnChangeStatusListner(a);
		}

		boolean ab;

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			Thread.sleep(1000);
		}
	}

	@Override
	protected Program3Looper createIOIOLooper() {
		IOIO = new Program3Looper(this);
		return IOIO;
	}
}
