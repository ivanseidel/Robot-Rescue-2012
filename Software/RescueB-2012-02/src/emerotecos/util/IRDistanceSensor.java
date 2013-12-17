package emerotecos.util;

import android.util.Log;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;

public class IRDistanceSensor implements Testable{
	private AnalogInput analogInput;

	private double in[] =	{0.40, 0.45, 0.51, 0.61, 0.74, 0.93, 1.08, 1.31, 1.64, 2.31, 2.74, 2.97, 3.15};
	private double out[] = 	{80.0, 70.0, 60.0, 50.0, 40.0, 30.0, 25.0, 20.0, 15.0, 10.0, 8.00, 6.50, 6.10};

	public double lastMeasure = 0;
	
	private double FmultiMap(double val)
	{
	  // take care the value is within range
	  // val = constrain(val, _in[0], _in[size-1]);
	  if (val <= in[0]) return out[0];
	  if (val >= in[in.length-1]) return out[out.length-1];

	  // search right interval
	  int pos = 1;  // _in[0] allready tested
	  while(val > in[pos]) pos++;

	  // this will handle all exact "points" in the _in array
	  if (val == in[pos]) return out[pos];

	  // interpolate in the right segment for the rest
	  return (val - in[pos-1]) * (out[pos] - out[pos-1]) / (in[pos] - in[pos-1]) + out[pos-1];
	}

	
	public IRDistanceSensor(AnalogInput analogInput) {
		this.analogInput = analogInput;
	}
	
	public double getDistance() throws InterruptedException, ConnectionLostException{
		lastMeasure = Math.round(FmultiMap(analogInput.getVoltage())*10)/10.0; 
		return lastMeasure;
	}


	public boolean isConnected() {
		try {
			Log.e("RESCUEB CHECK", "DIST Volt.: "+analogInput.getVoltage());
			return (analogInput.getVoltage() > 0.3);
		} catch (Exception e) {
			return false;
		}
	}
}
