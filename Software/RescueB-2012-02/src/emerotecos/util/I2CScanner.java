package emerotecos.util;

import java.util.ArrayList;

import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;

public class I2CScanner {
	private TwiMaster comm;

	public I2CScanner(TwiMaster comm) {
		this.comm = comm;
	}
	
	public ArrayList<Integer> scan(int init, int end) throws ConnectionLostException, InterruptedException{
		ArrayList<Integer> devices = new ArrayList<Integer>();
		byte[] ret = new byte[] {0};
		byte[] send = new byte[] {1};
		
		for(int i = init; i <= end; i++){
			if(comm.writeRead(i, false, send, 1, ret, 0))
				devices.add(i);
			Thread.sleep(5);
		}
		
		return devices;
	}
}
