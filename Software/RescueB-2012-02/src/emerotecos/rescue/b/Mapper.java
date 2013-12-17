package emerotecos.rescue.b;

import ioio.lib.api.exception.ConnectionLostException;
import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.Map;
import emerotecos.util.MapController;
import emerotecos.util.RobotHead;
import emerotecos.util.Util;
import emerotecos.util.Util.Heading;
import emerotecos.util.Wall;

public class Mapper {
	
	public static final int wallMinFirst = 5;
	public static final int wallMaxFirst = 25;
	
	public static final int wallMinSecond = 35;
	public static final int wallMaxSecond = 55;
	
	public static final int wallMinThird = 23;
	public static final int wallMaxThird = 43;
	
	public static final int wallMinFour = 48;
	public static final int wallMaxFour = 64;
	
	public static final int angle1 = 34;
	public static final int angle2 = 27;
	
	public static double prob_dist1 = 0.9;
	public static double prob_dist2 = 0.75;
	public static double prob_dist3 = 0.6;
	public static double prob_dist4 = 0.55;
	
	public static boolean hasVictimRight = false;
	public static boolean hasVictimLeft = false;
	public static boolean hasVictimFront = false;
	public static boolean hasVictimBack = false;
	public static BaseRescueBLooper lastIoio = null;
	
	public static void searchVictims(MapController robot, BaseRescueBLooper ioio){
		/*hasVictimLeft = false;
		hasVictimRight= false;
		hasVictimFront= false;
		hasVictimBack = false;
		lastIoio = ioio;
		try {
			Thread.sleep(40);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		hasVictimLeft  |= lastIoio.temp1.victimExist();
		hasVictimRight |= lastIoio.temp2.victimExist();
		hasVictimFront |= lastIoio.temp3.victimExist();
		hasVictimBack |= lastIoio.temp4.victimExist();
		
		boolean hasVictim = false;
		
		if(hasVictimLeft){
			if(robot.getRelativeWall(Heading.West).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimRight){
			if(robot.getRelativeWall(Heading.East).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimFront){
			if(robot.getRelativeWall(Heading.North).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimBack){
			if(robot.getRelativeWall(Heading.South).exist()){
				hasVictim = true;
			}
		}*/
		
		
		
		boolean hasVictim = ioio.victim.checkVictim();
		
		if(hasVictim && !robot.getThisCell().hasVictim){
			robot.getThisCell().hasVictim = true;
			for(int i = 0; i < 7; i++){
				try {
					ioio.oLedVictim.write((i%2) == 0);
					Thread.sleep(200);
				} catch (ConnectionLostException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void updateMapController(MapController robot, BaseRescueBLooper ioio){
		RobotHead head = ioio.Head;
		 
		//hasVictimRight = false;
		//hasVictimLeft = false;
		//hasVictimFront = false;
		//hasVictimBack = false;
		lastIoio = ioio;
		
		double reads[][] = head.performReads(new int[]{angle1*-1, angle2*-1, 0, angle2, angle1}, 50/*, new Runnable() {
			int pos = 0;
			public void run() {
				if(pos == 2){
					try {
						Thread.sleep(60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					hasVictimLeft  |= lastIoio.temp1.victimExist();
					hasVictimRight |= lastIoio.temp2.victimExist();
					hasVictimFront |= lastIoio.temp3.victimExist();
					hasVictimBack |= lastIoio.temp4.victimExist();
					//hasVictim |= lastIoio.temp3.victimExist();
				}
				pos++;
			}
		}*/);
		double read[] = reads[2];
		
		Wall w = robot.getRelativeWall(Heading.North);
		w.updateWall((Util.range(wallMinFirst, wallMaxFirst, read[0])? prob_dist1 : 1 - prob_dist1));
		if(!w.exist())
		{
			w = robot.getRelativeWall(0, 1,Heading.North);
			w.updateWall((Util.range(wallMinSecond, wallMaxSecond, read[0])? prob_dist2 : 1 - prob_dist2));
			
			w = robot.getRelativeWall(0, 1, Heading.West);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[1][0])? prob_dist3 : 1 - prob_dist3));
			
			if(!w.exist())
			{
				w = robot.getRelativeWall(-1, 1, Heading.North);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[0][0])? prob_dist4 : 1 - prob_dist4));
			}
			
			w = robot.getRelativeWall(0, 1, Heading.East);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[3][0])? prob_dist3 : 1 - prob_dist3));
			
			if (!w.exist())
			{
				w = robot.getRelativeWall(1, 1, Heading.North);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[4][0])? prob_dist4 : 1 - prob_dist4));
			}
		}
		
		w = robot.getRelativeWall(Heading.East);
		w.updateWall((Util.range(wallMinFirst, wallMaxFirst, read[1])? prob_dist1 : 1 - prob_dist1));
		if(!w.exist())
		{
			w = robot.getRelativeWall(1, 0,Heading.East);
			w.updateWall((Util.range(wallMinSecond, wallMaxSecond, read[1])? prob_dist2 : 1 - prob_dist2));
			
			w = robot.getRelativeWall(1, 0, Heading.North);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[1][1])? prob_dist3 : 1 - prob_dist3));
			
			if(!w.exist())
			{
				w = robot.getRelativeWall(1, 1, Heading.East);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[0][1])? prob_dist4 : 1 - prob_dist4));
			}
			
			w = robot.getRelativeWall(1, 0, Heading.South);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[3][1])? prob_dist3 : 1 - prob_dist3));
			
			if (!w.exist())
			{
				w = robot.getRelativeWall(1, -1, Heading.East);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[4][1])? prob_dist4 : 1 - prob_dist4));
			}
		}
		
		w = robot.getRelativeWall(Heading.South);
		w.updateWall((Util.range(wallMinFirst, wallMaxFirst, read[2])? prob_dist1 : 1 - prob_dist1));
		
		if(!w.exist())
		{
			w = robot.getRelativeWall(0, -1,Heading.South);
			w.updateWall((Util.range(wallMinSecond, wallMaxSecond, read[2])? prob_dist2 : 1 - prob_dist2));
			
			w = robot.getRelativeWall(0, -1, Heading.East);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[1][2])? prob_dist3 : 1 - prob_dist3));
			
			if(!w.exist())
			{
				w = robot.getRelativeWall(1, -1, Heading.South);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[0][2])? prob_dist4 : 1 - prob_dist4));
			}
			
			w = robot.getRelativeWall(0, -1, Heading.West);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[3][2])? prob_dist3 : 1 - prob_dist3));
			
			if (!w.exist())
			{
				w = robot.getRelativeWall(-1, -1, Heading.South);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[4][2])? prob_dist4 : 1 - prob_dist4));
			}
		}
		
		w = robot.getRelativeWall(Heading.West);
		w.updateWall((Util.range(wallMinFirst, wallMaxFirst, read[3])? prob_dist1 : 1 - prob_dist1));
		if(!w.exist())
		{
			w = robot.getRelativeWall(-1, 0,Heading.West);
			w.updateWall((Util.range(wallMinSecond, wallMaxSecond, read[3])? prob_dist2 : 1 - prob_dist2));
			
			w = robot.getRelativeWall(-1, 0, Heading.South);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[1][3])? prob_dist3 : 1 - prob_dist3));
			
			if(!w.exist())
			{
				w = robot.getRelativeWall(-1, -1, Heading.West);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[0][3])? prob_dist4 : 1 - prob_dist4));
			}
			
			w = robot.getRelativeWall(-1, 0, Heading.North);
			w.updateWall((Util.range(wallMinThird, wallMaxThird, reads[3][3])? prob_dist3 : 1 - prob_dist3));
			
			if (!w.exist())
			{
				w = robot.getRelativeWall(-1, 1, Heading.West);
				w.updateWall((Util.range(wallMinFour, wallMaxFour, reads[4][3])? prob_dist4 : 1 - prob_dist4));
			}
		}
		/*boolean hasVictim = false;
		
		if(hasVictimLeft){
			if(robot.getRelativeWall(Heading.West).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimRight){
			if(robot.getRelativeWall(Heading.East).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimFront){
			if(robot.getRelativeWall(Heading.North).exist()){
				hasVictim = true;
			}
		}
		if(hasVictimBack){
			if(robot.getRelativeWall(Heading.South).exist()){
				hasVictim = true;
			}
		}*/
		boolean hasVictim = ioio.victim.checkVictim();
		
		if(hasVictim && !robot.getThisCell().hasVictim){
			robot.getThisCell().hasVictim = true;
			for(int i = 0; i < 7; i++){
				try {
					ioio.oLedVictim.write((i%2) == 0);
					Thread.sleep(200);
				} catch (ConnectionLostException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Map scanMap(RobotHead head, Heading h){
		Map _map = new Map(3,3,1,1);
		
		//double prob_dist1 = 0.8;
		//double prob_dist2 = 0.6;
		
		double reads[][] = head.performReads(new int[]{angle1*-1, angle2*-1, 0, angle2, angle1}, 500);
		double read[] = reads[2];
		
		if(!_map.getWall(Heading.North).exist	( Util.range(wallMinFirst, wallMaxFirst, read[0])) ){
			_map.getWall(0, 1,Heading.North).exist	( Util.range(wallMinSecond, wallMaxSecond, read[0]) );
			
			if(!_map.getWall(0, 1, Heading.West).exist( Util.range(wallMinThird, wallMaxThird, reads[1][0])))
				_map.getWall(-1, 1, Heading.North).exist( Util.range(wallMinFour, wallMaxFour, reads[0][0]));
			
			if(!_map.getWall(0, 1, Heading.East).exist( Util.range(wallMinThird, wallMaxThird, reads[3][0])))
				_map.getWall(1, 1, Heading.North).exist( Util.range(wallMinFour, wallMaxFour, reads[4][0]));
		}
		if(!_map.getWall(Heading.East).exist	( Util.range(wallMinFirst, wallMaxFirst, read[1])) ){
			_map.getWall(1, 0,Heading.East).exist	( Util.range(wallMinSecond, wallMaxSecond, read[1]) );
			
			if(!_map.getWall(1, 0, Heading.North).exist( Util.range(wallMinThird, wallMaxThird, reads[1][1])))
				_map.getWall(1, 1, Heading.East).exist( Util.range(wallMinFour, wallMaxFour, reads[0][1]));
			
			if(!_map.getWall(1, 0, Heading.South).exist( Util.range(wallMinThird, wallMaxThird, reads[3][1])))
				_map.getWall(1, -1, Heading.East).exist( Util.range(wallMinFour, wallMaxFour, reads[4][1]));
		}
		if(!_map.getWall(Heading.South).exist	( Util.range(wallMinFirst, wallMaxFirst, read[2])) ){
			_map.getWall(0, -1,Heading.South).exist	( Util.range(wallMinSecond, wallMaxSecond, read[2]) );
			
			if(!_map.getWall(0, -1, Heading.East).exist( Util.range(wallMinThird, wallMaxThird, reads[1][2])))
				_map.getWall(1, -1, Heading.South).exist( Util.range(wallMinFour, wallMaxFour, reads[0][2]));
			
			if(!_map.getWall(0, -1, Heading.West).exist( Util.range(wallMinThird, wallMaxThird, reads[3][2])))
				_map.getWall(-1, -1, Heading.South).exist( Util.range(wallMinFour, wallMaxFour, reads[4][2]));
		}
		if(!_map.getWall(Heading.West).exist	( Util.range(wallMinFirst, wallMaxFirst, read[3])) ){
			_map.getWall(-1, 0,Heading.West).exist	( Util.range(wallMinSecond, wallMaxSecond, read[3]) );
			
			if(!_map.getWall(-1, 0, Heading.South).exist( Util.range(wallMinThird, wallMaxThird, reads[1][3])))
				_map.getWall(-1, -1, Heading.West).exist( Util.range(wallMinFour, wallMaxFour, reads[0][3]));
			
			if(!_map.getWall(-1, 0, Heading.North).exist( Util.range(wallMinThird, wallMaxThird, reads[3][3])))
				_map.getWall(-1, 1, Heading.West).exist( Util.range(wallMinFour, wallMaxFour, reads[4][3]));
		}
		return _map;
	}
	
	public static Map scanMap(RobotHead head){
		Map _map = new Map(3,3,1,1);
		
		//double prob_dist1 = 0.8;
		//double prob_dist2 = 0.6;
		
		double reads[][] = head.performReads(new int[]{angle1*-1, angle2*-1, 0, angle2, angle1}, 500);
		double read[] = reads[2];
		
		if(!_map.getWall(Heading.North).exist	( Util.range(wallMinFirst, wallMaxFirst, read[0])) ){
			_map.getWall(0, 1,Heading.North).exist	( Util.range(wallMinSecond, wallMaxSecond, read[0]) );
			
			if(!_map.getWall(0, 1, Heading.West).exist( Util.range(wallMinThird, wallMaxThird, reads[1][0])))
				_map.getWall(-1, 1, Heading.North).exist( Util.range(wallMinFour, wallMaxFour, reads[0][0]));
			
			if(!_map.getWall(0, 1, Heading.East).exist( Util.range(wallMinThird, wallMaxThird, reads[3][0])))
				_map.getWall(1, 1, Heading.North).exist( Util.range(wallMinFour, wallMaxFour, reads[4][0]));
		}
		if(!_map.getWall(Heading.East).exist	( Util.range(wallMinFirst, wallMaxFirst, read[1])) ){
			_map.getWall(1, 0,Heading.East).exist	( Util.range(wallMinSecond, wallMaxSecond, read[1]) );
			
			if(!_map.getWall(1, 0, Heading.North).exist( Util.range(wallMinThird, wallMaxThird, reads[1][1])))
				_map.getWall(1, 1, Heading.East).exist( Util.range(wallMinFour, wallMaxFour, reads[0][1]));
			
			if(!_map.getWall(1, 0, Heading.South).exist( Util.range(wallMinThird, wallMaxThird, reads[3][1])))
				_map.getWall(1, -1, Heading.East).exist( Util.range(wallMinFour, wallMaxFour, reads[4][1]));
		}
		if(!_map.getWall(Heading.South).exist	( Util.range(wallMinFirst, wallMaxFirst, read[2])) ){
			_map.getWall(0, -1,Heading.South).exist	( Util.range(wallMinSecond, wallMaxSecond, read[2]) );
			
			if(!_map.getWall(0, -1, Heading.East).exist( Util.range(wallMinThird, wallMaxThird, reads[1][2])))
				_map.getWall(1, -1, Heading.South).exist( Util.range(wallMinFour, wallMaxFour, reads[0][2]));
			
			if(!_map.getWall(0, -1, Heading.West).exist( Util.range(wallMinThird, wallMaxThird, reads[3][2])))
				_map.getWall(-1, -1, Heading.South).exist( Util.range(wallMinFour, wallMaxFour, reads[4][2]));
		}
		if(!_map.getWall(Heading.West).exist	( Util.range(wallMinFirst, wallMaxFirst, read[3])) ){
			_map.getWall(-1, 0,Heading.West).exist	( Util.range(wallMinSecond, wallMaxSecond, read[3]) );
			
			if(!_map.getWall(-1, 0, Heading.South).exist( Util.range(wallMinThird, wallMaxThird, reads[1][3])))
				_map.getWall(-1, -1, Heading.West).exist( Util.range(wallMinFour, wallMaxFour, reads[0][3]));
			
			if(!_map.getWall(-1, 0, Heading.North).exist( Util.range(wallMinThird, wallMaxThird, reads[3][3])))
				_map.getWall(-1, 1, Heading.West).exist( Util.range(wallMinFour, wallMaxFour, reads[4][3]));
		}
		return _map;
	}
}
