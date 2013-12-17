package emerotecos.util;

import java.util.ArrayList;

import android.graphics.Point;
import emerotecos.util.Util.Heading;

public class Map {
	protected Cell map[][];
	public int xOffset = 0;
	public int yOffset = 0;
	protected int xSize, ySize;
	
	public int xSize(){
		return xSize;
	}
	
	public int ySize(){
		return ySize;
	}
	
	
	public int xOffset(){
		return xOffset;
	}
	
	public int yOffset(){
		return yOffset;
	}
	
	public void resetWeights()
	{
		ArrayList<Cell> all = getAllCells();
		for (int i = 0; i < all.size(); i++)
			all.get(i).weight = BFS.MAX_WEIGHT;
	}
	
	public Map(int xMax, int yMax, int xOffset, int yOffset){
		this.xSize = xMax;
		this.ySize = yMax;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		
		map = new Cell[xMax][yMax];
		
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				Cell c = new Cell(x - xOffset, y - yOffset);
				
				c.WallNorth = new Wall();
				c.WallEast = new Wall();
				
				if(y == 0){
					c.WallSouth = new Wall();
					
				}else{
					c.WallSouth = map[x][y-1].WallNorth;
				}
				
				if(x == 0){
					c.WallWest = new Wall();
				}else{
					c.WallWest = map[x-1][y].WallEast;
				}
							
				map[x][y] = c;
			}	
		}
	}
	
	public int getMaxX()
	{
		return xSize;
	}
	
	public int getMaxY()
	{
		return ySize;
	}
	
	public int getFirstX()
	{
		return -xOffset;
	}
	
	public int getLastX()
	{
		return xSize - xOffset;
	}
	
	public int getFirstY()
	{
		return -yOffset;
	}
	
	public int getLastY()
	{
		return ySize - yOffset;
	}
	
	public Cell getCell(int x, int y){
		x += xOffset;
		y += yOffset;
		if (x >= xSize || x < 0)
			return null;
		if (y >= ySize || y < 0)
			return null;
		return map[x][y];
	}
	
	public ArrayList<Cell> getAllCells()
	{
		ArrayList<Cell> ret = new ArrayList<Cell>();
		for (int x = getFirstX(); x < getLastX(); x++)
			for (int y = getFirstY(); y < getLastY(); y++)
				ret.add(getCell(x, y));
		
		return ret;
	}
	
	public ArrayList<Cell> getCellSquare(int xPos, int yPos, int radius)
	{
		ArrayList<Cell> ret = new ArrayList<Cell>();
		for (int x = xPos - radius; x <= xPos + radius; x++)
			for (int y = yPos - radius; y <= yPos + radius; y++)
			{
				Cell c = getCell(x, y);
				if (c != null)
					ret.add(c);
			}
		return ret;
	}
	
	public ArrayList<Cell> getBlackCells()
	{
		ArrayList<Cell> all = getAllCells();
		ArrayList<Cell> ret = new ArrayList<Cell>();
		for (int i = 0; i < all.size(); i++)
		{
			Cell c = all.get(i);
			if (c.isBlack)
				ret.add(c);
		}
		return ret;
	}
	
	public ArrayList<Cell> getVisitedCells()
	{
		ArrayList<Cell> all = getAllCells();
		ArrayList<Cell> ret = new ArrayList<Cell>();
		for (int i = 0; i < all.size(); i++)
		{
			Cell c = all.get(i);
			if (c.isVisited)
				ret.add(c);
		}
		return ret;
	}
	
	public Cell getRampCell()
	{
		ArrayList<Cell> visited = getVisitedCells();
		for (int i = 0; i < visited.size(); i++)
		{
			Cell c = visited.get(i);
			if (c.isRamp)
				return c;
		}
		return null;
	}
	
	public void deleteBlackCells()
	{
		ArrayList<Cell> cells = getBlackCells();
		for (Cell c : cells)
			c.setBlack(false);
	}
	
	public void deleteLeastAccurateWall()
	{
		Wall l = null;
		boolean first = true;
		for (int x = getFirstX(); x < getLastX(); x++)
		{
			for (int y = getFirstY(); y < getLastY(); y++)
			{
				Cell c = getCell(x, y);
				if (first)
				{
					l = c.getLeastAccurateWall();;
					if (l != null)
						first = false;
				}
				else
				{
					Wall n = c.getLeastAccurateWall();
					if (n != null && n.getAccuracy() < l.getAccuracy())
					{
						l = n;
					}
				}
			}
		}
		if (l != null)
			l.setDeleted(true);
		deleteBlackCells();
	}
	
	public Wall getWall(int x, int y, Heading dir){
		return getCell(x, y).getRelativeWall(Heading.North, dir);
	}
	
	public Wall getWall(Heading dir){
		return getCell(0, 0).getRelativeWall(Heading.North, dir);
	}
	
	public static Point getDeltaPos(int dX, int dY, Heading d)
	{
		if (d == null)
			return null;
		switch(d){
		case North:
			return new Point(dX, dY);
		case East:
			return new Point(dY, -dX);
		case South:
			return new Point(-dX, -dY);
		case West:
			return new Point(-dY, dX);
		}
		return null;
	}
	
	public ArrayList<Cell> getAllAdjacentCells(Cell c)
	{
		ArrayList<Cell> ret = new ArrayList<Cell>();
		ArrayList<Heading> headings = Heading.getAllHeadings();
		for (Heading h : headings)
		{
			Cell adj = getAdjacentCell(c, h);
			if (adj != null && !c.getWall(h.getValue()).exist())
				ret.add(adj);	
		}
		return ret;
	}
	
	public ArrayList<Cell> getVisitedAdjacentCells(Cell c)
	{
		ArrayList<Cell> ret = new ArrayList<Cell>();
		ArrayList<Cell> all = getAllAdjacentCells(c);
		for (Cell cell : all)
			if (cell.isVisited)
				ret.add(cell);
		return ret;
	}
	
	public Cell getAdjacentCell(Cell c, Heading d)
	{
		Point delta = getDeltaPos(0, 1, d);
		return getCell(c.x + delta.x, c.y + delta.y);
	}
	
	public Cell getRelativeCell(Heading robotHeading, int xPos, int yPos, int dX, int dY){
		Point delta = getDeltaPos(dX, dY, robotHeading);
		int x = xPos + delta.x, y = yPos + delta.y;
		return getCell(x, y);
	}
	
	public Wall getRelativeWall(Heading robotHeading, int xPos, int yPos, int dX, int dY, Heading wall){
		return getRelativeCell(robotHeading, xPos, yPos, dX, dY).getRelativeWall(robotHeading, wall);
	}
	
	
	
}
