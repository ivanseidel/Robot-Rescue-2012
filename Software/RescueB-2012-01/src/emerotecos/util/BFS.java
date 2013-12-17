package emerotecos.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Point;
import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;


public class BFS {
	public static ArrayList<Point> createPointList(MapController mc, ArrayList<Heading> directions)
	{	
		ArrayList<Point> ret = new ArrayList<Point>();
		if (directions.isEmpty())
			return ret;
		Cell actual = mc.getThisCell();
		ret.add(new Point(actual.x, actual.y));
		for (Heading next : directions)
		{
			actual = mc.map.getAdjacentCell(actual, next);
			ret.add(new Point(actual.x, actual.y));
		}
		
		return ret;
	}
	
	public static int MAX_WEIGHT = 10000;
	
	public static int calculateWeight(MapController mc, ArrayList<Heading> directions)
	{
		if (directions.isEmpty())
			return MAX_WEIGHT;
		int weight = 0;
		Cell c = mc.getThisCell();
		Heading h = mc.dir; 
		for (Heading next : directions)
		{
			for (Movement move : getRelativeMovement(next ,h))
			{
				switch (move){
				case FRONT:
					weight += FORWARD_WEIGHT;
					break;
				case RIGHT:
					weight += RIGHT_WEIGHT;
					break;
				case BACK:
					weight += BACKWARD_WEIGHT;
					break;
				case LEFT:
					weight += LEFT_WEIGHT;
					break;
				}
			}
			
			c = mc.map.getAdjacentCell(c, next);
			if (c.isVisited)
				weight += SEEN_WEIGHT;
			else if (c.isBlack)
				return MAX_WEIGHT;
			h = next;
		}
		int walls = c.getExistingWalls().size();
		if (walls <= 3)
			weight += (3 - walls) * NEIGHBOR_WALLS_WEIGHT;
		
		int visited_adjacent = mc.map.getVisitedAdjacentCells(c).size();
		weight += (4 - visited_adjacent) * NEIGHBOR_VISITED_WEIGHT;
		return weight;
	}

	public static ArrayList<Movement> createMovementList(MapController mc, ArrayList<Heading> directions)
	{
		ArrayList<Movement> ret = new ArrayList<Movement>();
		if (directions.isEmpty())
			return ret;
		Heading actual = mc.dir;
		for (int i = 0; i < directions.size(); i++)
		{
			ret.addAll(getRelativeMovement(directions.get(i), actual));
			actual = directions.get(i);
		}
		return ret;
	}
	
	public static ArrayList<Movement> createMovementList(MapController mc, Cell goal)
	{
		ArrayList<Movement> ret = new ArrayList<Movement>();
		ArrayList<Heading> directions = createHeadingsList(mc, goal.x, goal.y);
		if (directions.isEmpty())
			return ret;
		Heading actual = mc.dir;
		for (int i = 0; i < directions.size(); i++)
		{
			ret.addAll(getRelativeMovement(directions.get(i), actual));
			actual = directions.get(i);
		}
		return ret;
	}
	
	public static ArrayList<Movement> getRelativeMovement(Heading whereToMove, Heading robotHeading){
		ArrayList<Movement> moves = new ArrayList<Movement>();
		
		if (whereToMove == robotHeading)
		{
			moves.add(Movement.FRONT);
		}
		else if (robotHeading.right() == whereToMove)
		{
			moves.add(Movement.RIGHT);
			moves.add(Movement.FRONT);
		}
		else if (robotHeading.left() == whereToMove)
		{
			moves.add(Movement.LEFT);
			moves.add(Movement.FRONT);
		}
		else
		{
			moves.add(Movement.BACK);
		}
		
		return moves;
	}
	
	public static ArrayList<Heading> createHeadingsList(MapController mc, int goalX, int goalY)
	{
		return doBFS(mc, goalX, goalY);
	}
	
	private static ArrayList<Heading> doBFS(MapController mc, int goalX, int goalY)
	{
		ArrayList<Cell> closed = new ArrayList<Cell>();
		Queue<Cell> open = new LinkedList<Cell>();
		
		ArrayList<Heading> ret = new ArrayList<Heading>();
		open.add(mc.getThisCell());
		Cell actual;
		while (true)
		{
			Thread.yield();
			if (open.isEmpty())
				return ret;
			actual = open.poll();

			if (actual.x == goalX && actual.y == goalY)
			{
				break;
			}
			
			for (Heading h : Heading.getAllHeadings())
			{
				Cell c = mc.map.getAdjacentCell(actual, h);
				int wallValue = h.invert().getValue();
				if (c != null && !closed.contains(c) && !open.contains(c) && !c.getWall(wallValue).exist() && !c.isBlack)
				{
					c.from_dir = h;
					c.from = actual;
					open.add(c);
				}
			}
			
			closed.add(actual);
		}
		while (actual != mc.getThisCell())
		{
			ret.add(actual.from_dir);
			actual = actual.from;
		}
		Collections.reverse(ret);
		return ret;
	}
	
private static int FORWARD_WEIGHT = 1, LEFT_WEIGHT = 0, RIGHT_WEIGHT = 0, BACKWARD_WEIGHT = 2, SEEN_WEIGHT = 2;
	
	public static int NEIGHBOR_WALLS_WEIGHT = 4, NEIGHBOR_VISITED_WEIGHT = 4;
	public static int DEX_NEIGHBOR_WALLS_WEIGHT = 4, DEX_NEIGHBOR_VISITED_WEIGHT = 6;
	
	public static boolean Dexter(MapController mc)
	{
		ArrayList<Cell> closed = new ArrayList<Cell>();
		ArrayList<Cell> open = mc.map.getAllCells();

		Cell actual;
		mc.map.resetWeights();
		mc.getThisCell().weight = 0;
		mc.getThisCell().dir = mc.dir;
		
		
		while (true)
		{
			Thread.yield();
			int least = -1;
			double leastWeight = BFS.MAX_WEIGHT;
			for (int i = 0; i < open.size(); i++)
			{
				if (!closed.contains(open.get(i)))
					if (leastWeight > open.get(i).weight)
					{
						least = i;
						leastWeight = open.get(i).weight;
					}
			}
			if (least == -1)
				break;
			
			actual = open.get(least);
			
			if (!actual.isVisited)
				return true;
			
			for (Heading h : Heading.getAllHeadings())
			{
				Cell c = mc.map.getAdjacentCell(actual, h);
				if (c != null && !closed.contains(c) && !c.getWall(h.right().right().getValue()).exist() && !c.isBlack)
				{
					c.weight = actual.weight;
					
					if (c.isVisited)
						c.weight += SEEN_WEIGHT;
					for (Movement move : getRelativeMovement(h ,actual.dir))
					{
						switch (move){
							case FRONT:
								c.weight += FORWARD_WEIGHT;
								break;
							case RIGHT:
								c.weight += RIGHT_WEIGHT;
								break;
							case BACK:
								c.weight += BACKWARD_WEIGHT;
								break;
							case LEFT:
								c.weight += LEFT_WEIGHT;
								break;
							}
						}
					
					int walls = c.getExistingWalls().size();
					if (walls <= 3)
						c.weight += (3 - walls) * DEX_NEIGHBOR_WALLS_WEIGHT;
					
					int visited_adjacent = mc.map.getVisitedAdjacentCells(c).size();
					c.weight += (4 - visited_adjacent) * DEX_NEIGHBOR_VISITED_WEIGHT;
					
					c.dir = h;
					c.from_dir = h;
					c.from = actual;
				}
			}
			
			closed.add(actual);
		}
		return false;
	}
	
}
