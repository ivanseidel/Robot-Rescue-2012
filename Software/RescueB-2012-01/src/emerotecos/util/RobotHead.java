package emerotecos.util;

import ioio.lib.api.exception.ConnectionLostException;

import java.util.Arrays;

import android.util.Log;

public class RobotHead {
	BaseRescueBLooper ioio;
	IRDistanceSensor ir1, ir2, ir3, ir4;
	ServoController servo;
	public RobotHead (BaseRescueBLooper ioio){
		this.ioio = ioio;
		this.ir1 = ioio.dist1;
		this.ir2 = ioio.dist2;
		this.ir3 = ioio.dist3;
		this.ir4 = ioio.dist4;
		this.servo = ioio.servo;
	}
	
	public double distances[] = new double[50];
	
	public double[] performReadAvg(int angle, int averages){
		double [] avg = new double[4];
		for(int i = 0; i < averages; i ++){
			double[] now = Arrays.copyOf(performRead(angle), 4);
			for(int s = 0; s < 4; s ++){
				avg[s] += now[s];
			}
		}
		for(int i = 0; i < 4; i ++){
			avg[i] /= averages;
		}
		return avg;
	}
	
	public double[] performRead(int angle){
		return performRead(angle, 0);
	}
	public double[] performRead(int angle, int postDelay){
		try{
			servo.setPosition(angle);
		}catch(Exception e){
			Log.e("RescueB.RobotHead", "Could not set head position to "+angle);
		}
		try {
			Thread.sleep(servo.getMilisToArrive() + 100 + postDelay);// 100!!!
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			distances[0] = ir1.getDistance();
			distances[1] = ir2.getDistance();
			distances[2] = ir3.getDistance();
			distances[3] = ir4.getDistance();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Log.e( "RescueB.RobotHead", "Interrupted Exp");
		} catch (ConnectionLostException e1) {
			// TODO Auto-generated catch block
			Log.e( "RescueB.RobotHead", "Connection Lost Exp");
		}
		return distances;
	}
	public double[][] performReads(int[] angles, int postDelay){
		return performReads(angles, postDelay, null);
	}
	
	
	
	// 0,  5, 15, 30 -> 0
	// 0, 1, 2, 3
	public double[][] performReads(int[] angles, int postDelay, Runnable run){
		double[][] reads = new double[angles.length][4];
		
		if(Math.abs(servo.getPosition() - angles[0]) < Math.abs(servo.getPosition() - angles[angles.length-1])){
			for(int i = 0; i < angles.length; i ++){
				performRead(angles[i], postDelay);
				reads[i][0] = distances[0];
				reads[i][1] = distances[1];
				reads[i][2] = distances[2];
				reads[i][3] = distances[3];
				if(run != null)
					run.run();
			}
		}else{
			for(int i = angles.length - 1; i >= 0; i --){
				performRead(angles[i], postDelay);
				reads[i][0] = distances[0];
				reads[i][1] = distances[1];
				reads[i][2] = distances[2];
				reads[i][3] = distances[3];
				if(run != null)
					run.run();
			}
		}
		return reads;
	}
	

	public final static int findMinimumDistance = 70; //cm
	public final static int avarages = 2;
	public int findMinimumAngle(int angles, int firstAngle, int step){
		double tmp;
		double reads[][] = new double[4][angles];
		double filt[][] = new double[4][angles];
		double avg[] = new double[4];
		
		for(int i = 0; i < angles; i ++){
			for(int s = 0; s < avarages; s++){
				
				ioio.Head.performRead(i*step+firstAngle);
				
				for(int a = 0; a < 4; a++){
					tmp = ioio.Head.distances[a];
					reads[a][i] += tmp;
					avg[a] += tmp;
				}
			}
			for(int a = 0; a < 4; a++){
				tmp = ioio.Head.distances[a];
				reads[a][i] /= avarages;
			}
		}
		
		// Calculates avarage of each sensor
		for(int i = 0; i < 4; i ++){
			avg[i] /= avarages*angles;
		}
		
		// Filter variation in all four reads array
		filt[0] = Util.filterVariation(reads[0], 1.5);
		filt[1] = Util.filterVariation(reads[1], 1.5);
		filt[2] = Util.filterVariation(reads[2], 1.5);
		filt[3] = Util.filterVariation(reads[3], 1.5);
		
		// Discovery what sensor has the minimum avarage to be used
		int sensorToUse = Util.whereIsMinimum(avg);
		
		// Finds the minimum value, and then, the crop to be used as base for search in array
		double[] sorted = Arrays.copyOf(reads[sensorToUse], angles);
		Arrays.sort(sorted);
		
		double minVal = sorted[0];
		double crop = Math.min(minVal + 2.0, sorted[sorted.length - 1]);
		
		// Finds start and end of the crop value
		int start = 0, end = angles - 1;
		double now, last = reads[sensorToUse][0];
		for(int i = 1; i < angles - 1; i++){
			now = reads[sensorToUse][i];
			if(now < crop){
				if(now < last && last >= crop)
					start = i;
				else
					end = i;
			}
		}
		
		// Converts start and end to a angle
		int middle = (start + end) / 2;
		int finalAngle = middle*step + firstAngle;
		
		return finalAngle;
	}
	/*public int findMinimumAngle(int angles, int firstAngle, int step){
		
		double tmp, dists[][] = new double[angles][4], integral[] = new double[4];
		
		int i, a;
		
		Log.i("RescueB.MapController", "Reading angles...");
		for(i = 0; i < angles; i ++){
			for(int s = 0; s < avarages; s++){
				
				try{
					ioio.Head.performRead(i*step+firstAngle);
				}catch (Exception e) {
					if(ioio == null)
						Log.e("RescueB.MapController","ioio object is Null");
					else
						if(ioio.Head == null)
							Log.e("RescueB.MapController","ioio.Head object is Null");
					Log.e("RescueB.MapController","Could not performRead with "+i);
					e.printStackTrace();
				}
				
				for(a = 0; a < 4; a++){
					tmp = ioio.Head.distances[a];
					dists[i][a] += tmp;
					integral[a] += tmp;
				}
			}
			for(a = 0; a < 4; a++){
				tmp = ioio.Head.distances[a];
				dists[i][a] /= avarages;
				integral[a] /= avarages;
			}
			
		}
		
		// Discover which sensors will be selected for calculus
		int sensors[] = new int[2];
		if(integral[0] < integral[2])
			sensors[0] = 0;
		else
			sensors[0] = 2;
		
		if(integral[1] < integral[3])
			sensors[0] = 1;
		else
			sensors[0] = 3;
		
		
		// Calculates the minimum
		int minPos[] = new int[]{0,0,0,0},
			follow[] = new int[]{0,0,0,0};  
		int sensN, sens;
		for(sensN = 0; sensN < sensors.length; sensN++){
			sens = sensors[sensN];
			for(i = 0; i < angles; i++){
				if(dists[i][sens] < dists[minPos[sensN]][sens]){
					minPos[sensN] = i;
					Log.i("RescueB.MapController", "New min for "+ sens+ ": "+minPos[sensN]+"["+dists[i][sens]+"]");
					follow[sensN] = 0;
				}else if(dists[i][sens] == dists[minPos[sensN]][sens]){
					follow[sensN] ++;
				}
			}
			minPos[sens] += (int)(follow[sens]/2);
		}
		
		int retAngle = (int) ((minPos[0]+minPos[1])/2)*step+firstAngle;
		
		Log.i("RescueB.MapController", "Calculated for: "+retAngle+ " Sensors "+sensors[0]+"|"+sensors[1]);
		return retAngle;
	}*/
	
	
}
