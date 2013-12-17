package emerotecos.util;

import java.util.ArrayList;

import android.graphics.Point;
import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;



public class MapController {
	public Map map;
	
	public ArrayList<Cell> closed;
	
	public static final int North	= 0;
	public static final int East 	= 90;
	public static final int South 	= 180;
	public static final int West 	= -90;
	
	public int xPos = 0, yPos = 0;
	protected int Angle = North;
	public Heading dir = Heading.North;
	
	private Movement lastMove = null;
	private int moveCount = 0;
	
	public boolean isRobot(int x, int y)
	{
		return x == xPos && y == yPos;
	}
	
	public MapController(){
		map = new Map(22, 22, 11, 11);
		closed = new ArrayList<Cell>();
	}
	
	public MapController(int xMax, int yMax, int xOffset, int yOffset){
		map = new Map(xMax, yMax, xOffset, yOffset);
		closed = new ArrayList<Cell>();
	}

	public int getMoveCount()
	{
		return moveCount;
	}
	
	public int getHeading() {
		return Angle;
	}
	
	public void setHeading(int Heading){
		this.Angle = Heading;
	}
	
	public Movement getLastMovement()
	{
		return lastMove;
	}
	
	public Movement reverseLastMovement()
	{
		return (lastMove!= null? lastMove.rotate(2) : Movement.BACK);
	}
	
	public Cell getRelativeCell(int dX, int dY){
		Point delta = Map.getDeltaPos(dX, dY, dir);
		int x = xPos + delta.x, y = yPos + delta.y;
		return map.getCell(x, y);
	}
	
	public Wall getRelativeWall(int dX, int dY, Heading wall){
		return getRelativeCell(dX, dY).getRelativeWall(dir, wall);
	}
	
	public Wall getRelativeWall(Heading wall){
		return getThisCell().getRelativeWall(dir, wall);
	}

	public Cell getThisCell() {
		return map.getCell(xPos, yPos);
	}
	
	public Point getPosition(){
		return new Point(xPos, yPos);
	}
	
	public void updateMap(Map read, int posX, int posY)
	{
		if (xPos + (read.xSize - posX) >= map.xSize || yPos + (read.ySize - posY) >= map.ySize)
			return;
		if (xPos - posX < 0 || yPos < 0)
			return;
		
		ArrayList<Wall> closed = new ArrayList<Wall>();
		for (int sX =  read.getFirstX(); sX < read.getLastX(); sX++)
		{
			for (int sY = read.getFirstY(); sY < read.getLastY(); sY++)
			{
				int x = this.xPos - (posX - sX);
				int y = this.yPos - (posY - sY);
				
				map.getCell(x, y).updateCell(read.getCell(sX, sY), closed);
			}
		}
	}

	public Cell getLastCell()
	{
		return map.getAdjacentCell(getThisCell(), Heading.South);
	}

	
	public void move(Movement movement) {
		Point p;
		Heading a;
		switch(movement){
		case  FRONT:
			if (getLastMovement() == Movement.BACK)
				moveCount++;
			else
				moveCount = 0;
			
			p = Map.getDeltaPos(0, 1, dir);
			xPos += p.x;
			yPos += p.y;
			break;
		case  BACK:
			if (getLastMovement() == Movement.FRONT)
				moveCount++;
			else
				moveCount = 0;
			
			p = Map.getDeltaPos(0, -1, dir);
			xPos += p.x;
			yPos += p.y;
			break;
		case LEFT:
			moveCount = 0;
			
			a = dir.left();
			dir = a;
			break;
		case RIGHT:
			moveCount = 0;
			
			a = dir.right();
			dir = a;
			break;
		}

		lastMove = movement;
	}

	public void backInTheHouse(Cell aux)
	{
		aux = getThisCell();
		aux.setBlack(true);
		dir = dir.invert();
		move(Movement.FRONT);
	}
}
