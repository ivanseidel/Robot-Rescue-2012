package emerotecos.util;

import android.util.Log;
import emerotecos.rescue.b.RobotActivity;
import emerotecos.util.Util.Direction;

public class Mecanum {
	
	public interface MoveInterruption{
		public void setup();
		public boolean isInterrupted(boolean TryStopAction);
		public void finish();
	}
	/*public static final int FORWARD = 1;
	public static final int BACKWARD = -1;
	*/
	
	protected static final int TICKS_PER_TURN = 1796;
	
	// M1, M2, M3, M4
	static final double[] wheelsAngles = {34, -34, -34, 34};
	static final int[] wheelsDir = {1,-1,1,-1};
	
	public double actualAngle = 999;
	public double actualSpeed = 0;
	
	public MotorController controller;
	
	public Mecanum (MotorController controller){
		this.controller = controller;
	}
	
	public boolean setAngle(double angle, double speed){
		return setAngle(angle, speed, 0);
	}
	
	/**
	 * Vai para uma dire?ao e for?a definida
	 * @param angle Dire?‹o do movimento (0¼ = frente, 90¼ = direita)
	 * @param force For?a a se locomover
	 */
	public boolean setAngle(double angle, double speed, double rotateSpeed){
		
		//if(actualAngle != angle || actualSpeed != speed){
			actualAngle = angle;
			actualSpeed = speed;
			double[] calculatedSpeed = {0,0,0,0};
			
			for(int i = 0; i < 4; i ++){
				calculatedSpeed[i] = Math.cos(Math.toRadians(wheelsAngles[i] - angle)) * speed + wheelsDir[i] * rotateSpeed;
			}
			
			try {
				if(controller.setMotorsSpeed(calculatedSpeed[0], calculatedSpeed[1], calculatedSpeed[2], calculatedSpeed[3]))
					return true;
				
				Log.e("RescueB.Mecannum", "Couldn`t contact Mbed at setAngle");
			} catch (Exception e) {
				Log.e("RescueB.Mecannum", "Exception at setAngle");
			}
			return false;
			//Log.e("**RESCUEB", "CALCULATED to "+angle+": "+calculatedSpeed[0]+","+calculatedSpeed[1]+","+calculatedSpeed[2]+","+calculatedSpeed[3]);
		//}
		//return true;
	}
	
	public boolean setRotationSpeed(double speed){
		double[] calculatedSpeed = {0,0,0,0};
		
		for(int i = 0; i < 4; i ++){
			calculatedSpeed[i] = wheelsDir[i] * speed;
		}
		
		try {
			return controller.setMotorsSpeed(calculatedSpeed[0], calculatedSpeed[1], calculatedSpeed[2], calculatedSpeed[3]);
		} catch (Exception e) {
			Log.e("RescueB.Mecannum", "Exception at setRotationSpeed");
			return false;
		}
	}
	
	public double getCompass(){
		return RobotActivity.angleVals[0];
	}
	public double normalize(double angle){
		while (angle > 180)angle -= 360;
	    while (angle < -180)angle += 360;
	    return angle;
	}
	public double getRelativeAngle(double angle){
		double compassAngle = getCompass(), delta;
		delta = angle - compassAngle;
		return normalize(delta);
	}

	public static final int SlowDownAt = 25;
	public boolean rotateRelative(double angle, double speed, int maxMilliseconds){
		
		RobotActivity.gyroIntVals[2] = 0;
		long start = System.currentTimeMillis();
		double delta;
		double firstAngle = RobotActivity.gyroIntVals[2];
		
		double _target = (firstAngle - angle);
		int _dir;
		speed = Math.abs(speed);
		Log.e("RescueB.Rotate", "Start");

		while(System.currentTimeMillis() < start + maxMilliseconds){
			
			// Give time to think and do
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Calculates the delta
			delta = RobotActivity.gyroIntVals[2] - _target;
			
			// Logs the speed and delta
			//Log.e("RescueB.Rotate", "Speed: "+angularSpeed+" Delta: "+delta);
			
			// Checks if ended
			if(Math.abs(delta) <= 2){
				stop();
				Log.e("RescueB.Rotate", "End");
				return true;
			}
			_dir = (delta>0?1:-1);
			setRotationSpeed(_dir * speed);
			
			
			/*setRotationSpeed(Math.max((angle>0?1:-1)*(delta > angle? -1: 1)*Math.abs(speed)-
					((angle-delta) <= SlowDownAt/0.4*speed? (angle-delta)/(SlowDownAt/0.4*speed)*Math.abs(speed):0),
					(angle>0?1:-1)*(delta > angle? -1: 1) * 0.05) );*/
			
		}
		Log.e("RescueB.Rotate", "Time limit!");
		stop();
		return false;
	}
	
	protected long inertPredict(Direction dir, double speed){
		if(dir.getVal() == 1)
			return (long)(speed*20*4.8225 - 3.2443);
		
		return (long)(speed*20*4.1146 + 5.7085);
		
		
	}
	
	public boolean go(Direction dir, double speed){
		if(dir == Direction.FORWARD){
			return setAngle(0, speed);
		}else{
			return setAngle(180, speed);
		}
	}
	
	public long go(Direction dir, int degrees, double speed, int maxMilliseconds){
		return go(dir, degrees, speed, null,maxMilliseconds);
	}
	
	public static final double FixHeadingConstant = 0.1;
	public static final double MinimumHeadingErrorForFixingHeading = 20;
	public long go(Direction dir, int degrees, double speed, MoveInterruption interrupt ,int maxMilliseconds){
		long start = System.currentTimeMillis();
		long degStart = controller.getEncoderPosition(MotorController.MOTOR_2), degNow, delta;//, predict = inertPredict(dir, speed);
		// Multiply degrees by TICKS_PER_TURN to equalize
		long ticks = (long) (TICKS_PER_TURN/360.0 * degrees);
		
		Log.e("RescueB.Go", "Start");
		if(interrupt != null){
			interrupt.setup();
		}
		
		RobotActivity.gyroIntVals[2] = 0;
		go(dir, speed);
		
		while(System.currentTimeMillis() < start + maxMilliseconds){
			try {
				Thread.sleep(32);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Get current motor degrees
			degNow = controller.getEncoderPosition(MotorController.MOTOR_2);
			
			// Calculates delta
			delta = degStart - degNow;
			//Log.e("RescueB.Go", " Delta: "+degNow+" ("+(ticks+degStart)+")");
			
			if(Math.abs(RobotActivity.gyroIntVals[2]) > MinimumHeadingErrorForFixingHeading){
				stop();
				go((dir == Direction.FORWARD? Direction.BACKWARD: Direction.FORWARD), 0.6);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rotateRelative(RobotActivity.gyroIntVals[2] * 1.3, 0.5, 3000);
				RobotActivity.gyroIntVals[2] = 0;
				stop();
				go(dir, speed);
			}
			
			/*
			 * PID Stuff
			 */
			//error = FixHeadingConstant * RobotActivity.gyroIntVals[2];
			
			//setAngle((dir == Direction.FORWARD? 0 : 180), speed, error);
			
			if(Math.abs(delta) >= ticks){
				
				if(interrupt != null){
					if(interrupt.isInterrupted(true)){
						stop();
						interrupt.finish();
						Log.i("RescueB.Go", "Normal End, asked Object");
						return degStart + ticks;
					}
				}else{
					stop();
					Log.i("RescueB.Go", "Normal End");
					return degStart + ticks;
				}
			}else if(interrupt != null){
				if(interrupt.isInterrupted(false)){
					stop();
					interrupt.finish();
					Log.i("RescueB.Go", "Move interrupted by Object");
					return degStart + ticks;
				}
			}
		}
		Log.e("RescueB.Go", "Time limit!");
		stop();
		return 0;
	}
	
	public long go(Direction dir, int degrees, double speed, MoveInterruption interrupt){
		return go(dir, degrees, speed, interrupt, (int)(degrees/360.0*speed*4500 + 500));
	}
	public long go(Direction dir, int degrees, double speed){
		return go(dir, degrees, speed, null);
	}
	
	public boolean stop(){
		return go(Direction.BACKWARD, 0);
	}
}
