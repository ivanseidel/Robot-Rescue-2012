package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import emerotecos.util.BFS;
import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.Cell;
import emerotecos.util.MapBrain;
import emerotecos.util.MapController;
import emerotecos.util.Statable;
import emerotecos.util.Util.Direction;
import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;
import emerotecos.util.onRobotStatusChange;
import emerotecos.view.SensorsView;

public class Program1 extends RobotActivity implements Statable{
	
	Program1Looper IOIO;
	SensorsView view_sensors;

	// Robot Control Variables
	MapController floor1, floor2;
	ArrayList<Movement> BFS_moves;
	
	boolean runProgram1 = true;

	ArrayList<Movement> moves = new ArrayList<Movement>();

	/*
	 * Logic Part
	 */
	public enum State {
		FirstFloor, ClimbingRamp, SecondFloor, BackToRamp, Unclimbing, LastFloor, GoingHome;
	}

	Cell myBeautifulHouse, myBeautifulHouse2, aux;

	State robotState = State.FirstFloor;

	public String getState() {
		return robotState.toString();
	}
	
	MapController getCurrentMapController() {
		switch (robotState) {
		case FirstFloor:
			return floor1;
		case ClimbingRamp:
			return floor2;
		case SecondFloor:
			return floor2;
		case Unclimbing:
			return floor2;
		case LastFloor:
			return floor1;
		case GoingHome:
			return floor1;
		case BackToRamp:
			return floor2;
		default:
			return floor1;
		}
	}

	/*
	 * ClimbRamp Method
	 */
	public void climbRamp() {
		Direction goDir = Direction.BACKWARD;
		
		IOIO.Head.performRead(15);
		
		IOIO.Move.go(goDir, 1.2);
		//while(isOnRamp()){
		//}
		try {
			boolean didVictim = false;
			while(IOIO.dist1.getDistance() > 12){
				Thread.sleep(30);
				//IOIO.Move.setAngle(IOIO.dist4.getDistance(), 1.0)
				IOIO.Move.setAngle(180, 1.1, (IOIO.dist4.getDistance() - 13) * -0.05);
				//if((IOIO.temp1.victimExist() || IOIO.temp2.victimExist()) && !didVictim){
				if((IOIO.victim.checkVictim()) && !didVictim){
					IOIO.Move.stop();
					for(int i = 0; i < 9; i++){
						try {
							IOIO.oLedVictim.write((i%2) == 0);
							Thread.sleep(200);
						} catch (ConnectionLostException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					IOIO.Move.go(goDir, 1.0);
					didVictim = true;
				}
				/*if(IOIO.dist4.getDistance() < 8 && !fixedHead){
					IOIO.Move.rotateRelative(10, 0.5, 5000);
					IOIO.Move.go(goDir, 1.0);
					fixedHead = true;
				}*/
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		IOIO.Move.stop();
	}

	public void unclimbRamp() {
		IOIO.Head.performRead(-15);
		Direction goDir = Direction.BACKWARD;
		
		//IOIO.Move.go(goDir, 3.2);
		try {
			while(IOIO.dist1.getDistance() > 10){ //8
				Thread.sleep(30);
				IOIO.Move.setAngle(180, 1.4, (IOIO.dist2.getDistance() - 13) * 0.10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		IOIO.Move.stop();
	}
	
	State lState = null;
	boolean isFirstOn = true;
	public Runnable Program1Loop = new Runnable() {
		// Map actualRead = new Map(6,6,0,0);

		public void run() {
			Log.i("RescueB", "Program1 Started");
			while (runProgram1) {
				if(lState != robotState){
					if(lState != null)
						Log.e("RescueB.Program1", "Changed State from "+lState.toString()+" to "+ robotState.toString());
					else
						Log.e("RescueB.Program1", "State started as "+ robotState.toString());
					lState = robotState;
					view_sensors.setMapController(getCurrentMapController());
				}
				if(IOIO.getStatus() == BaseRescueBLooper.RUNNING && isFirstOn){
					isFirstOn = false;
					IOIO.setStatus(BaseRescueBLooper.STOPPED);
				}else if (IOIO.getStatus() == BaseRescueBLooper.RUNNING) {
					
					
					if (robotState == State.FirstFloor) {
						/*
						 * Have just started, and is mapping until ramp
						 */
						
						int result = mainFloor(floor1);
						if (result == FoundRamp)
						{
							aux = floor1.getLastCell();
							aux.setBlack(true);
							robotState = State.ClimbingRamp;
						}else if (result == MazeCompleted)
							floor1.map.deleteLeastAccurateWall();

					} else if (robotState == State.ClimbingRamp) {
						/*
						 * Climbing ramp for the first time
						 */
						climbRamp();
						robotState = State.SecondFloor;
					} else if (robotState == State.SecondFloor) {
						myBeautifulHouse2.setBlack(true);
						int result = mainFloor(floor2);
						
						if (result == MazeCompleted)
						{
							myBeautifulHouse2.setBlack(false);
							myBeautifulHouse2.setVisited(false);
							myBeautifulHouse2.setRamp(false);
							
						/*	Cell goal = myBeautifulHouse2;
							BFS_moves = BFS.createMovementList(floor2, goal);
							ArrayList<Heading> dirs = BFS.createHeadingsList(floor2, goal.x, goal.y);
							view_sensors.path = BFS.createPointList(floor2, dirs);
							*/
							robotState = State.BackToRamp;
						}	
					} else if (robotState == State.BackToRamp) {

						//mainFloor(floor2);

						floorWithGoal(floor2, myBeautifulHouse2);
						if(floor2.getThisCell() == myBeautifulHouse2)
							robotState = State.Unclimbing;
						
					} else if (robotState == State.Unclimbing) {
						
						unclimbRamp();
						floor1.backInTheHouse(aux);
						robotState = State.LastFloor;
						
					} else if (robotState == State.LastFloor) {
						if (aux != null)
							aux.setBlack(true);
						int result = mainFloor(floor1);
						
						if (result == MazeCompleted){
							myBeautifulHouse.setBlack(false);
							myBeautifulHouse.setVisited(false);
							myBeautifulHouse.setRamp(false);
							/*
							Cell goal = myBeautifulHouse2;
							BFS_moves = BFS.createMovementList(floor2, goal);
							ArrayList<Heading> dirs = BFS.createHeadingsList(floor1, goal.x, goal.y);
							view_sensors.path = BFS.createPointList(floor1, dirs);
							*/
							robotState = State.GoingHome;
						}
						
					} else if (robotState == State.GoingHome) {
						//int result = mainFloor(floor1);
						//floorWithGoal(floor1, myBeautifulHouse);
						
						if (floor1.getThisCell() == myBeautifulHouse){
							IOIO.Move.stop();
							IOIO.Move.stop();
							IOIO.Move.stop();
							//Program1.this.finish();
						}
						else
						{
							floorWithGoal(floor1, myBeautifulHouse);
						}
						
					}

				}
			}
			Log.i("RescueB", "Program1 Stopped");
		}
	};

	final static int Nothing = 0;
	final static int FoundRamp = 1;
	final static int MazeCompleted = 2;
	
	/*
	public void followDirs(MapController mc, ArrayList<Movement> moves)
	{
		IOIO.Walk.ajeitadinha(true);
		IOIO.Walk.fixNextSquare(moves.get(0), floor2);

		Movement m = moves.remove(0);
		moveEverything(floor2, m);
	}
	*/
	
	public int floorWithGoal(MapController mc, Cell goal) {
		IOIO.Walk.ajeitadinha(true);
		
		if (!mc.getThisCell().isVisited)
			Mapper.updateMapController(mc, IOIO);
		mc.getThisCell().setVisited(true);
		
		if (isOnRamp()) {
			mc.getThisCell().setRamp(true);
			return FoundRamp;

		}else if (IOIO.light1.isBlack()) {
			mc.getThisCell().setBlack(true);

			moveEverything(mc, mc.reverseLastMovement());
 
		} else {
			
			if (goal == mc.getThisCell()) {
				return MazeCompleted;
			}

			ArrayList<Heading> dirs = BFS.createHeadingsList(mc, goal.x, goal.y);
			view_sensors.path = BFS.createPointList(mc, dirs);

			ArrayList<Movement> moves = MapBrain.getMovementList(mc, goal);
			
			if(!moves.isEmpty()){
				Log.e("RescueB.Program1", "Moved " + moves.get(0).toString());
				IOIO.Walk.fixNextSquare(moves.get(0), mc);
				moveEverything(mc, moves.get(0));
			}
		}
		return Nothing;
	}

	
	public int mainFloor(MapController mc) {
		IOIO.Walk.ajeitadinha(true);
		
		if (!mc.getThisCell().isVisited)
			Mapper.updateMapController(mc, IOIO);
		
		/*
		 * Turn to find victims if cell has 3 walls
		 */
		/*if(!mc.getThisCell().isVisited && !mc.getThisCell().hasVictim && mc.getThisCell().getExistingWalls().size() >= 2){
			IOIO.Move.rotateRelative(-25, 0.5, 3000);
			Mapper.searchVictims(mc, IOIO);
			IOIO.Move.rotateRelative(50, 0.5, 3000);
			Mapper.searchVictims(mc, IOIO);
			IOIO.Move.rotateRelative(-25, 0.5, 3000);
		}*/
		
		mc.getThisCell().setVisited(true);
		
		if (isOnRamp()) {
			mc.getThisCell().setRamp(true);
			return FoundRamp;

		}else if (IOIO.light1.isBlack()) {
			mc.getThisCell().setBlack(true);

			moveEverything(mc, mc.reverseLastMovement());
 
		} else {
			Cell goal = MapBrain.findNextGoalFast(mc);

			if (goal == mc.getThisCell()) {
				return MazeCompleted;
			}

			ArrayList<Heading> dirs = BFS.createHeadingsList(mc, goal.x, goal.y);
			view_sensors.path = BFS.createPointList(mc, dirs);

			ArrayList<Movement> moves = MapBrain.getMovementList(mc, goal);
			
			if(!moves.isEmpty()){
				Log.e("RescueB.Program1", "Moved " + moves.get(0).toString());
				IOIO.Walk.fixNextSquare(moves.get(0), mc);
				moveEverything(mc, moves.get(0));
			}
		}
		return Nothing;
	}

	public void moveEverything(MapController mc, Movement m) {
		// nao altera a ordem!
		RobotActivity.resetOnRamp();
		IOIO.Walk.move(m, mc.getMoveCount());
		mc.move(m);
		Mapper.searchVictims(mc, IOIO);
	}

	public void clear(View v) {
		// moves.clear();
	}

	public void deleteCell(View v){
		getCurrentMapController().getThisCell().resetToDefault();
	}
	
	public void frente(View v) {
		// moves.add(Movement.FRONT);
		getCurrentMapController().move(Movement.FRONT);
	}

	public void back(View v) {
		// moves.add(Movement.BACK);
		getCurrentMapController().move(Movement.BACK);
	}

	public void right(View v) {
		// moves.add(Movement.RIGHT);
		getCurrentMapController().move(Movement.RIGHT);
	}

	public void left(View v) {
		// moves.add(Movement.LEFT);
		getCurrentMapController().move(Movement.LEFT);
	}
	
	public void nextstate(View v){
		if(robotState == State.FirstFloor)
			robotState = State.ClimbingRamp;
		else if(robotState == State.ClimbingRamp)
			robotState = State.SecondFloor;
		else if(robotState == State.SecondFloor)
			robotState = State.BackToRamp;
		else if(robotState == State.BackToRamp)
			robotState = State.Unclimbing;
		else if(robotState == State.Unclimbing)
			robotState = State.LastFloor;
		else if(robotState == State.LastFloor)
			robotState = State.GoingHome;
		else
			Log.e("RescueB", "Impossible to change state > State "+robotState.toString());
	}
	public void previusstate(View v){
		if(robotState == State.ClimbingRamp)
			robotState = State.FirstFloor;
		else if(robotState == State.SecondFloor)
			robotState = State.ClimbingRamp;
		else if(robotState == State.BackToRamp)
			robotState = State.SecondFloor;
		else if(robotState == State.Unclimbing)
			robotState = State.BackToRamp;
		else if(robotState == State.LastFloor)
			robotState = State.Unclimbing;
		else if(robotState == State.GoingHome)
			robotState = State.LastFloor;
		else
			Log.e("RescueB", "Impossible to change state < State "+robotState.toString());
	}

	Thread Program1Thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program1);

		// Instantiates robot control variables
		floor1 = new MapController();
		floor2 = new MapController();

		// Debug mode
		floor1.setHeading(MapController.North);
		floor1.dir = Heading.North;
		floor1.xPos = 0;
		floor1.yPos = 1;

		floor2.dir = Heading.West;
		floor2.xPos = 0;
		floor2.yPos = 0;
		//floor2.getThisCell().setBlack(true);
		floor2.getRelativeCell(0, -1).setBlack(true);
		myBeautifulHouse = floor1.getThisCell();

		myBeautifulHouse2 = floor2.map.getCell(1, 0);
		myBeautifulHouse2.setVisited(true);
		myBeautifulHouse2.setRamp(true);
		myBeautifulHouse2.setBlack(true);
		
		// Configures the SensorView
		view_sensors = (SensorsView) findViewById(R.id.view_sensors);
		view_sensors.setMapController(floor1);
		view_sensors.dragable = true;
		view_sensors.robotState = this;

		actbar.setHomeButtonEnabled(false);
		actbar.setDisplayShowTitleEnabled(false);

		new Thread(new Runnable() {

			public void run() {
				while (true) {
					handler.post(new Runnable() {

						public void run() {
							view_sensors.invalidate();
						}
					});
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();

		Program1Thread = new Thread(Program1Loop);
		Program1Thread.setName("Program1LooperThread");

	}

	@Override
	protected void onPause() {
		/*
		 * runProgram1 = false; try { Program1Thread.join(); } catch
		 * (InterruptedException e) { e.printStackTrace(); }
		 */
		super.onPause();
	}

	@Override
	protected void onResume() {
		runProgram1 = true;
		try {
			Program1Thread.start();
		} catch (Exception e) {
			Log.i("RescueB.Program1", "Thread already running");
		}
		super.onResume();
	}

	@Override
	public void notifyIOIOhasConnected() {
		super.notifyIOIOhasConnected();
		resetOnRamp();
		view_sensors.setup(IOIO);
	}

	public class Program1Looper extends BaseRescueBLooper {

		public Program1Looper(RobotActivity activity) {
			super(activity);

			onRobotStatusChange a = new onRobotStatusChange() {
				public void onChangeStatus(final int actual_state) {
					changeRobotStatusIcon(actual_state == STOPPED ? R.drawable.pause
							: R.drawable.play);
					try {
						if (oLedStatusGreen != null) {
							oLedStatusGreen.write(actual_state == RUNNING);
							oLedStatusRed.write(actual_state == STOPPED);
						}
					} catch (ConnectionLostException e) {
						// TODO
						// ENTERS HERE IF CONNECTION IS LOST
					}
				}
			};

			setOnChangeStatusListner(a);
		}

		boolean ab;

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			// ab = !ab;
			// led_.write(ab);
			Thread.sleep(1000);
		}
	}

	@Override
	protected Program1Looper createIOIOLooper() {
		IOIO = new Program1Looper(this);
		return IOIO;
	}
}
