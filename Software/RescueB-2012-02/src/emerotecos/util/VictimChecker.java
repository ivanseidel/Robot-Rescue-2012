package emerotecos.util;

import java.util.ArrayList;


public class VictimChecker {
	
	ArrayList<IRTemperatureSensor> Sensors;
	public static double deltaT = 1.5; // Minimum Difference of temperature to be a "victim"
	public VictimChecker(ArrayList<IRTemperatureSensor> Sensors, double deltaT){
		this.Sensors = Sensors;
		VictimChecker.deltaT = deltaT;
	}
	
	public static boolean hasDifference(ArrayList<Double> vals){
		return hasDifference(vals, deltaT);
	}
	
	public static boolean hasDifference(ArrayList<Double> vals, double deltaT){
		
		for(int i = 0; i < vals.size(); i++){
			double sum = 0;
			int count = 0;
			for(int j = 0; j < vals.size(); j++){
				if(j == i)
					continue;
				
				sum += vals.get(j);
				count++;
			}
			
			if(Math.abs((sum/count) - vals.get(i)) > deltaT)
				return true;
		}
		
		return false;
	}
	
	public boolean checkVictim(){
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for(int i = 0; i < Sensors.size(); i++){
			double t = Sensors.get(i).getTemperature();
			if(t > 0.1)
				vals.add(t);
		}
		
		for(int i = 0; i < vals.size(); i++){
			double sum = 0;
			int count = 0;
			for(int j = 0; j < vals.size(); j++){
				if(j == i)
					continue;
				
				sum += vals.get(j);
				count++;
			}
			
			if(Math.abs((sum/count) - vals.get(i)) > deltaT)
				return true;
		}
		
		return false;
	}
	
}
