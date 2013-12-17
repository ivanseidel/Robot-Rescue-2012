package emerotecos.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import ioio.lib.api.Uart;

public class TimeoutUART {
	public Uart uart;
	public InputStream in;
	public OutputStream out;
	
	byte[] buf = new byte[1024];
	
	protected void init(){
		in = uart.getInputStream();
		out = uart.getOutputStream();
	}
	
	public TimeoutUART(Uart UART) {
		this.uart = UART;
		init();
	}
	
	protected void clearBuffer(){
		
		try {
			while(in.available() > 0){
				try {
					in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	int pos;
	byte[] ret;
	boolean exIO;
	
	public byte[] getBytes(final int bytes, int maxMillisTimeOut) throws TimeoutException, IOException{
		pos = 0;
		exIO = false;
		
		if(bytes <= 0){
			return new byte[0]; 
		}
		
		ret = new byte[bytes];
		
		long start = System.currentTimeMillis();
		
		
		Thread SafeThread  = new Thread(new Runnable() {
			
			public void run() {
				for(int i = 0; i < bytes; i++){
					try {
						ret[pos] = (byte)in.read();
					} catch (IOException e) {
						
					} catch (Exception e) {
					}
					pos ++;
				}
				
			}
		});
		
		SafeThread.start();
		
		// Loops until the pos < bytes
		// or until timeout is achieved
		while(pos < bytes){
			/*if(exIO){
				SafeThread.interrupt();
				Log.e("RescueB.TimeoutUART", "IOException at getBytes");
				throw new IOException();
			}*/
			if(start + maxMillisTimeOut < System.currentTimeMillis()){
				SafeThread.interrupt();
				Log.e("RescueB.TimeoutUART", "Total of "+pos+" bytes received of "+bytes+", timeout of "+maxMillisTimeOut);
				throw new TimeoutException();//"Total of "+pos+" bytes received of "+bytes+", timeout of "+maxMillisTimeOut);
			}
			
		}
		SafeThread.interrupt();
		return ret;
	}
	
	public void write(byte[] send) throws IOException{
		out.write(send);
	}
	public void write(byte send) throws IOException{
		out.write(send);
	}
	
	public boolean writeRead(byte[] send, byte[] receive, int nBytes, int maxMillisTimeOut) throws TimeoutException{
		clearBuffer();
		try {
			out.write(send);
		} catch (IOException e) {
			Log.e("RescueB.TimeoutUART", "IOException at writeRead");
			e.printStackTrace();
		}
		try {
			byte[] rec = getBytes(nBytes, maxMillisTimeOut);
			for(int i = 0; i < nBytes; i++){
				receive[i] = rec[i];
			}
			return true;
		} catch (TimeoutException e) {
			throw new TimeoutException();
		} catch (IOException e) {
			throw new TimeoutException();
		}
	}
}
