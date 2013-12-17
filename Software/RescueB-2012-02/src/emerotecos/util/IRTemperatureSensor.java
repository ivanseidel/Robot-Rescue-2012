package emerotecos.util;

import ioio.lib.api.TwiMaster;
import android.util.Log;

public class IRTemperatureSensor implements Testable{
	
	protected static byte ADR_SET_ADDRESS = 0x2E;//0x2e
	
	protected static byte ADR_GET_TEMP1 = 0x07;
	
	protected byte Address = 0x00;
	protected TwiMaster comm;
	
	public static double victimTemperature = 23.5;
	
	public IRTemperatureSensor(byte Address, TwiMaster comm) {
		this.Address = Address;
		this.comm = comm;
	}
	
	protected boolean comOk(){
		return comm != null;
	}
	
	public double getTemperature(){
		if(!comOk())
			return 0.0;
		
		byte[] send = new byte[] {ADR_GET_TEMP1};
		byte[] response = new byte[] {0,0,0,0,0};
		try{
			if(comm.writeRead(Address, false, send, 1, response, 3)){
				double t = (unsigned(response[1])*256 + unsigned(response[0]))/50.0 - 273.15;
				return t;
			}else
				return 0.0;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0.0;
	}
	
	private int unsigned(byte in){
		return (in < 0? in + 256: in);
	}
	
	public boolean victimExist(){
		return (getTemperature() > victimTemperature);
	}
	
	public boolean changeAddress(byte newAddress){
		byte[] response = new byte[] {0,0,0,0,0};
		byte[] send = new byte[] {ADR_SET_ADDRESS, 0x00, 0x00, 0};
		send[3] = CRC8.calc(send, 3);
		
		try{
			Log.e("RescueB.IRTemperatureSensor", "$: ERASE: "+(comm.writeRead(0, false, send, 4, response, 0)? "COMPLETED!": "ERROR"));
			send = new byte[] {ADR_SET_ADDRESS, newAddress, 0x00,0};
			send[3] = CRC8.calc(send, 3);
			Thread.sleep(50);
			Log.e("RescueB.IRTemperatureSensor", "$: WRITE: "+(comm.writeRead(0, false, send, 4, response, 0)? "COMPLETED!": "ERROR") );
			Thread.sleep(50);	
		}catch (Exception e) {
			//e.printStackTrace();
		}
		return false;
	}

	public boolean isConnected() {
		byte[] send = new byte[] {ADR_GET_TEMP1};
		byte[] response = new byte[] {0,0,0,0,0};
		try{
			if(comm.writeRead(Address, false, send, 1, response, 3)){
				return true;
			}
		}catch (Exception e) {
			return false;
		}
		return false;
	}

}
