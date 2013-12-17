package emerotecos.util;
import java.util.ArrayList;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import android.util.Log;
import emerotecos.rescue.b.RobotActivity;


public class BaseRescueBLooper extends BaseIOIOLooper {
	
	public final static int STOPPED = 0;
	public final static int RUNNING = 1;
	
	public static boolean isConnected = false;
	public boolean isConnected(){
		return isConnected;
	}
	
	/*
	 * Defines the pins that connects to pheriferals on the IOIO board
	 */
	protected final static int pLed = 0;
	protected final static int pLedStatusGreen = 10;
	protected final static int pLedStatusRed = 11;
	protected final static int pLedVictim = 46;
	
	protected final static int pButton = 12;
	protected final static int pServo1 = 13;
	
	protected final static int pDist1 = 45;
	protected final static int pDist2 = 42;
	protected final static int pDist3 = 43;
	protected final static int pDist4 = 44;
	
	protected final static int pLight1 = 34;
	protected final static int pLight2 = 33;
	protected final static int pLight3 = 32;
	protected final static int pLight4 = 31;
	
	protected RobotActivity activity;
	
	/*
	 * Motors objects
	 */
	public TimeoutUART uart;
	public MotorController Motors;
	public Mecanum Move;
	public MazeWalker Walk;
	
	/*
	 * Servo motor class
	 */
	public ServoController servo;
	
	/*
	 * LED`s
	 */
	public DigitalOutput led_;
	public DigitalOutput oLedStatusGreen;
	public DigitalOutput oLedStatusRed;
	public DigitalOutput oLedVictim;
	
	/*
	 * IR Distance Sensors
	 */
	public IRDistanceSensor dist1;
	public IRDistanceSensor dist2;
	public IRDistanceSensor dist3;
	public IRDistanceSensor dist4;
	
	/*
	 * IR Temperature Sensors
	 */
	protected final int ADR_TEMP1 = 0x20;
	//protected final int ADR_TEMP2 = 0x20;
	protected final int ADR_TEMP2 = 0x06;
	protected final int ADR_TEMP3 = 0x5A;
	protected final int ADR_TEMP4 = 0x21;
	
	public IRTemperatureSensor temp1;
	public IRTemperatureSensor temp2;
	public IRTemperatureSensor temp3;
	public IRTemperatureSensor temp4;
	
	public VictimChecker victim;
	
	protected TwiMaster IRTemperatureTwi;
	
	/*
	 * Light Sensors
	 */
	public LightSensor light1;
	public LightSensor light2;
	public LightSensor light3;
	public LightSensor light4;
	
	/*
	 * RobotHead Object
	 */
	public RobotHead Head;
	
	/*
	 * Functions to deal with status changes
	 */
	public DigitalInput statusButton;
	public int robotStatus = STOPPED;
	protected onRobotStatusChange onChangeStatus;
	
	/*
	 * I2C Device Scanner
	 */
	public I2CScanner I2C1Scanner;
	
	public boolean isPressing = false;
		
	public void setOnChangeStatusListner(onRobotStatusChange onChangeStatus){
		this.onChangeStatus = onChangeStatus;
	}
	
	/*
	 * Deal with STATUS changes
	 */
	public void setStatus(int new_status){
		this.robotStatus = new_status;
		if(this.onChangeStatus != null){
			onChangeStatus.onChangeStatus(new_status);
		}
	}
	
	public int getStatus(){
		return robotStatus;
	}
	
	private void changeStatus(){
		setStatus( (getStatus()==STOPPED? RUNNING: STOPPED));
	}
	
	
	/*
	 * Initiates the object
	 */
	public BaseRescueBLooper(RobotActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected void setup() throws ConnectionLostException {
		setStatus(STOPPED);
		
		// Configures LED`s
		led_ = ioio_.openDigitalOutput(pLed, true);
		oLedStatusGreen = ioio_.openDigitalOutput(pLedStatusGreen, true);
		oLedStatusRed = ioio_.openDigitalOutput(pLedStatusRed, true);
		oLedVictim = ioio_.openDigitalOutput(pLedVictim, true);
		
		// Configures stop/start button
		statusButton = ioio_.openDigitalInput(pButton, DigitalInput.Spec.Mode.PULL_UP);
		
		// Creates the Motor instance responsable for handeling motor messages
		uart = new TimeoutUART(ioio_.openUart(47, 48, 57600, Uart.Parity.NONE, Uart.StopBits.ONE));
		Motors = new MotorController(uart);
		Move = new Mecanum(Motors);
		Motors.setPID(true);
		Walk = new MazeWalker(this);
		
		// Creates the Servo Motor PWM instance
		servo = new ServoController(ioio_.openPwmOutput(pServo1, ServoController.DEFAULT_FREQUENCY));
		
		// Creates all the IR distance sensors
		dist1 = new IRDistanceSensor(ioio_.openAnalogInput(pDist1));
		dist2 = new IRDistanceSensor(ioio_.openAnalogInput(pDist2));
		dist3 = new IRDistanceSensor(ioio_.openAnalogInput(pDist3));
		dist4 = new IRDistanceSensor(ioio_.openAnalogInput(pDist4));
		
		// Creates all IR Temperature sensors
		IRTemperatureTwi = ioio_.openTwiMaster(2, TwiMaster.Rate.RATE_100KHz, true);
		temp1 = new IRTemperatureSensor((byte) ADR_TEMP1, IRTemperatureTwi);
		temp2 = new IRTemperatureSensor((byte) ADR_TEMP2, IRTemperatureTwi);
		temp3 = new IRTemperatureSensor((byte) ADR_TEMP3, IRTemperatureTwi);
		temp4 = new IRTemperatureSensor((byte) ADR_TEMP4, IRTemperatureTwi);
		
		ArrayList<IRTemperatureSensor> temps =  new ArrayList<IRTemperatureSensor>();
		temps.add(temp1);
		temps.add(temp2);
		temps.add(temp3);
		victim = new VictimChecker(temps, 1.5);
		
		// Creates all four light sensors
		light1 = new LightSensor(ioio_.openAnalogInput(pLight1));
		light2 = new LightSensor(ioio_.openAnalogInput(pLight2));
		light3 = new LightSensor(ioio_.openAnalogInput(pLight3));
		light4 = new LightSensor(ioio_.openAnalogInput(pLight4));
		
		// Creates the RobotHead object
		Head = new RobotHead(this);
		
		// Configures the I2C Scanner
		I2C1Scanner = new I2CScanner(IRTemperatureTwi);
		
		/*
		 * This thread keeps track of:
		 * 		+STOP/PLAY button
		 */
		new Thread(new Runnable() {
			boolean actualButtonState;
			
			public void run() {
				while(ioio_ != null){
					/*
					 * Constant checks for stop/start button
					 */
					try {
						actualButtonState = statusButton.read();
						if(actualButtonState != isPressing){
							isPressing = actualButtonState;
							if(isPressing == true)
								changeStatus();
						}
					} catch (Exception e){
						
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		Log.i("RescueB", "IOIO Connected");
		isConnected = true;
		activity.notifyIOIOhasConnected();
	}

	@Override
	public void disconnected() {
		Log.i("RescueB", "IOIO Disconnected");
		isConnected = false;
		setStatus(STOPPED);
		activity.notifyIOIOhasDisconnected();
		super.disconnected();
	}
}
