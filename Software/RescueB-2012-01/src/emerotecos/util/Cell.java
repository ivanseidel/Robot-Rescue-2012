package emerotecos.util;

import java.util.ArrayList;

import emerotecos.util.Util.Heading;

public class Cell{
	
	public int x, y;
	public Heading from_dir;
	public Heading dir;
	public Cell from;
	public double weight = BFS.MAX_WEIGHT;
	
	public Wall WallNorth;
	public Wall WallEast;
	public Wall WallSouth;
	public Wall WallWest;
	
	public boolean isVisited = false;
	public boolean hasVictim = false;
	public boolean isBlack = false;
	public boolean isRamp = false;
	public boolean isIsolated = false;
	
	public Cell(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void resetToDefault()
	{
		ArrayList<Wall> w = getAllWalls();
		for (int i = 0; i < w.size(); i++)
			w.get(i).resetWall();
		isVisited = false;
		hasVictim = false;
		isBlack = false;
		isRamp = false;
		isIsolated = false;
	}
	
	public Wall getWall(int n){
		if(n == 0)
			return WallNorth;
		if(n == 1)
			return WallEast;
		if(n == 2)
			return WallSouth;
		if(n == 3)
			return WallWest;
		
		System.out.println("RescueB.Map Impossible to reach Wall number "+ n);
		return null;
	}
	
	public ArrayList<Wall> getExistingWalls()
	{
		ArrayList<Wall> ret = new ArrayList<Wall>();
		for (int i = 0; i < 4; i++)
			if (getWall(i).exist())
				ret.add(getWall(i));
		return ret;
	}
	
	public ArrayList<Wall> getAllWalls()
	{
		ArrayList<Wall> ret = new ArrayList<Wall>();
		for (int i = 0; i < 4; i++)
			ret.add(getWall(i));
		return ret;
	}
	
	public Wall getLeastAccurateWall()
	{
		double l = 2.0;
		double n;
		int ret = -1;
		for (int i = 0; i < 4; i++)
		{
			Wall w = getWall(i);
			if (w.exist())
			{
				n = w.getAccuracy();
				if (n < l)
				{	
					ret = i;
					l = n;
				}
			}
		}
		if (ret == -1)
		{
			return null;
		}
		
		return getWall(ret);
	}
	
	public void updateCell(Cell read, ArrayList<Wall> closed)
	{
		if (!closed.contains(WallNorth))
		{
			WallNorth.updateWall(read.WallNorth.getProb());
			closed.add(WallNorth);
		}
		if (!closed.contains(WallEast))
		{
			WallEast.updateWall(read.WallEast.getProb());
			closed.add(WallEast);
		}
		if (!closed.contains(WallSouth))
		{
			WallSouth.updateWall(read.WallSouth.getProb());
			closed.add(WallSouth);
		}
		if (!closed.contains(WallWest))
		{
			WallWest.updateWall(read.WallWest.getProb());
			closed.add(WallWest);
		}
	}
	
	public Wall getRelativeWall(Heading seenHeading, Heading getHeading){
		return getWall( (seenHeading.getValue() + getHeading.getValue()) % 4 );
	}
	
	public boolean setVisited(boolean b)
	{
		if (isVisited == b)
			return false;
		isVisited = b;
		return true;
	}
	
	public boolean setRamp(boolean b)
	{
		if (isRamp == b)
			return false;
		isRamp = b;
		return true;
	}
	
	public boolean setBlack(boolean b)
	{
		if (isBlack == b)
			return false;
		isBlack = b;
		return true;
	}
	
	public boolean setIsolated(boolean b)
	{
		if (isIsolated == b)
			return false;
		isIsolated = b;
		return true;
	}
	
	public boolean reachable(){
		if (WallNorth.exist() & WallWest.exist() & WallSouth.exist() & WallWest.exist())
			setIsolated(true);
		return isIsolated;
	}
	
}