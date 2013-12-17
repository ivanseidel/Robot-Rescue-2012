package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class Program2 extends RobotActivity implements Statable {

	public static boolean canGo = false;
	
	Program2Looper IOIO;
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
		Waiting ,FirstRoom, SecondRoom, ThirdRoom, Climbing, LastFloor;
	}

	Cell myBeautifulHouse, myBeautifulHouse2, aux;

	State robotState = State.Waiting;

	public String getState() {
		return robotState.toString();
	}

	MapController getCurrentMapController() {
		switch (robotState) {
		case Waiting:
			return floor1;
		case FirstRoom:
			return floor1;
		case SecondRoom:
			return floor1;
		case ThirdRoom:
			return floor1;
		case Climbing:
			return floor1;
		case LastFloor:
			return floor2;
		default:
			return floor1;
		}
	}

	
	ArrayList<Point> Victims = new ArrayList<Point>();
	int start = 0;
	
	/*
	 * ClimbRamp Method
	 */
	public void climbRamp() {
		Direction goDir = Direction.BACKWARD;

		IOIO.Head.performRead(15);

		IOIO.Move.go(goDir, 1.2);
		try {
			boolean didVictim = false;
			while (IOIO.dist1.getDistance() > 12) {
				Thread.sleep(30);
				IOIO.Move.setAngle(180, 1.1, (IOIO.dist4.getDistance() - 13)
						* -0.05);
				if ((IOIO.temp1.victimExist() || IOIO.temp2.victimExist())
						&& !didVictim) {
					IOIO.Move.stop();
					for (int i = 0; i < 9; i++) {
						try {
							IOIO.oLedVictim.write((i % 2) == 0);
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
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		IOIO.Move.stop();
	}

	public void unclimbRamp() {
		IOIO.Head.performRead(0);
		Direction goDir = Direction.BACKWARD;

		IOIO.Move.go(goDir, 3.2);
		try {
			while (IOIO.dist1.getDistance() > 8)
				;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		IOIO.Move.stop();
	}

	State lState = null;
	boolean isFirstOn = true;
	public Runnable Program2Loop = new Runnable() {
		// Map actualRead = new Map(6,6,0,0);

		public void run() {
			Log.i("RescueB", "Program1 Started");
			while (runProgram1) {
				if (lState != robotState) {
					if (lState != null)
						Log.e("RescueB.Program1",
								"Changed State from " + lState.toString()
										+ " to " + robotState.toString());
					else
						Log.e("RescueB.Program1", "State started as "
								+ robotState.toString());
					lState = robotState;
					view_sensors.setMapController(getCurrentMapController());
				}
				if (IOIO.getStatus() == BaseRescueBLooper.RUNNING && isFirstOn) {
					isFirstOn = false;
					IOIO.setStatus(BaseRescueBLooper.STOPPED);
				} else if (IOIO.getStatus() == BaseRescueBLooper.RUNNING) {
					
					if(robotState == State.Waiting){
						
					}else if(robotState == State.FirstRoom){
						ArrayList<Cell> Goals = new ArrayList<Cell>();
						for(int i = start; i < Victims.size(); i ++){
							Log.e("RescueB.Program1", "New Goal");
							Goals.add(floor1.map.getCell(Victims.get(i).x, Victims.get(i).y));
							start++;
						}
						
						for(Cell goal: Goals){
							Log.e("RescueB.Program1", "Running goal");
							while(floor1.getThisCell() != goal){
								Log.e("RescueB.Program1", "Moving");
								floorWithGoal(floor1, goal);
							}
						}
					}else if(robotState == State.SecondRoom){
						ArrayList<Cell> Goals = new ArrayList<Cell>();
						for(int i = start; i < Victims.size(); i ++){
							Log.e("RescueB.Program1", "New Goal");
							Goals.add(floor1.map.getCell(Victims.get(i).x, Victims.get(i).y));
							start++;
						}
						
						for(Cell goal: Goals){
							while(floor1.getThisCell() != goal)
								floorWithGoal(floor1, goal);
						}
					}else if(robotState == State.ThirdRoom){
						ArrayList<Cell> Goals = new ArrayList<Cell>();
						for(int i = start; i < Victims.size(); i ++){
							Log.e("RescueB.Program1", "New Goal");
							Goals.add(floor1.map.getCell(Victims.get(i).x, Victims.get(i).y));
							start++;
						}
						
						for(Cell goal: Goals){
							while(floor1.getThisCell() != goal)
								floorWithGoal(floor1, goal);
						}
					}else if(robotState == State.Climbing){
						Cell goal = floor1.map.getCell(8, 0);
						while(floor1.getThisCell() != goal)
							floorWithGoal(floor1, goal);
						climbRamp();
					}else if(robotState == State.ThirdRoom){
						ArrayList<Cell> Goals = new ArrayList<Cell>();
						for(int i = start; i < Victims.size(); i ++){
							Log.e("RescueB.Program1", "New Goal");
							Goals.add(floor1.map.getCell(Victims.get(i).x, Victims.get(i).y));
							start++;
						}
						
						for(Cell goal: Goals){
							while(floor2.getThisCell() != goal)
								floorWithGoal(floor2, goal);
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

	public int floorWithGoal(MapController mc, Cell goal) {
		IOIO.Walk.ajeitadinha(true);

		if (!mc.getThisCell().isVisited)
			Mapper.updateMapController(mc, IOIO);
		mc.getThisCell().setVisited(true);

		if (isOnRamp()) {
			mc.getThisCell().setRamp(true);
			return FoundRamp;

		} else if (IOIO.light1.isBlack()) {
			mc.getThisCell().setBlack(true);

			moveEverything(mc, mc.reverseLastMovement());

		} else {

			if (goal == mc.getThisCell()) {
				return MazeCompleted;
			}

			ArrayList<Heading> dirs = BFS
					.createHeadingsList(mc, goal.x, goal.y);
			view_sensors.path = BFS.createPointList(mc, dirs);

			ArrayList<Movement> moves = MapBrain.getMovementList(mc, goal);

			if (!moves.isEmpty()) {
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
		if (!mc.getThisCell().isVisited && !mc.getThisCell().hasVictim
				&& mc.getThisCell().getExistingWalls().size() >= 2) {
			IOIO.Move.rotateRelative(-25, 0.5, 3000);
			Mapper.searchVictims(mc, IOIO);
			IOIO.Move.rotateRelative(50, 0.5, 3000);
			Mapper.searchVictims(mc, IOIO);
			IOIO.Move.rotateRelative(-25, 0.5, 3000);
		}

		mc.getThisCell().setVisited(true);

		if (isOnRamp()) {
			mc.getThisCell().setRamp(true);
			return FoundRamp;

		} else if (IOIO.light1.isBlack()) {
			mc.getThisCell().setBlack(true);

			moveEverything(mc, mc.reverseLastMovement());

		} else {
			Cell goal = MapBrain.findNextGoalFast(mc);

			if (goal == mc.getThisCell()) {
				return MazeCompleted;
			}

			ArrayList<Heading> dirs = BFS
					.createHeadingsList(mc, goal.x, goal.y);
			view_sensors.path = BFS.createPointList(mc, dirs);

			ArrayList<Movement> moves = MapBrain.getMovementList(mc, goal);

			if (!moves.isEmpty()) {
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
	}

	public void clear(View v) {
		// moves.clear();
	}

	public void deleteCell(View v) {
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

	public void nextstate(View v) {
		if (robotState == State.Waiting)
			robotState = State.FirstRoom;
		else if (robotState == State.FirstRoom)
			robotState = State.SecondRoom;
		else if (robotState == State.SecondRoom)
			robotState = State.ThirdRoom;
		else if (robotState == State.ThirdRoom)
			robotState = State.Climbing;
		else if (robotState == State.Climbing)
			robotState = State.LastFloor;
		else
			Log.e("RescueB", "Impossible to change state > State " + robotState.toString());
	}

	public void previusstate(View v) {
		if (robotState == State.FirstRoom)
			robotState = State.Waiting;
		else if (robotState == State.SecondRoom)
			robotState = State.FirstRoom;
		else if (robotState == State.ThirdRoom)
			robotState = State.SecondRoom;
		else if (robotState == State.Climbing)
			robotState = State.ThirdRoom;
		else if (robotState == State.LastFloor)
			robotState = State.Climbing;
		else
			Log.e("RescueB",
					"Impossible to change state < State "
							+ robotState.toString());
	}

	Thread Program2Thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program2);

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
		// floor2.getThisCell().setBlack(true);
		floor2.getRelativeCell(0, -1).setBlack(true);

		// Configures the SensorView
		view_sensors = (SensorsView) findViewById(R.id.view_sensors);
		view_sensors.setMapController(floor1);
		view_sensors.dragable = true;
		view_sensors.robotState = this;

		actbar.setHomeButtonEnabled(false);
		actbar.setDisplayShowTitleEnabled(false);

		new Thread(new Runnable() {
			int h;
			int rec = 0;
			
			//ArrayList<Point> Victims = new ArrayList<Point>();
			
			static final int CMD_CAN_GO = 255;
			static final int CMD_SET_VICTIM_POSITION = 254;
			
			public void run() {
				while (true) {
					handler.post(new Runnable() {

						public void run() {
							view_sensors.invalidate();
						}
					});
					
					//Log.e("RescueB.Program2", "Loop");
					if (otherRobotIn != null) {
						try {
							otherRobotOut.write(100);
							if (otherRobotIn.available() > 0) {
								rec = otherRobotIn.read();
								if(rec == CMD_CAN_GO){
									Log.e("RescueB.Program2", "Free to go!");
									nextstate(null);
								}else if(rec == CMD_SET_VICTIM_POSITION){
									int x = otherRobotIn.read();
									int y = 4 - otherRobotIn.read();
									Victims.add(new Point(x, y));
									getCurrentMapController().map.getCell(x, y).hasVictim = true;
									Log.e("RescueB.Program2", "Victim at "+x + " - " + y);
								}else{
									Log.e("RescueB.Program2", "No Command! " + rec);
								}
								
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("RescueB.Program2", "Error");
							pair(null);
							try{
								Thread.sleep(1000);
							}catch(Exception f){
								
							}
						}
					}else{
					}
					try{
						Thread.sleep(600);
					}catch(Exception e){
						
					}
					/*if(otherRobotOut != null){
						h++;
						try {
							otherRobotOut.write(h);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}*/
					
				}
			}
		}).start();

		Program2Thread = new Thread(Program2Loop);
		Program2Thread.setName("Program2LooperThread");

		/*
		 * Init Bluetooth
		 */
		initBT();
	}

	/*
	 * Bluetooth implementation
	 */
	public static BluetoothAdapter adapter;
	public static BluetoothDevice otherRobotDevice;
	public static BluetoothSocket otherRobotSocket;

	public static OutputStream otherRobotOut;
	public static InputStream otherRobotIn;

	public void initBT() {
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	static boolean tryCon = false;
	public void pair(final View v) {
		Log.e("RescueB.Program2", "Search stated");
		
		try{
			if(otherRobotIn != null)
				otherRobotIn.close();
			if(otherRobotOut!= null)
				otherRobotOut.close();
			if(otherRobotSocket != null)
				otherRobotSocket.close();
		}catch(Exception e){
			
		}
		if(!tryCon){
			if(v != null)
				((Button)v).setEnabled(false);
			new Thread(new Runnable() {
				int f = 0;
				Set<BluetoothDevice> devices = adapter.getBondedDevices();
				public void run() {
					for (BluetoothDevice device : devices) {
						Log.e("RescueB.Program2", "Found: '" + device.getName() + "'");
						if (f == 0) {
							otherRobotDevice = device;
							Log.e("RescueB.Program2", "Found!");
							try { // 00:16:53.08:FF:9B
								otherRobotSocket = device
										.createRfcommSocketToServiceRecord(UUID
												.fromString("00001101-0000-1000-8000-00805F9B34FB"));
								otherRobotSocket.connect();
							} catch (IOException e) {
								e.printStackTrace();
							}
	
							try {
								otherRobotOut = otherRobotSocket.getOutputStream();
								otherRobotIn = otherRobotSocket.getInputStream();
							} catch (IOException e) {
								e.printStackTrace();
							}
	
							try {
								otherRobotOut.write(new byte[] { 100, 101, 102 });
								Log.e("RescueB.Program2", "Send 3 bytes! =]");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						f++;
					}
					if (f == 0) {
						Log.e("RescueB.Program2", "None found");
					} else {
						Log.e("RescueB.Program2", "Found " + f + " devices");
					}
					handler.post(new Runnable() {
						
						public void run() {
							if(v != null)
								((Button)v).setEnabled(true);
							tryCon = false;
						}
					});
				}
			}).start();
		}
	}

	// -------------------

	@Override
	protected void onResume() {
		runProgram1 = true;
		try {
			Program2Thread.start();
		} catch (Exception e) {
			Log.i("RescueB.Program2", "Thread already running");
		}
		super.onResume();
	}

	@Override
	public void notifyIOIOhasConnected() {
		super.notifyIOIOhasConnected();
		resetOnRamp();
		view_sensors.setup(IOIO);
	}

	public class Program2Looper extends BaseRescueBLooper {

		public Program2Looper(RobotActivity activity) {
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
					}
				}
			};

			setOnChangeStatusListner(a);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			Thread.sleep(1000);
		}
	}

	@Override
	protected Program2Looper createIOIOLooper() {
		IOIO = new Program2Looper(this);
		return IOIO;
	}
}
