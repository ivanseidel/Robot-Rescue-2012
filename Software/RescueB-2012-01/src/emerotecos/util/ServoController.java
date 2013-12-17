package emerotecos.util;

import android.os.Handler;
import android.util.Log;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

public class ServoController {
	public final static int DEFAULT_FREQUENCY = 50; // Hz
	
	protected final static int DEFAULT_PULSE_WIDTH = 20000; // uS
	
	protected final static int DEFAULT_POS_MIDLE = 1500; // uS
	protected final static int DEFAULT_POS_START = 1050; // uS
	protected final static int DEFAULT_POS_END = 1960; // uS
	
	protected final static double DEFAULT_FEED_RATE = 2.6; // Seconds / º
	
	private PwmOutput comm;
	private int lastPos = -46;
	private int nowPos = -46;
	private long lastTime = 0;
	
	public ServoController(PwmOutput comm) {
		this.comm = comm;
		/*try {
			comm.setPulseWidth(DEFAULT_PULSE_WIDTH);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		lastPos = 0;
		lastTime = System.currentTimeMillis();
		setPosition(0);
	}
	
	/**
	 * Enter the angle (0 to 90) to set the target position <br>
	 * Use {@link #waitArrive()} to wait the servo arrive at the setted position
	 * @param angle the angle to send the servo.
	 */
	public void setPosition(int angle){
		try{
		//if(angle != nowPos){
			//angle += 45;
			angle = Math.min(Math.max(-45, angle), 45);
			float rawUs = (float) (((DEFAULT_POS_END - DEFAULT_POS_START) / 90.0) * (angle+45));
			int filteredUs = (int) (Math.min(Math.max(rawUs + DEFAULT_POS_START, DEFAULT_POS_START), DEFAULT_POS_END) );
			try {
				comm.setPulseWidth(filteredUs);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lastPos = nowPos;
			nowPos = angle;
			lastTime = System.currentTimeMillis();
		//}
		}catch(Exception e){
			Log.e("RescueB.ServoController", "Couldn`t set servo position");
			e.printStackTrace();
		}
	}
	
	public int getPosition(){
		return nowPos;
	}
	
	public int getMilisToArrive(){
		return (int) (Math.min(
				Math.max(
						Math.abs(lastPos - nowPos) * DEFAULT_FEED_RATE - (System.currentTimeMillis()-lastTime)
				,0)
				,1000));
		//return 300;
	}
	
	/**
	 * Will block until has arrived
	 */
	public void waitArrive(){
		
		try {
			
			Thread.sleep(getMilisToArrive());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Continues immediatly after calling. Pass a {#{@link Runnable} to run after reaching the position
	 * @param run the {#{@link Runnable} object to run after reaching the targeted position
	 */
	public void doWhenArrive(Runnable run){
		new Handler().postDelayed(run, getMilisToArrive());
	}
}
