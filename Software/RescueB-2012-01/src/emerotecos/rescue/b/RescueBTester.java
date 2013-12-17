package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.IRDistanceSensor;
import emerotecos.util.IRTemperatureSensor;
import emerotecos.util.MotorController;
import emerotecos.util.RobotError;
import emerotecos.util.Util;
import emerotecos.util.Util.Direction;

public class RescueBTester {
	public static boolean _isMotorInterfaceError = false;
	public static boolean _isMotorMotorError[] = {false, false, false, false};
	public static boolean _isMotorTemperatureError[] = {false, false};
	public static boolean _isLighSensorError[] = {false, false};
	
	public static final int ERR_TEMPERATURE_NOT_CONNECTED = 4000; 
	public static final int ERR_DISTANCE_NOT_CONNECTED = 4001; 
	
	public static ArrayList<RobotError> checkMotorInterface(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();
		try{
			if(!ioio.Motors.isConnected()){
				errors.add(new RobotError("Motor interface not responding", 1001, "The board that controls motors is not responding. Reset it and try egain", RobotError.E_ERROR, null));
			}else{
				errors.addAll(checkMotors(ioio));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return errors;
	}
	
	public static ArrayList<RobotError> checkMotors(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();

		for(int i = 1; i <= 4; i ++){
			if(_isMotorMotorError[i-1]){
				RobotError ret = new RobotError("Motor "+i+" is not working", 1002+i, "The motor #"+i+" is not working, maybe there is something blocking it's rotation", null);
				ret.type = RobotError.E_ERROR;
				errors.add(ret);
			}
		}
		return errors;
	}
	
	public static ArrayList<RobotError> checkTemperatureInterface(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();
		
		ArrayList<IRTemperatureSensor> sensors = new ArrayList<IRTemperatureSensor>();
		sensors.add(ioio.temp1);
		sensors.add(ioio.temp2);
		
		for(int i = 0; i < 2; i++){
			if(!sensors.get(i).isConnected())
				errors.add(new RobotError("Temperature sensor "+(i+1)+" is not responding", 2001+i, "The temperature sensor #"+i+" is not responding", RobotError.E_ERROR, null));
		}
		
		return errors;
	}
	
	public static ArrayList<RobotError> checkDistanceSensors(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();
		
		ArrayList<IRDistanceSensor> sensors = new ArrayList<IRDistanceSensor>();
		sensors.add(ioio.dist1);
		sensors.add(ioio.dist2);
		sensors.add(ioio.dist3);
		sensors.add(ioio.dist4);
		
		for(int i = 0; i < 4; i++){
			if(!sensors.get(i).isConnected())
				errors.add(new RobotError("Distance sensor "+(i+1)+" is not connected", ERR_DISTANCE_NOT_CONNECTED+i, "The distance sensor #"+(i+1)+" is not connected", RobotError.E_ERROR, null));
		}
		
		return errors;
	}
	
	public static ArrayList<RobotError> checkLighSensorInterface(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();

		for(int i = 1; i <= 2; i ++){
			if(_isLighSensorError[i-1]){
				RobotError ret = new RobotError("Light sensor "+i+" is not connected", 3001+i, "The light sensor #"+i+" probably is not connected to the IOIO interface", null);
				ret.type = RobotError.E_INFO;
				errors.add(ret);
			}
		}
		
		return errors;
		
	}
	
	public static ArrayList<RobotError> searchDevices(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();
		
		try {
			if(ioio.I2C1Scanner != null){
				ArrayList<Integer> founds = ioio.I2C1Scanner.scan(0, 100);
				for(int i = 0; i < founds.size(); i++){
					errors.add(new RobotError("Device Found", 4000, "ADR: "+founds.get(i).intValue()+" found", null));
				}
			}else{
				errors.add(new RobotError("Object not found", 1, "Object I2CScanner wasn`t found", RobotError.E_ERROR, null));
			}
			
		} catch (ConnectionLostException e) {
			errors.add(new RobotError("Conection lost", 4001, "Connection was lost while trying to communicate through SMBUS", null));
		} catch (InterruptedException e) {
			errors.add(new RobotError("Conection Interrupted", 4001, "Connection was interrupted while trying to communicate through SMBUS", null));
		}
		return errors;
	}
	
	public static ArrayList<RobotError> performTests(BaseRescueBLooper ioio){
		ArrayList<RobotError> errors = new ArrayList<RobotError>();
		
		if(ioio.isConnected()){
			errors.addAll(RescueBTester.searchDevices(ioio));
			errors.addAll(RescueBTester.checkMotorInterface(ioio));
			errors.addAll(RescueBTester.checkTemperatureInterface(ioio));
			errors.addAll(RescueBTester.checkLighSensorInterface(ioio));
			//errors.addAll(RescueBTester.checkDistanceSensors(ioio));
		}else{
			errors.add(new RobotError("IOIO Not connected", 0, "IOIO Board wasn`t found, try connecting or re-connecting the USB cable o the board", RobotError.E_INFO, null));
		}
		errors.remove(null);
		return errors;
	}
	
	public static void logMotorInertia(BaseRescueBLooper IOIO, Direction dir){
		int a = 1;
		File file;
		do{
		 file = new File(RescueB.mainDir, "incercia_"+a+".csv");
		 a++;
		}while(file.exists());
		
		OutputStreamWriter flSt = null; 
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            flSt = new OutputStreamWriter(fOut);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        long posF = 0;
        for(double i = 0.1; i <= 2.0; i+=0.05){
        	posF = IOIO.Move.go(dir, 90, i, 6000);
        	
        	try {
        		
        		Thread.sleep(500);
				flSt.write(i+","+posF+","+IOIO.Motors.getEncoderPosition(MotorController.MOTOR_1)+'\n');
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        /*8for(double i = 0.1; i <= 2.0; i+=0.05){
        	posF = IOIO.Move.go(Direction.FORWARD, 90, i, 6000);
        	
        	try {
        		
        		Thread.sleep(500);
				flSt.write(i+","+posF+","+IOIO.Motors.getEncoderPosition(MotorController.MOTOR_1)+'\n');
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }*/

        try {
        	if(flSt != null){
        		flSt.flush();
        		flSt.close();
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log360degree(BaseRescueBLooper IOIO){
		int a = 1;
		File file;
		do{
		 file = new File(RescueB.mainDir, "degrees_"+a+".csv");
		 a++;
		}while(file.exists());
		
		OutputStreamWriter flSt = null; 
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            flSt = new OutputStreamWriter(fOut);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        double[] reads = new double[360];
        int s;
        for(int i = -45; i < 45; i++){
        	
        	IOIO.Head.performRead(i);
        	s = i + 45;
        	reads[s] = IOIO.Head.distances[0];
        	reads[s+90] = IOIO.Head.distances[1];
        	reads[s+180] = IOIO.Head.distances[2];
        	reads[s+270] = IOIO.Head.distances[3];
        }
        double[] filtered= Util.filterVariation(reads, 1.5);
        try {
        	for(int i =0; i < 360; i++){
        		flSt.write(i+","+reads[i]+","+filtered[i]+'\n');
        	}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
        	if(flSt != null){
        		flSt.flush();
        		flSt.close();
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
