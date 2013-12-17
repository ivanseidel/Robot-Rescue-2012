package emerotecos.util;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;
import emerotecos.util.Mecanum.MoveInterruption;
import emerotecos.util.Util.Direction;
import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;
import graphics.Line2D;
import graphics.Point2D;

public class MazeWalker {
	
	public final double KcmToDegree = 20.333;
	public int cmToDegree(double centimiters){
		return (int) (centimiters * KcmToDegree);
	}
	
	BaseRescueBLooper ioio;
	
	public double RotateSpeed = 1.0; // 1.0
	public double WalkSpeed = 2.1; //2.0
	
	public MazeWalker(BaseRescueBLooper ioio){
		this.ioio = ioio;
	}
	public static final int OptimumDistance = 15;
	public static final int MaximumThrustableDistance = 28; //58
	public void move(Movement m){
		move(m, 0);
	}
	
	public static final double ErrorIncreaseInWalk = 2; //cm
	public static double IncreaseError = 0;
	public void move(Movement m, int count){
		boolean considerPositionError = true;
		double[] reads = Arrays.copyOf(ioio.Head.performRead(0), 4);
		IncreaseError = count * ErrorIncreaseInWalk;
		if(m == Movement.FRONT){
			if(considerPositionError){
				if(reads[2] > MaximumThrustableDistance )
					goFront(0);
				else
					goFront(OptimumDistance - (reads[2] + 2)%30);
			}else
				goFront(0);
		}
		else if(m == Movement.BACK){
			if(considerPositionError)
				if(reads[0] > MaximumThrustableDistance )
					goBack(0);
				else
					goBack(OptimumDistance - (reads[0] + 2)%30);
			else
				goBack(0);
		}
		else if(m == Movement.RIGHT)
			turnRight();
		else if(m == Movement.LEFT)
			turnLeft();
		
		//int thetaError = ioio.Head.findMinimumAngle();
		
		//ioio.Head.performRead(thetaError);
		
		// Calculates x and y error based on the rest of division by (x-15)%30
	}
	
	public static final int OptimumDistanceToFixNextSquare = 13;
	public static final int MaximumDistanceDeviationToFixNextSquare = 20;
	public static final int MinimumDistanceDeviationToFixNextSquare = 1;
	public static final int MaximumFixToFixNextSquare = 10; // 7
	public static final double FixNextSquareTurnK = 1.3;
	public void fixNextSquare(Movement m, MapController mc){
		if(m != Movement.LEFT && m != Movement.RIGHT){
			double error = 0;
			double[] reads = Arrays.copyOf(ioio.Head.performRead(0), 4);
			if(mc.getRelativeWall(Heading.East).exist()){
				error = reads[1] - OptimumDistanceToFixNextSquare;
			}else if(mc.getRelativeWall(Heading.West).exist()){
				error = OptimumDistanceToFixNextSquare - reads[3];
			}
			
			if(Math.abs(error) >= MinimumDistanceDeviationToFixNextSquare &&
				Math.abs(error) < MaximumDistanceDeviationToFixNextSquare){
				
				double finalError = Math.max(Math.min(FixNextSquareTurnK * error, MaximumFixToFixNextSquare), -MaximumFixToFixNextSquare);
				if(m == Movement.BACK){
					ioio.Move.rotateRelative(finalError *-1, 0.5, 5000);
				}else if(m == Movement.FRONT){
					ioio.Move.rotateRelative(finalError, 0.5, 5000);
				}
			}
		}
	}
	
	MoveInterruption FrontInterruption = new MoveInterruption() {
		public static final double MinimumDistanceToStop = 15.5;
		public static final double MinimumDistanceToContinueGoing = 28;
		
		public void setup() {
			ioio.Head.performRead(0);
		}
		
		public void finish() {
		}
		
		public boolean isInterrupted(boolean TryStopAction) {
			double dist = 0;
			try {
				dist = ioio.dist1.getDistance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(TryStopAction){
				
				if(dist > MinimumDistanceToContinueGoing + MazeWalker.IncreaseError){
					return true;
				}else{
					if(dist <= MinimumDistanceToStop)
						return true;
					else
						return false;
				}
			}
			if(dist <= MinimumDistanceToStop)
				return true;
			
			return false;
		}
	};
	MoveInterruption BackInterruption = new MoveInterruption() {
		public static final double MinimumDistanceToStop = 15.5;
		public static final double MinimumDistanceToContinueGoing = 28;
		
		public void setup() {
			ioio.Head.performRead(0);
		}
		
		public void finish() {
		}
		
		public boolean isInterrupted(boolean TryStopAction) {
			double dist = 0;
			try {
				dist = ioio.dist3.getDistance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(TryStopAction){
				
				if(dist > MinimumDistanceToContinueGoing + MazeWalker.IncreaseError){
					return true;
				}else{
					if(dist <= MinimumDistanceToStop)
						return true;
					else
						return false;
				}
			}
			if(dist <= MinimumDistanceToStop)
				return true;
			
			return false;
		}
	};
	
	final static int block_degrees = 750;
	private void goFront(double cmError){
		ioio.Move.go(Direction.BACKWARD, cmToDegree(29) + cmToDegree(cmError), WalkSpeed, FrontInterruption, 8000);
	}
	private void goBack(double cmError){
		ioio.Move.go(Direction.FORWARD, cmToDegree(29) + cmToDegree(cmError), WalkSpeed, BackInterruption,8000);
	}
	private void turnRight(){
		ioio.Move.rotateRelative(85, RotateSpeed, 6000);
	}
	private void turnLeft(){
		ioio.Move.rotateRelative(-85, RotateSpeed, 6000);
	}
	
	static final int MinimumErrorForFixHeading = 1;// 1
	static final int MinimumErrorForRedoingFixingHeading = 3;// 6
	static final int MaximumDistanceForFixingYPositionBack = 23;
	static final int MinimumDistanceForFixingYPositionBack = 15;
	
	static final int MaximumDistanceForFixingYPositionFront = 10;
	public void ajeitadinha(boolean canRedoit){
		
		// Ajusta lateralmente se necess‡rio
		sideAdjust();
		
		int firstError = findPerpendicularAngle();
		if(Math.abs(firstError) >= MinimumErrorForFixHeading){
			ioio.Move.rotateRelative(-firstError, RotateSpeed/3, 5000);
			if(canRedoit && Math.abs(firstError) >= MinimumErrorForRedoingFixingHeading){
				ajeitadinha(false);
			}else{
				double[] reads = Arrays.copyOf(ioio.Head.performRead(0), 4);
				if(reads[0] < MaximumDistanceForFixingYPositionBack && reads[0] > MinimumDistanceForFixingYPositionBack)
					ioio.Move.go(Direction.BACKWARD, 0, WalkSpeed, FrontInterruption, 8000);
				else if(reads[2] < MaximumDistanceForFixingYPositionBack && reads[2] > MinimumDistanceForFixingYPositionBack)
					ioio.Move.go(Direction.FORWARD, 0, WalkSpeed, BackInterruption, 8000);
				else if(reads[0] < MaximumDistanceForFixingYPositionFront)
					ioio.Move.go(Direction.FORWARD, cmToDegree(2.5), WalkSpeed, 8000);
				else if(reads[2] < MaximumDistanceForFixingYPositionFront)
					ioio.Move.go(Direction.BACKWARD, cmToDegree(2.5), WalkSpeed, 8000);
			}
		}
	}
	
	static final int MinimumErrorForFixPos = 0;
	static final int MinimumErrorForRedoingFixPos = 3;
	static final int MaximumWallDistance = 40;
	public void ajeitadinhaOrtogonal(boolean canRedoit){
		double[] read = ioio.Head.performRead(0);
		int correction = 0;
		if (read[0] > MaximumWallDistance)
			read[0] = 0;
		else
			correction += 13;
		if (read[2] > MaximumWallDistance)
			read[2] = 0;
		else
			correction += 13;
		double firstError = ((read[0] + read[2]) % 30 - correction) / 2;
		if(Math.abs(firstError) > MinimumErrorForFixPos){
			ioio.Move.go(Direction.FORWARD, (int) (-block_degrees * firstError / 30),WalkSpeed);
			if(canRedoit && Math.abs(firstError) >= MinimumErrorForRedoingFixPos){
				ajeitadinha(false);
			}
		}
	}
	
	static final int AngleFindPerpendicular = 18; 
	static final int MinimumDistanceToBeElected = 25; 
	public int findPerpendicularAngle(){
		double [] first = new double[4];
		double [] zeros = new double[4];
		double [] last = new double[4];
		
		first = ioio.Head.performReadAvg(AngleFindPerpendicular, 1);
		zeros = ioio.Head.performReadAvg(0, 1);
		last = ioio.Head.performReadAvg(AngleFindPerpendicular*-1, 1);
		
		// Finds the ones selected to be calculated
		ArrayList<Integer> sensors = new ArrayList<Integer>();
		for(int i = 0; i < 4; i++){
			if(zeros[i] < MinimumDistanceToBeElected)
				sensors.add(i);
		}
		
		// If none of the sensors are in range, then return 0 (no correction needed)
		if(sensors.size() == 0)
			return 0;
		
		// Creates a new Line based on the 2 polar scans
		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		for(Integer a: sensors){
			Point2D p1 = Point2D.fromPolar(first[a], AngleFindPerpendicular);
			Point2D p2 = Point2D.fromPolar(last[a], AngleFindPerpendicular*-1);
			lines.add(new Line2D(p1, p2));
		}
		
		// Normalizes the angle from atan2
		ArrayList<Integer> erros = new ArrayList<Integer>();
		for(Line2D l: lines){
			int error = (int) (90-(l.getLineAngle()+180 )%180);
			erros.add(error);
		}
		
		// Find the average of the errors
		int Error = 0;
		for(Integer e: erros){
			Error += e;
		}
		Error /= erros.size();
		
		return Error;
	}

	public static final double MinimumLateralDistanceForMovingAside = 11;
	public static final double MinumumMaximumLateralDistanceForMovingAside = 22;
	public static final double MaximumLateralDistanceForMovingAside = 14;
	public void sideAdjust() {
		double[] reads = Arrays.copyOf(ioio.Head.performRead(0), 4);
			
		ioio.Head.performRead(0);
		
		try {
			if(reads[3] < MinimumLateralDistanceForMovingAside){ // || (reads[1] > MaximumLateralDistanceForMovingAside && reads[1] < MinumumMaximumLateralDistanceForMovingAside))
				ioio.Move.setAngle(90, 0.6);// 0.5
				while(ioio.dist4.getDistance() < OptimumDistance - 2);
			}else if(reads[1] < MinimumLateralDistanceForMovingAside){ //|| (reads[3] > MaximumLateralDistanceForMovingAside && reads[3] < MinumumMaximumLateralDistanceForMovingAside))
				ioio.Move.setAngle(-90, 0.6);//0.5
				while(ioio.dist2.getDistance() < OptimumDistance - 2);
			}
		}catch (Exception e){
			Log.e("RescueB.MazeWalker", "exception at sideAdjust");
		}
		
		ioio.Move.stop();
	}	
}
