package emerotecos.util;

import ioio.lib.api.exception.ConnectionLostException;

import java.util.concurrent.TimeoutException;

import android.util.Log;


public class MotorController implements Testable{
	
	TimeoutUART comm;
	
	byte[] response = new byte[20];
	byte[] send = new byte[10];
	
	public static final byte MOTOR_1 = 0x00;
	public static final byte MOTOR_2 = 0x01;
	public static final byte MOTOR_3 = 0x02;
	public static final byte MOTOR_4 = 0x03;
	
	protected static final byte ADR_CMD_INIT = 0x02;
	protected static final byte ADR_CMD_RESET = 0x03;
	protected static final byte ADR_CMD_STANDBY = 0x04;
	protected static final byte ADR_CMD_PID = 0x09;
	
	protected static final byte ADR_SET_SPEED[] = {0x10,0x11,0x12,0x13};
	protected static final byte ADR_SET_SPEEDS = 0x15;
	
	protected static final byte ADR_GET_SPEED[] = {0x20,0x21,0x22,0x23};
	protected static final byte ADR_GET_SPEEDS_SPEEDS = 0x25;
	
	protected static final byte ADR_GET_ENCODER[] = {0x30,0x31,0x32,0x33};
	protected static final byte ADR_GET_ENCODERS = 0x35;
	
	protected static final int persists = 8;
	
	public MotorController(TimeoutUART comm) {
		this.comm = comm;
	}
	
	public boolean writeRead(byte[] writeData, byte[] read, int readSize){
		
		for(int i = 0; i < persists; i++){
			
			try {
				comm.writeRead(writeData, read, readSize, 70);
				return true;
			} catch (TimeoutException e) {
			}
		}
		Log.e("RescueB.MotorController", "Failed to contact Mbed board "+persists+" times");
		return false;
	}
	
	protected byte[] concat(byte[] A, byte[] B) {
		byte[] C= new byte[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}
	
	protected boolean comOk(){
		return comm != null;
	}
	
	protected short convertToShort(byte[] byteArray){
		short ret_s = (short) ((byteArray[0]<<8) & (byteArray[1]));
		return ret_s;
	}
	protected float convertToFloat(byte[] byteArray) {
		return (float)(convertToShort(byteArray)/100);
	}
	
	protected byte[] convertToByteArray(short shortV){
		
		byte[] ret_b = {
				(byte)(shortV >> 8),
				(byte)(shortV)
		};
		
		return ret_b;
	}
	protected byte[] convertToByteArray(float floating){
		return convertToByteArray((short)(floating*100));
	}
	
	
	public boolean setMotorSpeed(byte Motor, double Speed) throws ConnectionLostException, InterruptedException{
		if(!comOk())
			return false;
		
		byte[] send = new byte[] {ADR_SET_SPEED[Motor], SpeedToByte(Speed)};
		
		try{
			if(writeRead(send, response, 0))
				return true;
			else
				return false;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean setMotorsSpeed(double M1Speed, double M2Speed, double M3Speed, double M4Speed) throws ConnectionLostException, InterruptedException{
		if(!comOk()){
			Log.e("RescueB.MotorController", "Communication NOT ok");
			return false;
		}
		byte[] send = new byte[] {ADR_SET_SPEEDS, SpeedToByte(M1Speed), SpeedToByte(M2Speed), SpeedToByte(M3Speed), SpeedToByte(M4Speed)};
		
		try{
			if(writeRead(send, response, 0)){
				return true;
			}else{
				Log.e("RescueB.MotorController", "Couldn`t contact MotorController");
				return false;
			}
		}catch (Exception e) {
			Log.e("RescueB.MotorController", "Exception at setMotorSpeed");
			e.printStackTrace();
		}
		
		return false;
	}
	
	public int getEncoderPosition(byte Motor){
		if(!comOk()){
			Log.e("RescueB.MotorController", "Communication NOT ok");
			return 0;
		}
		
		byte[] send = new byte[] {ADR_GET_ENCODER[Motor]};
		int pos = 0;
		for(int i = 0; i< persists; i++){
			if(writeRead(send, response, 5)){
				pos = joinInt(response, 0);
				int check = 0;
				for(int a = 0; a < 4; a++)
					check += unsigned(response[a]);
				if(check%256 == unsigned(response[4]))
					return pos;
			}
			else{
				Log.e("RescueB.MotorController", "Couldn`t contact MotorController");
				return 0;
			}
		}
		Log.e("RescueB.MotorController", "Mismatch on communication: expected total of "+response[4]+" got "+ pos +". Tried "+persists+" times");
		return 0;
	}

	protected boolean sendCmd(byte adr_cmd, byte data){
		if(!comOk())
			return false;
		
		byte[] send = new byte[] {adr_cmd, data};
		
		try{
			if(writeRead(send, response, 0))
				return true;
			else
				return false;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean setStandy(boolean stdby){
		if(!comOk())
			return false;
		
		byte[] send = new byte[] {ADR_CMD_STANDBY, (byte) (stdby? 0x01: 0x00)};
		
		try{
			if(writeRead(send, response, 0))
				return true;
			else
				return false;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean setPID(boolean PID_ENABLED){
		return this.sendCmd(ADR_CMD_PID, (byte)(PID_ENABLED? 1: 0));
	}
	
	private byte SpeedToByte(double speed) {
		return (byte)(speed*42+128);
	}
	/*private double ByteToSpeed(byte speed){
		return (double)(((double)speed-128)/42);
	}*/
	private int unsigned(byte in){
		return (in < 0? in + 256: in);
	}
	private int joinInt(byte[] data, int start){
		int ret = 0;
		ret = ( 
				(0x000000FF & unsigned(data[start])) |
				(0x000000FF & unsigned(data[start+1]))<<8 |
				(0x000000FF & unsigned(data[start+2]))<<16 |
				(0x000000FF & unsigned(data[start+3]))<<24 );
		//Log.i("RescueB.MotorController", "Joining "+data[start+0]+","+data[start+1]+","+data[start+2]+","+data[start+3]+" ("+ret+")");
		return ret;
	}

	public boolean isConnected() {
		byte[] send = new byte[] {0x01, 1};
		
		try{
			if(writeRead(send, response, 0))
				return true;
			else
				return false;
		}catch (Exception e) {
			return false;
		}
	}
}

