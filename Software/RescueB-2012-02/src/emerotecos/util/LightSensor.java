package emerotecos.util;

import android.util.Log;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;

public class LightSensor {
	private static final double Histeresys = 15;

	public AnalogInput in;
	
	public double LightOffset = 36;
	
	public LightSensor(AnalogInput in){
		this.in = in;
	}
	
	public double getValue(){
		try {
			return Math.round(in.read()*100);
		} catch (InterruptedException e) {
			Log.e("RescueB.LightSensor", "Light sensor was interrupted");
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			Log.e("RescueB.LightSensor", "Conection lost while reading sensor");
			e.printStackTrace();
		}
		return 0;
	}
	
	public void setBlackOffset(){
		LightOffset = getValue() + Histeresys;
	}
	
	public boolean isBlack(){
		for(int i = 0; i < 5; i ++){
			if(!(getValue() < LightOffset))
				return false;
		}
		return true;
	}
}
